package gui.mvc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;
import javax.swing.text.AttributeSet;

import bio.Motif;
import bio.Promoter;

import util.FileLogger;
import util.InputLogger;
import util.Logger;

import engine.handler.DatabaseHandler;
import engine.rsatws.MatrixScanRequest;
import engine.rsatws.MatrixScanResponse;
import engine.rsatws.Monitor;
import engine.rsatws.RSATConnection;
import engine.rsatws.SOAPRequestFactory;
import engine.rsatws.Ticket;

public class MatrixScan extends Function<String, DatabaseHandler, Logger> {

	private static final int BATCH_SIZE = 10;
	private static final int MONITOR_WAIT_TIME_MS = 1000;
	
	private static int counter = 1;
	
	private Collection<Promoter> promoters;
	private Collection<Motif> motifs;
	
	public MatrixScan(Collection<Promoter> promoters, Collection<Motif> motifs) {
		this.motifs = motifs;
		this.promoters = promoters;
		
		setResult("");
	}
	
	@Override
	public void execute(DatabaseHandler handler, final Logger logger,
			final AttributeSet attr) {

		if (promoters.size() == 0) {
			logger.println("No promoters selected");
			return;
		}
		
		if (motifs.size() == 0) {
			logger.println("No motifs selected");
			return;
		}
		
		if (attr.containsAttribute("usingInputLogger", true))
			((InputLogger) logger).lock();
		
		logger.println();
		
		(new SwingWorker<Void, String>() {

			@Override
			protected Void doInBackground() throws Exception {
				String result = "";
				
				publish("Concatenating matrices... ");
				result += "#matrix\t\tname\n";
				String matrix = "";
				int matrixCount = 1;
				for(Motif motif : motifs) {
					matrix += motif.toString() + "\n//\n";
					result += ";matrix-scan." + ((matrixCount++)+1) + '\t' + motif.getName() + '\n';
				}
				publish("Done\n");
				
				int n = (int) Math.ceil((double) promoters.size()/BATCH_SIZE);
				int c = 0;
				
				Iterator<Promoter> iter = promoters.iterator();
				Collection<Promoter> batch = new ArrayList<Promoter>(BATCH_SIZE);
				
				publish("Establishing connection to RSAT webserver... ");
				RSATConnection conn = new RSATConnection();
				conn.open();
				publish("Done\n");
				
				do {
					batch.add(iter.next());
					if (batch.size() == BATCH_SIZE || !iter.hasNext()) {
						publish("Submitting and processing job for batch " + (++c) + " of " + n + "... ");
						
						String sequence = "";
						
						// Choosing primary keys to represent promoter in output result
						int delimiterInsertPosition = 80;
						for(Promoter promoter : batch) {
							sequence += '>' + promoter.getGeneReference() + '|' +
								promoter.getOrientation().name() + '\n';
							
							StringBuilder sequenceBuilder = new StringBuilder(promoter.getSequence());
							int fullRows = (int) Math.floor(sequenceBuilder.length() / delimiterInsertPosition);
							for(int i = 0; i < fullRows; i++)
								sequenceBuilder.insert((i+1)*(delimiterInsertPosition + 1) - 1, '\n');
							
							sequence += sequenceBuilder.toString() + '\n';
						}
						
						MatrixScanRequest request = SOAPRequestFactory.createMatrixScanRequest(
								sequence, matrix);
						
						Ticket<MatrixScanResponse> ticket = conn.ticket(request);
						
						Monitor monitor = new Monitor(ticket);
						do {
							try {
								Thread.sleep(MONITOR_WAIT_TIME_MS);
								monitor.update(conn);
							} catch (InterruptedException e) {
								logger.println("Interrupting progress");
							}
						} while(!monitor.isJobFinished());
						
						publish("Done\n");
						
						MatrixScanResponse response = conn.retrieve(ticket);
						
						result = result.concat(response.getContent());
						batch.clear();
					}
				} while(iter.hasNext());
				
				publish("Disconnecting from RSAT webserver... ");
				conn.close();
				publish("Done\n");
				
				setResult(result);
				
				String fileName;
				File file;
				do {
					fileName = "output/data_" + String.format("%03d", counter++) + ".txt"; 
					file = new File(fileName);
				} while(file.exists());
				
				FileLogger fileLogger = new FileLogger(file);
				fileLogger.open();
				publish("Writing result to file: " + fileName + "... ");
				output(fileLogger);
				fileLogger.close();
				publish("Done\n");
				
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

	@Override
	public void output(final Logger logger) {
		logger.print(getResult());
	}

}