package org.jboss.bqt.gui.appender;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.jboss.bqt.gui.panels.GUIRunnerPanel.BQTProperties;

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
	
	private static final Object LOCK = new Object();
	private static OutputStream OUTPUT_STREAM = null;
	private static FileOutputStream FILE_OUTPUT_STREAM = null; 
	
	private static String fileName;
	
	/**
	 * Create a new instance.
	 * 
	 * @param name name of the appender
	 * @param fileName name of file for the second file output stream
	 * @param filter filter
	 * @param layout layout
	 */
	protected GUIAppender(String name, String fileName, Filter filter,
			Layout<? extends Serializable> layout) {
		super(name, filter, layout, false);
		GUIAppender.fileName = fileName;
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
            @PluginAttribute(value="filename", defaultString="bqt.log") final String fileName, 
            @PluginAttribute("name") final String name) {
        if (name == null) {
            LOGGER.error("No name provided for GUIAppender");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        
        return new GUIAppender(name, fileName, filter, layout);
    }

	@Override
	public void append(LogEvent event) {
		synchronized (LOCK) {
			if(OUTPUT_STREAM != null){
				try{
					byte[] byteArray = getLayout().toByteArray(event);
					OUTPUT_STREAM.write(byteArray);
					if(FILE_OUTPUT_STREAM != null){
						FILE_OUTPUT_STREAM.write(byteArray);
					}
				} catch (Exception ex){
					LOGGER.error("Unable to write a message.", ex);
				}
			}
		}
	}
	
	/**
	 * <p>
	 * Clears previously set output stream. The output stream will not
	 * be closed.
	 * </p>
	 * <p>
	 * Along with the output stream, the second file output stream will be cleared too.
	 * As the second stream is managed by this class, it will be closed.
	 * </p>
	 * 
	 * @see GUIAppender#setOutputStream(OutputStream)
	 */
	public static void clearOutputStream(){
		synchronized (LOCK) {
			OUTPUT_STREAM = null;
			closeFileOutputStream();
			FILE_OUTPUT_STREAM = null;
		}
	}
	
	/**
	 * Close the file output stream.
	 */
	private static void closeFileOutputStream(){		
		try{
			FILE_OUTPUT_STREAM.close();
		} catch (NullPointerException ignore1){
		} catch (IOException ignore2){}
	}
	
	/**
	 * <p>
	 * Sets the output stream for this class. Along with setting, a new
	 * file output stream will be opened. If the file output stream is already opened,
	 * old stream will be closed.
	 * </p>
	 * <p>
	 * The name of file depends on system's property {@link BQTProperties#LOG_DIR} and
	 * appender's property {@code fileName}.
	 * </p>
	 * <pre>
	 * String logDir = System.getProperty(BQTProperties.LOG_DIR, ".");
	 * File logFile = new File(logDir, fileName);
	 * </pre>
	 * 
	 * @param os output stream
	 * 
	 * @see GUIAppender#clearOutputStream()
	 */
	public static void setOutputStream(OutputStream os){
		synchronized (LOCK) {
			OUTPUT_STREAM = os;
			closeFileOutputStream();
			try{
				String logDir = System.getProperty(BQTProperties.LOG_DIR, ".");
				File logFile = new File(logDir, fileName);
				if(!logFile.exists()){
					File parentDir = logFile.getParentFile();
					if(!parentDir.exists()){
						parentDir.mkdirs();
					}
					logFile.createNewFile();
				}
				FILE_OUTPUT_STREAM = new FileOutputStream(logFile, true);
			} catch (Exception ex){
				ex.printStackTrace();
				FILE_OUTPUT_STREAM = null;
			}
		}
	}
}

















