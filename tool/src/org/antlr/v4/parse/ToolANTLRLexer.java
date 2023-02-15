/*
 * Copyright (c) 2012-2017 The ANTLR Project. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 */

package org.antlr.v4.parse;

import org.antlr.v4.Tool;
import org.antlr.v4.runtime.CharStream;

public class ToolANTLRLexer extends ANTLRLexer {
	public Tool tool;
	private int numberOfSyntaxErrors;

	public ToolANTLRLexer(CharStream input, Tool tool) {
		super(input);
		this.tool = tool;
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
