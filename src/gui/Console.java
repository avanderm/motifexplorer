package gui;

import gui.mvc.Controller;

import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Formatter;

import javax.swing.*;
import javax.swing.text.*;

import util.InputLogger;

public class Console implements InputLogger {

	private static final String PROMPT = ">> ";
	private static final int TAB_SIZE = 5;
	private static final int INDENT_SIZE = 0;
	
	private JTextArea textArea;
	private JScrollPane scrollPane;
	
	private Controller controller;
	private ConsoleHistory history;
	
	private PrintStream stream;
	private Formatter formatter;
	
	private int readOnlyBoundary;
	private int currentCommandLength;
	
	private boolean indent;
	private boolean isLocked;
	
	public Console(Controller controller) {
		this(controller, 30, 120);
	}
	
	public Console(Controller controller, int rows, int cols) {
		PlainDocument doc = new PlainDocument();
		doc.setDocumentFilter(new TerminalFilter());
		textArea = new JTextArea(doc);
		textArea.setRows(rows);
		textArea.setColumns(cols);
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
		textArea.setMargin(new Insets(5, 5, 5, 5));
		textArea.setLineWrap(true);
		textArea.setTabSize(TAB_SIZE);
		
		this.controller = controller;
		controller.registerInterpreter(new ConsoleInterpreter(controller));
		this.history = new ConsoleHistory();
		
		// Set custom keymap
		Keymap oldMap = textArea.getKeymap();
		Keymap newMap = JTextComponent.addKeymap("Console keys", oldMap);
		
			// Process user-given command
			KeyStroke process = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
			Action processAction = new ProcessAction();
			newMap.addActionForKeyStroke(process, processAction);
			
			// Load in older command from history
			KeyStroke olderCommand = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
			Action olderCommandAction = new OlderCommand();
			newMap.addActionForKeyStroke(olderCommand, olderCommandAction);
			
			// Load in newer command from history
			KeyStroke newerCommand = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
			Action newerCommandAction = new NewerCommand();
			newMap.addActionForKeyStroke(newerCommand, newerCommandAction);
			
			// Load in standard command (default: blank)
			KeyStroke resetCommand = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0);
			Action resetCommandAction = new ResetCommand();
			newMap.addActionForKeyStroke(resetCommand, resetCommandAction);
		
		textArea.setKeymap(newMap);
		textArea.setEditable(false);
		
		scrollPane = new JScrollPane(textArea);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		stream = new PrintStream(new ConsoleOutputStream());
		formatter = new Formatter(stream);
		
		readOnlyBoundary = 0;
		currentCommandLength= 0;
		
