package org.jgll.sppf;

import org.jgll.traversal.SPPFVisitor;

/**
 * Represents an intermediary node resulting from EBNF to BNF conversion of 
 * X* and X+, where X is a grammar symbol. 
 * 
 * @author Ali Afroozeh
 *
 */
public class ListSymbolNode extends NonterminalNode {

	public ListSymbolNode(int id, int leftExtent, int rightExtent) {
		super(id, leftExtent, rightExtent);
	}
	
	@Override
	public void accept(SPPFVisitor visitAction) {
		visitAction.visit(this);
	}
	
	@Override
	public ListSymbolNode init() {
		super.init();
		return this;
	}

}
