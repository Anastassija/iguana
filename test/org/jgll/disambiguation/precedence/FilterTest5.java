package org.jgll.disambiguation.precedence;

import static org.jgll.util.CollectionsUtil.list;
import static org.junit.Assert.assertTrue;

import org.jgll.grammar.Grammar;
import org.jgll.grammar.GrammarBuilder;
import org.jgll.grammar.slot.factory.GrammarSlotFactoryImpl;
import org.jgll.grammar.slot.factory.GrammarSlotFactory;
import org.jgll.grammar.symbol.Character;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Rule;
import org.jgll.parser.GLLParser;
import org.jgll.parser.ParseError;
import org.jgll.parser.ParserFactory;
import org.jgll.sppf.NonterminalSymbolNode;
import org.jgll.sppf.SPPFNode;
import org.jgll.sppf.TokenSymbolNode;
import org.jgll.util.Input;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * E ::= E z   1
 *     > x E   2
 *     > E w   3
 *     > y E   4
 *     | a
 * 
 * @author Ali Afroozeh
 *
 */
public class FilterTest5 {

	private Grammar grammar;
	private GLLParser parser;
	
	private Nonterminal E = new Nonterminal("E");
	private Character a = new Character('a');
	private Character w = new Character('w');
	private Character x = new Character('x');
	private Character y = new Character('y');
	private Character z = new Character('z');

	@Before
	public void createGrammar() {
		
		GrammarSlotFactory factory = new GrammarSlotFactoryImpl();
		GrammarBuilder builder = new GrammarBuilder("TwoLevelFiltering", factory);
		
		// E ::= E z
		Rule rule1 = new Rule(E, list(E, z));
		builder.addRule(rule1);
		
		// E ::=  x E
		Rule rule2 = new Rule(E, list(x, E));
		builder.addRule(rule2);
		
		// E ::= E w
		Rule rule3 = new Rule(E, list(E, w));
		builder.addRule(rule3);
		
		// E ::= y E
		Rule rule4 = new Rule(E, list(y, E));
		builder.addRule(rule4);
		
		// E ::= a
		Rule rule5 = new Rule(E, list(a));
		builder.addRule(rule5);
		
		// (E, .E z, x E) 
		builder.addPrecedencePattern(E, rule1, 0, rule2);
		
		// (E, .E z, y E) 
		builder.addPrecedencePattern(E, rule1, 0, rule4);
		
		// (E, x .E, E w)
		builder.addPrecedencePattern(E, rule2, 1, rule3);
		
		// (E, .E w, y E)
		builder.addPrecedencePattern(E, rule3, 0, rule4);
		
		grammar =  builder.build();
	}

	@Test
	public void testParsers() throws ParseError {
		Input input = Input.fromString("xawz");
		parser = ParserFactory.newParser(grammar, input);
		NonterminalSymbolNode sppf = parser.parse(input, grammar, "E");
		assertTrue(sppf.deepEquals(getSPPF()));
	}
	
	private SPPFNode getSPPF() {
		NonterminalSymbolNode node1 = new NonterminalSymbolNode(grammar.getNonterminalId(E), 5, 0, 4);
		NonterminalSymbolNode node2 = new NonterminalSymbolNode(grammar.getNonterminalId(E), 5, 0, 3);
		NonterminalSymbolNode node3 = new NonterminalSymbolNode(grammar.getNonterminalId(E), 5, 0, 2);
		TokenSymbolNode node4 = new TokenSymbolNode(grammar.getRegularExpressionId(x), 0, 1);
		NonterminalSymbolNode node5 = new NonterminalSymbolNode(grammar.getNonterminalId(E), 5, 1, 2);
		TokenSymbolNode node6 = new TokenSymbolNode(grammar.getRegularExpressionId(a), 1, 1);
		node5.addChild(node6);
		node3.addChild(node4);
		node3.addChild(node5);
		TokenSymbolNode node7 = new TokenSymbolNode(grammar.getRegularExpressionId(w), 2, 1);
		node2.addChild(node3);
		node2.addChild(node7);
		TokenSymbolNode node8 = new TokenSymbolNode(grammar.getRegularExpressionId(z), 3, 1);
		node1.addChild(node2);
		node1.addChild(node8);
		return node1;
	}

}
