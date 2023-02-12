package org.antlr.runtime;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;

public interface TokenConstant{
	public static Token INVALID_TOKEN = new CommonToken(org.antlr.v4.runtime.Token.INVALID_TYPE);

}
