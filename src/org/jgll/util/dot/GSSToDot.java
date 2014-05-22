package org.jgll.util.dot;

import static org.jgll.util.dot.GraphVizUtil.GSS_EDGE;
import static org.jgll.util.dot.GraphVizUtil.GSS_NODE;

import java.util.Map;

import org.jgll.parser.gss.GSSEdge;
import org.jgll.parser.gss.GSSNode;

public class GSSToDot extends ToDot {
	
	private StringBuilder sb = new StringBuilder();
	
	public void execute(Iterable<GSSNode> set, Map<GSSNode, Iterable<GSSEdge>> map) {
		
		for(GSSNode gssNode : set) {
			
			sb.append("\"" + getId(gssNode) + "\"" + String.format(GSS_NODE, gssNode.toString()) + "\n");
			
			for(GSSEdge edge : map.get(gssNode)) {
				String label = edge.getReturnSlot() == null ? "" : String.format(GSS_EDGE, edge.getReturnSlot()); 
				sb.append(label + "\"" + getId(gssNode) + "\"" + "->" + "{\"" + getId(edge.getDestination()) + "\"}" + "\n");				
			}
		}
	}

	private String getId(GSSNode node) {
		return node.getGrammarSlot() + "" + node.getInputIndex();
	}
	
	public String getString() {
		return sb.toString();
	}
	
}
