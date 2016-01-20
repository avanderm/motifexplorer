package engine.sql;

import java.nio.charset.Charset;

public class UIDFactory {

	private static final long[] byteTable;
	private static final long HSTART = 0xBB40E64DA205B064L;
	private static final long HMULT = 7664345821815920749L;

	static {
		byteTable = new long[256];
		long h = 0x544B2FBACAAF1684L;
		for (int i = 0; i < 256; i++) {
			for (int j = 0; j < 31; j++) {
				h = (h >>> 7) ^ h;
				h = (h << 11) ^ h;
				h = (h >>> 10) ^ h;
			}
			byteTable[i] = h;
		}
	}

	public static long hash(byte[] data) {
		long h = HSTART;
		final long hmult = HMULT;
		final long[] ht = byteTable;
		for (int len = data.length, i = 0; i < len; i++) {
			h = (h * hmult) ^ ht[data[i] & 0xff];
		}
		return h;
	}
	
	public static long hash(String uniqueString) {
		byte[] data = uniqueString.getBytes(Charset.forName("UTF-8"));
		
		return hash(data);
	}
}