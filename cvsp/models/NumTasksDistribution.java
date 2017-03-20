package cvsp.models;

/**
 * Created by Haoxiang on 3/17/17.
 * This program use geometric distribution to simulate and generate random No. of tasks
 */
public class NumTasksDistribution {
    private double p = 0.5; // p parameter for geometric distribution (E(num of task) = 1/p)

    public NumTasksDistribution(double p){
        this.p = p;
    }

    /**
     * get the next random number of the task
     * @return
     */
    public int getNextNumOfTasks(){
        return (int)(Math.ceil(Math.log(Math.random())/Math.log(1.0-p)));
    }

    /**
     * Testing using Law of Large Number
     * @param args
     */
    public static void main(String[] args){
        NumTasksDistribution numTasksDistribution = new NumTasksDistribution(1.0/3);
        double mean = 0;
        for(int i = 0; i < 10000; i++){
            mean = mean * 1.0 * i * 1.0/(i+1);
            int numTask = numTasksDistribution.getNextNumOfTasks();
            mean += numTask * 1.0/(i+1);
        }
        System.out.println("Mean = " + mean);
        assert(Math.abs(mean - 3.0) < 0.1);
    }

}
