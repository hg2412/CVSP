package cvsp.models;

public class ParetoRuntime {
	private double alpha;
	private double tau_min;

	/**
	 * set parameter of the model
	 * @param alpha - parameter of the model
	 * @param tau_min - minimum runtime (in hours)
	 */
	public ParetoRuntime(double alpha, double tau_min) {
		this.alpha = alpha;
		this.tau_min = tau_min;
	}

	/**
	 * TODO: modify to be the piecewise Pareto Distribution in the paper
	 */
	public double getNextRuntime() {
		double v = Math.random();
		while (v == 0) {
			v = Math.random();
		}
		return tau_min / Math.pow(v, 1.0 / alpha);
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
