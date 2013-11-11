package org.jgll.grammar;

import static org.jgll.util.CollectionsUtil.*;
import static org.junit.Assert.*;

import org.jgll.grammar.ebnf.EBNFUtil;
import org.jgll.grammar.symbol.Character;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Plus;
import org.jgll.grammar.symbol.Rule;
import org.jgll.grammar.symbol.Terminal;
import org.jgll.parser.GLLParser;
import org.jgll.parser.ParseError;
import org.jgll.parser.ParserFactory;
import org.jgll.sppf.NonterminalSymbolNode;
import org.jgll.util.Input;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * S ::= A+
 *      
 * A ::= a
 * 
 * @author Ali Afroozeh
 *
 */
public class EBNFTest1 {
	
	private Grammar grammar;
	private GLLParser rdParser;
	private GLLParser levelParser;

	@Before
	public void init() {
		
		GrammarBuilder builder = new GrammarBuilder("EBNF");
		
		Nonterminal S = new Nonterminal("S");
		Nonterminal A = new Nonterminal("A");
		Terminal a = new Character('a');
		
		Rule rule1 = new Rule(S, list(new Plus(A)));
		
		Rule rule2 = new Rule(A, list(a));
		
		Iterable<Rule> newRules = EBNFUtil.rewrite(list(rule1, rule2));
		
		builder.addRules(newRules);
		
		grammar = builder.build();
		
		rdParser = ParserFactory.createRecursiveDescentParser(grammar);
		levelParser = ParserFactory.createLevelParser(grammar);
	}
	
	@Test
	public void test() throws ParseError {
		NonterminalSymbolNode sppf1 = rdParser.parse(Input.fromString("aaaaaa"), grammar, "S");
		NonterminalSymbolNode sppf2 = levelParser.parse(Input.fromString("aaaaaa"), grammar, "S");
		assertTrue(sppf1.deepEquals(sppf2));
	}

}
