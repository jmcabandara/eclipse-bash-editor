/*
 * Copyright 2017 Albert Tregnaghi
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
package de.jcup.basheditor.scriptmodel;

import java.util.ArrayList;
import java.util.List;

/**
 * A bash script model builder
 * 
 * @author Albert Tregnaghi
 *
 */
public class BashScriptModelBuilder {
	private boolean ignoreDoValidation;
	private boolean ignoreBlockValidation;
	private boolean ignoreIfValidation;
	private boolean ignoreFunctionValidation;

	/**
	 * Parses given script and creates a bash script model
	 * 
	 * @param bashScript
	 * @return a simple model with some information about bash script
	 */
	public BashScriptModel build(String bashScript) {
		BashScriptModel model = new BashScriptModel();

		TokenParser parser = new TokenParser();
		List<ParseToken> tokens = parser.parse(bashScript);

		buildFunctionsByTokens(model, tokens);

		List<ValidationResult> results = new ArrayList<>();
		for (BashScriptValidator<List<ParseToken>> validator : createParseTokenValidators()) {
			results.addAll(validator.validate(tokens));
		}

		for (ValidationResult result : results) {
			if (result instanceof BashError) {
				model.errors.add((BashError) result);
			}
		}
		return model;
	}

	public void setIgnoreBlockValidation(boolean ignoreBlockValidation) {
		this.ignoreBlockValidation = ignoreBlockValidation;
	}

	public void setIgnoreDoValidation(boolean ignoreDoValidation) {
		this.ignoreDoValidation = ignoreDoValidation;
	}

	public void setIgnoreIfValidation(boolean ignoreIfValidation) {
		this.ignoreIfValidation = ignoreIfValidation;
	}
	
	public void setIgnoreFunctionValidation(boolean ignoreFunctionValidation) {
		this.ignoreFunctionValidation = ignoreFunctionValidation;
	}
	

	private List<BashScriptValidator<List<ParseToken>>> createParseTokenValidators() {
		List<BashScriptValidator<List<ParseToken>>> validators = new ArrayList<>();
		if (!ignoreDoValidation) {
			validators.add(new DoEndsWithDoneValidator());
		}
		if (!ignoreBlockValidation) {
			validators.add(new ClosedBlocksValidator());
		}
		if (!ignoreIfValidation) {
			validators.add(new IfEndsWithFiValidator());
		}
		return validators;
	}

	private void buildFunctionsByTokens(BashScriptModel model, List<ParseToken> tokens) {

		for (int tokenNr = 0; tokenNr < tokens.size(); tokenNr++) {
			int currentTokenNr = tokenNr;
			ParseToken token = tokens.get(currentTokenNr++);
			boolean isFunctionName = false;
			Integer functionStart = null;
			int functionEnd = 0;
			/* ++++++++++++++++++++++ */
			/* + Scan for functions + */
			/* ++++++++++++++++++++++ */
			/* could be 'function MethodName()' or 'function MethodName()' */
			if (token.isFunctionKeyword() && hasPos(currentTokenNr, tokens)) {
				isFunctionName = true;
				functionStart = Integer.valueOf(token.start);
				token = tokens.get(currentTokenNr++);
			}
			/* could be 'MethodName()' */
			isFunctionName = isFunctionName || token.isFunctionName();
			if (!isFunctionName) {
				/* could be 'MethodName ()' */
				if (hasPos(currentTokenNr, tokens)) {
					ParseToken followToken = tokens.get(currentTokenNr++);
					isFunctionName = followToken.hasLength(2) && followToken.endsWithFunctionBrackets();
				}
			}
			if (isFunctionName) {
				if (functionStart == null) {
					functionStart = Integer.valueOf(token.start);
				}
				String functionName = token.getTextAsFunctionName();
				functionEnd = token.end;
				/* ++++++++++++++++++++++++++++++ */
				/* + Scan for curly braces open + */
				/* ++++++++++++++++++++++++++++++ */

				if (!hasPos(currentTokenNr, tokens)) {
					if (!ignoreFunctionValidation){
						model.errors.add(createBashErrorFunctionMissingCurlyBrace(token, functionName));
					}
					break;
				}
				ParseToken openCurlyBraceToken = tokens.get(currentTokenNr++);
				if (!openCurlyBraceToken.isOpenBlock()) {
					if (!ignoreFunctionValidation){
						model.errors.add(createBashErrorFunctionMissingCurlyBrace(token, functionName));
					}
					continue;
				}
				/* +++++++++++++++++++++++++++++++ */
				/* + Scan for curly braces close + */
				/* +++++++++++++++++++++++++++++++ */

				BashFunction function = new BashFunction();
				function.lengthToNameEnd = functionEnd - functionStart.intValue();
				function.position = functionStart.intValue();
				function.name = functionName;
				function.end = -1;

				while (hasPos(currentTokenNr, tokens)) {
					ParseToken closeCurlyBraceToken = tokens.get(currentTokenNr++);
					if (closeCurlyBraceToken.isCloseBlock()) {
						function.end = closeCurlyBraceToken.end;
						break;
					}
				}
				if (function.end == -1) {
					/* no close block found - mark this as an error */
					if (!ignoreFunctionValidation){
						model.errors.add(createBashErrorCloseFunctionCurlyBraceMissing(functionName, openCurlyBraceToken));
					}
					break;
				}

				model.functions.add(function);
				/*
				 * function created - last currentTokenNr++ was too much because
				 * it will be done by loop to- so reduce with 1
				 */
				tokenNr = currentTokenNr - 1;
			}
		}
	}

	private BashError createBashErrorCloseFunctionCurlyBraceMissing(String functionName,
			ParseToken openCurlyBraceToken) {
		return new BashError(openCurlyBraceToken.start, openCurlyBraceToken.end,
				"This curly brace is not closed. So function '" + functionName + "' is not valid.");
	}

	private BashError createBashErrorFunctionMissingCurlyBrace(ParseToken token, String functionName) {
		return new BashError(token.start, token.end,
				"The function '" + functionName + "' is not valid because no opening curly brace found.");
	}

	private boolean hasPos(int pos, List<?> elements) {
		if (elements == null) {
			return false;
		}
		return pos < elements.size();
	}

}
