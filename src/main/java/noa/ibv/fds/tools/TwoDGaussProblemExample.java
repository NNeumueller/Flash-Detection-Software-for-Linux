package noa.ibv.fds.tools;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Random;
import noa.ibv.fds.work.ArrayToImage;
import noa.ibv.fds.work.TextAreaLogger;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;

public class TwoDGaussProblemExample {
  static final String RUN_LABEL = "TwoDGaussProblemExample v1.0";
  
  static String[] fdsEventModel = new String[] { "2dGaussian", 
      "2dHotPixel", 
      "2dCosmicRay", 
      "2DUserDefined" };
  
  static String eventModel = "";
  
  static double[] valParams = new double[7];
  
  static int x_width = 111;
  
  static int y_width = 111;
  
  static double fractNoise = 0.0D;
  
  public static void main(String[] args) throws Exception {
    int imXpos = 100;
    int imYpos = 200;
    TextAreaLogger.createAndShowGUI("TwoDGaussProblemExample v1.0 - Logger");
    myLoggerAppend("TwoDGaussProblemExample v1.0 for FDS event model examples fitting. [" + 
        ZonedDateTime.now(ZoneOffset.UTC) + "]");
    if (args.length > 0) {
      eventModel = selectExample(args);
      if (eventModel.equals(""))
        return; 
      valParams = setFdsExampleEventParams(eventModel);
    } else {
      TwoDGaussDialogGui myDialog = new TwoDGaussDialogGui();
      myDialog.createAndShowDialog();
      if (!myDialog.getStatus()) {
        myLoggerAppend("\tPleas, close this window");
        myLoggerAppend("TwoDGaussProblemExample v1.0 CANCELED !!! [" + 
            ZonedDateTime.now(ZoneOffset.UTC) + "]");
        return;
      } 
      eventModel = fdsEventModel[myDialog.getExampleModeCode()];
      int[] imSizeXY = myDialog.getImSizeXY();
      x_width = imSizeXY[0];
      y_width = imSizeXY[1];
      int[] meanPosXY = myDialog.getMeanPosXY();
      valParams[2] = meanPosXY[0];
      valParams[3] = meanPosXY[1];
      double[] paramsSixmaXY = myDialog.getParamsSigmaXY();
      valParams[4] = paramsSixmaXY[0];
      valParams[5] = paramsSixmaXY[1];
      double[] paramsSignal = myDialog.getParamsSignal();
      valParams[0] = paramsSignal[0];
      valParams[1] = paramsSignal[1];
      fractNoise = paramsSignal[2];
      double[] paramsRotAngle = myDialog.getParamsRotAngle();
      valParams[6] = paramsRotAngle[0];
    } 
    String[] strParams = { "amplitude", "offsetVal", "mean_Xpos", "mean_Ypos", "sigma_Xval", "sigma_Yval", "rotAngleRad" };
    double ampl = valParams[0];
    double offs = valParams[1];
    double cntX = valParams[2];
    double cntY = valParams[3];
    double sgmX = valParams[4];
    double sgmY = valParams[5];
    double thet = valParams[6];
    output2Dparams(x_width, y_width, strParams, valParams, eventModel, fractNoise);
    double[] inputData = 
      TwoDGaussianCreator.create2d(x_width, y_width, ampl, offs, cntX, cntY, sgmX, sgmY, thet);
    output2Darray(inputData, y_width, x_width);
    if (fractNoise > 0.0D) {
      addNoise2Darray(inputData, offs, fractNoise, 0L);
      output2Darray(inputData, y_width, x_width);
    } 
    double[] newStart = 
      guessInitFitParams(inputData, x_width, y_width);
    int maxNumbEval = 10000;
    int maxNumbIter = 1000;
    double[] optimalValues = 
      lsFit2DGaussianWithLM(inputData, x_width, newStart, maxNumbEval, maxNumbIter);
    out2DFitParams(strParams, valParams, optimalValues);
    double[] outputData = 
      TwoDGaussianCreator.create2d(
        x_width, y_width, 
        optimalValues[0], optimalValues[1], optimalValues[2], optimalValues[3], 
        optimalValues[4], optimalValues[5], optimalValues[6]);
    ArrayToImage.createAndShowImageGui(inputData, outputData, x_width, y_width, imXpos, imYpos);
    myLoggerAppend("\nPlease, close this window.\n");
    myLoggerAppend("TwoDGaussProblemExample v1.0 COMPLETED !!! [" + 
        ZonedDateTime.now(ZoneOffset.UTC) + "]");
  }
  
