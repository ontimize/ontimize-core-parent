package com.ontimize.module;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ontimize.builder.xml.XMLApplicationBuilder;
import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.i18n.ExtendedPropertiesBundle;
import com.ontimize.jee.common.builder.CustomNode;
import com.ontimize.jee.common.util.extend.ExtendedClientApplicationParser;
import com.ontimize.jee.common.util.extend.ExtendedXmlParser;
import com.ontimize.jee.common.util.extend.OrderDocument;
import com.ontimize.jee.common.xml.XMLUtil;

public class ModuleManager {

	private static final Logger logger = LoggerFactory.getLogger(ModuleManager.class);

	private static final String ONTIMIZE_MODULE_FILE_NAME = "ontimize-module.xml";

	private static final String ONTIMIZE_MODULE_TAG = "OntimizeModule";

	private static final String CLIENT_TAG = "Client";

	private static final String SERVER_TAG = "Server";

	private static final String APPLICATION_TOOLBAR_TAG = "ApplicationToolBar";

	private static final String APPLICATION_MENUBAR_TAG = "ApplicationMenuBar";

	private static final String ENTITY_PROPERTIES_TAG = "EntityProperties";

	private static final String REMOTE_REFERENCES_TAG = "RemoteReferences";

	private static final String REFERENCES_TAG = "References";

	private static final String REFERENCE_LOCATOR = "ReferenceLocator";

	private static final String ID_ATTR = "id";

	private static final String PACKAGE_ATTR = "package";

	private static final String ARCHIVE_ATTR = "archive";

	private static final String RESOURCES_ATTR = "resources";

	private static final String LOCAL_ENTITY_PACKAGE_ATTR = "localentitypackage";

	private static final String LOCAL_ENTITIES_ATTR = "localentities";

	public static final String MODULE_ATTR = "module";

	public static final String REMOTE_REFERENCES_ELEMENT = "RemoteReferences";

	public static final String REMOTE_REFERENCE_ELEMENT = "RemoteReference";

	public static final String REFERENCES_ELEMENT = "References";

	public static final String REFERENCE_ELEMENT = "Reference";

	public static final String ENTITIES_CLASS = "EntityClass";

	protected List<OModule> modules = new ArrayList<>();

	protected ModuleType type;

	protected DocumentBuilder dBuilder;

