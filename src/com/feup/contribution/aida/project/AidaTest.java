package com.feup.contribution.aida.project;

import org.eclipse.core.resources.IResource;


public class AidaTest {
	private String methodName;
	private String className;
	private String packageName;
	private IResource resource;

	public AidaTest(String methodName, String className, String packageName, IResource resource) {
		this.methodName = methodName;
		this.className = className;
		this.packageName = packageName;
		this.resource = resource;
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

}
