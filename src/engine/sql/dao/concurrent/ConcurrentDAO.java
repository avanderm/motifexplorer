package engine.sql.dao.concurrent;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.*;

import engine.sql.ConnectionFactory;
import engine.sql.SQLStructure;
import engine.sql.dao.BatchDAO;
import engine.sql.dao.DAOException;

public abstract class ConcurrentDAO<T, I extends Serializable>
	implements BatchDAO<T, I> {

	private static final int POOL_SIZE = Runtime.getRuntime().availableProcessors() + 16;
	
	private ExecutorService pool;
	private boolean isPresent;
	
	private SQLStructure structure;
	
	protected ConcurrentDAO(SQLStructure structure) {
		this(structure, POOL_SIZE);
	}
	
	protected ConcurrentDAO(SQLStructure structure, int poolSize) {
		this.structure = structure;
		this.pool = Executors.newFixedThreadPool(poolSize);
		
		setPresent(detectPresence());
	}
	
	protected void submit(FutureTask<Void> task) {
		pool.submit(task);
	}
	
	public void shutdown() {
		// Not the DAO's task to know if tasks will be added in the future
		pool.shutdown();
	}
	
	public void shutdownNow() {
		pool.shutdownNow();
	}
	
	protected Connection requestConnection() {
		return ConnectionFactory.getInstance().getConnection();
	}
	
	protected void returnConnection(Connection conn) {
		ConnectionFactory.getInstance().returnConnection(conn);
	}
	
	protected boolean isPresent() {
		return isPresent;
	}
	
	private void setPresent(boolean isPresent) {
		this.isPresent = isPresent;
	}
	
	protected SQLStructure getSQLStructure() {
		return structure;
	}
	
	protected boolean detectPresence() {
		Connection conn = requestConnection();
		try {
			DatabaseMetaData metaData = conn.getMetaData();
			
			boolean tableResult = true;
			for(String table : getTableNames())
				tableResult = tableResult && (metaData.getTables(null, null,
						getSQLStructure().getTableNameFor(table), null)).next();
			
			boolean viewResult = true;
			if (getViewNames() != null)
				for(String view : getViewNames())
					viewResult = viewResult && (metaData.getTables(null, null,
							getSQLStructure().getTableNameFor(view), null)).next();
			
			return tableResult && viewResult;
		} catch (SQLException e) {
			//TODO
			e.printStackTrace();
			return false;
		} finally {
			returnConnection(conn);
		}
	}
	protected abstract String[] getTableNames();
	protected abstract String[] getViewNames();
	
	@Override
	public synchronized void createTable() {
		// First check if a different thread performed initialization, then check physical
		if (!isPresent() && !detectPresence()) {
			Connection conn = requestConnection();
			
			try {
				Statement statement = conn.createStatement();
				for (String table : getTableNames())
					statement.execute(getSQLStructure().getCreateStatementFor(table));
				
				if (getViewNames() != null)
					for (String view : getViewNames())
						statement.execute(getSQLStructure().getCreateStatementFor(view));
				
				setPresent(true);
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				returnConnection(conn);
			}
		}
	}
	
	@Override
	public synchronized void dropTable() {
		if (isPresent() || detectPresence()) {
			Connection conn = requestConnection();
			
			try {
				Statement statement = conn.createStatement();
				
				String[] views = getViewNames();
				if (views != null)
					for (int i = views.length - 1; i > -1; i--)
						statement.execute(getSQLStructure().getDropStatementFor(views[i]));
				
				String[] tables = getTableNames();
				for (int i = tables.length - 1; i > -1; i--)
					statement.execute(getSQLStructure().getDropStatementFor(tables[i]));
				
				setPresent(false);
			} catch (SQLException e) {
				// Possible dependencies conflict
				return;
			} finally {
				returnConnection(conn);
			}
		}
	}
	
	protected void insertImpl(T transientObject,
			Connection conn) throws DAOException {
		LinkedList<T> gene = new LinkedList<T>();
		gene.add(transientObject);
		insertImpl(gene, conn);
	}
	
	public Future<Void> insertFeedback(final T transientObject) throws DAOException {
		FutureTask<Void> task = new FutureTask<Void>(new Callable<Void>() {

			@Override
			public Void call() throws DAOException {
				Connection conn = requestConnection();
				
				if (!isPresent() && !detectPresence())
					createTable();
				
				insertImpl(transientObject, conn);
				returnConnection(conn);
				return null;
			}
			
		});
		
		submit(task);
		return task;
	}
	
	@Override
	public void insert(final T transientObject) throws DAOException {
		insertFeedback(transientObject);
	}
	
	protected abstract void insertImpl(Collection<T> transientObjects,
			Connection conn) throws DAOException;
	
	public Future<Void> insertFeedback(final Collection<T> transientObjects) throws DAOException {
		FutureTask<Void> task = new FutureTask<Void>(new Callable<Void>() {

			@Override
			public Void call() throws DAOException {
				Connection conn = requestConnection();
				
				if (!isPresent() && !detectPresence())
					createTable();
				
				insertImpl(transientObjects, conn);
				returnConnection(conn);
				return null;
			}
			
		});
		
		submit(task);
		return task;
	}
	
	@Override
	public void insert(final Collection<T> transientObjects) throws DAOException {
		insertFeedback(transientObjects);
	}
	
	protected void updateImpl(T transientObject,
			Connection conn) throws DAOException {
		LinkedList<T> gene = new LinkedList<T>();
		gene.add(transientObject);
		updateImpl(gene, conn);
	}
	
	public Future<Void> updateFeedback(final T transientObject) throws DAOException {
		FutureTask<Void> task = new FutureTask<Void>(new Callable<Void>() {

			@Override
			public Void call() throws DAOException {
				Connection conn = requestConnection();
				
				if (!isPresent() && !detectPresence()) {
					createTable();
					insertImpl(transientObject, conn);
					return null;
				}
				
				updateImpl(transientObject, conn);
				returnConnection(conn);
				return null;
			}
			
		});
		
		submit(task);
		return task;
	}
	
	@Override
	public void update(final T transientObject) throws DAOException {
		updateFeedback(transientObject);
	}
	
	protected abstract void updateImpl(Collection<T> transientObjects,
			Connection conn) throws DAOException;
	
	public Future<Void> updateFeedback(final Collection<T> transientObjects) throws DAOException {
		FutureTask<Void> task = new FutureTask<Void>(new Callable<Void>() {

			@Override
			public Void call() throws DAOException {
				Connection conn = requestConnection();
				
				if (!isPresent() && !detectPresence()) {
					createTable();
					insertImpl(transientObjects, conn);
					return null;
				}
				
				updateImpl(transientObjects, conn);
				returnConnection(conn);
				return null;
			}
			
		});
		
		submit(task);
		return task;
	}
	
	@Override
	public void update(final Collection<T> transientObjects) throws DAOException {
		updateFeedback(transientObjects);
	}
	
	protected void deleteImpl(I id,
			Connection conn) throws DAOException {
		
		LinkedList<I> coll = new LinkedList<I>();
		coll.add(id);
		deleteImpl(coll, conn);
	}
	
	protected abstract void deleteImpl(Collection<I> id,
			Connection conn) throws DAOException;
	
	@Override
	public void delete(I id) throws DAOException {
		if (!isPresent() && !detectPresence())
			return;
		
		Connection conn = requestConnection();
		deleteImpl(id, conn);
		returnConnection(conn);
	}
	
	@Override
	public void delete(Collection<I> id) throws DAOException {
		if (!isPresent() && !detectPresence())
			return;
		
		Connection conn = requestConnection();
		deleteImpl(id, conn);
		returnConnection(conn);
	}
	
	@Override
	public void deleteAll() throws DAOException {
		if (!isPresent() && !detectPresence())
			return;
		
		Connection conn = requestConnection();
		try {
			Statement statement = conn.createStatement();
			
			String[] tables = getTableNames();
			for (int i = tables.length - 1; i > -1; i--)
				statement.execute(getSQLStructure().getDeleteAllStatementFor(tables[i]));
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			returnConnection(conn);
		}
	}
	
	protected T retrieveImpl(I id,
			Connection conn) throws DAOException {
		
		LinkedList<I> coll = new LinkedList<I>();
		coll.add(id);
		
		return retrieveImpl(coll, conn).iterator().next();
	}
	
	protected abstract Collection<T> retrieveImpl(Collection<I> id,
			Connection conn) throws DAOException;

	@Override
	public T retrieve(I id) throws DAOException {
		if (!isPresent() && !detectPresence())
			// Preference to return an empty list instead of null
			return null;
		
		Connection conn = requestConnection();
		T result = retrieveImpl(id, conn);
		returnConnection(conn);
		
		return result;
	}
	
	@Override
	public Collection<T> retrieve(Collection<I> id) throws DAOException {
		if (!isPresent() && !detectPresence())
			// Preference to return an empty list instead of null
			return new LinkedList<T>();
		
		Connection conn = requestConnection();
		Collection<T> coll = retrieveImpl(id, conn);
		returnConnection(conn);
		
		return coll;
	}
	
	protected abstract Collection<T> retrieveAllImpl(Connection conn)
			throws DAOException;

	@Override
	public Collection<T> retrieveAll() throws DAOException {
		if (!isPresent() && !detectPresence()) {
			// Preference to return an empty list instead of null
			return new LinkedList<T>();
		}
		
		Connection conn = requestConnection();
		Collection<T> coll = retrieveAllImpl(conn);
		returnConnection(conn);
		
		return coll;
	}
	
	@Override
	public int count() throws DAOException {
		if (!isPresent() && !detectPresence()) {
			return 0;
		}
		
		Connection conn = requestConnection();
		try {
			String count = getSQLStructure().getCountStatementFor(
					getTableNames()[0]);
			Statement statement = conn.createStatement();
			
			ResultSet rs = statement.executeQuery(count);
			rs.next();
			
			return rs.getInt(1);
		} catch (SQLException e) {
			throw new DAOException();
		} finally {
			returnConnection(conn);
		}
	}

}