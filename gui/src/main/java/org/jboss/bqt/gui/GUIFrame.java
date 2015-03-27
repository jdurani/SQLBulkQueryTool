package org.jboss.bqt.gui;


import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.jboss.bqt.gui.panels.GUIRunnerPanel;

/**
 * Main frame of GUI tool. Contains only one panel of type {@link MainPanel}.
 * 
 * @author jdurani
 */
@SuppressWarnings("serial")
public class GUIFrame extends JFrame {

	private GUIRunnerPanel guiPanel;
	
	/**
	 * Creates a new instance.
	 */
	public GUIFrame(){
		super();
		init();
	}
	
	/**
	 * Initializes this frame.
	 */
	private void init(){
		guiPanel = new GUIRunnerPanel();
		JScrollPane pane = new JScrollPane(guiPanel);
		pane.setPreferredSize(new Dimension(1200, 800));
		add(pane);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle("BQT GUI Tool");
		pack();
		setLocationRelativeTo(null);
	}
	
	/**
	 * If {@link MainPanel#couldDispose()} returns true, then
	 * this frame will be disposed.
	 * 
	 * @see MainPanel#couldDispose()
	 * @see JFrame#dispose()
	 */
	@Override
	public void dispose() {
		if(guiPanel.couldDispose()){
			super.dispose();
		}
	}
}
