package cvsp;
import org.apache.commons.math3.special.Gamma;
/**
 * @author Haoxiang
 *         <p>
 *         Utility is a collection of commonly used functions
 */
public class Utility {

    private static final double hourToMillisecond = 60 * 60 * 1000;
    private static final double hourToSeconds = 60 * 60;

    /**
     * calculate individual user utility
     */
    public static double userUtility(double gamma, double lambda, double runtime, double waittime, double price, int numTasks) {

        return gamma * Math.log(1 + lambda * runtime / waittime * numTasks) - price * lambda * runtime * numTasks;
    }


    /**
     * solution to system arrival rate
     * @return system arrival rate
     */
    public static double systemArrivalRate(double gamma, double price, double runtime, double waittime, int numTasks, int numUsers) {
        return numUsers * (gamma - price * waittime) / (price * runtime * numTasks);
    }

    /**
     * get number of instances lower bound that satisfy finite expected waittime
     *
     */
    public static int getNumInstancesBound(double systemArrivalRate, double numTasks, double alpha, double tauMin){
        return (int)Math.ceil(alpha * numTasks * systemArrivalRate * tauMin / (alpha - 1));

    }

    /**
     * calculate the instance arrival rate
     * @param arrivalRate - instance arrival rate
     * @param alpha
     * @param tauMin
     * @return
     */
    public static double expectedWaittime(double arrivalRate, double alpha, double tauMin){
        double gamma = Gamma.regularizedGammaQ(-alpha, arrivalRate * tauMin) * Gamma.gamma(-alpha);
        System.out.println(gamma);
        return (1.0 / arrivalRate) * Math.log((alpha * Math.pow(arrivalRate * tauMin, alpha) * gamma)/(1.0 - alpha * tauMin * arrivalRate / (alpha - 1)));
    }


    /**
     * calculate GCP usage dicount
     *
     * @param theta: The expected idle-to-runtime ratio of an instance’s usage
     * @return w: Fraction of full price that the CVSP pays to GCP
     */
    public static double getGCPDiscount(double theta) {
        if (theta < 0) throw new ArithmeticException("Theta is invalid");
        if (theta >= 3) return 1.0;
        else if (theta < 3 && theta >= 1) return 0.05 * theta + 0.85;
        else if (theta < 1 && theta >= 0.33333) return 0.15 * theta + 0.75;
        else return 0.3 * theta + 0.7;
    }

    /**
     * Calculate CVSP profits per hour
     *
     * @param idleRatio
     * @param cvspPrice
     * @param gcpPrice
     * @param numInstances
     * @return
     */
    public static double calculateProfitPerHour(double idleRatio, double cvspPrice, double gcpPrice, int numInstances) {
        return numInstances * (cvspPrice - gcpPrice * getGCPDiscount(idleRatio)) * (1.0 / (idleRatio + 1.0));
    }


    public static double getExpectedIdleToRuntimeRatio(double arrivalRate, double alpha, double tauMin){
        double gamma = Gamma.gamma(-alpha - 1) * Gamma.regularizedGammaQ(-alpha -1, arrivalRate * tauMin);
        return alpha * Math.pow( arrivalRate * tauMin, alpha) * gamma;
    }


    public static double convertUtiliztionToIdleRatio(double utilization) {
        return (1.0 - utilization) / utilization;
    }
}
