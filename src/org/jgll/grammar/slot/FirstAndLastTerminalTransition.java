package org.jgll.grammar.slot;

import java.util.Set;

import org.jgll.datadependent.env.Environment;
import org.jgll.grammar.condition.Condition;
import org.jgll.parser.GLLParser;
import org.jgll.parser.gss.GSSNode;
import org.jgll.sppf.DummyNode;
import org.jgll.sppf.NonPackedNode;
import org.jgll.sppf.TerminalNode;


/**
 * 
 * Corresponds to grammar slots of the form A ::= . b
 * 
 * @author Ali Afroozeh
 *
 */
public class FirstAndLastTerminalTransition extends AbstractTerminalTransition {

	public FirstAndLastTerminalTransition(TerminalGrammarSlot slot, BodyGrammarSlot origin, BodyGrammarSlot dest, Set<Condition> preConditions, Set<Condition> postConditions) {
		super(slot, origin, dest, preConditions, postConditions);
	}

	@Override
	protected void createNode(int length, TerminalNode cr, GLLParser parser, GSSNode u, int i, NonPackedNode node) {
		if (dest.isEnd())
			dest.execute(parser, u, i + length, parser.getNonterminalNode((LastSymbolGrammarSlot) dest, cr));
		else {
			parser.setCurrentEndGrammarSlot(DummySlot.getInstance());
			dest.execute(parser, u, i + length, DummyNode.getInstance(cr.getLeftExtent(), i + length));
			
			if (parser.getCurrentEndGrammarSlot().isEnd())
				parser.getCurrentEndGrammarSlot().execute(parser, u, i + length, parser.getNonterminalNode((LastSymbolGrammarSlot) dest, cr));
		}
	}
	
	/**
	 * 
	 * Data-dependent GLL parsing
	 * 
	 */
	@Override
	protected void createNode(int length, TerminalNode cr, GLLParser parser, GSSNode u, int i, NonPackedNode node, Environment env) {
		if (dest.isEnd())
			dest.execute(parser, u, i + length, parser.getNonterminalNode((LastSymbolGrammarSlot) dest, cr), env);
		else {
			parser.setCurrentEndGrammarSlot(DummySlot.getInstance());
			dest.execute(parser, u, i + length, DummyNode.getInstance(cr.getLeftExtent(), i + length), env);
			
			if (parser.getCurrentEndGrammarSlot().isEnd())
				parser.getCurrentEndGrammarSlot().execute(parser, u, i + length, parser.getNonterminalNode((LastSymbolGrammarSlot) dest, cr), parser.getEnvironment());
		}
	}
}
