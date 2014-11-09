package org.jgll.grammar.slotaction;

import org.jgll.grammar.condition.Condition;
import org.jgll.lexer.GLLLexer;
import org.jgll.parser.GLLParser;
import org.jgll.parser.gss.GSSNode;
import org.jgll.util.generator.ConstructorCode;

public interface SlotAction<T> extends ConstructorCode {
	
	public T execute(GLLParser parser, GLLLexer lexer, GSSNode gssNode, int inputIndex);
	
	/**
	 * The condition from which this grammar slot is created. 
	 */
	public Condition getCondition();
	
}
