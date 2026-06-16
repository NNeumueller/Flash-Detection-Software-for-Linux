package noa.ibv.fds.tools;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;

public class QuadraticProblemExample2 {
  public static void main(String[] args) {
    List<Double> x = new ArrayList<>();
    List<Double> y = new ArrayList<>();
    QuadraticFunction qf = new QuadraticFunction(x, y);
    qf.addPoint(1.0D, 34.234064369D);
    qf.addPoint(2.0D, 68.2681162306D);
    qf.addPoint(3.0D, 118.6158990846D);
    qf.addPoint(4.0D, 184.1381972386D);
    qf.addPoint(5.0D, 266.5998779163D);
    qf.addPoint(6.0D, 364.1477352516D);
    qf.addPoint(7.0D, 478.0192260919D);
    qf.addPoint(8.0D, 608.1409492707D);
    qf.addPoint(9.0D, 754.5988686671D);
    qf.addPoint(10.0D, 916.1288180859D);
    LeastSquaresBuilder lsb = new LeastSquaresBuilder();
    lsb.model(qf.retMVF(), qf.retMMF());
    double[] newTarget = qf.calculateTarget();
    lsb.target(newTarget);
    double[] newStart = { 1.0D, 1.0D, 1.0D };
    lsb.start(newStart);
    lsb.maxEvaluations(100);
    lsb.maxIterations(1000);
    LevenbergMarquardtOptimizer lmo = new LevenbergMarquardtOptimizer();
    try {
      LeastSquaresOptimizer.Optimum lsoo = lmo.optimize(lsb.build());
      double[] optimalValues = lsoo.getPoint().toArray();
      System.out.println("A: " + optimalValues[0]);
      System.out.println("B: " + optimalValues[1]);
      System.out.println("C: " + optimalValues[2]);
      System.out.println("Iteration number: " + lsoo.getIterations());
      System.out.println("Evaluation number: " + lsoo.getEvaluations());
    } catch (Exception e) {
      System.out.println(e.toString());
    } 
  }
}


/* Location:              /home/nico/Downloads/V3/DetectionStandalone.jar!/noa/ibv/fds/tools/QuadraticProblemExample2.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */