package util;

public interface InputLogger extends Logger {

	void lock();
	void unlock();
	
	boolean isLocked();
}
