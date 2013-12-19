package org.jgll.grammar.slot;

import java.io.IOException;
import java.io.Writer;

import org.jgll.grammar.symbol.Keyword;
import org.jgll.grammar.symbol.RegularExpression;
import org.jgll.grammar.symbol.Terminal;
import org.jgll.grammar.symbol.Token;
import org.jgll.lexer.GLLLexer;
import org.jgll.parser.GLLParser;
import org.jgll.recognizer.GLLRecognizer;
import org.jgll.sppf.SPPFNode;
import org.jgll.sppf.TokenSymbolNode;

/**
 * A grammar slot whose next immediate symbol is a terminal.
 * 
 * @author Ali Afroozeh
 *
 */
public class TokenGrammarSlot extends BodyGrammarSlot {
	
	private static final long serialVersionUID = 1L;
	
	private int tokenID;
	
	private Token token;
	
	public TokenGrammarSlot(int position, BodyGrammarSlot previous, Token token, HeadGrammarSlot head, int tokenID) {
		super(position, previous, head);
		this.token = token;
		this.tokenID = tokenID;
	}
	
	public TokenGrammarSlot copy(BodyGrammarSlot previous, HeadGrammarSlot head) {
		TokenGrammarSlot slot = new TokenGrammarSlot(this.position, previous, this.token, head, this.tokenID);
		slot.preConditions = preConditions;
		slot.popActions = popActions;
		return slot;
	}
		
	@Override
	public GrammarSlot parse(GLLParser parser, GLLLexer lexer) {
		
		int ci = parser.getCurrentInputIndex();

		int length = lexer.tokenLengthAt(ci, tokenID);
		
		if(length > 0) {
				if(executePreConditions(parser, lexer)) {
					return null;
				}
				
				TokenSymbolNode cr = parser.getTokenNode(tokenID, ci, length);
				if(next instanceof LastGrammarSlot) {
					parser.getNonterminalNode((LastGrammarSlot) next, cr);
					parser.pop();
					return null;
				} else {
					parser.getIntermediateNode(next, cr);
				}
			} 
			else {
				parser.recordParseError(this);
				return null;
		}	
		
		return next;
	}
	
	@Override
	public SPPFNode parseLL1(GLLParser parser, GLLLexer lexer) {
		
		int ci = parser.getCurrentInputIndex();

		int length = lexer.tokenLengthAt(ci, tokenID);
		
		if(length > 0) {
			if(executePreConditions(parser, lexer)) {
				return null;
			}
			
			return parser.getTokenNode(tokenID, ci, length);
		} 			
		else {
			parser.recordParseError(this);
			return null;
		}
	}
	
	@Override
	public GrammarSlot recognize(GLLRecognizer recognizer, GLLLexer lexer) {
		int ci = recognizer.getCi();
		org.jgll.recognizer.GSSNode cu = recognizer.getCu();
		
		int length = lexer.tokenLengthAt(ci, tokenID);
		
		// A::= x1
		if(previous == null && next.next() == null) {
			if(length > 0) {
				ci += length;
				recognizer.update(cu, ci);
			} else {
				recognizer.recognitionError(cu, ci);
				return null;
			}
		}
		
		// A ::= x1...xf, f ≥ 2
		else if(previous == null && !(next.next() == null)) {
			if(length > 0) {
				ci += length;
				recognizer.update(cu, ci);
			} else {
				recognizer.recognitionError(cu, ci);
				return null;
			}
		}
		
		// A ::= α · a β
		else {
			if(length > 0) {
				ci += length;
				recognizer.update(cu, ci);
			} else {
				recognizer.recognitionError(cu, ci);
				return null;
			}
		}
		
		return next;
	}
	
	@Override
	public void codeParser(Writer writer) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void codeIfTestSetCheck(Writer writer) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isNullable() {
		if(token instanceof Terminal || token instanceof Keyword) {
			return false;
		}
		if(token instanceof RegularExpression) {
			return ((RegularExpression) token).isNullable();
		}
		return false;
	}

	
	public int getTokenID() {
		return tokenID;
	}
	
	@Override
	public Token getSymbol() {
		return token;
	}
	
	@Override
	public boolean isNameEqual(BodyGrammarSlot slot) {
		if(this == slot) {
			return true;
		}
		
		if(!(slot instanceof TokenGrammarSlot)) {
			return false;
		}
		
		TokenGrammarSlot other = (TokenGrammarSlot) slot;
		
		return tokenID == other.tokenID;
	}

}
