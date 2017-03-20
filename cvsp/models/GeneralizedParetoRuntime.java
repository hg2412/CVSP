package cvsp.models;

/**
 * Generalized Pareto Runtime Distribution model in CVSP paper
 */


public class GeneralizedParetoRuntime extends ParetoRuntime{
    /**
     * default parameters are parameters used in the paper for the 0 priority group
     */
    private double tauMin = 1.0/6;
    private double tauHat = 24;
    private double alpha = 1.01;
    private double sigma = 21187.83;
    private double delta = 2.14;
    private double mu = 27.98;

    /**
     * error threshold for bisection method to solve the inverse CDF function
     */
    private double errorThreshold = 1e-8;

    /**
     * constructor using default parameter in the papaer
     */
    public GeneralizedParetoRuntime(){
        ;
    }

    /**
     * Constructor - set parameters
     * @param tauMin
     * @param tauHat
     * @param alpha
     * @param sigma
     * @param delta
     * @param mu
     */
    public GeneralizedParetoRuntime(double tauMin, double tauHat, double alpha, double sigma, double delta, double mu) {
        this.tauMin = tauMin;
        this.tauHat = tauHat;
        this.alpha = alpha;
        this.sigma = sigma;
        this.delta = delta;
        this.mu = mu;
    }

    /**
     * get next random runtime using Inverse Transformation Method
     * @return
     */
    public double getNextRuntime(){
        double u = Math.random();
        return inverseCDF(u);
    }

    /**
     * inverse cdf function
     * @param u
     * @return
     */
    public double inverseCDF(double u){
        double low = tauMin;
        double high = (double)Integer.MAX_VALUE;
        double mid = tauMin;
        while(Math.abs(cdf(mid) - u) > errorThreshold){
            mid = (low + high) / 2;
            if (cdf(mid) < u)
                low = mid;
            else
                high = mid;
        }
        return mid;
    }

    /**
     * CDF function
     * @param tau
     * @return
     */
    public double cdf(double tau){
        if (tau < tauMin)
            return 0;
        else if (tau >= tauMin && tau < tauHat)
            return 1 - Math.pow(tauMin / tau, alpha);
        else
            return 1 - Math.pow(tauMin / tauHat, alpha) + Math.pow(1 + delta * (tauHat - mu)/sigma, -1/delta) - Math.pow(1 + delta * (tau - mu)/sigma, -1/delta);
    }

    /**
     * testing
     */
    public static void main(String[] args){
        GeneralizedParetoRuntime model = new GeneralizedParetoRuntime();
        //test cdf and inverse cdf
        for(int i = 0; i < 10000; i++) {
            double cdf = model.cdf(i * 0.1);
            double tau = model.inverseCDF(cdf);
            assert(Math.abs(tau - i * 0.1) > 1e-4);
        }
        //test random
        double mean = 0;
        for(int i = 0; i < 10000; i++) {
            mean = mean * i/(i+1);
            mean += model.getNextRuntime() /(i+1);
        }
        System.out.println("mean = " + mean);
    }
}
