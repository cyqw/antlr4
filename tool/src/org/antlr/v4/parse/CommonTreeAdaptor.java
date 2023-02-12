package org.antlr.v4.parse;

import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;

public abstract class CommonTreeAdaptor {
	public Object create(Token token) {
		return null;
	};

	/** Make sure even imaginary nodes know the input stream */
	public Object create(int tokenType, String text) {
		return null;
	};

	public abstract Object dupNode(Object t);

	public abstract Object errorNode(TokenStream input, Token start, Token stop,
									 RecognitionException e);
}
