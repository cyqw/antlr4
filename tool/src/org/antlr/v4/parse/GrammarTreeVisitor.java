package org.antlr.v4.parse;

import org.antlr.v4.codegen.model.TreeNodeStream;
import org.antlr.v4.tool.ErrorManager;
import org.antlr.v4.tool.ast.ActionAST;
import org.antlr.v4.tool.ast.GrammarAST;
import org.antlr.v4.tool.ast.GrammarRootAST;
import org.antlr.v4.tool.ast.TerminalAST;

public class GrammarTreeVisitor extends ANTLRParserBaseListener{
	public String currentRuleName;

	public int currentOuterAltNumber = 1; // 1..n

	private GrammarRootAST root ;

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

	public GrammarRootAST getTree() {
		return root;
	}

	@Override
	public void enterGrammarSpec(ANTLRParser.GrammarSpecContext ctx) {
		root = new GrammarRootAST(ctx);
		super.enterGrammarSpec(ctx);
	}

	@Override
	public void enterGrammarType(ANTLRParser.GrammarTypeContext ctx) {
		if (ctx.LEXER() != null) {
			root.grammarType = ANTLRParser.LEXER;
		} else if (ctx.PARSER() != null) {
			root.grammarType = ANTLRParser.PARSER;
		} else {
			root.grammarType = ANTLRParser.COMBINED;
		}
		super.enterGrammarType(ctx);
	}

	@Override
	public void enterOption(ANTLRParser.OptionContext ctx) {
		if (root.tokenVocab == null && ctx.identifier().getText().equals("tokenVocab")) {
			root.tokenVocab = ctx.optionValue();
		}
		root.setOption(ctx.identifier().getText(), new GrammarAST(ctx.optionValue()));
		super.enterOption(ctx);
	}
}
