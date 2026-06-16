package noa.ibv.fds.tools;

public class CheckLM {
  private static void check(double[] inputData, int sizeX, int sizeY) throws Exception {
    System.out.println("Check the MVF");
    double[] v = { 7.0D, 5.0D, 5.0D, 1.0D, 2.0D, -1.0D, 0.0D };
    double[] newData = checkMVF(v, inputData.length, sizeX);
    TwoDGaussianCreator.output2D(newData, sizeY, sizeX);
    for (int i = 0; i < inputData.length; i++) {
      if (Math.abs(newData[i] - inputData[i]) > 1.0E-7D)
        throw new Exception("Not match"); 
    } 
    System.out.println("Check PASSED");
    System.out.println("Check the MMF");
    double[][] jacobian = checkMMF(v, inputData.length, sizeX);
    System.out.println("Check PASSED");
  }
  
  private static double[] checkMVF(double[] v, int data_size, int x_width) {
    double[] values = new double[data_size];
    double ampl = v[0];
    double meanX = v[1];
    double meanY = v[2];
    double sigmaX = v[3];
    double sigmaY = v[4];
    double theta = v[5];
    double offset = v[6];
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
      difX = (i % x_width) - meanX;
      difY = (i / x_width) - meanY;
      values[i] = offset + 
        ampl * Math.exp(-(a * Math.pow(difX, 2.0D) + 2.0D * b * difX * difY + c * Math.pow(difY, 2.0D)));
    } 
    return values;
  }
  
  private static double[][] checkMMF(double[] v, int data_size, int x_width) {
    double[][] jacobian = new double[data_size][7];
    double ampl = v[0];
    double meanX = v[1];
    double meanY = v[2];
    double sigmaX = v[3];
    double sigmaY = v[4];
    double theta = v[5];
    double offset = v[6];
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
      difX = (i % x_width) - meanX;
      difY = (i / x_width) - meanY;
      dfXY = difX * difY;
      dfX2 = difX * difX;
      dfY2 = difY * difY;
      theExp = Math.exp(-2.0D * dfXY * sin2ThDif - dfX2 * scTh2Diff - dfY2 * csTh2Diff);
      jacobian[i][0] = 
        theExp;
      jacobian[i][1] = 
        ampl * (2.0D * difY * sin2ThDif + 2.0D * difX * scTh2Diff) * theExp;
      jacobian[i][2] = 
        ampl * (2.0D * difX * sin2ThDif + 2.0D * difY * csTh2Diff) * theExp;
      jacobian[i][3] = 
        ampl * (-sin2th * dfXY + sinTh2 * dfY2 + cosTh2 * dfX2) / sgmX3 * theExp;
      jacobian[i][4] = 
        ampl * (sin2th * dfXY + cosTh2 * dfY2 + sinTh2 * dfX2) / sgmY3 * theExp;
      jacobian[i][5] = 
        ampl * (-2.0D * dfXY * aTh - dfX2 * bTh - dfY2 * cTh) * theExp;
      jacobian[i][6] = 
        1.0D;
    } 
    return jacobian;
  }
}


/* Location:              /home/nico/Downloads/V3/DetectionStandalone.jar!/noa/ibv/fds/tools/CheckLM.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */