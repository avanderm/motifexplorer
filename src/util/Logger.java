package util;

public interface Logger {

	void open();
	void close();
	
	void print(String string);
	void println();
	void println(String string);
	
	void format(String format, Object... args);
}
