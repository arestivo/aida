package com.feup.contribution.aida.builder;

import java.util.HashSet; 
import java.util.List;
import java.util.Map;

import org.aspectj.asm.IRelationship;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.AJRelationshipType;
import org.eclipse.core.resources.IFile;
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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.feup.contribution.aida.AidaPlugin;
import com.feup.contribution.aida.diagram.DotDiagramCreator;
import com.feup.contribution.aida.project.AidaPackage;
import com.feup.contribution.aida.project.AidaProject;
import com.feup.contribution.aida.project.AidaTest;
import com.feup.contribution.aida.project.AidaUnit;

public class AidaBuilder extends IncrementalProjectBuilder {

	class ResourceVisitor implements IResourceVisitor {
		public boolean visit(IResource resource) {
			if (resource instanceof IFile && resource.getName().endsWith(".java")) checkJava(resource);
			if (resource instanceof IFile && resource.getName().endsWith(".aj")) checkJava(resource);
			return true;
		}
	}

	public static final String BUILDER_ID = "com.feup.contribution.aida.aidaBuilder";

//	private static final String MARKER_TYPE = "com.feup.contribution.aida.aidaProblem";
	
/*	private void addMarker(IFile file, String message, int lineNumber, int severity) {
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
	}*/
	
	@SuppressWarnings("unchecked")
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		fullBuild(monitor);
		return null;
	}

	protected String getPackageName(ICompilationUnit cu) {
		try {
			if (cu.getPackageDeclarations().length==0) {
				AidaPlugin.getDefault().log("DPN: " + cu.getElementName());
				return "default";
			}
			return cu.getPackageDeclarations()[0].getElementName();
		} catch (JavaModelException e) {}
		AidaPlugin.getDefault().log("UPN: " + cu.getElementName());
		return "unknown";
	}

	protected boolean isTestUnit(ICompilationUnit cu) {
		try {
			IType pt = cu.findPrimaryType();
			if (pt == null) return false;
			String parent = pt.getSuperclassName();
			if (parent == null) return false;
			if (parent.equals("TestCase")) return true;
		} catch (JavaModelException e) {}

		return false;
	}

	protected String getTestFor(ICompilationUnit cu) {
		try {
			for (IAnnotation annotation : cu.getTypes()[0].getAnnotations()) {
				if (annotation.getElementName().equals("TestFor")) {
						for (IMemberValuePair pair : annotation.getMemberValuePairs()) {
							if (pair.getMemberName().equals("value")) return (String) pair.getValue();
						}
				}
			}
		} catch (JavaModelException e) {} 
		return getPackageName(cu);
	}
	
	protected String getPackageLabel(ICompilationUnit cu) {
		try {
			if (cu.getTypes().length > 0)
				for (IAnnotation annotation : cu.getTypes()[0].getAnnotations()) {
					if (annotation.getElementName().equals("PackageName")) {
							for (IMemberValuePair pair : annotation.getMemberValuePairs()) {
								if (pair.getMemberName().equals("value")) return (String) pair.getValue();
							}
					}
				}
		} catch (JavaModelException e) {} 
		return getPackageName(cu);
	}
	
	void checkJava(IResource resource) {
		ICompilationUnit cu = (ICompilationUnit) JavaCore.create(resource);
		if (cu.findPrimaryType() == null) return;
		
		if (isTestUnit(cu)) {checkTest(resource); return;}
		//TODO: Do we need to add dependencies from tests?
		
		AidaProject project = AidaProject.getProject(getProject().getName());
		AidaPackage apackage = project.getPackage(getPackageLabel(cu));
		
		if (cu.findPrimaryType() == null) return;
		
		AidaUnit aidaUnit = apackage.addUnit(cu.findPrimaryType().getElementName(), getPackageName(cu)+"."+cu.findPrimaryType().getElementName(), resource);

		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(cu);
		parser.setResolveBindings(true);
		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
	   
		AidaASTVisitor aidaVisitor = new AidaASTVisitor();
		astRoot.accept(aidaVisitor);
		aidaUnit.addReferencedUnits(aidaVisitor.getUnitNames());
		aidaUnit.addMandatoryUnits(aidaVisitor.getUnitNames());
	}

/*	private void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
			
		}
	}*/

	private void checkTest(IResource resource) {
		ICompilationUnit cu = (ICompilationUnit) JavaCore.create(resource);
		
		AidaProject project = AidaProject.getProject(getProject().getName());

		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(cu);
		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
		
		AidaASTTestVisitor testVisitor = new AidaASTTestVisitor();
		astRoot.accept(testVisitor);
		
		for (String test : testVisitor.getTestNames()) {
			AidaPackage apackage = project.getPackage(getTestFor(cu));
			apackage.addTest(new AidaTest(test, cu.findPrimaryType().getElementName(), getPackageName(cu), resource));
		}
	}

	protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
		try {
			final AidaProject project = AidaProject.getProject(getProject().getName());
			if (project.getIProject() == null) project.setIProject(JavaCore.create(getProject()));
 	        monitor.beginTask("Aida Compile", 4);

 	        monitor.subTask("Preparing");
			project.reset();
	        monitor.worked(1);

 	        monitor.subTask("Compiling");
	        getProject().accept(new ResourceVisitor());
	        monitor.worked(1);

 	        monitor.subTask("Checking Advises");
		    checkAdvises(project);
	        monitor.worked(1);
		    
 	        monitor.subTask("Resolving Dependencies");
			project.resolveDependencies();
	        monitor.worked(1);

	        monitor.done();
	        
	        project.logStructure();
	        DotDiagramCreator diagramCreator = new DotDiagramCreator(project);
	        diagramCreator.drawDiagram();

	        //TODO: Redo compilation if first time
		} catch (Exception e) {
			AidaPlugin.getDefault().logException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private void checkAdvises(AidaProject project) throws CoreException {
		AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForProject(getProject());
		if (model.hasModel()) {
			AJRelationshipType[] relTypes = {AJRelationshipManager.ADVISES};
		 	List<IRelationship> rels = (List<IRelationship>) model.getRelationshipsForProject(relTypes);
		 	for (IRelationship rel : rels) {
		 		IJavaElement source = model.programElementToJavaElement(rel.getSourceHandle());
				ICompilationUnit scu = (ICompilationUnit) JavaCore.create(source.getUnderlyingResource());
		 		HashSet<String> unitNames = new HashSet<String>();
		 		for (String targetHandle : (Iterable<String>) rel.getTargets()) {
		 			IJavaElement target = model.programElementToJavaElement(targetHandle);
					ICompilationUnit tcu = (ICompilationUnit) JavaCore.create(target.getUnderlyingResource());
					String unitName = getPackageName(tcu) + "." + tcu.findPrimaryType().getElementName(); 
					unitNames.add(unitName);
		 		}
		 		String unitName = getPackageName(scu)+"."+scu.findPrimaryType().getElementName();
		 		project.getPackage(getPackageLabel(scu)).getUnit(unitName).addReferencedUnits(unitNames);
		 	}
		}
	}
}
