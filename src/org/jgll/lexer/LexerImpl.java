package org.jgll.lexer;

import java.util.BitSet;

import org.jgll.grammar.GrammarGraph;
import org.jgll.util.Input;

public class LexerImpl implements Lexer {
	
	private static final int UNMATCHED = -1;

	/**
	 * tokens[inputIndex][tokenID] = length
 	 */
	private int[][] tokens;

	private Input input;

	private GrammarGraph grammar;

	public LexerImpl(Input input, GrammarGraph grammar) {
		this.input = input;
		this.grammar = grammar;
		
		this.tokens = new int[input.length()][grammar.getCountTokens()];
		
		for(int i = 0; i < tokens.length; i++) {
			for(int j = 0; j < tokens[i].length; j++) {
				tokens[i][j] = UNMATCHED;
			}
		}
	}
	
	@Override
	public int tokenAt(int inputIndex, BitSet set) {
		for (int i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(i+1)) {
			if(tokens[inputIndex][i] >= 0) {
				return i;
			} else {
				int length = grammar.getAutomaton(i).match(input, inputIndex);
				tokens[inputIndex][i] = length;
				
				if(length >= 0) {
					return i;
				}
			}
		}
		return -1;
	}
	
	@Override
	public void setTokenAt(int inputIndex, int tokenID, int length) {
		tokens[inputIndex][tokenID] = length;
	}
	
	@Override
	public int tokenLengthAt(int inputIndex, int tokenID) {
		if(tokens[inputIndex][tokenID] == UNMATCHED) {
			int length = grammar.getAutomaton(tokenID).match(input, inputIndex);
			tokens[inputIndex][tokenID] = length;
			return length;
		}
		return tokens[inputIndex][tokenID];
	}
	
	@Override
	public Input getInput() {
		return input;
	}

}
