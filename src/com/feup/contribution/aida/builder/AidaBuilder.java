package com.feup.contribution.aida.builder;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.internal.compiler.ast.ForeachStatement;

import com.feup.contribution.aida.AidaPlugin;
import com.feup.contribution.aida.project.AidaPackage;
import com.feup.contribution.aida.project.AidaProject;

public class AidaBuilder extends IncrementalProjectBuilder {

	class ResourceVisitor implements IResourceVisitor {
		public boolean visit(IResource resource) {
			checkJava(resource);
			return true;
		}
	}

	public static final String BUILDER_ID = "com.feup.contribution.aida.aidaBuilder";

	private static final String MARKER_TYPE = "com.feup.contribution.aida.aidaProblem";
	
	private void addMarker(IFile file, String message, int lineNumber, int severity) {
		try {
			IMarker marker = file.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		} catch (CoreException e) {
		}
	}
	@SuppressWarnings("unchecked")
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		fullBuild(monitor);
		return null;
	}

	protected String getPackageName(ICompilationUnit cu) {
		try {
			for (IAnnotation annotation : cu.getTypes()[0].getAnnotations()) {
				if (annotation.getElementName().equals("PackageName")) {
						for (IMemberValuePair pair : annotation.getMemberValuePairs()) {
							if (pair.getMemberName().equals("value")) return (String) pair.getValue();
						}
				}
			} 
			if (cu.getPackageDeclarations().length==0) return "default";
			return cu.getPackageDeclarations()[0].getElementName();
		} catch (Exception e) {}
		return "unknown";
	}
	
	void checkJava(IResource resource) {
		if (resource instanceof IFile && resource.getName().endsWith(".java")) {
			ICompilationUnit cu = (ICompilationUnit) JavaCore.create(resource);
			AidaProject project = AidaProject.getProject(getProject().getName());
			AidaPackage apackage = project.getPackage(getPackageName(cu));

			apackage.addUnit(cu.findPrimaryType().getElementName(), resource);
		}
	}

	private void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
			
		}
	}

	protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
		try {
			AidaProject project = AidaProject.getProject(getProject().getName());
			project.reset();
			getProject().accept(new ResourceVisitor());
			project.logStructure();
		} catch (CoreException e) {

		}
	}
}
