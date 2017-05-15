package cvsp;

import com.google.cloud.compute.InstanceId;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * Created by Haoxiang on 5/12/17.
 */
public class HadoopInstance extends Instance{

    public String publicIp;

    public HadoopInstance(int instanceId, String publicIp) {
        super(instanceId);
        this.publicIp = publicIp;
    }




    public static Map<String, String> sendRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        InputStream input = url.openStream();
        Map<String, String> map = new Gson().fromJson(new InputStreamReader(input, "UTF-8"), new TypeToken<Map<String, String>>(){}.getType());
        return map;
    }


    public static void submitWordCountJob(String publicIp) throws IOException {
        String cmd = "gsutil rm -r gs://cvsp/output-$PUBLIC_IP; hadoop jar $HADOOP_PREFIX/share/hadoop/mapreduce/hadoop-mapreduce-examples-2.7.3.jar wordcount gs://cvsp/randomtexts/part-m-00000 gs://cvsp/output-$PUBLIC_IP &";
        Map<String, String> response = sendRequest("http://" + publicIp + ":5000/sh2?cmd=" + URLEncoder.encode(cmd));
        for(String key:response.keySet()){
            System.out.println(key + ": " + response.get(key));
        }

    }

    public static boolean isInstanceAvailable(String publicIp) throws IOException {
        String cmd = "hadoop job -list";
        Map<String, String> response = sendRequest("http://" + publicIp + ":5000/sh?cmd=" + URLEncoder.encode(cmd));
        for(String key:response.keySet()){
            System.out.println(key + ": " + response.get(key));
        }

        if (response.get("stdout").contains("Total jobs:0")) return true;
        else return false;
    }

    public static String getJobList(String publicIp) throws IOException {
        String cmd = "hadoop job -list all";
        Map<String, String> response = sendRequest("http://" + publicIp + ":5000/sh?cmd=" + URLEncoder.encode(cmd));
        for(String key:response.keySet()){
            System.out.println(key + ": " + response.get(key));
        }
        return response.get("stdout");
    }


    public static String getCompletedJob(String publicIp) throws IOException {
        String result = getJobList(publicIp);
        String[] lines = result.split("\n");
        return lines[lines.length - 1];
    }

    /**
     * Get if the instance is available
     *
     * @return true or false
     */
    @Override
    public boolean isAvailable() {
        if (runningTask == null)
            return true;
        else
            return false;
    }

    public void addTask(Task t){
        if (runningTask != null) {// the instance is busy
            waitingTasks.offer(t);
        } else {// the instance is available
            runningTask = t;
            try {
                submitWordCountJob(this.publicIp);
            } catch (IOException e) {
                e.printStackTrace();
            }
            t.startTime = new Date();
        }
    }

    public Task updateRunningTask() throws IOException {
        Task temp = null;

        if (runningTask != null && isInstanceAvailable(this.publicIp)){
            temp = runningTask;
            temp.endTime = new Date();
            this.runningTask = null;
        }

        if (runningTask == null && waitingTasks.size() > 0){
            Task t = waitingTasks.poll();
            try {
                runningTask = t;
                submitWordCountJob(this.publicIp);
            } catch (IOException e) {
                e.printStackTrace();
            }
            t.startTime = new Date();
        }
        return temp;
    }

    public static void main(String[] args) throws IOException {
        String publicIp = "104.196.213.113";
        submitWordCountJob(publicIp);
    }

}
