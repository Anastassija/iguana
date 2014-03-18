package org.jgll.grammar.slot.factory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgll.grammar.slot.BodyGrammarSlot;
import org.jgll.grammar.slot.CharacterGrammarSlot;
import org.jgll.grammar.slot.EpsilonGrammarSlot;
import org.jgll.grammar.slot.HeadGrammarSlot;
import org.jgll.grammar.slot.HeadGrammarSlotFirstFollow;
import org.jgll.grammar.slot.LastGrammarSlot;
import org.jgll.grammar.slot.LastGrammarSlotFirstFollow;
import org.jgll.grammar.slot.NonterminalGrammarSlot;
import org.jgll.grammar.slot.NonterminalGrammarSlotFirstFollow;
import org.jgll.grammar.slot.RangeGrammarSlot;
import org.jgll.grammar.slot.TokenGrammarSlot;
import org.jgll.grammar.slot.firstfollow.ArrayFollowTest;
import org.jgll.grammar.slot.firstfollow.ArrayPredictionTest;
import org.jgll.grammar.slot.firstfollow.FollowTest;
import org.jgll.grammar.slot.firstfollow.TreeMapFollowTest;
import org.jgll.grammar.slot.firstfollow.TreeMapPredictionTest;
import org.jgll.grammar.slot.firstfollow.PredictionTest;
import org.jgll.grammar.symbol.Character;
import org.jgll.grammar.symbol.Epsilon;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Range;
import org.jgll.grammar.symbol.Symbol;
import org.jgll.regex.RegularExpression;
import org.jgll.util.Tuple;

public class FirstFollowSetGrammarSlotFactory implements GrammarSlotFactory {
	
	private int headGrammarSlotId;
	private int bodyGrammarSlotId;
	
	@Override
	public HeadGrammarSlot createHeadGrammarSlot(Nonterminal nonterminal,
												 int nonterminalId,
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
	public NonterminalGrammarSlot createNonterminalGrammarSlot(int nodeId,
															   String label,
															   BodyGrammarSlot previous, 
															   HeadGrammarSlot nonterminal, 
															   HeadGrammarSlot head) {
		return new NonterminalGrammarSlotFirstFollow(bodyGrammarSlotId++, nodeId, label, previous, nonterminal, head);
	}

	@Override
	public TokenGrammarSlot createTokenGrammarSlot(int nodeId, String label, BodyGrammarSlot previous, RegularExpression regularExpression, 
												   HeadGrammarSlot head, int tokenID) {
		if(regularExpression instanceof Character) {
			return new CharacterGrammarSlot(bodyGrammarSlotId++, nodeId, label, previous, (Character) regularExpression, head, tokenID);
		}
		else if (regularExpression instanceof Range) {
			Range r = (Range) regularExpression;
			if(r.getStart() == r.getEnd()) {
				return new CharacterGrammarSlot(bodyGrammarSlotId++, nodeId, label, previous, new Character(r.getStart()), head, tokenID);
			} else {
				return new RangeGrammarSlot(bodyGrammarSlotId++, nodeId, label, previous, r, head, tokenID);
			}
		}
		return new TokenGrammarSlot(bodyGrammarSlotId++, nodeId, label, previous, regularExpression, head, tokenID);
	}


	@Override
	public LastGrammarSlot createLastGrammarSlot(String label, BodyGrammarSlot previous, HeadGrammarSlot head) {
		return new LastGrammarSlotFirstFollow(bodyGrammarSlotId++, label, previous, head);
	}
	
	@Override
	public EpsilonGrammarSlot createEpsilonGrammarSlot(String label, HeadGrammarSlot head) {
		return new EpsilonGrammarSlot(bodyGrammarSlotId++, label, head);
	}
	
}
