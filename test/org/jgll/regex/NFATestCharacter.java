package org.jgll.regex;

import static org.junit.Assert.*;

import org.jgll.grammar.symbol.Character;
import org.jgll.util.Input;
import org.junit.Test;

public class NFATestCharacter {
	
	@Test
	public void testCountStates() {
		RegularExpression regexp = new Character('a');
		NFA nfa = regexp.toNFA();
		DFA dfa = nfa.toDFA();
		assertEquals(2, nfa.getCountStates());
		System.out.println(	dfa.match(Input.fromString("a")));
	}

}
