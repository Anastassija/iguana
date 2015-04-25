/*
 * Copyright (c) 2015, CWI
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this 
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this 
 *    list of conditions and the following disclaimer in the documentation and/or 
 *    other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
 * OF SUCH DAMAGE.
 *
 */

package org.iguana.benchmark.jmh;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.iguana.grammar.Grammar;
import org.iguana.grammar.GrammarGraph;
import org.iguana.grammar.symbol.Character;
import org.iguana.grammar.symbol.Nonterminal;
import org.iguana.grammar.symbol.Rule;
import org.iguana.parser.GLLParser;
import org.iguana.parser.ParseResult;
import org.iguana.parser.ParserFactory;
import org.iguana.util.Configuration;
import org.iguana.util.Input;
import org.iguana.util.Configuration.GSSType;
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
 * java -Xms14g -Xmx14g -cp target/benchmark.jar org.jgll.util.benchmark.BenchmarkGamma
 * 
 * @author Ali Afroozeh
 *
 */

@State(Scope.Benchmark)
@Warmup(iterations=5)
@Measurement(iterations=10)
@Fork(1)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class BenchmarkGamma {
	
	@Param({ "300" })
	int inputSize;

	@Param({ "NEW" })
	GSSType gssType;
	
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
		config = Configuration.builder().setGSSType(gssType).build();
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
		int limit = 50;
		String[] inputParams = Stream.iterate(10, i -> i + 10).limit(limit).map(j -> j.toString()).toArray(s -> new String[limit]);
		String[] gssParams = new String[] { GSSType.NEW.toString(), GSSType.ORIGINAL.toString() };
		Options opt = new OptionsBuilder()
				          .include(BenchmarkGamma.class.getSimpleName())
				          .param("inputSize", inputParams)
				          .param("gssType", gssParams)
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
