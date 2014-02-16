package org.jgll.parser.gss;

import org.jgll.grammar.slot.GrammarSlot;
import org.jgll.grammar.slot.L0;
import org.jgll.sppf.NonPackedNode;
import org.jgll.util.hashing.ExternalHasher;
import org.jgll.util.hashing.hashfunction.HashFunction;

/**
 *
 * @author Ali Afroozeh
 * 
 */
public interface GSSNode {
	
	public static final ExternalHasher<GSSNode> externalHasher = new GSSNodeExternalHasher();
	
	public static final GSSNode U0 = new DummyGSSNode();
	
	public void addToPoppedElements(NonPackedNode node);
	
	public Iterable<NonPackedNode> getPoppedElements();
		
	public Iterable<GSSNode> getChildren();
	
	public void addChild(GSSNode node);
	
	public int sizeChildren();
		
	public GrammarSlot getGrammarSlot();

	public int getInputIndex();

	public static class GSSNodeExternalHasher implements ExternalHasher<GSSNode> {
		
		private static final long serialVersionUID = 1L;

		@Override
		public int hash(GSSNode node, HashFunction f) {
			return f.hash(node.getGrammarSlot().getId(), node.getInputIndex());
		}

		@Override
		public boolean equals(GSSNode g1, GSSNode g2) {
			return g1.getGrammarSlot() == g2.getGrammarSlot() &&
				   g1.getInputIndex() == g2.getInputIndex();
		}
	}
	
	static class DummyGSSNode implements GSSNode {
		
		@Override
		public void addToPoppedElements(NonPackedNode node) {}

		@Override
		public Iterable<NonPackedNode> getPoppedElements() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Iterable<GSSNode> getChildren() {
			throw new UnsupportedOperationException();
		}

		@Override
		public int sizeChildren() {
			return 0;
		}

		@Override
		public GrammarSlot getGrammarSlot() {
			return L0.getInstance();
		}

		@Override
		public int getInputIndex() {
			return 0;
		}

		@Override
		public void addChild(GSSNode node) {}
	}

}