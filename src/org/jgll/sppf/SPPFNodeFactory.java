package org.jgll.sppf;

import java.util.List;

import org.jgll.grammar.GrammarGraph;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Symbol;
import org.jgll.regex.RegularExpression;
import org.jgll.util.CollectionsUtil;

public class SPPFNodeFactory {

	private GrammarGraph grammarGraph;

	public SPPFNodeFactory(GrammarGraph grammarGraph) {
		this.grammarGraph = grammarGraph;
	}
	
	public NonterminalSymbolNode createNonterminalNode(Nonterminal nonterminal, int leftExtent, int rightExtent) {
		return new NonterminalSymbolNode(grammarGraph.getNonterminalId(nonterminal), 
										 grammarGraph.getCountAlternates(nonterminal), 
										 leftExtent, 
										 rightExtent);
	}

	public IntermediateNode createIntermediateNode(List<Symbol> symbols, int leftExtent, int rightExtent) {
		int id = grammarGraph.getIntermediateNodeId(CollectionsUtil.listToString(symbols, " "));
		return new IntermediateNode(id, leftExtent, rightExtent);
	}
	
	public TokenSymbolNode createTokenNode(RegularExpression regex, int leftExtent, int rightExtent) {
		return new TokenSymbolNode(grammarGraph.getRegularExpressionId(regex), leftExtent, rightExtent - leftExtent);
	}
	
}
