package org.antlr.v4.parse;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.v4.tool.ErrorType;

public class ANTLRLexer extends Lexer {
	public static final int STRING_LITERAL = 0;
	public CommonTokenStream tokens;

	public ANTLRLexer(CharStream input) {
	}

	@Override
	public void mTokens() throws RecognitionException {

	}

	public void displayRecognitionError(String[] tokenNames, RecognitionException e) {

	}

	public String getSourceName() {
		return null;
	}

	public void grammarError(ErrorType etype, Token token, Object... args) {

	}

	public int getNumberOfSyntaxErrors() {
		return 0;
	}
}
