/*
 * Copyright (c) 2012-2017 The ANTLR Project. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 */

package org.antlr.v4.parse;

import org.antlr.v4.Tool;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/** Override error handling for use with ANTLR tool itself; leaves
 *  nothing in grammar associated with Tool so others can use in IDEs, ...
 */
public class ToolANTLRParser extends ANTLRParser implements ANTLRErrorListener {
	public Tool tool;

	private List<ParserRuleContext> errorContexts = new ArrayList<>();


	public ToolANTLRParser(TokenStream input, Tool tool) {
		super(input);
		removeErrorListeners();
		addErrorListener(this);
		this.tool = tool;
	}

	public List<ParserRuleContext> getErrorContexts() {
		return errorContexts;
	}

	@Override
	public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
		errorContexts.add(((Parser) recognizer).getContext());
	}

	@Override
	public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
		errorContexts.add(recognizer.getContext());
	}

	@Override
	public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {

	}

	@Override
	public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {

	}

//	public ParserRuleReturnScope rule() {
//	}
//
//	@Override
//	public void displayRecognitionError(String[] tokenNames,
//										RecognitionException e)
//	{
//		String msg = getParserErrorMessage(this, e);
//		if ( !paraphrases.isEmpty() ) {
//			String paraphrase = paraphrases.peek();
//			msg = msg+" while "+paraphrase;
//		}
//	//	List stack = getRuleInvocationStack(e, this.getClass().getName());
//	//	msg += ", rule stack = "+stack;
//		tool.errMgr.syntaxError(ErrorType.SYNTAX_ERROR, getSourceName(), e.token, e, msg);
//	}
//
//	public String getParserErrorMessage(Parser parser, RecognitionException e) {
//		String msg;
//		if ( e instanceof NoViableAltException) {
//			String name = parser.getTokenErrorDisplay(e.token);
//			msg = name+" came as a complete surprise to me";
//		}
//		else if ( e instanceof v4ParserException) {
//			msg = ((v4ParserException)e).msg;
//		}
//		else {
//			msg = parser.getErrorMessage(e, parser.getTokenNames());
//		}
//		return msg;
//	}
//
//	@Override
//	public void grammarError(ErrorType etype, org.antlr.runtime.Token token, Object... args) {
//		tool.errMgr.grammarError(etype, getSourceName(), token, args);
//	}
}
