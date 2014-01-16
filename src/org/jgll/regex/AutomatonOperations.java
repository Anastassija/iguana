package org.jgll.regex;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jgll.util.Tuple;

public class AutomatonOperations {
	
	public static Automaton makeDeterministic(Automaton nfa) {
		
		Set<Set<State>> visitedStates = new HashSet<>();
		Deque<Set<State>> processList = new ArrayDeque<>();
		
		Set<State> initialState = new HashSet<>();
		initialState.add(nfa.getStartState());
		initialState = epsilonClosure(initialState);
		visitedStates.add(initialState);
		processList.add(initialState);
		
		int[] intervals = nfa.getIntervals();
				
		/**
		 * A map from the set of NFA states to the new state in the produced DFA.
		 * This map is used for sharing DFA states.
		 */
		Map<Set<State>, State> newStatesMap = new HashMap<>();
		
		State startState = new State();
		
		newStatesMap.put(initialState, startState);
		
		while(!processList.isEmpty()) {
			Set<State> stateSet = processList.poll();
			State source = newStatesMap.get(stateSet);
			
			// The state should have been created before.
			assert source != null;
			
			Map<Tuple<Integer, Integer>, Set<State>> transitionsMap = move(stateSet, intervals);

			for(Entry<Tuple<Integer, Integer>, Set<State>> e : transitionsMap.entrySet()) {
				Set<State> newState = epsilonClosure(e.getValue());
				
				State destination = newStatesMap.get(newState);
				if(destination == null) {
					destination = new State();
					newStatesMap.put(newState, destination);
				}
				
				Transition transition = new Transition(e.getKey().getFirst(), e.getKey().getSecond(), destination);
				source.addTransition(transition);
				
				if(!visitedStates.contains(newState)) {
					visitedStates.add(newState);
					processList.add(newState);
				}
			}
		}
		
		setStateIDs(startState);
		
		// Setting the final states.
		outer:
		for(Entry<Set<State>, State> e : newStatesMap.entrySet()) {
			for(State s : e.getKey()) {
				if(s.isFinalState()) {
					e.getValue().setFinalState(true);
					continue outer;
				}
			}			
		}
		
		return new Automaton(startState);
	}
	
	public static Matcher createDFA(Automaton nfa) {
		
		int[] intervals = nfa.getIntervals();
		
		if(intervals.length == 0) {
			return new TrueMatcher();
		}
		
		int statesCount = nfa.getCountStates();
		int inputLength = nfa.getIntervals().length;
		int[][] transitionTable = new int[statesCount][inputLength];
		boolean[] endStates = new boolean[statesCount];
		
		for(int i = 0; i < transitionTable.length; i++) {
			for(int j = 0; j < transitionTable[i].length; j++) {
				transitionTable[i][j] = -1;
			}
		}

		for(State state : nfa.getAllStates()) {
			for(Transition transition : state.getTransitions()) {
				transitionTable[state.getId()][transition.getId()] = transition.getDestination().getId();
			}
			
			if(state.isFinalState()) {
				endStates[state.getId()] = true;
			}
		}

		if(intervals[intervals.length - 1] - intervals[0] > Character.MAX_VALUE) {
			return new LargeIntervalMatcher(transitionTable, endStates, nfa.getStartState().getId(), intervals);					
		} else {
			return new ShortIntervalMatcher(transitionTable, endStates, nfa.getStartState().getId(), intervals);
		}
	}
	
	private static Set<State> epsilonClosure(Set<State> states) {
		Set<State> newStates = new HashSet<>();
		for(State state : states) {
			newStates.addAll(epsilonClosure(state));
		}
		return newStates;
	}
	
	private static Set<State> epsilonClosure(State state) {
		Set<State> newStates = new HashSet<>();
		newStates.add(state);
		for(Transition t : state.getTransitions()) {
			if(t.isEpsilonTransition()) {
				State destination = t.getDestination();
				newStates.add(destination);
				newStates.addAll(epsilonClosure(destination));
			}
		}
		
		return newStates;
	}
	
