package engine.sql.dao;

import java.util.Collection;

public interface DAO<T, I> {

	void insert(T transientObject) throws DAOException;
	void update(T transientObject) throws DAOException;
	void delete(I id) throws DAOException;
	void deleteAll() throws DAOException;
	
	T retrieve(I id) throws DAOException;
	Collection<T> retrieveAll() throws DAOException;
	
	int count() throws DAOException;
	
	void createTable();
	void dropTable();
}