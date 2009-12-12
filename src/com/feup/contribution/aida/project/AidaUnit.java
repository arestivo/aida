package com.feup.contribution.aida.project;

import java.util.LinkedList;

import org.eclipse.core.resources.IResource;

public class AidaUnit {
	private String name;
	private IResource resource;
	private String completeName;
	private LinkedList<String> referencedUnits;

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

	public void addReferencedUnits(LinkedList<String> unitNames) {
		this.referencedUnits = unitNames;		
	}

	public LinkedList<String> getReferencedUnits() {
		return referencedUnits;
	}	
}
