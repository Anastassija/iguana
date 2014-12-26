package org.jgll.grammar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgll.grammar.symbol.EOF;
import org.jgll.grammar.symbol.Epsilon;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Position;
import org.jgll.grammar.symbol.CharacterRange;
import org.jgll.grammar.symbol.Rule;
import org.jgll.grammar.symbol.Symbol;
import org.jgll.regex.RegularExpression;
import org.jgll.util.Tuple;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;

/**
 * 
 * 
 * 
 * @author Ali Afroozeh
 *
 */
public class GrammarOperations {
	
	private final ListMultimap<Nonterminal, Rule> definitions;

	private final SetMultimap<Nonterminal, RegularExpression> firstSets;
	
	private final SetMultimap<Nonterminal, RegularExpression> followSets;
	
	private final SetMultimap<Position, RegularExpression> predictionSets;

	private final SetMultimap<Nonterminal, Nonterminal> reachabilityGraph;

	private final Set<Nonterminal> nullableNonterminals;
	
	public GrammarOperations(ListMultimap<Nonterminal, Rule> definitions) {
		this.definitions = definitions;
		this.firstSets = HashMultimap.create();
		this.nullableNonterminals = new HashSet<>();
		this.followSets = HashMultimap.create();
		this.reachabilityGraph = HashMultimap.create();
		this.predictionSets = HashMultimap.create();
		
		calculateFirstSets();
		calculateFollowSets();
		calcualtePredictionSets();
	}
	
	public SetMultimap<Nonterminal, RegularExpression> getFirstSets() {
		return firstSets;
	}
	
	public SetMultimap<Nonterminal, RegularExpression> getFollowSets() {
		return followSets;
	}
	
	private void calculateFirstSets() {
		
		Set<Nonterminal> nonterminals = definitions.keySet();
		
		boolean changed = true;

		while (changed) {
			
			changed = false;
			
			for (Nonterminal head : nonterminals) {
				Set<RegularExpression> firstSet = firstSets.get(head);
				for (Rule alternate : definitions.get(head)) {
					changed |= addFirstSet(head, firstSet, alternate.getBody(), 0);
				}
			}
		}
	}
	
	/**
	 * Adds the first set of the current slot to the given set.
	 * 
	 * @param firstSet
	 * @param currentSlot
	 * @param changed
	 * 
	 * @return true if adding any new terminals are added to the first set.
	 */
	private boolean addFirstSet(Nonterminal head, Set<RegularExpression> firstSet, List<Symbol> alternate, int index) {

		boolean changed = false;
		
		if(alternate == null) {
			return false;
		}
		 
		//TODO: check if it is allowed or is it a good idea to enforce the instantiation of Epsilon.
		if (alternate.size() == 0) {
			nullableNonterminals.add(head);
			return firstSet.add(Epsilon.getInstance());
		}
		
		for(int i = index; i < alternate.size(); i++) {
			Symbol symbol = alternate.get(i);

			if (symbol instanceof RegularExpression) {
				RegularExpression regularExpression = (RegularExpression) symbol;
				changed |= firstSet.add(regularExpression);
				if (!regularExpression.isNullable()) {
					break;
				}
			}
			
			// Nonterminal
			else if (symbol instanceof Nonterminal) {
				Nonterminal nonterminal = (Nonterminal) symbol;
				
				Set<RegularExpression> set = new HashSet<>(firstSets.get(nonterminal));
				set.remove(Epsilon.getInstance());
				changed |= firstSet.addAll(set);
				if (!isNullable(nonterminal)) {
					break;
				}
			}
		}
		
		if (isChainNullable(alternate, 0)) {
			nullableNonterminals.add(head);
			changed |= firstSet.add(Epsilon.getInstance());
		}
		
		return changed;
	}
	
	private boolean isNullable(Nonterminal nt) {
		return nullableNonterminals.contains(nt);
	}
	
