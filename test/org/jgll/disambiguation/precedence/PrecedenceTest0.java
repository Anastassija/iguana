package org.jgll.disambiguation.precedence;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.jgll.grammar.Grammar;
import org.jgll.grammar.GrammarGraph;
import org.jgll.grammar.patterns.PrecedencePattern;
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
import org.jgll.sppf.TerminalNode;
import org.jgll.util.Configuration;
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
public class PrecedenceTest0 {

	private GLLParser parser;
	
	private Nonterminal E = Nonterminal.withName("E");
	private Character a = Character.from('a');
	private Character star = Character.from('*');
	private Character plus = Character.from('+');

	private Grammar grammar;

	@Before
	public void init() {
		
		Grammar.Builder builder = new Grammar.Builder();
		
		// E ::= E * E
		Rule rule1 = Rule.withHead(E).addSymbols(E, star, E).build();
		builder.addRule(rule1);
		
		// E ::= E + E
		Rule rule2 = Rule.withHead(E).addSymbols(E, plus, E).build();
		builder.addRule(rule2);
		
		// E ::= a
		Rule rule3 = Rule.withHead(E).addSymbol(a).build();
		builder.addRule(rule3);

		
		List<PrecedencePattern> list = new ArrayList<>();
		
		// (E, E * .E, E * E)
		list.add(PrecedencePattern.from(rule1, 2, rule1));
		
		// (E, E * .E, E + E)
		list.add(PrecedencePattern.from(rule1, 0, rule2));
		
		// (E, .E * E, E + E)
		list.add(PrecedencePattern.from(rule1, 2, rule2));
		
		// (E, E + .E, E + E)
		list.add(PrecedencePattern.from(rule2, 2, rule2));
		
		OperatorPrecedence operatorPrecedence = new OperatorPrecedence(list);
		grammar = operatorPrecedence.transform(builder.build());
	}
	
	@Test
	public void testGrammar() {
		assertEquals(getGrammar(), grammar);
	}
	
	@Test
	public void testParser() {
		Input input = Input.fromString("a+a*a");
		parser = ParserFactory.getParser(Configuration.DEFAULT, input, grammar);
		ParseResult result = parser.parse(input, grammar, Nonterminal.withName("E"));
		assertTrue(result.isParseSuccess());
		assertEquals(0, result.asParseSuccess().getStatistics().getCountAmbiguousNodes());
		assertTrue(result.asParseSuccess().getRoot().deepEquals(getSPPFNode(parser.getGrammarGraph())));
	}
	
	private Grammar getGrammar() {
		return Grammar.builder()
					  .addRule(Rule.withHead(Nonterminal.builder("E").build()).addSymbol(Nonterminal.builder("E").setIndex(2).build()).addSymbol(Character.from(42)).addSymbol(Nonterminal.builder("E").setIndex(1).build()).build())
					  .addRule(Rule.withHead(Nonterminal.builder("E").build()).addSymbol(Nonterminal.builder("E").build()).addSymbol(Character.from(43)).addSymbol(Nonterminal.builder("E").setIndex(2).build()).build())
					  .addRule(Rule.withHead(Nonterminal.builder("E").build()).addSymbol(Character.from(97)).build())
					  .addRule(Rule.withHead(Nonterminal.builder("E").setIndex(1).build()).addSymbol(Character.from(97)).build())
					  .addRule(Rule.withHead(Nonterminal.builder("E").setIndex(2).build()).addSymbol(Nonterminal.builder("E").setIndex(2).build()).addSymbol(Character.from(42)).addSymbol(Nonterminal.builder("E").setIndex(1).build()).build())
					  .addRule(Rule.withHead(Nonterminal.builder("E").setIndex(2).build()).addSymbol(Character.from(97)).build())
					  .build();
	}
	
	private SPPFNode getSPPFNode(GrammarGraph graph) {
		SPPFNodeFactory factory = new SPPFNodeFactory(graph);
		NonterminalNode node1 = factory.createNonterminalNode("E", 0, 0, 5);
		PackedNode node2 = factory.createPackedNode("E ::= E + E2 .", 2, node1);
		IntermediateNode node3 = factory.createIntermediateNode("E ::= E + . E2", 0, 2);
		PackedNode node4 = factory.createPackedNode("E ::= E + . E2", 1, node3);
		NonterminalNode node5 = factory.createNonterminalNode("E", 0, 0, 1);
		PackedNode node6 = factory.createPackedNode("E ::= a .", 1, node5);
		TerminalNode node7 = factory.createTerminalNode("a", 0, 1);
		node6.addChild(node7);
		node5.addChild(node6);
		TerminalNode node8 = factory.createTerminalNode("+", 1, 2);
		node4.addChild(node5);
		node4.addChild(node8);
		node3.addChild(node4);
		NonterminalNode node9 = factory.createNonterminalNode("E", 2, 2, 5);
		PackedNode node10 = factory.createPackedNode("E2 ::= E2 * E1 .", 4, node9);
		IntermediateNode node11 = factory.createIntermediateNode("E2 ::= E2 * . E1", 2, 4);
		PackedNode node12 = factory.createPackedNode("E2 ::= E2 * . E1", 3, node11);
		NonterminalNode node13 = factory.createNonterminalNode("E", 2, 2, 3);
		PackedNode node14 = factory.createPackedNode("E2 ::= a .", 3, node13);
		TerminalNode node15 = factory.createTerminalNode("a", 2, 3);
		node14.addChild(node15);
		node13.addChild(node14);
		TerminalNode node16 = factory.createTerminalNode("*", 3, 4);
		node12.addChild(node13);
		node12.addChild(node16);
		node11.addChild(node12);
		NonterminalNode node17 = factory.createNonterminalNode("E", 1, 4, 5);
		PackedNode node18 = factory.createPackedNode("E1 ::= a .", 5, node17);
		TerminalNode node19 = factory.createTerminalNode("a", 4, 5);
		node18.addChild(node19);
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
