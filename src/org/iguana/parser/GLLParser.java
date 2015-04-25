package org.iguana.parser;

import java.util.Collections;
import java.util.Map;

import org.iguana.datadependent.ast.Expression;
import org.iguana.datadependent.ast.Statement;
import org.iguana.datadependent.env.Environment;
import org.iguana.datadependent.env.IEvaluatorContext;
import org.iguana.grammar.Grammar;
import org.iguana.grammar.GrammarGraph;
import org.iguana.grammar.condition.DataDependentCondition;
import org.iguana.grammar.slot.BodyGrammarSlot;
import org.iguana.grammar.slot.GrammarSlot;
import org.iguana.grammar.slot.LastSymbolGrammarSlot;
import org.iguana.grammar.slot.NonterminalGrammarSlot;
import org.iguana.grammar.slot.TerminalGrammarSlot;
import org.iguana.grammar.symbol.Nonterminal;
import org.iguana.parser.gss.GSSNode;
import org.iguana.parser.gss.GSSNodeData;
import org.iguana.sppf.IntermediateNode;
import org.iguana.sppf.NonPackedNode;
import org.iguana.sppf.NonterminalNode;
import org.iguana.sppf.TerminalNode;
import org.iguana.util.Configuration;
import org.iguana.util.Input;

/**
 * 
 * 
 * @author Ali Afroozeh
 *
 */
public interface GLLParser {
	
	public ParseResult parse(Input input, GrammarGraph grammarGraph, Nonterminal startSymbol, Map<String, Object> map);
	
	default ParseResult parse(Input input, GrammarGraph grammarGraph, Nonterminal startSymbol) {
		return parse(input, grammarGraph, startSymbol, Collections.emptyMap());
	}
	
	default ParseResult parse(Input input, Grammar grammar, Nonterminal startSymbol) {
		return parse(input, grammar.toGrammarGraph(input, getConfiguration()), startSymbol);
	}
	
	default ParseResult parse(Input input, Grammar grammar, Nonterminal startSymbol, Map<String, Object> map) {
		return parse(input, grammar.toGrammarGraph(input, getConfiguration()), startSymbol, map);
	}
	
	public void pop(GSSNode gssNode, int inputIndex, NonPackedNode node);
	
	public GSSNode create(BodyGrammarSlot returnSlot, NonterminalGrammarSlot nonterminal, GSSNode gssNode, int i, NonPackedNode node);
	
	public TerminalNode getTerminalNode(TerminalGrammarSlot slot, int leftExtent, int rightExtent);

	public TerminalNode getEpsilonNode(TerminalGrammarSlot slot, int inputIndex);
	
	public NonterminalNode getNonterminalNode(LastSymbolGrammarSlot slot, NonPackedNode leftChild, NonPackedNode rightChild);
	
	public NonterminalNode getNonterminalNode(LastSymbolGrammarSlot slot, NonPackedNode child);
	
	public IntermediateNode getIntermediateNode(BodyGrammarSlot slot, NonPackedNode leftChild, NonPackedNode rightChild);
	
	public NonPackedNode getNode(GrammarSlot slot, NonPackedNode leftChild, NonPackedNode rightChild);
	
	public boolean hasDescriptor(GrammarSlot slot, GSSNode gssNode, int inputIndex, NonPackedNode sppfNode);
	
	public void recordParseError(GrammarSlot slot);
	
	public Input getInput();
	
	public Iterable<GSSNode> getGSSNodes();
	

	public void reset();

	public Configuration getConfiguration();
	
	/**
	 * 
	 * Data-dependent GLL parsing
	 * 
	 */	
	public Object evaluate(Statement[] statements, Environment env);
	
	public Object evaluate(DataDependentCondition condition, Environment env);
	
	public Object evaluate(Expression expression, Environment env);
	
	public Object[] evaluate(Expression[] arguments, Environment env);
	
	public IEvaluatorContext getEvaluatorContext();
	
	public BodyGrammarSlot getCurrentEndGrammarSlot();
	
	public void setCurrentEndGrammarSlot(BodyGrammarSlot slot);
	
	public Environment getEnvironment();
	
	public void setEnvironment(Environment env);
	
	public Environment getEmptyEnvironment();
	
	public GSSNode create(BodyGrammarSlot returnSlot, NonterminalGrammarSlot nonterminal, GSSNode gssNode, int i, NonPackedNode node, Expression[] arguments, Environment env);
	
	public boolean hasDescriptor(GrammarSlot slot, GSSNode gssNode, int inputIndex, NonPackedNode sppfNode, Environment env);
	
	public <T> NonterminalNode getNonterminalNode(LastSymbolGrammarSlot slot, NonPackedNode leftChild, NonPackedNode rightChild, GSSNodeData<T> data);
	
	public <T> NonterminalNode getNonterminalNode(LastSymbolGrammarSlot slot, NonPackedNode child, GSSNodeData<T> data);
	
	public IntermediateNode getIntermediateNode(BodyGrammarSlot slot, NonPackedNode leftChild, NonPackedNode rightChild, Environment env);
	
	public <T> NonPackedNode getNode(GrammarSlot slot, NonPackedNode leftChild, NonPackedNode rightChild, Environment env, GSSNodeData<T> data);

	public GrammarGraph getGrammarGraph();
	
}
