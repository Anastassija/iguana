package org.jgll.grammar.basic;

import static org.jgll.util.CollectionsUtil.*;
import static org.junit.Assert.*;

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
 * A ::= 'a' B 'c'
 *     | C
 *     
 * B ::= 'b'
 * 
 * C ::= 'a' C
 *     | 'c'
 * 
 * @author Ali Afroozeh
 *
 */
public class Test4 {

	private Grammar grammar;

	private Nonterminal A = new Nonterminal("A");
	private Nonterminal B = new Nonterminal("B");
	private Nonterminal C = new Nonterminal("C");
	private Character a = new Character('a');
	private Character b = new Character('b');
	private Character c = new Character('c');
	
	@Before
	public void init() {
		
		Rule r1 = new Rule(A, list(a, B, c));
		Rule r2 = new Rule(A, list(C));
		Rule r3 = new Rule(B, list(b));
		Rule r4 = new Rule(C, list(a, C));
		Rule r5 = new Rule(C, list(c));
		
		GrammarSlotFactory factory = new FirstFollowSetGrammarSlotFactory();
		grammar = new GrammarBuilder("test4", factory).addRule(r1).addRule(r2).addRule(r3).addRule(r4).addRule(r5).build();
	}
	
	@Test
	public void testNullable() {
		assertFalse(grammar.getHeadGrammarSlot("A").isNullable());
		assertFalse(grammar.getHeadGrammarSlot("B").isNullable());
		assertFalse(grammar.getHeadGrammarSlot("C").isNullable());
	}
	
	@Test
	public void testLL1() {
		assertTrue(grammar.isLL1SubGrammar(A));
		assertTrue(grammar.isLL1SubGrammar(B));
		assertTrue(grammar.isLL1SubGrammar(C));
	}
	
	@Test
	public void testParser1() throws ParseError {
		Input input = Input.fromString("abc");
		GLLParser parser = ParserFactory.newParser(grammar, input);
		NonterminalSymbolNode sppf1 = parser.parse(input, grammar, "A");
		assertTrue(sppf1.deepEquals(getSPPF1()));
	}
	
	@Test
	public void testParser2() throws ParseError {
		Input input = Input.fromString("aaaac");
		GLLParser parser = ParserFactory.newParser(grammar, input);
		NonterminalSymbolNode sppf2 = parser.parse(input, grammar, "A");
		assertTrue(sppf2.deepEquals(getSPPF2()));
	}
	
	private SPPFNode getSPPF1() {
		NonterminalSymbolNode node1 = new NonterminalSymbolNode(grammar.getNonterminalId(A), 2, 0, 3);
		IntermediateNode node2 = new IntermediateNode(grammar.getIntermediateNodeId(a, B), 0, 2);
		TokenSymbolNode node3 = new TokenSymbolNode(grammar.getRegularExpressionId(a), 0, 1);
		NonterminalSymbolNode node4 = new NonterminalSymbolNode(grammar.getNonterminalId(B), 1, 1, 2);
		TokenSymbolNode node5 = new TokenSymbolNode(grammar.getRegularExpressionId(b), 1, 1);
		node4.addChild(node5);
		node2.addChild(node3);
		node2.addChild(node4);
		TokenSymbolNode node6 = new TokenSymbolNode(grammar.getRegularExpressionId(c), 2, 1);
		node1.addChild(node2);
		node1.addChild(node6);
		return node1;
	}
	
	private SPPFNode getSPPF2() {
		NonterminalSymbolNode node1 = new NonterminalSymbolNode(grammar.getNonterminalId(A), 2, 0, 5);
		NonterminalSymbolNode node2 = new NonterminalSymbolNode(grammar.getNonterminalId(C), 2, 0, 5);
		TokenSymbolNode node3 = new TokenSymbolNode(grammar.getRegularExpressionId(a), 0, 1);
		NonterminalSymbolNode node4 = new NonterminalSymbolNode(grammar.getNonterminalId(C), 2, 1, 5);
		TokenSymbolNode node5 = new TokenSymbolNode(grammar.getRegularExpressionId(a), 1, 1);
		NonterminalSymbolNode node6 = new NonterminalSymbolNode(grammar.getNonterminalId(C), 2, 2, 5);
		TokenSymbolNode node7 = new TokenSymbolNode(grammar.getRegularExpressionId(a), 2, 1);
		NonterminalSymbolNode node8 = new NonterminalSymbolNode(grammar.getNonterminalId(C), 2, 3, 5);
		TokenSymbolNode node9 = new TokenSymbolNode(grammar.getRegularExpressionId(a), 3, 1);
		NonterminalSymbolNode node10 = new NonterminalSymbolNode(grammar.getNonterminalId(C), 2, 4, 5);
		TokenSymbolNode node11 = new TokenSymbolNode(grammar.getRegularExpressionId(c), 4, 1);
		node10.addChild(node11);
		node8.addChild(node9);
		node8.addChild(node10);
		node6.addChild(node7);
		node6.addChild(node8);
		node4.addChild(node5);
		node4.addChild(node6);
		node2.addChild(node3);
		node2.addChild(node4);
		node1.addChild(node2);
		return node1;
	}
	
}
	