package gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;

public class NestingGUI {

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
	private JProgressBar progressBar;
	private JTextArea txtrStatusBar;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					NestingGUI window = new NestingGUI();
					window.frame.setVisible(true);
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

	/**
	 * Set default values.
	 */
	private void initializeFields() {
		xmlFilePath.setText("");
		mprPath.setText("");
		btnGenerateNesting.setEnabled(false);
		progressBar.setValue(0);
		txtrStatusBar.setText("Status:");
		frame.repaint();
	}
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame("Netsing MPR Generator");
		frame.setBounds(100, 100, 600, 500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//look and feel:
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
		
		//Menu Bar:
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
		
		//Gui
		lblXmlFile = new JLabel("XML File");
		lblXmlFile.setFont(new Font("Lucida Grande", Font.BOLD, 18));
		lblXmlFile.setBounds(20, 37, 87, 29);
		frame.getContentPane().add(lblXmlFile);
		
		lblWoodwopFilesDirectory = new JLabel("WoodWop Files:");
		lblWoodwopFilesDirectory.setFont(new Font("Lucida Grande", Font.BOLD, 18));
		lblWoodwopFilesDirectory.setBounds(20, 89, 156, 29);
		frame.getContentPane().add(lblWoodwopFilesDirectory);
		
		btnGenerateNesting = new JButton("Generate Nesting!");
		btnGenerateNesting.setEnabled(false);
		btnGenerateNesting.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
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
		btnXmlBrowse.setBounds(461, 40, 117, 29);
		frame.getContentPane().add(btnXmlBrowse);
		
		btnMprBrowse = new JButton("Browse");
		btnMprBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnMprBrowse.setBounds(461, 92, 117, 29);
		frame.getContentPane().add(btnMprBrowse);
		
		progressBar = new JProgressBar();
		progressBar.setBounds(20, 182, 558, 20);
		frame.getContentPane().add(progressBar);
		
		txtrStatusBar = new JTextArea();
		txtrStatusBar.setEditable(false);
		txtrStatusBar.setText("Status");
		txtrStatusBar.setBounds(20, 228, 558, 201);
		frame.getContentPane().add(txtrStatusBar);
	}


}
