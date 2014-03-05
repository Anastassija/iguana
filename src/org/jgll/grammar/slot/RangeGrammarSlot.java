package org.jgll.grammar.slot;

import org.jgll.grammar.symbol.Range;
import org.jgll.grammar.symbol.Rule;
import org.jgll.lexer.GLLLexer;
import org.jgll.parser.GLLParser;
import org.jgll.sppf.TokenSymbolNode;

public class RangeGrammarSlot extends TokenGrammarSlot {

	private static final long serialVersionUID = 1L;
	
	private Range r;

	public RangeGrammarSlot(Rule rule, int position, int slotId, String label, BodyGrammarSlot previous, Range r, HeadGrammarSlot head, int tokenID) {
		super(rule, position, slotId, label, previous, r, head, tokenID);
		this.r = r;
	}
	
	public RangeGrammarSlot copy(BodyGrammarSlot previous, String label, HeadGrammarSlot head) {
		RangeGrammarSlot slot = new RangeGrammarSlot(rule, position, slotId, label, previous, this.r, head, this.tokenID);
		slot.preConditions = preConditions;
		slot.popActions = popActions;
		return slot;
	}
	
	@Override
	public GrammarSlot parse(GLLParser parser, GLLLexer lexer) {
		
		if(executePreConditions(parser, lexer)) {
			return null;
		}
		
		int ci = parser.getCurrentInputIndex();
		int v = lexer.getInput().charAt(ci);
		
		if(v < r.getStart() || v > r.getEnd()) {
			parser.recordParseError(this);
			return null;
		}
		
		TokenSymbolNode cr = parser.getTokenNode(tokenID, ci, 1);
		
		// No GSS node is created for token grammar slots, therefore, pop
		// actions should be executed at this point
		if(executePopActions(parser, lexer)) {
			return null;
		}
		
		if(next instanceof LastGrammarSlot) {
			parser.getNonterminalNode((LastGrammarSlot) next, cr);
			parser.pop();
			return null;
		} else {
			parser.getIntermediateNode(next, cr);
		}
		
		return next;
	}
	
	@Override
	public boolean isNullable() {
		return false;
	}

}
