/*
 * Copyright (c) 2015, CWI
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

package org.iguana.datadependent.traversal;

import org.iguana.datadependent.ast.Statement;
import org.iguana.datadependent.ast.VariableDeclaration;
import org.iguana.datadependent.ast.Expression.AndIndent;
import org.iguana.datadependent.ast.Expression.Assignment;
import org.iguana.datadependent.ast.Expression.Boolean;
import org.iguana.datadependent.ast.Expression.Call;
import org.iguana.datadependent.ast.Expression.EndOfFile;
import org.iguana.datadependent.ast.Expression.Equal;
import org.iguana.datadependent.ast.Expression.Greater;
import org.iguana.datadependent.ast.Expression.GreaterThanEqual;
import org.iguana.datadependent.ast.Expression.Integer;
import org.iguana.datadependent.ast.Expression.LShiftANDEqZero;
import org.iguana.datadependent.ast.Expression.LeftExtent;
import org.iguana.datadependent.ast.Expression.Less;
import org.iguana.datadependent.ast.Expression.LessThanEqual;
import org.iguana.datadependent.ast.Expression.Name;
import org.iguana.datadependent.ast.Expression.NotEqual;
import org.iguana.datadependent.ast.Expression.Or;
import org.iguana.datadependent.ast.Expression.OrIndent;
import org.iguana.datadependent.ast.Expression.Real;
import org.iguana.datadependent.ast.Expression.RightExtent;
import org.iguana.datadependent.ast.Expression.String;

public interface IAbstractASTVisitor<T> {
	
	public T visit(Boolean expression);
	
	public T visit(Integer expression);
	
	public T visit(Real expression);
	
	public T visit(String expression);
	
	public T visit(Name expression);
	
	public T visit(Call expression);
	
	public T visit(Assignment expression);
	
	public T visit(LShiftANDEqZero expression);
	
	public T visit(OrIndent expression);
	
	public T visit(AndIndent expression);
	
	public T visit(Or expression);
	
	public T visit(Less expression);
	
	public T visit(LessThanEqual expression);
	
	public T visit(Greater expression);
	
	public T visit(GreaterThanEqual expression);
	
	public T visit(Equal expression);
	
	public T visit(NotEqual expression);
	
	public T visit(LeftExtent expression);
	
	public T visit(RightExtent expression);
	
	public T visit(EndOfFile expression);
	
	public T visit(VariableDeclaration declaration);
	
	public T visit(Statement.Expression statement);
	
	public T visit(Statement.VariableDeclaration statement);

}
