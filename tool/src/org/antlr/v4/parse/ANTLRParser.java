package org.antlr.v4.parse;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleReturnScope;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.tool.ErrorType;

import java.util.Deque;

public abstract class ANTLRParser extends Parser {
	public static final int LEXER_ALT_ACTION = 0;
	public static final int LEXER_ACTION_CALL = 1;
	public static final int STRING_LITERAL = 2;
	public static final int LEXER = 0;
	public static final int INT = 1;
	public static final int ARG_ACTION = 0;
	public static final int PLUS_ASSIGN = 0;
	public static final int POSITIVE_CLOSURE = 0;
	public static final int CLOSURE = 11;
	public static final int OPTIONAL = 2;
	public static final int SET = 0;
	public static final int WILDCARD = 0;
	public static final int RULE_REF = 0;
	public static final int TOKEN_REF = 0;
	public static final int BLOCK = 1;
	public static final int ELEMENT_OPTIONS = 11;
	public static final int ACTION = 0;
	public static final int RANGE = 0;
	public static final int LEXER_CHAR_SET = 0;
	public static final int PARSER = 1;
	public static final int COMBINED = 2;
	public static final int CHANNELS = 0;
	public static final int TOKENS_SPEC = 0;
	public static final int AT = 0;
	public static final int RULES = 0;
	public static final int RULE = 3;
	public static final int MODE = 0;
	public static final int OPTIONS = 0;
	public static final int GRAMMAR = 0;
	public static final int ID = 0;
	public static final int ALT = 0;
	public static final int FRAGMENT = 0;
	public static final int SEMPRED = 12;
	public static final int ASSIGN = 10;
	public static final int IMPORT = 11;
	public static String[] tokenNames;
	protected Deque<String> paraphrases;

	public ANTLRParser(TokenStream input) {
		super(new CommonTokenStream(input.getTokenSource()));
	}

	public abstract void displayRecognitionError(String[] tokenNames,
												 RecognitionException e);

	public abstract void grammarError(ErrorType etype, org.antlr.v4.runtime.Token token, Object... args);

	public ParserRuleReturnScope grammarSpec() throws RecognitionException {
		return null;
	}

	public void setTreeAdaptor(GrammarASTAdaptor adaptor) {

	}

	public ParserRuleReturnScope rule() {
		return null;
	}

	public String getSourceName() {
		return null;
	}
}
