package cvsp;

import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Class of virtual machine instance
 */
public class Instance {

    public int instanceId;
    private Queue<Task> waitingTasks; // a queue of waiting tasks
    private Task runningTask; // current running task

    /**
     * constructor of the instance, and set instance id
     *
     * @param instanceId
     */
    public Instance(int instanceId) {
        this.instanceId = instanceId;
        waitingTasks = new LinkedList<Task>();
    }

    /**
     * add a task to the instance
     * if the instance is available, add the task to be running task;
     * if the instance is busy, add the task to the waiting queue
     *
     * @param t    - task
     * @param date - start time of the task
     */
    public void addTask(Task t, Date date) {
        if (runningTask != null) {// the instance is busy
            waitingTasks.offer(t);
        } else {// the instance is available
            runningTask = t;
            t.startTime = (Date) date.clone();
            t.endTime = new Date();
            runningTask.updateWaitTime();
            t.endTime.setTime(t.startTime.getTime() + t.runTime * 1000);
        }
    }

    /**
     * Run tasks that complete before a given time
     *
     * @param time
     * @return linked list of completed jobs
     */
    public LinkedList<Task> runTasks(Date time) {
        LinkedList<Task> completedTasks = new LinkedList<Task>();
        while (runningTask != null && (!runningTask.endTime.after(time))) {
            completedTasks.add(runningTask);
            runCurrentTask();
        }
        return completedTasks;
    }

    /**
     * run all the tasks remaining in the system
     *
     * @return completed tasks
     */
    public LinkedList<Task> runTasks() {
        LinkedList<Task> completedTasks = new LinkedList<Task>();
        while (runningTask != null) {
            completedTasks.add(runningTask);
            runCurrentTask();
        }
        return completedTasks;
    }

    /**
     * run current task
     * if the running task is null, fetch a task from waiting queue to be the running task
     * else set running task to null
     */
    public void runCurrentTask() {
        if (!waitingTasks.isEmpty()) {
            waitingTasks.peek().startTime = (Date) runningTask.endTime.clone();
            runningTask = waitingTasks.poll();
            runningTask.endTime = new Date();
            runningTask.endTime.setTime(runningTask.startTime.getTime() + runningTask.runTime * 1000);
            runningTask.updateWaitTime();
        } else {
            runningTask = null;
        }
    }

    /**
     * Get if the instance is available
     *
     * @return true or false
     */
    public boolean isAvailable() {
        if (runningTask == null)
            return true;
        else
            return false;
    }

    /**
     * get the length of the waiting queue
     *
     * @return
     */
    public int getWaitingJobsLength() {
        return waitingTasks.size();
    }


    public void printInstance() {
        System.out.println("Instance " + instanceId);
        if (isAvailable())
            System.out.println("No running Task:");
        else {
            System.out.println("Running Task:" + runningTask);
            for (Task task : waitingTasks) {
                System.out.println("Waiting Task:" + task);
            }
        }
    }
}
