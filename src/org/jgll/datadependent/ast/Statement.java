package org.jgll.datadependent.ast;

import org.jgll.datadependent.env.IEvaluatorContext;

public abstract class Statement extends AbstractAST {
	
	static public class Expression extends Statement {

		private final org.jgll.datadependent.ast.Expression exp;
		
		Expression(org.jgll.datadependent.ast.Expression exp) {
			this.exp = exp;
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
		
	}
	
	static public class VariableDeclaration extends Statement {
		
		private final org.jgll.datadependent.ast.VariableDeclaration decl;
		
		VariableDeclaration(org.jgll.datadependent.ast.VariableDeclaration decl) {
			this.decl = decl;
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
		
	}

}
