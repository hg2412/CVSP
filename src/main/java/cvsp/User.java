package cvsp;

import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

/**
 * Created by Haoxiang on 2/19/17.
 */
public class User {
    public String username;
    public int userId;
    public double arrivalRate;
    public int numTasks = 1;
    public double gamma = 9;
    public int totalRuntime = 0;
    public int totalWaittime = 0;
    public LinkedList<Job> jobs;
    public HashMap<Integer, Job> jobsHashMap;

    public User(String username, int userId, int numTasks, double arrivalRate, double gamma) {
        this.username = username;
        this.userId = userId;
        this.numTasks = numTasks;
        this.arrivalRate = arrivalRate;
        this.gamma = gamma;
        jobs = new LinkedList<Job>();
        jobsHashMap = new HashMap<Integer, Job>();
    }

    public void createJob(int jobId, int numTasks, int runtime) {
        Job job = new Job(jobId, numTasks, runtime);
        this.jobs.add(job);
        this.jobsHashMap.put(job.jobId, job);
    }

    public void createJob(int numTasks, int runtime) {
        Job job = new Job(userId, this.jobs.size() + 1, numTasks, runtime);
        this.jobs.add(job);
        this.jobsHashMap.put(job.jobId, job);
    }

    public Job createJob(int runtime) {
        Job job = new Job(this.userId, this.jobs.size() + 1, this.numTasks, runtime);
        this.jobs.add(job);
        this.jobsHashMap.put(job.jobId, job);
        return job;
    }

    public double getRuntimeToWaittimeRatio() {
        return totalRuntime / totalWaittime;
    }


    public static Job generateJobFromMultipleUsers(List<User> users, int runtime) {
        double totalRate = 0;
        for (User u : users)
            totalRate += u.arrivalRate;
        double cdf = 0;
        double threshold = Math.random();
        int idx = 0;
        do {
            cdf += users.get(idx).arrivalRate / totalRate;
            idx++;
        } while (idx < users.size() && cdf < threshold);
        User user = users.get(idx - 1);
        return user.createJob(runtime);
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", userId=" + userId +
                ", arrivalRate=" + arrivalRate +
                ", numTasks=" + numTasks +
                ", gamma=" + gamma +
                ", totalRuntime=" + totalRuntime +
                ", totalWaittime=" + totalWaittime +
                '}';
    }

    public static LinkedList<User> generateUsers(int numUsers, int numTasks, double arrivalRate, double gamma) {
        LinkedList<User> users = new LinkedList<User>();
        for (int i = 0; i < numUsers; i++) {
            users.add(new User(Integer.toString(i), i, numTasks, arrivalRate, gamma));
        }
        return users;
    }
}