		indent = true;
		lock();
	}
	
	public Component getComponent() {
		return scrollPane;
	}

	@Override
	public void open() {
		prompt();
	}
	
	@Override
	public void close() {
		
	}
	
	@Override
	public synchronized void lock() {
		textArea.setEditable(false);
		isLocked = true;
	}
	
	@Override
	public synchronized void unlock() {
		prompt();
	}
	
	@Override
	public synchronized boolean isLocked() {
		return isLocked;
	}
	
	private void prompt() {
		setIndentPolicy(false);
		
		stream.print(PROMPT);
		setIndentPolicy(true);
		
		textArea.setEditable(true);
		isLocked = false;
	}
	
	private void setIndentPolicy(boolean indent) {
		this.indent = indent;
	}
	
	private boolean getIndentPolicy() {
		return indent;
	}
	
	@Override
	public void print(String string) {
		stream.print(string);
	}
	
	@Override
	public void println() {
		stream.println();
	}

	@Override
	public void println(String string) {
		stream.println(string);
	}
	
	@Override
	public void format(String format, Object... args) {
		formatter.format(format, args);
	}
	
	private class TerminalFilter extends DocumentFilter {
		
		@Override
		public void insertString(final FilterBypass fb, final int offset, final String string,
				final AttributeSet attr) throws BadLocationException {
			int writeOffset = (offset < readOnlyBoundary)?readOnlyBoundary:offset;
			if (writeOffset < offset + string.length())
				super.insertString(fb, writeOffset, string, attr);
			
			readOnlyBoundary += string.length();
			textArea.setCaretPosition(writeOffset + string.length());
		}
		
		@Override
		public void remove(final FilterBypass fb, final int offset, final int length)
				throws BadLocationException {
			int removeOffset = (offset < readOnlyBoundary)?readOnlyBoundary:offset;
			if (removeOffset <= offset + length) {
				super.remove(fb, removeOffset, offset + length - removeOffset);
				
				currentCommandLength -= offset + length - removeOffset;
			}
			
			textArea.setCaretPosition(removeOffset);
		}
		
		@Override
		public void replace(final FilterBypass fb, final int offset, final int length,
				final String text, final AttributeSet attrs) throws BadLocationException {
			int replaceOffset = (offset < readOnlyBoundary)?readOnlyBoundary:offset;
			if (replaceOffset <= offset + length) {
				super.replace(fb, replaceOffset, offset + length - replaceOffset, text, attrs);
				textArea.setCaretPosition(replaceOffset + text.length());
				
				currentCommandLength += text.length() - (offset + length - replaceOffset);
			} else
				textArea.setCaretPosition(replaceOffset);
		}
	}
	
	class ConsoleOutputStream extends OutputStream {

		public ConsoleOutputStream() {
			
		}
		
		@Override
		public synchronized void write(final int b) throws IOException {
			SwingUtilities.invokeLater(new Runnable() {

				private boolean indentPolicy;
				
				{
					// Save current indent policy of Console for later invokation
					this.indentPolicy = getIndentPolicy();
				}
				
				@Override
				public void run() {
					try {
						int caretPosition = textArea.getCaretPosition();
						int column = caretPosition - Utilities.getRowStart(textArea, caretPosition) + 1;
						if (indentPolicy && (column > textArea.getColumns() || column < INDENT_SIZE)) {
							for (int i = 0; i < INDENT_SIZE - (column % textArea.getColumns()) + 1; i++)
								textArea.append(" ");
						}
					} catch (BadLocationException e) {
						e.printStackTrace();
					} finally {
						textArea.append(String.valueOf((char) b));
					}
				}
				
			});
		}
		
	}
	
	class ProcessAction extends TextAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8556625638040255994L;

		public ProcessAction() {
			super("process-command-action");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			println();
			
			try {
				String command = textArea.getText(readOnlyBoundary - 1, currentCommandLength + 1).trim();
				if (!command.equals(""))
					history.addCommand(command);
				
				readOnlyBoundary += currentCommandLength;
				currentCommandLength = 0;
				
				controller.interpret(command);
			} catch (BadLocationException e) {
				e.printStackTrace();
			} finally {
				if (!isLocked()) {
					prompt();
				}
			}
		}
		
	}
	
	class OlderCommand extends TextAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4080595906660635503L;

		public OlderCommand() {
			super("load-older-command");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			textArea.replaceRange(history.olderCommand(), readOnlyBoundary,
					readOnlyBoundary + currentCommandLength);
		}
		
	}
	
	class NewerCommand extends TextAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1317919152050988752L;

		public NewerCommand() {
			super("load-newer-command");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			textArea.replaceRange(history.newerCommand(), readOnlyBoundary,
					readOnlyBoundary + currentCommandLength);
		}
		
	}
	
	class ResetCommand extends TextAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1006789178345913414L;

		public ResetCommand() {
			super("blank-command");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			textArea.replaceRange(history.reset(), readOnlyBoundary,
					readOnlyBoundary + currentCommandLength);
		}
		
	}
	
	class CompleteCommand extends TextAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5043444433271553309L;

		public CompleteCommand() {
			super("complete path");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}

}