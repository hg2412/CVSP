package cvsp.models;

public class ParetoRuntime {
	private double alpha;
	private double minTau;

	/**
	 * set parameter of the model
	 * @param alpha - parameter of the model
	 * @param minTau - minimum runtime (in hours)
	 */
	public ParetoRuntime(double alpha, double minTau) {
		this.alpha = alpha;
		this.minTau = minTau;
	}

	/**
	 * TODO: modify to be the piecewise Pareto Distribution in the paper
	 */
	public double getNextRuntime() {
		double v = Math.random();

		return minTau / Math.pow(v, 1/alpha);
	}

	public double getExpectedRuntime(){
		return minTau * alpha / (alpha - 1);

	}

	/**
	 * TODO
	 * @param data - empirical data of job runtime
	 * @return fit runtime
	 */
	public double fit(double[] data) {
		return 0;
	}

}
