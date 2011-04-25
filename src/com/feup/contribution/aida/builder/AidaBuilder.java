package com.feup.contribution.aida.builder;

import java.util.HashMap; 
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	@SuppressWarnings("unchecked")
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		fullBuild(monitor);
		return null;
	}

	private String getPackageName(ICompilationUnit cu) {
		try {
			if (cu.getPackageDeclarations().length==0) {
				return "default";
			}
			return cu.getPackageDeclarations()[0].getElementName();
		} catch (JavaModelException e) {}
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
		if (cu.getResource().getFileExtension().equals("aj")) return getAspectPackageLabel(cu);
		if (cu.getResource().getFileExtension().equals("java")) return getClassPackageLabel(cu);
		return "unknown type";
	}
	
	private String getClassPackageLabel(ICompilationUnit cu) {
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

	private String getAspectPackageLabel(ICompilationUnit cu) {
		try {
			if (cu.getTypes().length > 0){
				String source = cu.getTypes()[0].getSource();
				Pattern pattern = Pattern.compile("@PackageName\\(\"(.+?)\"\\)");
				Matcher matcher = pattern.matcher(source);
				if (matcher.find()) return matcher.group(1);
			}
		} catch (JavaModelException e) {} 
		return getPackageName(cu);
	}

	void checkJava(IResource resource) {
		ICompilationUnit cu = (ICompilationUnit) JavaCore.create(resource);
		if (cu.findPrimaryType() == null) return;
		
		if (isTestUnit(cu)) checkTest(resource);
		
		AidaProject project = AidaProject.getProject(getProject().getName());
		AidaPackage apackage = project.getPackage(getPackageLabel(cu));
		
		apackage.setState(AidaPackage.State.COMPILED);
		
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
	
	private void checkTest(IResource resource) {
		ICompilationUnit cu = (ICompilationUnit) JavaCore.create(resource);
		
		AidaProject project = AidaProject.getProject(getProject().getName());

		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(cu);
		parser.setResolveBindings(true);
		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
		
		AidaASTTestVisitor testVisitor = new AidaASTTestVisitor();
		astRoot.accept(testVisitor);
		
		HashMap<String, HashSet<String>> replaces = testVisitor.getReplaces();
		for (String test : testVisitor.getTestNames()) {
			AidaPackage apackage = project.getPackage(getTestFor(cu));
			AidaTest aTest = new AidaTest(test, cu.findPrimaryType().getElementName(), getPackageName(cu), resource);
			apackage.addTest(aTest);
			aTest.addReplaces(replaces.get(test));
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
	        
	        DotDiagramCreator diagramCreator = new DotDiagramCreator(project);
	        diagramCreator.drawDiagram();

	        if (project.isFirstCompilation()){
	        	project.setFirstCompilation(false);
	        	needRebuild();
	        }
	        
		} catch (Exception e) {
			AidaPlugin.getDefault().logException(e);
		}
	}

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
