package org.jboss.bqt.gui.panels;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;

import org.jboss.bqt.client.TestClient;
import org.jboss.bqt.gui.appender.GUIAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel for starting BQT via GUI.
 * 
 * @author jdurani
 *
 */
@SuppressWarnings("serial")
public class GUIRunnerPanel extends JPanel {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GUIRunnerPanel.class);
	
	/**
	 * Default values for GUI tool. Contains also property names.
	 */
	private static interface GUIDefaults{
		//name of property which should contain path to properties file
		static final String GUI_DEFAULT_PATHS_PROP = "bqt.gui.default.paths";
		
		//properties
		static final String HOST_PROP = "bqt.gui.default.host";
		static final String PORT_PROP = "bqt.gui.default.port";
		static final String USER_NAME_PROP= "bqt.gui.default.username";
		static final String PASSWORD_PROP = "bqt.gui.default.password";
		
		static final String SCENARIOS_DIR_PROP = "bqt.gui.default.scenarios.dir";
		static final String OUTPUT_DIR_PROP = "bqt.gui.default.output.dir";
		static final String CONFIG_PROP = "bqt.gui.default.config";
		static final String ARTIFACTS_DIR_PROP = "bqt.gui.default.artifacts.dir";

		static final String INCLUDE_GUI_DEF_PROP = "bqt.gui.default.include";
		static final String EXCLUDE_GUI_DEF_PROP = "bqt.gui.default.exclude";
		
		//defaults
		static final String HOST = "localhost";
		static final String PORT = "31000";
		static final String USER_NAME= "user";
		static final String PASSWORD = "user";
		
		static final String SCENARIOS_DIR = "";
		static final String OUTPUT_DIR = "";
		static final String CONFIG = "";
		static final String ARTIFACTS_DIR = "";
		
		static final String INCLUDE = "";
		static final String EXCLUDE = "";
	}
	
	/**
	 * BQT tool properties.
	 */
	public static interface BQTProperties { 
		//keys for properties for bqt-distro
		public static final String SCENARIO_FILE_PROP = "scenario.file";
		public static final String RESULT_MODE_PROP = "result.mode";
		public static final String QUERYSET_ARTIFACTS_DIR_PROP = "queryset.artifacts.dir";
		public static final String OUTPUT_DIR_PROP = "output.dir";
		public static final String CONFIG_PROP = "config";
		public static final String HOST_NAME_PROP = "host.name";
		public static final String HOST_PORT_PROP = "host.port";
		public static final String USERNAME_PROP = "username";
		public static final String PASSWORD_PROP = "password";
		public static final String SUPPORT_OLD_PROP_FORMAT_PROP= "support.pre1.0.scenario";
		
		//include exclude options
		public static final String INCLUDE_PROP = "bqt.scenario.include";
		public static final String EXCLUDE_PROP = "bqt.scenario.exclude";
		
		//result modes
		public static final String RESULT_MODE_SQL = "SQL";
		public static final String RESULT_MODE_NONE = "NONE";
		public static final String RESULT_MODE_COMPARE = "COMPARE";
		public static final String RESULT_MODE_GENERATE= "GENERATE";
		
	}
	
	private JComboBox resultModes;
	private JLabel resultModesLabel;
	
	private JTextField userName;
	private JLabel userNameLabel;
	private JTextField password;
	private JLabel passwordLabel;
	private JTextField host;
	private JLabel hostLabel;
	private JTextField port;
	private JLabel portLabel;
	
	private JTextField scenarios;
	private JLabel scenariosLabel;
	private JButton scenariosBrowseButton;
	private JTextField outputDir;
	private JLabel outputDirLabel;
	private JButton outputDirBrowseButton;
	private JTextField config;
	private JLabel configLabel;
	private JButton configBrowseButton;
	private JTextField artifactsDir;
	private JLabel artifactsDirLabel;
	private JButton artifactsDirBrowseButton;
	
	private JTextField include;
	private JLabel includeLabel;
	private JTextField exclude;
	private JLabel excludeLabel;

	private JCheckBox pre1Supported;
	
	private JButton startButton;
	private JButton cancelButton;
	
	private BQTRunner runningInstance;
	
	private JTextPane bqtLogPane;
	private JScrollPane bqtLogScrollPane;
	
	private JLabel status;
	private JLabel statusLabel;
	
	/**
	 * Creates a new instance.
	 */
	public GUIRunnerPanel() {
		super();
		init();
	}
	
	/**
	 * Initializes this panel.
	 */
	private void init(){
		initResultModes();
		initConnectionProperties();
		initBqtConfig();
		initIncludeExclude();
		initPre1Support();
		initOptionButtons();
		initBqtLogPane();
		initStatusLabel();
		
		GroupLayout gl = new GroupLayout(this);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		gl.setHorizontalGroup(gl.createParallelGroup()
			.addGroup(gl.createParallelGroup()
				.addGroup(gl.createSequentialGroup()
					.addGroup(gl.createParallelGroup()
						.addComponent(userNameLabel)
						.addComponent(userName, 200, 200, 800))
					.addGroup(gl.createParallelGroup()
						.addComponent(passwordLabel)
						.addComponent(password)))
				.addGroup(gl.createSequentialGroup()
					.addGroup(gl.createParallelGroup()
						.addComponent(hostLabel)
						.addComponent(host))
					.addGroup(gl.createParallelGroup()
						.addComponent(portLabel)
						.addComponent(port))))
			.addGroup(gl.createSequentialGroup()
				.addComponent(resultModesLabel)
				.addComponent(resultModes, 120, 120, 120))
			.addGroup(gl.createParallelGroup()
				.addGroup(gl.createParallelGroup()
					.addComponent(scenariosLabel)
					.addGroup(gl.createSequentialGroup()
						.addComponent(scenarios)
						.addComponent(scenariosBrowseButton)))
				.addGroup(gl.createParallelGroup()
					.addComponent(outputDirLabel)
					.addGroup(gl.createSequentialGroup()
						.addComponent(outputDir)
						.addComponent(outputDirBrowseButton)))
				.addGroup(gl.createParallelGroup()
					.addComponent(configLabel)
					.addGroup(gl.createSequentialGroup()
						.addComponent(config)
						.addComponent(configBrowseButton)))
				.addGroup(gl.createParallelGroup()
					.addComponent(artifactsDirLabel)
					.addGroup(gl.createSequentialGroup()
						.addComponent(artifactsDir)
						.addComponent(artifactsDirBrowseButton))))
			.addGroup(gl.createParallelGroup()
				.addComponent(includeLabel)
				.addComponent(include)
				.addComponent(excludeLabel)
				.addComponent(exclude))
			.addComponent(pre1Supported)
			.addGroup(gl.createSequentialGroup()
				.addComponent(startButton)
				.addComponent(cancelButton)
				.addComponent(statusLabel)
				.addComponent(status))
			.addComponent(bqtLogScrollPane));
		
		int fieldHeight = 25;
		int groupsGap = 25;
		gl.setVerticalGroup(gl.createSequentialGroup()
			.addGroup(gl.createParallelGroup()
				.addGroup(gl.createSequentialGroup()
					.addComponent(userNameLabel)
					.addComponent(userName, fieldHeight, fieldHeight, fieldHeight)
					.addComponent(hostLabel)
					.addComponent(host))
				.addGroup(gl.createSequentialGroup()
					.addComponent(passwordLabel)
					.addComponent(password)
					.addComponent(portLabel)
					.addComponent(port)))
			.addGroup(gl.createSequentialGroup()
				.addGap(groupsGap)
				.addGroup(gl.createParallelGroup()
					.addComponent(resultModesLabel)
					.addComponent(resultModes, 25, 25, 25))
				.addGap(groupsGap)				
				.addComponent(scenariosLabel)
				.addGroup(gl.createParallelGroup()
					.addComponent(scenarios)
					.addComponent(scenariosBrowseButton))
				.addComponent(outputDirLabel)
				.addGroup(gl.createParallelGroup()
					.addComponent(outputDir)
					.addComponent(outputDirBrowseButton))
				.addComponent(configLabel)
				.addGroup(gl.createParallelGroup()
					.addComponent(config)
					.addComponent(configBrowseButton))
				.addComponent(artifactsDirLabel)
				.addGroup(gl.createParallelGroup()
					.addComponent(artifactsDir)
					.addComponent(artifactsDirBrowseButton)))
			.addGap(groupsGap)
			.addGroup(gl.createSequentialGroup()
				.addComponent(includeLabel)
				.addComponent(include)
				.addComponent(excludeLabel)
				.addComponent(exclude))
			.addGap(groupsGap)
			.addComponent(pre1Supported)
			.addGap(groupsGap)
			.addGroup(gl.createParallelGroup(Alignment.CENTER)
				.addComponent(startButton)
				.addComponent(cancelButton)
				.addComponent(statusLabel)
				.addComponent(status))
			.addComponent(bqtLogScrollPane));
		
		gl.linkSize(host, port, userName, password);
		gl.linkSize(SwingConstants.VERTICAL, scenarios, outputDir, config, artifactsDir, include, exclude);
		setLayout(gl);
		initDefaultPaths();
	}
	
	private static final void setToolTipText(JComponent component, String text){
		component.setToolTipText(text);
		ToolTipManager.sharedInstance().registerComponent(component);
	}
	
	private void initStatusLabel(){
		status = new JLabel("NOT RUNNING");
		status.setFont(new Font("Arial", Font.BOLD, 20));
		setToolTipText(status, "BQT status");
		statusLabel = new JLabel("Status: ");
	}
	
	private void initBqtLogPane(){
		bqtLogPane = GUIAppender.getTextPane("BQT_GUI");
		bqtLogScrollPane = new JScrollPane(bqtLogPane);
	}
	
	/**
	 * Initializes include/exclude related part of this panel; 
	 */
	private void initIncludeExclude(){
		include = new JTextField();
		exclude = new JTextField();
		
		setToolTipText(include, "Include scenario pattern. Same as \"bqt.scenario.include\" property.");
		setToolTipText(exclude, "Exclude scenario pattern. Same as \"bqt.scenario.exclude\" property.");
		
		includeLabel = new JLabel("Include scenarios");
		excludeLabel = new JLabel("Exclude scenarios");
	}
	
	/**
	 * Sets default values for every field.
	 */
	private void initDefaultPaths(){
		File def = new File(System.getProperty(GUIDefaults.GUI_DEFAULT_PATHS_PROP, ""));
		Properties props = new Properties();
		if(def.exists()){
			props = new Properties();
			InputStream is = null;
			try{
				is = new FileInputStream(def);
				props.load(is);
			} catch (IOException ex){
				props.clear();
			} finally {
				try{
					is.close();
				} catch(IOException ignore){}
			}
		}
		host.setText(props.getProperty(GUIDefaults.HOST_PROP, GUIDefaults.HOST));
		port.setText(props.getProperty(GUIDefaults.PORT_PROP, GUIDefaults.PORT));

		userName.setText(props.getProperty(GUIDefaults.USER_NAME_PROP, GUIDefaults.USER_NAME));
		password.setText(props.getProperty(GUIDefaults.PASSWORD_PROP, GUIDefaults.PASSWORD));
		
		scenarios.setText(props.getProperty(GUIDefaults.SCENARIOS_DIR_PROP, GUIDefaults.SCENARIOS_DIR));
		outputDir.setText(props.getProperty(GUIDefaults.OUTPUT_DIR_PROP, GUIDefaults.OUTPUT_DIR));
		config.setText(props.getProperty(GUIDefaults.CONFIG_PROP, GUIDefaults.CONFIG));
		artifactsDir.setText(props.getProperty(GUIDefaults.ARTIFACTS_DIR_PROP, GUIDefaults.ARTIFACTS_DIR));
		
		include.setText(props.getProperty(GUIDefaults.INCLUDE_GUI_DEF_PROP, GUIDefaults.INCLUDE));
		exclude.setText(props.getProperty(GUIDefaults.EXCLUDE_GUI_DEF_PROP, GUIDefaults.EXCLUDE));
	}
	
	/**
	 * Initializes buttons. 
	 */
	private void initOptionButtons(){
		startButton = new JButton("Start");
		startButton.addActionListener(new StartBQTActionListener());
		
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new CancelBQTActionListener());
		
		setToolTipText(startButton, "Start BQT task.");
		setToolTipText(cancelButton, "Cancel BQT task.");
	}
	
	/**
	 * Initializes pre-1-support check box. 
	 */
	private void initPre1Support(){
		pre1Supported = new JCheckBox("Support old names");
		pre1Supported.setSelected(true);
		
		setToolTipText(pre1Supported, "If old names of BQT properties are supported. Same as \"support.pre1.0.scenario\" property.");
	}
	
	/**
	 * Initializes bqt-config related part of this panel. 
	 */
	private void initBqtConfig(){
		scenarios = new JTextField();
		scenariosLabel = new JLabel("Scenario (directory or file)");
		scenariosBrowseButton = getBrowseButton(JFileChooser.FILES_AND_DIRECTORIES, scenarios);
		
		outputDir = new JTextField();
		outputDirLabel = new JLabel("Output directory");
		outputDirBrowseButton = getBrowseButton(JFileChooser.DIRECTORIES_ONLY, outputDir);
		
		config = new JTextField();
		configLabel = new JLabel("Configuration file");
		configBrowseButton = getBrowseButton(JFileChooser.FILES_ONLY, config);
		
		artifactsDir = new JTextField();
		artifactsDirLabel = new JLabel("Artifacts directory");
		artifactsDirBrowseButton = getBrowseButton(JFileChooser.DIRECTORIES_ONLY, artifactsDir);
		
		setToolTipText(scenarios, "Path to scenario file. It could be a single file or a directory. Same as \"scenario.file\" property.");
		setToolTipText(outputDir, "Path to output directory. Same as \"output.dir\" property.");
		setToolTipText(config, "Path to default config file. Usually <bqt-distro-path>/config/test.properties. Same as \"config\" property.");
		setToolTipText(artifactsDir, "Path to queries and expected results. "
				+ "Usually <dataservices-path>/<test-artifacts-dir>/ctc-tests/queries. Same as \"queryset.artifacts.dir\" property.");
	}
	
	/**
	 * Returns a new button with text {@code Browse} which will show {@link JFileChooser}.
	 * Path of selected file will be set as text to {@code textFiel}.
	 * 
	 * @param selectionMode selection mode for {@link JFileChooser}
	 * @param textField text field, where the path of selected file will be shown
	 * @return browse button
	 * 
	 * @see {@link JFileChooser#setFileSelectionMode(int)}
	 * 
	 */
	private JButton getBrowseButton(int selectionMode, JTextField textField){
		JButton button = new JButton("Browse");
		button.addActionListener(new BrowseActionListener(textField, selectionMode));
		return button;
	}
	
	/**
	 * Initializes connection-properties related part of this panel.
	 */
	private void initConnectionProperties(){
		userName = new JTextField();
		password = new JTextField();
		host = new JTextField();
		port = new JTextField();
		
		userNameLabel = new JLabel("Username");
		passwordLabel = new JLabel("Password");
		hostLabel = new JLabel("Host");
		portLabel = new JLabel("Port");
		
		setToolTipText(userName, "User name for JDV server. Same as \"username\" property.");
		setToolTipText(password, "Password for JDV server. Same as \"password\" property.");
		setToolTipText(host, "Host name of JDV server. Same as \"host.name\" property.");
		setToolTipText(port, "Port of JDV server. Same as \"host.port\" property.");
	}
	
	/**
	 * Initializes result-mode related part of this panel;
	 */
	private void initResultModes(){
		resultModes = new JComboBox();
		resultModes.addItem(BQTProperties.RESULT_MODE_NONE);
		resultModes.addItem(BQTProperties.RESULT_MODE_COMPARE);
		resultModes.addItem(BQTProperties.RESULT_MODE_SQL);
		resultModes.addItem(BQTProperties.RESULT_MODE_GENERATE);
		
		resultModes.setSelectedItem(BQTProperties.RESULT_MODE_COMPARE);
		
		setToolTipText(resultModes, "Result mode. Same as \"result.mode\" property.");
		
		resultModesLabel = new JLabel("Result mode");
	}
	
	/**
	 * Determines, if this panel could be disposed. If BQT is not running
	 * method return {@code true}. Otherwise method returns same results as
	 * method {@link #cancelActualJob(boolean)}.
	 * 
	 * @return true, if this panel could be disposed, false otherwise
	 * @see #cancelActualJob(boolean)
	 */
	public boolean couldDispose(){
		if(runningInstance == null){
			return true;
		}
		return cancelActualJob(true);
	}
	
	/**
	 * <p>
	 * This method shows a confirm dialog. If the user confirm that he want to cancel
	 * actual job, then actual BQT-job will be canceled and method will return {@code true}.
	 * Otherwise method will not cancel actual job and {@code false} will be returned.
	 * </p>
	 * <p>
	 * Method supposes that the BQT-job is running.
	 * </p>
	 * 
	 * @param disposeLogFrame If the BQT-job is running and a BQT-log-frame is visible,
	 * 		then the frame will be disposed.
	 * @return true, if actual job has been canceled, false otherwise
	 */
	public boolean cancelActualJob(boolean disposeLogFrame){
		int result = JOptionPane.showConfirmDialog(null, "BQT is still running. Do you want to interrupt it?");
		if(result == JOptionPane.YES_OPTION){
			if(runningInstance != null){
				runningInstance.cancel(true);
			}
			return true;
		} else {
			return false;
		}
		
	}
	
	/**
	 * Sets text and foreground color for status label.
	 * @param statusText
	 * @param bg
	 */
	private void setStatus(String statusText, Color bg) {
		status.setText(statusText);
		status.setForeground(bg);
		status.repaint();
	}

	/**
	 * Action for the Browse button.
	 * @author jdurani
	 *
	 */
	private static class BrowseActionListener implements ActionListener {
		
		private final JTextField textField;
		private final int selectionMode;
		private JFileChooser chooser;
		
		private BrowseActionListener(JTextField textField, int selectionMode) {
			this.textField = textField;
			this.selectionMode = selectionMode;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if(chooser == null){
				chooser = new JFileChooser(textField.getText());
				chooser.setMultiSelectionEnabled(false);
				chooser.setFileSelectionMode(selectionMode);
			} else {
				chooser.setCurrentDirectory(new File(textField.getText()));
			}
			if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
				textField.setText(chooser.getSelectedFile().getAbsolutePath());
			}
		}
	}
	
	/**
	 * Action for the Start-BQT button
	 * @author jdurani
	 *
	 */
	private class StartBQTActionListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if(runningInstance != null){
				JOptionPane.showMessageDialog(null, "BQT already running!", "BQT running", JOptionPane.ERROR_MESSAGE);
			} else {
				runningInstance = new BQTRunner();
				runningInstance.execute();
			}
		}
	}
	
	/**
	 * Action for the Cancel-BQT button.
	 * @author jdurani
	 *
	 */
	private class CancelBQTActionListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if(runningInstance != null){
				cancelActualJob(false);
			}
		}
	}
	
	/**
	 * Swing worker, which sets all required properties for BQT and runs BQT-job.
	 *  
	 * @author jdurani
	 *
	 */
	private class BQTRunner extends SwingWorker<Void, Void> {
		
		
		@Override
		protected Void doInBackground() throws Exception {
			Properties props = getProperties();
			bqtLogPane.setText("");
			LOGGER.info("Starting BQT with properties: {}.",props);
			setStatus("IN PROGRESS", Color.ORANGE); 
			new TestClient().runTest(props);
			LOGGER.debug("BQT ended.");
			return null;
		}
		
		@Override
		protected void done() {
			runningInstance = null;
			try{
				LOGGER.debug("Checking result.");
				get();
				setStatus("   DONE   ", Color.GREEN);
				LOGGER.debug("Result OK.");
			} catch (ExecutionException ex){
				ex.printStackTrace();
				LOGGER.warn("Task ends with an exception.", ex.getCause());
				JOptionPane.showMessageDialog(null, "Task ends with an exception: " + ex.getCause().getMessage() + "."
							+ System.getProperty("line.separator") + "See log for more details.",
						"Error", JOptionPane.ERROR_MESSAGE);
				setStatus("  FAILED  ", Color.RED);
			} catch (CancellationException ex){
				ex.printStackTrace();
				LOGGER.info("Task has been cancelled.", ex.getCause());
				JOptionPane.showMessageDialog(null, "Task has been cancelled.",
						"Cancelled", JOptionPane.WARNING_MESSAGE);
				setStatus("  FAILED  ", Color.RED);
			} catch (InterruptedException ex){
				ex.printStackTrace();
				LOGGER.warn("Task has been interrupted.", ex);
				JOptionPane.showMessageDialog(null, "Task has been interrupted. See log for more details.",
						"Error", JOptionPane.ERROR_MESSAGE);
				setStatus("  FAILED  ", Color.RED);
			}
		}
		
		/**
		 * Returns all required properties.
		 * @return
		 */
		private Properties getProperties(){
			Properties props = new Properties();
			props.setProperty(BQTProperties.HOST_NAME_PROP, host.getText());
			props.setProperty(BQTProperties.HOST_PORT_PROP, port.getText());
			props.setProperty(BQTProperties.USERNAME_PROP, userName.getText());
			props.setProperty(BQTProperties.PASSWORD_PROP, password.getText());
			props.setProperty(BQTProperties.SCENARIO_FILE_PROP, scenarios.getText());
			props.setProperty(BQTProperties.RESULT_MODE_PROP, resultModes.getSelectedItem().toString());
			props.setProperty(BQTProperties.QUERYSET_ARTIFACTS_DIR_PROP, artifactsDir.getText());
			props.setProperty(BQTProperties.OUTPUT_DIR_PROP, outputDir.getText());
			props.setProperty(BQTProperties.CONFIG_PROP, config.getText());
			props.setProperty(BQTProperties.SUPPORT_OLD_PROP_FORMAT_PROP, Boolean.toString(pre1Supported.isSelected()));
			props.setProperty(BQTProperties.INCLUDE_PROP, include.getText());
			props.setProperty(BQTProperties.EXCLUDE_PROP, exclude.getText());
			return props;
		}
	}
}












