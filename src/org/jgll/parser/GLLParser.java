package org.jgll.parser;

import org.jgll.datadependent.ast.Expression;
import org.jgll.datadependent.env.Environment;
import org.jgll.datadependent.env.IEvaluatorContext;
import org.jgll.grammar.Grammar;
import org.jgll.grammar.GrammarGraph;
import org.jgll.grammar.GrammarRegistry;
import org.jgll.grammar.condition.DataDependentCondition;
import org.jgll.grammar.slot.BodyGrammarSlot;
import org.jgll.grammar.slot.EndGrammarSlot;
import org.jgll.grammar.slot.GrammarSlot;
import org.jgll.grammar.slot.NonterminalGrammarSlot;
import org.jgll.grammar.slot.TerminalGrammarSlot;
import org.jgll.grammar.symbol.CodeBlock;
import org.jgll.grammar.symbol.Nonterminal;
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
	
	public ParseResult parse(Input input, GrammarGraph grammarGraph, Nonterminal startSymbol);
	
	default ParseResult parse(Input input, Grammar grammar, Nonterminal startSymbol) {
		return parse(input, grammar.toGrammarGraph(input, getConfiguration()), startSymbol);
	}
	
	public void pop(GSSNode gssNode, int inputIndex, NonPackedNode node);
	
	public GSSNode create(BodyGrammarSlot returnSlot, NonterminalGrammarSlot nonterminal, GSSNode gssNode, int i, NonPackedNode node);
	
	public TerminalNode getTerminalNode(TerminalGrammarSlot slot, int leftExtent, int rightExtent);

	public TerminalNode getEpsilonNode(TerminalGrammarSlot slot, int inputIndex);
	
	public NonterminalNode getNonterminalNode(EndGrammarSlot slot, NonPackedNode leftChild, NonPackedNode rightChild);
	
	public NonterminalNode getNonterminalNode(EndGrammarSlot slot, NonPackedNode child);
	
	public IntermediateNode getIntermediateNode(BodyGrammarSlot slot, NonPackedNode leftChild, NonPackedNode rightChild);
	
	public NonPackedNode getNode(GrammarSlot slot, NonPackedNode leftChild, NonPackedNode rightChild);
	
	public boolean hasDescriptor(GrammarSlot slot, GSSNode gssNode, int inputIndex, NonPackedNode sppfNode);
	
	public void recordParseError(GrammarSlot slot);
	
	public Input getInput();
	
	public Iterable<GSSNode> getGSSNodes();
	
	public GrammarRegistry getRegistry();
		
	public void reset();

	public Configuration getConfiguration();
	
	/**
	 * 
	 * Data-dependent GLL parsing
	 * 
	 */
	public Object evaluate(CodeBlock code, Environment env);
	
	public Object evaluate(DataDependentCondition condition, Environment env);
	
	public Object[] evaluate(Expression[] arguments, Environment env);
	
	public IEvaluatorContext getEvaluatorContext();
	
	public Environment getEnvironment();
	
	public void setEnvironment(Environment env);
	
	public Environment getEmptyEnvironment();
	
	public GSSNode create(BodyGrammarSlot returnSlot, NonterminalGrammarSlot nonterminal, GSSNode gssNode, int i, NonPackedNode node, Expression[] arguments, Environment env);
	
	public boolean hasDescriptor(GrammarSlot slot, GSSNode gssNode, int inputIndex, NonPackedNode sppfNode, Environment env);
	
}
