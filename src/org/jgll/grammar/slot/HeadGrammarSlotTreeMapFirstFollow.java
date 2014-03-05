package org.jgll.grammar.slot;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.jgll.grammar.symbol.Alternate;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Range;
import org.jgll.lexer.GLLLexer;
import org.jgll.parser.GLLParser;
import org.jgll.regex.RegularExpression;

public class HeadGrammarSlotTreeMapFirstFollow extends HeadGrammarSlot {

	private static final long serialVersionUID = 1L;
	
	private NavigableMap<Integer, Set<BodyGrammarSlot>> predictionMap;

	public HeadGrammarSlotTreeMapFirstFollow(Nonterminal nonterminal) {
		super(nonterminal);
		predictionMap = new TreeMap<>();
	}
	
	@Override
	public GrammarSlot parse(GLLParser parser, GLLLexer lexer) {
		int ci = parser.getCurrentInputIndex();
		
		Entry<Integer, Set<BodyGrammarSlot>> e = predictionMap.floorEntry(lexer.getInput().charAt(ci));
		if(e != null) {
			Set<BodyGrammarSlot> set = e.getValue();

			if(set != null) {
				for(BodyGrammarSlot slot : set) {
					parser.addDescriptor(slot);
				}
			}			
		}
		
		return null;
	}
	
	@Override
	public boolean test(int v) {
		Entry<Integer, Set<BodyGrammarSlot>> e = predictionMap.floorEntry(v);
		return e != null && e.getValue() != null;
	}
	
	@Override
	public void setPredictionSet() {
		
		predictionMap = new TreeMap<>();

		for(Alternate alt : alternates) {
			for(RegularExpression regex : alt.getPredictionSet()) {
				for(Range r : regex.getFirstSet()) {
					
					Set<BodyGrammarSlot> s1 = predictionMap.get(r.getStart());
					if(s1 == null) {
						s1 = new HashSet<>();
						predictionMap.put(r.getStart(), s1);
					}
					s1.add(alt.getFirstSlot());
					
					if(predictionMap.floorEntry(r.getStart() - 1) != null) {
						s1.addAll(predictionMap.floorEntry(r.getStart() - 1).getValue());
					}
					
					for(int i = r.getStart() + 1; i <= r.getEnd(); i++) {
						if(predictionMap.get(i) != null) {
							predictionMap.get(i).add(alt.getFirstSlot());
						}
					}
					
					if(predictionMap.ceilingEntry(r.getEnd() + 1) != null) {
						Set<BodyGrammarSlot> s2 = predictionMap.get(r.getEnd() + 1);
						if(s2 == null) {
							predictionMap.put(r.getEnd() + 1, null);						
						}						
					}
				}
			}
 		}
	}

}
