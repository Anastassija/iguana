package org.jgll.grammar.precedence;

import static org.jgll.util.CollectionsUtil.list;
import static org.junit.Assert.assertTrue;

import org.jgll.grammar.Grammar;
import org.jgll.grammar.GrammarBuilder;
import org.jgll.grammar.slot.factory.FirstFollowSetGrammarSlotFactory;
import org.jgll.grammar.slot.factory.GrammarSlotFactory;
import org.jgll.grammar.symbol.Character;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Rule;
import org.jgll.parser.GLLParser;
import org.jgll.parser.ParseError;
import org.jgll.parser.ParserFactory;
import org.jgll.sppf.IntermediateNode;
import org.jgll.sppf.NonterminalSymbolNode;
import org.jgll.sppf.SPPFNode;
import org.jgll.sppf.TokenSymbolNode;
import org.jgll.util.Input;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * E ::= E * E
 * 	   > E + E
 *     | a
 * 
 * @author Ali Afroozeh
 *
 */
public class ArithmeticExpressionsTest {

	private Grammar grammar;
	private GLLParser parser;

	@Before
	public void init() {

		GrammarSlotFactory factory = new FirstFollowSetGrammarSlotFactory();
		GrammarBuilder builder = new GrammarBuilder("ArithmeticExpressions", factory);

		Nonterminal E = new Nonterminal("E");

		// E ::= E * E
		Rule rule1 = new Rule(E, list(E, new Character('*'), E));
		builder.addRule(rule1);
		
		// E ::= E + E
		Rule rule2 = new Rule(E, list(E, new Character('+'), E));
		builder.addRule(rule2);
		
		// E ::= a
		Rule rule3 = new Rule(E, list(new Character('a')));
		builder.addRule(rule3);

		// (E * .E, E * E)
		builder.addPrecedencePattern(E, rule1, 2, rule1);
		
		// (E * .E, E + E)
		builder.addPrecedencePattern(E, rule1, 0, rule2);
		
		// (.E * E, E + E)
		builder.addPrecedencePattern(E, rule1, 2, rule2);
		
		// (E + .E, E + E)
		builder.addPrecedencePattern(E, rule2, 2, rule2);
		
		builder.rewritePrecedencePatterns();
		
		grammar = builder.build();
	}

	@Test
	public void test() throws ParseError {
		Input input = Input.fromString("a+a*a");
		parser = ParserFactory.newParser(grammar, input);
		NonterminalSymbolNode sppf = parser.parse(input, grammar, "E");
		assertTrue(sppf.deepEquals(getSPPFNode()));
	}
	
	private SPPFNode getSPPFNode() {
		NonterminalSymbolNode node1 = new NonterminalSymbolNode(grammar.getHeadGrammarSlot("E"), 0, 5);
		IntermediateNode node2 = new IntermediateNode(grammar.getGrammarSlotByName("E ::= E [+] . E2"), 0, 2);
		NonterminalSymbolNode node3 = new NonterminalSymbolNode(grammar.getHeadGrammarSlot("E"), 0, 1);
		TokenSymbolNode node4 = new TokenSymbolNode(4, 0, 1);
		node3.addChild(node4);
		TokenSymbolNode node5 = new TokenSymbolNode(3, 1, 1);
		node2.addChild(node3);
		node2.addChild(node5);
		NonterminalSymbolNode node6 = new NonterminalSymbolNode(grammar.getNonterminalByNameAndIndex("E", 2), 2, 5);
		IntermediateNode node7 = new IntermediateNode(grammar.getGrammarSlotByName("E2 ::= E2 [*] . E1"), 2, 4);
		NonterminalSymbolNode node8 = new NonterminalSymbolNode(grammar.getNonterminalByNameAndIndex("E", 2), 2, 3);
		TokenSymbolNode node9 = new TokenSymbolNode(4, 2, 1);
		node8.addChild(node9);
		TokenSymbolNode node10 = new TokenSymbolNode(2, 3, 1);
		node7.addChild(node8);
		node7.addChild(node10);
		NonterminalSymbolNode node11 = new NonterminalSymbolNode(grammar.getNonterminalByNameAndIndex("E", 1), 4, 5);
		TokenSymbolNode node12 = new TokenSymbolNode(4, 4, 1);
		node11.addChild(node12);
		node6.addChild(node7);
		node6.addChild(node11);
		node1.addChild(node2);
		node1.addChild(node6);
		return node1;
	}

}
