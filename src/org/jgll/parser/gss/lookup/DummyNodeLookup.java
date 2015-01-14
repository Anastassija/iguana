package org.jgll.parser.gss.lookup;

import java.util.Collections;

import org.jgll.grammar.slot.GrammarSlot;
import org.jgll.parser.gss.GSSNode;
import org.jgll.util.Input;

public class DummyNodeLookup implements GSSNodeLookup {
	
	private final static DummyNodeLookup instance = new DummyNodeLookup();
	
	public static DummyNodeLookup getInstance() {
		return instance;
	}
	
	private DummyNodeLookup() {}

	@Override
	public GSSNode getOrElseCreate(GrammarSlot slot, int i) {
		return null;
	}

	@Override
	public GSSNode get(int i) {
		return null;
	}

	@Override
	public void reset(Input input) {
	}

	@Override
	public Iterable<GSSNode> getNodes() {
		return Collections.emptyList();
	}

}
