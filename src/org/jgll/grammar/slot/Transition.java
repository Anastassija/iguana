package org.jgll.grammar.slot;

import org.jgll.parser.GLLParser;
import org.jgll.parser.gss.GSSNode;
import org.jgll.sppf.NonPackedNode;
import org.jgll.util.generator.ConstructorCode;


public interface Transition extends ConstructorCode {
	
	public void execute(GLLParser parser, GSSNode u, int i, NonPackedNode node);
	
	public GrammarSlot destination();
	
	public GrammarSlot origin();

	public String getLabel();
	
}
