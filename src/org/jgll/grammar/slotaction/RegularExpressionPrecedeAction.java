package org.jgll.grammar.slotaction;

import org.jgll.grammar.condition.Condition;
import org.jgll.grammar.condition.RegularExpressionCondition;
import org.jgll.lexer.Lexer;
import org.jgll.parser.GLLParser;
import org.jgll.parser.gss.GSSNode;
import org.jgll.regex.automaton.RunnableAutomaton;

public class RegularExpressionPrecedeAction implements SlotAction<Boolean> {
	
	private final RegularExpressionCondition condition;
	private final RunnableAutomaton r;

	public RegularExpressionPrecedeAction(RegularExpressionCondition condition) {
		this.condition = condition;
		r = condition.getRegularExpression().getAutomaton().reverse().getRunnableAutomaton();
	}
	
	@Override
	public Boolean execute(GLLParser parser, Lexer lexer, GSSNode gssNode, int inputIndex) {
		return r.matchBackwards(lexer.getInput(), inputIndex - 1) == -1;
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
	
	@Override
	public String getConstructorCode() {
		return "new RegularExpressionPrecedeAction(" + condition.getConstructorCode() + ")";
	}

}
