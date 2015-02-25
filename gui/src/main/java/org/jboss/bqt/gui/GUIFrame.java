package org.jboss.bqt.gui;


import javax.swing.JFrame;

import org.jboss.bqt.gui.panels.MainPanel;

/**
 * Main frame of GUI tool. Contains only one panel of type {@link MainPanel}.
 * 
 * @author jdurani
 */
@SuppressWarnings("serial")
public class GUIFrame extends JFrame {

	private MainPanel mainPanel;
	
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
		mainPanel = new MainPanel();
		add(mainPanel);
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
		if(mainPanel.couldDispose()){
			super.dispose();
		}
	}
}
