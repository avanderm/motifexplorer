package gui.mvc;

import javax.swing.text.AttributeSet;

import util.Logger;
import engine.handler.DatabaseHandler;

public class PrintHelp implements Procedure<DatabaseHandler, Logger> {

	private String command;
	
	public PrintHelp(String command) {
		this.command = command;
	}
	
	@Override
	public void execute(DatabaseHandler handler, Logger logger,
			AttributeSet attr) {
		logger.println();
		
		switch(command) {
		case "general":
			logger.println("SYNTAX\n" +
					"\thelp [variable|command];");
			logger.println("DESCRIPTION\n" +
					"\tHelp invokes support for the program in general, or limited to variables and commands. The output\n" +
					"\twill consist of a syntax description for correct use of the variable or command, followed by a\n" +
					"\tdescription to explain its role in the program.\n");
			logger.println("\tPossible variables are:\n" +
					"\t\tg - Genes: consist of an identifier, a chomosome and strand location, a start and end position.\n" +
					"\t\tp - Promoters: consist of a gene reference, whether it located before (FORWARD) or after (REVERSE) the gene,\n" +
					"\t\t    and the sequence. The location is independent of what strand the gene is located on, all are taken to be\n" +
					"\t\t    FORWARD.\n" +
					"\t\ts - Sets: a collection of gene identifiers that make up the set, accompanied by a set description.\n" +
					"\t\tm - Motifs: consists of an identifier and 4xN matrix.\n");
			logger.println("\t\tType \"help [variable];\" to retrieve more information on proper usage and examples.\n");
			logger.println("\t\tEXAMPLES\n" +
					"\t\t\thelp g;");
			
			logger.println("\n\tPossible commands are:\n" +
					"\t\tparse_g - parse genes: parses a general feature format file and extracts genes and sets, if present. The\n" +
					"\t\t          results are stored in the database (tables: Genes, Sets, GeneSetRecords (GeneSetView)).\n" +
					"\t\tparse_p - parse promoters: parses a TAIR database file in order extract promoter sequences for genes stored\n" +
					"\t\t          in the database (table: Genes). The result are stored in the database (table: Promoters).\n" +
					"\t\tparse_m - parse motifs: parses motifs found in a raw format file. The frequency matrix is transposed and\n" +
					"\t\t          formatted to proper values. The result are stored in the database (table: Motifs).\n" +
					"\t\tmatrix_scan - matrix_scan: takes a collection of promoters and motifs as input and sends out SOAP messages\n" +
					"\t\t              to the RSAT webserver. To decrease external load, the promoters are divided into batches. The\n" +
					"\t\t              results are written away to a random generated file. The user is informed of the location of\n" +
					"\t\t              the file for future use. All files are stored in the \"output/\" folder relative to the program\n" +
					"\t\t              location.\n" +
					"\t\tfeature_map - feature_map: takes the output of a matrix_scan (file) request as input to display an image.\n" +
					"\t\t              The image shows the motif location for each promoter sequence.\n" +
					"\t\tmatch_count - match count: takes the output of a matrix_scan (file) request as input to count how many times\n" +
					"\t\t              a motif has been detected, printing a list with the count numbers for each motif.\n" +
					"\t\thelp - help: displays help files, general or limited to a variable or command.\n" +
					"\t\texit - exit: exits the program and closes all database connections.\n");
			logger.println("\t\tType \"help [command];\" to retrieve more information on proper usage and examples.\n");
			logger.println("\t\tEXAMPLES\n" +
					"\t\t\thelp parse_g;");
			break;
		case "g":
			logger.println("SYNTAX\n" +
					"\tg[(#)(#:#)];\n" +
					"\t[command](.., g[(#)(#:#)], ...);");
			logger.println("DESCRIPTION\n" +
					"\tSelects genes from database to use in commands or display in the console. If the expression is not encapsulated\n" +
					"\tin a command, it will be printed in the console. The selector is zero-based. The user can select arbitrary genes\n" +
					"\tusing mixed mode selector. There are no limits or constraints on order, the user can provide any combination of\n" +
					"\tsingle and range selectors to make a mixed mode selector (see: EXAMPLES).");
			logger.println("EXAMPLES\n" +
					"\tg;           prints all genes stored in the database.\n" +
					"\tg(3);        single selector:\n" +
					"\t             prints out gene SIP3, if present in the database.\n" +
					"\tg(0,3);      single selector:\n" +
					"\t             prints out gene SIP0 and SIP3, if present in the database.\n" +
					"\tg(1:29);     range selector:\n" +
					"\t             prints out genes SIP1 to SIP29, if present in the database.\n" +
					"\tg(2,1:6,8);  mixed mode:\n" +
					"\t             prints out SIP1 to SIP6 and SIP8, if present in the database.\n" +
					"\tg(s(1,2));   prints out genes for sets 1 and 2 combined.\n" +
					"\tg(s);        prints out genes for all sets combined.");
			break;
		case "p":
			logger.println("SYNTAX\n" +
					"\tp[(#)(#:#)];\n" +
					"\t[command](.., p[(#)(#:#)], ...);");
			logger.println("DESCRIPTION\n" +
					"\tSelects promoters from database to use in commands or display in the console. If the expression is not\n" +
					"\tencapsulated in a command, it will be printed in the console. The selector is zero-based. The user can select\n" +
					"\tarbitrary promoters using mixed mode selector. There are no limits or constraints on order, the user can\n" +
					"\tprovide any combination of single and range selectors to make a mixed mode selector (see: EXAMPLES). The\n" +
					"\tresults are shown in fasta format for instant use in RSAT.");
			logger.println("EXAMPLES\n" +
					"\tp;           prints all promoters stored in the database.\n" +
					"\tp(3);        single selector:\n" +
					"\t             prints out promoters for gene SIP3, if present in the database.\n" +
					"\tp(0,3);      single selector:\n" +
					"\t             prints out promoters for gene SIP0 and SIP3, if present in the database.\n" +
					"\tp(1:29);     range selector:\n" +
					"\t             prints out promoters for genes SIP1 to SIP29, if present in the database.\n" +
					"\tp(2,1:6,8);  mixed mode:\n" +
					"\t             prints out promoters for SIP1 to SIP6 and SIP8, if present in the database.\n" +
					"\tp(s(1,2));   prints out promoters for sets 1 and 2 combined.");
			break;
		case "s":
			logger.println("SYNTAX\n" +
					"\ts[(#)(#:#)];\n" +
					"\t[command](.., s[(#)(#:#)], ...);");
			logger.println("DESCRIPTION\n" +
					"\tSelects sets from database to use in commands or display in the console. If the expression is not encapsulated\n" +
					"\tin a command, it will be printed in the console. The selector is zero-based. The user can select arbitrary\n" +
					"\tsets using mixed mode selector. There are no limits or constraints on order, the user can provide any\n" +
					"\tcombination of single and range selectors to make a mixed mode selector (see: EXAMPLES). The output of this\n" +
					"\tcommand may be used to select genes and promoters (see: g, p).");
			logger.println("EXAMPLES\n" +
					"\ts;           prints all sets stored in the database.\n" +
					"\ts(3);        single selector:\n" +
					"\t             prints out set 3, if present in the database.\n" +
					"\ts(0,3);      single selector:\n" +
					"\t             prints out sets 0 and 3, if present in the database.\n" +
					"\ts(1:29);     range selector:\n" +
					"\t             prints out sets 1 to 29, if present in the database.\n" +
					"\ts(2,1:6,8);  mixed mode:\n" +
					"\t             prints out sets 1 to 6 and 8, if present in the database.");
			break;
		case "m":
			logger.println("SYNTAX\n" +
					"\tm[(#)(#:#)];\n" +
					"\t[command](.., m[(#)(#:#)], ...);");
			logger.println("DESCRIPTION\n" +
					"\tSelects motifs from database to use in commands or display in the console. If the expression is not encapsulated\n" +
					"\tin a command, it will be printed in the console. The selector is zero-based. The user can select arbitrary\n" +
					"\tmotifs using mixed mode selector. There are no limits or constraints on order, the user can provide any\n" +
					"\tcombination of single and range selectors to make a mixed mode selector (see: EXAMPLES). The motifs are\n" +
					"\talphabetically ordered in the database.");
			logger.println("EXAMPLES\n" +
					"\tm;           prints all motifs stored in the database.\n" +
					"\tm(3);        single selector:\n" +
					"\t             prints out motif 3, if present in the database.\n" +
					"\tm(0,3);      single selector:\n" +
					"\t             prints out motifs 0 and 3, if present in the database.\n" +
					"\tm(1:29);     range selector:\n" +
					"\t             prints out motifs 1 to 29, if present in the database.\n" +
					"\tm(2,1:6,8);  mixed mode:\n" +
					"\t             prints out motifs 1 to 6 and 8, if present in the database.");
			break;
		case "parse_g":
			logger.println("SYNTAX\n" +
					"\tparse_g(FILE);");
			logger.println("DESCRIPTION\n" +
					"\tParses the general feature format file FILE to extract genes and sets and stores them in the database. Relevant\n" +
					"\ttables are Genes, Sets, GeneSetRecords (GeneSetView). Genes, sets and promoters present in the database are deleted\n" +
					"\twhen this command is invoked.");
			logger.println("EXAMPLES\n" +
					"\tparse_g(data/OSIP_locations.txt);\n" +
					"\t\t\tparses the file \"data/OSIP_locations.txt\", relative to the program directory.");
			break;
		case "parse_p":
			logger.println("SYNTAX\n" +
					"\tparse_p(FILE);");
			logger.println("DESCRIPTION\n" +
					"\tParses the TAIR database file FILE to extract promoters, based on the genes present in the database and stores\n" +
					"\tthem in the database. Relevant tables are Promoters, Genes. Promoters present in the database are deleted when\n" +
					"\tthis command is invoked. This command assumes the availability of following files in folder \"data/\" relative to\n" +
					"\tthe program directory: chr1.fas, chr2.fas, chr3.fas, chr4.fas, chr5.fas, chrC.fas, chrM.fas");
			logger.println("EXAMPLES\n" +
					"\tparse_p(data/TAIR7_gene_features);\n" +
					"\t\t\tparses the file \"data/TAIR7_gene_features\", relative to the program directory.");
			break;
		case "parse_m":
			logger.println("SYNTAX\n" +
					"\tparse_m(FILE);");
			logger.println("DESCRIPTION\n" +
					"\tParses the raw matrix format file FILE to extract motifs and stores them in the database. Relevant table is\n" +
					"\tMotifs. Motifs present in the database are deleted when this command is invoked. The raw format frequency\n" +
					"\tmatrices are transposed and converted to tab format.");
			logger.println("EXAMPLES\n" +
					"\tparse_m(data/PWMs.txt);\n" +
					"\t\t\tparses the file \"data/PWMs.txt\", relative to the program directory.");
			break;
		case "matrix_scan":
			logger.println("SYNTAX\n" +
					"\tmatrix_scan(p[(#)(#:#)], m[(#)(#:#)]);");
			logger.println("DESCRIPTION\n" +
					"\tTakes a collection of promoters and motifs as input and sends out SOAP messages to the RSAT webserver. To\n" +
					"\tdecrease external load, the promoters are divided into batches. The results are written away to a random\n" +
					"\tgenerated file. The user is informed of the location of the file for future use. All files are stored in the\n" +
					"\t\"output/\" folder relative to the program directory.");
			logger.println("EXAMPLES\n" +
					"\tmatrix_scan(p,m);\n" +
					"\t\t\tscan all promoters for all motifs.\n" +
					"\tmatrix_scan(p(4:33),m(2,8,9:12));\n" +
					"\t\t\tscans promoters 4 to 33 for motifs 2 and 8 to 12.");
			break;
		case "feature_map":
			logger.println("SYNTAX\n" +
					"\tfeature_map(FILE);");
			logger.println("DESCRIPTION\n" +
					"\tDraws a feature map for the general feature format file FILE.");
			logger.println("EXAMPLES\n" +
					"\tfeature_map(output/data_001.txt);\n" +
					"\t\t\tDraws a feature map for the matrix scan output stored in file \"output/data_001.txt\".");
			break;
		case "match_count":
			logger.println("SYNTAX\n" +
					"\tmatch_count(FILE);");
			logger.println("DESCRIPTION\n" +
					"\tCounts motif detections for the general feature format file FILE.");
			logger.println("EXAMPLES\n" +
					"\tmatch_count(output/data_001.txt);");
			break;
		case "db":
			logger.println("SYNTAX\n" +
					"\tdb [delete|drop|init];");
			logger.println("DESCRIPTION\n" +
					"\tGrants direct control over the database, allowing the user to manipulate the contents.\n" +
					"\tThe user can perform three actions: delete, drop and init. The \"delete\" action deletes all\n" +
					"\tdatabase contents, leaving the database tables and structure intact. The \"drop\" statement\n" +
					"\tdrops all tables, and therefore all content. Lastly, the \"init\" statement initializes the\n" +
					"\ttables.");
			logger.println("EXAMPLES\n" +
					"\tdb delete;\n" +
					"\tdb init;");
			break;
		case "exit":
			logger.println("SYNTAX\n" +
					"\texit;");
			logger.println("DESCRIPTION\n" +
					"\tExits the program and closes all connections to the database.");
			break;
		default:
			logger.println("No information present on: " + command);
		}
		
		logger.println();
	}

}