  private static void myLoggerAppend(String theText) {
    System.out.println(theText);
    TextAreaLogger.appendText(theText);
  }
  
  private static String selectExample(String[] args) {
    int eventModel = 0;
    if (args.length < 1) {
      eventModel = (int)(System.currentTimeMillis() / 1000L % 3L);
    } else {
      try {
        eventModel = Integer.parseInt(args[0]) - 1;
      } catch (Exception e) {
        eventModel = -1;
      } 
    } 
    if (eventModel < 0 || eventModel >= fdsEventModel.length) {
      argumentsHelp(1);
      return "";
    } 
    fractNoise = setOptionalNoise(args);
    return fdsEventModel[eventModel];
  }
  
  private static double setOptionalNoise(String[] args) {
    if (args.length < 2)
      return 0.0D; 
    double fractNoise = 0.0D;
    try {
      fractNoise = Double.parseDouble(args[1]);
    } catch (Exception e) {
      fractNoise = -1.0D;
    } 
    if (fractNoise < 0.0D || fractNoise >= 3.0D) {
      argumentsHelp(2);
      fractNoise = -1.0D;
    } 
    return fractNoise;
  }
  
  private static void argumentsHelp(int numCase) {
    int i;
    byte b;
    int j;
    String[] arrayOfString;
    switch (numCase) {
      case 1:
        myLoggerAppend("\nIllegal numerical argument for the event model, use one of the: 1, 2, or 3");
        i = 0;
        for (j = (arrayOfString = fdsEventModel).length, b = 0; b < j; ) {
          String str = arrayOfString[b];
          i++;
          myLoggerAppend("\t" + i + "\t=\t" + str);
          b++;
        } 
        break;
      case 2:
        myLoggerAppend("\nIllegal numerical argument for the optional noise, use float from 0.0 up to 3.0");
        break;
    } 
  }
  
  private static double[] setFdsExampleEventParams(String strExample) {
    double ampl = 0.0D;
    double offs = 0.0D;
    double cntX = 0.0D;
    double cntY = 0.0D;
    double sgmX = 0.0D;
    double sgmY = 0.0D;
    double thet = 0.0D;
    if (strExample.contains("2dGaussian")) {
      ampl = 50.0D;
      offs = 10.0D;
      cntX = 40.0D;
      cntY = 60.0D;
      sgmX = 10.0D;
      sgmY = 20.0D;
      thet = -0.5D;
    } 
    if (strExample.contains("2dHotPixel")) {
      ampl = 200.0D;
      offs = 10.0D;
      cntX = 65.0D;
      cntY = 55.0D;
      sgmX = 1.0D;
      sgmY = 1.0D;
      thet = 0.0D;
    } 
    if (strExample.contains("2dCosmicRay")) {
      ampl = 50.0D;
      offs = 10.0D;
      cntX = 55.0D;
      cntY = 70.0D;
      sgmX = 9.0D;
      sgmY = 1.0D;
      thet = 0.5D;
    } 
    double[] valParams = { ampl, offs, cntX, cntY, sgmX, sgmY, thet };
    return valParams;
  }
  
