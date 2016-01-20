package engine.rsatws;

public class MatrixScanRequest extends SOAPJob<MatrixScanResponse> {

	private String sequence;
	private String matrix;
	
	public MatrixScanRequest(final String sequence, final String matrix) {
		super();
		
		this.sequence = sequence;
		this.matrix = matrix;
	}
	
	public String getSequence() {
		return sequence;
	}
	
	public String getMatrix() {
		return matrix;
	}
	
	@Override
	protected String getServiceName() {
		return "matrix_scan";
	}

	@Override
	protected void completeRequest() {
		// Sequence in fasta format
		super.addNode("sequence", getSequence());
		super.addNode("sequence_format", "fasta");
		
		// Matrix in tab format
		super.addNode("matrix", getMatrix());
		super.addNode("matrix_format", "tab");
		
		super.addNode("organism", "Arabidopsis_thaliana");
		super.addNode("background", "upstream-noorf");
		super.addNode("markov", "2");
		super.addNode("lth", "score 4");
		super.addNode("n_treatment", "score");
		
		// Return fields
		super.addNode("return_fields", "sites");
	}

	@Override
	protected MatrixScanResponse constructResponse(String content) {
		return new MatrixScanResponse(content);
	}

}