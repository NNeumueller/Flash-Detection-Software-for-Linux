package noa.ibv.fds.work;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;

public class TextAreaLogger extends JPanel {
  private static final long serialVersionUID = 1L;
  
  private static JTextArea textArea;
  
  private static JFrame frame;
  
  public TextAreaLogger() {
    JPanel panel = new JPanel();
    add(panel);
    panel.setLayout(new BorderLayout(0, 0));
    JScrollPane scrollPane = new JScrollPane();
    panel.add(scrollPane, "Center");
    textArea = new JTextArea();
    textArea.setFont(new Font("Courier New", 0, 12));
    DefaultCaret caret = (DefaultCaret)textArea.getCaret();
    caret.setUpdatePolicy(2);
    textArea.setColumns(120);
    textArea.setRows(20);
    scrollPane.setViewportView(textArea);
  }
  
  public static void appendText(String text) {
    textArea.append(String.valueOf(text) + "\n");
  }
  
  public static void createAndShowGUI(String txtLabel) {
    frame = new JFrame(txtLabel);
    frame.setDefaultCloseOperation(3);
    frame.getContentPane().add(new TextAreaLogger());
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gd = ge.getScreenDevices();
    if (gd.length == 2)
      frame.setLocation((gd[1].getDefaultConfiguration().getBounds()).x, frame.getY()); 
    frame.pack();
    frame.setVisible(true);
  }
  
  public static void removeTheGUI() {
    frame.dispose();
  }
  
  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            TextAreaLogger.createAndShowGUI("TextAreaLogger");
          }
        });
  }
}


/* Location:              /home/nico/Downloads/V3/DetectionStandalone.jar!/noa/ibv/fds/work/TextAreaLogger.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */