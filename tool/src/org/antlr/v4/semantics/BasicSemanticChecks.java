/*
 * Copyright (c) 2012-2017 The ANTLR Project. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 */

package org.antlr.v4.semantics;

import org.antlr.v4.misc.Utils;
import org.antlr.v4.parse.ANTLRParser;
import org.antlr.v4.parse.GrammarTreeVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.Trees;
import org.antlr.v4.tool.ErrorManager;
import org.antlr.v4.tool.ErrorType;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.Rule;
import org.antlr.v4.tool.ast.GrammarRootAST;
import org.stringtemplate.v4.misc.MultiMap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.antlr.v4.parse.ANTLRParser.LexerRuleSpecContext;

/** No side-effects except for setting options into the appropriate node.
 *  TODO:  make the side effects into a separate pass this
 *
 * Invokes check rules for these:
 *
 * FILE_AND_GRAMMAR_NAME_DIFFER
 * LEXER_RULES_NOT_ALLOWED
 * PARSER_RULES_NOT_ALLOWED
 * CANNOT_ALIAS_TOKENS
 * ARGS_ON_TOKEN_REF
 * ILLEGAL_OPTION
 * REWRITE_OR_OP_WITH_NO_OUTPUT_OPTION
 * NO_RULES
 * REWRITE_FOR_MULTI_ELEMENT_ALT
 * HETERO_ILLEGAL_IN_REWRITE_ALT
 * AST_OP_WITH_NON_AST_OUTPUT_OPTION
 * AST_OP_IN_ALT_WITH_REWRITE
 * CONFLICTING_OPTION_IN_TREE_FILTER
 * WILDCARD_AS_ROOT
 * INVALID_IMPORT
 * TOKEN_VOCAB_IN_DELEGATE
 * IMPORT_NAME_CLASH
 * REPEATED_PREQUEL
 * TOKEN_NAMES_MUST_START_UPPER
 */
public class BasicSemanticChecks extends GrammarTreeVisitor {
	/** Set of valid imports.  Maps delegate to set of delegator grammar types.
	 *  validDelegations.get(LEXER) gives list of the kinds of delegators
	 *  that can import lexers.
	 */
	public final static MultiMap<Integer,Integer> validImportTypes =
		new MultiMap<Integer,Integer>() {
			{
				map(ANTLRParser.LEXER, ANTLRParser.LEXER);
				map(ANTLRParser.LEXER, ANTLRParser.COMBINED);

				map(ANTLRParser.PARSER, ANTLRParser.PARSER);
				map(ANTLRParser.PARSER, ANTLRParser.COMBINED);

				map(ANTLRParser.COMBINED, ANTLRParser.COMBINED);
			}
		};

	public Grammar g;
	public RuleCollector ruleCollector;
	public ErrorManager errMgr;

	/**
	 * When this is {@code true}, the semantic checks will report
	 * {@link ErrorType#UNRECOGNIZED_ASSOC_OPTION} where appropriate. This may
	 * be set to {@code false} to disable this specific check.
	 *
	 * <p>The default value is {@code true}.</p>
	 */
	public boolean checkAssocElementOption = true;


	/**
	 * Value of caseInsensitive option (false if not defined)
	 */
	private boolean grammarCaseInsensitive = false;

	public BasicSemanticChecks(Grammar g, RuleCollector ruleCollector) {
		this.g = g;
		this.ruleCollector = ruleCollector;
		this.errMgr = g.tool.errMgr;
	}

	@Override
	public ErrorManager getErrorManager() { return errMgr; }

