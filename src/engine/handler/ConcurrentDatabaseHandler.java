package engine.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import util.IdentifierSet;

import engine.concurrent.BatchDependencyQueue;
import engine.concurrent.Dependency;
import engine.sql.dao.DAOException;
import engine.sql.dao.DAOManager;
import engine.sql.dao.concurrent.ConcurrentGeneDAO;
import engine.sql.dao.concurrent.ConcurrentMotifDAO;
import engine.sql.dao.concurrent.ConcurrentPromoterDAO;


import bio.Gene;
import bio.Motif;
import bio.Promoter;

public class ConcurrentDatabaseHandler implements DatabaseHandler {

	// Optimal batch size from experiment = 1 (9 seconds with 8 + 16 DAO pool size)
	private static final int BATCH_SIZE = 1;
	// Detect number of processors
	private static final int POOL_SIZE = Runtime.getRuntime().availableProcessors();
	
	private DAOManager manager;
	
	private Queue<Gene> geneInsertList = null;
	private Queue<Promoter> promInsertList = null;
	private Queue<Motif> motifInsertList = null;
	
	private ExecutorService pool = null;
	private BatchDependencyQueue<Void> batchQueue;
	
	public ConcurrentDatabaseHandler(DAOManager manager) {
		this.manager = manager;
	}
	
	@Override
	public void startHandler() {
		pool = Executors.newFixedThreadPool(POOL_SIZE);
		
		geneInsertList  = new ConcurrentLinkedQueue<Gene>();
		promInsertList  = new ConcurrentLinkedQueue<Promoter>();
		motifInsertList = new ConcurrentLinkedQueue<Motif>();
		batchQueue = new BatchDependencyQueue<Void>();
		
		pool.submit(batchQueue);
	}

