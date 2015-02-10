package org.jgll.regex.automaton;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jgll.grammar.symbol.CharacterRange;
import org.jgll.util.Visualization;

import com.google.common.collect.Multimap;

public class AutomatonOperations {
	
	public static Automaton makeDeterministic(Automaton automaton) {
		if (automaton.isDeterministic())
			return automaton;
		
		return makeDeterministic(automaton.getStartState(), automaton.getAlphabet());
	}

	public static Automaton makeDeterministic(State start, CharacterRange[] alphabet) {
		
		Set<Set<State>> visitedStates = new HashSet<>();
		Deque<Set<State>> processList = new ArrayDeque<>();
		
		Set<State> initialState = new HashSet<>();
		initialState.add(start);
		initialState = epsilonClosure(initialState);
		visitedStates.add(initialState);
		processList.add(initialState);
		
		/**
		 * A map from the set of NFA states to the new state in the produced DFA.
		 * This map is used for sharing DFA states.
		 */
		Map<Set<State>, State> newStatesMap = new HashMap<>();
		
		State startState = new State();
		
		newStatesMap.put(initialState, startState);
		
		while (!processList.isEmpty()) {
			Set<State> stateSet = processList.poll();

			for (CharacterRange r : alphabet) {
				Set<State> destState = move(stateSet, r);
				
				if (destState.isEmpty())
					continue;
				
				State source = newStatesMap.get(stateSet);
				State dest = newStatesMap.computeIfAbsent(destState, s -> new State());
				source.addTransition(new Transition(r, dest));

				
				if (!visitedStates.contains(destState)) {
					visitedStates.add(destState);
					processList.add(destState);
				}
			}
		}
		
		// Setting the final states.
		outer:
		for (Entry<Set<State>, State> e : newStatesMap.entrySet()) {
			for (State s : e.getKey()) {
				if (s.getStateType() == StateType.FINAL) {
					e.getValue().setStateType(StateType.FINAL);
					continue outer;
				}
			}			
		}

		return Automaton.builder(startState).setDeterministic(true).build();
	}
	
	public static Automaton union(Automaton a1, Automaton a2) {
		return op(a1, a2, (s1, s2) -> s1.isFinalState() || s2.isFinalState());
	}
	
	public static Automaton intersect(Automaton a1, Automaton a2) {
		return op(a1, a2, (s1, s2) -> s1.isFinalState() && s2.isFinalState());
	}
	
	public static Automaton difference(Automaton a1, Automaton a2) {
		return op(a1, a2, (s1, s2) -> s1.isFinalState() && !s2.isFinalState());
	}
	
	private static Automaton op(Automaton a1, Automaton a2, Op op) {
		a1 = makeDeterministic(a1);
		a2 = makeDeterministic(a2);
		
		State startState = null;
		
		State[][] product = product(a1, a2);
		
		for (int i = 0; i < product.length; i++) {
			 for (int j = 0; j < product[i].length; j++) {
				State state = product[i][j];
				
				State state1 = a1.getStates()[i];
				State state2 = a2.getStates()[j];
				
				if (op.execute(state1, state2)) {
					state.setStateType(StateType.FINAL);
				}
				
				if (state1 == a1.getStartState() && state2 == a2.getStartState()) {
					startState = state;
				}
			 }
		}
		
		return new AutomatonBuilder(startState).makeDeterministic().build();
	}
	
