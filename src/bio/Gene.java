package bio;

public class Gene extends DNASequence {
	
	private String name;
	
	public Gene(String name, Orientation strand, int chromosomeIndex,
			int startPosition, int endPosition) {
		super(strand, chromosomeIndex, startPosition, endPosition);
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String toString() {
		return name + "\t" + super.toString();
	}
	
	public boolean equals(Object arg0) {
		Gene gene = (Gene) arg0;
		if (gene.getChromosomeIndex() == this.getChromosomeIndex() &&
				gene.getStartPosition() == this.getStartPosition() &&
				gene.getEndPosition() == this.getEndPosition())
			return true;
		else
			return false;
	}

}
