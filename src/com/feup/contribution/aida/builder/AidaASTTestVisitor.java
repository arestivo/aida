package com.feup.contribution.aida.builder;

import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class AidaASTTestVisitor extends ASTVisitor{
	private HashSet<String> tests = new HashSet<String>();
	private HashMap<String, HashSet<String>> replaces = new HashMap<String, HashSet<String>>();
	
	@Override
	public boolean visit(MethodDeclaration node) {
		String name = node.getName().getFullyQualifiedName();
		if (name.startsWith("test")) {
			tests.add(name);
			replaces.put(name, new HashSet<String>());

			IAnnotationBinding[] annotation = node.resolveBinding().getAnnotations();
			for (int i = 0; i < annotation.length; i++) {
				if (annotation[i].getName().equals("ReplaceTest")) {
					IMemberValuePairBinding[] members = annotation[i].getAllMemberValuePairs();
					for (int j = 0; j < members.length; j++) {
						if (members[j].getName().equals("value")) {
							String rep = members[j].getValue().toString();
							replaces.get(name).add(rep);
						}
					}
				}
			}
		}		
		return super.visit(node);
	}

	public HashSet<String> getTestNames() {
		return tests;
	}

	public HashMap<String,HashSet<String>> getReplaces() {
		return replaces;
	}

}
