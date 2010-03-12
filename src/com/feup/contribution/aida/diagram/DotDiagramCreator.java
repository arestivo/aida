package com.feup.contribution.aida.diagram;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.eclipse.core.runtime.Platform;

import com.feup.contribution.aida.AidaPlugin;
import com.feup.contribution.aida.project.AidaPackage;
import com.feup.contribution.aida.project.AidaProject;
import com.feup.contribution.aida.project.AidaTest;

public class DotDiagramCreator {
	AidaProject project;
	
	public DotDiagramCreator(AidaProject project) {
		this.project = project;
	}
	
	public void drawDiagram() {
		String workspacepath = Platform.getLocation().toOSString();
		String unitpath = project.getIProject().getPath().toOSString();
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(workspacepath + unitpath + "/aida.dot")));
			bw.write("graph \"druid\" {\n");
			bw.write("  node [ fontname = \"Trebuchet\", label = \"\\N\"]\n");

			bw.write("  node [ shape = \"tab\", color = \"blue\"]\n");

			for (AidaPackage apackage : project.getPackages()) {
				if (apackage.getState() == AidaPackage.State.FAILED) bw.write("  node [ color = \"red\"]\n");
				if (apackage.getState() == AidaPackage.State.PASSED) bw.write("  node [ color = \"green\"]\n");
				bw.write("    \"" + apackage.getName() + "\" [label=\""+apackage.getName()+"\"]\n");
				bw.write("  node [ color = \"blue\"]\n");
			}


			bw.write("  edge [ color = \"black\", arrowhead=\"empty\", style=\"dashed\" ]\n");

			for (AidaPackage apackage : project.getPackages()) {
				for (AidaPackage dpackage : apackage.getReferencedPackages()) {
					if (!apackage.getMandatoryPackages().contains(dpackage)) bw.write("  edge [ color = \"green\"]\n");
					bw.write("    \"" + apackage.getName() + "\" -- \"" + dpackage.getName() + "\"\n");
					if (!apackage.getMandatoryPackages().contains(dpackage)) bw.write("  edge [ color = \"black\"]\n");
				}
			}			

			bw.write("  edge [ color = \"blue\", arrowhead=\"dot\", style=\"solid\" ]\n");
			for (AidaPackage apackage : project.getPackages())
				for (AidaTest test : apackage.getTests())
					for (String replaces : test.getReplaces())
						for (AidaPackage rpackage : project.getPackages())
							for (AidaTest rtest : rpackage.getTests())
								if (replaces.equals(rtest.getFullName()))
									bw.write("    \"" + apackage.getName() + "\" -- \"" + rpackage.getName() + "\"\n");
			
			bw.write("}\n");
			bw.close();
			createPngFile();
		} catch (FileNotFoundException e) {
			AidaPlugin.getDefault().logException(e);
		} catch (IOException e) {
			AidaPlugin.getDefault().logException(e);
		}
	}

	private void createPngFile() throws IOException{
		String workspacepath = Platform.getLocation().toOSString();
		String unitpath = project.getIProject().getPath().toOSString();
		
		Process p = Runtime.getRuntime().exec(new String[]{"dot", workspacepath+unitpath+"/aida.dot", "-Tpng", "-o"+workspacepath+unitpath+"/aida.png"});
		try {
			p.waitFor();
		} catch (InterruptedException e) {}
	}
	
}
