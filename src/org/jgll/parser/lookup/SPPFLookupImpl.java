package org.jgll.parser.lookup;

import java.util.HashMap;
import java.util.Map;

import org.jgll.grammar.slot.BodyGrammarSlot;
import org.jgll.grammar.slot.HeadGrammarSlot;
import org.jgll.grammar.slot.TerminalGrammarSlot;
import org.jgll.grammar.symbol.Epsilon;
import org.jgll.sppf.IntermediateNode;
import org.jgll.sppf.ListSymbolNode;
import org.jgll.sppf.NonPackedNode;
import org.jgll.sppf.NonterminalNode;
import org.jgll.sppf.PackedNode;
import org.jgll.sppf.SPPFNode;
import org.jgll.sppf.TerminalSymbolNode;
import org.jgll.util.logging.LoggerWrapper;

public class SPPFLookupImpl implements SPPFLookup {
	
	private static final LoggerWrapper log = LoggerWrapper.getLogger(SPPFLookupImpl.class);
	
	private final Map<TerminalSymbolNode, TerminalSymbolNode> tokenNodes;
	
	private final Map<NonterminalNode, NonterminalNode> nonterminalNodes;

	private final Map<IntermediateNode, IntermediateNode> intermediateNodes;
	
	private int countPackedNodes;
	
	private int countAmbiguousNodes;

	public SPPFLookupImpl() {
		nonterminalNodes = new HashMap<>();
		intermediateNodes = new HashMap<>();
		tokenNodes = new HashMap<>();
	}

	@Override
	public TerminalSymbolNode getTokenSymbolNode(TerminalGrammarSlot slot, int inputIndex, int length) {
		TerminalSymbolNode key = new TerminalSymbolNode(slot, inputIndex, length);
		TerminalSymbolNode value = tokenNodes.get(key);
		if (value == null) {
			value = key;
//			System.out.println(grammar.getRegularExpressionById(value.getId()) + ", " + value.getLeftExtent() + ", " + value.getRightExtent());
			tokenNodes.put(key, value);
		}
		return value;
	}
	
	@Override
	public TerminalSymbolNode getEpsilonNode(int inputIndex) {
		return getTokenSymbolNode(Epsilon.TOKEN_ID, inputIndex, 0);
	}
	
	@Override
	public TerminalSymbolNode findTokenSymbolNode(TerminalGrammarSlot slot, int inputIndex, int length) {
		TerminalSymbolNode key = new TerminalSymbolNode(slot, inputIndex, length);
		return tokenNodes.get(key);
	}

	@Override
	public NonterminalNode getNonterminalNode(HeadGrammarSlot head, int leftExtent, int rightExtent) {
		NonterminalNode key = createNonterminalNode(head, leftExtent, rightExtent);
		NonterminalNode value = nonterminalNodes.get(key);
		if (value == null) {
			value = key;
//			System.out.println(grammar.getNonterminalById(value.getId()) + ", " + value.getLeftExtent() + ", " + value.getRightExtent());
			value.init();
			log.trace("Nonterminal node created: %s", key);
			nonterminalNodes.put(key, value);
		}
		return value;
	}
	
	protected NonterminalNode createNonterminalNode(HeadGrammarSlot head, int leftExtent, int rightExtent) {
		if(head.getNonterminal().isEbnfList()) {
			return new ListSymbolNode(head, leftExtent, rightExtent);
		} else {
			return new NonterminalNode(head, leftExtent, rightExtent);
		}
	}
	
	protected IntermediateNode createIntermediateNode(BodyGrammarSlot grammarSlot, int leftExtent, int rightExtent) {
		return new IntermediateNode(grammarSlot, leftExtent, rightExtent);
	}

	@Override
	public NonterminalNode findNonterminalNode(HeadGrammarSlot head, int leftExtent, int rightExtent) {		
		NonterminalNode key = createNonterminalNode(head, leftExtent, rightExtent);
		return nonterminalNodes.get(key);
	}

	@Override
	public IntermediateNode getIntermediateNode(BodyGrammarSlot grammarSlot, int leftExtent, int rightExtent) {
		IntermediateNode key = createIntermediateNode(grammarSlot, leftExtent, rightExtent);
		IntermediateNode value = intermediateNodes.get(key);
		
		if (value == null) {
			value = key;
			value.init();
			log.trace("Intermediate node created: %s", key);
			intermediateNodes.put(key, value);
		}
		return value;
	}

	@Override
	public IntermediateNode findIntermediateNode(BodyGrammarSlot grammarSlot, int leftExtent, int rightExtent) {
		IntermediateNode key = createIntermediateNode(grammarSlot, leftExtent, rightExtent);
		return intermediateNodes.get(key);
	}
	
	@Override
	public void addPackedNode(NonPackedNode parent, BodyGrammarSlot slot, int pivot, SPPFNode leftChild, SPPFNode rightChild) {
		PackedNode packedNode = new PackedNode(slot, pivot, parent);
		boolean ambiguousBefore = parent.isAmbiguous();
		if (parent.addPackedNode(packedNode, leftChild, rightChild)) {
			countPackedNodes++;
			boolean ambiguousAfter = parent.isAmbiguous();
			if (!ambiguousBefore && ambiguousAfter) {
//				log.warning("Ambiguity at line: %d, column: %d \n %s\n %s \n %s",
//						input.getLineNumber(parent.getLeftExtent()),
//						input.getColumnNumber(parent.getLeftExtent()),
//						grammar.getGrammarSlot(slot.getId()), 
//						grammar.getGrammarSlot(parent.getFirstPackedNodeGrammarSlot()),
//						input.subString(parent.getLeftExtent(), parent.getRightExtent()));
				countAmbiguousNodes++;
//				Visualization.generateSPPFGraph("/Users/aliafroozeh/output", parent, grammar, input);
//				System.exit(0);
			}
		}
	}
	
	@Override
	public NonterminalNode getStartSymbol(HeadGrammarSlot startSymbol, int inputSize) {
		return nonterminalNodes.get(createNonterminalNode(startSymbol, 0, inputSize - 1));
	}

	@Override
	public int getNonterminalNodesCount() {
		return nonterminalNodes.size();
	}

	@Override
	public int getIntermediateNodesCount() {
		return intermediateNodes.size();
	}

	@Override
	public int getTokenNodesCount() {
		return tokenNodes.size();
	}

	@Override
	public int getPackedNodesCount() {
		return countPackedNodes;
	}
	
	@Override
	public int getAmbiguousNodesCount() {
		return countAmbiguousNodes;
	}
}
