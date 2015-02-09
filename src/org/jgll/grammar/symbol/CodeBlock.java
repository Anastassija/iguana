package org.jgll.grammar.symbol;

import java.util.HashSet;
import java.util.Set;

import org.jgll.datadependent.attrs.AbstractAttrs;
import org.jgll.grammar.condition.Condition;
import org.jgll.traversal.ISymbolVisitor;
import org.jgll.util.generator.GeneratorUtil;

public class CodeBlock extends AbstractAttrs implements Symbol {

	private static final long serialVersionUID = 1L;
	
	private final org.jgll.datadependent.ast.Statement[] statements;
	
	CodeBlock(org.jgll.datadependent.ast.Statement[] statements) {
		this.statements = statements;
	}
	
	public static CodeBlock code(org.jgll.datadependent.ast.Statement... statements) {
		return new CodeBlock(statements);
	}
	
	public  org.jgll.datadependent.ast.Statement[] getStatements() {
		return statements;
	}

	@Override
	public String getConstructorCode() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public Set<Condition> getPreConditions() {
		return new HashSet<>();
	}

	@Override
	public Set<Condition> getPostConditions() {
		return new HashSet<>();
	}

	@Override
	public Object getObject() {
		return null;
	}

	@Override
	public String getLabel() {
		return null;
	}

	@Override
	public SymbolBuilder<? extends Symbol> copyBuilder() {
		return null;
	}
	
	@Override
	public String toString() {
		return GeneratorUtil.listToString(statements, ";");
	}

	@Override
	public <T> T accept(ISymbolVisitor<T> visitor) {
		return visitor.visit(this);
	}

}
