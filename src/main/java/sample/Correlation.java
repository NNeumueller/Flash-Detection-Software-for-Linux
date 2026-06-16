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

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Arrays;

/** Compares lunar masked image to a projection map by rotating and shifting the masked image
 * @author Ivi Chatzi
 */
public class Correlation {
    private final BufferedImage projMap;
    public int ximpact0;
    public int yimpact0;
    private final double[] circle;

    /** Constructor: stores all necessary info for correlation
     *
     * @param map binary map of orthographic projection of the lunar surface
     * @param x x pixel coordinate of impact flash on fit image
     * @param y y pixel coordinate of impact flash on fit image
     * @param c array containing center coordinates and radius of circle that fit the limb (pixels)
     */
    public Correlation(BufferedImage map, int x, int y, double[] c) {
        projMap=map;
        ximpact0=x;
        yimpact0=y;
        circle=c;
    }

    /** Rotates image around its center and compares with projection map
     *  to find the best matching rotation angle.
     *  For the eastern hemisphere the best rotation angle is the one with most matching black pixels.
     *  For the western hemisphere the matches are first normalised by a 3rd order polynomial fit
     *
     * @param fit0 unrotated image of lunar mask fit to circle (result of Projection.createFit)
     * @param range number of angles around the middle to check
     * @param mid middle angle
     * @param step difference between two consecutive angles to check
     * @param west boolean signifying hemisphere (true for west, false for east)
     * @return best matching rotation angle
     */
    public float correlate(BufferedImage fit0,int range, float mid, float step, boolean west) {
        int[] match=new int[2*range+1];
        float[] angle=new float[2*range+1];
        for(int i=-range; i<=range; i++) {
            angle[i+range]=i*step+mid;
            match[i+range]=0;
        }
        float bestangle=-range;
        int bestmatch=0;
        Raster raster1=projMap.getRaster();
        BufferedImage rotated;
        for(int k=0; k<=2*range; k++) {
            int[] impact0=new int[]{ximpact0, yimpact0};
            rotated=Correlation.rotate(fit0,angle[k],impact0);
            rotated=Local.binaryMap(rotated,0.5f);
//            match[k]=0;
//            Raster raster2=rotated.getRaster();
//
//            for(int i=0; i<projMap.getWidth(); i++) {
//                for(int j=0; j<projMap.getHeight(); j++) {
//                    double d=Math.sqrt((i-circle[2])*(i-circle[2])+(j-circle[2])*(j-circle[2]));
//                    if(d<=circle[2] && raster2.getSample(i,j,0)!=255 && raster1.getSample(i,j,0)==raster2.getSample(i,j,0)) {
//                        match[k]++;
//                    }
//                }
//            }
            match[k]=countMatches(rotated);
//            System.out.println("angle: "+angle[k]+"  match: "+match[k]);
            System.out.println(match[k]);
            if(match[k]>bestmatch) {
                bestmatch=match[k];
                bestangle=angle[k];
            }
        }

        if(west) {
            PolynomialCurveFitter fitter = PolynomialCurveFitter.create(3);
            WeightedObservedPoints obs=new WeightedObservedPoints();
            for(int i=0; i<=2*range; i++) {
                obs.add(angle[i],match[i]);
            }
            final double[] coeff = fitter.fit(obs.toList());
            System.out.println(Arrays.toString(coeff));
            double[] norm=new double[2*range+1];
            double bestmatch2=0;
            for(int i=0; i<=2*range; i++) {
                double poly=coeff[0]+coeff[1]*angle[i]+coeff[2]*angle[i]*angle[i]+coeff[3]*angle[i]*angle[i]*angle[i];
                norm[i]=match[i]/poly;
//                System.out.println(angle[i]+" "+match[i]+" "+poly+" "+norm[i]);
                if(norm[i]>bestmatch2) {
                    bestmatch2=norm[i];
                    bestangle=angle[i];
                }
            }
        }
//        bestangle=22f;
//        int[] impactRot=new int[]{ximpact0, yimpact0};
//        rotated=rotate(fit0,bestangle,impactRot);
//        rotated=Local.binaryMap(rotated,0.5f);
//        ximpact=impactRot[0];
//        yimpact=impactRot[1];
//        System.out.println(ximpact+" "+yimpact);

        return bestangle;
    }

