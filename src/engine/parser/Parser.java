package engine.parser;


import java.io.InputStream;

import javax.swing.text.AttributeSet;

import engine.handler.Handler;

import util.Logger;

public abstract class Parser<H extends Handler> {
	
	private boolean interrupt = false;
	
	public void interrupt() {
		interrupt = true;
	}
	
	public boolean isInterrupted() {
		return interrupt;
	}
	
	public void parse(InputStream stream, H handler, Logger logger, AttributeSet attr) {
		if (!attr.containsAttribute("suppressHandlerStart", true))
			handler.startHandler();
		parseContent(stream, handler, logger, attr);
		
		if (!attr.containsAttribute("suppressHandlerEnd", true))
			handler.endHandler();
	}

	protected abstract void parseContent(InputStream stream, H handler, Logger logger, AttributeSet attr);
}