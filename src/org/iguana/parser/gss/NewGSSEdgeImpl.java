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

package org.iguana.parser.gss;

import org.iguana.datadependent.env.Environment;
import org.iguana.grammar.slot.BodyGrammarSlot;
import org.iguana.parser.GLLParser;
import org.iguana.parser.HashFunctions;
import org.iguana.parser.descriptor.Descriptor;
import org.iguana.sppf.NonPackedNode;

public class NewGSSEdgeImpl implements GSSEdge {
	
	private final BodyGrammarSlot returnSlot;
	private final NonPackedNode node;
	private final GSSNode destination;

	public NewGSSEdgeImpl(BodyGrammarSlot slot, NonPackedNode node, GSSNode destination) {
		this.returnSlot = slot;
		this.node = node;
		this.destination = destination;
	}

	public NonPackedNode getNode() {
		return node;
	}

	public BodyGrammarSlot getReturnSlot() {
		return returnSlot;
	}

	public GSSNode getDestination() {
		return destination;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;

		if (!(obj instanceof GSSEdge))
			return false;

		GSSEdge other = (GSSEdge) obj;

		// Because destination.getInputIndex() == node.getLeftExtent, and
		// node.getRightExtent() == source.getLeftExtent we don't use them here.
		return 	returnSlot == other.getReturnSlot()
				&& destination.getInputIndex() == other.getDestination().getInputIndex()
				&& destination.getGrammarSlot() == other.getDestination().getGrammarSlot();
	}

	@Override
	public int hashCode() {
		return  HashFunctions.defaulFunction.hash(returnSlot.hashCode(), 
												  destination.getInputIndex(), 
												  destination.getGrammarSlot().hashCode());
	}
	
	@Override
	public String toString() {
		return String.format("(%s, %s, %s)", returnSlot, node, destination);
	}

	@Override
	public Descriptor addDescriptor(GLLParser parser, GSSNode source, int inputIndex, NonPackedNode sppfNode) {
		
		/**
		 * 
		 * Data-dependent GLL parsing
		 * 
		 */
		
		NonPackedNode y;
		BodyGrammarSlot returnSlot = this.returnSlot;
		
		if (returnSlot.requiresBinding()) {
			Environment env = returnSlot.doBinding(sppfNode, parser.getEmptyEnvironment());
			
			parser.setEnvironment(env);
			
			if (returnSlot.getConditions().execute(parser.getInput(), source, inputIndex, parser.getEvaluatorContext()))
				return null;
			
			env = parser.getEnvironment();	
			
			y = parser.getIntermediateNode2(returnSlot, node, sppfNode, env);
			
//			y = parser.getNode(returnSlot, node, sppfNode, env);
//			if (!parser.hasDescriptor(returnSlot, destination, inputIndex, y, env))
//				return new org.iguana.datadependent.descriptor.Descriptor(returnSlot, destination, inputIndex, y, env);
			
			return y != null ? new org.iguana.datadependent.descriptor.Descriptor(returnSlot, destination, inputIndex, y, env) : null;
		}
		
		if (returnSlot.getConditions().execute(parser.getInput(), source, inputIndex))
			return null;
		
		y = parser.getNode(returnSlot, node, sppfNode);
		
		if (!parser.hasDescriptor(returnSlot, destination, inputIndex, y))
			return new Descriptor(returnSlot, destination, inputIndex, y);
		
		return null;
	}

}
