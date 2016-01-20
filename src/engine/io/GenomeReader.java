package engine.io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;

import bio.DNASequence;

public class GenomeReader implements FastaFormatReader {

	private HashMap<Integer, ChromosomeReader> chromosomes;
	
	public GenomeReader() {
		chromosomes = new HashMap<Integer, ChromosomeReader>();
	}
	
	public void addChromosome(int chromosomeIndex, Path source) {
		chromosomes.put(chromosomeIndex, new ChromosomeReader(source));
	}
	
	@Override
	public void complete(DNASequence seq) throws IOException {
		chromosomes.get(seq.getChromosomeIndex()).complete(seq);
	}

	@Override
	public void close() throws IOException {
		for(ChromosomeReader chromosome : chromosomes.values())
			chromosome.close();
	}

}
