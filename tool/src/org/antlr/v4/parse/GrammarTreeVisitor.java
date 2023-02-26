package org.antlr.v4.parse;

import org.antlr.v4.codegen.model.TreeNodeStream;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.Trees;
import org.antlr.v4.tool.ErrorManager;
import org.antlr.v4.tool.ast.ActionAST;
import org.antlr.v4.tool.ast.GrammarAST;
import org.antlr.v4.tool.ast.GrammarRootAST;
import org.antlr.v4.tool.ast.TerminalAST;

public class GrammarTreeVisitor extends ANTLRParserBaseListener{

	public int currentOuterAltNumber = 1; // 1..n

	private GrammarRootAST root ;
	private ToolANTLRParser parser;
	private TerminalNode currentModeToken;

	public GrammarTreeVisitor(TreeNodeStream input) {
		super();
	}

	public GrammarTreeVisitor(ToolANTLRParser parser) {

		this.parser = parser;
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
		root.setErrorContexts(parser.getErrorContexts());
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
		super.enterOption(ctx);
	}

	@Override
	public void enterLexerRuleSpec(ANTLRParser.LexerRuleSpecContext ctx) {
		root.addLexerRule(currentModeToken, ctx);
		super.enterLexerRuleSpec(ctx);
	}

	@Override
	public void enterParserRuleSpec(ANTLRParser.ParserRuleSpecContext ctx) {
		root.addParserRule(ctx);
		super.enterParserRuleSpec(ctx);
	}

	@Override
	public void enterRuleref(ANTLRParser.RulerefContext ctx) {
		root.addRuleRef(ctx);
		super.enterRuleref(ctx);
	}

	@Override
	public void enterLabeledElement(ANTLRParser.LabeledElementContext ctx) {
		root.addLabeledElement(ctx);
		super.enterLabeledElement(ctx);
	}

	@Override
	public void enterTerminal(ANTLRParser.TerminalContext ctx) {
		root.addTokenRef(ctx);
		super.enterTerminal(ctx);
	}

	@Override
	public void enterLexerCommand(ANTLRParser.LexerCommandContext ctx) {
		root.addLexerCommand(ctx);
		super.enterLexerCommand(ctx);
	}

	@Override
	public void enterOptionsSpec(ANTLRParser.OptionsSpecContext ctx) {
		root.addOptionsSpec(ctx);
		super.enterOptionsSpec(ctx);
	}

	@Override
	public void enterTokensSpec(ANTLRParser.TokensSpecContext ctx) {
		root.addTokensSpec(ctx);
		super.enterTokensSpec(ctx);
	}

	@Override
	public void enterModeSpec(ANTLRParser.ModeSpecContext ctx) {
		TerminalNode token = (TerminalNode) Trees.findNodeSuchThat(ctx.identifier(), TerminalNode.class::isInstance);
		currentModeToken = token;
		root.addMode(token);
		super.enterModeSpec(ctx);
	}

	@Override
	public void enterChannelsSpec(ANTLRParser.ChannelsSpecContext ctx) {
		root.addChannel(ctx);
		super.enterChannelsSpec(ctx);
	}

	@Override
	public void enterElementOption(ANTLRParser.ElementOptionContext ctx) {
		root.addElementOption(ctx);
		super.enterElementOption(ctx);
	}

	@Override
	public void enterActionBlock(ANTLRParser.ActionBlockContext ctx) {
		root.addActions(ctx);
		super.enterActionBlock(ctx);
	}
}
