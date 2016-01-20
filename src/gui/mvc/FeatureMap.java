package gui.mvc;

import java.awt.HeadlessException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.text.AttributeSet;

import util.Logger;
import engine.handler.DatabaseHandler;
import engine.rsatws.FeatureMapRequest;
import engine.rsatws.RSATConnection;

public class FeatureMap implements Procedure<DatabaseHandler, Logger> {

	private File file;
	
	public FeatureMap(File file) {
		this.file = file;
	}
	
	@Override
	public void execute(DatabaseHandler handler, Logger logger, AttributeSet attr) {
		int len;
	    char[] chr = new char[4096];
	    final StringBuffer buffer = new StringBuffer();
	    FileReader reader;
		try {
			reader = new FileReader(file);
			
			while ((len = reader.read(chr)) > 0) {
	    		buffer.append(chr, 0, len);
	    	}
			
			reader.close();
		} catch (FileNotFoundException e) {
			logger.println("No such file found");
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String content = buffer.toString();
		
		FeatureMapRequest request = new FeatureMapRequest(content);
		RSATConnection conn = new RSATConnection();
		conn.open();
		URL url = conn.store(request);
		conn.close();
		
		JFrame frame;
		try {
			frame = new JFrame("Feature Map (" + file.getCanonicalPath() + ")");
			JLabel image = new JLabel(new ImageIcon(url));
			frame.getContentPane().add(new JScrollPane(image,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
			frame.pack();
			
			int height = (image.getHeight()+50 > 600)?600:image.getHeight()+50;
			frame.setSize(image.getWidth() + 15, height);
			frame.setResizable(false);
			frame.setVisible(true);
		} catch (HeadlessException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}