package org.jgll.disambiguation.precedence;

import static org.jgll.util.CollectionsUtil.*;
import static org.junit.Assert.*;

import org.jgll.grammar.Grammar;
import org.jgll.grammar.GrammarGraph;
import org.jgll.grammar.precedence.OperatorPrecedence;
import org.jgll.grammar.symbol.Character;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Rule;
import org.jgll.parser.GLLParser;
import org.jgll.parser.ParseResult;
import org.jgll.parser.ParserFactory;
import org.jgll.sppf.IntermediateNode;
import org.jgll.sppf.NonterminalNode;
import org.jgll.sppf.PackedNode;
import org.jgll.sppf.SPPFNode;
import org.jgll.sppf.SPPFNodeFactory;
import org.jgll.sppf.TokenSymbolNode;
import org.jgll.util.Input;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * E ::= E * E			  (left)
 * 	   > (E + E | E - E)  (left)
 *     > - E
 *     | a
 * 
 * 
 * @author Ali Afroozeh
 *
 */
public class PrecedenceTest6 {

	private GLLParser parser;

	private Nonterminal E = Nonterminal.withName("E");
	private Character star = Character.from('*');
	private Character minus = Character.from('-');
	private Character plus = Character.from('+');
	private Character a = Character.from('a');

	private Grammar grammar;

	@Before
	public void createGrammar() {
		
		Grammar.Builder builder = new Grammar.Builder();
		
		// E ::= E * E
		Rule rule1 = new Rule(E, list(E, star, E));
		builder.addRule(rule1);		
		
		// E ::= E + E
		Rule rule2 = new Rule(E, list(E, plus, E));
		builder.addRule(rule2);

		// E ::= E - E
		Rule rule3 = new Rule(E, list(E, minus, E));
		builder.addRule(rule3);
		
		// E ::= - E
		Rule rule4 = new Rule(E, list(minus, E));
		builder.addRule(rule4);
		
		// E ::= a
		Rule rule5 = new Rule(E, list(a));
		builder.addRule(rule5);
		
		OperatorPrecedence operatorPrecedence = new OperatorPrecedence();
		
		// E * E (left): 		E * . E, E * E
		operatorPrecedence.addPrecedencePattern(E, rule1, 2, rule1);
		
		// E * E > E + E:		E * . E, E + E and . E * E, E + E
		operatorPrecedence.addPrecedencePattern(E, rule1, 2, rule2);
		operatorPrecedence.addPrecedencePattern(E, rule1, 0, rule2);
		
		// E * E > E - E:		E * . E, E - E
		operatorPrecedence.addPrecedencePattern(E, rule1, 2, rule3);
		
		// E * E > - E:			. E * E, - E
		operatorPrecedence.addPrecedencePattern(E, rule1, 0, rule4);
		
		// E * E > E - E		. E * E, E - E
		operatorPrecedence.addPrecedencePattern(E, rule1, 0, rule3);
		operatorPrecedence.addPrecedencePattern(E, rule1, 2, rule3);
		
		// E + E (left):		E + . E, E + E
		operatorPrecedence.addPrecedencePattern(E, rule2, 2, rule2);
		
		// E - E (left):		E - . E, E - E
		operatorPrecedence.addPrecedencePattern(E, rule3, 2, rule3);
		
		// E + E left E - E		E + . E, E - E
		operatorPrecedence.addPrecedencePattern(E, rule2, 2, rule3);
		
		// E - E left E + E		E - . E, E + E 
		operatorPrecedence.addPrecedencePattern(E, rule3, 2, rule2);
		
		// E + E > - E			. E + E, - E
		operatorPrecedence.addPrecedencePattern(E, rule2, 0, rule4);
		
		// E - E > - E			. E - E, - E
		operatorPrecedence.addPrecedencePattern(E, rule3, 0, rule4);
		
		grammar = operatorPrecedence.transform(builder.build());
	}
	
	@Test
	public void testInput() {
		Input input = Input.fromString("a+a--a+-a+a-a-a+a");
		parser = ParserFactory.newParser(grammar, input);
		ParseResult result = parser.parse(input, grammar.toGrammarGraph(), "E");
		assertTrue(result.isParseSuccess());
		assertEquals(0, result.asParseSuccess().getParseStatistics().getCountAmbiguousNodes());
		assertTrue(result.asParseSuccess().getRoot().deepEquals(getSPPF()));
	}

	private SPPFNode getSPPF() {
		GrammarGraph grammarGraph = grammar.toGrammarGraph();
		SPPFNodeFactory factory = new SPPFNodeFactory(grammarGraph);
		NonterminalNode node1 = factory.createNonterminalNode("E", 0, 0, 17).init();
		PackedNode node2 = factory.createPackedNode("E ::= E5 - E4 .", 4, node1);
		IntermediateNode node3 = factory.createIntermediateNode("E ::= E5 - . E4", 0, 4).init();
		PackedNode node4 = factory.createPackedNode("E ::= E5 - . E4", 3, node3);
		NonterminalNode node5 = factory.createNonterminalNode("E", 5, 0, 3).init();
		PackedNode node6 = factory.createPackedNode("E5 ::= E5 + E7 .", 2, node5);
		IntermediateNode node7 = factory.createIntermediateNode("E5 ::= E5 + . E7", 0, 2).init();
		PackedNode node8 = factory.createPackedNode("E5 ::= E5 + . E7", 1, node7);
		NonterminalNode node9 = factory.createNonterminalNode("E", 5, 0, 1).init();
		PackedNode node10 = factory.createPackedNode("E5 ::= a .", 0, node9);
		TokenSymbolNode node11 = factory.createTokenNode("a", 0, 1);
		node10.addChild(node11);
		node9.addChild(node10);
		TokenSymbolNode node12 = factory.createTokenNode("+", 1, 1);
		node8.addChild(node9);
		node8.addChild(node12);
		node7.addChild(node8);
		NonterminalNode node13 = factory.createNonterminalNode("E", 7, 2, 3).init();
		PackedNode node14 = factory.createPackedNode("E7 ::= a .", 2, node13);
		TokenSymbolNode node15 = factory.createTokenNode("a", 2, 1);
		node14.addChild(node15);
		node13.addChild(node14);
		node6.addChild(node7);
		node6.addChild(node13);
		node5.addChild(node6);
		TokenSymbolNode node16 = factory.createTokenNode("-", 3, 1);
		node4.addChild(node5);
		node4.addChild(node16);
		node3.addChild(node4);
		NonterminalNode node17 = factory.createNonterminalNode("E", 4, 4, 17).init();
		PackedNode node18 = factory.createPackedNode("E4 ::= - E .", 5, node17);
		TokenSymbolNode node19 = factory.createTokenNode("-", 4, 1);
		NonterminalNode node20 = factory.createNonterminalNode("E", 0, 5, 17).init();
		PackedNode node21 = factory.createPackedNode("E ::= E5 + E3 .", 7, node20);
		IntermediateNode node22 = factory.createIntermediateNode("E ::= E5 + . E3", 5, 7).init();
		PackedNode node23 = factory.createPackedNode("E ::= E5 + . E3", 6, node22);
		NonterminalNode node24 = factory.createNonterminalNode("E", 5, 5, 6).init();
		PackedNode node25 = factory.createPackedNode("E5 ::= a .", 5, node24);
		TokenSymbolNode node26 = factory.createTokenNode("a", 5, 1);
		node25.addChild(node26);
		node24.addChild(node25);
		TokenSymbolNode node27 = factory.createTokenNode("+", 6, 1);
		node23.addChild(node24);
		node23.addChild(node27);
		node22.addChild(node23);
		NonterminalNode node28 = factory.createNonterminalNode("E", 3, 7, 17).init();
		PackedNode node29 = factory.createPackedNode("E3 ::= - E .", 8, node28);
		TokenSymbolNode node30 = factory.createTokenNode("-", 7, 1);
		NonterminalNode node31 = factory.createNonterminalNode("E", 0, 8, 17).init();
		PackedNode node32 = factory.createPackedNode("E ::= E5 + E3 .", 16, node31);
		IntermediateNode node33 = factory.createIntermediateNode("E ::= E5 + . E3", 8, 16).init();
		PackedNode node34 = factory.createPackedNode("E ::= E5 + . E3", 15, node33);
		NonterminalNode node35 = factory.createNonterminalNode("E", 5, 8, 15).init();
		PackedNode node36 = factory.createPackedNode("E5 ::= E5 - E7 .", 14, node35);
		IntermediateNode node37 = factory.createIntermediateNode("E5 ::= E5 - . E7", 8, 14).init();
		PackedNode node38 = factory.createPackedNode("E5 ::= E5 - . E7", 13, node37);
		NonterminalNode node39 = factory.createNonterminalNode("E", 5, 8, 13).init();
		PackedNode node40 = factory.createPackedNode("E5 ::= E5 - E7 .", 12, node39);
		IntermediateNode node41 = factory.createIntermediateNode("E5 ::= E5 - . E7", 8, 12).init();
		PackedNode node42 = factory.createPackedNode("E5 ::= E5 - . E7", 11, node41);
		NonterminalNode node43 = factory.createNonterminalNode("E", 5, 8, 11).init();
		PackedNode node44 = factory.createPackedNode("E5 ::= E5 + E7 .", 10, node43);
		IntermediateNode node45 = factory.createIntermediateNode("E5 ::= E5 + . E7", 8, 10).init();
		PackedNode node46 = factory.createPackedNode("E5 ::= E5 + . E7", 9, node45);
		NonterminalNode node47 = factory.createNonterminalNode("E", 5, 8, 9).init();
		PackedNode node48 = factory.createPackedNode("E5 ::= a .", 8, node47);
		TokenSymbolNode node49 = factory.createTokenNode("a", 8, 1);
		node48.addChild(node49);
		node47.addChild(node48);
		TokenSymbolNode node50 = factory.createTokenNode("+", 9, 1);
		node46.addChild(node47);
		node46.addChild(node50);
		node45.addChild(node46);
		NonterminalNode node51 = factory.createNonterminalNode("E", 7, 10, 11).init();
		PackedNode node52 = factory.createPackedNode("E7 ::= a .", 10, node51);
		TokenSymbolNode node53 = factory.createTokenNode("a", 10, 1);
		node52.addChild(node53);
		node51.addChild(node52);
		node44.addChild(node45);
		node44.addChild(node51);
		node43.addChild(node44);
		TokenSymbolNode node54 = factory.createTokenNode("-", 11, 1);
		node42.addChild(node43);
		node42.addChild(node54);
		node41.addChild(node42);
		NonterminalNode node55 = factory.createNonterminalNode("E", 7, 12, 13).init();
		PackedNode node56 = factory.createPackedNode("E7 ::= a .", 12, node55);
		TokenSymbolNode node57 = factory.createTokenNode("a", 12, 1);
		node56.addChild(node57);
		node55.addChild(node56);
		node40.addChild(node41);
		node40.addChild(node55);
		node39.addChild(node40);
		TokenSymbolNode node58 = factory.createTokenNode("-", 13, 1);
		node38.addChild(node39);
		node38.addChild(node58);
		node37.addChild(node38);
		NonterminalNode node59 = factory.createNonterminalNode("E", 7, 14, 15).init();
		PackedNode node60 = factory.createPackedNode("E7 ::= a .", 14, node59);
		TokenSymbolNode node61 = factory.createTokenNode("a", 14, 1);
		node60.addChild(node61);
		node59.addChild(node60);
		node36.addChild(node37);
		node36.addChild(node59);
		node35.addChild(node36);
		TokenSymbolNode node62 = factory.createTokenNode("+", 15, 1);
		node34.addChild(node35);
		node34.addChild(node62);
		node33.addChild(node34);
		NonterminalNode node63 = factory.createNonterminalNode("E", 3, 16, 17).init();
		PackedNode node64 = factory.createPackedNode("E3 ::= a .", 16, node63);
		TokenSymbolNode node65 = factory.createTokenNode("a", 16, 1);
		node64.addChild(node65);
		node63.addChild(node64);
		node32.addChild(node33);
		node32.addChild(node63);
		node31.addChild(node32);
		node29.addChild(node30);
		node29.addChild(node31);
		node28.addChild(node29);
		node21.addChild(node22);
		node21.addChild(node28);
		node20.addChild(node21);
		node18.addChild(node19);
		node18.addChild(node20);
		node17.addChild(node18);
		node2.addChild(node3);
		node2.addChild(node17);
		node1.addChild(node2);
		return node1;
	}
	
}
