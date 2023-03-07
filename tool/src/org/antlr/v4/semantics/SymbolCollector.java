/*
 * Copyright (c) 2012-2017 The ANTLR Project. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 */

package org.antlr.v4.semantics;

import org.antlr.v4.parse.ANTLRParser;
import org.antlr.v4.parse.GrammarTreeVisitor;
import org.antlr.v4.tool.ErrorManager;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.ast.ActionAST;
import org.antlr.v4.tool.ast.GrammarAST;
import org.antlr.v4.tool.ast.GrammarRootAST;
import org.antlr.v4.tool.ast.TerminalAST;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Collects (create) rules, terminals, strings, actions, scopes etc... from AST
 *  side-effects: sets resolver field of asts for actions and
 *  defines predicates via definePredicateInAlt(), collects actions and stores
 *  in alts.
 *  TODO: remove side-effects!
 */
public class SymbolCollector extends GrammarTreeVisitor {
	/** which grammar are we checking */
	public Grammar g;

	// stuff to collect
	public List<GrammarAST> rulerefs = new ArrayList<GrammarAST>();
	public List<GrammarAST> qualifiedRulerefs = new ArrayList<GrammarAST>();
	public List<ANTLRParser.TokensSpecContext> terminals = new ArrayList<ANTLRParser.TokensSpecContext>();
	public List<ANTLRParser.TokensSpecContext> tokenIDRefs = new ArrayList<ANTLRParser.TokensSpecContext>();
	public Set<String> strings = new HashSet<String>();
	public List<ANTLRParser.TokensSpecContext> tokensDefs = new ArrayList<ANTLRParser.TokensSpecContext>();
	public List<ANTLRParser.ChannelsSpecContext> channelDefs = new ArrayList<ANTLRParser.ChannelsSpecContext>();

	/** Track action name node in @parser::members {...} or @members {...} */
	List<ANTLRParser.Action_Context> namedActions = new ArrayList<ANTLRParser.Action_Context>();

	public ErrorManager errMgr;

	public SymbolCollector(Grammar g) {
		this.g = g;
		this.errMgr = g.tool.errMgr;
	}

	@Override
	public ErrorManager getErrorManager() { return errMgr; }

	public void process(GrammarRootAST ast) {
		ast.getActions().forEach(this::globalNamedAction);
		ast.getTokensSpecs().forEach(this::defineToken);
		ast.getChannels().forEach(this::defineChannel);
	}

	public void globalNamedAction(ANTLRParser.ActionBlockContext ast) {
		if (ast.getParent() instanceof ANTLRParser.Action_Context) {
			namedActions.add((ANTLRParser.Action_Context)ast.getParent());
		}
	}

	public void defineToken(ANTLRParser.TokensSpecContext ID) {
		terminals.add(ID);
		tokenIDRefs.add(ID);
		tokensDefs.add(ID);
	}

	public void defineChannel(ANTLRParser.ChannelsSpecContext ID) {
		channelDefs.add(ID);
	}

	public void stringRef(TerminalAST ref) {
		terminals.add(ref);
		strings.add(ref.getText());

	}

	public void tokenRef(TerminalAST ref) {
		terminals.add(ref);
		tokenIDRefs.add(ref);

	}


	public void ruleRef(GrammarAST ref, ActionAST arg) {
//		if ( inContext("DOT ...") ) qualifiedRulerefs.add((GrammarAST)ref.getParent());
		rulerefs.add(ref);

	}







}
