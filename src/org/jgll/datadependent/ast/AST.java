package org.jgll.datadependent.ast;

import org.jgll.datadependent.ast.Expression;
import org.jgll.datadependent.env.IEvaluatorContext;
import org.jgll.grammar.exception.UnexpectedTypeOfArgumentException;
import org.jgll.sppf.NonPackedNode;
import org.jgll.util.generator.GeneratorUtil;

public class AST {
	
	/**
	 * 		Expressions
	 */
	
	static public final Expression TRUE = Expression.Boolean.TRUE;
	static public final Expression FALSE = Expression.Boolean.FALSE;
	
	static public Expression integer(java.lang.Integer value) {
		return new Expression.Integer(value);
	}
	
	static public Expression real(java.lang.Float value) {
		return new Expression.Real(value);
	}
	
	static public Expression string(java.lang.String value) {
		return new Expression.String(value);
	}
	
	static public Expression var(java.lang.String name) {
		return new Expression.Name(name);
	}
	
	static public Expression println(Expression... args) {
		return new Expression.Call("println", args) {
					
			private static final long serialVersionUID = 1L;

					@Override
					public Object interpret(IEvaluatorContext ctx) {
						Object[] arguments = interpretArguments(ctx);
						for (Object argument : arguments) {
							System.out.print(argument);
							System.out.print("; ");
						}
						System.out.println();
						return null;
					}
					
					@Override
					public java.lang.String getConstructorCode() {
						java.lang.String[] codes = new java.lang.String[args.length];
						
						int j = 0;
						for (Expression arg : args) {
							codes[j] = arg.getConstructorCode();
							j++;
						}
						
						return "AST.println(" + GeneratorUtil.listToString(codes, ",") + ")";
					}
					
					@Override
					public java.lang.String toString() {
						return java.lang.String.format("%s(%s)", "println", GeneratorUtil.listToString(args, ","));
					}
		};
	}
	
	static public Expression indent(Expression arg) {
		return new Expression.Call("indent", arg) {
			
			private static final long serialVersionUID = 1L;

					@Override
					public Object interpret(IEvaluatorContext ctx) {
						Object value = arg.interpret(ctx);
						if (!(value instanceof java.lang.Integer)) {
							throw new UnexpectedTypeOfArgumentException(this);
						}
						
						return ctx.getInput().getColumnNumber((java.lang.Integer) value);
					}
					
					@Override
					public java.lang.String getConstructorCode() {
						return "AST.indent(" + arg.getConstructorCode() + ")";
					}
					
					@Override
					public java.lang.String toString() {
						return java.lang.String.format("indent(%s)", arg);
					}
		};
	}
	
	static public Expression ppDeclare(Expression variable, Expression value) {
		return new Expression.Call("ppDeclare", variable, value) {
			
			private static final long serialVersionUID = 1L;

					@Override
					public Object interpret(IEvaluatorContext ctx) {
						
						Object var = variable.interpret(ctx);
						
						if (!(var instanceof NonPackedNode))
							throw new UnexpectedTypeOfArgumentException(this);
						
						NonPackedNode node = (NonPackedNode) var;
						
						ctx.declareGlobalVariable(ctx.getInput().subString(node.getLeftExtent(), node.getRightExtent()), 
								                  value.interpret(ctx));
						
						return null;
					}
					
					@Override
					public java.lang.String getConstructorCode() {
						return "AST.ppDeclare(" + variable.getConstructorCode() + ", " + value.getConstructorCode() + ")";
					}
					
					@Override
					public java.lang.String toString() {
						return java.lang.String.format("ppDeclare(%s,%s)", variable, value);
					}
		};
	}
	
	static public Expression ppLookup(Expression arg) {
		return new Expression.Call("ppLookup", arg) {
			
			private static final long serialVersionUID = 1L;

					@Override
					public Object interpret(IEvaluatorContext ctx) {
						Object value = arg.interpret(ctx);
						if (!(value instanceof NonPackedNode)) {
							throw new UnexpectedTypeOfArgumentException(this);
						}
						
						NonPackedNode node = (NonPackedNode) value;
						
						java.lang.String subString = ctx.getInput().subString(node.getLeftExtent(), node.getRightExtent());
						
						if (subString.equals("true"))
							return true;
						else if (subString.equals("false"))
							return false;
						
						Object obj = ctx.lookupGlobalVariable(subString);
						return obj != null? obj : false;
					}
					
					@Override
					public java.lang.String getConstructorCode() {
						return "AST.ppLookup(" + arg.getConstructorCode() + ")";
					}
					
					@Override
					public java.lang.String toString() {
						return java.lang.String.format("ppLookup(%s)", arg);
					}
		};
	}
	
