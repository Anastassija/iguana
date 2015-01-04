package org.jgll.parser.basic;

import static org.jgll.util.Configurations.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.jgll.grammar.Grammar;
import org.jgll.grammar.GrammarRegistry;
import org.jgll.grammar.symbol.Character;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Rule;
import org.jgll.parser.GLLParser;
import org.jgll.parser.ParseResult;
import org.jgll.parser.ParserFactory;
import org.jgll.sppf.NonterminalNode;
import org.jgll.sppf.PackedNode;
import org.jgll.sppf.SPPFNode;
import org.jgll.sppf.SPPFNodeFactory;
import org.jgll.sppf.TerminalNode;
import org.jgll.util.Input;
import org.jgll.util.function.ExpectedSPPF;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * A ::= B
 * B ::= 'b'
 * 
 * @author Ali Afroozeh
 *
 */
@RunWith(Parameterized.class)
public class Test5 {

	private Grammar grammar;

	private Nonterminal A = Nonterminal.withName("A");
	private Nonterminal B = Nonterminal.withName("B");
	private Character b = Character.from('b');
	
	private GLLParser parser;
	
	private Input input;
	
	private ExpectedSPPF expectedSPPF;
	
	public Test5(GLLParser parser, Input input, ExpectedSPPF expectedSPPF) {
		this.parser = parser;
		this.input = input;
		this.expectedSPPF = expectedSPPF;
	}

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
        		{ ParserFactory.getParser(DEFAULT),  Input.fromString("b"), (ExpectedSPPF) Test5::expectedSPPF },
        		{ ParserFactory.getParser(CONFIG_1), Input.fromString("b"), (ExpectedSPPF) Test5::expectedSPPF },
        		{ ParserFactory.getParser(CONFIG_2), Input.fromString("b"), (ExpectedSPPF) Test5::expectedSPPF },
        		{ ParserFactory.getParser(CONFIG_3), Input.fromString("b"), (ExpectedSPPF) Test5::expectedSPPF }
           });
    }
	
	@Before
	public void init() {
		Rule r1 = Rule.builder(A).addSymbols(B).build();
		Rule r2 = Rule.builder(B).addSymbol(b).build();
		grammar = new Grammar.Builder().addRule(r1).addRule(r2).build();
	}
	
	@Test
	public void testNullable() {
		assertFalse(grammar.isNullable(A));
		assertFalse(grammar.isNullable(B));
	}
	
	@Test
	public void testLL1() {
//		assertTrue(grammarGraph.isLL1SubGrammar(A));
//		assertTrue(grammarGraph.isLL1SubGrammar(B));
//		assertTrue(grammarGraph.isLL1SubGrammar(C));
	}
	
	@Test
	public void testParser() {
		ParseResult result = parser.parse(input, grammar, "A");
		assertTrue(result.isParseSuccess());
		assertTrue(result.asParseSuccess().getRoot().deepEquals(expectedSPPF.get(parser.getRegistry())));
	}
	
	private static SPPFNode expectedSPPF(GrammarRegistry registry) {
		SPPFNodeFactory factory = new SPPFNodeFactory(registry);
		NonterminalNode node1 = factory.createNonterminalNode("A", 0, 0, 1).init();
		PackedNode node2 = factory.createPackedNode("A ::= B .", 1, node1);
		NonterminalNode node3 = factory.createNonterminalNode("B", 0, 0, 1).init();
		PackedNode node4 = factory.createPackedNode("B ::= b .", 1, node3);
		TerminalNode node5 = factory.createTerminalNode("b", 0, 1);
		node4.addChild(node5);
		node3.addChild(node4);
		node2.addChild(node3);
		node1.addChild(node2);
		return node1;
	}
	
}