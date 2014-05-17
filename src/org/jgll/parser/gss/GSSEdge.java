package org.jgll.parser.gss;

import org.jgll.grammar.slot.BodyGrammarSlot;
import org.jgll.parser.HashFunctions;
import org.jgll.sppf.SPPFNode;

public class GSSEdge {

	private BodyGrammarSlot returnSlot;
	private SPPFNode node;
	private GSSNode destination;

	private final int hash;

	public GSSEdge(BodyGrammarSlot slot, SPPFNode node, GSSNode destination) {
		this.returnSlot = slot;
		this.node = node;
		this.destination = destination;
		this.hash = HashFunctions.defaulFunction().hash(returnSlot.getId(), 
														node.getId(),
														destination.getInputIndex(), 
//														node.getRightExtent(),  
														destination.getGrammarSlot().getId());
	}

	public SPPFNode getNode() {
		return node;
	}

	public BodyGrammarSlot getReturnSlot() {
		return returnSlot;
	}

	public GSSNode getDestination() {
		return destination;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}

		if (!(obj instanceof GSSEdge)) {
			return false;
		}

		GSSEdge other = (GSSEdge) obj;

		// Because destination.getInputIndex() == node.getLeftExtent, and
		//         node.getRightExtent() == source.getLeftExtent 
		// we don't use them here.
		return 	returnSlot == other.returnSlot
				&& node.getId() == other.node.getId()
				&& destination.getInputIndex() == other.destination.getInputIndex()
//				&& node.getRightExtent() == other.node.getRightExtent()
				&& destination.getGrammarSlot() == other.destination.getGrammarSlot();
	}

	@Override
	public int hashCode() {
		return hash;
	}

}
