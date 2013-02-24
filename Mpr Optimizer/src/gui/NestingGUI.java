package gui;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

import mpr.NestingCreator;

import serial.License;
import serial.LocalVerifier;
import serial.RemoteVerifier;
import javax.swing.JProgressBar;

public class NestingGUI {

	private static final String LICENSE_FILE = "\\License\\license.data";

	private JFrame frame;
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenu mnAbout;
	private JMenuItem mntmNew;
	private JMenuItem mntmExit;
	private JMenuItem mntmGenerateNesting;
	private JLabel lblXmlFile;
	private JLabel lblWoodwopFilesDirectory;
	private JButton btnGenerateNesting;
	private JTextField xmlFilePath;
	private JTextField mprPath;
	private JButton btnXmlBrowse;
	private JButton btnMprBrowse;
	private JTextArea txtrStatusBar;
	private File homeFolder;
	private boolean isXMLFileSelected;
	private boolean isMPRDestDirSelected;
	private JProgressBar progressBar;
	private static LocalVerifier localVerifier;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					NestingGUI window = new NestingGUI();
					window.frame.setVisible(true);
					// window.remoteVerifyLicense();
					window.localVerifyLicense();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public NestingGUI() {
		initialize();
	}

	private void localVerifyLicense() {
		localVerifier = new LocalVerifier(homeFolder);
		File license = new File(homeFolder.getAbsolutePath() + LICENSE_FILE);
		String serial = "";
		if (!license.exists()) {
			try {
				final JTextField macField = new JTextField();
				JButton copyButton = new JButton("Copy Address To Clipboard");
				copyButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						StringSelection clipBoard = new StringSelection(
								macField.getText());
						Toolkit.getDefaultToolkit().getSystemClipboard()
								.setContents(clipBoard, null);
					}
				});
				macField.setText(LocalVerifier.getCurrentMac());
				macField.setEnabled(false);
				final JTextField serialField = new JTextField();
				final JComponent[] inputs = new JComponent[] {
						new JLabel("Serial not found."),
						new JLabel(
								"Please send an e-mail with the following address:"),
						macField, copyButton, new JLabel("Enter your serial:"),
						serialField };

