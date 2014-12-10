package org.jgll.grammar.slot.factory;

import static org.jgll.grammar.slot.BodyGrammarSlot.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgll.grammar.slot.BodyGrammarSlot;
import org.jgll.grammar.slot.EpsilonGrammarSlot;
import org.jgll.grammar.slot.HeadGrammarSlot;
import org.jgll.grammar.slot.LastGrammarSlot;
import org.jgll.grammar.slot.NonterminalGrammarSlot;
import org.jgll.grammar.slot.TerminalGrammarSlot;
import org.jgll.grammar.slot.TokenGrammarSlot;
import org.jgll.grammar.slot.nodecreator.DummyNodeCreator;
import org.jgll.grammar.slot.nodecreator.IntermediateNodeCreator;
import org.jgll.grammar.slot.nodecreator.NonterminalNodeCreator;
import org.jgll.grammar.slot.nodecreator.NonterminalWithOneChildNodeCreator;
import org.jgll.grammar.slot.nodecreator.RightChildNodeCreator;
import org.jgll.grammar.slot.specialized.LastTokenSlot;
import org.jgll.grammar.slot.test.ConditionTest;
import org.jgll.grammar.slot.test.FollowTest;
import org.jgll.grammar.slot.test.PredictionTest;
import org.jgll.grammar.slot.test.TrueFollowSet;
import org.jgll.grammar.slot.test.TruePredictionSet;
import org.jgll.grammar.symbol.Epsilon;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Rule;
import org.jgll.grammar.symbol.Symbol;
import org.jgll.regex.RegularExpression;

public class GrammarSlotFactoryImpl implements GrammarSlotFactory {
	
	private NonterminalNodeCreator nonterminalNodeCreator;
	private RightChildNodeCreator rightNodeCreator;
	private IntermediateNodeCreator intermediateNodeCreator;
	private NonterminalWithOneChildNodeCreator nonterminalWithOneChildNodeCreator;
	
	public GrammarSlotFactoryImpl() {
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
		

		
		PredictionTest predictionTest = new TruePredictionSet(alternates.size());
		boolean nullable = firstSets.get(nonterminal).contains(Epsilon.getInstance());
		FollowTest followSetTest = new TrueFollowSet();
		
		return new HeadGrammarSlot(nonterminal, 
								   alternates.size(), 
								   nullable,
								   predictionTest, 
								   followSetTest);
	}

	@Override
	public NonterminalGrammarSlot createNonterminalGrammarSlot(Rule rule, 
															   int symbolIndex,
															   BodyGrammarSlot previous, 
															   HeadGrammarSlot nonterminal,
															   ConditionTest preConditions,
															   ConditionTest popConditions) {
		
		if(symbolIndex == 1) {
			return new NonterminalGrammarSlot(getSlotName(rule, symbolIndex), previous, nonterminal, preConditions, popConditions, rightNodeCreator);
		}
		
		return new NonterminalGrammarSlot(getSlotName(rule, symbolIndex), previous, nonterminal, preConditions, popConditions, intermediateNodeCreator);
	}

	@Override
	public TokenGrammarSlot createTokenGrammarSlot(Rule rule,
												   int symbolIndex, 
												   BodyGrammarSlot previous, 
												   TerminalGrammarSlot slot,
												   ConditionTest preConditions,
												   ConditionTest postConditions,
												   ConditionTest popConditions) {
		
		if(preConditions == null) throw new IllegalArgumentException("PreConditions cannot be null.");
		if(postConditions == null) throw new IllegalArgumentException("PostConditions cannot be null.");
		
		// A ::= .x
		if (symbolIndex == 0 && rule.size() == 1) {
			return new LastTokenSlot(getSlotName(rule, symbolIndex), previous, slot, 
									 preConditions, postConditions, popConditions, nonterminalWithOneChildNodeCreator, DummyNodeCreator.getInstance());
		} 
		
		// A ::= x . y
		else if (symbolIndex == 1 && rule.size() == 2) {
			return new LastTokenSlot(getSlotName(rule, symbolIndex), previous, slot, 
									 preConditions, postConditions, popConditions, nonterminalNodeCreator, rightNodeCreator);
		} 
		
		// A ::= alpha .x  where |alpha| > 1
		else if (symbolIndex == rule.size() - 1) {
			return new LastTokenSlot(getSlotName(rule, symbolIndex), previous, slot, 
									 preConditions, postConditions, popConditions, nonterminalNodeCreator, intermediateNodeCreator);
		}
		
		// A ::= .x alpha  where |alpha| >= 1
		else if (symbolIndex == 0) {
			return new TokenGrammarSlot(getSlotName(rule, symbolIndex), previous, slot, 
										preConditions, postConditions, popConditions, rightNodeCreator, DummyNodeCreator.getInstance());
		}
		
		// A ::= x . y alpha  where |alpha| >= 1
		else if (symbolIndex == 1) {
			return new TokenGrammarSlot(getSlotName(rule, symbolIndex), previous, slot, 
										preConditions, postConditions, popConditions, intermediateNodeCreator, rightNodeCreator);
		}
		
		// A ::= beta .x alpha where |beta| >=1 and |alpha| >= 1
		else {
			return new TokenGrammarSlot(getSlotName(rule, symbolIndex), previous, slot, 
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
			return new LastGrammarSlot(getSlotName(rule, symbolIndex), previous, head, popConditions, nonterminalWithOneChildNodeCreator);
		}
		
		return new LastGrammarSlot(getSlotName(rule, symbolIndex), previous, head, popConditions, nonterminalNodeCreator);
	}
	
	@Override
	public EpsilonGrammarSlot createEpsilonGrammarSlot(HeadGrammarSlot head) {
		return new EpsilonGrammarSlot(head + " ::= .", head);
	}
}
