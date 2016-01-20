package engine.rsatws;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.w3c.dom.DOMException;

public abstract class SOAPJob<R extends SOAPResponse> extends SOAPRequest {
	
	private static final String RSAT_URL = "http://rsat.ulb.ac.be/rsat/";

	private R construct(SOAPMessage response) {
		try {
			return constructResponse(response.getSOAPBody().getElementsByTagName("client").item(0).getTextContent());
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (SOAPException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private String getServerLocationPath(SOAPMessage response) {
		String path = "";
		try {
			path = response.getSOAPBody().getElementsByTagName("server").item(0).getTextContent();
		} catch (DOMException e1) {
			e1.printStackTrace();
		} catch (SOAPException e1) {
			e1.printStackTrace();
		}
		
		Pattern p = Pattern.compile("public_html(/tmp/.+)");
		Matcher m = p.matcher(path);
		
		if (m.find())
			return RSAT_URL + m.group(1);
		
		return null;
	}
	
	protected abstract R constructResponse(String content);
	
	public R use(RSATConnection conn) {
		addNode("output", "client");
		if (!isComplete())
			complete();
		
		try {
			SOAPMessage response = conn.generateResponse(this);
			
			return construct(response);
		} catch (SOAPException e) {
			System.out.println("Unable to complete call to service: " + getServiceName());
			return null;
		}
	}
	
	public URL store(RSATConnection conn) {
		addNode("output", "server");
		if (!isComplete())
			complete();
		
		try {
			SOAPMessage response = conn.generateResponse(this);
			
			return new URL(getServerLocationPath(response));
		} catch (SOAPException e) {
			System.out.println("Unable to complete call to service: " + getServiceName());
			return null;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Ticket<R> ticket(RSATConnection conn) {
		addNode("output", "ticket");
		if (!isComplete())
			complete();
		
		try {
			SOAPMessage response = conn.generateResponse(this);
			// Extract "server" value from message
			return new Ticket<R>(response.getSOAPBody().getElementsByTagName("server").item(0).getTextContent(),
					this);
		} catch (SOAPException e) {
			System.out.println("Unable to retrieve ticket for service: " + getServiceName());
			return null;
		}
	}
}
