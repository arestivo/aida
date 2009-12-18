package com.feup.contribution.aida.project;

import java.util.HashSet;

import org.eclipse.core.resources.IResource;

public class AidaUnit {
	private String name;
	private IResource resource;
	private String completeName;
	private HashSet<String> referencedUnits = new HashSet<String>();
	private HashSet<String> mandatoryUnits = new HashSet<String>();

	public AidaUnit(String name, String completeName, IResource resource) {
		this.completeName = completeName;
		this.setName(name);
		this.setResource(resource);
	}

	protected void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	protected void setResource(IResource resource) {
		this.resource = resource;
	}

	public IResource getResource() {
		return resource;
	}

	public String getCompleteName() {
		return completeName;
	}

	public void addReferencedUnits(HashSet<String> unitNames) {
		this.referencedUnits.addAll(unitNames);
	}

	public HashSet<String> getReferencedUnits() {
		return referencedUnits;
	}

	public String getFullPath() {
		return getResource().getFullPath().toOSString(); 
	}

	public void addMandatoryUnits(HashSet<String> unitNames) {
		this.mandatoryUnits.addAll(unitNames);
	}

	public HashSet<String> getMandatoryUnits() {
		return mandatoryUnits;
	}	
}
