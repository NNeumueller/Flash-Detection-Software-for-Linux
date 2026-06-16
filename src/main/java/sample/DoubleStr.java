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

import java.util.Locale;

public class DoubleStr {

    /**
     * Return the number as a string with decimals specified by the precision.
     *
     * @param d
     * @param precision
     * @return
     */
    public static String double2str(double d, int precision){
        String format = "%." + precision + "f";
        return String.format(Locale.ENGLISH, format, d);
    }

    /**
     * Returns the number as a string with 2 decimal points.
     *
     * @param d
     * @return
     */
    public static String double2str(double d){
        return double2str(d,2);
    }


    public static String array2str(double[] d){
        return array2str(d,1);
    }

    public static String array2str(double[] d, int precision){
        int n = d.length;
        String str = "(";
        for (int i=0; i<n-1; i++){
            str += double2str(d[i],precision) + " , ";
        }
        str += double2str(d[n-1],precision) + ")";
        return str;
    }

}