  private static void output2Dparams(int sizeX, int sizeY, String[] paramNameArr, double[] paramValueArr, String fdsEventModel, double fNoise) {
    myLoggerAppend("\nThe 2D-Gaussian construction parameters");
    myLoggerAppend("\tFDS event example model: " + fdsEventModel);
    String fN = "\t%-25s";
    myLoggerAppend(String.format(
          String.valueOf(fN) + "%8d", new Object[] { "Size X:", Integer.valueOf(sizeX) }));
    myLoggerAppend(String.format(
          String.valueOf(fN) + "%8d", new Object[] { "Size Y:", Integer.valueOf(sizeY) }));
    myLoggerAppend(String.format(
          String.valueOf(fN) + "%8.3f", new Object[] { String.valueOf(paramNameArr[0]) + ":", Double.valueOf(paramValueArr[0]) }));
    myLoggerAppend(String.format(
          String.valueOf(fN) + "%8.3f", new Object[] { String.valueOf(paramNameArr[1]) + ":", Double.valueOf(paramValueArr[1]) }));
    myLoggerAppend(String.format(
          String.valueOf(fN) + "%8.3f %8.3f", new Object[] { String.valueOf(paramNameArr[2]) + " & " + paramNameArr[3] + ":", Double.valueOf(paramValueArr[2]), Double.valueOf(paramValueArr[3]) }));
    myLoggerAppend(String.format(
          String.valueOf(fN) + "%8.3f %8.3f", new Object[] { String.valueOf(paramNameArr[4]) + " & " + paramNameArr[5] + ":", Double.valueOf(paramValueArr[4]), Double.valueOf(paramValueArr[5]) }));
    myLoggerAppend(String.format(
          String.valueOf(fN) + "%8.3f %8.3f", new Object[] { String.valueOf(paramNameArr[6]) + " [rad,deg]:", Double.valueOf(paramValueArr[6]), Double.valueOf(Math.toDegrees(paramValueArr[6])) }));
    if (fNoise > 0.0D) {
      myLoggerAppend(String.format(
            String.valueOf(fN) + "%8.3f", new Object[] { "Noise (offset fraction):", Double.valueOf(fNoise) }));
    } else {
      myLoggerAppend(String.format(
            String.valueOf(fN) + "%8.3f", new Object[] { "Noise not requested:", Double.valueOf(fNoise) }));
    } 
  }
  
  private static void output2Darray(double[] the2d, int cols, int rows) {
    myLoggerAppend("\nThe created 2D-function values");
    String str = "\tColumn (X):";
    for (int col = 0; col < cols; col++) {
      str = String.valueOf(str) + String.format("       %02d", new Object[] { Integer.valueOf(col) });
    } 
    myLoggerAppend(str);
    int pixPos = -1;
    for (int row = 0; row < rows; row++) {
      str = "\tRow (Y): " + String.format("%02d", new Object[] { Integer.valueOf(row) });
      for (int i = 0; i < cols; i++) {
        pixPos++;
        str = String.valueOf(str) + String.format("%9.3f", new Object[] { Double.valueOf(the2d[pixPos]) });
      } 
      myLoggerAppend(str);
    } 
  }
  
  private static void addNoise2Darray(double[] inputData, double base, double fractNoise, long seed) {
    myLoggerAppend("\nApplying normalised random noise");
    myLoggerAppend("\tRandom noise = base*fraction*random");
    myLoggerAppend("\tSignal base: " + base);
    myLoggerAppend("\tNoise fraction: " + fractNoise);
    double weight = base * fractNoise;
    Random rand = null;
    if (seed < 0L) {
      myLoggerAppend("\tRandom number generator: No seed applied");
      rand = new Random();
    } else {
      myLoggerAppend("\tRandom number generator seed: " + seed);
      rand = new Random(seed);
    } 
    myLoggerAppend("\tNoise values scale factor: " + String.format("%8.3f", new Object[] { Double.valueOf(weight) }));
    for (int i = 0; i < inputData.length; i++)
      inputData[i] = inputData[i] + weight * rand.nextGaussian(); 
  }
  
