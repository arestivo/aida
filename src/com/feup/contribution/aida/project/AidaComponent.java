package com.feup.contribution.aida.project;

import java.util.LinkedList;
import java.util.Stack;

import com.feup.contribution.aida.AidaPlugin;

public class AidaComponent {
	private LinkedList<AidaPackage> components = new LinkedList<AidaPackage>();
	private LinkedList<AidaComponent> dependants = new LinkedList<AidaComponent>();
	private int indegree = 0;
	
	public LinkedList<AidaPackage> getPackages() {
		return components;
	}
	
	public static LinkedList<AidaComponent> getOrderedComponents(LinkedList<AidaPackage> packages) {
		LinkedList<AidaComponent> components = getComponents(packages);
		LinkedList<AidaComponent> orderedComponents = new LinkedList<AidaComponent>();
		
		Stack<AidaComponent> s = new Stack<AidaComponent>();
		
		for (AidaComponent component : components) 
			for (AidaPackage unit : component.getPackages())
				for (AidaPackage dependent : unit.getReferencedPackages())
					for (AidaComponent dComponent : components)
						if (!component.equals(dComponent) && dComponent.contains(dependent)) dComponent.addDependency(component);

		for (AidaComponent component : components) 
			for (AidaComponent dComponent : component.getDependents())
				dComponent.increaseIndegree();

		for (AidaComponent component : components) 
			if (component.getIndegree() == 0)
				s.add(component);
	
		while (!s.isEmpty()) {
			AidaComponent c = s.pop();
			orderedComponents.add(c);
			for (AidaComponent d : c.getDependents())
				if (d.decreaseIndegree() == 0 && !orderedComponents.contains(d)) s.add(d);
		}
		
		return orderedComponents;
	}
	
	private int decreaseIndegree() {
		return --indegree;
	}

	private int getIndegree() {
		return indegree;
	}

	private void increaseIndegree() {
		indegree++;
	}

	private LinkedList<AidaComponent> getDependents() {
		return dependants;
	}

	private void addDependency(AidaComponent component) {
		dependants.add(component);
	}

	private boolean contains(AidaPackage unit) {
		return components.contains(unit);
	}

	public static LinkedList<AidaComponent> getComponents(LinkedList<AidaPackage> packages) {
		int index[], lowlink[];
		int i = 0;
		index = new int[packages.size()];
		lowlink = new int[packages.size()];
		boolean instack[];
		instack = new boolean[packages.size()];
		for (int v = 0; v < packages.size(); v++) {
			index[v] = -1; instack[v] = false;
		}
		LinkedList<AidaComponent> components = new LinkedList<AidaComponent>();
		Stack<AidaPackage> s = new Stack<AidaPackage>();
		for (int v = 0; v < packages.size(); v++) {
			if (index[v]==-1) i = dfs(packages, v, i, index, lowlink, s, instack, components);
		}	
		
		return components;
	}
	
	private static int dfs(LinkedList<AidaPackage> units, int v, int i, int[] index, int[] lowlink, Stack<AidaPackage> s, boolean[] instack, LinkedList<AidaComponent> components) {
		index[v] = i;
		lowlink[v] = i++;
		s.push(units.get(v)); instack[v] = true;
		LinkedList<AidaPackage> depends = units.get(v).getReferencedByPackages();
		for (int e = 0; e < depends.size(); e++){
			int w = units.indexOf(depends.get(e));
			if (w == -1) continue;
			if (index[w]==-1) {
				dfs(units, w, i, index, lowlink, s, instack, components);
				lowlink[v] = Math.min(lowlink[v], lowlink[w]);
			} else if (instack[w]) lowlink[v] = Math.min(lowlink[v], index[w]);
		}
		if (lowlink[v] == index[v]) {
			AidaComponent newComponent = new AidaComponent();
			while (!s.empty()){
				AidaPackage wUnit = s.pop(); instack[units.indexOf(wUnit)] = false;
				newComponent.addPackage(wUnit);
				if (wUnit==units.get(v)) break;
			}
			components.add(newComponent);
		}
		return i;
	}

	private void addPackage(AidaPackage unit) {
		components.add(unit);
	}
	
	@Override
	public String toString() {
		String s = "";
		for (AidaPackage unit : components) {
			if (s.equals("")) s = unit.getName();
			else s += ", " + unit.getName();
		}
		return s;
	}

	public int getNumberTests() {
		int total = 0;
		for (AidaPackage apackage : components) {
			total += apackage.getTests().size();
		}
		return total;
	}

}
