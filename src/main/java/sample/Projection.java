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

import static java.lang.Math.*;

/** Creates a projection of the lunar surface centered at any point
 * and fits lunar images onto a circle of same size
 *
 * @author Ivi Chatzi
 */
public class Projection {
    private final int r;
    private final double R;
    private final double centerLong;
    private final double centerLat;
    private BufferedImage ref;
    private final int xc;
    private final int yc;
    private final int ximpact_original;
    private final int yimpact_original;
    public int ximpact,yimpact;

    public BufferedImage proj;
    public BufferedImage fit;

    /** Constructor: Inputs all necessary info to create a lunar projection and circle fit image, ready for rotation and correlation.
     *  The projection will be an orthographic projection of radius equal to the circle that fits the limb
     *  The radius is rounded to the nearest integer.
     *  Creates two binary square images of size 2*radius+1 containing a white circle of given radius.
     *
     * @param circle an array containing the center coordinates and radius (pixels) of the circle that fits to the limb
     * @param l longitude of center of observable disc
     * @param f latitude of center of observable disc
     * @param cyl reference image of lunar surface (simple cylindrical projection)
     * @param x x pixel coordinate of impact flash on original photo
     * @param y y pixel coordinate of impact flash on original photo
     */
    public Projection(double[] circle, double l, double f, BufferedImage cyl, int x, int y) {

        r=(int)Math.round(circle[2]);
        centerLong=l;
        centerLat=f;
        ref=cyl;
        xc=(int)Math.round(circle[0]);
        yc=(int)Math.round(circle[1]);
        ximpact_original=x;
        yimpact_original=y;
        R=cyl.getHeight()/PI;

        proj=new BufferedImage(2*r,2*r+1,BufferedImage.TYPE_BYTE_GRAY);
        fit=new BufferedImage(2*r,2*r+1,BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster1=proj.getRaster();
        WritableRaster raster2=fit.getRaster();
        int d;
        for(int i=0; i<proj.getWidth(); i++) {
            for(int j=0; j<proj.getHeight(); j++) {
                d=(i-r)*(i-r)+(j-r)*(j-r)-r*r;
                if(d<=0) {
                    raster1.setSample(i,j,0,255);
                    raster2.setSample(i,j,0,255);
                }
            }
        }

        proj.setData(raster1);
        fit.setData(raster2);
    }

    /** Creates an orthographic projection of the lunar disk using a reference map.
     *  The projection is centered at the point given during class construction.
     *  1) Uses the binary proj image containing a white circle of radius r
     *  2) Corresponds each pixel of the white circle image to a pixel on the simple cylindrical map,
     *  using orthographic and simple cylindircal formulas
     *  3) Paints white circle with the corresponding pixel from the reference map
     */
    public void createProjection() {
        Raster raster1=proj.getRaster();
        Raster raster2=ref.getRaster();
        WritableRaster raster=proj.getRaster();
        for(int i=0; i<proj.getWidth(); i++) {
            for(int j=0; j<proj.getHeight(); j++) {
                if(raster1.getSample(i,j,0)==255) {
                    double[] sel=orth2plan(i-r,r-j);
                    double[] cyl=plan2cyl(sel[0],sel[1]);
                    //transform to image coordinates: (0,0) is top left
                    int x=(int)round(cyl[0])+ref.getWidth()/2;
                    int y=ref.getHeight()/2-(int)round(cyl[1]);

                    if(x==ref.getWidth()) {
                        x=ref.getWidth()-1;
                    }
                    if(y==ref.getHeight()) {
                        y=ref.getHeight()-1;
                    }
//                    if(x>=ref.getWidth() || y>=ref.getHeight()) {
//                        System.out.println(ref.getWidth()+" "+ref.getHeight());
//                        System.out.println(x+" "+y);
//                    }
//                    proj.setRGB(i,j,ref.getRGB(x,y));
                    raster.setSample(i,j,0,raster2.getSample(x,y,0));
                }
            }
        }
        proj.setData(raster);

    }

    /** Fits the lunar mask to a complete circle of same radius as the projection
     * and tracks new location of impact flash
     * Based on its distance from the center of the circle fit found,
     * the image is pasted onto the white circle image onto points of same distance from the center of that image
     *
     * @param mask masked lunar image
     */
    public void createFit(BufferedImage mask) {
        Raster raster1=mask.getRaster();
        WritableRaster raster2=fit.getRaster();
        for(int i=0; i<mask.getWidth(); i++) {
            for(int j=0; j<mask.getHeight(); j++) {
                int x=i-xc;
                int y=yc-j;

                if(i==ximpact_original && j==yimpact_original) {
                    ximpact=x+r;
                    yimpact=r-y;
                }
                int d= (i-xc)*(i-xc)+(j-yc)*(j-yc)-r*r;
                if(d<=0) {
                    if(x==r) {
                        continue;
                    }
//                    fit.setRGB(x+r,r-y,mask.getRGB(i,j));
                    raster2.setSample(x+r,r-y,0,raster1.getSample(i,j,0));
                }
            }
        }
        fit.setData(raster2);
    }

    /** Fits any version of the original photo to a complete circle of same radius as the projection
     * Based on its distance from the center of the circle fit found,
     * the image is pasted onto the white circle image onto points of same distance from the center of that image
     *
     * @param mask original photo of the moon, or any filtered version of it of same dimensions
     * @return an image of size 2*r+1 with the input image fit onto a circle of radius r
     */
    public BufferedImage fitView(BufferedImage mask) {
        Raster raster1=mask.getRaster();
        BufferedImage fitImage=new BufferedImage(2*r,2*r+1,BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster2=fitImage.getRaster();
        for(int i=0; i<proj.getWidth(); i++) {
            for(int j=0; j<proj.getHeight(); j++) {
                int d=(i-r)*(i-r)+(j-r)*(j-r)-r*r;
                if(d<=0) {
                    raster2.setSample(i,j,0,255);
                }
            }
        }

        for(int i=0; i<mask.getWidth(); i++) {
            for(int j=0; j<mask.getHeight(); j++) {
                int x=i-xc;
                int y=yc-j;

                int d=(int) ((i-xc)*(i-xc)+(j-yc)*(j-yc)-r*r);
                if(d<=0) {
                    if(x==r) {
                        continue;
                    }
                    raster2.setSample(x+r,r-y,0,raster1.getSample(i,j,0));
                }
            }
        }
        fitImage.setData(raster2);
        return fitImage;
    }

    /** Transforms pixel coordinates to lunar coordinates using orthographic projection formulas
     * centered around given center point
     *
     * @param x x coordinate (pixels)
     * @param y y coordinate (pixels)
     * @return array containing lunar longitude and latitude of input point in degrees
     */
    public double[] orth2plan(double x, double y) {
        double ro=Math.sqrt(x*x+y*y);
        if((int) ro==0) {
            return new double[] {centerLong,centerLat};
        }

        double c=asin(ro/r);

        double l0=Math.toRadians(centerLong);
        double f0=Math.toRadians(centerLat);

        double f=asin((cos(c)*sin(f0))+((y*sin(c)*cos(f0))/ro));
        double l=l0+atan2(x*sin(c),((ro*cos(c)*cos(f0))-(y*sin(c)*sin(f0))));

        return new double[] {Math.toDegrees(l),Math.toDegrees(f)};
    }

    /** Transforms lunar coordinates to pixel coordinates onto the projection using orthographic projection formulas
     * centered around given center point
     *
     * @param ll longitude (degrees)
     * @param ff y latitude (degrees)
     * @return array containing x and y coordinate of corresponding pixel on the projection
     */
    public double[] plan2orth(double ll, double ff) {
        double l0=Math.toRadians(centerLong);
        double f0=Math.toRadians(centerLat);
        double l=Math.toRadians(ll);
        double f=Math.toRadians(ff);


        double x=r*cos(f)*sin(l-l0);
        double y=r*((cos(f0)*sin(f))-(sin(f0)*cos(f)*cos(l-l0)));
        return new double[] {x,y};
    }

    /** Transforms lunar coordinates to pixel coordinates onto the reference cylindrical projection map
     * using simple cylindrical formulas
     *
     * @param ll longitude (degrees)
     * @param ff y latitude (degrees)
     * @return array containing x and y coordinate of corresponding pixel on the cylindrical projection map
     */
    public double[] plan2cyl(double ll, double ff) {
        if(ff>=90) {
            ff=180-ff;
            ll+=180;
        }
        if(ff<-90) {
            ff=-180-ff;
            ll+=180;
        }

        if(ll>=180) {
            ll-=360;
        }
        if(ll<-180) {
            ll+=360;
        }

        double l=Math.toRadians(ll);
        double f=Math.toRadians(ff);

        double x=R*(l-0);
        double y=R*(f-0);

        return new double[] {x,y};
    }


}