package bio;

public class DNASequence {

	private String sequence = null;
	private Orientation strand;
	
	private int chromosomeIndex;
	private int startPosition;
	private int endPosition;
	
	public DNASequence(Orientation strand, int chromosomeIndex,
			int startPosition, int endPosition) {
		
		this(null, strand, chromosomeIndex, startPosition, endPosition);
	}
	
	public DNASequence(String sequence, Orientation strand, int chromosomeIndex,
			int startPosition, int endPosition) {
		
		this.sequence = sequence;
		this.strand = strand;
		
		this.chromosomeIndex = chromosomeIndex;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
	}
	
	public String getSequence() {
		if (this.sequence == null)
			return null;
		
		return sequence;
	}
	
	public void setSequence(String sequence, Orientation strand) {
		this.sequence = sequence;
		this.strand = strand;
	}
	
	public Orientation getOrientation() {
		return this.strand;
	}
	
	public int getChromosomeIndex() {
		return this.chromosomeIndex;
	}
	
	public int getStartPosition() {
		return this.startPosition;
	}
	
	public void setStartPosition(int newStartPosition) {
		// Setting a new position destroys the sequence, future implementation
		// may allow shortening using subSequence()
		this.startPosition = newStartPosition;
		setSequence(null, this.strand);
	}
	
	public int getEndPosition() {
		return this.endPosition;
	}
	
	public void setEndPosition(int newEndPosition) {
		// Setting a new position destroys the sequence, future implementation
		// may allow shortening using subSequence()
		this.endPosition = newEndPosition;
		setSequence(null, this.strand);
	}
	
	public String toString() {
		return "Chr" + chromosomeIndex + "\t" + getStartPosition() + "\t" + getEndPosition();
	}
	
	public enum Orientation {
		FORWARD, REVERSE, UNDEFINED
	}

}