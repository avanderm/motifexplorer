package engine.rsatws;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.soap.*;

public class RSATConnection {

	private static final String RSAT_URL = "http://rsat.ulb.ac.be/rsat/web_services/RSATWS.cgi";
	
	SOAPConnection conn;
	URL rsatws;
	
	public RSATConnection() {
		
	}
	
	public void open() {
		try {
			rsatws = new URL(RSAT_URL);
			
			SOAPConnectionFactory scf = SOAPConnectionFactory.newInstance();
			conn = scf.createConnection();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
		} catch (SOAPException e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			conn.close();
		} catch (SOAPException e) {
			e.printStackTrace();
		}
	}
	
	public SOAPMessage generateResponse(SOAPRequest request) throws SOAPException {
		return conn.call(request.getMessage(), rsatws);
	}
	
	public <R extends SOAPResponse> R call(SOAPJob<R> request) {
		return request.use(this);
	}
	
	public <R extends SOAPResponse> URL store(SOAPJob<R> request) {
		return request.store(this);
	}
	
	public <R extends SOAPResponse> Ticket<R> ticket(SOAPJob<R> request) {
		return request.ticket(this);
	}
	
	public <R extends SOAPResponse> R retrieve(Ticket<R> ticket) {
		return ticket.use(this);
	}

}