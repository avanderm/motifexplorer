package gui;

public class InvalidCommand extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8761468482739097658L;
	
	private String message;
	
	public InvalidCommand(String message) {
		this.message = message;
	}
	
	@Override
	public String getLocalizedMessage() {
		return message;
	}

}
