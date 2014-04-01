package org.jgll.regex.automaton;

import static org.jgll.regex.automaton.AutomatonOperations.*;
import static org.junit.Assert.*;

import org.jgll.grammar.symbol.Keyword;
import org.jgll.util.Input;
import org.junit.Test;

public class UnionTest {
	
	private Keyword k1 = new Keyword("if");
	private Keyword k2 = new Keyword("when");
	private Keyword k3 = new Keyword("new");

	@Test
	public void test1() {
		Automaton a = union(k1.toAutomaton(), k2.toAutomaton());
		assertTrue(a.getMatcher().match(Input.fromString("if")));
		assertTrue(a.getMatcher().match(Input.fromString("when")));
		assertFalse(a.getMatcher().match(Input.fromString("else")));
	}
	
	public void test3() {
		Automaton a = union(k1.toAutomaton(), union(k2.toAutomaton(), k3.toAutomaton()));
		assertTrue(a.getMatcher().match(Input.fromString("if")));
		assertTrue(a.getMatcher().match(Input.fromString("when")));
		assertTrue(a.getMatcher().match(Input.fromString("new")));
		assertFalse(a.getMatcher().match(Input.fromString("else")));
	}

}
