package engine.sql.dao;

import util.IdentifierSet;

public interface SetDAO extends BatchDAO<IdentifierSet, Integer> {

	void removeOccurences(String id) throws DAOException;
	void emptySets() throws DAOException;
}
