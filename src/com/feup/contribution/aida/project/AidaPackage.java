package com.feup.contribution.aida.project;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.core.resources.IResource;

public class AidaPackage {
	private String name;
	private HashMap<String, AidaUnit> units = new HashMap<String, AidaUnit>();
	private LinkedList<AidaPackage> referencedPackages = new LinkedList<AidaPackage>();
	
	public AidaPackage(String name) {
		this.name = name;
	}

	protected void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public AidaUnit addUnit(String name, String completeName, IResource resource) {
		AidaUnit unit = new AidaUnit(name, completeName, resource);
		units.put(completeName, unit);
		return unit;
	}

	public Set<String> getUnitNames() {
		return units.keySet();
	}

	public AidaUnit getUnit(String unitName) {
		return units.get(unitName);
	}
	
	public void resolveDependencies(AidaProject project) {
		for (String unitName : units.keySet()) {
			AidaUnit unit = units.get(unitName);
			LinkedList<String> referencedUnits = unit.getReferencedUnits();
			for (String rUnit : referencedUnits) {
				AidaPackage apackage = project.getPackageForUnit(rUnit);
				if (apackage != null) referencedPackages.add(apackage);
			}
		}
	}

	public LinkedList<AidaPackage> getReferencedPackages() {
		return referencedPackages;
	}
}
