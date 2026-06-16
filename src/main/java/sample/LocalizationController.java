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

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.*;

/** UI controller for localization on a directory.
 *  1) Screen for circle fitting
 *  2) Screen for hemisphere pick
 *  3) Screen for correlation
 *  4) Transition screens
 *  Also writes images to results directory and info to logger file
 *
 * @author Ivi Chatzi Christofidi Georgia Achlatis Stefanos
 */
public class LocalizationController {
    public Stage window;
    public Local local=null;
    public String path=null;
    public FileWriter metadataFile;
    private BorderPane circleLayout;
    private BorderPane correlationLayout;
    private BorderPane hemiLayout;

    private boolean flipped;

    private double[] initcor;
    private double[] prevCor;
    private double[] curCor;

    private double[] initCircle;
    private double[] prevCircle;
    private double[] curCircle;

    private int imgsize=600;

    private ArrayList<String> correlationHistory=new ArrayList<>();
    private ArrayList<String> autoCircleHistory=new ArrayList<>();

    private BufferedImage markedRot;
    private BufferedImage markedMap;
    private BufferedImage markedCor;
    ImageView centerImage;
    private boolean markOn=false;
    private int markx,marky;

    /** Prepares localization window for a given directory and creates localization_results directory with
     * localization_logger.txt inside event directory
     *
     * @param pathTemp path of event directory containing frames and metadata file
     * @throws IOException
     */
    public LocalizationController(String pathTemp) throws IOException {
        window=new Stage();
        //Image icon = new Image(getClass().getResourceAsStream( "icon.png" ));
        //window.getIcons().add(icon);
        window.initModality(Modality.APPLICATION_MODAL);

        path=pathTemp;
        new File(path+"/localization_results").mkdirs();
        File resultsDir=new File(path+"/localization_results");
        System.out.println("Created metadata file for "+pathTemp);
        metadataFile = new FileWriter(new File(resultsDir,"localization_logger.txt"));
    }

    /** Initialises localization instance for input directory.
     * Performs localization steps up until circle fitting.
     * Displays transition screen while localization is running.
     * Writes images to results directory:
     *      -Impact frame
     *      -Average image
     *
     * Catches localization exceptions and shows alert error screens:
     *      -Connection timed out with JPL Horizons API
     *      -Wrong Event_Metadata.txt format
     *
     * For other errors, writes error message and stack trace into an errorLog.txt file inside localization_results directory
     * Once the user closes the error screens, localization stops
     *
     *
     * @param coords array containing geographical coordinates of observation location:
     *               -longitude (degrees)
     *               -latitude (degrees)
     *               -elevation (km)
     * @param focal focal length of telescope (can be null)
     * @throws IOException
     */
    public void init(double[] coords, double focal) throws IOException {
        window.hide();
        Stage transitionStage=new Stage();
        display(transitionStage,"FDS: Localization in progress, please wait a moment...","");
//        window.setMaximized(true);
        String startTimeStamp = String.valueOf(new Timestamp((new Date()).getTime()));

        try {
            local=new Local(coords,focal,path);
            metadataFile.write("Localization Process Started for folder " + path + " at " + startTimeStamp + '\n');
            if(local.is16bit) {
                local.writeFile(local.impactImage, "0-impactframe", "short");
                local.writeFile(local.stackedImage,"1-stacked","short");
            }
            else {
                local.writeFile(local.impactImage, "0-impactframe", "byte");
                local.writeFile(local.stackedImage,"1-stacked","byte");
            }
        }
        catch(UnknownHostException e) {
            transitionStage.close();
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "There was a problem connectiong to the JPL Horizons API\nCheck your internet connection\n\n"+e);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.showAndWait();
            window.fireEvent(new WindowEvent(
                    window,
                    WindowEvent.WINDOW_CLOSE_REQUEST
            ));
            return;
        }
        catch (ConnectException e) {
            transitionStage.close();
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Connection timed out with JPL Horizons API\n\n"+e);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.showAndWait();
            window.fireEvent(new WindowEvent(
                    window,
                    WindowEvent.WINDOW_CLOSE_REQUEST
            ));
            return;
        }
        catch(ArrayIndexOutOfBoundsException | NumberFormatException e) {
            transitionStage.close();
            e.printStackTrace();
            FileWriter errorLog=new FileWriter(new File(path+"/localization_results","errorLog.txt"));
            String timeStamp = String.valueOf(new Timestamp((new Date()).getTime()));
            errorLog.write(timeStamp+"\n\n");
            errorLog.write(e+"\n"+ Arrays.toString(e.getStackTrace()));
            errorLog.close();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid Event_Metadata.txt\n\n"+e+"\n\nCheck error log for more details");
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.showAndWait();
            window.fireEvent(new WindowEvent(
                    window,
                    WindowEvent.WINDOW_CLOSE_REQUEST
            ));
            return;
        }
        catch (Exception ex) {
            transitionStage.close();
            ex.printStackTrace();
            FileWriter errorLog=new FileWriter(new File(path+"/localization_results","errorLog.txt"));
            String timeStamp = String.valueOf(new Timestamp((new Date()).getTime()));
            errorLog.write(timeStamp+"\n\n");
            errorLog.write(ex+"\n"+ Arrays.toString(ex.getStackTrace()));
            errorLog.close();
            Alert alert = new Alert(Alert.AlertType.ERROR, ""+ex+"\n\nCheck error log for more details");
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.showAndWait();
            window.fireEvent(new WindowEvent(
                    window,
                    WindowEvent.WINDOW_CLOSE_REQUEST
            ));
            return;
        }

