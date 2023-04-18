package org.antlr.v4.runtime.tree;

public abstract class TreeVisitorAction {
	public abstract Object pre(Object t);

	public abstract Object post(Object t);
}
