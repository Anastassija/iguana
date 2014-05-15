package org.jgll.regex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgll.grammar.condition.Condition;
import org.jgll.grammar.symbol.AbstractRegularExpression;
import org.jgll.grammar.symbol.Range;
import org.jgll.grammar.symbol.SymbolBuilder;
import org.jgll.regex.automaton.Automaton;
import org.jgll.regex.automaton.State;
import org.jgll.regex.automaton.StateType;
import org.jgll.regex.automaton.Transition;
import org.jgll.util.CollectionsUtil;

public class Sequence<T extends RegularExpression> extends AbstractRegularExpression implements Iterable<T> {

	private static final long serialVersionUID = 1L;

	private final List<T> regularExpressions;

	@SuppressWarnings("unchecked")
	public Sequence(List<T> regularExpressions, String label, Set<Condition> conditions, Object object) {
		super(getName(regularExpressions), label, conditions, object);
		
		if(regularExpressions.size() == 0) throw new IllegalArgumentException("The number of regular expressions in a sequence should be at least one.");
		
		List<T> list = new ArrayList<>();
		
		int i;
		for (i = 0; i < regularExpressions.size(); i++) {
			list.add((T) regularExpressions.get(i).withoutConditions());
		}
		
		this.regularExpressions = list;
	}
	
	public static <T extends RegularExpression> Sequence<T> from(List<T> regularExpressions) {
		return new Builder<>(regularExpressions).build();
	}
	
	@SafeVarargs
	public static <T extends RegularExpression> Sequence<T> from(T...regularExpressions) {
		return from(Arrays.asList(regularExpressions));
	}
	
	private static <T> String getName(List<T> regularExpressions) {
		return CollectionsUtil.listToString(regularExpressions, " ");
	}
		
	public List<T> getRegularExpressions() {
		return regularExpressions;
	}

	@Override
	protected Automaton createAutomaton() {
		
		List<Automaton> automatons = new ArrayList<>();
		
		for(int i = 0; i < regularExpressions.size(); i++) {
			automatons.add(regularExpressions.get(i).getAutomaton().copy());
		}
				
		Automaton result = automatons.get(0);
		State startState = result.getStartState();
		
		for (int i = 1; i < automatons.size(); i++) {
			Automaton next = automatons.get(i);
			
			for(State s : result.getFinalStates()) {
				s.setStateType(StateType.NORMAL);
				s.addTransition(Transition.epsilonTransition(next.getStartState()));
			}
			
			result = new Automaton(startState, this.name);
		}
		
		return result;
	}
	
	@Override
	public boolean isNullable() {
		for(RegularExpression regex : regularExpressions) {
			if(!regex.isNullable()) {
				return false;
			}
		}
		return true;
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
		
		if(!(obj instanceof Sequence)) {
			return false;
		}
		
		@SuppressWarnings("unchecked")
		Sequence<T> other = (Sequence<T>) obj;
		
		return regularExpressions.equals(other.regularExpressions);
	}
	
	@Override
	public int hashCode() {
		return regularExpressions.hashCode();
	}

	@Override
	public Iterator<T> iterator() {
		return regularExpressions.iterator();
	}
	
	@Override
	public String toString() {
		return getName();
	}

	@Override
	public Set<Range> getFirstSet() {
		Set<Range> firstSet = new HashSet<>();
		for(T t : regularExpressions) {
			firstSet.addAll(t.getFirstSet());
			if(!t.isNullable()) {
				break;
			}
		}
		return firstSet;
	}
	
	@Override
	public Set<Range> getNotFollowSet() {
		return Collections.emptySet();
	}

	@Override
	public Sequence<T> withConditions(Set<Condition> conditions) {
		return new Builder<>(regularExpressions).addConditions(this.conditions).addConditions(conditions).build();
	}
	
	@Override
	public Sequence<T> withoutConditions() {
		return Sequence.from(regularExpressions);
	}
	
	public static class Builder<T extends RegularExpression> extends SymbolBuilder<Sequence<T>> {

		private List<T> regularExpressions;

		public Builder(List<T> regularExpressions) {
			this.regularExpressions = regularExpressions;
		}
		
		@Override
		public Sequence<T> build() {
			return new Sequence<>(regularExpressions, label, conditions, object);
		}
		
	}
	
}
