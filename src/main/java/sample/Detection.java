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
import javafx.stage.Stage;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.ImageHDU;
import nom.tam.util.FitsOutputStream;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.*;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import static sample.Calibration.euclideanDistance;
import static sample.CircleFit.getEdge;
import static sample.Controller.*;
import static sample.Local.img2int;
import static sample.TestingLMA.resultsLMA;
import static sample.TestingLMA.test1;

/** Performs detection on a directory of frames containing an impact flash and writes images in the detection results directory
 *  After the flat and dark calibration, the average image of all the frames is calculated and subtracted by the event image.
 *  Then, if the event is inside the limb and if it is not a hot pixel, the LM algorithm is performed and the results are written
 *  on the folder, calling a separate method that writes them.
 *
 * @author Achlatis Stefanos Christofidi Georgia
 */

public class Detection {

    public static boolean ROIflag = false;
    public static boolean is16bit=false;

    public static String onlyEventName;

    public static boolean cannotDetermineLimb = false;

    public static BufferedImage limbImage=null;

    public static String path = folderPath;
    private static DecimalFormat df = new DecimalFormat("000");
    private static DecimalFormat dk = new DecimalFormat("00");

    public static int xCoord;
    public static int yCoord;

    public static String dataType = "png";

    /**
     * Performs the Detection Process.
     *
     * @param path1 the path of the directory for the detection
     * @throws Exception
     */

