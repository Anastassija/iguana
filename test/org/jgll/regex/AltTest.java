package org.jgll.regex;

import static org.junit.Assert.*;

import org.jgll.grammar.symbol.Character;
import org.jgll.grammar.symbol.Keyword;
import org.jgll.regex.automaton.Automaton;
import org.jgll.regex.automaton.AutomatonOperations;
import org.jgll.regex.automaton.RunnableAutomaton;
import org.jgll.util.Input;
import org.junit.Test;


public class AltTest {
	
	@Test
	public void test1() {
		Character a = new Character('a');
		Character b = new Character('b');
		
		RegularExpression regexp = new RegexAlt<>(a, b);
		Automaton nfa = regexp.toAutomaton();
		
		assertEquals(6, nfa.getCountStates());
		
		RunnableAutomaton dfa = nfa.getRunnableAutomaton();
		assertEquals(1, dfa.match(Input.fromString("a"), 0));
		assertEquals(1, dfa.match(Input.fromString("b"), 0));
	}
	
	@Test
	public void test() {
		Keyword k1 = new Keyword("for");
		Keyword k2 = new Keyword("forall");
		
		Automaton result = AutomatonOperations.or(k1.toAutomaton(), k2.toAutomaton());

		RunnableAutomaton dfa = result.getRunnableAutomaton();
		assertEquals(3, dfa.match(Input.fromString("for"), 0));
		assertEquals(6, dfa.match(Input.fromString("forall"), 0));
	}

	
	@Test
	public void testMatchAction() {
		RegularExpression regexp = new RegexAlt<>(new Keyword("when"), new Keyword("if"));

		Automaton automaton = regexp.toAutomaton();

		RunnableAutomaton dfa = automaton.getRunnableAutomaton();
		
		assertEquals(4, dfa.match(Input.fromString("when"), 0));
		assertEquals(2, dfa.match(Input.fromString("if"), 0));
	}
}
