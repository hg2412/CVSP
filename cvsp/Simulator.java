package cvsp;
import cvsp.models.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;

public class Simulator {
	private static Date currentTime;
	private static final double hourToMillisecond = 60 * 60 * 1000;
	private static final double hourToSeconds = 60 * 60;
	
	/**
	 * simulate the single user single-task job arrivals using Possion model and Pareto model and CVSP's schedulers
	 * @param startDate: starting date of the simulation
	 * @param numJobs: number of jobs to simulate
	 * @param numInstances: number of instances of CVSP
	 * @param possionArrival: Possion Arrival Model
	 * @param paretoRuntime: Pareto Runtime Model
	 */
	public static void simulate(Date startDate, int numJobs, int numInstances, PoissonArrival possionArrival, ParetoRuntime paretoRuntime){
		SingleTaskScheduler scheduler= new SingleTaskScheduler(numInstances);
		//sync time with scheduler
		currentTime = (Date)startDate.clone();
		SingleTaskScheduler.setCurrentTime(new Date(currentTime.getTime()));
		
		for(int i = 0; i < numJobs; i++){
			double arrivalTime = possionArrival.getNextArrivalTime();
			double runtime = paretoRuntime.getNextRuntime();
			System.out.println("Job " + i + " Arrival Time:" + arrivalTime + " Runtime:" + runtime);
			Date nextArrivalTime = new Date();
			nextArrivalTime.setTime((long) (currentTime.getTime() + Math.ceil( arrivalTime * hourToMillisecond)));
			
			// run previous jobs
			LinkedList<Job> completedJobs = scheduler.runJobs(nextArrivalTime);
			if (completedJobs != null){
				for (Job job: completedJobs){
					try {
						writeJobToCSV(startDate.toString() + ".csv", job);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			currentTime.setTime(nextArrivalTime.getTime());
			SingleTaskScheduler.setCurrentTime(currentTime);
			// create a new job and add to waiting jobs
			Job job = new Job(1, (int) Math.ceil(runtime * hourToSeconds));
			job.start = (Date) currentTime.clone();
			scheduler.addJob(job);
			scheduler.assignJob();
		}
		
		// deal with remaining jobs in the system
		LinkedList<Job> remainingJobs = scheduler.runJobs();
		if (remainingJobs != null){
			for(Job j:remainingJobs){
				try {
					writeJobToCSV(startDate.toString() + ".csv", j);
				} catch (IOException e){
					e.printStackTrace();
				}
			}
		}
	}

	public static void simulate(Date startDate, int numJobs, int numTasks, int numInstances, PoissonArrival poissonArrival, ParetoRuntime paretoRuntime){
		MultipleTaskScheduler scheduler= new MultipleTaskScheduler(numInstances);
		//sync time with scheduler
		currentTime = (Date)startDate.clone();
		MultipleTaskScheduler.setCurrentTime(new Date(currentTime.getTime()));

		for(int i = 0; i < numJobs; i++){
			double arrivalTime = poissonArrival.getNextArrivalTime();
			double runtime = paretoRuntime.getNextRuntime();
			System.out.println("Job " + i + " Arrival Time:" + arrivalTime + " Runtime:" + runtime);
			Date nextArrivalTime = new Date();
			nextArrivalTime.setTime((long) (currentTime.getTime() + Math.ceil( arrivalTime * hourToMillisecond)));

			// run previous jobs
			LinkedList<Task> completedTasks = scheduler.runTasks(nextArrivalTime);
			logCompletedTasks(startDate.toString(), completedTasks);

			currentTime.setTime(nextArrivalTime.getTime());
			SingleTaskScheduler.setCurrentTime(currentTime);
			// create a new job and add to waiting jobs
			Job job = new Job(numTasks, (int) Math.ceil(runtime * hourToSeconds));
			job.start = (Date) currentTime.clone();
			scheduler.addJob(job);
		}

		//run remaining jobs in the system
		LinkedList<Task> completedTasks = scheduler.runTasks();
		logCompletedTasks(startDate.toString(), completedTasks);

	}
	
	/**
	 * write job log to csv file
	 * @param filename
	 * @param  completedTasks
	 * @throws IOException
	 */
	public static void logCompletedTasks(String filename, LinkedList<Task> completedTasks){
		if (!completedTasks.isEmpty()){
			for (Task task: completedTasks){
				try {
					writeTaskToCSV(filename + ".csv", task);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public static void writeJobToCSV(String filename, Job job) throws IOException{
		FileWriter writer = new FileWriter(filename, true);
		StringBuilder sb = new StringBuilder();
		sb.append("\"" + job.start.toString()  + "\",");
		sb.append("\"" + job.end.toString()  + "\", ");
		sb.append( Integer.toString(job.runTime) + ", " );
		sb.append( Integer.toString(job.waitTime) + "\n");
		System.out.print(sb.toString());
		writer.write(sb.toString());
		writer.flush();
		writer.close();
		return;
	}

	/**
	 * write job log to csv file
	 * @param filename
	 * @param task
	 * @throws IOException
	 */
	public static void writeTaskToCSV(String filename, Task task) throws IOException{
		FileWriter writer = new FileWriter(filename, true);
		StringBuilder sb = new StringBuilder();
		sb.append("\"" + "job" + task.jobId  + "\",");
		sb.append("\"" + "task" + task.taskId  + "\",");
		sb.append("\"" + task.submitTime.toString()  + "\",");
		sb.append("\"" + task.startTime.toString()  + "\",");
		sb.append("\"" + task.endTime.toString()  + "\", ");
		sb.append( Integer.toString(task.runTime) + ", " );
		sb.append( Integer.toString(task.waitTime) + "\n");
		System.out.print(sb.toString());
		writer.write(sb.toString());
		writer.flush();
		writer.close();
		return;
	}

	public static void main(String[] args) {

		// simulate single task, single user job
		//Simulator.simulate(new Date(), 1200, 10, new PoissonArrival(60), new ParetoRuntime(1,0.1));
		// simulate multiple task, single user job
		Simulator.simulate(new Date(), 10, 10, 10, new PoissonArrival(60), new ParetoRuntime(0.4,0.01));

	}

}
