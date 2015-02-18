package org.jgll.datadependent.traversal;

import java.util.Set;

import org.eclipse.imp.pdb.facts.util.ImmutableSet;
import org.jgll.datadependent.ast.Expression.Assignment;
import org.jgll.datadependent.ast.Expression.Boolean;
import org.jgll.datadependent.ast.Expression.Call;
import org.jgll.datadependent.ast.Expression.Equal;
import org.jgll.datadependent.ast.Expression.Greater;
import org.jgll.datadependent.ast.Expression.GreaterThanEqual;
import org.jgll.datadependent.ast.Expression.Integer;
import org.jgll.datadependent.ast.Expression.LeftExtent;
import org.jgll.datadependent.ast.Expression.Less;
import org.jgll.datadependent.ast.Expression.Name;
import org.jgll.datadependent.ast.Expression.Real;
import org.jgll.datadependent.ast.Expression.RightExtent;
import org.jgll.datadependent.ast.Expression.String;
import org.jgll.datadependent.ast.Statement;
import org.jgll.datadependent.ast.Statement.Expression;
import org.jgll.datadependent.ast.VariableDeclaration;
import org.jgll.grammar.condition.Condition;
import org.jgll.grammar.condition.ContextFreeCondition;
import org.jgll.grammar.condition.DataDependentCondition;
import org.jgll.grammar.condition.PositionalCondition;
import org.jgll.grammar.condition.RegularExpressionCondition;
import org.jgll.grammar.symbol.Align;
import org.jgll.grammar.symbol.Block;
import org.jgll.grammar.symbol.Character;
import org.jgll.grammar.symbol.CharacterRange;
import org.jgll.grammar.symbol.Code;
import org.jgll.grammar.symbol.Conditional;
import org.jgll.grammar.symbol.EOF;
import org.jgll.grammar.symbol.Epsilon;
import org.jgll.grammar.symbol.IfThen;
import org.jgll.grammar.symbol.IfThenElse;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Offside;
import org.jgll.grammar.symbol.Symbol;
import org.jgll.grammar.symbol.Terminal;
import org.jgll.grammar.symbol.While;
import org.jgll.regex.Alt;
import org.jgll.regex.Opt;
import org.jgll.regex.Plus;
import org.jgll.regex.Sequence;
import org.jgll.regex.Star;
import org.jgll.traversal.IConditionVisitor;
import org.jgll.traversal.ISymbolVisitor;


public class FreeVariableVisitor implements IAbstractASTVisitor<Void>, ISymbolVisitor<Void>, IConditionVisitor<Void> {
	
	private final Set<java.lang.String> freeVariables;
	
	public FreeVariableVisitor(Set<java.lang.String> freeVariables) {
		this.freeVariables = freeVariables;
	}

	@Override
	public Void visit(Boolean expression) {
		return null;
	}

	@Override
	public Void visit(Integer expression) {
		return null;
	}

	@Override
	public Void visit(Real expression) {
		return null;
	}

	@Override
	public Void visit(String expression) {
		return null;
	}

	@Override
	public Void visit(Name expression) {
		java.lang.String name = expression.getName();
		
		if (!expression.getEnv().contains(name)) {
			freeVariables.add(name);
		}
		
		return null;
	}

	@Override
	public Void visit(Call expression) {
		
		for (org.jgll.datadependent.ast.Expression argument : expression.getArguments()) {
			argument.setEnv(expression.getEnv());
			argument.accept(this);
		}
		
		return null;
	}

	@Override
	public Void visit(Assignment expression) {
		
		java.lang.String id = expression.getId();
		org.jgll.datadependent.ast.Expression exp = expression.getExpression();
		
		if (!expression.getEnv().contains(id)) {
			freeVariables.add(id);
		}
		
		exp.setEnv(expression.getEnv());
		exp.accept(this);
		
		return null;
	}

	@Override
	public Void visit(Less expression) {
		
		org.jgll.datadependent.ast.Expression lhs = expression.getLhs();
		
		lhs.setEnv(expression.getEnv());
		lhs.accept(this);
		
		org.jgll.datadependent.ast.Expression rhs = expression.getRhs();
		
		rhs.setEnv(expression.getEnv());
		rhs.accept(this);
		
		return null;
	}

	@Override
	public Void visit(Greater expression) {
		
		org.jgll.datadependent.ast.Expression lhs = expression.getLhs();
		
		lhs.setEnv(expression.getEnv());
		lhs.accept(this);
		
		org.jgll.datadependent.ast.Expression rhs = expression.getRhs();
		
		rhs.setEnv(expression.getEnv());
		rhs.accept(this);
		
		return null;
	}

