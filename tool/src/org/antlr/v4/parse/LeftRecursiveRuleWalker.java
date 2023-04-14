package org.antlr.v4.parse;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.antlr.v4.tool.ast.AltAST;
import org.antlr.v4.tool.ast.GrammarAST;

public abstract class LeftRecursiveRuleWalker {
	protected static final int PLUS_ASSIGN = 1;
	protected static final int BLOCK = 1;
	protected static final int ELEMENT_OPTIONS = 0;
	protected static final int ASSIGN = 0;
	protected static final int POUND = 0;
	protected static final int TOKEN_REF = 0;
	protected static final int STRING_LITERAL = 0;
	protected static final int ARG_ACTION = 0;
	static protected int RULE_REF;
	protected int numAlts;

	public LeftRecursiveRuleWalker(CommonTreeNodeStream commonTreeNodeStream) {
	}

	public abstract void setReturnValues(GrammarAST t);

	public abstract void setAltAssoc(AltAST t, int alt);

	public abstract void binaryAlt(AltAST originalAltTree, int alt);

	public abstract void prefixAlt(AltAST originalAltTree, int alt);

	public abstract void suffixAlt(AltAST originalAltTree, int alt);

	public abstract void otherAlt(AltAST originalAltTree, int alt);

	public boolean rec_rule() throws RecognitionException {
		return false;
	}
}
