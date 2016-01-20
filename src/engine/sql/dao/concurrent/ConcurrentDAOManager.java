package engine.sql.dao.concurrent;

import engine.sql.SQLStructure;
import engine.sql.dao.DAOManager;
import engine.sql.dao.GeneDAO;
import engine.sql.dao.MotifDAO;
import engine.sql.dao.PromoterDAO;
import engine.sql.dao.SetDAO;

public class ConcurrentDAOManager implements DAOManager {
	
	private static ConcurrentDAOManager manager = null;
	
	private SQLStructure structure;
	
	private ConcurrentGeneDAO geneDAO;
	private ConcurrentSetDAO setDAO;
	private ConcurrentPromoterDAO promoterDAO;
	private ConcurrentMotifDAO motifDAO;
	
	private ConcurrentDAOManager(SQLStructure structure) {
		this.structure = structure;
	}
	
	public static ConcurrentDAOManager getManager(SQLStructure structure) {
		if (manager == null)
			manager = new ConcurrentDAOManager(structure);
		
		return manager;
	}
	
	private SQLStructure getSQLStructure() {
		return this.structure;
	}

	@Override
	public GeneDAO getGeneDAO() {
		if (geneDAO == null)
			geneDAO = new ConcurrentGeneDAO(getSQLStructure());
		
		return geneDAO;
	}
	
	@Override
	public PromoterDAO getPromoterDAO() {
		if (promoterDAO == null)
			promoterDAO = new ConcurrentPromoterDAO(getSQLStructure());
		
		return promoterDAO;
	}

	@Override
	public SetDAO getSetDAO() {
		if (setDAO == null)
			setDAO = new ConcurrentSetDAO(getSQLStructure());
		
		return setDAO;
	}
	
	@Override
	public MotifDAO getMotifDAO() {
		if (motifDAO == null)
			motifDAO = new ConcurrentMotifDAO(getSQLStructure());
		
		return motifDAO;
	}
	
	@Override
	public void orderShutdown() {
		if (geneDAO != null)
			geneDAO.shutdown();
		if (setDAO != null)
			setDAO.shutdown();
		if (promoterDAO != null)
			promoterDAO.shutdown();
		if (motifDAO != null)
			motifDAO.shutdown();
	}
	
	@Override
	public void dropGenes() {
		// Dependencies first
		getSetDAO().dropTable();
		getPromoterDAO().dropTable();
		
		getGeneDAO().dropTable();
	}
	
	@Override
	public void dropSets() {
		getSetDAO().dropTable();
	}
	
	@Override
	public void dropPromoters() {
		getPromoterDAO().dropTable();
	}
	
	@Override
	public void dropMotifs() {
		getMotifDAO().dropTable();
	}
	
	@Override
	public void initGenes() {
		getGeneDAO().createTable();
	}
	
	@Override
	public void initSets() {
		getSetDAO().createTable();
	}
	
	@Override
	public void initPromoters() {
		getPromoterDAO().createTable();
	}
	
	@Override
	public void initMotifs() {
		getMotifDAO().createTable();
	}

}