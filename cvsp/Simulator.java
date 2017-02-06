package cvsp;
import cvsp.models.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;

public class Simulator {
	private static Date currentTime;
	private static final double hourToMillisecond = 60 * 60 * 1000;
	private static final double hourToSeconds = 60 * 60 ;
	
	/**
	 * simulate the job arrivals using Possion model and Pareto model and CVSP's schedulers
	 * @param startDate: starting date of the simulation
	 * @param numJobs: number of jobs to simulate
	 * @param numInstances: number of instances of CVSP
	 * @param possionArrival: Possion Arrival Model
	 * @param paretoRuntime: Pareto Runtime Model
	 */
	public static void simulate(Date startDate, int numJobs, int numInstances, PoissonArrival possionArrival, ParetoRuntime paretoRuntime){
		Scheduler scheduler= new Scheduler(numInstances);
		//sync time with scheduler
		currentTime = (Date)startDate.clone();
		Scheduler.setCurrentTime(new Date(currentTime.getTime()));
		
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
			Scheduler.setCurrentTime(currentTime);
			// create a new job and add to waiting jobs
			Job job = new Job();
			job.runtime = (int) Math.ceil(runtime * hourToSeconds);
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
	
	/**
	 * write job log to csv file
	 * @param filename
	 * @param job
	 * @throws IOException
	 */
	public static void writeJobToCSV(String filename, Job job) throws IOException{
		FileWriter writer = new FileWriter(filename, true);
		StringBuilder sb = new StringBuilder();
		sb.append("\"" + job.start.toString()  + "\",");
		sb.append("\"" + job.end.toString()  + "\", ");
		sb.append( Integer.toString(job.runtime) + ", " );
		sb.append( Integer.toString(job.waitTime) + "\n");
		System.out.print(sb.toString());
		writer.write(sb.toString());
		writer.flush();
		writer.close();
		return;
	}

	public static void main(String[] args) {
		Simulator.simulate(new Date(), 1200, 10, new PoissonArrival(60), new ParetoRuntime(1,0.1));
		
	}

}
