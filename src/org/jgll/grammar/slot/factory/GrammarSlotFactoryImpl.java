package org.jgll.grammar.slot.factory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgll.grammar.slot.BodyGrammarSlot;
import org.jgll.grammar.slot.EpsilonGrammarSlot;
import org.jgll.grammar.slot.HeadGrammarSlot;
import org.jgll.grammar.slot.HeadGrammarSlotFirstFollow;
import org.jgll.grammar.slot.LastGrammarSlot;
import org.jgll.grammar.slot.NonterminalGrammarSlot;
import org.jgll.grammar.slot.TokenGrammarSlot;
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
import org.jgll.grammar.slot.test.TrueFollowSet;
import org.jgll.grammar.slot.test.TruePredictionSet;
import org.jgll.grammar.symbol.Epsilon;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Range;
import org.jgll.grammar.symbol.Symbol;
import org.jgll.regex.RegularExpression;
import org.jgll.util.Tuple;

public class GrammarSlotFactoryImpl implements GrammarSlotFactory {
	
	private int headGrammarSlotId;
	private int bodyGrammarSlotId;
	private boolean firstFollowCheck;
	
	private NonterminalNodeCreator nonterminalNodeCreator;
	private RightChildNodeCreator rightNodeCreator;
	private IntermediateNodeCreator intermediateNodeCreator;
	private NonterminalWithOneChildNodeCreator nonterminalWithOneChildNodeCreator;
	
	public GrammarSlotFactoryImpl() {
		this(true);
	}
	
	public GrammarSlotFactoryImpl(boolean firstFollowCheck) {
		this.firstFollowCheck = firstFollowCheck;
		this.nonterminalNodeCreator = new NonterminalNodeCreator();
		this.rightNodeCreator = new RightChildNodeCreator();
		this.intermediateNodeCreator = new IntermediateNodeCreator();
		this.nonterminalWithOneChildNodeCreator = new NonterminalWithOneChildNodeCreator();
	}

	
	@Override
	public HeadGrammarSlot createHeadGrammarSlot(Nonterminal nonterminal,
												 int nonterminalId,
												 List<List<Symbol>> alternates,
												 Map<Nonterminal, Set<RegularExpression>> firstSets,
												 Map<Nonterminal, Set<RegularExpression>> followSets,
												 Map<Nonterminal, List<Set<RegularExpression>>> predictionSets) {
		
		if(!firstFollowCheck) {
			return new HeadGrammarSlotFirstFollow(headGrammarSlotId++, 
					   nonterminal, 
					   nonterminalId, 
					   alternates, 
					   new TruePredictionSet(alternates.size()), 
					   new TrueFollowSet(), 
					   false);
		}
		
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
			predictionTest = new ArrayPredictionTest(predictionSet, alternates.size(), minPredictionSet, maxPredictionSet);
		} else {
			predictionTest = new TreeMapPredictionTest(predictionSet, alternates.size());
		}
		
		if(maxFollowSet - minFollowSet < 10000) {
			followSetTest = new ArrayFollowTest(followSet, minFollowSet, maxFollowSet);
		} else {
			followSetTest = new TreeMapFollowTest(followSet);
		}
		
		return new HeadGrammarSlotFirstFollow(headGrammarSlotId++, 
											   nonterminal, 
											   nonterminalId, 
											   alternates, 
											   predictionTest, 
											   followSetTest, 
											   nullable);
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
	public NonterminalGrammarSlot createNonterminalGrammarSlot(List<Symbol> body, 
															   int symbolIndex, 
															   int nodeId,
															   String label,
															   BodyGrammarSlot previous, 
															   HeadGrammarSlot nonterminal,
															   ConditionTest preConditions,
															   ConditionTest postConditions) {
		
		if(symbolIndex == 1) {
			return new NonterminalGrammarSlot(bodyGrammarSlotId++, nodeId, label, previous, nonterminal, preConditions, postConditions, rightNodeCreator);
		}
		
		return new NonterminalGrammarSlot(bodyGrammarSlotId++, nodeId, label, previous, nonterminal, preConditions, postConditions, intermediateNodeCreator);
	}

	@Override
	public TokenGrammarSlot createTokenGrammarSlot(List<Symbol> body, int symbolIndex, int nodeId, 
												   String label, BodyGrammarSlot previous,
												   int tokenID, ConditionTest preConditions, ConditionTest postConditions) {
		
		if(preConditions == null) throw new IllegalArgumentException("PreConditions cannot be null.");
		if(postConditions == null) throw new IllegalArgumentException("PostConditions cannot be null.");
		
		RegularExpression regularExpression = (RegularExpression) body.get(symbolIndex);
		
		if (symbolIndex == 0 && body.size() == 1) {
			return new TokenGrammarSlot(bodyGrammarSlotId++, nodeId, label, previous, regularExpression, tokenID, preConditions, postConditions, nonterminalWithOneChildNodeCreator, null);
		} else {
			if (symbolIndex == 1 && body.size() == 2) {
				return new LastTokenSlot(bodyGrammarSlotId++, nodeId, label, previous, regularExpression, tokenID, preConditions, postConditions, nonterminalNodeCreator, rightNodeCreator);
			} else {
				if (symbolIndex == body.size() - 1) {
					return new LastTokenSlot(bodyGrammarSlotId++, nodeId, label, previous, regularExpression, tokenID, preConditions, postConditions, nonterminalNodeCreator, intermediateNodeCreator);
				}
				else if (symbolIndex == 0) {
					return new TokenGrammarSlot(bodyGrammarSlotId++, nodeId, label, previous, regularExpression, tokenID, preConditions, postConditions, rightNodeCreator, null);
				}
				else if (symbolIndex == 1) {
					return new TokenGrammarSlot(bodyGrammarSlotId++, nodeId, label, previous, regularExpression, tokenID, preConditions, postConditions, intermediateNodeCreator, rightNodeCreator);
				}
				else {
					return new TokenGrammarSlot(bodyGrammarSlotId++, nodeId, label, previous, regularExpression, tokenID, preConditions, postConditions, nonterminalNodeCreator, intermediateNodeCreator);
				}
			}
		}
	}

	@Override
	public LastGrammarSlot createLastGrammarSlot(List<Symbol> body, int symbolIndex, String label, BodyGrammarSlot previous, HeadGrammarSlot head, ConditionTest postConditions) {
		
		if(postConditions == null) throw new IllegalArgumentException("PostConditions cannot be null.");

		if(symbolIndex == 1) {
			return new LastGrammarSlot(bodyGrammarSlotId++, label, previous, head, postConditions, nonterminalWithOneChildNodeCreator);
		}
		
		return new LastGrammarSlot(bodyGrammarSlotId++, label, previous, head, postConditions, nonterminalNodeCreator);
	}
	
	@Override
	public EpsilonGrammarSlot createEpsilonGrammarSlot(String label, HeadGrammarSlot head) {
		return new EpsilonGrammarSlot(bodyGrammarSlotId++, label, head);
	}
	
}
