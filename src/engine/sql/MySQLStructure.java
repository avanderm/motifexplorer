package engine.sql;

public class MySQLStructure implements SQLStructure {

	private static final String GENE_TABLE     = "Genes";
	private static final String GENE_SET_TABLE = "GeneSetRecords";
	private static final String SET_TABLE      = "Sets";
	private static final String GENE_SET_VIEW  = "GeneSetView";
	
	private static final String PROMOTER_TABLE = "Promoters";
	private static final String MOTIF_TABLE    = "Motifs";
	
	@Override
	public String getTableNameFor(String table) {
		switch(table) {
		case GENE_TABLE:
			return "Genes";
		case SET_TABLE:
			return "Sets";
		case GENE_SET_TABLE:
			return "GeneSetRecords";
		case GENE_SET_VIEW:
			return "GeneSetView";
		case PROMOTER_TABLE:
			return "Promoters";
		case MOTIF_TABLE:
			return "Motifs";
		}
		
		return null;
	}
	
	@Override
	public String getCreateStatementFor(String table) {
		switch(table) {
		case GENE_TABLE:
			return "CREATE TABLE IF NOT EXISTS Genes" +
					"(Gene_GeneID BIGINT," +
					" Gene_GeneName VARCHAR(10)," +
					" Gene_Chromosome INTEGER," +
					" Gene_GeneStart INTEGER," +
					" Gene_GeneEnd INTEGER," +
					" PRIMARY KEY(Gene_GeneID))";
		case SET_TABLE:
			return "CREATE TABLE IF NOT EXISTS Sets" +
					"(Set_SetID INTEGER," +
					" Set_SetDescription VARCHAR(500)," +
					" PRIMARY KEY(Set_SetID))";
		case GENE_SET_TABLE:
			return "CREATE TABLE IF NOT EXISTS GeneSetRecords" +
					"(GeneID BIGINT," +
					" SetID INTEGER, " +
					" FOREIGN KEY (GeneID) REFERENCES Genes(Gene_GeneID)," +
					" FOREIGN KEY (SetID)  REFERENCES Sets(Set_SetID)," + 
					" PRIMARY KEY (SetID, GeneID))";
		case GENE_SET_VIEW:
			return "CREATE OR REPLACE VIEW GeneSetView AS" +
					" SELECT SetID, Gene_GeneName, Gene_Chromosome, Gene_GeneStart" +
					" FROM GeneSetRecords, Genes" +
					" WHERE GeneID = Gene_GeneID";
		case PROMOTER_TABLE:
			return "CREATE TABLE IF NOT EXISTS Promoters" +
					"(Prom_PromID BIGINT," +
					" Prom_GeneID BIGINT," +
					" Prom_PromoterSequence VARCHAR(1500)," +
					" Prom_Strand VARCHAR(15)," +
					" Prom_Chromosome INTEGER," +
					" Prom_PromoterStart INTEGER," +
					" Prom_PromoterEnd INTEGER," +
					" PRIMARY KEY(Prom_PromID))";
		case MOTIF_TABLE:
			return "CREATE TABLE Motifs" +
					"(Mot_MotifName VARCHAR(20)," +
					" Mot_MotifMatrix VARCHAR(200)," +
					" PRIMARY KEY(Mot_MotifName))";
		}
		
		return null;
	}

	@Override
	public String getDropStatementFor(String table) {
		switch(table) {
		case GENE_TABLE:
			return "DROP TABLE Genes";
		case SET_TABLE:
			return "DROP TABLE Sets";
		case GENE_SET_TABLE:
			return "DROP TABLE GeneSetRecords";
		case GENE_SET_VIEW:
			return "DROP VIEW GeneSetView";
		case PROMOTER_TABLE:
			return "DROP TABLE Promoters";
		case MOTIF_TABLE:
			return "DROP TABLE Motifs";
		}
		
		return null;
	}

