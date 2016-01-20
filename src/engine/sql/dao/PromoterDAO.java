package engine.sql.dao;

import java.util.Collection;

import bio.DNASequence.Orientation;
import bio.Promoter;

public interface PromoterDAO extends BatchDAO<Promoter, String> {

	void delete(String id, Orientation strand) throws DAOException;
	void deleteAllForGene(String id) throws DAOException;
	
	Collection<Promoter> retrieveForGene(String id) throws DAOException;
}