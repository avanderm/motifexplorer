package engine.sql.dao;

public interface DAOManager {

	GeneDAO getGeneDAO();
	PromoterDAO getPromoterDAO();
	SetDAO getSetDAO();
	MotifDAO getMotifDAO();
	
	void orderShutdown();
	
	void dropGenes();
	void dropSets();
	void dropPromoters();
	void dropMotifs();
	
	void initGenes();
	void initSets();
	void initPromoters();
	void initMotifs();
}