	@Override
	public String getInsertStatementFor(String table, boolean ignoreDuplicates) {
		switch(table) {
		case GENE_TABLE:
			return "INSERT INTO Genes VALUES(?,?,?,?,?)";
		case SET_TABLE:
			return "INSERT INTO Sets VALUES(?,?)";
		case GENE_SET_TABLE:
			return "INSERT INTO GeneSetRecords VALUES(?,?)";
		case PROMOTER_TABLE:
			return "INSERT INTO Promoters VALUES(?,?,?,?,?,?,?)";
		case MOTIF_TABLE:
			return "INSERT INTO Motifs VALUES(?,?)";
		}
		
		return null;
	}

	@Override
	public String getUpdateStatementFor(String table) {
		switch(table) {
		case GENE_TABLE:
			return "UPDATE Genes SET" +
					" Gene_Chromosome = ?," +
					" Gene_GeneStart = ?," +
					" Gene_GeneEnd = ?" +
					" WHERE Gene_GeneID = ?";
		case SET_TABLE:
			return "UPDATE Sets SET" +
					" Set_SetDescription = ? " +
					" WHERE Set_SetID = ?";
		case PROMOTER_TABLE:
			return "UPDATE Promoters SET" +
					" Prom_PromoterSequence = ?" +
					" WHERE Prom_PromID = ?";
		case MOTIF_TABLE:
			return "UPDATE Motifs SET" +
					" Mot_MotifSequence = ?" +
					" WHERE Mot_MotifName = ?";
		}
		
		return null;
	}

	@Override
	public String getDeleteStatementFor(String table) {
		switch(table) {
		case GENE_TABLE:
			return "DELETE FROM Genes WHERE Gene_GeneID = ?";
		case SET_TABLE:
			return "DELETE FROM Sets WHERE Set_SetID = ?";
		case PROMOTER_TABLE:
			return "DELETE FROM Promoters WHERE Prom_PromID = ?";
		case MOTIF_TABLE:
			return "DELETE FROM Motifs WHERE Mot_MotifName = ?";
		}
		
		return null;
	}
	
	@Override
	public String getDeleteStatementForGene(String table) {
		switch(table) {
		case GENE_SET_TABLE:
			return "DELETE FROM GeneSetRecords WHERE GeneID = ?";
		case PROMOTER_TABLE:
			return "DELETE FROM Promoters WHERE Prom_GeneID = ?";
		}
		
		return null;
	}
	
	@Override
	public String getDeleteStatementForSet(String table) {
		switch(table) {
		case GENE_SET_TABLE:
			return "DELETE FROM GeneSetRecords WHERE SetID = ? AND GeneID = ?";
		}
		
		return null;
	}
	
	@Override
	public String getDeleteStatementForIndex(String table) {
		switch(table) {
		case MOTIF_TABLE:
			return "DELETE FROM Motifs" +
					" WHERE EXISTS" +
					"(SELECT Mot_MotifName" +
					" FROM Motifs" +
					" LIMIT ?,1)";
		}
		
		return null;
	}
	
	@Override
	public String getDeleteAllStatementForSet(String table) {
		switch(table) {
		case GENE_SET_TABLE:
			return "DELETE FROM GeneSetRecords WHERE SetID = ?";
		}
		
		return null;
	}

	@Override
	public String getDeleteAllStatementFor(String table) {
		switch(table) {
		case GENE_TABLE:
			return "DELETE FROM Genes";
		case SET_TABLE:
			return "DELETE FROM Sets";
		case GENE_SET_TABLE:
			return "DELETE FROM GeneSetRecords";
		case PROMOTER_TABLE:
			return "DELETE FROM Promoters";
		case MOTIF_TABLE:
			return "DELETE FROM Motifs";
		}
		
		return null;
	}
	
