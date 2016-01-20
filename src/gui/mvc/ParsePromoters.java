package gui.mvc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;

import engine.PromoterExtractor;
import engine.handler.DatabaseHandler;
import engine.io.GenomeReader;
import engine.parser.TAIRParser;
import util.InputLogger;
import util.Logger;

public class ParsePromoters implements Procedure<DatabaseHandler, Logger> {

	private String fileName;
	private boolean overwrite;
	
	public ParsePromoters(String fileName, boolean overwrite) {
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
					handler.deletePromoters();
				
				TAIRParser tairParser = new TAIRParser();
				
				GenomeReader genome = new GenomeReader();
				publish("Constructing genome reader... ");
				genome.addChromosome(1, Paths.get("data/chr1.fas"));
				genome.addChromosome(2, Paths.get("data/chr2.fas"));
				genome.addChromosome(3, Paths.get("data/chr3.fas"));
				genome.addChromosome(4, Paths.get("data/chr4.fas"));
				genome.addChromosome(5, Paths.get("data/chr5.fas"));
				genome.addChromosome(6, Paths.get("data/chrC.fas"));
				genome.addChromosome(7, Paths.get("data/chrM.fas"));
				publish("OK\n");
				PromoterExtractor extractor = new PromoterExtractor(tairParser, genome);
				
				InputStream stream;
				AttributeSet attr = new SimpleAttributeSet();
				try {
					publish("Streaming data file [" + fileName + "]... ");
					stream = new FileInputStream(fileName);
					publish("OK\n");
					publish("Parsing promoters... ");
					extractor.parse(stream, handler, logger, attr);
					publish("OK\n");
					
					publish(extractor.getPromoters().size() + " promoters added.\n");
					
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