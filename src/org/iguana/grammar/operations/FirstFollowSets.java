/*
 * Copyright (c) 2015, Ali Afroozeh and Anastasia Izmaylova, Centrum Wiskunde & Informatica (CWI)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this 
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this 
 *    list of conditions and the following disclaimer in the documentation and/or 
 *    other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
 * OF SUCH DAMAGE.
 *
 */

package org.iguana.grammar.operations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.iguana.grammar.Grammar;
import org.iguana.grammar.symbol.CharacterRange;
import org.iguana.grammar.symbol.EOF;
import org.iguana.grammar.symbol.Epsilon;
import org.iguana.grammar.symbol.Nonterminal;
import org.iguana.grammar.symbol.Rule;
import org.iguana.grammar.symbol.Symbol;
import org.iguana.regex.RegularExpression;
import org.iguana.util.Tuple;

/**
 * 
 * @author Ali Afroozeh
 *
 */
public class FirstFollowSets {
	
	private final Map<Nonterminal, List<Rule>> definitions;

	private final Map<Nonterminal, Set<CharacterRange>> firstSets;
	
	private final Map<Nonterminal, Set<CharacterRange>> followSets;
	
	private final Map<Tuple<Rule, Integer>, Set<CharacterRange>> predictionSets;

	private final Set<Nonterminal> nullableNonterminals;
	
	public FirstFollowSets(Grammar grammar) {
		this.definitions = grammar.getDefinitions();
		this.firstSets = new HashMap<>();
		this.nullableNonterminals = new HashSet<>();
		this.followSets = new HashMap<>();
		this.predictionSets = new HashMap<>();
		
		definitions.keySet().forEach(k -> { firstSets.put(k, new HashSet<>()); followSets.put(k, new HashSet<>()); });
		
		calculateFirstSets();
		calculateFollowSets();
		calcualtePredictionSets();
	}
	
	public Set<CharacterRange> getFirstSet(Nonterminal nonterminal) {
		return firstSets.get(nonterminal);
	}
	
	public Set<CharacterRange> getFollowSet(Nonterminal nonterminal) {
		return followSets.get(nonterminal);
	}
	
	public Set<CharacterRange> getPredictionSet(Rule rule, int index) {
		return predictionSets.get(Tuple.of(rule, index));
	}
	
