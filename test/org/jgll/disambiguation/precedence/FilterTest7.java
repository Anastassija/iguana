package org.jgll.disambiguation.precedence;

import static org.jgll.util.CollectionsUtil.*;
import static org.junit.Assert.*;

import org.jgll.grammar.Grammar;
import org.jgll.grammar.GrammarGraph;
import org.jgll.grammar.precedence.OperatorPrecedence;
import org.jgll.grammar.symbol.Character;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Rule;
import org.jgll.parser.GLLParser;
import org.jgll.parser.ParseResult;
import org.jgll.parser.ParserFactory;
import org.jgll.sppf.IntermediateNode;
import org.jgll.sppf.ListSymbolNode;
import org.jgll.sppf.NonterminalNode;
import org.jgll.sppf.SPPFNodeFactory;
import org.jgll.sppf.TokenSymbolNode;
import org.jgll.util.Input;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * E ::= EPlus E     (non-assoc)
 *     > E + E	  (left)
 *     | a
 * 
 * EPlus ::= EPlus E
 *      | E
 * 
 * @author Ali Afroozeh
 *
 */
public class FilterTest7 {

	private GLLParser parser;
	
	private Nonterminal E = Nonterminal.withName("E");
	private Nonterminal EPlus = new Nonterminal.Builder("EPlus").setEbnfList(true).build();
	private Character a = Character.from('a');
	private Character plus = Character.from('+');

	private Grammar grammar;

	@Before
	public void init() {
		
		Grammar.Builder builder = new Grammar.Builder();
		
		// E ::= EPlus E
		Rule rule1 = new Rule(E, list(EPlus, E));
		builder.addRule(rule1);
		
		// E ::=  E + E
		Rule rule2 = new Rule(E, list(E, plus, E));
		builder.addRule(rule2);
		
		// E ::= a
		Rule rule3 = new Rule(E, list(a));
		builder.addRule(rule3);
		
		// EPlus ::= EPlus E
		Rule rule4 = new Rule(EPlus, list(EPlus, E));
		builder.addRule(rule4);
		
		// EPlus ::= E
		Rule rule5 = new Rule(EPlus, list(E));
		builder.addRule(rule5);
		
		
		OperatorPrecedence operatorPrecedence = new OperatorPrecedence();
		
		// (E ::= .EPlus E, EPlus E) 
		operatorPrecedence.addPrecedencePattern(E, rule1, 0, rule1);
		
		// (E ::= EPlus .E, EPlus E)
		operatorPrecedence.addPrecedencePattern(E, rule1, 1, rule1);
		
		// (E ::= .EPlus E, E + E) 
		operatorPrecedence.addPrecedencePattern(E, rule1, 0, rule2);
		
		// (E ::= EPlus .E, E + E)
		operatorPrecedence.addPrecedencePattern(E, rule1, 1, rule2);
		
		// (E ::= E + .E, E + E)
		operatorPrecedence.addPrecedencePattern(E, rule2, 2, rule2);
		
		operatorPrecedence.addExceptPattern(EPlus, rule4, 1, rule1);
		operatorPrecedence.addExceptPattern(EPlus, rule4, 1, rule2);
		operatorPrecedence.addExceptPattern(EPlus, rule5, 0, rule1);
		operatorPrecedence.addExceptPattern(EPlus, rule5, 0, rule2);
		
		grammar = operatorPrecedence.transform(builder.build());
	}
	
	@Test
	public void test() {
		Input input = Input.fromString("aaa+aaaa+aaaa");
		parser = ParserFactory.newParser(grammar, input);
		ParseResult result = parser.parse(input, grammar.toGrammarGraph(), "E");
		assertTrue(result.isParseSuccess());
		assertTrue(result.asParseSuccess().getSPPFNode().deepEquals(getSPPF()));
	}
	
