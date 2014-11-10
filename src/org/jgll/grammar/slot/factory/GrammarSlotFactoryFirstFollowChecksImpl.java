package org.jgll.grammar.slot.factory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgll.grammar.slot.BodyGrammarSlot;
import org.jgll.grammar.slot.EpsilonGrammarSlot;
import org.jgll.grammar.slot.HeadGrammarSlot;
import org.jgll.grammar.slot.LastGrammarSlot;
import org.jgll.grammar.slot.NonterminalGrammarSlot;
import org.jgll.grammar.slot.TokenGrammarSlot;
import org.jgll.grammar.slot.nodecreator.DummyNodeCreator;
import org.jgll.grammar.slot.nodecreator.IntermediateNodeCreator;
import org.jgll.grammar.slot.nodecreator.NonterminalNodeCreator;
import org.jgll.grammar.slot.nodecreator.NonterminalWithOneChildNodeCreator;
import org.jgll.grammar.slot.nodecreator.RightChildNodeCreator;
import org.jgll.grammar.slot.specialized.LastTokenSlot;
import org.jgll.grammar.slot.test.ArrayFollowTest;
import org.jgll.grammar.slot.test.ArrayPredictionTest;
import org.jgll.grammar.slot.test.ConditionTest;
import org.jgll.grammar.slot.test.FollowTest;
import org.jgll.grammar.slot.test.PredictionTest;
import org.jgll.grammar.slot.test.TreeMapFollowTest;
import org.jgll.grammar.slot.test.TreeMapPredictionTest;
import org.jgll.grammar.symbol.Epsilon;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Range;
import org.jgll.grammar.symbol.Rule;
import org.jgll.grammar.symbol.Symbol;
import org.jgll.regex.RegularExpression;
import org.jgll.util.Tuple;

import static org.jgll.grammar.slot.BodyGrammarSlot.*;

public class GrammarSlotFactoryFirstFollowChecksImpl implements GrammarSlotFactory {
	
	private Map<Nonterminal, Integer> headGrammarSlotIds = new HashMap<>();

	private int bodyGrammarSlotId;
	
	private NonterminalNodeCreator nonterminalNodeCreator;
	private RightChildNodeCreator rightNodeCreator;
	private IntermediateNodeCreator intermediateNodeCreator;
	private NonterminalWithOneChildNodeCreator nonterminalWithOneChildNodeCreator;
	
	public GrammarSlotFactoryFirstFollowChecksImpl() {
		this.nonterminalNodeCreator = new NonterminalNodeCreator();
		this.rightNodeCreator = new RightChildNodeCreator();
		this.intermediateNodeCreator = new IntermediateNodeCreator();
		this.nonterminalWithOneChildNodeCreator = new NonterminalWithOneChildNodeCreator();
	}
	
	@Override
	public HeadGrammarSlot createHeadGrammarSlot(Nonterminal nonterminal,
												 List<List<Symbol>> alternates,
												 Map<Nonterminal, Set<RegularExpression>> firstSets,
												 Map<Nonterminal, Set<RegularExpression>> followSets,
												 Map<Nonterminal, List<Set<RegularExpression>>> predictionSets) {
		
		Set<RegularExpression> firstSet = firstSets.get(nonterminal);
		Set<RegularExpression> followSet = followSets.get(nonterminal);
		
		Set<RegularExpression> set = new HashSet<>(firstSet);
		if(set.contains(Epsilon.getInstance())) {
			set.addAll(followSet);
		}
		set.remove(Epsilon.getInstance());
		
		Tuple<Integer, Integer> minMax = getMinMax(set);
		int minPredictionSet = minMax.getFirst();
		int maxPredictionSet = minMax.getSecond();
		
		boolean nullable = firstSet.contains(Epsilon.getInstance());
		List<Set<RegularExpression>> predictionSet = predictionSets.get(nonterminal);
		
		Tuple<Integer, Integer> followSetsMinMax = getMinMax(followSet);
		int minFollowSet = followSetsMinMax.getFirst();
		int maxFollowSet = followSetsMinMax.getSecond();
		
		PredictionTest predictionTest;
		
		FollowTest followSetTest;
		
		if(maxPredictionSet - minPredictionSet < 10000) {
			predictionTest = new ArrayPredictionTest(predictionSet, minPredictionSet, maxPredictionSet);
		} else {
			predictionTest = new TreeMapPredictionTest(predictionSet, alternates.size());
		}

		if(maxFollowSet - minFollowSet < 10000) {
			followSetTest = new ArrayFollowTest(followSet, minFollowSet, maxFollowSet);
		} else {
			followSetTest = new TreeMapFollowTest(followSet);
		}
		
		Integer id = headGrammarSlotIds.get(nonterminal);
		if (id == null) {
			id = bodyGrammarSlotId++;
			headGrammarSlotIds.put(nonterminal, id);
		}
		
		return new HeadGrammarSlot(id, 
								   nonterminal, 
								   alternates.size(), 
								   nullable,
								   predictionTest, 
								   followSetTest);
	}
	
