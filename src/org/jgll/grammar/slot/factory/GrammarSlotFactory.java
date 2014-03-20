package org.jgll.grammar.slot.factory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgll.grammar.slot.BodyGrammarSlot;
import org.jgll.grammar.slot.EpsilonGrammarSlot;
import org.jgll.grammar.slot.HeadGrammarSlot;
import org.jgll.grammar.slot.LastGrammarSlot;
import org.jgll.grammar.slot.NonterminalGrammarSlot;
import org.jgll.grammar.slot.TokenGrammarSlot;
import org.jgll.grammar.slot.test.ConditionsTest;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Rule;
import org.jgll.grammar.symbol.Symbol;
import org.jgll.regex.RegularExpression;

public interface GrammarSlotFactory {

	public HeadGrammarSlot createHeadGrammarSlot(Nonterminal nonterminal,
												 int nodeId,
												 List<List<Symbol>> alternates,
												 Map<Nonterminal, Set<RegularExpression>> firstSets,
												 Map<Nonterminal, Set<RegularExpression>> followSets,
												 Map<Nonterminal, List<Set<RegularExpression>>> predictionSets);
	
	public NonterminalGrammarSlot createNonterminalGrammarSlot(int nodeId,
															   String label,
															   BodyGrammarSlot previous, 
															   HeadGrammarSlot nonterminal);

	public LastGrammarSlot createLastGrammarSlot(String label, BodyGrammarSlot previous, HeadGrammarSlot head);
	
	
	public EpsilonGrammarSlot createEpsilonGrammarSlot(String label, HeadGrammarSlot head);
	
	
	public TokenGrammarSlot createTokenGrammarSlot(Rule rule,
												   int symbolIndex, 
												   int nodeId,
												   String label,
												   BodyGrammarSlot previous, 
												   int tokenID, 
												   ConditionsTest preConditions,
												   ConditionsTest postConditions);
}
