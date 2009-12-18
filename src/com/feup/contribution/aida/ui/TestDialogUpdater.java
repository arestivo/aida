package com.feup.contribution.aida.ui;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class TestDialogUpdater {
	private Table table;
	final ArrayList<ProgressBar> bars = new ArrayList<ProgressBar>();
	private final AidaRunTestDialog aidaRunTestDialog;
	
	public TestDialogUpdater(Table table, AidaRunTestDialog aidaRunTestDialog) {
		this.table = table;
		this.aidaRunTestDialog = aidaRunTestDialog;
	}
	
	public void update(final int index, final int column, final String text) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				table.getItem(index).setText(column, text);
				if (column == 2 && text.equals("Failed")) table.getItem(index).setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
			}
		});
	}

	public void updateBar(final int index, final int value) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				bars.get(index).setSelection(value);
			}
		});
	}
	
	public void addLine(final String components, final int tests) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				TableItem item = new TableItem(table, SWT.LEFT);
				
				TableEditor editor = new TableEditor(table);
				editor.grabHorizontal = true;
				editor.grabVertical = true;
				
				ProgressBar bar = new ProgressBar(table, SWT.NONE);
				bar.setMaximum(tests);
				bar.setSelection(0);
				bars.add(bar);
				
				editor.setEditor(bar, item, 1);
				
				item.setText(0, components);
				item.setText(2, "Waiting");
			}
		});
	}

	public void cleanTable() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				table.removeAll();
				bars.clear();
			}
		});
	}

	public void disableButton(final Button runButton) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				runButton.setEnabled(false);
			}
		});
	}

	public void enableButton(final Button runButton) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				runButton.setEnabled(true);
			}
		});
	}
	
	public void setMessage(final String text) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				aidaRunTestDialog.setErrorMessage(text);
			}
		});
	}
}
