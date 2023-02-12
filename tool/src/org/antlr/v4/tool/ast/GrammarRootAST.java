/*
 * Copyright (c) 2012-2017 The ANTLR Project. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 */

package org.antlr.v4.tool.ast;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.HashMap;
import java.util.Map;

public class GrammarRootAST extends GrammarASTWithOptions {
	public static final Map<String, String> defaultOptions = new HashMap<String, String>();
	static {
		defaultOptions.put("language","Java");
	}

    public int grammarType; // LEXER, PARSER, GRAMMAR (combined)
	public boolean hasErrors;
	/** Track stream used to create this tree */

	public Map<String, String> cmdLineOptions; // -DsuperClass=T on command line
	public String fileName;

	public GrammarRootAST(GrammarRootAST node) {
		super(node);
		this.grammarType = node.grammarType;
		this.hasErrors = node.hasErrors;
	}

	public GrammarRootAST(Token t, TokenStream tokenStream) {
		super(t);
		if (tokenStream == null) {
			throw new NullPointerException("tokenStream");
		}

	}

	public GrammarRootAST(int type, Token t, TokenStream tokenStream) {
		super(type, t);
		if (tokenStream == null) {
			throw new NullPointerException("tokenStream");
		}

	}

	public GrammarRootAST(int type, Token t, String text, TokenStream tokenStream) {
		super(type,t,text);
		if (tokenStream == null) {
			throw new NullPointerException("tokenStream");
		}

    }

	public GrammarRootAST(Parser p, ParseTree r) {
		super(p, r);
	}

	public String getGrammarName() {
		GrammarAST t = getChild(0);
		if ( t!=null ) return t.getText();
		return null;
	}

	@Override
	public String getOptionString(String key) {
		if ( cmdLineOptions!=null && cmdLineOptions.containsKey(key) ) {
			return cmdLineOptions.get(key);
		}
		String value = super.getOptionString(key);
		if ( value==null ) {
			value = defaultOptions.get(key);
		}
		return value;
	}

	@Override
	public Object visit(GrammarASTVisitor v) { return v.visit(this); }

//	@Override
//	public GrammarRootAST dupNode() { return new GrammarRootAST(this); }
}
