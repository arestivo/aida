package com.feup.contribution.aida.project;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

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
		AidaPlugin.getDefault().log(" ============================ ");
		for (String packageName : packages.keySet()) {
			String l = packageName + " -> ";
			AidaPackage apackage = packages.get(packageName);
			LinkedList<AidaPackage> referenced = apackage.getReferencedPackages();
			for (AidaPackage aidaPackage : referenced) {
				l += ", " + aidaPackage.getName();
			}
			AidaPlugin.getDefault().log(l);
		}		
	}

	public Collection<AidaPackage> getPackagesForUnit(String unitName) {
		HashSet<AidaPackage> ret = new HashSet<AidaPackage>();
		for (String pName : packages.keySet()) {
			AidaPackage apackage = packages.get(pName);
			for (String unit : apackage.getUnitNames()) {
				if (unit.equals(unitName)) ret.add(apackage);
				if (unit.startsWith(unitName.substring(0, unitName.length()-1))) ret.add(apackage);
			}
		}
		return ret;
	}

	public void resolveDependencies() {
		for (String pName : packages.keySet()) {
			AidaPackage apackage = packages.get(pName);
			apackage.resolveDependencies(this);
		}
	}

	public LinkedList<AidaPackage> getPackages() {
		return new LinkedList<AidaPackage>(packages.values());
	}
}
