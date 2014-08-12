package org.jgll.grammar.slot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgll.grammar.Grammar;
import org.jgll.grammar.precedence.OperatorPrecedence;
import org.jgll.grammar.symbol.Rule;
import org.jgll.grammar.symbol.Symbol;
import org.jgll.util.CollectionsUtil;

public class SharedPrefixIntermediateNodeIds implements IntermediateNodeIds {
	
	private Map<List<Symbol>, Integer> intermediateNodeIds;
	
	private Map<Integer, List<Symbol>> idToNameMap;
	
	private Grammar grammar;

	public SharedPrefixIntermediateNodeIds(Grammar grammar) {
		this.grammar = grammar;
		this.intermediateNodeIds = new HashMap<>();
		this.idToNameMap = new HashMap<>();
		calculateIds();
	}
	
	@Override
	public void calculateIds() {
		int intermediateId = 0;
		
		for (Rule rule : grammar.getRules()) {
			if (rule.getBody() != null) {
				for (int i = 2; i < rule.getBody().size(); i++) {
					List<Symbol> prefix = rule.getBody().subList(0, i);
					List<Symbol> plain = OperatorPrecedence.plain(prefix);
					if (!intermediateNodeIds.containsKey(plain)) {
						intermediateNodeIds.put(plain, intermediateId);
						idToNameMap.put(intermediateId, plain);
						intermediateId++;
					}
				}
			}
		}
	}

	@Override
	public int getSlotId(Rule rule, int index) {
		
		List<Symbol> alt = rule.getBody();

		if(alt.size() <= 2 || index <= 1) {
			return -1;
		}

		// Last grammar slot
		if(index == alt.size()) {
			return -1;
		}

		return intermediateNodeIds.get(OperatorPrecedence.plain(alt.subList(0, index)));
	}
	
	@Override
	public int getSlotId(Rule rule) {
		return intermediateNodeIds.get(rule.getBody());
	}

	@Override
	public String getSlotName(int id) {
		return CollectionsUtil.listToString(idToNameMap.get(id), " ");
	}
	
	@Override
	public List<Symbol> getSequence(int id) {
		return idToNameMap.get(id);
	}
	
	@Override
	public String toString() {
		return intermediateNodeIds.toString();
	}
}
