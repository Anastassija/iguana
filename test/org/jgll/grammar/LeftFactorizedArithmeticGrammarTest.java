package org.jgll.grammar;

import static org.jgll.util.CollectionsUtil.*;
import static org.junit.Assert.*;

import org.jgll.grammar.slot.factory.FirstFollowSetGrammarSlotFactory;
import org.jgll.grammar.slot.factory.GrammarSlotFactory;
import org.jgll.grammar.symbol.Character;
import org.jgll.grammar.symbol.EOF;
import org.jgll.grammar.symbol.Epsilon;
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
 * 	E  ::= T E1
 * 	E1 ::= + T E1 | epsilon
 *  T  ::= F T1
 *  T1 ::= * F T1 |  epsilon
 *  F  ::= (E) | a
 *  
 */
public class LeftFactorizedArithmeticGrammarTest {
	
	private Grammar grammar;

	Nonterminal E = new Nonterminal("E");
	Nonterminal T = new Nonterminal("T");
	Nonterminal E1 = new Nonterminal("E1");
	Nonterminal F = new Nonterminal("F");
	Nonterminal T1 = new Nonterminal("T1");
	Character plus = new Character('+');
	Character star = new Character('*');
	Character a = new Character('a');
	Character openPar = new Character('(');
	Character closePar = new Character(')');

	@Before
	public void createGrammar() {

		GrammarSlotFactory factory = new FirstFollowSetGrammarSlotFactory();
		GrammarBuilder builder = new GrammarBuilder("LeftFactorizedArithmeticExpressions", factory);
		
		Rule r1 = new Rule(E, list(T, E1));
		Rule r2 = new Rule(E1, list(plus, T, E1));
		Rule r3 = new Rule(E1);
		Rule r4 = new Rule(T, list(F, T1));
		Rule r5 = new Rule(T1, list(star, F, T1));
		Rule r6 = new Rule(T1);
		Rule r7 = new Rule(F, list(openPar, E, closePar));
		Rule r8 = new Rule(F, list(a));
		
		builder.addRule(r1).addRule(r2).addRule(r3).addRule(r4).addRule(r5).addRule(r6).addRule(r7).addRule(r8);
		grammar = builder.build();
	}
	
	@Test
	public void testFirstSets() {
		assertEquals(set(openPar, a), grammar.getFirstSet(E));
		assertEquals(set(plus, Epsilon.getInstance()), grammar.getFirstSet(E1));
		assertEquals(set(star, Epsilon.getInstance()), grammar.getFirstSet(T1));
		assertEquals(set(openPar, a), grammar.getFirstSet(T));
		assertEquals(set(openPar, a), grammar.getFirstSet(F));
	}
	
	public void testFollowSets() {
		assertEquals(set(closePar, EOF.getInstance()), grammar.getFollowSet(E));
		assertEquals(set(closePar, EOF.getInstance()), grammar.getFollowSet(E1));
		assertEquals(set(plus, closePar, EOF.getInstance()), grammar.getFollowSet(T1));
		assertEquals(set(plus, closePar, EOF.getInstance()), grammar.getFollowSet(T));
		assertEquals(set(plus, star, closePar, EOF.getInstance()), grammar.getFollowSet(F));
	}
	
	@Test
	public void test() throws ParseError {
		Input input = Input.fromString("a+a*a+a");
		GLLParser parser = ParserFactory.newParser(grammar, input);
		NonterminalSymbolNode sppf = parser.parse(input, grammar, "E");
		assertTrue(sppf.deepEquals(getSPPF()));
	}
	
	private SPPFNode getSPPF() {
		NonterminalSymbolNode node1 = new NonterminalSymbolNode(grammar.getHeadGrammarSlot("E"), 0, 7);
		NonterminalSymbolNode node2 = new NonterminalSymbolNode(grammar.getHeadGrammarSlot("T"), 0, 1);
		NonterminalSymbolNode node3 = new NonterminalSymbolNode(grammar.getHeadGrammarSlot("F"), 0, 1);
		TokenSymbolNode node4 = new TokenSymbolNode(6, 0, 1);
		node3.addChild(node4);
		NonterminalSymbolNode node5 = new NonterminalSymbolNode(grammar.getHeadGrammarSlot("T1"), 1, 1);
		node2.addChild(node3);
		node2.addChild(node5);
		NonterminalSymbolNode node6 = new NonterminalSymbolNode(grammar.getHeadGrammarSlot("E1"), 1, 7);
		IntermediateNode node7 = new IntermediateNode(grammar.getGrammarSlotByName("E1 ::= [+] T . E1"), 1, 5);
		TokenSymbolNode node8 = new TokenSymbolNode(2, 1, 1);
		NonterminalSymbolNode node9 = new NonterminalSymbolNode(grammar.getHeadGrammarSlot("T"), 2, 5);
		NonterminalSymbolNode node10 = new NonterminalSymbolNode(grammar.getHeadGrammarSlot("F"), 2, 3);
		TokenSymbolNode node11 = new TokenSymbolNode(6, 2, 1);
		node10.addChild(node11);
		NonterminalSymbolNode node12 = new NonterminalSymbolNode(grammar.getHeadGrammarSlot("T1"), 3, 5);
		IntermediateNode node13 = new IntermediateNode(grammar.getGrammarSlotByName("T1 ::= [*] F . T1"), 3, 5);
		TokenSymbolNode node14 = new TokenSymbolNode(3, 3, 1);
		NonterminalSymbolNode node15 = new NonterminalSymbolNode(grammar.getHeadGrammarSlot("F"), 4, 5);
		TokenSymbolNode node16 = new TokenSymbolNode(6, 4, 1);
		node15.addChild(node16);
		node13.addChild(node14);
		node13.addChild(node15);
		NonterminalSymbolNode node17 = new NonterminalSymbolNode(grammar.getHeadGrammarSlot("T1"), 5, 5);
		node12.addChild(node13);
		node12.addChild(node17);
		node9.addChild(node10);
		node9.addChild(node12);
		node7.addChild(node8);
		node7.addChild(node9);
		NonterminalSymbolNode node18 = new NonterminalSymbolNode(grammar.getHeadGrammarSlot("E1"), 5, 7);
		IntermediateNode node19 = new IntermediateNode(grammar.getGrammarSlotByName("E1 ::= [+] T . E1"), 5, 7);
		TokenSymbolNode node20 = new TokenSymbolNode(2, 5, 1);
		NonterminalSymbolNode node21 = new NonterminalSymbolNode(grammar.getHeadGrammarSlot("T"), 6, 7);
		NonterminalSymbolNode node22 = new NonterminalSymbolNode(grammar.getHeadGrammarSlot("F"), 6, 7);
		TokenSymbolNode node23 = new TokenSymbolNode(6, 6, 1);
		node22.addChild(node23);
		NonterminalSymbolNode node24 = new NonterminalSymbolNode(grammar.getHeadGrammarSlot("T1"), 7, 7);
		node21.addChild(node22);
		node21.addChild(node24);
		node19.addChild(node20);
		node19.addChild(node21);
		NonterminalSymbolNode node25 = new NonterminalSymbolNode(grammar.getHeadGrammarSlot("E1"), 7, 7);
		node18.addChild(node19);
		node18.addChild(node25);
		node6.addChild(node7);
		node6.addChild(node18);
		node1.addChild(node2);
		node1.addChild(node6);
		return node1;
	}

}