	public void process() {
		g.ast.getLexerCommands().forEach(this::checkLexerCommand);
		checkGrammarName(g.ast.getGrammarNameToken());
		finishPrequels(g.ast);
		g.ast.getModes().forEach((modeNameNode, tokenRules) -> {
			checkModeRulesNotEmpty(modeNameNode, tokenRules);
			checkModeNotInLexer(modeNameNode);
		});
		g.ast.getLexerRules().forEach(it -> {
			checkInvalidRuleDef(it.TOKEN_REF().getSymbol());
			defineToken(it.TOKEN_REF());
		});
		g.ast.getParserRules().forEach(it -> {
			checkInvalidRuleDef(it.RULE_REF().getSymbol());
			finishRule(it, it.RULE_REF(), it.ruleBlock());
		});
		g.ast.getTokenRefs().forEach(this::enterTerminal1);
		g.ast.getRuleRefs().forEach(it -> ruleRef(it.RULE_REF()));
		g.ast.getOptionsSpecs().forEach(this::checkOptions);
		g.ast.getChannels().forEach(it -> {
			TerminalNode channelToken = it.CHANNELS();
			enterChannelsSpec(channelToken);
			defineChannel(channelToken);
		});
		g.ast.getActions().forEach(this::actionInAlt);
		g.ast.getElementOptions().forEach(it -> elementOption(it.getParent(), it.identifier(0), it.identifier(1)));
		g.ast.getLabeledElements().forEach(this::label);

		discoverRules(g.ast.getRules());
	}

	private void checkOptions(ANTLRParser.OptionsSpecContext options) {
		options.option().forEach(it -> {
			if (Utils.getParent(options, ANTLRParser.LexerRuleSpecContext.class) != null) {
				ruleOption(Utils.getFirstTokenNode(it.identifier()), Utils.getFirstTokenNode(it.optionValue()));
			} else if (Utils.getParent(options, ANTLRParser.LexerRuleSpecContext.class) != null) {
				blockOption(Utils.getFirstTokenNode(it.identifier()), Utils.getFirstTokenNode(it.optionValue()));
			} else {
				grammarOption(Utils.getFirstTokenNode(it.identifier()), Utils.getFirstTokenNode(it.optionValue()));
			}
		});
	}

	// Routines to route visitor traffic to the checking routines

	public void finishPrequels(GrammarRootAST grammar) {
		List<ANTLRParser.OptionsSpecContext> options = grammar.getOptionsSpecs();
		List<ANTLRParser.DelegateGrammarsContext> imports = grammar.getImportsSpecs();
		imports.forEach(this::importGrammar);
		List<ANTLRParser.TokensSpecContext> tokens = grammar.getTokensSpecs();
		checkNumPrequels(options, imports, tokens);
	}

	public void importGrammar(ANTLRParser.DelegateGrammarsContext importRules) {
		for (ANTLRParser.DelegateGrammarContext importRule : importRules.delegateGrammar()) {
			TerminalNode id = Utils.getFirstTokenNode(importRule);
			checkImport(id.getSymbol());
		}
	}

	public void discoverRules(List<ParseTree> rules) {
		checkNumRules(rules);
	}

	protected void checkModeRulesNotEmpty(TerminalNode modeNameNode, List<ANTLRParser.LexerRuleSpecContext> tokenRules) {
		if (tokenRules.size() == 0) {
			String name = modeNameNode.getText();

			g.tool.errMgr.grammarError(ErrorType.MODE_WITHOUT_RULES, g.fileName, modeNameNode.getSymbol(), name, g);
		}
	}

	public void checkModeNotInLexer(TerminalNode modeNode) {
		if ( !g.isLexer() ) {
			g.tool.errMgr.grammarError(ErrorType.MODE_NOT_IN_LEXER, g.fileName,
									   modeNode.getSymbol(), modeNode.getText(), g);
		}
	}

	public void ruleRef(TerminalNode ref) {
		checkInvalidRuleRef(ref);
	}

	public void grammarOption(TerminalNode ID, TerminalNode valueAST) {
		checkOptions(ANTLRParser.GRAMMAR, ID.getSymbol(), valueAST);
	}

	public void ruleOption(TerminalNode ID, TerminalNode valueAST) {
		checkOptions(ANTLRParser.RULE, ID.getSymbol(), valueAST);
	}

	public void blockOption(TerminalNode ID, TerminalNode valueAST) {
		checkOptions(ANTLRParser.BLOCK, ID.getSymbol(), valueAST);
	}

	public void defineToken(TerminalNode ID) {
		checkTokenDefinition(ID.getSymbol());
	}

	protected void enterChannelsSpec(TerminalNode tree) {
		ErrorType errorType = g.isParser()
				? ErrorType.CHANNELS_BLOCK_IN_PARSER_GRAMMAR
				: g.isCombined()
				? ErrorType.CHANNELS_BLOCK_IN_COMBINED_GRAMMAR
				: null;
		if (errorType != null) {
			g.tool.errMgr.grammarError(errorType, g.fileName, tree.getSymbol());
		}
	}

	public void defineChannel(TerminalNode ID) {
		checkChannelDefinition(ID.getSymbol());
	}

	public void elementOption(ParseTree elem, ParserRuleContext ID, ParserRuleContext valueAST) {
		checkElementOptions(elem, Utils.getFirstTokenNode(ID), valueAST);
	}

	public void finishRule(ANTLRParser.ParserRuleSpecContext rule, TerminalNode ID, ANTLRParser.RuleBlockContext block) {
		TerminalNode ruleIdNode = rule.RULE_REF();
		String ruleName = ruleIdNode.getText();
		ANTLRParser.RuleAltListContext blk = rule.ruleBlock().ruleAltList();//TODO:
		int nalts = blk.getChildCount();
//		GrammarAST idAST = rule.getChild(0);
		for (int i=0; i< nalts; i++) {
			ANTLRParser.LabeledAltContext altAST = blk.labeledAlt(i);
			if ( altAST.identifier()!=null ) {
				String altLabel = altAST.identifier().getText();
				// first check that label doesn't conflict with a rule
				// label X or x can't be rule x.
				Rule r = ruleCollector.rules.get(Utils.decapitalize(altLabel));
				TerminalNode labelNode = Utils.getFirstTokenNode(altAST.identifier());
				if ( r!=null ) {
					g.tool.errMgr.grammarError(ErrorType.ALT_LABEL_CONFLICTS_WITH_RULE,
											   g.fileName, labelNode.getSymbol(),
											   altLabel,
											   r.name);
				}
				// Now verify that label X or x doesn't conflict with label
				// in another rule. altLabelToRuleName has both X and x mapped.
				String prevRuleForLabel = ruleCollector.altLabelToRuleName.get(altLabel);
				if (prevRuleForLabel != null) {
					if (!prevRuleForLabel.equals(ruleName)) {
						g.tool.errMgr.grammarError(ErrorType.ALT_LABEL_REDEF,
								g.fileName, labelNode.getSymbol(),
								altLabel,
								ruleName,
								prevRuleForLabel);
					}
				}
			}
		}
		List<ANTLRParser.LabeledElementContext> altLabels = ruleCollector.ruleToAltLabels.get(ruleName);
		int numAltLabels = 0;
		if ( altLabels!=null ) numAltLabels = altLabels.size();
		if ( numAltLabels>0 && nalts != numAltLabels ) {
			g.tool.errMgr.grammarError(ErrorType.RULE_WITH_TOO_FEW_ALT_LABELS,
									   g.fileName, ruleIdNode.getSymbol(), ruleName);
		}
	}

	// Routines to do the actual work of checking issues with a grammar.
	// They are triggered by the visitor methods above.

	void checkGrammarName(Token nameToken) {
		String fullyQualifiedName = nameToken.getInputStream().getSourceName();
		if (fullyQualifiedName == null) {
			// This wasn't read from a file.
			return;
		}

		File f = new File(fullyQualifiedName);
		String fileName = f.getName();
		if ( g.originalGrammar!=null ) return; // don't warn about diff if this is implicit lexer
		if ( !Utils.stripFileExtension(fileName).equals(nameToken.getText()) &&
		     !fileName.equals(Grammar.GRAMMAR_FROM_STRING_NAME)) {
			g.tool.errMgr.grammarError(ErrorType.FILE_AND_GRAMMAR_NAME_DIFFER,
									   fileName, nameToken, nameToken.getText(), fileName);
		}
	}

	void checkNumRules(List<ParseTree> rulesNode) {
		if ( rulesNode.size()==0 ) {
			g.tool.errMgr.grammarError(ErrorType.NO_RULES, g.fileName,
					null, g.ast.getGrammarName(), g);
		}
	}

