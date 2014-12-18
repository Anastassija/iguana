package org.jgll.disambiguation.precedence;

import static org.jgll.util.CollectionsUtil.*;
import static org.junit.Assert.*;

import org.jgll.grammar.Grammar;
import org.jgll.grammar.precedence.OperatorPrecedence;
import org.jgll.grammar.symbol.Character;
import org.jgll.grammar.symbol.Keyword;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Rule;
import org.jgll.parser.GLLParser;
import org.jgll.parser.ParseResult;
import org.jgll.parser.ParserFactory;
import org.jgll.util.Input;
import org.jgll.util.Visualization;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * E ::= E Y    (none)
 * 	   > E ; E  (right)
 * 	   > - E
 *     | a
 * 
 * Y ::= X
 * 
 * X ::= X , E
 *     | , E
 * 
 * @author Ali Afroozeh
 *
 */
public class PrecedenceTest11 {
	
	private Nonterminal E = Nonterminal.withName("E");
	private Nonterminal X = Nonterminal.withName("X");
	private Nonterminal Y = Nonterminal.withName("Y");
	private Character a = Character.from('a');
	private Character comma = Character.from(',');
	private Character semicolon = Character.from(';');
	private Keyword min = Keyword.from("-");
	
	private GLLParser parser;
	private Grammar grammar;
	
	@Before
	public void init() {
		
		Grammar.Builder builder = new Grammar.Builder();
		
		// E ::= E Y
		Rule rule1 = new Rule(E, list(E, Y));
		builder.addRule(rule1);
		
		// E ::= E ; E
		Rule rule2 = new Rule(E, list(E, semicolon, E));
		builder.addRule(rule2);
		
		// E ::= - E
		Rule rule3 = new Rule(E, list(min, E));
		builder.addRule(rule3);
		
		// E ::= a
		Rule rule4 = new Rule(E, list(a));
		builder.addRule(rule4);
		
		// Y ::= X
		Rule rule5 = new Rule(Y, list(X));
		builder.addRule(rule5);
		
		// X ::= X , E
		Rule rule6 = new Rule(X, list(X, comma, E));
		builder.addRule(rule6);
		
		// X ::= , E
		Rule rule7 = new Rule(X, list(comma, E));
		builder.addRule(rule7);
		
		
		OperatorPrecedence operatorPrecedence = new OperatorPrecedence();

		// (E, .E Y, E ";" E)
		operatorPrecedence.addPrecedencePattern(E, rule1, 0, rule2);
		
		// (E, E .Y, E ";" E)
		operatorPrecedence.addPrecedencePattern(E, rule1, 1, rule2);
		
		// (E, .E Y, - E)
		operatorPrecedence.addPrecedencePattern(E, rule1, 0, rule3);		
		
		// (E, .E ";" E, - E)
		operatorPrecedence.addPrecedencePattern(E, rule2, 0, rule3);
		
		grammar = operatorPrecedence.transform(builder.build());
		System.out.println(grammar);
	}
	
	@Test
	public void test1() {
		Input input = Input.fromString("a,-a;a");
		parser = ParserFactory.newParser();
		ParseResult result = parser.parse(input, grammar.toGrammarGraph(), "E");
		assertTrue(result.isParseSuccess());
//		assertEquals(0, result.asParseSuccess().getParseStatistics().getCountAmbiguousNodes());
//		assertTrue(result.asParseSuccess().getRoot().deepEquals(getSPPF()));
	}	

}
