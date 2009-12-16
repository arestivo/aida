package com.feup.contribution.aida.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class TestDialogUpdater {
	private Table table;
	
	public TestDialogUpdater(Table table) {
		this.table = table;
	}
	
	public void update(final int index, final int column, final String text) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				table.getItem(index).setText(column, text);
				if (column == 2 && text.equals("Passed")) table.getItem(index).setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
			}
		});
	}

	public void addLine(final String components, final String tests, final String state) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				TableItem item = new TableItem(table, SWT.LEFT);
				String[] texts = {components, tests, state};
				item.setText(texts);
			}
		});
	}

	public void cleanTable() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				table.removeAll();
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
}
