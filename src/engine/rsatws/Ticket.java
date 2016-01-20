package engine.rsatws;

public class Ticket<R extends SOAPResponse> extends SOAPJob<R> {

	private String ticketId;
	private SOAPJob<R> originalRequest;
	
	public Ticket(String ticketId, SOAPJob<R> request) {
		this.ticketId = ticketId;
		this.originalRequest = request;
	}
	
	public String getId() {
		return ticketId;
	}

	@Override
	protected String getServiceName() {
		return "get_result";
	}

	@Override
	protected void completeRequest() {
		addNode("ticket", getId());
	}

	@Override
	protected R constructResponse(String content) {
		return originalRequest.constructResponse(content);
	}

}