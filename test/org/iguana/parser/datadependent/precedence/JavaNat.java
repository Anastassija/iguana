package org.iguana.parser.datadependent.precedence;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.iguana.datadependent.ast.AST;
import org.iguana.grammar.Grammar;
import org.iguana.grammar.GrammarGraph;
import org.iguana.grammar.condition.ConditionType;
import org.iguana.grammar.condition.RegularExpressionCondition;
import org.iguana.grammar.patterns.ExceptPattern;
import org.iguana.grammar.patterns.PrecedencePattern;
import org.iguana.grammar.precedence.OperatorPrecedence;
import org.iguana.grammar.symbol.*;
import org.iguana.grammar.symbol.Character;

import static org.iguana.grammar.symbol.LayoutStrategy.*;

import org.iguana.grammar.transformation.DesugarAlignAndOffside;
import org.iguana.grammar.transformation.DesugarPrecedenceAndAssociativity;
import org.iguana.grammar.transformation.DesugarState;
import org.iguana.grammar.transformation.EBNFToBNF;
import org.iguana.grammar.transformation.LayoutWeaver;
import org.iguana.parser.GLLParser;
import org.iguana.parser.ParseResult;
import org.iguana.parser.ParserFactory;
import org.iguana.regex.*;
import org.iguana.util.Configuration;
import org.iguana.util.Input;
import org.iguana.util.Visualization;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;

import static org.iguana.util.CollectionsUtil.*;

@SuppressWarnings("unused")
public class JavaNat {
	
	DesugarPrecedenceAndAssociativity desugarPrecedenceAndAssociativity = new DesugarPrecedenceAndAssociativity();

    @Test
    public void test() {
         Grammar grammar = Grammar.load(new File("test/org/iguana/parser/datadependent/precedence/JavaNatChar"));
         
         grammar = new EBNFToBNF().transform(grammar);
         
         desugarPrecedenceAndAssociativity.setOP1();
         
		 Grammar grammar1 = desugarPrecedenceAndAssociativity.transform(grammar);
         // System.out.println(grammar1);
         
         grammar1 = new LayoutWeaver().transform(grammar1);
         
         Grammar grammar2 = Grammar.load(new File("test/org/iguana/parser/datadependent/precedence/JavaNatChar"));
         List<PrecedencePattern> precedencePatterns = grammar2.getPrecedencePatterns();
		 List<ExceptPattern> exceptPatterns = grammar2.getExceptPatterns();
         
         grammar2 = new EBNFToBNF().transform(grammar2);
         
		 grammar2 = new OperatorPrecedence(precedencePatterns, exceptPatterns).transform(grammar2);
         
         // System.out.println(grammar2);
         
         grammar2 = new LayoutWeaver().transform(grammar2);
         
         desugarPrecedenceAndAssociativity.setOP2();
         
         Grammar grammar3 = desugarPrecedenceAndAssociativity.transform(grammar);
         // System.out.println(grammar3);
         
         grammar3 = new LayoutWeaver().transform(grammar3);
         
         Grammar grammar4 = Grammar.load(new File("test/org/iguana/parser/datadependent/precedence/JavaSpecChar"));
         
         grammar4 = new EBNFToBNF().transform(grammar4);
         grammar4 = new LayoutWeaver().transform(grammar4);

         Input input = Input.fromFile(new File("src/org/iguana/util/hashing/hashfunction/MurmurHash2.java"));
         GrammarGraph graph1 = grammar1.toGrammarGraph(input, Configuration.DEFAULT);
         GrammarGraph graph2 = grammar2.toGrammarGraph(input, Configuration.DEFAULT);
         GrammarGraph graph3 = grammar3.toGrammarGraph(input, Configuration.DEFAULT);
         GrammarGraph graph4 = grammar4.toGrammarGraph(input, Configuration.DEFAULT);

         GLLParser parser1 = ParserFactory.getParser(Configuration.DEFAULT, input, grammar1);
         GLLParser parser2 = ParserFactory.getParser(Configuration.DEFAULT, input, grammar2);
         GLLParser parser3 = ParserFactory.getParser(Configuration.DEFAULT, input, grammar3);
         GLLParser parser4 = ParserFactory.getParser(Configuration.DEFAULT, input, grammar4);
         
         ParseResult result1 = parser1.parse(input, graph1, Nonterminal.withName("CompilationUnit"));
         ParseResult result2 = parser2.parse(input, graph2, Nonterminal.withName("CompilationUnit"));
         ParseResult result3 = parser3.parse(input, graph3, Nonterminal.withName("CompilationUnit"));
         ParseResult result4 = parser4.parse(input, graph4, Nonterminal.withName("CompilationUnit"));
         
         Assert.assertTrue(result1.isParseSuccess());
         Assert.assertTrue(result2.isParseSuccess());
         Assert.assertTrue(result3.isParseSuccess());
         Assert.assertTrue(result4.isParseSuccess());
         
         Assert.assertEquals(0, result1.asParseSuccess().getStatistics().getCountAmbiguousNodes());
         Assert.assertEquals(0, result2.asParseSuccess().getStatistics().getCountAmbiguousNodes());
         Assert.assertEquals(0, result3.asParseSuccess().getStatistics().getCountAmbiguousNodes());
         Assert.assertEquals(0, result4.asParseSuccess().getStatistics().getCountAmbiguousNodes());
         
         System.out.println("OP scheme 1:");
         System.out.println(result1.asParseSuccess().getStatistics());
         System.out.println("Shape-preserving rewriting:");
         System.out.println(result2.asParseSuccess().getStatistics());
         System.out.println("OP scheme 2:");
         System.out.println(result3.asParseSuccess().getStatistics());
         System.out.println("Standard rewriting:");
         System.out.println(result4.asParseSuccess().getStatistics());
    }
}
