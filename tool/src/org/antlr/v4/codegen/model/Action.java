/*
 * Copyright (c) 2012-2017 The ANTLR Project. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 */

package org.antlr.v4.codegen.model;

import org.antlr.v4.codegen.ActionTranslator;
import org.antlr.v4.codegen.OutputModelFactory;
import org.antlr.v4.codegen.model.chunk.ActionChunk;
import org.antlr.v4.codegen.model.chunk.ActionTemplate;
import org.antlr.v4.codegen.model.decl.StructDecl;
import org.antlr.v4.parse.ANTLRParser;
import org.stringtemplate.v4.ST;

import java.util.ArrayList;
import java.util.List;

/** */
public class Action extends RuleElement {
	@ModelElement public List<ActionChunk> chunks;

	public Action(OutputModelFactory factory, ANTLRParser.ActionBlockContext ast) {
		super(factory,ast);
		RuleFunction rf = factory.getCurrentRuleFunction();
		if (ast != null) {
			chunks = ActionTranslator.translateAction(factory, rf, ast.BEGIN_ACTION().getSymbol(), ast);
		}
		else {
			chunks = new ArrayList<ActionChunk>();
		}
		//System.out.println("actions="+chunks);
	}

	public Action(OutputModelFactory factory, StructDecl ctx, ST actionST) {
		super(factory, null);
		chunks = new ArrayList<ActionChunk>();
		chunks.add(new ActionTemplate(ctx, actionST));
	}

}
