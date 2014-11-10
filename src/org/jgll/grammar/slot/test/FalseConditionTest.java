package org.jgll.grammar.slot.test;

import java.io.Serializable;

import org.jgll.lexer.Lexer;
import org.jgll.parser.GLLParser;
import org.jgll.parser.gss.GSSNode;

public class FalseConditionTest implements ConditionTest, Serializable {

	private static final long serialVersionUID = 1L;
	
	private FalseConditionTest() {}
	
	private static FalseConditionTest instance;
	
	public static FalseConditionTest getInstance() {
		if (instance == null) {
			instance = new FalseConditionTest();
		}
		return instance;
	} 

	@Override
	public boolean execute(GLLParser parser, Lexer lexer, GSSNode gssNode, int inputIndex) {
		return false;
	}

	@Override
	public String getConstructorCode() {
		return "FalseConditionTest.getInstance()";
	}

}
