package org.eclipse.update.tests.configurations;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.net.URL;

import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.*;
import org.eclipse.update.core.model.ConfigurationSiteModel;
import org.eclipse.update.core.model.InstallConfigurationModel;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.internal.core.ConfigurationPolicy;
import org.eclipse.update.internal.core.InternalSiteManager;
import org.eclipse.update.internal.core.SiteLocal;
import org.eclipse.update.internal.core.UpdateManagerUtils;
import org.eclipse.update.tests.UpdateManagerTestCase;
import org.eclipse.update.tests.regularInstall.*;

public class TestRevert extends UpdateManagerTestCase {
	
	/**
	 * Test the getFeatures()
	 */
	public TestRevert(String arg0) {
		super(arg0);
	}
	
	public void testSimpleRevertInstall() throws Exception {

		// cleanup
		SiteLocal siteLocal = ((SiteLocal)SiteManager.getLocalSite());
		File localFile = new File(new URL(siteLocal.getLocationURL(),SiteLocal.SITE_LOCAL_FILE).getFile());
		UpdateManagerUtils.removeFromFileSystem(localFile);		
		UpdateManagerUtils.removeFromFileSystem(new File(siteLocal.getCurrentConfiguration().getURL().getFile()));
		InternalSiteManager.localSite=null;		

		ILocalSite site = SiteManager.getLocalSite();
		ISite remoteSite = SiteManager.getSite(SOURCE_HTTP_SITE);
		IFeatureReference featureRef = remoteSite.getFeatureReferences()[0];
		IFeatureReference featureRef2 = remoteSite.getFeatureReferences()[1];
		IFeature feature = featureRef.getFeature();
		
		IInstallConfiguration old = site.getCurrentConfiguration();
		ConfigurationPolicy excludepolicy = new ConfigurationPolicy();
		excludepolicy.setPolicy(IPlatformConfiguration.ISitePolicy.USER_EXCLUDE);
		IConfigurationSite oldConfigSite = old.getConfigurationSites()[0];
		((ConfigurationSiteModel)oldConfigSite).setConfigurationPolicyModel((ConfigurationPolicyModel)excludepolicy);
		
		IInstallConfiguration newConfig = site.cloneCurrentConfiguration(null,"new Label");
		IConfigurationSite configSite = newConfig.getConfigurationSites()[0];
		if (!configSite.getSite().equals(oldConfigSite.getSite())) fail("Config sites are not equals");
		site.addConfiguration(newConfig);		
		IFeatureReference installedFeature = configSite.install(feature,null);
		site.save();

		configSite.unconfigure(installedFeature,null);

		IFeature feature2 = featureRef2.getFeature();
		IInstallConfiguration newConfig2 = site.cloneCurrentConfiguration(null,"new Label2");
		IConfigurationSite anotherConfigSite = newConfig2.getConfigurationSites()[0];
		if (!anotherConfigSite.getSite().equals(oldConfigSite.getSite())) fail("Config sites are not equals");		
		site.addConfiguration(newConfig2);		
		anotherConfigSite.install(feature2,null);
		site.save();

		site.revertTo(old,null,null);
		site.save();
		
		// check
		// there are 4 configuration
		String time = ""+site.getCurrentConfiguration().getCreationDate().getTime();
		File file = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocationURL(),"Config"+time+".xml").getFile());
		assertTrue("new configuration does not exist", file.exists());
		
		
		// teh current one points to a real fature
		// does not throw error.
		IConfigurationSite newConfigSite = null;
		IConfigurationSite[] sites = site.getCurrentConfiguration().getConfigurationSites();
		for (int i = 0; i < sites.length; i++) {
			if (sites[i].getSite().equals(oldConfigSite.getSite())){
				 newConfigSite = sites[i];
				 break;
			}
		}
		if (newConfigSite==null) fail("Cannot find configuration site");

		int oldNumber = oldConfigSite.getConfiguredFeatures().length;
		int newNumber = newConfigSite.getConfiguredFeatures().length;		
		assertTrue("Wrong number of configured features",oldNumber==newNumber);
		
		// test only 2 install config in local site
		assertTrue("wrong number of unconfigured features",newConfigSite.getConfigurationPolicy().getUnconfiguredFeatures().length==2);
		
		// cleanup
		localFile = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocationURL(),SiteLocal.SITE_LOCAL_FILE).getFile());
		UpdateManagerUtils.removeFromFileSystem(localFile);		
		localFile = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocationURL(),SiteLocal.DEFAULT_CONFIG_FILE).getFile());
		UpdateManagerUtils.removeFromFileSystem(localFile);				
		UpdateManagerUtils.removeFromFileSystem(file);	
		time = ""+newConfig.getCreationDate().getTime();
		file = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocationURL(),"DefaultConfig"+time+".xml").getFile());	
		UpdateManagerUtils.removeFromFileSystem(file);	
		time = ""+newConfig2.getCreationDate().getTime();
		file = new File(new URL(((SiteLocal)SiteManager.getLocalSite()).getLocationURL(),"DefaultConfig"+time+".xml").getFile());	
		UpdateManagerUtils.removeFromFileSystem(file);	
	}

}

