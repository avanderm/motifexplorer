package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Formatter;

public class FileLogger implements Logger {

	private File file;
	private FileOutputStream stream;
	private Formatter formatter;
	
	public FileLogger(File file) {
		this.file = file;
		this.stream = null;
	}
	
	@Override
	public void open() {
		try {
			stream = new FileOutputStream(file);
			formatter = new Formatter(stream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			file = null;
		}
	}
	
	@Override
	public void close() {
		try {
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void print(String string) {
		byte[] b;
		try {
			b = string.getBytes("UTF-8");
			stream.write(b);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void println() {
		print("\n");
	}

	@Override
	public void println(String string) {
		print(string + "\n");
	}

	@Override
	public void format(String format, Object... args) {
		formatter.format(format, args);
	}

}