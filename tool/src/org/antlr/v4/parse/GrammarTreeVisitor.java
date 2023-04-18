package org.antlr.v4.parse;

import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.tree.TreeNodeStream;
import org.antlr.v4.tool.ErrorManager;
import org.antlr.v4.tool.ast.ActionAST;
import org.antlr.v4.tool.ast.AltAST;
import org.antlr.v4.tool.ast.GrammarAST;
import org.antlr.v4.tool.ast.GrammarASTWithOptions;
import org.antlr.v4.tool.ast.GrammarRootAST;
import org.antlr.v4.tool.ast.PredAST;
import org.antlr.v4.tool.ast.RuleAST;
import org.antlr.v4.tool.ast.TerminalAST;

import java.util.List;

public class GrammarTreeVisitor {
	protected static final int CLOSURE = 0;
	protected static final int POSITIVE_CLOSURE = 0;
	protected static final int OPTIONAL = 0;
	protected static final int IMPORT = 0;
	protected static final int RULE = 0;
	protected static final int BLOCK = 0;
	protected static final int TOKEN_REF = 0;
	protected static final int STRING_LITERAL = 1;
	protected static final int RANGE = 2;
	protected static final int SET = 3;
	protected static final int NOT = 4;
	protected static final int RULE_REF = 5;
	protected static final int WILDCARD = 6;
	protected int currentOuterAltNumber;
	protected String currentRuleName;
	protected String currentModeName;
	protected int OPTIONS;
	protected int TOKENS_SPEC=0;

	public GrammarTreeVisitor(TreeNodeStream input) {
	}
	public GrammarTreeVisitor() {
	}
	public ErrorManager getErrorManager() {
		return null;
	}

	public void tokenRef(TerminalAST ref) {

	}

	protected void exitLexerRule(GrammarAST tree) {

	}

	public void ruleRef(GrammarAST ref, ActionAST arg) {

	}

	public void stringRef(TerminalAST ref) {

	}

	protected void enterAlternative(AltAST tree) {

	}

	protected void exitAlternative(AltAST tree) {

	}

	protected void enterElement(GrammarAST tree) {

	}

	protected void exitElement(GrammarAST tree) {

	}

	protected void enterBlockSet(GrammarAST tree) {

	}

	protected void exitBlockSet(GrammarAST tree) {

	}

	protected void exitSubrule(GrammarAST tree) {

	}

	protected void enterLexerAlternative(GrammarAST tree) {

	}

	protected void exitLexerAlternative(GrammarAST tree) {

	}

	public void ruleOption(GrammarAST ID, GrammarAST valueAST) {

	}

	public void blockOption(GrammarAST ID, GrammarAST valueAST) {

	}

	public void globalNamedAction(GrammarAST scope, GrammarAST ID, ActionAST action) {

	}

	public void defineToken(GrammarAST ID) {

	}

	protected void enterChannelsSpec(GrammarAST tree) {

	}

	public void defineChannel(GrammarAST ID) {

	}

	public void elementOption(GrammarASTWithOptions elem, GrammarAST ID, GrammarAST valueAST) {

	}

	public void finishRule(RuleAST rule, GrammarAST ID, GrammarAST block) {

	}

	protected void enterLexerElement(GrammarAST tree) {

	}

	protected void exitLexerElement(GrammarAST tree) {

	}

	protected void exitLexerSubrule(GrammarAST tree) {

	}

	public void visitGrammar(GrammarAST ast) {

	}

	public void discoverGrammar(GrammarRootAST root, GrammarAST ID) {

	}

	public void finishPrequels(GrammarAST firstPrequel) {

	}

	public void importGrammar(GrammarAST label, GrammarAST ID) {

	}

	public void discoverRules(GrammarAST rules) {

	}

	protected void enterMode(GrammarAST tree) {

	}

	protected void exitMode(GrammarAST tree) {

	}

	public void modeDef(GrammarAST m, GrammarAST ID) {

	}

	public void discoverRule(RuleAST rule, GrammarAST ID,
							 List<GrammarAST> modifiers, ActionAST arg,
							 ActionAST returns, GrammarAST thrws,
							 GrammarAST options, ActionAST locals,
							 List<GrammarAST> actions,
							 GrammarAST block) {

	}

	public void discoverOuterAlt(AltAST alt) {

	}

	public void grammarOption(GrammarAST ID, GrammarAST valueAST) {

	}

	public void discoverLexerRule(RuleAST rule, GrammarAST ID, List<GrammarAST> modifiers,
								  GrammarAST options, GrammarAST block) {

	}

	protected void enterLexerCommand(GrammarAST tree) {

	}

	public void actionInAlt(ActionAST action) {

	}

	public void sempredInAlt(PredAST pred) {

	}

	public void ruleCatch(GrammarAST arg, ActionAST action) {

	}

	public void finallyAction(ActionAST action) {

	}

	public void label(GrammarAST op, GrammarAST ID, GrammarAST element) {

	}

	protected void enterTerminal(GrammarAST tree) {

	}

	public void outerAlternative() throws RecognitionException {

	}

	public void visit(RuleAST t, String rule) {

	}
}
