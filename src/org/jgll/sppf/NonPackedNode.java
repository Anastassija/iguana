package org.jgll.sppf;


/**
 * 
 * A NonPackedNode is the abstract super class for nonterminal 
 * and intermediate symbol nodes.
 * 
 * 
 * @author Ali Afroozeh
 * 
 */

public abstract class NonPackedNode implements SPPFNode {
	
	protected final Object slot;
	
	protected final int leftExtent;
	
	protected final int rightExtent;
	
	public NonPackedNode(Object slot, int leftExtent, int rightExtent) {
		this.slot = slot;
		this.leftExtent = leftExtent;
		this.rightExtent = rightExtent;
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
	public boolean equals(Object obj) {		
		if(this == obj) 
			return true;
		
		if (!(obj instanceof SPPFNode)) 
			return false;
		
		return SPPFUtil.getInstance().equals(this, (SPPFNode) obj);
	}

	@Override
	public int hashCode() {
		return SPPFUtil.getInstance().hash(this);
	}
	
	@Override
	public String toString() {
		return String.format("(%s, %d, %d)", slot, getLeftExtent(), getRightExtent());
	}
	
	@FunctionalInterface
	public static interface Equals {
		public boolean equals(SPPFNode node, SPPFNode other);
	}
	
	@FunctionalInterface
	public static interface Hash {
	  public int hash(SPPFNode node);
	}
}