	/**
	 * Produces the Cartesian product of the states of an automata.
	 */
	private static State[][] product(Automaton a1, Automaton a2) {
		
		State[] states1 = a1.getStates();
		State[] states2 = a2.getStates();
		
		State[][] newStates = new State[states1.length][states2.length];
		
		for (int i = 0; i < states1.length; i++) {
			for (int j = 0; j < states2.length; j++) {
				newStates[i][j] = new State();
			}
		}

		Multimap<CharacterRange, CharacterRange> rangeMap = merge(a1.getAlphabet(), a2.getAlphabet());
		convertToNonOverlapping(a1, rangeMap);
		makeComplete(a1, rangeMap.values());
		
		convertToNonOverlapping(a2, rangeMap);
		makeComplete(a2, rangeMap.values());
		
		for (int i = 0; i < states1.length; i++) {
			for (int j = 0; j < states2.length; j++) {
				
				State state = newStates[i][j];
				State state1 = states1[i];
				State state2 = states2[j];
				
				for (CharacterRange r : rangeMap.values()) {
					State s1 = state1.getState(r);
					State s2 = state2.getState(r);
					if (s1 != null && s2 != null) {
						state.addTransition(new Transition(r, newStates[s1.getId()][s2.getId()]));
					}
				}
			}
		}
		
		return newStates;
	}
	
	public static void convertToNonOverlapping(Automaton a, Multimap<CharacterRange, CharacterRange> rangeMap) {
		for (State state : a.getStates()) {
			List<Transition> removeList = new ArrayList<>();
			for (Transition transition : state.getTransitions()) {
				if (!transition.isEpsilonTransition()) {
					removeList.add(transition);
					for (CharacterRange range : rangeMap.get(transition.getRange())) {
						state.addTransition(new Transition(range, transition.getDestination()));
					}					
				}
			}
			state.removeTransitions(removeList);
		}
	}
	
	
	public static void makeComplete(Automaton automaton, Iterable<CharacterRange> alphabet) {
		
		State dummyState = new State();
		
		for (State state : automaton.getStates()) {
			for (CharacterRange r : alphabet) {
				if (!state.hasTransition(r)) {
					state.addTransition(new Transition(r, dummyState));
				}
			}			
		}
	}
	
	private static Multimap<CharacterRange, CharacterRange> merge(CharacterRange[] alphabet1, CharacterRange[] alphabet2) {
		List<CharacterRange> alphabets = new ArrayList<>();
		for (CharacterRange r : alphabet1) { alphabets.add(r); }
		for (CharacterRange r : alphabet2) { alphabets.add(r); }
		
		return CharacterRange.toNonOverlapping(alphabets);
	}
	
	
	public static Automaton minimize(Automaton automaton) {
		if (automaton.isMinimized())
			return automaton;

		return minimize(automaton.getAlphabet(), automaton.getStates());
	}
	
