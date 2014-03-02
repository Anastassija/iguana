package org.jgll.grammar.slot;

import java.util.HashSet;
import java.util.Set;

import org.jgll.grammar.symbol.Alternate;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Range;
import org.jgll.lexer.GLLLexer;
import org.jgll.parser.GLLParser;
import org.jgll.regex.RegularExpression;

public class HeadGrammarSlotArrayFirstFollow extends HeadGrammarSlot {

	private static final long serialVersionUID = 1L;
	
	private Set<BodyGrammarSlot>[] predictionMap;
	
	private int min;
	
	private int max;

	public HeadGrammarSlotArrayFirstFollow(Nonterminal nonterminal, int min, int max) {
		super(nonterminal);
		this.min = min;
		this.max = max;
	}
	
	@Override
	public GrammarSlot parse(GLLParser parser, GLLLexer lexer) {
		int ci = parser.getCurrentInputIndex();
		
		Set<BodyGrammarSlot> set = predictionMap[lexer.getInput().charAt(ci)];
		if(set != null) {
			for(BodyGrammarSlot slot : set) {
				parser.addDescriptor(slot);
			}
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setPredictionSet() {
		
		predictionMap = new Set[max - min + 1];

		for(Alternate alt : alternates) {
			for(RegularExpression regex : alt.getPredictionSet()) {
				for(Range r : regex.getFirstSet()) {
					Set<BodyGrammarSlot> s1 = predictionMap[r.getStart()];
					if(s1 == null) {
						s1 = new HashSet<>();
						predictionMap[r.getStart()] =  s1;
					}
					s1.add(alt.getFirstSlot());
					
					Set<BodyGrammarSlot> s2 = predictionMap[r.getEnd() + 1];
					if(s2 == null) {
						predictionMap[r.getEnd() + 1] =  null;						
					}
				}
			}
 		}
	}

}
