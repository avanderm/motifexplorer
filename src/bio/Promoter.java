package bio;


public class Promoter extends DNASequence {

	private String geneReference;
	
	public Promoter(Gene gene, Orientation strand, int startPosition, int endPosition) {
		this(gene.getName(), strand, gene.getChromosomeIndex(), startPosition, endPosition);
	}
	
	public Promoter(String geneReference, Orientation strand, int chromosomeIndex,
			int startPosition, int endPosition) {
		
		this(geneReference, null, strand, chromosomeIndex, startPosition, endPosition);
	}
	
	public Promoter(String geneReference, String sequence, Orientation strand,
			int chromosomeIndex, int startPosition, int endPosition) {
		super(sequence, strand, chromosomeIndex, startPosition, endPosition);
		this.geneReference = geneReference;
	}
	
	public String getGeneReference() {
		return geneReference;
	}
	
	public String toString() {
		switch(getOrientation()) {
		case FORWARD:
			return getGeneReference();
		case REVERSE:
			return getGeneReference() + "os";
		default:
			return getGeneReference();
		}
	}

}