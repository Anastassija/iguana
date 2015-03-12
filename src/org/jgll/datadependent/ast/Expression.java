package org.jgll.datadependent.ast;

import org.jgll.datadependent.env.IEvaluatorContext;
import org.jgll.datadependent.traversal.IAbstractASTVisitor;
import org.jgll.grammar.exception.UndeclaredVariableException;
import org.jgll.grammar.exception.UnexpectedTypeOfArgumentException;
import org.jgll.sppf.NonPackedNode;


public abstract class Expression extends AbstractAST {
	
	public boolean isBoolean() {
		return false;
	}
	
	static public abstract class Boolean extends Expression {
		
		public boolean isBoolean() {
			return true;
		}
		
		static public final Boolean TRUE = new Boolean() {		
			@Override
			public Object interpret(IEvaluatorContext ctx) {
				return true;
			}
			
			@Override
			public java.lang.String getConstructorCode() {
				return "AST.TRUE";
			}
			
			@Override
			public java.lang.String toString() {
				return "true";
			}

			@Override
			public <T> T accept(IAbstractASTVisitor<T> visitor) {
				return visitor.visit(this);
			}
		};
		
		static public final Boolean FALSE = new Boolean() {
			@Override
			public Object interpret(IEvaluatorContext ctx) {
				return false;
			}
			
			@Override
			public java.lang.String getConstructorCode() {
				return "AST.FALSE";
			}
			
			@Override
			public java.lang.String toString() {
				return "false";
			}

			@Override
			public <T> T accept(IAbstractASTVisitor<T> visitor) {
				return visitor.visit(this);
			}
			
		};
		
	}
	
	public boolean isInteger() {
		return false;
	}
	
	static public class Integer extends Expression {

		private final java.lang.Integer value;
		
		Integer(java.lang.Integer value) {
			this.value = value;
		}
		
		public boolean isInteger() {
			return true;
		}
		
		@Override
        public Object interpret(IEvaluatorContext ctx) {
			return value;
		}
		
		@Override
		public java.lang.String getConstructorCode() {
			return "AST.integer(" + value + ")";
		}
		
		@Override
		public java.lang.String toString() {
			return value.toString();
		}

		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
	}
	
	public boolean isReal() {
		return false;
	}
	
	static public class Real extends Expression {
        
		private final java.lang.Float value;
		
		Real(java.lang.Float value) {
			this.value = value;
		}
		
		public boolean isReal() {
			return true;
		}
		
		@Override
		public Object interpret(IEvaluatorContext ctx) {
			return value;
		}
		
		@Override
		public java.lang.String getConstructorCode() {
			return "AST.real(" + value + ")";
		}
		
		@Override
		public java.lang.String toString() {
			return value.toString();
		}

		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
	}
	
	public boolean isString() {
		return false;
	}
	
	static public class String extends Expression {
		
		private final java.lang.String value;
		
		String(java.lang.String value) {
			this.value = value;
		}
		
		public boolean isString() {
			return true;
		}

		@Override
		public Object interpret(IEvaluatorContext ctx) {
			return value;
		}
		
		@Override
		public java.lang.String getConstructorCode() {
			return "AST.string(" + value + ")";
		}
		
		@Override
		public java.lang.String toString() {
			return value;
		}

		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
	}
	
	static public class Name extends Expression {
		
		private final java.lang.String name;
		
		Name(java.lang.String name) {
			this.name = name;
		}
		
		public java.lang.String getName() {
			return name;
		}

		@Override
		public Object interpret(IEvaluatorContext ctx) {
			Object value = ctx.lookupVariable(name);
			if (value == null) {
				throw new UndeclaredVariableException(name);
			}
			return value;
		}
		
		@Override
		public java.lang.String getConstructorCode() {
			return "AST.var(" + "\"" + name + "\"" + ")";
		}
		
		@Override
		public java.lang.String toString() {
			return name;
		}

		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
	}
	
	static public abstract class Call extends Expression {
		
		@SuppressWarnings("unused")
		private final java.lang.String fun;
		private final Expression[] arguments;
		
		Call(java.lang.String fun, Expression... arguments) {
			this.fun = fun;
			this.arguments = arguments;
		}
		
		public Expression[] getArguments() {
			return this.arguments;
		}
		
		protected Object[] interpretArguments(IEvaluatorContext ctx) {
			Object[] values = new Object[arguments.length];
			
			int i = 0;
			while(i < arguments.length) {
				values[i] = arguments[i].interpret(ctx);
				i++;
			}
			
			return values;
		}
		
		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
				
	}
	
