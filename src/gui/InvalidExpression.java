package gui;

public class InvalidExpression extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1992016556117403236L;

	private String message;
	
	public InvalidExpression(String message) {
		this.message = message;
	}
	
	@Override
	public String getLocalizedMessage() {
		return message;
	}
}
