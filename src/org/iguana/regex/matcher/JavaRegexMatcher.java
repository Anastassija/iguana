/*
 * Copyright (c) 2015, Ali Afroozeh and Anastasia Izmaylova, Centrum Wiskunde & Informatica (CWI)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this 
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this 
 *    list of conditions and the following disclaimer in the documentation and/or 
 *    other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
 * OF SUCH DAMAGE.
 *
 */

package org.iguana.regex.matcher;

import java.util.regex.Pattern;

import org.iguana.regex.RegularExpression;
import org.iguana.traversal.RegularExpressionVisitor;
import org.iguana.traversal.ToJavaRegexVisitor;
import org.iguana.util.Input;
import org.iguana.util.IntArrayCharSequence;

/**
 * 
 * @author Ali Afroozeh
 *
 */
public class JavaRegexMatcher implements Matcher {
	
	private final Pattern pattern;
	
	public JavaRegexMatcher(RegularExpression regex) {
		RegularExpressionVisitor<String> visitor = new ToJavaRegexVisitor();
		this.pattern = Pattern.compile(regex.accept(visitor));
	}
	
	@Override
	public int match(Input input, int i) {
        IntArrayCharSequence charSeq = input.asCharSequence();
        java.util.regex.Matcher matcher = pattern.matcher(charSeq);
        matcher.region(i, charSeq.length());
		if (matcher.lookingAt()) {
			return charSeq.logicalIndexAt(matcher.end()) - i;									
		}
		return -1;
	}

}