    /** Compares rotated image to projection map and colors matching pixels.
     * The impact flash is coloured green
     *
     * @param rot rotated image
     * @param impact impact flash pixel coordinates
     * @param matchColor color of matching pixels
     * @param nonMatchColor color of non matching pixels
     * @return coloured comparison image
     */
    public BufferedImage overlay(BufferedImage rot, int[] impact, Color matchColor, Color nonMatchColor) {
        BufferedImage correlation=new BufferedImage(projMap.getWidth(),projMap.getHeight(),BufferedImage.TYPE_INT_RGB);

        for(int i=0; i<projMap.getWidth(); i++) {
            for(int j=0; j<projMap.getHeight(); j++) {
                correlation.setRGB(i,j,projMap.getRGB(i,j));
                double d=Math.sqrt((i-circle[2])*(i-circle[2])+(circle[2]-j)*(circle[2]-j));
                if(d<=circle[2] && rot.getRGB(i,j)!=-1 && projMap.getRGB(i,j)==rot.getRGB(i,j)) {
                    correlation.setRGB(i,j,matchColor.getRGB());
                }
                if(d<=circle[2] && rot.getRGB(i,j)!=-1 && projMap.getRGB(i,j)!=rot.getRGB(i,j)) {
                    correlation.setRGB(i,j,nonMatchColor.getRGB());
                }
            }
        }

        int w=projMap.getWidth();
        int h=projMap.getHeight();
        int x=impact[0];
        int y=impact[1];
        for(int i=Math.max(x-12,0); i<Math.min(x+13,w-1); i++) {
            for(int j=Math.max(y-2,0); j<Math.min(y+3,h-1); j++) {
                correlation.setRGB(i,j,new Color(20,255,80).getRGB());
            }
        }
        for(int i=Math.max(x-2,0); i<Math.min(x+3,w-1); i++) {
            for(int j=Math.max(y-12,0); j<Math.min(y+13,h-1); j++) {
                correlation.setRGB(i,j,new Color(20,255,80).getRGB());
            }
        }

        return correlation;
    }

    /** Compares a rotated image to the projection map and counts matching black pixels inside the circle.
     *
     * @param binmap rotated lunar image fit to a circle of same radius as the projection
     * @return number of matching black pixels inside the circle
     */

    public int countMatches(BufferedImage binmap) {
        int matches=0;
        for(int i=0; i<projMap.getWidth(); i++) {
            for(int j=0; j<projMap.getHeight(); j++) {
                double d=Math.sqrt((i-circle[2])*(i-circle[2])+(j-circle[2])*(j-circle[2]));
                if(d<=circle[2] && binmap.getRGB(i,j)!=-1 && projMap.getRGB(i,j)!=-1) {
                    matches++;
                }
            }
        }
        return matches;
    }

    /** Rotates an image around its center by a given angle and tracks the location of a specific point
     *
     * @param img image to rotate
     * @param angle angle to rotate
     * @param impact pixel coordinates of a point in the image to keep track after rotation
     * @return rotated image
     */
    public static BufferedImage rotate(BufferedImage img, double angle, int[] impact) {
        int x=img.getWidth();
        int y=img.getHeight();
        AffineTransform trans=new AffineTransform();
        trans.rotate(Math.toRadians(angle),x/2,y/2);
        Point pt=new Point(impact[0],impact[1]);
        Point2D newpt=trans.transform(pt,null);
        impact[0]=(int)Math.round(newpt.getX());
        impact[1]=(int)Math.round(newpt.getY());
//        System.out.println(angle+" "+Arrays.toString(impact));
        AffineTransformOp op=new AffineTransformOp(trans,AffineTransformOp.TYPE_BILINEAR);
        return op.filter(img,null);
    }

    /** Rotates an image around its center by a given angle and tracks the location of a specific point
     *  Does not return the image. The location of the point is updated.
     *
     * @param img image to rotate
     * @param angle angle to rotate
     * @param point pixel coordinates of a point in the image to keep track after rotation
     */

    public static void rotateAndTrack(BufferedImage img, double angle, int[] point) {
        int x=img.getWidth();
        int y=img.getHeight();
        AffineTransform trans=new AffineTransform();
        trans.rotate(Math.toRadians(angle),x/2,y/2);
        Point pt=new Point(point[0],point[1]);
        Point2D newpt=trans.transform(pt,null);
        point[0]=(int)Math.round(newpt.getX());
        point[1]=(int)Math.round(newpt.getY());
    }

    /** Shifts an image horizontally by a given number of pixels.
     *
     * @param img image to shift
     * @param xoffset number of pixels to shift
     * @return shifted image
     */

    //8 bit only
    public static BufferedImage shiftX(BufferedImage img, int xoffset) {
        int w=img.getWidth();
        int h=img.getHeight();
        Raster raster1=img.getRaster();
        BufferedImage shifted=new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster2=shifted.getRaster();

        for(int i=0; i<w; i++) {
            for(int j=0; j<h; j++) {
                if(i-xoffset<0 || i-xoffset>=w) {
                    raster2.setSample(i,j,0,0);
                }
                else {
                    raster2.setSample(i,j,0,raster1.getSample(i-xoffset,j,0));
                }
            }
        }
        shifted.setData(raster2);
        return shifted;
    }

    /** Shifts an image vertically by a given number of pixels.
     *
     * @param img image to shift
     * @param yoffset number of pixels to shift
     * @return shifted image
     */
    //8 bit only
    public static BufferedImage shiftY(BufferedImage img, int yoffset) {
        int w=img.getWidth();
        int h=img.getHeight();
        Raster raster1=img.getRaster();
        BufferedImage shifted=new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster2=shifted.getRaster();
        for(int i=0; i<w; i++) {
            for(int j=0; j<h; j++) {
                if(j-yoffset<0 || j-yoffset>=h) {
                    raster2.setSample(i,j,0,0);
                }
                else {
                    raster2.setSample(i,j,0,raster1.getSample(i,j-yoffset,0));
                }
            }
        }
        shifted.setData(raster2);
        return shifted;
    }

}