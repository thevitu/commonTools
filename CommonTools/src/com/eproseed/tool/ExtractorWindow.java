package com.eproseed.tool;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Paths;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * 
 * Graphic window for XML extractor
 *
 */
public class ExtractorWindow extends JFrame {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 6739660443647686456L;

	private static final int WINDOW_HEIGHT = 300;
	private static final int WINDOW_WIDTH = 600;
	private static final String TITLE = "Extractor";
	private String envVar = null;
	private String defaultDir = null;

	
	private Component _this;
	private final JFileChooser fileChooser = new JFileChooser();
	private JEditorPane output;
	
	/**
	 * Default Contructor
	 */
	public ExtractorWindow() {
		super();
		_this = this;
		
		configFileChooser();
		
		initFrame();
		this.add(inputPanel());
		this.add(outputPanel());
	}

	public ExtractorWindow setEnvVar(String envVar) {
		this.envVar = envVar;
		return this;
	}

	public ExtractorWindow setDefaultDir(String defaultDir) {
		this.defaultDir = defaultDir;
		configFileChooser();
		return this;
	}

	private void configFileChooser() {
		if (envVar != null && defaultDir != null) {
			String env = System.getenv(envVar);
			if (env != null && ! env.isEmpty()) {
				File file = Paths.get(env + defaultDir).toFile();
				if (file.exists()) {
					Util.echo("Current dir: " + file.getPath());
					fileChooser.setCurrentDirectory(file);
				}
			}
		}
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileFilter(new FileNameExtensionFilter("XSD file", "xsd"));
	}
	
	private void initFrame() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
		this.setTitle(TITLE);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((screen.width - this.getSize().width) / 2, (screen.height - this.getSize().height) / 2);
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
	}

	private JPanel inputPanel() {
		
		JLabel labelPath = new JLabel();
		labelPath.setText("XSD file");
		labelPath.setHorizontalAlignment(SwingConstants.RIGHT);

		JTextField inputPath = new JTextField(30);
		
		JButton fileChooserButton = new JButton("Choose");
		fileChooserButton.addActionListener(fileChooserListener(inputPath));
		
		JButton generateButton = new JButton("Generate");
		generateButton.addActionListener(generateListener(inputPath));
		
		JPanel panel = new JPanel();		
		panel.setLayout(new FlowLayout());
		panel.add(labelPath);
		panel.add(inputPath);
		panel.add(fileChooserButton);
		panel.add(generateButton);
		return panel;
	}

	private JPanel outputPanel() {
		JEditorPane editorPane = new JEditorPane();
		output = editorPane;
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(editorPane, BorderLayout.CENTER);
		
		return panel;
	}
	
	
	private ActionListener fileChooserListener(JTextField textField) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (fileChooser.showOpenDialog(_this) == JFileChooser.APPROVE_OPTION) {
					textField.setText(fileChooser.getSelectedFile().getPath());
		        }
			}
		};
	}
	
	
	private ActionListener generateListener(JTextField textField) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (textField.getText() != null && ! textField.getText().isEmpty()) {
					File file = Paths.get(textField.getText()).toFile();
					if (file.exists()) {
						XMLExtractor extractor = new XMLExtractor(file);
						try {
							output.setText(extractor.extractDocumentTypes());
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		};
	}
	
}
