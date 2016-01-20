package engine;

import java.util.Stack;

import javax.swing.text.SimpleAttributeSet;

import engine.handler.ConcurrentDatabaseHandler;
import engine.handler.DatabaseHandler;
import engine.sql.dao.DAOManager;
import gui.InvalidCommand;
import gui.InvalidExpression;
import gui.mvc.Function;
import gui.mvc.Procedure;
import gui.mvc.Controller;
import util.Interpreter;
import util.Logger;

public class MEController implements Controller {

	private Interpreter interpreter;
	private Logger logger;
	
	private SimpleAttributeSet attr;
	
	private DAOManager manager;
	private DatabaseHandler handler;
	
	private Stack<Function<?, DatabaseHandler, Logger>> functionQuery;
	
	public MEController(DAOManager manager) {
		this.manager = manager;
		this.handler = new ConcurrentDatabaseHandler(manager);
		
		attr = new SimpleAttributeSet();
		attr.addAttribute("suppressHandlerStart", false);
		attr.addAttribute("suppressHandlerEnd", false);
		attr.addAttribute("usingInputLogger", true);
		attr.addAttribute("createPromoterBatchDependency", false);
		
		functionQuery = new Stack<Function<?, DatabaseHandler, Logger>>();
	}
	
	@Override
	public void interpret(String string) {
		try {
			interpreter.interpret(string);
		} catch (InvalidCommand e) {
			logger.println(e.getLocalizedMessage());
		} catch (InvalidExpression e) {
			logger.println(e.getLocalizedMessage());
		}
	}
	
	@Override
	public void process(Procedure<DatabaseHandler, Logger> procedure) {
		procedure.execute(handler, logger, attr);
	}

	@Override
	public void process(Function<?, DatabaseHandler, Logger> command) {
		command.execute(handler, logger, attr);
		functionQuery.push(command);
	}
	
	@Override
	public void print(Function<?, DatabaseHandler, Logger> function) {
		function.output(logger);
	}
	
	@Override
	public Function<?, DatabaseHandler, Logger> collect() {
		return functionQuery.pop();
	}

	@Override
	public void registerLogger(Logger logger) {
		this.logger = logger;
		logger.open();
	}

	@Override
	public void registerInterpreter(Interpreter interpreter) {
		this.interpreter = interpreter;
	}

	@Override
	public void shutDown() {
		manager.orderShutdown();
		System.exit(0);
	}

}