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
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/** Fits a circle to the lunar limb and creates a lunar mask
 * @author Ivi Chatzi
 */
public class CircleFit {

    /** Isolates pixels on the lunar limb.
     * Locates the brightest pixels of an image based on a brightness threshold.
     * Creates a new image where the bright pixels are white and the rest are black.
     *  Option to brighten top and bottom part of image.
     *
     * @param image image to read
     * @param threshold threshold to keep brightest pixel, corresponds to a % of the brightest pixel in the image
     * @param light % of top and bottom part of the image where a 15% lower threshold is applied
     * @return image where the limb pixels are white and the rest are black
     */

    public static BufferedImage isolateLimb(BufferedImage image, float threshold, float light) {
        int x=image.getWidth();
        int y=image.getHeight();

        int[][] imgArr=new int[x][y];
        Raster raster1=image.getRaster();

        int maxg=-1;

        BufferedImage output=new BufferedImage(x,y,BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster2=output.getRaster();

        for(int i=0; i<x; i++) {
            for(int j=0; j<y; j++) {
                imgArr[i][j]=raster1.getSample(i,j,0);
                if(maxg<imgArr[i][j]) maxg=imgArr[i][j];
            }
        }
        for(int i=0; i<x; i++) {
            for(int j=0; j<y; j++) {
                if(j<light*y) {
                    if(imgArr[i][j]<(int) ((threshold-0.15f)*maxg)) {
                        imgArr[i][j]=0;
                    }
                    else {
                        imgArr[i][j]=255;
                    }
                }
                else if(j>=(y-light*y-1)) {
                    if(imgArr[i][j]<(int) ((threshold-0.15f)*maxg)) {
                        imgArr[i][j]=0;
                    }
                    else {
                        imgArr[i][j]=255;
                    }
                }
                else {
                    if(imgArr[i][j]<(int)(threshold*maxg)) {
                        imgArr[i][j] = 0;
                    }
                    else {
                        imgArr[i][j] = 255;
                    }
                }

                raster2.setSample(i,j,0,imgArr[i][j]);
            }
        }
        output.setData(raster2);
        return output;
    }

    /** Locates the pixel coordinates of the detected limb pixels. To be used on the result of CircleFit.isolateLimb
     *
     * @param limb the binary image corresponding to the limb
     * @return an array of pixel coordinates
     */

    public static ArrayList<int[]> getEdge(BufferedImage limb) {
        int x=limb.getWidth();
        int y=limb.getHeight();
        ArrayList<int[]> pixels=new ArrayList<>();
        Raster raster=limb.getRaster();
        for(int i=0; i<x; i++) {
            for(int j=0; j<y; j++) {
                if(raster.getSample(i,j,0)==255) {
                    pixels.add(new int[] {i,j});
                }
            }
        }
        return pixels;
    }

    /** Applies Landau's circle fit algorithm to a given set of points.
     *
     * @param points array of pixel coordinates of points to fit circle
     * @return an array containing the x coordinate, y coordinate of the center, and the radius of the circle (pixels)
     */

    public static double[] landau(ArrayList<int[]> points) {
        int N=points.size();
        int x,y;
        double p1=0;
        double p2=0;
        double p3=0;
        double p4=0;
        double p5=0;
        double p6=0;
        double p7=0;
        double p8=0;
        double p9=0;
        double a1,b1,a2,b2,c1,c2;

        for (int i=0; i<N; i++){
            x=points.get(i)[0];
            y=points.get(i)[1];
            p1+=x;
            p2+=x*x;
            p3+=x*y;
            p4+=y;
            p5+=y*y;
            p6+=x*x*x;
            p7+=x*y*y;
            p8+=y*y*y;
            p9+=x*x*y;
        }

        a1=2*(p1*p1-N*p2);
        b1=2*(p1*p4-N*p3);
        a2=b1;
        b2=2*(p4*p4-N*p5);
        c1=p2*p1-N*p6+p1*p5-N*p7;
        c2 =p2*p4-N*p8+p4*p5-N*p9;

        double xc=(c1*b2-c2*b1)/(a1*b2-a2*b1);
        double yc=(a1*c2-a2*c1)/(a1*b2-a2*b1);
        double r=Math.sqrt((p2-2*p1*xc+N*xc*xc+p5-2*p4*yc+N*yc*yc)/N);

        return new double[]{xc,yc,r};
    }


    /** Creates a mask of the lunar disc based on a circle fit to the limb
     *  The mask size is 20% for the western hemisphere and 30% for the eastern.
     *
     * @param image image to mask
     * @param circle array containing coordinates of center and radius (pixels) of circle that fits image
     * @param west boolean signifying hemisphere (true for west)
     * @return Masked version of the original image. Pixels inside the limb, outside the mask are set to white. Pixels outside the limb, outside the mask are set to black.
     */

    public static BufferedImage lunarMask(BufferedImage image, double[] circle,boolean west) {
        int x=image.getWidth();
        int y=image.getHeight();
        BufferedImage output=new BufferedImage(x,y,BufferedImage.TYPE_BYTE_GRAY);
        Raster raster1=image.getRaster();
        WritableRaster raster2=output.getRaster();

        //define maskSize based on hemisphere: 0.7 for east, 0.8 for west
        float maskSize=0.7f;
        if(west) {
            maskSize=0.8f;
        }

        for(int i=0; i<x; i++) {
            for(int j=0; j<y; j++) {
                double d=Math.sqrt((i-circle[0])*(i-circle[0])+(j-circle[1])*(j-circle[1]));
                if(d>=maskSize*circle[2] && d<=circle[2]) {
//                    output.setRGB(i,j,image.getRGB(i,j));
                    raster2.setSample(i,j,0,raster1.getSample(i,j,0));
                }
                if(d<maskSize*circle[2]) {
//                    output.setRGB(i,j,-1);
                    raster2.setSample(i,j,0,255);
                }
            }
        }
        output.setData(raster2);
        return output;
    }

    /** Adds the edge of circle onto an image in a given color.
     *
     * @param circle array containing center coordinates and radius (pixels) of circle
     * @param back image to add circle onto
     * @param circleColor color of circle to add
     * @return image with red circle edge added on to it
     */

    public static BufferedImage edgeRed(double[] circle, BufferedImage back, Color circleColor) {
        int x=back.getWidth();
        int y=back.getHeight();
        BufferedImage edge=Sobel.sobel(showCircle(x,y,circle),16,1);
        BufferedImage output=new BufferedImage(x, y, BufferedImage.TYPE_INT_RGB);
        for(int i=0; i<x; i++) {
            for(int j=0; j<y; j++) {
                output.setRGB(i,j,back.getRGB(i,j));
                if(edge.getRGB(i,j)==-1) {
                    output.setRGB(i,j,circleColor.getRGB());
                }
            }
        }
        return output;
    }

    /** Given a center and radius for a circle, creates a mask where all points inside the disk are white and outside are black.
     *
     * @param x width of image
     * @param y height of image
     * @param circle array containing center coordinates and radius (pixels) of circle
     * @return mask image
     */

    public static BufferedImage showCircle(int x, int y, double[] circle) {
        BufferedImage output=new BufferedImage(x,y,BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster=output.getRaster();
        for(int i=0; i<x; i++) {
            for(int j=0; j<y; j++) {
                int d= (int) Math.round((i-circle[0])*(i-circle[0])+(j-circle[1])*(j-circle[1])-circle[2]*circle[2]);
                if(d<1) {
                    raster.setSample(i,j,0,255);
                }
            }
        }
        output.setData(raster);
        return output;
    }

    /** Randomly samples a set of points by separating them into connected components and picking a set number of sampled from each cluster.
     *  Requires an image showing the points in white as well as an array of their coordinates.
     *
     * @param limb a binary image with white pixels corresponding to the limb (result of CircleFit.isolateLimb)
     * @param edge coordinates of white pixels of limb (result of CircleFit.getEdge)
     * @param samples number of samples from each connected component
     * @return array of pixel coordinates of random points sampled from input points
     */

    public static ArrayList<int[]> edgeRansac(BufferedImage limb, ArrayList<int[]> edge, int samples)  {
        int x=limb.getWidth();
        int y=limb.getHeight();

        int[][] labels=new int[x][y];
        Raster raster=limb.getRaster();

        int Npixels=0;

        //label limb points as -1
        for(int i=0; i<x; i++) {
            for(int j=0; j<y; j++) {
                if(raster.getSample(i,j,0)==255) {
                    Npixels++;
                    labels[i][j]=-1;
                }
            }
        }

        int addedPixels=0;
        int label=0;

        while(addedPixels<Npixels) {

            int[] basePoint=edge.remove(0);
            int xb=basePoint[0];
            int yb=basePoint[1];

            //already labelled point
            if(labels[xb][yb]!=-1) {
                continue;
            }

            //new unconnected point, add to queue
            label++;
            LinkedList<int[]> queue=new LinkedList<int[]>();
            queue.add(new int[] {xb,yb});
            boolean[][] inQueue=new boolean[x][y];
            inQueue[xb][yb]=true;

            while(!queue.isEmpty()) {
                //label point
                int[] point=queue.remove();
                int xx=point[0];
                int yy=point[1];
                labels[xx][yy]=label;
                addedPixels++;

                //check neighbours
                //if they belong to the limb (-1) and they are not in the queue yet, add them
                if(xx>0 && labels[xx-1][yy]==-1 && !inQueue[xx-1][yy]) {
                    queue.add(new int[]{xx-1,yy});
                    inQueue[xx-1][yy]=true;
                }
                if(yy>0 && labels[xx][yy-1]==-1 && !inQueue[xx][yy-1]) {
                    queue.add(new int[]{xx,yy-1});
                    inQueue[xx][yy-1]=true;
                }
                if(xx<x-1 && labels[xx+1][yy]==-1 && !inQueue[xx+1][yy]) {
                    queue.add(new int[]{xx+1,yy});
                    inQueue[xx+1][yy]=true;
                }
                if(yy<y-1 && labels[xx][yy+1]==-1 && !inQueue[xx][yy+1]) {
                    queue.add(new int[]{xx,yy+1});
                    inQueue[xx][yy+1]=true;
                }

            }
        }

        //put components in a list of lists
        ArrayList<ArrayList<int[]>> conncomps=new ArrayList<>();
        for(int l=0; l<label; l++) {
            conncomps.add(new ArrayList<>());
        }
        for(int i=0; i<x; i++) {
            for(int j=0; j<y; j++) {
                if(labels[i][j]>0) {
                    conncomps.get(labels[i][j]-1).add(new int[]{i,j});
                }
            }
        }

        ArrayList<int[]> ransacEdge=new ArrayList<>();

        for(int l=0; l<label; l++) {
            int compSize=conncomps.get(l).size();
            boolean[] picked=new boolean[compSize];
            Random rd=new Random();
            for(int k=0; k<samples; k++) {
                int p=rd.nextInt(compSize);
                if(picked[p]) {
                    continue;
                }
                ransacEdge.add(conncomps.get(l).get(p));
                picked[p]=true;
            }
        }
        return ransacEdge;
    }
}