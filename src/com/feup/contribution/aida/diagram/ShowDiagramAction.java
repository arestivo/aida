package com.feup.contribution.aida.diagram;

import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.feup.contribution.aida.project.AidaProject;
import com.feup.contribution.aida.ui.AidaShowDiagramDialog;

public class ShowDiagramAction implements IObjectActionDelegate {

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
					project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
				}
				if (project != null) {
		            Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		            AidaShowDiagramDialog dialog = new AidaShowDiagramDialog(activeShell);
		            dialog.setProject(AidaProject.getProject(project.getName()));
		            dialog.open();
				}
			}
		}
	}
	
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

}