package org.jgll.util.benchmark;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jgll.grammar.Grammar;
import org.jgll.grammar.GrammarGraph;
import org.jgll.grammar.symbol.Character;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Rule;
import org.jgll.parser.GLLParser;
import org.jgll.parser.ParseResult;
import org.jgll.parser.ParserFactory;
import org.jgll.util.Configuration;
import org.jgll.util.Configuration.GSSType;
import org.jgll.util.Input;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * 
 * java -Xms14g -Xmx14g -cp target/benchmark.jar org.jgll.util.BenchmarkGamma
 * 
 * @author Ali Afroozeh
 *
 */

@State(Scope.Benchmark)
@Warmup(iterations=3)
@Measurement(iterations=10)
@Fork(1)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class BenchmarkGamma {
	
	@Param({ "300" })
	int inputSize;
    
	Input input;
	Configuration config;
	Grammar grammar;
	Nonterminal startSymbol;
	GrammarGraph grammarGraph;
	GLLParser parser;
	ParseResult result;	
	
	/**
	 * S ::= S S S | S S | b
	 */
	private static Grammar gamma2() {
		Nonterminal S = Nonterminal.withName("S");
		Character b = Character.from('b');
		Grammar.Builder builder = new Grammar.Builder();
		builder.addRule(Rule.withHead(S).addSymbols(S, S, S).build());
		builder.addRule(Rule.withHead(S).addSymbols(S, S).build());
		builder.addRule(Rule.withHead(S).addSymbol(b).build());
		return builder.build();
	}

	@Setup(Level.Iteration)
	public void setup() {
		input = Input.fromString(getBs(inputSize));
		config = Configuration.builder().setGSSType(GSSType.ORIGINAL).build();
		grammar = gamma2();
		startSymbol = Nonterminal.withName("S");
		grammarGraph = gamma2().toGrammarGraph(input, config);
		parser = ParserFactory.getParser(config, input, grammar);
	}
	
	@Benchmark
	public void test() {
		result = parser.parse(input, grammarGraph, startSymbol);
	}
	
	@TearDown(Level.Iteration)
	public void cleanUp() {
		result = null;
		grammarGraph = null;
		parser = null;
		input = null;
	}
	
	public static void main(String[] args) throws RunnerException {
		int limit = 60;
		String[] params = Stream.iterate(10, i -> i + 10).limit(limit).map(j -> j.toString()).toArray(s -> new String[limit]);
		Options opt = new OptionsBuilder()
				          .include(BenchmarkGamma.class.getSimpleName())
				          .param("inputSize", params)
				          .detectJvmArgs()
				          .resultFormat(ResultFormatType.CSV)		// -rf csv
				          .result("target/result.csv") 				// -rff target/result.csv 
				          .output("target/output.txt")				// -o target/output.txt
				          .shouldDoGC(true) 						// -gc true
				          .build();
		new Runner(opt).run();
	}
		
	private static String getBs(int size) {
		return Stream.generate(() -> "b").limit(size).collect(Collectors.joining());
	}

}
