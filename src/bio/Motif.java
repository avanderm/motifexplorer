package bio;

public class Motif {

	private static final String TAB_FORMAT = "tab";
	private static final String RAW_FORMAT = "raw";
	
	private char[] basePairOrder;
	
	private String name;
	private int[][] matrix;
	
	private Motif(String name, int[][] matrix, char[] basePairOrder) {
		this.name = name;
		this.matrix = matrix;
		this.basePairOrder = basePairOrder;
	}
	
	public static Motif parseFormat(String matrixString, String format) {
		switch (format) {
		case TAB_FORMAT:
			return parseTabFormat(matrixString);
		case RAW_FORMAT:
			return parseRawFormat(matrixString);
		default:
			return null;
		}
	}
	
	private static Motif parseTabFormat(String matrixString) {
		char[] basePairOrder = new char[4];
		
		String[] lines = matrixString.split("\n");
		
		String name = (lines[0].split(" "))[1];
		
		String[] numbers = lines[1].split("\t");
		int dim = numbers.length;
		int[][] matrix = new int[4][dim - 1];
		
		int i = 0;
		do {
			basePairOrder[i] = numbers[0].charAt(0);
			for (int j = 1; j < dim; j++)
				matrix[i][j-1] = Integer.parseInt(numbers[j]);
			if (i > 2)
				break;
			numbers = lines[i+2].split("\t");
			i++;
		} while(true);
		
		return new Motif(name, matrix, basePairOrder);
	}
	
	private static Motif parseRawFormat(String matrixString) {
		char[] basePairOrder = {'A', 'C', 'G', 'T'};
		
		String[] lines = matrixString.split("\n");
		String name = (String) lines[0].subSequence(6, lines[0].length());
		
		int[][] matrix = new int[4][lines.length - 3];
		
		int i = 0;
		String[] numbers;
		do {
			numbers = lines[i+3].split("\\s+");
			for (int j = 0; j < 4; j++)
				matrix[j][i] = Math.round(100*Float.parseFloat(numbers[j]));
			i++;
		} while(i < matrix[0].length);
		
		return new Motif(name, matrix, basePairOrder);
	}
	
	public String getName() {
		return name;
	}
	
	public String getMatrixString() {
		String matrixString = "";
		for (int i = 0; i < 4; i++) {
			matrixString += basePairOrder[i];
			for (int j = 0; j < matrix[0].length; j++)
				matrixString += "\t" + matrix[i][j];
			matrixString += "\n";
		}
		
		return matrixString.trim();
	}
	
	public String toString() {
		String matrixString = ";" + getName() + "\n" + getMatrixString();
		
		
		return matrixString;
	}
}