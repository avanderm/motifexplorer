package engine.sql.dao.concurrent;

import java.sql.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import engine.sql.SQLStructure;
import engine.sql.UIDFactory;
import engine.sql.dao.DAOException;
import engine.sql.dao.GeneDAO;

import bio.DNASequence.Orientation;
import bio.Gene;

public final class ConcurrentGeneDAO extends ConcurrentDAO<Gene, String>
	implements GeneDAO {
	
	public ConcurrentGeneDAO(SQLStructure structure) {
		super(structure);
	}
	
	@Override
	protected String[] getTableNames() {
		return new String[] {"Genes"};
	}
	
	@Override
	protected String[] getViewNames() {
		return null;
	}
	
	@Override
	protected void insertImpl(Collection<Gene> transientObjects,
			Connection conn) throws DAOException {
		try {
			String insert = getSQLStructure().getInsertStatementFor(getTableNames()[0], false);
			PreparedStatement statement = conn.prepareStatement(insert);
			
			for (Gene gene : transientObjects) {
				statement.setLong(1, UIDFactory.hash(gene.getName()));
				statement.setString(2, gene.getName());
				statement.setInt(3, gene.getChromosomeIndex());
				statement.setInt(4, gene.getStartPosition());
				statement.setInt(5, gene.getEndPosition());
				
				statement.addBatch();
			}
			
			statement.executeBatch();
		} catch (SQLException e) {
			//TODO: DAOException
			e.printStackTrace();
		}
	}
	
	@Override
	protected void updateImpl(Collection<Gene> transientObjects,
			Connection conn) throws DAOException {

		try {
			String update = getSQLStructure().getUpdateStatementFor(getTableNames()[0]);
			PreparedStatement statement = conn.prepareStatement(update);
			
			for (Gene gene : transientObjects) {
				statement.setLong(4, UIDFactory.hash(gene.getName()));
				statement.setInt(1, gene.getChromosomeIndex());
				statement.setInt(2, gene.getStartPosition());
				statement.setInt(3, gene.getEndPosition());
				
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
			String delete = getSQLStructure().getDeleteStatementFor(getTableNames()[0]);
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
	public Collection<Gene> retrieveImpl(Collection<String> id,
			Connection conn) throws DAOException {
		
		try {
			StringBuilder sb = new StringBuilder(2*id.size());
			sb.append('(').append('?');
			for (int i = 1; i < id.size(); i++)
				sb.append(',').append('?');
			sb.append(')');
			
			String retrieve = getSQLStructure().getRetrieveStatementFor(getTableNames()[0],
					sb.toString());
			PreparedStatement statement = conn.prepareStatement(retrieve);
			
			Iterator<String> iter = id.iterator();
			for (int i = 1; iter.hasNext(); i++)
				statement.setLong(i, UIDFactory.hash(iter.next()));
			
			ResultSet rs = statement.executeQuery();
			
			LinkedList<Gene> geneList = new LinkedList<Gene>();
			
			while(rs.next())
				geneList.add(new Gene(rs.getString(1), Orientation.UNDEFINED,
						rs.getInt(2), rs.getInt(3), rs.getInt(4)));
			
			return geneList;
		} catch (SQLException e) {
			throw new DAOException();
		}
	}

	@Override
	public Collection<Gene> retrieveAllImpl(Connection conn)
			throws DAOException {
		
		try {
			String query = getSQLStructure().getRetrieveAllStatementFor(getTableNames()[0]);
			PreparedStatement queryStat = conn.prepareStatement(query);
			
			ResultSet rs = queryStat.executeQuery();
			
			LinkedList<Gene> geneList = new LinkedList<Gene>();
			while(rs.next())
				geneList.add(new Gene(rs.getString(1), Orientation.UNDEFINED,
						rs.getInt(2), rs.getInt(3), rs.getInt(4)));
			
			return geneList;
		} catch (SQLException e) {
			//TODO
			throw new DAOException();
		}
	}

}