package noa.ibv.fds.work;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ArrayToImage {
  private static final int HEIGHT = 250;
  
  private static final int WIDTH = 250;
  
  public static void main(String[] args) {
    double[][] data = new double[250][250];
    Random r = new Random();
    int i;
    for (i = 0; i < 250; i++) {
      for (int k = 0; k < 250; k++)
        data[i][k] = r.nextDouble(); 
    } 
    for (i = 0; i < 250; i++) {
      for (int k = 0; k < 250; k++)
        data[i][k] = Math.sqrt(Math.pow(i - 125.0D, 2.0D) + Math.pow(k - 125.0D, 2.0D)); 
    } 
    double valMin = 100000.0D;
    double valMax = -valMin;
    double val = 0.0D;
    int j;
    for (j = 0; j < 250; j++) {
      for (int k = 0; k < 250; k++) {
        val = data[j][k];
        if (val < valMin)
          valMin = val; 
        if (val > valMax)
          valMax = val; 
      } 
    } 
    System.out.println("MAX: " + valMax);
    System.out.println("MIn: " + valMin);
    for (j = 0; j < 250; j++) {
      for (int k = 0; k < 250; k++)
        data[j][k] = data[j][k] / valMax; 
    } 
    createAndShowImageGui(data, 250, 250, 100, 100, "Test image");
  }
  
  public static void createAndShowImageGui(double[] inpData, double[] outData, int width, int height, int xPos, int yPos) {
    double[][] data2D = new double[height][width];
    double vMin = 100000.0D;
    double vMax = -vMin;
    byte b;
    int i;
    double[] arrayOfDouble;
    for (i = (arrayOfDouble = inpData).length, b = 0; b < i; ) {
      double val = arrayOfDouble[b];
      if (val < vMin)
        vMin = val; 
      if (val > vMax)
        vMax = val; 
      b++;
    } 
    for (i = (arrayOfDouble = outData).length, b = 0; b < i; ) {
      double val = arrayOfDouble[b];
      if (val < vMin)
        vMin = val; 
      if (val > vMax)
        vMax = val; 
      b++;
    } 
    System.out.println("Max: " + vMax);
    System.out.println("Min: " + vMin);
    int n = 0;
    int j;
    for (j = 0; j < height; j++) {
      for (int k = 0; k < width; k++)
        data2D[k][j] = inpData[n++] / vMax; 
    } 
    createAndShowImageGui(data2D, width, height, xPos, yPos, "INPUT");
    n = 0;
    for (j = 0; j < height; j++) {
      for (int k = 0; k < width; k++)
        data2D[k][j] = outData[n++] / vMax; 
    } 
    createAndShowImageGui(data2D, width, height, xPos + 2 * width, yPos, "OUTPUT");
    n = 0;
    for (j = 0; j < height; j++) {
      for (int k = 0; k < width; k++)
        data2D[k][j] = outData[n] - inpData[n++]; 
    } 
    createAndShowImageGui(data2D, width, height, xPos + 4 * width, yPos, "DIFFER");
  }
  
  public static void createAndShowImageGui(double[] data, int width, int height, int xPos, int yPos, String title) {
    double[][] data2D = new double[height][width];
    double vMin = 100000.0D;
    double vMax = -vMin;
    byte b;
    int i;
    double[] arrayOfDouble;
    for (i = (arrayOfDouble = data).length, b = 0; b < i; ) {
      double val = arrayOfDouble[b];
      if (val < vMin)
        vMin = val; 
      if (val > vMax)
        vMax = val; 
      b++;
    } 
    System.out.println("Max: " + vMax);
    System.out.println("Min: " + vMin);
    int n = 0;
    for (int j = 0; j < height; j++) {
      for (int k = 0; k < width; k++)
        data2D[k][j] = data[n++] / vMax; 
    } 
    createAndShowImageGui(data2D, width, height, xPos, yPos, title);
  }
  
  public static void createAndShowImageGui(double[][] data, int width, int height, int xPos, int yPos, String title) {
    final BufferedImage img = new BufferedImage(width, height, 1);
    Graphics2D g = (Graphics2D)img.getGraphics();
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        double v = data[i][j];
        float aCol = (float)v;
        try {
          g.setColor(new Color(aCol, aCol, aCol));
        } catch (Exception e) {
          System.out.println("Value: " + v + "\tColor: " + aCol);
          g.setColor(Color.WHITE);
        } 
        g.fillRect(i, j, 1, 1);
      } 
    } 
    JFrame frame = new JFrame(title);
    frame.setDefaultCloseOperation(3);
    frame.setLocation(xPos, yPos);
    JPanel panel = new JPanel() {
        private static final long serialVersionUID = 1L;
        
        protected void paintComponent(Graphics g) {
          Graphics2D g2d = (Graphics2D)g;
          g2d.clearRect(0, 0, getWidth(), getHeight());
          g2d.setRenderingHint(
              RenderingHints.KEY_INTERPOLATION, 
              RenderingHints.VALUE_INTERPOLATION_BILINEAR);
          g2d.scale(2.0D, 2.0D);
          g2d.drawImage(img, 0, 0, this);
        }
      };
    panel.setPreferredSize(new Dimension(width * 2, height * 2));
    frame.getContentPane().add(panel);
    frame.pack();
    frame.setVisible(true);
  }
}


/* Location:              /home/nico/Downloads/V3/DetectionStandalone.jar!/noa/ibv/fds/work/ArrayToImage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */