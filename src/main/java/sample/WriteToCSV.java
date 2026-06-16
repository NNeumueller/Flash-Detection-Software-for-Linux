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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static sample.TestingLMA.resultsLMA;

/** Creates and modifies the csv and the txt that contains the detection results.
 * @author Achlatis Stefanos Christofidi Georgia
 */

public class WriteToCSV {

    public static String FWHM_x;
    public static String FWHM_y;
    public static boolean NEOFlag = false;
    public static boolean satelliteFlag = false;
    public static boolean hotPixelFlag = false;
    public static boolean cosmicRayFlag = false;
    public static boolean outsideLimbFlag = false;
    public static boolean noEventFlag = false;
    public static boolean unknownFlag = false;

    /**
     * Checks if a sting is present in a file.
     *
     * @param fileName the path of the file
     * @param searchStr the string to be searched
     * @return yes if the file contains the string and no if not
     * @throws IOException
     */

    public static boolean contains(String fileName, String searchStr) throws IOException {
        Scanner scan = new Scanner(new File(fileName));
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String bufLine;
        while ((bufLine = br.readLine())!=null)
        if(bufLine.contains(searchStr)){
            return true;
        }
        while(scan.hasNext()) {
            String line = scan.nextLine().toLowerCase();
            if (line.contains(searchStr)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * Replaces a line in a file with another line.
     * @param path the path of the file
     * @param name the string that the line that will be changed contains
     * @throws IOException
     */

    public static void replaceLine(String path, String name) throws IOException {
        List<String> newLines = new ArrayList<>();
        for (String line : Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8)) {
            if (line.contains(name)) {
                if (NEOFlag == true && cosmicRayFlag == true)
                    newLines.add(line.replace(line, name + ";" + FWHM_x + ";" + FWHM_y + ";" + "MAYBE" + ";" + satelliteFlag + ";" + hotPixelFlag + ";" + "MAYBE" + ";" + outsideLimbFlag + ";" + resultsLMA));
                else if (Detection.cannotDetermineLimb)
                    newLines.add(line.replace(line, name + ";" + FWHM_x + ";" + FWHM_y + ";" + NEOFlag + ";" + satelliteFlag + ";" + hotPixelFlag + ";" + cosmicRayFlag + ";" + "Unknown" + ";" + resultsLMA));
                else {
                    newLines.add(line.replace(line, name + ";" + FWHM_x + ";" + FWHM_y + ";" + NEOFlag + ";" + satelliteFlag + ";" + hotPixelFlag + ";" + cosmicRayFlag + ";" + outsideLimbFlag + ";" + resultsLMA));
                }
            } else {
                newLines.add(line);
            }
        }
        Files.write(Paths.get(path), newLines, StandardCharsets.UTF_8);
    }


    /**
     * Creates and updates the csv with the detection results
     * @param folderPath the folder of the detection process
     * @param particularPath the path of each folder of the detection
     * @param xCoord the x coordinate of the event
     * @param yCoord the y coordinate of the event
     * @throws IOException
     */
    public static void createCSV(String folderPath, String particularPath, int xCoord, int yCoord) throws IOException {

        String onlyEventName = particularPath.split("/")[(particularPath.split("/")).length - 1];

        File detectionCsv = new File(folderPath + "/Detection_Results/OfflineDetectionResults.csv");
        System.out.println(particularPath);
        if (detectionCsv.exists()) {
            if(contains(folderPath + "/Detection_Results/OfflineDetectionResults.csv",onlyEventName)) {
                replaceLine(folderPath + "/Detection_Results/OfflineDetectionResults.csv",onlyEventName);
            }

            else {

                FileWriter csvWriter = new FileWriter(detectionCsv,true);

                csvWriter.write(onlyEventName + ";" + FWHM_x + ";" + FWHM_y + ";");

                if (NEOFlag == true && cosmicRayFlag == true)
                    csvWriter.write("MAYBE" + ";" + satelliteFlag + ";" + hotPixelFlag + ";" + "MAYBE" + ";" + outsideLimbFlag + ";");
                else if (Detection.cannotDetermineLimb)
                    csvWriter.write(NEOFlag + ";" + satelliteFlag + ";" + hotPixelFlag + ";" + cosmicRayFlag + ";" + "Unknown" + ";");
                else
                    csvWriter.write(NEOFlag + ";" + satelliteFlag + ";" + hotPixelFlag + ";" + cosmicRayFlag + ";" + outsideLimbFlag + ";");

                csvWriter.write(resultsLMA + "\n");

                csvWriter.close();
            }

            File particularDetectionCsv = new File(particularPath + "/Detection_Results/OfflineDetectionResults.csv");
            particularDetectionCsv.getParentFile().mkdirs();
            File particularParentDir = particularDetectionCsv.getParentFile();
            if (particularParentDir != null && !particularParentDir.exists() &&
                    !particularParentDir.mkdirs())
                throw new IOException("error while creating directories");

            FileWriter particularCsvWriter = new FileWriter(particularDetectionCsv);

            particularCsvWriter.write("Event Directory Name" + ";" + "FWHM x" + ";" + "FWHM y" + ";");
            particularCsvWriter.write("Impact Flash" + ";" + "Satellite" + ";" + "Hot Pixel" + ";");
            particularCsvWriter.write("Cosmic Ray" + ";" + "Event outside of the limb" + ";" + "Result:" + ";");
            particularCsvWriter.write("\n");


            particularCsvWriter.write(onlyEventName + ";" + FWHM_x + ";" + FWHM_y + ";");
            if (NEOFlag == true && cosmicRayFlag == true)
                particularCsvWriter.write("MAYBE" + ";" + satelliteFlag + ";" + hotPixelFlag + ";" + "MAYBE" + ";" + outsideLimbFlag + ";");
            else if (Detection.cannotDetermineLimb)
                particularCsvWriter.write(NEOFlag + ";" + satelliteFlag + ";" + hotPixelFlag + ";" + cosmicRayFlag + ";" + "Unknown" + ";");
            else
                particularCsvWriter.write(NEOFlag + ";" + satelliteFlag + ";" + hotPixelFlag + ";" + cosmicRayFlag + ";" + outsideLimbFlag + ";");

            particularCsvWriter.write(resultsLMA + "\n");

            particularCsvWriter.close();

            FileWriter detectionLogger = new FileWriter(particularPath + "/Detection_Results/Detection_Results.txt");
            detectionLogger.write(resultsLMA + "\n");
            detectionLogger.write("Detection completed.");
            detectionLogger.close();
        }

        else {
            detectionCsv.getParentFile().mkdirs();
            File parentDir = detectionCsv.getParentFile();
            if (parentDir != null && !parentDir.exists() &&
                    !parentDir.mkdirs())
                throw new IOException("error while creating directories");

            FileWriter csvWriter = new FileWriter(detectionCsv);

            csvWriter.write("Event Directory Name" + ";" + "FWHM x" + ";" + "FWHM y" + ";");
            csvWriter.write("Impact Flash" + ";" + "Satellite" + ";" + "Hot Pixel" + ";");
            csvWriter.write("Cosmic Ray" + ";" + "Event outside of the limb" + ";" + "Result:" + ";");
            csvWriter.write("\n");


            csvWriter.write(onlyEventName + ";" + FWHM_x + ";" + FWHM_y + ";");
            if (NEOFlag == true && cosmicRayFlag == true)
                csvWriter.write("MAYBE" + ";" + satelliteFlag + ";" + hotPixelFlag + ";" + "MAYBE" + ";" + outsideLimbFlag + ";");
            else if (Detection.cannotDetermineLimb)
                csvWriter.write(NEOFlag + ";" + satelliteFlag + ";" + hotPixelFlag + ";" + cosmicRayFlag + ";" + "Unknown" + ";");
            else
                csvWriter.write(NEOFlag + ";" + satelliteFlag + ";" + hotPixelFlag + ";" + cosmicRayFlag + ";" + outsideLimbFlag + ";");

            csvWriter.write(resultsLMA + "\n");


            csvWriter.close();

            File particularDetectionCsv = new File(particularPath + "/Detection_Results/OfflineDetectionResults.csv");

            particularDetectionCsv.getParentFile().mkdirs();
            File particularParentDir = particularDetectionCsv.getParentFile();
            if (particularParentDir != null && !particularParentDir.exists() &&
                    !particularParentDir.mkdirs())
                throw new IOException("error while creating directories");

            FileWriter particularCsvWriter = new FileWriter(particularDetectionCsv);

            particularCsvWriter.write("Event Directory Name" + ";" + "FWHM x" + ";" + "FWHM y" + ";");
            particularCsvWriter.write("Impact Flash" + ";" + "Satellite" + ";" + "Hot Pixel" + ";");
            particularCsvWriter.write("Cosmic Ray" + ";" + "Event outside of the limb" + ";" + "Result:" + ";");
            particularCsvWriter.write("\n");

            particularCsvWriter.write(onlyEventName + ";" + FWHM_x + ";" + FWHM_y + ";");
            if (NEOFlag == true && cosmicRayFlag == true)
                particularCsvWriter.write("MAYBE" + ";" + satelliteFlag + ";" + hotPixelFlag + ";" + "MAYBE" + ";" + outsideLimbFlag + ";");
            else if (Detection.cannotDetermineLimb)
                particularCsvWriter.write(NEOFlag + ";" + satelliteFlag + ";" + hotPixelFlag + ";" + cosmicRayFlag + ";" + "Unknown" + ";");
            else
                particularCsvWriter.write(NEOFlag + ";" + satelliteFlag + ";" + hotPixelFlag + ";" + cosmicRayFlag + ";" + outsideLimbFlag + ";");

            particularCsvWriter.write(resultsLMA + "\n");

            particularCsvWriter.close();

            FileWriter detectionLogger = new FileWriter(particularPath + "/Detection_Results/Detection_Results.txt");
            detectionLogger.write(resultsLMA + "\n");
            detectionLogger.write("(Coordinates: " + xCoord +", " + yCoord + ")." + "\n");
            if (Detection.cannotDetermineLimb) detectionLogger.write("Unable to determine the limb of the moon, due to noise. Please check manually if the event is outside of the limb." + "\n");
            detectionLogger.write("Detection completed.");
            detectionLogger.close();
        }

        NEOFlag = false;
        satelliteFlag = false;
        cosmicRayFlag = false;
        hotPixelFlag = false;
        outsideLimbFlag = false;

    }
}
