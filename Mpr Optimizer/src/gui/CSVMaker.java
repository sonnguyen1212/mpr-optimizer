package gui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

import mpr.NestingCreator;
import mpr.Point3D;
import javax.swing.JList;
import javax.swing.border.BevelBorder;
import java.awt.Rectangle;
import javax.swing.JCheckBox;

public class CSVMaker {

	private static final String LOG_FILE = "\\log.txt";
	private static File logFile = null;
	private JFrame frmCsvMaker;
	private ArrayList<File> mprFiles;
	private JTextField textField;
	private DefaultListModel<String> listModel;
	private JList<String> list;
	private File parentDir = null;
	private File homeFolder;
	private JTextField materialName;
	private JCheckBox chckbxRotateAllowed;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CSVMaker window = new CSVMaker();
					window.frmCsvMaker.setVisible(true);
				} catch (Exception e) {
					CSVMaker.writeException(e);
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public CSVMaker() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
			writeException(e1);
		} catch (InstantiationException e1) {
			e1.printStackTrace();
			writeException(e1);
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
			writeException(e1);
		} catch (UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
			writeException(e1);
		}
		String path = CSVMaker.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String decodedPath = null;

		try {
			decodedPath = URLDecoder.decode(path, "UTF-8");
			int lastOccurenceSlash = decodedPath.lastIndexOf("/");
			decodedPath = decodedPath.substring(0, lastOccurenceSlash + 1);
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
			writeException(e2);
		}
		homeFolder = new File(decodedPath);
		logFile = new File(homeFolder.getAbsolutePath() + LOG_FILE);
		mprFiles = new ArrayList<>();
		frmCsvMaker = new JFrame();
		frmCsvMaker.setTitle("CSV Maker");
		frmCsvMaker.setBounds(new Rectangle(200, 200, 400, 400));
		frmCsvMaker.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmCsvMaker.getContentPane().setLayout(null);

		JButton btnSelectFiles = new JButton("Go");
		btnSelectFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (textField.getText().matches("|\\s+")) {
					JOptionPane.showMessageDialog(frmCsvMaker, "Please enter output file.", "No output file",
							JOptionPane.INFORMATION_MESSAGE);
					return;
				}

				processCSV();
				JOptionPane.showMessageDialog(frmCsvMaker, "Processing Done.", "Done", JOptionPane.PLAIN_MESSAGE);
			}
		});
		btnSelectFiles.setBounds(80, 263, 204, 60);
		frmCsvMaker.getContentPane().add(btnSelectFiles);

		textField = new JTextField();
		textField.setBounds(80, 31, 204, 20);
		frmCsvMaker.getContentPane().add(textField);
		textField.setColumns(10);

		JButton btnBrowseCSV = new JButton("Browse");
		btnBrowseCSV.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = null;
				if(parentDir != null) {
					fileChooser = new JFileChooser(parentDir);
				} else {
					fileChooser = new JFileChooser();
				}
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				FileNameExtensionFilter mprExtensionFilter = new FileNameExtensionFilter("CSV File (*.csv)", "csv");
				fileChooser.setFileFilter(mprExtensionFilter);
				int result = fileChooser.showSaveDialog(frmCsvMaker);
				parentDir = fileChooser.getCurrentDirectory();
				if (result == JFileChooser.APPROVE_OPTION) {
					if (fileChooser.getSelectedFile().getAbsolutePath().endsWith(".csv")) {
						textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
					} else {
						textField.setText(fileChooser.getSelectedFile().getAbsolutePath() + ".csv");
					}
				}
			}
		});
		btnBrowseCSV.setBounds(294, 30, 80, 23);
		frmCsvMaker.getContentPane().add(btnBrowseCSV);

		JLabel lblOutputFile = new JLabel("Output File:");
		lblOutputFile.setBounds(10, 34, 57, 14);
		frmCsvMaker.getContentPane().add(lblOutputFile);

		listModel = new DefaultListModel<>();
		list = new JList<>(listModel);
		list.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		list.setBounds(80, 75, 204, 92);
		frmCsvMaker.getContentPane().add(list);

		JLabel lblNewLabel = new JLabel("MPR Files:");
		lblNewLabel.setBounds(10, 78, 57, 14);
		frmCsvMaker.getContentPane().add(lblNewLabel);

		JButton btnBrowseMPR = new JButton("Add");
		btnBrowseMPR.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = null;
				if (parentDir != null) {
					fileChooser = new JFileChooser(parentDir);
				} else {
					fileChooser = new JFileChooser();
				}
				fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fileChooser.setMultiSelectionEnabled(true);
				FileNameExtensionFilter mprExtensionFilter = new FileNameExtensionFilter("MPR File (*.mpr)", "mpr");
				fileChooser.setFileFilter(mprExtensionFilter);
				int result = fileChooser.showOpenDialog(frmCsvMaker);
				if (result == JFileChooser.APPROVE_OPTION) {
					parentDir = fileChooser.getCurrentDirectory();
					File[] selectedFiles = fileChooser.getSelectedFiles();
					if (selectedFiles[0].isDirectory()) {
						getMprFiles(selectedFiles[0]);
					} else {
						for (File file : selectedFiles) {
							mprFiles.add(file);
							listModel.addElement(file.getName());
						}
					}
				}
			}
		});
		btnBrowseMPR.setBounds(294, 77, 80, 23);
		frmCsvMaker.getContentPane().add(btnBrowseMPR);

		JButton btnDelete = new JButton("Delete");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int index = list.getSelectedIndex();
				if (index > -1) {
					listModel.remove(index);
					mprFiles.remove(index);
				}
			}
		});
		btnDelete.setBounds(294, 111, 80, 23);
		frmCsvMaker.getContentPane().add(btnDelete);

		chckbxRotateAllowed = new JCheckBox("Rotate Allowed?");
		chckbxRotateAllowed.setBounds(10, 186, 126, 23);
		frmCsvMaker.getContentPane().add(chckbxRotateAllowed);

		JLabel lblMaterialName = new JLabel("Material Name");
		lblMaterialName.setBounds(10, 216, 80, 23);
		frmCsvMaker.getContentPane().add(lblMaterialName);

		materialName = new JTextField();
		materialName.setBounds(80, 217, 204, 20);
		frmCsvMaker.getContentPane().add(materialName);
		materialName.setColumns(10);
	}

	protected void processCSV() {
		File csvFile = new File(textField.getText());
		try {
			if (!csvFile.createNewFile()) {
				int ret = JOptionPane.showConfirmDialog(frmCsvMaker, "File exists.\nOverride?", "File exists.",
						JOptionPane.YES_NO_CANCEL_OPTION);
				if (ret == JOptionPane.OK_OPTION) {
					if (!csvFile.delete() || !csvFile.createNewFile()) {
						JOptionPane.showMessageDialog(frmCsvMaker, "Could not override file", "Error",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
				} else {
					return;
				}
			}
			PrintWriter writer = new PrintWriter(new BufferedOutputStream(new FileOutputStream(csvFile, true)));
			for (File file : mprFiles) {
				String fileName = file.getName();
				if (fileName.toLowerCase().endsWith(".mpr")) {
					int extensionIndex = fileName.toLowerCase().lastIndexOf(".mpr");
					fileName = fileName.substring(0, extensionIndex);
				}
				writer.print(fileName + ","); // Description
				Point3D point = NestingCreator.readMprAndFileDims(file);
				writer.print(point.getX() + ","); // Length
				writer.print(point.getY() + ","); // Width
				writer.print("1,"); // Qty
				
				if (chckbxRotateAllowed.isSelected())  // Rotate
					writer.print("1,");
				else
					writer.print(","); 

				if (materialName.getText().length()==0) // Material
					writer.print(Double.parseDouble(point.getZ()) + ","); 
				else
					writer.print(materialName.getText() + ","); 

				writer.print(fileName + ","); // PartCode
				writer.print(","); // Top
				writer.print(","); // Left
				writer.print(","); // Bottom
				writer.print(","); // Right
				writer.print("\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			writeException(e);
		}
	}

	private void getMprFiles(File sourceDir) {
		File[] fileList = sourceDir.listFiles();
		for (File file : fileList) {
			if (file.isDirectory()) {
				getMprFiles(file);
			}
			if (file.getAbsolutePath().endsWith(".mpr")) {
				mprFiles.add(file);
				listModel.addElement(file.getName());

			}
		}
	}

	public static void writeException(Exception e) {
		FileWriter fw = null;
		try {

			fw = new FileWriter(logFile, true);
			PrintWriter pw = new PrintWriter(fw);
			e.printStackTrace(pw);
			if (pw != null) {
				pw.close();
			}
			JOptionPane
			.showMessageDialog(null,
					"An error occured.\nLog has been saved to " + logFile.getAbsolutePath()
					+ "\nPlease e-mail the file to us for maintance.", "Error!",
					JOptionPane.ERROR_MESSAGE);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}
}
