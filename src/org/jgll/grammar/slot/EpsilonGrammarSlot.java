package org.jgll.grammar.slot;

import java.util.Set;

import org.jgll.grammar.GrammarRegistry;
import org.jgll.grammar.condition.Condition;
import org.jgll.grammar.symbol.Position;
import org.jgll.parser.GLLParser;
import org.jgll.parser.gss.GSSNode;
import org.jgll.parser.gss.lookup.NodeLookup;
import org.jgll.sppf.NonPackedNode;
import org.jgll.sppf.TerminalNode;

public class EpsilonGrammarSlot extends EndGrammarSlot {

	private TerminalGrammarSlot epsilonSlot;

	public EpsilonGrammarSlot(int id, Position position, NonterminalGrammarSlot nonterminal, TerminalGrammarSlot epsilonSlot, NodeLookup nodeLookup, Set<Condition> conditions) {
		super(id, position, nonterminal, nodeLookup, conditions);
		this.epsilonSlot = epsilonSlot;
	}
	
	@Override
	public void execute(GLLParser parser, GSSNode u, int i, NonPackedNode node) {
		if (nonterminal.test(i)) {
			TerminalNode epsilonNode = parser.getEpsilonNode(epsilonSlot, i);
			parser.pop(u, i, parser.getNonterminalNode(this, epsilonNode));
		}
	}
	
	@Override
	public String getConstructorCode(GrammarRegistry registry) {
		return "new EpsilonGrammarSlot(slot" + registry.getId(nonterminal) + ")";
	}

}
