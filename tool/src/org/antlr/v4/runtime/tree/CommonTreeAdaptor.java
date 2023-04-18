package org.antlr.v4.runtime.tree;

import org.antlr.v4.runtime.Token;

public abstract class CommonTreeAdaptor {
	public abstract Object create(Token token);

	protected Object create(int tokenType, String text) {
		return null;
	}

	public abstract Object dupNode(Object t);

	public abstract Object errorNode(org.antlr.v4.runtime.TokenStream input, org.antlr.v4.runtime.Token start, org.antlr.v4.runtime.Token stop,
									 org.antlr.v4.runtime.RecognitionException e);

	public CommonTree dupTree(CommonTree o) {
		return null;
	}
}
