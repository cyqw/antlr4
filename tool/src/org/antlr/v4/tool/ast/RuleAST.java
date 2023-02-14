/*
 * Copyright (c) 2012-2017 The ANTLR Project. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 */

package org.antlr.v4.tool.ast;

import org.antlr.v4.parse.ANTLRParser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.tool.Grammar;

public class RuleAST extends GrammarASTWithOptions {
	public RuleAST(RuleAST node) {
		super(node);
	}

	public RuleAST(ParseTree tree) {super(tree);}

	public RuleAST(Token t) { super(t); }
    public RuleAST(int type) { super(type); }

	public boolean isLexerRule() {
		String name = getRuleName();
		return name!=null && Grammar.isTokenName(name);
	}

	public String getRuleName() {
		GrammarAST nameNode = (GrammarAST)getChild(0);
		if ( nameNode!=null ) return nameNode.getText();
		return null;
	}

//	@Override
//	public RuleAST dupNode() { return new RuleAST(this); }

	public ActionAST getLexerAction() {
		GrammarAST blk = getFirstChildWithType(ANTLRParser.BLOCK);
		if ( blk.getChildCount()==1 ) {
			GrammarAST onlyAlt = blk.getChild(0);
			GrammarAST lastChild = onlyAlt.getChild(onlyAlt.getChildCount()-1);
			if ( lastChild.getType()==ANTLRParser.ACTION ) {
				return (ActionAST)lastChild;
			}
		}
		return null;
	}

	@Override
	public Object visit(GrammarASTVisitor v) { return v.visit(this); }
}
