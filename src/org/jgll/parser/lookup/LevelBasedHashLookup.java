package org.jgll.parser.lookup;

import java.util.ArrayDeque;
import java.util.Queue;

import org.jgll.grammar.Grammar;
import org.jgll.grammar.slot.GrammarSlot;
import org.jgll.grammar.slot.HeadGrammarSlot;
import org.jgll.parser.Descriptor;
import org.jgll.parser.GSSNode;
import org.jgll.sppf.NonPackedNode;
import org.jgll.sppf.NonterminalSymbolNode;
import org.jgll.sppf.PackedNode;
import org.jgll.sppf.SPPFNode;
import org.jgll.sppf.TerminalSymbolNode;
import org.jgll.util.Input;
import org.jgll.util.hashing.HashTableFactory;
import org.jgll.util.hashing.MultiHashSet;
import org.jgll.util.logging.LoggerWrapper;

/**
 * 
 * Provides lookup functionality for the level-based processing of the input
 * in a GLL parser. 
 * 
 * 
 * @author Ali Afroozeh
 *
 */
public class LevelBasedHashLookup extends AbstractLookupTable {
	
	private static final LoggerWrapper log = LoggerWrapper.getLogger(LevelBasedHashLookup.class);
	
	private HashTableFactory factory;
	
	private int currentLevel;
	
	private int countNonPackedNodes;

	private int chainLength;
	
	private TerminalSymbolNode[][] terminals;
	
	private MultiHashSet<Descriptor> u;
	private MultiHashSet<Descriptor>[] forwardDescriptors;

	private MultiHashSet<NonPackedNode> currentSPPFNodes;
	private MultiHashSet<NonPackedNode>[] forwardSPPFNodes;
	
	private Queue<Descriptor> r;
	private Queue<Descriptor>[] forwardRs;
	
	private MultiHashSet<GSSNode> currentGssNodes;
	private MultiHashSet<GSSNode>[] forwardGssNodes;
		
	private int countGSSNodes;
	
	/**
	 * The number of descriptors waiting to be processed.
	 */
	private int size;
	
	/**
	 * The total number of descriptors added
	 */
	private int all;
	
	private int countPackedNodes;
	
	protected int countGSSEdges;
	
	private final int initialSize = 32;
	
	public LevelBasedHashLookup(Grammar grammar) {
		this(grammar, grammar.getLongestTerminalChain());
	}
	
	@SuppressWarnings("unchecked")
	public LevelBasedHashLookup(Grammar grammar, int regularListLength) {
		super(grammar);
		
		factory = HashTableFactory.getFactory();
		
		chainLength = grammar.getLongestTerminalChain() + regularListLength;
		terminals = new TerminalSymbolNode[chainLength + 1][2];
		
		u = factory.newHashSet(getSize(), Descriptor.levelBasedExternalHasher);
		r = new ArrayDeque<>();
		
		forwardDescriptors = new MultiHashSet[chainLength];
		forwardRs = new Queue[chainLength];
		
		currentSPPFNodes = factory.newHashSet(initialSize, NonPackedNode.levelBasedExternalHasher);
		forwardSPPFNodes = new MultiHashSet[chainLength];
		
		currentGssNodes = factory.newHashSet(initialSize, GSSNode.levelBasedExternalHasher);
		forwardGssNodes = new MultiHashSet[chainLength];
		
		for(int i = 0; i < chainLength; i++) {
			forwardDescriptors[i] = factory.newHashSet(getSize(), Descriptor.levelBasedExternalHasher);
			forwardRs[i] = new ArrayDeque<>(initialSize);
			forwardSPPFNodes[i] = factory.newHashSet(initialSize, NonPackedNode.levelBasedExternalHasher);
			forwardGssNodes[i] = factory.newHashSet(initialSize, GSSNode.levelBasedExternalHasher);
		}
	}
	
	private void gotoNextLevel() {
		int nextIndex = indexFor(currentLevel + 1);
		
		u = forwardDescriptors[nextIndex];
		forwardDescriptors[nextIndex] = factory.newHashSet(getSize(), Descriptor.levelBasedExternalHasher);
		
		Queue<Descriptor> tmpR = r;
		assert r.isEmpty();
		r = forwardRs[nextIndex];
		forwardRs[nextIndex] = tmpR;
		
		currentSPPFNodes = forwardSPPFNodes[nextIndex];
		forwardSPPFNodes[nextIndex] = factory.newHashSet(initialSize, NonPackedNode.levelBasedExternalHasher);
		
		currentGssNodes = forwardGssNodes[nextIndex];
		forwardGssNodes[nextIndex] = factory.newHashSet(initialSize, GSSNode.levelBasedExternalHasher);
		
		terminals[indexFor(currentLevel)][0] = null;
		terminals[indexFor(currentLevel)][1] = null;
		
		currentLevel++;
	}
	
	private int indexFor(int inputIndex) {
		return inputIndex % chainLength;
	}
	
