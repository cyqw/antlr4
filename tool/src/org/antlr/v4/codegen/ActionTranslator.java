/*
 * Copyright (c) 2012-2017 The ANTLR Project. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 */

package org.antlr.v4.codegen;

import org.antlr.v4.codegen.model.RuleFunction;
import org.antlr.v4.codegen.model.chunk.*;
import org.antlr.v4.codegen.model.decl.StructDecl;
import org.antlr.v4.parse.ActionSplitter;
import org.antlr.v4.parse.ActionSplitterListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.tool.Attribute;
import org.antlr.v4.tool.ErrorType;
import org.antlr.v4.tool.Rule;
import org.antlr.v4.tool.ast.ActionAST;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** */
public class ActionTranslator implements ActionSplitterListener {
	public static final Map<String, Class<? extends RulePropertyRef>> thisRulePropToModelMap =
		new HashMap<String, Class<? extends RulePropertyRef>>();
	static {
		thisRulePropToModelMap.put("start", ThisRulePropertyRef_start.class);
		thisRulePropToModelMap.put("stop",  ThisRulePropertyRef_stop.class);
		thisRulePropToModelMap.put("text",  ThisRulePropertyRef_text.class);
		thisRulePropToModelMap.put("ctx",   ThisRulePropertyRef_ctx.class);
		thisRulePropToModelMap.put("parser",  ThisRulePropertyRef_parser.class);
	}

	public static final Map<String, Class<? extends RulePropertyRef>> rulePropToModelMap =
		new HashMap<String, Class<? extends RulePropertyRef>>();
	static {
		rulePropToModelMap.put("start", RulePropertyRef_start.class);
		rulePropToModelMap.put("stop",  RulePropertyRef_stop.class);
		rulePropToModelMap.put("text",  RulePropertyRef_text.class);
		rulePropToModelMap.put("ctx",   RulePropertyRef_ctx.class);
		rulePropToModelMap.put("parser",  RulePropertyRef_parser.class);
	}

	public static final Map<String, Class<? extends TokenPropertyRef>> tokenPropToModelMap =
		new HashMap<String, Class<? extends TokenPropertyRef>>();
	static {
		tokenPropToModelMap.put("text",  TokenPropertyRef_text.class);
		tokenPropToModelMap.put("type",  TokenPropertyRef_type.class);
		tokenPropToModelMap.put("line",  TokenPropertyRef_line.class);
		tokenPropToModelMap.put("index", TokenPropertyRef_index.class);
		tokenPropToModelMap.put("pos",   TokenPropertyRef_pos.class);
		tokenPropToModelMap.put("channel", TokenPropertyRef_channel.class);
		tokenPropToModelMap.put("int",   TokenPropertyRef_int.class);
	}

	final CodeGenerator gen;
	final Target target;
	final ActionAST node;
	RuleFunction rf;
	final List<ActionChunk> chunks = new ArrayList<ActionChunk>();
	final OutputModelFactory factory;
	StructDecl nodeContext;

	public ActionTranslator(OutputModelFactory factory, ActionAST node) {
		this.factory = factory;
		this.node = node;
		this.gen = factory.getGenerator();
		this.target = gen.getTarget();
	}

	public static String toString(List<ActionChunk> chunks) {
		StringBuilder buf = new StringBuilder();
		for (ActionChunk c : chunks) buf.append(c.toString());
		return buf.toString();
	}

	public static List<ActionChunk> translateAction(OutputModelFactory factory,
													RuleFunction rf,
													Token tokenWithinAction,
													ActionAST node)
	{
		String action = tokenWithinAction.getText();
		if ( action!=null && action.length()>0 && action.charAt(0)=='{' ) {
			int firstCurly = action.indexOf('{');
			int lastCurly = action.lastIndexOf('}');
			if ( firstCurly>=0 && lastCurly>=0 ) {
				action = action.substring(firstCurly+1, lastCurly); // trim {...}
			}
		}
		return translateActionChunk(factory, rf, action, node);
	}

	public static List<ActionChunk> translateActionChunk(OutputModelFactory factory,
														 RuleFunction rf,
														 String action,
														 ActionAST node)
	{
		Token tokenWithinAction = node.token;
		ActionTranslator translator = new ActionTranslator(factory, node);
		translator.rf = rf;
        factory.getGrammar().tool.log("action-translator", "translate " + action);
		String altLabel = node.getAltLabel();
		if ( rf!=null ) {
		    translator.nodeContext = rf.ruleCtx;
	        if ( altLabel!=null ) translator.nodeContext = rf.altLabelCtxs.get(altLabel);
		}
		CharStream in = CharStreams.fromString(action);
		ActionSplitter trigger = new ActionSplitter(in, translator);
		// forces eval, triggers listener methods
		trigger.getActionTokens();
		return translator.chunks;
	}

