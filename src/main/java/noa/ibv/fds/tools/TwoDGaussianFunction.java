package noa.ibv.fds.tools;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;

public class TwoDGaussianFunction {
  int x_width;
  
  int data_size;
  
  public TwoDGaussianFunction(int x_width, int data_size) {
    this.x_width = x_width;
    this.data_size = data_size;
  }
  
  public MultivariateVectorFunction retMVF() {
    return new MultivariateVectorFunction() {
        public double[] value(double[] v) throws IllegalArgumentException {
          double[] values = new double[TwoDGaussianFunction.this.data_size];
          double ampl = v[0];
          double offset = v[1];
          double meanX = v[2];
          double meanY = v[3];
          double sigmaX = v[4];
          double sigmaY = v[5];
          double theta = v[6];
          double sgmX2 = sigmaX * sigmaX;
          double sgmY2 = sigmaY * sigmaY;
          double thCos2 = Math.pow(Math.cos(theta), 2.0D);
          double thSin2 = Math.pow(Math.sin(theta), 2.0D);
          double th2Sin = Math.sin(2.0D * theta);
          double a = thCos2 / 2.0D * sgmX2 + thSin2 / 2.0D * sgmY2;
          double b = -(th2Sin / 4.0D * sgmX2) + th2Sin / 4.0D * sgmY2;
          double c = thSin2 / 2.0D * sgmX2 + thCos2 / 2.0D * sgmY2;
          double difX = 0.0D;
          double difY = 0.0D;
          for (int i = 0; i < values.length; i++) {
            difX = (i % TwoDGaussianFunction.this.x_width) - meanX;
            difY = (i / TwoDGaussianFunction.this.x_width) - meanY;
            values[i] = 
              offset + 
              ampl * Math.exp(-(a * Math.pow(difX, 2.0D) + 2.0D * b * difX * difY + c * Math.pow(difY, 2.0D)));
          } 
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
          double[][] jacobian = new double[TwoDGaussianFunction.this.data_size][7];
          double ampl = v[0];
          double offset = v[1];
          double meanX = v[2];
          double meanY = v[3];
          double sigmaX = v[4];
          double sigmaY = v[5];
          double theta = v[6];
          double sin2th = Math.sin(2.0D * theta);
          double cos2th = Math.cos(2.0D * theta);
          double sgmX2 = sigmaX * sigmaX;
          double sgmX3 = sgmX2 * sigmaX;
          double sgmY2 = sigmaY * sigmaY;
          double sgmY3 = sgmY2 * sigmaY;
          double cosTh = Math.cos(theta);
          double sinTh = Math.sin(theta);
          double csnth = cosTh * sinTh;
          double cosTh2 = Math.pow(cosTh, 2.0D);
          double sinTh2 = Math.pow(sinTh, 2.0D);
          double sin2ThDif = sin2th / 4.0D * sgmY2 - sin2th / 4.0D * sgmX2;
          double scTh2Diff = sinTh2 / 2.0D * sgmY2 + cosTh2 / 2.0D * sgmX2;
          double csTh2Diff = cosTh2 / 2.0D * sgmY2 + sinTh2 / 2.0D * sgmX2;
          double aTh = cos2th / 2.0D * sgmY2 - cos2th / 2.0D * sgmX2;
          double bTh = csnth / sgmY2 - csnth / sgmX2;
          double cTh = csnth / sgmX2 - csnth / sgmY2;
          double difX = 0.0D;
          double difY = 0.0D;
          double dfXY = 0.0D;
          double dfX2 = 0.0D;
          double dfY2 = 0.0D;
          double theExp = 0.0D;
          for (int i = 0; i < jacobian.length; i++) {
            difX = (i % TwoDGaussianFunction.this.x_width) - meanX;
            difY = (i / TwoDGaussianFunction.this.x_width) - meanY;
            dfXY = difX * difY;
            dfX2 = difX * difX;
            dfY2 = difY * difY;
            theExp = Math.exp(-2.0D * dfXY * sin2ThDif - dfX2 * scTh2Diff - dfY2 * csTh2Diff);
            jacobian[i][0] = 
              theExp;
            jacobian[i][1] = 
              1.0D;
            jacobian[i][2] = 
              ampl * (2.0D * difY * sin2ThDif + 2.0D * difX * scTh2Diff) * theExp;
            jacobian[i][3] = 
              ampl * (2.0D * difX * sin2ThDif + 2.0D * difY * csTh2Diff) * theExp;
            jacobian[i][4] = 
              ampl * (-sin2th * dfXY + sinTh2 * dfY2 + cosTh2 * dfX2) / sgmX3 * theExp;
            jacobian[i][5] = 
              ampl * (sin2th * dfXY + cosTh2 * dfY2 + sinTh2 * dfX2) / sgmY3 * theExp;
            jacobian[i][6] = 
              ampl * (-2.0D * dfXY * aTh - dfX2 * bTh - dfY2 * cTh) * theExp;
          } 
          return jacobian;
        }
      };
  }
}


/* Location:              /home/nico/Downloads/V3/DetectionStandalone.jar!/noa/ibv/fds/tools/TwoDGaussianFunction.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */