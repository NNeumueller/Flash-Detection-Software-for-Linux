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

import javafx.fxml.FXML;

import java.io.*;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.swing.*;

import static sample.Detection.Test;

/** Manages the starting screen
 * @author Achlatis Stefanos Christofidi Georgia Chatzi Ivi
 */

public class Controller {
    public static String darkPath = null;
    public static String flatPath = null;
    public static FileWriter metadataFile = null;

    public static ArrayList<String> subfolders = new ArrayList<>();
    public static int events=subfolders.size();

    public static int ROIsize = 30;

    public static String folderPath;
    public static int finalevents;
    public static double focalLength;
    public static int calibrationConstant = 100;
    public static double param1 = 100;
    public static double param2 = 30;
    public static double param3 = 3;
    public static double param4 = 1;
    public static double param5 = 2.7;
    public static double param6 = 1.5;
    public static double param7 = 1.2;
    public static double param8 = 2.7;
    public static double param9 = 1.5;
    public static double param10 = 2;
    public static double param11 = 1.5;
    public static double param12 = 1.5;
    public static double param13 = 1;
    public static double param14 = 1.5;
    public static double param15 = 2;


    /** Manages localization starting screen.
     * Contains a directory chooser for the user to pick a directory.
     * Contains text input fields for observatory parameters, which read/write localization_observer_info.properties file
     * Contains a button to start localization.
     *
     * If a directory with multiple events is chosen, performs localization sequentially on all events.
     * If the directory contains an OfflineDetectionResults.csv file, performs localization only on NEO events and
     * writes results to the csv after the final event completes.
     * If a directory with a single event without a metadata file is chosen, displays a pop-up window prompting to create a custom metadata file
     *
     * In case of an exception occurring, the next instance of localization begins
     *
     * Displays an error screen if no folder is chosen or if an Event_Metadata.txt file can't be found (except for the case of a single event).
     * Displays an error screen for invalid input:
     *      -Observatory longitude must be between -360 and 360
     *      -Observatory latitude must be between -180 and 180
     *      -Observatory elevation must be between 0 and 8
     *      -Telescope focal length may be left blank
     *
     * Handles close requests for the localization window: writes final data to localization_logger.txt file, closes streams
     * and continues localization to next event if applicable.
     *
     * @param event
     */
    @FXML
    void localizationButton(ActionEvent event) {
        folderPath=null;
        Stage primaryStage = new Stage();
        primaryStage.setTitle("FDS: Localization");
        Image icon = new Image(getClass().getResourceAsStream( "icon.png" ));
        primaryStage.getIcons().add(icon);

        DirectoryChooser directoryChooser = new DirectoryChooser();

        //directoryChooser.setInitialDirectory(new File("test_1"));
        javafx.scene.control.Label label1 = new Label();
        label1.setText("No directory chosen");

        Button button = new Button("Select Directory");
        button.setOnAction(e -> {
            File selectedDirectory = null;
            ////edw allagh epikindunh
            try {
                selectedDirectory = directoryChooser.showDialog(primaryStage);
            }
            catch (IllegalArgumentException ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, ex+"");
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.showAndWait();
                return;
            }

            //System.out.println(selectedDirectory.getAbsolutePath());
            if(selectedDirectory!=null) {
                folderPath = selectedDirectory.getAbsolutePath();
            }

            System.out.println("The path of the chosen folder is " + folderPath);
            //Local.localization();
            label1.setText(folderPath);

        });

        String siteLongitude="0";
        String siteLatitude="0";
        String siteElevation="0";
        String focalLength="0";
        String propertiesPath=System.getProperty("user.dir")+"/localization_observer_info.properties";
        File localPropFile=new File(propertiesPath);
        if(localPropFile.exists()) {
            try {
                BufferedReader br=new BufferedReader(new FileReader(localPropFile));
                siteLongitude=br.readLine().split("\\s+")[1];
                siteLatitude=br.readLine().split("\\s+")[1];
                siteElevation=br.readLine().split("\\s+")[1];
                focalLength=br.readLine().split("\\s+")[1];
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, e+"");
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.showAndWait();
                return;
            }
        }
        else {
            siteLongitude="0";
            siteLatitude="0";
            siteElevation="0";
            focalLength="0";
        }

        TextField longitudeInput=new TextField();
        longitudeInput.setText(siteLongitude);
        TextField latitudeInput=new TextField();
        latitudeInput.setText(siteLatitude);
        TextField elevationInput=new TextField();
        elevationInput.setText(siteElevation);
        TextField focalLengthInput=new TextField();
        focalLengthInput.setText(focalLength);
        Label labelInfo=new Label("Enter observatory information:");
        Label labelLong=new Label("Observatory longitude (deg):");
        Label labelLat=new Label("Observatory latitude (deg):");
        Label labelElev=new Label("Observatory altitude (km):");
        Label labelFocalLength=new Label("(Optional) Telescope focal length (mm):");

        HBox longLayout=new HBox(10);
        longLayout.getChildren().addAll(labelLong,longitudeInput);
        longLayout.setAlignment(Pos.CENTER);
        HBox latLayout=new HBox(10);
        latLayout.getChildren().addAll(labelLat,latitudeInput);
        latLayout.setAlignment(Pos.CENTER);
        HBox elevLayout=new HBox(10);
        elevLayout.getChildren().addAll(labelElev,elevationInput);
        elevLayout.setAlignment(Pos.CENTER);
        HBox focalLayout=new HBox(10);
        focalLayout.getChildren().addAll(labelFocalLength,focalLengthInput);
        focalLayout.setAlignment(Pos.CENTER);

        Button button1 = new Button("Start Localization");
        button1.setOnAction(e -> {
            double siteLong = 0;
            double siteLat = 0;
            double siteElev = 0;
            double focal = 0;
            try {
                siteLong = Double.parseDouble(longitudeInput.getText());
                siteLat = Double.parseDouble(latitudeInput.getText());
                siteElev = Double.parseDouble(elevationInput.getText());
                if(focalLengthInput.getText()!="" || focalLengthInput.getText()!=null) {
                    focal = Double.parseDouble(focalLengthInput.getText());
                }
                else {
                    focal=0;
                }
                if(siteLong<-360 || siteLong>360) throw new NumberFormatException();
                if(siteLat<-180 || siteLat>180) throw new NumberFormatException();
                if(siteElev<0 || siteElev>8) throw new NumberFormatException();

                FileWriter writer = new FileWriter(localPropFile);
                writer.write("SITELONG: "+siteLong+"\nSITELAT: "+siteLat+"\nSITEELEV: "+siteElev+"\nFOCALLENGTH: "+focal);
                writer.close();
            }
            catch(NumberFormatException nfe) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid input\n\nLongitude must be between -360 and 360\nLatitude must be between -180 and 180\nElevation must be between 0 and 8");
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.showAndWait();
                return;
            } catch (IOException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, ex+"");
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.showAndWait();
                return;
            }

            if(folderPath==null) {
                Alert alert=new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("No folder selected");
                alert.showAndWait();
                return;
            }

            final File folder=new File(folderPath);
//            File event_metad=new File(folderPath+"/Event_Metadata.txt");
            if(new File(folderPath+"/frame_000.png").exists() || new File(folderPath+"/frame_000.jpg").exists() || new File(folderPath+"/frame_000.fits").exists()) {
                try {
                    File event_metad=new File(folderPath+"/Event_Metadata.txt");
                    LocalizationController localController;
                    if(event_metad.exists()) {
                        localController = new LocalizationController(folderPath);
                        localController.window.setOnCloseRequest(cr -> {
                            cr.consume();
                            String endTimeStamp = String.valueOf(new Timestamp((new Date()).getTime()));
                            try {
                                localController.metadataFile.write("Localization stopped at " + endTimeStamp + "." + '\n');
                                localController.metadataFile.close();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                                Alert alert2 = new Alert(Alert.AlertType.ERROR, "" + ex);
                                alert2.setTitle("Error");
                                alert2.setHeaderText(null);
                                alert2.showAndWait();
                            } finally {
                                folderPath=null;
                                localController.window.close();
                            }
                        });
                        localController.init(new double[]{siteLong, siteLat, siteElev}, focal);
                    }
                    else {
                        Alert alert1=new Alert(Alert.AlertType.CONFIRMATION, "There is no Event_Metadata.txt file in this directory. Create one now?",ButtonType.YES, ButtonType.NO);
                        alert1.setTitle("Missing metadata file");
                        alert1.setHeaderText(null);
                        alert1.showAndWait();
                        if(alert1.getResult() == ButtonType.YES) {
                            createMetadataScreen(folderPath);
                        }
                        else if(alert1.getResult()==ButtonType.NO) {
                            return;
                        }
                        return;
                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR, ex+"");
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.showAndWait();
                    return;
                }
            }
            else {
                try {
                    ArrayList<String> subfolders = new ArrayList<>();
                    ArrayList<Boolean> linesToUpdate=new ArrayList<>();
                    File detectionCsv=new File(folderPath+"/OfflineDetectionResults.csv");
                    if (detectionCsv.exists()) {
                        BufferedReader br = new BufferedReader(new FileReader(folderPath+"/OfflineDetectionResults.csv"));
                        String line = br.readLine();
                        while ((line = br.readLine()) != null) {
                            String pathTemp=folderPath+"/"+line.split(",")[0];
                            File event_metadt=new File(pathTemp+"/Event_Metadata.txt");
                            String suggestion=line.split(",")[7];
                            if(suggestion.contains("Flash")) {
                                if(event_metadt.exists()) {
//                                    subfolders.add(pathTemp);
                                    subfolders.add(line.split(",")[0]);
                                    linesToUpdate.add(true);
                                }
                                else {
                                    linesToUpdate.add(false);
                                    Alert alert=new Alert(Alert.AlertType.WARNING);
                                    alert.setTitle("Warning");
                                    alert.setHeaderText(null);
                                    alert.setContentText("Missing Event_Metadata.txt file in "+line.split(",")[0]);
                                    alert.showAndWait();
                                }
                            }
                            else linesToUpdate.add(false);
                        }
                        br.close();
                    }
                    else {
                        for(final File fileEntry:folder.listFiles()) {
                            if(fileEntry.isDirectory()) {
                                String pathTemp=folderPath+"/"+fileEntry.getName();
                                File event_metadt=new File(pathTemp+"/Event_Metadata.txt");
                                if(event_metadt.exists()) {
                                    subfolders.add(fileEntry.getName());
                                }
                            }
                        }
                    }

                    if(subfolders.isEmpty()) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.setContentText("There is no event with an Event_Metadata.txt file in this directory");
                        alert.showAndWait();
                        return;
                    }

                    int events=subfolders.size();
                    AtomicInteger cur= new AtomicInteger(0);
                    LocalizationController[] localControllers=new LocalizationController[events];
                    for(int i=0; i<events; i++) {
//                        localControllers[i]=new LocalizationController(subfolders.get(i));
                        localControllers[i]=new LocalizationController(folderPath+"/"+subfolders.get(i));
                        double finalSiteLong = siteLong;
                        double finalSiteLat = siteLat;
                        double finalSiteElev = siteElev;
                        double finalFocal = focal;
                        localControllers[i].window.setOnCloseRequest(cr -> {
                            cr.consume();
                            String endTimeStamp = String.valueOf(new Timestamp((new Date()).getTime()));
                            try {
                                localControllers[cur.get()].metadataFile.write("Localization stopped at " + endTimeStamp + "." + '\n');
                                localControllers[cur.get()].metadataFile.close();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                                Alert alert2 = new Alert(Alert.AlertType.ERROR, "" + ex);
                                alert2.setTitle("Error");
                                alert2.setHeaderText(null);
                                alert2.showAndWait();
                            } finally {
                                localControllers[cur.get()].window.close();
                                try {
                                    //next event
                                    if (cur.get() < events - 1) {
                                        cur.getAndIncrement();
                                        localControllers[cur.get()].init(new double[]{finalSiteLong, finalSiteLat, finalSiteElev}, finalFocal);
                                    }
                                    //ended, write to csv if exists
                                    else {
                                        if (detectionCsv.exists()) {
                                            File newCsv = new File(folderPath + "/resultsNew.csv");
                                            FileWriter csvWriter = new FileWriter(newCsv);
                                            BufferedReader br = new BufferedReader(new FileReader(folderPath + "/OfflineDetectionResults.csv"));
                                            String line = br.readLine();
                                            csvWriter.write(line + "\n");
                                            int j=0;
                                            int k=0;
                                            while((line = br.readLine()) != null) {
                                                String lineFolder = line.split(",")[0];
                                                if(linesToUpdate.get(j)) {
                                                    System.out.println("line "+j+" subfolder "+k);

                                                    double localX = localControllers[k].local.lunar[0];
                                                    double localY = localControllers[k].local.lunar[1];
                                                    String lunarX = "NaN";
                                                    String lunarY = "NaN";
                                                    if (!Double.isNaN(localX)) {
                                                        localX = Math.round(localX * 100) / 100.0;
                                                        lunarX = String.valueOf(localX);
                                                    }
                                                    if (!Double.isNaN(localY)) {
                                                        localY = Math.round(localY * 100) / 100.0;
                                                        lunarY = String.valueOf(localY);
                                                    }

                                                    String[] values = line.split(",");
                                                    StringBuilder newLine = new StringBuilder();
                                                    for (int c = 0; c < 8; c++) {
                                                        newLine.append(values[c]).append(",");
                                                    }
                                                    newLine.append(lunarX).append(",").append(lunarY);
                                                    csvWriter.write(newLine + "\n");
                                                    k++;
                                                }
                                                else {
                                                    csvWriter.write(line + "\n");
                                                }
                                                j++;
                                            }
                                            csvWriter.close();
                                            br.close();
                                            boolean r1 = detectionCsv.delete();
                                            boolean r2 = newCsv.renameTo(detectionCsv);
                                            if (!r1 || !r2) {
                                                System.out.println(r1 + " " + r2);
                                                throw new IOException("Failed to update CSV file");
                                            }
                                        }
                                        folderPath=null;
                                    }
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                    Alert alert2 = new Alert(Alert.AlertType.ERROR, ex + "");
                                    alert2.setTitle("Error");
                                    alert2.setHeaderText(null);
                                    alert2.showAndWait();
                                    return;
                                }

                            }
                        });
                    }

                    localControllers[0].init(new double[]{siteLong, siteLat, siteElev}, focal);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR, ex+"");
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.showAndWait();
                    return;
                }
            }

            primaryStage.close();

        });


        primaryStage.initModality(Modality.APPLICATION_MODAL);
        primaryStage.setTitle("FDS: Localization");
        primaryStage.setMinWidth(250);

        javafx.scene.control.Label label = new Label();
        label.setText("Please choose a folder first and then start the process.");


        VBox layout = new VBox(10);
        layout.getChildren().addAll(label,button,label1,labelInfo,longLayout,latLayout,elevLayout,focalLayout, button1);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 500, 500);


        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        //primaryStage.show();
        primaryStage.showAndWait();
    }

    /** Handles metadata creation screen
     * Contains input fields for user to add metadata:
     *  -Data and time of event
     *  -Number of frames
     *  -Impact frame and pixel location
     *  -Camera pixel size
     *  -Radio buttons to pick whether bin2 or 16bit mode was enabled
     *
     * Checks if input is valid and writes dummy metadata file with all information required for localization
     *
     * @param path event folder path
     */

    private void createMetadataScreen(String path) {
        Stage metawindow=new Stage();
        Image icon = new Image(getClass().getResourceAsStream( "icon.png" ));
        metawindow.getIcons().add(icon);
        metawindow.initModality(Modality.APPLICATION_MODAL);
        metawindow.setTitle("FDS: Localization");

        Label instrLabel=new Label("Fill in the following information necessary for localization.");

        DatePicker d=new DatePicker();
        HBox datelayout=new HBox(5);
        datelayout.setAlignment(Pos.CENTER);
        datelayout.getChildren().addAll(new Label("Date of event:"),d);

        TextField timeField=new TextField();
        timeField.setPromptText("hh:mm:ss");
        HBox timelayout=new HBox(5);
        timelayout.setAlignment(Pos.CENTER);
        timelayout.getChildren().addAll(new Label("Time of event:"),timeField);

        TextField framesField=new TextField();
        HBox frameslayout=new HBox(5);
        frameslayout.setAlignment(Pos.CENTER);
        frameslayout.getChildren().addAll(new Label("Number of frames:"),framesField);

        TextField impactfrField=new TextField();
        HBox impactfrlayout=new HBox(5);
        impactfrlayout.setAlignment(Pos.CENTER);
        impactfrlayout.getChildren().addAll(new Label("Impact frame number:"),impactfrField);

        TextField impactxField=new TextField();
        HBox impactxlayout=new HBox(5);
        impactxlayout.setAlignment(Pos.CENTER);
        impactxlayout.getChildren().addAll(new Label("Impact x coordinate (pixels):"),impactxField);

        TextField impactyField=new TextField();
        HBox impactylayout=new HBox(5);
        impactylayout.setAlignment(Pos.CENTER);
        impactylayout.getChildren().addAll(new Label("Impact y coordinate (pixels):"),impactyField);

        TextField cameraField=new TextField();
        HBox cameralayout=new HBox(5);
        cameralayout.setAlignment(Pos.CENTER);
        cameralayout.getChildren().addAll(new Label("Camera pixel size (microns):"),cameraField);

        ToggleGroup tg16bit=new ToggleGroup();
        RadioButton yes16bit=new RadioButton("Yes");
        yes16bit.setToggleGroup(tg16bit);
        RadioButton no16bit=new RadioButton("No");
        no16bit.setSelected(true);
        no16bit.setToggleGroup(tg16bit);
        VBox radio16=new VBox(5);
        radio16.getChildren().addAll(new Label("Is the bit depth more than 8 bit?"),yes16bit,no16bit);
        radio16.setAlignment(Pos.CENTER);

        ToggleGroup tgBin2=new ToggleGroup();
        RadioButton yesbin2=new RadioButton("Yes");
        yesbin2.setToggleGroup(tgBin2);
        RadioButton nobin2=new RadioButton("No");
        nobin2.setToggleGroup(tgBin2);
        nobin2.setSelected(true);
        VBox radiobin=new VBox(5);
        radiobin.getChildren().addAll(new Label("Is bin2 enabled?"),yesbin2,nobin2);
        radiobin.setAlignment(Pos.CENTER);

        Button writeButton=new Button("Create metadata file");
        writeButton.setOnAction(e -> {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm:ss");
            try {
                if(d.getValue()==null) throw new DateTimeParseException("","",0);
                String date=dateFormatter.format(d.getValue());
                System.out.println(date);

                if(timeField.getText()=="" || timeField.getText()=="") throw new DateTimeParseException("","",0);
                String time= timeFormatter.format(timeFormatter.parse(timeField.getText()));

                if(framesField.getText()=="" || impactfrField.getText()=="" || impactxField.getText()=="" || impactyField.getText()=="" || cameraField.getText()=="") {
                    throw new IllegalArgumentException("Please fill in all fields.");
                }

                String frames=framesField.getText();
                int numframes= Integer.parseInt(frames);
                if(numframes<1) throw new IllegalArgumentException("\n\nNumber of frames must be positive!");

                String impact=impactfrField.getText();
                int impactnum= Integer.parseInt(impact);
                if(impactnum<0) throw new IllegalArgumentException("\n\nImpact frame must not be negative!");
                if(impactnum>=numframes) throw new IllegalArgumentException("\n\nImpact frame must be within the number of frames!");
                impact= String.valueOf((impactnum+1));

                String impactx=impactxField.getText();
                int impx= Integer.parseInt(impactx);
                if(impx<0) throw new IllegalArgumentException("\n\nImpact x coordinate must not be negative!");

                String impacty=impactyField.getText();
                int impy= Integer.parseInt(impacty);
                if(impy<0) throw new IllegalArgumentException("\n\nImpact y coordinate must not be negative!");

                String pxsize=cameraField.getText();
                double px= Double.parseDouble(pxsize);
                if(px<=0) throw new IllegalArgumentException("\n\nCamera pixel size must be positive!");

                DecimalFormat df1=new DecimalFormat("000");
                DecimalFormat df2=new DecimalFormat("0.00");

                FileWriter eventmetadata=new FileWriter(folderPath+"/Event_Metadata.txt");
                eventmetadata.write("Event Info:\n\n");
                eventmetadata.write("The event occured at: "+date+" "+time+".000.\n");
                eventmetadata.write("The recording consists of "+frames+" frames.\n");
                eventmetadata.write("The event can be found at the "+impact+"th frame (filename: frame_"+df1.format(impactnum)+").\n");
                eventmetadata.write("xxx\n");
                eventmetadata.write("The coordinates of the brightest pixel of the frame are:\n");
                eventmetadata.write("x coordinate:"+impactx+", y coordinate:"+impacty+".\n");
                eventmetadata.write("xxx\n\nCamera Info:\n\nCamera Name: xxx\n");
                eventmetadata.write("Pixel size: "+ df2.format(px)+"\n");
                eventmetadata.write("xxx\nxxx\nxxx\n");
                if(yes16bit.isSelected()) {
                    eventmetadata.write("Is 16 bit: true.\n");
                }
                else if(no16bit.isSelected()) {
                    eventmetadata.write("Is 16 bit: false.\n");
                }
                if(yesbin2.isSelected()) {
                    eventmetadata.write("Is bin2: true.\n");
                }
                else if(nobin2.isSelected()) {
                    eventmetadata.write("Is bin2: false.\n");
                }
                eventmetadata.close();
                metawindow.close();
            }
            catch (DateTimeParseException | ParseException ex) {
                ex.printStackTrace();
                Alert alert=new Alert(Alert.AlertType.WARNING,"Invalid date or time.");
                alert.setTitle("Invalid input");
                alert.setHeaderText(null);
                alert.showAndWait();
                return;
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
                Alert alert=new Alert(Alert.AlertType.WARNING,ex+"");
                alert.setTitle("Invalid input");
                alert.setHeaderText(null);
                alert.showAndWait();
                return;
            } catch (IOException ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, e+"");
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.showAndWait();
                return;
            }

        });

        VBox metalayout=new VBox(10);
        metalayout.setPadding(new Insets(10,10,10,10));
        metalayout.setAlignment(Pos.CENTER);
        metalayout.getChildren().addAll(instrLabel,datelayout,timelayout,new Separator(),frameslayout,impactfrlayout,impactxlayout,impactylayout,new Separator(),cameralayout,radio16,radiobin,writeButton);

        ScrollPane sp = new ScrollPane();
        sp.setContent(metalayout);
        Scene scene=new Scene(sp);
        metawindow.setScene(scene);
        metawindow.showAndWait();
    }


    /** Manages detection starting screen.
     * Contains 3 directory choosers:
     * One for the user to pick an directory for the flat image (optional)
     * One for the user to pick an directory for the dark image (optional)
     * One for the user to pick an event directory.

     * Contains text input field for the constant for the flat calibration and one for the ROI size (square).
     * Contains a button to start detection.
     *
     * If a directory with multiple events is chosen, performs detection sequentially on all events that are in the directory.
     * If a directory with a single event is chosen, detection is performed only in this folder.
     *
     * @param event
     */

    @FXML
    void detectionButton(ActionEvent event) {
        folderPath = null;
        try {
            Stage primaryStage = new Stage();
            primaryStage.setTitle("Detection");

            Button configurationButton = new Button("Edit Parameters");
            configurationButton.setOnAction(e -> {
                Stage ConfigurationStage = new Stage();
                ConfigurationStage.setTitle("Edit Parameters");

                ConfigurationStage.initModality(Modality.APPLICATION_MODAL);

                ConfigurationStage.setMinWidth(450);
                ConfigurationStage.setHeight(700);
                //ConfigurationStage.setMaxHeight(Double.POSITIVE_INFINITY);

               // ConfigurationStage.setHeight(600);

                Label labelDescription = new Label();
                labelDescription.setText("No event detected: \n" +
                        "Case 1: sigma > param1 or sigma ratio > param2\n" +
                "Case 2: sigma ratio > param3 and sigma < param4\n" +
                        "Case 3: sigma ratio > param5 and sigma > param6 and mean ratio < param7\n" + "\n" +
                        "Satellite detected: \n" +
                        "sigma ratio > param8 and sigma > param9\n" + "\n"
                + "Cosmic Ray or Impact Flash Detected: \n"+
                        "param10 < sigma ratio < param11\n" +  "\n" +
                        "Impact Flash Detected: \n" + "sigma ratio < param12 and sigma > param13\n" + "\n" +
                        "Cosmic Ray Detected: \n" +
                        "sigma ratio > param14 and sigma < param15.");




                Label labelP1 = new Label();
                labelP1.setText("param1: ");

                TextField P1Input=new TextField();
                P1Input.setMaxWidth(32);
                P1Input.setText(String.valueOf(param1));

                Label labelP2 = new Label();
                labelP2.setText("param2: ");

                TextField P2Input=new TextField();
                P2Input.setMaxWidth(32);
                P2Input.setText(String.valueOf(param2));

                Label labelP3 = new Label();
                labelP3.setText("param3: ");

                TextField P3Input=new TextField();
                P3Input.setMaxWidth(32);
                P3Input.setText(String.valueOf(param3));

                Label labelP4 = new Label();
                labelP4.setText("param4: ");

                TextField P4Input=new TextField();
                P4Input.setMaxWidth(32);
                P4Input.setText(String.valueOf(param4));

                Label labelP5 = new Label();
                labelP5.setText("param5: ");

                TextField P5Input=new TextField();
                P5Input.setMaxWidth(32);
                P5Input.setText(String.valueOf(param5));

                Label labelP6 = new Label();
                labelP6.setText("param6: ");

                TextField P6Input=new TextField();
                P6Input.setMaxWidth(32);
                P6Input.setText(String.valueOf(param6));

                Label labelP7 = new Label();
                labelP7.setText("param7: ");

                TextField P7Input=new TextField();
                P7Input.setMaxWidth(32);
                P7Input.setText(String.valueOf(param7));

                Label labelP8 = new Label();
                labelP8.setText("param8: ");

                TextField P8Input=new TextField();
                P8Input.setMaxWidth(32);
                P8Input.setText(String.valueOf(param8));

                Label labelP9 = new Label();
                labelP9.setText("param9: ");

                TextField P9Input=new TextField();
                P9Input.setMaxWidth(32);
                P9Input.setText(String.valueOf(param9));

                Label labelP10 = new Label();
                labelP10.setText("param10: ");

                TextField P10Input=new TextField();
                P10Input.setMaxWidth(32);
                P10Input.setText(String.valueOf(param10));

                Label labelP11 = new Label();
                labelP11.setText("param11: ");

                TextField P11Input=new TextField();
                P11Input.setMaxWidth(32);
                P11Input.setText(String.valueOf(param11));

                Label labelP12 = new Label();
                labelP12.setText("param12: ");

                TextField P12Input=new TextField();
                P12Input.setMaxWidth(32);
                P12Input.setText(String.valueOf(param12));

                Label labelP13 = new Label();
                labelP13.setText("param13: ");

                TextField P13Input=new TextField();
                P13Input.setMaxWidth(32);
                P13Input.setText(String.valueOf(param13));

                Label labelP14 = new Label();
                labelP14.setText("param14: ");

                TextField P14Input=new TextField();
                P14Input.setMaxWidth(32);
                P14Input.setText(String.valueOf(param14));

                Label labelP15 = new Label();
                labelP15.setText("param15: ");

                TextField P15Input=new TextField();
                P15Input.setMaxWidth(32);
                P15Input.setText(String.valueOf(param15));

  /*              javafx.scene.control.Label labelNoEvent = new Label();
                labelNoEvent.setText("No event detected");

                javafx.scene.control.Label labelNoEvent1 = new Label();
                labelNoEvent1.setText("sigma > ");

                TextField NoEventInput=new TextField();
                NoEventInput.setMaxWidth(32);
                NoEventInput.setText(String.valueOf(noEventParam1));

                javafx.scene.control.Label labelNoEvent2 = new Label();
                labelNoEvent2.setText(" or sigma ratio > ");

                TextField NoEventInput1=new TextField();
                NoEventInput1.setMaxWidth(32);
                NoEventInput1.setText(String.valueOf(noEventParam2));

                javafx.scene.control.Label labelNoEvent2 = new Label();
                labelNoEvent2.setText(" or sigma ratio > ");

                javafx.scene.control.Label labelSatellite = new Label();
                labelSatellite.setText("Satellite detected");

                TextField SatelliteInput=new TextField();
                SatelliteInput.setMaxWidth(32);
                SatelliteInput.setText(String.valueOf(SatelliteParam1));

                javafx.scene.control.Label labelNEO = new Label();
                labelNEO.setText("Cosmic Ray or Impact Flash detected");

                TextField NEOInput=new TextField();
                NEOInput.setMaxWidth(32);
                NEOInput.setText(String.valueOf(NEOParam1));

                javafx.scene.control.Label labelImFl = new Label();
                labelImFl.setText("Impact flash detected");

                TextField IFInput=new TextField();
                IFInput.setMaxWidth(32);
                IFInput.setText(String.valueOf(IFParam1));

                javafx.scene.control.Label labelcosmicRay = new Label();
                labelcosmicRay.setText("Cosmic detected");

                TextField CRInput=new TextField();
                CRInput.setMaxWidth(32);
                CRInput.setText(String.valueOf(CRParam1));
*/

                VBox layout = new VBox(10);
                layout.getChildren().addAll(labelDescription, labelP1,  P1Input, labelP2,  P2Input, labelP3,  P3Input, labelP4,  P5Input, labelP6,  P6Input, labelP7,  P7Input, labelP8,  P8Input, labelP9,  P9Input, labelP10,  P10Input, labelP11,  P11Input, labelP12,  P12Input, labelP13,  P13Input, labelP14,  P14Input, labelP15,  P15Input);
                layout.setAlignment(Pos.CENTER);



                GridPane pane = new GridPane();
                pane.setAlignment(Pos.CENTER);
                pane.setHgap(5);
                pane.setVgap(5);
                pane.getColumnConstraints().add(new ColumnConstraints(100));

                pane.setPadding(new Insets(5,5,5,5)); // set top, right, bottom, left

//                RowConstraints row0 = new RowConstraints(100,100,Double.MAX_VALUE);
//                row0.setVgrow(Priority.ALWAYS);
//                RowConstraints row1 = new RowConstraints(100);
//                pane.getRowConstraints().addAll(row0, row1); // first row gets any extra width
//
                Pane canvas = new Pane();
                canvas.getChildren().addAll(labelDescription);

                ColumnConstraints column1 = new ColumnConstraints(100,100,Double.MAX_VALUE);
                column1.setHgrow(Priority.ALWAYS);
                ColumnConstraints column2 = new ColumnConstraints(100);
                pane.getColumnConstraints().addAll(column1, column2); // first column gets any extra width

                pane.add(canvas, 0, 0);
                pane.add(labelP1, 0, 5);
                pane.add(P1Input, 1, 5);
                pane.add(labelP2, 2, 5);
                pane.add(P2Input, 3, 5);
                pane.add(labelP3, 0, 6);
                pane.add(P3Input, 1, 6);
                pane.add(labelP4, 2, 6);
                pane.add(P4Input, 3, 6);
                pane.add(labelP5, 0, 7);
                pane.add(P5Input, 1, 7);
                pane.add(labelP6, 2, 7);
                pane.add(P6Input, 3, 7);
                pane.add(labelP7, 0, 8);
                pane.add(P7Input, 1, 8);
                pane.add(labelP8, 2, 8);
                pane.add(P8Input, 3, 8);
                pane.add(labelP9, 0, 9);
                pane.add(P9Input, 1, 9);
                pane.add(labelP10, 2, 9);
                pane.add(P10Input, 3, 9);
                pane.add(labelP11, 0, 10);
                pane.add(P11Input, 1, 10);
                pane.add(labelP12, 2, 10);
                pane.add(P12Input, 3, 10);
                pane.add(labelP13, 0, 11);
                pane.add(P13Input, 1, 11);
                pane.add(labelP14, 2, 11);
                pane.add(P14Input, 3, 11);
                pane.add(labelP15, 0, 12);
                pane.add(P15Input, 1, 12);

                BorderPane borderPane = new BorderPane();
                borderPane.setCenter(pane);

                Scene scene = new Scene(borderPane, 500, 500);


//                ScrollPane scrollPane = new ScrollPane(layout);
//                scrollPane.setFitToHeight(true);
//
//                BorderPane root = new BorderPane(scrollPane);
//                root.setPadding(new Insets(15));
//                root.setTop(labelDescription);

               // Scene scene = new Scene(layout, 400, 300);

                Button buttonSave = new Button("Save Values");
                buttonSave.setOnAction(e1 -> {
                    param1 = Double.parseDouble(P1Input.getText());
                    param2 = Double.parseDouble(P2Input.getText());
                    param3 = Double.parseDouble(P3Input.getText());
                    param4 = Double.parseDouble(P4Input.getText());
                    param5 = Double.parseDouble(P5Input.getText());
                    param6 = Double.parseDouble(P6Input.getText());
                    param7 = Double.parseDouble(P7Input.getText());
                    param8 = Double.parseDouble(P8Input.getText());
                    param9 = Double.parseDouble(P9Input.getText());
                    param10 = Double.parseDouble(P10Input.getText());
                    param11 = Double.parseDouble(P11Input.getText());
                    param12 = Double.parseDouble(P12Input.getText());
                    param13 = Double.parseDouble(P13Input.getText());
                    param14 = Double.parseDouble(P14Input.getText());
                    param15 = Double.parseDouble(P15Input.getText());


                        });
                pane.add(buttonSave, 0, 13);
                    ConfigurationStage.setScene(scene);
                ConfigurationStage.setResizable(false);
                ConfigurationStage.showAndWait();


            });

            DirectoryChooser directoryChooser = new DirectoryChooser();
            Label label1 = new Label();
            label1.setText("No directory chosen");


            Button buttonDark = new Button("Select Dark Image for Calibration (Optional)");
            Button buttonFlat = new Button("Select Flat Image for Calibration (Optional)");

            FileChooser darkChooser = new FileChooser();

            Label label2 = new Label();
            label2.setText("No directory chosen");

            Label label3 = new Label();
            label3.setText("No directory chosen");

            buttonDark.setOnAction(e -> {
                File selectedDirectoryDARK = darkChooser.showOpenDialog(null);
                darkPath = selectedDirectoryDARK.getAbsolutePath();
                System.out.println(selectedDirectoryDARK.getAbsolutePath());
                label2.setText(darkPath);
            });

            FileChooser flatChooser = new FileChooser();

            buttonFlat.setOnAction(e -> {
                File selectedDirectoryFLAT = flatChooser.showOpenDialog(null);
                flatPath = selectedDirectoryFLAT.getAbsolutePath();
                System.out.println(selectedDirectoryFLAT.getAbsolutePath());
                label3.setText(flatPath);
            });

            TextField roiInput=new TextField();
            roiInput.setMaxWidth(32);
            roiInput.setText(String.valueOf(ROIsize));

            TextField calibrationInput=new TextField();
            calibrationInput.setMaxWidth(40);
            calibrationInput.setText(String.valueOf(calibrationConstant));

            Button button = new Button("Select Directory");
            button.setOnAction(e -> {
                File selectedDirectory = directoryChooser.showDialog(primaryStage);

                if(selectedDirectory!=null) {
                    folderPath = selectedDirectory.getAbsolutePath();
                }
                System.out.println("The path of the chosen folder is " + folderPath);
                label1.setText(folderPath);

            });

            Button button1 = new Button("Start Detection");
            button1.setOnAction(e -> {
                ROIsize = Integer.parseInt(roiInput.getText());

                File folder = null;
                try {
                    folder = new File(folderPath);
                } catch (Exception exception) {
                    exception.printStackTrace();
                    Alert alert2 = new Alert(Alert.AlertType.ERROR, exception + "");
                    alert2.setTitle("Error");
                    alert2.setHeaderText(null);
                    alert2.setContentText("Please enter valid folder path for detection.");
                    alert2.showAndWait();
                }

                if(new File(folderPath+"/frame_000.png").exists() || new File(folderPath+"/frame_000.jpg").exists() || new File(folderPath+"/frame_000.fits").exists()) {
                    try {
                        Test(folderPath);
                    } catch (IllegalArgumentException exception) {
                        exception.printStackTrace();
                        Alert alert2 = new Alert(Alert.AlertType.ERROR, exception + "");
                        alert2.setTitle("Error");
                        alert2.setHeaderText(null);
                        alert2.setContentText("The coordinates of the event are such that the event cannot be analysed. Please remove this event from the folder and try again.(Event Path: " + Detection.path + ").");
                        alert2.setResizable(true);
                        alert2.showAndWait();

                    } catch (Exception exception) {
                        exception.printStackTrace();
                        Alert alert2 = new Alert(Alert.AlertType.ERROR, exception + "");
                        alert2.setTitle("Error");
                        alert2.setHeaderText(null);
                        alert2.setContentText("Please enter valid folder path for detection.");
                        alert2.showAndWait();
                    }
                }
                else {
                    subfolders = new ArrayList<>();

                    for(final File fileEntry:folder.listFiles()) {
                        if(fileEntry.isDirectory()) {
                            String pathTemp=folderPath+"/"+fileEntry.getName();

                            if(new File(folderPath+"/"+fileEntry.getName()+"/frame_000.png").exists() || new File(folderPath+"/"+fileEntry.getName()+"/frame_000.fits").exists())
                                subfolders.add(fileEntry.getName());
                        }
                    }
                    events=subfolders.size();
                    ROIsize = Integer.parseInt(roiInput.getText());
                    calibrationConstant = Integer.parseInt(calibrationInput.getText());
                    if(ROIsize<=0 || ROIsize>500) throw new NumberFormatException();
                    try {
                        finalevents = events;
                        events = events -1;
                        System.out.println(ROIsize);
                        Test(folderPath+"/"+subfolders.get(events));
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        Alert alert2 = new Alert(Alert.AlertType.ERROR, exception + "");
                        alert2.setTitle("Error");
                        alert2.setHeaderText(null);
                        alert2.setContentText("Make sure you enter a valid path for detection.");
                        alert2.showAndWait();
                    }
                }

                primaryStage.close();

            });
            Label ROIlabel = new Label("Specify ROI Dimension (Optional)");

            Label calibrationLabel = new Label("Specify the constant for the flat and dark calibration (Optional)");

            primaryStage.initModality(Modality.APPLICATION_MODAL);
            primaryStage.setTitle("Detection");
            primaryStage.setMinWidth(250);

            Label label = new Label();
            label.setText("Please choose a folder first and then start the process.");

            VBox layout = new VBox(10);
            layout.getChildren().addAll(configurationButton, buttonDark,label2, buttonFlat, label3,calibrationLabel,calibrationInput, label,button,label1, ROIlabel, roiInput, button1);
            layout.setAlignment(Pos.CENTER);
            //focalLabel, focalInput

            Scene scene = new Scene(layout, 400, 500);

            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.showAndWait();
        }
        catch(Throwable e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, e+"\n"+ Arrays.toString(e.getStackTrace()));
            alert.setTitle("Error");
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.setHeaderText(null);
            alert.setResizable(true);
            alert.showAndWait();
            return;
        }
    }

    /** Calls the controller for the details window
     *
     * @param event
     */

    @FXML
    void showDetails(ActionEvent event) {
        DetailsController.display("Details", "Flash Detection Software: an open source tool for lunar impact flash detections.");
    }

}
