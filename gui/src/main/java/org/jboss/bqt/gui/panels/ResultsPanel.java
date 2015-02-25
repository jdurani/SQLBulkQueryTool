package org.jboss.bqt.gui.panels;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * The panel for Results.
 * @author jdurani
 *
 */
@SuppressWarnings("serial")
public class ResultsPanel extends JPanel {

	public ResultsPanel() {
		init();
	}
	
	private void init(){
		//TODO
		add(new JLabel("No results panel yet."));
	}
	
	public boolean couldDispose(){
		//TODO
		return true;
	}
}