    public static void Test(String path1) throws Exception {
        path = path1;
        System.out.println(path);

        onlyEventName = path.split("/")[(path.split("/")).length - 1];

        System.out.println("Detection Process has started for folder " + path);

        Stage window = new Stage();
        if (finalevents!=0) {
            LocalizationController.display(window, "Detection in progress, please wait a few seconds. Progress: " + dk.format((double) 100 * (Controller.finalevents - events - 1) / Controller.finalevents) + "%.", "Processing event with path: " + path + ". Progress: " + (events + 1) / Controller.finalevents);
        }
        else {
            LocalizationController.display(window, "Detection in progress, please wait a few seconds. Progress: 0%.", "");
        }
        int numFrames = 0;
        String eventFrame = "";
        int onlyNumberOfFrame = 0;
        File metadata = new File(path + "/Event_Metadata.txt");
        //get number of frames from 4th line of metadata file
        BufferedReader br = null;
        String numPixelsWithDot = "";
        xCoord = 0;
        yCoord = 0;
        if (metadata.exists()) {
            try {
                System.out.println("metadata exists");
                br = new BufferedReader(new FileReader(metadata));
                br.readLine();
                br.readLine();
                br.readLine();
                String line3 = br.readLine();
                String line4 = br.readLine();
                System.out.println(line3);
                System.out.println(line4);
                numFrames = Integer.parseInt(line3.split("\\s+")[4]);
                String halfLine = line4.split("\\s+")[10];
                eventFrame = halfLine.split("\\)+")[0];
                onlyNumberOfFrame = Integer.valueOf(eventFrame.split("\\_")[1]);
                System.out.println(numFrames);
                System.out.println(eventFrame);
                System.out.println(onlyNumberOfFrame);
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert2 = new Alert(Alert.AlertType.ERROR, e + "");
                alert2.setTitle("Error");
                alert2.setHeaderText(null);
                alert2.setContentText("Metadata file is incorrect. Please correct it and try again. (Event Path: " + Detection.path + ").");
                alert2.showAndWait();
            }


            File test1 = new File(path + "/frame_000" + ".png");
            File test2 = new File(path + "/frame_000" + ".fits");
            if (test1.exists()) {
                dataType = "png";
            } else if (test2.exists()) {
                dataType = "fits";
            }

            String line5 = br.readLine();
            numPixelsWithDot = line5.split("\\s+")[8];
            System.out.println(numPixelsWithDot);
            br.readLine();

            String coordLine = br.readLine();
            String almostXCoord = coordLine.split("\\:+")[1];
            String almostYCoord = coordLine.split("\\:+")[2];
            xCoord = Integer.parseInt(almostXCoord.split("\\,+")[0]);
            yCoord = Integer.parseInt(almostYCoord.split("\\.+")[0]);
            System.out.println(xCoord+" "+yCoord);
            double[] pos = new double[2];
            pos[0] = xCoord;
            pos[1] = yCoord;

            for (int k = 0; k < 11+numFrames; k++)
                br.readLine();

           /* String[] line;
            line = br.readLine().split("\\s+");
            System.out.println(line);
            if (Objects.equals(line[3], "true.")) {
                is16bit = true;
                // System.out.println("Is 16 bit true");
            }*/
        }
        BufferedImage[] frames = new BufferedImage[numFrames];
        System.out.println(numPixelsWithDot);
        int numPixels = Integer.parseInt(numPixelsWithDot.split("\\.+")[0]);
        BufferedImage darkImage = null;
        BufferedImage flatImage = null;

        if(dataType=="png") {
            for (int fr = 0; fr < numFrames; fr++) {
                File file = new File(path + "/frame_" + df.format(fr) + ".png");
                try {
                    byte[] data = Files.readAllBytes(file.toPath());
                    ByteArrayInputStream bis = new ByteArrayInputStream(data);
                    frames[fr] = ImageIO.read(bis);
                } catch (IOException e) {
                    e.printStackTrace();
                    Alert alert2 = new Alert(Alert.AlertType.ERROR, e + "");
                    alert2.setTitle("Error");
                    alert2.setHeaderText(null);
                    alert2.setContentText("Incorrect number of frames in the folder. Please check the folder and try again.(Event Path: " + Detection.path + ").");
                    alert2.showAndWait();
                }
            }
            if (darkPath != null) {
                //read dark image
                File file = new File(darkPath);
                try {
                    byte[] data = Files.readAllBytes(file.toPath());
                    ByteArrayInputStream bis = new ByteArrayInputStream(data);
                    darkImage = ImageIO.read(bis);
                } catch (IOException e) {
                    e.printStackTrace();
                    Alert alert2 = new Alert(Alert.AlertType.ERROR, e + "");
                    alert2.setTitle("Error");
                    alert2.setHeaderText(null);
                    alert2.setContentText("Failed to read dark image. Please provide the same format as the event images. (Event Path: " + Detection.path + ").");
                    alert2.showAndWait();
                }
            }
            if (flatPath != null) {
                //read flat image
                File file = new File(flatPath);
                try {
                    byte[] data = Files.readAllBytes(file.toPath());
                    ByteArrayInputStream bis = new ByteArrayInputStream(data);
                    flatImage = ImageIO.read(bis);
                } catch (IOException e) {
                    e.printStackTrace();
                    Alert alert2 = new Alert(Alert.AlertType.ERROR, e + "");
                    alert2.setTitle("Error");
                    alert2.setHeaderText(null);
                    alert2.setContentText("Failed to read flat image. Please provide the same format as the event images. (Event Path: " + Detection.path + ").");
                    alert2.showAndWait();
                }
            }
        }

        else if(dataType.equals("fits")) {

            for(int fr=0; fr<numFrames; fr++) {
                Fits f=new Fits(path+"/frame_"+df.format(fr)+".fits");
                ImageHDU hdu=(ImageHDU) f.getHDU(0);
                int[][] image=(int[][]) hdu.getKernel();
                if(is16bit) {
                    frames[fr]=new BufferedImage(image[0].length,image.length,BufferedImage.TYPE_USHORT_GRAY);
                }
                else {
                    frames[fr]=new BufferedImage(image[0].length,image.length,BufferedImage.TYPE_BYTE_GRAY);
                }

                WritableRaster rasterFITS=frames[fr].getRaster();
                for(int i=0; i<image.length; i++) {
                    for(int j=0; j<image[0].length; j++) {
                        rasterFITS.setSample(j,i,0,image[i][j]);
                    }
                }
            }
            yCoord = frames[onlyNumberOfFrame].getHeight() - yCoord;

            if (darkPath != null) {
                //read dark image
                Fits f = new Fits(darkPath);
                ImageHDU hdu = (ImageHDU) f.getHDU(0);
                int[][] image = (int[][]) hdu.getKernel();
                if(is16bit) {
                   darkImage = new BufferedImage(image[0].length,image.length,BufferedImage.TYPE_USHORT_GRAY);
                }
                else {
                    darkImage = new BufferedImage(image[0].length,image.length,BufferedImage.TYPE_BYTE_GRAY);
                }

                WritableRaster rasterFITS=darkImage.getRaster();
                for(int i = 0; i < image.length; i++) {
                    for(int j = 0; j < image[0].length; j++) {
                        rasterFITS.setSample(j,i,0,image[i][j]);
                    }
                }

            }

            if (flatPath != null) {
                //read dark image
                Fits f = new Fits(flatPath);
                ImageHDU hdu = (ImageHDU) f.getHDU(0);
                int[][] image = (int[][]) hdu.getKernel();
                if(is16bit) {
                    flatImage = new BufferedImage(image[0].length,image.length,BufferedImage.TYPE_USHORT_GRAY);
                }
                else {
                    flatImage = new BufferedImage(image[0].length,image.length,BufferedImage.TYPE_BYTE_GRAY);
                }

                WritableRaster rasterFITS=flatImage.getRaster();
                for(int i = 0; i < image.length; i++) {
                    for(int j = 0; j < image[0].length; j++) {
                        rasterFITS.setSample(j,i,0,image[i][j]);
                    }
                }

            }
        }
        System.out.println("calibration constant is: " + calibrationConstant);
        if (darkPath != null) {
            for (int fr = 0; fr < numFrames; fr++)
                    frames[fr] = Calibration.difference(frames[fr],darkImage,fr);
        }

        if (flatPath != null) {
            for (int fr = 0; fr < numFrames; fr++)
                frames[fr] = Calibration.division(frames[fr],flatImage,fr,calibrationConstant);
        }

        BufferedImage average = Stacker.stackImages(frames);
        BufferedImage eventImage = frames[onlyNumberOfFrame];

        if (is16bit) {
            average = Calibration.clipping(average);
            eventImage = Calibration.clipping(eventImage);
      }

        BufferedImage cleanImage = Calibration.difference(eventImage, average, 1);
        WriteToCSV.hotPixelFlag = isHotPixel(cleanImage,numPixels, xCoord, yCoord);
        //WriteToCSV.outsideLimbFlag =outsideTheLimb(eventImage, cleanImage, xCoord, yCoord);
        BufferedImage ROIImage = null;
        ROIflag = false;

        File testoutput1 = new File("GENERICClean_Image.png");

        try {
            ImageIO.write(cleanImage, "png", testoutput1);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert2 = new Alert(Alert.AlertType.ERROR, e + "");
            alert2.setTitle("Error");
            alert2.setHeaderText(null);
            alert2.setContentText("Image couldn't be written. (Event Path: " + Detection.path + ").");
            alert2.showAndWait();
        }
        BufferedImage compressedClean = null;
        try {
            byte[] data = Files.readAllBytes(testoutput1.toPath());
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            compressedClean = ImageIO.read(bis);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert2 = new Alert(Alert.AlertType.ERROR, e + "");
            alert2.setTitle("Error");
            alert2.setHeaderText(null);
            alert2.setContentText("Incorrect folder path or contents. (Event Path: " + Detection.path + ").");
            alert2.showAndWait();
        }

        ROIflag = false;

        if(dataType=="png") {
            System.out.println(" tha TA GRApsEI");

            File testoutput000 = new File(path + "/Detection_Results/Stacked_Image.png");

            testoutput000.getParentFile().mkdirs();
            File particularParentDir = testoutput000.getParentFile();
            if (particularParentDir != null && !particularParentDir.exists() &&
                    !particularParentDir.mkdirs())
                throw new IOException("error while creating directories");

            try {
                ImageIO.write(average, "png", testoutput000);
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert2 = new Alert(Alert.AlertType.ERROR, e + "");
                alert2.setTitle("Error");
                alert2.setHeaderText(null);
                alert2.setContentText("Image couldn't be written. (Event Path: " + Detection.path + ").");
                alert2.showAndWait();
            }

            File genericoutput1 = new File(path + "/Detection_Results/Difference_Image.png");

            try {
                ImageIO.write(cleanImage, "png", genericoutput1);
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert2 = new Alert(Alert.AlertType.ERROR, e + "");
                alert2.setTitle("Error");
                alert2.setHeaderText(null);
                alert2.setContentText("Image couldn't be written. (Event Path: " + Detection.path + ").");
                alert2.showAndWait();
            }

            File genericoutput2 = new File(path + "/Detection_Results/Event_Image.png");

            try {
                ImageIO.write(eventImage, "png", genericoutput2);
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert2 = new Alert(Alert.AlertType.ERROR, e + "");
                alert2.setTitle("Error");
                alert2.setHeaderText(null);
                alert2.setContentText("Image couldn't be written. (Event Path: " + Detection.path + ").");
                alert2.showAndWait();
            }
//0208
           // File genericoutput3 = new File(path + "/Detection_Results/ROI_Image.png");
            File genericoutputROI = new File(folderPath + "/Detection_Results/" + onlyEventName + "_ROI_Image.png");
            if (ROIflag == true) {
                try {
                    //ImageIO.write(ROIImage, "png", genericoutput3);
                    ImageIO.write(ROIImage, "png", genericoutputROI);
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
        else if (dataType.equals("fits")){
            Fits averageFits = new Fits();
            Fits cleanFits = new Fits();
            Fits eventFits = new Fits();
            Fits particularROIFits = new Fits();

            averageFits.addHDU(FitsFactory.hduFactory(img2int(average)));
            cleanFits.addHDU(FitsFactory.hduFactory(img2int(cleanImage)));
            eventFits.addHDU(FitsFactory.hduFactory(img2int(eventImage)));
            if (ROIflag == true) {
                particularROIFits.addHDU(FitsFactory.hduFactory(img2int(ROIImage)));
            }

            File particularDetectionCsv = new File(path+"/Detection_Results/Average_Image.fits");
            particularDetectionCsv.getParentFile().mkdirs();
            File particularParentDir = particularDetectionCsv.getParentFile();
            if (particularParentDir != null && !particularParentDir.exists() &&
                    !particularParentDir.mkdirs())
                throw new IOException("error while creating directories");


            FitsOutputStream out0=new FitsOutputStream(new FileOutputStream(new File(path+"/Detection_Results/Average_Image.fits")));
            FitsOutputStream out1=new FitsOutputStream(new FileOutputStream(new File(path+"/Detection_Results/Difference_Image.fits")));
            FitsOutputStream out2=new FitsOutputStream(new FileOutputStream(new File(path+"/Detection_Results/Event_Image.fits")));
            FitsOutputStream out4 = new FitsOutputStream(new FileOutputStream(new File(folderPath + "/Detection_Results/" + onlyEventName + "_ROI_Image.fits")));

            averageFits.write(out0);
            cleanFits.write(out1);
            eventFits.write(out2);

            if (ROIflag == true) {
               // ROIFits.write(out3);
                particularROIFits.write(out4);
            }
            out0.close();
            out1.close();
            out2.close();
            if (ROIflag == true) {
                //out3.close();
                out4.close();
            }
        }

        if (WriteToCSV.outsideLimbFlag == false && WriteToCSV.hotPixelFlag == false){
            ROIflag = true;
            ROIImage = test1(compressedClean, xCoord, yCoord);

        }

        //print roi
        if(dataType=="png") {

            File genericoutputROI1 = new File(folderPath + "/Detection_Results/" + onlyEventName + "_ROI_Image.png");
            genericoutputROI1.getParentFile().mkdirs();
            File particularParentDir1 = genericoutputROI1.getParentFile();
            if (particularParentDir1 != null && !particularParentDir1.exists() &&
                    !particularParentDir1.mkdirs())
                throw new IOException("error while creating directories");

            if (ROIflag == true) {
                try {
                    //ImageIO.write(ROIImage, "png", genericoutput3);
                    ImageIO.write(ROIImage, "png", genericoutputROI1);
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

        else if (dataType.equals("fits")){

            Fits particularROIFits = new Fits();

            if (ROIflag == true) {
                particularROIFits.addHDU(FitsFactory.hduFactory(img2int(ROIImage)));
            }
            FitsOutputStream out4 = new FitsOutputStream(new FileOutputStream(new File(folderPath + "/Detection_Results/" + onlyEventName + "_ROI_Image.fits")));

            if (ROIflag == true) {

                particularROIFits.write(out4);
            }
            if (ROIflag == true) {
                out4.close();
            }
        }






        try {
            WriteToCSV.createCSV(folderPath, path, xCoord, yCoord);
        } catch (Exception exception) {
                exception.printStackTrace();
                Alert alert2 = new Alert(Alert.AlertType.ERROR, exception + "");
                alert2.setTitle("Error");
                alert2.setHeaderText(null);
                alert2.setContentText("Error creating directories. Make sure you have given permission to the software to write in the folder provided. (Event Path: " + Detection.path + ").");
                alert2.showAndWait();
        }

        //Prevent OOM
        System.out.println("Cleaning Memory...");
        for (int i = 0; i < frames.length; i++) {
            frames[i] = null;            // drop individual images
        }
        frames = null;                   // drop the array itself
        System.gc();

        System.out.println("Detection completed.");
        String endTimeStamp = String.valueOf(new Timestamp((new Date()).getTime()));

        LocalizationController.closeStage(window);
        if (Controller.events == 0)
        LocalizationController.display(new Stage(), "Detection", "The process has ended, please close the window and consult log file.");
        Controller.events--;
        if (Controller.events>=0) {
            Test(folderPath+"/"+subfolders.get(events));
        }
    }


    /**
     * Checks if the event is a hot pixel, by the number of pixels that triggered the first detection, as written in the metadata file.
     *
     * @param cleanImage the difference image
     * @param numPixels the number of pixels of the event from the metadata file
     * @param xCoord the x coordinate of the pixel in the image
     * @param yCoord the y coordinate of the pixel in the image
     * @return true if the event is a hot pixel, false if not
     * @throws Exception
     */

    private static boolean isHotPixel(BufferedImage cleanImage, int numPixels, int xCoord, int yCoord) throws Exception {
        if (numPixels == 1) {
            //searches the area of the brightest pixel to find the values of the pixels of the neighborhood

            int sumOfNeighbors = 0;

            sumOfNeighbors += Calibration.findValueOfPixel(cleanImage, xCoord, yCoord + 1);
            sumOfNeighbors += Calibration.findValueOfPixel(cleanImage, xCoord, yCoord - 1);
            sumOfNeighbors += Calibration.findValueOfPixel(cleanImage, xCoord + 1, yCoord);
            sumOfNeighbors += Calibration.findValueOfPixel(cleanImage, xCoord - 1, yCoord);

            sumOfNeighbors /= 4;

            if ((Calibration.findValueOfPixel(cleanImage, xCoord, yCoord) - sumOfNeighbors) < 30)
                return false;
        } else if (numPixels >= 2) {

            return false;
        } else {

            return false;
        }
        resultsLMA = "This is a hot pixel.";
        return true;
    }

    /**
     * Determines if the event is inside of the lunar limb.
     *
     * @param eventFrame the event image
     * @param cleanImage the difference image
     * @param xCoord the x coordinate of the event
     * @param yCoord the y coordinate of the event
     * @return true if the event occurred outside the limb, false if not
     * @throws Exception
     */

    private static boolean outsideTheLimb(BufferedImage eventFrame, BufferedImage cleanImage, int xCoord, int yCoord) throws Exception {

        BufferedImage stacked = eventFrame;

        BufferedImage noLight = null;
        int crop = 0;

        //gaussian blur
        int sd1 = 25;
        Gaussian gaussFilter = new Gaussian(sd1, 6 * sd1);
        BufferedImage blurred = gaussFilter.paddedGauss(stacked);

        //save blurred image
        ImageIO.write(blurred, "png", new File("2-gauss-big.png"));

        //remove gradient
        noLight = Gaussian.removeLight(stacked, blurred, crop);

        //save light removed image
        ImageIO.write(noLight, "png", new File("3-removed-light.png"));
        System.out.println("Light removal done");

        //gaussian blur again
        int sd2 = 9;
        Gaussian gaussFilter2 = new Gaussian(sd2, 6 * sd2);
        BufferedImage blurred2 = gaussFilter2.blur(noLight, crop);
        crop += 3 * sd2;

        //save blurred image
        ImageIO.write(blurred2, "png", new File("4-gauss-small.png"));

        //sobel edge detection
        int bright = 1;
        BufferedImage sobelImage = Sobel.sobel(blurred2, bright, crop);
        crop += 1;

        //save image after sobel filter
        ImageIO.write(sobelImage, "png", new File("5-sobel.png"));
        System.out.println("Sobel done");

        //isolate limb and fit circle
        BufferedImage limb=null;
        ArrayList<int[]> edgePixels=null;
        double[] circle=null;
        double mindiff=-1;
        double maxR=-1;
        float bestThreshold=0.8f;
        int ransacSamples = 15;
        //ransacSamples=samples;
        int boostPart=0;
        double[] bestCircle = null;
        for(float threshold=0.8f; threshold<0.95f; threshold+=0.01f) {
            limb=CircleFit.isolateLimb(sobelImage,threshold,0*0.01f);
            edgePixels= getEdge(limb);
            edgePixels=CircleFit.edgeRansac(limb,edgePixels,15);
            circle=CircleFit.landau(edgePixels);
            System.out.println(circle[2]);

                if(maxR<circle[2] && circle[2]<5000) {
                    maxR=circle[2];
                    bestThreshold=threshold;
                    bestCircle=circle;
                }
            }

        System.out.println("Threshold: "+bestThreshold);

        int attempts=10;
        mindiff=-1;
        maxR=-1;
        for(int i=0; i<attempts; i++) {
            limb=CircleFit.isolateLimb(sobelImage,bestThreshold,0*0.01f);
            edgePixels= getEdge(limb);
            edgePixels=CircleFit.edgeRansac(limb,edgePixels,15);
            circle=CircleFit.landau(edgePixels);
            System.out.println(circle[2]);
                if(maxR<circle[2] && circle[2]<5000) {
                    maxR=circle[2];
                    bestCircle=circle;
                    limbImage=limb;
                }
            }
        circle=bestCircle;

        int correctPointsCounter = 0;
        int badPointsCounter = 0;
        int [][] limbPointsArr = getEdge(limbImage).toArray(new int[2][]);
        double radius = circle[2];
        double xCenter = circle[0];
        double yCenter = circle[1];

        System.out.println(limbPointsArr.length);
        for (int k = 0; k < limbPointsArr.length; k++) {
            System.out.println( euclideanDistance(limbPointsArr[k][0], limbPointsArr[k][1], (int) circle[0], (int) circle[1]));
            System.out.println(radius);
            System.out.println(limbPointsArr[k][0]);
            System.out.println(limbPointsArr[k][1]);
            System.out.println((int) circle[0]);
            System.out.println((int) circle[1]);
            System.out.println();
            if (euclideanDistance(limbPointsArr[k][0], limbPointsArr[k][1], (int) circle[0], (int) circle[1]) > 1.1*radius
            || euclideanDistance(limbPointsArr[k][0], limbPointsArr[k][1], (int) circle[0], (int) circle[1]) < 0.9*radius) {
                badPointsCounter++;
            }
            else {
                correctPointsCounter++;
            }
        }

        System.out.println(badPointsCounter);
        System.out.println(correctPointsCounter);
//        if (badPointsCounter > correctPointsCounter) {
//            cannotDetermineLimb = true;
//            return false;
//
//        }

        BufferedImage blWhCircle = CircleFit.showCircle(eventFrame.getWidth(), eventFrame.getHeight(), circle);
        File outputFileCircle2 = new File("7-circleEdge.png");
        try {
            ImageIO.write(blWhCircle, "png", outputFileCircle2);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert2 = new Alert(Alert.AlertType.ERROR, e + "");
            alert2.setTitle("Error");
            alert2.setHeaderText(null);
            alert2.setContentText("Image couldn't be written. (Event Path: " + Detection.path + ").");
            alert2.showAndWait();
        }

        //Calibration.multiplication(eventFrame, blWhCircle);
        BufferedImage maskedEvent = Calibration.multiplication(eventFrame, blWhCircle);
        File masked = new File("7-masked.png");
        try {
            ImageIO.write(maskedEvent, "png", masked);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert2 = new Alert(Alert.AlertType.ERROR, e + "");
            alert2.setTitle("Error");
            alert2.setHeaderText(null);
            alert2.setContentText("Image couldn't be written. (Event Path: " + Detection.path + ").");
            alert2.showAndWait();
        }

        Raster raster1 = maskedEvent.getData();

        if (badPointsCounter > correctPointsCounter) {
            cannotDetermineLimb = true;
            return false;

        }

        if (raster1.getSample(xCoord, maskedEvent.getHeight() - yCoord, 0) > 0) {
            return false;
        } else {
            resultsLMA = "The event is outside of the lunar limb.";
            return true;
        }
    }
}
