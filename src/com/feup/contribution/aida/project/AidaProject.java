package com.feup.contribution.aida.project;

import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;

import com.feup.contribution.aida.AidaPlugin;

public class AidaProject {
	private static HashMap<String, AidaProject> projects = new HashMap<String, AidaProject>();
	private HashMap<String, AidaPackage> packages = new HashMap<String, AidaPackage>();
	
	private String name;

	protected AidaProject(String name) {
		this.setName(name);
	}
	
	public static AidaProject getProject(String name) {
		if (projects.containsKey(name)) return projects.get(name);
		AidaProject project = new AidaProject(name);
		projects.put(name, project);
		return project;
	}

	public AidaPackage getPackage(String name) {
		if (packages.containsKey(name)) return packages.get(name);
		AidaPackage apackage = new AidaPackage(name);
		packages.put(name, apackage);
		return apackage;
	}
	
	protected void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void reset() {
		packages.clear();		
	}

	public void logStructure() {
		for (String packageName : packages.keySet()) {
			AidaPlugin.getDefault().log(packageName);
			AidaPackage apackage = packages.get(packageName);
			for (String unitName : apackage.getUnitNames()) {
				AidaPlugin.getDefault().log("  " + unitName);
			}
			LinkedList<AidaPackage> referenced = apackage.getReferencedPackages();
			for (AidaPackage aidaPackage : referenced) {
				AidaPlugin.getDefault().log(" -> " + aidaPackage.getName());				
			}
		}		
	}

	public AidaPackage getPackageForUnit(String unitName) {
		AidaPlugin.getDefault().log("Looking for " + unitName);
		for (String pName : packages.keySet()) {
			AidaPackage apackage = packages.get(pName);
			for (String unit : apackage.getUnitNames()) {
				AidaPlugin.getDefault().log("Found " + unit);
				if (unit.equals(unitName)) return apackage;
			}
		}
		return null;
	}

	public void resolveDependencies() {
		for (String pName : packages.keySet()) {
			AidaPackage apackage = packages.get(pName);
			apackage.resolveDependencies(this);
		}
	}
}
