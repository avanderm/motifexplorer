package engine.rsatws;

public class SOAPRequestFactory {

	public static MatrixScanRequest createMatrixScanRequest(String sequence, String matrix) {
		MatrixScanRequest request = new MatrixScanRequest(sequence, matrix);
		return request;
	}
}
