/*
 * Copyright (c) 2015, Ali Afroozeh and Anastasia Izmaylova, Centrum Wiskunde & Informatica (CWI)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this
 *    list of conditions and the following disclaimer in the documentation and/or
 *    other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 *
 */
package org.iguana.grammar.iggy;

import org.iguana.datadependent.ast.AST;
import org.iguana.grammar.symbol.*;
import org.iguana.regex.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Anastasia Izmaylova
 */
public class GrammarBuilder {

    public static class Identifier {
        public final String id;
        public Identifier(String id) {
            this.id = id;
        }
        public static Identifier id(String name) {
            return new Identifier(name);
        }
    }

    public static Identifier identifier() { return new Identifier(null); }
	
	public static class Rule {
		public static List<org.iguana.grammar.symbol.Rule> syntax(List<String> tag, Identifier name, List<Identifier> parameters, List<Alternates> body) {
            List<org.iguana.grammar.symbol.Rule> rules = new ArrayList<>();
            body.forEach(group -> { // TODO: integrate precedence logic
                group.alternates.forEach(alternate -> {
                    if (alternate.rest != null) { // Associativity group
                        {   // Note: Do not move this block!
                            org.iguana.grammar.symbol.Rule.Builder builder = org.iguana.grammar.symbol.Rule.withHead(Nonterminal.withName(name.id));
                            List<org.iguana.grammar.symbol.Symbol> symbols = new ArrayList<>();
                            symbols.add(alternate.first.first);
                            if (alternate.first.rest != null)
                                symbols.addAll(alternate.first.rest);
                            symbols.addAll(alternate.first.ret);
                            rules.add(builder.addSymbols().build());
                        }
                        alternate.rest.forEach(sequence -> {
                            org.iguana.grammar.symbol.Rule.Builder builder = org.iguana.grammar.symbol.Rule.withHead(Nonterminal.withName(name.id));
                            List<org.iguana.grammar.symbol.Symbol> symbols = new ArrayList<>();
                            symbols.add(sequence.first);
                            if (sequence.rest != null)
                                symbols.addAll(sequence.rest);
                            symbols.addAll(sequence.ret);
                            rules.add(builder.addSymbols(symbols).build());
                        });
                    } else {
                        org.iguana.grammar.symbol.Rule.Builder builder = org.iguana.grammar.symbol.Rule.withHead(Nonterminal.withName(name.id));
                        List<org.iguana.grammar.symbol.Symbol> symbols = new ArrayList<>();
                        symbols.add(alternate.first.first);
                        if (alternate.first.rest != null)
                            symbols.addAll(alternate.first.rest);
                        symbols.addAll(alternate.first.ret);
                        rules.add(builder.addSymbols().build());
                    }
                });
            });
			return rules;
		}
		public static List<org.iguana.grammar.symbol.Rule> regex() {
			return null;
		}
        public static List<org.iguana.grammar.symbol.Rule> regexs() {
            return null;
        }
	}
	
	public static Rule rule() { return new Rule(); }

    public static class Alternates {
        public final List<Alternate> alternates;
        public Alternates(List<Alternate> alternates) {
            this.alternates = alternates;
        }

        public static Alternates prec(List<Alternate> alternates) { return new Alternates(alternates); }
    }

    public static Alternates alternates() { return new Alternates(null); }

    public static class Alternate {

        public final Sequence first;
        public final List<Sequence> rest;
        public final String associativity;

        public Alternate(Sequence sequence) {
            this.first = sequence;
            this.rest = null;
            this.associativity = null;
        }
        public Alternate(Sequence sequence, List<Sequence> sequences, String associativity) {
            this.first = sequence;
            this.rest = sequences;
            this.associativity = associativity;
        }

        public static Alternate sequence(Sequence sequence) { return new Alternate(sequence); }
        public static Alternate assoc(Sequence sequence, List<Sequence> sequences, String associativity) {
            return new Alternate(sequence, sequences, associativity);
        }
    }

    public static Alternate alternate() { return new Alternate(null); }

    public static class Sequence {

        public final org.iguana.grammar.symbol.Symbol first;
        public final List<org.iguana.grammar.symbol.Symbol> rest;
        public final List<org.iguana.grammar.symbol.Symbol> ret;
        public final List<Attribute> attributes;
        public final List<String> label;

        public Sequence(org.iguana.grammar.symbol.Symbol symbol, List<org.iguana.grammar.symbol.Symbol> ret, List<String> label) {
            this.first = symbol;
            this.rest = null;
            this.ret = ret;
            this.attributes = null;
            this.label = label;
        }

        public Sequence(org.iguana.grammar.symbol.Symbol symbol, List<org.iguana.grammar.symbol.Symbol> symbols, List<org.iguana.grammar.symbol.Symbol> ret, List<Attribute> attributes) {
            this.first = symbol;
            this.rest = symbols;
            this.ret = ret;
            this.attributes = attributes;
            this.label = null;
        }

        public static Sequence single(org.iguana.grammar.symbol.Symbol symbol, List<org.iguana.grammar.symbol.Symbol> ret, List<String> label) {
            return new Sequence(symbol, ret, label);
        }

