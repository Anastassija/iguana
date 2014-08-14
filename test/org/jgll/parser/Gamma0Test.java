package org.jgll.parser;


import static org.jgll.util.CollectionsUtil.*;
import static org.junit.Assert.*;

import org.jgll.grammar.Grammar;
import org.jgll.grammar.GrammarGraph;
import org.jgll.grammar.symbol.Character;
import org.jgll.grammar.symbol.EOF;
import org.jgll.grammar.symbol.Epsilon;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Rule;
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
		
		Grammar.Builder builder = new Grammar.Builder();

		Rule r1 = new Rule(S, list(a, S));
		builder.addRule(r1);
		
		Rule r2 = new Rule(S, list(A, S, d));
		builder.addRule(r2);
		
		Rule r3 = new Rule(S);
		builder.addRule(r3);
		
		Rule r4 = new Rule(A, list(a));
		builder.addRule(r4);
		
		grammar = builder.build();
	}
	
	@Test
	public void testNullables() {
		assertTrue(grammar.isNullable(S));
		assertFalse(grammar.isNullable(A));
	}
	
	@Test
	public void testFirstSets() {
		assertEquals(set(a, Epsilon.getInstance()), grammar.getFirstSet(S));
		assertEquals(set(a), grammar.getFirstSet(A));
	}

	@Test
	public void testFollowSets() {
		assertEquals(set(a, d, EOF.getInstance()), grammar.getFollowSet(A));
		assertEquals(set(d, EOF.getInstance()), grammar.getFollowSet(S));
	}
	
	@Test
	public void testSPPF() {
		Input input = Input.fromString("aad");
		GLLParser parser = ParserFactory.newParser(grammar, input);
		ParseResult result = parser.parse(input, grammar.toGrammarGraph(), "S");
		assertTrue(result.isParseSuccess());
		assertTrue(result.asParseSuccess().getRoot().deepEquals(getSPPF()));
	}
	
	public SPPFNode getSPPF() {
		GrammarGraph grammarGraph = grammar.toGrammarGraph();
		SPPFNodeFactory factory = new SPPFNodeFactory(grammarGraph);
		NonterminalNode node1 = factory.createNonterminalNode(S, 0, 3);
		PackedNode node2 = new PackedNode(grammarGraph.getPackedNodeId(S, a, S), 1, node1);
		TokenSymbolNode node3 = factory.createTokenNode(a, 0, 1);
		NonterminalNode node4 = factory.createNonterminalNode(S, 1, 3);
		IntermediateNode node5 = factory.createIntermediateNode(list(A, S), 1, 2);
		NonterminalNode node6 = factory.createNonterminalNode(A, 1, 2);
		TokenSymbolNode node7 = factory.createTokenNode(a, 1, 1);
		node6.addChild(node7);
		NonterminalNode node8 = factory.createNonterminalNode(S, 2, 2);
		node5.addChild(node6);
		node5.addChild(node8);
		TokenSymbolNode node9 = factory.createTokenNode(d, 2, 1);
		node4.addChild(node5);
		node4.addChild(node9);
		node2.addChild(node3);
		node2.addChild(node4);
		PackedNode node10 = new PackedNode(grammarGraph.getPackedNodeId(S, A, S, d), 2, node1);
		IntermediateNode node11 = factory.createIntermediateNode(list(A, S), 0, 2);
		NonterminalNode node12 = factory.createNonterminalNode(A, 0, 1);
		node12.addChild(node3);
		NonterminalNode node13 = factory.createNonterminalNode(S, 1, 2);
		node13.addChild(node7);
		node13.addChild(node8);
		node11.addChild(node12);
		node11.addChild(node13);
		node10.addChild(node11);
		node10.addChild(node9);
		node1.addChild(node2);
		node1.addChild(node10);
		return node1;
	}
	
}
