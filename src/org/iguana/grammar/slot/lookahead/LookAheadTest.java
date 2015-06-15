package org.iguana.grammar.slot.lookahead;

import java.util.Collections;
import java.util.List;

import org.iguana.grammar.slot.BodyGrammarSlot;

@FunctionalInterface
public interface LookAheadTest {
	
	default boolean test(int v) {
		return get(v) != null && !get(v).isEmpty();
	}
	
	/**
	 * Returns a list of first slots that can be parsed
	 * at the given input character. 
	 */
	public List<BodyGrammarSlot> get(int v);
	
	public static final LookAheadTest NO_LOOKAYOUT = new LookAheadTest() {
		
		@Override
		public boolean test(int v) { return true; };
		
		@Override
		public List<BodyGrammarSlot> get(int v) { return Collections.emptyList(); }
	};
}
