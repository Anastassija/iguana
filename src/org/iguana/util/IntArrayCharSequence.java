package org.jgll.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Ali Afroozeh
 *
 */
public class IntArrayCharSequence implements CharSequence {
	
	private final int[] input;

	private final char[] chars;

	private final int[] indices;
	
	private final int logicalLength;
	
	public IntArrayCharSequence(int[] input) {
		this.input = input;
		List<Integer> indicesList = new ArrayList<>();
		List<Character> charsList = new ArrayList<>();
		
		int logicalIndex = 0;
		
		for (int i = 0; i < input.length; i++) {
			if (Character.isBmpCodePoint(input[i])) {
				charsList.add((char) input[i]);
				indicesList.add(logicalIndex++);
			} else {
				char[] arr = Character.toChars(input[i]);
				charsList.add(arr[0]);
				charsList.add(arr[1]);
				indicesList.add(logicalIndex);
				indicesList.add(logicalIndex++);
			}
		}
		
		this.logicalLength = logicalIndex; 
		
		this.indices = new int[indicesList.size()];
		this.chars = new char[charsList.size()];
		for (int i = 0; i < indicesList.size(); i++) {
			indices[i] = indicesList.get(i);
			chars[i] = charsList.get(i);
		}
	}
	
	public int logicalLength() {
		return logicalLength;
	}
	
	public int logicalIndexAt(int index) {
		return indices[index];
	}

	@Override
	public int length() {
		return chars.length;
	}

	@Override
	public char charAt(int index) {
		return chars[index];
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		int[] dest = new int[end - start + 1];
		System.arraycopy(input, start, dest, end, end - start + 1);
		return new IntArrayCharSequence(dest);
	}
	
	@Override
	public String toString() {
		return new String(chars);
	}

}