        public static Sequence morethantwo(org.iguana.grammar.symbol.Symbol symbol, List<org.iguana.grammar.symbol.Symbol> symbols, List<org.iguana.grammar.symbol.Symbol> ret, List<Attribute> attributes) {
            return new Sequence(symbol, symbols, ret, attributes);
        }

    }

    public static Sequence sequence() { return new Sequence(null, null, null); }

    public static class Attribute {
        public final String attribute;
        public Attribute(String attribute) {
            this.attribute = attribute;
        }

        public static Attribute assoc(String associativity) { return new Attribute(associativity); }
        public static Attribute label(Identifier label) { return new Attribute(label.id); }
    }

    public static Attribute attribute() { return new Attribute(null); }

    public static class Symbols {
        public static org.iguana.regex.Sequence<org.iguana.grammar.symbol.Symbol> sequence(List<org.iguana.grammar.symbol.Symbol> symbols) { return org.iguana.regex.Sequence.from(symbols); }
    }

    public static Symbols symbols() { return new Symbols(); }

    public static class Symbol {

        public static org.iguana.grammar.symbol.Symbol star(org.iguana.grammar.symbol.Symbol symbol) {
            return Star.from(symbol);
        }
        public static org.iguana.grammar.symbol.Symbol plus(org.iguana.grammar.symbol.Symbol symbol) {
            return Plus.from(symbol);
        }
        public static org.iguana.grammar.symbol.Symbol option(org.iguana.grammar.symbol.Symbol symbol) {
            return Opt.from(symbol);
        }
        public static org.iguana.grammar.symbol.Symbol sequence(org.iguana.grammar.symbol.Symbol symbol, List<org.iguana.grammar.symbol.Symbol> symbols) {
            List<org.iguana.grammar.symbol.Symbol> l = new ArrayList<>();
            l.add(symbol);
            l.addAll(symbols);
            return org.iguana.regex.Sequence.from(l);
        }
        public static org.iguana.grammar.symbol.Symbol alternation(org.iguana.regex.Sequence<org.iguana.grammar.symbol.Symbol> sequence, List<org.iguana.regex.Sequence<org.iguana.grammar.symbol.Symbol>> sequences) {
            List<org.iguana.grammar.symbol.Symbol> l = new ArrayList<>();
            if (sequence.getSymbols().isEmpty())
                l.add(Epsilon.getInstance());
            else if (sequence.getSymbols().size() == 1)
                l.add(sequence.getSymbols().get(0));
            else
                l.add(sequence);
            sequences.forEach(s -> {
                if (s.getSymbols().isEmpty())
                    l.add(Epsilon.getInstance());
                else if (s.getSymbols().size() == 1)
                    l.add(s.getSymbols().get(0));
                else
                    l.add(s);
            });
            return Alt.from(l);
        }
        public static org.iguana.grammar.symbol.Symbol call(org.iguana.grammar.symbol.Symbol symbol, List<org.iguana.datadependent.ast.Expression> arguments) {
            Nonterminal nt = (Nonterminal) symbol;
            return nt.copyBuilder().apply(arguments.stream().toArray(org.iguana.datadependent.ast.Expression[]::new)).build();
        }
        public static org.iguana.grammar.symbol.Symbol align(org.iguana.grammar.symbol.Symbol symbol) {
            return Align.align(symbol);
        }
        public static org.iguana.grammar.symbol.Symbol offside(org.iguana.grammar.symbol.Symbol symbol) {
            return Offside.offside(symbol);
        }
        public static org.iguana.grammar.symbol.Symbol ignore(org.iguana.grammar.symbol.Symbol symbol) {
            return Ignore.ignore(symbol);
        }
        public static org.iguana.grammar.symbol.Symbol conditional(org.iguana.datadependent.ast.Expression expression, org.iguana.grammar.symbol.Symbol thenPart, org.iguana.grammar.symbol.Symbol elsePart) {
            return IfThenElse.ifThenElse(expression, thenPart, elsePart);
        }
        public static org.iguana.grammar.symbol.Symbol variable(Identifier name, org.iguana.grammar.symbol.Symbol symbol) {
            Nonterminal nt = (Nonterminal) symbol;
            return nt.copyBuilder().setVariable(name.id).build();
        }
        public static org.iguana.grammar.symbol.Symbol labeled(Identifier name, org.iguana.grammar.symbol.Symbol symbol) {
            return symbol.copyBuilder().setLabel(name.id).build();
        }
        public static org.iguana.grammar.symbol.Symbol contraints(List<org.iguana.datadependent.ast.Expression> expressions) {
            return null;
        }
        public static org.iguana.grammar.symbol.Symbol bindings(List<org.iguana.datadependent.ast.Statement> bindings) {
            return null;
        }
        public static org.iguana.grammar.symbol.Symbol precede(org.iguana.grammar.symbol.Symbol symbol, RegularExpression regex) {
            return null;
        }
        public static org.iguana.grammar.symbol.Symbol notprecede(org.iguana.grammar.symbol.Symbol symbol, RegularExpression regex) {
            return null;
        }
        public static org.iguana.grammar.symbol.Symbol follow(org.iguana.grammar.symbol.Symbol symbol, RegularExpression regex) {
            return null;
        }
        public static org.iguana.grammar.symbol.Symbol nonfollow(org.iguana.grammar.symbol.Symbol symbol, RegularExpression regex) {
            return null;
        }
        public static org.iguana.grammar.symbol.Symbol exclude(org.iguana.grammar.symbol.Symbol symbol, RegularExpression regex) {
            return null;
        }
        public static org.iguana.grammar.symbol.Symbol nont(Identifier name) {
            return Nonterminal.withName(name.id);
        }
        public static org.iguana.grammar.symbol.Symbol string(String s) {
            return Terminal.from(org.iguana.regex.Sequence.from(s.substring(1,s.length() - 1).chars().toArray()));
        }
        public static org.iguana.grammar.symbol.Symbol character(String s) {
            return Terminal.from(org.iguana.regex.Sequence.from(s.substring(1,s.length() - 1).chars().toArray()));
        }
    }

