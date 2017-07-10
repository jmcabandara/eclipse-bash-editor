/*
 * Copyright 2016 Albert Tregnaghi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 */
 package de.jcup.basheditor.document.keywords;

// see http://docs.oracle.com/javase/tutorial/java/nutsandbolts/_keywords.html
public enum BashLanguageKeyWords implements DocumentKeyWord {

	FOR("for"),

	GOTO("goto"),
	
	DO("do"),
	
	IF("if"),
	
	FI("fi"),
	
	THEN("then"),
	
	ELSE("else"),
	
	RETURN("return"),
	
	WHILE("while")

	;

	private String text;

	private BashLanguageKeyWords(String text) {
		this.text = text;
	}


	@Override
	public String getText() {
		return text;
	}
}