				JOptionPane.showMessageDialog(frame, inputs,
						"Serial Not Found", JOptionPane.WARNING_MESSAGE);
				serial = serialField.getText();
			} catch (HeadlessException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			localVerifier.createLicense(serial);
			int verifierResult = localVerifier.verify();
			if (verifierResult != LocalVerifier.LICENSE_MATCH) {
				if (verifierResult == LocalVerifier.LICENSE_KEYS_DONT_MATCH) {
					JOptionPane.showMessageDialog(null, "Keys don't match",
							"Error!", JOptionPane.ERROR_MESSAGE);
				} else if (verifierResult == LocalVerifier.LICENSE_EXPIRED) {
					JOptionPane.showMessageDialog(null, "License Expired",
							"Error!", JOptionPane.ERROR_MESSAGE);
				}
				System.exit(-1);
			}
		} else {
			int verifierResult = localVerifier.verify();
			if (verifierResult != LocalVerifier.LICENSE_MATCH) {
				if (verifierResult == LocalVerifier.LICENSE_KEYS_DONT_MATCH) {
					JOptionPane.showMessageDialog(null, "Keys don't match",
							"Error!", JOptionPane.ERROR_MESSAGE);
				} else if (verifierResult == LocalVerifier.LICENSE_EXPIRED) {
					JOptionPane.showMessageDialog(null, "License Expired",
							"Error!", JOptionPane.ERROR_MESSAGE);
				}
				System.exit(-1);
			}
		}
	}

	@SuppressWarnings("unused")
	private void remoteVerifyLicense() {
		try {
			RemoteVerifier remoteVerifier = new RemoteVerifier();
			File license = new File(LICENSE_FILE);
			String serial = "";
			if (!license.exists()) {
				serial = JOptionPane.showInputDialog(frame,
						"Serial not found.\nPlease enter serial number:",
						"Serial Not Found", JOptionPane.WARNING_MESSAGE);
				if (remoteVerifier.verify(serial) != 1) {
					JOptionPane.showMessageDialog(null, "Keys don't match",
							"Error!", JOptionPane.ERROR_MESSAGE);
					System.exit(-1);
				} else {
					License serialObject = new License(serial);
					ObjectOutputStream oos = new ObjectOutputStream(
							new FileOutputStream(license));
					oos.writeObject(serialObject);
					oos.close();
				}
			} else {
				ObjectInputStream ois = new ObjectInputStream(
						new FileInputStream(license));
				License serialObject = (License) ois.readObject();
				ois.close();
				if (remoteVerifier.verify(serialObject.getSerial()) != 1) {
					JOptionPane.showMessageDialog(null, "Keys don't match",
							"Error!", JOptionPane.ERROR_MESSAGE);
					System.exit(-1);
				}

			}

		} catch (UnknownHostException e1) {
			printErrorMessage("Couldn't resolve MAC Address");
			e1.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void printErrorMessage(String message) {
		JOptionPane.showMessageDialog(frame, message);
	}

	/**
	 * Set default values.
	 */
	private void initializeFields() {
		xmlFilePath.setText("");
		mprPath.setText("");
		btnGenerateNesting.setEnabled(false);
		txtrStatusBar.setText("Status:");
		progressBar.setValue(0);
		frame.repaint();
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
		String path = NestingGUI.class.getProtectionDomain().getCodeSource()
				.getLocation().getPath();
		String decodedPath = null;

		try {
			decodedPath = URLDecoder.decode(path, "UTF-8");
			int lastOccurenceSlash = decodedPath.lastIndexOf("/");
			decodedPath = decodedPath.substring(0, lastOccurenceSlash + 1);
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}
		homeFolder = new File(decodedPath);
		isMPRDestDirSelected = false;
		isXMLFileSelected = false;
		frame = new JFrame("Nesting MPR Generator");
		frame.setBounds(100, 100, 600, 500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Menu Bar:
		menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		mnFile = new JMenu("File");
		menuBar.add(mnFile);

		mntmNew = new JMenuItem("New");
		mntmNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				initializeFields();
			}
		});
		mnFile.add(mntmNew);

		mntmGenerateNesting = new JMenuItem("Generate Nesting");
		mnFile.add(mntmGenerateNesting);

		mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		mnFile.add(mntmExit);

		mnAbout = new JMenu("About");
		menuBar.add(mnAbout);
		frame.getContentPane().setLayout(null);

		// Gui
		lblXmlFile = new JLabel("XML File");
		lblXmlFile.setFont(new Font("Lucida Grande", Font.BOLD, 18));
		lblXmlFile.setBounds(20, 37, 87, 29);
		frame.getContentPane().add(lblXmlFile);

		lblWoodwopFilesDirectory = new JLabel("WoodWop Files:");
		lblWoodwopFilesDirectory.setFont(new Font("Lucida Grande", Font.BOLD,
				18));
		lblWoodwopFilesDirectory.setBounds(20, 89, 156, 29);
		frame.getContentPane().add(lblWoodwopFilesDirectory);

		btnGenerateNesting = new JButton("Generate Nesting!");
		btnGenerateNesting.setEnabled(false);
		btnGenerateNesting.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> errorMessages = new ArrayList<>();
				NestingCreator nestingCreator = new NestingCreator(xmlFilePath
						.getText(), mprPath.getText(), errorMessages,
						progressBar);
				try {
					txtrStatusBar.setText("Status: Processing..");
					nestingCreator.createLayoutMprs();
				} catch (IOException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(frame,
							"Unknown error occured, please contact support");
					// write error log!!!!!!!!!
				}
				if (errorMessages.size() == 0) {
					txtrStatusBar.setText("All Files Created Succesfully.");
					JOptionPane.showMessageDialog(frame,
							"All Files Created Succesfully.");
				} else {
					String resultString = "";
					for (String currentLine : errorMessages) {
						resultString = resultString.concat(currentLine + "\n");
					}
					txtrStatusBar.setText(resultString);
					JOptionPane.showMessageDialog(frame,
							"Operation With Errors, Please Check Status Log");
				}

			}
		});
		btnGenerateNesting.setBounds(246, 141, 139, 29);
		frame.getContentPane().add(btnGenerateNesting);

		xmlFilePath = new JTextField();
		xmlFilePath.setEditable(false);
		xmlFilePath.setBounds(183, 39, 226, 28);
		frame.getContentPane().add(xmlFilePath);
		xmlFilePath.setColumns(10);

		mprPath = new JTextField();
		mprPath.setEditable(false);
		mprPath.setColumns(10);
		mprPath.setBounds(183, 91, 226, 28);
		frame.getContentPane().add(mprPath);

		btnXmlBrowse = new JButton("Browse");
		btnXmlBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser(homeFolder);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				FileNameExtensionFilter xmlExtensionFilter = new FileNameExtensionFilter(
						"XML File (*.xml)", "xml");
				fileChooser.setFileFilter(xmlExtensionFilter);
				fileChooser.setDialogTitle("Choose your XML file:");
				fileChooser.setBounds(6, 6, 550, 400);
				int result = fileChooser.showOpenDialog(frame);
				if (result == JFileChooser.APPROVE_OPTION) {
					File selectedXML = fileChooser.getSelectedFile();
					xmlFilePath.setText(selectedXML.getAbsolutePath());
					isXMLFileSelected = true;
					if (isMPRDestDirSelected) {
						btnGenerateNesting.setEnabled(true);
					}
					homeFolder = new File(selectedXML.getAbsolutePath());
				}
			}
		});
		btnXmlBrowse.setBounds(461, 40, 117, 29);
		frame.getContentPane().add(btnXmlBrowse);

		btnMprBrowse = new JButton("Browse");
		btnMprBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser(homeFolder);
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.setDialogTitle("Choose your destionation folder:");
				fileChooser.setBounds(6, 6, 550, 400);
				int result = fileChooser.showOpenDialog(frame);
				if (result == JFileChooser.APPROVE_OPTION) {
					File selectedDir = fileChooser.getSelectedFile();
					mprPath.setText(selectedDir.getAbsolutePath());
					isMPRDestDirSelected = true;
					if (isXMLFileSelected) {
						btnGenerateNesting.setEnabled(true);
					}
					homeFolder = selectedDir;
				}
			}
		});
		btnMprBrowse.setBounds(461, 92, 117, 29);
		frame.getContentPane().add(btnMprBrowse);

		txtrStatusBar = new JTextArea();
		txtrStatusBar.setEditable(false);
		txtrStatusBar.setText("Status");
		txtrStatusBar.setBounds(20, 228, 558, 201);
		frame.getContentPane().add(txtrStatusBar);

		// xmlFilePath.setText("c:\\\\test\\\\xml.xml");
		// mprPath.setText("c:\\\\test\\\\mpr\\\\");
		// btnGenerateNesting.setEnabled(true);

		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		progressBar.setBounds(20, 196, 558, 20);
		frame.getContentPane().add(progressBar);
	}
}
