package org.jgll.regex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgll.grammar.condition.Condition;
import org.jgll.grammar.symbol.AbstractRegularExpression;
import org.jgll.grammar.symbol.Range;
import org.jgll.regex.automaton.Automaton;
import org.jgll.regex.automaton.State;
import org.jgll.regex.automaton.Transition;
import org.jgll.util.CollectionsUtil;

public class RegexAlt<T extends RegularExpression> extends AbstractRegularExpression implements Iterable<T> {

	private static final long serialVersionUID = 1L;

	private final List<T> regularExpressions;
	
	@SuppressWarnings("unchecked")
	public RegexAlt(List<T> regularExpressions, Set<Condition> conditions) {
		super("(" + CollectionsUtil.listToString(regularExpressions, " | ") + ")", conditions);
		
		if(regularExpressions == null) throw new IllegalArgumentException("The list of regular expressions cannot be null.");
		if(regularExpressions.size() == 0) throw new IllegalArgumentException("The list of regular expressions cannot be empty.");

		List<T> list = new ArrayList<>();
		for (T regex : regularExpressions) {
			list.add((T) regex.withConditions(conditions));
		}
		
		this.regularExpressions = list;
	}
	
	@SuppressWarnings("unchecked")
	public RegexAlt(List<T> regularExpressions) {
		super("(" + CollectionsUtil.listToString(regularExpressions, " | ") + ")");
		
		if(regularExpressions == null) {
			throw new IllegalArgumentException("The list of regular expressions cannot be null.");
		}
		
		if(regularExpressions.size() == 0) {
			throw new IllegalArgumentException("The list of regular expressions cannot be empty.");
		}

		List<T> list = new ArrayList<>();
		for (T regex : regularExpressions) {
			list.add((T) regex.withConditions(conditions));
		}
		
		this.regularExpressions = list;
	}
	
	@SafeVarargs
	public RegexAlt(T...regularExpressions) {
		this(Arrays.asList(regularExpressions));
	}
	
	public List<T> getRegularExpressions() {
		return regularExpressions;
	}

	@Override
	protected Automaton createAutomaton() {

		List<Automaton> automatons = new ArrayList<>();
		for (RegularExpression regexp : regularExpressions) {
			automatons.add(regexp.getAutomaton());
		}
		
		State startState = new State();
		State finalState = new State(true);
		
		for (Automaton automaton : automatons) {
			startState.addTransition(Transition.epsilonTransition(automaton.getStartState()));
			
			Set<State> finalStates = automaton.getFinalStates();
			for (State s : finalStates) {
				s.setFinalState(false);
				s.addTransition(Transition.epsilonTransition(finalState));				
			}
		}
		
		return new Automaton(startState, name);
	}

	@Override
	public boolean isNullable() {
		for (RegularExpression regex : regularExpressions) {
			if (regex.isNullable()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Iterator<T> iterator() {
		return regularExpressions.iterator();
	}
	
	public int size() {
		return regularExpressions.size();
	}
	
	public T get(int index) {
		return regularExpressions.get(index);
	}
	
	@Override
	public boolean equals(Object obj) {
	
		if(obj == this) {
			return true;
		}
		
		if(!(obj instanceof RegexAlt)) {
			return false;
		}
		
		@SuppressWarnings("rawtypes")
		RegexAlt other = (RegexAlt) obj;
		
		return other.regularExpressions.equals(regularExpressions);
	}
	
	@Override
	public int hashCode() {
		return regularExpressions.hashCode();
	}

	@Override
	public Set<Range> getFirstSet() {
		Set<Range> firstSet = new HashSet<>();
		for (T t : regularExpressions) {
			firstSet.addAll(t.getFirstSet());
		}
		return firstSet;
	}

	@Override
	public RegexAlt<T> withConditions(Set<Condition> conditions) {
		return new RegexAlt<>(regularExpressions, conditions);
	}
	
}
