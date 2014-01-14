package org.jgll.regex;

import org.jgll.util.Input;

public interface Matcher {
	
	/**
	 * Indicates whether the complete given input can be matched. 
	 */
	public boolean match(Input input);
	
	/**
	 * Returns the length of the longest match from the given input index.
	 * 
	 * @param input
	 * @param inputIndex
	 * @return
	 */
	public int match(Input input, int inputIndex);
	
	/**
	 * Returns true if can match the given input from the provided start position (inclusive)
	 * to the provided end position (exclusive).
	 *  
	 */
	public boolean match(Input input, int start, int end);
	
	public int matchBackwards(Input input, int inputIndex);
	
	public int getId();
	
	public void setId(int id);
}
