/*
 * Copyright (c) 2012-2017 The ANTLR Project. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 */

package org.antlr.v4.semantics;

import org.antlr.v4.analysis.LeftRecursiveRuleAnalyzer;
import org.antlr.v4.misc.OrderedHashMap;
import org.antlr.v4.misc.Utils;
import org.antlr.v4.parse.ANTLRParser;
import org.antlr.v4.parse.ScopeParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.tool.AttributeDict;
import org.antlr.v4.tool.ErrorManager;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.LeftRecursiveRule;
import org.antlr.v4.tool.Rule;
import org.antlr.v4.tool.ast.GrammarRootAST;
import org.stringtemplate.v4.misc.MultiMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RuleCollector {
	private boolean globalGrammarCaseInsensitive = false;

	/** which grammar are we checking */
	public Grammar g;
	public ErrorManager errMgr;

	// stuff to collect. this is the output
	public OrderedHashMap<String, Rule> rules = new OrderedHashMap<String, Rule>();
	public MultiMap<String,ANTLRParser.LabeledElementContext> ruleToAltLabels = new MultiMap<String, ANTLRParser.LabeledElementContext>();
	public Map<String,String> altLabelToRuleName = new HashMap<String, String>();

	public RuleCollector(Grammar g) {
		this.g = g;
		this.errMgr = g.tool.errMgr;
	}

	public void process(GrammarRootAST ast) {
		g.ast.getGlobalOptionsSpecs().forEach(it2 -> {
			it2.option().forEach(it -> grammarOption(it.identifier(), it.optionValue()));
		});
		g.ast.getLexerRules().forEach(this::discoverLexerRule);
		g.ast.getParserRules().forEach(this::discoverRule);
		g.ast.getLabeledElements().forEach(this::discoverOuterAlt);
	}

	public void discoverRule(ANTLRParser.ParserRuleSpecContext rule)
	{
		ANTLRParser.RuleBlockContext block = rule.ruleBlock();
		TerminalNode ID = rule.RULE_REF();
		int numAlts = block.ruleAltList().labeledAlt().size();
		Rule r;
		if ( LeftRecursiveRuleAnalyzer.hasImmediateRecursiveRuleRefs(rule, ID.getText()) ) {
			r = new LeftRecursiveRule(g, ID.getText(), rule);
		}
		else {
			r = new Rule(g, ID.getText(), rule, numAlts);
		}
		rules.put(r.name, r);

		ANTLRParser.ArgActionBlockContext arg = rule.argActionBlock();
		ANTLRParser.ArgActionBlockContext returns = rule.ruleReturns().argActionBlock();
		ANTLRParser.ArgActionBlockContext locals = rule.localsSpec().argActionBlock();

		if ( arg!=null ) {
			r.args = ScopeParser.parseTypedArgList(arg, arg.getText(), g);
			r.args.type = AttributeDict.DictType.ARG;
			r.args.ast = arg;
		}

		if ( returns!=null ) {
			r.retvals = ScopeParser.parseTypedArgList(returns, returns.getText(), g);
			r.retvals.type = AttributeDict.DictType.RET;
			r.retvals.ast = returns;
		}

		if ( locals!=null ) {
			r.locals = ScopeParser.parseTypedArgList(locals, locals.getText(), g);
			r.locals.type = AttributeDict.DictType.LOCAL;
			r.locals.ast = locals;
		}

		for (ANTLRParser.RulePrequelContext a : rule.rulePrequel()) {
			// a = ^(AT ID ACTION)
			ANTLRParser.RuleActionContext action = a.ruleAction();
			if (action != null) {
				r.namedActions.put(action.identifier().getText(), action.actionBlock());
			}
		}
	}

	public void discoverOuterAlt(ANTLRParser.LabeledElementContext alt) {
			ANTLRParser.ParserRuleSpecContext ruleNode = Utils.getParent(alt, ANTLRParser.ParserRuleSpecContext.class);
		String currentRuleName = ruleNode.RULE_REF().getText();
		ruleToAltLabels.map(currentRuleName, alt);
			String altLabel = alt.identifier().getText();
			altLabelToRuleName.put(Utils.capitalize(altLabel), currentRuleName);
			altLabelToRuleName.put(Utils.decapitalize(altLabel), currentRuleName);
	}

	public void grammarOption(ParseTree ID, ParseTree valueAST) {
		Boolean caseInsensitive = getCaseInsensitiveValue(ID, valueAST);
		if (caseInsensitive != null) {
			globalGrammarCaseInsensitive = caseInsensitive;
		}
	}

	public void discoverLexerRule(ANTLRParser.LexerRuleSpecContext rule)
	{
		boolean currentCaseInsensitive = globalGrammarCaseInsensitive;
		ANTLRParser.OptionsSpecContext options = rule.optionsSpec();
		if (options != null) {
			for (ANTLRParser.OptionContext childAST : options.option()) {
				Boolean caseInsensitive = getCaseInsensitiveValue(childAST.identifier(), childAST.optionValue());
				if (caseInsensitive != null) {
					currentCaseInsensitive = caseInsensitive;
				}
			}
		}

		ParserRuleContext parentNode = rule.getParent();
		String currentModeName = null;
		if (parentNode instanceof ANTLRParser.ModeSpecContext) {
			currentModeName = ((ANTLRParser.ModeSpecContext)parentNode).identifier().getText();
		}
		int numAlts = rule.lexerRuleBlock().getChildCount();
		Rule r = new Rule(g, rule.TOKEN_REF().getText(), rule, numAlts, currentModeName, currentCaseInsensitive);
		if ( rule.FRAGMENT() != null ) r.modifiers = Collections.singletonList(rule.FRAGMENT());
		rules.put(r.name, r);
	}

	private Boolean getCaseInsensitiveValue(ParseTree optionID, ParseTree valueAST) {
		String optionName = optionID.getText();
		if (optionName.equals(Grammar.caseInsensitiveOptionName)) {
			String valueText = valueAST.getText();
			if (valueText.equals("true") || valueText.equals("false")) {
				return Boolean.parseBoolean(valueText);
			}
		}
		return null;
	}
}
