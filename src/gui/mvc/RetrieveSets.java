package gui.mvc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import javax.swing.text.AttributeSet;

import util.IdentifierSet;
import util.Logger;
import engine.handler.DatabaseHandler;

public class RetrieveSets extends Function<Collection<String>, DatabaseHandler, Logger> {

	private Collection<Integer> indices;
	private Collection<IdentifierSet> sets;
	
	public RetrieveSets(Collection<Integer> indices) {
		this.indices = indices;
		this.sets = null;
	}
	
	@Override
	public void execute(DatabaseHandler handler, Logger logger,
			AttributeSet attr) {
		sets = new LinkedList<IdentifierSet>();
		
		if (indices != null)
			for(Integer index : indices)
				sets.add(handler.findSet(index));
		else
			sets = handler.findSets();
		
		Collection<String> result = new TreeSet<String>();
		for(IdentifierSet set : sets)
			result.addAll(set);
		
		setResult(result);
		
		indices = null;
	}

	@Override
	public void output(Logger logger) {
		logger.println();
		
		int nbOfCols = 6;
		for (IdentifierSet set : sets) {
			List<String> list = new ArrayList<String>(set.size());
			list.addAll(set);
			
			logger.println("Set " + set.getID() + " (" + list.size() + " entries): " +
					set.getDescription() + "\n");
			
			int rowNb = (int) Math.floor(list.size() / nbOfCols);
			int remNb = list.size() - nbOfCols*rowNb;
			
			for(int i = 0; i < rowNb; i++) {
				logger.format("\t");
				for (int j = 0; j < nbOfCols; j++)
					if (j <= remNb)
						logger.format("%-15s", list.get(i + j*rowNb + j));
					else
						logger.format("%-15s", list.get(i + j*rowNb + remNb));
				
				logger.format("\n");
			}
			
			if (remNb != 0) {
				logger.format("\t");
				for (int i = 0; i < remNb; i++)
					logger.format("%-15s", list.get(rowNb*(i+1) + i));
				
				logger.println("\n");
			}
		}
		
		logger.println();
	}

}