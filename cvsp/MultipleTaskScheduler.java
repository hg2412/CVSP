package cvsp;

import java.util.*;

/**
 * The task scheduler with multiple instances accepting jobs with multiple tasks
 *
 * To support simulation, the scheduler has its own timer
 * You can use getCurrentTime and setCurrentTime to change the time of the scheduler
 * You can also use fastForwardTimeByHours or fastForwardTimeBySeconds to adjust the time of the scheduler
 *
 * The scheduler also supports two policies to assign tasks to instances
 *
 */
public class MultipleTaskScheduler {
    public int numInstances; //number of instance
    public ArrayList<Instance> instances; // list of instances
    private Date startTime; // start time of the simulation
    private Date currentTime; // currentTime of the scheduler
    double totalRuntime; //total runtime since start of simulation
    double totalWaitTime;  //total wait time since the start of simulation

    /**
     * two scheduling policy to assign tasks
     * random - assign task to a random instance
     * leastBusiest - assign task to the least busiest instance
     */
    public enum SchedulePolicy{random, leastBusiest};
    private SchedulePolicy policy;

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
     * get current time of the scheduler
     * @return
     */
    public Date getCurrentTime() {
        return (Date) this.currentTime.clone();
    }

    /**
     * set the current time of the scheduler
     * @param currentTime
     */
    public void setCurrentTime(Date currentTime) {
        if (this.currentTime == null)
            this.currentTime = new Date(currentTime.getTime());
        else
            this.currentTime.setTime(currentTime.getTime());
    }

    /**
     * get the start time of the scheduler
     * @return
     */
    public Date getStartTime() {
        return (Date) startTime.clone();
    }

    /**
     * set the starttime of the scheduler
     * @param startTime
     */
    public void setStartTime(Date startTime)
    {
        if (this.startTime == null)
            this.startTime = new Date(startTime.getTime());
        else
            this.startTime.setTime(startTime.getTime());
    }



    /**
     * assign a job's tasks according to given policy
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
     * @return linkedlist of completed tasks
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
     * @return linkedlist of completed tasks
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
     * @return linkedlist of completed tasks
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

    /**
     * get the idle to runtime ratio of the scheduler
     * @return
     */
    public double getIdleRatio(){
        return 1/getUtilizationRate() - 1;
    }

    /**
     * get the utilization rate of the scheduler
     * @return
     */
    public double getUtilizationRate(){
        long diff = (this.currentTime.getTime() - this.startTime.getTime());
        double totalTime = diff / 1000 * this.numInstances;
        if (totalTime < 1e-6) return 0;
        return totalRuntime / totalTime;
    }


    /**
     * get total runtime of all instances
     * @return total runtime in seconds
     */
    public double getTotalRuntime(){
        return totalRuntime;
    }
    public double getTotalWaittime(){
        return totalWaitTime;
    }

    /**
     * print status of instances
     */
    public void printInstances(){
        System.out.println("\nStartTime: " + startTime);
        System.out.println("CurrentTime" + currentTime);
        for(Instance instance:instances){
            instance.printInstance();
        }
    }

    /**
     * fast forward the current time of the system by seconds
     * @param seconds
     */
    public void fastForwardTimeBySeconds(int seconds){
        this.currentTime.setTime(currentTime.getTime() + (long)seconds * 1000);
    }

    /**
     * fast forward the current time of the system by hours
     * @param hours
     */
    public void fastForwardTimeByHours(double hours){
        this.currentTime.setTime(currentTime.getTime() + (long)Math.ceil(hours * 3600 * 1000));
    }

    /**
     * get current runtime to waitime ratio of the system
     * @return
     */
    public double getRuntimeWaittimeRatio(){
        if (totalWaitTime < 1e-10) return (double)Integer.MAX_VALUE;
        return totalRuntime/ totalWaitTime;
    }

}
