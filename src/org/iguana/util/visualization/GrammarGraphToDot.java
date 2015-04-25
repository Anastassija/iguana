package org.iguana.util.visualization;

import static org.iguana.util.generator.GeneratorUtil.*;
import static org.iguana.util.visualization.GraphVizUtil.*;

import org.iguana.grammar.GrammarGraph;
import org.iguana.grammar.slot.BeforeLastTerminalTransition;
import org.iguana.grammar.slot.BodyGrammarSlot;
import org.iguana.grammar.slot.ConditionalTransition;
import org.iguana.grammar.slot.EndGrammarSlot;
import org.iguana.grammar.slot.FirstAndLastTerminalTransition;
import org.iguana.grammar.slot.FirstTerminalTransition;
import org.iguana.grammar.slot.GrammarSlot;
import org.iguana.grammar.slot.LastSymbolAndEndGrammarSlot;
import org.iguana.grammar.slot.LastSymbolGrammarSlot;
import org.iguana.grammar.slot.NonterminalGrammarSlot;
import org.iguana.util.generator.GeneratorUtil;

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
		if (slot instanceof LastSymbolAndEndGrammarSlot) {
			sb.append("\"" + slot.getId() + "\"" + String.format(LAST_SYMBOL_AND_END_SLOT, "") + "\n");
		} else if (slot instanceof LastSymbolGrammarSlot) {
			sb.append("\"" + slot.getId() + "\"" + String.format(LAST_SYMBOL_SLOT, "") + "\n");
		} else if (slot instanceof EndGrammarSlot) {
			sb.append("\"" + slot.getId() + "\"" + String.format(END_SLOT, "") + "\n");
		} else {
			sb.append("\"" + slot.getId() + "\"" + BODY_SLOT + "\n");
		}
		
		// TODO: improve this code
		slot.getTransitions().forEach(t -> { 
			if(t instanceof ConditionalTransition) {
				sb.append(String.format(TRANSITION, t.getLabel() + ", true") + "\"" + slot.getId() + "\"" + "->" + "{\"" + t.destination().getId() + "\"}" + "\n");
				
				BodyGrammarSlot ifFalse = ((ConditionalTransition) t).ifFalseDestination();
				
				if (ifFalse != null)
					sb.append(String.format(TRANSITION, t.getLabel() + ", false") + "\"" + slot.getId() + "\"" + "->" + "{\"" + ifFalse.getId() + "\"}" + "\n");
			} else if (t instanceof FirstAndLastTerminalTransition
						|| t instanceof FirstTerminalTransition
						|| t instanceof BeforeLastTerminalTransition) {
				sb.append(String.format(SPECIAL_TERMINAL_TRANSITION, t.getLabel()) + "\"" + slot.getId() + "\"" + "->" + "{\"" + t.destination().getId() + "\"}" + "\n");
			}
			else sb.append(String.format(TRANSITION, t.getLabel()) + "\"" + slot.getId() + "\"" + "->" + "{\"" + t.destination().getId() + "\"}" + "\n"); 
		});
		slot.getTransitions().forEach(t -> toDot(t.destination(), sb));
	}
	
}
