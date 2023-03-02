/*
 * [The "BSD license"]
 *  Copyright (c) 2012-2016 Terence Parr
 *  Copyright (c) 2012-2016 Sam Harwell
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

grammar Action;


@parser::header {
import org.antlr.v4.tool.*;
import org.antlr.v4.tool.ast.*;
}

@parser::members {
ActionSplitterListener delegate;

public ActionParser(TokenStream input, ActionSplitterListener delegate) {
    this(input);
    this.delegate = delegate;
}

private boolean isIDStartChar(int c) {
	return c == '_' || Character.isLetter(c);
}
}

action: (comment | line_comment | set_nonlocal_attr |nonlocal_attr | qualified_attr|set_attr|set_nonlocal_attr|text_)+;
// ignore comments right away

comment
    :   '/*' .*? '*/' {delegate.text($text);}
    ;

line_comment
    : '//' ~('\n'|'\r')* '\r'? '\n' {delegate.text($text);}
    ;

set_nonlocal_attr
	:	'$' x=ID '::' y=ID WS? '=' expr=ATTR_VALUE_EXPR ';'
		{
		delegate.setNonLocalAttr($text, $x, $y, $expr);
		}
	;

nonlocal_attr
	:	'$' x=ID '::' y=ID {delegate.nonLocalAttr($text, $x, $y);}
	;

qualified_attr
	:	'$' x=ID '.' y=ID {_input.LA(1)!='('}? {delegate.qualifiedAttr($text, $x, $y);}
	;

set_attr
	:	'$' x=ID WS? '=' expr=ATTR_VALUE_EXPR ';'
		{
		delegate.setAttr($text, $x, $expr);
		}
	;

attr
	:	'$' x=ID {delegate.attr($text, $x);}
	;

// Anything else is just random text

text_
	:
{StringBuilder buf = new StringBuilder();}
		(	c=~('\\'| '$') {buf.append($c);}
		|	'\\$' {buf.append('$');}
		|	'\\' c=~('$') {buf.append('\\').append($c);}
		|	{!isIDStartChar(_input.LA(2))}? '$' {buf.append('$');}
		)+
{delegate.text(buf.toString());}
	;

ID  :	('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
    ;

/** Don't allow an = as first char to prevent $x == 3; kind of stuff. */
ATTR_VALUE_EXPR
	:	~'=' (~';')*
	;

WS	:	(' '|'\t'|'\n'|'\r')+
	;

