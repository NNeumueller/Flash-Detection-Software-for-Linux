/** 
 * Copyright (c) 2022 European Space Agency
 * This file is part of FDS
 * FDS is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * FDS is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with FDS. If not, see <https://www.gnu.org/licenses/>.
 **/

package sample;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import javafx.scene.control.Alert;
import noa.ibv.fds.tools.OneDGaussianFunction;
import org.apache.commons.math3.exception.TooManyIterationsException;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;

import noa.ibv.fds.tools.TwoDGaussianFunction;
import noa.ibv.fds.work.TextAreaLogger;

import static sample.Controller.folderPath;
import static sample.Detection.yCoord;

/**
 * Performs the LM algorithm.
 * @author ibellas Achlatis Stefanos Christofidi Georgia
 * @version $Id: MoonEventAnalyser.java 23322 2022-06-10 11:43:57Z ibellas $
 */
public class MoonEventAnalyser {

    private static final String TITLE = "MoonEventAnalyser v0.5";

    static final int NUMBER_EVAL_MAX = 10000;
    static final int NUMBER_ITER_MAX = 1000;

    private static final boolean LOG_OUT = true;

    //
    static String[] strParams = {
            "amplitude", "offsetVal", "mean_Xpos", "mean_Ypos", "sigma_Xval", "sigma_Yval", "rotAngleRad"
    };

    private static int frameWidth = 0;
    private static int frameHeight = 0;

    private static double[] eventFrame;

    /**
     *Initialises the process.
     * @param args
     */
    public static void main(String[] args) {
        init();
    }

    /**
     *Initialises the logger.
     */
    public static void init() {
        /*
         * Initialize the program logger
         */
        TextAreaLogger.createAndShowGUI(TITLE + " - Logger");
        myLoggerAppend(
                TITLE + " [" + ZonedDateTime.now(ZoneOffset.UTC) + "]");

    }

    /**
     *Prints a text to the terminal.
     * @param theText
     */
    private static void myLoggerAppend(String theText) {
        System.out.println(theText);
    }

    /**
     *Guesses the initial parameters for the 2 dimensional Gaussian fit.
     *
     * @param inputData
     * @param x_width
     * @param y_width
     * @return
     */
    public static double[] guessInitFitParams(double[] inputData, int x_width, int y_width) {

        try{
        myLoggerAppend("\nGuessing initial parameters for the 2D-fitting");

        // TheX-axis and Y-axis cumulative histograms
        double[][] xyHist = evalHistXandY(inputData, x_width, y_width);

//        System.out.println(x_width);
//        System.out.println(y_width);

        if (LOG_OUT) outXandYhist(xyHist);

        // initial guess
        double[] xRoughParams = evalHistParams(xyHist[0]);
        double[] yRoughParams = evalHistParams(xyHist[1]);

        // fit 1D (OneDGaussianFunction) to x-hist and 1D to y-hist
        double[] xOptimParams = opt1dGaussianFit(xyHist[0], xRoughParams);
        double[] yOptimParams = opt1dGaussianFit(xyHist[1], yRoughParams);


        // create initial guess parameters for 2D (TwoDGaussianFunction) fitting
        double[] newStart = {
                Math.max(xOptimParams[0], yOptimParams[0]),		// Amplitude
                0.5 * (xOptimParams[1] + yOptimParams[1]),		// Offset
                xOptimParams[2],								// Mean_X
                yOptimParams[2],								// Mean_Y
                xOptimParams[3],								// Sigma_X
                yOptimParams[3],								// Sigma_Y
                0												// RotationAngleDeg (not fit here)
        };
        return newStart;
        }
        catch (TooManyIterationsException | IOException e) {
            e.printStackTrace();
            Alert alert2 = new Alert(Alert.AlertType.ERROR, e + "");
            alert2.setTitle("Error: the algorithm does not converge.");
            alert2.setHeaderText("Error: the algorithm does not converge.");
            alert2.setContentText("Bad image quality or invalid hyperparameters.(Event Path: " + Detection.path + ").");
            alert2.showAndWait();
        }
        return null;
    }

