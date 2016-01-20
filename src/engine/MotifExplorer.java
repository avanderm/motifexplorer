package engine;

import engine.sql.MySQLStructure;
import engine.sql.dao.concurrent.ConcurrentDAOManager;

import gui.Terminal;
import gui.mvc.Controller;

public class MotifExplorer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ConcurrentDAOManager manager = 
				ConcurrentDAOManager.getManager(new MySQLStructure());
		Controller controller = new MEController(manager);
		
		Terminal terminal = new Terminal(controller);
		
		// Feedback component in MVC model
		controller.registerLogger(terminal);
	}

}