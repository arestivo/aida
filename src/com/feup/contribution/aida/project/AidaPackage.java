package com.feup.contribution.aida.project;

import java.util.HashMap;
import java.util.Set;

import org.eclipse.core.resources.IResource;

public class AidaPackage {
	private String name;
	private HashMap<String, AidaUnit> units = new HashMap<String, AidaUnit>();
	
	public AidaPackage(String name) {
		this.name = name;
	}

	protected void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void addUnit(String name, IResource resource) {
		units.put(name, new AidaUnit(name, resource));		
	}

	public Set<String> getUnitNames() {
		return units.keySet();
	}

	public AidaUnit getUnit(String unitName) {
		return units.get(unitName);
	}
}