    public static Symbol symbol() { return new Symbol(); }

    public static class Expression {
        public static org.iguana.datadependent.ast.Expression call(org.iguana.datadependent.ast.Expression expression, List<org.iguana.datadependent.ast.Expression> arguments) {
            return null;
        }
        public static org.iguana.datadependent.ast.Expression multiplication(org.iguana.datadependent.ast.Expression lhs, org.iguana.datadependent.ast.Expression rhs) {
            return null;
        }
        public static org.iguana.datadependent.ast.Expression division(org.iguana.datadependent.ast.Expression lhs, org.iguana.datadependent.ast.Expression rhs) {
            return null;
        }
        public static org.iguana.datadependent.ast.Expression plus(org.iguana.datadependent.ast.Expression lhs, org.iguana.datadependent.ast.Expression rhs) {
            return null;
        }
        public static org.iguana.datadependent.ast.Expression minus(org.iguana.datadependent.ast.Expression lhs, org.iguana.datadependent.ast.Expression rhs) {
            return null;
        }
        public static org.iguana.datadependent.ast.Expression greatereq(org.iguana.datadependent.ast.Expression lhs, org.iguana.datadependent.ast.Expression rhs) {
            return AST.greaterEq(lhs, rhs);
        }
        public static org.iguana.datadependent.ast.Expression lesseq(org.iguana.datadependent.ast.Expression lhs, org.iguana.datadependent.ast.Expression rhs) {
            return AST.lessEq(lhs, rhs);
        }
        public static org.iguana.datadependent.ast.Expression greater(org.iguana.datadependent.ast.Expression lhs, org.iguana.datadependent.ast.Expression rhs) {
            return AST.greater(lhs, rhs);
        }
        public static org.iguana.datadependent.ast.Expression less(org.iguana.datadependent.ast.Expression lhs, org.iguana.datadependent.ast.Expression rhs) {
            return AST.less(lhs, rhs);
        }
        public static org.iguana.datadependent.ast.Expression equal(org.iguana.datadependent.ast.Expression lhs, org.iguana.datadependent.ast.Expression rhs) {
            return AST.equal(lhs, rhs);
        }
        public static org.iguana.datadependent.ast.Expression notequal(org.iguana.datadependent.ast.Expression lhs, org.iguana.datadependent.ast.Expression rhs) {
            return AST.notEqual(lhs, rhs);
        }
        public static org.iguana.datadependent.ast.Expression and(org.iguana.datadependent.ast.Expression lhs, org.iguana.datadependent.ast.Expression rhs) {
            return AST.and(lhs, rhs);
        }
        public static org.iguana.datadependent.ast.Expression or(org.iguana.datadependent.ast.Expression lhs, org.iguana.datadependent.ast.Expression rhs) {
            return AST.or(lhs, rhs);
        }
        public static org.iguana.datadependent.ast.Expression lextent(Identifier name) {
            return AST.lExt(name.id);
        }
        public static org.iguana.datadependent.ast.Expression rextent(Identifier name) {
            return AST.rExt(name.id);
        }
        public static org.iguana.datadependent.ast.Expression yield(Identifier name) {
            return AST.yield(name.id);
        }
        public static org.iguana.datadependent.ast.Expression name(Identifier name) {
            return AST.var(name.id);
        }
        public static org.iguana.datadependent.ast.Expression number(String n) {
            return AST.integer(Integer.valueOf(n));
        }
    }

    public static Expression expression() { return new Expression(); }

    public static class Regex {

    }

    public static Regex regex() { return new Regex(); }

    public static class Binding {
        public static org.iguana.datadependent.ast.Statement assign(Identifier name, org.iguana.datadependent.ast.Expression expression) {
            return AST.stat(AST.assign(name.id, expression));
        }
        public static org.iguana.datadependent.ast.Statement declare(Identifier name, org.iguana.datadependent.ast.Expression expression) {
            return AST.varDeclStat(name.id, expression);
        }
    }

    public static Binding binding() { return new Binding(); }

}
