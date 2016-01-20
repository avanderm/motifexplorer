package engine.rsatws;

import javax.xml.soap.*;

public abstract class SOAPRequest {

	private SOAPMessage msg;
	private SOAPElement content;
	
	private boolean isComplete;
	
	public SOAPRequest() {
		try {
			MessageFactory mf = MessageFactory.newInstance();
			
			setMessage(mf.createMessage());
			
			SOAPPart sp = getMessage().getSOAPPart();
			SOAPEnvelope envelope = sp.getEnvelope();
			SOAPBody body = envelope.getBody();
			
			envelope.addNamespaceDeclaration("soap", "http://schemas.xmlsoap.org/soap/envelope/");
			envelope.addNamespaceDeclaration("urn", "urn:RSATWS");
			
			SOAPFactory sf = SOAPFactory.newInstance();
			Name bodyName = sf.createName("urn:" + getServiceName());
			SOAPBodyElement bodyElement = body.addBodyElement(bodyName);
			Name requestName = sf.createName("request");
			content = bodyElement.addChildElement(requestName);
			
			isComplete = false;
		} catch (SOAPException e) {
			e.printStackTrace();
		}
	}
	
	protected void complete() {
		completeRequest();
		isComplete = true;
	}
	
	protected boolean isComplete() {
		return isComplete;
	}
	
	protected void addNode(String name, String value) {
		try {
			SOAPElement node = content.addChildElement(name);
			node.addTextNode(value);
		} catch (SOAPException e) {
			e.printStackTrace();
		}
	}
	
	protected abstract String getServiceName();
	protected abstract void completeRequest();

	public SOAPMessage getMessage() {
		return msg;
	}

	private void setMessage(SOAPMessage msg) {
		this.msg = msg;
	}

}