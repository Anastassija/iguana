package org.jgll.grammar.basic;

import static org.jgll.util.CollectionsUtil.*;
import static org.junit.Assert.*;

import org.jgll.grammar.Grammar;
import org.jgll.grammar.GrammarGraph;
import org.jgll.grammar.symbol.Character;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Rule;
import org.jgll.parser.GLLParser;
import org.jgll.parser.ParseResult;
import org.jgll.parser.ParserFactory;
import org.jgll.sppf.NonterminalSymbolNode;
import org.jgll.sppf.SPPFNode;
import org.jgll.sppf.TokenSymbolNode;
import org.jgll.util.Input;
import org.junit.Before;
import org.junit.Test;

/**
 * A ::= B C
 * B ::= 'b'
 * C ::= 'c'
 * 
 * @author Ali Afroozeh
 *
 */
public class Test5 {

	private GrammarGraph grammar;

	private Nonterminal A = new Nonterminal("A");
	private Nonterminal B = new Nonterminal("B");
	private Nonterminal C = new Nonterminal("C");
	private Character b = Character.from('b');
	private Character c = Character.from('c');
	
	@Before
	public void init() {
		Rule r1 = new Rule(A, list(B, C));
		Rule r2 = new Rule(B, list(b));
		Rule r3 = new Rule(C, list(c));
		
		grammar = new Grammar().addRule(r1).addRule(r2).addRule(r3).toGrammarGraph();
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
	public void testParser() {
		Input input = Input.fromString("bc");
		GLLParser parser = ParserFactory.newParser(grammar, input);
		ParseResult result = parser.parse(input, grammar, "A");
		assertTrue(result.isParseSuccess());
		assertTrue(result.asParseSuccess().getSPPFNode().deepEquals(getSPPF()));
	}
	
	private SPPFNode getSPPF() {
		NonterminalSymbolNode node1 = new NonterminalSymbolNode(grammar.getNonterminalId(A), 1, 0, 2);
		NonterminalSymbolNode node2 = new NonterminalSymbolNode(grammar.getNonterminalId(B), 1, 0, 1);
		TokenSymbolNode node3 = new TokenSymbolNode(grammar.getRegularExpressionId(b), 0, 1);
		node2.addChild(node3);
		NonterminalSymbolNode node4 = new NonterminalSymbolNode(grammar.getNonterminalId(C), 1, 1, 2);
		TokenSymbolNode node5 = new TokenSymbolNode(grammar.getRegularExpressionId(c), 1, 1);
		node4.addChild(node5);
		node1.addChild(node2);
		node1.addChild(node4);		
		return node1;
	}
	
}