    /**
     *Evaluates the histogram.
     * @param inputData
     * @param x_width
     * @param y_width
     * @return
     */
    private static double[][] evalHistXandY(double[] inputData, int x_width, int y_width) {
        double[] inputXhist = new double[x_width];
        double[] inputYhist = new double[y_width];
        int i = 0;
        int ix = 0;
        int iy = 0;
        for (double val : inputData) {
            ix = i%x_width;
            iy = i/x_width;
            inputXhist[ix] += val;
            inputYhist[iy] += val;
            i ++;
        }
        double[][] xyHist = new double [2][];
        xyHist[0] = inputXhist;
        xyHist[1] = inputYhist;

        return xyHist;
    }

    /**
     * Prints the histogram values.
     * @param inputXandYhist
     */
    private static void outXandYhist(double[][] inputXandYhist) {
        myLoggerAppend("\tThe 2D function's x-axis and the y-axis cumulative histograms");
        String strX = "";
        for (double val : inputXandYhist[0]) {
            strX += String.format("\t%.3f", val);
        }
        String strY = "";
        for (double val : inputXandYhist[1]) {
            strY += String.format("\t%.3f", val);
        }
        myLoggerAppend("\t\tX_hist:" + strX);
        myLoggerAppend("\t\tY_hist:" + strY);

    }

    /**
     *Evaluates the histogram parameters
     * @param hist
     * @return
     */
    private static double[] evalHistParams(double[] hist) {
        double valMax = -100000.0;
        double valMin = 100000.0;
        double posMax = 0;
        //
        int i = 0;
        for (double val : hist) {
            if (val >= valMax) {
                valMax = val;
                posMax = i;
            }
            if (val <= valMin) {
                valMin = val;
            }
            i++;
        }
        //
        int s1 = 0;
        int s2 = 0;
        double halfMaxVal = (valMax + valMin) / 2.0;
        for (int n=0; n<(int)posMax; n++) {
            if (hist[n] >= halfMaxVal) {
                s1 = n;
                break;
            }
        }
        for (int n=(int)posMax; n<hist.length; n++) {
            if (hist[n] <= halfMaxVal) {
                s2 = n;
                break;
            }
        }
        double valSgm = (s2 - s1 + 1) / 2.0;

        double[] paramsHist = new double[4];
        paramsHist[0] = valMax;			// Amplitude
        paramsHist[1] = valMin;			// Offset
        paramsHist[2] = posMax;			// Center
        paramsHist[3] = valSgm;			// Sigma

        return paramsHist;
    }


    /**
     *Applies the 1D Gaussian Fit.
     * @param inputData
     * @param initParams
     * @return
     */
    private static double[] opt1dGaussianFit(double[] inputData, double[] initParams) throws IOException {
        // Construct a one-dimensional Gaussian function model
        OneDGaussianFunction odgf = new OneDGaussianFunction(inputData.length);

        // Pprepare construction of LeastSquresProblem by builder
        LeastSquaresBuilder lsb = new LeastSquaresBuilder();

        // Set model function and its jacobian
        lsb.model(odgf.retMVF(), odgf.retMMF());

        //set target data
        lsb.target(inputData);

        //set initial parameters
        lsb.start(initParams);

        //set upper limit of evaluation time
        lsb.maxEvaluations(10000);

        //set upper limit of iteration time
        lsb.maxIterations(1000);

        LevenbergMarquardtOptimizer lmo = new LevenbergMarquardtOptimizer();

        double[] optimalValues = null;
        try {
            //do LevenbergMarquardt optimization
            LeastSquaresOptimizer.Optimum lsoo = lmo.optimize(lsb.build());

            //get optimized parameters
            optimalValues = lsoo.getPoint().toArray();

        } catch (TooManyIterationsException e) {
            System.out.println("No event detected");
            TestingLMA.resultsLMA = "No event detected.";
            WriteToCSV.noEventFlag = true;
            WriteToCSV.createCSV(folderPath, Detection.path, Detection.xCoord, yCoord);

            System.out.println("Creates CSV");

            myLoggerAppend(e.toString());
            Alert alert2 = new Alert(Alert.AlertType.ERROR, e + "");
            alert2.setTitle("Error");
            alert2.setHeaderText(null);
            alert2.setContentText("Bad image quality or invalid hyperparameters. (Event Path: " + Detection.path + ").");
            alert2.showAndWait();
        }
        catch (Exception e) {
            myLoggerAppend(e.toString());
            Alert alert2 = new Alert(Alert.AlertType.ERROR, e + "");
            alert2.setTitle("Error");
            alert2.setHeaderText(null);
            alert2.setContentText("Please inspect event manually. (Event Path: " + Detection.path + ").");
            alert2.showAndWait();
        }

        return optimalValues;
    }


