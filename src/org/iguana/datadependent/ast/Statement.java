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

package org.iguana.datadependent.ast;

import org.iguana.datadependent.env.IEvaluatorContext;
import org.iguana.datadependent.traversal.IAbstractASTVisitor;

public abstract class Statement extends AbstractAST {
	
	private static final long serialVersionUID = 1L;

	static public class Expression extends Statement {

		private static final long serialVersionUID = 1L;
		
		private final org.iguana.datadependent.ast.Expression exp;
		
		Expression(org.iguana.datadependent.ast.Expression exp) {
			this.exp = exp;
		}
		
		public org.iguana.datadependent.ast.Expression getExpression() {
			return exp;
		}
		
		@Override
		public Object interpret(IEvaluatorContext ctx) {
			exp.interpret(ctx);
			return null;
		}

		@Override
		public String toString() {
			return exp.toString();
		}

		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
	}
	
	static public class VariableDeclaration extends Statement {
		
		private static final long serialVersionUID = 1L;
		
		private final org.iguana.datadependent.ast.VariableDeclaration decl;
		
		VariableDeclaration(org.iguana.datadependent.ast.VariableDeclaration decl) {
			this.decl = decl;
		}
		
		public org.iguana.datadependent.ast.VariableDeclaration getDeclaration() {
			return decl;
		}

		@Override
		public Object interpret(IEvaluatorContext ctx) {
			decl.interpret(ctx);
			return null;
		}
		
		@Override
		public String toString() {
			return decl.toString();
		}

		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
	}

}
