package cvsp.models;

/**
 * This class uses Pareto Distribution to simulate the runtime of user's task
 */
public class ParetoRuntime {
	/**
	 * default model parameters
	 */
	protected double alpha = 1.01;
	protected double minTau = 1.0 / 6; // in hours

	/**
	 * constructor using default parameter
	 */
	public ParetoRuntime(){
		;
	}

	/**
	 * constructor that sets parameters of the model
	 * @param alpha - parameter of the model
	 * @param minTau - minimum runtime (in hours)
	 */
	public ParetoRuntime(double alpha, double minTau) {
		this.alpha = alpha;
		this.minTau = minTau;
	}

	/**
	 * get the next random runtime
	 * @return
	 */
	public double getNextRuntime() {
		double v = Math.random();
		return minTau / Math.pow(v, 1/alpha);
	}

	/**
	 * get the expected runtime
	 * @return
	 */
	public double getExpectedRuntime(){
		return minTau * alpha / (alpha - 1);

	}

}
