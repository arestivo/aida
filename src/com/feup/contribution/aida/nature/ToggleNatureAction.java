package com.feup.contribution.aida.nature;

import java.util.ArrayList; 
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.feup.contribution.aida.container.AidaClasspathContainer;

public class ToggleNatureAction implements IObjectActionDelegate {

	private ISelection selection;

	public void run(IAction action) {
		if (selection instanceof IStructuredSelection) {
			for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it
					.hasNext();) {
				Object element = it.next();
				IProject project = null;
				if (element instanceof IProject) {
					project = (IProject) element;
				} else if (element instanceof IAdaptable) {
					project = (IProject) ((IAdaptable) element)
							.getAdapter(IProject.class);
				}
				if (project != null) {
					toggleNature(project);
				}
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	private void toggleNature(IProject project) {
		try {
			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();

			for (int i = 0; i < natures.length; ++i) {
				if (AidaNature.NATURE_ID.equals(natures[i])) {

					MessageDialog.openInformation(new Shell(), "Aida", "Aida Support Removed");
					
					// Remove the nature
					String[] newNatures = new String[natures.length - 1];
					System.arraycopy(natures, 0, newNatures, 0, i);
					System.arraycopy(natures, i + 1, newNatures, i,
							natures.length - i - 1);
					description.setNatureIds(newNatures);
					project.setDescription(description, null);
					return;
				}
			}

			MessageDialog.openInformation(new Shell(), "Aida", "Aida Support Activated");

			// Add the nature
			String[] newNatures = new String[natures.length + 1];
			System.arraycopy(natures, 0, newNatures, 0, natures.length);
			newNatures[natures.length] = AidaNature.NATURE_ID;
			description.setNatureIds(newNatures);
			project.setDescription(description, null);
			
			// Add Classpath
			
   		    IJavaProject javaProject = (IJavaProject) JavaCore.create((IProject) project);
			  
		    IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();

		    List<IClasspathEntry> newEntries = new ArrayList<IClasspathEntry>(rawClasspath.length+1);
		    for(IClasspathEntry e: rawClasspath) newEntries.add(e);

		    newEntries.add(JavaCore.newContainerEntry(AidaClasspathContainer.CONTAINER_ID));

		    IClasspathEntry[] newEntriesArray = new IClasspathEntry[newEntries.size()];
		    newEntriesArray = (IClasspathEntry[])newEntries.toArray(newEntriesArray);
		    javaProject.setRawClasspath(newEntriesArray, null);
		      
		    javaProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
		}
	}

}
