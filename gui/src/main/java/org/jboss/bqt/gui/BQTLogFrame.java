package org.jboss.bqt.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.jboss.bqt.gui.panels.GUIRunnerPanel;
import org.jboss.bqt.gui.panels.MainPanel;

/**
 * This frame shows log of running BQT.
 * 
 * @author jdurani
 */
@SuppressWarnings("serial")
public class BQTLogFrame extends JFrame {

	/**
	 * BQT status.
	 */
	public static enum Status {DONE, IN_PROGRESS, FAILED}
	
	private static final Color DONE_COLOR = new Color(34, 139, 34);
	private static final Color FAILED_COLOR = new Color(220, 20, 60);
	private static final Color IN_PROGRESS_COLOR = new Color(184, 134, 11);
	private static final Color DEFAULT_COLOR = Color.BLACK;
	
	private static final Color TRACE_COLOR = DONE_COLOR;
	private static final Color DEBUG_COLOR = DONE_COLOR;
	private static final Color INFO_COLOR = Color.BLACK;
	private static final Color WARN_COLOR = IN_PROGRESS_COLOR;
	private static final Color ERROR_COLOR = FAILED_COLOR;
	
	private JTextPane logPane;
	private StyledDocument doc;
	private JLabel statusLabel;
	private Status actualStataus = Status.DONE;
	private OutputStream logAreaStream;

	private final GUIRunnerPanel parent;
	
	/**
	 * Creates a new instance. 
	 * 
	 * @param parent parent window 
	 */
	public BQTLogFrame(GUIRunnerPanel parent) {
		super();
		this.parent = parent;
		init();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle("BQT log");
		pack();
		setLocationRelativeTo(null);
	}

	/**
	 * Initializes this window.
	 */
	private void init() {
		logPane = new JTextPane();
		logPane.setEditable(false);
		JPanel logPanePanel = new JPanel(new BorderLayout());
		logPanePanel.add(logPane);
		initStyles();
		JScrollPane pane = MainPanel.getScrollPane(logPanePanel);

		statusLabel = new JLabel("           ");
		
		JPanel panel = new JPanel();

		GroupLayout gl = new GroupLayout(panel);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		gl.setHorizontalGroup(gl.createSequentialGroup()
			.addGap(10, 10 ,10)
			.addGroup(gl.createParallelGroup(Alignment.CENTER, true)
				.addComponent(pane, 800, 800, 2000)
				.addComponent(statusLabel, 100, 100, 100))
			.addGap(10, 10, 10));
		gl.setVerticalGroup(gl.createParallelGroup(Alignment.CENTER, true)
			.addGroup(gl.createSequentialGroup()
				.addComponent(pane, 600, 600, 2000)
				.addComponent(statusLabel, 30, 30, 30)));

		panel.setLayout(gl);
		add(panel);
		logAreaStream = new JTextPaneOutputStream();
	}

	/**
	 * Initializes styles. Every log level will have different color.
	 */
	private void initStyles(){
		doc = logPane.getStyledDocument();
		Style trace = doc.addStyle("trace", null);
		Style debug = doc.addStyle("debug", null);
		Style info = doc.addStyle("info", null);
		Style warn = doc.addStyle("warn", null);
		Style error = doc.addStyle("error", null);
		Style def = doc.addStyle("default", null);
		
		StyleConstants.setForeground(debug, TRACE_COLOR);
		StyleConstants.setForeground(trace, DEBUG_COLOR);
		StyleConstants.setForeground(info, INFO_COLOR);
		StyleConstants.setForeground(warn, WARN_COLOR);
		StyleConstants.setForeground(error, ERROR_COLOR);
		StyleConstants.setForeground(def, DEFAULT_COLOR);
	}
	
	/**
	 * Disposes this widow. If BQT is still running (progress is {@link Status#IN_PROGRESS})
	 * then it will call {@link GUIRunnerPanel#cancelActualJob(boolean)}. If the method returns
	 * true, then this window will be disposed.
	 * 
	 * @see #setStatus(Status)
	 * @see JFrame#dispose()
	 */
	@Override
	public void dispose() {
		if(actualStataus == Status.IN_PROGRESS){
			if(parent.cancelActualJob(true)){
				super.dispose();
			}
		} else {
			super.dispose();
		}
	}
	
	/**
	 * Sets status of BQT.  
	 * 
	 * @param status status to be set
	 */
	public void setStatus(Status status) {
		statusLabel.setText(status.toString());
		switch(status){
			case DONE:
				statusLabel.setForeground(DONE_COLOR);
				break;
			case FAILED:
				statusLabel.setForeground(FAILED_COLOR);
				break;
			case IN_PROGRESS:
				statusLabel.setForeground(IN_PROGRESS_COLOR);
				break;
			default:
				statusLabel.setForeground(DEFAULT_COLOR);
		}
		actualStataus = status;
		repaint();
	}
	
	/**
	 * Returns an {@link OutputStream} which prints bytes to this {@link JTextPane}.
	 * 
	 * @return
	 */
	public OutputStream getLogAreaStream() {
		return logAreaStream;
	}

	/**
	 * Private {@link OutputStream} which prints the output to our {@link JTextPane}
	 * ({@link BQTLogFrame#logPane}).
	 * 
	 * @author jdurani
	 *
	 */
	private class JTextPaneOutputStream extends OutputStream {

		public void write(byte b[], int off, int len) throws IOException {
			if (b == null) {
				throw new NullPointerException();
			} else if ((off < 0) || (off > b.length) || (len < 0)
					|| ((off + len) > b.length) || ((off + len) < 0)) {
				throw new IndexOutOfBoundsException();
			} else if (len == 0) {
				return;
			}
			String text = new String(b, off, len);
			String[] splitted = text.split("[ ]+"); 
			String level = (splitted != null && splitted.length > 1) ? splitted[1].toLowerCase() : "default";
			try {
				Style s = doc.getStyle(level);
				s = s == null ? doc.getStyle("default") : s;
				doc.insertString(doc.getLength(), text, s);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			logPane.getCaret().setDot(doc.getLength());
		}

		@Override
		public void write(int b) throws IOException {
			try {
				doc.insertString(doc.getLength(), Character.toString((char)b), doc.getStyle("default"));
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			logPane.getCaret().setDot(doc.getLength());
		}
	}

}
