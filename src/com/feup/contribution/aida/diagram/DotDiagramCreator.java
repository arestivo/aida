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

			bw.write("  node [ shape = \"component\", color = \"blue\"]\n");

			for (AidaPackage apackage : project.getPackages()) {
				bw.write("    \"" + apackage.getName() + "\" [label=\""+apackage.getName()+"\"]\n");
			}


			bw.write("  edge [ color = \"black\", arrowhead=\"vee\" ]\n");

			for (AidaPackage apackage : project.getPackages()) {
				for (AidaPackage dpackage : apackage.getReferencedPackages()) {
					if (!apackage.getMandatoryPackages().contains(dpackage)) bw.write("  edge [ color = \"green\", arrowhead=\"vee\" ]\n");
					bw.write("    \"" + apackage.getName() + "\" -- \"" + dpackage.getName() + "\"\n");
					if (!apackage.getMandatoryPackages().contains(dpackage)) bw.write("  edge [ color = \"black\", arrowhead=\"vee\" ]\n");
				}
			}			
			
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
