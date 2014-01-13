package org.jgll.grammar.symbol;


public class Star extends Nonterminal {

	private static final long serialVersionUID = 1L;
	
	private Symbol s;
	
	public Star(Symbol s) {
		super(s.getName() + "*");
		this.s = s;
	}
	
	public Symbol getSymbol() {
		return s;
	}
	
}
