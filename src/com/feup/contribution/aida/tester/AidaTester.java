package com.feup.contribution.aida.tester;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedList;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

import com.feup.contribution.aida.AidaPlugin;
import com.feup.contribution.aida.project.AidaComponent;
import com.feup.contribution.aida.project.AidaPackage;
import com.feup.contribution.aida.project.AidaProject;
import com.feup.contribution.aida.project.AidaUnit;

public class AidaTester {
	private String details;
	private final AidaProject aidaProject;
	private final IJavaProject project;
	
	public AidaTester(AidaProject aidaProject, IJavaProject project) {
		this.project = project;
		this.aidaProject = aidaProject;
	}

	public void setUpTest() {
		try {
			Runtime.getRuntime().exec("rm -Rf /tmp/aida").waitFor();
		} catch (IOException e) {
			AidaPlugin.getDefault().log(e.toString());
		} catch (InterruptedException e) {
			AidaPlugin.getDefault().log(e.toString());
		}
		new File("/tmp/aida/src/").mkdirs();
		LinkedList<AidaComponent> components = AidaComponent.getOrderedComponents(aidaProject.getPackages());
		for (AidaComponent aidaComponent : components) {
			AidaPlugin.getDefault().log("C: " + aidaComponent);
			String unitpath = project.getPath().toOSString();
			for (AidaPackage aidaPackage : aidaComponent.getPackages()) {
				for (AidaUnit unit : aidaPackage.getUnits()) {
					AidaPlugin.getDefault().log(unit.getCompleteName());
					copyPackage(unitpath, unit);					
				}
			}
		}
	}

	private void copyPackage(String unitpath, AidaUnit unit) {
		String path = unit.getResource().getFullPath().toOSString(); 
		
		String newdir = path.substring(path.indexOf('/') + 1);
		newdir = newdir.substring(newdir.indexOf('/'));
		String newfile = newdir.substring(newdir.lastIndexOf('/') + 1);
		newdir = newdir.substring(0, newdir.lastIndexOf('/') + 1);
														
		String workspacepath = Platform.getLocation().toOSString();

		new File("/tmp/aida/" + newdir).mkdirs(); 

		File source = new File(workspacepath + unitpath + newdir , newfile); 
		File dest = new File("/tmp/aida/" + newdir, newfile); 
		try {
			copy(source, dest);
		} catch (IOException e) {
			AidaPlugin.getDefault().log(e.toString());
		}
	}
	
	public void compile(String classpath) {
		try {
			Runtime.getRuntime().exec("rm -Rf /tmp/aida/bin").waitFor();
			
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/tmp/aida/compile.sh")));
			bw.write("ajc -d /tmp/aida/bin -source 1.5  -sourceroots /tmp/druid/src/ -verbose -classpath \"" + classpath + "\"");
			bw.close();
			
			new File("/tmp/aida/compile.sh").setExecutable(true);
					
			Runtime.getRuntime().exec("/tmp/aida/compile.sh").waitFor();
		} catch (IOException e) {
			AidaPlugin.getDefault().log(e.toString());
		} catch (InterruptedException e) {
			AidaPlugin.getDefault().log(e.toString());
		}
	}

	private void copy(File source, File dest) throws IOException {
		AidaPlugin.getDefault().log("COPY " + source.getAbsolutePath() + " to " + dest.getAbsolutePath());
		if (!source.getName().endsWith(".java") && !source.getName().endsWith(".aj")) return;
		
		InputStream in = new FileInputStream(source);
        OutputStream out = new FileOutputStream(dest);
        
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

	public void tearDown() {
	}

	public boolean test(IMethod method, String classpath) {
		details = "";
		try {
			String unitname = method.getCompilationUnit().getPackageDeclarations()[0].getElementName();

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/tmp/druid/bin/test.sh")));
			bw.write("export CLASSPATH=" + classpath + "\ncd /tmp/druid/bin/\njunit -m " + unitname + "." + method.getParent().getElementName() + "." + method.getElementName());
			bw.close();

			new File("/tmp/druid/bin/test.sh").setExecutable(true);
			
			Process p = Runtime.getRuntime().exec("/tmp/druid/bin/test.sh");
			
			String line;
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = br.readLine()) != null){
				details += line + "\n";
				if (line.contains(new String("OK (1 test)"))) return true;
			}
			p.waitFor();
		} catch (JavaModelException e) {
			AidaPlugin.getDefault().log(e.toString());
		} catch (IOException e) {
			AidaPlugin.getDefault().log(e.toString());
		} catch (InterruptedException e) {
			AidaPlugin.getDefault().log(e.toString());
		}
		return false;
	}

	public String getDetails() {
		return details.trim();
	}

}
