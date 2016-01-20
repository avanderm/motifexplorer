package engine.handler;

import java.util.Collection;

import util.IdentifierSet;

import bio.*;

public interface DatabaseHandler extends Handler {

	void dropAll();
	void deleteAll();
	void initAll();
	
	void insert(Gene gene);
	void update(Gene gene);
	void deleteGene(String id);
	void deleteGenes(Collection<String> id);
	void deleteGenes();
	Gene findGene(String id);
	Collection<Gene> findGenes(Collection<String> id);
	Collection<Gene> findGenes();
	int countGenes();
	
	void insert(Promoter promoter);
	void update(Promoter promoter);
	void deletePromoter(String id);
	void deletePromoters(Collection<String> id);
	void deletePromoters();
	Collection<Promoter> findPromoters(String id);
	Collection<Promoter> findPromoters(Collection<String> id);
	Collection<Promoter> findPromoters();
	
	void insert(IdentifierSet set);
	void update(IdentifierSet set);
	void deleteSet(int id);
	void deleteSets();
	IdentifierSet findSet(int id);
	Collection<IdentifierSet> findSets();
	
	void insert(Motif pwm);
	void update(Motif pwm);
	void deleteMotifs(String id);
	void deleteMotifs(int index);
	void deleteMotifs(Collection<Integer> id);
	void deleteMotifs();
	Motif findMotif(String id);
	Motif findMotif(int index);
	Collection<Motif> findMotifs(Collection<Integer> index);
	Collection<Motif> findMotifs();
}