	private void calculateFirstSets() {
		
		Set<Nonterminal> nonterminals = definitions.keySet();
		
		boolean changed = true;

		while (changed) {
			
			changed = false;
			
			for (Nonterminal head : nonterminals) {
				Set<CharacterRange> firstSet = firstSets.get(head);
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
	private boolean addFirstSet(Nonterminal head, Set<CharacterRange> firstSet, List<Symbol> alternate, int index) {

		boolean changed = false;
		
		if(alternate == null) {
			return false;
		}
		 
		//TODO: check if it is allowed or is it a good idea to enforce the instantiation of Epsilon.
		if (alternate.size() == 0) {
			nullableNonterminals.add(head);
			return firstSet.addAll(Epsilon.getInstance().getFirstSet());
		}
		
		for(int i = index; i < alternate.size(); i++) {
			Symbol symbol = alternate.get(i);

			if (symbol instanceof RegularExpression) {
				RegularExpression regularExpression = (RegularExpression) symbol;
				changed |= firstSet.addAll(regularExpression.getFirstSet());
				if (!regularExpression.isNullable()) {
					break;
				}
			}
			
			// Nonterminal
			else if (symbol instanceof Nonterminal) {
				Nonterminal nonterminal = (Nonterminal) symbol;
				
				Set<CharacterRange> set = new HashSet<>(firstSets.get(nonterminal));
				set.remove(Epsilon.getInstance());
				changed |= firstSet.addAll(set);
				if (!isNullable(nonterminal)) {
					break;
				}
			}
		}
		
		if (isChainNullable(alternate, 0)) {
			nullableNonterminals.add(head);
			changed |= firstSet.addAll(Epsilon.getInstance().getFirstSet());
		}
		
		return changed;
	}
	
	public boolean isNullable(Nonterminal nt) {
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
							Set<CharacterRange> followSet =  followSets.get(nonterminal);
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
			followSets.get(head).removeAll(Epsilon.getInstance().getFirstSet());

			// Add the EOF to all nonterminals as each nonterminal can be used
			// as the start symbol.
			followSets.get(head).addAll(EOF.getInstance().getFirstSet());
		}
	}
	
	private void calcualtePredictionSets() {

		for (Nonterminal nonterminal : definitions.keySet()) {
			List<Rule> rules = definitions.get(nonterminal);
			
			for (Rule rule : rules) {
				for (int i = 0; i <= rule.size(); i++) {
					calculatePredictionSet(rule, i);
				}
			}
		}
	}
	
	private void calculatePredictionSet(Rule rule, int index) {
		
		Tuple<Rule, Integer> position = Tuple.of(rule, index);
		List<Symbol> alternate = rule.getBody();
		
		if (alternate == null)
			return;
		
		for (int i = index; i < alternate.size(); i++) {
			
			Symbol symbol = alternate.get(i);
			
			if (symbol instanceof Nonterminal) {
				Nonterminal nonterminal = (Nonterminal) symbol;
				predictionSets.computeIfAbsent(position, k -> new HashSet<>()).addAll(firstSets.get(nonterminal));
				
				if (!firstSets.get(nonterminal).containsAll(Epsilon.getInstance().getFirstSet())) {
					break;
				}
			} 
			else if (symbol instanceof RegularExpression) {
				RegularExpression regex = (RegularExpression) symbol;
				predictionSets.computeIfAbsent(position, k -> new HashSet<>()).addAll(regex.getFirstSet());
				if(!regex.isNullable()) {
					break;
				}
			}
		}
		
		if (isChainNullable(alternate, 0)) {
			predictionSets.computeIfAbsent(position, k -> new HashSet<>()).addAll(followSets.get(rule.getHead()));
		}
		
		predictionSets.remove(position, Epsilon.getInstance());
	}
	
//	public Set<Nonterminal> calculateLLNonterminals() {
//
//		Set<Nonterminal> nonterminals = definitions.keySet();
//		
//		Set<Nonterminal> ll1Nonterminals = new HashSet<>();
//		
//		Set<Nonterminal> ll1SubGrammarNonterminals = new HashSet<>();
//		
//		// Calculating character level predictions
//		Map<Tuple<Nonterminal, Integer>, Set<Integer>> predictions = new HashMap<>();
//		
//		for (Nonterminal head : nonterminals) {
//
//			int alternateIndex = 0;
//			for(Rule rule : definitions.get(head)) {
//				
//				List<Symbol> alt = rule.getBody();
//			
//				// Calculate the prediction set for the alternate
//				Set<RegularExpression> s = new HashSet<>();
//				addFirstSet(head, s, alt, 0);
//				if(s.contains(Epsilon.getInstance())) {
//					s.addAll(followSets.get(head));
//				}
//
//				// Expand ranges into integers
//				Set<Integer> set = new HashSet<>();
//				for(RegularExpression r : s) {
//					set.addAll(convert(r.getFirstSet()));
//				}
//				
//				predictions.put(Tuple.of(head, alternateIndex), set);
//				
//				alternateIndex++;
//			}			
//		}
//		
//		for (Nonterminal head : nonterminals) {
//			if(isLL1(head, predictions)) {
//				ll1Nonterminals.add(head);
//			}
//		}
//		
//		for (Nonterminal head : nonterminals) {
//			if(ll1Nonterminals.contains(head)) {
//				boolean ll1SubGrammar = true;
//				for(Nonterminal reachableHead : reachabilityGraph.get(head)) {
//					if(!ll1Nonterminals.contains(reachableHead)) {
//						ll1SubGrammar = false;
//					}
//				}
//				if(ll1SubGrammar) {
//					ll1SubGrammarNonterminals.add(head);
//				}
//			}
//		}
//		
//		return ll1SubGrammarNonterminals;
//	}
	
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
    		
}
