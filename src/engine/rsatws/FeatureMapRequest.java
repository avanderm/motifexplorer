package engine.rsatws;

public class FeatureMapRequest extends SOAPJob<FeatureMapResponse> {

	private String featureMap;
	
	public FeatureMapRequest(String featureMap) {
		super();
		
		this.featureMap = featureMap;
	}
	
	@Override
	protected FeatureMapResponse constructResponse(String content) {
		return new FeatureMapResponse(content);
	}

	@Override
	protected String getServiceName() {
		return "feature_map";
	}

	@Override
	protected void completeRequest() {
		super.addNode("features", featureMap);
		super.addNode("format", "png");
		
		super.addNode("from", "auto");
		super.addNode("to", "auto");
		
		super.addNode("mlen", "500");
		super.addNode("mapthick", "25");
		super.addNode("mspacing", "2");
		super.addNode("origin", "0");
		
		super.addNode("scalebar", "1");
		
		super.addNode("legend", "1");
		super.addNode("orientation", "horiz");
	}

}
