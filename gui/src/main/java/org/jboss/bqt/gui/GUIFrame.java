package org.jboss.bqt.gui;


import javax.swing.JFrame;

import org.jboss.bqt.gui.panels.MainPanel;

@SuppressWarnings("serial")
public class GUIFrame extends JFrame {

	private MainPanel mainPanel;
	
	public GUIFrame(){
		super();
		init();
	}
	
	private void init(){
		mainPanel = new MainPanel();
		add(mainPanel);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle("BQT GUI Tool");
		pack();
		setLocationRelativeTo(null);
	}
	
	@Override
	public void dispose() {
		if(mainPanel.couldDispose()){
			super.dispose();
		}
	}
}
