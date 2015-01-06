package org.jgll.parser.ambiguous;

import static org.jgll.util.Configurations.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jgll.AbstractParserTest;
import org.jgll.grammar.Grammar;
import org.jgll.grammar.GrammarRegistry;
import org.jgll.grammar.symbol.Character;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Rule;
import org.jgll.parser.ParseResult;
import org.jgll.parser.ParseSuccess;
import org.jgll.parser.ParserFactory;
import org.jgll.util.Input;
import org.jgll.util.ParseStatistics;
import org.junit.runners.Parameterized.Parameters;

public class Gamma2TestLargeInput extends AbstractParserTest {
	
	@Parameters
    public static Collection<Object> data() {
		List<Object> parameters = Stream.iterate(1, n -> ++n).limit(6).flatMap(i -> newConfigs.stream().map(c -> new Object[] {
	    		getInput(i * 50), 
	    		getGrammar(), 
	    		getStartSymbol(),
	    		ParserFactory.getParser(c, getInput(i * 50), getGrammar()),
	    		getNewParseResult(i - 1)
	    	})).collect(Collectors.toList());
		
		return parameters;
    }

	private static Input getInput(int size) {
		return Input.fromString(Stream.generate(() -> "b").limit(size).collect(Collectors.joining()));
	}
	
	private static Grammar getGrammar() {
		Nonterminal S = Nonterminal.withName("S");
		Character b = Character.from('b');
		Rule rule1 = Rule.builder(S).addSymbols(S, S, S).build();
		Rule rule2 = Rule.builder(S).addSymbols(S, S).build();
		Rule rule3 = Rule.builder(S).addSymbols(b).build();
		return Grammar.builder().addRules(rule1, rule2, rule3).build();
	}
	
	private static Nonterminal getStartSymbol() {
		return Nonterminal.withName("S");
	}
	
	private static Function<GrammarRegistry, ParseResult> getNewParseResult(int i) {
		return results.get(i);
	}
	
	static List<Function<GrammarRegistry, ParseResult>> results = new ArrayList<>();
	
	static {
		results.add((GrammarRegistry registry) -> new ParseSuccess(null, 
				ParseStatistics.builder()
							   .setDescriptorsCount(6329)
							   .setGSSNodesCount(51)
							   .setGSSEdgesCount(3877)
							   .setNonterminalNodesCount(1275)
							   .setTerminalNodesCount(1275)
							   .setIntermediateNodesCount(1225)
							   .setPackedNodesCount(61300) 
							   .setAmbiguousNodesCount(2352) 
							   .build()));
		
		results.add((GrammarRegistry registry) -> new ParseSuccess(null, 
				ParseStatistics.builder()
							   .setDescriptorsCount(25154)
							   .setGSSNodesCount(101)
							   .setGSSEdgesCount(15252)
							   .setNonterminalNodesCount(5050)
							   .setTerminalNodesCount(5050)
							   .setIntermediateNodesCount(4950)
							   .setPackedNodesCount(495100) 
							   .setAmbiguousNodesCount(9702) 
							   .build()));

		results.add((GrammarRegistry registry) -> new ParseSuccess(null, 
				ParseStatistics.builder()
							   .setDescriptorsCount(56479)
							   .setGSSNodesCount(151)
							   .setGSSEdgesCount(34127)
							   .setNonterminalNodesCount(11325)
							   .setTerminalNodesCount(11325)
							   .setIntermediateNodesCount(11175)
							   .setPackedNodesCount(1676400) 
							   .setAmbiguousNodesCount(22052)
							   .build()));

		results.add((GrammarRegistry registry) -> new ParseSuccess(null, 
				ParseStatistics.builder()
							   .setDescriptorsCount(100304)
							   .setGSSNodesCount(201)
							   .setGSSEdgesCount(60502)
							   .setNonterminalNodesCount(20100)
							   .setTerminalNodesCount(20100)
							   .setIntermediateNodesCount(19900)
							   .setPackedNodesCount(3980200) 
							   .setAmbiguousNodesCount(39402) 
							   .build()));

		results.add((GrammarRegistry registry) -> new ParseSuccess(null, 
				ParseStatistics.builder()
							   .setDescriptorsCount(156629)
							   .setGSSNodesCount(251)
							   .setGSSEdgesCount(94377)
							   .setNonterminalNodesCount(31375)
							   .setTerminalNodesCount(31375)
							   .setIntermediateNodesCount(31125)
							   .setPackedNodesCount(7781500) 
							   .setAmbiguousNodesCount(61752) 
							   .build()));

		results.add((GrammarRegistry registry) -> new ParseSuccess(null, 
				ParseStatistics.builder()
							   .setDescriptorsCount(225454)
							   .setGSSNodesCount(301)
							   .setGSSEdgesCount(135752)
							   .setNonterminalNodesCount(45150)
							   .setTerminalNodesCount(45150)
							   .setIntermediateNodesCount(44850)
							   .setPackedNodesCount(13455300) 
							   .setAmbiguousNodesCount(89102)
							   .build()));

//		results.add((GrammarRegistry registry) -> new ParseSuccess(null, 
//				ParseStatistics.builder()
//							   .setDescriptorsCount(306779)
//							   .setGSSNodesCount(351)
//							   .setGSSEdgesCount(184627)
//							   .setNonterminalNodesCount(61425)
//							   .setTerminalNodesCount(61425)
//							   .setIntermediateNodesCount(61075)
//							   .setPackedNodesCount(21376600) 
//							   .setAmbiguousNodesCount(121452) 
//							   .build()));
//
//		results.add((GrammarRegistry registry) -> new ParseSuccess(null, 
//				ParseStatistics.builder()
//							   .setDescriptorsCount(400604)
//							   .setGSSNodesCount(401)
//							   .setGSSEdgesCount(241002)
//							   .setNonterminalNodesCount(80200)
//							   .setTerminalNodesCount(80200)
//							   .setIntermediateNodesCount(79800)
//							   .setPackedNodesCount(31920400) 
//							   .setAmbiguousNodesCount(158802) 
//							   .build()));

	}
}
