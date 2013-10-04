package org.jgll.grammar.slot;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;

import org.jgll.grammar.symbol.RegularExpression;
import org.jgll.grammar.symbol.Symbol;
import org.jgll.parser.GLLParserInternals;
import org.jgll.recognizer.GLLRecognizer;
import org.jgll.sppf.DummyNode;
import org.jgll.sppf.RegularListNode;
import org.jgll.sppf.SPPFNode;
import org.jgll.util.Input;

import dk.brics.automaton.RunAutomaton;


public class RegularExpressionGrammarSlot extends BodyGrammarSlot {

	private static final long serialVersionUID = 1L;
	private RegularExpression regexp;

	public RegularExpressionGrammarSlot(int position, RegularExpression regexp, BodyGrammarSlot previous, HeadGrammarSlot head) {
		super(position, previous, head);
		this.regexp = regexp;
	}
	
	public RegularExpressionGrammarSlot copy(BodyGrammarSlot previous, HeadGrammarSlot head) {
		RegularExpressionGrammarSlot slot = new RegularExpressionGrammarSlot(this.position, regexp, previous, head);
		slot.preConditions = preConditions;
		slot.popActions = popActions;
		return slot;
	}
		
	@Override
	public GrammarSlot parse(GLLParserInternals parser, Input input) {
		
		int ci = parser.getCurrentInputIndex();
		
		int regularListLength = parser.getRegularListLength();
		
		RunAutomaton automaton = regexp.getAutomaton();

		int state = 0;
		
		int l = 0;
		for(int i = 0; i < regularListLength; i++) {
			int charAtCi = input.charAt(ci + i);
			
			state = automaton.step(state, (char) charAtCi);
			l++;
			if(state == -1) {
				break;
			}
		}
		
		// The regular node that is going to be created as the result of this
		// slot action.
		RegularListNode regularNode = parser.getRegularNode(this, ci, ci + l);

		SPPFNode currentSPPFNode = parser.getCurrentSPPFNode();
		
		// If the current SPPF node is a partially matched list node, merge the nodes
		if(currentSPPFNode instanceof RegularListNode && ((RegularListNode) currentSPPFNode).isPartial()) {
			regularNode = parser.getRegularNode(this, currentSPPFNode.getLeftExtent(), ci + l);
		}

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			automaton.store(output);
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		
		// If the whole list is matched
		if(l <= regularListLength && automaton.step(state, (char) input.charAt(ci + l)) != -1) {
			parser.setCurrentSPPFNode(DummyNode.getInstance());
			parser.getNonterminalNode((LastGrammarSlot) next, regularNode);
			parser.pop();
			return null;
		} 
		else {
			try {
				automaton =  RunAutomaton.load(new ByteArrayInputStream(output.toByteArray()));
			}
			catch (ClassCastException | ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
			regularNode.setPartial(true);
			parser.addDescriptor(this, parser.getCurrentGSSNode(), ci + l, regularNode);
		}
		
		return null;
	}


	@Override
	public boolean testFirstSet(int index, Input input) {
		return true;
	}

	@Override
	public boolean testFollowSet(int index, Input input) {
		return true;
	}

	@Override
	public void codeIfTestSetCheck(Writer writer) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Symbol getSymbol() {
		return regexp;
	}

	@Override
	public boolean isNullable() {
		return true;
	}

	@Override
	public boolean isNameEqual(BodyGrammarSlot slot) {
		return false;
	}

	@Override
	public void codeParser(Writer writer) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public GrammarSlot recognize(GLLRecognizer recognizer, Input input) {
		// TODO Auto-generated method stub
		return null;
	}

}
