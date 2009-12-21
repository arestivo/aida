package com.feup.contribution.aida.ui;

import java.util.HashSet;
import java.util.LinkedList;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.feup.contribution.aida.AidaPlugin;
import com.feup.contribution.aida.project.AidaComponent;
import com.feup.contribution.aida.project.AidaPackage;
import com.feup.contribution.aida.project.AidaProject;
import com.feup.contribution.aida.project.AidaTest;
import com.feup.contribution.aida.tester.AidaTester;

public class AidaRunTestDialog extends TitleAreaDialog{

	private Table packagesTable;
	private Table testsTable;
	private AidaProject aidaProject;
	private IJavaProject project;

	public static final int CLOSE = 9999;
	public static final int RUN = 9998;

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

		GridData gdp = new GridData(GridData.FILL_BOTH);
		packagesTable = new Table(parent, SWT.CHECK | SWT.BORDER | SWT.H_SCROLL);
		packagesTable.setLayoutData(gdp);

		int itemHeightP = packagesTable.getItemHeight();
		gdp.heightHint = itemHeightP * 5;

		TableColumn packageColumn = new TableColumn(packagesTable, SWT.LEFT);
		packageColumn.setWidth(300);
		packageColumn.setText("Packages");

		LinkedList<AidaPackage> packages = aidaProject.getPackages();
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

