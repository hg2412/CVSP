package cvsp;

import java.util.Queue;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.Date;

public class SingleTaskScheduler {
    public int numInstances; // number of instances, M in the paper
    private Queue<Job> waitingJobs; // a queue of jobs
    private PriorityQueue<Job> runningJobs; // an array of lists
    private static Date currentTime; // currentTime of the scheduler


    public static Date getCurrentTime() {
        return currentTime;
    }

    public static void setCurrentTime(Date currentTime) {
        SingleTaskScheduler.currentTime = currentTime;
    }

    public class JobComparator implements Comparator<Job> {

        public int compare(Job j1, Job j2) {
            return j1.end.compareTo(j2.end);
        }
    }

    public int totalRuntime;
    public int totalIdleTime;

    /**
     * given number of instances, initialize running jobs and waiting jobs of CVSP
     */
    public SingleTaskScheduler(int numInstances) {
        this.numInstances = numInstances;
        this.runningJobs = new PriorityQueue<Job>(numInstances, new JobComparator());
        this.waitingJobs = new LinkedList<Job>();
    }

    /**
     * assign waiting jobs to available instances
     *
     * @return
     */
    public boolean assignJob() {
        if (waitingJobs.isEmpty())
            return false;
        while (waitingJobs.size() > 0 && runningJobs.size() < numInstances) {
            Job job = waitingJobs.poll();
            job.waitTime = (int) ((long) currentTime.getTime() - (long) job.start.getTime()) / 1000;
            job.end = new Date();
            job.end.setTime(currentTime.getTime() + (long) job.runTime * 1000);
            runningJobs.add(job);
        }
        return true;
    }

    /**
     * run jobs which ends before the argument end time
     *
     * @param end: end time
     * @return list of completed jobs
     */
    public LinkedList<Job> runJobs(Date end) {
        if (runningJobs.isEmpty())
            return null;
        LinkedList<Job> removedJobs = new LinkedList<Job>();
        while (!runningJobs.isEmpty() && runningJobs.peek().end.compareTo(end) < 0) {
            System.out.println("Job completed!");
            Job removedJob = runningJobs.poll();
            // update current time
            SingleTaskScheduler.currentTime.setTime(removedJob.end.getTime());
            removedJobs.add(removedJob);
            this.assignJob();
        }
        return removedJobs;
    }

    /**
     * run jobs until all jobs in the scheduler are completed
     *
     * @return removedJobs: a linkedlist of completed jobs; null if no job are
     * in the system
     */
    public LinkedList<Job> runJobs() {
        if (waitingJobs.isEmpty() && runningJobs.isEmpty())
            return null;
        LinkedList<Job> removedJobs = new LinkedList<Job>();
        assignJob();
        while (!runningJobs.isEmpty()) {
            System.out.println("Job completed!");
            Job removedJob = runningJobs.poll();
            // update current time
            SingleTaskScheduler.currentTime.setTime(removedJob.end.getTime());
            removedJobs.add(removedJob);
            assignJob();
        }
        return removedJobs;
    }

    /**
     * add a job to waiting queue
     *
     * @param job
     * @return true if success
     */
    public boolean addJob(Job job) {
        job.start = (Date) currentTime.clone();
        System.out.println("Job start time:" + job.start.toString());
        waitingJobs.add(job);
        return true;
    }

    /**
     * remove completed jobs in the running jobs queue
     *
     * @return list of removed jobs
     */
    public LinkedList<Job> removeCompletedJobs() {
        if (runningJobs.isEmpty())
            return null;
        LinkedList<Job> removedJobs = new LinkedList<Job>();
        while (!runningJobs.isEmpty() && runningJobs.peek().end.compareTo(currentTime) < 0) {
            removedJobs.add(runningJobs.poll());
        }
        return removedJobs;
    }

    /**
     * remove running jobs in the system
     *
     * @return list of removed jobs
     */
    public LinkedList<Job> removeRunningJobs() {
        if (runningJobs.isEmpty())
            return null;
        LinkedList<Job> removedJobs = new LinkedList<Job>();
        while (!runningJobs.isEmpty()) {
            removedJobs.add(runningJobs.poll());
        }
        return removedJobs;
    }

    /**
     * print jobs in waiting queue and running queue
     */
    public void printJobs() {
        for (Job j : waitingJobs)
            System.out.println("job x is waiting");
        for (Job j : runningJobs)
            System.out.println("job x is running");
    }
}
