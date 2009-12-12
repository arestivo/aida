package com.feup.contribution.aida.container;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Bundle;

public class AidaClasspathContainer implements IClasspathContainer {
    public static final Path CONTAINER_ID = new Path("AIDA_CONTAINER");
    private IClasspathEntry[] _entries;

	@SuppressWarnings("deprecation")
	public AidaClasspathContainer() throws IOException{
        List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
        Bundle bundle = Platform.getBundle("com.feup.contribution.aida");
        URL entry = FileLocator.getBundleFile(bundle).toURL();
        String rootPath = FileLocator.toFileURL(entry).getPath(); 
        IClasspathEntry cpe = JavaCore.newLibraryEntry(new Path(rootPath), null, null);
        entries.add(cpe);
        IClasspathEntry[] cpes = new IClasspathEntry[entries.size()];
        _entries = (IClasspathEntry[])entries.toArray(cpes);
	}
	
	public IClasspathEntry[] getClasspathEntries() {
		return _entries;
	}

	public String getDescription() {
		return "Aida Library";
	}

	public int getKind() {
		return K_APPLICATION;
	}

	public IPath getPath() {
		return CONTAINER_ID;
	}
}