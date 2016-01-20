package gui.mvc;

import javax.swing.text.AttributeSet;

import util.Logger;
import engine.handler.Handler;

public interface Procedure<H extends Handler, L extends Logger> {

	void execute(H handler, L logger, AttributeSet attr);
}