package org.eclipse.update.tests.parser;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.update.core.*;
import org.eclipse.update.core.ISite;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.internal.core.Writer;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestSiteGeneration extends UpdateManagerTestCase {
	
	private static ISite TEMP_SITE;
	public static final String TEMP_NAME = "update_tmp/";
	/**
	 * Constructor for Test1
	 */
	public TestSiteGeneration(String arg0) {
		super(arg0);
	}
	
	
	public void testGenerate() throws Exception {
		// get a Site.xml
		// generate in another place
		// create a site on it
		// check with the first one
		
		// get site.xml
		ISite remoteSite = SiteManager.getSite(SOURCE_FILE_SITE);
		
		// generate
		ISite tempSite = getTempSite();
		new File(tempSite.getURL().getFile()).mkdirs();
		File file = new File(tempSite.getURL().getFile()+"site.xml");
		PrintWriter fileWriter = new PrintWriter(new FileOutputStream(file));
		Writer writer = new Writer();
		writer.writeSite((IWritable)remoteSite,fileWriter); 
		fileWriter.close();
		
		//get the local Site again
		URL newURL =tempSite.getURL();
		ISite compareSite = SiteManager.getSite(newURL);
		
		// compare
		String remoteURLAsString = UpdateManagerUtils.getURLAsString(remoteSite.getURL(),remoteSite.getInfoURL());
		String compareURLAsString = UpdateManagerUtils.getURLAsString(compareSite.getURL(),compareSite.getInfoURL());
		assertEquals(remoteURLAsString, compareURLAsString);

		remoteURLAsString = UpdateManagerUtils.getURLAsString(remoteSite.getURL(),remoteSite.getFeatureReferences()[0].getURL());
		compareURLAsString = UpdateManagerUtils.getURLAsString(compareSite.getURL(),compareSite.getFeatureReferences()[0].getURL());
		assertEquals(remoteURLAsString,compareURLAsString);
		
		assertEquals(remoteSite.getCategories()[0].getLabel(),compareSite.getCategories()[0].getLabel());
		
		// cleanup
		UpdateManagerUtils.removeFromFileSystem(file);	
	
	}
	
	
		/**
	 * return the local site where the feature will be temporary transfered
	 */
	public static ISite getTempSite() throws CoreException {
		if (TEMP_SITE == null) {
			String tempDir = System.getProperty("java.io.tmpdir");
			if (!tempDir.endsWith(File.separator))
				tempDir += File.separator;
			String fileAsURL = (tempDir+TEMP_NAME).replace(File.separatorChar,'/');
				File file = new File(fileAsURL);
				TEMP_SITE = createSite(file);
		}
		return TEMP_SITE;
	}
	
	/**
	 * Creates a new site on the file system
	 * This is the only Site we can create.
	 * 
	 * @param siteLocation
	 * @throws CoreException
	 */
	 private static ISite createSite(File siteLocation) throws CoreException {
		Site site = null;
		if (siteLocation != null) {
			try {
				siteLocation.mkdirs();
				URL siteURL = siteLocation.toURL();
				site = (Site) InternalSiteManager.getSite(siteURL, true);
				// FIXME, when creating a site, should we manage site.xml ?
				//site.save();
			} catch (MalformedURLException e) {
				throw newCoreException("Cannot create a URL from:" + siteLocation.getAbsolutePath(), e);
			}
		}
		return site;
	}	
	
	/**
	 * returns a Core Exception
	 */
	private static CoreException newCoreException(String s, Throwable e) throws CoreException {
		return new CoreException(new Status(IStatus.ERROR, "org.eclipse.update.core", 0, s, e));
	}	
	}

