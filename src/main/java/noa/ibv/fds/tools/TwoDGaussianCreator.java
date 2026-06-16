package noa.ibv.fds.tools;

public class TwoDGaussianCreator {
  public static void main(String[] args) throws Exception {
    int sizeX = 11;
    int sizeY = 11;
    double ampl = 7.0D;
    double offset = 0.0D;
    double cntX = 5.0D;
    double cntY = 5.0D;
    double sgmX = 1.0D;
    double sgmY = 2.0D;
    double theta = -1.0D;
    double[] the2dArr = 
      create2d(sizeX, sizeY, ampl, offset, cntX, cntY, sgmX, sgmY, theta);
    output2D(the2dArr, sizeY, sizeX);
  }
  
  public static double[] create2d(int sizeX, int sizeY, double ampl, double offset, double aveX, double aveY, double sgmX, double sgmY, double theta) throws Exception {
    if (sizeY < 5 || sizeX < 5)
      throw new Exception("Dimension to small: " + sizeY + "*" + sizeX); 
    if (sizeY > 500 || sizeX > 500)
      throw new Exception("Dimension to large: " + sizeY + "*" + sizeX); 
    double[] the2dGaussian = new double[sizeY * sizeX];
    double sgmX2 = sgmX * sgmX;
    double sgmY2 = sgmY * sgmY;
    double thCos2 = Math.pow(Math.cos(theta), 2.0D);
    double thSin2 = Math.pow(Math.sin(theta), 2.0D);
    double th2Sin = Math.sin(2.0D * theta);
    double a = thCos2 / 2.0D * sgmX2 + thSin2 / 2.0D * sgmY2;
    double b = -(th2Sin / 4.0D * sgmX2) + th2Sin / 4.0D * sgmY2;
    double c = thSin2 / 2.0D * sgmX2 + thCos2 / 2.0D * sgmY2;
    double difX = 0.0D;
    double difY = 0.0D;
    int pixPos = 0;
    for (int iy = 0; iy < sizeY; iy++) {
      pixPos = sizeX * iy - 1;
      difY = iy - aveY;
      for (int ix = 0; ix < sizeX; ix++) {
        pixPos++;
        difX = ix - aveX;
        the2dGaussian[pixPos] = 
          offset + ampl * Math.exp(-(a * difX * difX + 2.0D * b * difX * difY + c * difY * difY));
      } 
    } 
    return the2dGaussian;
  }
  
  public static void output2D(double[] the2d, int cols, int rows) {
    String str = "Column: ";
    for (int col = 0; col < cols; col++) {
      str = String.valueOf(str) + String.format("%6d\t", new Object[] { Integer.valueOf(col) });
    } 
    System.out.println(str);
    int pixPos = -1;
    for (int row = 0; row < rows; row++) {
      str = "Row: " + row + "\t";
      for (int i = 0; i < cols; i++) {
        pixPos++;
        str = String.valueOf(str) + String.format("%6.3f\t", new Object[] { Double.valueOf(the2d[pixPos]) });
      } 
      System.out.println(str);
    } 
  }
}


/* Location:              /home/nico/Downloads/V3/DetectionStandalone.jar!/noa/ibv/fds/tools/TwoDGaussianCreator.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */