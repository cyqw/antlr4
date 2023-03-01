/*
 * Copyright (c) 2012-2017 The ANTLR Project. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 */

package org.antlr.v4.tool;

import org.antlr.runtime.tree.TreeVisitorAction;
import org.antlr.v4.Tool;
import org.antlr.v4.analysis.LeftRecursiveRuleTransformer;
import org.antlr.v4.parse.ANTLRParser;
import org.antlr.v4.parse.BlockSetTransformer;
import org.antlr.v4.parse.GrammarASTAdaptor;
import org.antlr.v4.parse.GrammarToken;
import org.antlr.v4.runtime.misc.DoubleKeyMap;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.Trees;
import org.antlr.v4.tool.ast.GrammarAST;
import org.antlr.v4.tool.ast.GrammarRootAST;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Handle left-recursion and block-set transforms */
public class GrammarTransformPipeline {
	public Grammar g;
	public Tool tool;

	public GrammarTransformPipeline(Grammar g, Tool tool) {
		this.g = g;
		this.tool = tool;
	}

	public void process() {
		GrammarRootAST root = g.ast;
		if ( root==null ) return;
        tool.log("grammar", "before: "+ Trees.toStringTree(root.root));

        integrateImportedGrammars(g);
		reduceBlocksToSets(root);
        expandParameterizedLoops(root);

        tool.log("grammar", "after: "+Trees.toStringTree(root.root));
	}

	public void reduceBlocksToSets(GrammarRootAST root) {
		CommonTreeNodeStream nodes = new CommonTreeNodeStream(new GrammarASTAdaptor(), root);
		GrammarASTAdaptor adaptor = new GrammarASTAdaptor();
		BlockSetTransformer transformer = new BlockSetTransformer(nodes, g);
		transformer.setTreeAdaptor(adaptor);
		transformer.downup(root);
	}


    /** Utility visitor that sets grammar ptr in each node */

	public static void augmentTokensWithOriginalPosition(final Grammar g, ANTLRParser.RuleSpecContext tree) {
		if ( tree==null ) return;

//		List<ParseTree> optionsSubTrees  = Trees.findAllNodes(tree, ANTLRParser.OPTIONS, true);
//		for (int i = 0; i < optionsSubTrees.size(); i++) {
//			ParseTree t = optionsSubTrees.get(i);
//			ANTLRParser.OptionsSpecContext elWithOpt = (ANTLRParser.OptionsSpecContext) t.getParent();
//			ANTLRParser.ParserRuleSpecContext ruleNode = (ANTLRParser.ParserRuleSpecContext) elWithOpt.getParent();
//			for (ANTLRParser.OptionContext option : elWithOpt.option()) {
//				if (option.identifier().getText().equals(LeftRecursiveRuleTransformer.TOKENINDEX_OPTION_NAME)) {
//					GrammarToken newTok = new GrammarToken(g, ruleNode.RULE_REF().getSymbol());
//					newTok.originalTokenIndex = Integer.valueOf(option.optionValue().getText());
//					elWithOpt.token = newTok;
//
//					GrammarAST originalNode = g.ast.getNodeWithTokenIndex(newTok.getTokenIndex());
//					if (originalNode != null) {
//						// update the AST node start/stop index to match the values
//						// of the corresponding node in the original parse tree.
//						elWithOpt.setTokenStartIndex(originalNode.getTokenStartIndex());
//						elWithOpt.setTokenStopIndex(originalNode.getTokenStopIndex());
//					}
//					else {
//						// the original AST node could not be located by index;
//						// make sure to assign valid values for the start/stop
//						// index so toTokenString will not throw exceptions.
//						elWithOpt.setTokenStartIndex(newTok.getTokenIndex());
//						elWithOpt.setTokenStopIndex(newTok.getTokenIndex());
//					}
//				}
//			}
//		}
	}