	@Override
	public NonPackedNode hasNonPackedNode(GrammarSlot grammarSlot, int leftExtent, int rightExtent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public NonPackedNode hasNonPackedNode(NonPackedNode key) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public NonPackedNode getNonPackedNode(GrammarSlot slot, int leftExtent, int rightExtent) {
		NonPackedNode key = createNonPackedNode(slot, leftExtent, rightExtent);
		return getNonPackedNode(key);
	}
	
	@Override
	public NonPackedNode getNonPackedNode(NonPackedNode key) {
		boolean newNodeCreated = false;
		NonPackedNode value;

		int rightExtent = key.getRightExtent();
		
		if(rightExtent  == currentLevel) {
			value = currentSPPFNodes.add(key);
			if(value == null) {
				countNonPackedNodes++;
				newNodeCreated = true;
				value = key;
			}
		} else {
			int index = indexFor(rightExtent);
			value = forwardSPPFNodes[index].add(key);
			if(value == null){
				countNonPackedNodes++;
				newNodeCreated = true;
				value = key;
			}
		}
		
		log.trace("SPPF node created: %s : %b", value, newNodeCreated);
		return value;
	}
	
	@Override
	public TerminalSymbolNode getTerminalNode(int terminalIndex, int leftExtent) {
		
		boolean newNodeCreated = false;
		
		int index2;
		int rightExtent;
		if(terminalIndex == -2) {
			rightExtent = leftExtent;
			index2 = 1;
		} else {
			rightExtent = leftExtent + 1;
			index2 = 0;
		}
		
		int index = indexFor(rightExtent);

		TerminalSymbolNode terminal = terminals[index][index2];
		if(terminal == null) {
			terminal = new TerminalSymbolNode(terminalIndex, leftExtent);
			countNonPackedNodes++;
			terminals[index][index2] = terminal;
			newNodeCreated = true;
		}
		
		log.trace("SPPF Terminal node created: %s : %b", terminal, newNodeCreated);
		return terminal;
	}

	
	@Override
	public NonterminalSymbolNode getStartSymbol(HeadGrammarSlot startSymbol, int inputSize) {
		
		MultiHashSet<NonPackedNode> currentNodes;
		
		if(currentLevel != inputSize - 1) {
			int index = indexFor(inputSize - 1); 
			currentNodes = forwardSPPFNodes[index];
		} else {
			currentNodes = this.currentSPPFNodes;
		}
		
		return (NonterminalSymbolNode) currentNodes.get(new NonterminalSymbolNode(startSymbol, 0, inputSize - 1));
	}

	@Override
	public int getNonPackedNodesCount() {
		return countNonPackedNodes;
	}

	@Override
	public boolean hasNextDescriptor() {
		return size > 0;
	}

	@Override
	public Descriptor nextDescriptor() {
		if(!r.isEmpty()) {
			size--;
			return r.remove();
		}
		else {
			gotoNextLevel();
			return nextDescriptor();
		}
	}
	
	private int getSize() {
		return grammar.getMaxDescriptorsAtInput();
	}

	@Override
	public boolean addDescriptor(Descriptor descriptor) {
		int inputIndex = descriptor.getInputIndex();
		if(inputIndex == currentLevel) {
			if(u.add(descriptor) == null) {
				 r.add(descriptor);
				 size++;
				 all++;
			} else {
				return false;
			}
		}
		
		else {
			int index = indexFor(descriptor.getInputIndex());
			if(forwardDescriptors[index].add(descriptor) == null) {
				forwardRs[index].add(descriptor);
				size++;
				all++;
			}  else {
				return false;
			}
		}
		
		return true;
	}

	@Override
	public int getDescriptorsCount() {
		return all;
	}
	
	@Override
	public GSSNode getGSSNode(GrammarSlot slot, int inputIndex) {
		GSSNode key = new GSSNode(slot, inputIndex);
		GSSNode value;
		if(inputIndex == currentLevel) {
			value = currentGssNodes.add(key);
			if(value == null) {
				countGSSNodes++;
				value = key;
			}
		} else {
			int index = indexFor(inputIndex);
			value = forwardGssNodes[index].add(key);
			if(value == null) {
				countGSSNodes++;
				value = key;
			}
		}
		return value;
	}
	
	@Override
	public boolean getGSSEdge(GSSNode source, SPPFNode node, GSSNode destination) {
		boolean added = source.createEdge(destination, node);
		if(added) {
			countGSSEdges++;
		}
		return added;
	}

	@Override
	public int getGSSNodesCount() {
		return countGSSNodes;
	}

	@Override
	public Iterable<GSSNode> getGSSNodes() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addPackedNode(NonPackedNode parent, GrammarSlot slot, int pivot, SPPFNode leftChild, SPPFNode rightChild) {
		PackedNode packedNode = new PackedNode(slot, pivot, parent);
		parent.addPackedNode(packedNode, leftChild, rightChild);
	}

	@Override
	public int getPackedNodesCount() {
		return countPackedNodes;
	}

	@Override
	public int getGSSEdgesCount() {
		return countGSSEdges;
	}

	@Override
	public void addToPoppedElements(GSSNode gssNode, NonPackedNode sppfNode) {
		gssNode.addToPoppedElements((NonPackedNode) sppfNode);
	}

	@Override
	public Iterable<NonPackedNode> getSPPFNodesOfPoppedElements(GSSNode gssNode) {
		return gssNode.getPoppedElements();
	}

	@Override
	public void init(Input input) {
		terminals = new TerminalSymbolNode[chainLength + 1][2];
		
		u.clear();
		r.clear();
		
		currentSPPFNodes.clear();
		
		currentGssNodes.clear();
		
		for(int i = 0; i < chainLength; i++) {
			forwardDescriptors[i].clear();
			forwardRs[i].clear();
			forwardSPPFNodes[i].clear();
			forwardGssNodes[i].clear();
		}
		
		currentLevel = 0;
		
		countNonPackedNodes = 0;

		countGSSNodes = 0;
		
		size = 0;
		
		all = 0;
		
		countPackedNodes = 0;
		
		countGSSEdges = 0;
	}

	@Override
	public Iterable<GSSNode> getChildren(GSSNode node) {
		return node.getChildren();
	}

	@Override
	public Iterable<SPPFNode> getSPPFNodeOnEdgeFrom(GSSNode source, GSSNode dest) {
		return source.getNodesForChild(dest);
	}

}
