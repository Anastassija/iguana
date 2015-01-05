package org.jgll.grammar.symbol;

import org.jgll.datadependent.ast.Expression;
import org.jgll.grammar.GrammarRegistry;
import org.jgll.grammar.condition.Condition;
import org.jgll.parser.HashFunctions;

public class Nonterminal extends AbstractSymbol {

	private static final long serialVersionUID = 1L;
	
	private final boolean ebnfList;
	
	private final int index;
	
	private final String variable;
	
	private final String[] parameters;
	
	private final Expression[] arguments;
	
	public static Nonterminal withName(String name) {
		return builder(name).build();
	}
	
	private Nonterminal(Builder builder) {
		super(builder);
		this.ebnfList = builder.ebnfList;
		this.index = builder.index;
		this.variable = builder.variable;
		this.parameters = builder.parameters;
		this.arguments = builder.arguments;
	}
	
	public boolean isEbnfList() {
		if (ebnfList == true) {
			return true;
		} else {
			if(name.startsWith("List")) {
				return true;
			}
		}

		return false;
	}
	
	public int getIndex() {
		return index;
	}
	
	public String getVariable() {
		return variable;
	}
	
	public String[] getParameters() {
		return parameters;
	}
	
	public Expression[] getArguments() {
		return arguments;
	}
	
	@Override
	public String toString() {
		return (variable != null? variable + " = " : "")
			    + (label != null? label + ":" : "")
			    + name + (index > 0 ? index : "")
			    + (parameters != null? "(" + "..." + ")" : "")
		        + (arguments != null? "(" + "..." + ")" : "");
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		
		if(!(obj instanceof Nonterminal))
			return false;
		
		Nonterminal other = (Nonterminal) obj;
		
		return name.equals(other.name) && index == other.index;
	}
	
	@Override
	public int hashCode() {
		return HashFunctions.defaulFunction.hash(name.hashCode(), index);
	}
	
	public static Builder builder(String name) {
		return new Builder(name);
	}
	
	public static Builder builder(Nonterminal nonterminal) {
		return new Builder(nonterminal);
	}

	public static class Builder extends SymbolBuilder<Nonterminal> {

		private boolean ebnfList;
		
		private int index;
		
		private String variable;
		
		private String[] parameters;
		
		private Expression[] arguments;
		
		public Builder(Nonterminal nonterminal) {
			super(nonterminal);
			this.name = nonterminal.name;
			this.ebnfList = nonterminal.ebnfList;
			this.index = nonterminal.index;
			this.parameters = nonterminal.parameters;
			this.arguments = nonterminal.arguments;
		}

		public Builder(String name) {
			super(name);
		}
		
		public Builder setIndex(int index) {
			this.index = index;
			return this;
		}
		
		public Builder setVariable(String variable) {
			this.variable = variable;
			return this;
		}
		
		public Builder setEbnfList(boolean ebnfList) {
			this.ebnfList = ebnfList;
			return this;
		}
		
		public Builder addParameters(String... parameters) {
			this.parameters = parameters;
			return this;
		}
		
		public Builder applyTo(Expression... arguments) {
			this.arguments = arguments;
			return this;
		}
		
		@Override
		public Builder setLabel(String label) {
			super.setLabel(label);
			return this;
		}
		
		@Override
		public Builder addPreCondition(Condition condition) {
			preConditions.add(condition);
			return this;
		}
		
		@Override
		public Builder addPostCondition(Condition condition) {
			postConditions.add(condition);
			return this;
		}	
		
		@Override
		public Builder setObject(Object object) {
			this.object = object;
			return this;
		}
		
		@Override
	 	public Builder addPreConditions(Iterable<Condition> conditions) {
	 		conditions.forEach(c -> preConditions.add(c));
			return this;
		}
	 	
		@Override
	 	public Builder addPostConditions(Iterable<Condition> conditions) {
	 		conditions.forEach(c -> postConditions.add(c));
			return this;
		}
		
		@Override
		public Nonterminal build() {
			return new Nonterminal(this);
		}
	}

	@Override
	public String getConstructorCode(GrammarRegistry registry) {
		return new StringBuilder()
		  .append("Nonterminal.builder(\"" + name + "\")")
		  .append(label == null? "" : ".setLabel(" + label + ")")
		  .append(object == null? "" : ".setObject(" + object + ")")
		  .append(preConditions.isEmpty()? "" : ".setPreConditions(" + getConstructorCode(preConditions, registry) + ")")
		  .append(postConditions.isEmpty()? "" : ".setPostConditions(" + getConstructorCode(postConditions, registry) + ")")
		  .append(index == 0 ? "" : ".setIndex(" + index + ")")
		  .append(ebnfList == false? "" : ".setEbnfList(" + ebnfList + ")")
		  .append(".build()").toString();
	}
	
}
