package org.antlr.v4.runtime;

import org.antlr.v4.runtime.misc.Interval;

public class ANTLRStringStream implements CharStream {
	public String name;

	public ANTLRStringStream(String text) {
	}

	public void setLine(int line) {

	}

	public void setCharPositionInLine(int charPositionInLine) {

	}

	@Override
	public String getText(Interval interval) {
		return null;
	}

	@Override
	public void consume() {

	}

	@Override
	public int LA(int i) {
		return 0;
	}

	@Override
	public int mark() {
		return 0;
	}

	@Override
	public void release(int marker) {

	}

	@Override
	public int index() {
		return 0;
	}

	@Override
	public void seek(int index) {

	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public String getSourceName() {
		return null;
	}
}
