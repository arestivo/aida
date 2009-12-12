package com.feup.contribution.aida.project;

import com.feup.contribution.aida.AidaPlugin;

public class AidaPackage {
	private String name;
	
	public AidaPackage(String name) {
		this.name = name;
	}

	protected void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
