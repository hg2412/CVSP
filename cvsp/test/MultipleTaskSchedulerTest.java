package cvsp.test;

import static org.junit.Assert.*;
import cvsp.*;
import org.junit.Test;

import java.util.Date;

/**
 * Created by Haoxiang on 3/4/17.
 */



public class MultipleTaskSchedulerTest {
    @Test
    public void runTasks() throws Exception {
        MultipleTaskScheduler scheduler = new MultipleTaskScheduler(3);
        Date startTime = new Date();
        Date currentTime = new Date(startTime.getTime());
        scheduler.setCurrentTime(startTime);
        scheduler.setStartTime(startTime);

        scheduler.printInstances();

        // three job with 1 tasks and runtime 60 seconds arrives
        Job job1 = new Job(1, 1, 60);
        scheduler.addJob(job1, MultipleTaskScheduler.SchedulePolicy.random);
        Job job2 = new Job(2, 1, 60);
        scheduler.addJob(job2, MultipleTaskScheduler.SchedulePolicy.leastBusiest);
        Job job3 = new Job(3, 1, 60);
        scheduler.addJob(job3, MultipleTaskScheduler.SchedulePolicy.leastBusiest);

        scheduler.printInstances();
        // after 30 seconds, the fourth job arrives
        scheduler.fastForwardTimeBySeconds(30);
        Job job4 = new Job(4, 1, 30);
        scheduler.addJob(job4, MultipleTaskScheduler.SchedulePolicy.leastBusiest);
        scheduler.runTasks(currentTime);
        scheduler.printInstances();

        // after 1 minutes, first three jobs should be completed
        scheduler.fastForwardTimeBySeconds(30);
        scheduler.runCurrentTasks();
        assert(scheduler.getTotalRuntime() == 180);
        assert(Math.abs(scheduler.getUtilizationRate() - 1) < 0.01);

        // after 30 seconds, job 4 completed
        scheduler.fastForwardTimeBySeconds(30);
        scheduler.runCurrentTasks();
        scheduler.printInstances();
        System.out.println(scheduler.getTotalWaittime());
        assert(scheduler.getTotalWaittime() == 30);
        assert(scheduler.getTotalRuntime() == 210);
        assert(Math.abs(scheduler.getUtilizationRate() - (7d/9d)) < 0.01);

    }
}