package engine.sql.dao.concurrent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import util.IdentifierSet;

import engine.sql.ConnectionFactory;
import engine.sql.SQLStructure;
import engine.sql.UIDFactory;
import engine.sql.dao.DAOException;
import engine.sql.dao.SetDAO;

public final class ConcurrentSetDAO extends ConcurrentDAO<IdentifierSet, Integer>
	implements SetDAO {

	public ConcurrentSetDAO(SQLStructure structure) {
		super(structure);
	}

	@Override
	protected String[] getTableNames() {
		return new String[] {"Sets", "GeneSetRecords"};
	}
	
	@Override
	protected String[] getViewNames() {
		return new String[] {"GeneSetView"};
	}

	private void insert(Collection<String> transientObjects, int setIndex,
			Connection conn) throws DAOException {
		
		try {
			String insert = getSQLStructure().getInsertStatementFor(getTableNames()[1], false);
			PreparedStatement statement = conn.prepareStatement(insert);

			for (String gene : transientObjects) {
				statement.setLong(1, UIDFactory.hash(gene));
				statement.setInt(2, setIndex);
				statement.addBatch();
			}

			statement.executeBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void delete(Collection<String> transientObjects, int setIndex,
			Connection conn) throws DAOException {
		try {
			String delete = getSQLStructure().getDeleteStatementForSet(getTableNames()[1]);
			PreparedStatement statement = conn.prepareStatement(delete);

			for (String gene : transientObjects) {
				statement.setInt(1, setIndex);
				statement.setLong(2, UIDFactory.hash(gene));
				statement.addBatch();
			}

			statement.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void insertImpl(IdentifierSet transientObject,
			Connection conn) throws DAOException {
		
		int setIndex = transientObject.getID();

		PreparedStatement statement = null;
		try {
			// Add set
			String insert = getSQLStructure().getInsertStatementFor(getTableNames()[0], false);
			statement = conn.prepareStatement(insert);
			statement.setInt(1, setIndex);
			statement.setString(2, transientObject.getDescription());
			
			statement.execute();
		} catch (SQLException e) {
			// TODO: DAOException
			e.printStackTrace();
			return;
		}

		// Helper method 
		insert(transientObject, setIndex, conn);
	}

	@Override
	@Deprecated
	protected void insertImpl(Collection<IdentifierSet> transientObjects,
			Connection conn) throws DAOException {

	}

	@Override
	public Future<Void> insertFeedback(final Collection<IdentifierSet> transientObjects)
			throws DAOException {
		
		FutureTask<Void> task = new FutureTask<Void>(new Callable<Void>() {

			@Override
			public Void call() throws DAOException {
				List<Future<Void>> inserts = new LinkedList<Future<Void>>();
				for (IdentifierSet set : transientObjects)
					inserts.add(insertFeedback(set));

				// Wait for threads working on separate sets to finish
				for (Future<Void> insert : inserts)
					try {
						insert.get();
					} catch (InterruptedException e) {
						continue;
					} catch (ExecutionException e) {
						continue;
					}

				return null;
			}

		});

		submit(task);
		return task;
	}

	@Override
	protected void updateImpl(IdentifierSet transientObject,
			Connection conn) throws DAOException {
		
		try {
			int setIndex = transientObject.getID();
			
			String update = getSQLStructure().getUpdateStatementFor(getTableNames()[0]);
			PreparedStatement statement = conn.prepareStatement(update);
			statement.setInt(2, setIndex);
			statement.setString(1, transientObject.getDescription());

			statement.execute();

			Set<String> currentSet = retrieve(transientObject.getID());
			Set<String> replaceSet = transientObject;

			// Remove shared records
			Set<String> insertSet = new TreeSet<String>();
			insertSet.addAll(transientObject);
			insertSet.removeAll(currentSet);
			Set<String> deleteSet = currentSet;
			deleteSet.removeAll(replaceSet);

			// Helper methods
			if (deleteSet.size() != 0)
				delete(deleteSet, setIndex, conn);
			if (insertSet.size() != 0)
				insert(insertSet, setIndex, conn);
		} catch (SQLException e) {
			// TODO: DAOException
			e.printStackTrace();
		} finally {
			ConnectionFactory.getInstance().returnConnection(conn);
		}
	}

	@Override
	@Deprecated
	protected void updateImpl(Collection<IdentifierSet> transientObjects,
			Connection conn) throws DAOException {

	}

	@Override
	public Future<Void> updateFeedback(final Collection<IdentifierSet> transientObjects)
			throws DAOException {
		
		FutureTask<Void> task = new FutureTask<Void>(new Callable<Void>() {

			@Override
			public Void call() throws DAOException {
				List<Future<Void>> updates = new LinkedList<Future<Void>>();
				for (IdentifierSet set : transientObjects)
					updates.add(updateFeedback(set));

				// Wait for threads working on separate sets to finish
				for (Future<Void> update : updates)
					try {
						update.get();
					} catch (InterruptedException e) {
						continue;
					} catch (ExecutionException e) {
						continue;
					}

				return null;
			}

		});

		submit(task);
		return task;
	}

	@Override
	public void deleteImpl(Integer id,
			Connection conn) throws DAOException {
		
		try {
			// Delete dependencies
			String deleteDependencies = getSQLStructure().getDeleteAllStatementForSet(
					getTableNames()[1]);
			PreparedStatement statement = conn.prepareStatement(deleteDependencies);
			statement.setInt(1, id);
			
			statement.execute();

			// Delete working set
			String delete = getSQLStructure().getDeleteStatementFor(
					getTableNames()[0]);
			statement = conn.prepareStatement(delete);
			statement.setInt(1, id);
			
			statement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void deleteImpl(Collection<Integer> id,
			Connection conn) throws DAOException {
		
		for (Integer i : id)
			deleteImpl(i, conn);
	}

	@Override
	protected IdentifierSet retrieveImpl(Integer id,
			Connection conn) throws DAOException {
		
		try {
			String retrieve = getSQLStructure().getRetrieveStatementForSet(
					getTableNames()[0]);
			PreparedStatement statement = conn.prepareStatement(retrieve);
			statement.setInt(1, id);
			
			ResultSet rs = statement.executeQuery();
			
			if (rs.next()) {
				String retrieveSet = getSQLStructure().getRetrieveStatementForSet(
						getViewNames()[0]);
				statement = conn.prepareStatement(retrieveSet);
				statement.setInt(1, id);
				
				ResultSet result = statement.executeQuery();

				List<String> entries = new LinkedList<String>();
				while (result.next())
					entries.add(result.getString(1));

				return new IdentifierSet(id, rs.getString(1), entries);
			} else {
				throw new DAOException();
			}
		} catch (SQLException e) {
			throw new DAOException();
		} finally {
			ConnectionFactory.getInstance().returnConnection(conn);
		}
	}
	
	@Override
	protected Collection<IdentifierSet> retrieveImpl(Collection<Integer> id,
			Connection conn) throws DAOException {
		
		LinkedList<IdentifierSet> coll = new LinkedList<IdentifierSet>();
		for(Integer i : id)
			coll.add(retrieve(i));
		
		return coll;
	}

	@Override
	public Collection<IdentifierSet> retrieveAllImpl(Connection conn)
			throws DAOException {

		try {
			String retrieve = getSQLStructure().getRetrieveAllStatementFor(
					getTableNames()[0]);
			PreparedStatement statement = conn.prepareStatement(retrieve);
			ResultSet rs = statement.executeQuery();

			LinkedList<IdentifierSet> setList = new LinkedList<IdentifierSet>();

			while (rs.next()) {
				setList.add(retrieve(rs.getInt(1)));
			}

			return setList;
		} catch (SQLException e) {
			throw new DAOException();
		} finally {
			ConnectionFactory.getInstance().returnConnection(conn);
		}
	}

	@Override
	public void removeOccurences(String id) throws DAOException {
		if (!isPresent())
			return;

		Connection conn = ConnectionFactory.getInstance().getConnection();
		try {
			// Delete dependencies
			String delete = getSQLStructure().getDeleteStatementForGene(
					getTableNames()[1]);
			PreparedStatement statement = conn.prepareStatement(delete);
			statement.setLong(1, UIDFactory.hash(id));
			statement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionFactory.getInstance().returnConnection(conn);
		}
	}

	@Override
	public void emptySets() throws DAOException {
		if (!isPresent())
			return;

		Connection conn = ConnectionFactory.getInstance().getConnection();
		try {
			// Delete dependencies
			String delete = getSQLStructure().getDeleteAllStatementFor(
					getTableNames()[1]);
			PreparedStatement statement = conn.prepareStatement(delete);
			
			statement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionFactory.getInstance().returnConnection(conn);
		}
	}

}