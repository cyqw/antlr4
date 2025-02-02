/*
 * Copyright (c) 2012-2017 The ANTLR Project. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 */

package org.antlr.v4.misc;

import org.antlr.v4.parse.ANTLRParser.LexerRuleSpecContext;
import org.antlr.v4.parse.ANTLRParser.ParserRuleSpecContext;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.IntegerList;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.Trees;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/** */
public class Utils {
	public static final int INTEGER_POOL_MAX_VALUE = 1000;

	public static String getRuleName(ParserRuleContext node) {
		LexerRuleSpecContext lexerRule = getParent(node, LexerRuleSpecContext.class);
		if (lexerRule != null) {
			return lexerRule.TOKEN_REF().getText();
		} else {
			ParserRuleSpecContext parserRule = getParent(node, ParserRuleSpecContext.class);
			if (parserRule != null) {
				return parserRule.RULE_REF().getText();
			}
		}
		return null;
	}

	public static <T> T getParent(ParseTree node, Class<T> typeClass) {
		ParseTree parent = node.getParent();
		while (parent != null) {
			if (parent.getClass().equals(typeClass)) {
				return (T)parent;
			}
			parent = parent.getParent();
		}
		return null;
	}

	public static <T> T getFirstFirstChildWithType(ParseTree node, Class<T> typeClass) {
		return (T)Trees.findNodeSuchThat(node, it -> it.getClass().equals(typeClass));
	}

	public static TerminalNode getFirstTokenNode(ParserRuleContext node) {
		return (TerminalNode) Trees.findNodeSuchThat(node, TerminalNode.class::isInstance);
	}

	public interface Filter<T> {
		boolean select(T t);
	}

	public interface Func0<TResult> {
		TResult exec();
	}

	public interface Func1<T1, TResult> {
		TResult exec(T1 arg1);
	}

    public static String stripFileExtension(String name) {
        if ( name==null ) return null;
        int lastDot = name.lastIndexOf('.');
        if ( lastDot<0 ) return name;
        return name.substring(0, lastDot);
    }

	public static String join(Object[] a, String separator) {
		StringBuilder buf = new StringBuilder();
		for (int i=0; i<a.length; i++) {
			Object o = a[i];
			buf.append(o.toString());
			if ( (i+1)<a.length ) {
				buf.append(separator);
			}
		}
		return buf.toString();
	}

	public static String sortLinesInString(String s) {
		String lines[] = s.split("\n");
		Arrays.sort(lines);
		List<String> linesL = Arrays.asList(lines);
		StringBuilder buf = new StringBuilder();
		for (String l : linesL) {
			buf.append(l);
			buf.append('\n');
		}
		return buf.toString();
	}

	public static <T extends TerminalNode> List<String> nodesToStrings(List<T> nodes) {
		if ( nodes == null ) return null;
		List<String> a = new ArrayList<String>();
		for (T t : nodes) a.add(t.getText());
		return a;
	}

//	public static <T> List<T> list(T... values) {
//		List<T> x = new ArrayList<T>(values.length);
//		for (T v : values) {
//			if ( v!=null ) x.add(v);
//		}
//		return x;
//	}

	public static void writeSerializedATNIntegerHistogram(String filename, IntegerList serializedATN) {
		HashMap<Integer, Integer> histo = new HashMap<>();
		for (int i : serializedATN.toArray()) {
			if ( histo.containsKey(i) ) {
				histo.put(i, histo.get(i) + 1);
			}
			else {
				histo.put(i, 1);
			}
		}
		TreeMap<Integer,Integer> sorted = new TreeMap<>(histo);

		String output = "";
		output += "value,count\n";
		for (int key : sorted.keySet()) {
			output += key+","+sorted.get(key)+"\n";
		}
		try {
			Files.write(Paths.get(filename), output.getBytes(StandardCharsets.UTF_8));
		}
		catch (IOException ioe) {
			System.err.println(ioe);
		}
	}

	public static String capitalize(String s) {
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	public static String decapitalize(String s) {
		return Character.toLowerCase(s.charAt(0)) + s.substring(1);
	}

	/** apply methodName to list and return list of results. method has
	 *  no args.  This pulls data out of a list essentially.
	 */
	public static <From,To> List<To> select(List<From> list, Func1<From, To> selector) {
		if ( list==null ) return null;
		List<To> b = new ArrayList<To>();
		for (From f : list) {
			b.add(selector.exec(f));
		}
		return b;
	}

	/** Find exact object type or subclass of cl in list */
	public static <T> T find(List<?> ops, Class<T> cl) {
		for (Object o : ops) {
			if ( cl.isInstance(o) ) return cl.cast(o);
//			if ( o.getClass() == cl ) return o;
		}
		return null;
	}

	public static <T> int indexOf(List<? extends T> elems, Filter<T> filter) {
		for (int i=0; i<elems.size(); i++) {
			if ( filter.select(elems.get(i)) ) return i;
		}
		return -1;
	}

	public static <T> int lastIndexOf(List<? extends T> elems, Filter<T> filter) {
		for (int i=elems.size()-1; i>=0; i--) {
			if ( filter.select(elems.get(i)) ) return i;
		}
		return -1;
	}

	public static void setSize(List<?> list, int size) {
		if (size < list.size()) {
			list.subList(size, list.size()).clear();
		}
		else {
			while (size > list.size()) {
				list.add(null);
			}
		}
	}

}
