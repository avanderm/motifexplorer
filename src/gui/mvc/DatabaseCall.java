package gui.mvc;

import javax.swing.text.AttributeSet;

import util.Logger;

import engine.handler.DatabaseHandler;

public class DatabaseCall implements Procedure<DatabaseHandler, Logger> {

	private String call;
	
	public DatabaseCall(String call) {
		this.call = call;
	}
	
	@Override
	public void execute(DatabaseHandler handler, Logger logger,
			AttributeSet attr) {
		
		System.out.println(call);
		switch(call) {
		case "drop":
			handler.dropAll();
			break;
		case "delete":
			handler.deleteAll();
			break;
		case "init":
			handler.initAll();
			break;
		}
	}

}