	@Override
	public String getRetrieveStatementFor(String table, String placeHolder) {
		switch(table) {
		case GENE_TABLE:
			return "SELECT Gene_GeneName, Gene_Chromosome, Gene_GeneStart, Gene_GeneEnd" +
					" FROM Genes" +
					" WHERE Gene_GeneID IN " + placeHolder +
					" ORDER BY Gene_Chromosome ASC, Gene_GeneStart ASC";
		case SET_TABLE:
			return "SELECT *" +
					" FROM Sets" +
					" WHERE Set_SetID IN " + placeHolder;
		case PROMOTER_TABLE:
			return "SELECT Gene_GeneName, Prom_PromoterSequence," +
					" Prom_Strand, Prom_Chromosome," +
					" Prom_PromoterStart, Prom_PromoterEnd" +
					" FROM Promoters, Genes" +
					" WHERE Gene_GeneID = Prom_GeneID AND Gene_GeneID IN " + placeHolder +
					" ORDER BY Prom_Chromosome ASC, Prom_PromoterStart ASC";
		case MOTIF_TABLE:
			return "SELECT Mot_MotifMatrix" +
					" FROM Motifs" +
					" WHERE Mot_MotifName IN " + placeHolder;
		}
		
		return null;
	}
	
	@Override
	public String getRetrieveStatementForGene(String table) {
		//TODO: Incorrect for promoters
		switch(table) {
		case GENE_SET_TABLE:
			return "SELECT SetID FROM GeneSetRecords" +
					" WHERE GeneID = ?";
		case PROMOTER_TABLE:
			return "SELECT Gene_GeneName, Prom_PromoterSequence," +
					" Prom_Strand, Prom_Chromosome," +
					" Prom_PromoterStart, Prom_PromoterEnd" +
					" FROM Promoters, Genes" +
					" WHERE Prom_GeneID = Gene_GeneID AND" +
					" Prom_GeneID = ?";
		}
		
		return null;
	}
	
	public String getRetrieveStatementForSet(String table) {
		switch(table) {
		case GENE_SET_VIEW:
			return "SELECT Gene_GeneName" +
					" FROM GeneSetView" +
					" WHERE SetID = ?" +
					" ORDER BY Gene_Chromosome ASC, Gene_GeneStart ASC";
		case SET_TABLE:
			return "SELECT Set_SetDescription" +
					" FROM Sets" +
					" WHERE Set_SetID = ?";
		}
		return null;
	}
	
	@Override
	public String getRetrieveStatementForIndex(String table) {
		switch(table) {
		case GENE_TABLE:
			return "SELECT Gene_GeneName, Gene_Chromosome, Gene_GeneStart, Gene_GeneEnd" +
					" FROM Genes" +
					" ORDER BY Gene_Chromosome ASC, Gene_GeneStart ASC" +
					" LIMIT ?,1";
		case MOTIF_TABLE:
			return "SELECT Mot_MotifName, Mot_MotifMatrix" +
					" FROM Motifs" +
					" LIMIT ?,1";
		}
		
		return null;
	}
	
	@Override
	public String getRetrieveAllStatementFor(String table) {
		switch(table) {
		case GENE_TABLE:
			return "SELECT Gene_GeneName, Gene_Chromosome, Gene_GeneStart, Gene_GeneEnd" + 
					" FROM Genes" +
					" ORDER BY Gene_Chromosome ASC, Gene_GeneStart ASC";
		case SET_TABLE:
			return "SELECT *" +
					" FROM Sets";
		case PROMOTER_TABLE:
			return "SELECT Gene_GeneName, Prom_PromoterSequence," +
					" Prom_Strand, Prom_Chromosome," +
					" Prom_PromoterStart, Prom_PromoterEnd" +
					" FROM Promoters, Genes" +
					" WHERE Gene_GeneID = Prom_GeneID" +
					" ORDER BY Prom_Chromosome ASC, Prom_PromoterStart ASC";
		case MOTIF_TABLE:
			return "SELECT Mot_MotifName, Mot_MotifMatrix" +
					" FROM Motifs";
		}
		
		return null;
	}

	@Override
	public String getCountStatementFor(String table) {
		switch(table) {
		case GENE_TABLE:
			return "SELECT COUNT(Gene_GeneID)" +
					" FROM Genes";
		}
		
		return null;
	}
	
	@Override
	public String getCountStatementForGene(String table) {
		switch(table) {
		case PROMOTER_TABLE:
			return "SELECT COUNT(Prom_PromID)" +
					" FROM Promoters" +
					" WHERE Prom_GeneID = ?";
		}
		
		return null;
	}

}