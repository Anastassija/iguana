package org.jgll.regex.automaton;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgll.grammar.symbol.CharacterRange;
import org.jgll.regex.RegularExpression;

public class State implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Set<Transition> transitions;
	
	/**
	 * The state that is reachable using the epsilon transition
	 */
	private final Set<State> epsilonStates;
	
	private final Map<CharacterRange, State> reachableStates;
			
	/**
	 * The set of regular expressions whose final state is this state.
	 */
	private Set<RegularExpression> regularExpressions;
	
	private StateType stateType = StateType.NORMAL;
	
	private int id;
	
	public State() {
		this(StateType.NORMAL);
	}
	
	public State(StateType stateType) {
		this.transitions = new HashSet<>();
		this.epsilonStates = new HashSet<>();
		this.reachableStates = new HashMap<>();
		this.stateType = stateType;
		this.regularExpressions = new HashSet<>();
	}
	
	public Set<Transition> getTransitions() {
		return transitions;
	}
	
	public State getState(CharacterRange r) {
		return reachableStates.get(r);
	}

	public boolean hasTransition(CharacterRange r) {
		return reachableStates.get(r) != null;
	}

	public Set<State> getEpsilonStates() {
		return epsilonStates;
	}
		
	public StateType getStateType() {
		return stateType;
	}
	
	public boolean isFinalState() {
		return stateType == StateType.FINAL;
	}
	
	public void setStateType(StateType stateType) {
		this.stateType = stateType;
	}
	
	public void addTransition(Transition transition) {
		if (transitions.add(transition)) {
			if (transition.isEpsilonTransition()) {
				epsilonStates.add(transition.getDestination());
			} else {
				reachableStates.put(transition.getRange(), transition.getDestination());
			}
		}
	}
	
	public void removeTransition(Transition transition) {
		transitions.remove(transition);
	}
	
	public void removeTransitions(Collection<Transition> c) {
		c.forEach(t -> transitions.remove(t));
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public Set<RegularExpression> getRegularExpressions() {
		return regularExpressions;
	}
	
	public State addRegularExpression(RegularExpression regex) {
		regularExpressions.add(regex);
		return this;
	}
	
	public State addRegularExpressions(Collection<RegularExpression> c) {
		regularExpressions.addAll(c);
		return this;
	}
	
	public int getCountTransitions() {
		return transitions.size();
	}
		
	@Override
	public String toString() {
		return "State" + id;
	}

}
