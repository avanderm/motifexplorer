package engine.io;

import java.io.IOException;

import bio.DNASequence;

public interface FastaFormatReader {
	
	void complete(DNASequence seq) throws IOException;
	void close() throws IOException;
}
