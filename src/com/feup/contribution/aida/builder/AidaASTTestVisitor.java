package com.feup.contribution.aida.builder;

import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class AidaASTTestVisitor extends ASTVisitor{
	private HashSet<String> testNames = new HashSet<String>();
	
	@Override
	public boolean visit(MethodDeclaration node) {
		String name = node.getName().getFullyQualifiedName();
		if (name.startsWith("test")) testNames.add(name);
		return super.visit(node);
	}

	public HashSet<String> getTestNames() {
		return testNames;
	}
}
