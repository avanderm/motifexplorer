package gui.mvc;

import util.Interpreter;
import util.Logger;
import engine.handler.DatabaseHandler;

public interface Controller {

	void interpret(String string);
	
	void process(Procedure<DatabaseHandler, Logger> procedure);
	void process(Function<?, DatabaseHandler, Logger> function);
	void print(Function<?, DatabaseHandler, Logger> function);
	
	Function<?, DatabaseHandler, Logger> collect();
	
	void registerLogger(Logger logger);
	void registerInterpreter(Interpreter interpreter);
	
	void shutDown();
}
