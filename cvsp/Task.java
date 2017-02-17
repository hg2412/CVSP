package cvsp;

import java.util.Date;
import java.math.*;

/**
 * Created by Haoxiang on 2/16/17.
 */

public class Task {
    public Date submitTime;
    public Date startTime;
    public Date endTime;
    public int jobId;
    public int taskId;
    public int waitTime = 0;
    public int runTime = 0;

    public Task(int jobId, int taskId, int runTime){
        this.jobId = jobId;
        this.taskId = taskId;
        this.runTime = runTime;
    }

    public void updateWaitTime(){
        this.waitTime = (int)Math.round((this.startTime.getTime() - this.submitTime.getTime())/1000);
    }
}
