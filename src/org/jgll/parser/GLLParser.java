package org.jgll.parser;

import org.jgll.grammar.GrammarGraph;
import org.jgll.grammar.GrammarSlotRegistry;
import org.jgll.grammar.slot.EndGrammarSlot;
import org.jgll.grammar.slot.GrammarSlot;
import org.jgll.grammar.slot.NonterminalGrammarSlot;
import org.jgll.grammar.slot.TerminalGrammarSlot;
import org.jgll.parser.descriptor.Descriptor;
import org.jgll.parser.gss.GSSNode;
import org.jgll.sppf.IntermediateNode;
import org.jgll.sppf.NonPackedNode;
import org.jgll.sppf.NonterminalNode;
import org.jgll.sppf.TerminalNode;
import org.jgll.util.Input;

/**
 * 
 * 
 * @author Ali Afroozeh
 *
 */
public interface GLLParser {
	
	public ParseResult parse(Input input, GrammarGraph grammar, String startSymbolName);
	
	default void pop() {
		pop(getCurrentGSSNode(), getCurrentInputIndex(), getCurrentSPPFNode());
	}
	
	public void pop(GSSNode gssNode, int inputIndex, NonPackedNode node);
	
	public NonterminalGrammarSlot create(GrammarSlot slot, NonterminalGrammarSlot head);
	
	public TerminalNode getTerminalNode(TerminalGrammarSlot slot, int leftExtent, int rightExtent);

	public TerminalNode getEpsilonNode(int inputIndex);
	
	public NonterminalNode getNonterminalNode(EndGrammarSlot slot, NonPackedNode leftChild, NonPackedNode rightChild);
	
	public NonterminalNode getNonterminalNode(EndGrammarSlot slot, NonPackedNode child);
	
	public IntermediateNode getIntermediateNode(GrammarSlot slot, NonPackedNode leftChild, NonPackedNode rightChild);
	
	public boolean hasDescriptor(Descriptor descriptor);
	
	public void scheduleDescriptor(Descriptor descriptor);
	
	default boolean addDescriptor(Descriptor descriptor) {
		if (!hasDescriptor(descriptor)) {
			scheduleDescriptor(descriptor);
			return true;
		}
		return false;
	}
	
	public boolean hasNextDescriptor();
	
	/**
	 * Reads the next descriptor and sets the state of the parser to it.
	 */
	public Descriptor nextDescriptor();
	
	public int getCurrentInputIndex();
	
	public GSSNode getCurrentGSSNode();
	
	public NonPackedNode getCurrentSPPFNode();
	
	public void recordParseError(GrammarSlot slot);
	
	public Input getInput();
	
	public GrammarSlotRegistry getRegistry();
	
}
