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
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrammarRootAST {
	public static final Map<String, String> defaultOptions = new HashMap<String, String>();
	static {
		defaultOptions.put("language","Java");
	}

	public ANTLRParser.GrammarSpecContext root;

	public int grammarType; // LEXER, PARSER, GRAMMAR (combined)

	//TODO: TBD which item is need when two tokenVocabs in options
	public ANTLRParser.OptionValueContext tokenVocab;

	public boolean hasErrors;
	/** Track stream used to create this tree */

	public Map<String, String> cmdLineOptions; // -DsuperClass=T on command line
	public String fileName;

	private List<ParseTree> rules = new ArrayList<>();
	private List<ANTLRParser.LexerRuleSpecContext> lexerRules = new ArrayList<>();
	private List<ANTLRParser.ParserRuleSpecContext> parserRules = new ArrayList<>();
	private List<ANTLRParser.LexerCommandContext> lexerCommands = new ArrayList<>();

	private List<ANTLRParser.OptionsSpecContext> optionsSpecs = new ArrayList<>();

	private List<ANTLRParser.OptionsSpecContext> globalOptionsSpecs = new ArrayList<>();

	private Map<String, String> options = new HashMap<>();
	private List<ANTLRParser.DelegateGrammarsContext> importSpecs = new ArrayList<>();
	private List<ANTLRParser.TokensSpecContext> tokensSpecs = new ArrayList<>();
	private Map<TerminalNode,List<ANTLRParser.LexerRuleSpecContext>> modes = new HashMap<>();
	private List<ANTLRParser.ChannelsSpecContext> channels = new ArrayList<>();
	private List<ANTLRParser.ElementOptionContext> elementOptions = new ArrayList<>();
	private List<ANTLRParser.ActionBlockContext> actions = new ArrayList<>();

	private List<ANTLRParser.LabeledElementContext> labeledElements = new ArrayList<>();

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
		this.grammarType = node.grammarType;
		this.hasErrors = node.hasErrors;
	}

	public GrammarRootAST(Token t, TokenStream tokenStream) {
		if (tokenStream == null) {
			throw new NullPointerException("tokenStream");
		}

	}

	public GrammarRootAST(int type, Token t, TokenStream tokenStream) {
		if (tokenStream == null) {
			throw new NullPointerException("tokenStream");
		}

	}

	public GrammarRootAST(int type, Token t, String text, TokenStream tokenStream) {
		if (tokenStream == null) {
			throw new NullPointerException("tokenStream");
		}

    }

	public GrammarRootAST(ANTLRParser.GrammarSpecContext r) {
		this.root = r;
	}

	public String getGrammarName() {
		return getGrammarNameToken().getText();
	}

	public Token getGrammarNameToken() {
		ANTLRParser.IdentifierContext identifier = root.grammarDecl().identifier();
		TerminalNode ruleRef = identifier.RULE_REF();
		TerminalNode terminalNode = ruleRef != null ? ruleRef : identifier.TOKEN_REF();
		return terminalNode.getSymbol();
	}

	public String getOptionString(String key) {
		if ( cmdLineOptions!=null && cmdLineOptions.containsKey(key) ) {
			return cmdLineOptions.get(key);
		}
		String value = getGrammarOptionString(key);
		if ( value==null ) {
			value = defaultOptions.get(key);
		}
		return value;
	}

	private String getGrammarOptionString(String key) {
		return this.options.get(key);
	}

	public void addParserRule(ANTLRParser.ParserRuleSpecContext rule) {
		parserRules.add(rule);
		rules.add(rule);
	}
	public void addLexerRule(TerminalNode currentModeToken, ANTLRParser.LexerRuleSpecContext rule) {
		lexerRules.add(rule);
		rules.add(rule);
		if (currentModeToken != null) {
			if (rule.FRAGMENT() != null) {
				modes.get(currentModeToken).add(rule);
			}
		}
	}

	public List<ParseTree> getRules() {
		return rules;
	}

	public List<ANTLRParser.LexerRuleSpecContext> getLexerRules() {
		return lexerRules;
	}

	public List<ANTLRParser.ParserRuleSpecContext> getParserRules() {
		return parserRules;
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

	public void addLexerCommand(ANTLRParser.LexerCommandContext ctx) {
		this.lexerCommands.add(ctx);
	}

	public List<ANTLRParser.LexerCommandContext> getLexerCommands() {
		return this.lexerCommands;
	}

	public Token getGrammarId() {
		return null;
	}

	public List<ANTLRParser.OptionsSpecContext> getOptionsSpecs() {
		return optionsSpecs;
	}

	public List<ANTLRParser.DelegateGrammarsContext> getImportsSpecs() {
		return importSpecs;
	}

	public List<ANTLRParser.TokensSpecContext> getTokensSpecs() {
		return tokensSpecs;
	}

	public List<ANTLRParser.OptionsSpecContext> getGlobalOptionsSpecs() {
		return globalOptionsSpecs;
	}

	public void addOptionsSpec(ANTLRParser.OptionsSpecContext ctx) {
		this.optionsSpecs.add(ctx);
		ParserRuleContext parentNode = ctx.getParent();
		if (parentNode instanceof ANTLRParser.PrequelConstructContext) {
			this.globalOptionsSpecs.add(ctx);
			for (ANTLRParser.OptionContext option: ctx.option()) {
				options.put(option.identifier().getText(), option.optionValue().getText());
			}
		}
	}

	public void addTokensSpec(ANTLRParser.TokensSpecContext ctx) {
		this.tokensSpecs.add(ctx);
	}

	public void addMode(TerminalNode currentModeName) {
		this.modes.put(currentModeName, new ArrayList<>());
	}

	public Map<TerminalNode, List<ANTLRParser.LexerRuleSpecContext>> getModes() {
		return modes;
	}

	public void addChannel(ANTLRParser.ChannelsSpecContext ctx) {
		this.channels.add(ctx);
	}

	public List<ANTLRParser.ChannelsSpecContext> getChannels() {
		return channels;
	}

	public List<ANTLRParser.ElementOptionContext> getElementOptions() {
		return elementOptions;
	}

	public void addElementOption(ANTLRParser.ElementOptionContext ctx) {
		this.elementOptions.add(ctx);
	}

	public List<ANTLRParser.ActionBlockContext> getActions() {
		return actions;
	}

	public void addActions(ANTLRParser.ActionBlockContext ctx) {
		this.actions.add(ctx);
	}

	public void addLabeledElement(ANTLRParser.LabeledElementContext ctx) {
		this.labeledElements.add(ctx);
	}


	public List<ANTLRParser.LabeledElementContext> getLabeledElements() {
		return labeledElements;
	}
	//	@Override
//	public GrammarRootAST dupNode() { return new GrammarRootAST(this); }
}