	void checkNumPrequels(List<ANTLRParser.OptionsSpecContext> options,
						  List<ANTLRParser.DelegateGrammarsContext> imports,
						  List<ANTLRParser.TokensSpecContext> tokens)
	{
		List<Token> secondOptionTokens = new ArrayList<Token>();
		if ( options!=null && options.size()>1 ) {
			secondOptionTokens.add(options.get(1).OPTIONS().getSymbol());
		}
		if ( imports!=null && imports.size()>1 ) {
			secondOptionTokens.add(imports.get(1).IMPORT().getSymbol());
		}
		if ( tokens!=null && tokens.size()>1 ) {
			secondOptionTokens.add(tokens.get(1).TOKENS().getSymbol());
		}
		for (Token t : secondOptionTokens) {
			String fileName = t.getInputStream().getSourceName();
			g.tool.errMgr.grammarError(ErrorType.REPEATED_PREQUEL,
									   fileName, t);
		}
	}

	void checkInvalidRuleDef(Token ruleID) {
		String fileName = null;
		if ( ruleID.getInputStream()!=null ) {
			fileName = ruleID.getInputStream().getSourceName();
		}
		if ( g.isLexer() && Character.isLowerCase(ruleID.getText().charAt(0)) ) {
			g.tool.errMgr.grammarError(ErrorType.PARSER_RULES_NOT_ALLOWED,
									   fileName, ruleID, ruleID.getText());
		}
		if ( g.isParser() &&
			Grammar.isTokenName(ruleID.getText()) )
		{
			g.tool.errMgr.grammarError(ErrorType.LEXER_RULES_NOT_ALLOWED,
									   fileName, ruleID, ruleID.getText());
		}
	}

	void checkInvalidRuleRef(TerminalNode ruleID) {
		ANTLRParser.ParserRuleSpecContext ruleNode = Utils.getParent(ruleID, ANTLRParser.ParserRuleSpecContext.class);
		String fileName = ruleID.getSymbol().getInputStream().getSourceName();
		if ( g.isLexer() && Character.isLowerCase(ruleID.getText().charAt(0)) ) {
			g.tool.errMgr.grammarError(ErrorType.PARSER_RULE_REF_IN_LEXER_RULE,
									   fileName, ruleID.getSymbol(), ruleID.getText(), ruleNode.RULE_REF().getText());
		}
	}

	void checkTokenDefinition(Token tokenID) {
		String fileName = tokenID.getInputStream().getSourceName();
		if ( !Grammar.isTokenName(tokenID.getText()) ) {
			g.tool.errMgr.grammarError(ErrorType.TOKEN_NAMES_MUST_START_UPPER,
									   fileName,
									   tokenID,
									   tokenID.getText());
		}
	}

	void checkChannelDefinition(Token tokenID) {
	}

	protected void checkLexerCommand(ANTLRParser.LexerCommandContext tree) {
		checkElementIsOuterMostInSingleAlt(tree);
		LexerRuleSpecContext rule = Utils.getParent(tree, LexerRuleSpecContext.class);

		if (rule.FRAGMENT() != null) {
			String fileName = g.fileName;
			String ruleName = Utils.getRuleName(tree);
			Token token = ((TerminalNode)(Trees.findNodeSuchThat(tree, TerminalNode.class::isInstance))).getSymbol();
			g.tool.errMgr.grammarError(ErrorType.FRAGMENT_ACTION_IGNORED, fileName, token, ruleName);
		}
	}

	public void actionInAlt(ANTLRParser.ActionBlockContext action) {
		ANTLRParser.LexerRuleSpecContext ruleSpec = Utils.getParent(action, ANTLRParser.LexerRuleSpecContext.class);
		if (ruleSpec!= null && ruleSpec.FRAGMENT() != null) {
			String fileName = g.fileName;
			String ruleName = ruleSpec.TOKEN_REF().getText();
			g.tool.errMgr.grammarError(ErrorType.FRAGMENT_ACTION_IGNORED, fileName, action.BEGIN_ACTION().getSymbol(), ruleName);
		}
	}

