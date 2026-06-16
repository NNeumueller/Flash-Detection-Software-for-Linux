package noa.ibv.fds.tools;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class TwoDGaussDialogGui extends JDialog {
  private static final long serialVersionUID = 1L;
  
  private String mess_EventSetInit = "Event-like parameters selected; press Run-key";
  
  private String mess_HotPxSetInit = "Hot pixel simulation parameters set; press Run-key";
  
  private String mess_CsmRySetInit = "Cosmic ray simulation parameters set; press Run-key";
  
  private String mess_UserSetInit = "Set/update the parameters; press Accept-key";
  
  int rangeSizeMin = 5;
  
  int rangeSizeMax = 1000;
  
  int rangePosMin = 1;
  
  double rangeAmplMin = 0.1D;
  
  double rangeAmplMax = 100000.0D;
  
  private JDialog dialog;
  
  private JRadioButton rdbtn_ExampleEvent;
  
  private JRadioButton rdbtn_ExampleHotPix;
  
  private JRadioButton rdbtn_ExampleCosmRay;
  
  private JRadioButton rdbtn_ExampleUserDef;
  
  private final ButtonGroup buttonGroup = new ButtonGroup();
  
  private final JPanel contentPanel = new JPanel();
  
  private JTextField textField_ImSizeX;
  
  private JTextField textField_ImSizeY;
  
  private JTextField textField_SignalAmpl;
  
  private JTextField textField_SignalOffs;
  
  private JTextField textField_SignalNoise;
  
  private JTextField textField_MeanX;
  
  private JTextField textField_MeanY;
  
  private JTextField textField_SigmaX;
  
  private JTextField textField_SigmaY;
  
  private JTextField textField_RotAngleDeg;
  
  private JTextField textField_RotAngleRad;
  
  private Button btn_ImSizeXPlus;
  
  private Button btn_ImSizeXMinus;
  
  private Button btn_ImSizeYPlus;
  
  private Button btn_ImSizeYMinus;
  
  private Button btn_MeanXPlus;
  
  private Button btn_MeanXMinus;
  
  private Button btn_MeanYPlus;
  
  private Button btn_MeanYMinus;
  
  private Button btn_SignalAmplPlus;
  
  private Button btn_SignalAmplMinus;
  
  private Button btn_SignalOffsPlus;
  
  private Button btn_SignalOffsMinus;
  
  private Button btn_SignalNoisePlus;
  
  private Button btn_SignalNoiseMinus;
  
  private Button btn_SigmaXPlus;
  
  private Button btn_SigmaXMinus;
  
  private Button btn_SigmaYPlus;
  
  private Button btn_SigmaYMinus;
  
  private Button btn_RotAngleRadPlus;
  
  private Button btn_RotAngleRadMinus;
  
  private Button btn_RotAngleDegPlus;
  
  private Button btn_RotAngleDegMinus;
  
  private JButton btn_Accept;
  
  private JButton btn_Run;
  
  private JButton btn_Cancel;
  
  private JTextField txtStart;
  
  int exampleModeCode = 3;
  
  int[] imSizeXY = new int[2];
  
  int[] meanPosXY = new int[2];
  
  double[] paramsSignal = new double[] { 0.0D, 0.0D, 0.0D };
  
  double[] paramsSigmaXY = new double[] { 0.0D, 0.0D };
  
  double[] paramsRotAngle = new double[] { 0.0D, 0.0D };
  
  boolean doRun = false;
  
  private JSeparator separatorBottom;
  
  public static void main(String[] args) {
    TwoDGaussDialogGui myDialog = new TwoDGaussDialogGui();
    myDialog.createAndShowDialog();
  }
  
  public void createAndShowDialog() {
    this.dialog.setDefaultCloseOperation(2);
    this.dialog.setVisible(true);
  }
  
  public TwoDGaussDialogGui() {
    this.dialog = new JDialog();
    this.dialog.setResizable(false);
    this.dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
    this.dialog.setTitle("2D-Gaussian Parameters Set-up");
    this.dialog.setBounds(100, 100, 360, 560);
    this.dialog.getContentPane().setLayout(new BorderLayout());
    this.contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    this.dialog.getContentPane().add(this.contentPanel, "Center");
    GridBagLayout gbl_contentPanel = new GridBagLayout();
    gbl_contentPanel.columnWidths = new int[] { 0, 0, 100 };
    gbl_contentPanel.rowHeights = new int[24];
    gbl_contentPanel.columnWeights = new double[] { 1.0D, 0.0D, 1.0D, 0.0D, 0.0D };
    gbl_contentPanel.rowWeights = new double[] { 
        0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 
        0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 
        0.0D, 0.0D, 0.0D, Double.MIN_VALUE };
    this.contentPanel.setLayout(gbl_contentPanel);
    this.txtStart = new JTextField("START");
    this.txtStart.setEditable(false);
    GridBagConstraints gbc_txtStart = new GridBagConstraints();
    gbc_txtStart.insets = new Insets(0, 0, 5, 0);
    gbc_txtStart.gridwidth = 5;
    gbc_txtStart.fill = 2;
    gbc_txtStart.gridx = 0;
    gbc_txtStart.gridy = 21;
    this.contentPanel.add(this.txtStart, gbc_txtStart);
    this.txtStart.setColumns(10);
    JLabel lbl_Example = new JLabel("Example");
    lbl_Example.setFont(new Font("Tahoma", 1, 12));
    GridBagConstraints gbc_lbl_Example = new GridBagConstraints();
    gbc_lbl_Example.anchor = 17;
    gbc_lbl_Example.gridheight = 4;
    gbc_lbl_Example.insets = new Insets(0, 5, 5, 5);
    gbc_lbl_Example.gridx = 0;
    gbc_lbl_Example.gridy = 0;
    this.contentPanel.add(lbl_Example, gbc_lbl_Example);
    this.rdbtn_ExampleEvent = new JRadioButton("");
    this.rdbtn_ExampleEvent.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            TwoDGaussDialogGui.this.resetParamsToExample(0);
            TwoDGaussDialogGui.this.btn_Accept.setEnabled(false);
            TwoDGaussDialogGui.this.btn_Run.setEnabled(true);
            TwoDGaussDialogGui.this.messageInfo(TwoDGaussDialogGui.this.mess_EventSetInit);
          }
        });
    this.rdbtn_ExampleEvent.setHorizontalAlignment(2);
    this.buttonGroup.add(this.rdbtn_ExampleEvent);
    GridBagConstraints gbc_rdbtn_ExampleEvent = new GridBagConstraints();
    gbc_rdbtn_ExampleEvent.insets = new Insets(0, 0, 5, 5);
    gbc_rdbtn_ExampleEvent.gridx = 1;
    gbc_rdbtn_ExampleEvent.gridy = 0;
    this.contentPanel.add(this.rdbtn_ExampleEvent, gbc_rdbtn_ExampleEvent);
    JLabel lbl_ExampleEvent = new JLabel("Event-like");
    lbl_ExampleEvent.setFont(new Font("Tahoma", 0, 12));
    GridBagConstraints gbc_lbl_ExampleEvent = new GridBagConstraints();
    gbc_lbl_ExampleEvent.gridwidth = 3;
    gbc_lbl_ExampleEvent.anchor = 17;
    gbc_lbl_ExampleEvent.insets = new Insets(0, 0, 5, 0);
    gbc_lbl_ExampleEvent.gridx = 2;
    gbc_lbl_ExampleEvent.gridy = 0;
    this.contentPanel.add(lbl_ExampleEvent, gbc_lbl_ExampleEvent);
    this.rdbtn_ExampleHotPix = new JRadioButton("");
    this.rdbtn_ExampleHotPix.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            TwoDGaussDialogGui.this.resetParamsToExample(1);
            TwoDGaussDialogGui.this.btn_Accept.setEnabled(false);
            TwoDGaussDialogGui.this.btn_Run.setEnabled(true);
            TwoDGaussDialogGui.this.messageInfo(TwoDGaussDialogGui.this.mess_HotPxSetInit);
          }
        });
    this.buttonGroup.add(this.rdbtn_ExampleHotPix);
    GridBagConstraints gbc_rdbtn_ExampleHotPix = new GridBagConstraints();
    gbc_rdbtn_ExampleHotPix.insets = new Insets(0, 0, 5, 5);
    gbc_rdbtn_ExampleHotPix.gridx = 1;
    gbc_rdbtn_ExampleHotPix.gridy = 1;
    this.contentPanel.add(this.rdbtn_ExampleHotPix, gbc_rdbtn_ExampleHotPix);
    JLabel lbl_ExampleHotPix = new JLabel("Hot-pixel emulation");
    lbl_ExampleHotPix.setFont(new Font("Tahoma", 0, 12));
    GridBagConstraints gbc_lbl_ExampleHotPix = new GridBagConstraints();
    gbc_lbl_ExampleHotPix.gridwidth = 3;
    gbc_lbl_ExampleHotPix.anchor = 17;
    gbc_lbl_ExampleHotPix.insets = new Insets(0, 0, 5, 0);
    gbc_lbl_ExampleHotPix.gridx = 2;
    gbc_lbl_ExampleHotPix.gridy = 1;
    this.contentPanel.add(lbl_ExampleHotPix, gbc_lbl_ExampleHotPix);
    this.rdbtn_ExampleCosmRay = new JRadioButton("");
    this.rdbtn_ExampleCosmRay.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            TwoDGaussDialogGui.this.resetParamsToExample(2);
            TwoDGaussDialogGui.this.btn_Accept.setEnabled(false);
            TwoDGaussDialogGui.this.btn_Run.setEnabled(true);
            TwoDGaussDialogGui.this.messageInfo(TwoDGaussDialogGui.this.mess_CsmRySetInit);
          }
        });
    this.buttonGroup.add(this.rdbtn_ExampleCosmRay);
    GridBagConstraints gbc_rdbtn_ExampleCosmRay = new GridBagConstraints();
    gbc_rdbtn_ExampleCosmRay.insets = new Insets(0, 0, 5, 5);
    gbc_rdbtn_ExampleCosmRay.gridx = 1;
    gbc_rdbtn_ExampleCosmRay.gridy = 2;
    this.contentPanel.add(this.rdbtn_ExampleCosmRay, gbc_rdbtn_ExampleCosmRay);
    JLabel lbl_ExampleCosmRay = new JLabel("Cosmic-ray emulation");
    lbl_ExampleCosmRay.setFont(new Font("Tahoma", 0, 12));
    GridBagConstraints gbc_lbl_ExampleCosmRay = new GridBagConstraints();
    gbc_lbl_ExampleCosmRay.gridwidth = 3;
    gbc_lbl_ExampleCosmRay.anchor = 17;
    gbc_lbl_ExampleCosmRay.insets = new Insets(0, 0, 5, 0);
    gbc_lbl_ExampleCosmRay.gridx = 2;
    gbc_lbl_ExampleCosmRay.gridy = 2;
    this.contentPanel.add(lbl_ExampleCosmRay, gbc_lbl_ExampleCosmRay);
    this.rdbtn_ExampleUserDef = new JRadioButton("");
    this.rdbtn_ExampleUserDef.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            TwoDGaussDialogGui.this.textFieldsEnable(true);
            TwoDGaussDialogGui.this.btn_Accept.setEnabled(true);
            TwoDGaussDialogGui.this.btn_Run.setEnabled(false);
            TwoDGaussDialogGui.this.messageInfo(TwoDGaussDialogGui.this.mess_UserSetInit);
          }
        });
    this.rdbtn_ExampleUserDef.setSelected(true);
    this.buttonGroup.add(this.rdbtn_ExampleUserDef);
    GridBagConstraints gbc_rdbtn_ExampleUserDef = new GridBagConstraints();
    gbc_rdbtn_ExampleUserDef.insets = new Insets(0, 0, 5, 5);
    gbc_rdbtn_ExampleUserDef.gridx = 1;
    gbc_rdbtn_ExampleUserDef.gridy = 3;
    this.contentPanel.add(this.rdbtn_ExampleUserDef, gbc_rdbtn_ExampleUserDef);
    JLabel lbl_ExampleUserDef = new JLabel("User defined parameters");
    lbl_ExampleUserDef.setFont(new Font("Tahoma", 0, 12));
    GridBagConstraints gbc_lbl_ExampleUserDef = new GridBagConstraints();
    gbc_lbl_ExampleUserDef.gridwidth = 3;
    gbc_lbl_ExampleUserDef.anchor = 17;
    gbc_lbl_ExampleUserDef.insets = new Insets(0, 0, 5, 0);
    gbc_lbl_ExampleUserDef.gridx = 2;
    gbc_lbl_ExampleUserDef.gridy = 3;
    this.contentPanel.add(lbl_ExampleUserDef, gbc_lbl_ExampleUserDef);
    JSeparator jSeparator5 = new JSeparator();
    jSeparator5.setForeground(Color.BLACK);
    GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
    gridBagConstraints5.fill = 2;
    gridBagConstraints5.gridwidth = 5;
    gridBagConstraints5.insets = new Insets(0, 0, 5, 0);
    gridBagConstraints5.gridx = 0;
    gridBagConstraints5.gridy = 4;
    this.contentPanel.add(jSeparator5, gridBagConstraints5);
    JLabel lbl_ImSize = new JLabel("Image size [pix]");
    lbl_ImSize.setFont(new Font("Tahoma", 1, 12));
    GridBagConstraints gbc_lbl_ImSize = new GridBagConstraints();
    gbc_lbl_ImSize.gridheight = 2;
    gbc_lbl_ImSize.insets = new Insets(0, 5, 5, 5);
    gbc_lbl_ImSize.gridx = 0;
    gbc_lbl_ImSize.gridy = 5;
    this.contentPanel.add(lbl_ImSize, gbc_lbl_ImSize);
    JLabel lbl_ImSizeX = new JLabel("X (columns)");
    lbl_ImSizeX.setFont(new Font("Tahoma", 0, 12));
    GridBagConstraints gbc_lbl_ImSizeX = new GridBagConstraints();
    gbc_lbl_ImSizeX.anchor = 13;
    gbc_lbl_ImSizeX.insets = new Insets(0, 0, 5, 5);
    gbc_lbl_ImSizeX.gridx = 1;
    gbc_lbl_ImSizeX.gridy = 5;
    this.contentPanel.add(lbl_ImSizeX, gbc_lbl_ImSizeX);
    this.textField_ImSizeX = new JTextField();
    myListener(this.textField_ImSizeX, this.txtStart);
    this.textField_ImSizeX.setHorizontalAlignment(4);
    this.textField_ImSizeX.setFont(new Font("Tahoma", 0, 12));
    this.textField_ImSizeX.setText("0");
    GridBagConstraints gbc_textField_ImSizeX = new GridBagConstraints();
    gbc_textField_ImSizeX.insets = new Insets(0, 0, 5, 5);
    gbc_textField_ImSizeX.fill = 2;
    gbc_textField_ImSizeX.gridx = 2;
    gbc_textField_ImSizeX.gridy = 5;
    this.contentPanel.add(this.textField_ImSizeX, gbc_textField_ImSizeX);
    this.textField_ImSizeX.setColumns(5);
    this.btn_ImSizeXPlus = new Button("+");
    this.btn_ImSizeXPlus.setFont(new Font("Tahoma", 1, 12));
    GridBagConstraints gbc_btn_ImSizeXPlus = new GridBagConstraints();
    gbc_btn_ImSizeXPlus.insets = new Insets(0, 0, 5, 5);
    gbc_btn_ImSizeXPlus.gridx = 3;
    gbc_btn_ImSizeXPlus.gridy = 5;
    this.contentPanel.add(this.btn_ImSizeXPlus, gbc_btn_ImSizeXPlus);
    this.btn_ImSizeXMinus = new Button("-");
    this.btn_ImSizeXMinus.setFont(new Font("Tahoma", 1, 12));
    GridBagConstraints gbc_btn_ImSizeXMinus = new GridBagConstraints();
    gbc_btn_ImSizeXMinus.insets = new Insets(0, 0, 5, 0);
    gbc_btn_ImSizeXMinus.gridx = 4;
    gbc_btn_ImSizeXMinus.gridy = 5;
    this.contentPanel.add(this.btn_ImSizeXMinus, gbc_btn_ImSizeXMinus);
    JLabel lbl_ImSizeY = new JLabel("Y (rows)");
    lbl_ImSizeY.setFont(new Font("Tahoma", 0, 12));
    GridBagConstraints gbc_lbl_ImSizeY = new GridBagConstraints();
    gbc_lbl_ImSizeY.anchor = 13;
    gbc_lbl_ImSizeY.insets = new Insets(0, 0, 5, 5);
    gbc_lbl_ImSizeY.gridx = 1;
    gbc_lbl_ImSizeY.gridy = 6;
    this.contentPanel.add(lbl_ImSizeY, gbc_lbl_ImSizeY);
    this.textField_ImSizeY = new JTextField();
    myListener(this.textField_ImSizeY, this.txtStart);
    this.textField_ImSizeY.setHorizontalAlignment(4);
    this.textField_ImSizeY.setFont(new Font("Tahoma", 0, 12));
    this.textField_ImSizeY.setText("0");
    GridBagConstraints gbc_textField_ImSizeY = new GridBagConstraints();
    gbc_textField_ImSizeY.insets = new Insets(0, 0, 5, 5);
    gbc_textField_ImSizeY.fill = 2;
    gbc_textField_ImSizeY.gridx = 2;
    gbc_textField_ImSizeY.gridy = 6;
    this.contentPanel.add(this.textField_ImSizeY, gbc_textField_ImSizeY);
    this.textField_ImSizeY.setColumns(5);
    this.btn_ImSizeYPlus = new Button("+");
    this.btn_ImSizeYPlus.setFont(new Font("Tahoma", 1, 12));
    GridBagConstraints gbc_btn_ImSizeYPlus = new GridBagConstraints();
    gbc_btn_ImSizeYPlus.insets = new Insets(0, 0, 5, 5);
    gbc_btn_ImSizeYPlus.gridx = 3;
    gbc_btn_ImSizeYPlus.gridy = 6;
    this.contentPanel.add(this.btn_ImSizeYPlus, gbc_btn_ImSizeYPlus);
    this.btn_ImSizeYMinus = new Button("-");
    this.btn_ImSizeYMinus.setFont(new Font("Tahoma", 1, 12));
    GridBagConstraints gbc_btn_ImSizeYMinus = new GridBagConstraints();
    gbc_btn_ImSizeYMinus.insets = new Insets(0, 0, 5, 0);
    gbc_btn_ImSizeYMinus.gridx = 4;
    gbc_btn_ImSizeYMinus.gridy = 6;
    this.contentPanel.add(this.btn_ImSizeYMinus, gbc_btn_ImSizeYMinus);
    JSeparator jSeparator4 = new JSeparator();
    GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
    gridBagConstraints4.gridwidth = 5;
    gridBagConstraints4.insets = new Insets(0, 0, 5, 0);
    gridBagConstraints4.gridx = 0;
    gridBagConstraints4.gridy = 7;
    this.contentPanel.add(jSeparator4, gridBagConstraints4);
    JLabel lbl_Signal = new JLabel("Signal");
    lbl_Signal.setFont(new Font("Tahoma", 1, 12));
    GridBagConstraints gbc_lbl_Signal = new GridBagConstraints();
    gbc_lbl_Signal.gridheight = 3;
    gbc_lbl_Signal.anchor = 17;
    gbc_lbl_Signal.insets = new Insets(0, 5, 5, 5);
    gbc_lbl_Signal.gridx = 0;
    gbc_lbl_Signal.gridy = 8;
    this.contentPanel.add(lbl_Signal, gbc_lbl_Signal);
    JLabel lbl_SignalAmpl = new JLabel("Amplitude");
    lbl_SignalAmpl.setFont(new Font("Tahoma", 0, 12));
    GridBagConstraints gbc_lbl_SignalAmpl = new GridBagConstraints();
    gbc_lbl_SignalAmpl.anchor = 13;
    gbc_lbl_SignalAmpl.insets = new Insets(0, 0, 5, 5);
    gbc_lbl_SignalAmpl.gridx = 1;
    gbc_lbl_SignalAmpl.gridy = 8;
    this.contentPanel.add(lbl_SignalAmpl, gbc_lbl_SignalAmpl);
    this.textField_SignalAmpl = new JTextField();
    myListener(this.textField_SignalAmpl, this.txtStart);
    this.textField_SignalAmpl.setHorizontalAlignment(4);
    this.textField_SignalAmpl.setFont(new Font("Tahoma", 0, 12));
    this.textField_SignalAmpl.setText("0.0");
    GridBagConstraints gbc_textField_SignalAmpl = new GridBagConstraints();
    gbc_textField_SignalAmpl.insets = new Insets(0, 0, 5, 5);
    gbc_textField_SignalAmpl.fill = 2;
    gbc_textField_SignalAmpl.gridx = 2;
    gbc_textField_SignalAmpl.gridy = 8;
    this.contentPanel.add(this.textField_SignalAmpl, gbc_textField_SignalAmpl);
    this.textField_SignalAmpl.setColumns(10);
    this.btn_SignalAmplPlus = new Button("+");
    this.btn_SignalAmplPlus.setFont(new Font("Tahoma", 1, 12));
    GridBagConstraints gbc_btn_SignalAmplPlus = new GridBagConstraints();
    gbc_btn_SignalAmplPlus.insets = new Insets(0, 0, 5, 5);
    gbc_btn_SignalAmplPlus.gridx = 3;
    gbc_btn_SignalAmplPlus.gridy = 8;
    this.contentPanel.add(this.btn_SignalAmplPlus, gbc_btn_SignalAmplPlus);
    this.btn_SignalAmplMinus = new Button("-");
    this.btn_SignalAmplMinus.setFont(new Font("Tahoma", 1, 12));
    GridBagConstraints gbc_btn_SignalAmplMinus = new GridBagConstraints();
    gbc_btn_SignalAmplMinus.insets = new Insets(0, 0, 5, 0);
    gbc_btn_SignalAmplMinus.gridx = 4;
    gbc_btn_SignalAmplMinus.gridy = 8;
    this.contentPanel.add(this.btn_SignalAmplMinus, gbc_btn_SignalAmplMinus);
    JLabel lbl_SignalOffs = new JLabel("Offset");
    lbl_SignalOffs.setFont(new Font("Tahoma", 0, 12));
    GridBagConstraints gbc_lbl_SignalOffs = new GridBagConstraints();
    gbc_lbl_SignalOffs.anchor = 13;
    gbc_lbl_SignalOffs.insets = new Insets(0, 0, 5, 5);
    gbc_lbl_SignalOffs.gridx = 1;
    gbc_lbl_SignalOffs.gridy = 9;
    this.contentPanel.add(lbl_SignalOffs, gbc_lbl_SignalOffs);
    this.textField_SignalOffs = new JTextField();
    myListener(this.textField_SignalOffs, this.txtStart);
    this.textField_SignalOffs.setHorizontalAlignment(4);
    this.textField_SignalOffs.setFont(new Font("Tahoma", 0, 12));
    this.textField_SignalOffs.setText("0.0");
    GridBagConstraints gbc_textField_SignalOffs = new GridBagConstraints();
    gbc_textField_SignalOffs.insets = new Insets(0, 0, 5, 5);
    gbc_textField_SignalOffs.fill = 2;
    gbc_textField_SignalOffs.gridx = 2;
    gbc_textField_SignalOffs.gridy = 9;
    this.contentPanel.add(this.textField_SignalOffs, gbc_textField_SignalOffs);
    this.textField_SignalOffs.setColumns(10);
    this.btn_SignalOffsPlus = new Button("+");
    this.btn_SignalOffsPlus.setFont(new Font("Tahoma", 1, 12));
    GridBagConstraints gbc_btn_SignalOffsPlus = new GridBagConstraints();
    gbc_btn_SignalOffsPlus.insets = new Insets(0, 0, 5, 5);
    gbc_btn_SignalOffsPlus.gridx = 3;
    gbc_btn_SignalOffsPlus.gridy = 9;
    this.contentPanel.add(this.btn_SignalOffsPlus, gbc_btn_SignalOffsPlus);
    this.btn_SignalOffsMinus = new Button("-");
    this.btn_SignalOffsMinus.setFont(new Font("Tahoma", 1, 12));
    GridBagConstraints gbc_btn_SignalOffsMinus = new GridBagConstraints();
    gbc_btn_SignalOffsMinus.insets = new Insets(0, 0, 5, 0);
    gbc_btn_SignalOffsMinus.gridx = 4;
    gbc_btn_SignalOffsMinus.gridy = 9;
    this.contentPanel.add(this.btn_SignalOffsMinus, gbc_btn_SignalOffsMinus);
    JLabel lbl_SignalNoise = new JLabel("Noise factor");
    lbl_SignalNoise.setFont(new Font("Tahoma", 0, 12));
    GridBagConstraints gbc_lbl_SignalNoise = new GridBagConstraints();
    gbc_lbl_SignalNoise.anchor = 13;
    gbc_lbl_SignalNoise.insets = new Insets(0, 0, 5, 5);
    gbc_lbl_SignalNoise.gridx = 1;
    gbc_lbl_SignalNoise.gridy = 10;
    this.contentPanel.add(lbl_SignalNoise, gbc_lbl_SignalNoise);
    this.textField_SignalNoise = new JTextField();
    myListener(this.textField_SignalNoise, this.txtStart);
    this.textField_SignalNoise.setHorizontalAlignment(4);
    this.textField_SignalNoise.setFont(new Font("Tahoma", 0, 12));
    this.textField_SignalNoise.setText("0.0");
    GridBagConstraints gbc_textField_SignalNoise = new GridBagConstraints();
    gbc_textField_SignalNoise.insets = new Insets(0, 0, 5, 5);
    gbc_textField_SignalNoise.fill = 2;
    gbc_textField_SignalNoise.gridx = 2;
    gbc_textField_SignalNoise.gridy = 10;
    this.contentPanel.add(this.textField_SignalNoise, gbc_textField_SignalNoise);
    this.textField_SignalNoise.setColumns(10);
    this.btn_SignalNoisePlus = new Button("+");
    this.btn_SignalNoisePlus.setFont(new Font("Tahoma", 1, 12));
    this.btn_SignalNoisePlus.setActionCommand("v");
    GridBagConstraints gbc_btn_SignalNoisePlus = new GridBagConstraints();
    gbc_btn_SignalNoisePlus.insets = new Insets(0, 0, 5, 5);
    gbc_btn_SignalNoisePlus.gridx = 3;
    gbc_btn_SignalNoisePlus.gridy = 10;
    this.contentPanel.add(this.btn_SignalNoisePlus, gbc_btn_SignalNoisePlus);
    this.btn_SignalNoiseMinus = new Button("-");
    this.btn_SignalNoiseMinus.setFont(new Font("Tahoma", 1, 12));
    GridBagConstraints gbc_btn_SignalNoiseMinus = new GridBagConstraints();
    gbc_btn_SignalNoiseMinus.insets = new Insets(0, 0, 5, 0);
    gbc_btn_SignalNoiseMinus.gridx = 4;
    gbc_btn_SignalNoiseMinus.gridy = 10;
    this.contentPanel.add(this.btn_SignalNoiseMinus, gbc_btn_SignalNoiseMinus);
    JSeparator jSeparator3 = new JSeparator();
    GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
    gridBagConstraints3.gridwidth = 5;
    gridBagConstraints3.insets = new Insets(0, 0, 5, 0);
    gridBagConstraints3.gridx = 0;
    gridBagConstraints3.gridy = 11;
    this.contentPanel.add(jSeparator3, gridBagConstraints3);
    JLabel lbl_Mean = new JLabel("Mean position");
    lbl_Mean.setFont(new Font("Tahoma", 1, 12));
    GridBagConstraints gbc_lbl_Mean = new GridBagConstraints();
    gbc_lbl_Mean.gridheight = 2;
    gbc_lbl_Mean.anchor = 17;
    gbc_lbl_Mean.insets = new Insets(0, 5, 5, 5);
    gbc_lbl_Mean.gridx = 0;
    gbc_lbl_Mean.gridy = 12;
    this.contentPanel.add(lbl_Mean, gbc_lbl_Mean);
    JLabel lbl_MeanX = new JLabel("X axis (pixel)");
    lbl_MeanX.setFont(new Font("Tahoma", 0, 12));
    GridBagConstraints gbc_lbl_MeanX = new GridBagConstraints();
    gbc_lbl_MeanX.anchor = 13;
    gbc_lbl_MeanX.insets = new Insets(0, 0, 5, 5);
    gbc_lbl_MeanX.gridx = 1;
    gbc_lbl_MeanX.gridy = 12;
    this.contentPanel.add(lbl_MeanX, gbc_lbl_MeanX);
    this.textField_MeanX = new JTextField();
    myListener(this.textField_MeanX, this.txtStart);
    this.textField_MeanX.setHorizontalAlignment(4);
    this.textField_MeanX.setFont(new Font("Tahoma", 0, 12));
    this.textField_MeanX.setText("0");
    GridBagConstraints gbc_textField_MeanX = new GridBagConstraints();
    gbc_textField_MeanX.insets = new Insets(0, 0, 5, 5);
    gbc_textField_MeanX.fill = 2;
    gbc_textField_MeanX.gridx = 2;
    gbc_textField_MeanX.gridy = 12;
    this.contentPanel.add(this.textField_MeanX, gbc_textField_MeanX);
    this.textField_MeanX.setColumns(10);
    this.btn_MeanXPlus = new Button("+");
    this.btn_MeanXPlus.setFont(new Font("Tahoma", 1, 12));
    GridBagConstraints gbc_btn_MeanXPlus = new GridBagConstraints();
    gbc_btn_MeanXPlus.insets = new Insets(0, 0, 5, 5);
    gbc_btn_MeanXPlus.gridx = 3;
    gbc_btn_MeanXPlus.gridy = 12;
    this.contentPanel.add(this.btn_MeanXPlus, gbc_btn_MeanXPlus);
    this.btn_MeanXMinus = new Button("-");
    this.btn_MeanXMinus.setFont(new Font("Tahoma", 1, 12));
    GridBagConstraints gbc_btn_MeanXMinus = new GridBagConstraints();
    gbc_btn_MeanXMinus.insets = new Insets(0, 0, 5, 0);
    gbc_btn_MeanXMinus.gridx = 4;
    gbc_btn_MeanXMinus.gridy = 12;
    this.contentPanel.add(this.btn_MeanXMinus, gbc_btn_MeanXMinus);
    JLabel lbl_MeanY = new JLabel("Y-axis (pixel)");
    lbl_MeanY.setFont(new Font("Tahoma", 0, 12));
    GridBagConstraints gbc_lbl_MeanY = new GridBagConstraints();
    gbc_lbl_MeanY.anchor = 13;
    gbc_lbl_MeanY.insets = new Insets(0, 0, 5, 5);
    gbc_lbl_MeanY.gridx = 1;
    gbc_lbl_MeanY.gridy = 13;
    this.contentPanel.add(lbl_MeanY, gbc_lbl_MeanY);
    this.textField_MeanY = new JTextField();
    myListener(this.textField_MeanY, this.txtStart);
    this.textField_MeanY.setHorizontalAlignment(4);
    this.textField_MeanY.setFont(new Font("Tahoma", 0, 12));
    this.textField_MeanY.setText("0");
    GridBagConstraints gbc_textField_MeanY = new GridBagConstraints();
    gbc_textField_MeanY.insets = new Insets(0, 0, 5, 5);
    gbc_textField_MeanY.fill = 2;
    gbc_textField_MeanY.gridx = 2;
    gbc_textField_MeanY.gridy = 13;
    this.contentPanel.add(this.textField_MeanY, gbc_textField_MeanY);
    this.textField_MeanY.setColumns(10);
    this.btn_MeanYPlus = new Button("+");
    this.btn_MeanYPlus.setFont(new Font("Tahoma", 1, 12));
    GridBagConstraints gbc_btn_MeanYPlus = new GridBagConstraints();
    gbc_btn_MeanYPlus.insets = new Insets(0, 0, 5, 5);
    gbc_btn_MeanYPlus.gridx = 3;
    gbc_btn_MeanYPlus.gridy = 13;
    this.contentPanel.add(this.btn_MeanYPlus, gbc_btn_MeanYPlus);
    this.btn_MeanYMinus = new Button("-");
    this.btn_MeanYMinus.setFont(new Font("Tahoma", 1, 12));
    GridBagConstraints gbc_btn_MeanYMinus = new GridBagConstraints();
    gbc_btn_MeanYMinus.insets = new Insets(0, 0, 5, 0);
    gbc_btn_MeanYMinus.gridx = 4;
    gbc_btn_MeanYMinus.gridy = 13;
    this.contentPanel.add(this.btn_MeanYMinus, gbc_btn_MeanYMinus);
    JSeparator jSeparator2 = new JSeparator();
    GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
    gridBagConstraints2.gridwidth = 5;
    gridBagConstraints2.insets = new Insets(0, 0, 5, 0);
    gridBagConstraints2.gridx = 0;
    gridBagConstraints2.gridy = 14;
    this.contentPanel.add(jSeparator2, gridBagConstraints2);
    JLabel lbl_Sigma = new JLabel("Sigma (value)");
    lbl_Sigma.setFont(new Font("Tahoma", 1, 12));
    GridBagConstraints gbc_lbl_Sigma = new GridBagConstraints();
    gbc_lbl_Sigma.gridheight = 2;
    gbc_lbl_Sigma.anchor = 17;
    gbc_lbl_Sigma.insets = new Insets(0, 5, 5, 5);
    gbc_lbl_Sigma.gridx = 0;
    gbc_lbl_Sigma.gridy = 15;
    this.contentPanel.add(lbl_Sigma, gbc_lbl_Sigma);
    JLabel lbl_SigmaX = new JLabel("X-axis (value)");
    lbl_SigmaX.setFont(new Font("Tahoma", 0, 12));
    GridBagConstraints gbc_lbl_SigmaX = new GridBagConstraints();
    gbc_lbl_SigmaX.anchor = 13;
    gbc_lbl_SigmaX.insets = new Insets(0, 0, 5, 5);
    gbc_lbl_SigmaX.gridx = 1;
    gbc_lbl_SigmaX.gridy = 15;
    this.contentPanel.add(lbl_SigmaX, gbc_lbl_SigmaX);
    this.textField_SigmaX = new JTextField();
    myListener(this.textField_SigmaX, this.txtStart);
    this.textField_SigmaX.setHorizontalAlignment(4);
    this.textField_SigmaX.setFont(new Font("Tahoma", 0, 12));
    this.textField_SigmaX.setText("0.0");
    GridBagConstraints gbc_textField_SigmaX = new GridBagConstraints();
    gbc_textField_SigmaX.insets = new Insets(0, 0, 5, 5);
    gbc_textField_SigmaX.fill = 2;
    gbc_textField_SigmaX.gridx = 2;
    gbc_textField_SigmaX.gridy = 15;
    this.contentPanel.add(this.textField_SigmaX, gbc_textField_SigmaX);
    this.textField_SigmaX.setColumns(10);
    this.btn_SigmaXPlus = new Button("+");
    this.btn_SigmaXPlus.setFont(new Font("Tahoma", 1, 12));
    GridBagConstraints gbc_btn_SigmaXPlus = new GridBagConstraints();
    gbc_btn_SigmaXPlus.insets = new Insets(0, 0, 5, 5);
    gbc_btn_SigmaXPlus.gridx = 3;
    gbc_btn_SigmaXPlus.gridy = 15;
    this.contentPanel.add(this.btn_SigmaXPlus, gbc_btn_SigmaXPlus);
    this.btn_SigmaXMinus = new Button("-");
    this.btn_SigmaXMinus.setFont(new Font("Tahoma", 1, 12));
    GridBagConstraints gbc_btn_SigmaXMinus = new GridBagConstraints();
    gbc_btn_SigmaXMinus.insets = new Insets(0, 0, 5, 0);
    gbc_btn_SigmaXMinus.gridx = 4;
    gbc_btn_SigmaXMinus.gridy = 15;
    this.contentPanel.add(this.btn_SigmaXMinus, gbc_btn_SigmaXMinus);
    JLabel lbl_SigmaY = new JLabel("Y-axis (value)");
    lbl_SigmaY.setFont(new Font("Tahoma", 0, 12));
    GridBagConstraints gbc_lbl_SigmaY = new GridBagConstraints();
    gbc_lbl_SigmaY.anchor = 13;
    gbc_lbl_SigmaY.insets = new Insets(0, 0, 5, 5);
    gbc_lbl_SigmaY.gridx = 1;
    gbc_lbl_SigmaY.gridy = 16;
    this.contentPanel.add(lbl_SigmaY, gbc_lbl_SigmaY);
    this.textField_SigmaY = new JTextField();
    myListener(this.textField_SigmaY, this.txtStart);
    this.textField_SigmaY.setHorizontalAlignment(4);
    this.textField_SigmaY.setFont(new Font("Tahoma", 0, 12));
    this.textField_SigmaY.setText("0.0");
    GridBagConstraints gbc_textField_SigmaY = new GridBagConstraints();
    gbc_textField_SigmaY.insets = new Insets(0, 0, 5, 5);
    gbc_textField_SigmaY.fill = 2;
    gbc_textField_SigmaY.gridx = 2;
    gbc_textField_SigmaY.gridy = 16;
    this.contentPanel.add(this.textField_SigmaY, gbc_textField_SigmaY);
    this.textField_SigmaY.setColumns(10);
    this.btn_SigmaYPlus = new Button("+");
    this.btn_SigmaYPlus.setFont(new Font("Tahoma", 1, 12));
    GridBagConstraints gbc_btn_SigmaYPlus = new GridBagConstraints();
    gbc_btn_SigmaYPlus.insets = new Insets(0, 0, 5, 5);
    gbc_btn_SigmaYPlus.gridx = 3;
    gbc_btn_SigmaYPlus.gridy = 16;
    this.contentPanel.add(this.btn_SigmaYPlus, gbc_btn_SigmaYPlus);
    this.btn_SigmaYMinus = new Button("-");
    this.btn_SigmaYMinus.setFont(new Font("Tahoma", 1, 12));
    GridBagConstraints gbc_btn_SigmaYMinus = new GridBagConstraints();
    gbc_btn_SigmaYMinus.insets = new Insets(0, 0, 5, 0);
    gbc_btn_SigmaYMinus.gridx = 4;
    gbc_btn_SigmaYMinus.gridy = 16;
    this.contentPanel.add(this.btn_SigmaYMinus, gbc_btn_SigmaYMinus);
    JSeparator jSeparator1 = new JSeparator();
    GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
    gridBagConstraints1.gridwidth = 5;
    gridBagConstraints1.insets = new Insets(0, 0, 5, 0);
    gridBagConstraints1.gridx = 0;
    gridBagConstraints1.gridy = 17;
    this.contentPanel.add(jSeparator1, gridBagConstraints1);
    JLabel lbl_RotAngle = new JLabel("Rotation angle");
    lbl_RotAngle.setFont(new Font("Tahoma", 1, 12));
    GridBagConstraints gbc_lbl_RotAngle = new GridBagConstraints();
    gbc_lbl_RotAngle.gridheight = 2;
    gbc_lbl_RotAngle.anchor = 17;
    gbc_lbl_RotAngle.insets = new Insets(0, 5, 5, 5);
    gbc_lbl_RotAngle.gridx = 0;
    gbc_lbl_RotAngle.gridy = 18;
    this.contentPanel.add(lbl_RotAngle, gbc_lbl_RotAngle);
    JLabel lbl_RotAngleRad = new JLabel("Radians");
    lbl_RotAngleRad.setFont(new Font("Tahoma", 0, 12));
    GridBagConstraints gbc_lbl_RotAngleRad = new GridBagConstraints();
    gbc_lbl_RotAngleRad.anchor = 13;
    gbc_lbl_RotAngleRad.insets = new Insets(0, 0, 5, 5);
    gbc_lbl_RotAngleRad.gridx = 1;
    gbc_lbl_RotAngleRad.gridy = 18;
    this.contentPanel.add(lbl_RotAngleRad, gbc_lbl_RotAngleRad);
    this.textField_RotAngleRad = new JTextField();
    myListener(this.textField_RotAngleRad, this.txtStart);
    this.textField_RotAngleRad.setHorizontalAlignment(4);
    this.textField_RotAngleRad.setFont(new Font("Tahoma", 0, 12));
    this.textField_RotAngleRad.setText("0.0");
    GridBagConstraints gbc_textField_RotAngleRad = new GridBagConstraints();
    gbc_textField_RotAngleRad.insets = new Insets(0, 0, 5, 5);
    gbc_textField_RotAngleRad.fill = 2;
    gbc_textField_RotAngleRad.gridx = 2;
    gbc_textField_RotAngleRad.gridy = 18;
    this.contentPanel.add(this.textField_RotAngleRad, gbc_textField_RotAngleRad);
    this.textField_RotAngleRad.setColumns(10);
    this.btn_RotAngleRadPlus = new Button("+");
    this.btn_RotAngleRadPlus.setFont(new Font("Tahoma", 1, 12));
    GridBagConstraints gbc_btn_RotAngleRadPlus = new GridBagConstraints();
    gbc_btn_RotAngleRadPlus.insets = new Insets(0, 0, 5, 5);
    gbc_btn_RotAngleRadPlus.gridx = 3;
    gbc_btn_RotAngleRadPlus.gridy = 18;
    this.contentPanel.add(this.btn_RotAngleRadPlus, gbc_btn_RotAngleRadPlus);
    this.btn_RotAngleRadMinus = new Button("-");
    this.btn_RotAngleRadMinus.setFont(new Font("Tahoma", 1, 12));
    GridBagConstraints gbc_btn_RotAngleRadMinus = new GridBagConstraints();
    gbc_btn_RotAngleRadMinus.insets = new Insets(0, 0, 5, 0);
    gbc_btn_RotAngleRadMinus.gridx = 4;
    gbc_btn_RotAngleRadMinus.gridy = 18;
    this.contentPanel.add(this.btn_RotAngleRadMinus, gbc_btn_RotAngleRadMinus);
    JLabel lbl_RotAngleDeg = new JLabel("Degrees");
    lbl_RotAngleDeg.setEnabled(false);
    lbl_RotAngleDeg.setFont(new Font("Tahoma", 0, 12));
    GridBagConstraints gbc_lbl_RotAngleDeg = new GridBagConstraints();
    gbc_lbl_RotAngleDeg.anchor = 13;
    gbc_lbl_RotAngleDeg.insets = new Insets(0, 0, 5, 5);
    gbc_lbl_RotAngleDeg.gridx = 1;
    gbc_lbl_RotAngleDeg.gridy = 19;
    this.contentPanel.add(lbl_RotAngleDeg, gbc_lbl_RotAngleDeg);
    this.textField_RotAngleDeg = new JTextField();
    this.textField_RotAngleDeg.setEditable(false);
    this.textField_RotAngleDeg.setEnabled(false);
    myListener(this.textField_RotAngleDeg, this.txtStart);
    this.textField_RotAngleDeg.setHorizontalAlignment(4);
    this.textField_RotAngleDeg.setFont(new Font("Tahoma", 0, 12));
    this.textField_RotAngleDeg.setText("0.0");
    GridBagConstraints gbc_textField_RotAngleDeg = new GridBagConstraints();
    gbc_textField_RotAngleDeg.insets = new Insets(0, 0, 5, 5);
    gbc_textField_RotAngleDeg.fill = 2;
    gbc_textField_RotAngleDeg.gridx = 2;
    gbc_textField_RotAngleDeg.gridy = 19;
    this.contentPanel.add(this.textField_RotAngleDeg, gbc_textField_RotAngleDeg);
    this.textField_RotAngleDeg.setColumns(10);
    this.btn_RotAngleDegPlus = new Button("+");
    this.btn_RotAngleDegPlus.setEnabled(false);
    this.btn_RotAngleDegPlus.setFont(new Font("Tahoma", 1, 12));
    GridBagConstraints gbc_btn_RotAngleDegPlus = new GridBagConstraints();
    gbc_btn_RotAngleDegPlus.insets = new Insets(0, 0, 5, 5);
    gbc_btn_RotAngleDegPlus.gridx = 3;
    gbc_btn_RotAngleDegPlus.gridy = 19;
    this.contentPanel.add(this.btn_RotAngleDegPlus, gbc_btn_RotAngleDegPlus);
    this.btn_RotAngleDegMinus = new Button("-");
    this.btn_RotAngleDegMinus.setEnabled(false);
    this.btn_RotAngleDegMinus.setFont(new Font("Tahoma", 1, 12));
    GridBagConstraints gbc_btn_RotAngleDegMinus = new GridBagConstraints();
    gbc_btn_RotAngleDegMinus.insets = new Insets(0, 0, 5, 0);
    gbc_btn_RotAngleDegMinus.gridx = 4;
    gbc_btn_RotAngleDegMinus.gridy = 19;
    this.contentPanel.add(this.btn_RotAngleDegMinus, gbc_btn_RotAngleDegMinus);
    JSeparator separator = new JSeparator();
    GridBagConstraints gbc_separator = new GridBagConstraints();
    gbc_separator.gridwidth = 5;
    gbc_separator.insets = new Insets(0, 0, 5, 0);
    gbc_separator.gridx = 0;
    gbc_separator.gridy = 20;
    this.contentPanel.add(separator, gbc_separator);
    this.separatorBottom = new JSeparator();
    GridBagConstraints gbc_separatorBottom = new GridBagConstraints();
    gbc_separatorBottom.fill = 2;
    gbc_separatorBottom.gridwidth = 5;
    gbc_separatorBottom.insets = new Insets(0, 0, 0, 5);
    gbc_separatorBottom.gridx = 0;
    gbc_separatorBottom.gridy = 22;
    this.contentPanel.add(this.separatorBottom, gbc_separatorBottom);
    JPanel buttonPane = new JPanel();
    buttonPane.setLayout(new FlowLayout(2));
    this.dialog.getContentPane().add(buttonPane, "South");
    this.btn_Accept = new JButton("Accept");
    this.btn_Accept.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            TwoDGaussDialogGui.this.btn_Run.setEnabled(TwoDGaussDialogGui.this.checkParameters());
          }
        });
    this.btn_Accept.setFont(new Font("Tahoma", 1, 12));
    buttonPane.add(this.btn_Accept);
    this.btn_Run = new JButton("Run");
    this.btn_Run.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            TwoDGaussDialogGui.this.dialog.dispose();
            TwoDGaussDialogGui.this.doRun = true;
          }
        });
    this.btn_Run.setEnabled(false);
    this.btn_Run.setFont(new Font("Tahoma", 1, 12));
    this.btn_Run.setActionCommand("OK");
    buttonPane.add(this.btn_Run);
    getRootPane().setDefaultButton(this.btn_Run);
    this.btn_Cancel = new JButton("Cancel");
    this.btn_Cancel.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            TwoDGaussDialogGui.this.dialog.dispose();
            TwoDGaussDialogGui.this.doRun = false;
          }
        });
    this.btn_Cancel.setFont(new Font("Tahoma", 1, 12));
    this.btn_Cancel.setActionCommand("Cancel");
    buttonPane.add(this.btn_Cancel);
  }
  
  private void messageInfo(String strMessage) {
    this.txtStart.setText(strMessage);
    this.txtStart.setForeground(Color.BLACK);
  }
  
  private void messageError(String strMessage) {
    this.txtStart.setText("Error> " + strMessage);
    this.txtStart.setForeground(Color.RED);
  }
  
  private boolean checkParameters() {
    if (checkIntegerParam(this.textField_ImSizeX, this.rangeSizeMin, this.rangeSizeMax)) {
      this.imSizeXY[0] = Integer.parseInt(this.textField_ImSizeX.getText());
    } else {
      return false;
    } 
    if (checkIntegerParam(this.textField_ImSizeY, this.rangeSizeMin, this.rangeSizeMax)) {
      this.imSizeXY[1] = Integer.parseInt(this.textField_ImSizeY.getText());
    } else {
      return false;
    } 
    if (checkIntegerParam(this.textField_MeanX, this.rangePosMin, this.imSizeXY[0])) {
      this.meanPosXY[0] = Integer.parseInt(this.textField_MeanX.getText());
    } else {
      return false;
    } 
    if (checkIntegerParam(this.textField_MeanY, this.rangePosMin, this.imSizeXY[1])) {
      this.meanPosXY[1] = Integer.parseInt(this.textField_MeanY.getText());
    } else {
      return false;
    } 
    if (checkDoubleParam(this.textField_SignalAmpl, this.rangeAmplMin, this.rangeAmplMax)) {
      this.paramsSignal[0] = Double.parseDouble(this.textField_SignalAmpl.getText());
    } else {
      return false;
    } 
    if (checkDoubleParam(this.textField_SignalOffs, this.rangeAmplMin / 10.0D, this.paramsSignal[0] / 5.0D)) {
      this.paramsSignal[1] = Double.parseDouble(this.textField_SignalOffs.getText());
    } else {
      return false;
    } 
    if (checkDoubleParam(this.textField_SignalNoise, 0.0D, this.paramsSignal[0] / 2.0D)) {
      this.paramsSignal[2] = Double.parseDouble(this.textField_SignalNoise.getText());
    } else {
      return false;
    } 
    if (checkDoubleParam(this.textField_SigmaX, this.rangeSizeMin / 10.0D, this.rangeSizeMax / 2.0D)) {
      this.paramsSigmaXY[0] = Double.parseDouble(this.textField_SigmaX.getText());
    } else {
      return false;
    } 
    if (checkDoubleParam(this.textField_SigmaY, this.rangeSizeMin / 10.0D, this.rangeSizeMax / 2.0D)) {
      this.paramsSigmaXY[1] = Double.parseDouble(this.textField_SigmaY.getText());
    } else {
      return false;
    } 
    return true;
  }
  
  private boolean checkIntegerParam(JTextField textField, int rangeMin, int rangeMax) {
    boolean isCheckOk = true;
    String strMessage = "";
    try {
      int value = Integer.parseInt(textField.getText());
      if (value < rangeMin || value > rangeMax) {
        isCheckOk = false;
        strMessage = "Value out of the allowed range [" + rangeMin + "," + rangeMax + "]";
      } 
    } catch (Exception e) {
      isCheckOk = false;
      strMessage = "Wrong field value, expected INTEGER";
    } 
    if (!isCheckOk) {
      messageError(strMessage);
      textField.setForeground(Color.RED);
    } 
    return isCheckOk;
  }
  
  private boolean checkDoubleParam(JTextField textField, double rangeMin, double rangeMax) {
    boolean isCheckOk = true;
    String strMessage = "";
    try {
      double value = Double.parseDouble(textField.getText());
      if (value < rangeMin || value > rangeMax) {
        isCheckOk = false;
        strMessage = "Value out of the allowed range [" + rangeMin + "," + rangeMax + "]";
      } 
    } catch (Exception e) {
      isCheckOk = false;
      strMessage = "Wrong field value; expected FLOAT";
    } 
    if (!isCheckOk) {
      messageError(strMessage);
      textField.setForeground(Color.RED);
    } 
    return isCheckOk;
  }
  
  public int getExampleModeCode() {
    return this.exampleModeCode;
  }
  
  public int[] getImSizeXY() {
    return this.imSizeXY;
  }
  
  public int[] getMeanPosXY() {
    return this.meanPosXY;
  }
  
  public double[] getParamsSigmaXY() {
    return this.paramsSigmaXY;
  }
  
  public double[] getParamsSignal() {
    return this.paramsSignal;
  }
  
  public double[] getParamsRotAngle() {
    return this.paramsRotAngle;
  }
  
  public boolean getStatus() {
    return this.doRun;
  }
  
  private void resetParamsToExample(int numCode) {
    int[][] imSizeXYSet = { { 111, 111 }, { 111, 111 }, { 111, 111 }, new int[2] };
    int[][] meanPosXYSet = { { 40, 60 }, { 65, 55 }, { 55, 70 }, new int[2] };
    double[][] paramsSignalSet = { { 50.0D, 10.0D, 0.0D }, { 200.0D, 10.0D, 0.0D }, { 50.0D, 10.0D, 0.0D }, { 0.0D, 0.0D, 0.0D } };
    double[][] paramsSigmaSet = { { 10.0D, 20.0D }, { 1.0D, 1.0D }, { 9.0D, 1.0D }, { 0.0D, 0.0D } };
    double[][] paramsRotangleSet = { { -0.5D, 0.0D }, { 0.0D, 0.0D }, { 0.5D, 0.0D }, { 0.0D, 0.0D } };
    this.imSizeXY = imSizeXYSet[numCode];
    this.meanPosXY = meanPosXYSet[numCode];
    this.paramsSignal = paramsSignalSet[numCode];
    this.paramsSigmaXY = paramsSigmaSet[numCode];
    this.paramsRotAngle = paramsRotangleSet[numCode];
    this.textField_ImSizeX.setText(this.imSizeXY[0]+"");
    this.textField_ImSizeX.setForeground(Color.BLACK);
    this.textField_ImSizeY.setText(this.imSizeXY[1]+"");
    this.textField_ImSizeY.setForeground(Color.BLACK);
    this.textField_MeanX.setText(this.meanPosXY[0]+"");
    this.textField_MeanX.setForeground(Color.BLACK);
    this.textField_MeanY.setText(this.meanPosXY[1]+"");
    this.textField_MeanY.setForeground(Color.BLACK);
    this.textField_SigmaX.setText(this.paramsSigmaXY[0]+"");
    this.textField_SigmaX.setForeground(Color.BLACK);
    this.textField_SigmaY.setText(this.paramsSigmaXY[1]+"");
    this.textField_SigmaY.setForeground(Color.BLACK);
    this.textField_SignalAmpl.setText(this.paramsSignal[0]+"");
    this.textField_SignalAmpl.setForeground(Color.BLACK);
    this.textField_SignalOffs.setText(this.paramsSignal[1]+"");
    this.textField_SignalOffs.setForeground(Color.BLACK);
    this.textField_SignalNoise.setText(this.paramsSignal[2]+"");
    this.textField_SignalNoise.setForeground(Color.BLACK);
    this.textField_RotAngleRad.setText(this.paramsRotAngle[0]+"");
    this.textField_RotAngleRad.setForeground(Color.BLACK);
    this.textField_RotAngleDeg.setText(this.paramsRotAngle[1]+"");
    this.textField_RotAngleDeg.setForeground(Color.BLACK);
    if (numCode < 3)
      textFieldsEnable(false); 
    this.exampleModeCode = numCode;
  }
  
  private void textFieldsEnable(boolean isEnable) {
    this.textField_ImSizeX.setEnabled(isEnable);
    this.textField_ImSizeY.setEnabled(isEnable);
    this.textField_MeanX.setEnabled(isEnable);
    this.textField_MeanY.setEnabled(isEnable);
    this.textField_SigmaX.setEnabled(isEnable);
    this.textField_SigmaY.setEnabled(isEnable);
    this.textField_SignalAmpl.setEnabled(isEnable);
    this.textField_SignalOffs.setEnabled(isEnable);
    this.textField_SignalNoise.setEnabled(isEnable);
    this.textField_RotAngleRad.setEnabled(isEnable);
    this.btn_ImSizeXPlus.setEnabled(isEnable);
    this.btn_ImSizeXMinus.setEnabled(isEnable);
    this.btn_ImSizeYPlus.setEnabled(isEnable);
    this.btn_ImSizeYMinus.setEnabled(isEnable);
    this.btn_MeanXPlus.setEnabled(isEnable);
    this.btn_MeanXMinus.setEnabled(isEnable);
    this.btn_MeanYPlus.setEnabled(isEnable);
    this.btn_MeanYMinus.setEnabled(isEnable);
    this.btn_SignalAmplPlus.setEnabled(isEnable);
    this.btn_SignalAmplMinus.setEnabled(isEnable);
    this.btn_SignalOffsPlus.setEnabled(isEnable);
    this.btn_SignalOffsMinus.setEnabled(isEnable);
    this.btn_SignalNoisePlus.setEnabled(isEnable);
    this.btn_SignalNoiseMinus.setEnabled(isEnable);
    this.btn_SigmaXPlus.setEnabled(isEnable);
    this.btn_SigmaXMinus.setEnabled(isEnable);
    this.btn_SigmaYPlus.setEnabled(isEnable);
    this.btn_SigmaYMinus.setEnabled(isEnable);
    this.btn_RotAngleRadPlus.setEnabled(isEnable);
    this.btn_RotAngleRadMinus.setEnabled(isEnable);
  }
  
  private void myListener(final JTextField textField, final JTextField message) {
    textField.getDocument().addDocumentListener(new DocumentListener() {
          private void warn() {
            message.setText("");
            textField.setForeground(Color.BLACK);
          }
          
          public void changedUpdate(DocumentEvent e) {
            warn();
          }
          
          public void removeUpdate(DocumentEvent e) {
            warn();
          }
          
          public void insertUpdate(DocumentEvent e) {
            warn();
          }
        });
  }
}


/* Location:              /home/nico/Downloads/V3/DetectionStandalone.jar!/noa/ibv/fds/tools/TwoDGaussDialogGui.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */