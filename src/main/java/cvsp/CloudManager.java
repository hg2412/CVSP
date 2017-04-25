package cvsp;

/**
 * A singleton class to manage cloud resources
 * Created by haoxiang on 4/10/17.
 */
import com.google.cloud.compute.*;
import com.google.cloud.compute.Instance;
import com.google.cloud.compute.AttachedDisk;
import com.google.cloud.compute.Compute;
import com.google.cloud.compute.ComputeOptions;
import com.google.cloud.compute.ImageId;
import com.google.cloud.compute.Instance;
import com.google.cloud.compute.InstanceId;
import com.google.cloud.compute.InstanceInfo;
import com.google.cloud.compute.MachineTypeId;
import com.google.cloud.compute.NetworkId;
import com.google.cloud.compute.NetworkInterface.AccessConfig;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;


public class CloudManager {
    private static CloudManager ourInstance;
    /**
     * startup script to run on instances
     */
    private static String startupSrcipt= "";

    static{
        try {
            ourInstance = new CloudManager();
            startupSrcipt = getStartupScript("CVSP.sh");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Compute compute;

    public static CloudManager getInstance() {
        return ourInstance;
    }

    /**
     * check credential
     * @throws IOException
     */
    private CloudManager() throws IOException {
        if (System.getenv("GOOGLE_APPLICATION_CREDENTIALS") == null){
            System.out.println("No credential detected! For more information: https://developers.google.com/identity/protocols/application-default-credentials");
        }else{
            compute = ComputeOptions.getDefaultInstance().getService();
        }

    }

    /**
     * create external ip
     * @param name
     * @return
     * @throws TimeoutException
     * @throws InterruptedException
     */

    public RegionAddressId createExternalIP(String name) throws TimeoutException, InterruptedException {
        // Create an external region address
        RegionAddressId addressId = RegionAddressId.of("us-east1", name);
        Operation operation = compute.create(AddressInfo.of(addressId));
        // Wait for operation to complete
        operation = operation.waitFor();
        if (operation.getErrors() == null) {
            System.out.println("Address " + addressId + " was successfully created");
        } else {
            // inspect operation.getErrors()
            throw new RuntimeException("Address creation failed");
        }
        return addressId;
    }

    /**
     * create all instances
     * @param number
     * @param instanceName
     * @param shouldWait
     * @return
     * @throws TimeoutException
     * @throws InterruptedException
     */
    public List<InstanceId> createInstances(int number, String instanceName, boolean shouldWait, boolean staticIp) throws TimeoutException, InterruptedException {
        ImageId imageId = ImageId.of("ubuntu-os-cloud", "ubuntu-1604-xenial-v20170330");
        NetworkId networkId = NetworkId.of("default");

        AttachedDisk attachedDisk = AttachedDisk.of(AttachedDisk.CreateDiskConfiguration.newBuilder(imageId).setAutoDelete(true).build());;
        MachineTypeId machineTypeId = MachineTypeId.of("us-east1-c", "g1-small");

        LinkedList<InstanceId> instanceIds = new LinkedList<InstanceId>();
        LinkedList<Operation> operations = new LinkedList<Operation>();

        for(int i = 0; i < number; i++){
            InstanceId instanceId = InstanceId.of("us-east1-c", instanceName.toLowerCase() + i);
            HashMap<String, String> metaDataMap = new HashMap<String, String>();
            NetworkInterface networkInterface = null;
            String ipAddress = "YOUR_PUBLIC_IP";
            if (!staticIp) {
                networkInterface = NetworkInterface.newBuilder(networkId).
                        setAccessConfigurations(AccessConfig.newBuilder()
                                .setName("external-nat")
                                .build())
                        .build();
            }else{
                Address externalIp = compute.getAddress(createExternalIP(instanceName.toLowerCase() + i));
                AccessConfig accessConfig = AccessConfig.of(externalIp.getAddress());
                ipAddress = AccessConfig.of(externalIp.getAddress()).getNatIp();
                System.out.println("Static IP: " + ipAddress);
                networkInterface = NetworkInterface.newBuilder(networkId)
                        .setAccessConfigurations(accessConfig)
                        .build();

            }
            metaDataMap.put("startup-script", startupSrcipt.replace("YOUR_PUBLIC_IP", ipAddress));
            Metadata metadata = Metadata.of(metaDataMap);
            InstanceInfo instanceInfo = InstanceInfo.newBuilder(instanceId, machineTypeId).setDescription("CVSP")
                    .setAttachedDisks(attachedDisk)
                    .setNetworkInterfaces(networkInterface)
                    .setMetadata(metadata)
                    .build();
            Operation operation = compute.create(instanceInfo);
            instanceIds.add(instanceId);
            operations.add(operation);
        }
        if (shouldWait){
            for(int i = 0; i < number; i++){
                Operation operation = operations.get(i);
                operation.waitFor();
                System.out.println("Create Instance ");
                if (operation.getErrors() == null){
                    if (operation.getErrors() == null) {
                        System.out.printf("Instance %s was created%n", instanceIds.get(i));
                    } else {
                        System.out.printf("Creation of instance %s failed%n", instanceIds.get(i));
                        System.out.printf("Error: %s%n", operation.getErrors());
                    }
                }
            }
        }

        return instanceIds;
    }


    /**
     * list all instances status
     * @param zone
     */
    public void listInstances(ZoneId zone){
        Iterator<Instance> instanceIterator;
        System.out.println("List Instances:");
        if (zone != null) {
            instanceIterator = compute.listInstances(zone.getZone()).iterateAll();
        } else {
            instanceIterator = compute.listInstances().iterateAll();
        }
        while (instanceIterator.hasNext()) {
            System.out.println(instanceIterator.next());
        }

    }


    /**
     * list all instances status
     * @param zone
     */
    public List<InstanceId> getInstanceIds(ZoneId zone){
        Iterator<Instance> instanceIterator;
        LinkedList<InstanceId> instanceIds = new LinkedList<InstanceId>();
        System.out.println("List Instances:");
        if (zone != null) {
            instanceIterator = compute.listInstances(zone.getZone()).iterateAll();
        } else {
            instanceIterator = compute.listInstances().iterateAll();
        }
        while (instanceIterator.hasNext()) {
            //System.out.println(instanceIterator.next());
            instanceIds.add(instanceIterator.next().getInstanceId());
        }
        return instanceIds;

    }
    /**
     * list public ip of all instances
     */
    public void listPublicIpAddresses(){
        Iterator<Instance> instanceIterator;
        System.out.println("List Instances Ip Addresses:");
        instanceIterator = compute.listInstances().iterateAll();
        while (instanceIterator.hasNext()) {
            System.out.println(instanceIterator.next().getNetworkInterfaces().get(0).getAccessConfigurations().get(0).getNatIp());
        }

    }





    /**
     * stop all instances
     * @param instanceIds
     */
    public void shutdownInstances(List<InstanceId> instanceIds){
        for(InstanceId instanceId : instanceIds){
            compute.getInstance(instanceId).stop();
        }
    }


    /**
     * stop all instances
     * @param instanceIds
     */
    public void deleteInstances(List<InstanceId> instanceIds){
        for(InstanceId instanceId : instanceIds){
            System.out.println("Deleting Instance" + instanceId);
            compute.getInstance(instanceId).delete();
        }
    }

    public static String getStartupScript(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();
        while (line != null) {
            sb.append(line + "\n");
            line = br.readLine();
        }

        return sb.toString();
    }


    public static void main(String[] args) throws TimeoutException, InterruptedException {


        CloudManager cloudManager = CloudManager.getInstance();
//        cloudManager.listInstances(null);
        List<InstanceId> instanceIds = cloudManager.createInstances(1, "cvsp", true, true);
        cloudManager.listPublicIpAddresses();
//        //System.out.println(startupSrcipt);

        cloudManager.listInstances(null);
        //cloudManager.shutdownInstances(instanceIds);
        //

//        cloudManager.deleteInstances(cloudManager.getInstanceIds(null));
    }
}