	/**
	 * 
	 * Note: unreachable states are already removed as we gather the states
	 * reachable from the start state of the given NFA.
	 * 
	 * @param automaton
	 * @return
	 */
	public static Automaton minimize(CharacterRange[] alphabet, State[] states) {
		
		int size = states.length;
		int[][] table = new int[size][size];
		
		final int EMPTY = -2;
		final int EPSILON = -1;
		
		for (int i = 0; i < table.length; i++) {
			for (int j = 0; j < i; j++) {
				table[i][j] = EMPTY;
			}
 		}
		
		for (int i = 0; i < table.length; i++) {
			for (int j = 0; j < i; j++) {
				if(states[i].isFinalState() && !states[j].isFinalState()) {
					table[i][j] = EPSILON;
				}
				if(states[j].isFinalState() && !states[i].isFinalState()) {
					table[i][j] = EPSILON;
				}
				
				// Differentiate between final states
				if(states[i].isFinalState() && 
				   states[j].isFinalState()) {
					table[i][j] = EPSILON;
				}
			}
		}
		
		boolean changed = true;
		
		while (changed) {
			changed = false;

				for (int i = 0; i < table.length; i++) {
					for (int j = 0; j < i; j++) {
						
						// If two states i and j are distinct
						if (table[i][j] == EMPTY) {
							for (int t = 0; t < alphabet.length; t++) {
								State q1 = states[i].getState(alphabet[t]);
								State q2 = states[j].getState(alphabet[t]);

								// If both states i and j have no outgoing transitions on the interval t, continue with the
								// next transition.
								if(q1 == null && q2 == null) {
									continue;
								}

								// If the transition t can be applied on state i but not on state j, two states are
								// disjoint. Continue with the next pair of states.
								if((q1 == null && q2 != null) || (q2 == null && q1 != null)) {
									table[i][j] = t;
									changed = true;
									break;
								}
								
								if(q1.getId() == q2.getId()) {
									continue;
								}
								
								int a;
								int b;
								if (q1.getId() > q2.getId()) {
									a = q1.getId();
									b = q2.getId();
								} else {
									a = q2.getId();
									b = q1.getId();
								}
								
								if (table[a][b] != EMPTY) {
									table[i][j] = t;
									changed = true;
									break;
								}
							}
						}
					}
				}
		}
		
		Map<State, Set<State>> partitionsMap = new HashMap<>();
		
		for (int i = 0; i < table.length; i++) {
			for (int j = 0; j < i; j++) {
				if (table[i][j] == EMPTY) {
					State stateI = states[i];
					State stateJ = states[j];
					
					Set<State> partitionI = partitionsMap.get(stateI);
					Set<State> partitionJ = partitionsMap.get(stateJ);
					
					if(partitionI == null && partitionJ == null) {
						Set<State> set = new HashSet<>();
						set.add(stateI);
						set.add(stateJ);
						partitionsMap.put(stateI, set);
						partitionsMap.put(stateJ, set);
					}
					else if(partitionI == null && partitionJ != null) {
						partitionJ.add(stateI);
						partitionsMap.put(stateI, partitionJ);
					} 
					else if(partitionJ == null && partitionI != null) {
						partitionI.add(stateJ);
						partitionsMap.put(stateJ, partitionI);
					}
					else { 
						partitionJ.addAll(partitionI);
						partitionI.addAll(partitionJ);
					}
				}
			}
		}
		
		HashSet<Set<State>> partitions = new HashSet<Set<State>>(partitionsMap.values());
		
		State startState = null;
		
		for (State state : states) {
			if (partitionsMap.get(state) == null) {
				Set<State> set = new HashSet<>();
				set.add(state);
				partitions.add(set);
			}
		} 
		
		Map<State, State> newStates = new HashMap<>();

		for (Set<State> set : partitions) {
			State newState = new State();
			for (State state : set) {
				
				newState.addRegularExpressions(state.getRegularExpressions());
				
				if (startState == state) {
					startState = newState;
				}
				if (state.isFinalState()) {
					newState.setStateType(StateType.FINAL);
				}
				newStates.put(state, newState);
			}
		}
		
		for (State state : states) {
			for (Transition t : state.getTransitions()) {
				newStates.get(state).addTransition(new Transition(t.getStart(), t.getEnd(), newStates.get(t.getDestination())));;				
			}
		}
		
		return Automaton.builder(startState).build();
	}

	
	private static Set<State> epsilonClosure(Set<State> states) {
		Set<State> newStates = new HashSet<>(states);
		
		for(State state : states) {
			Set<State> s = state.getEpsilonSates();
			if(!s.isEmpty()) {
				newStates.addAll(s);
				newStates.addAll(epsilonClosure(s));
			}
		}
		
		return newStates;
	}
	
	private static Set<State> move(Set<State> state, CharacterRange r) {
		Set<State> result = new HashSet<>();
		for (State s: state) {
			State dest = s.getState(r);
			if (dest != null) {
				result.add(dest);
			}
		}
		
		return epsilonClosure(result);
	}
	
	private static List<CharacterRange> getRanges(State[] states) {
		final Set<CharacterRange> ranges = new HashSet<>();
		
		for (State state : states) {
			for (Transition transition : state.getTransitions()) {
				if (!transition.isEpsilonTransition()) {
					ranges.add(transition.getRange());
				}
			}
		}
		
		return new ArrayList<>(ranges);
	}
	
	@FunctionalInterface
	private static interface Op {
		public boolean execute(State s1, State s2);
	}
	
}
