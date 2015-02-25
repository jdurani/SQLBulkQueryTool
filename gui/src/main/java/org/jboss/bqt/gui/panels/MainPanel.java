package org.jboss.bqt.gui.panels;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class MainPanel extends JPanel {

	private static final String RUNNER_PANEL= "runner";
	private static final String JENKINS_PANEL= "jenkins";
	private static final String RESULTS_PANEL= "results";
	
	private GUIRunnerPanel runnerPanel;
	private ResultsPanel resultsPanel;
	private JenkinsPanel jenkinsPanel;
	private JPanel defaultPanel;
	
	private JPanel menuPanel;
	
	private JScrollPane actualPane;
	private JScrollPane defaultPane;
	private JScrollPane runnerPane;
	private JScrollPane jenkinsPane;
	private JScrollPane resultsPane;
	
	private JButton runnerButton;
	private JButton resultsButton;
	private JButton jenkinsButton;
	private JButton exitButton;
	
	public MainPanel() {
		super();
		init();
	}
	
	private void init(){
		initMenuPanel();
		
		defaultPanel = new JPanel();
		defaultPane = new JScrollPane(defaultPanel);
		
		actualPane = defaultPane;
		
		GroupLayout gl = new GroupLayout(this);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		gl.setHorizontalGroup(gl.createSequentialGroup()
				.addComponent(menuPanel)
				.addComponent(actualPane, 400, 800, 1000));
		gl.setVerticalGroup(gl.createParallelGroup()
				.addComponent(menuPanel)
				.addComponent(actualPane, 300, 600, 800));
		setLayout(gl);
	}
	
	private void initMenuPanel(){
		runnerButton = new JButton("Runner");
		runnerButton.addActionListener(new ShowRunnerPanelActionListener());
		
		resultsButton = new JButton("Results");
		resultsButton.addActionListener(new ShowResultsPanelActionListener());
		
		jenkinsButton = new JButton("Jenkins");
		jenkinsButton.addActionListener(new ShowJenkinsPanelActionListener());
		
		exitButton = new JButton("Exit");
		exitButton.addActionListener(new ExitActionListener());
		
		menuPanel = new JPanel();
		
		GroupLayout gl = new GroupLayout(menuPanel);
		gl.setAutoCreateContainerGaps(false);
		gl.setAutoCreateGaps(true);
		gl.setHorizontalGroup(gl.createParallelGroup()
				.addComponent(runnerButton)
				.addComponent(resultsButton)
				.addComponent(jenkinsButton)
				.addComponent(exitButton));
		
		gl.setVerticalGroup(gl.createSequentialGroup()
				.addComponent(runnerButton)
				.addComponent(resultsButton)
				.addComponent(jenkinsButton)
				.addGap(20)
				.addComponent(exitButton));
		
		gl.linkSize(runnerButton, resultsButton, jenkinsButton, exitButton);
		menuPanel.setLayout(gl);
	}
		
	private void showPanel(final String panelName){
		JScrollPane toSet;
		if(RUNNER_PANEL == panelName){
			if(runnerPane == null){
				runnerPanel = new GUIRunnerPanel();
				runnerPane = new JScrollPane(runnerPanel);
			}
			toSet = runnerPane;
		} else if(RESULTS_PANEL == panelName){
			if(resultsPane == null){
				resultsPanel = new ResultsPanel();
				resultsPane = new JScrollPane(resultsPanel);
			}
			toSet = resultsPane;
		} else if(JENKINS_PANEL == panelName){
			if(jenkinsPane == null){
				jenkinsPanel = new JenkinsPanel();
				jenkinsPane = new JScrollPane(jenkinsPanel);
			}
			toSet = jenkinsPane;
		} else {
			toSet = defaultPane;
		}
		GroupLayout gl = (GroupLayout)getLayout();
		gl.replace(actualPane, toSet);
		actualPane = toSet;
	}
	
	public boolean couldDispose(){
		return (runnerPanel == null ? true : runnerPanel.couldDispose()) 
				&& (jenkinsPanel == null ? true : jenkinsPanel.couldDispose()) 
				&& (resultsPanel == null ? true : resultsPanel.couldDispose());
	}
	
	private class ShowResultsPanelActionListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			showPanel(RESULTS_PANEL);
		}
	}
	
	private class ShowJenkinsPanelActionListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			showPanel(JENKINS_PANEL);
		}
	}
	
	private class ShowRunnerPanelActionListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			showPanel(RUNNER_PANEL);
		}
	}
	
	private class ExitActionListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Window w = SwingUtilities.getWindowAncestor(((Component)e.getSource()));
			if(w != null){
				w.dispose();
			}
		}
	}
}




















