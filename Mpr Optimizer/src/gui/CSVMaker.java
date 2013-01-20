package gui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class CSVMaker {

	private JFrame frame;
	private JTable table;
	private DefaultTableModel dataModel;
	private ArrayList<File> mprFiles;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CSVMaker window = new CSVMaker();
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
		frame = new JFrame();
		frame.setBounds(100, 100, 900, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		dataModel = new DefaultTableModel(new String[] { "Description",
				"Length", "Width", "Qty", "Rotate", "Material", "Part code" },
				1);
		table = new JTable(dataModel);
		table.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null,
				null));
		table.setSize(820, 400);
		table.setLocation(20, 20);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBounds(10, 11, 864, 368);
		frame.getContentPane().add(scrollPane);

		JButton btnSelectFiles = new JButton("Select Files");
		btnSelectFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser(
						"C:\\Users\\La bla bla\\Dropbox");
				fileChooser
						.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fileChooser.setMultiSelectionEnabled(true);
				FileNameExtensionFilter mprExtensionFilter = new FileNameExtensionFilter(
						"MPR File (*.mpr)", "mpr");
				fileChooser.setFileFilter(mprExtensionFilter);
				int result = fileChooser.showOpenDialog(frame);
				if (result == JFileChooser.APPROVE_OPTION) {
					File[] selectedFiles = fileChooser.getSelectedFiles();
					if (selectedFiles[0].isDirectory()) {
						getMprFiles(selectedFiles[0]);
					} else {
						for(File file: selectedFiles) {
							mprFiles.add(file);
						}
					}
				}
			}
		});
		btnSelectFiles.setBounds(10, 445, 137, 60);
		frame.getContentPane().add(btnSelectFiles);
	}

	private void getMprFiles(File sourceDir) {
		File[] fileList = sourceDir.listFiles();
		for (File file : fileList) {
			if (file.isDirectory()) {
				getMprFiles(file);
			}
			if (file.getAbsolutePath().endsWith(".mpr")) {
				mprFiles.add(file);
			}
		}
	}
}
