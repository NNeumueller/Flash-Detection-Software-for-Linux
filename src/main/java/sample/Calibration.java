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

import java.awt.image.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Performs the calibration and arithmetic procedures on images
 * @author Achlatis Stefanos Christofidi Georgia
 */

public class Calibration {

    /** Subtracts 2 images of the same size pixel by pixel.
     *      *
     *      *
     *      @param path1 path of the first image
     *      @param path2 path of the second image
     *      @param pathname the first character (an int) of the path that the difference image is written
     *      @return the difference image
     *
     *      * @throws Exception
     *      */

    public static BufferedImage difference(String path1, String path2, int pathname) throws Exception {
            BufferedImage image1 = ImageIO.read(new File(path1));
            BufferedImage image2 = ImageIO.read(new File(path2));

            int w = image1.getWidth();
            int h = image1.getHeight();

            int[] imgArr1 = new int[w*h];

            Raster raster1 = image1.getData();
            Raster raster2 = image2.getData();

            for(int x = 0; x < w; x++)
                for(int y = 0; y < h; y++) {
                    imgArr1[y*w + x] = Math.max(0,(raster1.getSample(x, y, 0) - raster2.getSample(x, y, 0)));
                }

            BufferedImage outputImage = new BufferedImage(image1.getWidth(), image1.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            WritableRaster raster = outputImage.getRaster();

            raster.setSamples(0, 0, w, h, 0, imgArr1);

            ImageIO.write(outputImage, "png", new File(pathname + "DifferenceImage.png"));

            return outputImage;

        }

    /** Subtracts 2 images of the same size pixel by pixel.
     *      *
     *      *
     *      @param image1 first image
     *      @param image2 second image
     *      @param pathname the first character (an int) of the path that the difference image is written
     *      @return the difference image
     *
     *      * @throws Exception
     *      */

    public static BufferedImage difference(BufferedImage image1, BufferedImage image2, int pathname) throws Exception {

        int w = image1.getWidth();
        int h = image1.getHeight();

        int[] imgArr1 = new int[w*h];

        Raster raster1 = image1.getData();
        Raster raster2 = image2.getData();

        for(int x = 0; x < w; x++)
            for(int y = 0; y < h; y++) {
                imgArr1[y*w + x] = Math.max(0,(raster1.getSample(x, y, 0) - raster2.getSample(x, y, 0)));
            }
        BufferedImage outputImage = new BufferedImage(image1.getWidth(), image1.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        WritableRaster raster = outputImage.getRaster();

        raster.setSamples(0, 0, w, h, 0, imgArr1);

        ImageIO.write(outputImage, "png", new File(pathname + "DifferenceImage.png"));

        return outputImage;

    }

    /** Normalises the pixels of an image to the values 0-255.
     *      *
     *      *
     *      @param Image image
     *      @return the normalised image
     *      */

    public static BufferedImage clipping(BufferedImage Image) {

        int w = Image.getWidth();
        int h = Image.getHeight();

        int[] imgArr1 = new int[w*h];
        Raster raster1 = Image.getData();

        for(int x = 0; x < w; x++)
            for(int y = 0; y < h; y++) {
               imgArr1[y * w + x] = (int) Math.floor(raster1.getSample(x, y, 0)/256);
                }

        BufferedImage outputImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = outputImage.getRaster();

        raster.setSamples(0, 0, w, h, 0, imgArr1);

        return outputImage;
    }

    /** Divides 2 images of the same size pixel by pixel.
     *      *
     *      *
     *      @param path1 path of the first image
     *      @param path2 path of the second image
     *      @param pathname the first character (an int) of the path that the difference image is written
     *      @return the divided image
     *
     *      * @throws Exception
     *      */

    public static BufferedImage division(String path1, String path2, int pathname) throws Exception {
        BufferedImage image1 = ImageIO.read(new File(path1));
        BufferedImage image2 = ImageIO.read(new File(path2));

        int w = image1.getWidth();
        int h = image1.getHeight();

        int[] imgArr1 = new int[w*h];

        Raster raster1 = image1.getData();
        Raster raster2 = image2.getData();

        for(int x = 0; x < w; x++)
            for(int y = 0; y < h; y++) {
                if(raster2.getSample(x, y, 0) == 0)
                    imgArr1[y*w + x] = raster1.getSample(x, y, 0);
                else
                    imgArr1[y*w + x] = 100*((raster1.getSample(x, y, 0) / raster2.getSample(x, y, 0)));
            }

        BufferedImage outputImage = new BufferedImage(image1.getWidth(), image1.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = outputImage.getRaster();

        raster.setSamples(0, 0, w, h, 0, imgArr1);

        ImageIO.write(outputImage, "png", new File(pathname + "DivisionImage.png"));

        return outputImage;
    }

    /** Divides 2 images of the same size pixel by pixel.
     *      *
     *      *
     *      @param image1 first image
     *      @param image2 second image
     *      @param pathname the first character (an int) of the path that the difference image is written
     *      @param constant the constant that is used for the scaling of the second image used for division
     *      @return the divided image
     *
     *      * @throws Exception
     *      */

    public static BufferedImage division( BufferedImage image1, BufferedImage image2, int pathname, int constant) throws Exception {
        int w = image1.getWidth();
        int h = image1.getHeight();

        int[] imgArr1 = new int[w*h];

        Raster raster1 = image1.getData();
        Raster raster2 = image2.getData();

        for(int x = 0; x < w; x++)
            for(int y = 0; y < h; y++) {
                if(raster2.getSample(x, y, 0) == 0)
                    imgArr1[y*w + x] = raster1.getSample(x, y, 0);
                else
                    imgArr1[y*w + x] = constant*((raster1.getSample(x, y, 0) / raster2.getSample(x, y, 0)));
            }

        BufferedImage outputImage = new BufferedImage(image1.getWidth(), image1.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = outputImage.getRaster();

        raster.setSamples(0, 0, w, h, 0, imgArr1);

        ImageIO.write(outputImage, "png", new File(pathname + "DivisionImage.png"));

        return outputImage;
    }

    /** Multiplies 2 images of the same size pixel by pixel.
     *      *
     *      *
     *      @param path1 path of the first image
     *      @param path2 path of the second image
     *      @param pathname the first character (an int) of the path that the difference image is written
     *      @return the multiplied image
     *
     *      * @throws Exception
     *      */

    public static BufferedImage multiplication(String path1, String path2, int pathname) throws Exception {
        BufferedImage image1 = ImageIO.read(new File(path1));
        BufferedImage image2 = ImageIO.read(new File(path2));

        {

            int w1 = image1.getWidth();
            int h1 = image1.getHeight();
            int w2 = image1.getWidth();
            int h2 = image1.getHeight();

            Raster raster1 = image1.getData();
            Raster raster2 = image2.getData();

            int[] imgArr1 = new int[0];

            if ((w1==w2) && (h1==h2)) {
                imgArr1 = new int[w1 * h1];
                for (int x = 0; x < w1; x++)
                    for (int y = 0; y < h1; y++)
                        imgArr1[y * w1 + x] = ((raster1.getSample(x, y, 0) * raster2.getSample(x, y, 0)));


                System.out.println("In multiplication");

                BufferedImage outputImage = new BufferedImage(image1.getWidth(), image1.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
                WritableRaster raster = outputImage.getRaster();

                raster.setSamples(0, 0, w1, h1, 0, imgArr1);
                System.out.println(raster.getSample(1006,721,0));
                ImageIO.write(outputImage, "png", new File( "MaskedImage.png"));

                return outputImage;
            }
            else {
                System.out.println("Error in Edge Detection!.");
                return null;
            }
        }
    }

    /** Multiplies 2 images of the same size pixel by pixel.
     *      *
     *      *
     *      @param image1 first image
     *      @param image2 second image
     *      @return the multiplied image
     *
     *      * @throws Exception
     *      */

    public static BufferedImage multiplication(BufferedImage image1, BufferedImage image2) throws Exception {

        int w1 = image1.getWidth();
        int h1 = image1.getHeight();
        int w2 = image1.getWidth();
        int h2 = image1.getHeight();

        Raster raster1 = image1.getData();
        Raster raster2 = image2.getData();

        int[] imgArr1 = new int[0];

        if ((w1==w2) && (h1==h2)) {
            imgArr1 = new int[w1 * h1];
            for (int x = 0; x < w1; x++)
                for (int y = 0; y < h1; y++)
                    if (raster2.getSample(x, y, 0) == 0)
                        imgArr1[y * w1 + x] = 0;
                    else {
                        imgArr1[y * w1 + x] = raster1.getSample(x, y, 0);
                        if(raster1.getSample(x, y, 0)!=0) {
                           // System.out.println(raster1.getSample(x, y, 0));
                           // System.out.println(x + "   " + y);
                        }
                    }
            BufferedImage outputImage = new BufferedImage(w1, h1, BufferedImage.TYPE_BYTE_GRAY);
            WritableRaster raster = outputImage.getRaster();

            raster.setSamples(0, 0, w1, h1, 0, imgArr1);

            ImageIO.write(outputImage, "png", new File( "MaskedImage.png"));

            return outputImage;
        }
        else {
            System.out.println("Error in Edge Detection!.");
            return null;
        }
    }

    /** Applies a threshold on the values of the pixels of an image, giving the value 0 to all the pixels whose value is below this threshold.
     *      *
     *      *
     *      @param Image the image
     *      @param threshold the value of the threshold used
     *      @return the image after the application of the threshold
     *
     *      * @throws Exception
     *      */

    public static BufferedImage thresholding(BufferedImage Image, int threshold) throws Exception {

        int w = Image.getWidth();
        int h = Image.getHeight();

        int[] imgArr1 = new int[w*h];

        Raster raster1 = Image.getData();

        for(int x = 0; x < w; x++)
            for(int y = 0; y < h; y++) {
                if (raster1.getSample(x, y, 0) < threshold) {
                    imgArr1[y * w + x] = 0;
                }
                else {
                    imgArr1[y * w + x] = raster1.getSample(x, y, 0);
                }
            }

        BufferedImage outputImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = outputImage.getRaster();

        raster.setSamples(0, 0, w, h, 0, imgArr1);

        ImageIO.write(outputImage, "png", new File("ImageAfterThreshold.png"));

        return outputImage;
    }

    /** Converts a Buffered Image to an int array.
     *      *
     *      *
     *      @param Image the image to be converted
     *      @return the int array
     *
     *      */

    public static int[] convertImageToIntArray(BufferedImage Image) {

        int w = Image.getWidth();
        int h = Image.getHeight();

        int[] imgArr1 = new int[w*h];

        Raster raster1 = Image.getData();

        for(int x = 0; x < w; x++)
            for(int y = 0; y < h; y++) {
                    imgArr1[y * w + x] = raster1.getSample(x, y, 0);
                }

        return imgArr1;
    }

    /** Finds the value of a pixel of an image of given coordinates.
     *      *
     *      *
     *      @param Image the image
     *      @param xCoord the x coordinate (starting from left, increases while moving right)
     *      @param yCoord the y coordinate (starting from up, increases while moving down)
     *      @return the int value of the given pixel
     *      */

    public static int findValueOfPixel(BufferedImage Image, int xCoord, int yCoord) {

        int w = Image.getWidth();
        int h = Image.getHeight();

        Raster raster1 = Image.getData();

        int value = 0;

        if(xCoord <= w && xCoord >= 0 && yCoord <= h && yCoord >= 0)
            value = raster1.getSample(xCoord, yCoord, 0);

        /*This part prints the values of all pixels of the image above a specific value*/
        /*System.out.println("Starts");
        for(int x = 0; x < w; x++)
            for(int y = 0; y < h; y++) {
                if (raster1.getSample(x, y, 0) > 27) {
                    System.out.print(raster1.getSample(x, y, 0));
                    System.out.print("//");
                    System.out.print(x);
                    System.out.print("//");
                    System.out.println(y);
                }
            }
        System.out.println("Stops");
         */

        return value;
    }

    /** Finds the euclidean distance between two pixels of an image.
     *      *
     *      *
     *      @param x1 the x coordinate of the first pixel
     *      @param y1 the y coordinate of the first pixel
     *      @param x2 the x coordinate of the second pixel
     *      @param y2 the y coordinate of the second pixel
     *      @return the euclidean distance of the pixels in int format
     *      * @throws Exception
     *      */

    public static int euclideanDistance(int x1, int y1, int x2, int y2) {

        int xDif = Math.abs(x1-x2);
        int yDif = Math.abs(y1-y2);
        int distance = (int) Math.sqrt(Math.pow(xDif,2) + Math.pow(yDif,2));

        return distance;
    }

    /** Converts a Buffered Image to an array of doubles.
     *      *
     *      *
     *      @param Image the image to be converted
     *      @return the double array
     *
     *      */

    public static double[] convertImageToDoubleArray(BufferedImage Image) {

        int w = Image.getWidth();
        int h = Image.getHeight();

        double[] imgArr1 = new double[w*h];

        Raster raster1 = Image.getData();

        for(int x = 0; x < w; x++)
            for(int y = 0; y < h; y++) {
                imgArr1[y * w + x] = raster1.getSample(x, y, 0);
            }

        // BufferedImage outputImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        // WritableRaster raster = outputImage.getRaster();

        //raster.setSamples(0, 0, w, h, 0, imgArr1);

        return imgArr1;
    }
}