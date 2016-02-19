# MotifExplorer
SOAP interaction with RSAT (Regulatory Sequence Analysis Tools)

## Components

### bio

Contains object representations of biological data, in this case promotors and genes (DNA sequences) and motifs. These classes contains internal methods to convert between different formats.

### engine

The engine package consists of several parts, given below. It also provides the main class MotifExplorer initializating the GUI and engine. Application specfic classes to extract DNA sequences from fasta files are given as well.

#### sql

The sql packages provides the main classes interacting with the MySQL database through the  data access object design pattern. The database structure is hidden from the user, who interacts using functions to add/remove/... genes, promotors and other biological data.

SQL queries are hardcoded in this project, though user functions added to the SQL database would be a better choice.

#### parser

Uses the regex engine to parse files relevant to the project, such as RSAT data for matrix-scan, the TAIR7 gene annotation file, motif matrices and more.

#### io

Contains files for reading fasta format files to extract DNA regions given some interval. GenomeReader provides a high level reader by grouping together the sequences from all chromosome. Supplying it a DNASequence object where only the chromosome number and the interval are given will cause the DNA sequence to look up the DNA sequence and complete the DNASequence object.

#### concurrent
