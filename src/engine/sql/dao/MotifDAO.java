package engine.sql.dao;

import java.util.Collection;

import bio.Motif;

public interface MotifDAO extends BatchDAO<Motif, String> {

	void deleteByIndex(int index) throws DAOException;
	void deleteByIndex(Collection<Integer> index) throws DAOException;
	
	Motif retrieveByIndex(int index) throws DAOException;
	Collection<Motif> retrieveByIndex(Collection<Integer> index) throws DAOException;
}