	private static Map<Tuple<Integer, Integer>, Set<State>> move(Set<State> states, int[] intervals) {
		
		Map<Tuple<Integer, Integer>, Set<State>> map = new HashMap<>();
		
		for(int i = 0; i < intervals.length; i++) {
			Set<State> reachableStates = new HashSet<>();

			for(State state : states) {
				for(Transition transition : state.getTransitions()) {
					if(transition.canMove(intervals[i])) {
						reachableStates.add(transition.getDestination());						
					}
				}
			}
			
			// Creating the transitions for the reachable states based on the transition intervals.
			if(!reachableStates.isEmpty()) {
				if(i + 1 < intervals.length) {
					map.put(new Tuple<>(intervals[i], intervals[i+1] - 1), reachableStates);
				} 
				if(i + 1 == intervals.length) {
					map.put(new Tuple<>(intervals[i] - 1, intervals[i] - 1), reachableStates);
				}
			}			
		}
		
		return map;
	}
		
	/**
	 * 
	 * Note: unreachable states are already removed as we gather the states
	 * reachable from the start state of the given NFA.
	 * 
	 * @param nfa
	 * @return
	 */
	public static Automaton minimize(Automaton nfa) {
		
		int[][] table = new int[nfa.getCountStates()][nfa.getCountStates()];
		
		final int EMPTY = -2;
		final int EPSILON = -1;
		
		for (int i = 0; i < table.length; i++) {
			for (int j = 0; j < i; j++) {
				table[i][j] = EMPTY;
			}
 		}
		
		for (int i = 0; i < table.length; i++) {
			for (int j = 0; j < i; j++) {
				if(nfa.getState(i).isFinalState() && !nfa.getState(j).isFinalState()) {
					table[i][j] = EPSILON;
				}
				if(nfa.getState(j).isFinalState() && !nfa.getState(i).isFinalState()) {
					table[i][j] = EPSILON;
				}
			}
		}
		
		int[] intervals = nfa.getIntervals();
		
		boolean changed = true;
		
		while(changed) {
			changed = false;

				for (int i = 0; i < table.length; i++) {
					for (int j = 0; j < i; j++) {
						
						// If two states i and j are distinct
						if(table[i][j] == EMPTY) {
							for(int t = 0; t < intervals.length; t++) {
								State q1 = moveTransition(nfa.getState(i), intervals[t]);
								State q2 = moveTransition(nfa.getState(j), intervals[t]);

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
								if(q1.getId() > q2.getId()) {
									a = q1.getId();
									b = q2.getId();
								} else {
									a = q2.getId();
									b = q1.getId();
								}
								
								if(table[a][b] != EMPTY) {
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
				if(table[i][j] == EMPTY) {
					State stateI = nfa.getState(i);
					State stateJ = nfa.getState(j);
					
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
		
		for(State state : nfa.getAllStates()) {
			if(partitionsMap.get(state) == null) {
				Set<State> set = new HashSet<>();
				set.add(state);
				partitions.add(set);
			}
		} 
		
		Map<State, State> newStates = new HashMap<>();

		for(Set<State> set : partitions) {
			State newState = new State();
			for(State state : set) {
				if(nfa.getStartState() == state) {
					startState = newState;
				}
				if(state.isFinalState()) {
					newState.setFinalState(true);
				}
				newStates.put(state, newState);
			}
		}
		
		for(State state : nfa.getAllStates()) {
			for(Transition t : state.getTransitions()) {
				newStates.get(state).addTransition(new Transition(t.getStart(), t.getEnd(), newStates.get(t.getDestination())));;				
			}
		}
		
		return new Automaton(startState);
	}

	/**
	 * For debugging purposes 
	 */
	@SuppressWarnings("unused")
	private static void printMinimizationTable(int[][] table) {
		for (int i = 0; i < table.length; i++) {
			for (int j = 0; j < i; j++) {
				System.out.print(table[i][j] + " ");
			}
			System.out.println("\n");
		}
	}


	private static State moveTransition(State state, int i) {
		for(Transition transition : state.getTransitions()) {
			if(transition.canMove(i)) {
				return transition.getDestination();				
			}
		}
		return null;
	}
	
	public static String toJavaCode(Automaton automaton) {
		State startState = automaton.getStartState();
		StringBuilder sb = new StringBuilder();
		Map<State, Integer> visitedStates = new HashMap<>();
		visitedStates.put(startState, 1);
		toJavaCode(startState, sb, visitedStates);
		return sb.toString();
	}
	
	private static void toJavaCode(State state, StringBuilder sb, Map<State, Integer> visitedStates) {
		
		if(state.isFinalState()) {
			sb.append("State state" + visitedStates.get(state) + " = new State(true);\n");			
		} else {
			sb.append("State state" + visitedStates.get(state) + " = new State();\n");
		}
		
		for(Transition transition : state.getTransitions()) {
			State destination = transition.getDestination();
			
			if(!visitedStates.keySet().contains(destination)) {
				visitedStates.put(destination, visitedStates.size() + 1);
				toJavaCode(destination, sb, visitedStates);
			}
			
			sb.append("state" + visitedStates.get(state) + ".addTransition(new Transition(" + transition.getStart() + 
					                                               ", " + transition.getEnd() + ", state" + visitedStates.get(destination) + "));\n");
		}
	}
	
	public static BitSet getCharacters(Automaton automaton) {
		final BitSet bitSet = new BitSet();
		
		AutomatonVisitor.visit(automaton, new VisitAction() {
			
			@Override
			public void visit(State state) {
				for(Transition transition : state.getTransitions()) {
					bitSet.set(transition.getStart(), transition.getEnd() + 1);
				}
			}
		});
		
		return bitSet;
	}
	
	public static int[] getIntervals(Set<Transition> transitions) {
		Set<Integer> set = new HashSet<>();
		
		for(Transition transition : transitions) {
			if(!transition.isEpsilonTransition()) {
				set.add(transition.getStart());
				set.add(transition.getEnd() + 1);						
			}
		}
		
		Integer[] array = set.toArray(new Integer[] {});
		Arrays.sort(array);
		
		int[] result = new int[array.length];
		for(int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}
		
		return result;		
	}
	
	public static int[] getIntervals(Automaton automaton) {		
		return getIntervals(getAllTransitions(automaton));
	}
	
	public static Set<Transition> getAllTransitions(Automaton automaton) {
		final Set<Transition> transitions = new HashSet<>();

		AutomatonVisitor.visit(automaton, new VisitAction() {
			
			@Override
			public void visit(State state) {
				for(Transition transition : state.getTransitions()) {
					if(!transition.isEpsilonTransition()) {
						transitions.add(transition);
					}
				}
			}
		});
		
		return transitions;
	}
 	
	public static int getCountStates(Automaton automaton) {
		
		final int[] count = new int[1];
		
		AutomatonVisitor.visit(automaton, new VisitAction() {
			
			@Override
			public void visit(State state) {
				count[0]++;
			}
		});

		return count[0];
	}
	
	public static Set<State> getAllStates(Automaton automaton) {
		final Set<State> states = new HashSet<>();
		
		AutomatonVisitor.visit(automaton, new VisitAction() {
			
			@Override
			public void visit(State state) {
				states.add(state);
			}
		});
		
		return states;
	}
	
	public static void setStateIDs(State startState) {
				
		AutomatonVisitor.visit(startState, new VisitAction() {
			
			int id = 0;

			@Override
			public void visit(State state) {
				state.setId(id++);
			}
		});
	}
	
	public static void setTransitionIDs(Automaton automaton) {
		int[] intervals = automaton.getIntervals();
		
		/*
		 * A map from each interval's start to the interval id.
		 */
		final Map<Integer, Integer> intervalIds = new HashMap<>();
		
		for(int i = 0; i < intervals.length; i++) {
			intervalIds.put(intervals[i], i);
		}
		
		AutomatonVisitor.visit(automaton, new VisitAction() {
			
			@Override
			public void visit(State state) {
				for(Transition transition : state.getTransitions()) {
					if(transition.isEpsilonTransition()) {
						transition.setId(-1);
					} else {
						transition.setId(intervalIds.get(transition.getStart()));
					}
				}
			}
		});
	}
	
	public static void setStateIDs(Automaton automaton) {
		setStateIDs(automaton.getStartState());
	}

	public static Set<State> getFinalStates(Automaton nfa) {
		
		final Set<State> finalStates = new HashSet<>();
		
		AutomatonVisitor.visit(nfa, new VisitAction() {
			
			@Override
			public void visit(State state) {
				if(state.isFinalState()) {
					finalStates.add(state);
				}
			} 
		});
		
		return finalStates;
	}
	
	/**
	 * Merges consecutive outgoing transitions of a state that are of the form\
	 * [a - b] and [b+1 - c] to [a - c]. 
	 * 
	 * Note: This method should be used with caution because it may break the 
	 * working of Matchers in case of overlapping transitions in different states.
	 * Transitions are indexed globally, not per state. Therefore, during the working
	 * of the matcher, because of overlapping, a wrong transition that does not exist
	 * at a state may be chosen.  
	 * 
	 */
	public static Automaton mergeTransitions(final Automaton automaton) {
		
		final State[] startStates = new State[1];
		
		final Map<State, State> newStates = new HashMap<>();
		
		AutomatonVisitor.visit(automaton, new VisitAction() {

			@Override
			public void visit(State state) {
				State newState;
				if(state.isFinalState()) {
					newState = new State(true);
				} else {
					newState = new State();					
				}
				newStates.put(state, newState);
				
				if(automaton.getStartState() == state) {
					startStates[0] = newState;
				}
 			}
		});
		
		AutomatonVisitor.visit(automaton, new VisitAction() {

			@Override
			public void visit(State state) {
				Transition[] t = state.getSortedTransitions();
				int i = 0;
				while(i < t.length) {
					int j = i;
					while(i < t.length - 1 && 
						  t[i + 1].getStart() == t[i].getEnd() + 1 &&
						  t[i].getDestination() == t[i + 1].getDestination()) {
						i++;
					}
					State newState = newStates.get(state);
					newState.addTransition(new Transition(t[j].getStart(), t[i].getEnd(), newStates.get(t[j].getDestination())));
					i++;
				}
			}
		});
		
		return new Automaton(startStates[0]);
	}
	
	/**
	 * Creates the reverse of the given automaton. A reverse automaton 
	 * accept the reverse language accepted by the original automaton. To construct
	 * a reverse automaton, all final states of the original automaton are becoming 
	 * start states, transitions are reversed and the start state becomes the
	 * only final state.
	 * 
	 */
	public static Automaton reverse(Automaton automaton) {

		// 0. creating new states for each state of the original automaton
		final Map<State, State> newStates = new HashMap<>();
		
		AutomatonVisitor.visit(automaton, new VisitAction() {
			
			@Override
			public void visit(State state) {
				newStates.put(state, new State());
			}
		});
		
		
		// 1. creating a new start state and adding epsilon transitions to the final
		// states of the original automata
		State startState = new State();
		
		for(State finalState : automaton.getFinalStates()) {
			startState.addTransition(Transition.emptyTransition(newStates.get(finalState)));
		}
		
		// 2. Reversing the transitions
		AutomatonVisitor.visit(automaton, new VisitAction() {
			
			@Override
			public void visit(State state) {
				for(Transition t : state.getTransitions()) {
					newStates.get(t.getDestination()).addTransition(new Transition(t.getStart(), t.getEnd(), newStates.get(state)));
				}
			}
		});
		
		// 2. making the start state final
		newStates.get(automaton.getStartState()).setFinalState(true);
		 
		return new Automaton(startState);
	}
	
	/**
	 * Union(a1, a2) = a1 | a2 
	 */
	public static Automaton union(Automaton a1, Automaton a2) {
		
		a1 = a1.copy();
		a2 = a2.copy();
		
		if(!a1.isDeterministic()) {
			a1.determinize();
		}
		
		if(!a2.isDeterministic()) {
			a2.determinize();
		}
		
		State startState = null;
		
		Map<Tuple<Integer, Integer>, State> map = product(a1, a2);
		
		for(Entry<Tuple<Integer, Integer>, State> e : map.entrySet()) {
			int i = e.getKey().getFirst();
			int j = e.getKey().getSecond();
			State state = e.getValue();
			
			State state1 = a1.getState(i);
			State state2 = a2.getState(j);
			
			if(state1.isFinalState() || state2.isFinalState()) {
				state.setFinalState(true);
			}
			
			if(state1 == a1.getStartState() && state2 == a2.getStartState()) {
				startState = state;
			}
		}
		
		return new Automaton(startState);
	}
	
	/**
	 *  A state in the resulting intersection automata is final
	 *  if all its composing states are final. 
	 * 
	 */
	public static Automaton intersection(Automaton a1, Automaton a2) {
		
		a1 = a1.copy();
		a2 = a2.copy();
		
		if(!a1.isDeterministic()) {
			a1.determinize();
		}
		
		if(!a2.isDeterministic()) {
			a2.determinize();
		}
		
		State startState = null;
		
		Map<Tuple<Integer, Integer>, State> map = product(a1, a2);
		
		for(Entry<Tuple<Integer, Integer>, State> e : map.entrySet()) {
			int i = e.getKey().getFirst();
			int j = e.getKey().getSecond();
			State state = e.getValue();
			
			State state1 = a1.getState(i);
			State state2 = a2.getState(j);
			
			if(state1.isFinalState() && state2.isFinalState()) {
				state.setFinalState(true);
			}
			
			if(state1 == a1.getStartState() && state2 == a2.getStartState()) {
				startState = state;
			}
		}
		
		return new Automaton(startState);
	}
	
	/**
	 * Produces the Cartesian product of the states of an automata.
	 */
	private static Map<Tuple<Integer, Integer>, State> product(Automaton a1, Automaton a2) {
		
		State[] states1 = a1.getAllStates();
		State[] states2 = a2.getAllStates();
		
		
		Map<Tuple<Integer, Integer>, State> newStates = new HashMap<>();
		
		for(int i = 0; i < states1.length; i++) {
			for(int j = 0; j < states2.length; j++) {
				newStates.put(Tuple.from(i, j), new State());
			}
		}
		
		Set<Transition> transitions = getAllTransitions(a1);
		transitions.addAll(getAllTransitions(a2));
		int[] intervals = getIntervals(transitions);
		
		for(int i = 0; i < states1.length; i++) {
			for(int j = 0; j < states2.length; j++) {
				
				State state = newStates.get(Tuple.from(i, j));
				State state1 = states1[i];
				State state2 = states2[j];
				
				for(int t = 0; t < intervals.length - 1; t++) {
					Set<State> reachableStates1 = state1.move(intervals[t]);
					Set<State> reachableStates2 = state2.move(intervals[t]);
					
					assert reachableStates2.size() <= 1; // Automatons are already determinized.
					assert reachableStates1.size() <= 1;
					
					if(reachableStates1.size() == 1 && reachableStates2.size() == 1) {
						State s1 = reachableStates1.iterator().next();
						State s2 = reachableStates2.iterator().next();
						state.addTransition(new Transition(intervals[t], intervals[t + 1] - 1, newStates.get(Tuple.from(s1.getId(), s2.getId()))));
					}						
				}
				
			}
		}
		
		return newStates;
	}
	
	public static Automaton copy(final Automaton automaton) {
		
		final Map<State, State> newStates = new HashMap<>();
		
		final State[] startState = new State[1];
		
		AutomatonVisitor.visit(automaton, new VisitAction() {
			
			@Override
			public void visit(State state) {
				State newState = new State();
				newStates.put(state, newState);
				if(state.isFinalState()) {
					newState.setFinalState(true);
				}
				if(state == automaton.getStartState()) {
					startState[0] = newState;
				}
			}
		});
		
		AutomatonVisitor.visit(automaton, new VisitAction() {
			
			@Override
			public void visit(State state) {
				for(Transition transition : state.getTransitions()) {
					State newState = newStates.get(state);
					newState.addTransition(new Transition(transition.getStart(), transition.getEnd(), newStates.get(transition.getDestination())));
				}
			}
		});
		
		return new Automaton(startState[0]);
	}
	
	/**
	 * Creates an automaton which is the result of applying the or operator to the list
	 * of automatons. 
	 */
	public static Automaton or(List<Automaton> automatons) {
		State startState = new State();
		State finalState = new State(true);
		
		for(Automaton a : automatons) {
			startState.addTransition(Transition.emptyTransition(a.getStartState()));
			
			for(State f : a.getFinalStates()) {
				f.setFinalState(false);
				f.addTransition(Transition.emptyTransition(finalState));
			}
		}
		
		Automaton a = new Automaton(startState).minimize();
		
		return a;
	}
	
	
	/**
	 * Returns true if there is a sentence accepted by the automaton a1 which is is a prefix of
	 * a sentence accepted by the automaton a2. 
	 */
	public static boolean prefix(final Automaton a1, final Automaton a2) {
		Automaton copy = makeAllStatesFinal(a2);
		return !copy.intersection(a1).isLanguageEmpty();
	}
	
	/**
	 * Returns a copy of the given automaton where all its states are set to final. 
	 */
	private static Automaton makeAllStatesFinal(final Automaton a) {
		
		Automaton copy = a.copy();
		
		AutomatonVisitor.visit(copy, new VisitAction() {
			
			@Override
			public void visit(State state) {
				state.setFinalState(true);
			}
		});
		
		return copy;
	}
}
