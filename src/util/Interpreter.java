package util;

import gui.InvalidCommand;
import gui.InvalidExpression;

public interface Interpreter {

	void interpret(String command) throws InvalidCommand, InvalidExpression;
}
