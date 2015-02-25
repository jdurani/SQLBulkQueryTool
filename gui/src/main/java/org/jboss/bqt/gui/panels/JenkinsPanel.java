package org.jboss.bqt.gui.panels;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * The panel for Jenkins.
 * 
 * @author jdurani
 *
 */
@SuppressWarnings("serial")
public class JenkinsPanel extends JPanel {

	public JenkinsPanel() {
		init();
	}
	
	private void init(){
		//TODO
		add(new JLabel("No jenkins panel yet."));
	}
	
	public boolean couldDispose(){
		//TODO
		return true;
	}
}
