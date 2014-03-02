package org.jgll.grammar;

import static org.jgll.util.CollectionsUtil.*;
import static org.junit.Assert.*;

import java.util.Set;

import org.jgll.grammar.slot.HeadGrammarSlot;
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
 * 
 * A ::= B A d | a
 * 
 * B ::= epsilon | b
 * 
 * 
 * @author Ali Afroozeh
 *
 */
public class IndirectRecursion2Test {

	private GrammarBuilder builder;
	private Grammar grammar;

	@Before
	public void init() {
		Nonterminal A = new Nonterminal("A");
		Nonterminal B = new Nonterminal("B");
		Rule r1 = new Rule(A, list(B, A, new Character('d')));
		Rule r2 = new Rule(A, list(new Character('a')));
		Rule r3 = new Rule(B);
		Rule r4 = new Rule(B, list(new Character('b')));

		GrammarSlotFactory factory = new FirstFollowSetGrammarSlotFactory();
		builder = new GrammarBuilder("IndirectRecursion", factory).addRule(r1)
													     		  .addRule(r2)
													     		  .addRule(r3)
													     		  .addRule(r4);
		grammar = builder.build();
	}
	
	@Test
	public void testNullable() {
		assertFalse(grammar.getHeadGrammarSlot("A").isNullable());
		assertTrue(grammar.getHeadGrammarSlot("B").isNullable());
	}
	
	@Test
	public void testParser() throws ParseError {
		Input input = Input.fromString("ad");
		GLLParser parser = ParserFactory.newParser(grammar, input);
		NonterminalSymbolNode sppf = parser.parse(input, grammar, "A");
		assertTrue(sppf.deepEquals(expectedSPPF()));
	}
	
	@Test
	public void testReachabilityGraph() {
		Set<HeadGrammarSlot> set = builder.getDirectReachableNonterminals("A");
		assertTrue(set.contains(grammar.getHeadGrammarSlot("A")));
		assertTrue(set.contains(grammar.getHeadGrammarSlot("B")));
	}
	
	private SPPFNode expectedSPPF() {		
		NonterminalSymbolNode node1 = new NonterminalSymbolNode(grammar.getHeadGrammarSlot("A"), 0, 2);
		IntermediateNode node2 = new IntermediateNode(grammar.getGrammarSlotByName("A ::= B A . [d]"), 0, 1);
		IntermediateNode node3 = new IntermediateNode(grammar.getGrammarSlotByName("A ::= B . A [d]"), 0, 0);
		NonterminalSymbolNode node4 = new NonterminalSymbolNode(grammar.getHeadGrammarSlot("B"), 0, 0);
		node3.addChild(node4);
		NonterminalSymbolNode node5 = new NonterminalSymbolNode(grammar.getHeadGrammarSlot("A"), 0, 1);
		TokenSymbolNode node6 = new TokenSymbolNode(3, 0, 1);
		node5.addChild(node6);
		node2.addChild(node3);
		node2.addChild(node5);
		TokenSymbolNode node7 = new TokenSymbolNode(2, 1, 1);
		node1.addChild(node2);
		node1.addChild(node7);
		return node1;
	}

}