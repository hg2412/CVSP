package cvsp;

import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Haoxiang on 2/16/17.
 */
public class Instance {
    public int instanceId;
    private Queue<Task> waitingTasks; // a queue of jobs
    private Task runningTask; //current running job

    public void addTask(Task t){
        if (runningTask != null){// the instance is busy
            waitingTasks.offer(t);
        }else{// the instance is available
            runningTask = t;
            t.startTime = (Date) MultipleTaskScheduler.getCurrentTime().clone();
            t.endTime = new Date();
            runningTask.updateWaitTime();
            t.endTime.setTime(t.startTime.getTime() + t.runTime * 1000);
        }
    }

    public LinkedList<Task> runTasks(Date time){
        LinkedList<Task> completedTasks = new LinkedList<Task>();
        while(runningTask != null && runningTask.endTime.before(time)){
            completedTasks.add(runningTask);
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
        return completedTasks;
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

    public int getTotalRuntime(){
        int total = 0;

        if (runningTask == null)
            return 0;
        else
            total += runningTask.runTime;

        for(Task t:waitingTasks)
            total += t.runTime;
        return total;
    }

    public Instance(int instanceId){
        this.instanceId = instanceId;
        waitingTasks = new LinkedList<Task>();
    }
}
