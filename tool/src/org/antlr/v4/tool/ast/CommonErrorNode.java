package org.antlr.v4.tool.ast;

import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;

public class CommonErrorNode {
	public CommonErrorNode(TokenStream input, Token start, Token stop, RecognitionException e) {

	}

	public boolean isNil() {
		return false;
	}

	public int getType() {
		return 0;
	}

	public String getText() {
		return null;
	}
}
