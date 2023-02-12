package org.antlr.v4.parse;

import org.antlr.v4.codegen.model.TreeNodeStream;
import org.antlr.v4.tool.ErrorManager;
import org.antlr.v4.tool.ast.ActionAST;
import org.antlr.v4.tool.ast.GrammarAST;
import org.antlr.v4.tool.ast.TerminalAST;

public class GrammarTreeVisitor extends ANTLRParserBaseListener{
	public String currentRuleName;

	public int currentOuterAltNumber = 1; // 1..n

	public GrammarTreeVisitor(TreeNodeStream input) {
		super();
	}

	public GrammarTreeVisitor() {

	}


	public void tokenRef(TerminalAST ref) {

	}

	public void ruleRef(GrammarAST ref, ActionAST arg) {

	}

	public void visitGrammar(GrammarAST ast) {
	}

	public void stringRef(TerminalAST ref) {

	};

	public ErrorManager getErrorManager(){
		return null;
	}
}
