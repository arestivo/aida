package com.feup.contribution.aida;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class AidaPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.feup.contribution.aida";

	// The shared instance
	private static AidaPlugin plugin;

	private MessageConsoleStream log;
	
	/**
	 * The constructor
	 */
	public AidaPlugin() {
		MessageConsole aidaConsole = new MessageConsole("Aida Log", null);
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { aidaConsole });
		log = aidaConsole.newMessageStream();
	}

	public void log(String message) {
		log.println(message);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static AidaPlugin getDefault() {
		return plugin;
	}
	

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public void logException(Exception e) {
		log(e.toString());
		StackTraceElement[] st = e.getStackTrace();
		for (int i = 0; i < st.length; i++) {
			log(st[i].toString());
		}
	}
}
