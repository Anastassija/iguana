package org.jgll.grammar;

import java.io.File;
import java.io.IOException;

import org.jgll.benchmark.IguanaBenchmark;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Start;
import org.jgll.grammar.transformation.EBNFToBNF;
import org.jgll.grammar.transformation.LayoutWeaver;
import org.jgll.util.Input;
import org.junit.Test;

public class TestCSharp {
	
	private static Grammar originalGrammar = Grammar.load(new File("grammars/csharp/csharp"));
	private static Grammar grammar = new LayoutWeaver().transform(new EBNFToBNF().transform(originalGrammar));
	private static Start start = Start.from(Nonterminal.withName("CompilationUnit"));

	@Test
	public void test() throws IOException {
		Input input = Input.fromPath(getClass().getResource("examples/Test1").getPath());
//		Input input = Input.fromPath("/Users/aliafroozeh/test.cs");
//		GLLParser parser = ParserFactory.getParser(Configuration.DEFAULT, input, grammar);
//		ParseResult result = parser.parse(input, grammar, start);
//		System.out.println(result);
		
		IguanaBenchmark.builder(grammar, start).addDirectory("/Users/aliafroozeh/corpus/CSharp/roslyn", "cs", true).setTimeout(30).build().run();;
	}
}