	/**
	 Make sure that action is last element in outer alt; here action,
	 a2, z, and zz are bad, but a3 is ok:
	 (RULE A (BLOCK (ALT {action} 'a')))
	 (RULE B (BLOCK (ALT (BLOCK (ALT {a2} 'x') (ALT 'y')) {a3})))
	 (RULE C (BLOCK (ALT 'd' {z}) (ALT 'e' {zz})))
	 */
	protected void checkElementIsOuterMostInSingleAlt(ANTLRParser.LexerCommandContext tree) {

		g.ast.getErrorContexts();
//		ParseTree alt = tree.parent;
//		ParseTree blk = alt.getParent();
//		boolean outerMostAlt = blk instanceof ANTLRParser.LexerRuleSpecContext;
//		Tree rule = Trees.getParent(tree, ANTLRParser.LexerRuleSpecContext.class);
//		String fileName = g.fileName;
//		if ( !outerMostAlt || blk.getChildCount()>1 )
//		{
//			ErrorType e = ErrorType.LEXER_COMMAND_PLACEMENT_ISSUE;
//			g.tool.errMgr.grammarError(e,
//									   fileName,
//									   tree.get(),
//									   rule.getChild(0).getText());
//
//		}
	}

	public void label(ANTLRParser.LabeledElementContext tree) {
		TerminalNode ID = (TerminalNode)Trees.findNodeSuchThat(tree.identifier(), TerminalNode.class::isInstance);

		if (tree.block() != null) {
			String fileName = g.fileName;
			g.tool.errMgr.grammarError(ErrorType.LABEL_BLOCK_NOT_A_SET, fileName, ID.getSymbol(), ID.getText());
		}
	}

	protected void enterTerminal1(ANTLRParser.TerminalContext tree) {
		String text = tree.getText();
		if (text.equals("''")) {
			TerminalNode tokenNode = (TerminalNode)Trees.findNodeSuchThat(tree, TerminalNode.class::isInstance);
			g.tool.errMgr.grammarError(ErrorType.EMPTY_STRINGS_AND_SETS_NOT_ALLOWED, g.fileName, tokenNode.getSymbol(), "''");
		}
	}

	/** Check option is appropriate for grammar, rule, subrule */
	void checkOptions(int parentType, Token optionID, TerminalNode valueAST) {
		Set<String> optionsToCheck = null;
		switch (parentType) {
			case ANTLRParser.BLOCK:
				optionsToCheck = g.isLexer() ? Grammar.lexerBlockOptions : Grammar.parserBlockOptions;
				break;
			case ANTLRParser.RULE:
				optionsToCheck = g.isLexer() ? Grammar.lexerRuleOptions : Grammar.parseRuleOptions;
				break;
			case ANTLRParser.GRAMMAR:
				optionsToCheck = g.getType() == ANTLRParser.LEXER
						? Grammar.lexerOptions
						: Grammar.parserOptions;
				break;
		}
		String optionName = optionID.getText();
		if (optionsToCheck != null && !optionsToCheck.contains(optionName)) {
			g.tool.errMgr.grammarError(ErrorType.ILLEGAL_OPTION, g.fileName, optionID, optionName);
		}
		else {
			checkCaseInsensitiveOption(optionID, valueAST, parentType);
		}
	}

	private void checkCaseInsensitiveOption(Token optionID, TerminalNode valueAST, int parentType) {
		String optionName = optionID.getText();
		if (optionName.equals(Grammar.caseInsensitiveOptionName)) {
			String valueText = valueAST.getText();
			if (valueText.equals("true") || valueText.equals("false")) {
				boolean currentValue = Boolean.parseBoolean(valueText);
				if (parentType == ANTLRParser.GRAMMAR) {
					grammarCaseInsensitive = currentValue;
				}
				else {
					if (grammarCaseInsensitive == currentValue) {
						g.tool.errMgr.grammarError(ErrorType.REDUNDANT_CASE_INSENSITIVE_LEXER_RULE_OPTION,
								g.fileName, optionID, currentValue);
					}
				}
			}
			else {
				g.tool.errMgr.grammarError(ErrorType.ILLEGAL_OPTION_VALUE, g.fileName, valueAST.getSymbol(),
						optionName, valueText);
			}
		}
	}