  private static double[] guessInitFitParams(double[] inputData, int x_width, int y_width) {
    myLoggerAppend("\nGuessing initial parameters for the 2D-fitting");
    double[][] xyHist = evalHistXandY(inputData, x_width, y_width);
    outXandYhist(xyHist);
    double[] xRoughParams = evalHistParams(xyHist[0]);
    double[] yRoughParams = evalHistParams(xyHist[1]);
    double[] xOptimParams = opt1dGaussianFit(xyHist[0], xRoughParams);
    double[] yOptimParams = opt1dGaussianFit(xyHist[1], yRoughParams);
    outHistParams(xRoughParams, yRoughParams, xOptimParams, yOptimParams);
    double[] newStart = { Math.max(xOptimParams[0], yOptimParams[0]), 
        0.5D * (xOptimParams[1] + yOptimParams[1]), 
        xOptimParams[2], 
        yOptimParams[2], 
        xOptimParams[3], 
        yOptimParams[3], 
        0.0D };
    return newStart;
  }
  
  private static double[][] evalHistXandY(double[] inputData, int x_width, int y_width) {
    double[] inputXhist = new double[x_width];
    double[] inputYhist = new double[y_width];
    int i = 0;
    int ix = 0;
    int iy = 0;
    byte b;
    int j;
    double[] arrayOfDouble1;
    for (j = (arrayOfDouble1 = inputData).length, b = 0; b < j; ) {
      double val = arrayOfDouble1[b];
      ix = i % x_width;
      iy = i / x_width;
      inputXhist[ix] = inputXhist[ix] + val;
      inputYhist[iy] = inputYhist[iy] + val;
      i++;
      b++;
    } 
    double[][] xyHist = new double[2][];
    xyHist[0] = inputXhist;
    xyHist[1] = inputYhist;
    return xyHist;
  }
  
  private static void outXandYhist(double[][] inputXandYhist) {
    myLoggerAppend("\tThe 2D function's x-axis and the y-axis cumulative histograms");
    String strX = "";
    byte b;
    int i;
    double[] arrayOfDouble1;
    for (i = (arrayOfDouble1 = inputXandYhist[0]).length, b = 0; b < i; ) {
      double val = arrayOfDouble1[b];
      strX = String.valueOf(strX) + String.format("\t%.3f", new Object[] { Double.valueOf(val) });
      b++;
    } 
    String strY = "";
    double[] arrayOfDouble2;
    for (int j = (arrayOfDouble2 = inputXandYhist[1]).length; i < j; ) {
      double val = arrayOfDouble2[i];
      strY = String.valueOf(strY) + String.format("\t%.3f", new Object[] { Double.valueOf(val) });
      i++;
    } 
    myLoggerAppend("\t\tX_hist:" + strX);
    myLoggerAppend("\t\tY_hist:" + strY);
  }
  
  private static double[] evalHistParams(double[] hist) {
    double valMax = -100000.0D;
    double valMin = 100000.0D;
    double posMax = 0.0D;
    int i = 0;
    byte b;
    int j;
    double[] arrayOfDouble1;
    for (j = (arrayOfDouble1 = hist).length, b = 0; b < j; ) {
      double val = arrayOfDouble1[b];
      if (val >= valMax) {
        valMax = val;
        posMax = i;
      } 
      if (val <= valMin)
        valMin = val; 
      i++;
      b++;
    } 
    int s1 = 0;
    int s2 = 0;
    double halfMaxVal = (valMax + valMin) / 2.0D;
    int n;
    for (n = 0; n < (int)posMax; n++) {
      if (hist[n] >= halfMaxVal) {
        s1 = n;
        break;
      } 
    } 
    for (n = (int)posMax; n < hist.length; n++) {
      if (hist[n] <= halfMaxVal) {
        s2 = n;
        break;
      } 
    } 
    double valSgm = (s2 - s1 + 1) / 2.0D;
    double[] paramsHist = new double[4];
    paramsHist[0] = valMax;
    paramsHist[1] = valMin;
    paramsHist[2] = posMax;
    paramsHist[3] = valSgm;
    return paramsHist;
  }
  
