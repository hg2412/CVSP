package cvsp;

import cvsp.models.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Simulator {
    private static Date currentTime;
    private static final double hourToMillisecond = 60 * 60 * 1000;
    private static final double hourToSeconds = 60 * 60;

    /**
     * simulate the single user single-task job arrivals using Possion model and Pareto model and CVSP's schedulers
     *
     * @param startDate:      starting date of the simulation
     * @param numJobs:        number of jobs to simulate
     * @param numInstances:   number of instances of CVSP
     * @param poissonArrival: Poisson Arrival Model
     * @param paretoRuntime:  Pareto Runtime Model
     */
    public static void simulateSingleUserSingleTask(Date startDate, int numJobs, int numInstances, PoissonArrival poissonArrival, ParetoRuntime paretoRuntime) {
        SingleTaskScheduler scheduler = new SingleTaskScheduler(numInstances);
        //sync time with scheduler
        currentTime = (Date) startDate.clone();
        SingleTaskScheduler.setCurrentTime(new Date(currentTime.getTime()));

        for (int i = 0; i < numJobs; i++) {
            double arrivalTime = poissonArrival.getNextArrivalTime();
            double runtime = paretoRuntime.getNextRuntime();
            System.out.println("Job " + i + " Arrival Time:" + arrivalTime + " Runtime:" + runtime);
            Date nextArrivalTime = new Date();
            nextArrivalTime.setTime((long) (currentTime.getTime() + Math.ceil(arrivalTime * hourToMillisecond)));
            // run previous jobs
            LinkedList<Job> completedJobs = scheduler.runJobs(nextArrivalTime);
            writeCompletedJobsToCSV(startDate.toString(), completedJobs);
            currentTime.setTime(nextArrivalTime.getTime());
            SingleTaskScheduler.setCurrentTime(currentTime);
            // create a new job and add to waiting jobs
            Job job = new Job( i ,1, (int) Math.ceil(runtime * hourToSeconds));
            job.start = (Date) currentTime.clone();
            scheduler.addJob(job);
            scheduler.assignJob();
        }
        // deal with remaining jobs in the system
        LinkedList<Job> remainingJobs = scheduler.runJobs();
        writeCompletedJobsToCSV(startDate.toString(), remainingJobs);
    }



    public static void simulateSingleUserMultipleTasksByJobsNum(Date startDate, int numJobs, int numTasks, int numInstances, PoissonArrival poissonArrival, ParetoRuntime paretoRuntime, boolean writeToCSV) {
        MultipleTaskScheduler scheduler = new MultipleTaskScheduler(numInstances);
        currentTime = (Date) startDate.clone();
        scheduler.setCurrentTime(new Date(currentTime.getTime()));
        scheduler.setStartTime(new Date(currentTime.getTime()));

        for (int i = 0; i < numJobs; i++) {
            double arrivalTime = poissonArrival.getNextArrivalTime();
            double runtime = paretoRuntime.getNextRuntime();
            scheduler.fastForwardTimeByHours(arrivalTime);
            LinkedList<Task> completedTasks = scheduler.runCurrentTasks();
            if (writeToCSV)
                writeCompletedTasks(startDate.toString(), completedTasks);
            // create a new job and add to waiting jobs
            Job job = new Job(i, numTasks, (int) Math.ceil(runtime * hourToSeconds));
            scheduler.addJob(job, MultipleTaskScheduler.SchedulePolicy.leastBusiest);
            System.out.println("Utilization Rate: " + scheduler.getUtilizationRate());
            System.out.println("Runtime to Waittime Ratio: " + scheduler.getRuntimeWaittimeRatio());
        }
        System.out.println("Utilization Rate: " + scheduler.getUtilizationRate());
        System.out.println("Runtime to Waittime Ratio: " + scheduler.getRuntimeWaittimeRatio());
        //run remaining jobs in the system
        LinkedList<Task> completedTasks = scheduler.runAllTasks();
        if (writeToCSV)
            writeCompletedTasks(startDate.toString(), completedTasks);

    }


    public static double simulateSingleUserMultipleTasksByHours(Date startDate, int numHours, int numTasks, int numInstances, PoissonArrival poissonArrival, ParetoRuntime paretoRuntime, boolean writeToCSV) {
        MultipleTaskScheduler scheduler = new MultipleTaskScheduler(numInstances);
        //sync time with scheduler
        currentTime = (Date) startDate.clone();
        scheduler.setCurrentTime(new Date(currentTime.getTime()));
        scheduler.setStartTime(new Date(currentTime.getTime()));
        double time = 0;
        scheduler.setCurrentTime(new Date(currentTime.getTime()));
        scheduler.setStartTime(new Date(currentTime.getTime()));
        int jobCount = 0;
        while (time < numHours) {
            double arrivalTime = poissonArrival.getNextArrivalTime();
            time += arrivalTime;
            if (time >= numHours) break;
            double runtime = paretoRuntime.getNextRuntime();
            scheduler.fastForwardTimeByHours(arrivalTime);
            // run previous jobs
            LinkedList<Task> completedTasks = scheduler.runCurrentTasks();
            if(writeToCSV)
                writeCompletedTasks(startDate.toString(), completedTasks);
            Job job = new Job(jobCount, numTasks, (int) Math.ceil(runtime * hourToSeconds));
            jobCount++;
            scheduler.addJob(job, MultipleTaskScheduler.SchedulePolicy.leastBusiest);
        }
        System.out.println();
        System.out.println("Idle Ratio: " + scheduler.getIdleRatio());
        return scheduler.getUtilizationRate();


    }

    public static void simulateMultipleUserMultipleTasksByJobsNum(Date startDate, List<User> users, int numJobs, int numInstances, ParetoRuntime paretoRuntime, boolean writeToCSV) {
        MultipleTaskScheduler scheduler = new MultipleTaskScheduler(numInstances);
        currentTime = (Date) startDate.clone();
        double arrivalRate = 0;
        for(User user:users)
            arrivalRate += user.arrivalRate;
        PoissonArrival poissonArrival = new PoissonArrival(arrivalRate);

        scheduler.setCurrentTime(new Date(currentTime.getTime()));
        scheduler.setStartTime(new Date(currentTime.getTime()));

        for (int i = 0; i < numJobs; i++) {
            double arrivalTime = poissonArrival.getNextArrivalTime();
            double runtime = paretoRuntime.getNextRuntime();
            scheduler.fastForwardTimeByHours(arrivalTime);
            LinkedList<Task> completedTasks = scheduler.runCurrentTasks();
            if (writeToCSV)
                writeCompletedTasks(startDate.toString(), completedTasks);
            // create a new job and add to waiting jobs
            Job job = User.generateJobFromMultipleUsers(users, (int)Math.ceil(runtime * hourToSeconds));
            scheduler.addJob(job, MultipleTaskScheduler.SchedulePolicy.leastBusiest);
            System.out.println("Utilization Rate: " + scheduler.getUtilizationRate());
            System.out.println("Runtime to Waittime Ratio: " + scheduler.getRuntimeWaittimeRatio());
        }
        System.out.println("Utilization Rate: " + scheduler.getUtilizationRate());
        System.out.println("Runtime to Waittime Ratio: " + scheduler.getRuntimeWaittimeRatio());
        //run remaining jobs in the system
        LinkedList<Task> completedTasks = scheduler.runAllTasks();
        if (writeToCSV)
            writeCompletedTasks(startDate.toString(), completedTasks);
    }


    public static void simulateMultipleUserMultipleTasksByHours(Date startDate, List<User> users, int numHours, int numInstances, ParetoRuntime paretoRuntime, boolean writeToCSV) {
        MultipleTaskScheduler scheduler = new MultipleTaskScheduler(numInstances);
        currentTime = (Date) startDate.clone();
        double arrivalRate = 0;
        HashMap<Integer, User> usersHashMap = new HashMap<Integer, User>();
        for(User user:users) {
            arrivalRate += user.arrivalRate;
            usersHashMap.put(user.userId, user);
        }
        PoissonArrival poissonArrival = new PoissonArrival(arrivalRate);
        scheduler.setCurrentTime(new Date(currentTime.getTime()));
        scheduler.setStartTime(new Date(currentTime.getTime()));
        double time = 0;

        while (time < numHours) {
            double arrivalTime = poissonArrival.getNextArrivalTime();
            time += arrivalTime;
            double runtime = paretoRuntime.getNextRuntime();
            scheduler.fastForwardTimeByHours(arrivalTime);
            LinkedList<Task> completedTasks = scheduler.runCurrentTasks();
            if (writeToCSV)
                writeCompletedTasks(startDate.toString(), completedTasks);
            updateUserData(usersHashMap, completedTasks);
            // create a new job and add to waiting jobs
            Job job = User.generateJobFromMultipleUsers(users, (int)Math.ceil(runtime * hourToSeconds));
            scheduler.addJob(job, MultipleTaskScheduler.SchedulePolicy.leastBusiest);

            System.out.println("Utilization Rate: " + scheduler.getUtilizationRate());
            System.out.println("Runtime to Waittime Ratio: " + scheduler.getRuntimeWaittimeRatio());

        }
        System.out.println("Utilization Rate: " + scheduler.getUtilizationRate());
        System.out.println("Runtime to Waittime Ratio: " + scheduler.getRuntimeWaittimeRatio());
        //run remaining jobs in the system
        LinkedList<Task> completedTasks = scheduler.runAllTasks();
        if (writeToCSV)
            writeCompletedTasks(startDate.toString(), completedTasks);
    }


    public static void updateUserData(HashMap<Integer, User> usersHashMap, LinkedList<Task> completedTasks){
        for(Task task:completedTasks){
            usersHashMap.get(task.userId).totalRuntime += task.runTime;
            usersHashMap.get(task.userId).totalWaittime += task.waitTime;
        }
    }

    /**
     * write job log to csv file
     *
     * @param filename
     * @param completedTasks
     * @throws IOException
     */
    public static void writeCompletedTasks(String filename, LinkedList<Task> completedTasks) {
        if (!completedTasks.isEmpty()) {
            for (Task task : completedTasks) {
                try {
                    writeTaskToCSV(filename + ".csv", task);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void writeJobToCSV(String filename, Job job) throws IOException {
        FileWriter writer = new FileWriter(filename, true);
        StringBuilder sb = new StringBuilder();
        sb.append("\"" + job.start.toString() + "\",");
        sb.append("\"" + job.end.toString() + "\", ");
        sb.append(Integer.toString(job.runTime) + ", ");
        sb.append(Integer.toString(job.waitTime) + "\n");
        System.out.print(sb.toString());
        writer.write(sb.toString());
        writer.flush();
        writer.close();
        return;
    }

    /**
     * write job log to csv file
     *
     * @param filename
     * @param task
     * @throws IOException
     */
    public static void writeTaskToCSV(String filename, Task task) throws IOException {
        FileWriter writer = new FileWriter(filename, true);
        StringBuilder sb = new StringBuilder();
        sb.append("\"" + "user" + task.userId + "\",");
        sb.append("\"" + "job" + task.jobId + "\",");
        sb.append("\"" + "task" + task.taskId + "\",");
        sb.append("\"" + task.submitTime.toString() + "\",");
        sb.append("\"" + task.startTime.toString() + "\",");
        sb.append("\"" + task.endTime.toString() + "\", ");
        sb.append(Integer.toString(task.runTime) + ", ");
        sb.append(Integer.toString(task.waitTime) + "\n");
        System.out.print(sb.toString());
        writer.write(sb.toString());
        writer.flush();
        writer.close();
        return;
    }


    public static void writeCompletedJobsToCSV(String filename, LinkedList<Job> completedJobs){
        if (completedJobs.size() > 0) {
            for (Job job : completedJobs) {
                try {
                    writeJobToCSV(filename + ".csv", job);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {

        // simulate single task, single user job
        //Simulator.simulate(new Date(), 1200, 10, new PoissonArrival(60), new ParetoRuntime(1,0.1));
        // simulate multiple task, single user job
        //Simulator.simulate(new Date(), 10, 10, 10, new PoissonArrival(60), new ParetoRuntime(0.4,0.01));
        // simulate multiple task, single user job
        //simulateSingleUserMultipleTasksByJobsNum(new Date(),1000,1,61, new PoissonArrival(60),new ParetoRuntime(2, 0.5));

        //Example: Multiple Users simulation
        LinkedList<User> users = User.generateUsers(60, 1, 1, 0);
        //simulateMultipleUserMultipleTasksByJobsNum(new Date(), users, 5000, 60, new ParetoRuntime(2, 0.5), false);
        simulateMultipleUserMultipleTasksByHours(new Date(), users, 300, 60, new ParetoRuntime(2, 0.5), false);
        //experiment();
    }

    public static void experiment() {
        int[] numInstances = {30, 40, 50, 60, 80};
        double price = 0.13;
        double[] rates = {20, 40, 60, 80, 100};
        double[][] results = new double[5][5];

        for (int i = 0; i < numInstances.length; i++) {
            for (int j = 0; j < numInstances.length; j++) {
                System.out.println("M=" + numInstances[i] + " Rates=" + rates[j]);
                double utilization = Simulator.simulateSingleUserMultipleTasksByHours(new Date(), 1000, 1, numInstances[i], new PoissonArrival(rates[j]), new ParetoRuntime(2, 0.5), false);
                results[i][j] = (price * utilization - Utility.getGCPDiscount(Math.max(1 / utilization - 1, 0)) * 0.17) * numInstances[i] * 1000;
            }
        }
        for (int i = 0; i < 5; i++) {
            int j = 0;
            for (j = 0; j < 4; j++) {
                System.out.print(results[i][j] + " ");
            }
            System.out.print(results[i][j] + "\n");
        }
    }
}
