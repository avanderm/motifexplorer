package engine.parser;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.AttributeSet;

import bio.DNASequence.Orientation;
import bio.Gene;

import util.Logger;

import engine.handler.TAIRHandler;

public class TAIRParser extends Parser<TAIRHandler> {
	
	private int geneCount;
	
	public TAIRParser() {
		
	}
	
	@Override
	protected void parseContent(InputStream stream, TAIRHandler handler, Logger logger, AttributeSet attr) {
		geneCount = 0;
		
		try {
			InputStreamReader in = new InputStreamReader(stream, "UTF-8");
			BufferedReader bin = new BufferedReader(in);
			
			Pattern gp = Pattern.compile("^([0-9]+).+GENE\t(FORWARD|REVERSE)\t([0-9]+)\t([0-9]+)");
			Matcher gm;
			
			String line;
			while(!isInterrupted() && (line = bin.readLine()) != null) {
				if ((gm = gp.matcher(line)).find())
					handler.process(new Gene("TAIR" + geneCount++, Orientation.valueOf(gm.group(2)), Integer.parseInt(gm.group(1)),
							Integer.parseInt(gm.group(3)), Integer.parseInt(gm.group(4))));
			}
			
			bin.close();
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
	}

}