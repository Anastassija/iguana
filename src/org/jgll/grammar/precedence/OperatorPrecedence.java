package org.jgll.grammar.precedence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jgll.grammar.Grammar;
import org.jgll.grammar.patterns.AbstractPattern;
import org.jgll.grammar.patterns.ExceptPattern;
import org.jgll.grammar.patterns.PrecedencePattern;
import org.jgll.grammar.slot.NonterminalGrammarSlot;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Rule;
import org.jgll.grammar.symbol.Symbol;
import org.jgll.grammar.transformation.GrammarTransformation;
import org.jgll.util.logging.LoggerWrapper;

public class OperatorPrecedence implements GrammarTransformation {
	
	private static final LoggerWrapper log = LoggerWrapper.getLogger(OperatorPrecedence.class);
	
	private Map<Nonterminal, List<List<Symbol>>> definitions;
	
	private Map<String, Integer> newNonterminals;
	
	private Map<Nonterminal, List<PrecedencePattern>> precednecePatterns;
	
	private List<ExceptPattern> exceptPatterns;
	
	private Map<List<List<Symbol>>, Nonterminal> existingAlternates;
	
	private List<Rule> newRules;
	
	public OperatorPrecedence() {
		this.newNonterminals = new HashMap<>();
		this.precednecePatterns = new HashMap<>();
		this.existingAlternates = new HashMap<>();
		this.exceptPatterns = new ArrayList<>();
		this.newRules = new ArrayList<>();
	}
	
	@Override
	public Grammar transform(Grammar grammar) {
		this.definitions = new HashMap<>(grammar.getDefinitions());
		rewritePrecedencePatterns();
		rewriteExceptPatterns();
		
		for(Nonterminal nonterminal : this.definitions.keySet()) {
			if(nonterminal.getIndex() > 0) {
				addNewRules(nonterminal, this.definitions.get(nonterminal));
			}
		}
		
		Grammar.Builder builder = new Grammar.Builder();
		for (Rule rule : grammar.getRules()) {
			builder.addRule(rule);
		}
		
		for (Rule rule : newRules) {
			builder.addRule(rule);
		}
		
		return builder.build();
	}
	
	private void rewriteExceptPatterns() {
		rewriteExceptPatterns(groupPatterns(exceptPatterns));
	}
	
	private void rewriteExceptPatterns(Map<ExceptPattern, List<List<Symbol>>> patterns) {
		for (Entry<ExceptPattern, List<List<Symbol>>> e : patterns.entrySet()) {
			ExceptPattern pattern = e.getKey();
			
			for (List<Symbol> alt : definitions.get(pattern.getNonterminal())) {
				if (match(plain(alt), pattern.getParent())) {
					Nonterminal newNonterminal = createNewNonterminal(alt, pattern.getPosition(), e.getValue());
					alt.set(pattern.getPosition(), newNonterminal);
				}
			}
			
			if(newNonterminals.containsKey(pattern.getNonterminal().getName())) {
				int index = newNonterminals.get(pattern.getNonterminal().getName());
				for(int i = 1; i <= index; i++) {
					Nonterminal nonterminal = new Nonterminal.Builder(pattern.getNonterminal().getName()).setIndex(i).setEbnfList(pattern.getNonterminal().isEbnfList()).build();
					for(List<Symbol> alt : definitions.get(nonterminal)) {
						
						if(alt != null) {
							if (match(plain(alt), pattern.getParent())) {
								Nonterminal newNonterminal = createNewNonterminal(alt, pattern.getPosition(), e.getValue());
								alt.set(pattern.getPosition(), newNonterminal);
							}
						}						
					}					
				}
			}
		}
	}

	
	public void addPrecedencePattern(Nonterminal nonterminal, Rule parent, int position, Rule child) {
		PrecedencePattern pattern = new PrecedencePattern(nonterminal, parent.getBody(), position, child.getBody());

		if (precednecePatterns.containsKey(nonterminal)) {
			precednecePatterns.get(nonterminal).add(pattern);
		} else {
			List<PrecedencePattern> set = new ArrayList<>();
			set.add(pattern);
			precednecePatterns.put(nonterminal, set);
		}
		log.debug("Precedence pattern added %s", pattern);
	}
	
