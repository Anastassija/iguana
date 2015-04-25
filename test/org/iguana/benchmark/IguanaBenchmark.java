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

package org.iguana.benchmark;

import static org.iguana.util.BenchmarkUtil.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.iguana.grammar.Grammar;
import org.iguana.grammar.GrammarGraph;
import org.iguana.grammar.symbol.Nonterminal;
import org.iguana.parser.GLLParser;
import org.iguana.parser.ParseResult;
import org.iguana.parser.ParserFactory;
import org.iguana.util.BenchmarkUtil;
import org.iguana.util.Configuration;
import org.iguana.util.Input;

import com.google.common.testing.GcFinalization;


public class IguanaBenchmark {
	
	private final List<File> files;
	private final Grammar grammar;
	private final Configuration config;
	private final int warmupCount;
	private final int runCount;
	private final Nonterminal start;
	private final boolean runGCInBetween;
	private final int timeout;

	public IguanaBenchmark(Builder builder) {
		this.files = builder.files;
		this.grammar = builder.grammar;
		this.config = builder.config;
		this.start = builder.start;
		this.warmupCount = builder.warmupCount;
		this.runCount = builder.runCount;
		this.runGCInBetween = builder.runGCInBetween;
		this.timeout = builder.timeout;
	}
	
	public void run() throws IOException {

		final GrammarGraph grammarGraph = grammar.toGrammarGraph(Input.empty(), config);
		
		System.out.println(BenchmarkUtil.header());
		
		for (File f : files) {
			
			Input input = Input.fromFile(f);
			GLLParser parser = ParserFactory.getParser(config, input, grammar);
			
			for (int i = 0; i < warmupCount; i++) {
				try {
					run(parser, grammarGraph, input, start);
				} catch (Exception e) {
					continue;
				}
			}
			
			System.out.println(f);
			
			for (int i = 0; i < runCount; i++) {
				
				ParseResult result;
				try {
					result = run(parser, grammarGraph, input, start);
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Time out");
					continue;
				}
				
				if (result.isParseSuccess()) {
					System.out.println(BenchmarkUtil.format(input, result.asParseSuccess().getStatistics()));
				} else {
					System.out.println("Parse error " + result.asParseError());
				}
			}
			
//			ParseResult result;
//            try {
//                result = run(parser, grammarGraph, input, start);
//                if (result.isParseSuccess()) {
//                    AtomicInteger countNonterminals = new AtomicInteger();
//                    AtomicInteger countTerminals = new AtomicInteger();
//                    AtomicInteger countIntermediates = new AtomicInteger();
//                    
//                    GeneralNodeVisitor visitor = new GeneralNodeVisitor(
//                    			(t) -> countTerminals.incrementAndGet(), 
//                    			(n) -> countNonterminals.incrementAndGet(),
//                    			(i) -> countIntermediates.incrementAndGet());
//                    
//                    visitor.visit(result.asParseSuccess().getRoot());
//                    System.out.println(String.format("Actual nodes: %d, %d, %d",
//                    								 countTerminals.get(),
//                    								 countNonterminals.get(),
//                    								 countIntermediates.get()));
//                }
//            } catch (Exception e) {
//            	System.out.println("Time out");
//            }
            
		}
		
	}
	
	private ParseResult run(GLLParser parser, GrammarGraph grammarGraph, Input input, Nonterminal start) throws Exception {
		ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<ParseResult> future = executor.submit(() -> {
			return parser.parse(input, grammarGraph, start);
        });
        
        ParseResult result;
        if (timeout > 0)
        	result = future.get(timeout, TimeUnit.SECONDS);
        else 
        	result = future.get();
        
        executor.shutdownNow();

        if (runGCInBetween)
        	GcFinalization.awaitFullGc();
        return result;
	}
	
	public static Builder builder(Grammar grammar, Nonterminal start) {
		return new Builder(grammar, start);
	}
	
	public static class Builder {

		private final Grammar grammar;
		private final Nonterminal start;
		private final List<File> files = new ArrayList<>();
		private Configuration config = Configuration.DEFAULT;
		private int warmupCount = 0;
		private int runCount = 1;
		private boolean runGCInBetween = false;
		private int timeout = 0;
		
		public Builder(Grammar grammar, Nonterminal start) {
			this.grammar = grammar;
			this.start = start;
		}
		
		public Builder addDirectory(String dir, String ext, boolean recursive) {
			files.addAll(find(dir, ext, recursive));
			return this;
		}
		
		public Builder addFile(String f) {
			files.add(new File(f));
			return this;
		}
		
		public Builder setConfiguration(Configuration config) {
			this.config = config;
			return this;
		}

		public Builder setWarmupCount(int warmupCount) {
			this.warmupCount = warmupCount;
			return this;
		}
		
		public Builder setRunCount(int runCount) {
			this.runCount = runCount;
			return this;
		}
		
		public Builder setRunGCInBetween(boolean runGCInBetween) {
			this.runGCInBetween = runGCInBetween;
			return this;
		}
		
		public Builder ignore(String s) {
			files.remove(new File(s));
			return this;
		}
		
		public Builder setTimeout(int timeout) {
			this.timeout = timeout;
			return this;
		}
		
		public IguanaBenchmark build() {
			if (files.isEmpty())
				throw new RuntimeException("No file for benchmarking added.");
			return new IguanaBenchmark(this);
		}
	}
	
}
