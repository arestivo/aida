package com.feup.contribution.aida.project;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.core.resources.IResource;

public class AidaPackage {
	public enum State {COMPILED, FAILED, PASSED, CONFLICTS};

	private String name;
	
	private HashMap<String, AidaUnit> units = new HashMap<String, AidaUnit>();
	private HashSet<AidaPackage> referencedPackages = new HashSet<AidaPackage>();
	private HashSet<AidaPackage> mandatoryPackages = new HashSet<AidaPackage>();
	private LinkedList<AidaPackage> referencedByPackages = new LinkedList<AidaPackage>();
	private LinkedList<AidaTest> tests = new LinkedList<AidaTest>();
	
	private State state = State.COMPILED;
	
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
			HashSet<String> referencedUnits = unit.getReferencedUnits();
			for (String rUnit : referencedUnits) {
				Collection<AidaPackage> packages = project.getPackagesForUnit(rUnit);
				referencedPackages.addAll(packages);
				for (AidaPackage aidaPackage : packages) {
					aidaPackage.addReferencedBy(this);
				}
			}
			HashSet<String> mandatoryUnits = unit.getMandatoryUnits();
			for (String mUnit : mandatoryUnits)
				mandatoryPackages.addAll(project.getPackagesForUnit(mUnit));
		}
		referencedPackages.remove(this);
		mandatoryPackages.remove(this);
	}

	private void addReferencedBy(AidaPackage aidaPackage) {
		if (referencedByPackages.contains(aidaPackage)) return;
		if (this != aidaPackage) referencedByPackages.add(aidaPackage);
	}

	public HashSet<AidaPackage> getReferencedPackages() {
		return referencedPackages;
	}

	public HashSet<AidaPackage> getMandatoryPackages() {
		return mandatoryPackages;
	}

	public LinkedList<AidaPackage> getReferencedByPackages() {
		return referencedByPackages;
	}
	
	public LinkedList<AidaUnit> getUnits() {
		return new LinkedList<AidaUnit>(units.values());
	}

	public void addTest(AidaTest aidaTest) {
		tests.add(aidaTest);
	}

	public LinkedList<AidaTest> getTests() {
		return tests;
	}

	public AidaTest getTests(String fullName) {
		for (AidaTest test : tests) {
			if (test.getFullName().equals(fullName)) return test;
		}
		return null;
	}

	public void setState(State state) {
		this.state = state;
	}

	public State getState() {
		return state;
	}

	public Collection<String> getReplaces() {
		HashSet<String> replaces = new HashSet<String>();
		for (AidaTest test : getTests())
			replaces.addAll(test.getReplaces());
		return replaces;
	}
}