package com.feup.contribution.aida.tester;

import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.feup.contribution.aida.project.AidaProject;
import com.feup.contribution.aida.ui.AidaRunTestDialog;

public class RunTestsAction implements IObjectActionDelegate {

	private ISelection selection;
	
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	@Override
	public void run(IAction action) {
		if (selection instanceof IStructuredSelection) {
			for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it.hasNext();) {
				Object element = it.next();
				IProject project = null;
				if (element instanceof IProject) {
					project = (IProject) element;
				} else if (element instanceof IAdaptable) {
					project = (IProject) ((IAdaptable) element)
							.getAdapter(IProject.class);
				}
				if (project != null) {
					runTests(project);
				}
			}
		}
	}
	
	private void runTests(final IProject project) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
		    public void run() {
		            Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

		            AidaRunTestDialog dialog = new AidaRunTestDialog(activeShell);
		            IJavaProject javaProject = JavaCore.create(project);
		            dialog.setProject(AidaProject.getProject(project.getName()), javaProject);
		            dialog.setBlockOnOpen(true);
		            dialog.open();		            
		    }
		});		
		
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

}