package org.jgll.grammar.slot;

import java.util.Set;

import org.jgll.grammar.condition.Condition;
import org.jgll.parser.GLLParser;
import org.jgll.parser.gss.GSSNode;
import org.jgll.sppf.NonPackedNode;
import org.jgll.sppf.TerminalNode;


/**
 * 
 * Corresponds to grammar slots of the form X ::= alpha . b where |alpha| > 0
 * 
 * @author Ali Afroozeh
 *
 */
public class BeforeLastTerminalTransition extends AbstractTerminalTransition {

	public BeforeLastTerminalTransition(TerminalGrammarSlot slot, GrammarSlot origin, GrammarSlot dest,
			Set<Condition> preConditions, Set<Condition> postConditions) {
		super(slot, origin, dest, preConditions, postConditions);
	}

	@Override
	protected void createNode(int length, TerminalNode cr, GLLParser parser, GSSNode u, int i, NonPackedNode node) {
		dest.execute(parser, u, i + length, parser.getNonterminalNode((EndGrammarSlot) dest, node, cr));
	}
	
}