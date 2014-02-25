package org.jgll.sppf;

import org.jgll.grammar.slot.HeadGrammarSlot;
import org.jgll.traversal.SPPFVisitor;

/**
 * Represents an intermediary node resulting from EBNF to BNF conversion of 
 * X* and X+, where X is a grammar symbol. 
 * 
 * @author Ali Afroozeh
 *
 */
public class ListSymbolNode extends NonterminalSymbolNode {

	public ListSymbolNode(HeadGrammarSlot slot, int leftExtent, int rightExtent) {
		super(slot, leftExtent, rightExtent);
	}
	
	@Override
	public void accept(SPPFVisitor visitAction) {
		visitAction.visit(this);
	}

}
