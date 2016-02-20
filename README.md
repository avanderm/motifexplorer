# MotifExplorer
SOAP interaction with RSAT (Regulatory Sequence Analysis Tools)

## Components

### bio

Contains object representations of biological data, in this case promotors and genes (DNA sequences) and motifs. These classes contains internal methods to convert between different formats.

### engine

The engine package consists of several parts, given below. It also provides the main class MotifExplorer initializating the GUI and engine. Application specfic classes to extract DNA sequences from fasta files are given as well.

The **sql** package provides the main classes interacting with the MySQL database through the  data access object design pattern (DAO). The database structure is hidden from the user, who interacts using functions to add/remove/... genes, promotors and other biological data. The ConcurrentDAO class was implemented using multithreading to perform database calls as an exercise.

SQL queries are hardcoded in this project as opposed to creating database functions.

The **parser** package uses the regex engine to parse files relevant to the project, such as RSAT data for matrix-scan, the TAIR7 gene annotation file, motif matrices and more.

The **io** package contains files for reading fasta format files to extract DNA regions given some interval. GenomeReader provides a high level reader by grouping together the sequences from all chromosome. Supplying it a DNASequence object where only the chromosome number and the interval are given will cause the DNA sequence to look up the DNA sequence and complete the DNASequence object.

The **concurrent** package supplies classes to deal with database dependencies such as foreign keys to ensure that data is not added to the database until other data it relies upon has been added first. In general this structure could be used for multithreading purposes to postpone tasks that rely upon completing other tasks first.

The **handler** package provides a high level interface to the database (dropping tables, querying, ...) in this application. It can also be used to parse files that require additional logic by pooling together multiple resources such as the PromotorExtractor class in the **engine** package.

Finally the **rsatws** interacts with the RSAT webserver through the SOAP protocol to detects motifs in the extracted promotor regions. Using tickets, multiple jobs may be posted at the same time and monitored until completion. This has been implemented using only the standard Java library.

### gui

The gui package bundles together classes to set up and draw a simple terminal and a console. The terminal comprises all the GUI components (frame, menus, ...) and in the console refers to the JTextArea in the terminal.

The console does not only provide a custom behaviour (can't delete text from previous commands and results, console history, ...) but it also includes a parser to process commands given and links up with the engine through the command design pattern with classes given in the **mvc** subpackage (processing files, database storing, database querying, ...).

### util

Utility classes mainly linked to design patterns and logging.
