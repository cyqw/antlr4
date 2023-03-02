package org.antlr.v4.parse;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;

public class ActionSplitter {
	private CharStream stream;
	private ActionSplitterListener listener;

	public ActionSplitter(CharStream in, ActionSplitterListener listener) {
		stream = in;
		this.listener = listener;
	}

    public ActionParser.ActionContext getActionTokens() {
		ActionLexer lexer = new ActionLexer(stream);
		TokenStream tokens = new CommonTokenStream(lexer);
		ActionParser parser = new ActionParser(tokens, listener);
		return parser.action();
	}
}
