package org.jgll.parser;

import org.jgll.datadependent.ast.Expression;
import org.jgll.datadependent.env.Environment;
import org.jgll.datadependent.env.IEvaluatorContext;
import org.jgll.grammar.Grammar;
import org.jgll.grammar.GrammarRegistry;
import org.jgll.grammar.condition.DataDependentCondition;
import org.jgll.grammar.slot.BodyGrammarSlot;
import org.jgll.grammar.slot.EndGrammarSlot;
import org.jgll.grammar.slot.GrammarSlot;
import org.jgll.grammar.slot.NonterminalGrammarSlot;
import org.jgll.grammar.slot.TerminalGrammarSlot;
import org.jgll.grammar.symbol.CodeBlock;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.parser.descriptor.Descriptor;
import org.jgll.parser.gss.GSSNode;
import org.jgll.sppf.IntermediateNode;
import org.jgll.sppf.NonPackedNode;
import org.jgll.sppf.NonterminalNode;
import org.jgll.sppf.TerminalNode;
import org.jgll.util.Configuration;
import org.jgll.util.Input;

/**
 * 
 * 
 * @author Ali Afroozeh
 *
 */
public interface GLLParser {
	
	public ParseResult parse(Input input, Grammar grammar, Nonterminal startSymbol, Configuration config);
	
	default ParseResult parse(Input input, Grammar grammar, Nonterminal startSymbol) {
		return parse(input, grammar, startSymbol, Configuration.DEFAULT);
	}
	
	/**
	 * Data-dependent GLL parsing
	 */
	public Object eval(CodeBlock code, Environment env);
	
	public Object eval(DataDependentCondition condition, Environment env);
	
	public Object[] eval(Expression[] arguments, Environment env);
	
	public IEvaluatorContext getEvaluatorContext();
	
	public void pop(GSSNode gssNode, int inputIndex, NonPackedNode node);
	
	public GSSNode create(GrammarSlot returnSlot, NonterminalGrammarSlot nonterminal, GSSNode gssNode, int i, NonPackedNode node, Expression[] arguments, Environment env);
	
	public TerminalNode getTerminalNode(TerminalGrammarSlot slot, int leftExtent, int rightExtent);

	public TerminalNode getEpsilonNode(int inputIndex);
	
	public NonterminalNode getNonterminalNode(EndGrammarSlot slot, NonPackedNode leftChild, NonPackedNode rightChild);
	
	public NonterminalNode getNonterminalNode(EndGrammarSlot slot, NonPackedNode child);
	
	public IntermediateNode getIntermediateNode(BodyGrammarSlot slot, NonPackedNode leftChild, NonPackedNode rightChild);
	
	public boolean hasDescriptor(GrammarSlot slot, GSSNode gssNode, int inputIndex, NonPackedNode sppfNode);
	
	public void scheduleDescriptor(Descriptor descriptor);
	
	default boolean addDescriptor(GrammarSlot slot, GSSNode gssNode, int inputIndex, NonPackedNode sppfNode, Environment env) {
		if (!hasDescriptor(slot, gssNode, inputIndex, sppfNode)) {
			scheduleDescriptor(new Descriptor(slot, gssNode, inputIndex, sppfNode, env));
			return true;
		}
		return false;
	}
	
	public boolean hasNextDescriptor();
	
	/**
	 * Reads the next descriptor and sets the state of the parser to it.
	 */
	public Descriptor nextDescriptor();
	
	public void recordParseError(GrammarSlot slot);
	
	public Input getInput();
	
	public Iterable<GSSNode> getGSSNodes();
	
	public GrammarRegistry getRegistry();
		
	public void reset();

	public Configuration getConfiguration();
	
}
