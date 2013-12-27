package org.jgll.regex;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class AutomatonOperations {
	
	public static DFA convertNFAtoDFA(NFA nfa) {
		
		Set<Set<State>> visitedStates = new HashSet<>();
		Deque<Set<State>> processList = new ArrayDeque<>();
		
		Set<State> initialState = new HashSet<>();
		initialState.add(nfa.getStartState());
		Set<State> newState = epsilonClosure(initialState);
		visitedStates.add(newState);
		processList.add(newState);
		
		BitSet characters = nfa.getCharacters();

		// For sharing states.
		Map<Set<State>, State> newStatesMap = new HashMap<>();
		
		State startState = null;
		
		while(!processList.isEmpty()) {
			Set<State> stateSet = processList.poll();
			State source = new State();
			
			if(startState == null) {
				startState = source;
			}
			
			Map<Integer, Set<State>> map = move(stateSet, characters);

			for(Entry<Integer, Set<State>> e : map.entrySet()) {
				newState = epsilonClosure(e.getValue());
				
				State destination = newStatesMap.get(newState);
				if(destination == null) {
					destination = new State();
					newStatesMap.put(e.getValue(), destination);
					
					for(State s : newState) {
						if(s.isFinalState()){
							destination.setFinalState(true);
							break;
						}
					}
				}
				
				source.addTransition(new Transition(e.getKey(), destination));
				
				if(!visitedStates.contains(newState)) {
					processList.add(newState);
				}
			}
		}
		
		return new DFA(startState);
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
	
	private static Map<Integer, Set<State>> move(Set<State> states, BitSet bitSet) {
		
		Map<Integer, Set<State>> map = new HashMap<>();
		
		for (int i = bitSet.nextSetBit(0); i >= 0; i = bitSet.nextSetBit(i+1)) {
			
			Set<State> newStates = new HashSet<>();

			for(State state : states) {
				for(Transition transition : state.getTransitions()) {
					if(transition.getStart() <= i && transition.getEnd() >= i) {
						newStates.add(transition.getDestination());
					}
				}
			}
			if(!newStates.isEmpty()) {
				map.put(i, newStates);
			}
		}
		
		return map;
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
		sb.append("State state" + visitedStates.get(state) + " = new State();\n");
		for(Transition transition : state.getTransitions()) {
			State destination = transition.getDestination();
			
			if(!visitedStates.keySet().contains(destination)) {
				visitedStates.put(destination, visitedStates.size() + 1);
				toJavaCode(destination, sb, visitedStates);
			}
			
			sb.append("state" + visitedStates.get(state) + ".addTransition(new Transition(" + transition.getStart() + 
					                                               ", " + transition.getEnd() + ", state" + visitedStates.get(destination) + ");\n");
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
	
	public static int getCountStates(Automaton automaton) {
		
		final int[] count = new int[0];
		
		AutomatonVisitor.visit(automaton, new VisitAction() {
			
			@Override
			public void visit(State state) {
				count[0]++;
			}
		});

		return count[0];
	}
	
	public static void setStateIDs(Automaton automaton) {
		
		AutomatonVisitor.visit(automaton, new VisitAction() {

			int id = 0;
			
			@Override
			public void visit(State state) {
				state.setId(++id);
			}
		});
	}

	
}