	@Override
	public Void visit(GreaterThanEqual expression) {	
		
		org.jgll.datadependent.ast.Expression lhs = expression.getLhs();
		
		lhs.setEnv(expression.getEnv());
		lhs.accept(this);
		
		org.jgll.datadependent.ast.Expression rhs = expression.getRhs();
		
		rhs.setEnv(expression.getEnv());
		rhs.accept(this);
		
		return null;
	}

	@Override
	public Void visit(Equal expression) {
		
		org.jgll.datadependent.ast.Expression lhs = expression.getLhs();
		
		lhs.setEnv(expression.getEnv());
		lhs.accept(this);
		
		org.jgll.datadependent.ast.Expression rhs = expression.getRhs();
		
		rhs.setEnv(expression.getEnv());
		rhs.accept(this);
		
		return null;
	}

	@Override
	public Void visit(LeftExtent expression) {
		
		java.lang.String name = java.lang.String.format(org.jgll.datadependent.ast.Expression.LeftExtent.format, expression.getLabel());
		
		if (!expression.getEnv().contains(name)) {
			freeVariables.add(name);
		}
		return null;
	}

	@Override
	public Void visit(RightExtent expression) {
		
		java.lang.String label = expression.getLabel();
		
		if (!expression.getEnv().contains(label)) {
			freeVariables.add(label);
		}
		return null;
	}

	@Override
	public Void visit(VariableDeclaration declaration) {
		
		org.jgll.datadependent.ast.Expression expression = declaration.getExpression();
		
		if (expression != null) {
			expression.setEnv(declaration.getEnv());
			expression.accept(this);
		}
		
		declaration.setEnv(declaration.getEnv().__insert(declaration.getName()));
		
		return null;
	}

	@Override
	public Void visit(org.jgll.datadependent.ast.Statement.VariableDeclaration statement) {
		
		VariableDeclaration declaration = statement.getDeclaration();
		
		declaration.setEnv(statement.getEnv());
		declaration.accept(this);
		
		statement.setEnv(declaration.getEnv());
		
		return null;
	}

	@Override
	public Void visit(Expression statement) {
		
		org.jgll.datadependent.ast.Expression expression = statement.getExpression();
		
		expression.setEnv(statement.getEnv());
		expression.accept(this);
		
		return null;
	}
	
	@Override
	public Void visit(Align symbol) {
		Symbol sym = symbol.getSymbol();
		
		sym.setEnv(symbol.getEnv());
		visitSymbol(sym);
		
		symbol.setEnv(sym.getEnv());
		
		return null;
	}

	@Override
	public Void visit(Block symbol) {
		
		ImmutableSet<java.lang.String> env = symbol.getEnv();
		
		for (Symbol sym : symbol.getSymbols()) {
			sym.setEnv(env);
			visitSymbol(sym);
			env = sym.getEnv();
		}
		
		return null;
	}

	@Override
	public Void visit(Character symbol) {
		return null;
	}

	@Override
	public Void visit(CharacterRange symbol) {
		return null;
	}

	@Override
	public Void visit(Code symbol) {
		
		ImmutableSet<java.lang.String> env = symbol.getEnv();
		
		Symbol sym = symbol.getSymbol();
		
		sym.setEnv(env);
		visitSymbol(sym);
		env = sym.getEnv();
		
		for (Statement statement : symbol.getStatements()) {
			statement.setEnv(env);
			statement.accept(this);
			env = statement.getEnv();
		}
		
		symbol.setEnv(env);
		
		return null;
	}
	
	@Override
	public Void visit(Conditional symbol) {
		
		Symbol sym = symbol.getSymbol();
		org.jgll.datadependent.ast.Expression expression = symbol.getExpression();
		
		ImmutableSet<java.lang.String> env = symbol.getEnv();
		
		sym.setEnv(env);
		visitSymbol(sym);
		
		env = sym.getEnv();
		
		expression.setEnv(env);
		expression.accept(this);
		
		return null;
	}

	@Override
	public Void visit(EOF symbol) {
		return null;
	}

	@Override
	public Void visit(Epsilon symbol) {
		return null;
	}
	
	@Override
	public Void visit(IfThen symbol) {
		
		org.jgll.datadependent.ast.Expression expression = symbol.getExpression();
		Symbol thenPart = symbol.getThenPart();
		
		expression.setEnv(symbol.getEnv());
		expression.accept(this);
		
		thenPart.setEnv(symbol.getEnv());
		visitSymbol(thenPart);
		
		return null;
	}

