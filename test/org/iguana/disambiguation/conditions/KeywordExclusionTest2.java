/*
 * Copyright (c) 2015, Ali Afroozeh and Anastasia Izmaylova, Centrum Wiskunde & Informatica (CWI)
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

package org.iguana.disambiguation.conditions;

import static org.junit.Assert.assertTrue;

import org.iguana.grammar.Grammar;
import org.iguana.grammar.condition.RegularExpressionCondition;
import org.iguana.grammar.symbol.Character;
import org.iguana.grammar.symbol.CharacterRange;
import org.iguana.grammar.symbol.Nonterminal;
import org.iguana.grammar.symbol.Rule;
import org.iguana.parser.GLLParser;
import org.iguana.parser.ParseResult;
import org.iguana.parser.ParserFactory;
import org.iguana.regex.Alt;
import org.iguana.regex.Plus;
import org.iguana.regex.Sequence;
import org.iguana.util.Configuration;
import org.junit.Before;
import org.junit.Test;

import iguana.utils.input.Input;


/**
 * 
 * Id ::= [a-z]+ !>> [a-z] \ { "if", "when", "do", "while"}
 * 
 * @author Ali Afroozeh
 *
 */
public class KeywordExclusionTest2 {
	
	private Grammar grammar;

	@Before
	public void init() {
		
		Nonterminal Id = Nonterminal.withName("Id");
		CharacterRange az = CharacterRange.in('a', 'z');
		
		Sequence<Character> iff = Sequence.from("if");
		Sequence<Character> when = Sequence.from("when");
		Sequence<Character> doo = Sequence.from("do");
		Sequence<Character> whilee = Sequence.from("while");
		Alt<?> alt = Alt.from(iff, when, doo, whilee);		
		Plus AZPlus = Plus.builder(az).addPostCondition(RegularExpressionCondition.notFollow(az))
									  .addPostCondition(RegularExpressionCondition.notMatch(alt)).build();
		
		Rule r1 = Rule.withHead(Id).addSymbol(AZPlus).build();
		
		grammar = Grammar.builder().addRule(r1).build();
	}
	
	@Test
	public void testWhen() {
		Input input = Input.fromString("when");
		GLLParser parser = ParserFactory.getParser();
		ParseResult result = parser.parse(input, grammar, Nonterminal.withName("Id"));
		assertTrue(result.isParseError());
	}
	
	@Test
	public void testIf() {
		Input input = Input.fromString("if");		
		GLLParser parser = ParserFactory.getParser();
		ParseResult result = parser.parse(input, grammar, Nonterminal.withName("Id"));
		assertTrue(result.isParseError());
	}
	
	@Test
	public void testDo() {
		Input input = Input.fromString("do");
		GLLParser parser = ParserFactory.getParser();
		ParseResult result = parser.parse(input, grammar, Nonterminal.withName("Id"));
		assertTrue(result.isParseError());
	}
	
	@Test
	public void testWhile() {
		Input input = Input.fromString("while");
		GLLParser parser = ParserFactory.getParser();
		ParseResult result = parser.parse(input, grammar, Nonterminal.withName("Id"));
		assertTrue(result.isParseError());
	}

}
