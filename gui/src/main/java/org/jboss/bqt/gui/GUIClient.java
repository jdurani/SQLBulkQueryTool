package org.jboss.bqt.gui;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class with main(String[]) method. This class starts GUI tool.
 * 
 * @author jdurani
 */
public class GUIClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(GUIClient.class);
	
	/**
	 * Main method.
	 * 
	 * @param args arguments are being ignored
	 */
	public static void main(String[] args) {
		new GUIClient().start();
	}
	
	/**
	 * Shows GUI window.
	 * 
	 * @see SwingUtilities#invokeLater(Runnable)
	 */
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
