package org.jgll.grammar.slot;

import java.io.IOException;
import java.io.Writer;

import org.jgll.grammar.slot.test.ConditionTest;
import org.jgll.grammar.symbol.Symbol;
import org.jgll.lexer.GLLLexer;
import org.jgll.parser.GLLParser;
import org.jgll.parser.lookup.SPPFLookup;
import org.jgll.sppf.IntermediateNode;
import org.jgll.sppf.SPPFNode;


/**
 * A grammar slot immediately before a nonterminal.
 *
 * @author Ali Afroozeh
 *
 */
public class NonterminalGrammarSlot extends BodyGrammarSlot {
	
	private static final long serialVersionUID = 1L;

	protected HeadGrammarSlot nonterminal;
	
	protected final int nodeId;
	
	public NonterminalGrammarSlot(int id, int nodeId, String label, BodyGrammarSlot previous, HeadGrammarSlot nonterminal, ConditionTest preConditions, ConditionTest postConditions) {
		super(id, label, previous, preConditions, postConditions);
		if(nonterminal == null) {
			throw new IllegalArgumentException("Nonterminal cannot be null.");
		}
		this.nonterminal = nonterminal;
		this.nodeId = nodeId;
	}
	
	public HeadGrammarSlot getNonterminal() {
		return nonterminal;
	}
	
	public void setNonterminal(HeadGrammarSlot nonterminal) {
		this.nonterminal = nonterminal;
	}
	
	@Override
	public GrammarSlot parse(GLLParser parser, GLLLexer lexer) {
		
		int ci = parser.getCurrentInputIndex();
		
		if(!nonterminal.test(lexer.getInput().charAt(ci))) {
			parser.recordParseError(this);
			return null;
		}
		
		if(preConditions.execute(parser, lexer, ci)) {
			return null;
		}
		
		if(!parser.hasGSSNode(next, nonterminal)) {
			parser.createGSSNode(next, nonterminal);			
			return nonterminal;
		}
		
		return null;
	}
	
	@Override
	public void codeParser(Writer writer) throws IOException {
		
		if(previous == null) {
			codeIfTestSetCheck(writer);
			writer.append("   cu = create(grammar.getGrammarSlot(" + next.id + "), cu, ci, cn);\n");
			writer.append("   label = " + nonterminal.getId() + ";\n");
			codeElseTestSetCheck(writer);
			writer.append("}\n");
			
			writer.append("// " + next + "\n");
			writer.append("private void parse_" + next.id + "() {\n");
			
			BodyGrammarSlot slot = next;
			while(slot != null) {
				slot.codeParser(writer);
				slot = slot.next;
			}
		} 
		
		else { 
		
			// TODO: add the testSet check
			// code(A ::= α · Xl β) = 
			//						if(test(I[cI ], A, Xβ) {
			// 							cU :=create(RXl,cU,cI,cN); 
			//							gotoLX 
			//						}
			// 						else goto L0
			// RXl:
			codeIfTestSetCheck(writer);
			writer.append("   cu = create(grammar.getGrammarSlot(" + next.id + "), cu, ci, cn);\n");
			writer.append("   label = " + nonterminal.getId() + ";\n");
			codeElseTestSetCheck(writer);
			writer.append("}\n");
			
			writer.append("// " + next + "\n");
			writer.append("private void parse_" + next.id + "(){\n");
		}
	}
	
	@Override
	public void codeIfTestSetCheck(Writer writer) throws IOException {
		writer.append("if (");
//		int i = 0;
//		for(Terminal terminal : testSet) {
//			writer.append(terminal.getMatchCode());
//			if(++i < testSet.size()) {
//				writer.append(" || ");
//			}
//		}
		writer.append(") {\n");
	}
			
	@Override
	public boolean isNullable() {
		return nonterminal.isNullable();
	}

	@Override
	public Symbol getSymbol() {
		return nonterminal.getNonterminal();
	}

	@Override
	public int getNodeId() {
		return nodeId;
	}

	@Override
	public SPPFNode createNodeFromPop(GLLParser parser, SPPFNode leftChild, SPPFNode rightChild) {
		int leftExtent = leftChild.getLeftExtent();
		int rightExtent = rightChild.getRightExtent();
		
		SPPFLookup sppfLookup = parser.getSPPFLookup();
		IntermediateNode newNode = sppfLookup.getIntermediateNode(this, leftExtent, rightExtent);
		
		sppfLookup.addPackedNode(newNode, this, rightChild.getLeftExtent(), leftChild, rightChild);
		
		return newNode;
	}
	
}