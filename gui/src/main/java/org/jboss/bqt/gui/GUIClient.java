package org.jboss.bqt.gui;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GUIClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(GUIClient.class);
	
	public static void main(String[] args) {
		new GUIClient().start();
	}
	
	public void start(){
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				LOGGER.debug("Starting main JFrame.");
				new GUIFrame().setVisible(true);
			}
		});
	}

}
