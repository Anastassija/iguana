package org.jgll.disambiguation.precedence;

import static org.jgll.util.CollectionsUtil.*;
import static org.junit.Assert.*;

import org.jgll.grammar.Grammar;
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
 * E ::= E ^ E	(right)
 *     > E + E	(left)
 *     > - E
 *     | a
 * 
 * 
 * @author Ali Afroozeh
 *
 */
public class PrecedenceTest2 {

	private GLLParser parser;

	private Nonterminal E = Nonterminal.withName("E");
	private Character a = Character.from('a');
	private Character hat = Character.from('^');
	private Character plus = Character.from('+');
	private Character minus = Character.from('-');

	private Grammar grammar;

	
	@Before
	public void createGrammar() {
		
		Grammar.Builder builder = new Grammar.Builder();
		
		// E ::= E ^ E
		Rule rule0 = new Rule(E, list(E, hat, E));
		builder.addRule(rule0);
		
		// E ::= E + E
		Rule rule1 = new Rule(E, list(E, plus, E));
		builder.addRule(rule1);
		
		// E ::= E - E
		Rule rule2 = new Rule(E, list(minus, E));
		builder.addRule(rule2);
		
		// E ::= a
		Rule rule3 = new Rule(E, list(a));
		builder.addRule(rule3);
		
		OperatorPrecedence operatorPrecedence = new OperatorPrecedence();
		
		// left associative E + E
		operatorPrecedence.addPrecedencePattern(E, rule1, 2, rule1);
		
		// + has higher priority than -
		operatorPrecedence.addPrecedencePattern(E, rule1, 0, rule2);
		
		// right associative E ^ E
		operatorPrecedence.addPrecedencePattern(E, rule0, 0, rule0);
		
		// ^ has higher priority than -
		operatorPrecedence.addPrecedencePattern(E, rule0, 0, rule2);
		
		// ^ has higher priority than +
		operatorPrecedence.addPrecedencePattern(E, rule0, 0, rule1);
		operatorPrecedence.addPrecedencePattern(E, rule0, 2, rule1);
		
		grammar = operatorPrecedence.transform(builder.build());
	}

	@Test
	public void test() {
		Input input = Input.fromString("a+a^a^-a+a");
		parser = ParserFactory.newParser(grammar, input);
		ParseResult result = parser.parse(input, grammar.toGrammarGraph(), "E");
		assertTrue(result.isParseSuccess());
		assertEquals(0, result.asParseSuccess().getParseStatistics().getCountAmbiguousNodes());
		assertTrue(result.asParseSuccess().getRoot().deepEquals(getSPPF()));
	}
	
	private SPPFNode getSPPF() {
		SPPFNodeFactory factory = new SPPFNodeFactory(grammar.toGrammarGraph());
		NonterminalNode node1 = factory.createNonterminalNode("E", 0, 0, 10).init();
		PackedNode node2 = factory.createPackedNode("E ::= E2 + E1 .", 2, node1);
		IntermediateNode node3 = factory.createIntermediateNode("E ::= E2 + . E1", 0, 2).init();
		PackedNode node4 = factory.createPackedNode("E ::= E2 + . E1", 1, node3);
		NonterminalNode node5 = factory.createNonterminalNode("E", 2, 0, 1).init();
		PackedNode node6 = factory.createPackedNode("E2 ::= a .", 0, node5);
		TokenSymbolNode node7 = factory.createTokenNode("a", 0, 1);
		node6.addChild(node7);
		node5.addChild(node6);
		TokenSymbolNode node8 = factory.createTokenNode("+", 1, 1);
		node4.addChild(node5);
		node4.addChild(node8);
		node3.addChild(node4);
		NonterminalNode node9 = factory.createNonterminalNode("E", 1, 2, 10).init();
		PackedNode node10 = factory.createPackedNode("E1 ::= E3 ^ E1 .", 4, node9);
		IntermediateNode node11 = factory.createIntermediateNode("E1 ::= E3 ^ . E1", 2, 4).init();
		PackedNode node12 = factory.createPackedNode("E1 ::= E3 ^ . E1", 3, node11);
		NonterminalNode node13 = factory.createNonterminalNode("E", 3, 2, 3).init();
		PackedNode node14 = factory.createPackedNode("E3 ::= a .", 2, node13);
		TokenSymbolNode node15 = factory.createTokenNode("a", 2, 1);
		node14.addChild(node15);
		node13.addChild(node14);
		TokenSymbolNode node16 = factory.createTokenNode("^", 3, 1);
		node12.addChild(node13);
		node12.addChild(node16);
		node11.addChild(node12);
		NonterminalNode node17 = factory.createNonterminalNode("E", 1, 4, 10).init();
		PackedNode node18 = factory.createPackedNode("E1 ::= E3 ^ E1 .", 6, node17);
		IntermediateNode node19 = factory.createIntermediateNode("E1 ::= E3 ^ . E1", 4, 6).init();
		PackedNode node20 = factory.createPackedNode("E1 ::= E3 ^ . E1", 5, node19);
		NonterminalNode node21 = factory.createNonterminalNode("E", 3, 4, 5).init();
		PackedNode node22 = factory.createPackedNode("E3 ::= a .", 4, node21);
		TokenSymbolNode node23 = factory.createTokenNode("a", 4, 1);
		node22.addChild(node23);
		node21.addChild(node22);
		TokenSymbolNode node24 = factory.createTokenNode("^", 5, 1);
		node20.addChild(node21);
		node20.addChild(node24);
		node19.addChild(node20);
		NonterminalNode node25 = factory.createNonterminalNode("E", 1, 6, 10).init();
		PackedNode node26 = factory.createPackedNode("E1 ::= - E .", 7, node25);
		TokenSymbolNode node27 = factory.createTokenNode("-", 6, 1);
		NonterminalNode node28 = factory.createNonterminalNode("E", 0, 7, 10).init();
		PackedNode node29 = factory.createPackedNode("E ::= E2 + E1 .", 9, node28);
		IntermediateNode node30 = factory.createIntermediateNode("E ::= E2 + . E1", 7, 9).init();
		PackedNode node31 = factory.createPackedNode("E ::= E2 + . E1", 8, node30);
		NonterminalNode node32 = factory.createNonterminalNode("E", 2, 7, 8).init();
		PackedNode node33 = factory.createPackedNode("E2 ::= a .", 7, node32);
		TokenSymbolNode node34 = factory.createTokenNode("a", 7, 1);
		node33.addChild(node34);
		node32.addChild(node33);
		TokenSymbolNode node35 = factory.createTokenNode("+", 8, 1);
		node31.addChild(node32);
		node31.addChild(node35);
		node30.addChild(node31);
		NonterminalNode node36 = factory.createNonterminalNode("E", 1, 9, 10).init();
		PackedNode node37 = factory.createPackedNode("E1 ::= a .", 9, node36);
		TokenSymbolNode node38 = factory.createTokenNode("a", 9, 1);
		node37.addChild(node38);
		node36.addChild(node37);
		node29.addChild(node30);
		node29.addChild(node36);
		node28.addChild(node29);
		node26.addChild(node27);
		node26.addChild(node28);
		node25.addChild(node26);
		node18.addChild(node19);
		node18.addChild(node25);
		node17.addChild(node18);
		node10.addChild(node11);
		node10.addChild(node17);
		node9.addChild(node10);
		node2.addChild(node3);
		node2.addChild(node9);
		node1.addChild(node2);
		return node1;
	}

}
