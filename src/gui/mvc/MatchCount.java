package gui.mvc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.swing.text.AttributeSet;

import util.Logger;
import engine.handler.DatabaseHandler;
import engine.parser.RSATParser;

public class MatchCount implements Procedure<DatabaseHandler, Logger> {

	private File file;
	
	public MatchCount(File file) {
		this.file = file;
	}
	
	@Override
	public void execute(DatabaseHandler handler, Logger logger,
			AttributeSet attr) {

		logger.println();
		try {
			InputStream stream = new FileInputStream(file);
			RSATParser parser = new RSATParser();
			
			parser.parse(stream, handler, logger, attr);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			logger.println();
		}
	}

}