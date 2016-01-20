package engine.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import bio.DNASequence;

public class ChromosomeReader implements FastaFormatReader {

	private FileChannel chromosomeSequence;
	
	private int offset;
	private int length;
	
	public ChromosomeReader(Path chromosomeSource) {
		try {
			BufferedReader reader = Files.newBufferedReader(chromosomeSource, Charset.forName("UTF-8"));
			
			offset = reader.readLine().length();
			length = reader.readLine().length();
			reader.close();
			
			this.chromosomeSequence = FileChannel.open(chromosomeSource);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void complete(DNASequence seq) throws IOException {
		int off = seq.getStartPosition();
		int len = seq.getEndPosition();
		
		// Corrected values after incorporating line terminators and offset
		off = offset + off + (off-1)/length;
		len = offset + len + (len-1)/length + 1;
		
		ByteBuffer bbuf = chromosomeSequence.map(FileChannel.MapMode.READ_ONLY, off, len-off);
		CharBuffer cbuf = Charset.forName("UTF-8").newDecoder().decode(bbuf);
		
		String result = cbuf.toString().replaceAll("\n", "");
		
		// Complete sequence after removing line terminators
		seq.setSequence(result, seq.getOrientation());
	}

	@Override
	public void close() throws IOException {
		chromosomeSequence.close();
	}

}