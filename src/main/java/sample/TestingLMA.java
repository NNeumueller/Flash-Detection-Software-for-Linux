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

import javafx.scene.control.Alert;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsFactory;
import nom.tam.util.FitsOutputStream;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static sample.Controller.folderPath;
import static sample.Controller.param2;
import static sample.Local.img2int;
import static sample.MoonEventAnalyser.*;

/**
 * Applies the LMA algorithm and checks the ROI.
 * @author Achlatis Stefanos Christofidi Georgia
 */

public class TestingLMA {

    public static String resultsLMA = "";

    public static BufferedImage outputImage;

    public static BufferedImage test1(BufferedImage frameImage, int eventXpos, int eventYpos) throws Exception {

            //System.out.println(Controller.ROIsize);
            int eventWidth = Controller.ROIsize;
            int eventHeight = Controller.ROIsize;
            int imageWidth = frameImage.getWidth();
            int imageHeight = frameImage.getHeight();
            int x0 = eventXpos - eventWidth / 2;
            int y0 = (imageHeight - eventYpos) - eventHeight / 2;
            double[] eventFrameArray;
            //BufferedImage outputImage;

            if (x0 > imageWidth || y0 > imageHeight || eventXpos < 0 || eventYpos < 0 || eventXpos > imageWidth || eventYpos > imageHeight) {
                System.out.println("The coordinates you entered are: " + eventXpos + " " + eventYpos + ".");
                System.out.println("" + x0 + " " + y0 + ".");
                System.out.println("" + imageWidth + " " + imageHeight + ".");
                System.out.println("Warning: check metadata file and try again.");
                resultsLMA = "Warning: The coordinates of the event are incorrect. Check metadata file and try again. If that doesn't solve the " +
                        "problem, consider trying with a smaller ROI.";
                WriteToCSV.unknownFlag = true;
                Detection.ROIflag = false;
                return null;
            }

            else if ((x0 + eventWidth) > imageWidth && !((y0 + eventHeight) >= imageHeight) && y0 < 0) {
                //y outside of the upper edge and x outside of the right edge
                int numPixelsOutOfImageY = Math.abs(y0);
                //System.out.println(numPixelsOutOfImageY);
                int eventBetweenHeight = eventHeight - 2 * numPixelsOutOfImageY;
                //System.out.println(eventBetweenHeight);
                eventHeight = eventBetweenHeight;

                //x outside of limits
                int numPixelsOutOfImageX = x0 + eventWidth - imageWidth;
                //System.out.println(numPixelsOutOfImageX);
                int eventBetweenWidth = eventWidth - 2 * numPixelsOutOfImageX;
                //System.out.println(eventBetweenWidth);
                eventWidth = eventBetweenWidth;

                if (eventWidth < eventHeight) {
                    eventHeight = eventWidth;
                    x0 = eventXpos - eventWidth / 2;
                    y0 = (imageHeight - eventYpos) - eventHeight / 2;
                } else {
                    eventWidth = eventHeight;
                    x0 = eventXpos - eventWidth / 2;
                    y0 = 0;
                }
            }

            else if (x0 < 0) {
                //x outside of the left edge
                int numPixelsOutOfImageX = Math.abs(x0);
                //System.out.println(numPixelsOutOfImageX);
                int eventBetweenWidth = eventWidth - 2 * numPixelsOutOfImageX;
                //System.out.println(eventBetweenWidth);
                eventWidth = eventBetweenWidth;
                eventHeight = eventWidth;

                x0 = eventXpos - eventWidth / 2;
                y0 = (eventYpos) - eventHeight / 2;
            }

            if (y0 < 0) {
                //y outside of upper edge
                int numPixelsOutOfImageY = Math.abs(y0);
                //System.out.println(numPixelsOutOfImageY);
                int eventBetweenHeight = eventHeight - 2 * numPixelsOutOfImageY;
                //System.out.println(eventBetweenHeight);
                eventWidth = eventBetweenHeight;
                eventHeight = eventWidth;

                x0 = eventXpos - eventWidth / 2;
                y0 = 0;
            }
            else if ((x0 + eventWidth) > imageWidth || (y0 + eventHeight) >= imageHeight) {
                //outside of image limits
                int eventBetweenWidth = 0;
                int eventBetweenHeight = 0;

                if ((x0 + eventWidth) > imageWidth && !((y0 + eventHeight) >= imageHeight)) {
                    //x is outside of the limit
                    int numPixelsOutOfImageX = x0 + eventWidth - imageWidth;
                    //System.out.println(numPixelsOutOfImageX);
                    eventBetweenWidth = eventWidth - 2 * numPixelsOutOfImageX;
                    //System.out.println(eventBetweenWidth);
                    eventWidth = eventBetweenWidth;
                    eventHeight = eventWidth;
                }

                else if (!((x0 + eventWidth) > imageWidth) && ((y0 + eventHeight) >= imageHeight)) {
                    //y is outside the upper limit
                    int numPixelsOutOfImageY = y0 + eventHeight - imageHeight;
                    System.out.println(numPixelsOutOfImageY);
                    eventBetweenHeight = eventHeight - 2 * numPixelsOutOfImageY;
                    System.out.println(eventBetweenHeight);
                    eventHeight = eventBetweenHeight;
                    if (eventWidth<=eventHeight) {
                        eventHeight = eventWidth;
                    }
                    else eventWidth = eventHeight;
                }

                else {
                    //x and y outside of the limits
                    int numPixelsOutOfImageX = x0 + eventWidth - imageWidth;
                    int numPixelsOutOfImageY = y0 + eventHeight - imageHeight;
                    eventBetweenWidth = eventWidth - numPixelsOutOfImageX;
                    eventBetweenHeight = eventHeight - numPixelsOutOfImageY;
                    if (eventBetweenWidth <= eventBetweenHeight) {
                        eventWidth = eventBetweenWidth;
                        eventHeight = eventBetweenWidth;
                    } else {
                        eventWidth = eventBetweenHeight;
                        eventHeight = eventBetweenHeight;
                    }
                }
                x0 = eventXpos - eventWidth / 2;
                y0 = (imageHeight - eventYpos) - eventHeight / 2;
                // y0 = (eventYpos) - eventHeight / 2;
            }

            if (eventWidth == 0 || eventHeight == 0) throw new IllegalArgumentException ("The event is at the edge of the image and it cannot be analysed.");
            // Get the event frame
            System.out.println(eventWidth + "  " + eventHeight + " " + x0 + " " + y0);

            eventFrameArray = new double[eventWidth * eventHeight];
            eventFrameArray = frameImage.getData().getPixels(x0, y0, eventWidth, eventHeight, eventFrameArray);
            System.out.println(" INFO " + "got event frame data into double array of size " + eventFrameArray.length);

            outputImage = new BufferedImage(eventWidth, eventHeight, BufferedImage.TYPE_BYTE_GRAY);
            WritableRaster raster = outputImage.getRaster();
            raster.setSamples(0, 0, eventWidth, eventHeight, 0, eventFrameArray);

       /* if(Detection.dataType=="png") {


            File genericoutput3 = new File(Detection.path + "/Detection_Results/ROI_Image.png");
            File genericoutputROI = new File(folderPath + "/Detection_Results/" + Detection.onlyEventName + "_ROI_Image.png");
            if (outputImage != null) {
                try {
                    ImageIO.write(outputImage, "png", genericoutput3);
                    ImageIO.write(outputImage, "png", genericoutputROI);
                } catch (IOException e) {
                    e.printStackTrace();
                    Alert alert2 = new Alert(Alert.AlertType.ERROR, e + "");
                    alert2.setTitle("Error");
                    alert2.setHeaderText(null);
                    alert2.setContentText("Image couldn't be written. (Event Path: " + Detection.path + ").");
                    alert2.showAndWait();
                }
            }
        }
        else if (Detection.dataType.equals("fits")){

            Fits ROIFits = new Fits();
            Fits particularROIFits = new Fits();

            if (outputImage != null) {
                ROIFits.addHDU(FitsFactory.hduFactory(img2int(outputImage)));
                particularROIFits.addHDU(FitsFactory.hduFactory(img2int(outputImage)));
            }
            FitsOutputStream out3 = new FitsOutputStream(new FileOutputStream(new File(Detection.path + "/Detection_Results/ROI_Image.fits")));
            FitsOutputStream out4 = new FitsOutputStream(new FileOutputStream(new File(folderPath + "/Detection_Results/" + Detection.onlyEventName + "_ROI_Image.fits")));


            if (outputImage != null) {
                ROIFits.write(out3);
                particularROIFits.write(out4);
            }

            if (outputImage != null) {
                out3.close();
                out4.close();
            }
        }

            raster.setSamples(0, 0, eventWidth, eventHeight, 0, eventFrameArray);
            ImageIO.write(outputImage, "png", new File("OURNEWImage.png"));
*/
            /*
             * Prepare initial parameters for the 2D-fitting from the X-axis and Y-axis cumulative distributions
             */
            try{
           // System.out.println(eventHeight);
            double[] newStart =
                    guessInitFitParams(eventFrameArray, eventWidth, eventHeight);

            /*
             * Apply Levenberg-Marquardt algorithm for optimal least-squares fitting of 2D-Gaussian
             */
            double[] optimalValues =
                    lsFit2DGaussianWithLM(eventFrameArray, eventWidth, newStart, NUMBER_EVAL_MAX, NUMBER_ITER_MAX);
            //output data
            out2DFitParams(strParams, optimalValues);

            double sigma_X = optimalValues[4];

            double sigma_Y = optimalValues[5];

            double mean_Xpos = optimalValues[2];
            double mean_Ypos = optimalValues[3];

            double max_mean = 0;
            double min_mean = 0;

            if (mean_Ypos > mean_Xpos) {
                max_mean = mean_Ypos;
                min_mean = mean_Xpos;
            } else {
                max_mean = mean_Xpos;
                min_mean = mean_Ypos;
            }

            double max_sigma = 0;
            double min_sigma = 0;

            if (sigma_Y > sigma_X) {
                max_sigma = sigma_Y;
                min_sigma = sigma_X;
            } else {
                max_sigma = sigma_X;
                min_sigma = sigma_Y;
            }

            double sigma_ratio = max_sigma / min_sigma;

            double sigma = max_sigma;
            if (sigma_ratio == 1) {
                System.out.println("No event detected");
                resultsLMA = "No event detected. The ROI is completely black. (Coordinates: " + eventXpos + ", " + eventYpos + ").";
                WriteToCSV.noEventFlag = true;
            } else if (sigma_ratio > Controller.param14 && sigma < Controller.param15 || (sigma < 1 && min_mean/max_mean < 1.2 && min_mean/max_mean > 0.8 )) {
                    System.out.println("Cosmic ray detected");
                    resultsLMA = "Cosmic ray detected. (Coordinates: " + eventXpos +", " + eventYpos + ").";
                    WriteToCSV.cosmicRayFlag = true;

            } else if (sigma > Controller.param1 || sigma_ratio > param2) {
                System.out.println("No event detected");
                resultsLMA = "No event detected. (Coordinates: " + eventXpos +", " + eventYpos + ").";
                WriteToCSV.noEventFlag = true;
            } else if (sigma_ratio > Controller.param3 && sigma < Controller.param4) {
                System.out.println("No event detected");
                resultsLMA = "No event detected. (Coordinates: " + eventXpos +", " + eventYpos + ").";
                WriteToCSV.noEventFlag = true;
            } else if (sigma_ratio > Controller.param5 && sigma > Controller.param6 && (max_mean/min_mean < Controller.param7)) {
                System.out.println("No event detected");
                resultsLMA = "No event detected. (Coordinates: " + eventXpos +", " + eventYpos + ").";
                WriteToCSV.noEventFlag = true;
            } else if (sigma_ratio > Controller.param8 && sigma > Controller.param9) {
                System.out.println("Satellite detected");
                resultsLMA = "Satellite detected. (Coordinates: " + eventXpos +", " + eventYpos + ").";
                WriteToCSV.satelliteFlag = true;
                WriteToCSV.NEOFlag = false;
            } else if (sigma_ratio < Controller.param10 && sigma_ratio >= Controller.param11) {
                System.out.println("Cosmic Ray or Impact Flash");
                resultsLMA = "Cosmic Ray or Impact Flash. (Coordinates: " + eventXpos +", " + eventYpos + ").";
                WriteToCSV.cosmicRayFlag = true;
                WriteToCSV.NEOFlag = true;
            } else if (sigma_ratio < Controller.param12 && sigma > Controller.param13) {
                System.out.println("Impact flash detected");
                resultsLMA = "Impact flash detected. (Coordinates: " + eventXpos +", " + eventYpos + ").";
                WriteToCSV.NEOFlag = true;
                WriteToCSV.satelliteFlag = false;
            }  else {
                System.out.println("Faint Event (Unknown).");
                resultsLMA = "Faint Event (Unknown). (Coordinates: " + eventXpos +", " + eventYpos + ").";
                WriteToCSV.unknownFlag = true;
            }

       /*     try {
                ImageIO.write(outputImage, "png", new File("ThisROI.png"));
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert2 = new Alert(Alert.AlertType.ERROR, e + "");
                alert2.setTitle("Error");
                alert2.setHeaderText(null);
                alert2.setContentText("Image couldn't be written.");
                alert2.showAndWait();
            }*/
            return outputImage;
        }
     catch (NullPointerException ex) {
         ex.printStackTrace();
         Alert alert2 = new Alert(Alert.AlertType.ERROR, ex + "");
         alert2.setTitle("Error");
         alert2.setResizable(true);
         alert2.setHeaderText(null);
         alert2.setContentText("The event is at the edge of the image and it cannot be analysed. Please check manually of try using a smaller ROI. (Event Path: " + Detection.path + ").");
         System.out.println("No event detected");
         resultsLMA = "No event detected. If the event coordinates are on the edge of the image, please check manually.";
         WriteToCSV.noEventFlag = true;
         alert2.showAndWait();
         return outputImage;
    }
    }
    }

