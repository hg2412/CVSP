package cvsp;

import cvsp.models.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Simulator contains serveral functions to simulate the CVSP scheduler
 */
public class Simulator {

    private static Date currentTime;
    private static final double hourToMillisecond = 60 * 60 * 1000;
    private static final double hourToSeconds = 60 * 60;

    /**
     * inner class to report simulation result
     */
    public static class SimulationResult{
        public double utilization;
        public double idleRatio;
        public double runtimeToWaittime;
        public double averageWaittime;
        public SimulationResult(double utilization, double idleRatio, double runtimeToWaittime, double averageWaittime) {
            this.utilization = utilization;
            this.idleRatio = idleRatio;
            this.runtimeToWaittime = runtimeToWaittime;
            this.averageWaittime = averageWaittime;
        }
    }


    public static class IdleRatioVersusTime{
        public LinkedList<String> times;
        public LinkedList<Double> idleRatios;

        public IdleRatioVersusTime(){
            times = new LinkedList<String>();
            idleRatios = new LinkedList<Double>();
        }

        public String generateCSVString(){
            StringBuilder result =  new StringBuilder();
            for(int i = 0; i < times.size(); i++){
                result.append("\"" + times.get(i) + "\"");
                result.append(",");
                result.append(idleRatios.get(i));
                result.append("\n");
            }
            return result.toString();
        }

        public void writeToCSV(String filename) throws IOException {
            FileWriter writer = new FileWriter(filename, true);
            writer.write(generateCSVString());
            writer.flush();
            writer.close();
            return;
        }

    }


    public static class IdleRatioVersusNumTasks{
        public LinkedList<Double> numTasks;
        public LinkedList<Double> idleRatios;
        public LinkedList<Double> profits;

        public IdleRatioVersusNumTasks(){
            numTasks = new LinkedList<Double>();
            idleRatios = new LinkedList<Double>();
            profits = new LinkedList<Double>();
        }

        public String generateCSVString(){
            StringBuilder result =  new StringBuilder();
            for(int i = 0; i < numTasks.size(); i++){
                result.append("\"" + (int)(double)numTasks.get(i) + "\"");
                result.append(",");
                result.append(idleRatios.get(i));
                result.append(",");
                result.append(profits.get(i));
                result.append("\n");
            }
            return result.toString();
        }

        public void writeToCSV(String filename) throws IOException {
            FileWriter writer = new FileWriter(filename, true);
            writer.write(generateCSVString());
            writer.flush();
            writer.close();
            return;
        }

    }
    /**
     * simulate the single user single-task job arrivals using Possion model and Pareto model and CVSP's schedulers
     *
     * @param startDate:      starting date of the simulation
     * @param numJobs:        number of jobs to simulate
     * @param numInstances:   number of instances of CVSP
     * @param poissonArrival: Poisson Arrival Model
     * @param paretoRuntime:  Pareto Runtime Model
     *
     *
     *  Deprecated!!!
     *
     */
    /*
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
            Job job = new Job(i, 1, (int) Math.ceil(runtime * hourToSeconds));
            job.start = (Date) currentTime.clone();
            scheduler.addJob(job);
            scheduler.assignJob();
        }
        // deal with remaining jobs in the system
        LinkedList<Job> remainingJobs = scheduler.runJobs();
        writeCompletedJobsToCSV(startDate.toString(), remainingJobs);
    }
    */


    /**
     * simulate the single user multiple-task job arrivals using Possion model and Pareto model and CVSP's schedulers
     *
     * @param startDate:      starting date of the simulation
     * @param numJobs:        number of jobs to simulate
     * @param numInstances:   number of instances of CVSP
     * @param poissonArrival: Poisson Arrival Model
     * @param paretoRuntime:  Pareto Runtime Model
     */
    public static SimulationResult simulateSingleUserMultipleTasksByJobsNum(Date startDate, int numJobs, int numTasks, int numInstances, PoissonArrival poissonArrival, ParetoRuntime paretoRuntime, boolean writeToCSV) {
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
            scheduler.addJob(job, MultipleTaskScheduler.SchedulePolicy.random);
            System.out.println("Utilization Rate: " + scheduler.getUtilizationRate());
            System.out.println("Runtime to Waittime Ratio: " + scheduler.getRuntimeWaittimeRatio());
        }
        System.out.println("Utilization Rate: " + scheduler.getUtilizationRate());
        System.out.println("Runtime to Waittime Ratio: " + scheduler.getRuntimeWaittimeRatio());
        //run remaining jobs in the system
        LinkedList<Task> completedTasks = scheduler.runAllTasks();
        if (writeToCSV)
            writeCompletedTasks(startDate.toString(), completedTasks);

