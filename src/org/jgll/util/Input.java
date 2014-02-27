package org.jgll.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jgll.traversal.PositionInfo;

/**
 * 
 * Is backed by an integer array to support UTF-32. 
 * 
 * @author Ali Afroozeh
 *
 */
public class Input {

	private int[] input;

	/**
	 * This array keeps the line and column information associated with each input index.
	 */
	private LineColumn[] lineColumns;
	
	/**
	 * Number of lines in the input.
	 */
	private int lineCount;
	
	private URI uri;
	
	private static int[] convert(String s) {
		int[] input = new int[s.length() + 1];
		for (int i = 0; i < s.length(); i++) {
			input[i] = s.codePointAt(i);
		}
		// The EOF character is assumed to have value 0 instead of the more common -1.  
		// as Bitsets cannot work with negative values. 
		input[s.length()] = 0; // TODO: we will run into some problems because Jurgen used 0 for escaping.

		return input;
	}
	
	public static Input fromChar(char c) {
		int[] input = new int[2];
		input[0] = c;
		input[1] = 0;
		return new Input(input, URI.create("dummy:///"));
	}
	
	public static Input fromIntArray(int[] input) {
		return new Input(input, URI.create("dummy:///"));
	}
	
	public static Input fromCharArray(char[] input, URI uri) {
		int[] result = new int[Character.codePointCount(input, 0, input.length) + 1];
		int j = 0;
		
		for(int i = 0; i < input.length; i++){
			if (!Character.isLowSurrogate(input[i])) {
				result[j++] = Character.codePointAt(input, i);
			}
		}
		
		result[result.length - 1] = 0;
		return new Input(result, uri);
	}

	public static Input fromString(String s) {
		return new Input(convert(s), URI.create("dummy:///"));
	}
	
	public static Input fromString(String s, URI uri) {
		return new Input(convert(s), uri);
	}
	
	public static Input fromIntArray(int[] input, URI uri) {
		return new Input(input, uri);
	}
	
	public static Input fromPath(String path) throws IOException {
		return new Input(convert(readTextFromFile(path)), URI.create("file:///" + path));
	}

	private Input(int[] input, URI uri) {
		this.input = input;
		this.uri = uri;
		lineColumns = new LineColumn[input.length];
		calculateLineLengths();
	}
	
	public int charAt(int index) {
		return input[index];
	}

	public int length() {
		return input.length;
	}
	
	/**
	 * Returns the whole contents of a text file as a string.
	 * 
	 * @param path
	 *            the path to the text file
	 * @throws IOException
	 */
	private static String readTextFromFile(String path) throws IOException {
		return readTextFromFile(new File(path));
	}

	private static String readTextFromFile(File file) throws IOException {
		StringBuilder sb = new StringBuilder();

		InputStream in = new BufferedInputStream(new FileInputStream(file));
		int c = 0;
		while ((c = in.read()) != -1) {
			sb.append((char) c);
		}

		in.close();

		return sb.toString();
	}

