package cvsp;
/**
 * @author Haoxiang
 * 
 * Utility is a collection of commonly used functions
 *
 */
public class Utility {

	private static final double hourToMillisecond = 60 * 60 * 1000;
	private static final double hourToSeconds = 60 * 60 ;

	/**
	 * calculate individual user utility
	 */
	public static double userUtility(double gamma, double lambda, double runtime, double waittime, double price, int numTasks){

		return gamma * Math.log(1 + lambda * runtime / waittime * numTasks) - price * lambda * runtime * numTasks;
	}
	

	/**
	 * solution to system arrival rate
	 * @return system arrival rate
	 */
	public static double systemArrivalRate(double gamma, double price, double runtime, double waittime, int numTasks, int numUsers){
		return numUsers * (gamma - price * waittime)/(price * runtime * numTasks);
	}

	
	/**
	 * @param theta: The expected idle-to-runtime ratio of an instance’s usage
	 * @return w: Fraction of full price that the CVSP pays to GCP
	 */
	public static double getGCPDiscount(double theta){
		if (theta < 0) throw new ArithmeticException("Theta is invalid"); 
		if (theta >= 3) return 1.0;
		else if (theta < 3 && theta >= 1) return 0.05 * theta + 0.85;
		else if (theta < 1 && theta >= 0.33333) return 0.15 * theta + 0.75;
		else return 0.3 * theta + 0.7;
	}

	public static double calculateProfitPerHour(double idleRatio, double cvspPrice, double gcpPrice, int numInstances){
		return numInstances * (gcpPrice * getGCPDiscount(idleRatio) - cvspPrice) * (1/(idleRatio + 1));
	}
}
