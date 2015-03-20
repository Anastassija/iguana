package org.jgll.parser.datadependent.haskell;

import java.util.Set;

import org.jgll.traversal.NonterminalNodeVisitor;
import org.jgll.util.Input;

import com.google.common.collect.ImmutableSet;

public class SelectedFiles {
	
	public static final String test = "/Users/anastasiaizmaylova/git/diguana/testH.hs";
	
	private static final String ghc = "/Users/anastasiaizmaylova/git/ghc/";
	public final static String[] files = new String[] {
									ghc + "compiler/utils/Bag.hs",           // check #0
									ghc + "compiler/utils/BufWrite.hs",      // check #1
									ghc + "compiler/utils/Encoding.hs",      //       #2
									ghc + "compiler/utils/Exception.hs",     //       #3
									ghc + "compiler/utils/FiniteMap.hs",     //       #4
									ghc + "compiler/utils/GraphBase.hs",     //       #5
									ghc + "compiler/utils/GraphColor.hs",    //       #6
									ghc + "compiler/utils/GraphOps.hs",      // check #7
									ghc + "compiler/utils/GraphPpr.hs",      //       #8
									ghc + "compiler/utils/MonadUtils.hs",    //       #9
									ghc + "compiler/utils/Platform.hs",      //       #10
									ghc + "compiler/utils/Outputable.hs",    // check #11
									ghc + "compiler/utils/UnVarGraph.hs",    //       #12
									ghc + "compiler/prelude/ForeignCall.hs", //       #13
									ghc + "compiler/profiling/CostCentre.hs",//       #14
									ghc + "compiler/profiling/ProfInit.hs",  //       #15
									ghc + "compiler/simplCore/CallArity.hs", // check #16
									ghc + "compiler/simplCore/SimplMonad.hs",//       #17
									ghc + "compiler/main/Annotations.hs",    //       #18
									ghc + "compiler/main/GhcPlugins.hs",     //       #19
									ghc + "compiler/main/HscStats.hs",       //       #20
									ghc + "compiler/main/PipelineMonad.hs"   //       #21
								};
	
	private static final Set<String> target = ImmutableSet.of("Decls", "CDecls", "GADTDecls", "Alts", "Stmts");
	
	public static NonterminalNodeVisitor getVisitor(Input input) {
		return NonterminalNodeVisitor.create(n -> {
						if (target.contains(n.getGrammarSlot().getNonterminal().getName())) {
							System.out.println(n.getGrammarSlot().getNonterminal().getName() + ": "
											   + input.getLineNumber(n.getLeftExtent()) + ":"
											   + input.getColumnNumber(n.getLeftExtent()) + " "
											   + input.getLineNumber(n.getRightExtent()) + ":"
											   + input.getColumnNumber(n.getRightExtent()));
						}
			   });
	}

}