	@Override
	public void attr(String expr, Token x) {
		gen.g.tool.log("action-translator", "attr "+x);
		Attribute a = node.resolver.resolveToAttribute(x.getText(), node);
		String name = x.getText();
		String escapedName = target.escapeIfNeeded(name);
		if ( a!=null ) {
			switch ( a.dict.type ) {
				case ARG:
					chunks.add(new ArgRef(nodeContext, name, escapedName));
					break;
				case RET:
					chunks.add(new RetValueRef(rf.ruleCtx, name, escapedName));
					break;
				case LOCAL:
					chunks.add(new LocalRef(nodeContext, name, escapedName));
					break;
				case PREDEFINED_RULE:
					chunks.add(getRulePropertyRef(null, x));
					break;
				default:
					break;
			}
		}
		if ( node.resolver.resolvesToToken(name, node) ) {
			String tokenLabel = getTokenLabel(name);
			chunks.add(new TokenRef(nodeContext, tokenLabel, target.escapeIfNeeded(tokenLabel))); // $label
			return;
		}
		if ( node.resolver.resolvesToLabel(name, node) ) {
			String tokenLabel = getTokenLabel(name);
			chunks.add(new LabelRef(nodeContext, tokenLabel, target.escapeIfNeeded(tokenLabel))); // $x for x=ID etc...
			return;
		}
		if ( node.resolver.resolvesToListLabel(name, node) ) {
			chunks.add(new ListLabelRef(nodeContext, name, escapedName)); // $ids for ids+=ID etc...
			return;
		}
		Rule r = factory.getGrammar().getRule(name);
		if ( r!=null ) {
			String ruleLabel = getRuleLabel(name);
			chunks.add(new LabelRef(nodeContext, ruleLabel, target.escapeIfNeeded(ruleLabel))); // $r for r rule ref
		}
	}

	@Override
	public void qualifiedAttr(String expr, Token x, Token y) {
		gen.g.tool.log("action-translator", "qattr "+x+"."+y);
		if ( node.resolver.resolveToAttribute(x.getText(), node)!=null ) {
			// must be a member access to a predefined attribute like $ctx.foo
			attr(expr, x);
			chunks.add(new ActionText(nodeContext, "."+y.getText()));
			return;
		}
		Attribute a = node.resolver.resolveToAttribute(x.getText(), y.getText(), node);
		if ( a==null ) {
			// Added in response to https://github.com/antlr/antlr4/issues/1211
			gen.g.tool.errMgr.grammarError(ErrorType.UNKNOWN_SIMPLE_ATTRIBUTE,
			                               gen.g.fileName, x,
			                               x.getText(),
			                               "rule");
			return;
		}
		switch ( a.dict.type ) {
			case ARG: chunks.add(new ArgRef(nodeContext, y.getText(), target.escapeIfNeeded(y.getText()))); break; // has to be current rule
			case RET:
				chunks.add(new QRetValueRef(nodeContext, getRuleLabel(x.getText()), y.getText(), target.escapeIfNeeded(y.getText())));
				break;
			case PREDEFINED_RULE:
				chunks.add(getRulePropertyRef(x, y));
				break;
			case TOKEN:
				chunks.add(getTokenPropertyRef(x, y));
				break;
			default:
				break;
		}
	}

	@Override
	public void setAttr(String expr, Token x, Token rhs) {
		gen.g.tool.log("action-translator", "setAttr "+x+" "+rhs);
		List<ActionChunk> rhsChunks = translateActionChunk(factory,rf,rhs.getText(),node);
		String name = x.getText();
		SetAttr s = new SetAttr(nodeContext, name, target.escapeIfNeeded(name), rhsChunks);
		chunks.add(s);
	}

	@Override
	public void nonLocalAttr(String expr, Token x, Token y) {
		gen.g.tool.log("action-translator", "nonLocalAttr "+x+"::"+y);
		Rule r = factory.getGrammar().getRule(x.getText());
		String name = y.getText();
		chunks.add(new NonLocalAttrRef(nodeContext, x.getText(), name, target.escapeIfNeeded(name), r.index));
	}

	@Override
	public void setNonLocalAttr(String expr, Token x, Token y, Token rhs) {
		gen.g.tool.log("action-translator", "setNonLocalAttr "+x+"::"+y+"="+rhs);
		Rule r = factory.getGrammar().getRule(x.getText());
		List<ActionChunk> rhsChunks = translateActionChunk(factory,rf,rhs.getText(),node);
		String name = y.getText();
		SetNonLocalAttr s = new SetNonLocalAttr(nodeContext, x.getText(), name, target.escapeIfNeeded(name), r.index, rhsChunks);
		chunks.add(s);
	}

	@Override
	public void text(String text) {
		chunks.add(new ActionText(nodeContext,text));
	}

	TokenPropertyRef getTokenPropertyRef(Token x, Token y) {
		try {
			Class<? extends TokenPropertyRef> c = tokenPropToModelMap.get(y.getText());
			Constructor<? extends TokenPropertyRef> ctor = c.getConstructor(StructDecl.class, String.class);
			return ctor.newInstance(nodeContext, getTokenLabel(x.getText()));
		}
		catch (Exception e) {
			factory.getGrammar().tool.errMgr.toolError(ErrorType.INTERNAL_ERROR, e);
		}
		return null;
	}

	RulePropertyRef getRulePropertyRef(Token x, Token prop) {
		try {
			Class<? extends RulePropertyRef> c = (x != null ? rulePropToModelMap : thisRulePropToModelMap).get(prop.getText());
			Constructor<? extends RulePropertyRef> ctor = c.getConstructor(StructDecl.class, String.class);
			return ctor.newInstance(nodeContext, getRuleLabel((x != null ? x : prop).getText()));
		}
		catch (Exception e) {
			factory.getGrammar().tool.errMgr.toolError(ErrorType.INTERNAL_ERROR, e, prop.getText());
		}
		return null;
	}

	public String getTokenLabel(String x) {
		if ( node.resolver.resolvesToLabel(x, node) ) return x;
		return target.getImplicitTokenLabel(x);
	}

	public String getRuleLabel(String x) {
		if ( node.resolver.resolvesToLabel(x, node) ) return x;
		return target.getImplicitRuleLabel(x);
	}
}