    /**
     * Fits a 2D-Gaussian to the data through a Least-Squares optimization applying Levenberg-Marquardt
     * minimization algorithm.
     *
     * @param inputData
     * @param x_width
     * @param newStart
     * @param maxNumbEval
     * @param maxNumbIter
     * @return
     */
    public static double[] lsFit2DGaussianWithLM(
            double[] inputData, int x_width,
            double[] newStart,
            int maxNumbEval, int maxNumbIter) {
        myLoggerAppend("\nApplying Least-Squares with LM minimization for 2D-Gaussian fitting");

        // Construct a two-dimensional Gaussian function model
        TwoDGaussianFunction tdgf = new TwoDGaussianFunction(x_width, inputData.length);

        // Prepare construction of LeastSquresProblem by builder
        LeastSquaresBuilder lsb = new LeastSquaresBuilder();

        // Set model function and its jacobian
        lsb.model(tdgf.retMVF(), tdgf.retMMF());

        // Set target data and the initial parameters
        lsb.target(inputData);
        lsb.start(newStart);

        //set upper limit of evaluation time and of iteration time
        lsb.maxEvaluations(maxNumbEval);
        lsb.maxIterations(maxNumbIter);

        LevenbergMarquardtOptimizer lmo = new LevenbergMarquardtOptimizer();


        try {
            // Do LevenbergMarquardt optimization
            LeastSquaresOptimizer.Optimum lsoo = lmo.optimize(lsb.build());


            // Get optimized parameters

            final double[] optimalValues = lsoo.getPoint().toArray();
            myLoggerAppend("\tIteration number:\t" + lsoo.getIterations());
            myLoggerAppend("\tEvaluation number:\t" + lsoo.getEvaluations());


            return optimalValues;
        }
        catch (TooManyIterationsException e) {
            e.printStackTrace();
            Alert alert2 = new Alert(Alert.AlertType.ERROR, e + "");
            alert2.setTitle("Error");
            alert2.setHeaderText(null);
            alert2.setContentText("Bad image quality or invalid hyperparameters. (Event Path:" + Detection.path + ").");
            alert2.showAndWait();
        }
        return null;
    }


    /**
     * Prints the output values on the terminal
     * @param strParams
     * @param optimalValues
     */
    public static void out2DFitParams(
            String[] strParams,
            double[] optimalValues
    ) {
        myLoggerAppend("\nThe 2D-Gausian original and fitting parameters");
        //
        optimalValues[4] = Math.abs(optimalValues[4]);
        optimalValues[5] = Math.abs(optimalValues[5]);
        myLoggerAppend("\tParameter\t Fitting");
        for (int i=0; i<strParams.length; i++ ) {
            String str = String.format("\t%s\t%8.3f", strParams[i], optimalValues[i]);
            myLoggerAppend(str);
        }

        DecimalFormat df = new DecimalFormat("0.000");
        WriteToCSV.FWHM_x = String.valueOf(df.format(2.3*optimalValues[4]));
        WriteToCSV.FWHM_y = String.valueOf(df.format(2.3*optimalValues[5]));
    }
}