        return new SimulationResult(scheduler.getUtilizationRate(), scheduler.getIdleRatio(), scheduler.getRuntimeWaittimeRatio(), scheduler.getExpectedWaittime());

    }

    /**
     * simulate the single user multiple-task job arrivals using Possion model and Pareto model and CVSP's schedulers
     *
     * @param startDate:      starting date of the simulation
     * @param numHours:       number of hours to simulate
     * @param numInstances:   number of instances of CVSP
     * @param poissonArrival: Poisson Arrival Model
     * @param paretoRuntime:  Pareto Runtime Model
     */
    public static SimulationResult simulateSingleUserMultipleTasksByHours(Date startDate, int numHours, int numTasks, int numInstances, PoissonArrival poissonArrival, ParetoRuntime paretoRuntime, boolean writeToCSV) {
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
            if (writeToCSV)
                writeCompletedTasks(startDate.toString(), completedTasks);
            Job job = new Job(jobCount, numTasks, (int) Math.ceil(runtime * hourToSeconds));
            jobCount++;
            scheduler.addJob(job, MultipleTaskScheduler.SchedulePolicy.random);
        }
        return new SimulationResult(scheduler.getUtilizationRate(), scheduler.getIdleRatio(), scheduler.getRuntimeWaittimeRatio(), scheduler.getExpectedWaittime());



    }


    /**
     * simulate the single user multiple-task job arrivals using Possion model and Pareto model and CVSP's schedulers
     *
     * @param startDate:      starting date of the simulation
     * @param numHours:       number of hours to simulate
     * @param numInstances:   number of instances of CVSP
     * @param poissonArrival: Poisson Arrival Model
     * @param paretoRuntime:  Pareto Runtime Model
     */
    public static SimulationResult simulateSingleUserRandomMultipleTasksByHours(Date startDate, int numHours, NumTasksDistribution numTasksDist, int numInstances, PoissonArrival poissonArrival, ParetoRuntime paretoRuntime, boolean writeToCSV) throws IOException {
        MultipleTaskScheduler scheduler = new MultipleTaskScheduler(numInstances);
        //sync time with scheduler
        currentTime = (Date) startDate.clone();
        scheduler.setCurrentTime(new Date(currentTime.getTime()));
        scheduler.setStartTime(new Date(currentTime.getTime()));
        double time = 0;
        scheduler.setCurrentTime(new Date(currentTime.getTime()));
        scheduler.setStartTime(new Date(currentTime.getTime()));
        int jobCount = 0;

        IdleRatioVersusTime idleRatioVersusTime = new IdleRatioVersusTime();

        while (time < numHours) {
            double arrivalTime = poissonArrival.getNextArrivalTime();
            time += arrivalTime;
            if (time >= numHours) break;
            double runtime = paretoRuntime.getNextRuntime();
            scheduler.fastForwardTimeByHours(arrivalTime);
            // run previous jobs
            LinkedList<Task> completedTasks = scheduler.runCurrentTasks();
            if (writeToCSV)
                writeCompletedTasks(startDate.toString(), completedTasks);
            Job job = new Job(jobCount, numTasksDist.getNextNumOfTasks(), (int) Math.ceil(runtime * hourToSeconds));
            jobCount++;
            scheduler.addJob(job, MultipleTaskScheduler.SchedulePolicy.random);

            //record system idle ratio
            if (writeToCSV) {
                if (jobCount % 10 == 0) {
                    idleRatioVersusTime.times.add(scheduler.getCurrentTime().toString());
                    idleRatioVersusTime.idleRatios.add(scheduler.getIdleRatio());
                }
            }
        }
        //output idle ratio
        if (writeToCSV) idleRatioVersusTime.writeToCSV("IdleRatioVersusTime M=" + numInstances + " N=" + numTasksDist.getMeanNumOfTasks() + " Rate=" + poissonArrival.getRate() + ".csv");
        return new SimulationResult(scheduler.getUtilizationRate(), scheduler.getIdleRatio(), scheduler.getRuntimeWaittimeRatio(), scheduler.getExpectedWaittime());
    }


    /**
     * simulate multiple user multiple-task job arrivals using Possion model and Pareto model and CVSP's schedulers.
     *
     * @param startDate     starting date of the simulation
     * @param users         linkedlist of users
     * @param numJobs       number of jobs to simulate
     * @param numInstances  number of instances
     * @param paretoRuntime pareto runtime distribution
     * @param writeToCSV
     */
    public static SimulationResult simulateMultipleUserMultipleTasksByJobsNum(Date startDate, List<User> users, int numJobs, int numInstances, ParetoRuntime paretoRuntime, boolean writeToCSV) {
        MultipleTaskScheduler scheduler = new MultipleTaskScheduler(numInstances);
        currentTime = (Date) startDate.clone();
        double arrivalRate = 0;
        for (User user : users)
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
            Job job = User.generateJobFromMultipleUsers(users, (int) Math.ceil(runtime * hourToSeconds));
            scheduler.addJob(job, MultipleTaskScheduler.SchedulePolicy.random);
            System.out.println("Utilization Rate: " + scheduler.getUtilizationRate());
            System.out.println("Runtime to Waittime Ratio: " + scheduler.getRuntimeWaittimeRatio());
        }
        System.out.println("Utilization Rate: " + scheduler.getUtilizationRate());
        System.out.println("Runtime to Waittime Ratio: " + scheduler.getRuntimeWaittimeRatio());
        //run remaining jobs in the system
        LinkedList<Task> completedTasks = scheduler.runAllTasks();
        if (writeToCSV)
            writeCompletedTasks(startDate.toString(), completedTasks);

        return new SimulationResult(scheduler.getUtilizationRate(), scheduler.getIdleRatio(), scheduler.getRuntimeWaittimeRatio(), scheduler.getExpectedWaittime());

    }

    /**
     * simulate the multiple user multiple-task job arrivals using Possion model and Pareto model and CVSP's schedulers
     *
     * @param startDate:     starting date of the simulation
     * @param numHours:      number of hours to simulate
     * @param numInstances:  number of instances of CVSP
     * @param paretoRuntime: Pareto Runtime Model
     * @param writeToCSV:    write to csv?
     */
    public static SimulationResult simulateMultipleUserMultipleTasksByHours(Date startDate, List<User> users, int numHours, int numInstances, ParetoRuntime paretoRuntime, boolean writeToCSV) throws IOException {
        MultipleTaskScheduler scheduler = new MultipleTaskScheduler(numInstances);
        currentTime = (Date) startDate.clone();
        double arrivalRate = 0;
        HashMap<Integer, User> usersHashMap = new HashMap<Integer, User>();
        for (User user : users) {
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
            Job job = User.generateJobFromMultipleUsers(users, (int) Math.ceil(runtime * hourToSeconds));
            scheduler.addJob(job, MultipleTaskScheduler.SchedulePolicy.random);
            writeSchedulerStatus(startDate.toString() + "System Utilization and Idle Ratio.csv", scheduler);
        }

        writeUserData(startDate.toString() + " user_data", users);
        System.out.println("Utilization Rate: " + scheduler.getUtilizationRate());
        System.out.println("Runtime to Waittime Ratio: " + scheduler.getRuntimeWaittimeRatio());
        //run remaining jobs in the system
        LinkedList<Task> completedTasks = scheduler.runAllTasks();
        if (writeToCSV)
            writeCompletedTasks(startDate.toString(), completedTasks);
        return new SimulationResult(scheduler.getUtilizationRate(), scheduler.getIdleRatio(), scheduler.getRuntimeWaittimeRatio(), scheduler.getExpectedWaittime());

    }

    /**
     * write users info
     *
     * @param filename
     * @param users
     * @throws IOException
     */
    public static void writeUserData(String filename, List<User> users) throws IOException {
        FileWriter writer = new FileWriter(filename, true);
        for (User user : users) {
            writer.write(user + "\n");
        }
        writer.close();
        return;
    }

    public static void writeSchedulerStatus(String filename, MultipleTaskScheduler scheduler) throws IOException {
        FileWriter writer = new FileWriter(filename, true);
        writer.write("\"" + scheduler.getCurrentTime() + "\"," + scheduler.getUtilizationRate() + "," + scheduler.getIdleRatio() + "\n");
        writer.close();
        return;
    }


    public static void updateUserData(HashMap<Integer, User> usersHashMap, LinkedList<Task> completedTasks) {
        for (Task task : completedTasks) {
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


    /**
     * write job to csv
     *
     * @param filename
     * @param job
     * @throws IOException
     */
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

    /**
     * write completed jobs to csv
     *
     * @param filename
     * @param completedJobs
     */
    public static void writeCompletedJobsToCSV(String filename, LinkedList<Job> completedJobs) {
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

    public static void main(String[] args) throws IOException {

        // simulate multiple task, single user job
        //simulateSingleUserMultipleTasksByJobsNum(new Date(),1000,1,61, new PoissonArrival(60),new ParetoRuntime(2, 0.5));

        //Example: Multiple Users simulation
        //LinkedList<User> users = User.generateUsers(60, 1, 1, 0);
        //simulateMultipleUserMultipleTasksByJobsNum(new Date(), users, 5000, 60, new ParetoRuntime(2, 0.5), false);
        //simulateMultipleUserMultipleTasksByHours(new Date(), users, 300, 60, new ParetoRuntime(2, 0.5), false);
        //experiment();
        //experiment();
        //experimentIdleRatioVersusNumTasks();
        experimentIdleRatioVersusTime();
        //experimentExpectedWaitime();
    }

    /**
     * experiment using parameters in the paper
     */
    public static void experiment() throws IOException {
        int[] numInstances = {100, 200, 300, 400, 500}; // M in the paper
        double[] prices = {0.16, 0.145, 0.13};
        double[][] numTasks = {
                {1.055, 1.0737, 1.0983, 1.1048, 1.1382},
                {1.0547, 1.0733, 1.0981, 1.1045, 1.1377},
                {1.0545, 1.0731, 1.0979, 1.1043, 1.1376}
        };
        double[][] rates =
                {
                        {40.77, 41.28, 41.79, 41.83, 42.07},
                        {41.36, 41.87, 42.39, 42.44, 42.68},
                        {41.65, 42.16, 42.69, 42.74, 42.98}
                };
        double[][] profits = new double[3][5];

        int hours = 1000;

        for (int i = 0; i < prices.length; i++) {
            for (int j = 0; j < numInstances.length; j++) {
                System.out.println("p=" + prices[i] + " M=" + numInstances[j] + " Rates=" + rates[i][j]);
                double utilization = simulateSingleUserRandomMultipleTasksByHours(new Date(), hours, new NumTasksDistribution(1.0 / numTasks[i][j]), numInstances[j], new PoissonArrival(rates[i][j]), new GeneralizedParetoRuntime(), false).utilization;
                profits[i][j] = (prices[i] - Utility.getGCPDiscount(Math.max(1 / utilization - 1, 0)) * 0.17) * numInstances[j] * hours * utilization;
                System.out.println("Profit: " + profits[i][j] + "\n");
            }
        }
        /**
         for (int i = 0; i < prices.length; i++) {
         int j = 0;
         for (j = 0; j < numTasks[0].length - 1; j++) {
         System.out.print(results[i][j] + " ");
         }
         System.out.print(results[i][j] + "\n");
         }
         */
    }


    //
    public static void experimentExpectedWaitime(){

        int[] M = {5, 10, 15, 20};
        int[] N = {2, 4, 8, 10};
        double alpha = 2;
        double systemArrivalRate = 10;
        double tauMin = 1.0 / 6;

        int hours = 10000;
        for(int m : M) {
            for (int n: N){
                double instanceArrivalRate = systemArrivalRate * n * 1.0 / m;
                System.out.println("M = " + m + " N = " + n);
                System.out.println("M exceed lower bound? " + (m >= Utility.getNumInstancesBound(systemArrivalRate, n, alpha ,tauMin)));

                double expectedWaittime = simulateSingleUserMultipleTasksByHours(new Date(), hours, n, m, new PoissonArrival(systemArrivalRate), new ParetoRuntime(alpha, tauMin), false).averageWaittime / 3600;
                System.out.println("Simulation Expected Waittime:" + expectedWaittime);
                double theoreticalWaittime = Utility.expectedWaittime(instanceArrivalRate, alpha, tauMin);
                System.out.println("Expected Waittime in paper: " + theoreticalWaittime);
            }
        }

    }


    public static void experimentIdleToRuntimeRatio(){

        int[] M = {5, 10, 15, 20};
        int[] N = {2, 4, 8, 10};
        double alpha = 2;
        double systemArrivalRate = 10;
        double tauMin = 1.0 / 6;

        int hours = 1000;
        for(int m : M) {
            for (int n: N){
                double instanceArrivalRate = systemArrivalRate * n * 1.0 / m;
                System.out.println("M = " + m + " N = " + n);
                double expectedIdleRatio = simulateSingleUserMultipleTasksByHours(new Date(), hours, n, m, new PoissonArrival(systemArrivalRate), new ParetoRuntime(alpha, tauMin), false).idleRatio;
                System.out.println("Simulation Expected Idle Ratio:" + expectedIdleRatio);
                double theoreticalIdleRatio = Utility.getExpectedIdleToRuntimeRatio(instanceArrivalRate, alpha, tauMin);
                System.out.println("Expected Idle Ratio in paper: " + theoreticalIdleRatio);
            }
        }

    }



    public static void experimentIdleRatioVersusTime() throws IOException {
        int[] numInstances = {100, 200, 300, 400, 500}; // M in the paper
        double[] prices = {0.16, 0.145, 0.13};
        double[][] numTasks = {
                {1.055, 1.0737, 1.0983, 1.1048, 1.1382},
                {1.0547, 1.0733, 1.0981, 1.1045, 1.1377},
                {1.0545, 1.0731, 1.0979, 1.1043, 1.1376}
        };
        double[][] rates =
                {
                        {40.77, 41.28, 41.79, 41.83, 42.07},
                        {41.36, 41.87, 42.39, 42.44, 42.68},
                        {41.65, 42.16, 42.69, 42.74, 42.98}
                };
        double[][] profits = new double[3][5];

        int hours = 1000;
        double alpha = 1.5;

        for (int i = 0; i < prices.length; i++) {
            for (int j = 0; j < numInstances.length; j++) {
                System.out.println("p=" + prices[i] + " M=" + numInstances[j] + "N=" + numTasks[i][j] + " Rates=" + rates[i][j]);
                double utilization = simulateSingleUserRandomMultipleTasksByHours(new Date(), hours, new NumTasksDistribution(1.0 / numTasks[i][j]), numInstances[j], new PoissonArrival(rates[i][j]), new ParetoRuntime(alpha), true).utilization;
                profits[i][j] = (prices[i] - Utility.getGCPDiscount(Math.max(1 / utilization - 1, 0)) * 0.17) * numInstances[j] * hours * utilization;
                System.out.println("Profit: " + profits[i][j] + "\n");
            }
        }



    }

    public static void experimentIdleRatioVersusNumTasks() throws IOException {

        int[] numInstances = {100, 200, 300, 400, 500}; // M in the paper
        double[] prices = {0.16, 0.145, 0.13};
        double[][] numTasks = {
                {1.055, 1.0737, 1.0983, 1.1048, 1.1382},
                {1.0547, 1.0733, 1.0981, 1.1045, 1.1377},
                {1.0545, 1.0731, 1.0979, 1.1043, 1.1376}
        };
        double[][] rates =
                {
                        {40.77, 41.28, 41.79, 41.83, 42.07},
                        {41.36, 41.87, 42.39, 42.44, 42.68},
                        {41.65, 42.16, 42.69, 42.74, 42.98}
                };

        double[] N ={1.01, 2, 5, 10,25,50,100};

        int hours = 1000;

        double alpha = 1.5;

        for (int i = 0; i < prices.length; i++) {
            for (int j = 0; j < numInstances.length; j++) {
                IdleRatioVersusNumTasks idleRatioVersusNumTasks = new IdleRatioVersusNumTasks();
                for(int k = 0; k < N.length; k++){
                    System.out.println("p=" + prices[i] + " M=" + numInstances[j] + " N=" + N[k] + " Rate=" + rates[i][j]);
                    double utilization = simulateSingleUserRandomMultipleTasksByHours(new Date(), hours, new NumTasksDistribution(1.0 / N[k]), numInstances[j], new PoissonArrival(rates[i][j]), new ParetoRuntime(alpha), false).utilization;
                    double profit = (prices[i] - Utility.getGCPDiscount(Math.max(1 / utilization - 1, 0)) * 0.17) * numInstances[j] * hours * utilization;
                    System.out.println("Profit: " + profit + "\n");
                    idleRatioVersusNumTasks.numTasks.add(N[k]);
                    idleRatioVersusNumTasks.idleRatios.add(1 / utilization - 1);
                    idleRatioVersusNumTasks.profits.add(profit);
                }
                idleRatioVersusNumTasks.writeToCSV("IdleRatioVersusNumTasks M=" + numInstances[j] + " Rate=" + rates[i][j] + " Alpha=" + alpha + ".csv");
            }
        }

    }
}
