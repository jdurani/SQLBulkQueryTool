package org.jboss.bqt.gui.panels;

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
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.jboss.bqt.client.TestClient;
import org.jboss.bqt.gui.BQTLogFrame;
import org.jboss.bqt.gui.BQTLogFrame.Status;
import org.jboss.bqt.gui.appender.GUIAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class GUIRunnerPanel extends JPanel {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GUIRunnerPanel.class);
	
	private static interface GUIDefaults{
		//properties
		public static final String GUI_DEFAULT_PATHS_PROP = "bqt.gui.default.paths";
		
		public static final String HOST_PROP = "bqt.gui.default.host";
		public static final String PORT_PROP = "bqt.gui.default.port";
		public static final String USER_NAME_PROP= "bqt.gui.default.username";
		public static final String PASSWORD_PROP = "bqt.gui.default.password";
		
		public static final String LOG_DIR_PROP = "bqt.gui.default.log.dir";
		public static final String SCENARIOS_DIR_PROP = "bqt.gui.default.scenarios.dir";
		public static final String OUTPUT_DIR_PROP = "bqt.gui.default.output.dir";
		public static final String CONFIG_PROP = "bqt.gui.default.config";
		public static final String ARTIFACTS_DIR_PROP = "bqt.gui.default.artifacts.dir";
		
		//defaults
		public static final String HOST = "localhost";
		public static final String PORT = "31000";
		public static final String USER_NAME= "user";
		public static final String PASSWORD = "user";
		
		public static final String LOG_DIR = "";
		public static final String SCENARIOS_DIR = "";
		public static final String OUTPUT_DIR = "";
		public static final String CONFIG = "";
		public static final String ARTIFACTS_DIR = "";
	}
	
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
		public static final String LOG_DIR= "log.dir";
		
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
	
	private JTextField logDir;
	private JLabel logDirLabel;
	private JButton logDirBrowseButton;
	
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
	
	private JCheckBox pre1Supported;
	
	private JButton startButton;
	private JButton cancelButton;
	
	private BQTRunner runningInstance;
	private BQTLogFrame logFrame = null;
	
	public GUIRunnerPanel() {
		super();
		init();
	}
	
	private void init(){
		initResultModes();
		initConnectionProperties();
		initLogDir();
		initBqtConfig();
		initPre1Support();
		initOptionButtons();
		
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
			.addGroup(gl.createParallelGroup()
				.addComponent(logDirLabel)
				.addGroup(gl.createSequentialGroup()
					.addComponent(logDir)
					.addComponent(logDirBrowseButton)))
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
			.addComponent(pre1Supported)
			.addGroup(gl.createSequentialGroup()
				.addComponent(startButton)
				.addComponent(cancelButton)));
		
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
				.addComponent(logDirLabel)
				.addGroup(gl.createParallelGroup()
					.addComponent(logDir, fieldHeight, fieldHeight, fieldHeight)
					.addComponent(logDirBrowseButton))
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
			.addComponent(pre1Supported)
			.addGap(groupsGap)
			.addGroup(gl.createParallelGroup(Alignment.CENTER)
				.addComponent(startButton)
				.addComponent(cancelButton)));
		
		gl.linkSize(host, port, userName, password);
		gl.linkSize(SwingConstants.VERTICAL, logDir, scenarios, outputDir, config, artifactsDir);
		setLayout(gl);
		initDefaultPaths();
	}
	
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
		
		logDir.setText(props.getProperty(GUIDefaults.LOG_DIR_PROP, GUIDefaults.LOG_DIR));
		
		scenarios.setText(props.getProperty(GUIDefaults.SCENARIOS_DIR_PROP, GUIDefaults.SCENARIOS_DIR));
		outputDir.setText(props.getProperty(GUIDefaults.OUTPUT_DIR_PROP, GUIDefaults.OUTPUT_DIR));
		config.setText(props.getProperty(GUIDefaults.CONFIG_PROP, GUIDefaults.CONFIG));
		artifactsDir.setText(props.getProperty(GUIDefaults.ARTIFACTS_DIR_PROP, GUIDefaults.ARTIFACTS_DIR));
	}
	
	private void initOptionButtons(){
		startButton = new JButton("Start");
		startButton.addActionListener(new StartBQTActionListener(this));
		
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new CancelBQTActionListener());
	}
	
	private void initPre1Support(){
		pre1Supported = new JCheckBox("Support old names");
		pre1Supported.setSelected(true);
	}
	
	private void initBqtConfig(){
		scenarios = new JTextField();
		scenariosLabel = new JLabel("Scenario (directory or file)");
		scenariosBrowseButton = getBrowseButton(JFileChooser.FILES_AND_DIRECTORIES, scenarios);
		
		outputDir = new JTextField();
		outputDirLabel = new JLabel("Output direcotry");
		outputDirBrowseButton = getBrowseButton(JFileChooser.DIRECTORIES_ONLY, outputDir);
		
		config = new JTextField();
		configLabel = new JLabel("Configuration file");
		configBrowseButton = getBrowseButton(JFileChooser.FILES_ONLY, config);
		
		artifactsDir = new JTextField();
		artifactsDirLabel = new JLabel("Artifacts direcotry");
		artifactsDirBrowseButton = getBrowseButton(JFileChooser.DIRECTORIES_ONLY, artifactsDir);
	}
	
	private void initLogDir(){
		logDir = new JTextField();
		logDirLabel = new JLabel("Log direcotry");
		logDirBrowseButton = getBrowseButton(JFileChooser.DIRECTORIES_ONLY, logDir);
	}
	
	private JButton getBrowseButton(int selectionMode, JTextField textField){
		JButton button = new JButton("Browse");
		button.addActionListener(new BrowseActionListener(textField, selectionMode));
		return button;
	}
	
	private void initConnectionProperties(){
		userName = new JTextField();
		password = new JTextField();
		host = new JTextField();
		port = new JTextField();
		
		userNameLabel = new JLabel("Username");
		passwordLabel = new JLabel("Password");
		hostLabel = new JLabel("Host");
		portLabel = new JLabel("Port");
	}
	
	private void initResultModes(){
		resultModes = new JComboBox();
		resultModes.addItem(BQTProperties.RESULT_MODE_NONE);
		resultModes.addItem(BQTProperties.RESULT_MODE_COMPARE);
		resultModes.addItem(BQTProperties.RESULT_MODE_SQL);
		resultModes.addItem(BQTProperties.RESULT_MODE_GENERATE);
		
		resultModes.setSelectedItem(BQTProperties.RESULT_MODE_COMPARE);
		
		resultModesLabel = new JLabel("Result mode");
	}
	
	public boolean couldDispose(){
		if(runningInstance == null){
			if(logFrame != null){
				logFrame.dispose();
			}
			return true;
		}
		return cancelActualJob(true);
	}
	
	public boolean cancelActualJob(boolean disposeLogFrame){
		int result = JOptionPane.showConfirmDialog(null, "BQT is still running. Do you want to interrupt it?");
		if(result == JOptionPane.YES_OPTION){
			if(runningInstance != null){
				runningInstance.cancel(true);
			}
			if(disposeLogFrame && logFrame != null){
				logFrame.dispose();
			}
			return true;
		} else {
			return false;
		}
		
	}
	
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
	
	private class StartBQTActionListener implements ActionListener {
		
		private final GUIRunnerPanel panel;
		
		private StartBQTActionListener(GUIRunnerPanel panel) {
			this.panel = panel;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if(runningInstance != null){
				JOptionPane.showMessageDialog(null, "BQT already running!", "BQT running", JOptionPane.ERROR_MESSAGE);
			} else {
				runningInstance = new BQTRunner(panel);
				runningInstance.execute();
			}
		}
	}
	
	private class CancelBQTActionListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if(runningInstance != null){
				cancelActualJob(false);
			}
		}
	}
	
	private class BQTRunner extends SwingWorker<Void, Void> {
		
		private final GUIRunnerPanel panel;
		
		private BQTRunner(GUIRunnerPanel panel) {
			this.panel = panel;
		}
		
		@Override
		protected Void doInBackground() throws Exception {
			if(logFrame != null){
				logFrame.dispose();
			}
			logFrame = new BQTLogFrame(panel);
			logFrame.setVisible(true);
			Properties props = getProperties();
			LOGGER.info("Starting BQT with properties: {}.",props);
			logFrame.setStatus(Status.IN_PROGRESS);
			GUIAppender.setOutputStream(logFrame.getLogAreaStream());
			new TestClient().runTest(props);
			GUIAppender.clearOutputStream();
			LOGGER.debug("BQT ends.");
			return null;
		}
		
		@Override
		protected void done() {
			runningInstance = null;
			try{
				LOGGER.debug("Checking result.");
				get();
				logFrame.setStatus(Status.DONE);
				LOGGER.debug("Result OK.");
			} catch (ExecutionException ex){
				LOGGER.warn("Task ends with an exception.", ex.getCause());
				JOptionPane.showMessageDialog(null, "Task ends with an exception: " + ex.getCause().getMessage() + "."
							+ System.getProperty("line.separator") + "See log for more details.",
						"Error", JOptionPane.ERROR_MESSAGE);
				logFrame.setStatus(Status.FAILED);
			} catch (CancellationException ex){
				LOGGER.info("Task has been cancelled.", ex.getCause());
				JOptionPane.showMessageDialog(null, "Task has been cancelled.",
						"Cancelled", JOptionPane.WARNING_MESSAGE);
				logFrame.setStatus(Status.FAILED);
			} catch (InterruptedException ex){
				LOGGER.warn("Task has been interrupted.", ex);
				JOptionPane.showMessageDialog(null, "Task has been interrupted. See log for more details.",
						"Error", JOptionPane.ERROR_MESSAGE);
				logFrame.setStatus(Status.FAILED);
			}
		}
		
		private Properties getProperties(){
			Properties props = new Properties();
			props.setProperty(BQTProperties.HOST_NAME_PROP, host.getText());
			props.setProperty(BQTProperties.HOST_PORT_PROP, port.getText());
			props.setProperty(BQTProperties.USERNAME_PROP, userName.getText());
			props.setProperty(BQTProperties.PASSWORD_PROP, password.getText());
			System.setProperty(BQTProperties.LOG_DIR, logDir.getText());
			props.setProperty(BQTProperties.SCENARIO_FILE_PROP, scenarios.getText());
			props.setProperty(BQTProperties.RESULT_MODE_PROP, resultModes.getSelectedItem().toString());
			props.setProperty(BQTProperties.QUERYSET_ARTIFACTS_DIR_PROP, artifactsDir.getText());
			props.setProperty(BQTProperties.OUTPUT_DIR_PROP, outputDir.getText());
			props.setProperty(BQTProperties.CONFIG_PROP, config.getText());
			props.setProperty(BQTProperties.SUPPORT_OLD_PROP_FORMAT_PROP, Boolean.toString(pre1Supported.isSelected()));
			return props;
		}
	}
}