	private Tuple<Integer, Integer> getMinMax(Set<RegularExpression> set) {
		Set<Range> ranges = new HashSet<>();
		for(RegularExpression regex : set) {
			for(Range range : regex.getFirstSet()) {
				ranges.add(range);
			}
		}
		
		int min = Integer.MAX_VALUE;
		int max = 0;
		
		for(Range range : ranges) {
			if(range.getStart() < min) {
				min = range.getStart();
			}
			if(range.getEnd() > max) {
				max = range.getEnd();
			}
		}
		
		return Tuple.of(min, max);
	}	

	@Override
	public NonterminalGrammarSlot createNonterminalGrammarSlot(Rule rule, 
															   int symbolIndex,
															   BodyGrammarSlot previous, 
															   HeadGrammarSlot nonterminal,
															   ConditionTest preConditions,
															   ConditionTest popConditions) {
		
		if(symbolIndex == 1) {
			return new NonterminalGrammarSlot(bodyGrammarSlotId++, getSlotName(rule, symbolIndex), previous, nonterminal, preConditions, popConditions, rightNodeCreator);
		}
		
		return new NonterminalGrammarSlot(bodyGrammarSlotId++, getSlotName(rule, symbolIndex), previous, nonterminal, preConditions, popConditions, intermediateNodeCreator);
	}

	@Override
	public TokenGrammarSlot createTokenGrammarSlot(Rule rule,
												   int symbolIndex, 
												   BodyGrammarSlot previous, 
												   int tokenID, 
												   ConditionTest preConditions,
												   ConditionTest postConditions,
												   ConditionTest popConditions) {
		
		if(preConditions == null) throw new IllegalArgumentException("PreConditions cannot be null.");
		if(postConditions == null) throw new IllegalArgumentException("PostConditions cannot be null.");
		
		RegularExpression regularExpression = (RegularExpression) rule.getBody().get(symbolIndex);
		
		// A ::= .x
		if (symbolIndex == 0 && rule.size() == 1) {
			return new LastTokenSlot(bodyGrammarSlotId++, getSlotName(rule, symbolIndex), previous, regularExpression, tokenID, 
									 preConditions, postConditions, popConditions, nonterminalWithOneChildNodeCreator, DummyNodeCreator.getInstance());
		} 
		
		// A ::= x . y
		else if (symbolIndex == 1 && rule.size() == 2) {
			return new LastTokenSlot(bodyGrammarSlotId++, getSlotName(rule, symbolIndex), previous, regularExpression, tokenID, 
									 preConditions, postConditions, popConditions, nonterminalNodeCreator, rightNodeCreator);
		} 
		
		// A ::= alpha .x  where |alpha| > 1
		else if (symbolIndex == rule.size() - 1) {
			return new LastTokenSlot(bodyGrammarSlotId++, getSlotName(rule, symbolIndex), previous, regularExpression, tokenID, 
									 preConditions, postConditions, popConditions, nonterminalNodeCreator, intermediateNodeCreator);
		}
		
		// A ::= .x alpha  where |alpha| >= 1
		else if (symbolIndex == 0) {
			return new TokenGrammarSlot(bodyGrammarSlotId++, getSlotName(rule, symbolIndex), previous, regularExpression, tokenID, 
										preConditions, postConditions, popConditions, rightNodeCreator, DummyNodeCreator.getInstance());
		}
		
		// A ::= x . y alpha  where |alpha| >= 1
		else if (symbolIndex == 1) {
			return new TokenGrammarSlot(bodyGrammarSlotId++, getSlotName(rule, symbolIndex), previous, regularExpression, tokenID, 
										preConditions, postConditions, popConditions, intermediateNodeCreator, rightNodeCreator);
		}
		
		// A ::= beta .x alpha where |beta| >=1 and |alpha| >= 1
		else {
			return new TokenGrammarSlot(bodyGrammarSlotId++, getSlotName(rule, symbolIndex), previous, regularExpression, tokenID, 
										preConditions, postConditions, popConditions, intermediateNodeCreator, intermediateNodeCreator);
		}
	}

	@Override
	public LastGrammarSlot createLastGrammarSlot(Rule rule, 
												 int symbolIndex, 
												 BodyGrammarSlot previous, 
												 HeadGrammarSlot head, 
												 ConditionTest popConditions) {
		
		if(popConditions == null) throw new IllegalArgumentException("PostConditions cannot be null.");

		if(symbolIndex == 1) {
			return new LastGrammarSlot(bodyGrammarSlotId++, getSlotName(rule, symbolIndex), previous, head, popConditions, nonterminalWithOneChildNodeCreator);
		}
		
		return new LastGrammarSlot(bodyGrammarSlotId++, getSlotName(rule, symbolIndex), previous, head, popConditions, nonterminalNodeCreator);
	}
	
	@Override
	public EpsilonGrammarSlot createEpsilonGrammarSlot(HeadGrammarSlot head) {
		return new EpsilonGrammarSlot(bodyGrammarSlotId++, head + " ::= .", head);
	}
	
}
