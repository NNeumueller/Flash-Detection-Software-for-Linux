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

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.ImageHDU;
import nom.tam.util.FitsOutputStream;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.*;
import java.net.ConnectException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/** Performs localization on a directory of frames containing an impact flash and writes images in result directory
 * Based on a paper by Chrysa Avdellidou, 2021
 * 1) Averages and filters the images to clean noise and isolate the lunar limb
 * 2) Finds a circle that best fits the lunar limb
 * 3) Calls JPL Horizons API to get information about the lunar disk observed
 * 4) Creates an orthographic lunar projection of the lunar surface
 * 5) Rotates the lunar image and compares with the projection to find the best matching rotation angle
 * 6) Transforms the pixel coordinates of the impact flash into lunar coordinates
 *
 * @author Ivi Chatzi
 */

public class Local {
    private String path0=Controller.folderPath;
    private String path=null;
    private static DecimalFormat df=new DecimalFormat("000");

    public boolean is16bit=false;
    private boolean bin2=false;
    private boolean west=false;
    private String dataType="png";

    public int impactX=0;
    public int impactY=0;

    public int impactXfit=0;
    public int impactYfit=0;

    public int impactXrot=0;
    public int impactYrot=0;

    public double[] lunar=new double[]{0,0};

    public double correctRadius=0;
    public double centerLong=0;
    public double centerLat=0;
    public double angDiam=0;
    public double arcsecPixelRatio=0;
    public double[] circle=null;

    public int sd2=5;
    public int ransacSamples=15;
    public int boostPart=0;

    public double match=0;
    private int blackPixels=0;

    public Color circleColor=new Color(255,40,0);
    public Color matchColor=new Color(0,114,178);
    public Color nonMatchColor=new Color(213,94,0);

    private Projection lunarProj=null;
    public Correlation corr=null;
    public double bestangle=0;
    public boolean flipped=false;
    public int shiftx=0;
    public int shifty=0;

    public BufferedImage impactImage=null;
    public BufferedImage stackedImage=null;
    public BufferedImage removedLight=null;
    public BufferedImage sobelImage=null;
    public BufferedImage limbImage=null;
    public BufferedImage circleImage=null;
    public BufferedImage projectionMap=null;
    private BufferedImage maskedImage=null;
    public BufferedImage fitImage=null;
    public BufferedImage rotationImage=null;
    public BufferedImage correlationImage=null;

