package org.jgll.util.visualization;

import static org.jgll.util.generator.GeneratorUtil.*;
import static org.jgll.util.visualization.GraphVizUtil.*;

import org.jgll.grammar.GrammarGraph;
import org.jgll.grammar.slot.BodyGrammarSlot;
import org.jgll.grammar.slot.ConditionalTransition;
import org.jgll.grammar.slot.GrammarSlot;
import org.jgll.grammar.slot.NonterminalGrammarSlot;
import org.jgll.util.generator.GeneratorUtil;

public class GrammarGraphToDot {

	public static String toDot(GrammarGraph g) {
		
		final StringBuilder sb = new StringBuilder();
		
		for (NonterminalGrammarSlot nonterminal : g.getNonterminals()) {
			toDot(nonterminal, sb);
		}
		
		return sb.toString();
	}
	
	private static void toDot(NonterminalGrammarSlot slot, StringBuilder sb) {
		sb.append("\"" + slot.getId() + "\"" + String.format(NONTERMINAL_SLOT, 
				escape(slot.getNonterminal().getParameters() != null? 
						String.format("%s(%s)", slot.getNonterminal().getName(), GeneratorUtil.listToString(slot.getNonterminal().getParameters(), ",")) 
						: slot.getNonterminal().getName())) + "\n");
		
		slot.getFirstSlots().forEach(s -> sb.append(EPSILON_TRANSITION + "\"" + slot.getId() + "\"" + "->" + "{\"" + s.getId() + "\"}" + "\n"));
		slot.getFirstSlots().forEach(s -> toDot(s, sb));
	}
	
	private static void toDot(GrammarSlot slot, StringBuilder sb) {
		sb.append("\"" + slot.getId() + "\"" + BODY_SLOT + "\n");
		// TODO: improve this code
		slot.getTransitions().forEach(t -> { 
			if(t instanceof ConditionalTransition) {
				sb.append(String.format(TRANSITION, t.getLabel() + ", true") + "\"" + slot.getId() + "\"" + "->" + "{\"" + t.destination().getId() + "\"}" + "\n");
				
				BodyGrammarSlot ifFalse = ((ConditionalTransition) t).ifFalseDestination();
				
				if (ifFalse != null)
					sb.append(String.format(TRANSITION, t.getLabel() + ", false") + "\"" + slot.getId() + "\"" + "->" + "{\"" + ifFalse.getId() + "\"}" + "\n");
			}
			else sb.append(String.format(TRANSITION, t.getLabel()) + "\"" + slot.getId() + "\"" + "->" + "{\"" + t.destination().getId() + "\"}" + "\n"); 
		});
		slot.getTransitions().forEach(t -> toDot(t.destination(), sb));
	}
	
}
