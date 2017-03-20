package cvsp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class Job {
	public Date start; // start time
	public Date end;  //end time
    public int jobId;
    public int userId;
    public int runTime;  // in seconds
    public int waitTime; // in seconds
	public int numTasks; // number of tasks, N in paper
    public int completedTasks;
    public ArrayList<Task> tasks;
    public HashMap<Integer, Task> tasksHashMap = new HashMap<Integer, Task>();

    /**
     * constructor of job without setting userId, create a list and a map of tasks
     * @param jobId
     * @param numTasks
     * @param runtime
     */
    public Job(int jobId, int numTasks, int runtime){
        this.numTasks = numTasks;
        this.completedTasks = 0;
        this.runTime = runtime;
        this.tasks = new ArrayList<Task>();
        this.jobId = jobId;
        // create N tasks, task id = 0, 1, ..., N - 1
        for(int i = 0; i < numTasks; i++){
            Task task = new Task(this.jobId, i, this.runTime);
            tasks.add(task);
            tasksHashMap.put(i,task);
        }
    }

    /**
     * constructor of job, create a list and a map of tasks
     * @param userId
     * @param jobId
     * @param numTasks
     * @param runtime
     */
    public Job(int userId, int jobId, int numTasks, int runtime){
        this.userId = userId;
        this.numTasks = numTasks;
        this.runTime = runtime;
        this.tasks = new ArrayList<Task>();
        this.jobId = jobId;
        // create N tasks, task id = 0, 1, ..., N - 1
        for(int i = 0; i < numTasks; i++){
            Task task = new Task(this.userId, this.jobId, i, this.runTime);
            tasks.add(task);
            tasksHashMap.put(i,task);
        }
    }

}