    /** Constructor: stores observation information
     * 1) Reads metadata file for:
     *      -date and time of event
     *      -number of frames
     *      -impact frame number
     *      -pixel coordinates of impact flash
     *      -dimensions of frames
     *      -pixel size of camera
     *      -bin2
     *      -16bit
     * 2) Calls JPL Horizons API to get lunar coordinates of center of observable disk and angular diameter of the moon
     * 3) If positive value of focal length given, calculates arcsec/pixel ratio of frames
     *  and correct radius of the lunar disk in pixels
     * 4) Checks datatype of input frames (png, jpeg, fits)
     * 5) Reads event frames and creates an average image of them
     * 6) Stores average image and impact frame in results directory (same format as input)
     *
     * @param coords array with the geographical coordinates and elevation of the observation location
     *               (longitude and latitude in degrees, elevation in km)
     * @param focal focal length of telescope in mm (0 or negative if unknown)
     * @param pathTemp path of event directory
     *
     * @throws ConnectException if JPL Horizons API takes too long to respond
     * @throws java.net.UnknownHostException if no internet connection
     * @throws ArrayIndexOutOfBoundsException problem reading metadata file (wrong format), or impact location out of bounds
     * @throws NumberFormatException problem reading metadata file (wrong format)
     * @throws FitsException problem reading/writing FITS files
     * @throws IOException
     * @throws Exception
     */
    public Local(double[] coords, double focal, String pathTemp) throws Exception {
        path=pathTemp;
        System.out.print("Localization Process Started");
        System.out.println(" for folder " + path);

        int numFrames=0;
        int impactFrame=0;
        String date="";
        String time="";

        double siteLong=coords[0];
        double siteLat=coords[1];
        double elev=coords[2];

        double focalLength=focal;

        double pixelSize=0;

        File metadata = new File(path + "/Event_Metadata.txt");
//        read metadata to get number of frames, date/time of event, and location of impact
        if(metadata.exists()) {
            BufferedReader br=new BufferedReader(new FileReader(metadata));
            br.readLine();
            br.readLine();

            //date and time
            String[] line=br.readLine().split("\\s+");
            date=line[4];
            time=line[5];

            //number of frames
            numFrames=Integer.parseInt(br.readLine().split("\\s+")[4]);
            line=br.readLine().split("\\s+");

            //impact frame
            impactFrame=Integer.parseInt(line[7].substring(0,line[7].length()-2))-1;

            //new metadata - impact location
            br.readLine();
            br.readLine();
            line=br.readLine().split("\\s+");
            String x=line[1].split(":")[1];
            String y=line[3].split(":")[1];
            x=x.substring(0,x.length()-1);
            y=y.substring(0,y.length()-1);
            impactX=Integer.parseInt(x);
            impactY=Integer.parseInt(y);

            //skip till camera info
            String[] l=br.readLine().split("\\s+");
            while(!l[0].equals("Camera")) {
                l=br.readLine().split("\\s+");
            }

            //pixel size
            br.readLine();
            br.readLine();
            line=br.readLine().split("\\s+");
            pixelSize=Double.parseDouble(line[2].substring(0,line[2].length()-1));

            //image size
            br.readLine();
            br.readLine();

            //16 bit
            br.readLine();
            line=br.readLine().split("\\s+");
            if(Objects.equals(line[3], "true.")) {
                is16bit=true;
            }

            //bin2
            line=br.readLine().split("\\s+");
            if(Objects.equals(line[2], "true.")) {
                bin2=true;
            }
        }
        else {
            System.out.println("Missing metadata file");
            throw new IOException("Missing metadata file");
        }


        double[] obsSubCoords=new double[] {0,0};
        obsSubCoords=JPLHorizons.getCenter(date,time,siteLong,siteLat,elev);
        centerLong=obsSubCoords[0];
        centerLat=obsSubCoords[1];
        System.out.println("Center coordinates and diameter in arcsec: "+ Arrays.toString(obsSubCoords));
        angDiam=obsSubCoords[2]/60;

        if(focalLength>0) {
            arcsecPixelRatio=206.265*pixelSize/focalLength;
            if(bin2) arcsecPixelRatio*=2;
            System.out.println(arcsecPixelRatio);

            correctRadius=(obsSubCoords[2]/2)/arcsecPixelRatio;

            System.out.println("Correct radius in pixels: "+correctRadius);
        }



        File test=new File(path+"/frame_000"+".png");
        if(test.exists()) {
            dataType="png";
        }
        test=new File(path+"/frame_000"+".fits");
        if(test.exists()) {
            dataType="fits";
        }
        test=new File(path+"/frame_000.jpeg");
        if(test.exists()) {
            dataType="jpeg";
        }

        //get frames
        BufferedImage[] frames=new BufferedImage[numFrames];
        if(Objects.equals(dataType, "png")) {
            for(int fr=0; fr<numFrames; fr++) {
                File file=new File(path+"/frame_"+df.format(fr)+".png");
                if(!file.exists()) throw new FileNotFoundException("Cannot find "+file);
                byte[] data=Files.readAllBytes(file.toPath());
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                frames[fr]=ImageIO.read(bis);
            }
        }
        else if(Objects.equals(dataType, "jpeg")) {
            for(int fr=0; fr<numFrames; fr++) {
                File file=new File(path+"/frame_"+df.format(fr)+".jpeg");
                if(!file.exists()) throw new FileNotFoundException("Cannot find "+file);
                byte[] data=Files.readAllBytes(file.toPath());
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                frames[fr]=ImageIO.read(bis);
            }
        }
        else if(dataType.equals("fits")) {
            for(int fr=0; fr<numFrames; fr++) {
                File file=new File(path+"/frame_"+df.format(fr)+".fits");
                if(!file.exists()) throw new FileNotFoundException("Cannot find "+file);
                Fits f=new Fits(path+"/frame_"+df.format(fr)+".fits");
                ImageHDU hdu=(ImageHDU) f.getHDU(0);
                int[][] image=(int[][]) hdu.getKernel();
                if(is16bit) {
//                    short[][] images = (short[][]) hdu.getKernel();
//                    frames[fr]=short2img(image);
                    frames[fr]=new BufferedImage(image[0].length,image.length,BufferedImage.TYPE_USHORT_GRAY);
                }
                else {
//                    byte[][] image=(byte[][]) hdu.getKernel();
//                    frames[fr]=byte2img(image);
                    frames[fr]=new BufferedImage(image[0].length,image.length,BufferedImage.TYPE_BYTE_GRAY);
                }
                WritableRaster raster=frames[fr].getRaster();
                for(int i=0; i<image.length; i++) {
                    for(int j=0; j<image[0].length; j++) {
                        raster.setSample(j,i,0,image[i][j]);
                    }
                }
            }
        }
        //darkPath = path + "";
        //stack frames
        //Calibration.difference(path+"/frame_"+df.format(010)+".png",path+"/frame_for_testing.png");
        //Calibration.difference(path+"/frame_"+df.format(010)+".png",darkPath);
        stackedImage = Stacker.stackImages(frames);

        //dark calibration
        //for (int fr = 0; fr < numFrames; fr++) {
        //Calibration.difference(path+"/frame_"+df.format(fr)+".png",darkPath);
        //}
        //save stacked image


        //flat calibration

        //Calibration.division(path+"/frame_"+df.format(010)+".png",path+"/frame_for_testing.png");
        //Calibration.division(path+"/frame_"+df.format(010)+".png",flatPath);

//        //flip image
//        if(false) {
//            frames[impactFrame]=flip(frames[impactFrame]);
//            stackedImage=flip(stackedImage);
//            impactY=stackedImage.getHeight()-impactY-1;
//        }

        impactImage=frames[impactFrame];
        impactY=impactImage.getHeight()-impactY-1;
        if(impactX<0 || impactX>=impactImage.getWidth()) throw new ArrayIndexOutOfBoundsException("\nImpact location out of bounds!");
        if(impactY<0 || impactY>=impactImage.getHeight()) throw new ArrayIndexOutOfBoundsException("\nImpact location out of bounds!");

        System.out.println("Impact location: "+impactX+" "+impactY);

//        //save stacked image and impact frame
//        if(dataType.equals("png")) {
//            ImageIO.write(frames[impactFrame],"png",new File(path+"/localization_results/0-impact-frame.png"));
//            ImageIO.write(stackedImage,"png",new File(path+"/localization_results/1-stacked.png"));
//        }
//        else if(dataType.equals("jpeg")) {
//            ImageIO.write(frames[impactFrame],"jpeg",new File(path+"/localization_results/0-impact-frame.jpeg"));
//            ImageIO.write(stackedImage,"jpeg",new File(path+"/localization_results/1-stacked.jpeg"));
//        }
//        else if(dataType.equals("fits")) {
//            Fits impactfits=new Fits();
//            Fits stackedfits=new Fits();
////            if(is16bit) {
////                impactfits.addHDU(FitsFactory.hduFactory(img2short(frames[impactFrame])));
////                stackedfits.addHDU(FitsFactory.hduFactory(img2short(stackedImage)));
////            }
////            else {
////                impactfits.addHDU(FitsFactory.hduFactory(img2byte(frames[impactFrame])));
////                stackedfits.addHDU(FitsFactory.hduFactory(img2byte(stackedImage)));
////            }
//            impactfits.addHDU(FitsFactory.hduFactory(img2int(frames[impactFrame])));
//            stackedfits.addHDU(FitsFactory.hduFactory(img2int(stackedImage)));
//            FitsOutputStream out1=new FitsOutputStream(new FileOutputStream(new File(path+"/localization_results/0-impact-frame.fits")));
//            FitsOutputStream out2=new FitsOutputStream(new FileOutputStream(new File(path+"/localization_results/1-stacked.fits")));
//            impactfits.write(out1); stackedfits.write(out2);
//            out1.close(); out2.close();
//        }

        System.out.println("Stacking done");
    }

