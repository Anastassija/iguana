package org.jgll.lexer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgll.grammar.Grammar;
import org.jgll.grammar.symbol.EOF;
import org.jgll.grammar.symbol.Keyword;
import org.jgll.grammar.symbol.RegularExpression;
import org.jgll.grammar.symbol.Symbol;
import org.jgll.grammar.symbol.Terminal;
import org.jgll.grammar.symbol.Token;
import org.jgll.util.Input;

public class GLLLexerImpl implements GLLLexer {

	private BitSet[] tokenIDs;
	
	/**
	 * tokens[inputIndex][tokenID] = length
 	 */
	private int[][] tokens;

	private Input input;

	private Grammar grammar;
		
	public GLLLexerImpl(Input input, Grammar grammar) {
		this.input = input;
		this.grammar = grammar;
		
		this.tokenIDs = new BitSet[input.size()];
		this.tokens = new int[input.size()][grammar.getCountTokens()];
				
		for(int i = 0; i < tokens.length; i++) {
			for(int j = 0; j < tokens[i].length; j++) {
				tokens[i][j] = -1;
			}
		}
		
		for(int i = 0; i < tokenIDs.length; i++) {
			tokenIDs[i] = new BitSet();
		}
				
		tokenize(input.toString());
	}

	@Override
	public BitSet tokenIDsAt(int index) {
		return tokenIDs[index];
	}
	
	@Override
	public boolean match(int inputIndex, BitSet expectedTokens) {
		return tokenIDs[inputIndex].intersects(expectedTokens);
	}
	
	@Override
	public int tokenLengthAt(int inputIndex, int tokenID) {
		return tokens[inputIndex][tokenID];
	}
	
	@Override
	public List<Integer> tokensAt(int inputIndex, BitSet expectedTokens) {
		List<Integer> list = new ArrayList<>();
		 for (int i = expectedTokens.nextSetBit(0); i >= 0; i = expectedTokens.nextSetBit(i+1)) {
			 if(tokens[inputIndex][i] >= 0) {
				 list.add(i);
			 }
		 }

		return list;
	}
	
	
	private void tokenize(String input) {
		
		Set<Integer> jobsSet = new HashSet<>();
		Deque<Integer> jobs = new ArrayDeque<>();
		jobs.add(0);
		jobsSet.add(0);
		
		while(!jobs.isEmpty()) {
			
			int i = jobs.poll();
			
			if(i >= input.length()) {
				continue;
			}
			
			// TODO: add a createToken method on the Token interface
			for(Token token : grammar.getTokens()) {
				int length;
				
				if(!token.asBitSet().get(input.charAt(i))) {
					continue;
				}
				
				if(token instanceof Keyword) {
					length = tokenize(i, input, (Keyword) token);
					if(length > 0) {
						int next = i + length;
						if(!jobsSet.contains(next)) {
							jobs.add(next);
							jobsSet.add(next);
						}
					}
				} 
				else if (token instanceof RegularExpression) {
					length = tokenize(i, input, (RegularExpression) token);
					if(length > 0) {
						int next = i + length;
						if(!jobsSet.contains(next)) {
							jobs.add(next);
							jobsSet.add(next);
						}
					}
				}
				else if(token instanceof Terminal) {
					length = tokenize(i, input, (Terminal) token);
					if(length > 0) {
						int next = i + length;
						if(!jobsSet.contains(next)) {
							jobs.add(next);
							jobsSet.add(next);
						}
					}
				}
			}
		}
		
		
		tokens[input.length() - 1][EOF.TOKEN_ID] = 0;
		tokenIDs[input.length() - 1].set(EOF.TOKEN_ID);
	}
	
	private int tokenize(int inputIndex, String input, Keyword keyword) {
		if(keyword.match(input, inputIndex)) {
			createToken(inputIndex, keyword, keyword.size());
			return keyword.size();
		}
		return -1;
	}
	
	private int tokenize(int inputIndex, String input, RegularExpression regex) {
		int length = regex.getAutomaton().run(input, inputIndex);
		if(length != -1) {
			createToken(inputIndex, regex, length);
		}
		return length;
	}
	
	private int tokenize(int inputIndex, String input, Terminal terminal) {
		if(terminal.match(input.charAt(inputIndex))) {
			createToken(inputIndex, terminal, 1);
			return 1;
		}
		return -1;
	}

	private void createToken(int inputIndex, Symbol symbol, int length) {
		Integer tokenID = grammar.getTokenID(symbol);
		tokenIDs[inputIndex].set(tokenID);
		tokens[inputIndex][tokenID] = length;
	}
	
	@Override
	public Input getInput() {
		return input;
	}

}
