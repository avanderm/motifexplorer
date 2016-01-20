package engine.parser;


import java.io.*;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.AttributeSet;

import engine.handler.DatabaseHandler;

import util.Logger;
import util.IdentifierSet;

import bio.DNASequence.Orientation;
import bio.Gene;

public class OSIPParser extends Parser<DatabaseHandler> {
	
	private HashSet<String> geneNames;
	
	public OSIPParser() {
		
	}
	
	@Override
	protected void parseContent(InputStream stream, DatabaseHandler handler, Logger logger, AttributeSet attr) {
		geneNames = new HashSet<String>();
		try {
			InputStreamReader in = new InputStreamReader(stream, "UTF-8");
			BufferedReader bin = new BufferedReader(in);
			
			Pattern sp = Pattern.compile("^# Set([0-9]+): ([\\w\\s]*\\w)\\s*$");
			Pattern gp = Pattern.compile("^Chr([CM[0-9]]+).+gene\t([0-9]+)\t([0-9]+).+\tID=(SIP[0-9]+)");
			
			Matcher sm;
			Matcher gm;
			
			// Standard set index
			IdentifierSet set = null;
			
			String line;
			while((line = bin.readLine()) != null) {
				if ((sm = sp.matcher(line)).find()) {
					// Change set index
					if (set == null)
						set = new IdentifierSet(Integer.parseInt(sm.group(1)), sm.group(2));
					else {
						handler.insert(set);
						set = new IdentifierSet(Integer.parseInt(sm.group(1)), sm.group(2));
					}
					
				} else if ((gm = gp.matcher(line)).find()) {
					int chromosomeIndex;
					switch (gm.group(1)) {
					case "C":
						chromosomeIndex = 6; break;
					case "M":
						chromosomeIndex = 7; break;
					default:
						chromosomeIndex = Integer.parseInt(gm.group(1));
					}
					
					if (!geneNames.contains(gm.group(4))) {
						geneNames.add(gm.group(4));
						handler.insert(new Gene(gm.group(4), Orientation.UNDEFINED, chromosomeIndex,
								Integer.parseInt(gm.group(2)), Integer.parseInt(gm.group(3))));
					}
					
					set.add(gm.group(4));
				}
			}
			
			// Add last set
			handler.insert(set);
			
			bin.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}