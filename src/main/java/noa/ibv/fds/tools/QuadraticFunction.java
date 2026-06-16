package noa.ibv.fds.tools;

import java.util.List;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;

public class QuadraticFunction {
  List<Double> x;
  
  List<Double> y;
  
  public QuadraticFunction(List<Double> x, List<Double> y) {
    this.x = x;
    this.y = y;
  }
  
  public void addPoint(double xin, double yin) {
    this.x.add(Double.valueOf(xin));
    this.y.add(Double.valueOf(yin));
  }
  
  public double[] calculateTarget() {
    double[] target = new double[this.y.size()];
    for (int i = 0; i < this.y.size(); i++)
      target[i] = ((Double)this.y.get(i)).doubleValue(); 
    return target;
  }
  
  public MultivariateVectorFunction retMVF() {
    return new MultivariateVectorFunction() {
        public double[] value(double[] variables) throws IllegalArgumentException {
          double[] values = new double[QuadraticFunction.this.x.size()];
          for (int i = 0; i < values.length; i++)
            values[i] = (variables[0] * ((Double)QuadraticFunction.this.x.get(i)).doubleValue() + variables[1]) * ((Double)QuadraticFunction.this.x.get(i)).doubleValue() + variables[2]; 
          return values;
        }
      };
  }
  
  public MultivariateMatrixFunction retMMF() {
    return new MultivariateMatrixFunction() {
        public double[][] value(double[] point) throws IllegalArgumentException {
          return jacobian(point);
        }
        
        private double[][] jacobian(double[] variables) {
          double[][] jacobian = new double[QuadraticFunction.this.x.size()][3];
          for (int i = 0; i < jacobian.length; i++) {
            jacobian[i][0] = ((Double)QuadraticFunction.this.x.get(i)).doubleValue() * ((Double)QuadraticFunction.this.x.get(i)).doubleValue();
            jacobian[i][1] = ((Double)QuadraticFunction.this.x.get(i)).doubleValue();
            jacobian[i][2] = 1.0D;
          } 
          return jacobian;
        }
      };
  }
}


/* Location:              /home/nico/Downloads/V3/DetectionStandalone.jar!/noa/ibv/fds/tools/QuadraticFunction.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */