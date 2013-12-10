package org.jgll.recognizer;

import org.jgll.grammar.Grammar;
import org.jgll.grammar.slot.GrammarSlot;
import org.jgll.recognizer.lookup.Lookup;

public class PrefixGLLRecognizer extends AbstractGLLRecognizer {
	
	public PrefixGLLRecognizer(Lookup lookup, Grammar grammar) {
		super(lookup, grammar);
	}

	@Override
	public void add(GrammarSlot slot, GSSNode u, int inputIndex) {
		if(slot == startSlot && u == u0) {
			recognized = true;
			return;
		}
		super.add(slot, u, inputIndex);
	}

}
