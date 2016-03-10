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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import iguana.parsetrees.sppf.NonPackedNode;
import iguana.parsetrees.sppf.NonterminalNode;
import iguana.parsetrees.sppf.SPPFNode;
import org.iguana.datadependent.env.IEvaluatorContext;
import org.iguana.datadependent.traversal.IAbstractASTVisitor;
import org.iguana.grammar.exception.UnexpectedTypeOfArgumentException;

import static iguana.utils.string.StringUtil.*;

public abstract class Expression extends AbstractAST {
	
	private static final long serialVersionUID = 1L;

	public boolean isBoolean() {
		return false;
	}
	
	static public abstract class Boolean extends Expression {
		
		private static final long serialVersionUID = 1L;

		public boolean isBoolean() {
			return true;
		}
		
		static public final Boolean TRUE = new Boolean() {		

			private static final long serialVersionUID = 1L;

			@Override
			public Object interpret(IEvaluatorContext ctx) {
				return true;
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

			private static final long serialVersionUID = 1L;

			@Override
			public Object interpret(IEvaluatorContext ctx) {
				return false;
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

		private static final long serialVersionUID = 1L;
		
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
        
		private static final long serialVersionUID = 1L;
		
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
		
		private static final long serialVersionUID = 1L;
		
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
		public java.lang.String toString() {
			return value;
		}

		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
	}
	
	static public class Tuple extends Expression {
		
		private static final long serialVersionUID = 1L;
		
		private final Expression[] elements;
		private final int length;
		
		Tuple(Expression... elements) {
			this.elements = elements;
			for (Expression element : elements) 
				if (element == null)
					throw new RuntimeException("Expressions of a tuple should not be null.");
			this.length = elements.length;
		}
		
		public Expression[] getElements() {
			return elements;
		}
		
		@Override
		public Object interpret(IEvaluatorContext ctx) {
			if (length == 1)
				return elements[0].interpret(ctx);
			
			List<Object> values = new ArrayList<Object>();		
			for (Expression element : elements)
				values.add(element.interpret(ctx));
			
			return values;
		}
		
		@Override
		public java.lang.String toString() {
			return "(" + listToString(Arrays.stream(elements).map(elem -> elem.toString()).collect(Collectors.toList()), ",") + ")";
		}
		
		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
	}
	
	static public class Name extends Expression {
		
		private static final long serialVersionUID = 1L;
		
		private final java.lang.String name;
		private final int i;
		
		Name(java.lang.String name) {
			this(name, -1);
		}
		
		Name(java.lang.String name, int i) {
			this.name = name;
			this.i = i;
		}
		
		public java.lang.String getName() {
			return name;
		}

		@Override
		public Object interpret(IEvaluatorContext ctx) {
            return ctx.lookup(i);
		}
		
		@Override
		public java.lang.String toString() {
			return name + (i != -1 ? ":" + i : "");
		}

		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
	}
	
	static public abstract class Call extends Expression {
		
		private static final long serialVersionUID = 1L;
		
		private final java.lang.String fun;
		private final Expression[] arguments;
		
		Call(java.lang.String fun, Expression... arguments) {
			this.fun = fun;
			this.arguments = arguments;
		}
		
		public java.lang.String getFunName() {
			return fun;
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
		
		private static final long serialVersionUID = 1L;
		
		private final java.lang.String id;
		private final int i;
		private final Expression exp;
		
		Assignment(java.lang.String id, Expression exp) {
			this(id, -1, exp);
		}
		
		Assignment(java.lang.String id, int i, Expression exp) {
			this.id = id;
			this.i = i;
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
			ctx.store(i, exp.interpret(ctx));
			return null;
		}
		
		@Override
		public java.lang.String toString() {
			return i != -1 ? java.lang.String.format("%s:%s = %s", id, i, exp) : java.lang.String.format("%s = %s", id, exp);
		}

		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
	}
	
	static public class LShiftANDEqZero extends Expression {
		
		private static final long serialVersionUID = 1L;
		
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
				return (((int) lhs) & (1 << ((int) rhs))) == 0;
			
			throw new UnexpectedTypeOfArgumentException(this);
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
		
		private static final long serialVersionUID = 1L;
		
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
		
		public Expression getIndex() {
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
			
			int ind = (int) this.ind.interpret(ctx);
			
			if (ind == 0)
				return true;
			
			int first = (int) this.first.interpret(ctx);
			int lExt = -1;
			if (first == 1) {
				
				int index = (int) this.index.interpret(ctx);
				lExt = (int) this.lExt.interpret(ctx);
				
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
		public java.lang.String toString() {
			// return ind + " == 0 || (" + first + " && " + lExt + " - " + index + " == 0) || indent(" + lExt + ") > " + ind;
			return java.lang.String.format("f(%s,%s,%s,%s)", index, ind, first, lExt);
		}
		
		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
	}
	
	static public class AndIndent extends Expression {
		
		private static final long serialVersionUID = 1L;
		
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
		
		public Expression getIndex() {
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
		public java.lang.String toString() {
//			return returnIndex? "(" +first + " && " + lExt + " - " + index + " == 0)?" + index
//					          : first + " && " + lExt + " - " + index + " == 0";
			return returnIndex? java.lang.String.format("g(%s,%s,%s,%s)", index, first, lExt, 1)
					          : java.lang.String.format("g(%s,%s,%s,%s)", index, first, lExt, 0);
		}

		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
	}
	
	static public class Or extends Expression {
		
		private static final long serialVersionUID = 1L;
		
		private final Expression lhs;
		private final Expression rhs;
		
		Or(Expression lhs, Expression rhs) {
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
			boolean lhs = (java.lang.Boolean) this.lhs.interpret(ctx);
			if (lhs == true)
				return lhs;
			
			return this.rhs.interpret(ctx);
		}

		@Override
		public java.lang.String toString() {
			return java.lang.String.format("%s || %s", lhs, rhs);
		}

		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
	}
	
	static public class And extends Expression {
		
		private static final long serialVersionUID = 1L;
		
		private final Expression lhs;
		private final Expression rhs;
		
		And(Expression lhs, Expression rhs) {
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
			boolean lhs = (java.lang.Boolean) this.lhs.interpret(ctx);
			if (lhs == false)
				return lhs;
			
			return this.rhs.interpret(ctx);
		}

		@Override
		public java.lang.String toString() {
			return java.lang.String.format("%s && %s", lhs, rhs);
		}

		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
	}
		
	static public class Less extends Expression {
		
		private static final long serialVersionUID = 1L;
		
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
			
			if (lhs instanceof java.lang.Integer && rhs instanceof java.lang.Integer)
				return ((int) lhs) < ((int) rhs);
			
			if (lhs instanceof java.lang.Float && rhs instanceof java.lang.Float)
				return ((float) lhs) < ((float) rhs);
						
			throw new UnexpectedTypeOfArgumentException(this);
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
	
	static public class LessThanEqual extends Expression {
		
		private static final long serialVersionUID = 1L;
		
		private final Expression lhs;
		private final Expression rhs;
		
		LessThanEqual(Expression lhs, Expression rhs) {
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
				return ((int) lhs) <= ((int) rhs);
			
			if (lhs instanceof java.lang.Float && rhs instanceof java.lang.Float)
				return ((float) lhs) <= ((float) rhs);
						
			throw new UnexpectedTypeOfArgumentException(this);
		}
		
		@Override
		public java.lang.String toString() {
			return java.lang.String.format("%s <= %s", lhs, rhs);
		}

		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
	}
	
	static public class Greater extends Expression {
		
		private static final long serialVersionUID = 1L;
		
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
			
			if (lhs instanceof java.lang.Integer && rhs instanceof java.lang.Integer)
				return ((int) lhs) > ((int) rhs);
			
			if (lhs instanceof java.lang.Float && rhs instanceof java.lang.Float)
				return ((float) lhs) > ((float) rhs);
						
			throw new UnexpectedTypeOfArgumentException(this);
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
		
		private static final long serialVersionUID = 1L;
		
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
			
			if (lhs instanceof java.lang.Integer && rhs instanceof java.lang.Integer)
				return ((int) lhs) >= ((int) rhs);
			
			if (lhs instanceof java.lang.Float && rhs instanceof java.lang.Float)
				return ((float) lhs) >= ((float) rhs);
						
			throw new UnexpectedTypeOfArgumentException(this);
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
		
		private static final long serialVersionUID = 1L;
		
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
			
			if (lhs == AST.UNDEF || rhs == AST.UNDEF) {
				if (lhs == rhs)
					return true;
				return false;
			}
			
			if (lhs instanceof java.lang.Integer && rhs instanceof java.lang.Integer)
				return ((int) lhs) == ((int) rhs);
			
			if (lhs instanceof java.lang.Float && rhs instanceof java.lang.Float)
				return ((float) lhs) == ((float) rhs);
			
			if (lhs instanceof java.lang.String && rhs instanceof java.lang.String)
				return lhs.equals(rhs);
						
			throw new UnexpectedTypeOfArgumentException(this);
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
		
		private static final long serialVersionUID = 1L;
		
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
			
			if (lhs instanceof java.lang.Integer && rhs instanceof java.lang.Integer)
				return ((int) lhs) != ((int) rhs);
			
			if (lhs instanceof java.lang.Float && rhs instanceof java.lang.Float)
				return ((float) lhs) != ((float) rhs);

            throw new UnexpectedTypeOfArgumentException(this);
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
		
		private static final long serialVersionUID = 1L;

		static public java.lang.String format = "%s.lExt";
		
		private final java.lang.String label;
        private final int i;

        LeftExtent(java.lang.String label) {
            this(label, -1);
        }

		LeftExtent(java.lang.String label, int i) {
			this.label = label;
            this.i = i;
		}

		public java.lang.String getLabel() {
			return label;
		}

		@Override
		public Object interpret(IEvaluatorContext ctx) {
            Object value = ctx.lookup(i);

            if (value instanceof org.iguana.util.Tuple<?,?>)
                return ((org.iguana.util.Tuple<?,?>) value).getFirst();
            else
                return ((SPPFNode) value).getLeftExtent();
		}
		
		@Override
		public java.lang.String toString() {
			return i != -1 ? java.lang.String.format("%s:%d.lExt", label, i) : java.lang.String.format("%s.lExt", label);
		}

		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
	}
	
	static public class RightExtent extends Expression {
		
		private static final long serialVersionUID = 1L;

		static public java.lang.String format = "%s.rExt";
		
		private final java.lang.String label;
        private final int i;
		
		RightExtent(java.lang.String label) {
			this(label, -1);
		}

        RightExtent(java.lang.String label, int i) {
            this.label = label;
            this.i = i;
        }
		
		public java.lang.String getLabel() {
			return label;
		}

		@Override
		public Object interpret(IEvaluatorContext ctx) {
            Object value = ctx.lookup(i);
			return ((SPPFNode) value).getRightExtent();
		}
		
		@Override
		public java.lang.String toString() {
			return i != -1 ? java.lang.String.format("%s:%d.rExt", label, i) : java.lang.String.format("%s.rExt", label);
		}

		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
	}
	
	static public class Yield extends Expression {
		private static final long serialVersionUID = 1L;

		static public java.lang.String format = "%s.yield";
		
		private final java.lang.String label;
        private final int i;
		
		Yield(java.lang.String label) {
			this(label, -1);
		}
        Yield(java.lang.String label, int i) {
            this.label = label;
            this.i = i;
        }
		
		public java.lang.String getLabel() {
			return label;
		}
		
		@Override
		public Object interpret(IEvaluatorContext ctx) {
            NonPackedNode value = (NonPackedNode) ctx.lookup(i);
            return ctx.getInput().subString(value.getLeftExtent(), value.getRightExtent());
		}
		
		@Override
		public java.lang.String toString() {
			return i == -1 ? java.lang.String.format("%s.yield", label) : java.lang.String.format("%s:%d.yield", label, i);
		}

		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
	}
	
	static public class Val extends Expression {
		private static final long serialVersionUID = 1L;

		static public java.lang.String format = "%s.val";
		
		private final java.lang.String label;
        private final int i;
		
		Val(java.lang.String label) {
			this(label, -1);
		}

        Val(java.lang.String label, int i) {
            this.label = label;
            this.i = i;
        }
		
		public java.lang.String getLabel() {
			return label;
		}
		
		@Override
		public Object interpret(IEvaluatorContext ctx) {
            NonterminalNode value = (NonterminalNode) ctx.lookup(i);
			return value.getValue();
		}
		
		@Override
		public java.lang.String toString() {
			return i != -1 ? java.lang.String.format("%s:%d.val", label, i) : java.lang.String.format("%s.val", label);
		}

		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
	}
	
	static public class EndOfFile extends Expression {
		
		private static final long serialVersionUID = 1L;
		
		private final Expression index;
		
		EndOfFile(Expression index) {
			this.index = index;
		}
		
		public Expression getIndex() {
			return index;
		}

		@Override
		public Object interpret(IEvaluatorContext ctx) {
			int index = (java.lang.Integer) this.index.interpret(ctx);
			int length = ctx.getInput().length();
			return length == index + 1;
		}

		@Override
		public java.lang.String toString() {
			return java.lang.String.format("$(%s)", index);
		}

		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
		
	}
	
	static public class IfThenElse extends Expression {
		
		private static final long serialVersionUID = 1L;
		
		private final Expression condition;
		private final Expression thenPart;
		private final Expression elsePart;
		
		IfThenElse(Expression condition, Expression thenPart, Expression elsePart) {
			this.condition = condition;
			this.thenPart = thenPart;
			this.elsePart = elsePart;
		}
		
		public Expression getCondition() {
			return condition;
		}
		
		public Expression getThenPart() {
			return thenPart;
		}
		
		public Expression getElsePart() {
			return elsePart;
		}

		@Override
		public Object interpret(IEvaluatorContext ctx) {
			boolean cond = (java.lang.Boolean) condition.interpret(ctx);
			if (cond)
				return thenPart.interpret(ctx);
			else 
				return elsePart.interpret(ctx);
		}

		@Override
		public java.lang.String toString() {
			return java.lang.String.format("(%s)? %s : %s", condition, thenPart, elsePart);
		}

		@Override
		public <T> T accept(IAbstractASTVisitor<T> visitor) {
			return visitor.visit(this);
		}
	}
	
}
