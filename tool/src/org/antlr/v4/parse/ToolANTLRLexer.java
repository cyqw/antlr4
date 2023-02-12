/*
 * Copyright (c) 2012-2017 The ANTLR Project. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 */

package org.antlr.v4.parse;

import org.antlr.v4.Tool;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

import java.util.BitSet;

public class ToolANTLRLexer extends ANTLRLexer implements ANTLRErrorListener {
	public Tool tool;
	private int numberOfSyntaxErrors;

	public ToolANTLRLexer(CharStream input, Tool tool) {
		super(input);
		this.tool = tool;
		this.addErrorListener(this);
	}

	@Override
	public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {

	}

	@Override
	public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {

	}

	@Override
	public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {

	}

	@Override
	public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {

	}

	public int getNumberOfSyntaxErrors() {
		return numberOfSyntaxErrors;
	}

//	@Override
//	public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
//		String msg = getErrorMessage(e, tokenNames);
//		tool.errMgr.syntaxError(ErrorType.SYNTAX_ERROR, getSourceName(), e.token, e, msg);
//	}
//
//	@Override
//	public void grammarError(ErrorType etype, Token token, Object... args) {
//		tool.errMgr.grammarError(etype, getSourceName(), token, args);
//	}
}