	public void addExceptPattern(Nonterminal nonterminal, Rule parent, int position, Rule child) {
		ExceptPattern pattern = new ExceptPattern(nonterminal, parent.getBody(), position, child.getBody());
		exceptPatterns.add(pattern);
		log.debug("Except pattern added %s", pattern);
	}
	
	/**
	 * Groups filters based on their parent and position.
	 * For example, two filters (E, E * .E, E + E) and
	 * (E, E * .E, E * E) will be grouped as:
	 * (E, E * .E, {E * E, E + E}) 
	 * 
	 * @param patterns
	 * @return
	 */
	private <T extends AbstractPattern> Map<T, List<List<Symbol>>> groupPatterns(Iterable<T> patterns) {
		Map<T, Set<List<Symbol>>> group = new LinkedHashMap<>();
		
		for(T pattern : patterns) {
			Set<List<Symbol>> set = group.get(pattern);
			if(set == null) {
				set = new LinkedHashSet<>();
				group.put(pattern, set);
			}
			set.add(pattern.getChild());
		}
		
		Map<T, List<List<Symbol>>> result = new LinkedHashMap<>();
		for(Entry<T, Set<List<Symbol>>> e : group.entrySet()) {
			result.put(e.getKey(), new ArrayList<>(e.getValue()));
		}
		
		return result;
	}
	
	private void rewritePrecedencePatterns() {
		for (Entry<Nonterminal, List<PrecedencePattern>> entry : precednecePatterns.entrySet()) {
			log.debug("Applying the pattern %s with %d.", entry.getKey(), entry.getValue().size());

			Nonterminal nonterminal = entry.getKey();
			Map<PrecedencePattern, List<List<Symbol>>> patterns = groupPatterns(entry.getValue());
			
			rewriteFirstLevel(nonterminal, patterns);
			rewriteDeeperLevels(nonterminal, patterns);
		}
	}

	private void rewriteDeeperLevels(Nonterminal head, Map<PrecedencePattern, List<List<Symbol>>> patterns) {

		for(Entry<PrecedencePattern, List<List<Symbol>>> e : patterns.entrySet()) {
			
			PrecedencePattern pattern = e.getKey();
			List<List<Symbol>> children = e.getValue();
			
			for (List<Symbol> alt : definitions.get(head)) {
				if (pattern.isLeftMost() && match(plain(alt), pattern.getParent())) {
					rewriteRightEnds((Nonterminal)alt.get(0), pattern, children);
				}

				if (pattern.isRightMost() && match(plain(alt), pattern.getParent())) {
					rewriteLeftEnds((Nonterminal)alt.get(alt.size() - 1), pattern, children);
				}
			}
		}
	}
	
	private void rewriteLeftEnds(Nonterminal nonterminal, PrecedencePattern pattern, List<List<Symbol>> children) {
		
		// Direct filtering
		if(plainEqual(nonterminal, pattern.getNonterminal())) {
			
			for(List<Symbol> alternate : definitions.get(nonterminal)) {
				
				if(alternate == null) {
					continue;
				}
				
				if(!(alternate.get(0) instanceof Nonterminal)) {
					continue;
				}

				Nonterminal first = (Nonterminal) alternate.get(0);
				
				if(contains(first, children)) {
					Nonterminal newNonterminal = createNewNonterminal(alternate, 0, children);
					alternate.set(0, newNonterminal);
					rewriteLeftEnds(newNonterminal, pattern, children);
				}
			}			
		} else {
			assert pattern.isRightMost();

			Set<List<Symbol>> alternates = new LinkedHashSet<>(); 
			getRightEnds(nonterminal, pattern.getNonterminal(), alternates);

			for(List<Symbol> alt : alternates) {
				rewriteLeftEnds((Nonterminal) alt.get(alt.size() - 1), pattern, children);
			}
		}
	}
	
	private void rewriteRightEnds(Nonterminal nonterminal, PrecedencePattern pattern, List<List<Symbol>> children) {
		
		// Direct filtering
		if(plainEqual(nonterminal, pattern.getNonterminal())) {
			
			for(List<Symbol> alternate : definitions.get(nonterminal)) {
				
				if(alternate == null) {
					continue;
				}
				
				if(!(alternate.get(alternate.size() - 1) instanceof Nonterminal)) {
					continue;
				}

				Nonterminal last = (Nonterminal) alternate.get(alternate.size() - 1); 
				
				if(contains(last, children)) {
					Nonterminal newNonterminal = createNewNonterminal(alternate, alternate.size() - 1, children);
					alternate.set(alternate.size() - 1, newNonterminal);
					rewriteRightEnds(newNonterminal, pattern, children);
				}				
			}
			
		} else {
			
			assert pattern.isLeftMost();

			Set<List<Symbol>> alternates = new LinkedHashSet<>(); 
			getLeftEnds(nonterminal, pattern.getNonterminal(), alternates);

			for(List<Symbol> alt : alternates) {
				rewriteRightEnds((Nonterminal) alt.get(0), pattern, children);
			}
		}
	}
	
