package com.ontimize.builder.xml;

import java.awt.Component;
import java.awt.Container;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JToolBar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.ontimize.builder.ButtonBarBuilder;
import com.ontimize.gui.ApToolBarModule;
import com.ontimize.jee.common.builder.CustomNode;
import com.ontimize.jee.common.util.extend.ExtendedMenuXmlParser;
import com.ontimize.jee.common.util.extend.ExtendedXmlParser;
import com.ontimize.xml.DefaultXMLParametersManager;
import com.ontimize.xml.XMLInterpreter;

public class XMLButtonBarBuilder extends XMLInterpreter implements ButtonBarBuilder {

	private static final Logger logger = LoggerFactory.getLogger(XMLButtonBarBuilder.class);

	protected String baseClasspath = null;

	public static boolean INCLUDE_DEFAULT_LABELS = true;

	protected String defaultPackage = "com.ontimize.gui.";

	protected Map<Object, Object>			equivalenceLabelList	= new HashMap<>();

	protected JToolBar toolbar = null;

	protected static ExtendedMenuXmlParser toolbarParser = new ExtendedMenuXmlParser();

	/**
	 * Class used to create the application toolbar.
	 * @param uriLabelsFile URI to the labels file. Example 'http://.../xml/labels.xml'.<br>
	 *        The classes indicate in the XML file are in package com.ontimize.gui and this class uses
	 *        reflection (Class.forName()) to load them.
	 * @throws Exception
	 */
	public XMLButtonBarBuilder(String uriLabelsFile) throws Exception {
		if (XMLButtonBarBuilder.INCLUDE_DEFAULT_LABELS) {
			this.equivalenceLabelList = this.getDefaultLabelList();
		}
		try {
			this.processLabelFile(uriLabelsFile, this.equivalenceLabelList, new ArrayList<>());
		} catch (Exception e) {
			XMLButtonBarBuilder.logger.error("Processing label file", e);
		}
	}

	@Override
	public void appendButtonBar(JToolBar toolbar, String xmlDefinition) {
		try {
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(xmlDefinition.getBytes());
			Document current = this.getDocumentModel(byteArrayInputStream);
			CustomNode rootNode = new CustomNode(current.getDocumentElement());
			for (int i = 0; i < rootNode.getChildrenNumber(); i++) {
				this.processChildren(rootNode.child(i), toolbar);
			}
		} catch (Exception ex) {
			XMLButtonBarBuilder.logger.error("Append button", ex);
		}
	}

	/**
	 * Class used to create the application toolbar.
	 * @param uriLabelsFile URI to the labels file. Example 'http://.../xml/labels.xml'.<br>
	 * @param guiClassesPackage Package where the gui classes are. This class uses reflection to create
	 *        all objects (Class.forName()).
	 * @throws Exception
	 */
	public XMLButtonBarBuilder(String uriLabelsFile, String guiClassesPackage) throws Exception {
		this(uriLabelsFile);
		if (this.defaultPackage != null) {
			this.defaultPackage = guiClassesPackage;
		}
	}

	public XMLButtonBarBuilder(Map<Object, Object> equivalenceLabels) throws Exception {
		this.equivalenceLabelList = (Map<Object, Object>) new HashMap<>(equivalenceLabels).clone();
	}

	public XMLButtonBarBuilder(Map<Object, Object> equivalenceLabels, String guiClassesPackage) throws Exception {
		this(equivalenceLabels);
		if (this.defaultPackage != null) {
			this.defaultPackage = guiClassesPackage;
		}
	}

	protected Document performExtendedToolbar(Document doc, String fileURI) {
		Enumeration<URL> input = ExtendedXmlParser.getExtendedFile(fileURI, this.getBaseClasspath());
		Map<Object, Integer> extendsForm = new HashMap<>();
		if (input == null) {
			return doc;
		}

		Document extendedDocument = null;

		while (input.hasMoreElements()) {
			try {
				extendedDocument = this.getExtendedDocument(input.nextElement().openStream());
				Node s = extendedDocument.getChildNodes().item(0).getAttributes().getNamedItem("order");
				int index = s != null ? Integer.parseInt(s.getNodeValue()) : -1;
				extendsForm.put(extendedDocument, index);
			} catch (IOException exc) {
				XMLButtonBarBuilder.logger.error(null, exc);
			}
		}

		Set<Entry<Object, Integer>> set = extendsForm.entrySet();
		List<Entry<Object, Integer>> list = new ArrayList<>(set);
		Collections.sort(list, (o1, o2) -> o1.getValue().compareTo(o2.getValue()));

		if (extendedDocument == null) {
			return doc;
		}

		try {

			for (Map.Entry<Object, Integer> entry : list) {
				extendedDocument = (Document) entry.getKey();
				doc = XMLButtonBarBuilder.toolbarParser.parseExtendedXml(doc, extendedDocument);
				XMLButtonBarBuilder.logger.debug("{} ButtonBar extend, Load order -> {}", entry.getKey(),
						entry.getValue());
			}
		} catch (Exception ex) {
			XMLButtonBarBuilder.logger.error("Extending Toolbar", ex);
			// If an error happens executing the parser, original dom is reload
			return this.getDocumentModel(fileURI);
		}
		return doc;
	}

