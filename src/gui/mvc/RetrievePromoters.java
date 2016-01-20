package gui.mvc;

import java.util.Collection;

import javax.swing.text.AttributeSet;

import util.Logger;
import engine.handler.DatabaseHandler;
import bio.Promoter;

public class RetrievePromoters extends Function<Collection<Promoter>, DatabaseHandler, Logger> {

	private Collection<String> geneNames;
	
	public RetrievePromoters(Collection<String> geneNames) {
		this.geneNames = geneNames;
	}
	
	@Override
	public void execute(DatabaseHandler handler, Logger logger,
			AttributeSet attr) {
		if (geneNames != null)
			setResult(handler.findPromoters(geneNames));
		else
			setResult(handler.findPromoters());
		
		geneNames = null;
	}

	@Override
	public void output(Logger logger) {
		logger.println();
		
		int delimiterInsertPosition = 80;
		for(Promoter promoter : getResult()) {
			logger.println(">" + promoter.getGeneReference() + "|" +
					promoter.getOrientation().name());
			
			StringBuilder sequence = new StringBuilder(promoter.getSequence());
			int fullRows = (int) Math.floor(sequence.length() / delimiterInsertPosition);
			for(int i = 0; i < fullRows; i++)
				sequence.insert((i+1)*(delimiterInsertPosition + 1) - 1, '\n');
			
			logger.println(sequence.toString());
		}
		
		logger.println();
	}

}
