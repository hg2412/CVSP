package cvsp;

import com.google.cloud.compute.InstanceId;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import cvsp.models.ParetoRuntime;
import cvsp.models.PoissonArrival;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.net.URLEncoder;

/**
 * Created by Haoxiang on 5/10/17.
 */
public class HadoopSimulator {

    public static void main(String[] args) throws InterruptedException, IOException, TimeoutException {
        simulate(1, 2, 2, new PoissonArrival(10));
    }

    public static void simulate(int numHours, int numTasks, int numInstances, PoissonArrival poissonArrival) throws TimeoutException, InterruptedException, IOException {
        //sync time with scheduler
        int jobCount = 0;
        double time = 0;
        CloudManager cloudManager = CloudManager.getInstance();
//        cloudManager.listInstances(null);
        cloudManager.createInstances(numInstances, "cvsp", true, true);
        cloudManager.listInstances(null);
        List<HadoopInstance> hadoopInstances = cloudManager.getHadoopInstances();

        while (time < numHours) {
            double arrivalTime = poissonArrival.getNextArrivalTime();
            long timestamp = System.currentTimeMillis();
            while (true) {
                if ((System.currentTimeMillis() - timestamp) * 1.0 / 3600000 >= arrivalTime) break;
                Thread.sleep(10000);
                for (HadoopInstance instance : hadoopInstances) {
                    Task t = instance.updateRunningTask();
                    if (t != null) Simulator.writeTaskToCSV("hadoop simulation.csv", t);
                }
            }
            System.out.println("New job arrived!");

            Job job = new Job(jobCount, numTasks, 0);
            for (Task t : job.tasks) {
                t.submitTime = new Date();
                t.status = Task.Status.submitted;
                Random generator = new Random();
                int i = generator.nextInt(numInstances);
                hadoopInstances.get(i).addTask(t);
            }
            time += arrivalTime;
            jobCount++;
        }

        //cloudManager.deleteInstances(null);

    }

}
