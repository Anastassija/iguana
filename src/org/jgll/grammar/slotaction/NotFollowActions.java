package org.jgll.grammar.slotaction;

import org.jgll.grammar.condition.Condition;
import org.jgll.lexer.GLLLexer;
import org.jgll.parser.GLLParser;
import org.jgll.parser.gss.GSSNode;
import org.jgll.regex.RegularExpression;
import org.jgll.regex.automaton.RunnableAutomaton;

public class NotFollowActions {
	
	public static SlotAction<Boolean> fromRegularExpression(final RegularExpression regex, final Condition condition) {
		
		final RunnableAutomaton r = regex.getAutomaton().getRunnableAutomaton();
		
		return new SlotAction<Boolean>() {
			
			@Override
			public Boolean execute(GLLParser parser, GLLLexer lexer, GSSNode gssNode, int inputIndex) {
				return r.match(lexer.getInput(), inputIndex) >= 0;
			}

			@Override
			public Condition getCondition() {
				return condition;
			}
			
			@Override
			public boolean equals(Object obj) {
				if(this == obj) {
					return true;
				}
				
				if(!(obj instanceof SlotAction)) {
					return false;
				}
				
				@SuppressWarnings("unchecked")
				SlotAction<Boolean> other = (SlotAction<Boolean>) obj;
				return getCondition().equals(other.getCondition());
			}
			
			@Override
			public String toString() {
				return condition.toString();
			}

		};
	}		
}
