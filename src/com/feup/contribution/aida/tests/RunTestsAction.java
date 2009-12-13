package com.feup.contribution.aida.tests;

import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class RunTestsAction implements IObjectActionDelegate {

	private ISelection selection;
	
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	@Override
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
					runTests(project);
				}
			}
		}
	}

	private void runTests(IProject project) {
		MessageDialog.openInformation(new Shell(), "Aida", "Not implemented yet");
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

}