    /** Finds a circle that best fits the limb:
     * 1) Applies a large Gaussian blur to the stacked image
     * 2) Divides stacked image by blurred image to remove light
     * 3) Applies a second Gaussian blur to the light-removed image for noise cleanup
     * 4) Uses a Sobel filter on the light-removed image for edge detection
     * 5) Isolates pixels on the limb using a brightness threshold, possibly boosting the top and bottom parts of the image
     * 6) Samples limb pixels and runs Landau's circle fit algorithm.
     *      -If focal length known, keeps circle with radius closest to correct.
     *      -Else keeps the largest circle found.
     * 7) Finds center coordinates and radius in pixels of circle that fits limb
     * All images from the light-removed onwards are in 8-bit due to being binary
     *
     *
     * @param gaussSd2 standard deviation of noise cleaning gaussian filter (2nd)
     * @param samples number of random sample points to take from each connected cluster of limb pixels
     * @param boostp percentage (in %) of top and bottom part of an image to brighten
     * @throws IOException
     * @throws Exception
     */
    public void circleFitting(int gaussSd2, int samples, int boostp) throws Exception {
        int crop = 0;

        //gaussian blur
        int sd1=25;
        Gaussian gaussFilter = new Gaussian(sd1, 6 * sd1);
        BufferedImage blurred = gaussFilter.paddedGauss(stackedImage);

//        //save blurred image
//        if(dataType.equals("png")) {
//            ImageIO.write(blurred,"png",new File(path+"/localization_results/2-blurred.png"));
//        }
//        else if(dataType.equals("jpeg")) {
//            ImageIO.write(blurred,"jpeg",new File(path+"/localization_results/2-blurred.jpeg"));
//        }
//        else if(dataType.equals("fits")) {
//            Fits blurredfits=new Fits();
//            blurredfits.addHDU(FitsFactory.hduFactory(img2int(blurred)));
//            FitsOutputStream out=new FitsOutputStream(new FileOutputStream(new File(path+"/localization_results/2-blurred.fits")));
//            blurredfits.write(out);
//            out.close();
//        }

        //remove gradient
        removedLight = Gaussian.removeLight(stackedImage, blurred, crop);

//        //save light removed image
//        if(dataType.equals("png")) {
//            ImageIO.write(removedLight,"png",new File(path+"/localization_results/3-removed-light.png"));
//        }
//        else if(dataType.equals("jpeg")) {
//            ImageIO.write(removedLight,"jpeg",new File(path+"/localization_results/3-removed-light.jpeg"));
//        }
//        else if(dataType.equals("fits")) {
//            Fits removedLightfits=new Fits();
//            removedLightfits.addHDU(FitsFactory.hduFactory(img2int(removedLight)));
//            FitsOutputStream out=new FitsOutputStream(new FileOutputStream(new File(path+"/localization_results/3-removed-light.fits")));
//            removedLightfits.write(out);
//            out.close();
//        }
        System.out.println("Light removal done");

        blackPixels=0;

        for(int i=0; i<removedLight.getWidth(); i++) {
            for(int j=0; j<removedLight.getHeight(); j++) {
                if(removedLight.getRGB(i,j)!=-1) blackPixels++;
            }
        }

        //gaussian blur again
        sd2=gaussSd2;
        Gaussian gaussFilter2=new Gaussian(sd2,6*gaussSd2);
        BufferedImage blurred2=gaussFilter2.blur(removedLight,crop);
        crop+=3*gaussSd2;

//        //save blurred image
//        if(dataType.equals("png")) {
//            ImageIO.write(blurred2,"png",new File(path+"/localization_results/4-gauss.png"));
//        }
//        else if(dataType.equals("jpeg")) {
//            ImageIO.write(blurred2,"jpeg",new File(path+"/localization_results/4-gauss.jpeg"));
//        }
//        else if(dataType.equals("fits")) {
//            Fits gauss2fits=new Fits();
//            gauss2fits.addHDU(FitsFactory.hduFactory(img2int(blurred2)));
//            FitsOutputStream out=new FitsOutputStream(new FileOutputStream(new File(path+"/localization_results/4-gauss.fits")));
//            gauss2fits.write(out);
//            out.close();
//        }

        //sobel edge detection
        int bright=1;
        sobelImage=Sobel.sobel(blurred2,bright,crop);
        crop+=1;

        //save image after sobel filter
//        if(dataType.equals("png")) {
//            ImageIO.write(sobelImage,"png",new File(path+"/localization_results/5-sobel.png"));
//        }
//        else if(dataType.equals("jpeg")) {
//            ImageIO.write(sobelImage,"jpeg",new File(path+"/localization_results/5-sobel.jpeg"));
//        }
//        else if(dataType.equals("fits")) {
//            Fits sobelfits=new Fits();
//            sobelfits.addHDU(FitsFactory.hduFactory(img2byte(sobelImage)));
//            FitsOutputStream out=new FitsOutputStream(new FileOutputStream(new File(path+"/localization_results/5-sobel.fits")));
//            sobelfits.write(out);
//            out.close();
//        }
        System.out.println("Sobel done");


        //isolate limb and fit circle
        BufferedImage limb=null;
        ArrayList<int[]> edgePixels=null;
        circle=null;
        double mindiff=-1;
        double maxR=-1;
        float bestThreshold=0.8f;
        ransacSamples=samples;
        boostPart=boostp;
        double[] bestCircle = null;
        for(float threshold=0.8f; threshold<0.95f; threshold+=0.01f) {
            limb=CircleFit.isolateLimb(sobelImage,threshold,boostp*0.01f);
            edgePixels=CircleFit.getEdge(limb);
            edgePixels=CircleFit.edgeRansac(limb,edgePixels,samples);
            circle=CircleFit.landau(edgePixels);
            System.out.println(circle[2]);
            if(arcsecPixelRatio>0) {
                double diff=Math.abs(correctRadius-circle[2]);
                if(mindiff==-1) {
                    mindiff=diff;
                }
                if(diff<=mindiff) {
                    mindiff=diff;
                    bestThreshold=threshold;
                    bestCircle=circle;
                }
            }
            else {
                if(maxR<circle[2] && circle[2]<5000) {
                    maxR=circle[2];
                    bestThreshold=threshold;
                    bestCircle=circle;
                }
            }
        }
        System.out.println("Threshold: "+bestThreshold);

        int attempts=10;
        mindiff=-1;
        maxR=-1;
        for(int i=0; i<attempts; i++) {
            limb=CircleFit.isolateLimb(sobelImage,bestThreshold,boostp*0.01f);
            edgePixels=CircleFit.getEdge(limb);
            edgePixels=CircleFit.edgeRansac(limb,edgePixels,samples);
            circle=CircleFit.landau(edgePixels);
            System.out.println(circle[2]);
            if(arcsecPixelRatio>0) {
                double diff=Math.abs(correctRadius-circle[2]);
                if(mindiff==-1) {
                    mindiff=diff;
                    limbImage=limb;
                }
                if(diff<=mindiff) {
                    mindiff=diff;
                    bestCircle=circle;
                    limbImage=limb;
                }
            }
            else {
                if(maxR<circle[2] && circle[2]<5000) {
                    maxR=circle[2];
                    bestCircle=circle;
                    limbImage=limb;
                }
            }
        }
        circle=bestCircle;

        System.out.println("Radius in pixels: "+circle[2]+"\nThreshold: "+bestThreshold);
        System.out.println("Center of circle: "+circle[0]+", "+circle[1]);

        if(arcsecPixelRatio>0) {
            double differror=circle[2]-correctRadius;
            System.out.println("Radius error: "+differror);
        }

//        circle[2]=correctRadius;
        //just the edge of the circle, for comparison purposes
        circleImage=CircleFit.edgeRed(circle,removedLight, circleColor);

        System.out.println("Circle fit done");

    }

