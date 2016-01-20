package gui;

import gui.mvc.Controller;

import java.awt.*;
import javax.swing.*;

import util.InputLogger;

public class Terminal implements InputLogger {

	private JFrame terminalFrame;
	private Console console;
	
	public Terminal(Controller controller) {
		terminalFrame = new JFrame("MotifExplorer Terminal");
		terminalFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Container contentPane = terminalFrame.getContentPane();
		contentPane.setLayout(new GridLayout(1,1));
		
		console = new Console(controller);
		
		contentPane.add(console.getComponent());
		terminalFrame.pack();
		
		terminalFrame.setVisible(true);
	}
	
	@Override
	public void open() {
		console.open();
	}
	
	@Override
	public void close() {
		console.close();
	}
	
	@Override
	public synchronized void lock() {
		console.lock();
	}
	
	@Override
	public synchronized void unlock() {
		console.unlock();
	}
	
	@Override
	public boolean isLocked() {
		return console.isLocked();
	}

	@Override
	public void print(String string) {
		console.print(string);
	}
	
	@Override
	public void println() {
		console.println();
	}

	@Override
	public void println(String string) {
		console.println(string);
	}

	@Override
	public void format(String format, Object... args) {
		console.format(format, args);
	}
	
}