package org.jgll.grammar.slot.specialized;

import org.jgll.grammar.slot.BodyGrammarSlot;
import org.jgll.grammar.slot.TokenGrammarSlot;
import org.jgll.grammar.slot.test.ConditionTest;
import org.jgll.parser.GLLParser;
import org.jgll.regex.RegularExpression;
import org.jgll.sppf.SPPFNode;

public class SecondTokenSlot extends TokenGrammarSlot {

	private static final long serialVersionUID = 1L;

	public SecondTokenSlot(int id, int nodeId, String label,
			BodyGrammarSlot previous, RegularExpression regularExpression,
			int tokenID, ConditionTest preConditions,
			ConditionTest postConditions) {
		super(id, nodeId, label, previous, regularExpression, tokenID, preConditions, postConditions);
	}
	
	@Override
	public SPPFNode createNodeFromPop(GLLParser parser, SPPFNode leftChild, SPPFNode rightChild) {
		return rightChild;	
	}
	
}