	private NonterminalNode getSPPF() {
		GrammarGraph grammarGraph = grammar.toGrammarGraph();
		SPPFNodeFactory factory = new SPPFNodeFactory(grammarGraph);
		NonterminalNode node1 = factory.createNonterminalNode(E, 0, 13);
		IntermediateNode node2 = factory.createIntermediateNode(list(E, plus), 0, 9);
		NonterminalNode node3 = factory.createNonterminalNode(E, 0, 8);
		IntermediateNode node4 = factory.createIntermediateNode(list(E, plus), 0, 4);
		NonterminalNode node5 = factory.createNonterminalNode(E, 0, 3);
		ListSymbolNode node6 = factory.createListNode(EPlus, 0, 2);
		ListSymbolNode node7 = factory.createListNode(EPlus, 0, 1);
		NonterminalNode node8 = factory.createNonterminalNode(E, 0, 1);
		TokenSymbolNode node9 = factory.createTokenNode(a, 0, 1);
		node8.addChild(node9);
		node7.addChild(node8);
		NonterminalNode node10 = factory.createNonterminalNode(E, 1, 2);
		TokenSymbolNode node11 = factory.createTokenNode(a, 1, 1);
		node10.addChild(node11);
		node6.addChild(node7);
		node6.addChild(node10);
		NonterminalNode node12 = factory.createNonterminalNode(E, 2, 3);
		TokenSymbolNode node13 = factory.createTokenNode(a, 2, 1);
		node12.addChild(node13);
		node5.addChild(node6);
		node5.addChild(node12);
		TokenSymbolNode node14 = factory.createTokenNode(plus, 3, 1);
		node4.addChild(node5);
		node4.addChild(node14);
		NonterminalNode node15 = factory.createNonterminalNode(E, 4, 8);
		ListSymbolNode node16 = factory.createListNode(EPlus, 4, 7);
		ListSymbolNode node17 = factory.createListNode(EPlus, 4, 6);
		ListSymbolNode node18 = factory.createListNode(EPlus, 4, 5);
		NonterminalNode node19 = factory.createNonterminalNode(E, 4, 5);
		TokenSymbolNode node20 = factory.createTokenNode(a, 4, 1);
		node19.addChild(node20);
		node18.addChild(node19);
		NonterminalNode node21 = factory.createNonterminalNode(E, 5, 6);
		TokenSymbolNode node22 = factory.createTokenNode(a, 5, 1);
		node21.addChild(node22);
		node17.addChild(node18);
		node17.addChild(node21);
		NonterminalNode node23 = factory.createNonterminalNode(E, 6, 7);
		TokenSymbolNode node24 = factory.createTokenNode(a, 6, 1);
		node23.addChild(node24);
		node16.addChild(node17);
		node16.addChild(node23);
		NonterminalNode node25 = factory.createNonterminalNode(E, 7, 8);
		TokenSymbolNode node26 = factory.createTokenNode(a, 7, 1);
		node25.addChild(node26);
		node15.addChild(node16);
		node15.addChild(node25);
		node3.addChild(node4);
		node3.addChild(node15);
		TokenSymbolNode node27 = factory.createTokenNode(plus, 8, 1);
		node2.addChild(node3);
		node2.addChild(node27);
		NonterminalNode node28 = factory.createNonterminalNode(E, 9, 13);
		ListSymbolNode node29 = factory.createListNode(EPlus, 9, 12);
		ListSymbolNode node30 = factory.createListNode(EPlus, 9, 11);
		ListSymbolNode node31 = factory.createListNode(EPlus, 9, 10);
		NonterminalNode node32 = factory.createNonterminalNode(E, 9, 10);
		TokenSymbolNode node33 = factory.createTokenNode(a, 9, 1);
		node32.addChild(node33);
		node31.addChild(node32);
		NonterminalNode node34 = factory.createNonterminalNode(E, 10, 11);
		TokenSymbolNode node35 = factory.createTokenNode(a, 10, 1);
		node34.addChild(node35);
		node30.addChild(node31);
		node30.addChild(node34);
		NonterminalNode node36 = factory.createNonterminalNode(E, 11, 12);
		TokenSymbolNode node37 = factory.createTokenNode(a, 11, 1);
		node36.addChild(node37);
		node29.addChild(node30);
		node29.addChild(node36);
		NonterminalNode node38 = factory.createNonterminalNode(E, 12, 13);
		TokenSymbolNode node39 = factory.createTokenNode(a, 12, 1);
		node38.addChild(node39);
		node28.addChild(node29);
		node28.addChild(node38);
		node1.addChild(node2);
		node1.addChild(node28);
		return node1;
	}

}