  private static double[] opt1dGaussianFit(double[] inputData, double[] initParams) {
    OneDGaussianFunction odgf = new OneDGaussianFunction(inputData.length);
    LeastSquaresBuilder lsb = new LeastSquaresBuilder();
    lsb.model(odgf.retMVF(), odgf.retMMF());
    lsb.target(inputData);
    lsb.start(initParams);
    lsb.maxEvaluations(10000);
    lsb.maxIterations(1000);
    LevenbergMarquardtOptimizer lmo = new LevenbergMarquardtOptimizer();
    double[] optimalValues = null;
    try {
      LeastSquaresOptimizer.Optimum lsoo = lmo.optimize(lsb.build());
      optimalValues = lsoo.getPoint().toArray();
    } catch (Exception e) {
      myLoggerAppend(e.toString());
    } 
    return optimalValues;
  }
  
  private static void outHistParams(double[] xRoughParams, double[] yRoughParams, double[] xOptimParams, double[] yOptimParams) {
    myLoggerAppend("\tThe X and Y histograms rough and 1D-Gaussian fitting parameters");
    String outForm = "\t\t%-10s %8.3f %8.3f %8.3f %8.3f";
    myLoggerAppend(String.format(
          outForm, new Object[] { "Amplitude:", Double.valueOf(xRoughParams[0]), Double.valueOf(xOptimParams[0]), Double.valueOf(yRoughParams[0]), Double.valueOf(yOptimParams[0]) }));
    myLoggerAppend(String.format(
          outForm, new Object[] { "Center:", Double.valueOf(xRoughParams[1]), Double.valueOf(xOptimParams[1]), Double.valueOf(yRoughParams[1]), Double.valueOf(yOptimParams[1]) }));
    myLoggerAppend(String.format(
          outForm, new Object[] { "Sigma:", Double.valueOf(xRoughParams[2]), Double.valueOf(xOptimParams[2]), Double.valueOf(yRoughParams[2]), Double.valueOf(yOptimParams[2]) }));
    myLoggerAppend(String.format(
          outForm, new Object[] { "Offset:", Double.valueOf(xRoughParams[3]), Double.valueOf(xOptimParams[3]), Double.valueOf(yRoughParams[3]), Double.valueOf(yOptimParams[3]) }));
  }
  
  private static double[] lsFit2DGaussianWithLM(double[] inputData, int x_width, double[] newStart, int maxNumbEval, int maxNumbIter) {
    myLoggerAppend("\nApplying Least-Squares with LM minimization for 2D-Gaussian fitting");
    TwoDGaussianFunction tdgf = new TwoDGaussianFunction(x_width, inputData.length);
    LeastSquaresBuilder lsb = new LeastSquaresBuilder();
    lsb.model(tdgf.retMVF(), tdgf.retMMF());
    lsb.target(inputData);
    lsb.start(newStart);
    lsb.maxEvaluations(maxNumbEval);
    lsb.maxIterations(maxNumbIter);
    LevenbergMarquardtOptimizer lmo = new LevenbergMarquardtOptimizer();
    LeastSquaresOptimizer.Optimum lsoo = lmo.optimize(lsb.build());
    double[] optimalValues = lsoo.getPoint().toArray();
    myLoggerAppend("\tIteration number:\t" + lsoo.getIterations());
    myLoggerAppend("\tEvaluation number:\t" + lsoo.getEvaluations());
    return optimalValues;
  }
  
  private static void out2DFitParams(String[] strParams, double[] originParams, double[] optimalValues) {
    myLoggerAppend("\nThe 2D-Gausian original and fitting parameters");
    myLoggerAppend("\tParameter\tOriginal  Fitting");
    for (int i = 0; i < strParams.length; i++) {
      String str = String.format("\t%s\t%8.3f %8.3f", new Object[] { strParams[i], Double.valueOf(originParams[i]), Double.valueOf(optimalValues[i]) });
      myLoggerAppend(str);
    } 
  }
}


/* Location:              /home/nico/Downloads/V3/DetectionStandalone.jar!/noa/ibv/fds/tools/TwoDGaussProblemExample.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */