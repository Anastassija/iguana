package org.jgll.parser.ambiguous;


import static org.jgll.util.CollectionsUtil.*;
import static org.junit.Assert.*;

import org.jgll.grammar.Grammar;
import org.jgll.grammar.GrammarGraph;
import org.jgll.grammar.symbol.Character;
import org.jgll.grammar.symbol.EOF;
import org.jgll.grammar.symbol.Epsilon;
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
 *	S ::= a S 
 *      | A S d 
 *      | epsilon
 *       
 * 	A ::= a
 */
public class Gamma0Test {

	private Character a = Character.from('a');
	private Nonterminal S = Nonterminal.withName("S");
	private Nonterminal A = Nonterminal.withName("A");
	private Character d = Character.from('d');

	private Grammar grammar;

	@Before
	public void init() {
		Rule r1 = Rule.withHead(S).addSymbols(a, S).build();
		Rule r2 = Rule.withHead(S).addSymbols(A, S, d).build();
		Rule r3 = Rule.withHead(S).build();
		Rule r4 = Rule.withHead(A).addSymbols(a).build();
		
		grammar = Grammar.builder().addRules(r1, r2, r3, r4).build();
	}
	
//	@Test
//	public void testNullables() {
//		assertTrue(grammar.isNullable(S));
//		assertFalse(grammar.isNullable(A));
//	}
//	
//	@Test
//	public void testFirstSets() {
//		assertEquals(set(a, Epsilon.getInstance()), grammar.getFirstSet(S));
//		assertEquals(set(a), grammar.getFirstSet(A));
//	}
//
//	@Test
//	public void testFollowSets() {
//		assertEquals(set(a, d, EOF.getInstance()), grammar.getFollowSet(A));
//		assertEquals(set(d, EOF.getInstance()), grammar.getFollowSet(S));
//	}
	
	@Test
	public void test() {
		Input input = Input.fromString("aad");
		GLLParser parser = ParserFactory.getParser(Configuration.DEFAULT, input, grammar);
		ParseResult result = parser.parse(input, grammar, Nonterminal.withName("S"));
		assertEquals(1, result.asParseSuccess().getStatistics().getCountAmbiguousNodes());
		assertTrue(result.isParseSuccess());
		assertTrue(result.asParseSuccess().getRoot().deepEquals(getSPPF(parser.getGrammarGraph())));
	}
	
	public SPPFNode getSPPF(GrammarGraph graph) {
		SPPFNodeFactory factory = new SPPFNodeFactory(graph);
		NonterminalNode node1 = factory.createNonterminalNode("S", 0, 0, 3);
		PackedNode node2 = factory.createPackedNode("S ::= a S .", 1, node1);
		TerminalNode node3 = factory.createTerminalNode("a", 0, 1);
		NonterminalNode node4 = factory.createNonterminalNode("S", 0, 1, 3);
		PackedNode node5 = factory.createPackedNode("S ::= A S d .", 2, node4);
		IntermediateNode node6 = factory.createIntermediateNode("S ::= A S . d", 1, 2);
		PackedNode node7 = factory.createPackedNode("S ::= A S . d", 2, node6);
		NonterminalNode node8 = factory.createNonterminalNode("A", 0, 1, 2);
		PackedNode node9 = factory.createPackedNode("A ::= a .", 2, node8);
		TerminalNode node10 = factory.createTerminalNode("a", 1, 2);
		node9.addChild(node10);
		node8.addChild(node9);
		NonterminalNode node11 = factory.createNonterminalNode("S", 0, 2, 2);
		PackedNode node12 = factory.createPackedNode("S ::= .", 2, node11);
		TerminalNode node13 = factory.createEpsilonNode(2);
		node12.addChild(node13);
		node11.addChild(node12);
		node7.addChild(node8);
		node7.addChild(node11);
		node6.addChild(node7);
		TerminalNode node14 = factory.createTerminalNode("d", 2, 3);
		node5.addChild(node6);
		node5.addChild(node14);
		node4.addChild(node5);
		node2.addChild(node3);
		node2.addChild(node4);
		PackedNode node15 = factory.createPackedNode("S ::= A S d .", 2, node1);
		IntermediateNode node16 = factory.createIntermediateNode("S ::= A S . d", 0, 2);
		PackedNode node17 = factory.createPackedNode("S ::= A S . d", 1, node16);
		NonterminalNode node18 = factory.createNonterminalNode("A", 0, 0, 1);
		PackedNode node19 = factory.createPackedNode("A ::= a .", 1, node18);
		node19.addChild(node3);
		node18.addChild(node19);
		NonterminalNode node21 = factory.createNonterminalNode("S", 0, 1, 2);
		PackedNode node22 = factory.createPackedNode("S ::= a S .", 2, node21);
		node22.addChild(node10);
		node22.addChild(node11);
		node21.addChild(node22);
		node17.addChild(node18);
		node17.addChild(node21);
		node16.addChild(node17);
		node15.addChild(node16);
		node15.addChild(node14);
		node1.addChild(node2);
		node1.addChild(node15);
		return node1;
	}
	
}
