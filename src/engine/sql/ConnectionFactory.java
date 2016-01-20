package engine.sql;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionFactory {

	private String driverClassName = "org.gjt.mm.mysql.Driver";
	private String connectionUrl;
	private String dbUser;
	private String dbPass;
	
	private AtomicInteger count;
	
	private static ConnectionFactory connectionFactory = null;
	
	private ConnectionFactory() {
		try {
			Driver drv = (Driver) Class.forName(driverClassName).newInstance();
			DriverManager.registerDriver(drv);
			
			Properties prop = new Properties();
			prop.load(new FileInputStream("conf/config.properties"));
			
			setConnectionUrl(prop.getProperty("db.url"));
			setUser(prop.getProperty("db.user"));
			setPass(prop.getProperty("db.pass"));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			count = new AtomicInteger(0);
		}
	}
	
	public void setConnectionUrl(String connectionUrl) {
		this.connectionUrl = connectionUrl;
	}
	
	public void setUser(String dbUser) {
		this.dbUser = dbUser;
	}
	
	public void setPass(String dbPass) {
		this.dbPass = dbPass;
	}
	
	public Connection getConnection() {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(connectionUrl, dbUser, dbPass);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		count.incrementAndGet();
		return conn;
	}
	
	public void returnConnection(Connection conn) {
		try {
			conn.close();
			count.decrementAndGet();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static ConnectionFactory getInstance() {
		if (connectionFactory == null)
			connectionFactory = new ConnectionFactory();
		
		return connectionFactory;
	}
}