package org.jgll.sppf.lookup;

import org.jgll.grammar.slot.BodyGrammarSlot;
import org.jgll.grammar.slot.EndGrammarSlot;
import org.jgll.grammar.slot.GrammarSlot;
import org.jgll.grammar.slot.NonterminalGrammarSlot;
import org.jgll.grammar.slot.TerminalGrammarSlot;
import org.jgll.grammar.symbol.Epsilon;
import org.jgll.sppf.DummyNode;
import org.jgll.sppf.IntermediateNode;
import org.jgll.sppf.ListSymbolNode;
import org.jgll.sppf.NonPackedNode;
import org.jgll.sppf.NonterminalNode;
import org.jgll.sppf.NonterminalOrIntermediateNode;
import org.jgll.sppf.PackedNode;
import org.jgll.sppf.TerminalNode;
import org.jgll.util.hashing.ExternalHashEquals;

public interface SPPFLookup {

	/**
	 * @param terminal
	 * @param leftExtent
	 * @param rightExtent
	 * @return
	 */
	public TerminalNode getTerminalNode(TerminalGrammarSlot slot, int leftExtent, int rightExtent);
	
	public TerminalNode findTerminalNode(TerminalGrammarSlot slot, int leftExtent, int rightExtent);
	
	default TerminalNode getEpsilonNode(int inputIndex) {
		return getTerminalNode(Epsilon.TOKEN_ID, inputIndex, inputIndex);
	}
	
	default NonPackedNode getNode(GrammarSlot slot, NonPackedNode leftChild, NonPackedNode rightChild) {

		// A ::= \alpha .
		if (slot.isLast()) {
			if (leftChild == DummyNode.getInstance()) {
				return getNonterminalNode((EndGrammarSlot) slot, rightChild);
			} else {
				return getNonterminalNode((EndGrammarSlot) slot, leftChild, rightChild);				
			}
		}
		
		// A ::= X . \alpha, in this case leftChild is the dummy node. 
		if (slot.isFirst()) {
			return rightChild;
		}
		
		return getIntermediateNode((BodyGrammarSlot) slot, leftChild, rightChild);
	}
	
	default NonterminalNode getNonterminalNode(EndGrammarSlot slot, NonPackedNode leftChild, NonPackedNode rightChild) {
		NonterminalNode newNode = getNonterminalNode(slot.getNonterminal(), leftChild.getLeftExtent(), rightChild.getRightExtent());
		addPackedNode(newNode, slot, leftChild.getRightExtent(), leftChild, rightChild);
		return newNode;
	}
	
	default NonterminalNode getNonterminalNode(EndGrammarSlot slot, NonPackedNode child) {
		NonterminalNode newNode = getNonterminalNode(slot.getNonterminal(), child.getLeftExtent(), child.getRightExtent());
		addPackedNode(newNode, slot, child.getRightExtent(), child);
		return newNode;
	}
	
	default IntermediateNode getIntermediateNode(BodyGrammarSlot slot, NonPackedNode leftChild, NonPackedNode rightChild) {
		IntermediateNode newNode = getIntermediateNode(slot, leftChild.getLeftExtent(), rightChild.getRightExtent());
		addPackedNode(newNode, slot, rightChild.getLeftExtent(), leftChild, rightChild);
		return newNode;
	}
	
	/**
	 * 
	 * Returns an existing SPPF node with the given parameters. If such a node
	 * does not exists, creates one.
	 * 
	 * @param grammarSlot
	 * @param leftExtent
	 * @param rightExtent
	 * @return
	 */
	public NonterminalNode getNonterminalNode(NonterminalGrammarSlot grammarSlot, int leftExtent, int rightExtent);
	
	default NonterminalNode createNonterminalNode(NonterminalGrammarSlot head, int leftExtent, int rightExtent, ExternalHashEquals<NonPackedNode> hashEquals) {
		if(head.getNonterminal().isEbnfList()) {
			return new ListSymbolNode(head, leftExtent, rightExtent, hashEquals);
		} else {
			return new NonterminalNode(head, leftExtent, rightExtent, hashEquals);
		}
	}
	
	/**
	 * 
	 * Returns the existing SPPF node with the given parameters if it exists, otherwise
	 * return null.
	 * 
	 * @param grammarSlot
	 * @param leftExtent
	 * @param rightExtent
	 * 
	 * @return null if no nonterminal node is found with the given parameters
	 * 
	 */
	public NonterminalNode findNonterminalNode(NonterminalGrammarSlot slot, int leftExtent, int rightExtent);
	
	public IntermediateNode getIntermediateNode(BodyGrammarSlot slot, int leftExtent, int rightExtent);
	
	public IntermediateNode findIntermediateNode(BodyGrammarSlot slot, int leftExtent, int rightExtent);
	
	default void addPackedNode(NonterminalOrIntermediateNode parent, GrammarSlot slot, int pivot, NonPackedNode leftChild, NonPackedNode rightChild) {
		PackedNode packedNode = new PackedNode(slot, pivot, parent);
		boolean ambiguousBefore = parent.isAmbiguous();
		if (parent.addPackedNode(packedNode, leftChild, rightChild)) {
			packedNodeAdded(packedNode);
			boolean ambiguousAfter = parent.isAmbiguous();
			if (!ambiguousBefore && ambiguousAfter) {
				ambiguousNodeAdded(parent);
			}
		}
	}
	
	default void addPackedNode(NonterminalOrIntermediateNode parent, GrammarSlot slot, int pivot, NonPackedNode child) {
		PackedNode packedNode = new PackedNode(slot, pivot, parent);
		boolean ambiguousBefore = parent.isAmbiguous();
		if (parent.addPackedNode(packedNode, child)) {
			packedNodeAdded(packedNode);
			boolean ambiguousAfter = parent.isAmbiguous();
			if (!ambiguousBefore && ambiguousAfter) {
				ambiguousNodeAdded(parent);
			}
		}
	}

	
	void ambiguousNodeAdded(NonterminalOrIntermediateNode node);
	
	void packedNodeAdded(PackedNode packedNode);
	
	void intermediateNodeAdded(IntermediateNode node);
	
	void nonterminalNodeAdded(NonterminalNode node);
	
	void terminalNodeAdded(TerminalNode node);
	
	public NonterminalNode getStartSymbol(NonterminalGrammarSlot startSymbol, int inputSize);
	
	public int getNonterminalNodesCount();
	
	public int getIntermediateNodesCount();
	
	public int getTokenNodesCount();
	
	public int getPackedNodesCount();
	
	public int getAmbiguousNodesCount();
	
	default void reset() {}
	
}
