package org.jgll.parser.basic;

import static org.jgll.util.Configurations.*;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jgll.AbstractParserTest;
import org.jgll.grammar.Grammar;
import org.jgll.grammar.GrammarRegistry;
import org.jgll.grammar.symbol.Character;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Rule;
import org.jgll.parser.ParseResult;
import org.jgll.parser.ParseSuccess;
import org.jgll.parser.ParserFactory;
import org.jgll.sppf.IntermediateNode;
import org.jgll.sppf.NonterminalNode;
import org.jgll.sppf.PackedNode;
import org.jgll.sppf.SPPFNodeFactory;
import org.jgll.sppf.TerminalNode;
import org.jgll.util.Input;
import org.jgll.util.ParseStatistics;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * S ::= A A b
 *     
 * A ::= 'a' | epsilon
 * 
 * @author Ali Afroozeh
 *
 */
@RunWith(Parameterized.class)
public class Test11 extends AbstractParserTest {
	
	@Parameters
    public static Collection<Object[]> data() {
		 List<Object[]> parameters = newConfigs.stream().map(c -> new Object[] {
	    		getInput(), 
	    		getGrammar(), 
	    		getStartSymbol(),
	    		ParserFactory.getParser(c, getInput(), getGrammar()),
	    		(Function<GrammarRegistry, ParseResult>) Test11::getNewParseResult
	    	}).collect(Collectors.toList());
		 parameters.addAll(originalConfigs.stream().map(c -> new Object[] {
		    		getInput(), 
		    		getGrammar(), 
		    		getStartSymbol(),
		    		ParserFactory.getParser(c, getInput(), getGrammar()),
		    		(Function<GrammarRegistry, ParseResult>) Test11::getOriginalParseResult
		    	}).collect(Collectors.toList()));
		 return parameters;
    }
    
    private static Input getInput() {
    	return Input.fromString("ab");
    }
    
    private static Nonterminal getStartSymbol() {
    	return Nonterminal.withName("S");
    }
	
	private static Grammar getGrammar() {
		Nonterminal S = Nonterminal.withName("S");
		Nonterminal A = Nonterminal.withName("A");
		Character a = Character.from('a');
		Character b = Character.from('b');
		Rule r1 = Rule.builder(S).addSymbols(A, A, b).build();
		Rule r2 = Rule.builder(A).addSymbol(a).build();
		Rule r3 = Rule.builder(A).build();
		return Grammar.builder().addRule(r1).addRule(r2).addRule(r3).build();
	}
	
	private static ParseSuccess getNewParseResult(GrammarRegistry registry) {
		ParseStatistics statistics = ParseStatistics.builder()
				.setDescriptorsCount(9)
				.setGSSNodesCount(3)
				.setGSSEdgesCount(3)
				.setNonterminalNodesCount(4)
				.setTerminalNodesCount(4)
				.setIntermediateNodesCount(2)
				.setPackedNodesCount(7)
				.setAmbiguousNodesCount(1).build();
		return new ParseSuccess(expectedSPPF(registry), statistics);
	}
	
	private static ParseSuccess getOriginalParseResult(GrammarRegistry registry) {
		ParseStatistics statistics = ParseStatistics.builder()
				.setDescriptorsCount(11)
				.setGSSNodesCount(4)
				.setGSSEdgesCount(3)
				.setNonterminalNodesCount(4)
				.setTerminalNodesCount(4)
				.setIntermediateNodesCount(2)
				.setPackedNodesCount(7)
				.setAmbiguousNodesCount(1).build();
		return new ParseSuccess(expectedSPPF(registry), statistics);
	}
	
	private static NonterminalNode expectedSPPF(GrammarRegistry registry) {
		SPPFNodeFactory factory = new SPPFNodeFactory(registry);
		NonterminalNode node1 = factory.createNonterminalNode("S", 0, 0, 2);
		PackedNode node2 = factory.createPackedNode("S ::= A A b .", 1, node1);
		IntermediateNode node3 = factory.createIntermediateNode("S ::= A A . b", 0, 1);
		PackedNode node4 = factory.createPackedNode("S ::= A A . b", 0, node3);
		NonterminalNode node5 = factory.createNonterminalNode("A", 0, 0, 0);
		PackedNode node6 = factory.createPackedNode("A ::= .", 0, node5);
		TerminalNode node7 = factory.createEpsilonNode(0);
		node6.addChild(node7);
		node5.addChild(node6);
		NonterminalNode node8 = factory.createNonterminalNode("A", 0, 0, 1);
		PackedNode node9 = factory.createPackedNode("A ::= a .", 1, node8);
		TerminalNode node10 = factory.createTerminalNode("a", 0, 1);
		node9.addChild(node10);
		node8.addChild(node9);
		node4.addChild(node5);
		node4.addChild(node8);
		PackedNode node11 = factory.createPackedNode("S ::= A A . b", 1, node3);
		NonterminalNode node13 = factory.createNonterminalNode("A", 0, 1, 1);
		PackedNode node14 = factory.createPackedNode("A ::= .", 1, node13);
		TerminalNode node15 = factory.createEpsilonNode(1);
		node14.addChild(node15);
		node13.addChild(node14);
		node11.addChild(node8);
		node11.addChild(node13);
		node3.addChild(node4);
		node3.addChild(node11);
		TerminalNode node16 = factory.createTerminalNode("b", 1, 2);
		node2.addChild(node3);
		node2.addChild(node16);
		node1.addChild(node2);
		return node1;
	}
}
	