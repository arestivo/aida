package com.feup.contribution.aida.builder;

import java.util.LinkedList;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

import com.feup.contribution.aida.AidaPlugin;

public class AidaASTVisitor extends ASTVisitor{
	private LinkedList<String> unitNames = new LinkedList<String>();
	
	@Override
	public boolean visit(MethodInvocation node) {
		String cn = node.resolveMethodBinding().getDeclaringClass().getName();
		String pn = node.resolveMethodBinding().getDeclaringClass().getPackage().getName();
		getUnitNames().add(pn+"."+cn);
		return super.visit(node);
	}

	public LinkedList<String> getUnitNames() {
		return unitNames;
	}
	
}
