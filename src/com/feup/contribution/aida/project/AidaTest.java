package com.feup.contribution.aida.project;

import java.util.HashSet;

import org.eclipse.core.resources.IResource;


public class AidaTest {
	private String methodName;
	private String className;
	private String packageName;
	private IResource resource;
	private HashSet<String> replaces;
	
	public AidaTest(String methodName, String className, String packageName, IResource resource) {
		this.methodName = methodName;
		this.className = className;
		this.packageName = packageName;
		this.resource = resource;
		replaces = new HashSet<String>();
	}

	public String getPackageName() {
		return packageName;
	}
	
	public String getClassName() {
		return className;
	}
	
	public String getMethodName() {
		return methodName;
	}

	public String getFullPath() {
		return getResource().getFullPath().toOSString(); 
	}

	private IResource getResource() {
		return resource;
	}

	public String getFullName() {
		return packageName + "." + className + "." + methodName;
	}

	public void addReplaces(HashSet<String> replaces) {
		this.replaces.addAll(replaces);
	}

	public HashSet<String> getReplaces() {
		return replaces;
	}	

}
