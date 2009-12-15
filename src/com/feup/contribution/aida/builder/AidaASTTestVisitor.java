package com.feup.contribution.aida.builder;

import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class AidaASTTestVisitor extends ASTVisitor{
	private HashSet<String> testNames = new HashSet<String>();
	
	@Override
	public boolean visit(MethodInvocation node) {
		if (node.resolveMethodBinding() != null)
			testNames.add(node.resolveMethodBinding().getName());
		return super.visit(node);
	}

	public HashSet<String> getTestNames() {
		return testNames;
	}
}
