package cvsp;
import java.util.ArrayList;
import java.util.Date;


public class Job {
	public Date start;
	public Date end;
    public int jobId;
	public static int numJobs = 0;
    public int runTime;  // in seconds
    public int waitTime; // in seconds
	public int numTasks; // number of tasks, N in paper
    public ArrayList<Task> tasks;

    public Job(int numTasks, int runtime){
        this.numTasks = numTasks;
        this.runTime = runtime;
        this.tasks = new ArrayList<Task>();
        Job.numJobs++;
        this.jobId = numJobs;
        // create N tasks, task id = 0, 1, ..., N - 1
        for(int i = 0; i < numTasks; i++){
            tasks.add(new Task(this.jobId, i, this.runTime));
        }
    }
}
