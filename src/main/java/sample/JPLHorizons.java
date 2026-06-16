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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;

/** Contains a static function that calls the JPL Horizons API to get information about the moon
 * @author Ivi Chatzi
 *
 */
public class JPLHorizons {
    /** Calls JPL Horizons API to get information about the lunar disc as seen from the time and location of observation.
     * Returns angular diameter of disc and lunar coordinates of center
     *
     * @param date date of observation (format YYYY-MM-DD)
     * @param time time of observation (format HH:MM:SS.mmm)
     * @param obsLong geographical longitude of observation (degrees)
     * @param obsLat geographical latitude of observation (degrees)
     * @param elev altitude of observation (km)
     *
     * @throws ConnectException the API took too long to respond
     *
     * @return an array containing the lunar longitude (degrees), lunar latitude (degrees) of center, angular diameter (arcsec) of disc observed
     */
    public static double[] getCenter(String date, String time, double obsLong, double obsLat, double elev) throws IOException, ConnectException {
        double obsSubLong=0;
        double obsSubLat=0;
        double ang=0;

        String starttime=time.substring(0,8);

        Integer stop=Integer.parseInt(date.substring(0,4))+1;
        String stopdate=String.valueOf(stop)+date.substring(4,10);

//            System.out.println(obsLong+" "+obsLat+" "+elev);
        String url_s="https://ssd.jpl.nasa.gov/api/horizons.api?format=text&COMMAND='301'&OBJ_DATA='NO'&MAKE_EPHEM='YES'&EPHEM_TYPE='OBSERVER'&CENTER='coord@399'&SITE_COORD='"+obsLong+","+obsLat+","+elev+"'&START_TIME='"+date+"%20"+starttime+"'&STOP_TIME='"+stopdate+"%20"+starttime+"'&STEP_SIZE='1'&QUANTITIES='13,14'";
        System.out.println(url_s);
        URL url = new URL(url_s);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();
        String res= String.valueOf(content);
        String[] parts = res.split("SOE");
        String[] parts2=parts[1].split("EOE");
        parts2=parts2[0].split("\\s+");
//            System.out.println(Arrays.toString(parts2));
        if(parts2[3].matches("[*a-zA-Z]+")) {
            ang=Double.parseDouble(parts2[4]);
            obsSubLong=Double.parseDouble(parts2[5]);
            obsSubLat=Double.parseDouble(parts2[6]);
        }
        else {
            ang=Double.parseDouble(parts2[3]);
            obsSubLong=Double.parseDouble(parts2[4]);
            obsSubLat=Double.parseDouble(parts2[5]);
        }

        return new double[]{obsSubLong,obsSubLat,ang};
    }
}