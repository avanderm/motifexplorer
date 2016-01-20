package engine.sql.dao.concurrent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import engine.sql.SQLStructure;
import engine.sql.UIDFactory;
import engine.sql.dao.DAOException;
import engine.sql.dao.PromoterDAO;


import bio.DNASequence.Orientation;
import bio.Promoter;

public final class ConcurrentPromoterDAO extends ConcurrentDAO<Promoter, String> implements
		PromoterDAO {

	public ConcurrentPromoterDAO(SQLStructure structure) {
		super(structure);
	}
	
	@Override
	protected String[] getTableNames() {
		return new String[] {"Promoters"};
	}
	
	@Override
	protected String[] getViewNames() {
		return null;
	}
	
	@Override
	protected void insertImpl(Collection<Promoter> transientObjects,
			Connection conn) throws DAOException {

		try {
			String insert = getSQLStructure().getInsertStatementFor(
					getTableNames()[0], false);
			PreparedStatement statement = conn.prepareStatement(insert);
			
			for (Promoter promoter : transientObjects) {
				long promID = UIDFactory.hash(promoter.toString());
				
				statement.setLong(1, promID);
				statement.setLong(2, UIDFactory.hash(promoter.getGeneReference()));
				statement.setString(3, promoter.getSequence());
				statement.setString(4, promoter.getOrientation().name());
				statement.setInt(5, promoter.getChromosomeIndex());
				statement.setInt(6, promoter.getStartPosition());
				statement.setInt(7, promoter.getEndPosition());
				
				statement.addBatch();
			}
			
			statement.executeBatch();
		} catch (SQLException e) {
			//TODO: DAOException
			e.printStackTrace();
		}
	}
	
	@Override
	protected void updateImpl(Collection<Promoter> transientObjects,
			Connection conn) throws DAOException {

		try {
			String update = getSQLStructure().getUpdateStatementFor(
					getTableNames()[0]);
			PreparedStatement statement = conn.prepareStatement(update);
			
			for (Promoter promoter : transientObjects) {
				statement.setString(1, promoter.getSequence());
				statement.setLong(2, UIDFactory.hash(promoter.toString()));
				
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
			String delete = getSQLStructure().getDeleteStatementForGene(
					getTableNames()[0]);
			PreparedStatement statement = conn.prepareStatement(delete);
			for (String name: id) {
				statement.setLong(1, UIDFactory.hash(name));
				statement.addBatch();
			}
			
			statement.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void delete(String id, Orientation strand) throws DAOException {
		if (!isPresent() && !detectPresence())
			return;
		
		Connection conn = super.requestConnection();
		try {
			String delete = getSQLStructure().getDeleteStatementFor(
					getTableNames()[0]);
			PreparedStatement statement = conn.prepareStatement(delete);
			statement.setLong(1, UIDFactory.hash(id + strand.name()));
			
			statement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			super.returnConnection(conn);
		}
	}
	
	@Override
	public void deleteAllForGene(String id) throws DAOException {
		if (!isPresent() && !detectPresence())
			return;
		
		Connection conn = super.requestConnection();
		try {
			String delete = getSQLStructure().getDeleteStatementForGene(
					getTableNames()[0]);
			PreparedStatement statement = conn.prepareStatement(delete);
			statement.setLong(1, UIDFactory.hash(id));
			
			statement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			super.returnConnection(conn);
		}
	}
	
	@Override @Deprecated
	public Promoter retrieve(String id) {
		return null;
	}

	@Override
	public Collection<Promoter> retrieveImpl(Collection<String> id,
			Connection conn) throws DAOException {

		try {
			StringBuilder sb = new StringBuilder(2*id.size());
			sb.append("(?");
			for (int i = 1; i < id.size(); i++)
				sb.append(',').append('?');
			sb.append(")");
			
			String query = getSQLStructure().getRetrieveStatementFor(
					getTableNames()[0], sb.toString());
			PreparedStatement statement = conn.prepareStatement(query);
			
			Iterator<String> iter = id.iterator();
			for (int i = 1; iter.hasNext(); i++)
				statement.setLong(i, UIDFactory.hash(iter.next()));
			
			ResultSet rs = statement.executeQuery();
			
			LinkedList<Promoter> promList = new LinkedList<Promoter>();
			
			while(rs.next()) {
				Promoter promoter = new Promoter(rs.getString(1), rs.getString(2),
						Orientation.valueOf(rs.getString(3)), rs.getInt(4),
						rs.getInt(5), rs.getInt(6));
				
				promList.add(promoter);
			}
			
			return promList;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DAOException();
		}
	}

	@Override
	public Collection<Promoter> retrieveAllImpl(Connection conn)
			throws DAOException {

		try {
			String query = getSQLStructure().getRetrieveAllStatementFor(
					getTableNames()[0]);
			PreparedStatement queryStat = conn.prepareStatement(query);
			
			ResultSet rs = queryStat.executeQuery();
			
			LinkedList<Promoter> promList = new LinkedList<Promoter>();
			while(rs.next()) {
				Promoter promoter = new Promoter(rs.getString(1), rs.getString(2),
						Orientation.valueOf(rs.getString(3)), rs.getInt(4),
						rs.getInt(5), rs.getInt(6));
				
				promList.add(promoter);
			}
			
			return promList;
		} catch (SQLException e) {
			throw new DAOException();
		}
	}

	@Override
	public Collection<Promoter> retrieveForGene(String id) throws DAOException {
		LinkedList<String> coll = new LinkedList<String>();
		coll.add(id);
		
		return retrieve(coll);
	}

}