	@Override
	public Void visit(IfThenElse symbol) {
		
		org.jgll.datadependent.ast.Expression expression = symbol.getExpression();
		Symbol thenPart = symbol.getThenPart();
		Symbol elsePart = symbol.getElsePart();
		
		expression.setEnv(symbol.getEnv());
		expression.accept(this);
		
		thenPart.setEnv(symbol.getEnv());
		visitSymbol(thenPart);
		
		elsePart.setEnv(symbol.getEnv());
		visitSymbol(thenPart);
		
		return null;
	}

	@Override
	public Void visit(Nonterminal symbol) {
		
		ImmutableSet<java.lang.String> env = symbol.getEnv();
		
		if (symbol.getVariable() != null) {
			env = env.__insert(symbol.getVariable());
		}
		
		symbol.setEnv(env);
		
		return null;
	}

	@Override
	public Void visit(Offside symbol) {
		
		Symbol sym = symbol.getSymbol();
		
		sym.setEnv(symbol.getEnv());
		visitSymbol(sym);
		
		symbol.setEnv(sym.getEnv());
		
		return null;
	}
	
	@Override
	public Void visit(Terminal symbol) {
		return null;
	}
	
	@Override
	public Void visit(While symbol) {
		
		org.jgll.datadependent.ast.Expression expression = symbol.getExpression();
		Symbol body = symbol.getBody();
		
		expression.setEnv(symbol.getEnv());
		expression.accept(this);
		
		body.setEnv(symbol.getEnv());
		visitSymbol(body);
		
		return null;
	}

	@Override
	public <E extends Symbol> Void visit(Alt<E> symbol) {
		
		ImmutableSet<java.lang.String> env = symbol.getEnv();
		
		for (Symbol sym : symbol.getSymbols()) {
			sym.setEnv(env);
			visitSymbol(sym);
		}
		
		return null;
	}

	@Override
	public Void visit(Opt symbol) {
		
		Symbol sym = symbol.getSymbol();
		
		sym.setEnv(symbol.getEnv());
		visitSymbol(sym);
		
		return null;
	}

	@Override
	public Void visit(Plus symbol) {
		
		Symbol sym = symbol.getSymbol();
		
		sym.setEnv(symbol.getEnv());
		visitSymbol(sym);
		
		return null;
	}

	@Override
	public <E extends Symbol> Void visit(Sequence<E> symbol) {
		
		ImmutableSet<java.lang.String> env = symbol.getEnv();
		
		for (E sym : symbol.getSymbols()) {
			sym.setEnv(env);
			visitSymbol(sym);
			env = sym.getEnv();
		}
		
		return null;
	}

	@Override
	public Void visit(Star symbol) {
		
		Symbol sym = symbol.getSymbol();
		
		sym.setEnv(symbol.getEnv());
		visitSymbol(sym);
		
		return null;
	}
	
	/**
	 * 
	 * Accounts for optional label and optional preconditions and postconditions
	 */
	public Void visitSymbol(Symbol symbol) {
		
		ImmutableSet<java.lang.String> env = symbol.getEnv();
		
		if (symbol.getLabel() != null) {
			env = env.__insert(java.lang.String.format(LeftExtent.format, symbol.getLabel()));
		}
		
		for (Condition condition : symbol.getPreConditions()) {
			condition.setEnv(env);
			condition.accept(this);
		}
		
		symbol.setEnv(env);
		symbol.accept(this);
		
		env = symbol.getEnv();
		
		if (symbol.getLabel() != null) {
			env = env.__insert(symbol.getLabel());
		}
		
		for (Condition condition : symbol.getPostConditions()) {
			condition.setEnv(env);
			condition.accept(this);
		}
		
		symbol.setEnv(env);
		
		return null;
	}

	@Override
	public Void visit(ContextFreeCondition condition) {
		throw new UnsupportedOperationException("Context-free condition");
	}

	@Override
	public Void visit(DataDependentCondition condition) {
		
		org.jgll.datadependent.ast.Expression expression = condition.getExpression();
		
		expression.setEnv(condition.getEnv());
		expression.accept(this);
		
		return null;
	}

	@Override
	public Void visit(PositionalCondition condition) {
		return null;
	}

	@Override
	public Void visit(RegularExpressionCondition condition) {
		return null;
	}
	
}
