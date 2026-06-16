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

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import static java.lang.Math.abs;
import static java.lang.Math.exp;

/** Removes light gradient from lunar images using Gaussian blurring and image division
 * @author Ivi Chatzi
 *
 */
public class Gaussian {
    private int s,r;
    private float[] Gx;
    private float F;

    /** Constructor: calculates Gaussian kernel of given size and standard deviation
     *
     * @param sd standard deviation
     * @param size kernel size
     */
    public Gaussian(int sd, int size) {
        s=sd;
        r=size/2;

        //calculate 1D kernel
        Gx=new float[2*r+1];
        F=0;
        for(int i=0; i<2*r+1; i++) {
            float exponent=-((i-r)*(i-r))/(float)(2*s*s);
            Gx[i]=(float) (1f/(s*Math.sqrt(2*Math.PI))*exp(exponent));
            F+=Gx[i];
        }
        if(F==0f) {
            F=1;
        }
    }

    /** Applies a gaussian blur on an image, by convolving with a 1-D gaussian kernel first on the X-axis and then on the Y-axis.
     * The image is padded to resolve border pixels, creating straight lines near the edge.
     *
     * @param img image to blur
     * @return blurred image
     */

    public BufferedImage paddedGauss(BufferedImage img) {
        int x=img.getWidth();
        int y=img.getHeight();

        Raster raster=img.getData();
        int[][] imgArr=new int[x+2*r][y+2*r];

        //pad on x axis
        for(int i=0; i<r; i++) {
            for(int j=r; j<y+r; j++) {
                imgArr[i][j]=raster.getSample(0,j-r,0);
                imgArr[x+2*r-i-1][j]=raster.getSample(x-1,j-r,0);
            }
        }

        //pad on y axis
        for(int i=r; i<x+r; i++) {
            for(int j=0; j<r; j++) {
                imgArr[i][j]=raster.getSample(i-r,0,0);
                imgArr[i][y+2*r-j-1]=raster.getSample(i-r,y-1,0);
            }
        }

        //original image in the middle
        for(int i=0; i<x; i++) {
            for(int j=0; j<y; j++) {
                imgArr[i+r][j+r]=raster.getSample(i,j,0);
            }
        }

        int maxg=-1;

        //convolution x axis
        for(int i=0;i<x; i++) {
            for(int j=0; j<y; j++){
                float gval=0;
                for(int k=0; k<2*r+1; k++) {
                    gval+=Gx[k]*imgArr[i+k][j+r];
                }
                gval=abs(gval/F);
                int g=(int) gval;
                imgArr[i+r][j+r]=g;
            }
        }

        //convolution y axis
        for(int i=0;i<x; i++) {
            for(int j=0; j<y; j++){
                float gval=0;
                for(int k=0; k<2*r+1; k++) {
                    gval+=Gx[k]*imgArr[i+r][j+k];
                }
                gval=abs(gval/F);
                int g=(int) gval;
                imgArr[i+r][j+r]=g;
                if(g>maxg) maxg=g;
            }
        }

        float scale=1;

        int[][] imgArr2=new int[x][y];
        for(int i=0; i<x; i++) {
            for(int j=0; j<y; j++) {
                imgArr2[i][j]=(int) (imgArr[i+r][j+r]*scale);
            }
        }
        BufferedImage output=new BufferedImage(x,y,img.getType());
        WritableRaster raster2=output.getRaster();

        for(int i=0; i<x; i++) {
            for(int j=0; j<y; j++) {
                raster2.setSample(i,j,0,imgArr2[i][j]);
            }
        }
        output.setData(raster2);

        return output;
    }

    /** Applies a gaussian blur on an image, by convolving with a 1-D gaussian kernel first on the X-axis and then on the Y-axis.
     * Pixels (kernel size/2) near the border are ignored and turned black.
     *
     * @param img image to blur
     * @param crop extra number pixels to ignore near the border
     * @return blurred image, with black pixels near the border
     */

    public BufferedImage blur(BufferedImage img, int crop) {
        int x=img.getWidth();
        int y=img.getHeight();

        Raster raster1=img.getRaster();
        int[][] imgArr=new int[x][y];

        int maxg=-1;

        //convolution x-axis
        for(int i=r+crop;i<x-r-crop; i++) {
            for(int j=0+crop; j<y-crop; j++){
                float gval=0;
                for(int k=0; k<2*r+1; k++) {
                    gval+=Gx[k]*raster1.getSample(i-r+k,j,0);
                }
                gval=abs(gval/F);
                int g=(int) gval;
                imgArr[i][j]=g;
            }
        }

        //convolution y-axis
        for(int i=0+crop;i<x-crop; i++) {
            for(int j=r+crop; j<y-r-crop; j++){
                float gval=0;
                for(int k=0; k<2*r+1; k++) {
                    gval+=Gx[k]*imgArr[i][j-r+k];
                }
                gval=abs(gval/F);
                int g=(int) gval;
                imgArr[i][j]=g;
                if(g>maxg) maxg=g;
            }
        }

        BufferedImage output=new BufferedImage(x,y,BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster2=output.getRaster();

        //scale to 0-255
        float scale=1;
        if(maxg>255) {
            scale=255f/maxg;
        }
        for(int i=r+crop; i<x-r-crop; i++) {
            for(int j=r+crop; j<y-r-crop; j++) {
                imgArr[i][j]=(int) (imgArr[i][j]*scale);
                raster2.setSample(i,j,0,imgArr[i][j]);
            }
        }

        output.setData(raster2);

        return output;
    }
    public BufferedImage blur(BufferedImage img) {
        return blur(img,0);
    }

    /** Removes a light gradient from an image by dividing it with a heavily blurred copy of itself.
     *  Any non black pixels are turned to white for contrast.
     *
     * @param img image to remove light
     * @param blurred heavily blurred image
     * @param crop number of pixels to ignore near the border
     *
     * @return black and white only image with light removed.
     */

    public static BufferedImage removeLight(BufferedImage img, BufferedImage blurred, int crop) {
        int x=img.getWidth();
        int y=img.getHeight();
        Raster raster1=img.getRaster();
        Raster raster2=blurred.getRaster();

        BufferedImage output=new BufferedImage(x,y,BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster=output.getRaster();

        for(int i=crop; i<x-crop; i++) {
            for(int j=crop; j<y-crop; j++) {
                int imgp=raster1.getSample(i,j,0);
                int blp=raster2.getSample(i,j,0);
                //divide images
                int gray=imgp;
                if(blp!=0) {
                    gray=imgp/blp;
                }
                //turn into binary map
                if(gray>0) {
                    raster.setSample(i,j,0,255);
                }
            }
        }
        output.setData(raster);
        return output;
    }

    public static BufferedImage removeLight(BufferedImage img, BufferedImage blurred) {
        return removeLight(img,blurred,0);
    }
}