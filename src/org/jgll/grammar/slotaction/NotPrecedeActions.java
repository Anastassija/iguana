package org.jgll.grammar.slotaction;

import org.jgll.grammar.condition.Condition;
import org.jgll.grammar.slot.BodyGrammarSlot;
import org.jgll.lexer.GLLLexer;
import org.jgll.parser.GLLParser;
import org.jgll.regex.Matcher;
import org.jgll.regex.RegularExpression;
import org.jgll.util.logging.LoggerWrapper;


public class NotPrecedeActions {

	private static final LoggerWrapper log = LoggerWrapper.getLogger(NotPrecedeActions.class);
		
	public static void fromKeywordList(BodyGrammarSlot slot, final RegularExpression regex, final Condition condition) {
		
		log.debug("Precede restriction added %s <<! %s", regex, slot);
		
		final Matcher matcher = regex.toNFA().reverse().getMatcher();

		slot.addPreCondition(new SlotAction<Boolean>() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public Boolean execute(GLLParser parser, GLLLexer lexer) {
				return matcher.matchBackwards(lexer.getInput(), parser.getCurrentInputIndex() - 1) >= 0;
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
		});
	}
}
