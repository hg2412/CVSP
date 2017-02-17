package cvsp;

import java.util.*;

/**
 * Created by Haoxiang on 2/16/17.
 * The task scheduler with multiple instances accepting jobs with multiple tasks
 */
public class MultipleTaskScheduler {
    public int numInstances;
    public ArrayList<Instance> instances;
    private static Date currentTime; // currentTime of the scheduler

    public static Date getCurrentTime() {
        return MultipleTaskScheduler.currentTime;
    }

    public static void setCurrentTime(Date currentTime) {
        MultipleTaskScheduler.currentTime = currentTime;
    }

    /**
     * Constructor, create M instances for the scheduler
     * @param numInstances
     */
    public MultipleTaskScheduler(int numInstances){
        this.numInstances = numInstances;
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
    public boolean addJob(Job job) {
        for(Task t:job.tasks){
            t.submitTime = (Date) MultipleTaskScheduler.currentTime.clone();
            Instance instance = findLeastBusiestInstance();
            instance.addTask(t);
        }
        return true;
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
        return completedTasks;
    }

    /**
     * run all tasks remaining in the system
     * @return
     */
    public LinkedList<Task> runTasks(){
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
            int total = instance.getTotalRuntime();
            if (total < minTotalRuntime){
                minTotalRuntime = total;
                bestInstance = instance;
            }
        }
        return bestInstance;
    }
}
