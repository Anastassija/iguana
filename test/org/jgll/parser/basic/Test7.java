package org.jgll.parser.basic;

import static org.jgll.util.CollectionsUtil.*;
import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.jgll.grammar.Grammar;
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
import org.jgll.util.generator.CompilationUtil;
import org.junit.Before;
import org.junit.Test;

/**
 * S ::= 'a' A 'c'
 *     | 'a' A 'b'
 *     
 * A ::= 'a'
 * 
 * @author Ali Afroozeh
 *
 */
public class Test7 {

	private Grammar grammar;

	private Nonterminal S = Nonterminal.withName("S");
	private Nonterminal A = Nonterminal.withName("A");
	private Character a = Character.from('a');
	private Character b = Character.from('b');
	private Character c = Character.from('c');
	
	@Before
	public void init() {
		Rule r1 = new Rule(S, list(a, A, c));
		Rule r2 = new Rule(S, list(a, A, b));
		Rule r3 = new Rule(A, list(a));
		grammar = new Grammar.Builder().addRule(r1).addRule(r2).addRule(r3).build();
	}
	
	@Test
	public void test() {
		Input input = Input.fromString("aab");
		GLLParser parser = ParserFactory.newParser(grammar, input);
		ParseResult result = parser.parse(input, grammar.toGrammarGraph(), "S");
		assertTrue(result.isParseSuccess());
		assertTrue(result.asParseSuccess().getRoot().deepEquals(getSPPF()));
	}	

	@Test
	public void testGenerated() {
		StringWriter writer = new StringWriter();
		grammar.toGrammarGraph().generate(new PrintWriter(writer));
		GLLParser parser = CompilationUtil.getParser(writer.toString());
		ParseResult result = parser.parse(Input.fromString("aab"), grammar.toGrammarGraph(), "S");
    	assertTrue(result.isParseSuccess());
		assertTrue(result.asParseSuccess().getRoot().deepEquals(getSPPF()));
	}
	
	private SPPFNode getSPPF() {
		SPPFNodeFactory factory = new SPPFNodeFactory(grammar.toGrammarGraph());
		NonterminalNode node1 = factory.createNonterminalNode("S", 0, 3).init();
		PackedNode node2 = factory.createPackedNode("S ::= a A b .", 2, node1);
		IntermediateNode node3 = factory.createIntermediateNode("S ::= a A . b", 0, 2).init();
		PackedNode node4 = factory.createPackedNode("S ::= a A . b", 1, node3);
		TokenSymbolNode node5 = factory.createTokenNode("a", 0, 1);
		NonterminalNode node6 = factory.createNonterminalNode("A", 1, 2).init();
		PackedNode node7 = factory.createPackedNode("A ::= a .", 1, node6);
		TokenSymbolNode node8 = factory.createTokenNode("a", 1, 1);
		node7.addChild(node8);
		node6.addChild(node7);
		node4.addChild(node5);
		node4.addChild(node6);
		node3.addChild(node4);
		TokenSymbolNode node9 = factory.createTokenNode("b", 2, 1);
		node2.addChild(node3);
		node2.addChild(node9);
		node1.addChild(node2);
		return node1;
	}
}
	