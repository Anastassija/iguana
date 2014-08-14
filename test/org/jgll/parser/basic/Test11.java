package org.jgll.parser.basic;

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
import org.jgll.sppf.IntermediateNode;
import org.jgll.sppf.NonterminalNode;
import org.jgll.sppf.SPPFNode;
import org.jgll.sppf.TokenSymbolNode;
import org.jgll.util.Input;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * A ::=  B 'a' 'c'
 * B ::= 'b'
 * 
 * @author Ali Afroozeh
 */
public class Test11 {

	private Grammar grammar;

	private Nonterminal A = Nonterminal.withName("A");
	private Nonterminal B = Nonterminal.withName("B");
	private Character a = Character.from('a');
	private Character b = Character.from('b');
	private Character c = Character.from('c');
	
	@Before
	public void init() {
		Rule r1 = new Rule(A, list(B, a, c));
		Rule r2 = new Rule(B, list(b));
		
		grammar = new Grammar.Builder().addRule(r1).addRule(r2).build();
	}
	
	@Test
	public void testNullable() {
		assertFalse(grammar.isNullable(A));
		assertFalse(grammar.isNullable(B));
	}
	
	@Test
	public void testParser() {
		Input input = Input.fromString("bac");
		GLLParser parser = ParserFactory.newParser(grammar, input);
		ParseResult result = parser.parse(input, grammar.toGrammarGraph(), "A");
		assertTrue(result.isParseSuccess());
		assertTrue(result.asParseSuccess().getRoot().deepEquals(expectedSPPF()));
	}
	
	private SPPFNode expectedSPPF() {
		GrammarGraph grammarGraph = grammar.toGrammarGraph();
		NonterminalNode node1 = new NonterminalNode(grammarGraph.getNonterminalId(A), 1, 0, 3);
		IntermediateNode node2 = new IntermediateNode(grammarGraph.getIntermediateNodeId(B, a), 0, 2);
		NonterminalNode node3 = new NonterminalNode(grammarGraph.getNonterminalId(B), 1, 0, 1);
		TokenSymbolNode node4 = new TokenSymbolNode(grammarGraph.getRegularExpressionId(b), 0, 1);
		node3.addChild(node4);
		TokenSymbolNode node5 = new TokenSymbolNode(grammarGraph.getRegularExpressionId(a), 1, 1);
		node2.addChild(node3);
		node2.addChild(node5);
		TokenSymbolNode node6 = new TokenSymbolNode(grammarGraph.getRegularExpressionId(c), 2, 1);
		node1.addChild(node2);
		node1.addChild(node6);		
		return node1;
	}
}
