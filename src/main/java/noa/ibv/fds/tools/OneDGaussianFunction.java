package noa.ibv.fds.tools;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;

public class OneDGaussianFunction {
  int data_size;
  
  public OneDGaussianFunction(int data_size) {
    this.data_size = data_size;
  }
  
  public MultivariateVectorFunction retMVF() {
    return new MultivariateVectorFunction() {
        public double[] value(double[] v) throws IllegalArgumentException {
          double[] values = new double[OneDGaussianFunction.this.data_size];
          double ampl = v[0];
          double offset = v[1];
          double meanX = v[2];
          double sigmaX = v[3];
          for (int i = 0; i < values.length; i++)
            values[i] = 
              ampl * Math.exp(-Math.pow(i - meanX, 2.0D) / 2.0D * Math.pow(sigmaX, 2.0D)) + offset; 
          return values;
        }
      };
  }
  
  public MultivariateMatrixFunction retMMF() {
    return new MultivariateMatrixFunction() {
        public double[][] value(double[] point) throws IllegalArgumentException {
          return jacobian(point);
        }
        
        private double[][] jacobian(double[] v) {
          double[][] jacobian = new double[OneDGaussianFunction.this.data_size][4];
          double ampl = v[0];
          double offset = v[1];
          double meanX = v[2];
          double sigmaX = v[3];
          double sgmX2 = sigmaX * sigmaX;
          double sgmX3 = sgmX2 * sigmaX;
          double sgm2X2 = 2.0D * sgmX2;
          double difX = 0.0D;
          double dfX2 = 0.0D;
          double theExp = 0.0D;
          for (int i = 0; i < jacobian.length; i++) {
            difX = i - meanX;
            dfX2 = difX * difX;
            theExp = Math.exp(-dfX2 / sgm2X2);
            jacobian[i][0] = theExp;
            jacobian[i][1] = 1.0D;
            jacobian[i][2] = ampl * difX * theExp / sgmX2;
            jacobian[i][3] = ampl * dfX2 * theExp / sgmX3;
          } 
          return jacobian;
        }
      };
  }
}


/* Location:              /home/nico/Downloads/V3/DetectionStandalone.jar!/noa/ibv/fds/tools/OneDGaussianFunction.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */