package org.jgll.regex.automaton;

import static org.jgll.regex.automaton.AutomatonOperations.*;
import static org.junit.Assert.*;

import org.jgll.grammar.symbol.CharacterClass;
import org.jgll.grammar.symbol.Range;
import org.jgll.regex.RegularExpression;
import org.jgll.regex.automaton.Automaton;
import org.jgll.regex.automaton.State;
import org.jgll.regex.automaton.Transition;
import org.junit.Test;

public class MergeTransitionsTest {
	
	@Test
	public void test() {
		RegularExpression regexp = new CharacterClass(Range.in('0', '4'), Range.in('5', '7'), Range.in('8', '9'));
		Automaton a = mergeTransitions(regexp.toAutomaton().minimize());
		assertEquals(a, getAutomaton());
	}
	
	private Automaton getAutomaton() {
		State state1 = new State();
		State state2 = new State(true);
		state1.addTransition(new Transition(48, 57, state2));
		return new Automaton(state1);
	}

}