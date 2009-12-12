package com.feup.contribution.aida.container;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class AidaClasspathContainerInitializer extends ClasspathContainerInitializer{

	@Override
	public void initialize(IPath containerPath, IJavaProject project)
			throws CoreException {
        IClasspathContainer container;
		try {
			container = new AidaClasspathContainer();
	        JavaCore.setClasspathContainer(containerPath, new IJavaProject[] {project}, new IClasspathContainer[] {container}, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}