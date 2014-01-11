package org.jgll.sppf;

import java.util.ArrayList;
import java.util.List;

import org.jgll.grammar.slot.GrammarSlot;
import org.jgll.grammar.slot.LastGrammarSlot;
import org.jgll.parser.HashFunctions;
import org.jgll.util.hashing.ExternalHasher;
import org.jgll.util.hashing.hashfunction.HashFunction;

/**
 * A NonPackedNode corresponds to nonterminal symbol nodes or
 * intermediate symbols nodes in the GLL paper. These nodes 
 * have the common property of being related to a grammar slot
 * in the body of production rules.
 * 
 * 
 * @author Ali Afroozeh
 * 
 * TODO: rename this class! The current name does not make much sense.
 *
 */

public abstract class NonPackedNode extends SPPFNode {
	
	public static final ExternalHasher<NonPackedNode> externalHasher = new NonPackedNodeExternalHasher();
	public static final ExternalHasher<NonPackedNode> levelBasedExternalHasher = new LevelBasedNonPackedNodeExternalHasher();
	
	protected final GrammarSlot slot;
	
	protected final int leftExtent;
	
	protected final int rightExtent;
	
	protected List<SPPFNode> children;
	
	private int countPackedNode;
	
	private final int hash;
	
	private PackedNode firstPackedNode;

	public NonPackedNode(GrammarSlot slot, int leftExtent, int rightExtent) {
		
		assert slot != null;
		
		this.slot = slot;
		this.leftExtent = leftExtent;
		this.rightExtent = rightExtent;
		this.children = new ArrayList<>();
		
		this.hash = externalHasher.hash(this, HashFunctions.defaulFunction()); 
	}
	
	@Override
	public int hashCode() {
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if(this == obj) {
			return true;
		}

		// Only nodes with the same concrete class can be equal.
		if(this.getClass() != obj.getClass()) {
			return false;
		}
		
		NonPackedNode other = (NonPackedNode) obj;

		return  slot == other.slot &&
				leftExtent == other.leftExtent &&
				rightExtent == other.rightExtent;
	}
	
	@Override
	public GrammarSlot getGrammarSlot() {
		return slot;
	}
	
	@Override
	public int getLeftExtent() {
		return leftExtent;
	}

	@Override
	public int getRightExtent() {
		return rightExtent;
	}
	
	@Override
	public String toString() {
		return String.format("(%s, %d, %d)", getLabel(), leftExtent, rightExtent);
	}
	
	@Override
	public String getLabel() {
		return slot.toString();
	}
	
	public void addFirstPackedNode(PackedNode packedNode) {
		firstPackedNode = packedNode;
		countPackedNode++;
	}

	public void addPackedNode(PackedNode packedNode, SPPFNode leftChild, SPPFNode rightChild) {
		
		createPackedNode(packedNode, leftChild, rightChild);
		
		if(countPackedNode == 0) {
			firstPackedNode = packedNode;
			if(leftChild != DummyNode.getInstance()) {
				children.add(leftChild);
			}
			children.add(rightChild);
		}
		else if(countPackedNode == 1) {
			children.clear();
			children.add(firstPackedNode);
			children.add(packedNode);
		}
		else {
			children.add(packedNode);
		}
		
		countPackedNode++;
	}
	
	/**
	 * Attaches the given left and right children to the given packed node.
	 *  
	 */
	private void createPackedNode(PackedNode packedNode, SPPFNode leftChild, SPPFNode rightChild) {
		
		if (leftChild != DummyNode.getInstance()) {
			packedNode.addChild(leftChild);
		}
		
		packedNode.addChild(rightChild);
	}
	
	@Override
	public boolean isAmbiguous() {
		return countPackedNode > 1;
	}
	
	public void addChild(SPPFNode node) {
		//TODO: change it! PackedNodes cannot be added via this method at parse time.
		if(node instanceof PackedNode) {
			countPackedNode++;
		}
		children.add(node);
	}
	
	public void removeChild(SPPFNode node) {
		children.remove(node);
	}
	
	/**
	 * Replaces the given node with its children.
	 * If this does node have the given node as child,
	 * nothing will happen.  
	 */
	public void replaceWithChildren(SPPFNode node) {
		int index = children.indexOf(node);
		children.remove(node);
		if(index >= 0) {
			for(SPPFNode child : node.getChildren()) {
				children.add(index++, child);				
			}
		}
	}
	
	@Override
	public SPPFNode getChildAt(int index) {
		if(children.size() > index) {
			return children.get(index);
		}
		return null;
	}
	
	@Override
	public SPPFNode getFirstChild() {
		return children.get(0);
	}
	
	@Override
	public SPPFNode getLastChild() {
		if(children.size() == 0) {
			return null;
		}
		return children.get(children.size() - 1);
	}
	
	@Override
	public Iterable<SPPFNode> getChildren() {
		return children;
	}
	
	@Override
	public int childrenCount() {
		return children.size();
	}
	
	public LastGrammarSlot getFirstPackedNodeGrammarSlot() {
		if(firstPackedNode == null) {
			throw new RuntimeException("The set PackedNodes cannot be empty.");
		}
		return (LastGrammarSlot) firstPackedNode.getGrammarSlot();
	}
	
	public int getCountPackedNode() {
		return countPackedNode;
	}
	
	public static class NonPackedNodeExternalHasher implements ExternalHasher<NonPackedNode> {

		private static final long serialVersionUID = 1L;

		@Override
		public int hash(NonPackedNode nonPackedNode, HashFunction f) {
			if(nonPackedNode == null) {
				System.out.println("WTF");
			}
			
			if(nonPackedNode.slot == null) {
				System.out.println("WTF");
			}
			return f.hash(nonPackedNode.slot.getId(), nonPackedNode.leftExtent, nonPackedNode.rightExtent);
		}

		@Override
		public boolean equals(NonPackedNode node1, NonPackedNode node2) {
			return  node1.rightExtent == node2.rightExtent &&
					node1.slot == node2.slot &&
					node1.leftExtent == node2.leftExtent;
		}
	}
	
	public static class LevelBasedNonPackedNodeExternalHasher implements ExternalHasher<NonPackedNode> {

		private static final long serialVersionUID = 1L;

		@Override
		public int hash(NonPackedNode nonPackedNode, HashFunction f) {
			return f.hash(nonPackedNode.slot.getId(), nonPackedNode.leftExtent);
		}
		
		@Override
		public boolean equals(NonPackedNode node1, NonPackedNode node2) {
			return  node1.slot == node2.slot &&
					node1.leftExtent == node2.leftExtent;
		}

	}

	
}
