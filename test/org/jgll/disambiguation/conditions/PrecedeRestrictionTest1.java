package org.jgll.disambiguation.conditions;

import static org.junit.Assert.*;

import org.jgll.grammar.Grammar;
import org.jgll.grammar.GrammarRegistry;
import org.jgll.grammar.condition.RegularExpressionCondition;
import org.jgll.grammar.symbol.Character;
import org.jgll.grammar.symbol.Keyword;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Opt;
import org.jgll.grammar.symbol.Plus;
import org.jgll.grammar.symbol.CharacterRange;
import org.jgll.grammar.symbol.Rule;
import org.jgll.parser.GLLParser;
import org.jgll.parser.ParseResult;
import org.jgll.parser.ParserFactory;
import org.jgll.sppf.NonterminalNode;
import org.jgll.sppf.PackedNode;
import org.jgll.sppf.SPPFNode;
import org.jgll.sppf.SPPFNodeFactory;
import org.jgll.sppf.TerminalNode;
import org.jgll.util.Configuration;
import org.jgll.util.Input;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * S ::= "for" L? Id | "forall"
 * 
 * Id ::= [a-z] !<< [a-z]+ !>> [a-z]
 * 
 * L ::= " "
 * 
 * @author Ali Afroozeh
 * 
 */
public class PrecedeRestrictionTest1 {

	private Grammar grammar;
	
	private Nonterminal S = Nonterminal.withName("S");
	private Keyword forr = Keyword.from("for");
	private Keyword forall = Keyword.from("forall");
	private Nonterminal L = Nonterminal.withName("L");
	private Nonterminal Id = Nonterminal.withName("Id");
	private Character ws = Character.from(' ');
	private CharacterRange az = CharacterRange.in('a', 'z');
	
	private Plus AZPlus = Plus.builder(az).addPostCondition(RegularExpressionCondition.notFollow(az))
			                              .addPreCondition(RegularExpressionCondition.notPrecede(az)).build();

	@Before
	public void createParser() {
		
		Rule r1 = Rule.builder(S).addSymbols(forr, Opt.from(L), Id).build();

		Rule r2 = Rule.builder(S).addSymbol(forall).build();

		Rule r3 = Rule.builder(Id).addSymbol(AZPlus).build();

		Rule r4 = Rule.builder(L).addSymbol(ws).build();

		grammar = Grammar.builder().addRules(r1, r2, r3, r4, forr.toRule(), forall.toRule()).build();
	}

	@Test
	public void test() {
		Input input = Input.fromString("forall");
		GLLParser parser = ParserFactory.getParser(Configuration.DEFAULT, input, grammar);
		ParseResult result = parser.parse(input, grammar, Nonterminal.withName("S"));
		assertTrue(result.isParseSuccess());
		assertTrue(result.asParseSuccess().getRoot().deepEquals(getExpectedSPPF(parser.getRegistry())));
	}

	private SPPFNode getExpectedSPPF(GrammarRegistry registry) {
		SPPFNodeFactory factory = new SPPFNodeFactory(registry);
		NonterminalNode node1 = factory.createNonterminalNode("S", 0, 6);
		PackedNode node2 = factory.createPackedNode("S ::= f o r a l l .", 0, node1);
		TerminalNode node3 = factory.createTerminalNode("f o r a l l", 0, 6);
		node2.addChild(node3);
		node1.addChild(node2);		
		return node1;
	}

}
