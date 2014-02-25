package org.jgll.parser.lookup;

import org.jgll.grammar.Grammar;
import org.jgll.grammar.slot.GrammarSlot;
import org.jgll.grammar.slot.HeadGrammarSlot;
import org.jgll.sppf.IntermediateNode;
import org.jgll.sppf.NonPackedNode;
import org.jgll.sppf.NonterminalSymbolNode;
import org.jgll.sppf.SPPFNode;
import org.jgll.sppf.TokenSymbolNode;
import org.jgll.util.Input;
import org.jgll.util.hashing.ExternalHasher;
import org.jgll.util.hashing.HashTableFactory;
import org.jgll.util.hashing.IguanaSet;
import org.jgll.util.hashing.hashfunction.HashFunction;
import org.jgll.util.logging.LoggerWrapper;

public class SPPFLookupImpl implements SPPFLookup {
	
	private static final LoggerWrapper log = LoggerWrapper.getLogger(SPPFLookupImpl.class);
	
	private HashTableFactory factory;

	private int tableSize = (int) Math.pow(2, 10);
	
	private final TokenSymbolNode[][] tokenSymbolNodes;
	
	private final IguanaSet<NonterminalSymbolNode>[] nonterminalNodes;

	private final IguanaSet<IntermediateNode>[] intermediateNodes;
	
	public SPPFLookupImpl(Grammar grammar, Input input) {
		long start = System.nanoTime();
		nonterminalNodes = new IguanaSet[input.length()];
		
		intermediateNodes = new IguanaSet[input.length()];

		tokenSymbolNodes = new TokenSymbolNode[grammar.getCountTokens()][input.length()];
		long end = System.nanoTime();
		log.info("SPPF lookup initialization: %d ms", (end - start) / 1000_000);
	}

	@Override
	public TokenSymbolNode getTokenSymbolNode(int tokenID, int inputIndex, int length) {
		TokenSymbolNode node = tokenSymbolNodes[tokenID][inputIndex];
		if (node == null) {
			node = new TokenSymbolNode(tokenID, inputIndex, length);
			tokenSymbolNodes[tokenID][inputIndex] = node;
		}
		return node;
	}
	
	@Override
	public TokenSymbolNode findTokenSymbolNode(int tokenID, int inputIndex, int length) {
		return tokenSymbolNodes[tokenID][inputIndex];
	}

	@Override
	public NonterminalSymbolNode getNonterminalNode(GrammarSlot grammarSlot, int leftExtent, int rightExtent) {
		NonterminalSymbolNode key = new NonterminalSymbolNode(grammarSlot, leftExtent, rightExtent);

		IguanaSet<NonterminalSymbolNode> set = nonterminalNodes[rightExtent];

		if (set == null) {
			set = factory.newHashSet(tableSize, new ExternalHasher<NonterminalSymbolNode>() {

				private static final long serialVersionUID = 1L;

				@Override
				public int hash(NonterminalSymbolNode n, HashFunction f) {
					return f.hash(n.getGrammarSlot().getId(), n.getLeftExtent());
				}

				@Override
				public boolean equals(NonterminalSymbolNode n1, NonterminalSymbolNode n2) {
					return n1.getGrammarSlot() == n2.getGrammarSlot() &&
						   n1.getLeftExtent() == n2.getLeftExtent();
				}
			});
			nonterminalNodes[rightExtent] = set;
			set.add(key);
			return key;
		}

		NonterminalSymbolNode oldValue = set.add(key);
		if (oldValue == null) {
			oldValue = key;
		}

		return oldValue;
	}

	@Override
	public NonterminalSymbolNode findNonterminalNode(GrammarSlot grammarSlot, int leftExtent, int rightExtent) {
		
		IguanaSet<NonterminalSymbolNode> set = nonterminalNodes[rightExtent];

		if (set == null) {
			return null;
		}

		NonterminalSymbolNode key = new NonterminalSymbolNode(grammarSlot, leftExtent, rightExtent);
		return set.get(key);
	}

	@Override
	public IntermediateNode getIntermediateNode(GrammarSlot grammarSlot, int leftExtent, int rightExtent) {
		IntermediateNode key = new IntermediateNode(grammarSlot, leftExtent, rightExtent);

		IguanaSet<IntermediateNode> set = intermediateNodes[rightExtent];

		if (set == null) {
			set = factory.newHashSet(tableSize, new ExternalHasher<IntermediateNode>() {

				private static final long serialVersionUID = 1L;

				@Override
				public int hash(IntermediateNode n, HashFunction f) {
					return f.hash(n.getGrammarSlot().getId(), n.getLeftExtent());
				}

				@Override
				public boolean equals(IntermediateNode n1, IntermediateNode n2) {
					return n1.getGrammarSlot() == n2.getGrammarSlot() &&
						   n1.getLeftExtent() == n2.getLeftExtent();
				}
			});
			intermediateNodes[rightExtent] = set;
			set.add(key);
			return key;
		}

		IntermediateNode oldValue = set.add(key);
		if (oldValue == null) {
			oldValue = key;
		}

		return oldValue;
	}

	@Override
	public IntermediateNode findIntermediateNode(GrammarSlot grammarSlot, int leftExtent, int rightExtent) {
		IguanaSet<IntermediateNode> set = intermediateNodes[rightExtent];

		if (set == null) {
			return null;
		}

		IntermediateNode key = new IntermediateNode(grammarSlot, leftExtent, rightExtent);
		return set.get(key);
	}
	
	@Override
	public void addPackedNode(NonPackedNode parent, GrammarSlot slot, int pivot, SPPFNode leftChild, SPPFNode rightChild) {
	}

	@Override
	public NonterminalSymbolNode getStartSymbol(HeadGrammarSlot startSymbol, int inputSize) {
		if (nonterminalNodes[inputSize - 1] == null) {
			return null;
		}
		return nonterminalNodes[inputSize - 1].get(new NonterminalSymbolNode(startSymbol, 0, inputSize - 1));
	}

	@Override
	public int getNonterminalNodesCount() {
		int count = 0;
		for(IguanaSet<NonterminalSymbolNode> set : nonterminalNodes) {
			count += set.size();
		}
		return count;
	}

	@Override
	public int getIntermediateNodesCount() {
		int count = 0;
		for(IguanaSet<IntermediateNode> set : intermediateNodes) {
			count += set.size();
		}
		return count;
	}

	@Override
	public int getTokenNodesCount() {
		int count = 0;
		for(int i = 0; i < tokenSymbolNodes.length; i++) {
			for(int j = 0; j < tokenSymbolNodes[i].length; j++) {
				count++;
			}
		}
		return count;
	}

	@Override
	public int getPackedNodesCount() {
		return 0;
	}

}
