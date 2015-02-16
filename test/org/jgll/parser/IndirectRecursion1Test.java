package org.jgll.parser;

import static org.jgll.util.CollectionsUtil.*;
import static org.junit.Assert.*;

import org.jgll.grammar.Grammar;
import org.jgll.grammar.GrammarGraph;
import org.jgll.grammar.symbol.Character;
import org.jgll.grammar.symbol.EOF;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Rule;
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
 * A ::= B C | a
 * 
 * B ::= A | b
 * 
 * C ::= c
 * 
 * 
 * @author Ali Afroozeh
 *
 */
public class IndirectRecursion1Test {

	private Grammar grammar;

	private Nonterminal A = Nonterminal.withName("A");
	private Nonterminal B = Nonterminal.withName("B");
	private Nonterminal C = Nonterminal.withName("C");
	private Character a = Character.from('a');
	private Character b = Character.from('b');
	private Character c = Character.from('c');

	@Before
	public void createGrammar() {
		Rule r1 = Rule.withHead(A).addSymbols(B, C).build();
		Rule r2 = Rule.withHead(A).addSymbols(a).build();
		Rule r3 = Rule.withHead(B).addSymbols(A).build();
		Rule r4 = Rule.withHead(B).addSymbols(b).build();
		Rule r5 = Rule.withHead(C).addSymbols(c).build();
		
		grammar = new Grammar.Builder().addRule(r1).addRule(r2).addRule(r3)
								  	   .addRule(r4).addRule(r5).build();
	}
	
	
	@Test
	public void testFirstFollowSets() {
		assertEquals(set(a, b), grammar.getFirstSet(A));
		assertEquals(set(a, b), grammar.getFirstSet(B));
		assertEquals(set(c), grammar.getFirstSet(C));
		assertEquals(set(c, EOF.getInstance()), grammar.getFollowSet(A));
		assertEquals(set(c, EOF.getInstance()), grammar.getFollowSet(B));
	}
	
	@Test
	public void testParser() {
		Input input = Input.fromString("bc");
		GLLParser parser = ParserFactory.getParser(Configuration.DEFAULT, input, grammar);
		ParseResult result = parser.parse(input, grammar, Nonterminal.withName("A"));
		assertTrue(result.isParseSuccess());
		assertTrue(result.asParseSuccess().getRoot().deepEquals(expectedSPPF(parser.getGrammarGraph())));
	}
	
	private SPPFNode expectedSPPF(GrammarGraph registry) {
		SPPFNodeFactory factory = new SPPFNodeFactory(registry);
		NonterminalNode node1 = factory.createNonterminalNode("A", 0, 0, 2);
		PackedNode node2 = factory.createPackedNode("A ::= B C .", 1, node1);
		NonterminalNode node3 = factory.createNonterminalNode("B", 0, 0, 1);
		PackedNode node4 = factory.createPackedNode("B ::= b .", 1, node3);
		TerminalNode node5 = factory.createTerminalNode("b", 0, 1);
		node4.addChild(node5);
		node3.addChild(node4);
		NonterminalNode node6 = factory.createNonterminalNode("C", 0, 1, 2);
		PackedNode node7 = factory.createPackedNode("C ::= c .", 2, node6);
		TerminalNode node8 = factory.createTerminalNode("c", 1, 2);
		node7.addChild(node8);
		node6.addChild(node7);
		node2.addChild(node3);
		node2.addChild(node6);
		node1.addChild(node2);
		return node1;
	}

}