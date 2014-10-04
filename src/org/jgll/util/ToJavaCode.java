package org.jgll.util;

import org.jgll.grammar.GrammarGraph;
import org.jgll.sppf.IntermediateNode;
import org.jgll.sppf.ListSymbolNode;
import org.jgll.sppf.NonterminalNode;
import org.jgll.sppf.PackedNode;
import org.jgll.sppf.SPPFNode;
import org.jgll.sppf.TokenSymbolNode;
import org.jgll.traversal.SPPFVisitor;

public class ToJavaCode implements SPPFVisitor {
	
	
	private int count = 1;
	private StringBuilder sb = new StringBuilder();
	private GrammarGraph grammar;

	public ToJavaCode(GrammarGraph grammar) {
		this.grammar = grammar;
		sb.append("SPPFNodeFactory factory = new SPPFNodeFactory(grammar.toGrammarGraph());\n");
	}
	
	public static String toJavaCode(NonterminalNode node, GrammarGraph grammar) {
		ToJavaCode toJavaCode = new ToJavaCode(grammar);
		toJavaCode.visit(node);
		return toJavaCode.toString();
	}
	
	@Override
	public void visit(TokenSymbolNode node) {
		if(!node.isVisited()) {
			node.setVisited(true);
			sb.append("TokenSymbolNode node" + count + " = factory.createTokenNode(" +
					  "\"" + escape(grammar.getRegularExpressionById(node.getTokenID()).getName()) + "\", " +
					  node.getLeftExtent() + ", " + node.getLength() + ");\n");
			node.setObject("node" + count++);
		}
	}

	@Override
	public void visit(NonterminalNode node) {
		if(!node.isVisited()) {
			node.setVisited(true);
			node.setObject("node" + count);
			
			sb.append("NonterminalNode node" + count + " = factory.createNonterminalNode(" +
					"\"" + grammar.getNonterminalById(node.getId()).getName() + "\", " +
					grammar.getNonterminalById(node.getId()).getIndex() + ", " +
					node.getLeftExtent() + ", " + 
					node.getRightExtent() + ").init();\n");
			
			count++;
			
			visitChildren(node);
			
			addChildren(node);
		}
	}

	@Override
	public void visit(IntermediateNode node) {
		if(!node.isVisited()) {
			node.setVisited(true);
			node.setObject("node" + count);

			sb.append("IntermediateNode node" + count + " = factory.createIntermediateNode(" +
					  "\"" + escape(grammar.getGrammarSlot(node.getId()).toString()) + "\", " + 
					  node.getLeftExtent() + ", " + 
					  node.getRightExtent() + ").init();\n");
			
			count++;
			
			visitChildren(node);
			
			addChildren(node);
		}
	}

	@Override
	public void visit(PackedNode node) {
		if(!node.isVisited()) {
			node.setVisited(true);
			node.setObject("node" + count);

			sb.append("PackedNode node" + count + " = factory.createPackedNode(" +
					  "\"" + escape(grammar.getGrammarSlot(node.getId()).toString()) + "\", " + 
					  node.getPivot() + ", " + node.getParent().getObject() + ");\n");				
			
			count++;
			
			visitChildren(node);
			
			addChildren(node);			
		}
	}

	@Override
	public void visit(ListSymbolNode node) {
		if(!node.isVisited()) {
			node.setVisited(true);
			node.setObject("node" + count);

			sb.append("ListSymbolNode node" + count + " = factory.createListNode(" +
					  "\"" + grammar.getNonterminalById(node.getId()).getName() + "\", " +
					  grammar.getNonterminalById(node.getId()).getIndex() + ", " +
					  node.getLeftExtent() + ", " + 
					  node.getRightExtent() + ").init();\n");
			
			count++;
			
			visitChildren(node);
			
			addChildren(node);
		}
	}
	
	private void visitChildren(SPPFNode node) {
		for(SPPFNode child : node.getChildren()) {
			child.accept(this);
		}
	}
	
	private void addChildren(SPPFNode node) {
		for(SPPFNode child : node.getChildren()) {
			String childName = (String) child.getObject();
			assert childName != null;
			sb.append(node.getObject() + ".addChild(" + childName + ");\n");
		}
	}
	
	private String escape(String s) {
		return s.replaceAll("\\\\", "\\\\\\\\");
	}

	@Override
	public String toString() {
		return sb.toString();
	}

}
