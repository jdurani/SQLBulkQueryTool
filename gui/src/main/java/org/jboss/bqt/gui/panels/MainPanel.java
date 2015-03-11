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

/**
 * The main panel of this application.
 * 
 * @author jdurani
 *
 */
@SuppressWarnings("serial")
public class MainPanel extends JPanel {

	private static final int RUNNER_PANEL= 1;
	private static final int JENKINS_PANEL= 2;
	private static final int RESULTS_PANEL= 3;
	
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
	
	/**
	 * Creates a new instance.
	 */
	public MainPanel() {
		super();
		init();
	}
	
	/**
	 * Initializes this panel.
	 */
	private void init(){
		initMenuPanel();
		
		defaultPanel = new JPanel();
		defaultPane = getScrollPane(defaultPanel);
		
		actualPane = defaultPane;
		
		GroupLayout gl = new GroupLayout(this);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		gl.setHorizontalGroup(gl.createSequentialGroup()
				.addComponent(menuPanel)
				.addComponent(actualPane, 400, 800, 2000));
		gl.setVerticalGroup(gl.createParallelGroup()
				.addComponent(menuPanel)
				.addComponent(actualPane, 300, 800, 2000));
		setLayout(gl);
	}
	
	/**
	 * Initializes menu-panel related part of this panel.
	 */
	private void initMenuPanel(){
		runnerButton = new JButton("Runner");
		runnerButton.addActionListener(new ShowPanelActionListener(RUNNER_PANEL));
		
		resultsButton = new JButton("Results");
		resultsButton.addActionListener(new ShowPanelActionListener(RESULTS_PANEL));
		
		jenkinsButton = new JButton("Jenkins");
		jenkinsButton.addActionListener(new ShowPanelActionListener(JENKINS_PANEL));
		
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
	
	/**
	 * Shows required panel.
	 * 
	 * @param panel one of {@link #RUNNER_PANEL}, {@link #JENKINS_PANEL}, {@link #RESULTS_PANEL}
	 */
	private void showPanel(final int panel){
		JScrollPane toSet;
		switch(panel){
			case RUNNER_PANEL:
				if(runnerPane == null){
					runnerPanel = new GUIRunnerPanel();
					runnerPane = getScrollPane(runnerPanel);
				}
				toSet = runnerPane;
				break;
			case JENKINS_PANEL:
				if(jenkinsPane == null){
					jenkinsPanel = new JenkinsPanel();
					jenkinsPane = getScrollPane(jenkinsPanel);
				}
				toSet = jenkinsPane;
				break;
			case RESULTS_PANEL:
				if(resultsPane == null){
					resultsPanel = new ResultsPanel();
					resultsPane = getScrollPane(resultsPanel);
				}
				toSet = resultsPane;
				break;
			default:
				toSet = defaultPane;
		}
		GroupLayout gl = (GroupLayout)getLayout();
		gl.replace(actualPane, toSet);
		actualPane = toSet;
	}
	
	/**
	 * Returns new scroll pane.
	 * 
	 * @param view the view for JScrollPane
	 * @return
	 */
	public static JScrollPane getScrollPane(JPanel view){
		JScrollPane pane = new JScrollPane(view);
		pane.getVerticalScrollBar().setUnitIncrement(10);
		pane.getHorizontalScrollBar().setUnitIncrement(10);
		return pane;
	}
	
	/**
	 * Returns true, if all dependent panels could be disposed.
	 * 
	 * @return
	 */
	public boolean couldDispose(){
		return (runnerPanel == null ? true : runnerPanel.couldDispose()) 
				&& (jenkinsPanel == null ? true : jenkinsPanel.couldDispose()) 
				&& (resultsPanel == null ? true : resultsPanel.couldDispose());
	}
	
	/**
	 * Shows required panel.
	 * 
	 * @author jdurani
	 * @see #showPanel(int)
	 */
	private class ShowPanelActionListener implements ActionListener {
		
		private final int requiredPanelID;
		
		private ShowPanelActionListener(int requiredPanelID) {
			this.requiredPanelID = requiredPanelID;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			showPanel(requiredPanelID);
		}
	}
	
	/**
	 * Exit application action.
	 * 
	 * @author jdurani
	 *
	 */
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




















