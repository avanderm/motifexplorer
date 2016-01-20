package engine.sql.dao.concurrent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import bio.Motif;
import engine.sql.ConnectionFactory;
import engine.sql.SQLStructure;
import engine.sql.dao.DAOException;
import engine.sql.dao.MotifDAO;

public final class ConcurrentMotifDAO extends ConcurrentDAO<Motif, String> implements
		MotifDAO {

	public ConcurrentMotifDAO(SQLStructure structure) {
		super(structure);
	}
	
	@Override
	protected String[] getTableNames() {
		return new String[] {"Motifs"};
	}
	
	@Override
	protected String[] getViewNames() {
		return null;
	}
	
	@Override
	protected void insertImpl(Collection<Motif> transientObjects,
			Connection conn) throws DAOException {

		try {
			String insert = getSQLStructure().getInsertStatementFor(
					getTableNames()[0], false);
			PreparedStatement statement = conn.prepareStatement(insert);
			
			for (Motif motif : transientObjects) {
				statement.setString(1, motif.getName());
				statement.setString(2, motif.getMatrixString());
				
				statement.addBatch();
			}
			
			statement.executeBatch();
		} catch (SQLException e) {
			//TODO: DAOException
			e.printStackTrace();
		}
	}

	@Override
	protected void updateImpl(Collection<Motif> transientObjects,
			Connection conn) throws DAOException {

		try {
			String update = getSQLStructure().getUpdateStatementFor(
					getTableNames()[0]);
			PreparedStatement statement = conn.prepareStatement(update);
			
			for (Motif motif : transientObjects) {
				statement.setString(1, motif.getName());
				statement.setString(2, motif.getMatrixString());
				
				statement.addBatch();
			}
			
			statement.executeBatch();
		} catch (SQLException e) {
			//TODO: DAOException
			e.printStackTrace();
		}
	}

	@Override
	public void deleteImpl(Collection<String> id,
			Connection conn) throws DAOException {

		try {
			String delete = getSQLStructure().getDeleteStatementFor(
					getTableNames()[0]);
			PreparedStatement statement = conn.prepareStatement(delete);
			
			for (String name: id) {
				statement.setString(1, name);
				statement.addBatch();
			}
			
			statement.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Collection<Motif> retrieveImpl(Collection<String> id,
			Connection conn) throws DAOException {

		try {
			StringBuilder sb = new StringBuilder(2*id.size());
			sb.append('(').append('?');
			for (int i = 1; i < id.size(); i++)
				sb.append(',').append('?');
			sb.append(')');
			
			String retrieve = getSQLStructure().getRetrieveStatementFor(
					getTableNames()[0], sb.toString());
			PreparedStatement statement = conn.prepareStatement(retrieve);
			
			Iterator<String> iter = id.iterator();
			for (int i = 1; iter.hasNext(); i++)
				statement.setString(i, iter.next());
			
			ResultSet rs = statement.executeQuery();
			
			LinkedList<Motif> motifList = new LinkedList<Motif>();
			
			while(rs.next())
				motifList.add(Motif.parseFormat("; " + rs.getString(1) + "\n" +
						rs.getString(2), "tab"));
			
			return motifList;
		} catch (SQLException e) {
			throw new DAOException();
		}
	}

	@Override
	public Collection<Motif> retrieveAllImpl(Connection conn) throws DAOException {

		try {
			String retrieve = getSQLStructure().getRetrieveAllStatementFor(
					getTableNames()[0]);
			PreparedStatement statement = conn.prepareStatement(retrieve);
			
			ResultSet rs = statement.executeQuery();
			
			LinkedList<Motif> motifList = new LinkedList<Motif>();
			
			while(rs.next())
				motifList.add(Motif.parseFormat("; " + rs.getString(1) + "\n" +
						rs.getString(2), "tab"));
			
			return motifList;
		} catch (SQLException e) {
			throw new DAOException();
		} finally {
			ConnectionFactory.getInstance().returnConnection(conn);
		}
	}

	@Override
	public void deleteByIndex(int index) throws DAOException {
		if (!isPresent() && !detectPresence())
			return;
		
		Connection conn = super.requestConnection();
		try {
			String retrieve = getSQLStructure().getDeleteStatementForIndex(
					getTableNames()[0]);
			PreparedStatement statement = conn.prepareStatement(retrieve);
			statement.setInt(1, index);
			
			statement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			super.returnConnection(conn);
		}
	}

	@Override
	public void deleteByIndex(Collection<Integer> index) throws DAOException {
		for (Integer i : index)
			deleteByIndex(i);
	}

	@Override
	public Motif retrieveByIndex(int index) throws DAOException {
		LinkedList<Integer> coll = new LinkedList<Integer>();
		coll.add(index);
		
		Collection<Motif> result = retrieveByIndex(coll); 
		if (result.iterator().hasNext())
			return result.iterator().next();
		else
			return null;
	}

	@Override
	public Collection<Motif> retrieveByIndex(Collection<Integer> index) throws DAOException {
		if (!isPresent() && !detectPresence())
			return null;
		
		Connection conn = super.requestConnection();
		try {
			LinkedList<Motif> list = new LinkedList<Motif>();
			
			String retrieve = getSQLStructure().getRetrieveStatementForIndex(
					getTableNames()[0]);
			PreparedStatement statement = conn.prepareStatement(retrieve);
			for(Integer i : index) {
				statement.setInt(1, i);
				
				ResultSet rs = statement.executeQuery();
			
				if (rs.next())
					list.add(Motif.parseFormat("; " + rs.getString(1) + "\n" +
							rs.getString(2), "tab"));
			}
			
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} finally {
			super.returnConnection(conn);
		}
	}

}