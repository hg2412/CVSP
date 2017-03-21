package cvsp;

import cvsp.models.GeneralizedParetoRuntime;
import cvsp.models.NumTasksDistribution;
import cvsp.models.ParetoRuntime;
import cvsp.models.PoissonArrival;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Scanner;

import static cvsp.Simulator.*;

/**
 * User interfaces for simulation.
 */
public class UserInterface {
    public static void main(String args[]) throws IOException {
        Scanner scan = new Scanner(System.in);
        System.out.println("CVSP Simulator");
        System.out.println("Demo? yes or no");

        boolean demo;
        String demoInput = scan.next();
        if (demoInput.toLowerCase().equals("yes") || demoInput.toLowerCase().equals("y")) {
            System.out.println("1000 hours Simulation using parameters in the paper:");
            Simulator.experiment();
            return;
        } else {
            System.out.println("[1] Single User Fixed Number of Tasks By Hours");
            System.out.println("[2] Single User Random Number of Tasks By Hours");
            System.out.println("[3] Multiple User Fixed Number of Tasks By Hours");
            System.out.println("Please input 1 or 2 or 3");
        }

        int option = scan.nextInt();
        switch (option) {
            case 1:
                task1();
                break;
            case 2:
                task2();
                break;
            case 3:
                task3();
                break;
            default:
                return;
        }

    }

    public static void task1() {
        Scanner scan = new Scanner(System.in);
        System.out.println("Number of hours: ");
        int numHours = scan.nextInt();
        System.out.println("Number of Tasks: ");
        int numTasks = scan.nextInt();
        System.out.println("Number of Instances: ");
        int numInstances = scan.nextInt();
        System.out.println("Job Arrival Rates: ");
        double arrivalRate = scan.nextDouble();
        System.out.println("CVSP Price: ");
        double price = scan.nextDouble();
        System.out.println("GCP Price: ");
        double gcpPrice = scan.nextDouble();

        double utilization = simulateSingleUserMultipleTasksByHours(new Date(), numHours, numTasks, numInstances, new PoissonArrival(arrivalRate), new GeneralizedParetoRuntime(), true);
        double idleRatio = Utility.convertUtiliztionToIdleRatio(utilization);
        System.out.println("Idle Ratio: " + idleRatio);
        double profitPerHour = Utility.calculateProfitPerHour(idleRatio, price, gcpPrice, numInstances);
        System.out.println("Profit: " + numHours * 1.0 * profitPerHour);
    }

    public static void task2() {
        Scanner scan = new Scanner(System.in);
        System.out.println("Number of hours: ");
        int numHours = scan.nextInt();
        System.out.println("Expected Number of Tasks: ");
        int numTasks = scan.nextInt();
        System.out.println("Number of Instances: ");
        int numInstances = scan.nextInt();
        System.out.println("Job Arrival Rates: ");
        double arrivalRate = scan.nextDouble();
        System.out.println("CVSP Price: ");
        double price = scan.nextDouble();
        System.out.println("GCP Price: ");
        double gcpPrice = scan.nextDouble();

        double utilization = simulateSingleUserRandomMultipleTasksByHours(new Date(), numHours, new NumTasksDistribution(1.0 / numTasks), numInstances, new PoissonArrival(arrivalRate), new GeneralizedParetoRuntime(), true);
        double idleRatio = Utility.convertUtiliztionToIdleRatio(utilization);
        System.out.println("Idle Ratio: " + idleRatio);
        double profitPerHour = Utility.calculateProfitPerHour(idleRatio, price, gcpPrice, numInstances);
        System.out.println("Profit: " + numHours * 1.0 * profitPerHour);
    }

    public static void task3() throws IOException {
        Scanner scan = new Scanner(System.in);
        System.out.println("Number of hours: ");
        int numHours = scan.nextInt();
        System.out.println("Number of Users: ");
        int numUsers = scan.nextInt();
        System.out.println("Number of Tasks: ");
        int numTasks = scan.nextInt();
        System.out.println("Number of Instances: ");
        int numInstances = scan.nextInt();
        System.out.println("User Job Arrival Rates: ");
        double arrivalRate = scan.nextDouble();
        System.out.println("CVSP Price: ");
        double price = scan.nextDouble();
        System.out.println("GCP Price: ");
        double gcpPrice = scan.nextDouble();

        LinkedList<User> users = User.generateUsers(numUsers, numTasks, arrivalRate, 9.0);
        double utilization = simulateMultipleUserMultipleTasksByHours(new Date(), users, numHours, numInstances, new GeneralizedParetoRuntime(), true);
        double idleRatio = Utility.convertUtiliztionToIdleRatio(utilization);
        System.out.println("Idle Ratio: " + idleRatio);
        double profitPerHour = Utility.calculateProfitPerHour(idleRatio, price, gcpPrice, numInstances);
        System.out.println("Profit: " + numHours * 1.0 * profitPerHour);
    }

}
