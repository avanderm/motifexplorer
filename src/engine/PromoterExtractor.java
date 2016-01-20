package engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.text.AttributeSet;

import util.Logger;

import engine.handler.DatabaseHandler;
import engine.handler.TAIRHandler;
import engine.io.FastaFormatReader;
import engine.parser.Parser;
import engine.parser.TAIRParser;

import bio.*;
import bio.DNASequence.Orientation;

public class PromoterExtractor extends Parser<DatabaseHandler> implements TAIRHandler {

	private static boolean DEBUG = false;
	private static final File logFile = new File("log/class@PromoterExtractor");
	
	private static Formatter logger = null;
	
	private TAIRParser parser;
	private FastaFormatReader genome;
	private DatabaseHandler handler;
	
	// Result storage
	private LinkedList<Promoter> ps;
	
	// Local variables for working object
	private Iterator<Gene> osipGenes;
	private Gene pastGene;
	private Gene currentGene;
	private Gene futureGene;
	
	private GeneInterval hold;
	private boolean leftClamped;
	private boolean rightClamped;
	
	// Metadata
	int promoterCount;
	
	public PromoterExtractor(TAIRParser parser, FastaFormatReader genome) {
		this.parser = parser;
		this.genome = genome;
	}
	
	@Override
	public void startHandler() {
		// Assume non-overlapping genes sorted by chromosome, followed by start position
		osipGenes = handler.findGenes().iterator();
		
		// Maximum two promoter sequences per OSIP
		ps = new LinkedList<Promoter>();
		
		pastGene = null;
		currentGene = osipGenes.next();
		if (osipGenes.hasNext())
			futureGene  = osipGenes.next(); 
		
		hold = new GeneInterval();
		
		promoterCount = 0;
		
		if (DEBUG)
			try {
				if (!logFile.exists())
					logFile.createNewFile();
				logger = new Formatter(new FileOutputStream(logFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	@Override
	public void endHandler() {
		//TODO: destructor
		
		osipGenes = null;
		
		pastGene = null;
		currentGene = null;
		futureGene = null;
		
		hold = null;
		
		if (DEBUG)
			logger.close();
		
		logger = null;
	}
	
	public LinkedList<Promoter> getPromoters() {
		return ps;
	}

	@Override
	public void process(Gene gene) {
		if (currentGene.getChromosomeIndex() == gene.getChromosomeIndex()) {
			GeneInterval next = new GeneInterval(gene.getStartPosition(), gene.getEndPosition());
			
			// Test left clamp
			if (!leftClamped) {
				if (!next.rightOf(currentGene.getStartPosition())) {
					// Left clamp moves closer to OSIP gene
					hold.setMinMax(next.min(), next.max());
					// Left clamp overlaps with OSIP gene
					if (hold.contains(currentGene.getStartPosition()))
						leftClamped = true;
				} else {
					leftClamped = true;
				}
			}
			
			// Test right clamp
			if (!rightClamped)
				if (!next.leftOf(currentGene.getEndPosition()+1))
					rightClamped = true;
			
			if (leftClamped && rightClamped) {
				Promoter leftPromoter  = null;
				Promoter rightPromoter = null;
				
				if (!hold.contains(currentGene.getStartPosition())) {
					int reach;
					if (pastGene == null || hold.max() > pastGene.getEndPosition() ||
							currentGene.getChromosomeIndex() != pastGene.getChromosomeIndex())
						reach = currentGene.getStartPosition() - hold.max() - 1;
					else
						reach = currentGene.getStartPosition() - pastGene.getEndPosition() - 1;
					
					if (reach > 1500)
						reach = 1500;
					
					if (currentGene.getOrientation().equals(Orientation.REVERSE))
						leftPromoter = new Promoter(currentGene.getName(), Orientation.REVERSE, currentGene.getChromosomeIndex(),
								currentGene.getStartPosition() - reach, currentGene.getStartPosition() - 1) ;
					else
						leftPromoter = new Promoter(currentGene.getName(), Orientation.FORWARD, currentGene.getChromosomeIndex(),
								currentGene.getStartPosition() - reach, currentGene.getStartPosition() - 1) ;
					
					try {
						genome.complete(leftPromoter);
						process(leftPromoter);
					} catch (IOException e) {
						e.printStackTrace();
					}
					ps.add(leftPromoter);
					
					if (DEBUG)
						logger.format("Chr%d\t%8d\t%8d", currentGene.getChromosomeIndex(),
								leftPromoter.getStartPosition(), leftPromoter.getEndPosition());
				} else if (DEBUG)
					logger.format("Chr%d\t%8s\t%8s", currentGene.getChromosomeIndex(), "--", "--");
				
				if (DEBUG)
					logger.format("\t%8d\t%8d", currentGene.getStartPosition(), currentGene.getEndPosition());
				
				if (!next.contains(currentGene.getEndPosition())) {
					int reach;
					if (currentGene == futureGene ||
							next.min() < futureGene.getStartPosition() ||
							futureGene.getChromosomeIndex() != currentGene.getChromosomeIndex())
						reach = next.min() - currentGene.getEndPosition() - 1;
					else
						reach = futureGene.getStartPosition() - currentGene.getEndPosition()- 1;
					
					if (reach > 1500)
						reach = 1500;
					
					if (currentGene.getOrientation().equals(Orientation.REVERSE))
						rightPromoter = new Promoter(currentGene.getName(), Orientation.FORWARD, currentGene.getChromosomeIndex(),
								currentGene.getEndPosition() + 1, currentGene.getEndPosition() + reach);
					else
						rightPromoter = new Promoter(currentGene.getName(), Orientation.REVERSE, currentGene.getChromosomeIndex(),
								currentGene.getEndPosition() + 1, currentGene.getEndPosition() + reach);
					
					try {
						genome.complete(rightPromoter);
						process(rightPromoter);
					} catch (IOException e) {
						e.printStackTrace();
					}
					ps.add(rightPromoter);
					
					if (DEBUG)
						logger.format("\t%8d\t%8d\n", rightPromoter.getStartPosition(), rightPromoter.getEndPosition());
				} else if (DEBUG)
					logger.format("\t%8s\t%8s\n", "--", "--");
				
				// Introduce shift
				pastGene    = currentGene;
				
				leftClamped  = false;
				rightClamped = false;
				
				if (!osipGenes.hasNext() && currentGene.equals(futureGene)) {
					parser.interrupt();
					return;
				}
				
				// Introduce shift
				currentGene = futureGene;
				
				if (osipGenes.hasNext())
					futureGene  = osipGenes.next();
				
				// In case of closely located OSIP genes, process once more for next gene
				process(gene);
			}
		}
	}
	
	@Override
	public void process(Promoter promoter) {
		handler.insert(promoter);
	}
	
	protected void parseContent(InputStream stream, DatabaseHandler handler, Logger logger, AttributeSet attr) {
		// Mount handler
		this.handler = handler;
		
		if (handler.countGenes() != 0)
			parser.parse(stream, this, logger, attr);
		
		// Unmount handler
		this.handler = null;
	}

}