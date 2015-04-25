package org.iguana.grammar.leftfactorization;

import org.iguana.grammar.Grammar;
import org.iguana.grammar.symbol.Character;
import org.iguana.grammar.symbol.Nonterminal;
import org.iguana.grammar.symbol.Rule;
import org.iguana.grammar.transformation.LeftFactorize;
import org.junit.Before;
import org.junit.Test;


public class LeftFactorizationTest2 {

	private Nonterminal E = Nonterminal.withName("E");
	private Character a = Character.from('a');
	private Character star = Character.from('*');
	private Character plus = Character.from('+');
	private Grammar grammar;

	
	@Before
	public void init() {
		Grammar.Builder builder = new Grammar.Builder();
		
		// E ::= E * E
		Rule rule1 = Rule.withHead(E).addSymbols(E, star, E).build();
		builder.addRule(rule1);
		
		
		// E ::= E + E
		Rule rule2 = Rule.withHead(E).addSymbols(E, plus, E).build();
		builder.addRule(rule2);
		
		// E ::= E +
		Rule rule3 = Rule.withHead(E).addSymbols(E, plus).build();
		builder.addRule(rule3);
		
		// E ::= a
		Rule rule4 = Rule.withHead(E).addSymbols(a).build();
		builder.addRule(rule4);
		
		grammar = builder.build();
	}
	
	@Test
	public void test1() {
		LeftFactorize lf = new LeftFactorize();
		lf.transform(grammar);
	}
	
}
