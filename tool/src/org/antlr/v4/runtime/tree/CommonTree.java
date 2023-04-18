package org.antlr.v4.runtime.tree;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.tool.ast.GrammarAST;
import org.antlr.v4.tool.ast.RuleAST;

import java.util.List;

public class CommonTree {
	public Token token;
	protected ATNConfigSet children;
	public CommonTree parent;

	public CommonTree(Token t) {

	}

	public CommonTree(GrammarAST node) {

	}

	public CommonTree() {
	}

	public int getType() {
		return 0;
	}

	public int getChildCount() {
		return 0;
	}

	public CommonTree getChild(int i) {
		return null;
	}

	public Token getToken() {
		return null;
	}

	public String getText() {
		return null;
	}

	public String toStringTree() {
		return null;
	}

	public int getTokenStartIndex() {
		return 0;
	}

	public void setTokenStartIndex(Object tokenStartIndex) {

	}

	public int getTokenStopIndex() {
		return 0;
	}

	public void setTokenStopIndex(Object tokenStopIndex) {

	}

	public CharStream getInputStream() {
		return null;
	}

	public void setInputStream(CharStream input) {

	}

	protected List<? extends Tree> getAncestors() {
		return null;
	}

	public Object getFirstChildWithType(int channels) {
		return null;
	}

	public List<CommonTree> getChildren() {
		return null;
	}

	public void insertChild(int i, GrammarAST channelsRoot) {

	}

	public CommonTree dupNode() {
		return null;
	}

	public void addChild(CommonTree commonTree) {

	}

	public void addChildren(List<CommonTree> list) {

	}

	public GrammarAST getParent() {
		return null;
	}

	public void freshenParentAndChildIndexes() {

	}

	public void sanityCheckParentAndChildIndexes() {
	}

	public int getLine() {
		return 0;
	}

	public int getCharPositionInLine() {
		return 0;
	}

	public CommonTree getAncestor(int rule) {
		return null;
	}

	public int getChildIndex() {
		return 0;
	}

	public void setChild(int childIndex, RuleAST t) {

	}

	public boolean isNil() {
		return false;
	}
}
