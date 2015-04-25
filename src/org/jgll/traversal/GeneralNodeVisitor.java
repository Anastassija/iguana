package org.jgll.traversal;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.jgll.sppf.IntermediateNode;
import org.jgll.sppf.NonterminalNode;
import org.jgll.sppf.PackedNode;
import org.jgll.sppf.SPPFNode;
import org.jgll.sppf.TerminalNode;

public class GeneralNodeVisitor implements SPPFVisitor {

	private Set<SPPFNode> visitedNodes;
	
	private final Consumer<TerminalNode> terminalAction;
	private final Consumer<NonterminalNode> nonterminalAction;
	private final Consumer<IntermediateNode> intermediateAction;

	public GeneralNodeVisitor(Consumer<TerminalNode> terminalAction, 
							  Consumer<NonterminalNode> nonterminalAction,
							  Consumer<IntermediateNode> intermediateAction) {
		this.terminalAction = terminalAction;
		this.nonterminalAction = nonterminalAction;
		this.intermediateAction = intermediateAction;
		visitedNodes = new HashSet<>();
	}

	@Override
	public void visit(PackedNode node) {
		visitChildren(node);
	}

	@Override
	public void visit(IntermediateNode node) {
		intermediateAction.accept(node);
		visitChildren(node);
	}

	@Override
	public void visit(NonterminalNode node) {
		nonterminalAction.accept(node);
		visitChildren(node);
	}

	@Override
	public void visit(TerminalNode node) {
		terminalAction.accept(node);
	}
	
	private boolean isVisited(SPPFNode node) {
		return visitedNodes.contains(node);
	}	
	
	private void visitChildren(SPPFNode node) {
		if (!isVisited(node)) {
			visitedNodes.add(node);
			node.getChildren().forEach(c -> c.accept(this));
		}
	}
	
}
