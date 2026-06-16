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
/** Contains a static function that applies a sobel filter on an image for edge detection
 *
 * @author Ivi Chatzi
 */
public class Sobel {

    /** Applies a sobel filter on an image for edge detection
     *
     * @param img the image to apply the filter
     * @param bright a brightness multiplication factor
     * @param crop number of pixels to ignore from the border
     * @return image after edge detection
     */
    public static BufferedImage sobel(BufferedImage img, float bright, int crop) {
        int x = img.getWidth();
        int y = img.getHeight();
        int[][] imgArr = new int[x][y];
        Raster raster1 = img.getRaster();
        int maxg = -1;
        //convolution with sobel kernel
        for (int i = 1 + crop; i < x - 1 - crop; i++) {
            for (int j = 1 + crop; j < y - 1 - crop; j++) {
                int val00 = raster1.getSample(i - 1,j - 1,0);
                int val01 = raster1.getSample(i - 1, j,0);
                int val02 = raster1.getSample(i - 1,j + 1,0);
                int val10 = raster1.getSample(i,j - 1,0);
                int val11 = raster1.getSample(i, j,0);
                int val12 = raster1.getSample(i,j + 1,0);
                int val20 = raster1.getSample(i + 1,j - 1,0);
                int val21 = raster1.getSample(i + 1, j,0);
                int val22 = raster1.getSample(i + 1,j + 1,0);
                int gx = ((-1*val00) + (0*val01) + (1*val02))
                        + ((-2*val10) + (0*val11) + (2*val12))
                        + ((-1*val20) + (0*val21) + (1*val22));
                int gy = ((-1*val00) + (-2*val01) + (-1*val02))
                        + ((0*val10) + (0*val11) + (0*val12))
                        + ((1*val20) + (2*val21) + (1*val22));

                //magnitude
                double gval = Math.sqrt((gx*gx) + (gy*gy));
                int g = (int) gval;
                if(maxg < g) {
                    maxg = g;
                }
                imgArr[i][j] = g;
            }
        }
        BufferedImage output;

            output = new BufferedImage(x, y, BufferedImage.TYPE_BYTE_GRAY);

        WritableRaster raster2 = output.getRaster();
        //scale to 0-255
        double scale = 255.0/maxg;
        for (int i = 1 + crop; i < x - 1 - crop; i++) {
            for (int j = 1 + crop; j < y - 1 - crop; j++) {
                int edgeColor = imgArr[i][j];

                //make it brighter
                if (edgeColor > 0) {
                    edgeColor = Math.min(maxg, (int)(edgeColor*bright));
                }
                edgeColor = (int) (edgeColor*scale);
                raster2.setSample(i, j,0, edgeColor);
            }
        }
        output.setData(raster2);
        return output;
    }
    public static BufferedImage sobel(BufferedImage image, float bright) {
        return sobel(image, bright,0);
    }
}