	/** Check option is appropriate for elem; parent of ID is ELEMENT_OPTIONS */
	boolean checkElementOptions(ParseTree elem,
								TerminalNode ID,
								ParserRuleContext valueAST)
	{
		if (checkAssocElementOption && ID != null && "assoc".equals(ID.getText())) {
			if (!(elem instanceof ANTLRParser.AltListContext)) { // TODO: check elem is ALT type
				TerminalNode idNode = (TerminalNode)Trees.findNodeSuchThat(elem, TerminalNode.class::isInstance);

				Token optionID = ID.getSymbol();
				String fileName = optionID.getInputStream().getSourceName();
				g.tool.errMgr.grammarError(ErrorType.UNRECOGNIZED_ASSOC_OPTION,
										   fileName,
										   optionID,
						idNode.getText());
			}
		}

		if ( elem instanceof ANTLRParser.ParserRuleSpecContext ) { //TODO:
			return checkRuleRefOptions((ANTLRParser.ParserRuleSpecContext)elem, ID, valueAST);
		}
		if ( elem instanceof ANTLRParser.LexerBlockContext ) { //TODO:
			return checkTokenOptions((ANTLRParser.LexerBlockContext)elem, ID, valueAST);
		}
		if ( elem instanceof  ANTLRParser.Action_Context ) { //TODO:
			return false;
		}
		if ( elem instanceof  ANTLRParser.SetElementContext ) { // TODO:
			Token optionID = ID.getSymbol();
			String fileName = optionID.getInputStream().getSourceName();
			if ( valueAST!=null && !Grammar.semPredOptions.contains(optionID.getText()) ) {
				g.tool.errMgr.grammarError(ErrorType.ILLEGAL_OPTION,
										   fileName,
										   optionID,
										   optionID.getText());
				return false;
			}
		}
		return false;
	}

	boolean checkRuleRefOptions(ANTLRParser.ParserRuleSpecContext elem, TerminalNode ID, ParseTree valueAST) {
		Token optionID = ID.getSymbol();
		String fileName = optionID.getInputStream().getSourceName();
		// don't care about id<SimpleValue> options
		if ( valueAST!=null && !Grammar.ruleRefOptions.contains(optionID.getText()) ) {
			g.tool.errMgr.grammarError(ErrorType.ILLEGAL_OPTION,
									   fileName,
									   optionID,
									   optionID.getText());
			return false;
		}
		// TODO: extra checks depending on rule kind?
		return true;
	}

	boolean checkTokenOptions(ANTLRParser.LexerBlockContext elem, TerminalNode ID, ParseTree valueAST) {
		Token optionID = ID.getSymbol();
		String fileName = optionID.getInputStream().getSourceName();
		// don't care about ID<ASTNodeName> options
		if ( valueAST!=null && !Grammar.tokenOptions.contains(optionID.getText()) ) {
			g.tool.errMgr.grammarError(ErrorType.ILLEGAL_OPTION,
									   fileName,
									   optionID,
									   optionID.getText());
			return false;
		}
		// TODO: extra checks depending on terminal kind?
		return true;
	}

	void checkImport(Token importID) {
		Grammar delegate = g.getImportedGrammar(importID.getText());
		if ( delegate==null ) return;
		List<Integer> validDelegators = validImportTypes.get(delegate.getType());
		if ( validDelegators!=null && !validDelegators.contains(g.getType()) ) {
			g.tool.errMgr.grammarError(ErrorType.INVALID_IMPORT,
									   g.fileName,
									   importID,
									   g, delegate);
		}
		if ( g.isCombined() &&
			 (delegate.name.equals(g.name+Grammar.getGrammarTypeToFileNameSuffix(ANTLRParser.LEXER))||
			  delegate.name.equals(g.name+Grammar.getGrammarTypeToFileNameSuffix(ANTLRParser.PARSER))) )
		{
			g.tool.errMgr.grammarError(ErrorType.IMPORT_NAME_CLASH,
									   g.fileName,
									   importID,
									   g, delegate);
		}
	}
}
