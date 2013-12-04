package org.jgll.grammar.precedence;

import static org.jgll.util.CollectionsUtil.*;

import org.jgll.grammar.Grammar;
import org.jgll.grammar.GrammarBuilder;
import org.jgll.grammar.symbol.Character;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Rule;
import org.jgll.parser.GLLParser;
import org.jgll.parser.ParseError;
import org.jgll.parser.ParserFactory;
import org.jgll.util.Input;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * E ::= E z
 *     > x E
 *     > E w
 *     | a
 * 
 * 
 * @author Ali Afroozeh
 *
 */
public class FilterTest4 {
	
	private Grammar grammar;
	private GLLParser parser;

	@Before
	public void init() {
		
		GrammarBuilder builder = new GrammarBuilder("TwoLevelFiltering");
		
		Nonterminal E = new Nonterminal("E");

		// E ::= E z
		Rule rule1 = new Rule(E, list(E, new Character('z')));
		builder.addRule(rule1);
		
		// E ::=  x E
		Rule rule2 = new Rule(E, list(new Character('x'), E));
		builder.addRule(rule2);
		
		// E ::= E w
		Rule rule3 = new Rule(E, list(E, new Character('w')));
		builder.addRule(rule3);
		
		// E ::= a
		Rule rule4 = new Rule(E, list(new Character('a')));
		builder.addRule(rule4);
		
		// (E, .E z, x E) 
		builder.addPrecedencePattern(E, rule1, 0, rule2);
		
		// (E, x .E, E w)
		builder.addPrecedencePattern(E, rule2, 1, rule3);
		
		builder.rewritePrecedencePatterns();
		
		grammar = builder.build();
		parser = ParserFactory.createRecursiveDescentParser(grammar);
	}

	@Test
	public void testAssociativityAndPriority() throws ParseError {
		parser.parse(Input.fromString("xawz"), grammar, "E");
	}

}
