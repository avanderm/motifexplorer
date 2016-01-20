package gui.mvc;

import java.util.Collection;

import javax.swing.text.AttributeSet;

import util.Logger;
import bio.Motif;
import engine.handler.DatabaseHandler;

public class RetrieveMotifs extends Function<Collection<Motif>, DatabaseHandler, Logger> {

	private Collection<Integer> motifIndices;
	
	public RetrieveMotifs(Collection<Integer> motifIndices) {
		this.motifIndices = motifIndices;
	}
	
	@Override
	public void execute(DatabaseHandler handler, Logger logger,
			AttributeSet attr) {
		if (motifIndices != null)
			setResult(handler.findMotifs(motifIndices));
		else
			setResult(handler.findMotifs());
		
		motifIndices = null;
	}

	@Override
	public void output(Logger logger) {
		logger.println();
		
		for(Motif motif : getResult()) {
			logger.println(motif.toString());
			logger.println("//");
		}
		
		logger.println();
	}

}
