package org.jgll.parser.basic;

import static org.jgll.util.Configurations.*;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jgll.AbstractParserTest;
import org.jgll.grammar.Grammar;
import org.jgll.grammar.GrammarGraph;
import org.jgll.grammar.operations.FirstFollowSets;
import org.jgll.grammar.operations.ReachabilityGraph;
import org.jgll.grammar.symbol.Character;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Rule;
import org.jgll.parser.ParseResult;
import org.jgll.parser.ParseSuccess;
import org.jgll.parser.ParserFactory;
import org.jgll.sppf.NonterminalNode;
import org.jgll.sppf.PackedNode;
import org.jgll.sppf.SPPFNodeFactory;
import org.jgll.sppf.TerminalNode;
import org.jgll.util.Input;
import org.jgll.util.ParseStatistics;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.ImmutableSet;

/**
 * 
 * A ::= A A | a | epsilon
 * 
 * @author Ali Afroozeh
 *
 */
@RunWith(Parameterized.class)
public class Test12 extends AbstractParserTest {
	
	static Nonterminal A = Nonterminal.withName("A");
	
	@Parameters
    public static Collection<Object[]> data() {
    	List<Object[]> parameters = 
    		newConfigs.stream().map(c -> new Object[] {
	    		getInput1(), 
	    		getGrammar(), 
	    		getStartSymbol(),
	    		ParserFactory.getParser(c, getInput1(), getGrammar()),
	    		(Function<GrammarGraph, ParseResult>) Test12::getParseResult1
	    	}).collect(Collectors.toList());
    	parameters.addAll(    		
    		newConfigs.stream().map(c -> new Object[] {
	    		getInput2(), 
	    		getGrammar(), 
	    		getStartSymbol(),
	    		ParserFactory.getParser(c, getInput2(), getGrammar()),
	    		(Function<GrammarGraph, ParseResult>) Test12::getParseResult2
	    	}).collect(Collectors.toList()));
    	parameters.addAll(    		
        	originalConfigs.stream().map(c -> new Object[] {
    	    	getInput1(), 
    	    	getGrammar(), 
    	    	getStartSymbol(),
    	    	ParserFactory.getParser(c, getInput2(), getGrammar()),
    	    	(Function<GrammarGraph, ParseResult>) Test12::getParseResult3
    	    	}).collect(Collectors.toList()));
    	parameters.addAll(    		
    		originalConfigs.stream().map(c -> new Object[] {
	    		getInput2(), 
	    		getGrammar(), 
	    		getStartSymbol(),
	    		ParserFactory.getParser(c, getInput2(), getGrammar()),
	    		(Function<GrammarGraph, ParseResult>) Test12::getParseResult4
	    	}).collect(Collectors.toList()));
    	return parameters;
    }
    
    private static Nonterminal getStartSymbol() {
    	return Nonterminal.withName("A");
    }
    
	@Test
	public void testReachableNonterminals() {
		ReachabilityGraph reachabilityGraph = new ReachabilityGraph(grammar);
		assertEquals(ImmutableSet.of(A), reachabilityGraph.getReachableNonterminals(A));
	}
	
	@Test
	public void testNullable() {
		FirstFollowSets firstFollowSets = new FirstFollowSets(grammar);
		assertTrue(firstFollowSets.isNullable(A));
	}
    
    private static Input getInput1() {
    	return Input.fromString("a");
    }
    
    private static Input getInput2() {
    	return Input.empty();
    }
    
	private static Grammar getGrammar() {
		Rule r1 = Rule.withHead(A).addSymbols(A, A).build();
		Rule r2 = Rule.withHead(A).addSymbol(Character.from('a')).build();
		Rule r3 = Rule.withHead(A).build();
		return Grammar.builder().addRule(r1).addRule(r2).addRule(r3).build();
	}
	
	private static ParseSuccess getParseResult1(GrammarGraph registry) {
		ParseStatistics statistics = ParseStatistics.builder()
				.setDescriptorsCount(12)
				.setGSSNodesCount(2)
				.setGSSEdgesCount(5)
				.setNonterminalNodesCount(3)
				.setTerminalNodesCount(3)
				.setIntermediateNodesCount(0)
				.setPackedNodesCount(7)
				.setAmbiguousNodesCount(3).build();
		return new ParseSuccess(expectedSPPF1(registry), statistics);
	}
	
