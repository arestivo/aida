package com.feup.contribution.aida.ui;

import java.util.LinkedList;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.feup.contribution.aida.AidaPlugin;
import com.feup.contribution.aida.project.AidaPackage;
import com.feup.contribution.aida.project.AidaProject;

public class AidaRunTestDialog extends TitleAreaDialog{

	private Table packagesTable;
	private Table testsTable;
	private AidaProject project;
	
	public static final int CLOSE = 9999;
	public static final int RUN = 9998;
	
	private LinkedList<AidaPackage> selected = new LinkedList<AidaPackage>();
	
	public AidaRunTestDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	public void create() {
		super.create();
		setTitle("Run Aida Tests");
		setMessage("Select the packages you want to test and click \"Run Tests\". Packages required for a selected package will be automatically selected.");
	}
	
	protected Control createDialogArea(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		parent.setLayout(layout);

	    GridData gd = new GridData(GridData.FILL_BOTH);
		packagesTable = new Table(parent, SWT.CHECK);
		packagesTable.setLayoutData(gd);
	    
		TableColumn packageColumn = new TableColumn(packagesTable, SWT.LEFT | SWT.BORDER | SWT.H_SCROLL);
		packageColumn.setWidth(300);
		packageColumn.setText("Packages");

		LinkedList<AidaPackage> packages = project.getPackages();
		for (AidaPackage aidaPackage : packages) {
			TableItem item = new TableItem(packagesTable, SWT.NONE);
			item.setText(aidaPackage.getName());			
		}
		
 		packagesTable.setHeaderVisible(true);
		packagesTable.setLinesVisible(true);

		setErrorMessage("At least one package must be selected");
		
	    packagesTable.addListener(SWT.Selection, new Listener() {
	        public void handleEvent(Event event) {
	          if (event.item instanceof TableItem) {
	        	  TableItem item = (TableItem) event.item;

	        	  setErrorMessage("At least one package must be selected");
				  getButton(RUN).setEnabled(false);
	        	  
	        	  TableItem[] items = packagesTable.getItems();
        		  for (int i = 0; i < items.length; i++)
					if (items[i].getChecked()) {
						setErrorMessage(null);
						getButton(RUN).setEnabled(true);
					}
	        	  
		          if (event.detail == SWT.CHECK) {
		        	  AidaPackage p = project.getPackage(item.getText());
		        	  LinkedList<AidaPackage> referenced = p.getReferencedPackages();
		        	  for (AidaPackage aidaPackage : referenced) {
		        		  for (int i = 0; i < items.length; i++) {
							if (items[i].getText().equals(aidaPackage.getName()))
									items[i].setChecked(true);
						}
		        	  }
		          } else {
		        	  AidaPackage p = project.getPackage(item.getText());
		        	  LinkedList<AidaPackage> referencedBy = p.getReferencedByPackages();
		        	  for (AidaPackage aidaPackage : referencedBy) {
		        		  for (int i = 0; i < items.length; i++) {
							if (items[i].getText().equals(aidaPackage.getName()))
									items[i].setChecked(false);
						}
		        	  }		        	  
		          }
		      }
	        }
	    });
		
	    testsTable = new Table(parent, SWT.LEFT | SWT.BORDER | SWT.H_SCROLL);

	    TableColumn componentColumn = new TableColumn(packagesTable, SWT.LEFT | SWT.BORDER | SWT.H_SCROLL);
	    componentColumn.setWidth(150);
	    componentColumn.setText("Packages");

		TableColumn testColumn = new TableColumn(packagesTable, SWT.LEFT | SWT.BORDER | SWT.H_SCROLL);
	    testColumn.setWidth(50);
		testColumn.setText("Tests");

	    TableColumn resultColumn = new TableColumn(packagesTable, SWT.LEFT | SWT.BORDER | SWT.H_SCROLL);
		resultColumn.setWidth(100);
		resultColumn.setText("Result");

		return parent;
	}

	protected void createButtonsForButtonBar(Composite parent) {
	    Button runButton = createButton(parent, RUN, "Run Tests", true);
	    runButton.setEnabled(false);

	    runButton.addSelectionListener(new SelectionAdapter(){
	      public void widgetSelected(SelectionEvent e) {
	        setReturnCode(RUN);
	        saveSelectedPackages();
	        close();
	      }
	    });

	    Button closeButton = createButton(parent, CLOSE, "Close", false);

	    closeButton.addSelectionListener(new SelectionAdapter(){
	      public void widgetSelected(SelectionEvent e) {
	        setReturnCode(CLOSE);
	        close();
	      }
	    });

	  }
	
	public void setProject(AidaProject project) {
		this.project = project;		
	}

	private void saveSelectedPackages() {
		TableItem[] items = packagesTable.getItems();
		for (int i = 0; i < items.length; i++)
			if (items[i].getChecked())
				selected.add(project.getPackage(items[i].getText()));
	}

	public LinkedList<AidaPackage> getSelectedPackages() {
		return selected;
	}
}
