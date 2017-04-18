package cvsp.models;


import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.RombergIntegrator;
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
import org.apache.commons.math3.analysis.integration.TrapezoidIntegrator;
import org.apache.commons.math3.analysis.integration.UnivariateIntegrator;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;

import static org.apache.commons.math3.util.ArithmeticUtils.factorial;

/**
 * Created by Haoxiang on 4/17/17.
 */
public class ExpectedIdleRatio {

    public double alpha = 1.01;
    public int n = 1;
    public int M = 500;
    public double lambda = 40.77;
    public double T = 1000;
    public double tauMin = 1.0 / 6;

    public ExpectedIdleRatio() {

    }

    public ExpectedIdleRatio(double alpha, int n, int m, double lambda, double t, double tauMin) {
        this.alpha = alpha;
        this.n = n;
        this.M = m;
        this.lambda = lambda;
        this.T = t;
        this.tauMin = tauMin;
    }

    @Override
    public String toString() {
        return "ExpectedIdleRatio{" +
                "alpha=" + alpha +
                ", n=" + n +
                ", M=" + M +
                ", lambda=" + lambda +
                ", T=" + T +
                ", tauMin=" + tauMin +
                '}';
    }


    public double part0(double alpha, int n, int M, double lambda, double T, double tau, double tauMin, double t){
        double result = 0;
        double temp1 = n * 1.0 * lambda / M ;
        return 1.0 / t * (t - tau) * temp1 * Math.exp(-temp1 * t);
    }


    private class Part0Function implements UnivariateFunction {
        public double tau = 1.0 / 6;
        public double value(double t) {
            return part0( alpha, n, M, lambda, T, this.tau, tauMin, t);
        }
    }



    private class Part1Function implements UnivariateFunction {
        public double value(double tau) {
            SimpsonIntegrator simpson = new SimpsonIntegrator();
            Part0Function f0 = new Part0Function();
            f0.tau = tau;
            return simpson.integrate(Integer.MAX_VALUE, f0, tau, T + 0.01) * alpha * Math.pow(tauMin, alpha) / Math.pow(tau, alpha + 1);
        }
    }


    public double part2(int n, int M, double lambda, double t){
        double temp1 = n * 1.0 * lambda / M;
        return temp1 * Math.exp(-temp1 * t);
    }

    private class Part2Function implements UnivariateFunction {
        public double value(double tau) {
            return part2(n, M, lambda, tau);
        }
    }



    public double part3(double alpha, int n, int M, double lambda, double T, double tau, double tauMin){
        return (T -tau) * alpha * Math.pow(tauMin,alpha) / Math.pow(tau, alpha + 1);
    }

    private class Part3Function implements UnivariateFunction {
        public double value(double tau) {
            return part3(alpha, n, M, lambda, T, tau, tauMin);
        }
    }


    public double calculate(){
        UnivariateIntegrator integrator = new RombergIntegrator();
        SimpsonIntegrator simpson = new SimpsonIntegrator();
        UnivariateFunction f1 = new Part1Function();
        UnivariateFunction f2 = new Part2Function();
        UnivariateFunction f3 = new Part3Function();

        double result1 = simpson.integrate(Integer.MAX_VALUE, f1 , tauMin, T);
        double result2 = simpson.integrate(Integer.MAX_VALUE, f2 , T, Integer.MAX_VALUE);
        double result3 = simpson.integrate(Integer.MAX_VALUE, f3 , tauMin, T);
        //System.out.println(result1);
        //System.out.println(result2);
        //System.out.println(result3);

        double idleTime = T * result1 + result2 * result3;
        return idleTime / (T - idleTime);
    }

    public static void main(String[] args){
        ExpectedIdleRatio expectedRatio = new ExpectedIdleRatio();
        System.out.println(expectedRatio);
        System.out.println(expectedRatio.calculate());
    }

}