    /** Performs Landau's circle fitting algorithm directly onto a given set of points
     *
     * @param points list of point coordinates to use for circle fitting
     */
    public void manualCircle(ArrayList<int[]> points) {
        circle=CircleFit.landau(points);
        limbImage=new BufferedImage(removedLight.getWidth(), removedLight.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for(int[] point:points) {
            limbImage.setRGB(point[0],point[1],255);
        }
        System.out.println(Arrays.toString(circle));
        circleImage=CircleFit.edgeRed(circle,removedLight, circleColor);
    }

    /** Sets the circle field to the given circle
     *
     * @param c array containing coordinates of center and radius in pixels
     */
    public void setCircle(double[] c) {
        circle=c;
        circleImage=CircleFit.edgeRed(circle,removedLight,circleColor);
    }

    /** Creates a lunar projection and prepares for image correlation.
     * 1) Masks light-removed image keeping only a part near the limb
     * 2) Reads reference cylindrical map of lunar surface
     * 3) Creates orthographic projection of moon centered at coordinates from JPL Horizons API of radius found from circle fitting
     * 4) Creates a full circle image with the lunar mask fit onto it
     * 5) Calculates the new location of the impact flash
     *
     * @throws FileNotFoundException if cylindrical map reference file missing
     * @throws NullPointerException if failed to read cylindrical map reference image
     * @throws OutOfMemoryError if radius found is too big, problem making projection
     */
    public void setupProjection(boolean westinput) throws Exception {
        west=westinput;
        maskedImage=CircleFit.lunarMask(removedLight,circle,west);
//        File refCyl=new File("ref\\WAC_GLOBAL_E000N0000_016P.png");
//        BufferedImage cylMoon=ImageIO.read(refCyl);
        Image refCyl = new Image(getClass().getResourceAsStream( "WAC_GLOBAL_E000N0000_016P.png" ));
        BufferedImage cylMoon=SwingFXUtils.fromFXImage(refCyl,null);

        double l0=centerLong;
        if(l0>=180) {
            l0-=360;
        }
        double f0=centerLat;

        lunarProj=new Projection(circle,l0,f0,cylMoon,impactX,impactY);
        lunarProj.createProjection();


        System.out.println("Projection done");

        //binary map projection
        projectionMap=binaryMap(lunarProj.proj,0.18f);

        lunarProj.createFit(maskedImage);
        impactXfit=lunarProj.ximpact;
        impactYfit=lunarProj.yimpact;
        System.out.println("Impact location on fit image: "+impactXfit+", "+impactYfit);
        fitImage=lunarProj.fit;

        System.out.println("Binary maps done");

        corr=new Correlation(projectionMap,impactXfit,impactYfit,circle);
    }

    /** Rotates fit masked image around its center and compares with orthographic projection
     *  Tracks location of impact flash
     *  Transforms pixel coordinates of impact flash into lunar coordinates
     *
     * @param fullRotate true to check for best matching rotation angle, false to rotate image by a set angle
     * @param mirrorY true to flip fit image vertically before rotation
     * @throws OutOfMemoryError
     */
    public void correlation(boolean fullRotate,boolean mirrorY) throws OutOfMemoryError{
        shiftx=0;
        shifty=0;
        if(mirrorY) {
            flipped= !flipped;
            fitImage=flip(fitImage);
            corr.yimpact0=fitImage.getHeight()-corr.yimpact0-1;
        }
        if(fullRotate) {
            bestangle=corr.correlate(fitImage,30,0.5f,1f,west);
            bestangle=corr.correlate(fitImage,10, (float) bestangle,0.05f,false);
        }

        System.out.println("Rotation angle: "+bestangle);
        int[] impactRot=new int[]{corr.ximpact0, corr.yimpact0};
        BufferedImage fullmask=lunarProj.fitView(removedLight);
        if(flipped) {
            fullmask=flip(fullmask);
        }
        //dummy image for cropping
        BufferedImage rot=Correlation.rotate(fullmask,bestangle,impactRot);
        rot=Local.binaryMap(rot,0.5f);
        rotationImage=new BufferedImage(projectionMap.getWidth(),projectionMap.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
        Raster raster1=rot.getRaster();
        WritableRaster raster2=rotationImage.getRaster();
        for(int i=0; i<projectionMap.getWidth(); i++) {
            for(int j=0; j<projectionMap.getHeight(); j++) {
                raster2.setSample(i,j,0,raster1.getSample(i,j,0));
            }
        }
        impactXrot=impactRot[0];
        impactYrot=impactRot[1];

        match=corr.countMatches(rotationImage)*1.00/blackPixels;

        correlationImage=corr.overlay(rotationImage,new int[]{impactXrot, impactYrot},matchColor,nonMatchColor);

        System.out.println("Impact location on rotated image: "+ Arrays.toString(impactRot));

        double[] ans=lunarProj.orth2plan(impactRot[0]-circle[2],circle[2]-impactRot[1]);
        System.out.println("Lunar coordinates: "+Arrays.toString(ans));
        lunar=ans;
    }


    /** Shifts rotation image by a given amount vertically and/or horizontally.
     *  Correlates with projection
     *  Tracks new location of impact point
     *
     * @param xoffset number of pixels to shift horizontally
     * @param yoffset number of pixels to shift vertically
     * @throws OutOfMemoryError
     */
    public void shift(int xoffset, int yoffset) throws OutOfMemoryError{
        shiftx+=xoffset;
        shifty+=yoffset;
        rotationImage=Correlation.shiftX(rotationImage,xoffset);
        rotationImage=Correlation.shiftY(rotationImage,yoffset);
        impactXrot+=xoffset;
        impactYrot+=yoffset;
        match=corr.countMatches(rotationImage)*1.00/blackPixels;
        int[] impactRot={impactXrot, impactYrot};
        correlationImage=corr.overlay(rotationImage,impactRot,matchColor,nonMatchColor);

        System.out.println("Impact location on rotated image: "+ Arrays.toString(impactRot));

        double[] ans=lunarProj.orth2plan(impactRot[0]-circle[2],circle[2]-impactRot[1]);
        System.out.println("Lunar coordinates: "+Arrays.toString(ans));
        lunar=ans;
    }

    /** Sets rotation image to a specific angle and offset
     *
     * @param cor array with the rotation angle, x offset and y offset
     */
    public void setCorrelation(double[] cor) {
        bestangle = cor[0];
        correlation(false,false);
        shiftx= (int) cor[1];
        shifty= (int) cor[2];
        if(shiftx!=0 || shifty!=0) {
            shift(shiftx, shifty);
        }
    }

    /** Transforms short array to 16-bit grayscale image.
     * Flips image vertically.
     *
     * @param image short array to create image
     * @return image from array
     */
    private static BufferedImage short2img(short[][] image) {
        int w=image[0].length;
        int h=image.length;

        short[][] imageFlipped=new short[h][w];
        for(int i=0; i<w; i++) {
            for(int j=0; j<h; j++) {
                imageFlipped[j][i]=image[h-j-1][i];
            }
        }
        image=imageFlipped;

        int[] intArray=new int[w*h];
        for(int i=0; i<w; i++) {
            for(int j=0; j<h; j++) {
                intArray[w*j+i]=image[j][i];
            }
        }

        BufferedImage output=new BufferedImage(w, h,BufferedImage.TYPE_USHORT_GRAY);
        WritableRaster raster=output.getRaster();
        raster.setSamples(0, 0, w, h, 0, intArray);
        return output;
    }

    /** Transform byte array to 8-bit grayscale BufferedImage
     *
     * @param image byte array to create image
     * @return image from byte array
     */
    private static BufferedImage byte2img(byte[][] image) {
        int w=image[0].length;
        int h=image.length;

        int[] intArray=new int[w*h];
        for(int i=0; i<w; i++) {
            for(int j=0; j<h; j++) {
                intArray[w*j+i]=image[j][i];
            }
        }

        BufferedImage output=new BufferedImage(w, h,BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster=output.getRaster();
        raster.setSamples(0, 0, w, h, 0, intArray);
        return output;
    }

    /** Transform BufferedImage to short array
     * Use for 16bit greyscale image
     *
     * @param image image to get array
     * @return short array of input image
     */
    public static short[][] img2short(BufferedImage image) {
        int w=image.getWidth();
        int h=image.getHeight();
        Raster raster=image.getRaster();

        short[][] imgArray=new short[h][w];
        for(int i=0; i<w; i++) {
            for(int j=0; j<h; j++) {
                imgArray[j][i]= (short) raster.getSample(i,j,0);
            }
        }
        return imgArray;
    }

    /** Transforms a BufferedImage into a byte array
     * Use for 8bit greyscale images
     *
     * @param image image to get array
     * @return byte array of input image
     */
    private static byte[][] img2byte(BufferedImage image) {
        int w=image.getWidth();
        int h=image.getHeight();
        Raster raster=image.getRaster();

        byte[][] imgArray=new byte[h][w];
        for(int i=0; i<w; i++) {
            for(int j=0; j<h; j++) {
                imgArray[j][i]= (byte) raster.getSample(i,j,0);
            }
        }
        return imgArray;
    }

    /** Transforms a BufferedImage into an integer array.
     * Use for RGB images
     *
     * @param image image to get array
     * @return integer array from input image
     */
    public static int[][] img2int(BufferedImage image) {
        int w=image.getWidth();
        int h=image.getHeight();
        Raster raster=image.getRaster();

        int[][] imgArray=new int[h][w];
        for(int i=0; i<w; i++) {
            for(int j=0; j<h; j++) {
                imgArray[j][i]= raster.getSample(i,j,0);
            }
        }
        return imgArray;
    }

    /** Creates a binary map of an image using a brightness threshold.
     *  Pixels brighter than the threshold are white, darker are black.
     *
     * @param image image to create binary map of
     * @param threshold % of brightest pixel in the image
     * @return binary map o input image
     */
    public static BufferedImage binaryMap(BufferedImage image, float threshold) {
        int x=image.getWidth();
        int y=image.getHeight();
        BufferedImage output=new BufferedImage(x,y,BufferedImage.TYPE_BYTE_GRAY);
        Raster raster1=image.getRaster();
        WritableRaster raster2=output.getRaster();

        int maxg=-1;

        for(int i=0; i<x; i++) {
            for(int j=0; j<y; j++) {
                int pixel=raster1.getSample(i,j,0);
                if(maxg<pixel) maxg=pixel;
            }
        }
        for(int i=0; i<x; i++) {
            for(int j=0; j<y; j++) {
                int pixel=raster1.getSample(i,j,0);
                if(pixel<(int)(threshold*maxg)) pixel=0;
                else pixel=255;
                raster2.setSample(i,j,0,pixel);
            }
        }
        output.setData(raster2);
        return output;
    }

    /** Flips an image vertically
     *
     * @param img image to flip
     * @return flipped image
     */
    public static BufferedImage flip(BufferedImage img) {
        int x=img.getWidth();
        int y=img.getHeight();
        Raster raster1=img.getRaster();
        BufferedImage flipped=new BufferedImage(x,y,BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster2=flipped.getRaster();
        for (int i=0; i<x; i++) {
            for (int j=0; j<y; j++) {
                raster2.setSample(i,j,0,raster1.getSample(i,y-j-1,0));
            }
        }
        flipped.setData(raster2);
        return flipped;
    }

    /** Writes a BufferedImage to a file (png, jpeg or fits)
     *
     *
     * @param img image to write
     * @param filename name of file to write
     * @param fitsType byte, short or int to write fits
     * @throws IOException
     * @throws FitsException problem with FITS files
     */
    public void writeFile(BufferedImage img, String filename, String fitsType) throws IOException, FitsException {
        if(dataType.equals("png")) {
            ImageIO.write(img,"png",new File(path+"/localization_results/"+filename+".png"));
        }
        if(dataType.equals("jpeg")) {
            ImageIO.write(img,"png",new File(path+"/localization_results/"+filename+".jpeg"));
        }
        else if(dataType.equals("fits")) {
            Fits fits=new Fits();
            if(fitsType.equals("short")) {
                fits.addHDU(FitsFactory.hduFactory(img2short(img)));
            }
            else if(fitsType.equals("byte")) {
                fits.addHDU(FitsFactory.hduFactory(img2short(img)));
            }
            else if(fitsType.equals("int")) {
                fits.addHDU(FitsFactory.hduFactory(img2int(img)));
            }

            FitsOutputStream out=new FitsOutputStream(new FileOutputStream(new File(path+"/localization_results/"+filename+".fits")));
            fits.write(out);
            out.close();
        }
    }


}