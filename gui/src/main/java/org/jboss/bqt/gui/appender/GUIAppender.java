package org.jboss.bqt.gui.appender;

import java.awt.Color;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * Log Appender. The user can dynamically change an output stream for this appender.
 * This class is thread safe.
 * 
 * @author jdurani
 *
 */
@SuppressWarnings("serial")
@Plugin(name = "GUI", category = "Core", elementType = "appender", printObject = true)
public final class GUIAppender extends AbstractAppender {
	
	private static final Color TRACE_COLOR = new Color(34, 139, 34);
	private static final Color DEBUG_COLOR = new Color(34, 139, 34);
	private static final Color INFO_COLOR = Color.BLACK;
	private static final Color WARN_COLOR = new Color(184, 134, 11);
	private static final Color ERROR_COLOR = new Color(220, 20, 60);
	private static final Color DEFAULT_COLOR = Color.BLACK;
	
	private static final Map<String, JTextPane> TEXT_PANES = new HashMap<String, JTextPane>();
	
	private JTextPane textPane;
	
	/**
	 * Create a new instance.
	 * 
	 * @param name name of the appender
	 * @param fileName name of file for the second file output stream
	 * @param filter filter
	 * @param layout layout
	 */
	protected GUIAppender(String name, Filter filter,
			Layout<? extends Serializable> layout) {
		super(name, filter, layout, false);
		initTextPane();
	}
	
	private void initTextPane(){
		textPane = new JTextPane();
		textPane.setEditable(false);
		TEXT_PANES.put(getName(), textPane);
		StyledDocument doc = textPane.getStyledDocument();
		Style trace = doc.addStyle(Level.TRACE.name(), null);
		Style debug = doc.addStyle(Level.DEBUG.name(), null);
		Style info = doc.addStyle(Level.INFO.name(), null);
		Style warn = doc.addStyle(Level.WARN.name(), null);
		Style error = doc.addStyle(Level.ERROR.name(), null);
		Style def = doc.addStyle("default", null);
		
		StyleConstants.setForeground(debug, TRACE_COLOR);
		StyleConstants.setForeground(trace, DEBUG_COLOR);
		StyleConstants.setForeground(info, INFO_COLOR);
		StyleConstants.setForeground(warn, WARN_COLOR);
		StyleConstants.setForeground(error, ERROR_COLOR);
		StyleConstants.setForeground(def, DEFAULT_COLOR);
	}

	/**
     * Create a Console Appender.
     * @param layout The layout to use (required).
     * @param filter The Filter or null.
     * @param fileName The name of the file (default bqt.log).
     * @param name The name of the Appender (required).
     * @return The GUIAppender.
     */
	@PluginFactory
    public static GUIAppender createAppender(
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter, 
            @PluginAttribute("name") final String name) {
        if (name == null) {
            LOGGER.error("No name provided for GUIAppender");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        
        return new GUIAppender(name, filter, layout);
    }

	@Override
	public void append(LogEvent event) {
		try{
			byte[] byteArray = getLayout().toByteArray(event);
			writeToTextPane(byteArray, event.getLevel());
		} catch (Exception ex){
			LOGGER.error("Unable to write a message.", ex);
		}
	}
	
	/**
	 * This method writes byte array to JTextOutpuStream
	 * @param bArray
	 * @param level
	 */
	private void writeToTextPane(byte[] bArray, Level level){
		if (bArray == null) {
			return;
		}
		String text = new String(bArray); 
		try {
			StyledDocument doc = textPane.getStyledDocument();
			Style s = doc.getStyle(level.name());
			s = s == null ? doc.getStyle("default") : s;
			doc.insertString(doc.getLength(), text, s);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns an instance of {@link JTextPane} that is associated with appender
	 * with name {@code appendersName}.
	 * 
	 * @param appendersName name of the appender
	 * @return
	 */
	public static final JTextPane getTextPane(String appendersName){
		return TEXT_PANES.get(appendersName);
	}
}

