	public static String read(InputStream is) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedInputStream in = new BufferedInputStream(is);
		int c = 0;
		while ((c = in.read()) != -1) {
			sb.append((char) c);
		}
		in.close();
		return sb.toString();
	}
	
	public int[] subInput(int start, int end) {
		int length = end - start + 1;
		int[] subInput = new int[length];
		
		System.arraycopy(input, start, subInput, 0, length);
		
		return subInput;
	}
	
	public boolean match(int start, int end, String target) {
		return match(start, end, toIntArray(target));
	}
	
	public boolean match(int start, int end, int[] target) {
		if(target.length != end - start) {
			return false;
		}
	 	
		int i = 0;
		while(i < target.length) {
			if(target[i] != input[start + i]) {
				return false;
			}
			i++;
		}
		
		return true;
	}

	public boolean match(int from, String target) {
		return match(from, toIntArray(target));
	}
	
	public boolean matchBackward(int start, String target) {
		return matchBackward(start, toIntArray(target));
	}
	
	public boolean matchBackward(int start, int[] target) {
		if(start - target.length < 0) {
			return false;
		}
		
		int i = target.length - 1;
		int j = start - 1;
		while(i >= 0) {
			if(target[i] != input[j]) {
				return false;
			}
			i--;
			j--;
		}
		
		return true;
	}
	
	public boolean match(int from, int[] target) {
		
		if(target.length > length() - from) {
			return false;
		}
		
		int i = 0;
		while(i < target.length) {
			if(target[i] != input[from + i]) {
				return false;
			}
			i++;
		}
		
		return true;
	}
	
	public static int[] toIntArray(String s) {
		int[] array = new int[s.codePointCount(0, s.length())];
		for(int i = 0; i < array.length; i++) {
			array[i] = s.codePointAt(i);
		}
		return array;
	}
 
	public int getLineNumber(int index) {
		if(index < 0 || index >= lineColumns.length) {
			return 0;
		}
		return lineColumns[index].getLineNumber();
	}
	
	public int getColumnNumber(int index) {
		if(index < 0 || index >= lineColumns.length) {
			return 0;
		}
		return lineColumns[index].getColumnNumber();
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if(this == obj) {
			return true;
		}
		
		if(! (obj instanceof Input)) {
			return false;
		}
		
		Input other = (Input) obj;
		
		return Arrays.equals(input, other.input);
	}
	
	public PositionInfo getPositionInfo(int leftExtent, int rightExtent) {
		return new PositionInfo(leftExtent, 
								rightExtent - leftExtent, 
								getLineNumber(leftExtent), 
								getColumnNumber(leftExtent), 
								getLineNumber(rightExtent), 
								getColumnNumber(rightExtent),
								uri);
	}
	
	

	private void calculateLineLengths() {
		int lineNumber = 1;
		int columnNumber = 1;

		// Empty input: only the end of line symbol
		if(input.length == 1) {
			lineColumns[0] = new LineColumn(lineNumber, columnNumber);
			return;
		}
		
		for (int i = 0; i < input.length; i++) {
			lineColumns[i] = new LineColumn(lineNumber, columnNumber);
			if (input[i] == '\n') {
				lineCount++;
				lineNumber++;
				columnNumber = 1;
			} else if (input[i] == '\r') {
				columnNumber = 1;
			} else {
				columnNumber++;
			}
		}
		
		// The end of the line char column as the last character
//		lineColumns[input.length - 1] = new LineColumn(lineColumns[input.length - 2]);
	}
		
	private static class LineColumn {

		private int lineNumber;
		private int columnNumber;
		
		public LineColumn(int lineNumber, int columnNumber) {
			this.lineNumber = lineNumber;
			this.columnNumber = columnNumber;
		}
		
		public int getLineNumber() {
			return lineNumber;
		}
		
		public int getColumnNumber() {
			return columnNumber;
		}
		
		@Override
		public String toString() {
			return "(" + lineNumber + ":" + columnNumber + ")";
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			
			if(!(obj instanceof LineColumn)) {
				return false;
			}
			
			LineColumn other = (LineColumn) obj;
			return lineNumber == other.lineNumber && columnNumber == other.columnNumber;
		}
	}
	
	/**
	 * Returns a string representation of this input instance from the
	 * given start (including) and end (excluding) indices.
	 *  
	 */
	public String subString(int start, int end) {
		List<Character> charList = new ArrayList<>();
		
		for(int i = start; i < end; i++) {
			char[] chars = Character.toChars(input[i]);
			for(char c : chars) {
				charList.add(c);
			}			
		}
		
		StringBuilder sb = new StringBuilder();
		for(char c : charList) {
			sb.append(c);
		}
		
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return subString(0, input.length);
	}
	
	public int getLineCount() {
		return lineCount;
	}
	
	public boolean isEmpty() {
		return length() == 1;
	}

	public boolean isEndOfLine(int currentInputIndex) {
		return input[currentInputIndex] == 0 || lineColumns[currentInputIndex + 1].columnNumber == 1;
	}

	public boolean isStartOfLine(int currentInputIndex) {
		return currentInputIndex == 0 || lineColumns[currentInputIndex].columnNumber == 1;
	}
}