					if (event.detail == SWT.CHECK && item.getChecked()) {
						AidaPackage p = aidaProject.getPackage(item.getText());
						HashSet<AidaPackage> referenced = p.getMandatoryPackages();
						for (AidaPackage aidaPackage : referenced) {
							for (int i = 0; i < items.length; i++) {
								if (items[i].getText().equals(aidaPackage.getName()))
									items[i].setChecked(true);
							}
						}
					}
					if (event.detail == SWT.CHECK && !item.getChecked()) {
						AidaPackage p = aidaProject.getPackage(item.getText());
						LinkedList<AidaPackage> referencedBy = p.getReferencedByPackages();
						for (AidaPackage aidaPackage : referencedBy) {
							for (int i = 0; i < items.length; i++) {
								if (items[i].getText().equals(aidaPackage.getName()) && aidaPackage.getMandatoryPackages().contains(p))
									items[i].setChecked(false);
							}
						}		        	  
					}
				}
			}
		});

		new Label(parent, SWT.NONE).setText("Test Results");		

		GridData gdt = new GridData(GridData.FILL_BOTH);
		testsTable = new Table(parent, SWT.LEFT | SWT.BORDER | SWT.H_SCROLL);
		testsTable.setLayoutData(gdt);

		int itemHeightT = testsTable.getItemHeight();
		gdt.heightHint = itemHeightT * 8;


		testsTable.setLinesVisible(true);
		testsTable.setHeaderVisible(true);

		TableColumn componentColumn = new TableColumn(testsTable, SWT.LEFT | SWT.BORDER | SWT.H_SCROLL);
		componentColumn.setWidth(150);
		componentColumn.setText("Packages");

		TableColumn testColumn = new TableColumn(testsTable, SWT.LEFT | SWT.BORDER | SWT.H_SCROLL);
		testColumn.setWidth(150);
		testColumn.setText("Tests");

		TableColumn resultColumn = new TableColumn(testsTable, SWT.LEFT | SWT.BORDER | SWT.H_SCROLL);
		resultColumn.setWidth(50);
		resultColumn.setText("Result");		
		
		return parent;
	}

	protected void createButtonsForButtonBar(final Composite parent) {
		final Button runButton = createButton(parent, RUN, "Run Tests", true);
		runButton.setEnabled(false);

		runButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				runTests(runButton);
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

	private void runTests(final Button runButton) {
		setReturnCode(RUN);
		final LinkedList<AidaPackage> selectedPackages = getSelectedPackages();

		final TestDialogUpdater updater = new TestDialogUpdater(testsTable, this);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try{
				updater.cleanTable();
				updater.setMessage(null);
						
				updater.disableButton(runButton);
		
				int numberTests = 0;
				LinkedList<AidaComponent> components = AidaComponent.getOrderedComponents(selectedPackages);
				for (AidaComponent aidaComponent : components) {
					numberTests += aidaComponent.getNumberTests();
					updater.addLine(aidaComponent.toString(), numberTests);
				}

				int index = 0;
				LinkedList<AidaComponent> currentComponents = new LinkedList<AidaComponent>();
				for (AidaComponent aidaComponent : components) {
					LinkedList<AidaComponent> oldComponents = new LinkedList<AidaComponent>();
					oldComponents.addAll(currentComponents);
					currentComponents.add(aidaComponent);

					AidaTester tester = new AidaTester(aidaProject, project);
					tester.setUpTest(currentComponents);
					updater.update(index, 2, "Compiling");
					tester.compile(aidaProject.getClasspath(project));

					updater.update(index, 2, "Testing");

					HashSet<String> replaces = new HashSet<String>();
					for (AidaComponent currentComponent : currentComponents) {
						LinkedList<AidaPackage> packages = currentComponent.getComponents();
						for (AidaPackage aidaPackage : packages) {
							LinkedList<AidaTest> tests = aidaPackage.getTests();
							for (AidaTest test : tests) {
								replaces.addAll(test.getReplaces());
							}
							aidaPackage.setState(AidaPackage.State.PASSED);
						}
					}
					
					int testNumber = 1;
					for (AidaComponent oldComponent : oldComponents) {
						LinkedList<AidaPackage> packages = oldComponent.getComponents();
						for (AidaPackage aidaPackage : packages) {
							LinkedList<AidaTest> tests = aidaPackage.getTests();
							for (AidaTest test : tests) {
								if (!replaces.contains(test.getFullName())) {
									boolean result = tester.test(test.getPackageName(), test.getClassName(), test.getMethodName(), aidaProject.getClasspath(project));
									if (!result) {
										updater.update(index, 2, "Failed");
										updater.setMessage("Component " + aidaComponent.toString() + " conflicts with " + test.getPackageName() + "." + test.getClassName() + "." + test.getMethodName());
										updater.enableButton(runButton);
										return;
									}
								}
								updater.updateBar(index, testNumber++);
							}
						}
					}

					LinkedList<AidaPackage> packages = aidaComponent.getComponents();
					for (AidaPackage aidaPackage : packages) {
						LinkedList<AidaTest> tests = aidaPackage.getTests();
						for (AidaTest test : tests) {
							boolean result = tester.test(test.getPackageName(), test.getClassName(), test.getMethodName(), aidaProject.getClasspath(project));
							if (!result) {
								updater.update(index, 2, "Failed");
								updater.setMessage("Test failed " + test.getPackageName() + "." + test.getClassName() + "." + test.getMethodName());
								updater.enableButton(runButton);
								aidaPackage.setState(AidaPackage.State.FAILED);
								return;
							}
							updater.updateBar(index, testNumber++);
						}
					}
					
					updater.update(index, 2, "Passed");
					index++;
				}

				updater.enableButton(runButton);
				} catch (Exception e1) {AidaPlugin.getDefault().logException(e1);}
			}
		}).start();
	}

	public void setProject(AidaProject aidaProject, IJavaProject project) {
		this.aidaProject = aidaProject;		
		this.project = project;
	}

	public LinkedList<AidaPackage> getSelectedPackages() {
		LinkedList<AidaPackage> selected = new LinkedList<AidaPackage>();
		TableItem[] items = packagesTable.getItems();
		selected.clear();
		for (int i = 0; i < items.length; i++)
			if (items[i].getChecked())
				selected.add(aidaProject.getPackage(items[i].getText()));
		return selected;
	}

	public Table getTestTable() {
		return testsTable;		
	}
}