	private Nonterminal createNewNonterminal(List<Symbol> alt, int position, List<List<Symbol>> filteredAlternates) {
		
		Nonterminal filteredNonterminal = (Nonterminal) alt.get(position);

		List<List<Symbol>> set = without(filteredNonterminal, filteredAlternates);
		Nonterminal newNonterminal = existingAlternates.get(plain2(set));
		
		if(newNonterminal == null) {
			newNonterminal = createNewNonterminal(filteredNonterminal);
			List<List<Symbol>> copy = copyAlternates(set);
			existingAlternates.put(plain2(copy), newNonterminal);
			definitions.put(newNonterminal, copy);
		}
		
		return newNonterminal;
	}
	
	private void rewriteFirstLevel(Nonterminal head, Map<PrecedencePattern, List<List<Symbol>>> patterns) {
		
		Map<PrecedencePattern, Nonterminal> freshNonterminals = new LinkedHashMap<>();
		
		Map<List<List<Symbol>>, Nonterminal> map = new HashMap<>();
		
		if(!newNonterminals.containsKey(head.getName())) {
			newNonterminals.put(head.getName(), 0);
		}
		
		// Creating fresh nonterminals
		for(Entry<PrecedencePattern, List<List<Symbol>>> e : patterns.entrySet()) {
			
			PrecedencePattern pattern = e.getKey();
			
			Nonterminal freshNonterminal = map.get(e.getValue());
			
			if(freshNonterminal == null) {
				
				int index = newNonterminals.get(pattern.getNonterminal().getName());
				freshNonterminal = new Nonterminal.Builder(pattern.getNonterminal().getName()).setIndex(index + 1).setEbnfList(pattern.getNonterminal().isEbnfList()).build();
				newNonterminals.put(freshNonterminal.getName(), index + 1);
				map.put(e.getValue(), freshNonterminal);
			}

			freshNonterminals.put(pattern, freshNonterminal);
		}
		
		// Replacing nonterminals with their fresh ones
		for(Entry<PrecedencePattern, List<List<Symbol>>> e : patterns.entrySet()) {
			
			for(List<Symbol> alt : definitions.get(head)) {
				
				PrecedencePattern pattern = e.getKey();
				
				if(!match(plain(alt), pattern.getParent())) {
					continue;
				}
				
				log.trace("Applying the pattern %s on %s.", pattern, alt);
				
				if (!pattern.isDirect()) {
					
					Nonterminal copy;
					
					Set<List<Symbol>> alternates = new LinkedHashSet<>();
					if(pattern.isLeftMost()) {
						copy = copyIndirectAtLeft((Nonterminal) alt.get(pattern.getPosition()), pattern.getNonterminal());
						getLeftEnds(copy, pattern.getNonterminal(), alternates);
						for(List<Symbol> a : alternates) {
							a.set(0, freshNonterminals.get(pattern));
						}
					} else {
						copy = copyIndirectAtRight((Nonterminal) alt.get(pattern.getPosition()), pattern.getNonterminal());
						getRightEnds(copy, pattern.getNonterminal(), alternates);
						for(List<Symbol> a : alternates) {
							a.set(a.size() - 1, freshNonterminals.get(pattern));
						}
					}
					
					alt.set(pattern.getPosition(), copy);
					
				} else {
					alt.set(pattern.getPosition(), freshNonterminals.get(pattern));
				}
			}
		}
		
		// creating the body of fresh direct nonterminals
		for(Entry<PrecedencePattern, Nonterminal> e : freshNonterminals.entrySet()) {
			PrecedencePattern pattern = e.getKey();
			Nonterminal freshNonterminal = e.getValue();
			List<List<Symbol>> alternates = copyAlternates(without(head, patterns.get(pattern)));
			definitions.put(freshNonterminal, alternates);
		}
	}
	
