package org.jgll.sppf;

import java.util.Collections;
import java.util.List;

import org.jgll.grammar.slot.TerminalGrammarSlot;
import org.jgll.traversal.SPPFVisitor;

/**
 * 
 * @author Ali Afroozeh
 *
 */
public class TerminalNode extends NonPackedNode {
	
	public TerminalNode(TerminalGrammarSlot slot, int leftExtent, int rightExtent) {
		super(slot, leftExtent, rightExtent);
	}
	
	@Override
	public void accept(SPPFVisitor visitAction) {
		visitAction.visit(this);
	}

	@Override
	public TerminalGrammarSlot getGrammarSlot() {
		return (TerminalGrammarSlot) slot;
	}

	@Override
	public int childrenCount() {
		return 0;
	}
	
	@Override
	public boolean isAmbiguous() {
		return false;
	}

	@Override
	public SPPFNode getChildAt(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<PackedNode> getChildren() {
		return Collections.emptyList();
	}
	
}