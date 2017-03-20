package cvsp.models;

/**
 * This class uses poisson distribution to simulate jobs arrivals
 */
public class PoissonArrival {
	/**
	 * arrival rate
	 */
	public double rate;

	/**
	 * constructor
	 * @param rate
	 */
	public PoissonArrival(double rate) {
		this.rate = rate;
	}
	
	/**
	 *  get next arrival time (follows exponential dist)
	 * @return time of next arrival (hour)
	 */
	public double getNextArrivalTime(){
		return Math.log(Math.random())/(-rate);
	}
	
	/**
	 * get number of job arrivals in time t (hour) interval
	 * @param t: length of time interval (hours)
	 * @return number of job arrivals
	 */
	public int getNumberOfArrivals(int t){
		double L = Math.exp(-rate * t); 
        int k = 0; 
        double p = 1.0; 
        do { 
            k++; 
            p = p * Math.random(); 
        } while (p > L); 
        return k - 1; 
	}

}