	private static ParseSuccess getParseResult2(GrammarGraph registry) {
		ParseStatistics statistics = ParseStatistics.builder()
				.setDescriptorsCount(5)
				.setGSSNodesCount(1)
				.setGSSEdgesCount(2)
				.setNonterminalNodesCount(1)
				.setTerminalNodesCount(1)
				.setIntermediateNodesCount(0)
				.setPackedNodesCount(2)
				.setAmbiguousNodesCount(1).build();
		return new ParseSuccess(expectedSPPF2(registry), statistics);
	}
	
	private static ParseSuccess getParseResult3(GrammarGraph registry) {
		ParseStatistics statistics = ParseStatistics.builder()
				.setDescriptorsCount(31)
				.setGSSNodesCount(5)
				.setGSSEdgesCount(13)
				.setNonterminalNodesCount(3)
				.setTerminalNodesCount(3)
				.setIntermediateNodesCount(0)
				.setPackedNodesCount(7)
				.setAmbiguousNodesCount(3).build();
		return new ParseSuccess(expectedSPPF1(registry), statistics);
	}
	
	private static ParseSuccess getParseResult4(GrammarGraph registry) {
		ParseStatistics statistics = ParseStatistics.builder()
				.setDescriptorsCount(15)
				.setGSSNodesCount(3)
				.setGSSEdgesCount(6)
				.setNonterminalNodesCount(1)
				.setTerminalNodesCount(1)
				.setIntermediateNodesCount(0)
				.setPackedNodesCount(2)
				.setAmbiguousNodesCount(1).build();
		return new ParseSuccess(expectedSPPF2(registry), statistics);
	}
	
	private static NonterminalNode expectedSPPF1(GrammarGraph registry) {
		SPPFNodeFactory factory = new SPPFNodeFactory(registry);
		NonterminalNode node1 = factory.createNonterminalNode("A", 0, 0, 1);
		PackedNode node2 = factory.createPackedNode("A ::= a .", 1, node1);
		TerminalNode node3 = factory.createTerminalNode("a", 0, 1);
		node2.addChild(node3);
		PackedNode node4 = factory.createPackedNode("A ::= A A .", 1, node1);
		NonterminalNode node6 = factory.createNonterminalNode("A", 0, 1, 1);
		PackedNode node7 = factory.createPackedNode("A ::= .", 1, node6);
		TerminalNode node8 = factory.createEpsilonNode(1);
		node7.addChild(node8);
		PackedNode node9 = factory.createPackedNode("A ::= A A .", 1, node6);
		node9.addChild(node6);
		node9.addChild(node6);
		node6.addChild(node7);
		node6.addChild(node9);
		node4.addChild(node1);
		node4.addChild(node6);
		PackedNode node12 = factory.createPackedNode("A ::= A A .", 0, node1);
		NonterminalNode node13 = factory.createNonterminalNode("A", 0, 0, 0);
		PackedNode node14 = factory.createPackedNode("A ::= .", 0, node13);
		TerminalNode node15 = factory.createEpsilonNode(0);
		node14.addChild(node15);
		PackedNode node16 = factory.createPackedNode("A ::= A A .", 0, node13);
		node16.addChild(node13);
		node16.addChild(node13);
		node13.addChild(node14);
		node13.addChild(node16);
		node12.addChild(node13);
		node12.addChild(node1);
		node1.addChild(node2);
		node1.addChild(node4);
		node1.addChild(node12);
		return node1;
	}
	
	private static NonterminalNode expectedSPPF2(GrammarGraph registry) {
		SPPFNodeFactory factory = new SPPFNodeFactory(registry);
		NonterminalNode node1 = factory.createNonterminalNode("A", 0, 0, 0);
		PackedNode node2 = factory.createPackedNode("A ::= .", 0, node1);
		TerminalNode node3 = factory.createEpsilonNode(0);
		node2.addChild(node3);
		PackedNode node4 = factory.createPackedNode("A ::= A A .", 0, node1);
		node4.addChild(node1);
		node4.addChild(node1);
		node1.addChild(node2);
		node1.addChild(node4);
		return node1;
	}
}
