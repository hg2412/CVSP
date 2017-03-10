package cvsp;

import java.util.*;

/**
 * Created by Haoxiang on 2/16/17.
 * The task scheduler with multiple instances accepting jobs with multiple tasks
 */

public class MultipleTaskScheduler {
    public int numInstances;
    public ArrayList<Instance> instances;

    private Date startTime;
    private Date currentTime; // currentTime of the scheduler
    double totalRuntime;
    double totalWaitTime;
    public enum SchedulePolicy{random, leastBusiest};
    private SchedulePolicy policy;

    public Date getCurrentTime() {
        return (Date) this.currentTime.clone();
    }

    public void setCurrentTime(Date currentTime) {
        if (this.currentTime == null)
            this.currentTime = new Date(currentTime.getTime());
        else
            this.currentTime.setTime(currentTime.getTime());
    }

    public Date getStartTime() {
        return (Date) startTime.clone();
    }

    public void setStartTime(Date startTime)
    {
        if (this.startTime == null)
            this.startTime = new Date(startTime.getTime());
        else
            this.startTime.setTime(startTime.getTime());
    }

    /**
     * Constructor, create M instances for the scheduler
     * @param numInstances
     */
    public MultipleTaskScheduler(int numInstances){
        this.numInstances = numInstances;
        totalRuntime = 0;
        totalWaitTime = 0;
        instances = new ArrayList<Instance>();
        // create M instances
        for(int i = 0; i < numInstances; i++){
            Instance instance = new Instance(i);
            instances.add(instance);
        }
    }

    /**
     * assign a job's tasks to least busiest instances
     * @return
     */
    public boolean addJob(Job job, SchedulePolicy policy) {
        if (policy == SchedulePolicy.leastBusiest) {
            for (Task t : job.tasks) {
                t.submitTime = (Date) this.currentTime.clone();
                t.status = Task.Status.submitted;
                Instance instance = findLeastBusiestInstance();
                instance.addTask(t, this.currentTime);
            }
            return true;
        }else{// randomly assign task to instances
            Random generator = new Random();
            for (Task t : job.tasks) {
                t.submitTime = (Date) this.currentTime.clone();
                t.status = Task.Status.submitted;
                int i = generator.nextInt(numInstances);
                instances.get(i).addTask(t, this.currentTime);
            }
            return true;
        }
    }

    /**
     * run tasks that complete before given time, and return completed jobs
     * @param time
     * @return
     */
    public LinkedList<Task> runTasks(Date time){
        LinkedList<Task> completedTasks = new LinkedList<Task>();
        for(Instance instance:instances)
            completedTasks.addAll(instance.runTasks(time));
        for (Task task: completedTasks){
            this.totalRuntime += task.runTime;
            this.totalWaitTime += task.waitTime;
        }
        return completedTasks;
    }


    /**
     * run tasks that complete before given time, and return completed jobs
     * @return
     */
    public LinkedList<Task> runCurrentTasks(){
        LinkedList<Task> completedTasks = new LinkedList<Task>();
        for(Instance instance:instances)
            completedTasks.addAll(instance.runTasks(this.currentTime));
        for (Task task: completedTasks){
            this.totalRuntime += task.runTime;
            this.totalWaitTime += task.waitTime;
            task.status = Task.Status.completed;

        }
        return completedTasks;
    }

    /**
     * run all tasks remaining in the system
     * @return
     */
    public LinkedList<Task> runAllTasks(){
        LinkedList<Task> completedTasks = new LinkedList<Task>();
        for(Instance instance:instances)
            completedTasks.addAll(instance.runTasks());
        return completedTasks;
    }

    /**
     * find the least busiest instance of the scheduler
     * @return the least busiest instance
     */
    public Instance findLeastBusiestInstance(){
        int minTotalRuntime = Integer.MAX_VALUE;
        Instance bestInstance = null;
        for(Instance instance:instances){
            if (instance.isAvailable()) {
                bestInstance = instance;
                break;
            }
            int total = instance.getWaitingJobsLength();
            if (total < minTotalRuntime){
                minTotalRuntime = total;
                bestInstance = instance;
            }
        }
        return bestInstance;
    }

    public double getIdleRatio(){
        return 1/getUtilizationRate() - 1;
    }

    public double getUtilizationRate(){
        long diff = (this.currentTime.getTime() - this.startTime.getTime());
        double totalTime = diff / 1000 * this.numInstances;
        if (totalTime < 1e-6) return 0;
        return totalRuntime / totalTime;
    }


    public double getTotalRuntime(){
        return totalRuntime;
    }
    public double getTotalWaittime(){
        return totalWaitTime;
    }

    public void printInstances(){
        System.out.println("\nStartTime: " + startTime);
        System.out.println("CurrentTime" + currentTime);
        for(Instance instance:instances){
            instance.printInstance();
        }
    }

    public void fastForwardTimeBySeconds(int seconds){
        this.currentTime.setTime(currentTime.getTime() + (long)seconds * 1000);
    }

    public void fastForwardTimeByHours(double hours){
        this.currentTime.setTime(currentTime.getTime() + (long)Math.ceil(hours * 3600 * 1000));
    }

    public double getRuntimeWaittimeRatio(){
        return totalRuntime/ totalWaitTime;
    }

}
