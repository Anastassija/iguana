package org.jgll.grammar;

import static org.jgll.grammar.Conditions.getPostConditions;
import static org.jgll.grammar.Conditions.getPreConditions;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgll.grammar.condition.Condition;
import org.jgll.grammar.condition.ConditionType;
import org.jgll.grammar.exception.GrammarValidationException;
import org.jgll.grammar.precedence.OperatorPrecedence;
import org.jgll.grammar.slot.BodyGrammarSlot;
import org.jgll.grammar.slot.EpsilonGrammarSlot;
import org.jgll.grammar.slot.HeadGrammarSlot;
import org.jgll.grammar.slot.LastGrammarSlot;
import org.jgll.grammar.slot.NonterminalGrammarSlot;
import org.jgll.grammar.slot.factory.GrammarSlotFactory;
import org.jgll.grammar.slot.test.ConditionTest;
import org.jgll.grammar.symbol.Character;
import org.jgll.grammar.symbol.EOF;
import org.jgll.grammar.symbol.Epsilon;
import org.jgll.grammar.symbol.Keyword;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Rule;
import org.jgll.grammar.symbol.Symbol;
import org.jgll.regex.RegularExpression;
import org.jgll.regex.automaton.Automaton;
import org.jgll.regex.automaton.RunnableAutomaton;
import org.jgll.util.Tuple;
import org.jgll.util.logging.LoggerWrapper;

