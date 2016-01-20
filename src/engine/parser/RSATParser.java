package engine.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.AttributeSet;

import engine.handler.DatabaseHandler;
import util.Logger;

public class RSATParser extends Parser<DatabaseHandler> {

	private ArrayList<String> motifList;
	private ArrayList<Integer> motifCnt;
	
	public RSATParser() {
		
	}
	
	@Override
	protected void parseContent(InputStream stream, DatabaseHandler handler,
			Logger logger, AttributeSet attr) {
		
		motifList = new ArrayList<String>();
		motifCnt = new ArrayList<Integer>();
		
		int base = 2;
		
		try {
			InputStreamReader in = new InputStreamReader(stream, "UTF-8");
			BufferedReader bin = new BufferedReader(in);
			
			Pattern mp = Pattern.compile("^;matrix-scan\\.([0-9]+)\t(.+)$");
			Pattern cp = Pattern.compile("^(SIP[0-9]+)\\|(FORWARD|REVERSE).+matscan-matrix.([0-9]+).+$");
			Matcher mm;
			Matcher cm;
			
			String line;
			while((line = bin.readLine()) != null) {
				if ((mm = mp.matcher(line)).find()) {
					int index = Integer.parseInt(mm.group(1)) - base;
					motifList.add(index, mm.group(2));
					motifCnt.add(index, new Integer(0));
				} else if ((cm = cp.matcher(line)).find()) {
					int index = Integer.parseInt(cm.group(3)) - base;
					motifCnt.set(index, motifCnt.get(index) + 1);
				}
			}
			
			for(int i = 0; i < motifList.size(); i++)
				if (motifCnt.get(i) != null)
					if (motifList.get(i) != null)
						logger.format("%-20s\t%5d\n", motifList.get(i), motifCnt.get(i).intValue());
					else
						logger.format("%-20s\t%5d\n", "" + i, motifCnt.get(i).intValue());
			
			bin.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
