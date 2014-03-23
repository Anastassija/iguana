package org.jgll.grammar.slotaction;

import org.jgll.grammar.condition.Condition;
import org.jgll.grammar.slot.BodyGrammarSlot;
import org.jgll.lexer.GLLLexer;
import org.jgll.parser.GLLParser;
import org.jgll.regex.RegularExpression;
import org.jgll.regex.matcher.Matcher;

public class NotMatchActions {

	  public static SlotAction<Boolean> fromGrammarSlot(final BodyGrammarSlot ifNot, final Condition condition) {

			return new SlotAction<Boolean>() {
				
				private static final long serialVersionUID = 1L;

				@Override
				public Boolean execute(GLLParser parser, GLLLexer lexer, int inputIndex) {
					throw new UnsupportedOperationException();
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

			};
		}
	  
		public static SlotAction<Boolean> fromRegularExpression(final RegularExpression regex, final Condition condition) {
			
			final Matcher matcher = regex.toAutomaton().getMatcher();
			
			return	new SlotAction<Boolean>() {
					
					private static final long serialVersionUID = 1L;

					@Override
					public Boolean execute(GLLParser parser, GLLLexer lexer, int inputIndex) {
						return matcher.match(lexer.getInput(), parser.getCurrentGSSNode().getInputIndex(), inputIndex);
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

				};
		}
}
