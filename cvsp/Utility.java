package cvsp;
/**
 * @author Haoxiang
 * 
 * Utility is a collection of commonly used functions
 *
 */
public class Utility {
	
	/**
	 * TODO
	 * calculate individual user utility
	 */
	public static double userUtility(){
		return 0;
	}
	

	/**
	 * TODO
	 * solve system arrival rate
	 * @param gammas: arrays of individual jobs
	 * @param phi
	 * @param N
	 * @return system arrival rate
	 */
	public static double systemArrivalRate(double[] gammas, double phi, int N){
		return 0;
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
	
}
