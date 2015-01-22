package org.jgll.regex;

import java.util.ArrayList;
import java.util.List;

import org.jgll.grammar.symbol.Character;
import org.jgll.grammar.symbol.CharacterRange;
import org.jgll.grammar.symbol.SymbolBuilder;

public class RegularExpressionExamples {

	/**
	 * Id ::= [a-zA-Z][a-zA-Z0-9]*
	 */
	public static SymbolBuilder<? extends RegularExpression> getId() {
		Alt<CharacterRange> c1 = Alt.from(CharacterRange.in('a', 'z'), CharacterRange.in('A', 'Z'));
		Alt<CharacterRange> c2 = Alt.from(CharacterRange.in('a', 'z'), CharacterRange.in('A', 'Z'), CharacterRange.in('0', '9'));
		return Sequence.builder(c1, Star.from(c2));
	}
	
	/**
	 * Float ::= [0-9]+[.][0-9]+
	 */
	public static SymbolBuilder<? extends RegularExpression> getFloat() {
		Alt<CharacterRange> c = Alt.from(CharacterRange.in('0', '9'));
		return Sequence.builder(Plus.from(c), Character.from('.'), Plus.from(c));
	}
	
	/**
	 * UnicodeEscape ::= "\\" [u]+ [0-9 A-F a-f] [0-9 A-F a-f] [0-9 A-F a-f] [0-9 A-F a-f];
	 */
	public static SymbolBuilder<? extends RegularExpression> getJavaUnicodeEscape() {
		List<RegularExpression> regularExpressions = new ArrayList<>();

		regularExpressions.add(Sequence.from("\\"));
		
		regularExpressions.add(Plus.from(Character.from('u')));
		
		Alt<CharacterRange> c = Alt.from(CharacterRange.in('0', '9'), CharacterRange.in('a', 'z'), CharacterRange.in('A', 'Z'));
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
		regularExpressions.add(Plus.from(Alt.not(Character.from('\''))));
		regularExpressions.add(Character.from('\''));
		return Sequence.builder(regularExpressions);
	}
	
	/**
	 * StringPart ::= !["\\]+ | "\n"
	 */
	public static SymbolBuilder<? extends RegularExpression> getStringPart() {
		Character c1 = Character.from('"');
		Character c2 = Character.from('\\');
		Alt<Character> c = Alt.from(c1, c2);
		Sequence<Character> newline = Sequence.from("\\n");

		return Alt.builder(Plus.from(Alt.not(c1, c2)), newline);
	}
	
	// "/*" (![*] | [*] !>> [/])* "*/"
	public static SymbolBuilder<? extends RegularExpression> getMultilineComment() {
		
		Sequence<Character> r1 = Sequence.from("/*");
		Character star = Character.from('*');
		Character slash = Character.from('/');

		RegularExpression r2 = Star.from(Sequence.from(star, slash));
		
		Sequence<Character> r3 = Sequence.from("*/");
		
		return Sequence.builder(r1, r2, r3);
	}
	 
}
