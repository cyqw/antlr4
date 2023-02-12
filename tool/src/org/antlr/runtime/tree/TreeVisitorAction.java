package org.antlr.runtime.tree;

public interface TreeVisitorAction {
	Object pre(Object t);

	Object post(Object t);
}
