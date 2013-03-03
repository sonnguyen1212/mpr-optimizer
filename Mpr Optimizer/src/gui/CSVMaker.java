package gui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
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

public class CSVMaker {

	private JFrame frmCsvMaker;
	private ArrayList<File> mprFiles;
	private JTextField textField;
	private DefaultListModel<String> listModel;
	private JList<String> list;

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
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
		mprFiles = new ArrayList<>();
		frmCsvMaker = new JFrame();
		frmCsvMaker.setTitle("CSV Maker");
		frmCsvMaker.setBounds(new Rectangle(200, 200, 400, 300));
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
		btnSelectFiles.setBounds(80, 190, 204, 60);
		frmCsvMaker.getContentPane().add(btnSelectFiles);

		textField = new JTextField();
		textField.setBounds(80, 31, 204, 20);
		frmCsvMaker.getContentPane().add(textField);
		textField.setColumns(10);

		JButton btnBrowseCSV = new JButton("Browse");
		btnBrowseCSV.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser("C:\\Users\\La bla bla\\Dropbox");
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				FileNameExtensionFilter mprExtensionFilter = new FileNameExtensionFilter("CSV File (*.csv)", "csv");
				fileChooser.setFileFilter(mprExtensionFilter);
				int result = fileChooser.showSaveDialog(frmCsvMaker);
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
				JFileChooser fileChooser = new JFileChooser("C:\\Users\\La bla bla\\Dropbox");
				fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fileChooser.setMultiSelectionEnabled(true);
				FileNameExtensionFilter mprExtensionFilter = new FileNameExtensionFilter("MPR File (*.mpr)", "mpr");
				fileChooser.setFileFilter(mprExtensionFilter);
				int result = fileChooser.showOpenDialog(frmCsvMaker);
				if (result == JFileChooser.APPROVE_OPTION) {
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
				writer.print(","); // Rotate
				writer.print(point.getZ() + ","); // Material
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
}
