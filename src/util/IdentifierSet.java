package util;

import java.util.Collection;
import java.util.TreeSet;

public class IdentifierSet extends TreeSet<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2742694314800206027L;
	
	private int id;
	private String description;
	
	public IdentifierSet(int id, String description) {
		this(id, description, null);
	}
	
	public IdentifierSet(int id, String description, Collection<String> entries) {
		this.id = id;
		this.description = description;
		
		if (entries != null)
			this.addAll(entries);
	}
	
	public int getID() {
		return id;
	}
	
	public String getDescription() {
		return description;
	}

}