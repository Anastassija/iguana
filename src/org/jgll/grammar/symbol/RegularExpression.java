package org.jgll.grammar.symbol;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgll.grammar.condition.Condition;
import org.jgll.grammar.condition.ConditionType;
import org.jgll.util.CollectionsUtil;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;


public class RegularExpression extends AbstractSymbol {

	private static final long serialVersionUID = 1L;
	
	private RunAutomaton automaton;

	private List<? extends Symbol> symbols;
	
	public RegularExpression(List<? extends Symbol> symbols) {
		// This is probably not the best design. We need a separate interface for
		// regular expressions and their compositions.
		// For now, if the regular expression is a simple sequence, the regular
		// expression gets the conditions from the first and the last one.
		// If it is a group, we still need to get the precede restrictions of the 
		// first one. Follow restrictions are ignored, as we have the longest match
		// anyway.
		this.symbols = symbols;
	
		if(symbols.size() == 1 && symbols.get(0) instanceof Group) {
			Group group = (Group) symbols.get(0);
			conditions.addAll(group.getConditions());
			addConditions(group.getSymbols());
		} else {
			addConditions(symbols);
		}
		
		System.out.println(toBricsDFA());
		this.automaton = new RunAutomaton(new RegExp(toBricsDFA()).toAutomaton());
	}
	
	private void addConditions(List<? extends Symbol> symbols) {
		conditions.addAll(symbols.get(0).getConditions());
		if(symbols.size() > 1) {
			for(Condition condition : symbols.get(symbols.size() - 1).getConditions()) {
				if(condition.getType() != ConditionType.NOT_FOLLOW) {
					conditions.add(condition);
				}
			}
		}		
	}
	
	public RunAutomaton getAutomaton() {
		return automaton;
	}
	
	public List<? extends Symbol> getSymbols() {
		return symbols;
	}
	
	@Override
	public String getName() {
		return CollectionsUtil.listToString(symbols);
	}

	@Override
	public RegularExpression addConditions(Collection<Condition> conditions) {
		RegularExpression regex = new RegularExpression(this.symbols);
		regex.conditions.addAll(this.conditions);
		regex.conditions.addAll(conditions);
		return regex;
	}
	
	public Set<Terminal> getFirstTerminal() {
		Symbol firstSymbol = symbols.get(0);
		
		Set<Terminal> set = new HashSet<>();
		
		getFirstTerminal(set, firstSymbol);

		return set;
	}
	
	private void getFirstTerminal(Set<Terminal> set, Symbol symbol) {
		
		if(symbol instanceof Terminal) {
			set.add((Terminal) symbol);
		}
		
		else if(symbol instanceof Plus) {
			getFirstTerminal(set, (((Plus) symbol).getSymbol()));
		}
		
		else if(symbol instanceof Star) {
			getFirstTerminal(set, (((Star) symbol).getSymbol()));
		}

		else if(symbol instanceof Opt) {
			getFirstTerminal(set, (((Opt) symbol).getSymbol()));
		}
		
		else if(symbol instanceof Group) {
			List<? extends Symbol> list = ((Group) symbol).getSymbols();
			for(Symbol s : list) {
				getFirstTerminal(set, s);
				if(!isRegexpNullable(s)) {
					break;
				}
			}
		}
		
		else if(symbol instanceof Alt) {
			for(Symbol s : ((Alt) symbol).getSymbols()) {
				getFirstTerminal(set, s);
			}
		} 

		else {
			throw new IllegalStateException("Unsupported regular symbol: " + symbol);			
		}
	}
	
	public boolean isNullable() {
		for(Symbol s : symbols) {
			if(!isRegexpNullable(s)) {
				return false;
			}
		}
		return true;
	}
	
	private static boolean isRegexpNullable(Symbol symbol) {
		
		if(symbol instanceof Terminal) {
			return false;
		}
		else if(symbol instanceof Plus) {
			return false;
		}
		else if(symbol instanceof Star) {
			return true;
		}
		else if(symbol instanceof Opt) {
			return true;
		}
		else if(symbol instanceof Alt) {
			for(Symbol s : ((Alt) symbol).getSymbols()) {
				if(isRegexpNullable(s)) {
					return true;
				}
			}
			return false;
		}
		else if(symbol instanceof Group) {
			boolean nullable = false;
			for(Symbol s : ((Group) symbol).getSymbols()) {
				nullable |= isRegexpNullable(s);
			}
			return nullable;
		}
		else {
			throw new IllegalStateException("Unsupported regular symbol: " + symbol);			
		}
	}
	
	private String toBricsDFA() {
		
		StringBuilder sb = new StringBuilder();
		
		for(Symbol symbol : symbols) {
			symbolToString(symbol, sb);
		}
		
		return sb.toString();
	}
	
	private void symbolToString(Symbol symbol, StringBuilder sb) {
		if(symbol instanceof Terminal) {
			terminalToString((Terminal) symbol, sb);
		} 
		else if(symbol instanceof Plus) {
			symbolToString(((Plus) symbol).getSymbol(), sb);
			sb.append("+");
		} 
		else if(symbol instanceof Star) {
			symbolToString(((Star) symbol).getSymbol(), sb);
			sb.append("*");
		}
		else if(symbol instanceof Opt) {
			symbolToString(((Opt) symbol).getSymbol(), sb);
			sb.append("?");
		}
		else if(symbol instanceof Group) {
			sb.append("(");
			for(Symbol s : ((Group)symbol).getSymbols()) {
				symbolToString(s, sb);
			}
			sb.append(")");
		}
		else if(symbol instanceof Alt) {
			sb.append("(");
			for(Symbol s : ((Alt)symbol).getSymbols()) {
				symbolToString(s, sb);
				sb.append("|");
			}
			sb.delete(sb.length() - 1, sb.length());
			sb.append(")");
		}
	}

	private void terminalToString(Terminal symbol, StringBuilder sb) {
		if(symbol instanceof Character) {
			sb.append((char)((Character) symbol).get());
		}
		else if (symbol instanceof CharacterClass) {
			sb.append(symbol.toString());
		}
	}

	
}
