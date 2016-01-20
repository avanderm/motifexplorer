package gui.mvc;

import javax.swing.text.AttributeSet;

import util.Logger;
import engine.handler.Handler;

public abstract class Function<O, H extends Handler, L extends Logger> {

	private O result;
	
	protected void setResult(O result) {
		this.result = result;
	}
	
	public O getResult() {
		return result;
	}
	
	public abstract void execute(H handler, L logger, AttributeSet attr);
	public abstract void output(L logger);

}
