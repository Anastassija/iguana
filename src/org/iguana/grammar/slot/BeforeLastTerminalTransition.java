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

import java.util.Set;

import org.iguana.datadependent.env.Environment;
import org.iguana.grammar.condition.Condition;
import org.iguana.parser.GLLParser;
import org.iguana.parser.gss.GSSNode;
import org.iguana.sppf.DummyNode;
import org.iguana.sppf.NonPackedNode;
import org.iguana.sppf.NonterminalNode;
import org.iguana.sppf.TerminalNode;


/**
 * 
 * Corresponds to grammar slots of the form X ::= alpha . b where |alpha| > 0
 * 
 * @authors Ali Afroozeh, Anastasia Izmaylova
 *
 */
public class BeforeLastTerminalTransition extends AbstractTerminalTransition {

	public BeforeLastTerminalTransition(TerminalGrammarSlot slot, BodyGrammarSlot origin, BodyGrammarSlot dest, Set<Condition> preConditions, Set<Condition> postConditions) {
		super(slot, origin, dest, preConditions, postConditions);
	}
	
	@Override
	protected void createNode(int length, TerminalNode cr, GLLParser parser, GSSNode u, int i, NonPackedNode node) {
		if (dest.isEnd())
			dest.execute(parser, u, i + length, parser.getNonterminalNode((LastSymbolGrammarSlot) dest, node, cr));
		else {
			parser.setCurrentEndGrammarSlot(DummySlot.getInstance());
			parser.resetCurrentValue();
			dest.execute(parser, u, i + length, DummyNode.getInstance(node.getLeftExtent(), i + length));
			
			if (parser.getCurrentEndGrammarSlot().isEnd()) {
				// TODO: support for return values
				NonterminalNode currentNode = parser.getNonterminalNode((LastSymbolGrammarSlot) dest, node, cr);
				parser.getCurrentEndGrammarSlot().execute(parser, u, i + length, currentNode);
			}
		}
			
	}

	/**
	 * 
	 * Data-dependent GLL parsing
	 * 
	 */
	@Override
	protected void createNode(int length, TerminalNode cr, GLLParser parser, GSSNode u, int i, NonPackedNode node, Environment env) {
		if (dest.isEnd()) {
			if (u instanceof org.iguana.datadependent.gss.GSSNode<?>) {
				org.iguana.datadependent.gss.GSSNode<?> gssNode = (org.iguana.datadependent.gss.GSSNode<?>) u;
				dest.execute(parser, u, i + length, parser.getNonterminalNode((LastSymbolGrammarSlot) dest, node, cr, gssNode.getData()), env);
			} else
				dest.execute(parser, u, i + length, parser.getNonterminalNode((LastSymbolGrammarSlot) dest, node, cr), env);
		} else {
			parser.setCurrentEndGrammarSlot(DummySlot.getInstance());
			parser.resetCurrentValue();
			dest.execute(parser, u, i + length, DummyNode.getInstance(node.getLeftExtent(), i + length), env);
			
			if (parser.getCurrentEndGrammarSlot().isEnd()) {
				// TODO: support for return values
				if (u instanceof org.iguana.datadependent.gss.GSSNode<?>) {
					org.iguana.datadependent.gss.GSSNode<?> gssNode = (org.iguana.datadependent.gss.GSSNode<?>) u;
					parser.getCurrentEndGrammarSlot().execute(parser, u, i + length, parser.getNonterminalNode((LastSymbolGrammarSlot) dest, node, cr, gssNode.getData()), parser.getEnvironment());
				} else
					parser.getCurrentEndGrammarSlot().execute(parser, u, i + length, parser.getNonterminalNode((LastSymbolGrammarSlot) dest, node, cr), parser.getEnvironment());
			}
		}
	}
	
}