	/**
	 * 
	 * Returns a list of all nonterminals with the given name which are
	 * reachable from the given head and are on the left-most end.
	 * 
	 * @param head
	 * @param directName
	 * @param alternates
	 */
	private void getLeftEnds(Nonterminal head, Nonterminal nonterminal, Set<List<Symbol>> nonterminals) {
		getLeftEnds(head, nonterminal, nonterminals, new HashSet<Nonterminal>());
	}
	
	private void getLeftEnds(Nonterminal head, Nonterminal nonterminal, Set<List<Symbol>> nonterminals, Set<Nonterminal> visited) {
		
		if(visited.contains(head)) {
			return;
		}
		
		for (List<Symbol> alt : definitions.get(head)) {
			
			if(alt == null) {
				continue;
			}
			
			if (alt.get(0) instanceof Nonterminal) {
				Nonterminal first = (Nonterminal) alt.get(0);
				if (first.equals(nonterminal)) {
					nonterminals.add(alt);
				} else {
					visited.add(first);
					getLeftEnds(first, nonterminal, nonterminals, visited);
				}
			}
		}
	}

	/**
	 * 
	 * Returns a list of all nonterminals with the given name which are
	 * reachable from the given head and are on the right-most end.
	 * 
	 * @param head
	 * @param directNonterminal
	 * @param alternates
	 */
	private void getRightEnds(Nonterminal head, Nonterminal directNonterminal, Set<List<Symbol>> alternates) {
		getRightEnds(head, directNonterminal, alternates, new HashSet<Nonterminal>());
	}
	
	private void getRightEnds(Nonterminal head, Nonterminal directNonterminal, Set<List<Symbol>> alternates, Set<Nonterminal> visited) {
		
		if(visited.contains(head)) {
			return;
		}
		
		for (List<Symbol> alt : definitions.get(head)) {
			
			if(alt == null) {
				continue;
			}
			
			if (alt.get(alt.size() - 1) instanceof NonterminalGrammarSlot) {
				Nonterminal last = (Nonterminal) alt.get(alt.size() - 1);
				if (last.equals(directNonterminal)) {
					alternates.add(alt);
				} else {
					visited.add(last);
					getRightEnds(last, directNonterminal, alternates, visited);
				}
			}
		}
	}
	
	
	private Nonterminal copyIndirectAtLeft(Nonterminal head, Nonterminal directNonterminal) {
		return copyIndirectAtLeft(head, directNonterminal, new HashMap<Nonterminal, Nonterminal>());
	}

	private Nonterminal copyIndirectAtRight(Nonterminal head, Nonterminal directNonterminal) {
		return copyIndirectAtRight(head, directNonterminal, new HashMap<Nonterminal, Nonterminal>());
	}
	
	private Nonterminal copyIndirectAtLeft(Nonterminal head, Nonterminal directName, HashMap<Nonterminal, Nonterminal> map) {
		
		Nonterminal copy = map.get(head);
		if(copy != null) {
			return copy;
		}
		
		copy = createNewNonterminal(head);
		map.put(head, copy);

		List<List<Symbol>> copyAlternates = copyAlternates(definitions.get(head));
		definitions.put(copy, copyAlternates);
		
		for(List<Symbol> alt : copyAlternates) {
			if(alt.get(0) instanceof Nonterminal) {
				Nonterminal nonterminal = (Nonterminal) alt.get(0);
				// Leave the direct nonterminal, copy indirect ones
				if(!nonterminal.equals(directName)) {
					alt.set(0, copyIndirectAtLeft(nonterminal, directName, map));
				}
			}
		}
		
		return copy;
	}
	
	private Nonterminal copyIndirectAtRight(Nonterminal head, Nonterminal directNonterminal, HashMap<Nonterminal, Nonterminal> map) {
		
		Nonterminal copy = map.get(head);
		if(copy != null) {
			return copy;
		}
		
		copy = createNewNonterminal(head);
		map.put(head, copy);
		
		List<List<Symbol>> copyAlternates = copyAlternates(definitions.get(head));
		definitions.put(copy, copyAlternates);
		
		for(List<Symbol> alt : copyAlternates) {
			if(alt.get(alt.size() - 1) instanceof Nonterminal) {
				Nonterminal nonterminal = (Nonterminal) alt.get(alt.size() - 1);
				
				// Leave the direct nonterminal, copy indirect ones
				if(!nonterminal.equals(directNonterminal)) {
					alt.set(alt.size() - 1, copyIndirectAtLeft(nonterminal, directNonterminal, map));
				}
			}
		}
		
		return copy;
	}
	