	@Override
	public JToolBar buildButtonBar(String uriFile) {
		// Creates the menu specified in the XML file.
		// Moves throw the tree and creates the IU
		// from the root node, and while children number is greater than zero
		long initTime = System.currentTimeMillis();
		Document current = this.getDocumentModel(uriFile);
		current = this.performExtendedToolbar(current, uriFile);
		CustomNode rootNode = new CustomNode(current.getDocumentElement());
		JToolBar jToolbar = null;
		try {
			if (rootNode.isTag()) {
				String tag = rootNode.getNodeInfo();
				String className = (String) this.equivalenceLabelList.get(tag);
				if (className == null) {
					XMLButtonBarBuilder.logger.debug("Label not found in equivalence list: {}", tag);
					// Try with tag
					className = tag;
				}
				// Converts tag to the correct form (defaultPackage + class
				// name)
				className = this.defaultPackage + className;
				// Gets attribute list
				NamedNodeMap attributeList = rootNode.attributeList();
				Map<Object, Object> attributeTable = DefaultXMLParametersManager.getParameters(className);
				for (int i = 0; i < attributeList.getLength(); i++) {
					Node nNode = attributeList.item(i);
					attributeTable.put(nNode.getNodeName(), nNode.getNodeValue());
				}
				try {
					Class classObject = Class.forName(className);
					try {
						Constructor[] constructors = classObject.getConstructors();
						Object[] parameters = { attributeTable };
						jToolbar = (JToolBar) constructors[0].newInstance(parameters);
					} catch (Exception e2) {
						XMLButtonBarBuilder.logger.error("Error creating object", e2);
					}
				} catch (Exception e) {
					XMLButtonBarBuilder.logger.error("Error loading class", e);
				}
			}
			this.toolbar = jToolbar;

			for (int i = 0; i < rootNode.getChildrenNumber(); i++) {
				CustomNode childNode = rootNode.child(i);
				this.processChildren(childNode, jToolbar);
			}
			long endTime = System.currentTimeMillis();
			double totalTime = (endTime - initTime) / 1000.0;
			XMLButtonBarBuilder.logger.trace("Total building time Buttons Bar: {} seconds.",
					new Double(totalTime).toString());
		} catch (Exception e) {
			XMLButtonBarBuilder.logger.error("Error building Buttons Bar", e);
		}
		return jToolbar;
	}

	protected void processChildren(CustomNode node, Container parent) {
		this.processChildren(node, parent, null);
	}

	protected void processChildren(CustomNode node, Container parent, String moduleName) {
		Container container = this.interpreterTag(node, parent, this.defaultPackage, this.equivalenceLabelList);
		if ((moduleName != null) && (parent instanceof JToolBar)) {
			// This is a module.Find AppToolBarModule
			Component[] comps = parent.getComponents();
			Component currentPlace = null;
			for (Component current : comps) {
				if (current instanceof ApToolBarModule) {
					String id = ((ApToolBarModule) current).getName();
					if (moduleName.equalsIgnoreCase(id)) {
						currentPlace = current;
						break;
					} else if (currentPlace == null) {
						currentPlace = current;
					}
				}
			}

			if (currentPlace != null) {

			}
		}
		for (int i = 0; i < node.getChildrenNumber(); i++) {
			CustomNode auxNode = node.child(i);
			this.processChildren(auxNode, container, moduleName);
		}
	}

	public void processModules(String moduleId, CustomNode node, Container jToolbar) {
		for (int i = 0; i < node.getChildrenNumber(); i++) {
			CustomNode childNode = node.child(i);
			this.processChildren(childNode, jToolbar, moduleId);
		}
	}

	public String getBaseClasspath() {
		return this.baseClasspath;
	}

	public void setBaseClasspath(String baseClasspath) {
		this.baseClasspath = baseClasspath;
	}

}