	/**
	 * 
	 * Checks if a grammar slot is nullable. This check is performed until
	 * the end of the alternate: isChainNullable(X ::= alpha . beta) says if the
	 * part beta is nullable.
	 *   
	 */
	private boolean isChainNullable(List<Symbol> alternate, int index) {
		
		if(index >= alternate.size()) {
			return true;
		}
		
		for(int i = index; i < alternate.size(); i++) {
			Symbol s = alternate.get(i);
			
			if(s instanceof RegularExpression) {
				if(!((RegularExpression)s).isNullable()) {
					return false;
				}
			} else {
				if(!isNullable((Nonterminal) s)) {
					return false;
				}				
			}
		}

		return true;
	}
		
	private void calculateFollowSets() {
		
		Set<Nonterminal> nonterminals = definitions.keySet();
		
		boolean changed = true;

		while (changed) {
			
			changed = false;
			
			for (Nonterminal head : nonterminals) {

				for (Rule rule : definitions.get(head)) {
					List<Symbol> alternative = rule.getBody();
					
					if(alternative == null || alternative.size() == 0) {
						continue;
					}
					
					for(int i = 0; i < alternative.size(); i++) {
					
						Symbol symbol = alternative.get(i);
						
						if(symbol instanceof Nonterminal) {
							Nonterminal nonterminal = (Nonterminal) symbol;

							// For rules of the form X ::= alpha B beta, add the
							// first set of beta to
							// the follow set of B.
							Set<RegularExpression> followSet = followSets.get(nonterminal);
							changed |= addFirstSet(nonterminal, followSet, alternative, i + 1);

							// If beta is nullable, then add the follow set of X
							// to the follow set of B.
							if (isChainNullable(alternative, i + 1)) {
								changed |= followSet.addAll(followSets.get(head));
							}
						}
					}
				}
			}
		}

		for (Nonterminal head : nonterminals) {
			// Remove the epsilon which may have been added from nullable
			// nonterminals
			followSets.get(head).remove(Epsilon.getInstance());

			// Add the EOF to all nonterminals as each nonterminal can be used
			// as the start symbol.
			followSets.get(head).add(EOF.getInstance());
		}
	}
	
	private void calcualtePredictionSets() {

		for (Nonterminal nonterminal : definitions.keySet()) {
			List<Rule> rules = definitions.get(nonterminal);
			
			for (Rule rule : rules) {
				for (int i = 0; i <= rule.size(); i++) {
					calculatePredictionSet(rule.getPosition(i));
				}
			}
		}
	}
	
	
	private void calculatePredictionSet(Position position) {
		
		Rule rule = position.getRule();
		int index = position.getPosition();
		
		List<Symbol> alternate = rule.getBody();
		
		if (alternate == null)
			return;
		
		for (int i = index; i < alternate.size(); i++) {
			
			Symbol symbol = alternate.get(i);
			
			if (symbol instanceof Nonterminal) {
				Nonterminal nonterminal = (Nonterminal) symbol;
				predictionSets.putAll(position, firstSets.get(nonterminal));
				
				if (!firstSets.get(nonterminal).contains(Epsilon.getInstance())) {
					break;
				}
			} 
			else if (symbol instanceof RegularExpression) {
				RegularExpression regex = (RegularExpression) symbol;
				predictionSets.put(position, regex);
				if(!regex.isNullable()) {
					break;
				}
			}
		}
		
		if (isChainNullable(alternate, 0)) {
			predictionSets.putAll(position, followSets.get(rule.getHead()));
		}
		
		predictionSets.remove(position, Epsilon.getInstance());
	}
	
