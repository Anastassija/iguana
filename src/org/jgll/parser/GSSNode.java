package org.jgll.parser;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jgll.grammar.slot.GrammarSlot;
import org.jgll.grammar.slot.L0;
import org.jgll.sppf.NonPackedNode;
import org.jgll.sppf.SPPFNode;
import org.jgll.util.hashing.ExternalHasher;
import org.jgll.util.hashing.HashTableFactory;
import org.jgll.util.hashing.MultiHashMap;
import org.jgll.util.hashing.MultiHashSet;
import org.jgll.util.hashing.OpenAddressingHashMap;
import org.jgll.util.hashing.hashfunction.HashFunction;

/**
 * A {@code GSSNode} is a representation of a node in an Graph Structured Stack.
 * A {@code GSSNode} is defined by a three tuple ({@code label},
 * {@code position}, {@code index}). <br/> 
 * 
 * {@code label} is a label of a GLL Parser
 * state, {@code position} is an integer array of length three indication a
 * position in a grammar and {@code index} is an index in the input string. <br />
 * A {@code GSSNode} has children which are labeled by an {@code SPPFNode}.
 * These children define the structure of a Graph Structured Stack.
 * 
 * @author Maarten Manders
 * @author Ali Afroozeh
 * 
 */
public class GSSNode {
	
	public static final ExternalHasher<GSSNode> externalHasher = new GSSNodeExternalHasher();
	public static final ExternalHasher<GSSNode> levelBasedExternalHasher = new LevelBasedGSSNodeExternalHasher();

	/**
	 * The initial GSS node
	 */
	public static final GSSNode U0 = new GSSNode(L0.getInstance(), 0);

	private final GrammarSlot slot;

	private final int inputIndex;

	private MultiHashSet<NonPackedNode> poppedElements;
	
	private MultiHashMap<GSSNode, Set<SPPFNode>> edges;
	
	/**
	 * Creates a new {@code GSSNode} with the given {@code label},
	 * {@code position} and {@code index}
	 * 
	 * @param slot
	 * @param inputIndex
	 */
	public GSSNode(GrammarSlot slot, int inputIndex) {
		this.slot = slot;
		this.inputIndex = inputIndex;
		
		this.poppedElements = HashTableFactory.getFactory().newHashSet(NonPackedNode.externalHasher);
		this.edges = new OpenAddressingHashMap<>(externalHasher);			
	}
		
	public boolean createEdge(GSSNode dest, SPPFNode node) {

		Set<SPPFNode> set = edges.get(dest);
		
		if(set == null) {
			set = new HashSet<>();
			set.add(node);
			edges.put(dest, set);
			return true;
		}
		
		return set.add(node);
	}
	
	public Iterable<GSSNode> getChildren() {
		return new Iterable<GSSNode>() {
			
			@Override
			public Iterator<GSSNode> iterator() {
				return edges.keyIterator();
			}
		};
	}
	
	public int sizeChildren() {
		return edges.size();
	}
	
	public Iterable<SPPFNode> getNodesForChild(final GSSNode gssNode) {
		Set<SPPFNode> set = edges.get(gssNode);
		if(set == null) {
			return Collections.emptySet();
		}
		return set;
	}
	
	public GrammarSlot getGrammarSlot() {
		return slot;
	}

	public int getInputIndex() {
		return inputIndex;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if(this == obj) {
			return true;
		}

		if (!(obj instanceof GSSNode)) {
			return false;
		}
		
		GSSNode other = (GSSNode) obj;

		return  slot == other.slot &&
				inputIndex == other.inputIndex;
	}

	@Override
	public int hashCode() {
		return externalHasher.hash(this, HashFunctions.defaulFunction());
	}
	
	@Override
	public String toString() {
		return "(" + slot + "," + inputIndex + ")";
	}

	public void addToPoppedElements(NonPackedNode node) {
		poppedElements.add(node);
	}
	
	public Iterable<NonPackedNode> getPoppedElements() {
		return poppedElements;
	}
	
	public static class GSSNodeExternalHasher implements ExternalHasher<GSSNode> {
		
		private static final long serialVersionUID = 1L;

		@Override
		public int hash(GSSNode node, HashFunction f) {
			return f.hash(node.slot.getId(), node.inputIndex);
		}

		@Override
		public boolean equals(GSSNode g1, GSSNode g2) {
			return g1.slot.getId() == g2.slot.getId() &&
				   g1.inputIndex == g2.inputIndex;
		}
	}
	
	public static class LevelBasedGSSNodeExternalHasher implements ExternalHasher<GSSNode> {
		
		private static final long serialVersionUID = 1L;

		@Override
		public int hash(GSSNode node, HashFunction f) {
			return f.hash(node.slot.getId());
		}

		@Override
		public boolean equals(GSSNode g1, GSSNode g2) {
			return g1.slot == g2.slot;
		}
	}


}