	@Override
	public void endHandler() {		
		// Finish off last batches using dummies (triggers batch insert)
		if(geneInsertList != null) {
			Gene geneDummy = null;
			insert(geneDummy);
			
			geneInsertList = null;
		}
		
		if (promInsertList != null) {
			Promoter promDummy = null;
			insert(promDummy);
			
			promInsertList = null;
		}
		
		if (motifInsertList != null) {
			Motif motifDummy = null;
			insert(motifDummy);
			
			motifInsertList = null;
		}
		
		// Shutdown batch handler
		if (batchQueue != null)
			batchQueue.shutdown();
		
		pool.shutdown();
		try {
			// Grant shutdown time before DAO manager blocks access
			pool.awaitTermination(1000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			pool.shutdownNow();
		} finally {
			// Thread pools for DAO's are finished
			// manager.orderShutdown();
		}
	}
	
	@Override
	public void dropAll() {
		manager.dropMotifs();
		manager.dropPromoters();
		manager.dropSets();
		manager.dropGenes();
	}

	@Override
	public void deleteAll() {
		deleteMotifs();
		deleteGenes();
	}
	
	@Override
	public void initAll() {
		manager.initMotifs();
		manager.initGenes();
		manager.initSets();
		manager.initPromoters();
	}

	@Override
	public synchronized void insert(Gene gene) {
		if (gene != null)
			geneInsertList.add(gene);
		
		if ((geneInsertList.size() % BATCH_SIZE) == 0 || gene == null) {
			ConcurrentGeneDAO dao = (ConcurrentGeneDAO) manager.getGeneDAO();
			
			try {
				if (geneInsertList.size() != 0)
					batchQueue.addBatch(dao.insertFeedback(new ArrayList<Gene>(geneInsertList)));
			} catch (DAOException e) {
				//TODO: remove gene (find index!) unless ignore and retry
				e.printStackTrace();
			} finally {
				geneInsertList = new ConcurrentLinkedQueue<Gene>();
			}
		}
	}

	@Override
	public void update(Gene gene) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteGene(String id) {
		try {
			// Delete dependencies first
			manager.getPromoterDAO().delete(id);
			
			manager.getGeneDAO().delete(id);
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void deleteGenes() {
		try {
			// Delete dependencies first
			manager.getPromoterDAO().deleteAll();
			manager.getSetDAO().deleteAll();
			
			manager.getGeneDAO().deleteAll();
		} catch (DAOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Gene findGene(String id) {
		try {
			return manager.getGeneDAO().retrieve(id);
		} catch (DAOException e) {
			return null;
		}
	}

	@Override
	public Collection<Gene> findGenes(Collection<String> id) {
		try {
			return manager.getGeneDAO().retrieve(id);
		} catch (DAOException e) {
			return new LinkedList<Gene>();
		}		
	}
	
	@Override
	public Collection<Gene> findGenes() {
		try {
			return manager.getGeneDAO().retrieveAll();
		} catch (DAOException e) {
			return new LinkedList<Gene>();
		}
	}
	
	@Override
	public int countGenes() {
		try {
			return manager.getGeneDAO().count();
		} catch (DAOException e) {
			return 0;
		}
	}

	@Override
	public void insert(Promoter promoter) {
		if (promoter != null)
			promInsertList.add(promoter);
		
		if ((promInsertList.size() % BATCH_SIZE) == 0 || promoter == null) {
			ConcurrentPromoterDAO dao = (ConcurrentPromoterDAO) manager.getPromoterDAO();
			
			try {
				if (promInsertList.size() != 0)
					batchQueue.addBatch(dao.insertFeedback(new ArrayList<Promoter>(promInsertList)));
			} catch (DAOException e) {
				//TODO: remove gene (find index!) unless ignore and retry
				e.printStackTrace();
			} finally {
				promInsertList = new ConcurrentLinkedQueue<Promoter>();
			}
		}
	}

	@Override
	public void update(Promoter promoter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deletePromoter(String id) {
		try {
			manager.getPromoterDAO().delete(id);
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void deletePromoters() {
		try {
			manager.getPromoterDAO().deleteAll();
		} catch (DAOException e) {
			//TODO
			e.printStackTrace();
		}
	}

	@Override
	public Collection<Promoter> findPromoters(String id) {
		try {
			return manager.getPromoterDAO().retrieveForGene(id);
		} catch (DAOException e) {
			return new LinkedList<Promoter>();
		}		
	}

	@Override
	public Collection<Promoter> findPromoters(Collection<String> id) {
		try {
			return manager.getPromoterDAO().retrieve(id);
		} catch (DAOException e) {
			System.out.println("ConcurrentDatabaseHandler: no promoters found");
			return new LinkedList<Promoter>();
		}		
	}
	
	@Override
	public Collection<Promoter> findPromoters() {
		try {
			return manager.getPromoterDAO().retrieveAll();
		} catch (DAOException e) {
			return new LinkedList<Promoter>();
		}
	}

	@Override
	public void insert(final IdentifierSet set) {
		int totalGeneBatchCount = batchQueue.getPendingBatchCount() +
				batchQueue.getProcessedBatchCount();
		
		batchQueue.addDependency(new Dependency<Integer>(totalGeneBatchCount + 1) {

			@Override
			public boolean isFulfilled(Integer comparisonValue) {
				return comparisonValue > getDependencyValue();
			}

			@Override
			public void resume() {
				try {
					manager.getSetDAO().insert(set);
				} catch (DAOException e) {
					//TODO: DAOException
					e.printStackTrace();
				}
			}
			
		});
	}

	@Override
	public void update(IdentifierSet set) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteSet(int id) {
		try {
			manager.getSetDAO().delete(id);
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void deleteSets() {
		try {
			manager.getSetDAO().deleteAll();
		} catch (DAOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public IdentifierSet findSet(int id) {
		try {
			return manager.getSetDAO().retrieve(id);
		} catch (DAOException e) {
			return null;
		}
		
	}
	
	@Override
	public Collection<IdentifierSet> findSets() {
		try {
			return manager.getSetDAO().retrieveAll();
		} catch (DAOException e) {
			return null;
		}
	}

	@Override
	public void insert(Motif motif) {
		if (motif != null)
			motifInsertList.add(motif);
		
		if ((motifInsertList.size() % BATCH_SIZE) == 0 || motif == null) {
			ConcurrentMotifDAO dao = (ConcurrentMotifDAO) manager.getMotifDAO();
			
			try {
				if (motifInsertList.size() != 0)
					batchQueue.addBatch(dao.insertFeedback(new ArrayList<Motif>(motifInsertList)));
			} catch (DAOException e) {
				//TODO: remove motif (find index!) unless ignore and retry
				e.printStackTrace();
			} finally {
				motifInsertList = new ConcurrentLinkedQueue<Motif>();
			}
		}
	}

	@Override
	public void update(Motif pwm) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteMotifs(String id) {
		try {
			manager.getMotifDAO().delete(id);
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void deleteMotifs(int index) {
		try {
			manager.getMotifDAO().deleteByIndex(index);
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void deleteMotifs() {
		try {
			manager.getMotifDAO().deleteAll();
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Motif findMotif(String name) {
		try {
			return manager.getMotifDAO().retrieve(name);
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Motif findMotif(int id) {
		try {
			return manager.getMotifDAO().retrieveByIndex(id);
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public Collection<Motif> findMotifs(Collection<Integer> index) {
		try {
			return manager.getMotifDAO().retrieveByIndex(index);
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Collection<Motif> findMotifs() {
		try {
			return manager.getMotifDAO().retrieveAll();
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void deleteGenes(Collection<String> id) {
		try {
			manager.getGeneDAO().delete(id);
		} catch (DAOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void deletePromoters(Collection<String> id) {
		try {
			manager.getPromoterDAO().delete(id);
		} catch (DAOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void deleteMotifs(Collection<Integer> id) {
		try {
			manager.getMotifDAO().deleteByIndex(id);
		} catch (DAOException e) {
			e.printStackTrace();
		}
	}
	
}