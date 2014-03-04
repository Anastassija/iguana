package org.jgll.grammar.slot;

import org.jgll.grammar.symbol.Rule;
import org.jgll.lexer.GLLLexer;
import org.jgll.parser.GLLParser;
import org.jgll.sppf.SPPFNode;


/**
 * A grammar slot immediately before a nonterminal.
 *
 * @author Ali Afroozeh
 *
 */
public class LL1NonterminalGrammarSlot extends NonterminalGrammarSlot {
	
	private static final long serialVersionUID = 1L;

	public LL1NonterminalGrammarSlot(Rule rule, int position, String label, BodyGrammarSlot previous, LL1HeadGrammarSlot nonterminal, HeadGrammarSlot head) {
		super(rule, position, label, previous, nonterminal, head);
	}
	
	public LL1NonterminalGrammarSlot copy(BodyGrammarSlot previous, String label, HeadGrammarSlot nonterminal, HeadGrammarSlot head) {
		LL1NonterminalGrammarSlot slot = new LL1NonterminalGrammarSlot(rule, position, label, previous, (LL1HeadGrammarSlot) nonterminal, head);
		slot.preConditions = preConditions;
		slot.popActions = popActions;
		return slot;
	}

	@Override
	public GrammarSlot parse(GLLParser parser, GLLLexer lexer) {
		
		if(!nonterminal.test(lexer.getInput().charAt(parser.getCurrentInputIndex()))) {
			parser.recordParseError(this);
			return null;
		}
		
		if(executePreConditions(parser, lexer)) {
			return null;
		}

		nonterminal.parse(parser, lexer);
		
		SPPFNode node = null;
		
		if(node == null) {
			return null;
		}
		
		if(next instanceof LastGrammarSlot) {
			parser.getNonterminalNode((LastGrammarSlot) next, node);
			
			if(executePopActions(parser, lexer)) {
				return null;
			}
			parser.pop();
			
		} else {
			parser.getIntermediateNode(next, node);
			return next;
		}
		
		return null;
	}
	
}