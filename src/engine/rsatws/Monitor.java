package engine.rsatws;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

public class Monitor extends SOAPRequest {

	private String ticketId;
	private String status;
	
	public <R extends SOAPResponse> Monitor(Ticket<R> ticket) {
		this.ticketId = ticket.getId();
		this.status = "Unknown";
	}
	
	public String getTicketId() {
		return ticketId;
	}
	
	public boolean isJobFinished() {
		return status.equals("Done");
	}
	
	public String getStatus() {
		return status;
	}
	
	private void setStatus(String newStatus) {
		status = newStatus;
	}
	
	public void update(RSATConnection conn) {
		if (!isComplete())
			complete();
		
		try {
			SOAPMessage response = conn.generateResponse(this);
			
			setStatus(response.getSOAPBody().getElementsByTagName("status").item(0).getTextContent());
		} catch (SOAPException e) {
			System.out.println("Unable to complete call to service: " + getServiceName());
		}
	}
	
	@Override
	protected String getServiceName() {
		return "monitor";
	}

	@Override
	protected void completeRequest() {
		addNode("ticket", getTicketId());
	}

}