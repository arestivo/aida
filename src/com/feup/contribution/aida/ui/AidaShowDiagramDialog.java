package com.feup.contribution.aida.ui;

import java.util.LinkedList;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.feup.contribution.aida.AidaPlugin;
import com.feup.contribution.aida.diagram.ImageCanvas;
import com.feup.contribution.aida.project.AidaProject;

public class AidaShowDiagramDialog extends TitleAreaDialog{
	private AidaProject project;

	public static int ZOOM_IN = 9999;
	public static int ZOOM_OUT = 9998;
	public static int ZOOM_FIT = 9997;
	public static int CLOSE = 9996;

	private String imagePath;
	
	private ImageCanvas canvas;

	private static LinkedList<AidaShowDiagramDialog> openDialogs = new LinkedList<AidaShowDiagramDialog>();
	
	public AidaShowDiagramDialog(Shell parentShell) {
		super(parentShell);
	}

	public static void refreshOpenDialogs() {
		for (AidaShowDiagramDialog dialog : openDialogs) {
			dialog.refreshImage();
		}
	}
	
	private void refreshImage() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				canvas.setImageData(new ImageData(imagePath));
				canvas.getParent().pack();
			}
		});
	}

	@Override
	public void create() {
		super.create();
		setTitle("Aida Dependency Diagram");
	}

	protected Control createDialogArea(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		parent.setLayout(layout);

		GridData gd = new GridData(1000, 550);
		canvas = new ImageCanvas(parent);
		canvas.setLayoutData(gd);
		canvas.pack();

		String workspacepath = Platform.getLocation().toOSString();
		String unitpath = project.getIProject().getPath().toOSString();

		imagePath = workspacepath+unitpath+"/aida.png";
		refreshImage();

		openDialogs.add(this);
		
		return parent;
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		final Button zoomIn = createButton(parent, ZOOM_IN, "Zoom In", true);
		zoomIn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				canvas.zoomIn();
			}
		});

		final Button zoomOut = createButton(parent, ZOOM_OUT, "Zoom Out", true);
		zoomOut.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				canvas.zoomOut();
			}
		});

		final Button zoomFit = createButton(parent, ZOOM_FIT, "Zoom Fit", true);
		zoomFit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				canvas.fitCanvas();
			}
		});		

		final Button close = createButton(parent, CLOSE, "Close", true);
		close.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openDialogs.remove(this);
				close();
			}
		});		

	}
	
	public void setProject(AidaProject project) {
		this.project = project;
	}
}
