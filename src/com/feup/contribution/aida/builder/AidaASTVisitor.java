package com.feup.contribution.aida.builder;

import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class AidaASTVisitor extends ASTVisitor{
	private HashSet<String> unitNames = new HashSet<String>();
	
	@Override
	public boolean visit(ImportDeclaration node) {
		IBinding binding = node.resolveBinding();
		if (binding instanceof ITypeBinding) 
			addBinding(((ITypeBinding) binding));
		if (binding instanceof IPackageBinding)
			addBinding(((IPackageBinding) binding));
		return super.visit(node);
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		addBinding(node.resolveTypeBinding());
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		for (ITypeBinding binding : node.resolveBinding().getInterfaces()) {
			addBinding(binding);
		}
		addBinding(node.resolveBinding().getSuperclass());
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodInvocation node) {
		addBinding(node.resolveMethodBinding().getDeclaringClass());
		return super.visit(node);
	}

	private void addBinding(ITypeBinding binding) {
		if (binding == null) return;
		String cn = binding.getName();
		String pn = binding.getPackage().getName();
		unitNames.add(pn+"."+cn);
	}

	private void addBinding(IPackageBinding binding) {
		if (binding == null) return;
		String pn = binding.getName();
		unitNames.add(pn+".*");
	}
	
	public HashSet<String> getUnitNames() {
		return unitNames;
	}
}
