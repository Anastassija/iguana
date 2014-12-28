package org.jgll.regex;

import java.util.ArrayList;
import java.util.List;

import org.jgll.grammar.symbol.Character;
import org.jgll.grammar.symbol.CharacterClass;
import org.jgll.grammar.symbol.CharacterRange;
import org.jgll.grammar.symbol.Keyword;
import org.jgll.grammar.symbol.SymbolBuilder;

public class RegularExpressionExamples {

	/**
	 * Id ::= [a-zA-Z][a-zA-Z0-9]*
	 */
	public static SymbolBuilder<? extends RegularExpression> getId() {
		CharacterClass c1 = CharacterClass.from(CharacterRange.in('a', 'z'), CharacterRange.in('A', 'Z'));
		CharacterClass c2 = CharacterClass.from(CharacterRange.in('a', 'z'), CharacterRange.in('A', 'Z'), CharacterRange.in('0', '9'));
		return Sequence.builder(c1, RegexStar.from(c2));
	}
	
	/**
	 * Float ::= [0-9]+[.][0-9]+
	 */
	public static SymbolBuilder<? extends RegularExpression> getFloat() {
		CharacterClass c = CharacterClass.from(CharacterRange.in('0', '9'));
		return Sequence.builder(RegexPlus.from(c), Character.from('.'), RegexPlus.from(c));
	}
	
	/**
	 * UnicodeEscape ::= "\\" [u]+ [0-9 A-F a-f] [0-9 A-F a-f] [0-9 A-F a-f] [0-9 A-F a-f];
	 */
	public static SymbolBuilder<? extends RegularExpression> getJavaUnicodeEscape() {
		List<RegularExpression> regularExpressions = new ArrayList<>();

		regularExpressions.add(Keyword.from("\\"));
		
		regularExpressions.add(RegexPlus.from(Character.from('u')));
		
		CharacterClass c = CharacterClass.from(CharacterRange.in('0', '9'), CharacterRange.in('a', 'z'), CharacterRange.in('A', 'Z'));
		regularExpressions.add(c);
		regularExpressions.add(c);
		regularExpressions.add(c);
		regularExpressions.add(c);
		
		return Sequence.builder(regularExpressions);
	}
	
	/**
	 * Character ::= ['] ![']+ ['] 
	 * 
	 * @return
	 */
	public static SymbolBuilder<? extends RegularExpression> getCharacter() {
		List<RegularExpression> regularExpressions = new ArrayList<>();
		regularExpressions.add(Character.from('\''));
		regularExpressions.add(RegexPlus.from(Character.from('\'').not()));
		regularExpressions.add(Character.from('\''));
		return Sequence.builder(regularExpressions);
	}
	
	/**
	 * StringPart ::= !["\\]+ | "\n"
	 */
	public static SymbolBuilder<? extends RegularExpression> getStringPart() {
		Character c1 = Character.from('"');
		Character c2 = Character.from('\\');
		CharacterClass c = CharacterClass.fromChars(c1, c2);
		Keyword newline = Keyword.from("\\n");

		return RegexAlt.builder(RegexPlus.from(c.not()), newline);
	}
	
	// "/*" (![*] | [*] !>> [/])* "*/"
	public static SymbolBuilder<? extends RegularExpression> getMultilineComment() {
		
		Keyword r1 = Keyword.from("/*");
		Character star = Character.from('*');
		Character slash = Character.from('/');

		RegularExpression r2 = RegexStar.from(Sequence.from(star, slash));
		
		Keyword r3 = Keyword.from("*/");
		
		return Sequence.builder(r1, r2, r3);
	}
	 
}