public class GrammarGraphBuilder implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final LoggerWrapper log = LoggerWrapper.getLogger(GrammarGraphBuilder.class);

	Map<Nonterminal, HeadGrammarSlot> nonterminalsMap;

	List<BodyGrammarSlot> slots;
	
	List<HeadGrammarSlot> headGrammarSlots;

	int maximumNumAlternates;

	int maxDescriptors;
	
	int averageDescriptors;
	
	double stDevDescriptors;

	String name;
	
	Grammar grammar;
	
	Map<RegularExpression, Integer> tokenIDMap;
	
	List<RegularExpression> tokens;
	
	List<Nonterminal> nonterminals;
	
	RunnableAutomaton[] dfas;
	
	Map<Nonterminal, Set<RegularExpression>> firstSets;

	Map<Nonterminal, Set<RegularExpression>> followSets;
	
	Set<Nonterminal> ll1SubGrammarNonterminals;

	private GrammarSlotFactory grammarSlotFactory;
	
	Map<String, Integer> nonterminalIds;
	
	Map<List<Symbol>, Integer> intermediateNodeIds;
	
	Map<Tuple<Nonterminal, List<Symbol>>, Integer> packedNodeIds;
	
	Map<Nonterminal, List<Set<RegularExpression>>> predictionSets;
	
	/**
	 * Indexed by nonterminal index and alternate index
	 */
	Object[][] objects;
	
	public GrammarGraphBuilder(Grammar grammar, GrammarSlotFactory grammarSlotFactory) {
		this("no-name", grammar, grammarSlotFactory);
	}
	
	public GrammarGraphBuilder(String name, Grammar grammar, GrammarSlotFactory grammarSlotFactory) {
		this.name = name;
		this.grammarSlotFactory = grammarSlotFactory;
		this.grammar = grammar;

		Set<RuntimeException> exceptions = grammar.validate();
		if (!exceptions.isEmpty()) {
			throw new GrammarValidationException(exceptions);
		}
		
		headGrammarSlots = new ArrayList<>();
		nonterminalsMap = new HashMap<>();
		
		nonterminalIds = new HashMap<>();
		intermediateNodeIds = new HashMap<>();
		packedNodeIds = new HashMap<>();
		
		tokenIDMap = new HashMap<>();
		tokenIDMap.put(Epsilon.getInstance(), 0);
		tokenIDMap.put(EOF.getInstance(), 1);
		
		tokens = new ArrayList<>();
		tokens.add(Epsilon.getInstance());
		tokens.add(EOF.getInstance());
		
		nonterminals = new ArrayList<>();
	}

	public GrammarGraph build() {
		
		calculateIds();
		
		objects = new Object[grammar.getNonterminals().size()][];
		for(Nonterminal nonterminal : grammar.getNonterminals()) {
			objects[nonterminalIds.get(nonterminal.getName())] = new Object[grammar.getAlternatives(nonterminal).size()];
		}

		for (Nonterminal nonterminal : grammar.getNonterminals()) {
			for (int alternateIndex = 0; alternateIndex < grammar.getAlternatives(nonterminal).size(); alternateIndex++) {
				int nonterminalIndex = nonterminalIds.get(nonterminal.getName());
				objects[nonterminalIndex][alternateIndex] = grammar.getObject(nonterminal, alternateIndex);				
			}
		}
		
		long start;
		long end;
		
		
		start = System.nanoTime();
		GrammarOperations grammarOperations = new GrammarOperations(grammar);
		end = System.nanoTime();
		log.info("First and follow set calculation in %d ms", (end - start) / 1000_000);
		
		firstSets = grammarOperations.getFirstSets();
		followSets = grammarOperations.getFollowSets();
		predictionSets = grammarOperations.getPredictionSets();
		
//		start = System.nanoTime();
//		Map<Nonterminal, Set<Nonterminal>> reachabilityGraph = GrammarProperties.calculateReachabilityGraph(definitions);
		ll1SubGrammarNonterminals = new HashSet<>();
//		ll1SubGrammarNonterminals = GrammarProperties.calculateLLNonterminals(definitions, firstSets, followSets, reachabilityGraph);
//		end = System.nanoTime();
//		log.info("LL1 property is calcuated in in %d ms", (end - start) / 1000_000);
				
		
		start = System.nanoTime();
		
		for (Nonterminal nonterminal : grammar.getNonterminals()) {
			convert(nonterminal);
		}

		end = System.nanoTime();
		log.info("Grammar Graph is composed in %d ms", (end - start) / 1000_000);
		
		start = System.nanoTime();
		createAutomatonsMap();
		end = System.nanoTime();
		log.info("Automatons created in %d ms", (end - start) / 1000_000);
		
		// related to rewriting the patterns
		removeUnusedNewNonterminals();
		
//		GrammarProperties.setPredictionSetsForConditionals(conditionSlots);
		
		slots = new ArrayList<>();
		
		for(HeadGrammarSlot nonterminal : headGrammarSlots) {
			for (BodyGrammarSlot slot : nonterminal.getFirstSlots()) {
				BodyGrammarSlot currentSlot = slot;
				
				while(currentSlot != null) {
					slots.add(currentSlot);
					currentSlot = currentSlot.next();
				}
			}
		}

		
		return new GrammarGraph(this);
	}
	
	int nonterminalId = 0;
	int intermediateId = 0;
	Map<Nonterminal, Set<List<Symbol>>> addedDefinitions = new HashMap<>();

	public GrammarGraphBuilder calculateIds() {
				
		for (Rule rule : grammar.getRules()) {
			
			Nonterminal head = rule.getHead();
			
			if(!nonterminalIds.containsKey(head.getName())) {
				nonterminalIds.put(head.getName(), nonterminalId++);
				nonterminals.add(head);
			}
			
			if(rule.getBody() != null) {
				for(int i = 2; i < rule.getBody().size(); i++) {
					List<Symbol> prefix = rule.getBody().subList(0, i);
					List<Symbol> plain = OperatorPrecedence.plain(prefix);
					if(!intermediateNodeIds.containsKey(plain)) {
						intermediateNodeIds.put(plain, intermediateId++);
					}
				}
				
				packedNodeIds.put(Tuple.of(rule.getHead(), rule.getBody()), grammar.getAlternatives(head).size() - 1);
			}
		}
		
		return this;
	}
 
	private void convert(Nonterminal head) {
		List<List<Symbol>> alternates = grammar.getAlternatives(head);
		popActions.clear();
		
		HeadGrammarSlot headGrammarSlot = getHeadGrammarSlot(head);
		
		int alternateIndex = 0;
		
		for(List<Symbol> body : alternates) {
			
			if(body == null) {
				alternateIndex++;
				continue;
			}
			
			BodyGrammarSlot currentSlot = null;
	
			if (body.size() == 0) {
				EpsilonGrammarSlot epsilonSlot = grammarSlotFactory.createEpsilonGrammarSlot(getSlotName(head, body, 0), headGrammarSlot);
				epsilonSlot.setAlternateIndex(alternateIndex);
				headGrammarSlot.setFirstGrammarSlotForAlternate(epsilonSlot, alternateIndex);
			} 
			else {
				BodyGrammarSlot firstSlot = null;
				int symbolIndex = 0;
				for (; symbolIndex < body.size(); symbolIndex++) {
					
					currentSlot = getBodyGrammarSlot(head, body, symbolIndex, currentSlot);
	
					if (symbolIndex == 0) {
						firstSlot = currentSlot;
					}
				}
	
				ConditionTest popCondition = getPostConditions(popActions);
				LastGrammarSlot lastGrammarSlot = grammarSlotFactory.createLastGrammarSlot(body, symbolIndex, getSlotName(head, body, symbolIndex), currentSlot, headGrammarSlot, popCondition);
	
				lastGrammarSlot.setAlternateIndex(alternateIndex);
				headGrammarSlot.setFirstGrammarSlotForAlternate(firstSlot, alternateIndex);
			}
			alternateIndex++;
		}
	}
	
	/**
	 * Removes unnecssary follow restrictions
	 * @return 
	 */
	private ConditionTest getPostConditionsForRegularExpression(Set<Condition> conditions) {
		Set<Condition> set = new HashSet<>(conditions);
		
		for (Condition condition : conditions) {
			if(condition.getType() != ConditionType.NOT_MATCH) {
				set.add(condition);
			} 
//			else if (condition.getType() != ConditionType.NOT_FOLLOW) {
//				set.add(condition);
//			}
		}
		
		// Make RegularExpression completely immutable. Now this works because
		// getConditons can be modified.
		return getPostConditions(set);
	}
	
	private String getSlotName(Nonterminal head, List<Symbol> body, int index) {
		StringBuilder sb = new StringBuilder();
		
		sb.append(head.getName()).append(" ::= ");
		
		for(int i = 0; i < body.size(); i++) {
			Symbol s = body.get(i);
			
			if(i == index) {
				sb.append(". ");
			}
			
			if(s instanceof Nonterminal) {
				sb.append(s.getName()).append(" ");
			} else {
				sb.append(s).append(" ");				
			}
		}

		if(index == body.size()) {
			sb.append(".");
		} else {
			sb.delete(sb.length() - 1, sb.length());			
		}
		
		return sb.toString();
	}
	
	private int getSlotId(List<Symbol> alt, int index) {

		if(alt.size() <= 2 || index <= 1) {
			return -1;
		}

		// Last grammar slot
		if(index == alt.size()) {
			return -1;
		}

		return intermediateNodeIds.get(OperatorPrecedence.plain(alt.subList(0, index)));
	}
	
	Set<Condition> popActions = new HashSet<>();
	
	private BodyGrammarSlot getBodyGrammarSlot(Nonterminal head, List<Symbol> body, int symbolIndex, BodyGrammarSlot currentSlot) {
		Symbol symbol = body.get(symbolIndex);
		
		if(symbol instanceof RegularExpression) {
			RegularExpression token = (RegularExpression) symbol;
			
			ConditionTest preConditionsTest = getPreConditions(symbol.getConditions());
			ConditionTest postConditionsTest = getPostConditionsForRegularExpression(symbol.getConditions());
			ConditionTest popConditionsTest = getPostConditions(popActions);
			
			return grammarSlotFactory.createTokenGrammarSlot(body, symbolIndex, getSlotId(body, symbolIndex), 
					getSlotName(head, body, symbolIndex), currentSlot, getTokenID(token), preConditionsTest, postConditionsTest, popConditionsTest);
		}
		
		// Nonterminal
		else {
			ConditionTest preConditionsTest = getPreConditions(symbol.getConditions());
			ConditionTest popConditionsTest = getPostConditions(popActions);
			
			popActions = symbol.getConditions();
			
			HeadGrammarSlot nonterminal = getHeadGrammarSlot((Nonterminal) symbol);
			return grammarSlotFactory.createNonterminalGrammarSlot(body, symbolIndex, getSlotId(body, symbolIndex), getSlotName(head, body, symbolIndex), currentSlot, nonterminal, preConditionsTest, popConditionsTest);						
		}		
	}

	private HeadGrammarSlot getHeadGrammarSlot(Nonterminal nonterminal) {
		HeadGrammarSlot headGrammarSlot = nonterminalsMap.get(nonterminal);

		if (headGrammarSlot == null) {
			headGrammarSlot = grammarSlotFactory.createHeadGrammarSlot(nonterminal, nonterminalIds.get(nonterminal.getName()), grammar.getAlternatives(nonterminal), firstSets, followSets, predictionSets);
			nonterminalsMap.put(nonterminal, headGrammarSlot);
			headGrammarSlots.add(headGrammarSlot);
		}

		return headGrammarSlot;
	}
	
	private void createAutomatonsMap() {
		dfas = new RunnableAutomaton[tokens.size()];
		
		System.out.println(tokens);
		
		for(RegularExpression regex : tokens) {
			
			Integer id = tokenIDMap.get(regex);
			
//			if(regex instanceof CharacterClass) {
//				if(regex.getConditions().isEmpty()) {
//					CharacterClass charClass = (CharacterClass) regex;
//					if(charClass.size() == 1) {
//						Range range = charClass.get(0);
//						
//						if(range.getStart() == range.getEnd()) {
//							Matcher matcher = new CharacterMatcher(range.getStart());
//							dfas[id] = matcher;
//							continue;
//						}
//					}					
//				}
//			}
			Automaton a = regex.getAutomaton();
			dfas[id] = a.getRunnableAutomaton();
			System.out.println(a);
		}
	}
	
	private int getTokenID(RegularExpression token) {
		if(tokenIDMap.containsKey(token)) {
			return tokenIDMap.get(token);
		}
		int id = tokenIDMap.size();
		tokenIDMap.put(token, id);
		tokens.add(token);
		return id;
	}
	
	/**
	 * Creates the corresponding grammar rule for the given keyword.
	 * For example, for the keyword "if", a rule If ::= [i][f]
	 * is returned.
	 * 
	 * @param keyword
	 * @return
	 */
	public static Rule fromKeyword(Keyword keyword) {
		Rule.Builder builder = new Rule.Builder(Nonterminal.withName(keyword.getName()));
		for(Character c : keyword.getSequence()) {
			builder.addSymbol(c);
		}
		return builder.build();
	}

	
	@SafeVarargs
	protected static <T> Set<T> set(T... objects) {
		Set<T> set = new HashSet<>();
		for (T t : objects) {
			set.add(t);
		}
		return set;
	}
	
	/**
	 * Removes non-reachable nonterminals from the given nonterminal
	 * 
	 * @param head
	 * @return
	 */
	public GrammarGraphBuilder removeUnusedNonterminals(Nonterminal nonterminal) {

		Set<HeadGrammarSlot> referedNonterminals = new HashSet<>();
		Deque<HeadGrammarSlot> queue = new ArrayDeque<>();
		queue.add(nonterminalsMap.get(nonterminal));
		
		while(!queue.isEmpty()) {
			HeadGrammarSlot head = queue.poll();
			referedNonterminals.add(head);
			
			for(BodyGrammarSlot slot : head.getFirstSlots()) {
				BodyGrammarSlot currentSlot = slot;
				
				while(currentSlot.next() != null) {
					if(currentSlot instanceof NonterminalGrammarSlot) {
						if(!referedNonterminals.contains(((NonterminalGrammarSlot) currentSlot).getNonterminal())) {
							queue.add(((NonterminalGrammarSlot) currentSlot).getNonterminal());
						}
					}
					currentSlot = currentSlot.next();
				}
			}
		}

		headGrammarSlots.retainAll(referedNonterminals);

		return this;
	}
	
	
	/**
	 * The reason that we only remove unused new nonterminals, instead of
	 * all nonterminals, is that each nonterminal can be a potential start
	 * symbol of the grammar.
	 * 
	 * New nonterminals are generated during the parser generation time and
	 * are not visible to the outside. 
	 */
	private void removeUnusedNewNonterminals() {
		Set<HeadGrammarSlot> reachableNonterminals = new HashSet<>();
		Deque<HeadGrammarSlot> queue = new ArrayDeque<>();

		for(HeadGrammarSlot head : headGrammarSlots) {
			queue.add(head);			
		}
		
		while(!queue.isEmpty()) {
			HeadGrammarSlot head = queue.poll();
			reachableNonterminals.add(head);
			
			for(BodyGrammarSlot slot : head.getFirstSlots()) {
				
				if(slot == null) continue;
				
				BodyGrammarSlot currentSlot = slot;
				
				while(currentSlot.next() != null) {
					if(currentSlot instanceof NonterminalGrammarSlot) {
						HeadGrammarSlot reachableHead = ((NonterminalGrammarSlot) currentSlot).getNonterminal();
						if(!reachableNonterminals.contains(reachableHead)) {
							queue.add(reachableHead);
						}
					}
					currentSlot = currentSlot.next();
				}
			}
		}

		// Remove new nonterminals
//		for(List<HeadGrammarSlot> list : newNonterminalsMap.values()) {
//			list.retainAll(reachableNonterminals);
//		}
	}
	
}