	static public class Assignment extends Expression {
		
		private final java.lang.String id;
		private final Expression exp;
		
		Assignment(java.lang.String id, Expression exp) {
			this.id = id;
			this.exp = exp;
		}
		
		public java.lang.String getId() {
			return id;
		}
		
		public Expression getExpression() {
			return exp;
		}

		@Override
		public Object interpret(IEvaluatorContext ctx) {
			ctx.storeVariable(id, exp.interpret(ctx));
			return null;
		}
		
		@Override
		public java.lang.String getConstructorCode() {
			return "AST.assign(" + "\"" + id + "\"" + "," + exp.getConstructorCode() + ")";
		}
		
		@Override
		public java.lang.String toString() {
			return java.lang.String.format("%s = %s", id, exp);
		}

		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
	}
	
	static public class LShiftANDEqZero extends Expression {
		
		private final Expression lhs;
		private final Expression rhs;

		LShiftANDEqZero(Expression lhs, Expression rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
		}
		
		public Expression getLhs() {
			return lhs;
		}
		
		public Expression getRhs() {
			return rhs;
		}
		
		@Override
		public Object interpret(IEvaluatorContext ctx) {
			Object lhs = this.lhs.interpret(ctx);
			Object rhs = this.rhs.interpret(ctx);
			
			if (lhs instanceof java.lang.Integer && rhs instanceof java.lang.Integer)
				return (((java.lang.Integer) lhs) & (1 << ((java.lang.Integer) rhs))) == 0;
			
			throw new UnexpectedTypeOfArgumentException(this);
		}

		@Override
		public java.lang.String getConstructorCode() {
			return "AST.lShiftANDEqZero(" + lhs.getConstructorCode() + "," + rhs.getConstructorCode() + ")";
		}
		
		@Override
		public java.lang.String toString() {
			return java.lang.String.format("%s&(1<<%s) == 0", lhs, rhs);
		}

		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
	}
	
	static public class OrIndent extends Expression {
		
		private final Expression index;
		private final Expression ind;
		private final Expression first;
		private final Expression lExt;
		
		OrIndent(Expression index, Expression ind, Expression first, Expression lExt) {
			this.index = index;
			this.ind = ind;
			this.first = first;
			this.lExt = lExt;
		}
		
		public Expression getIndext() {
			return index;
		}
		
		public Expression getIndent() {
			return ind;
		}
		
		public Expression getFirst() {
			return first;
		}
		
		public Expression getLExt() {
			return lExt;
		}

		@Override
		public Object interpret(IEvaluatorContext ctx) {
			
			int ind = (java.lang.Integer) this.ind.interpret(ctx);
			
			if (ind == 0)
				return true;
			
			int first = (java.lang.Integer) this.first.interpret(ctx);
			int lExt = -1;
			if (first == 1) {
				
				int index = (java.lang.Integer) this.index.interpret(ctx);
				lExt = (java.lang.Integer) this.lExt.interpret(ctx);
				
				if(lExt - index == 0) 
					return true;
				else {
					int indent = ctx.getInput().getColumnNumber(lExt);
					return indent > ind;
				}
				
			} else {
				lExt = (java.lang.Integer) this.lExt.interpret(ctx);
				int indent = ctx.getInput().getColumnNumber(lExt);
				return indent > ind;
			}
			
		}

		@Override
		public java.lang.String getConstructorCode() {
			return "AST.orIndent(" + index.getConstructorCode() + "," + ind.getConstructorCode() + "," + first.getConstructorCode() + lExt.getConstructorCode() + ")";
		}

		@Override
		public java.lang.String toString() {
			return ind + " == 0 || (" + first + " && " + lExt + " - " + index + " == 0) || indent(" + lExt + ") > " + ind;
		}
		
		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
	}
	
	static public class AndIndent extends Expression {
		
		private final Expression index;
		private final Expression first;
		private final Expression lExt;
		
		private final boolean returnIndex;
		
		AndIndent(Expression index, Expression first, Expression lExt) {
			this.index = index;
			this.first = first;
			this.lExt = lExt;
			this.returnIndex = false;
		}
		
		AndIndent(Expression index, Expression first, Expression lExt, boolean returnIndex) {
			this.index = index;
			this.first = first;
			this.lExt = lExt;
			this.returnIndex = returnIndex;
		}
		
		public Expression getIndext() {
			return index;
		}
		
