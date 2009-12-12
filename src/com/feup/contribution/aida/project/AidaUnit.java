package com.feup.contribution.aida.project;

import org.eclipse.core.resources.IResource;

public class AidaUnit {
	private String name;
	private IResource resource;

	public AidaUnit(String name, IResource resource) {
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

}
