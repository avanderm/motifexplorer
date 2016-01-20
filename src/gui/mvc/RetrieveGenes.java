package gui.mvc;

import java.util.Collection;
import javax.swing.text.AttributeSet;

import util.Logger;
import engine.handler.DatabaseHandler;
import bio.Gene;

public class RetrieveGenes extends Function<Collection<Gene>, DatabaseHandler, Logger> {
	
	private Collection<String> geneNames;
	
	public RetrieveGenes(Collection<String> geneNames) {
		this.geneNames = geneNames;
	}

	@Override
	public void execute(DatabaseHandler handler, Logger logger,
			AttributeSet attr) {
		if (geneNames != null)
			setResult(handler.findGenes(geneNames));
		else
			setResult(handler.findGenes());
		
		geneNames = null;
	}

	@Override
	public void output(Logger logger) {
		logger.println();
		
		logger.format("\t%-8s\tChr%s\t%10s\t%10s\n", "Name", "#", "start", "end");
		for (Gene gene : getResult())
			logger.format("\t%-8s\tChr%d\t%10d\t%10d\n",
					gene.getName(), gene.getChromosomeIndex(),
					gene.getStartPosition(), gene.getEndPosition());
		
		logger.println();
	}

}