		public Expression getFirst() {
			return first;
		}
		
		public Expression getLExt() {
			return lExt;
		}

		@Override
		public Object interpret(IEvaluatorContext ctx) {
			int first = (java.lang.Integer) this.first.interpret(ctx);
			if (first == 1) {
				
				int index = (java.lang.Integer) this.index.interpret(ctx);
				int lExt = (java.lang.Integer) this.lExt.interpret(ctx);
				
				if(lExt - index == 0) 
					return returnIndex? index : 1;
			}
			
			return 0;
		}

		@Override
		public java.lang.String getConstructorCode() {
			return "AST.andIndent(" + index.getConstructorCode() + "," + first.getConstructorCode() + lExt.getConstructorCode() + "," + returnIndex + ")";
		}
		
		@Override
		public java.lang.String toString() {
			return returnIndex? "(" +first + " && " + lExt + " - " + index + " == 0)?" + index
					          : first + " && " + lExt + " - " + index + " == 0";
		}

		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
	}
		
	static public class Less extends Expression {
		
		private final Expression lhs;
		private final Expression rhs;
		
		Less(Expression lhs, Expression rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
		}
		
		public Expression getLhs() {
			return lhs;
		}
		
		public Expression getRhs() {
			return rhs;
		}

		@Override
		public Object interpret(IEvaluatorContext ctx) {
			Object lhs = this.lhs.interpret(ctx);
			Object rhs = this.rhs.interpret(ctx);
			
			if (lhs instanceof java.lang.Integer && rhs instanceof java.lang.Integer) {
				return ((java.lang.Integer) lhs) < ((java.lang.Integer) rhs);
			}
			
			if (lhs instanceof java.lang.Float && rhs instanceof java.lang.Float) {
				return ((java.lang.Float) lhs) < ((java.lang.Float) rhs);
			}
						
			throw new UnexpectedTypeOfArgumentException(this);
		}
		
		@Override
		public java.lang.String getConstructorCode() {
			return "AST.less(" + lhs.getConstructorCode() + "," + rhs.getConstructorCode() + ")";
		}
		
		@Override
		public java.lang.String toString() {
			return java.lang.String.format("%s < %s", lhs, rhs);
		}

		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
	}
	
	static public class Greater extends Expression {
		
		private final Expression lhs;
		private final Expression rhs;
		
		Greater(Expression lhs, Expression rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
		}
		
		public Expression getLhs() {
			return lhs;
		}
		
		public Expression getRhs() {
			return rhs;
		}

		@Override
		public Object interpret(IEvaluatorContext ctx) {
			Object lhs = this.lhs.interpret(ctx);
			Object rhs = this.rhs.interpret(ctx);
			
			if (lhs instanceof java.lang.Integer && rhs instanceof java.lang.Integer) {
				return ((java.lang.Integer) lhs) > ((java.lang.Integer) rhs);
			}
			
			if (lhs instanceof java.lang.Float && rhs instanceof java.lang.Float) {
				return ((java.lang.Float) lhs) > ((java.lang.Float) rhs);
			}
						
			throw new UnexpectedTypeOfArgumentException(this);
		}
		
		@Override
		public java.lang.String getConstructorCode() {
			return "AST.greater(" + lhs.getConstructorCode() + "," + rhs.getConstructorCode() + ")";
		}
		
		@Override
		public java.lang.String toString() {
			return java.lang.String.format("%s > %s", lhs, rhs);
		}

		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
	}
	
	static public class GreaterThanEqual extends Expression {
		
		private final Expression lhs;
		private final Expression rhs;
		
		GreaterThanEqual(Expression lhs, Expression rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
		}
		
		public Expression getLhs() {
			return lhs;
		}
		
		public Expression getRhs() {
			return rhs;
		}

		@Override
		public Object interpret(IEvaluatorContext ctx) {
			Object lhs = this.lhs.interpret(ctx);
			Object rhs = this.rhs.interpret(ctx);
			
			if (lhs instanceof java.lang.Integer && rhs instanceof java.lang.Integer) {
				return ((java.lang.Integer) lhs) >= ((java.lang.Integer) rhs);
			}
			
			if (lhs instanceof java.lang.Float && rhs instanceof java.lang.Float) {
				return ((java.lang.Float) lhs) >= ((java.lang.Float) rhs);
			}
						
			throw new UnexpectedTypeOfArgumentException(this);
		}
		