	static public Expression endsWith(Expression index, Expression character) {
		return new Expression.Call("endsWith", index, character) {
			
					private static final long serialVersionUID = 1L;

					@Override
					public Object interpret(IEvaluatorContext ctx) {
						Object i = index.interpret(ctx);
						if (!(i instanceof java.lang.Integer)) {
							throw new UnexpectedTypeOfArgumentException(this);
						}
						
						int j = (java.lang.Integer) i;
						
						Object c = character.interpret(ctx);
						
						if (!(c instanceof java.lang.String)) {
							throw new UnexpectedTypeOfArgumentException(this);
						}
						
						Object obj = ctx.getInput().subString(j - 1, j);
						return obj.equals(c);
					}
					
					@Override
					public java.lang.String getConstructorCode() {
						return "AST.endsWith(" + index.getConstructorCode() + "," + character.getConstructorCode() + ")";
					}
					
					@Override
					public java.lang.String toString() {
						return java.lang.String.format("endsWith(%s,\"%s\")", index, character);
					}
		};
	}
	
	static public Expression startsWith(Expression index, Expression string) {
		return new Expression.Call("startsWith", index, string) {
			
					private static final long serialVersionUID = 1L;

					@Override
					public Object interpret(IEvaluatorContext ctx) {
						Object i = index.interpret(ctx);
						if (!(i instanceof java.lang.Integer)) {
							throw new UnexpectedTypeOfArgumentException(this);
						}
						
						int j = (java.lang.Integer) i;
						
						Object str = string.interpret(ctx);
						
						if (!(str instanceof java.lang.String)) {
							throw new UnexpectedTypeOfArgumentException(this);
						}
						
						Object obj = ctx.getInput().subString(j, j + ((java.lang.String) str).length());
						return obj.equals(str);
					}
					
					@Override
					public java.lang.String getConstructorCode() {
						return "AST.startsWith(" + index.getConstructorCode() + "," + string.getConstructorCode() + ")";
					}
					
					@Override
					public java.lang.String toString() {
						return java.lang.String.format("startsWith(%s,\"%s\")", index, string);
					}
		};
	}
	
	static public Expression lShiftANDEqZero(Expression lhs, Expression rhs) {
		return new Expression.LShiftANDEqZero(lhs, rhs);
	}
	
	static public Expression orIndent(Expression index, Expression ind, Expression first, Expression lExt) {
		return new Expression.OrIndent(index, ind, first, lExt);
	}
	
	static public Expression andIndent(Expression index, Expression first, Expression lExt) {
		return new Expression.AndIndent(index, first, lExt);
	}
	
	static public Expression andIndent(Expression index, Expression first, Expression lExt, boolean returnIndex) {
		return new Expression.AndIndent(index, first, lExt, returnIndex);
	}
	
	static public Expression or(Expression lhs, Expression rhs) {
		return new Expression.Or(lhs, rhs);
	}
	
	static public Expression less(Expression lhs, Expression rhs) {
		return new Expression.Less(lhs, rhs);
	}
	
	static public Expression lessEq(Expression lhs, Expression rhs) {
		return new Expression.LessThanEqual(lhs, rhs);
	}
	
	static public Expression greater(Expression lhs, Expression rhs) {
		return new Expression.Greater(lhs, rhs);
	}
	
	static public Expression greaterEq(Expression lhs, Expression rhs) {
		return new Expression.GreaterThanEqual(lhs, rhs);
	}
	
	static public Expression equal(Expression lhs, Expression rhs) {
		return new Expression.Equal(lhs, rhs);
	}
	
	static public Expression notEqual(Expression lhs, Expression rhs) {
		return new Expression.NotEqual(lhs, rhs);
	}
	
	static public Expression lExt(String label) {
		return new Expression.LeftExtent(label);
	}
	
	static public Expression rExt(String label) {
		return new Expression.RightExtent(label);
	}
	
	static public Expression endOfFile(Expression index) {
		return new Expression.EndOfFile(index);
	}
	
	/**
	 * 
	 * 		Statements
	 */
	
	static public Expression assign(java.lang.String id, Expression exp) {
		return new Expression.Assignment(id, exp);
	}
	
	static public Statement stat(Expression exp) {
		return new Statement.Expression(exp);
	}
	
	static public Statement varDeclStat(String name) {
		return new Statement.VariableDeclaration(new VariableDeclaration(name));
	}
	
	static public Statement varDeclStat(String name, Expression exp) {
		return new Statement.VariableDeclaration(new VariableDeclaration(name, exp));
	}
	
	static public Statement varDeclStat(VariableDeclaration varDecl) {
		return new Statement.VariableDeclaration(varDecl);
	}
	
	static public VariableDeclaration varDecl(String name) {
		return new VariableDeclaration(name);
	}
	
	static public VariableDeclaration varDecl(String name, Expression exp) {
		return new VariableDeclaration(name, exp);
	}

}
