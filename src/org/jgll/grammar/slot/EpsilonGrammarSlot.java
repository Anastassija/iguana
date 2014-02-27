package org.jgll.grammar.slot;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;

import org.jgll.grammar.symbol.Epsilon;
import org.jgll.grammar.symbol.Symbol;
import org.jgll.lexer.GLLLexer;
import org.jgll.parser.GLLParser;
import org.jgll.sppf.NonterminalSymbolNode;

/**
 * The grammar slot representing an empty body.
 * 
 * 
 * @author Ali Afroozeh
 *
 */
public class EpsilonGrammarSlot extends LastGrammarSlot {

	private static final long serialVersionUID = 1L;
	
	public EpsilonGrammarSlot(int position, HeadGrammarSlot head, Serializable object) {
		super(position, null, head, object);
	}
	
	@Override
	public GrammarSlot parse(GLLParser parser, GLLLexer input) {
		
		if(executePreConditions(parser, input)) {
			return null;
		}
		
		// A ::= ε
		// Do not create epsilon nodes
		int ci = parser.getCurrentInputIndex();
		NonterminalSymbolNode node = parser.getSPPFLookup().getNonterminalNode(this.getHead(), ci, ci);
		node.addFirstPackedNode(this, ci);
		parser.setCurrentSPPFNode(node);
		parser.pop();
		return null;
	}
	
	@Override
	public Symbol getSymbol() {
		return Epsilon.getInstance();
	}
	
	@Override
	public void codeParser(Writer writer) throws IOException {
		/**
		 * code(A ::= ε) =
		 * 				  cR ::= getNodeT(ε,cI);
		 * 				  cN ::= getNodeP(A ::= ·,cN,cR)
		 * 				  pop(cU , cI , cN );
		 * 				  goto L0;
		 */
		writer.append("   cr = getNodeT(-2, ci);\n");
		writer.append("   cn = getNodeP(grammar.getGrammarSlot(" + id + "), cn, cr);\n");
		writer.append("   pop(cu, ci, cn);\n");
		writer.append("   label = L0;\n}\n");
	}	
}