		@Override
		public java.lang.String getConstructorCode() {
			return "AST.greaterEq(" + lhs.getConstructorCode() + "," + rhs.getConstructorCode() + ")";
		}
		
		@Override
		public java.lang.String toString() {
			return java.lang.String.format("%s >= %s", lhs, rhs);
		}

		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
	}
	
	static public class Equal extends Expression {
		
		private final Expression lhs;
		private final Expression rhs;
		
		Equal(Expression lhs, Expression rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
		}
		
		public Expression getLhs() {
			return lhs;
		}
		
		public Expression getRhs() {
			return rhs;
		}

		@Override
		public Object interpret(IEvaluatorContext ctx) {
			Object lhs = this.lhs.interpret(ctx);
			Object rhs = this.rhs.interpret(ctx);
			
			if (lhs instanceof java.lang.Integer && rhs instanceof java.lang.Integer) {
				return ((java.lang.Integer) lhs) == ((java.lang.Integer) rhs);
			}
			
			if (lhs instanceof java.lang.Float && rhs instanceof java.lang.Float) {
				return ((java.lang.Float) lhs) == ((java.lang.Float) rhs);
			}
						
			throw new UnexpectedTypeOfArgumentException(this);
		}
		
		@Override
		public java.lang.String getConstructorCode() {
			return "AST.equal(" + lhs.getConstructorCode() + "," + rhs.getConstructorCode() + ")";
		}
		
		@Override
		public java.lang.String toString() {
			return java.lang.String.format("%s == %s", lhs, rhs);
		}

		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
	}
	
	static public class NotEqual extends Expression {
		
		private final Expression lhs;
		private final Expression rhs;
		
		NotEqual(Expression lhs, Expression rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
		}
		
		public Expression getLhs() {
			return lhs;
		}
		
		public Expression getRhs() {
			return rhs;
		}

		@Override
		public Object interpret(IEvaluatorContext ctx) {
			Object lhs = this.lhs.interpret(ctx);
			Object rhs = this.rhs.interpret(ctx);
			
			if (lhs instanceof java.lang.Integer && rhs instanceof java.lang.Integer) {
				return ((java.lang.Integer) lhs) != ((java.lang.Integer) rhs);
			}
			
			if (lhs instanceof java.lang.Float && rhs instanceof java.lang.Float) {
				return ((java.lang.Float) lhs) != ((java.lang.Float) rhs);
			}
						
			throw new UnexpectedTypeOfArgumentException(this);
		}
		
		@Override
		public java.lang.String getConstructorCode() {
			return "AST.notEqual(" + lhs.getConstructorCode() + "," + rhs.getConstructorCode() + ")";
		}
		
		@Override
		public java.lang.String toString() {
			return java.lang.String.format("%s != %s", lhs, rhs);
		}

		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
	}
	
	static public class LeftExtent extends Expression {
		
		static public java.lang.String format = "%s.lExt";
		
		private final java.lang.String label;
		
		LeftExtent(java.lang.String label) {
			this.label = label;
		}
		
		public java.lang.String getLabel() {
			return label;
		}

		@Override
		public Object interpret(IEvaluatorContext ctx) {
			Object value = ctx.lookupVariable(java.lang.String.format(format, label));
			if (value == null) {
				throw new UndeclaredVariableException(label + "." + "lExt");
			}
			return value;
		}
		
		@Override
		public java.lang.String getConstructorCode() {
			return "AST.lExt(" + "\"" + label + "\"" + ")";
		}
		
		@Override
		public java.lang.String toString() {
			return java.lang.String.format("%s.lExt", label);
		}

		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
	}
	
	static public class RightExtent extends Expression {
		
		static public java.lang.String format = "%s.rExt";
		
		private final java.lang.String label;
		
		RightExtent(java.lang.String label) {
			this.label = label;
		}
		
		public java.lang.String getLabel() {
			return label;
		}

		@Override
		public Object interpret(IEvaluatorContext ctx) {
			Object value = ctx.lookupVariable(label);
			if (value == null) {
				throw new UndeclaredVariableException(label);
			}
			
			if (!(value instanceof NonPackedNode)) {
				throw new UnexpectedTypeOfArgumentException(this);
			}
			
			return ((NonPackedNode) value).getRightExtent();
		}
		
		@Override
		public java.lang.String getConstructorCode() {
			return "AST.rExt(" + "\"" + label + "\"" + ")";
		}
		
		@Override
		public java.lang.String toString() {
			return java.lang.String.format("%s.rExt", label);
		}

		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
	}
	
}
