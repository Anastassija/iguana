package org.jgll.regex.automaton;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.jgll.grammar.symbol.EOF;
import org.jgll.grammar.symbol.Range;
import org.jgll.parser.HashFunctions;
import org.jgll.util.Input;

public class Transition implements Comparable<Transition>, Serializable {
	
	private static final long serialVersionUID = 1L;

	private int start;
	
	private int end;
	
	private State destination;
	
	private int id;
	
	private Set<Action> actions;
	
	public Transition(int start, int end, State destination, Set<Action> actions) {
		
		if(end < start) 		throw new IllegalArgumentException("start cannot be less than end.");
		if(destination == null) throw new IllegalArgumentException("Destination cannot be null.");
		if (actions == null)    throw new IllegalArgumentException("Transition actions cannot be null.");
		
		this.start = start;
		this.end = end;
		this.destination = destination;
		this.actions = actions;		
	}
	
	public Transition(int c, State destination) {
		this(c, c, destination);
	}
	
	public Transition(int start, int end, State destination) {
		this(start, end, destination, new HashSet<Action>());
	}
	
	public static Transition epsilonTransition(State destination) {
		return new Transition(-1, destination);
	}
	
	public static Transition EOFTransition(State destination) {
		return new Transition(EOF.VALUE, destination);
	}
	
	public int getStart() {
		return start;
	}
	
	public int getEnd() {
		return end;
	}
	
	public Range getRange() {
		return Range.in(start, end);
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public State getDestination() {
		return destination;
	}
	
	public boolean isEpsilonTransition() {
		return start == -1;
	}
	
	public boolean isLoop(State source) {
		return source == destination;
	}
	
	public boolean canMove(int c) {
		return start <= c && c <= end;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if(this == obj) {
			return true;
		}
		
		if(!(obj instanceof Transition)) {
			return false;
		}
		
		Transition other = (Transition) obj;
		
		return destination == other.destination &&
			   start == other.start &&
			   end == other.end;
	}
	
	@Override
	public int hashCode() {
		return HashFunctions.defaulFunction().hash(start, end, destination.hashCode());
	}
	
	@Override
	public String toString() {
		
		if(isEpsilonTransition()) {
			return "-1";
		}
		
		return Range.in(start, end).toString() + " " + (actions.isEmpty() ? "" : actions.toString());
	}

	@Override
	public int compareTo(Transition t) {
		return this.start - t.getStart();
	}

	public Transition addTransitionAction(Action action) {
		if(action != null) {
			actions.add(action);			
		}
		return this;
	}
	
	public Set<Action> getActions() {
		return actions;
	}
	
	public boolean executeActions(Input input, int index) {
		
		for (Action action : actions) {
			if(action.execute(input, index)) {
				return true;
			}
		}
		
		return false;
	}
		
}
