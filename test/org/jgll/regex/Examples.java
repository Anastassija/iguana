package org.jgll.regex;

import static org.junit.Assert.*;

import org.jgll.grammar.symbol.Character;
import org.jgll.grammar.symbol.CharacterClass;
import org.jgll.grammar.symbol.Range;
import org.jgll.util.Input;
import org.junit.Test;


public class Examples {
	
	/**
	 * Id ::= [a-zA-Z][a-zA-Z0-9]*
	 */
	@Test
	public void test1() {
		CharacterClass c1 = new CharacterClass(new Range('a', 'z'), new Range('A', 'Z'));
		CharacterClass c2 = new CharacterClass(new Range('a', 'z'), new Range('A', 'Z'), new Range('0', '9'));
		
		RegularExpression regexp = new Sequence(c1, new RegexStar(c2));
		Automaton nfa = regexp.toNFA();
		
		Matcher dfa = nfa.getMatcher();

		assertTrue(dfa.match(Input.fromString("a")));
		assertFalse(dfa.match(Input.fromString("9")));
		assertTrue(dfa.match(Input.fromString("abc")));
		assertTrue(dfa.match(Input.fromString("Identifier")));
		assertTrue(dfa.match(Input.fromString("Identifier12")));
		assertTrue(dfa.match(Input.fromString("Identifier12Assdfd")));
	}
	
	/**
	 * Float ::= [0-9]+[.][0-9]+
	 */
	@Test
	public void test2() {
		CharacterClass c = new CharacterClass(new Range('0', '9'));
		RegularExpression regexp = new Sequence(new RegexPlus(c), new Character('.'), new RegexPlus(c));
		
		Automaton nfa = regexp.toNFA();
		
		Matcher dfa = nfa.getMatcher();

		assertTrue(dfa.match(Input.fromString("1.2")));
		assertFalse(dfa.match(Input.fromString("9")));
		assertFalse(dfa.match(Input.fromString(".9")));
		assertFalse(dfa.match(Input.fromString("123.")));
		assertTrue(dfa.match(Input.fromString("12.2")));
		assertTrue(dfa.match(Input.fromString("1342343.27890")));
		assertTrue(dfa.match(Input.fromString("908397439483.278902433")));
	}
	

}
