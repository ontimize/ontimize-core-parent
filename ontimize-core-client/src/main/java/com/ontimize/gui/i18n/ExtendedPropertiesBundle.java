package com.ontimize.gui.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.MainApplication;
import com.ontimize.jee.common.gui.i18n.IDatabaseBundleManager;
import com.ontimize.jee.common.locator.ClientReferenceLocator;
import com.ontimize.jee.common.locator.EntityReferenceLocator;
import com.ontimize.jee.common.locator.InitialContext;
import com.ontimize.jee.common.locator.UtilReferenceLocator;
import com.ontimize.jee.common.util.extend.EntitiesPropertiesParser;
import com.ontimize.jee.common.util.extend.OrderProperties;

public class ExtendedPropertiesBundle extends ResourceBundle implements Serializable {

	private static final Logger								logger				= LoggerFactory.getLogger(ExtendedPropertiesBundle.class);

	public static final String								INCLUDE_KEY			= "@include";

	private static Map<CacheKey, ExtendedPropertiesBundle>	cache				= new HashMap<>();

	protected static Map<Object, Object>					databaseCache;

	protected static Map<Object, Object>					extendedBundleCache	= new HashMap<>();

	protected static List<String>							moduleResources		= new ArrayList<>();

	private static class CacheKey implements Serializable {

		protected String	baseName	= null;

		protected Locale	locale		= null;

