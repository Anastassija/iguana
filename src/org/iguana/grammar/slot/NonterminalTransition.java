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

package org.iguana.grammar.slot;

import org.iguana.datadependent.ast.Expression;
import org.iguana.datadependent.env.Environment;
import org.iguana.grammar.condition.Conditions;
import org.iguana.grammar.condition.ConditionsFactory;
import org.iguana.parser.GLLParser;
import org.iguana.parser.gss.GSSNode;
import org.iguana.sppf.NonPackedNode;
import org.iguana.util.generator.GeneratorUtil;


public class NonterminalTransition extends AbstractTransition {
	
	private final NonterminalGrammarSlot nonterminal;
	
	private final Conditions preConditions;
	
	private final Expression[] arguments;

	public NonterminalTransition(NonterminalGrammarSlot nonterminal, BodyGrammarSlot origin, BodyGrammarSlot dest, Conditions preConditions) {
		this(nonterminal, origin, dest, null, preConditions);
	}
	
	public NonterminalTransition(NonterminalGrammarSlot nonterminal, BodyGrammarSlot origin, BodyGrammarSlot dest) {
		this(nonterminal, origin, dest, null, ConditionsFactory.DEFAULT);
	}	
	
	public NonterminalTransition(NonterminalGrammarSlot nonterminal, BodyGrammarSlot origin, BodyGrammarSlot dest, 
			Expression[] arguments, Conditions preConditions) {
		super(origin, dest);
		this.nonterminal = nonterminal;
		this.arguments = arguments;
		this.preConditions = preConditions;
	}

	@Override
	public void execute(GLLParser parser, GSSNode u, int i, NonPackedNode node) {		
//		if (!nonterminal.testPredict(parser.getInput().charAt(i))) {
//			parser.recordParseError(origin);
//			return;
//		}
		
		if (nonterminal.getParameters() == null && dest.getLabel() == null) {
			
			if (preConditions.execute(parser.getInput(), u, i))
				return;
			
			nonterminal.create(parser, dest, u, i, node);
			
		} else {
			
			Environment env = parser.getEmptyEnvironment();
			
			if (dest.getLabel() != null) {
				env = env.declare(String.format(Expression.LeftExtent.format, dest.getLabel()), i);
			}
			
			parser.setEnvironment(env);
			
			if (preConditions.execute(parser.getInput(), u, i, parser.getEvaluatorContext()))
				return;
			
			nonterminal.create(parser, dest, u, i, node, arguments, parser.getEnvironment());
		}
		
	}
	
	public NonterminalGrammarSlot getSlot() {
		return nonterminal;
	}
	
	@Override
	public String getConstructorCode() {
		return new StringBuilder()
			.append("new NonterminalTransition(")
			.append("slot" + nonterminal.getId()).append(", ")
			.append("slot" + origin.getId()).append(", ")
			.append("slot" + dest.getId()).append(", ")
			.append(preConditions)
			.toString();
	}
	
	@Override
	public String getLabel() {
		return (dest.getVariable() != null? dest.getVariable() + "=" : "") 
				+ (dest.getLabel() != null? dest.getLabel() + ":"  : "")
				+ (arguments != null? String.format("%s(%s)", getSlot().toString(), GeneratorUtil.listToString(arguments, ",")) : getSlot().toString());
	}

	/**
	 * 
	 * Data-dependent GLL parsing
	 * 
	 */
	@Override
	public void execute(GLLParser parser, GSSNode u, int i, NonPackedNode node, Environment env) {
		
//		if (!nonterminal.testPredict(parser.getInput().charAt(i))) {
//			parser.recordParseError(origin);
//			return;
//		}
		
		if (dest.getLabel() != null) {
			env = env.declare(String.format(Expression.LeftExtent.format, dest.getLabel()), i);
		}
		
		parser.setEnvironment(env);
		
		if (preConditions.execute(parser.getInput(), u, i, parser.getEvaluatorContext()))
			return;
				
		nonterminal.create(parser, dest, u, i, node, arguments, parser.getEnvironment());
	}

}