        try {
            local.circleFitting(local.sd2,local.ransacSamples,local.boostPart);
            if(autoCircleHistory.size()==5) {
                autoCircleHistory.remove(0);
            }
            DecimalFormat df=new DecimalFormat("0.00");
            autoCircleHistory.add("AUTOMATIC: radius="+df.format(local.circle[2])+" (sd="+local.sd2+", boost="+local.boostPart+"%)");
            initCircle=local.circle;
            prevCircle=local.circle;
            curCircle=local.circle;
        } catch (Exception e) {
            transitionStage.close();
            e.printStackTrace();
            FileWriter errorLog=new FileWriter(new File(path+"/localization_results","errorLog.txt"));
            String timeStamp = String.valueOf(new Timestamp((new Date()).getTime()));
            errorLog.write(timeStamp+"\n\n");
            errorLog.write(e+"\n"+ Arrays.toString(e.getStackTrace()));
            errorLog.close();
            Alert alert = new Alert(Alert.AlertType.ERROR, ""+e+"\n\nCheck error log for more details");
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.showAndWait();
            window.fireEvent(new WindowEvent(
                    window,
                    WindowEvent.WINDOW_CLOSE_REQUEST
            ));
            return;
        }
        circleFitScreen();
        transitionStage.close();
    }

    /** Handles screen for circle fitting onto the lunar limb.
     * Allows user to view circle fit image, image without circle, limb image and impact frame
     * Allows changing of color of circle
     * Allows user to zoom in and out
     * Allows user to tweak gaussian standard deviation and image brightness boost % and retry circle fitting
     * Shows physical and image pixel coordinates of points clicked on the images and the impact flash
     * Allows user to manually circle fit by clicking points on the images
     * Has a button to reset to original parameters
     * Shows information about the circle found, correct radius and pixel scale of frames if known
     * Allows user to undo, redo and see move history
     * Has a button to continue localization to hemisphere selection
     *
     * Writes images to results directory: light-removed image, sobel image, limb image, colored circle image
     *
     * Catches localization exceptions, writes error message and stack trace into an errorLog.txt file inside localization_results directory
     * Once the user closes the error screens, localization instance stops.
     */
    private void circleFitScreen() {
        window.setTitle("FDS: Localization");
//        window.setMaximized(true);
        circleLayout=new BorderPane();
        circleLayout.setPadding(new Insets(5,5,5,5));

        Label coordLabel=new Label("Click on the image to get the pixel\ncoordinates of the point:");
        Label imagePixelLabel=new Label("Image (pixels): ");
        Label physicalPixelLabel=new Label("Physical (pixels): ");

        Label infoLabel=new Label("The following circle was found:");

        VBox confirmLayout=new VBox(10);
        Button yesButton=new Button("Continue");
        yesButton.setOnAction(e -> {
            DecimalFormat df=new DecimalFormat("0.00");
            try {
                if(Double.isNaN(local.circle[0]) || Double.isNaN(local.circle[1]) || Double.isNaN(local.circle[2])) {
                    Alert alert=new Alert(Alert.AlertType.WARNING,"No radius found");
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.showAndWait();
                    return;
                }
                else if(local.circle[2]<3) {
                    Alert alert=new Alert(Alert.AlertType.WARNING,"Radius is too small: R="+df.format(local.circle[2])+" pixels");
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.showAndWait();
                    return;
                }
                else if(local.circle[2]>4000) {
                    Alert alert=new Alert(Alert.AlertType.CONFIRMATION, "Radius is too big: R="+df.format(local.circle[2])+" pixels. Continue anyway?",ButtonType.YES, ButtonType.NO);
                    alert.setTitle("FDS: Localization");
                    alert.setHeaderText(null);
                    alert.showAndWait();
                    if(alert.getResult() == ButtonType.NO) {
                        return;
                    }
                    else if(alert.getResult()==ButtonType.YES) {
                        local.writeFile(local.removedLight,"2-removedLight","byte");
                        local.writeFile(local.limbImage,"4-limb","byte");
                        local.writeFile(local.sobelImage,"3-sobel","byte");
                        local.writeFile(local.circleImage,"5-circle","int");

                        hemisphereScreen();
                    }
                }
                else {
                    local.writeFile(local.removedLight,"2-removedLight","byte");
                    local.writeFile(local.limbImage,"4-limb","byte");
                    local.writeFile(local.sobelImage,"3-sobel","byte");
                    local.writeFile(local.circleImage,"5-circle","int");

                    hemisphereScreen();
                }
            } catch (Exception | Error ex) {
                ex.printStackTrace();
                try {
                    FileWriter errorLog = new FileWriter(new File(path+"/localization_results","errorLog.txt"));
                    String timeStamp = String.valueOf(new Timestamp((new Date()).getTime()));
                    errorLog.write(timeStamp+"\n\n");
                    errorLog.write(ex+"\n"+ Arrays.toString(ex.getStackTrace()));
                    errorLog.close();
                } catch (IOException exc) {
                    exc.printStackTrace();

                }
                Alert alert = new Alert(Alert.AlertType.ERROR, ex+""+"\n\nCheck error log for more details");
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.showAndWait();
                window.fireEvent(new WindowEvent(
                        window,
                        WindowEvent.WINDOW_CLOSE_REQUEST
                ));
                return;
            }
        });
        confirmLayout.getChildren().addAll(yesButton);
        confirmLayout.setAlignment(Pos.CENTER);

        VBox infoVBox=new VBox(10);

        Button circleButton=new Button("View circle");
        circleButton.setOnAction(e -> {
            infoLabel.setText("The following circle was found:");
            centerImage=getImage(local.circleImage,imgsize,imagePixelLabel,physicalPixelLabel);
            circleLayout.setCenter(centerImage);
        });
        Button limbButton=new Button("View limb");
        limbButton.setOnAction(e -> {
            infoLabel.setText("Pixels in white were detected to be on the limb. Increase the sd parameter if pixels outside the limb were detected.");
            centerImage=getImage(local.limbImage,imgsize,imagePixelLabel,physicalPixelLabel);
            circleLayout.setCenter(centerImage);
        });
        Button impactButton=new Button("View impact frame");
        impactButton.setOnAction(e -> {
            infoLabel.setText("Frame containing the impact flash:");
            centerImage=getImage(local.impactImage,imgsize,imagePixelLabel,physicalPixelLabel);
            circleLayout.setCenter(centerImage);
        });
        Button noCircleButton=new Button("Hide circle");
        noCircleButton.setOnAction(e -> {
            infoLabel.setText("Image without circle:");
            centerImage=getImage(local.removedLight,imgsize,imagePixelLabel,physicalPixelLabel);
            circleLayout.setCenter(centerImage);
        });
        HBox imageButtons=new HBox(10);
        imageButtons.getChildren().addAll(circleButton,noCircleButton,limbButton,impactButton);
        imageButtons.setAlignment(Pos.CENTER);

        VBox topVBox=new VBox(10);
        topVBox.getChildren().addAll(imageButtons, infoLabel);
        topVBox.setAlignment(Pos.CENTER);

        DecimalFormat df=new DecimalFormat("0.00");

        Label colorLabel=new Label("Change circle color");
        java.awt.Color initcolor=local.circleColor;
        javafx.scene.paint.Color initcol=javafx.scene.paint.Color.rgb(initcolor.getRed(),initcolor.getGreen(),initcolor.getBlue());
        ColorPicker colorPicker=new ColorPicker(initcol);
        colorPicker.setOnAction(e -> {
            javafx.scene.paint.Color curColor=colorPicker.getValue();
            java.awt.Color newColor=new java.awt.Color((float) curColor.getRed(),(float) curColor.getGreen(),(float) curColor.getBlue());
            local.circleColor=newColor;
            local.circleImage=CircleFit.edgeRed(local.circle,local.removedLight,newColor);
            infoLabel.setText("The following circle was found:");
            centerImage=getImage(local.circleImage,imgsize,imagePixelLabel,physicalPixelLabel);
            circleLayout.setCenter(centerImage);
        });

        Label circleLabel=new Label("Center found (pixels):\nX="+df.format(local.circle[0])+", Y="+df.format(local.circle[1])+"\nRadius found (pixels): "+df.format(local.circle[2]));
        Label arcsecPixelLabel=new Label("Pixel scale (arcsec/pixels): -");
        Label correctLabel=new Label("Suggested radius (pixels)\nbased on pixel scale: -");
        if(local.arcsecPixelRatio>0) {
            arcsecPixelLabel.setText("Pixel scale (arcsec/pixels): "+df.format(local.arcsecPixelRatio));
            correctLabel.setText("Suggested radius (pixels)\nbased on pixel scale: "+df.format(local.correctRadius));
        }

        Label impactLabel=new Label("Impact location\nPhysical (pixels): X="+local.impactX+", Y="+(local.removedLight.getHeight()-1-local.impactY));

        ArrayList<int[]> points=new ArrayList<>();
        Label manualfitLabel=new Label("MANUAL CIRCLE FITTING");
        Label manualLabel=new Label("Click on the image and then add point\nto manually select limb pixels.\nClick manual fit when ready.");
        Button addButton=new Button("Add point");
        addButton.setOnAction(e -> {
            String xs=physicalPixelLabel.getText().split("\\s+")[2];
            String ys=physicalPixelLabel.getText().split("\\s+")[3];
            int x= (int) Math.round(Double.parseDouble(xs.substring(2,xs.length()-1)));
            int y= (int) Math.round(Double.parseDouble(ys.substring(2)));
            y=local.circleImage.getHeight()-y-1;
            points.add(new int[]{x,y});
        });
        Button resetButton=new Button("Clear all points");
        resetButton.setOnAction(e -> {
            points.clear();
        });
        Button clearPreviousButton=new Button("Undo previous point");
        clearPreviousButton.setOnAction(e -> {
            if(points.size()>0) {
                int[] last=points.get(points.size()-1);
                points.remove(points.size()-1);
            }
        });

        HBox addundoLayout=new HBox(5);
        addundoLayout.setAlignment(Pos.CENTER);
        addundoLayout.getChildren().addAll(addButton,clearPreviousButton);

        Button manualButton=new Button("Manual fit");
        manualButton.setOnAction(e -> {
            try {
                prevCircle=local.circle;
                local.manualCircle(points);
                curCircle=local.circle;
                if(autoCircleHistory.size()==5) {
                    autoCircleHistory.remove(0);
                }
                autoCircleHistory.add("MANUAL: radius="+df.format(local.circle[2]));
                circleFitScreen();
            }
            catch (Exception ex) {
                ex.printStackTrace();
                try {
                    FileWriter errorLog = new FileWriter(new File(path+"/localization_results","errorLog.txt"));
                    String timeStamp = String.valueOf(new Timestamp((new Date()).getTime()));
                    errorLog.write(timeStamp+"\n\n");
                    errorLog.write(ex+"\n"+ Arrays.toString(ex.getStackTrace()));
                    errorLog.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
                Alert alert = new Alert(Alert.AlertType.ERROR, ex+"\n\nCheck error log for more details");
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.showAndWait();
                window.fireEvent(new WindowEvent(
                        window,
                        WindowEvent.WINDOW_CLOSE_REQUEST
                ));
                return;
            }
        });
        Button viewPointsButton=new Button("See last 5 points");
        viewPointsButton.setOnAction(e -> {
            Alert alert=new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setTitle("Selected points");
            StringBuilder content= new StringBuilder("Last 5 points (physical pixel coordinates):\n\n");
            if(points.size()>0) {
                for(int i=points.size()-1; i>=Math.max(0,points.size()-5); i--) {
                    content.append("X=").append(points.get(i)[0]).append(", Y=").append(local.removedLight.getHeight()-1-points.get(i)[1]).append("\n");
                }
            }

            alert.setContentText(content.toString());
            alert.showAndWait();
        });

        VBox manualVBox=new VBox(10);
        manualVBox.getChildren().addAll(manualfitLabel,manualLabel,viewPointsButton,addundoLayout,resetButton,manualButton);
        manualVBox.setAlignment(Pos.CENTER);

        Slider slider3=new Slider();
        slider3.setMin(0.1);
        slider3.setMax(1);
        slider3.setValue(0.1);
        slider3.setBlockIncrement(0.05);
        Label zoomLabel=new Label("100%");

        Button zoomButton=new Button("Change focus (zoom)");
        zoomButton.setOnAction(e -> {
            float percent= (float) slider3.getValue();
            percent=1-percent+0.1f;
            zoomLabel.setText(Math.round(percent*100)+"%");
            int xx=(int) local.impactX;
            int yy=(int) local.impactY;

            if(physicalPixelLabel.getText().split("\\s+").length>2) {

                String xs=physicalPixelLabel.getText().split("\\s+")[2];
                String ys=physicalPixelLabel.getText().split("\\s+")[3];
                xx= (int) Math.round(Double.parseDouble(xs.substring(2,xs.length()-1)));
                yy= (int) Math.round(Double.parseDouble(ys.substring(2)));
                yy=local.circleImage.getHeight()-yy-1;
            }
            BufferedImage displayImage=local.circleImage;
            if(infoLabel.getText().charAt(0)=='T') displayImage=local.circleImage;
            else if(infoLabel.getText().charAt(0)=='P') displayImage=local.limbImage;
            else if(infoLabel.getText().charAt(0)=='F') displayImage=local.impactImage;
            else if(infoLabel.getText().charAt(0)=='I') displayImage=local.removedLight;
            centerImage=zoomImage(displayImage,xx,yy,imgsize,percent,imagePixelLabel,physicalPixelLabel);
            circleLayout.setCenter(centerImage);
        });
        HBox zoomBox=new HBox(5);
        zoomBox.getChildren().addAll(slider3,zoomLabel);
        zoomBox.setAlignment(Pos.CENTER_LEFT);

        infoVBox.getChildren().addAll(colorLabel,colorPicker,new Separator(),circleLabel,arcsecPixelLabel,correctLabel,new Separator(),coordLabel,imagePixelLabel,physicalPixelLabel,new Separator(),impactLabel,new Separator(),zoomBox,zoomButton);
        infoVBox.setAlignment(Pos.CENTER_LEFT);
        infoVBox.setPrefWidth(250);

        VBox parametersVBox=new VBox(10);

        Label automaticLabel=new Label("AUTOMATIC CIRCLE FITTING");
        Label autolabel=new Label("Change the parameters below and click Retry.");
        Button infosdButton=new Button("Info");

        infosdButton.setStyle("-fx-text-fill: #0000ff;");
        infosdButton.setOnAction(e -> {
            Alert alert=new Alert(Alert.AlertType.INFORMATION,"Increase sd if limb pixels were detected inside the moon.\n\nThis usually happens if the image has too much noise, resulting in a too small circle.\n\nTo see detected limb pixels click View limb.");
            alert.setTitle("Info about sd parameter");
            alert.setHeaderText(null);
            alert.showAndWait();
        });
        Label sdInfo=new Label("Change sd of gaussian filter:");

        HBox sdHBox=new HBox(10);
        Label sdLabel=new Label(Integer.toString(local.sd2));
        Button decrSdButton=new Button("-");
        decrSdButton.setOnAction(e -> {
            if(local.sd2>=4) {
                local.sd2-=1;
            }
            sdLabel.setText(Integer.toString(local.sd2));
        });
        Button incrSdButton=new Button("+");
        incrSdButton.setOnAction(e -> {
            if(local.sd2<=8) {
                local.sd2+=1;
            }
            sdLabel.setText(Integer.toString(local.sd2));
        });
        sdHBox.getChildren().addAll(decrSdButton,sdLabel,incrSdButton, infosdButton);
        sdHBox.setAlignment(Pos.CENTER);

        Label boostInfo=new Label("Boost top and bottom % of image:");

        Button infoboostButton=new Button("Info");
        infoboostButton.setStyle("-fx-text-fill: #0000ff;");
        infoboostButton.setOnAction(e -> {
            Alert alert=new Alert(Alert.AlertType.INFORMATION,"\nIncrease to detect more limb pixels near the top and bottom part of the image, if the circle is wrong there.\n\nTo see detected limb pixels click View limb.");
            alert.setTitle("Info about boost parameter");
            alert.setHeaderText(null);
            alert.showAndWait();
        });
        HBox boostHBox=new HBox(10);
        Label boostLabel=new Label(local.boostPart +"%");
        Button decrBoostButton=new Button("-");
        decrBoostButton.setOnAction(e -> {
            if(local.boostPart>=1) {
                local.boostPart-=1;
            }
            boostLabel.setText(local.boostPart +"%");
        });
        Button incrBoostButton=new Button("+");
        incrBoostButton.setOnAction(e -> {
            if(local.boostPart<=24) {
                local.boostPart+=1;
            }
            boostLabel.setText(local.boostPart +"%");
        });
        boostHBox.getChildren().addAll(decrBoostButton,boostLabel,incrBoostButton, infoboostButton);
        boostHBox.setAlignment(Pos.CENTER);

        Button retryButton=new Button("Retry");
        retryButton.setOnAction(e -> {
            try {
                prevCircle=local.circle;
                local.circleFitting(local.sd2,local.ransacSamples,local.boostPart);
                curCircle=local.circle;
                if(autoCircleHistory.size()==5) {
                    autoCircleHistory.remove(0);
                }
                autoCircleHistory.add("AUTOMATIC: radius="+df.format(local.circle[2])+" (sd="+local.sd2+", boost="+local.boostPart+"%)");
                circleFitScreen();
            } catch (Exception ex) {
                ex.printStackTrace();
                try {
                    FileWriter errorLog = new FileWriter(new File(path+"/localization_results","errorLog.txt"));
                    String timeStamp = String.valueOf(new Timestamp((new Date()).getTime()));
                    errorLog.write(timeStamp+"\n\n");
                    errorLog.write(ex+"\n"+ Arrays.toString(ex.getStackTrace()));
                    errorLog.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }

                Alert alert = new Alert(Alert.AlertType.ERROR, ""+ex+"\n\nCheck error log for more details");
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.showAndWait();
                window.fireEvent(new WindowEvent(
                        window,
                        WindowEvent.WINDOW_CLOSE_REQUEST
                ));
                return;
            }
        });
        Button undoButton=new Button("Undo");
        undoButton.setOnAction(e -> {
            local.setCircle(prevCircle);
            if(autoCircleHistory.size()==5) {
                autoCircleHistory.remove(0);
            }
            autoCircleHistory.add("After undo: radius="+df.format(local.circle[2]));
            circleFitScreen();
        });
        Button redoButton=new Button("Redo");
        redoButton.setOnAction(e -> {
            local.setCircle(curCircle);
            if(autoCircleHistory.size()==5) {
                autoCircleHistory.remove(0);
            }
            autoCircleHistory.add("After redo: radius="+df.format(local.circle[2]));
            circleFitScreen();
        });

        Button initButton=new Button("Reset to initial circle");
        initButton.setOnAction(e -> {
            prevCircle=local.circle;
            local.setCircle(initCircle);
            if(autoCircleHistory.size()==5) {
                autoCircleHistory.remove(0);
            }
            autoCircleHistory.add("AUTOMATIC: radius="+df.format(local.circle[2])+" (sd="+local.sd2+", boost="+local.boostPart+"%)");
            curCircle=local.circle;
            circleFitScreen();
        });
        Button historyButton=new Button("Past attempts");
        historyButton.setOnAction(e -> {
            Alert alert=new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setTitle("Past attempts");
            StringBuilder content= new StringBuilder("Last " + autoCircleHistory.size() + " attempts:\n\n");
            for(int i=autoCircleHistory.size()-1; i>=0; i--) {
                content.append(autoCircleHistory.get(i)).append("\n");
            }
            alert.setContentText(content.toString());
            alert.showAndWait();
        });
        Button clearButton=new Button("Reset to default");
        clearButton.setOnAction(e -> {
            local.boostPart=0;
            local.sd2=5;
            circleFitScreen();
        });

        HBox undoLayout=new HBox(10);
        undoLayout.setAlignment(Pos.CENTER);
        undoLayout.getChildren().addAll(undoButton,redoButton,historyButton);
        parametersVBox.getChildren().addAll(undoLayout,initButton,new Separator(),automaticLabel,autolabel,sdInfo,sdHBox,boostInfo,boostHBox,retryButton,clearButton);
        parametersVBox.setAlignment(Pos.CENTER);

        VBox rightPart=new VBox(5);
        rightPart.getChildren().addAll(parametersVBox,new Separator(),manualVBox);

        centerImage=getImage(local.circleImage,imgsize,imagePixelLabel,physicalPixelLabel);
//        pane.setAlignment(Pos.CENTER);

        circleLayout.setTop(topVBox);
        circleLayout.setCenter(centerImage);
        circleLayout.setBottom(confirmLayout);
        circleLayout.setLeft(infoVBox);
        circleLayout.setRight(rightPart);

        ScrollPane sp = new ScrollPane();
        sp.setContent(circleLayout);
        Scene circleScene=new Scene(sp);

//        Scene circleScene = new Scene(circleLayout);
        window.setScene(circleScene);
//        window.setMaximized(true);
        window.show();
    }

    /** Transforms a BufferedImage to an ImageView of given size to display in UI
     * Handles image and physical pixel location labels, to display the correct coordinates on click.
     * The physical location is calculated by dividing with the resize ratio
     *
     * @param img BufferedImage to display
     * @param size max dimension of display image
     * @param imagePixelLabel label for image pixel coordinates of clicked points
     * @param physicalPixelLabel label for physical pixel coordinates of clicked points
     * @return ImageView ready to display in UI
     */
    private ImageView getImage(BufferedImage img, int size, Label imagePixelLabel, Label physicalPixelLabel) {
        Image image;
        ImageView imageView;

        double resizeRatio;
        image=SwingFXUtils.toFXImage(img, null);
        double w=image.getWidth();
        double h=image.getHeight();
        int viewH,viewW;
        if(w>h) {
            resizeRatio=size/w;
            imageView=new ImageView(image);
            imageView.setFitWidth(size);
            imageView.setPreserveRatio(true);
            viewW=size;
            viewH= (int) (h*resizeRatio);
        }
        else {
            resizeRatio=size/h;
            imageView=new ImageView(image);
            imageView.setFitHeight(size);
            imageView.setPreserveRatio(true);
            viewH=size;
            viewW= (int) (w*resizeRatio);
        }

        if(imagePixelLabel==null && physicalPixelLabel==null) return imageView;

        imageView.setPickOnBounds(true);

        imageView.setOnMouseClicked(e -> {
            DecimalFormat df=new DecimalFormat("0.00");
            double physX,physY,imgX,imgY;
            imgX=e.getX();
            imgY=viewH-e.getY()-1;
            physX=e.getX()/resizeRatio;
            physY=h-e.getY()/resizeRatio-1;
            if(imgX<0) imgX=0;
            if(imgY<0) imgY=0;
            if(physX<0) physX=0;
            if(physY<0) physY=0;
            if(imgX>viewW-1) imgX=viewW-1;
            if(imgY>viewH-1) imgY=viewH-1;
            if(physX>w-1) physX=w-1;
            if(physY>h-1) physY=h-1;
            imagePixelLabel.setText("Image (pixels): X="+Math.round(imgX)+", Y="+Math.round(imgY));
            physicalPixelLabel.setText("Physical (pixels): X="+Math.round(physX)+", Y="+Math.round(physY));
        });

        imageView.setStyle("-fx-cursor: crosshair");
        return imageView;
    }

    /** Handles screen for hemisphere and orientation selection.
     * Allows user to view light-removed image, impact frame and reference moon picture
     * Contains radio buttons to choose lunar hemisphere and whether the image is flipped
     * Contains buttons to continue localization until correlation, with or without automatic rotation, and displays transition screen
     * Writes projection to results directory
     */
    private void hemisphereScreen() {
        window.setTitle("FDS: Localization");
//        window.setMaximized(true);

        hemiLayout=new BorderPane();
        hemiLayout.setPadding(new Insets(5,5,5,5));

        Label pickhemlabel=new Label("Pick non-sunlit lunar hemisphere:");
        ToggleGroup tgHemisphere=new ToggleGroup();
        RadioButton rE=new RadioButton("East");
        rE.setSelected(true);
        rE.setToggleGroup(tgHemisphere);
        RadioButton rW=new RadioButton("West");
        rW.setToggleGroup(tgHemisphere);

        Label pickfliplabel=new Label("Is the image flipped on the Y-axis? (upside down)");
        ToggleGroup tgflip=new ToggleGroup();
        RadioButton ryes=new RadioButton("Yes");
        ryes.setToggleGroup(tgflip);
        RadioButton rno=new RadioButton("No");
        rno.setSelected(true);
        rno.setToggleGroup(tgflip);

        Image map = new Image(getClass().getResourceAsStream( "moonmap.png" ));
        ImageView mapview=new ImageView(map);
        mapview.setFitWidth(imgsize);
        mapview.setPreserveRatio(true);

        Label infoLabel=new Label("Filtered observation image");

        Button mapButton=new Button("View reference");
        mapButton.setOnAction(e -> {
            infoLabel.setText("Moon reference picture");
            hemiLayout.setCenter(mapview);
        });
        Button noLightButton=new Button("View filtered image");
        noLightButton.setOnAction(e -> {
            infoLabel.setText("Filtered observation image");
            centerImage=getImage(local.removedLight,imgsize,null,null);
            hemiLayout.setCenter(centerImage);
        });
        Button impactButton=new Button("View impact frame");
        impactButton.setOnAction(e -> {
            infoLabel.setText("Impact frame");
            centerImage=getImage(local.impactImage,imgsize,null,null);
            hemiLayout.setCenter(centerImage);
        });

        HBox imagebuttons=new HBox(10);
        imagebuttons.getChildren().addAll(noLightButton,mapButton,impactButton);
        imagebuttons.setAlignment(Pos.CENTER);
        VBox topLayout=new VBox(10);
        topLayout.getChildren().addAll(imagebuttons,infoLabel);
        topLayout.setAlignment(Pos.CENTER);

        Button yesButton=new Button("Continue");
        yesButton.setOnAction(e -> {
            Stage transitionStage=new Stage();
            try {
                window.hide();

                display(transitionStage,"FDS: Localization in progress, please wait a moment...","This part may take a couple minutes.");
                local.setupProjection(rW.isSelected());
                local.writeFile(local.projectionMap, "6-projection", "byte");
                if(ryes.isSelected() && flipped) {
                    local.correlation(true, false);
                    flipped=true;
                }
                else if(ryes.isSelected() && !flipped) {
                    local.correlation(true, true);
                    flipped=true;
                }
                else if(!ryes.isSelected() && !flipped) {
                    local.correlation(true, false);
                    flipped=false;
                }
                else if(!ryes.isSelected() && flipped) {
                    local.correlation(true, true);
                    flipped=false;
                }


                initcor= new double[3];
                prevCor=new double[3];
                curCor=new double[3];

                initcor[0]=local.bestangle;
                initcor[1]=0;
                initcor[2]=0;

                prevCor[0]=local.bestangle;
                prevCor[1]=0;
                prevCor[2]=0;

                curCor[0]=local.bestangle;
                curCor[1]=0;
                curCor[2]=0;

                if (correlationHistory.size() == 5) {
                    correlationHistory.remove(0);
                }
                DecimalFormat df=new DecimalFormat("0.00");
                correlationHistory.add("Rotated to " + df.format(local.bestangle) + " degrees. Matches: " + df.format(local.match * 100) + "%");
                correlationScreen();
                transitionStage.close();
            } catch (Exception | OutOfMemoryError ex) {
                transitionStage.close();
                ex.printStackTrace();
                try {
                    FileWriter errorLog = new FileWriter(new File(path+"/localization_results","errorLog.txt"));
                    String timeStamp = String.valueOf(new Timestamp((new Date()).getTime()));
                    errorLog.write(timeStamp+"\n\n");
                    errorLog.write(ex+"\n"+ Arrays.toString(ex.getStackTrace()));
                    errorLog.close();
                } catch (IOException exc) {
                    exc.printStackTrace();

                }
                Alert alert = new Alert(Alert.AlertType.ERROR, ex+""+"\n\nCheck error log for more details");
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.showAndWait();
                window.fireEvent(new WindowEvent(
                        window,
                        WindowEvent.WINDOW_CLOSE_REQUEST
                ));
                return;
            }
        });

        Button noautobutton=new Button("Continue without\nautomatic rotation");
        noautobutton.setOnAction(e -> {
            Stage transitionStage=new Stage();
            try {
                window.hide();

                display(transitionStage,"FDS: Localization in progress, please wait a moment...","This part may take a couple minutes.");
                local.setupProjection(rW.isSelected());
                local.writeFile(local.projectionMap, "6-projection", "byte");
                local.bestangle=0;
                if(ryes.isSelected() && flipped) {
                    local.correlation(false, false);
                    flipped=true;
                }
                else if(ryes.isSelected() && !flipped) {
                    local.correlation(false, true);
                    flipped=true;
                }
                else if(!ryes.isSelected() && !flipped) {
                    local.correlation(false, false);
                    flipped=false;
                }
                else if(!ryes.isSelected() && flipped) {
                    local.correlation(false, true);
                    flipped=false;
                }

                initcor= new double[3];
                prevCor=new double[3];
                curCor=new double[3];

                initcor[0]=local.bestangle;
                initcor[1]=0;
                initcor[2]=0;

                prevCor[0]=local.bestangle;
                prevCor[1]=0;
                prevCor[2]=0;

                curCor[0]=local.bestangle;
                curCor[1]=0;
                curCor[2]=0;

                if (correlationHistory.size() == 5) {
                    correlationHistory.remove(0);
                }
                DecimalFormat df=new DecimalFormat("0.00");
                correlationHistory.add("Rotated to " + df.format(local.bestangle) + " degrees. Matches: " + df.format(local.match * 100) + "%");
                correlationScreen();
                transitionStage.close();
            } catch (Exception | OutOfMemoryError ex) {
                transitionStage.close();
                ex.printStackTrace();
                try {
                    FileWriter errorLog = new FileWriter(new File(path+"/localization_results","errorLog.txt"));
                    String timeStamp = String.valueOf(new Timestamp((new Date()).getTime()));
                    errorLog.write(timeStamp+"\n\n");
                    errorLog.write(ex+"\n"+ Arrays.toString(ex.getStackTrace()));
                    errorLog.close();
                } catch (IOException exc) {
                    exc.printStackTrace();

                }
                Alert alert = new Alert(Alert.AlertType.ERROR, ex+""+"\n\nCheck error log for more details");
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.showAndWait();
                window.fireEvent(new WindowEvent(
                        window,
                        WindowEvent.WINDOW_CLOSE_REQUEST
                ));
                return;
            }
        });

        VBox pickLayout=new VBox(10);
        pickLayout.getChildren().addAll(new Label("Use the reference moon picture (click View reference) to pick\nhemisphere and identify if the observation image is flipped"),new Separator(),pickhemlabel,rE,rW,new Separator(),pickfliplabel,ryes,rno,new Separator(),yesButton,noautobutton);
        pickLayout.setAlignment(Pos.CENTER);

        hemiLayout.setTop(topLayout);
        hemiLayout.setCenter(getImage(local.removedLight,imgsize,null,null));
        hemiLayout.setLeft(pickLayout);

        ScrollPane sp = new ScrollPane();
        sp.setContent(hemiLayout);
        Scene hemiScene=new Scene(sp);
        window.setScene(hemiScene);
//        window.setMaximized(true);
        window.show();
    }

    /** Handles screen for rotation and correlation parts of localization
     * Allows the user to view the correlation image, projection binary map and rotated lunar mask
     * Allows the user to pick correlation colors
     * Displays the image and physical pixel coordinates, and lunar coordinates, of the impact flash
     * Displays the image and physical pixel coordinates of any point the user clicks on the images
     * Displays the radius of the circle and current rotation angle
     * Allows the user to rotate the lunar mask (+-180 degrees) using sliders
     * Allows the user to zoom in and out
     * Allows the user to shift the rotated image vertically and/or horizontally
     * Has a button to flip the image vertically and retry automatic correlation
     * Has a button to reset to original rotation angle
     * Allows user to undo, redo and see move history
     * Allows user to mark a point with a red cross
     * Has a button to complete localization and fires WINDOW_CLOSE_REQUEST
     *
     * Writes results in localization_logger.txt file:
     *  -Impact pixel coordinates on rotated image
     *  -Rotation angle
     *  -Localization results (longitude and latitude in degrees)
     *
     *  Writes images to results directory: rotated image and correlation image
     *
     * Catches localization exceptions, writes error message and stack trace into an errorLog.txt file inside localization_results directory
     * Once the user closes the error screens, localization stops.
     */
    private void correlationScreen() {
        window.setTitle("FDS: Localization");
//        window.setMaximized(true);
        correlationLayout=new BorderPane();
        correlationLayout.setPadding(new Insets(5,5,5,5));

        Slider slider=new Slider();
        slider.setMin(-180);
        slider.setMax(180);
        slider.setValue(local.bestangle);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setBlockIncrement(1);
        slider.setPrefWidth(800);

        Slider slider2=new Slider();
        slider2.setMin(-3);
        slider2.setMax(3);
        slider2.setValue(0);
        slider2.setShowTickLabels(true);
        slider2.setShowTickMarks(true);
        slider2.setBlockIncrement(0.05f);
        slider2.setPrefWidth(800);

        DoubleProperty angle = new SimpleDoubleProperty();
        angle.bind(slider.valueProperty().add(slider2.valueProperty()));
        Label angleLabel=new Label("Rotation angle (deg): "+local.bestangle);
        angleLabel.textProperty().bind(Bindings.format("Rotation angle (deg): %.2f", angle));

        Button flipButton=new Button("Flip vertically and retry");
        flipButton.setOnAction(e -> {
            Stage transitionStage=new Stage();
            try {
                markOn=false;
                prevCor[0]=local.bestangle;
                prevCor[1]=local.shiftx;
                prevCor[2]=local.shifty;

                window.hide();
                display(transitionStage,"FDS: Localization in progress, please wait a moment...","This part may take a couple minutes.");

                local.correlation(true,true);
                flipped=!flipped;

                curCor[0]=local.bestangle;
                curCor[1]=0;
                curCor[2]=0;

                if(correlationHistory.size()==5) {
                    correlationHistory.remove(0);
                }
                DecimalFormat df=new DecimalFormat("0.00");
                correlationHistory.add("Flipped vertically. Rotated to "+df.format(local.bestangle)+" degrees. Matches: "+df.format(local.match*100)+"%");
                correlationScreen();
                transitionStage.close();
            } catch (Exception | OutOfMemoryError ex) {
                transitionStage.close();
                ex.printStackTrace();
                try {
                    FileWriter errorLog = new FileWriter(new File(path+"/localization_results","errorLog.txt"));
                    String timeStamp = String.valueOf(new Timestamp((new Date()).getTime()));
                    errorLog.write(timeStamp+"\n\n");
                    errorLog.write(ex+"\n"+ Arrays.toString(ex.getStackTrace()));
                    errorLog.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
                Alert alert = new Alert(Alert.AlertType.ERROR, ex+"\n\nCheck error log for more details");
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.showAndWait();
                window.fireEvent(new WindowEvent(
                        window,
                        WindowEvent.WINDOW_CLOSE_REQUEST
                ));
                return;
            }
        });

        Button rotateButton=new Button("Rotate");
        rotateButton.setOnAction(e -> {
            try {
                prevCor[0]=local.bestangle;
                prevCor[1]=local.shiftx;
                prevCor[2]=local.shifty;

                if(markOn) {
                    int[] mark=new int[]{markx, marky};
                    Correlation.rotateAndTrack(local.rotationImage,angle.doubleValue()-local.bestangle,mark);
                    markx=mark[0];
                    marky=mark[1];
                }

                if(local.shifty!=0 || local.shiftx!=0) {
                    markOn=false;
                }

                local.bestangle=angle.doubleValue();
                local.correlation(false,false);
                curCor[0]=local.bestangle;
                curCor[1]=local.shiftx;
                curCor[2]=local.shifty;

                if(markOn) {
                    markedMap=createMark(local.projectionMap,markx,marky);
                    markedRot=createMark(local.rotationImage,markx,marky);
                    markedCor=createMark(local.correlationImage,markx,marky);
                }

                if(correlationHistory.size()==5) {
                    correlationHistory.remove(0);
                }
                DecimalFormat df=new DecimalFormat("0.00");
                correlationHistory.add("Rotated to "+df.format(local.bestangle)+" degrees. Matches: "+df.format(local.match*100)+"%");
                correlationScreen();
            } catch (Exception | OutOfMemoryError ex) {
                ex.printStackTrace();
                try {
                    FileWriter errorLog = new FileWriter(new File(path+"/localization_results","errorLog.txt"));
                    String timeStamp = String.valueOf(new Timestamp((new Date()).getTime()));
                    errorLog.write(timeStamp+"\n\n");
                    errorLog.write(ex+"\n"+ Arrays.toString(ex.getStackTrace()));
                    errorLog.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
                Alert alert = new Alert(Alert.AlertType.ERROR, ex+"\n\nCheck error log for more details");
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.showAndWait();
                window.fireEvent(new WindowEvent(
                        window,
                        WindowEvent.WINDOW_CLOSE_REQUEST
                ));
                return;
            }
        });

        VBox sliderVBox=new VBox(5);
        sliderVBox.getChildren().addAll(new Label("Change rotation angle (based on unrotated image):"),slider,slider2);
        sliderVBox.setAlignment(Pos.CENTER);

        VBox rotateVBox=new VBox(5);
        rotateVBox.getChildren().addAll(angleLabel,rotateButton);
        rotateVBox.setAlignment(Pos.CENTER);

        HBox rotateLayout=new HBox(10);
        rotateLayout.getChildren().addAll(sliderVBox,rotateVBox);
        rotateLayout.setAlignment(Pos.CENTER);

        Button yesButton=new Button("Complete localization");
        yesButton.setOnAction(e -> {
            DecimalFormat df=new DecimalFormat("0.00");
            Alert alert1=new Alert(Alert.AlertType.CONFIRMATION, "Result: LONG="+df.format(local.lunar[0])+", LAT="+df.format(local.lunar[1])+"\nProceed and write results to localization_logger.txt?",ButtonType.YES, ButtonType.NO);
            alert1.setTitle("FDS: Localization");
            alert1.setHeaderText(null);
            alert1.showAndWait();
            if(alert1.getResult() == ButtonType.NO) {
                return;
            }

            try {
                local.writeFile(local.rotationImage,"7-rotation","int");
                local.writeFile(local.correlationImage,"8-correlation","int");

                metadataFile.write("Lunar coordinates (deg): LONG="+df.format(local.lunar[0])+", LAT="+ df.format(local.lunar[1])+ "\n\nOther info:\n");

                metadataFile.write("Center coordinates (deg): LONG="+df.format(local.centerLong)+", LAT="+df.format(local.centerLat)+'\n');
                metadataFile.write("Angular diameter (arcmin): "+df.format(local.angDiam)+'\n');
                if(local.arcsecPixelRatio>0) {
                    metadataFile.write("Pixel scale (arcsec/pixel): "+df.format(local.arcsecPixelRatio)+'\n');
                    metadataFile.write("Radius calculated based on pixel scale (pixels): "+df.format(local.correctRadius)+'\n');
                }
                else {
                    metadataFile.write("Pixel scale (arcsec/pixel): -\n");
                    metadataFile.write("Radius calculated based on pixel scale (pixels): -\n");
                }
                metadataFile.write("Radius (pixels): "+df.format(local.circle[2])+'\n');
                metadataFile.write("Center of circle (pixels): X="+df.format(local.circle[0])+", Y="+df.format(local.circle[1])+'\n');

                metadataFile.write("Rotation angle (deg): "+df.format(local.bestangle)+"\n");
                metadataFile.write("Offset (pixels): X="+local.shiftx+", Y="+local.shifty+"\n");
                metadataFile.write("Impact location on rotated image (pixels): "+ local.impactXrot+", "+(local.rotationImage.getHeight()-local.impactYrot-1)+'\n');

                window.fireEvent(new WindowEvent(
                        window,
                        WindowEvent.WINDOW_CLOSE_REQUEST
                ));
            } catch (Exception ex) {
                ex.printStackTrace();
                try {
                    FileWriter errorLog = new FileWriter(new File(path+"/localization_results","errorLog.txt"));
                    String timeStamp = String.valueOf(new Timestamp((new Date()).getTime()));
                    errorLog.write(timeStamp+"\n\n");
                    errorLog.write(ex+"\n"+ Arrays.toString(ex.getStackTrace()));
                    errorLog.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
                Alert alert = new Alert(Alert.AlertType.ERROR, ex+"\n\nCheck error log for more details");
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.showAndWait();
                window.fireEvent(new WindowEvent(
                        window,
                        WindowEvent.WINDOW_CLOSE_REQUEST
                ));
                return;
            }
        });

        VBox bottomLayout=new VBox(10);
        bottomLayout.getChildren().addAll(rotateLayout,yesButton);
        bottomLayout.setAlignment(Pos.BOTTOM_CENTER);

        DecimalFormat df=new DecimalFormat("0.00");
        Label radiusLabel=new Label("Radius (pixels): "+df.format(local.circle[2]));
        Button radiusButton=new Button("Info");
        radiusButton.setStyle("-fx-text-fill: #0000ff;");
        radiusButton.setOnAction(e -> {
            Alert alert=new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Info about radius");
            alert.setContentText("If the lunar features on the image appear larger than the moon, the radius is too small.\n\nIf the lunar features on the image appear smaller than the moon, the radius is too big.");
            alert.setHeaderText(null);
            alert.showAndWait();
        });
        HBox radiusHbox=new HBox(5);
        radiusHbox.getChildren().addAll(radiusButton,radiusLabel);
        Button goBackButton=new Button("Go back to circle fitting");
        goBackButton.setOnAction(e -> {
            circleFitScreen();
            markOn=false;
            prevCircle=local.circle;
            autoCircleHistory=new ArrayList<>();
            autoCircleHistory.add("AUTOMATIC: radius="+df.format(local.circle[2])+" (sd="+local.sd2+", boost="+local.boostPart+"%)");
        });


        Label imagePixelLabel=new Label("Image (pixels): ");
        Label physicalPixelLabel=new Label("Physical (pixels): ");

        Image image=SwingFXUtils.toFXImage(local.projectionMap,null);
        double size=image.getHeight();
        Label impactLabel=new Label("Impact flash location (pixels)\nPhysical: X="+local.impactXrot+", Y="+(int)(size-local.impactYrot-1));

        Label infoLabel=new Label("Correlation");
        Button correlationButton=new Button("View correlation");
        correlationButton.setOnAction(e -> {
            infoLabel.setText("Correlation");
            if (markOn) {
                centerImage = getImage(markedCor, imgsize, imagePixelLabel, physicalPixelLabel);
            } else {
                centerImage = getImage(local.correlationImage, imgsize, imagePixelLabel, physicalPixelLabel);
            }
            correlationLayout.setCenter(centerImage);
        });
        Button mapButton=new Button("View moon");
        mapButton.setOnAction(e -> {
            infoLabel.setText("Moon");
            if(markOn) {
                centerImage=getImage(markedMap,imgsize,imagePixelLabel,physicalPixelLabel);
            }
            else {
                centerImage=getImage(local.projectionMap,imgsize,imagePixelLabel,physicalPixelLabel);
            }
            correlationLayout.setCenter(centerImage);
        });
        Button rotationButton=new Button("View rotation");
        rotationButton.setOnAction(e -> {
            infoLabel.setText("Rotated image");
            if(markOn) {
                centerImage=getImage(markedRot,imgsize,imagePixelLabel,physicalPixelLabel);
            }
            else {
                centerImage=getImage(local.rotationImage,imgsize,imagePixelLabel,physicalPixelLabel);
            }
            correlationLayout.setCenter(centerImage);
        });

        HBox imageButtons=new HBox(10);
        imageButtons.getChildren().addAll(correlationButton,mapButton,rotationButton);
        imageButtons.setAlignment(Pos.CENTER);

        VBox topBox=new VBox(5);
        topBox.getChildren().addAll(imageButtons,infoLabel);
        topBox.setAlignment(Pos.CENTER);

        Label impactLunarLabel=new Label("Lunar coordinates (deg):\nLongitude= "+df.format(local.lunar[0])+"\nLatitude= "+df.format(local.lunar[1]));

        Button undoButton=new Button("Undo");
        undoButton.setOnAction(e -> {
            local.setCorrelation(prevCor);
            markOn=false;
            try {
                if(correlationHistory.size()==5) {
                    correlationHistory.remove(0);
                }
                correlationHistory.add("Undo");
                correlationScreen();
            } catch (Exception ex) {
                ex.printStackTrace();
                try {
                    FileWriter errorLog = new FileWriter(new File(path+"/localization_results","errorLog.txt"));
                    String timeStamp = String.valueOf(new Timestamp((new Date()).getTime()));
                    errorLog.write(timeStamp+"\n\n");
                    errorLog.write(ex+"\n"+ Arrays.toString(ex.getStackTrace()));
                    errorLog.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
                Alert alert = new Alert(Alert.AlertType.ERROR, ex+"\n\nCheck error log for more details");
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.showAndWait();
                window.fireEvent(new WindowEvent(
                        window,
                        WindowEvent.WINDOW_CLOSE_REQUEST
                ));
            }
        });

        Button redoButton=new Button("Redo");
        redoButton.setOnAction(e -> {
            local.setCorrelation(curCor);
            markOn=false;
            try {
                if(correlationHistory.size()==5) {
                    correlationHistory.remove(0);
                }
                correlationHistory.add("Redo");
                correlationScreen();
            } catch (Exception ex) {
                ex.printStackTrace();
                try {
                    FileWriter errorLog = new FileWriter(new File(path+"/localization_results","errorLog.txt"));
                    String timeStamp = String.valueOf(new Timestamp((new Date()).getTime()));
                    errorLog.write(timeStamp+"\n\n");
                    errorLog.write(ex+"\n"+ Arrays.toString(ex.getStackTrace()));
                    errorLog.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
                Alert alert = new Alert(Alert.AlertType.ERROR, ex+"\n\nCheck error log for more details");
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.showAndWait();
                window.fireEvent(new WindowEvent(
                        window,
                        WindowEvent.WINDOW_CLOSE_REQUEST
                ));
            }
        });

        HBox undoLayout=new HBox(5);
        undoLayout.setAlignment(Pos.CENTER);
        undoLayout.getChildren().addAll(undoButton,redoButton);

        Button resetButton=new Button("Reset to initial");
        resetButton.setOnAction(e -> {
            prevCor[0]=local.bestangle;
            prevCor[1]=local.shiftx;
            prevCor[2]=local.shifty;

            local.setCorrelation(initcor);
            prevCor=new double[3];
            curCor=new double[3];

            curCor[0]=local.bestangle;
            curCor[1]=0;
            curCor[2]=0;
            markOn=false;

            try {
                if(correlationHistory.size()==5) {
                    correlationHistory.remove(0);
                }
                correlationHistory.add("Rotated to "+df.format(local.bestangle)+" degrees. Matches: "+df.format(local.match*100)+"%");
                local.correlation(false,false);
                correlationScreen();
            } catch (Exception | OutOfMemoryError ex) {
                ex.printStackTrace();
                try {
                    FileWriter errorLog = new FileWriter(new File(path+"/localization_results","errorLog.txt"));
                    String timeStamp = String.valueOf(new Timestamp((new Date()).getTime()));
                    errorLog.write(timeStamp+"\n\n");
                    errorLog.write(ex+"\n"+ Arrays.toString(ex.getStackTrace()));
                    errorLog.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
                Alert alert = new Alert(Alert.AlertType.ERROR, ex+"\n\nCheck error log for more details");
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.showAndWait();
                window.fireEvent(new WindowEvent(
                        window,
                        WindowEvent.WINDOW_CLOSE_REQUEST
                ));
            }
        });

        Label matchLabel=new Label("Matching pixels: "+df.format(local.match*100)+"%");
        Button matchbutton=new Button("Info");
        matchbutton.setStyle("-fx-text-fill: #0000ff;");
        matchbutton.setOnAction(e -> {
            Alert alert=new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Info about matches");
            alert.setContentText("Ratio of matching pixels divided by total (colored).\n\nFor the western lunar hemisphere this is not a reliable metric because Ocean Procellarum creates many false positive matches.\n\nDoes not work after shifting.");
            alert.setHeaderText(null);
            alert.showAndWait();
        });
        HBox matchHbox=new HBox(5);
        matchHbox.getChildren().addAll(matchbutton, matchLabel);
        Label colormatchLabel=new Label("Change match color");
        Label colornonmatchLabel=new Label("Change non-match color");
        java.awt.Color initmatchcolor=local.matchColor;
        javafx.scene.paint.Color initmatchcol=javafx.scene.paint.Color.rgb(initmatchcolor.getRed(),initmatchcolor.getGreen(),initmatchcolor.getBlue());
        ColorPicker colorPicker=new ColorPicker(initmatchcol);
        colorPicker.setOnAction(e -> {
            javafx.scene.paint.Color curColor=colorPicker.getValue();
            local.matchColor= new java.awt.Color((float) curColor.getRed(),(float) curColor.getGreen(),(float) curColor.getBlue());
            local.correlationImage=local.corr.overlay(local.rotationImage,new int[]{(int) local.impactXrot, (int) local.impactYrot},local.matchColor,local.nonMatchColor);
            markedCor=createMark(local.correlationImage,markx,marky);
            infoLabel.setText("Correlation");
            if(markOn) {
                centerImage=getImage(markedCor,imgsize,imagePixelLabel,physicalPixelLabel);
            }
            else {
                centerImage=getImage(local.correlationImage,imgsize,imagePixelLabel,physicalPixelLabel);
            }
            correlationLayout.setCenter(centerImage);
        });
        java.awt.Color initnomatchcolor=local.nonMatchColor;
        javafx.scene.paint.Color initnonmatchcol=javafx.scene.paint.Color.rgb(initnomatchcolor.getRed(),initnomatchcolor.getGreen(),initnomatchcolor.getBlue());
        ColorPicker colorPicker2=new ColorPicker(initnonmatchcol);
        colorPicker2.setOnAction(e -> {
            javafx.scene.paint.Color curColor=colorPicker2.getValue();
            java.awt.Color newColor=new java.awt.Color((float) curColor.getRed(),(float) curColor.getGreen(),(float) curColor.getBlue());
            local.nonMatchColor=newColor;
            local.correlationImage=local.corr.overlay(local.rotationImage,new int[]{(int) local.impactXrot, (int) local.impactYrot},local.matchColor,local.nonMatchColor);
            markedCor=createMark(local.correlationImage,markx,marky);
            infoLabel.setText("Correlation");
            if(markOn) {
                centerImage=getImage(markedCor,imgsize,imagePixelLabel,physicalPixelLabel);
            }
            else {
                centerImage=getImage(local.correlationImage,imgsize,imagePixelLabel,physicalPixelLabel);
            }
            correlationLayout.setCenter(centerImage);
        });

        Button markButton=new Button("Mark point");
        markButton.setOnAction(e -> {
            int xx, yy;
            if(physicalPixelLabel.getText().split("\\s+").length>2) {
                String xs=physicalPixelLabel.getText().split("\\s+")[2];
                String ys=physicalPixelLabel.getText().split("\\s+")[3];
                xx= (int) Math.round(Double.parseDouble(xs.substring(2,xs.length()-1)));
                yy= (int) Math.round(Double.parseDouble(ys.substring(2)));
                yy=local.correlationImage.getHeight()-yy-1;
            }
            else return;

            markOn=true;
            markx=xx;
            marky=yy;

            markedMap=createMark(local.projectionMap,xx,yy);
            markedRot=createMark(local.rotationImage,xx,yy);
            markedCor=createMark(local.correlationImage,xx,yy);

            if(infoLabel.getText()=="Moon") {
                centerImage=getImage(markedMap,imgsize,imagePixelLabel,physicalPixelLabel);
                correlationLayout.setCenter(centerImage);
            }
            else if (infoLabel.getText()=="Rotated image") {
                centerImage=getImage(markedRot,imgsize,imagePixelLabel,physicalPixelLabel);
                correlationLayout.setCenter(centerImage);
            }
            else if(infoLabel.getText()=="Correlation") {
                centerImage=getImage(markedCor,imgsize,imagePixelLabel,physicalPixelLabel);
                correlationLayout.setCenter(centerImage);
            }
        });
        Button removeMarkButton=new Button("Remove mark");
        removeMarkButton.setOnAction(e -> {
            markOn=false;
            if(infoLabel.getText()=="Moon") {
                centerImage=getImage(local.projectionMap,imgsize,imagePixelLabel,physicalPixelLabel);
                correlationLayout.setCenter(centerImage);
            }
            else if (infoLabel.getText()=="Rotated image") {
                centerImage=getImage(local.rotationImage,imgsize,imagePixelLabel,physicalPixelLabel);
                correlationLayout.setCenter(centerImage);
            }
            else if(infoLabel.getText()=="Correlation") {
                centerImage=getImage(local.correlationImage,imgsize,imagePixelLabel,physicalPixelLabel);
                correlationLayout.setCenter(centerImage);
            }
        });
        HBox markHBox=new HBox(5);
        markHBox.getChildren().addAll(markButton,removeMarkButton);

        VBox infoLabels=new VBox(5);
        infoLabels.getChildren().addAll(new Label("Try to match large lunar features"),matchHbox,colormatchLabel,colorPicker,colornonmatchLabel,colorPicker2,new Separator(),radiusHbox,goBackButton,new Separator(),new Label("Impact flash: green cross mark"),impactLabel,impactLunarLabel,new Separator(),imagePixelLabel,physicalPixelLabel,new Label("Create a red cross mark"),markHBox);
        infoLabels.setPrefWidth(220);
        infoLabels.setAlignment(Pos.CENTER_LEFT);

        Slider sliderX=new Slider();
        sliderX.setMin(-50);
        sliderX.setMax(50);
        sliderX.setValue(0);
        sliderX.setShowTickLabels(true);
        sliderX.setShowTickMarks(true);
        sliderX.setBlockIncrement(1);

        IntegerProperty xoffset = new SimpleIntegerProperty();
        xoffset.bind(sliderX.valueProperty());
        Label shiftamount=new Label("0 pixels");
        shiftamount.textProperty().bind(Bindings.format("%d pixels", xoffset));

        Button shiftXbutton=new Button("Shift horizontally");
        shiftXbutton.setOnAction(e -> {
            int shift=Integer.parseInt(shiftamount.getText().split("\\s+")[0]);
            try {
                prevCor[0]=local.bestangle;
                prevCor[1]=local.shiftx;
                prevCor[2]=local.shifty;

                if(markOn) {
                    markx+=shift;
                }
                local.shift(shift,0);
                if(markOn) {
                    markedMap=createMark(local.projectionMap,markx,marky);
                    markedRot=createMark(local.rotationImage,markx,marky);
                    markedCor=createMark(local.correlationImage,markx,marky);
                }
                if(correlationHistory.size()==5) {
                    correlationHistory.remove(0);
                }
                curCor[0]=local.bestangle;
                curCor[1]=local.shiftx;
                curCor[2]=local.shifty;
                correlationHistory.add("Shifted horizontally "+shift+" pixels.");
                correlationScreen();
            } catch (Exception | OutOfMemoryError ex) {
                ex.printStackTrace();
                try {
                    FileWriter errorLog = new FileWriter(new File(path+"/localization_results","errorLog.txt"));
                    String timeStamp = String.valueOf(new Timestamp((new Date()).getTime()));
                    errorLog.write(timeStamp+"\n\n");
                    errorLog.write(ex+"\n"+ Arrays.toString(ex.getStackTrace()));
                    errorLog.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
                Alert alert = new Alert(Alert.AlertType.ERROR, ex+"\n\nCheck error log for more details");
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.showAndWait();
                window.fireEvent(new WindowEvent(
                        window,
                        WindowEvent.WINDOW_CLOSE_REQUEST
                ));
                return;
            }
        });

        Button shiftYbutton=new Button("Shift vertically");
        shiftYbutton.setOnAction(e -> {
            int shift=-Integer.parseInt(shiftamount.getText().split("\\s+")[0]);
            try {
                prevCor[0]=local.bestangle;
                prevCor[1]=local.shiftx;
                prevCor[2]=local.shifty;
                if(markOn) {
                    marky+=shift;
                }
                local.shift(0,shift);
                if(markOn) {
                    markedMap=createMark(local.projectionMap,markx,marky);
                    markedRot=createMark(local.rotationImage,markx,marky);
                    markedCor=createMark(local.correlationImage,markx,marky);
                }
                curCor[0]=local.bestangle;
                curCor[1]=local.shiftx;
                curCor[2]=local.shifty;
                if(correlationHistory.size()==5) {
                    correlationHistory.remove(0);
                }
                correlationHistory.add("Shifted vertically "+shift+" pixels");
                correlationScreen();
            } catch (Exception | OutOfMemoryError ex) {
                ex.printStackTrace();
                try {
                    FileWriter errorLog = new FileWriter(new File(path+"/localization_results","errorLog.txt"));
                    String timeStamp = String.valueOf(new Timestamp((new Date()).getTime()));
                    errorLog.write(timeStamp+"\n\n");
                    errorLog.write(ex+"\n"+ Arrays.toString(ex.getStackTrace()));
                    errorLog.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
                Alert alert = new Alert(Alert.AlertType.ERROR, ex+"\n\nCheck error log for more details");
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.showAndWait();
                window.fireEvent(new WindowEvent(
                        window,
                        WindowEvent.WINDOW_CLOSE_REQUEST
                ));
                return;
            }
        });

        Slider slider3=new Slider();
        slider3.setMin(0.1);
        slider3.setMax(1);
        slider3.setValue(0.1);
        slider3.setBlockIncrement(0.05);
        Label zoomLabel=new Label("100%");

        Button zoomButton=new Button("Change focus (zoom)");
        zoomButton.setOnAction(e -> {
            float percent= (float) slider3.getValue();
            percent=1-percent+0.1f;
            zoomLabel.setText(Math.round(percent*100)+"%");
            int xx=(int) local.impactXrot;
            int yy=(int) local.impactYrot;

            if(physicalPixelLabel.getText().split("\\s+").length>2) {
                String xs=physicalPixelLabel.getText().split("\\s+")[2];
                String ys=physicalPixelLabel.getText().split("\\s+")[3];
                xx= (int) Math.round(Double.parseDouble(xs.substring(2,xs.length()-1)));
                yy= (int) Math.round(Double.parseDouble(ys.substring(2)));
                yy=local.correlationImage.getHeight()-yy-1;
            }
            BufferedImage displayImage=local.correlationImage;
            if(infoLabel.getText().charAt(0) == 'C') {
                if(markOn) displayImage=markedCor;
                else displayImage=local.correlationImage;
            }
            else if(infoLabel.getText().charAt(0)=='M') {
                if(markOn) displayImage=markedMap;
                else displayImage=local.projectionMap;
            }
            else if(infoLabel.getText().charAt(0)=='R') {
                if(markOn) displayImage=markedRot;
                else displayImage=local.rotationImage;
            }
            centerImage=zoomImage(displayImage,xx,yy,imgsize,percent,imagePixelLabel,physicalPixelLabel);
            correlationLayout.setCenter(centerImage);
        });
        HBox zoomBox=new HBox(5);
        zoomBox.getChildren().addAll(slider3,zoomLabel);
        zoomBox.setAlignment(Pos.CENTER);

        Button historyButton=new Button("Move history");
        historyButton.setOnAction(e -> {
            Alert alert=new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setTitle("Move history");
            StringBuilder content= new StringBuilder("Last " + correlationHistory.size() + " moves:\n\n");
            for(int i=correlationHistory.size()-1; i>=0; i--) {
                content.append(correlationHistory.get(i)).append("\n");
            }
            alert.setContentText(content.toString());
            alert.showAndWait();
        });

        VBox shiftLayout=new VBox(10);
        shiftLayout.getChildren().addAll(undoLayout,historyButton,resetButton,new Separator(),sliderX,shiftamount,shiftXbutton,shiftYbutton,new Separator(),zoomBox,zoomButton,new Separator(),flipButton);
        shiftLayout.setAlignment(Pos.CENTER);
        shiftLayout.setPrefWidth(200);

        if(markOn) {
            centerImage=getImage(markedCor,imgsize,imagePixelLabel,physicalPixelLabel);
        }
        else centerImage=getImage(local.correlationImage,imgsize,imagePixelLabel,physicalPixelLabel);
        correlationLayout.setCenter(centerImage);
        correlationLayout.setBottom(bottomLayout);
        correlationLayout.setLeft(infoLabels);
        correlationLayout.setTop(topBox);
        correlationLayout.setRight(shiftLayout);

        ScrollPane sp = new ScrollPane();
        sp.setContent(correlationLayout);
        Scene scene=new Scene(sp);
        window.setScene(scene);
//        window.setMaximized(true);
        window.show();
    }

    /** Handles zooming in images, correctly updating image and physical pixel coordinate labels on click
     *
     * @param size max dimension of ImageView to display
     * @param percent zoom amount as a % of the original image
     * @param imagePixelLabel label that displays image pixel coordinates of clicked point
     * @param physicalPixelLabel label that displays physical pixel coordinates of clicked point
     * @return ImageView of zoomed image, ready to display on UI
     */
    private ImageView zoomImage(BufferedImage img, int xx, int yy, int size, float percent, Label imagePixelLabel, Label physicalPixelLabel) {
        ImageView imageView;

        int osizex=img.getWidth();
        int osizey=img.getHeight();

        int newsizex= (int) (osizex*percent);
        int newsizey=(int) (osizey*percent);
        int x=Math.min(Math.max(0,xx-newsizex/2),osizex-newsizex);
        int y=Math.min(Math.max(0,yy-newsizey/2),osizey-newsizey);

        BufferedImage subImage=img.getSubimage(x,y,newsizex,newsizey);

        double resizeRatio;
        int viewH,viewW;
        if(newsizex>newsizey) {
            resizeRatio=size*1.0/newsizex;
            imageView=new ImageView(SwingFXUtils.toFXImage(subImage,null));
            imageView.setFitWidth(size);
            imageView.setPreserveRatio(true);
            viewW=size;
            viewH= (int) (newsizey*resizeRatio);
        }
        else {
            resizeRatio=size*1.0/newsizey;
            imageView=new ImageView(SwingFXUtils.toFXImage(subImage,null));
            imageView.setFitHeight(size);
            imageView.setPreserveRatio(true);
            viewH=size;
            viewW= (int) (newsizex*resizeRatio);
        }

        imageView.setPickOnBounds(true);

        imageView.setOnMouseClicked(e -> {
            DecimalFormat df=new DecimalFormat("0.00");
            double physX,physY,imgX,imgY;
            imgX=e.getX();
            imgY=size-e.getY()-1;
            physX=e.getX()/resizeRatio+x;
            physY=osizey-(e.getY()/resizeRatio+y)-1;
            if(imgX<0) imgX=0;
            if(imgY<0) imgY=0;
            if(physX<0) physX=0;
            if(physY<0) physY=0;
            if(imgX>viewW-1) imgX=viewW-1;
            if(imgY>viewH-1) imgY=viewH-1;
            if(physX>osizex-1) physX=osizey-1;
            if(physY>osizex-1) physY=osizey-1;
            imagePixelLabel.setText("Image (pixels): X="+Math.round(imgX)+", Y="+Math.round(imgY));
            physicalPixelLabel.setText("Physical (pixels): X="+Math.round(physX)+", Y="+Math.round(physY));
        });
        imageView.setStyle("-fx-cursor: crosshair");
        return imageView;
    }

    /** Adds a red cross mark on a specific point on an image
     *
     * @param background image to add mark
     * @param x x pixel location of mark
     * @param y y pixel location of mark
     * @return image with mark on
     */
    public BufferedImage createMark(BufferedImage background,int x, int y) {
        int w=background.getWidth();
        int h=background.getHeight();
        BufferedImage output=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        for(int i=0; i<w; i++) {
            for(int j=0; j<h; j++) {
                output.setRGB(i,j,background.getRGB(i,j));
            }
        }

        for(int i=Math.max(x-12,0); i<Math.min(x+13,w-1); i++) {
            for(int j=Math.max(y-2,0); j<Math.min(y+3,h-1); j++) {
                output.setRGB(i,j,new Color(255,40,0).getRGB());
            }
        }
        for(int i=Math.max(x-2,0); i<Math.min(x+3,w-1); i++) {
            for(int j=Math.max(y-12,0); j<Math.min(y+13,h-1); j++) {
                output.setRGB(i,j,new Color(255,40,0).getRGB());
            }
        }

        return output;
    }

    public static void closeStage(Stage window){
        window.close();
    }

    /** Creates a window with a given title and message
     *
     * @param window
     * @param title title of window
     * @param message content of window
     */
    public static void display(Stage window,String title, String message) {
        window.setTitle(title);
        Label label = new Label();
        label.setText(message);
        window.setResizable(false);

        VBox layout = new VBox(10);
        layout.getChildren().addAll(label);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout,600,250);
        window.setScene(scene);

        window.show();

    }

    public void Test(String folderPath) throws Exception {
        Detection.Test(folderPath);
    }


//    public static void print(Node node) {
//
//        System.out.println("Creating a printer job...");
//        PrinterJob job = PrinterJob.createPrinterJob();
//        if (job != null) {
//            System.out.println(job.jobStatusProperty().asString());
//            System.out.println("kainourgia dokimiiii");
//            boolean printed = job.printPage(node);
//            if (printed) {
//                job.endJob();
//            } else {
//                System.out.println("Printing failed.");
//            }
//        } else {
//            System.out.println("Could not create a printer job.");
//        }
//    }
}