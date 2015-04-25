package org.jgll.parser.datadependent.haskell;

import java.util.Set;

import org.jgll.traversal.NonterminalNodeVisitor;
import org.jgll.util.Input;

import com.google.common.collect.ImmutableSet;

public class SelectedFiles {
	
	public static final String test = "/Users/anastasiaizmaylova/git/diguana/testH.hs";
	
	public static final String test1 = "/Users/anastasiaizmaylova/git/diguana/test/org/jgll/parser/datadependent/haskell/test1.hs";
	public static final String test2 = "/Users/anastasiaizmaylova/git/diguana/test/org/jgll/parser/datadependent/haskell/test2.hs";
	public static final String test3 = "/Users/anastasiaizmaylova/git/diguana/test/org/jgll/parser/datadependent/haskell/test3.hs";
	public static final String test4 = "/Users/anastasiaizmaylova/git/diguana/test/org/jgll/parser/datadependent/haskell/test4.hs";
	public static final String test5 = "/Users/anastasiaizmaylova/git/diguana/test/org/jgll/parser/datadependent/haskell/test5.hs";
	public static final String test6 = "/Users/anastasiaizmaylova/git/diguana/test/org/jgll/parser/datadependent/haskell/test6.hs";
	public static final String test7 = "/Users/anastasiaizmaylova/git/diguana/test/org/jgll/parser/datadependent/haskell/test7.hs";
	public static final String test8 = "/Users/anastasiaizmaylova/git/diguana/test/org/jgll/parser/datadependent/haskell/test8.hs";
	public static final String test9 = "/Users/anastasiaizmaylova/git/diguana/test/org/jgll/parser/datadependent/haskell/test9.hs";
	public static final String test10 = "/Users/anastasiaizmaylova/git/diguana/test/org/jgll/parser/datadependent/haskell/test10.hs";
	public static final String test11_1 = "/Users/anastasiaizmaylova/git/diguana/test/org/jgll/parser/datadependent/haskell/test11_1.hs";
	public static final String test11_2 = "/Users/anastasiaizmaylova/git/diguana/test/org/jgll/parser/datadependent/haskell/test11_2.hs";
	public static final String test11_3 = "/Users/anastasiaizmaylova/git/diguana/test/org/jgll/parser/datadependent/haskell/test11_3.hs";
	
	private static final String ghc = "/Users/anastasiaizmaylova/git/ghc/";
	public final static String[] files = new String[] {
									ghc + "compiler/utils/Bag.hs",            // check #0
									ghc + "compiler/utils/BufWrite.hs",       // check #1
									ghc + "compiler/utils/Encoding.hs",       //       #2
									ghc + "compiler/utils/Exception.hs",      //       #3
									ghc + "compiler/utils/FiniteMap.hs",      //       #4
									ghc + "compiler/utils/GraphBase.hs",      //       #5
									ghc + "compiler/utils/GraphColor.hs",     //       #6
									ghc + "compiler/utils/GraphOps.hs",       // check #7
									ghc + "compiler/utils/GraphPpr.hs",       //       #8
									ghc + "compiler/utils/MonadUtils.hs",     //       #9
									ghc + "compiler/utils/Platform.hs",       //       #10
									ghc + "compiler/utils/Outputable.hs",     // check #11
									ghc + "compiler/utils/UnVarGraph.hs",     //       #12
									ghc + "compiler/prelude/ForeignCall.hs",  //       #13
									ghc + "compiler/profiling/CostCentre.hs", //       #14
									ghc + "compiler/profiling/ProfInit.hs",   //       #15
									ghc + "compiler/simplCore/CallArity.hs",  // check #16
									ghc + "compiler/simplCore/SimplMonad.hs", //       #17
									ghc + "compiler/main/Annotations.hs",     //       #18
									ghc + "compiler/main/GhcPlugins.hs",      //       #19
									ghc + "compiler/main/HscStats.hs",        //       #20
									ghc + "compiler/main/PipelineMonad.hs",   //       #21
									
									ghc + "compiler/ghci/ByteCodeItbls.hs",   // check #22
									ghc + "compiler/ghci/ByteCodeAsm.hs",     // check #23
									ghc + "compiler/ghci/ByteCodeGen.hs",     // check #24
									ghc + "compiler/ghci/Debugger.hs",        // check #25
								};
	
	private static final Set<String> target = ImmutableSet.of("Decls", "CDecls", "GADTDecls", "Alts", "DAlts", "Stmts");
	
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
