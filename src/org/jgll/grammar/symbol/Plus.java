package org.jgll.grammar.symbol;


public class Plus extends AbstractSymbol {

	private static final long serialVersionUID = 1L;
	
	private final Symbol s;
	
	public static Plus from(Symbol s) {
		return new Builder(s).build();
	}
	
	private Plus(Builder builder) {
		super(builder);
		this.s = builder.s;
	}
	
	public Symbol getSymbol() {
		return s;
	}
	
	public static Builder builder(Symbol s) {
		return new Builder(s);
	}
	
	@Override
	public String getConstructorCode() {
		return new StringBuilder()
		  .append("new Plus.builder(" + s.getConstructorCode() + ")")
		  .append(".setLabel(" + label + ")")
		  .append(".setObject(" + object + ")")
		  .append(".setPreConditions(" + getConstructorCode(preConditions) + ")")
		  .append(".setPostConditions(" + getConstructorCode(postConditions) + ")")
		  .append(".build()").toString();
	}

	@Override
	public SymbolBuilder<? extends Symbol> copyBuilder() {
		return new Builder(this);
	}
	
	public static class Builder extends SymbolBuilder<Plus> {

		private Symbol s;
		
		public Builder(Symbol s) {
			super(s.getName() + "+");
			this.s = s;
		}
		
		public Builder(Plus plus) {
			super(plus);
			this.s = plus.s;
		}
		
		@Override
		public Plus build() {
			return new Plus(this);
		}
	}
	
}