	private Nonterminal createNewNonterminal(Nonterminal nonterminal) {
		if(!newNonterminals.containsKey(nonterminal.getName())) {
			newNonterminals.put(nonterminal.getName(), 0);
		}
		
		int index = newNonterminals.get(nonterminal.getName());
		Nonterminal newNonterminal = new Nonterminal.Builder(nonterminal.getName()).setIndex(index + 1).setEbnfList(nonterminal.isEbnfList()).build();
		newNonterminals.put(nonterminal.getName(), index + 1);
		return newNonterminal;
	}
	
	public boolean match(List<Symbol> list1, List<Symbol> list2) {
		
		if(list1.size() != list2.size()) {
			return false;
		}
		
		for(int i = 0; i < list1.size(); i++) {
			if(!list1.get(i).equals(list2.get(i))) {
				return false;
			}
		}
		return true;
	}
	
	private List<List<Symbol>> copyAlternates(List<List<Symbol>> alternates) {
		List<List<Symbol>> copy = new ArrayList<>();
		
		for(List<Symbol> alternate : alternates) {
			
			if(alternate == null) {
				copy.add(null);
			} else {
				List<Symbol> copyAlt = new ArrayList<>();
				for(Symbol symbol : alternate) {
					copyAlt.add(symbol);
				}
				copy.add(copyAlt);				
			}
		}
		
		return copy;
	}
	
	private List<List<Symbol>> without(Nonterminal head, List<List<Symbol>> set) {
		List<List<Symbol>> without = new ArrayList<>();
		for(List<Symbol> alt : definitions.get(head)) {
			
			if(alt == null) {
				without.add(null);
			} 
			else if (!set.contains(plain(alt))) {
				without.add(alt);
			} else {
				without.add(null);
			}
		}
		return without;
	}
	
	public static List<List<Symbol>> plain2(List<List<Symbol>> alternates) {
		List<List<Symbol>> list = new ArrayList<>();
		
		for(List<Symbol> l : alternates) {
			if(l == null) {
				list.add(null);
			} else {
				list.add(plain(l));				
			}
		}
		
		return list;
	}
	
	public static Rule plain(Rule rule) {
		Nonterminal plainHead = (Nonterminal) plain(rule.getHead());
		List<Symbol> plainAlternate = plain(rule.getBody());
		return new Rule(plainHead, plainAlternate);
	}
	
	public static List<Symbol> plain(List<Symbol> alternate) {
		List<Symbol> plain = new ArrayList<>();
		for(Symbol symbol : alternate) {
			if(symbol instanceof Nonterminal && ((Nonterminal) symbol).getIndex() > 0) {
				Nonterminal nonterminal = new Nonterminal.Builder(symbol.getName()).setEbnfList(((Nonterminal) symbol).isEbnfList()).build();
				plain.add(nonterminal);
			} else {
				plain.add(symbol);
			}
		}
		return plain;
	}
	
	public static Symbol plain(Symbol symbol) {
		if(symbol instanceof Nonterminal && ((Nonterminal) symbol).getIndex() > 0) {
			return new Nonterminal.Builder(symbol.getName()).setEbnfList(((Nonterminal) symbol).isEbnfList()).build();
		} else {
			return symbol;
		}
	}
	
	public static boolean plainEqual(Nonterminal n1, Nonterminal n2) {
		return n1.getName().equals(n2.getName());
	}

	private void addNewRules(Nonterminal nonterminal, List<List<Symbol>> alternates) {
		for(List<Symbol> alternate : alternates) {
			Rule rule = new Rule(nonterminal, alternate);
			newRules.add(rule);
		}
	}
	
	private boolean contains(Nonterminal nonterminal, List<List<Symbol>> alternates) {
		
		List<List<Symbol>> set = definitions.get(nonterminal);
		
		for(List<Symbol> alt1 : set) {
			
			if(alt1 == null) continue;
			
			for(List<Symbol> alt2 : alternates) {
				
				if(alt2 == null) continue;
				
				if(plain(alt1).equals(alt2)) {
					return true;
				}
			}			
		}
		
		return false;
	}
	
}
