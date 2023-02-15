/*
 * Copyright (c) 2012-2017 The ANTLR Project. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 */

package org.antlr.v4.tool.ast;

import org.antlr.v4.parse.ANTLRParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GrammarRootAST extends GrammarASTWithOptions {
	public static final Map<String, String> defaultOptions = new HashMap<String, String>();
	static {
		defaultOptions.put("language","Java");
	}

	private ANTLRParser.GrammarSpecContext root;

	public int grammarType; // LEXER, PARSER, GRAMMAR (combined)

	//TODO: TBD which item is need when two tokenVocabs in options
	public ANTLRParser.OptionValueContext tokenVocab;

	public boolean hasErrors;
	/** Track stream used to create this tree */

	public Map<String, String> cmdLineOptions; // -DsuperClass=T on command line
	public String fileName;
	private List<ANTLRParser.LexerRuleSpecContext> lexerRules = new ArrayList<>();
	private List<ANTLRParser.ParserRuleSpecContext> parserRules = new ArrayList<>();

	public List<ANTLRParser.RulerefContext> getRuleRefs() {
		return ruleRefs;
	}

	public List<ANTLRParser.TerminalContext> getTokenRefs() {
		return tokenRefs;
	}

	private List<ANTLRParser.RulerefContext> ruleRefs = new ArrayList<>();
	private List<ANTLRParser.TerminalContext> tokenRefs = new ArrayList<>();
	private List<ParserRuleContext> errorContexts;

	public GrammarRootAST(GrammarRootAST node) {
		super(node);
		this.grammarType = node.grammarType;
		this.hasErrors = node.hasErrors;
	}

	public GrammarRootAST(Token t, TokenStream tokenStream) {
		super(t);
		if (tokenStream == null) {
			throw new NullPointerException("tokenStream");
		}

	}

	public GrammarRootAST(int type, Token t, TokenStream tokenStream) {
		super(type, t);
		if (tokenStream == null) {
			throw new NullPointerException("tokenStream");
		}

	}

	public GrammarRootAST(int type, Token t, String text, TokenStream tokenStream) {
		super(type,t,text);
		if (tokenStream == null) {
			throw new NullPointerException("tokenStream");
		}

    }

	public GrammarRootAST(ANTLRParser.GrammarSpecContext r) {
		super(r);
		this.root = r;
	}

	public String getGrammarName() {
		return root.grammarDecl().identifier().getText();
	}

	@Override
	public String getOptionString(String key) {
		if ( cmdLineOptions!=null && cmdLineOptions.containsKey(key) ) {
			return cmdLineOptions.get(key);
		}
		String value = super.getOptionString(key);
		if ( value==null ) {
			value = defaultOptions.get(key);
		}
		return value;
	}

	@Override
	public Object visit(GrammarASTVisitor v) { return v.visit(this); }

	public void addParserRule(ANTLRParser.ParserRuleSpecContext rule) {
		parserRules.add(rule);
	}
	public void addLexerRule(ANTLRParser.LexerRuleSpecContext rule) {
		lexerRules.add(rule);
	}

	public List<RuleAST> getRules() {
		List<RuleAST> rules = lexerRules.stream().map(RuleAST::new).collect(Collectors.toList());
		parserRules.stream().map(RuleAST::new).forEach(rules::add);
		return rules;
	}

	public void addTokenRef(ANTLRParser.TerminalContext ctx) {
		this.tokenRefs.add(ctx);
	}

	public void addRuleRef(ANTLRParser.RulerefContext ctx) {
		this.ruleRefs.add(ctx);
	}

	public List<ParserRuleContext> getErrorContexts() {
		return this.errorContexts;
	}

	public void setErrorContexts(List<ParserRuleContext> errorContexts) {
		this.errorContexts = errorContexts;
	}

//	@Override
//	public GrammarRootAST dupNode() { return new GrammarRootAST(this); }
}
