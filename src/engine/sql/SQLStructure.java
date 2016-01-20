package engine.sql;

public interface SQLStructure {

	String getTableNameFor(String table);
	
	String getCreateStatementFor(String table);
	String getDropStatementFor(String table);
	
	String getInsertStatementFor(String table, boolean ignoreDuplicates);
	String getUpdateStatementFor(String table);
	String getDeleteStatementFor(String table);
	String getDeleteStatementForGene(String table);
	String getDeleteStatementForSet(String table);
	String getDeleteStatementForIndex(String table);
	
	String getDeleteAllStatementFor(String table);
	String getDeleteAllStatementForSet(String table);
	
	String getRetrieveStatementFor(String table, String placeHolder);
	String getRetrieveStatementForGene(String table);
	String getRetrieveStatementForSet(String table);
	String getRetrieveStatementForIndex(String table);
	
	String getRetrieveAllStatementFor(String table);
	
	String getCountStatementFor(String table);
	String getCountStatementForGene(String table);
}
