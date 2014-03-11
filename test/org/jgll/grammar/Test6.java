package org.jgll.grammar;

import static org.jgll.util.CollectionsUtil.*;

import org.jgll.grammar.slot.factory.FirstFollowSetGrammarSlotFactory;
import org.jgll.grammar.slot.factory.GrammarSlotFactory;
import org.jgll.grammar.symbol.Character;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Rule;
import org.jgll.parser.GLLParser;
import org.jgll.parser.ParseError;
import org.jgll.parser.ParserFactory;
import org.jgll.sppf.NonterminalSymbolNode;
import org.jgll.util.Input;
import org.jgll.util.Visualization;
import org.junit.Before;
import org.junit.Test;

/**
 * S ::= A B C
 *     | A B D
 *     
 * A ::= 'a'
 * B ::= 'b'§
 * C ::= 'c'
 * D ::= 'c'
 * 
 * @author Ali Afroozeh
 *
 */
public class Test6 {

	private Grammar grammar;
	
	private Nonterminal S = new Nonterminal("S");
	private Nonterminal A = new Nonterminal("A");
	private Nonterminal B = new Nonterminal("B");
	private Nonterminal C = new Nonterminal("C");
	private Nonterminal D = new Nonterminal("D");
	
	private Character a = new Character('a');
	private Character b = new Character('b');
	private Character c = new Character('c');

	
	@Before
	public void init() {
		Rule r1 = new Rule(S, list(A, B, C));
		Rule r2 = new Rule(S, list(A, B, D));
		Rule r3 = new Rule(A, list(a));
		Rule r4 = new Rule(B, list(b));
		Rule r5 = new Rule(C, list(c));
		Rule r6 = new Rule(D, list(c));
		
		GrammarSlotFactory factory = new FirstFollowSetGrammarSlotFactory();
		grammar = new GrammarBuilder("test5", factory).addRule(r1).addRule(r2).addRule(r3).addRule(r4).addRule(r5).addRule(r6).build();
	}
	
	@Test
	public void test1() throws ParseError {
		Input input = Input.fromString("abc");
		GLLParser parser = ParserFactory.newParser(grammar, input);
		NonterminalSymbolNode sppf1 = parser.parse(input, grammar, "S");
		Visualization.generateSPPFGraph("/Users/ali/output", sppf1, grammar, input);
	}
}
	