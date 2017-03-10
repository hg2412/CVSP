package cvsp;

import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Haoxiang on 2/16/17.
 */
public class Instance {
    public int instanceId;
    private int total;
    private Queue<Task> waitingTasks; // a queue of jobs
    private Task runningTask; //current running job

    public void addTask(Task t, Date date){
        if (runningTask != null){// the instance is busy
            waitingTasks.offer(t);
        }else{// the instance is available
            runningTask = t;
            t.startTime = (Date) date.clone();
            t.endTime = new Date();
            runningTask.updateWaitTime();
            t.endTime.setTime(t.startTime.getTime() + t.runTime * 1000);
        }
    }

    public LinkedList<Task> runTasks(Date time){
        LinkedList<Task> completedTasks = new LinkedList<Task>();
        while(runningTask != null && (!runningTask.endTime.after(time))){
            completedTasks.add(runningTask);
            runCurrentTask();
        }
        return completedTasks;
    }

    /**
     *  run all the tasks in the system
     * @return completed tasks
     */
    public LinkedList<Task> runTasks(){
        LinkedList<Task> completedTasks = new LinkedList<Task>();
        while(runningTask != null){
            completedTasks.add(runningTask);
            runCurrentTask();
        }
        return completedTasks;
    }

    public void runCurrentTask(){
        if (!waitingTasks.isEmpty()){
            waitingTasks.peek().startTime = (Date)runningTask.endTime.clone();
            runningTask = waitingTasks.poll();
            runningTask.endTime = new Date();
            runningTask.endTime.setTime(runningTask.startTime.getTime() + runningTask.runTime * 1000);
            runningTask.updateWaitTime();
        }else{
            runningTask = null;
        }
    }

    /**
     *  is the instance available
     * @return
     */
    public boolean isAvailable(){
        if (runningTask == null)
            return true;
        else
            return false;
    }

    public int getWaitingJobsLength(){
        return waitingTasks.size();
    }

    public Instance(int instanceId){
        this.instanceId = instanceId;
        waitingTasks = new LinkedList<Task>();
    }

    public void printInstance(){
        System.out.println("Instance " + instanceId);
        if (isAvailable())
            System.out.println("No running Task:");
        else{
            System.out.println("Running Task:" + runningTask);
            for(Task task:waitingTasks){
                System.out.println("Waiting Task:" + task);
            }
        }
    }
}