	/** Merge all the rules, token definitions, and named actions from
		imported grammars into the root grammar tree.  Perform:

	 	(tokens { X (= Y 'y')) + (tokens { Z )	-&gt;	(tokens { X (= Y 'y') Z)

	 	(@ members {foo}) + (@ members {bar})	-&gt;	(@ members {foobar})

	 	(RULES (RULE x y)) + (RULES (RULE z))	-&gt;	(RULES (RULE x y z))

	 	Rules in root prevent same rule from being appended to RULES node.

	 	The goal is a complete combined grammar so we can ignore subordinate
	 	grammars.
	 */
	public void integrateImportedGrammars(Grammar rootGrammar) {
		List<Grammar> imports = rootGrammar.getAllImportedGrammars();
		if ( imports==null ) return;

		GrammarRootAST root = rootGrammar.ast;
		ANTLRParser.IdentifierContext id = root.root.grammarDecl().identifier();

		List<ANTLRParser.ChannelsSpecContext> channelsRoot = root.getChannels();
	 	List<ANTLRParser.TokensSpecContext> tokensRoot = root.getTokensSpecs();

		List<ANTLRParser.ActionBlockContext> actionRoots = root.getActions();

		// Compute list of rules in root grammar and ensure we have a RULES node
		List<ANTLRParser.ParserRuleSpecContext> rootRules = root.getParserRules();
		Set<String> rootRuleNames = new HashSet<String>();
		// make list of rules we have in root grammar
		for (ANTLRParser.ParserRuleSpecContext r : rootRules) rootRuleNames.add(r.RULE_REF().getText());

		// make list of modes we have in root grammar
		Map<TerminalNode, List<ANTLRParser.LexerRuleSpecContext>> rootModes = root.getModes();
		Set<String> rootModeNames = new HashSet<String>();
		for (TerminalNode m : rootModes.keySet()) rootModeNames.add(m.getText());
		List<GrammarAST> addedModes = new ArrayList<GrammarAST>();

		for (Grammar imp : imports) {
			// COPY CHANNELS
			List<ANTLRParser.ChannelsSpecContext> imp_channelRoot = imp.ast.getChannels();
			if ( imp_channelRoot != null) {
				rootGrammar.tool.log("grammar", "imported channels: "+imp_channelRoot);
				if (channelsRoot==null) {
					channelsRoot = imp_channelRoot.dupTree();
					channelsRoot.g = rootGrammar;
					root.insertChild(1, channelsRoot); // ^(GRAMMAR ID TOKENS...)
				} else {
					for (int c = 0; c < imp_channelRoot.getChildCount(); ++c) {
						String channel = imp_channelRoot.getChild(c).getText();
						boolean channelIsInRootGrammar = false;
						for (int rc = 0; rc < channelsRoot.getChildCount(); ++rc) {
							String rootChannel = channelsRoot.getChild(rc).getText();
							if (rootChannel.equals(channel)) {
								channelIsInRootGrammar = true;
								break;
							}
						}
						if (!channelIsInRootGrammar) {
                            channelsRoot.addChild(imp_channelRoot.getChild(c).dupNode());
						}
					}
				}
			}

			// COPY TOKENS
			GrammarAST imp_tokensRoot = (GrammarAST)imp.ast.getFirstChildWithType(ANTLRParser.TOKENS_SPEC);
			if ( imp_tokensRoot!=null ) {
				rootGrammar.tool.log("grammar", "imported tokens: "+imp_tokensRoot.getChildren());
				if ( tokensRoot==null ) {
					tokensRoot = (GrammarAST)adaptor.create(ANTLRParser.TOKENS_SPEC, "TOKENS");
					tokensRoot.g = rootGrammar;
					root.insertChild(1, tokensRoot); // ^(GRAMMAR ID TOKENS...)
				}
				tokensRoot.addChildren(Arrays.asList(imp_tokensRoot.getChildren().toArray(new Tree[0])));
			}

			List<GrammarAST> all_actionRoots = new ArrayList<GrammarAST>();
			List<GrammarAST> imp_actionRoots = imp.ast.getAllChildrenWithType(ANTLRParser.AT);
			if ( actionRoots!=null ) all_actionRoots.addAll(actionRoots);
			all_actionRoots.addAll(imp_actionRoots);

			// COPY ACTIONS
			if ( imp_actionRoots!=null ) {
				DoubleKeyMap<String, String, GrammarAST> namedActions =
					new DoubleKeyMap<String, String, GrammarAST>();

				rootGrammar.tool.log("grammar", "imported actions: "+imp_actionRoots);
				for (GrammarAST at : all_actionRoots) {
					String scopeName = rootGrammar.getDefaultActionScope();
					GrammarAST scope, name, action;
					if ( at.getChildCount()>2 ) { // must have a scope
						scope = (GrammarAST)at.getChild(0);
						scopeName = scope.getText();
						name = (GrammarAST)at.getChild(1);
						action = (GrammarAST)at.getChild(2);
					}
					else {
						name = (GrammarAST)at.getChild(0);
						action = (GrammarAST)at.getChild(1);
					}
					GrammarAST prevAction = namedActions.get(scopeName, name.getText());
					if ( prevAction==null ) {
						namedActions.put(scopeName, name.getText(), action);
					}
					else {
						if ( prevAction.g == at.g ) {
							rootGrammar.tool.errMgr.grammarError(ErrorType.ACTION_REDEFINITION,
												at.g.fileName, name.token, name.getText());
						}
						else {
							String s1 = prevAction.getText();
							s1 = s1.substring(1, s1.length()-1);
							String s2 = action.getText();
							s2 = s2.substring(1, s2.length()-1);
							String combinedAction = "{"+s1 + '\n'+ s2+"}";
							prevAction.token.setText(combinedAction);
						}
					}
				}
				// at this point, we have complete list of combined actions,
				// some of which are already living in root grammar.
				// Merge in any actions not in root grammar into root's tree.
				for (String scopeName : namedActions.keySet()) {
					for (String name : namedActions.keySet(scopeName)) {
						GrammarAST action = namedActions.get(scopeName, name);
						rootGrammar.tool.log("grammar", action.g.name+" "+scopeName+":"+name+"="+action.getText());
						if ( action.g != rootGrammar ) {
							root.insertChild(1, action.getParent());
						}
					}
				}
	}


}
