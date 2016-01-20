package engine.handler;

import bio.*;

public interface TAIRHandler extends Handler {

	void process(Gene gene);
	void process(Promoter promoter);
}
