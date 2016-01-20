package engine.sql.dao;

import java.io.Serializable;
import java.util.Collection;

public interface BatchDAO<T, I extends Serializable> extends DAO<T, I> {

	void insert(Collection<T> transientObjects) throws DAOException;
	void update(Collection<T> transientObjects) throws DAOException;
	void delete(Collection<I> id) throws DAOException;
	
	Collection<T> retrieve(Collection<I> id) throws DAOException;
}
