package org.jgll.grammar.slot;

import java.util.Collections;
import java.util.Set;

import org.jgll.grammar.GrammarRegistry;
import org.jgll.parser.gss.lookup.HashMapNodeLookup;
import org.jgll.util.Input;


public class DummySlot extends BodyGrammarSlot {

	public DummySlot() {
		super(0, null, new HashMapNodeLookup());
	}

	@Override
	public String getConstructorCode(GrammarRegistry registry) {
		return "new DummySlot()";
	}

	@Override
	public void reset(Input input) {
	}

	@Override
	public boolean addTransition(Transition transition) {
		return false;
	}

	@Override
	public Set<Transition> getTransitions() {
		return Collections.emptySet();
	}
	
	@Override
	public String toString() {
		return "$";
	}
	
}