		public CacheKey(String baseName, Locale l) {
			this.baseName = baseName;
			this.locale = l;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o instanceof CacheKey) {
				if (this.baseName.equals(((CacheKey) o).baseName) && this.locale.equals(((CacheKey) o).locale)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public int hashCode() {
			return this.baseName.hashCode() + this.locale.hashCode();
		}

		@Override
		public String toString() {
			return this.baseName + "_" + this.locale.toString();
		}

		public Locale getLocale() {
			return this.locale;
		}

	}

	private ResourceBundle[]	res			= null;

	private List<Object>		cachedKeys	= null;

	public ResourceBundle[] getBundles() {
		return this.res;
	}

	protected void setBundles(ResourceBundle[] bundles) {
		this.res = bundles;
	}

	protected void addBundle(ResourceBundle bundle) {
		int size = this.res != null ? this.res.length : 0;
		ResourceBundle[] result = new ResourceBundle[size + 1];
		if (this.res != null) {
			System.arraycopy(this.res, 0, result, 0, this.res.length);
		}
		result[size] = bundle;
		this.res = result;
	}

	protected void setBundles(List<Object> bundles) {
		this.res = new ResourceBundle[bundles.size()];
		for (int i = 0; i < bundles.size(); i++) {
			this.res[i] = (ResourceBundle) bundles.get(i);
		}

	}

	private ExtendedPropertiesBundle(List<Object> bundles) {
		this.res = new ResourceBundle[bundles.size()];
		for (int i = 0; i < bundles.size(); i++) {
			this.res[i] = (ResourceBundle) bundles.get(i);
		}
	}

	@Override
	public Locale getLocale() {
		if ((this.res == null) || (this.res.length == 0) || (this.res[0] == null)) {
			return super.getLocale();
		}
		return this.res[0].getLocale();
	}

	@Override
	public Enumeration getKeys() {
		if (this.cachedKeys == null) {
			List<Object> keys = new ArrayList<>();
			for (ResourceBundle r : this.res) {
				if (r == null) {
					continue;
				}
				Enumeration ks = r.getKeys();
				while (ks.hasMoreElements()) {
					Object k = ks.nextElement();
					if (!keys.contains(k)) {
						keys.add(k);
					}
				}
			}
			this.cachedKeys = keys;
		}
		return Collections.enumeration(this.cachedKeys);
	}

	@Override
	public Object handleGetObject(String key) {
		if ((key == null) || "".equals(key)) {
			return "";
		}

		for (ResourceBundle r : this.res) {
			try {
				if (r.containsKey(key)) {
					return r.getObject(key);
				}
			} catch (Exception e) {
				ExtendedPropertiesBundle.logger.trace(null, e);
			}
		}
		// Review comment svn - revision 2430.
		// TODO Review Xestre.
		return key;
		// return null;
	}

	public static ResourceBundle getExtendedBundle(String baseName, Locale l) {

		CacheKey key = new CacheKey(baseName, l);
		if (ExtendedPropertiesBundle.cache.containsKey(key)) {
			ExtendedPropertiesBundle.logger.debug("Bundle {} in cache", baseName);
			ExtendedPropertiesBundle extendedPropertiesBundle = ExtendedPropertiesBundle.cache.get(key);
			return extendedPropertiesBundle;
		}
		List<Object> v = ExtendedPropertiesBundle.getBundles(baseName, l);
		ExtendedPropertiesBundle b = new ExtendedPropertiesBundle(v);
		ExtendedPropertiesBundle.cache.put(key, b);
		return b;
	}

	private static List<Object> getBundles(String baseName, Locale l) {
		List<Object> v = new ArrayList<>();

		if (ExtendedPropertiesBundle.useDatabaseBundle) {
			Object object = ExtendedPropertiesBundle.databaseCache.get(new CacheKey(baseName, l));
			if (object == null) {
				Map<Object, Object> allDatabaseResourceBundles = ExtendedPropertiesBundle.getAllDatabaseResourceBundles(l);
				if (allDatabaseResourceBundles != null) {
					ExtendedPropertiesBundle.databaseCache.putAll(allDatabaseResourceBundles);
				}
			}
		}

		Object extendedBundle = ExtendedPropertiesBundle.extendedBundleCache.get(new CacheKey(baseName, l));
		if (extendedBundle == null) {
			Map<Object, Object> extendedResourceBundle = ExtendedPropertiesBundle.getExtendedResourceBundles(baseName, l);
			if (extendedResourceBundle != null) {
				ExtendedPropertiesBundle.extendedBundleCache.putAll(extendedResourceBundle);
			}
		}

		ExtendedPropertiesBundle.getBundles(baseName, l, v);
		// Include modules bundle...
		for (String moduleBundle : ExtendedPropertiesBundle.moduleResources) {
			ExtendedPropertiesBundle.getBundles(moduleBundle, l, v);
		}
		return v;
	}

	private static void getBundles(String baseName, Locale l, List<Object> dest) {
		ExtendedPropertiesBundle.logger.debug("Loading bundle: {}", baseName);
		// If the bundle is in cache then put it directly
		ResourceBundle bundle = null;
		CacheKey key = new CacheKey(baseName, l);

		if (!ExtendedPropertiesBundle.cache.containsKey(key)) {
			try {
				if ((ExtendedPropertiesBundle.databaseCache != null) && ExtendedPropertiesBundle.databaseCache.containsKey(key)) {
					dest.add(ExtendedPropertiesBundle.databaseCache.get(key));
				}
				if ((ExtendedPropertiesBundle.extendedBundleCache != null) && ExtendedPropertiesBundle.extendedBundleCache.containsKey(key)) {
					dest.add(ExtendedPropertiesBundle.extendedBundleCache.get(key));
					ExtendedPropertiesBundle.checkImports((ResourceBundle) ExtendedPropertiesBundle.extendedBundleCache.get(key), l, dest);
				}
				bundle = ResourceBundle.getBundle(baseName, l);
			} catch (Exception e) {
				ExtendedPropertiesBundle.logger.error("Error loading bundle: {}", e.getMessage(), e);
			}
		} else {
			ExtendedPropertiesBundle.logger.debug("Bundle {} in cache", key);
			bundle = ExtendedPropertiesBundle.cache.get(key);
		}

		dest.add(bundle);
		ExtendedPropertiesBundle.checkImports(bundle, l, dest);

	}

	protected static void checkImports(ResourceBundle bundle, Locale l, List<Object> dest) {
		if ((bundle instanceof PropertyResourceBundle) || (bundle instanceof ExtendedPropertiesResourceBundle)) {
			// Search the key '@include'
			try {
				String sIncludeBundle = bundle.getString(ExtendedPropertiesBundle.INCLUDE_KEY);
				if ((sIncludeBundle != null) && (sIncludeBundle.indexOf(';') > 0)) {
					StringTokenizer st = new StringTokenizer(sIncludeBundle, ";");
					while (st.hasMoreTokens()) {
						String bundleI = st.nextToken();
						ExtendedPropertiesBundle.logger.debug("Including: {}", bundleI);
						ExtendedPropertiesBundle.getBundles(bundleI, l, dest);
						Map<Object, Object> extendedResourcesBundle = ExtendedPropertiesBundle.getExtendedResourceBundles(bundleI, l);
						// ExtendedPropertiesBundle.extendedBundleCache.putAll(extendedResourcesBundle);
						if (extendedResourcesBundle != null) {
							dest.add(extendedResourcesBundle.get(new CacheKey(bundleI, l)));
						}

					}
				} else {
					ExtendedPropertiesBundle.logger.debug("Including: {}", sIncludeBundle);
					ExtendedPropertiesBundle.getBundles(sIncludeBundle, l, dest);
				}
			} catch (Exception e) {
				ExtendedPropertiesBundle.logger.trace("{}", e.getMessage(), e);
			}
		}
	}

	private static Map<Object, Object> getExtendedResourceBundles(String baseName, Locale l) {
		Properties extendedBundleProperties = new Properties();

		StringBuilder builder = new StringBuilder();
		String baseNameWithSlashes = baseName.replace('.', '/');
		builder.append(baseNameWithSlashes);
		builder.append("_extends_");
		builder.append(l);
		builder.append(".properties");
		String _extendsBundle = builder.toString();

		try {
			Enumeration<URL> _extends = EntitiesPropertiesParser.class.getClassLoader().getResources(builder.toString());

			if (_extends.hasMoreElements()) {

				EntitiesPropertiesParser parser = new EntitiesPropertiesParser();
				List<OrderProperties> extendsResourceBundle = new ArrayList<>();

				while (_extends.hasMoreElements()) {
					Properties extendedBundle = new Properties();
					URL urlStream = _extends.nextElement();
					InputStream stream = urlStream.openStream();
					extendedBundle.clear();
					extendedBundle.load(stream);
					int index = parser.parseExtendPropertiesOrder(extendedBundle);
					extendsResourceBundle.add(new OrderProperties(index, extendedBundle));
				}

				Collections.sort(extendsResourceBundle);
				Properties extendedBundle = new Properties();

				for (OrderProperties entry : extendsResourceBundle) {
					extendedBundle = entry.getProperties();
					extendedBundleProperties = parser.parseProperties(extendedBundleProperties, extendedBundle);
					ExtendedPropertiesBundle.logger.debug("Resource bundle \"{}\" with locale \"{}\" extended, Load order -> {}", baseName, l, entry.getIndex());
				}

				Map<Object, Object> toRet = new HashMap<>();
				ExtendedPropertiesResourceBundle extendedProperties = new ExtendedPropertiesResourceBundle(new HashMap<>(extendedBundleProperties), l);
				toRet.put(new CacheKey(baseName, l), extendedProperties);

				return toRet;
			}
		} catch (IOException exc) {
			ExtendedPropertiesBundle.logger.error("Resource bundle \"{}\" with locale \"{}\" can't be extended. ERROR: {}", baseName, l, exc.getMessage(), exc);
		}

		return null;
	}

	public static int getSizeInMemory() {
		List<Object> vCounter = new ArrayList<>();
		Iterator<CacheKey> enumKeys = ExtendedPropertiesBundle.cache.keySet().iterator();
		int iSize = 0;
		while (enumKeys.hasNext()) {
			Object oKey = enumKeys.next();
			ResourceBundle ress = ExtendedPropertiesBundle.cache.get(oKey);
			if (ress instanceof ExtendedPropertiesBundle) {
				for (int j = 0; j < ((ExtendedPropertiesBundle) ress).getBundles().length; j++) {
					ResourceBundle resourceBundle = ((ExtendedPropertiesBundle) ress).getBundles()[j];
					if (resourceBundle == null) {
						continue;
					}
					boolean counted = false;
					for (Object element : vCounter) {
						if (resourceBundle == element) {
							counted = true;
						}
					}
					if (!counted) {
						Enumeration<?> keys = resourceBundle.getKeys();
						while (keys.hasMoreElements()) {
							Object key = keys.nextElement();
							String sValue = resourceBundle.getString(key.toString());
							if (sValue == null) {
								sValue = "";
							}
							iSize = iSize + (key.toString().length() * 2) + (sValue.length() * 2);
						}
						vCounter.add(resourceBundle);
					}
				}
			} else {
				boolean counted = false;
				for (Object element : vCounter) {
					if (ress == element) {
						counted = true;
					}
				}
				if (!counted) {
					Enumeration<?> keys = ress.getKeys();
					while (keys.hasMoreElements()) {
						Object key = keys.nextElement();
						String sValue = ress.getString(key.toString());
						if (sValue == null) {
							sValue = "";
						}
						iSize = iSize + (key.toString().length() * 2) + (sValue.length() * 2);
					}
					vCounter.add(ress);
				}
			}
		}
		return iSize;
	}

	protected static UtilReferenceLocator	locator;

	protected static String					dbBundleManagerName;

	protected static boolean				useDatabaseBundle	= false;

	public static void useDatabaseBundle(UtilReferenceLocator locator, String databaseBundleManagerName) {
		ExtendedPropertiesBundle.locator = locator;
		ExtendedPropertiesBundle.dbBundleManagerName = databaseBundleManagerName;

		ExtendedPropertiesBundle.databaseCache = new HashMap<>();
		ExtendedPropertiesBundle.extendedBundleCache = new HashMap<>();
		ExtendedPropertiesBundle.useDatabaseBundle = true;

		if (ExtendedPropertiesBundle.cache != null) {
			// Include the database bundle if exist in the existing resources
			for (Entry<CacheKey, ExtendedPropertiesBundle> entry : ExtendedPropertiesBundle.cache.entrySet()) {
				CacheKey key = entry.getKey();
				Map<Object, Object> allDatabaseResourceBundles = ExtendedPropertiesBundle.getAllDatabaseResourceBundles(key.locale);

				if (allDatabaseResourceBundles != null) {
					ExtendedPropertiesBundle.databaseCache.putAll(allDatabaseResourceBundles);
				}

				ExtendedPropertiesBundle propBundle = ExtendedPropertiesBundle.cache.remove(key);
				List<Object> resourceBundleList = ExtendedPropertiesBundle.getBundles(key.baseName, key.locale);
				propBundle.setBundles(resourceBundleList);
				ExtendedPropertiesBundle.cache.put(key, propBundle);
			}
		}
	}

	// Obtener el primer locale para el init
	public static Locale getDefaultCacheLocale() {
		if (ExtendedPropertiesBundle.cache != null) {
			Enumeration<?> keys = Collections.enumeration(ExtendedPropertiesBundle.cache.keySet());
			while (keys.hasMoreElements()) {
				CacheKey key = (CacheKey) keys.nextElement();
				return key.locale;
			}
		}
		return null;
	}

	protected static ResourceBundle getDatabaseBundle(String resourcesFileName, Locale locale) {
		try {
			if (ExtendedPropertiesBundle.useDatabaseBundle && (ExtendedPropertiesBundle.locator != null) && (((EntityReferenceLocator) ExtendedPropertiesBundle.locator)
					.getSessionId() >= 0)) {
				IDatabaseBundleManager remoteReference = (IDatabaseBundleManager) ExtendedPropertiesBundle.locator.getRemoteReference(
						ExtendedPropertiesBundle.dbBundleManagerName,
						((EntityReferenceLocator) ExtendedPropertiesBundle.locator).getSessionId());
				ResourceBundle bundle = remoteReference.getBundle(resourcesFileName, locale, ((EntityReferenceLocator) ExtendedPropertiesBundle.locator).getSessionId());
				if (bundle != null) {
					return bundle;
				}
			}
		} catch (Exception exc) {
			ExtendedPropertiesBundle.logger.error("{}", exc.getMessage(), exc);
		}
		return null;
	}

	protected static Map<Object, Object> getAllDatabaseResourceBundles(Locale locale) {
		try {
			if (ExtendedPropertiesBundle.useDatabaseBundle && (ExtendedPropertiesBundle.locator != null) && (((EntityReferenceLocator) ExtendedPropertiesBundle.locator)
					.getSessionId() >= 0)) {

				Map<Object, Object> bundles = null;
				if ((((ClientReferenceLocator) ExtendedPropertiesBundle.locator).getInitialContext() != null) && ((ClientReferenceLocator) ExtendedPropertiesBundle.locator)
						.getInitialContext().containsKey(InitialContext.ALL_RESOURCES_BUNDLES)) {
					bundles = (Map<Object, Object>) ((ClientReferenceLocator) ExtendedPropertiesBundle.locator).getInitialContext().get(InitialContext.ALL_RESOURCES_BUNDLES);
				} else {
					IDatabaseBundleManager remoteReference = (IDatabaseBundleManager) ExtendedPropertiesBundle.locator
							.getRemoteReference(ExtendedPropertiesBundle.dbBundleManagerName, ((EntityReferenceLocator) ExtendedPropertiesBundle.locator).getSessionId());
					bundles = remoteReference.getAllResourceBundles(locale, ((EntityReferenceLocator) ExtendedPropertiesBundle.locator).getSessionId());
				}

				if ((bundles != null) && !bundles.isEmpty()) {
					Map<Object, Object> result = new HashMap<>();
					for (Entry<Object, Object> entry : bundles.entrySet()) {
						String baseName = (String) entry.getKey();
						result.put(new CacheKey(baseName, locale), entry.getValue());
					}
					return result;
				}
			}
		} catch (Exception exc) {
			ExtendedPropertiesBundle.logger.error("{}", exc.getMessage(), exc);
		}
		return null;
	}

	public static void addModuleResourceBundle(String resourceBundle) {
		if ((resourceBundle == null) || (resourceBundle.length() == 0)) {
			return;
		}

		StringTokenizer st = new StringTokenizer(resourceBundle, ";");
		while (st.hasMoreTokens()) {
			String bundle = st.nextToken();
			if (!ExtendedPropertiesBundle.moduleResources.contains(bundle)) {
				ExtendedPropertiesBundle.moduleResources.add(bundle);
				// Check if exits bundles already cache.
				for (Entry<CacheKey, ExtendedPropertiesBundle> entry : ExtendedPropertiesBundle.cache.entrySet()) {
					CacheKey key = entry.getKey();
					ExtendedPropertiesBundle extendedPropertiesBundle = entry.getValue();
					ResourceBundle bundleToAdd = ResourceBundle.getBundle(bundle, key.getLocale());
					extendedPropertiesBundle.addBundle(bundleToAdd);
					ExtendedPropertiesBundle.logger.debug("{} {} has been added to {}", bundle, key.getLocale(), key);
				}
			} else {
				ExtendedPropertiesBundle.logger.warn("The {} bundle already exists in module resource list", bundle);
			}
		}
	}

	public static void reloadBundle() {
		if (ExtendedPropertiesBundle.cache != null) {
			ExtendedPropertiesBundle.cache.clear();
		}
		if (ExtendedPropertiesBundle.useDatabaseBundle) {
			ExtendedPropertiesBundle.databaseCache.clear();
		}

		if (ExtendedPropertiesBundle.extendedBundleCache != null) {
			ExtendedPropertiesBundle.extendedBundleCache.clear();
		}

		if ((((ClientReferenceLocator) ExtendedPropertiesBundle.locator).getInitialContext() != null) && ((ClientReferenceLocator) ExtendedPropertiesBundle.locator)
				.getInitialContext().containsKey(InitialContext.ALL_RESOURCES_BUNDLES)) {
			((ClientReferenceLocator) ExtendedPropertiesBundle.locator).getInitialContext().remove(InitialContext.ALL_RESOURCES_BUNDLES);
		}

		ResourceBundle extendedBundle = ExtendedPropertiesBundle.getExtendedBundle(
				((MainApplication) ApplicationManager.getApplication()).getResourcesFileName(),
				ApplicationManager.getApplication().getResourceBundle().getLocale());
		ApplicationManager.getApplication().setResourceBundle(extendedBundle);
	}

	public static boolean isUsingDatabaseBundle() {
		return ExtendedPropertiesBundle.useDatabaseBundle;
	}

	public static String getDbBundleManagerName() {
		return ExtendedPropertiesBundle.dbBundleManagerName;
	}

}
