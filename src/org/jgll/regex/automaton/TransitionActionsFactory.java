package org.jgll.regex.automaton;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jgll.grammar.condition.Condition;
import org.jgll.grammar.condition.RegularExpressionCondition;
import org.jgll.regex.RegexAlt;
import org.jgll.regex.RegularExpression;
import org.jgll.regex.matcher.Matcher;
import org.jgll.util.Input;

public class TransitionActionsFactory {

	public static TransitionAction getPostActions(Set<Condition> conditions) {
		
		final List<RegularExpression> notFollows = new ArrayList<>();
		
		for(Condition condition : conditions) {
			
			switch (condition.getType()) {
			
//			case FOLLOW:
//				if (condition instanceof RegularExpressionCondition) {
//					postConditionActions.add(FollowActions.fromRegularExpression(((RegularExpressionCondition) condition).getRegularExpression(), condition));
//				} 
//				else {
////					postConditions.addCondition(convertCondition((ContextFreeCondition) condition), condition);
//				}
//				break;
				
			case NOT_FOLLOW:
				if (condition instanceof RegularExpressionCondition) {
					RegularExpression regex = ((RegularExpressionCondition) condition).getRegularExpression();
					notFollows.add(regex);
				} 
				else {
				}
				break;
				
			
			default:
				break;
			}
		}
		
		return new TransitionAction() {

			RegularExpression regex = new RegexAlt<>(notFollows);
			Matcher m = regex.toAutomaton().getMatcher();
			
			@Override
			public boolean execute(Input input, int index) {
				return m.match(input, index) > 0;
			}
			
			@Override
			public String toString() {
				return "!>> " + regex.toString();
			}
		};
	}
	
}
