package engine.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.AttributeSet;

import util.Logger;

import engine.handler.DatabaseHandler;

import bio.Motif;


public class PWMParser extends Parser<DatabaseHandler> {;
	
	public PWMParser() {
		
	}
	
	@Override
	protected void parseContent(InputStream stream, DatabaseHandler handler, Logger logger, AttributeSet attr) {
		try {
			InputStreamReader in = new InputStreamReader(stream, "UTF-8");
			BufferedReader bin = new BufferedReader(in);
			
			Pattern ip = Pattern.compile("^#ID = ([\\w\\-/ ]+)\\s*$");
			Pattern mp = Pattern.compile("^\\d\\.\\d+\t\\d\\.\\d+\t\\d\\.\\d+\t\\d\\.\\d+$");
			
			Matcher im;
			
			String line;
			while((line = bin.readLine()) != null) {
				im = ip.matcher(line);
				if (im.find()) {
					boolean hasContent = false;
					String matrixString = line.trim();
					while((line = bin.readLine().trim()).length() != 0 || !hasContent) {
						if (line.length() > 0) {
							matrixString += "\n" + line.trim();
							if (!hasContent && (mp.matcher(line)).find())
								hasContent = true;
						}
					}
					
					handler.insert(Motif.parseFormat(matrixString, "raw"));
				}
			}
			
			bin.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
