package org.jgll.parser;

import static org.junit.Assert.*;

import org.jgll.grammar.Grammar;
import org.jgll.grammar.GrammarSlotRegistry;
import org.jgll.grammar.symbol.Character;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Rule;
import org.jgll.sppf.IntermediateNode;
import org.jgll.sppf.NonterminalNode;
import org.jgll.sppf.PackedNode;
import org.jgll.sppf.SPPFNode;
import org.jgll.sppf.SPPFNodeFactory;
import org.jgll.sppf.TerminalNode;
import org.jgll.util.Input;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * S ::= a S b
 *     | a S
 *     | s
 * 
 * @author Ali Afroozeh
 *
 */
public class DanglingElseGrammar4 {

	Nonterminal S = Nonterminal.withName("S");
	Character s = Character.from('s');
	Character a = Character.from('a');
	Character b = Character.from('b');
	private Grammar grammar;

	@Before
	public void init() {
		
		Grammar.Builder builder = new Grammar.Builder();
		
		Rule rule1 = Rule.builder(S).addSymbols(a, S, b).build();
		builder.addRule(rule1);
		
		Rule rule2 = Rule.builder(S).addSymbols(a, S).build();
		builder.addRule(rule2);
		
		Rule rule3 = Rule.builder(S).addSymbols(s).build();
		builder.addRule(rule3);
		
		grammar = builder.build();
	}
	
	@Test
	public void test() {
		Input input = Input.fromString("aasb");
		GLLParser parser = ParserFactory.newParser();
		ParseResult result = parser.parse(input, grammar, "S");
		assertTrue(result.isParseSuccess());
		assertTrue(result.asParseSuccess().getRoot().deepEquals(getExpectedSPPF(parser.getRegistry())));
	}
	
	private SPPFNode getExpectedSPPF(GrammarSlotRegistry registry) {
		SPPFNodeFactory factory = new SPPFNodeFactory(registry);
		NonterminalNode node1 = factory.createNonterminalNode("S", 0, 0, 4).init();
		PackedNode node2 = factory.createPackedNode("S ::= a S b .", 3, node1);
		IntermediateNode node3 = factory.createIntermediateNode("S ::= a S . b", 0, 3).init();
		PackedNode node4 = factory.createPackedNode("S ::= a S . b", 1, node3);
		TerminalNode node5 = factory.createTerminalNode("a", 0, 1);
		NonterminalNode node6 = factory.createNonterminalNode("S", 0, 1, 3).init();
		PackedNode node7 = factory.createPackedNode("S ::= a S .", 2, node6);
		TerminalNode node8 = factory.createTerminalNode("a", 1, 2);
		NonterminalNode node9 = factory.createNonterminalNode("S", 0, 2, 3).init();
		PackedNode node10 = factory.createPackedNode("S ::= s .", 3, node9);
		TerminalNode node11 = factory.createTerminalNode("s", 2, 3);
		node10.addChild(node11);
		node9.addChild(node10);
		node7.addChild(node8);
		node7.addChild(node9);
		node6.addChild(node7);
		node4.addChild(node5);
		node4.addChild(node6);
		node3.addChild(node4);
		TerminalNode node12 = factory.createTerminalNode("b", 3, 4);
		node2.addChild(node3);
		node2.addChild(node12);
		PackedNode node13 = factory.createPackedNode("S ::= a S .", 1, node1);
		NonterminalNode node15 = factory.createNonterminalNode("S", 0, 1, 4).init();
		PackedNode node16 = factory.createPackedNode("S ::= a S b .", 3, node15);
		IntermediateNode node17 = factory.createIntermediateNode("S ::= a S . b", 1, 3).init();
		PackedNode node18 = factory.createPackedNode("S ::= a S . b", 2, node17);
		node18.addChild(node8);
		node18.addChild(node9);
		node17.addChild(node18);
		node16.addChild(node17);
		node16.addChild(node12);
		node15.addChild(node16);
		node13.addChild(node5);
		node13.addChild(node15);
		node1.addChild(node2);
		node1.addChild(node13);
		return node1;
	}

}
