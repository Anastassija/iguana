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
import org.jgll.sppf.TerminalSymbolNode;
import org.jgll.util.Input;
import org.jgll.util.generator.CompilationUtil;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * A ::= 'a' 'b' 'c'
 * 
 * @author Ali Afroozeh
 */
public class Test4 {

	private Grammar grammar;

	private Nonterminal A = Nonterminal.withName("A");
	private Character a = Character.from('a');
	private Character b = Character.from('b');
	private Character c = Character.from('c');
	
	@Before
	public void init() {
		Rule r1 = new Rule(A, list(a, b, c));
		grammar = new Grammar.Builder().addRule(r1).build();
	}
	
	@Test
	public void testNullable() {
		assertFalse(grammar.isNullable(A));
	}
	
	@Test
	public void testParser() {
		Input input = Input.fromString("abc");
		GLLParser parser = ParserFactory.newParser(grammar, input);
		ParseResult result = parser.parse(input, grammar.toGrammarGraph(), "A");
		assertTrue(result.asParseSuccess().getRoot().deepEquals(expectedSPPF()));
	}
	
	@Test
	public void testGenerated() {
		StringWriter writer = new StringWriter();
		grammar.toGrammarGraph().generate(new PrintWriter(writer));
		GLLParser parser = CompilationUtil.getParser(writer.toString());
		ParseResult result = parser.parse(Input.fromString("abc"), grammar.toGrammarGraph(), "A");
    	assertTrue(result.isParseSuccess());
		assertTrue(result.asParseSuccess().getRoot().deepEquals(expectedSPPF()));
	}
	
	private SPPFNode expectedSPPF() {
		SPPFNodeFactory factory = new SPPFNodeFactory(grammar.toGrammarGraph());
		NonterminalNode node1 = factory.createNonterminalNode("A", 0, 3).init();
		PackedNode node2 = factory.createPackedNode("A ::= a b c .", 2, node1);
		IntermediateNode node3 = factory.createIntermediateNode("A ::= a b . c", 0, 2).init();
		PackedNode node4 = factory.createPackedNode("A ::= a b . c", 1, node3);
		TerminalSymbolNode node5 = factory.createTokenNode("a", 0, 1);
		TerminalSymbolNode node6 = factory.createTokenNode("b", 1, 1);
		node4.addChild(node5);
		node4.addChild(node6);
		node3.addChild(node4);
		TerminalSymbolNode node7 = factory.createTokenNode("c", 2, 1);
		node2.addChild(node3);
		node2.addChild(node7);
		node1.addChild(node2);
		return node1;
	}

}