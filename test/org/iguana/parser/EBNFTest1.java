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

package org.iguana.parser;

import static org.junit.Assert.*;

import org.iguana.grammar.Grammar;
import org.iguana.grammar.operations.ReachabilityGraph;
import org.iguana.grammar.symbol.Character;
import org.iguana.grammar.symbol.Nonterminal;
import org.iguana.grammar.symbol.Rule;
import org.iguana.grammar.symbol.Terminal;
import org.iguana.grammar.transformation.EBNFToBNF;
import org.iguana.parser.GLLParser;
import org.iguana.parser.ParserFactory;
import org.iguana.regex.Plus;
import org.iguana.util.Configuration;
import org.iguana.util.Input;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

/**
 * 
 * S ::= A+
 *      
 * A ::= a
 * 
 * @author Ali Afroozeh
 *
 */
public class EBNFTest1 {
	
	private Grammar grammar;
	
	static Nonterminal S = Nonterminal.withName("S");
	static Nonterminal A = Nonterminal.withName("A");
	static Terminal a = Terminal.from(Character.from('a'));


	@Before
	public void init() {
		Grammar.Builder builder = new Grammar.Builder();
		
		Rule rule1 = Rule.withHead(S).addSymbols(Plus.from(A)).build();
		builder.addRule(rule1);
		Rule rule2 = Rule.withHead(A).addSymbols(a).build();
		builder.addRule(rule2);
		
		grammar = new EBNFToBNF().transform(builder.build());
	}
	
	@Test
	public void testReachability() {
		ReachabilityGraph reachabilityGraph = new ReachabilityGraph(grammar);
		assertEquals(ImmutableSet.of(A, Nonterminal.withName("A+")), reachabilityGraph.getReachableNonterminals(S));
	}
	
	@Test
	public void testParser() {
		Input input = Input.fromString("aaaaaa");
		GLLParser parser = ParserFactory.getParser(Configuration.DEFAULT, input, grammar);
		parser.parse(input, grammar, S);
	}

}
