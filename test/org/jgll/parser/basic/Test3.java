package org.jgll.parser.basic;

import static org.jgll.util.Configurations.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

import org.jgll.AbstractParserTest;
import org.jgll.grammar.Grammar;
import org.jgll.grammar.GrammarRegistry;
import org.jgll.grammar.symbol.Character;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Rule;
import org.jgll.parser.ParseResult;
import org.jgll.sppf.NonterminalNode;
import org.jgll.sppf.PackedNode;
import org.jgll.sppf.SPPFNode;
import org.jgll.sppf.SPPFNodeFactory;
import org.jgll.sppf.TerminalNode;
import org.jgll.util.Configuration;
import org.jgll.util.Input;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * 
 * A ::= 'a' 'b'
 * 
 * @author Ali Afroozeh
 */
@RunWith(Parameterized.class)
public class Test3 extends AbstractParserTest {

	
	public Test3(Configuration config, Input input, Grammar grammar,
			Function<GrammarRegistry, SPPFNode> expectedSPPF) {
		super(config, input, grammar, expectedSPPF);
	}

	@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
        		{ DEFAULT,  Input.fromString("ab"), getGrammar(), (Function<GrammarRegistry, SPPFNode>) Test3::expectedSPPF },
        		{ CONFIG_1, Input.fromString("ab"), getGrammar(), (Function<GrammarRegistry, SPPFNode>) Test3::expectedSPPF },
        		{ CONFIG_2, Input.fromString("ab"), getGrammar(), (Function<GrammarRegistry, SPPFNode>) Test3::expectedSPPF },
        		{ CONFIG_3, Input.fromString("ab"), getGrammar(), (Function<GrammarRegistry, SPPFNode>) Test3::expectedSPPF }
           });
    }
	
	private static Grammar getGrammar() {
		Nonterminal A = Nonterminal.withName("A");
		Character a = Character.from('a');
		Character b = Character.from('b');
		Rule r1 = Rule.builder(A).addSymbols(a, b).build();
		return Grammar.builder().addRule(r1).build();
	}
	
	@Test
	public void testNullable() {
		assertFalse(grammar.isNullable(Nonterminal.withName("A")));
	}
	
	public void testParser() {
		ParseResult result = parser.parse(input, grammar, "A");
		assertTrue(result.isParseSuccess());
		assertTrue(result.asParseSuccess().getRoot().deepEquals(expectedSPPF.apply(parser.getRegistry())));
	}
	
	private static SPPFNode expectedSPPF(GrammarRegistry registry) {
		SPPFNodeFactory factory = new SPPFNodeFactory(registry);
		NonterminalNode node1 = factory.createNonterminalNode("A", 0, 2);
		PackedNode node2 = factory.createPackedNode("A ::= a b .", 1, node1);
		TerminalNode node3 = factory.createTerminalNode("a", 0, 1);
		TerminalNode node4 = factory.createTerminalNode("b", 1, 2);
		node2.addChild(node3);
		node2.addChild(node4);
		node1.addChild(node2);
		return node1;
	}

}