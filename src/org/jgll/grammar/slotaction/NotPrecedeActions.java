package org.jgll.grammar.slotaction;

import java.util.BitSet;
import java.util.List;

import org.jgll.grammar.condition.Condition;
import org.jgll.grammar.slot.BodyGrammarSlot;
import org.jgll.grammar.symbol.Keyword;
import org.jgll.grammar.symbol.Terminal;
import org.jgll.lexer.GLLLexer;
import org.jgll.parser.GLLParser;
import org.jgll.regex.Matcher;
import org.jgll.regex.RegexAlt;
import org.jgll.util.logging.LoggerWrapper;


public class NotPrecedeActions {

	private static final LoggerWrapper log = LoggerWrapper.getLogger(NotPrecedeActions.class);
	
	public static void fromTerminal(BodyGrammarSlot slot, final Terminal terminal, final Condition condition) {
		log.debug("Precede restriction added %s <<! %s", terminal, slot);
		
		BitSet testSet = new BitSet();
		testSet.or(terminal.asBitSet());
		
		final BitSet set = testSet;
		
		
		slot.addPreCondition(new SlotAction<Boolean>() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public Boolean execute(GLLParser parser, GLLLexer lexer) {
				
				int ci = parser.getCurrentInputIndex();
				if (ci == 0) {
					return false;
				}
			
				return set.get(lexer.getInput().charAt(ci - 1));
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
	
	public static void fromKeywordList(BodyGrammarSlot slot, final List<Keyword> list, final Condition condition) {
		
		log.debug("Precede restriction added %s <<! %s", list, slot);
		
		RegexAlt<Keyword> alt = new RegexAlt<>(list);
		final Matcher matcher = alt.toNFA().reverse().getMatcher();


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
