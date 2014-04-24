package org.jgll.regex;

import static org.junit.Assert.*;

import org.jgll.grammar.condition.RegularExpressionCondition;
import org.jgll.grammar.symbol.Character;
import org.jgll.regex.automaton.Automaton;
import org.jgll.regex.automaton.RunnableAutomaton;
import org.jgll.util.Input;
import org.jgll.util.Visualization;
import org.junit.Test;

public class PlusTest {
	
	@Test
	public void test1() {
		RegularExpression regexp = new RegexPlus(new Character('a'));
		Automaton nfa = regexp.getAutomaton();
		
		assertEquals(6, nfa.getCountStates());
		
		RunnableAutomaton dfa = nfa.getRunnableAutomaton();
		
		assertEquals(1, dfa.match(Input.fromString("a"), 0));
		assertEquals(2, dfa.match(Input.fromString("aa"), 0));
		assertEquals(3, dfa.match(Input.fromString("aaa"), 0));
		assertEquals(6, dfa.match(Input.fromString("aaaaaa"), 0));
		assertEquals(17, dfa.match(Input.fromString("aaaaaaaaaaaaaaaaa"), 0));
		assertEquals(33, dfa.match(Input.fromString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"), 0));
		
		assertFalse(dfa.match(Input.fromString("")));
	}

	
	@Test
	public void test2() {
		
		
		RegularExpression regexp = new Sequence<>(new RegexPlus(new Character('a')), new Character(':'));
		
		Automaton automaton = regexp.getAutomaton();
		
		Visualization.generateAutomatonGraph("/Users/aliafroozeh/output", automaton.getStartState());
		
		RunnableAutomaton dfa = automaton.getRunnableAutomaton();
		
		assertEquals(1, dfa.match(Input.fromString("a"), 0));
		assertEquals(-1, dfa.match(Input.fromString("a:"), 0));
		
		assertEquals(2, dfa.match(Input.fromString("aa"), 0));
		assertEquals(-1, dfa.match(Input.fromString("aa:"), 0));
		
		assertEquals(3, dfa.match(Input.fromString("aaa"), 0));
		assertEquals(-1, dfa.match(Input.fromString("aaa:"), 0));
		
		assertEquals(6, dfa.match(Input.fromString("aaaaaa"), 0));
		assertEquals(-1, dfa.match(Input.fromString("aaaaaa:"), 0));
		
		assertEquals(17, dfa.match(Input.fromString("aaaaaaaaaaaaaaaaa"), 0));
		assertEquals(-1, dfa.match(Input.fromString("aaaaaaaaaaaaaaaaa:"), 0));
		
		assertEquals(33, dfa.match(Input.fromString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"), 0));
		assertEquals(-1, dfa.match(Input.fromString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa:"), 0));

		
		assertFalse(dfa.match(Input.fromString("")));
	}

}
