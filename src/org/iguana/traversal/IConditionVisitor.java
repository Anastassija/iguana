package org.jgll.traversal;

import org.jgll.grammar.condition.ContextFreeCondition;
import org.jgll.grammar.condition.DataDependentCondition;
import org.jgll.grammar.condition.PositionalCondition;
import org.jgll.grammar.condition.RegularExpressionCondition;

public interface IConditionVisitor<T> {
	
	public T visit(ContextFreeCondition condition);
	
	public T visit(DataDependentCondition condition);
	
	public T visit(PositionalCondition condition);
	
	public T visit(RegularExpressionCondition condition);

}
