package gui.mvc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;
import javax.swing.text.AttributeSet;

import util.InputLogger;
import util.Logger;
import engine.handler.DatabaseHandler;
import engine.parser.PWMParser;

public class ParseMotifs implements Procedure<DatabaseHandler, Logger> {

	private String fileName;
	private boolean overwrite;
	
	public ParseMotifs(String fileName, boolean overwrite) {
		this.fileName = fileName;
		this.overwrite = overwrite;
	}

	@Override
	public void execute(final DatabaseHandler handler, final Logger logger,
			final AttributeSet attr) {
		
		if (attr.containsAttribute("usingInputLogger", true))
			((InputLogger) logger).lock();
		
		logger.println();
		
		(new SwingWorker<Void, String>() {

			@Override
			protected Void doInBackground() throws Exception {
				if (overwrite)
					handler.deleteMotifs();
				
				PWMParser motifParser = new PWMParser();
				
				InputStream stream;
				try {
					publish("Streaming data file [" + fileName + "]... ");
					stream = new FileInputStream(fileName);
					publish("OK\n");
					publish("Parsing motifs... ");
					motifParser.parse(stream, handler, logger, attr);
					publish("OK\n");
					stream.close();
				} catch (FileNotFoundException e) {
					publish("ERROR\nNo such file: " + fileName + '\n');
				} catch (IOException e) {
					publish("ERROR\n");
				}
				
				return null;
			}
			
			@Override
			protected void process(List<String> chunks) {
				for (String message : chunks)
					logger.print(message);
			}
			
			@Override
			protected void done() {
				try {
					get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				} finally {
					logger.println();
					
					if (attr.containsAttribute("usingInputLogger", true))
						((InputLogger) logger).unlock();
				}
			}
			
		}).execute();
	}

}