	public Set<Nonterminal> calculateLLNonterminals() {

		Set<Nonterminal> nonterminals = definitions.keySet();
		
		Set<Nonterminal> ll1Nonterminals = new HashSet<>();
		
		Set<Nonterminal> ll1SubGrammarNonterminals = new HashSet<>();
		
		// Calculating character level predictions
		Map<Tuple<Nonterminal, Integer>, Set<Integer>> predictions = new HashMap<>();
		
		for (Nonterminal head : nonterminals) {

			int alternateIndex = 0;
			for(Rule rule : definitions.get(head)) {
				
				List<Symbol> alt = rule.getBody();
			
				// Calculate the prediction set for the alternate
				Set<RegularExpression> s = new HashSet<>();
				addFirstSet(head, s, alt, 0);
				if(s.contains(Epsilon.getInstance())) {
					s.addAll(followSets.get(head));
				}

				// Expand ranges into integers
				Set<Integer> set = new HashSet<>();
				for(RegularExpression r : s) {
					set.addAll(convert(r.getFirstSet()));
				}
				
				predictions.put(Tuple.of(head, alternateIndex), set);
				
				alternateIndex++;
			}			
		}
		
		for (Nonterminal head : nonterminals) {
			if(isLL1(head, predictions)) {
				ll1Nonterminals.add(head);
			}
		}
		
		for (Nonterminal head : nonterminals) {
			if(ll1Nonterminals.contains(head)) {
				boolean ll1SubGrammar = true;
				for(Nonterminal reachableHead : reachabilityGraph.get(head)) {
					if(!ll1Nonterminals.contains(reachableHead)) {
						ll1SubGrammar = false;
					}
				}
				if(ll1SubGrammar) {
					ll1SubGrammarNonterminals.add(head);
				}
			}
		}
		
		return ll1SubGrammarNonterminals;
	}
	
	/**
	 * Converts a 
	 * @param set
	 * @return
	 */
	private static Set<Integer> convert(Set<CharacterRange> set) {
		Set<Integer> integerSet = new HashSet<>();
		for(CharacterRange range : set) {
			for(int i = range.getStart(); i < range.getEnd(); i++) {
				integerSet.add(i);
			}
		}
		return integerSet;
	}
	
    private boolean isLL1(Nonterminal nonterminal, Map<Tuple<Nonterminal, Integer>, Set<Integer>> predictions) {
    	
    	int size = definitions.get(nonterminal).size();
    	
    	// If there is only one alternate
		if(size == 1) {
        	return true;
        }
        
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
            	if(i != j) {
            		HashSet<Integer> intersection = new HashSet<>(predictions.get(Tuple.of(nonterminal, i)));
            		intersection.retainAll(predictions.get(Tuple.of(nonterminal, j)));
        			if(!intersection.isEmpty()) {
        				return false;
                    }
            	}
            }
        }

        return true;
    }
		
	/**
	 * 
	 * Calculate the set of nonterminals that are reachable via the alternates of A.
	 * In other words, if A is a nonterminal, reachable nonterminals are all the B's such as
	 * A =>* alpha B gamma. Note that this method does not calculate direct-nullable reachable
	 * nonterminals.
	 * 
	 */
	public static Map<Nonterminal, Set<Nonterminal>> calculateReachabilityGraph(Grammar grammar) {
		
		Set<Nonterminal> nonterminals = grammar.getNonterminals();
		
		Map<Nonterminal, Set<Nonterminal>> reachabilityGraph = new HashMap<>();
		
		for (Nonterminal head : nonterminals) {
			reachabilityGraph.put(head, new HashSet<Nonterminal>());
		}
		
		boolean changed = true;
		
		while (changed) {
			
			changed = false;
			
			for (Nonterminal head : nonterminals) {
				Set<Nonterminal> set = reachabilityGraph.get(head);
				for (Rule rule : grammar.getAlternatives(head)) {
					
					List<Symbol> alternate = rule.getBody();
					
					if(alternate == null) {
						continue;
					}
					
					for(Symbol symbol : alternate) {
						if(symbol instanceof Nonterminal) {
							Nonterminal nonterminal = (Nonterminal) symbol;
							changed |= set.add(nonterminal);
							changed |= set.addAll(reachabilityGraph.get(nonterminal));
						} 
					}
				}
			}
		}
		
		return reachabilityGraph;
	}
}
