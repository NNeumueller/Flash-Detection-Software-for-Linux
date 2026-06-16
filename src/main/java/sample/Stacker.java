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
import java.awt.image.WritableRaster;

/**Contains a static function that creates the average of an array of images
 *
 * @author Ivi Chatzi
 */
public class Stacker {
    /** Function to stack images, takes as input an array of images
     * and returns their average image (same image type as input)
     *
     * @param images an array of images to be stacked
     * @return the average image
     */
    public static BufferedImage stackImages(BufferedImage[] images) {
        BufferedImage average=new BufferedImage(images[0].getWidth(),images[0].getHeight(),
                images[0].getType());
        WritableRaster raster=average.getRaster();
        for(int k=0; k<images[0].getHeight(); ++k) {
            for(int j=0; j<images[0].getWidth(); ++j) {
                float sum=0.0f;
                for(int i=0; i<images.length; ++i) {
                    sum=sum+images[i].getRaster().getSample(j,k,0);
                }
                raster.setSample(j,k,0,Math.round(sum/images.length));
            }
        }
        average.setData(raster);
        return average;
    }

}
