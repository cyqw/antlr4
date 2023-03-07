/*
 * Copyright (c) 2012-2017 The ANTLR Project. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 */

package org.antlr.v4.semantics;

import org.antlr.v4.parse.ActionSplitter;
import org.antlr.v4.parse.ActionSplitterListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.tool.Alternative;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.Rule;
import org.antlr.v4.tool.ast.ActionAST;

/** Look for errors and deadcode stuff */
public class UseDefAnalyzer {
	// side-effect: updates Alternative with refs in actions
	public static void trackTokenRuleRefsInActions(Grammar g) {
		for (Rule r : g.rules.values()) {
			for (int i=1; i<=r.numberOfAlts; i++) {
				Alternative alt = r.alt[i];
				for (ActionAST a : alt.actions) {
					ActionSniffer sniffer =	new ActionSniffer(g, r, alt, a, a.token);
					sniffer.examineAction();
				}
			}
		}
	}

	public static boolean actionIsContextDependent(ActionAST actionAST) {
		CharStream in = CharStreams.fromString(actionAST.token.getText());
		final boolean[] dependent = new boolean[] {false}; // can't be simple bool with anon class
		ActionSplitterListener listener = new BlankActionSplitterListener() {
			@Override
			public void nonLocalAttr(String expr, Token x, Token y) { dependent[0] = true; }
			@Override
			public void qualifiedAttr(String expr, Token x, Token y) { dependent[0] = true; }
			@Override
			public void setAttr(String expr, Token x, Token rhs) { dependent[0] = true; }
			@Override
			public void setExprAttribute(String expr) { dependent[0] = true; }
			@Override
			public void setNonLocalAttr(String expr, Token x, Token y, Token rhs) { dependent[0] = true; }
			@Override
			public void attr(String expr, Token x) {  dependent[0] = true; }
		};
		ActionSplitter splitter = new ActionSplitter(in, listener);
		// forces eval, triggers listener methods
		splitter.getActionTokens();
		return dependent[0];
	}


}