	public ModuleManager(ModuleType type) {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			this.dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			ModuleManager.logger.error(null, e);
		}
		this.type = type;
	}

	public enum ModuleType {

		CLIENT, SERVER, BOTH

	}

	public void retrieveOntimizeModules() {

		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Enumeration<URL> resources = classLoader.getResources(ModuleManager.ONTIMIZE_MODULE_FILE_NAME);
			while (resources.hasMoreElements()) {
				URL element = resources.nextElement();
				Document doc = this.dBuilder.parse(element.openStream());
				// optional, but recommended
				// read this -
				// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
				doc.getDocumentElement().normalize();
				this.processModule(doc.getDocumentElement());
			}
		} catch (Exception ex) {
			ModuleManager.logger.error("Error parsing xml modules {}", ex.getMessage(), ex);
		}
	}

	public void processModule(String resource) {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Enumeration<URL> resources = classLoader.getResources(resource);
			while (resources.hasMoreElements()) {
				URL element = resources.nextElement();
				Document doc = this.dBuilder.parse(element.openStream());
				// optional, but recommended
				// read this -
				// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
				doc.getDocumentElement().normalize();
				doc = this.performExtendedModule(doc, resource);
				this.processModule(doc.getDocumentElement());
			}
		} catch (Exception ex) {
			ModuleManager.logger.error("Error parsing xml modules {}", ex.getMessage(), ex);
		}
	}

	protected Document performExtendedModule(Document doc, String fileURI) {
		Enumeration<URL> input = ExtendedXmlParser.getExtendedFile(fileURI, null);

		if ((input == null) || (!input.hasMoreElements())) {
			return doc;
		}

		List<OrderDocument> extendedDocumentList = new ArrayList<>();

		while (input.hasMoreElements()) {
			try {
				Document extendedDocument = XMLUtil.getExtendedDocument(input.nextElement().openStream());
				Node s = extendedDocument.getChildNodes().item(0).getAttributes().getNamedItem(ExtendedXmlParser.ORDER);
				int index = (s != null) ? Integer.parseInt(s.getNodeValue()) : -1;
				extendedDocumentList.add(new OrderDocument(index, extendedDocument));
			} catch (Exception e) {
				ModuleManager.logger.error("{}", e);
			}
		}

		Collections.sort(extendedDocumentList);

		for (OrderDocument oDocument : extendedDocumentList) {
			try {
				doc = new ExtendedClientApplicationParser().parseExtendedXml(doc, oDocument.getDocument());
				ModuleManager.logger.debug("Module file extends, Load order -> {}", oDocument.getIndex());
			} catch (Exception e) {
				ModuleManager.logger.error("Extending Module", e);
			}
		}

		return doc;
	}


	public void processModule(Element parentElement) {
		if (ModuleManager.ONTIMIZE_MODULE_TAG.equals(parentElement.getNodeName())) {
			OModule currentModule;
			if (parentElement.hasAttribute(ModuleManager.ID_ATTR)) {
				String idModule = parentElement.getAttribute(ModuleManager.ID_ATTR);
				ModuleManager.logger.info("Processing {} module ..........", idModule);
				currentModule = new OModule(idModule);
				this.modules.add(currentModule);
			} else {
				ModuleManager.logger.error("{} ATTRIBUTE in {} TAG is required to process the module",
						ModuleManager.ID_ATTR, ModuleManager.ONTIMIZE_MODULE_TAG);
				return;
			}

			if (parentElement.hasAttribute(ModuleManager.RESOURCES_ATTR)) {
				String resourceBundle = parentElement.getAttribute(ModuleManager.RESOURCES_ATTR);
				currentModule.setResources(resourceBundle);
				ExtendedPropertiesBundle.addModuleResourceBundle(resourceBundle);
			}

			if (ModuleType.CLIENT.equals(this.type) || ModuleType.BOTH.equals(this.type)) {
				this.processClientModule(parentElement, currentModule);
			}

			if (ModuleType.SERVER.equals(this.type) || ModuleType.BOTH.equals(this.type)) {
				this.processServerModule(parentElement, currentModule);
			}
		}
	}

	protected void processServerModule(Element parentElement, OModule currentModule) {
		NodeList serverList = parentElement.getElementsByTagName(ModuleManager.SERVER_TAG);
		if (serverList.getLength() != 1) {
			ModuleManager.logger.warn("Found {} {} tags.", serverList.getLength(), ModuleManager.SERVER_TAG);
		}
		for (int i = 0; i < serverList.getLength(); i++) {
			CustomNode serverNode = new CustomNode(serverList.item(i));
			if (serverNode.isTag()) {
				Map<String, String> attributes = serverNode.hashtableAttribute();
				if (attributes.containsKey(ModuleManager.PACKAGE_ATTR)) {
					currentModule.setServerPackage(attributes.get(ModuleManager.PACKAGE_ATTR));

					int number = serverNode.getChildrenNumber();
					for (int j = 0; j < number; j++) {
						CustomNode node = serverNode.child(j);
						if (node.isTag()) {
							String tag = node.getNodeInfo();
							if (ModuleManager.ENTITY_PROPERTIES_TAG.equalsIgnoreCase(tag)) {
								Map<String, String> entityProperyAttrs = node.hashtableAttribute();
								if (entityProperyAttrs.containsKey(ModuleManager.ARCHIVE_ATTR)) {
									ModuleManager.logger.debug("Process entity properties archive : {}",
											entityProperyAttrs.get(ModuleManager.ARCHIVE_ATTR));
									this.processEntityProperties(currentModule,
											entityProperyAttrs.get(ModuleManager.ARCHIVE_ATTR));
								} else {
									ModuleManager.logger.warn("Entity properties is required");
								}
							} else if (ModuleManager.REMOTE_REFERENCES_TAG.equalsIgnoreCase(tag)) {
								Map<String, String> remoteAttrs = node.hashtableAttribute();
								if (remoteAttrs.containsKey(ModuleManager.ARCHIVE_ATTR)) {
									ModuleManager.logger.debug("Process remote references archive : {}",
											remoteAttrs.get(ModuleManager.ARCHIVE_ATTR));
									this.processRemoteReferences(currentModule,
											remoteAttrs.get(ModuleManager.ARCHIVE_ATTR), this.dBuilder);
								} else {
									ModuleManager.logger.warn("Remote references archive is required");
								}
							} else if (ModuleManager.REFERENCES_TAG.equalsIgnoreCase(tag)) {
								Map<String, String> referencesAttrs = node.hashtableAttribute();
								if (referencesAttrs.containsKey(ModuleManager.ARCHIVE_ATTR)) {
									ModuleManager.logger.debug("Process remote references archive : {}",
											referencesAttrs.get(ModuleManager.ARCHIVE_ATTR));
									this.processReferences(currentModule,
											referencesAttrs.get(ModuleManager.ARCHIVE_ATTR), this.dBuilder);
								} else {
									ModuleManager.logger.warn("References archive is required");
								}
							}
						}
					}
				}
			}
		}
	}

	protected void processClientModule(Element parentElement, OModule currentModule) {
		NodeList clientList = parentElement.getElementsByTagName(ModuleManager.CLIENT_TAG);
		if (clientList.getLength() != 1) {
			ModuleManager.logger.warn("Found {} {} tags.", clientList.getLength(), ModuleManager.CLIENT_TAG);
		}
		for (int i = 0; i < clientList.getLength(); i++) {
			CustomNode clientNode = new CustomNode(clientList.item(i));
			if (clientNode.isTag()) {
				Map<String, String> attributes = clientNode.hashtableAttribute();
				if (attributes.containsKey(ModuleManager.PACKAGE_ATTR)) {
					currentModule.setClientBaseClasspath(attributes.get(ModuleManager.PACKAGE_ATTR));
				}
				int number = clientNode.getChildrenNumber();
				for (int j = 0; j < number; j++) {
					CustomNode node = clientNode.child(j);
					if (node.isTag()) {
						String tag = node.getNodeInfo();
						if (XMLApplicationBuilder.TOOLBAR.equalsIgnoreCase(tag)) {
							Map<String, String> toolBarAttrs = node.hashtableAttribute();
							if (toolBarAttrs.containsKey(ModuleManager.ARCHIVE_ATTR)) {
								ModuleManager.logger.debug("Process toolbar menu archive : {}",
										toolBarAttrs.get(ModuleManager.ARCHIVE_ATTR));
								this.processToolbar(currentModule, toolBarAttrs.get(ModuleManager.ARCHIVE_ATTR),
										this.dBuilder);
							} else {
								ModuleManager.logger.warn("Toolbar menu archive is required");
							}
						} else if (XMLApplicationBuilder.TOOLBARLISTENER.equalsIgnoreCase(tag)) {
							Map<String, String> param = node.hashtableAttribute();
							String classTListener = param.get("class");
							if (classTListener == null) {
								ModuleManager.logger.warn("Toolbar Listener not specified in {} module",
										currentModule.getId());
							} else {
								currentModule.setToolbarListener(classTListener);
							}
						} else if (XMLApplicationBuilder.MENU.equalsIgnoreCase(tag)) {
							Map<String, String> menuAttrs = node.hashtableAttribute();
							if (menuAttrs.containsKey(ModuleManager.ARCHIVE_ATTR)) {
								ModuleManager.logger.debug("Process menu archive : {}",
										menuAttrs.get(ModuleManager.ARCHIVE_ATTR));
								this.processMenu(currentModule, menuAttrs.get(ModuleManager.ARCHIVE_ATTR),
										this.dBuilder);
							} else {
								ModuleManager.logger.warn("Toolbar menu archive is required");
							}
						} else if (XMLApplicationBuilder.MENULISTENER.equalsIgnoreCase(tag)) {
							Map<String, String> param = node.hashtableAttribute();
							String classMListener = param.get("class");
							if (classMListener == null) {
								ModuleManager.logger.warn("Toolbar Listener not specified in {} module",
										currentModule.getId());
							} else {
								currentModule.setMenuListener(classMListener);
							}
						} else if (ModuleManager.REFERENCES_TAG.equalsIgnoreCase(tag)) {
							Map<String, String> referencesAttrs = node.hashtableAttribute();
							if (referencesAttrs.containsKey(ModuleManager.ARCHIVE_ATTR)) {
								ModuleManager.logger.debug("Process remote references archive : {}",
										referencesAttrs.get(ModuleManager.ARCHIVE_ATTR));
								this.processReferences(currentModule, referencesAttrs.get(ModuleManager.ARCHIVE_ATTR),
										this.dBuilder);
							} else {
								ModuleManager.logger.warn("References archive is required");
							}
						} else if (ModuleManager.REFERENCE_LOCATOR.equalsIgnoreCase(tag)) {
							Map<String, String> referencesAttrs = node.hashtableAttribute();
							if (referencesAttrs.containsKey(ModuleManager.LOCAL_ENTITY_PACKAGE_ATTR)) {
								currentModule.setLocalEntityPackage(
										referencesAttrs.get(ModuleManager.LOCAL_ENTITY_PACKAGE_ATTR));
							}
							if (referencesAttrs.containsKey(ModuleManager.LOCAL_ENTITIES_ATTR)) {
								String localEntities = referencesAttrs.get(ModuleManager.LOCAL_ENTITIES_ATTR);
								List<String> entitiesProperties = ApplicationManager.getTokensAt(localEntities, ";");
								currentModule.setLocalEntities(entitiesProperties);
							}
						} else {
							// FormManagers
							Map<String, String> nodeAttrs = node.hashtableAttribute();
							if (nodeAttrs.containsKey(ModuleManager.ID_ATTR)) {
								ModuleManager.logger.trace("Add {} tag with id: {}", tag,
										nodeAttrs.get(ModuleManager.ID_ATTR));
								currentModule.getFormManagers().add(node);
							} else {
								ModuleManager.logger.warn("FormManager hasn't been added because {} tag is required",
										ModuleManager.ID_ATTR);
							}
						}
					}
				}
			}
		}
	}

	protected void processToolbar(OModule module, String archiveFile, DocumentBuilder dBuilder) {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			StringBuilder toolbarPath = new StringBuilder(module.getClientBaseClasspath());
			toolbarPath.append(archiveFile);
			InputStream input = classLoader.getResourceAsStream(toolbarPath.toString());
			Document doc = dBuilder.parse(input);
			doc.getDocumentElement().normalize();
			if (ModuleManager.APPLICATION_TOOLBAR_TAG.equals(doc.getDocumentElement().getNodeName())) {
				CustomNode rootNode = new CustomNode(doc.getDocumentElement());
				module.setToolbar(rootNode);
			} else {
				ModuleManager.logger.warn("{} tag is required in {} file", ModuleManager.APPLICATION_TOOLBAR_TAG,
						toolbarPath);
			}
		} catch (Exception ex) {
			ModuleManager.logger.error("Error processing toolbar: {}", ex.getMessage(), ex);
		}
	}

	protected void processMenu(OModule module, String archiveFile, DocumentBuilder dBuilder) {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			StringBuilder menuPath = new StringBuilder(module.getClientBaseClasspath());
			menuPath.append(archiveFile);
			InputStream input = classLoader.getResourceAsStream(menuPath.toString());
			Document doc = dBuilder.parse(input);
			doc.getDocumentElement().normalize();
			if (ModuleManager.APPLICATION_MENUBAR_TAG.equals(doc.getDocumentElement().getNodeName())) {
				CustomNode rootNode = new CustomNode(doc.getDocumentElement());
				module.setMenu(rootNode);
			} else {
				ModuleManager.logger.warn("{} tag is required in {} file", ModuleManager.APPLICATION_MENUBAR_TAG,
						menuPath);
			}
		} catch (Exception ex) {
			ModuleManager.logger.error("Error processing toolbar: {}", ex.getMessage(), ex);
		}
	}

	protected void processRemoteReferences(OModule module, String archiveFile, DocumentBuilder dBuilder) {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			InputStream input = classLoader.getResourceAsStream(archiveFile);
			Document doc = dBuilder.parse(input);
			doc.getDocumentElement().normalize();
			if (ModuleManager.REMOTE_REFERENCES_ELEMENT.equals(doc.getDocumentElement().getNodeName())) {
				Node rootNode = doc.getDocumentElement();
				module.setRemoteReferences(rootNode);
			} else {
				ModuleManager.logger.warn("{} tag is required in {} file", ModuleManager.REMOTE_REFERENCES_ELEMENT, archiveFile);
			}
		} catch (Exception ex) {
			ModuleManager.logger.error("Error processing toolbar: {}", ex.getMessage(), ex);
		}
	}

	protected void processReferences(OModule module, String archiveFile, DocumentBuilder dBuilder) {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			InputStream input = classLoader.getResourceAsStream(archiveFile);
			Document doc = dBuilder.parse(input);
			doc.getDocumentElement().normalize();
			if (ModuleManager.REFERENCES_ELEMENT.equals(doc.getDocumentElement().getNodeName())) {
				Node rootNode = doc.getDocumentElement();
				module.setReferences(rootNode);
			} else {
				ModuleManager.logger.warn("{} tag is required in {} file", ModuleManager.REFERENCES_ELEMENT, archiveFile);
			}
		} catch (Exception ex) {
			ModuleManager.logger.error("Error processing toolbar: {}", ex.getMessage(), ex);
		}
	}

	protected void processEntityProperties(OModule module, String archiveFile) {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			InputStream input = classLoader.getResourceAsStream(archiveFile);
			Properties properties = new Properties();
			properties.load(input);

			if (properties.containsKey(ModuleManager.ENTITIES_CLASS)) {
				module.setEntityClass(properties.getProperty(ModuleManager.ENTITIES_CLASS));
				ModuleManager.logger.warn("{} parameter has been setted in {} module", ModuleManager.ENTITIES_CLASS, module.getId());
			}
			module.setEntityProperties(properties);
		} catch (Exception ex) {
			ModuleManager.logger.error("Error processing entity properties: {} {}", archiveFile, ex);
			module.setEntityProperties(new Properties());
		}
	}

	public List<OModule> getModules() {
		return this.modules;
	}

	public boolean hasModule(String name) {
		if ((this.modules != null) && (this.modules.size() > 0)) {
			return this.modules.contains(new OModule(name));
		}
		return false;
	}

}
