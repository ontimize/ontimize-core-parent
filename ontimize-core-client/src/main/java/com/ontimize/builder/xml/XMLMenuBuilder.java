package com.ontimize.builder.xml;

import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.ontimize.builder.MenuBuilder;
import com.ontimize.gui.ApplicationMenuBar;
import com.ontimize.gui.CheckMenuItem;
import com.ontimize.gui.RadioMenuItem;
import com.ontimize.gui.container.RadioItemGroup;
import com.ontimize.gui.field.IdentifiedElement;
import com.ontimize.jee.common.builder.CustomNode;
import com.ontimize.jee.common.util.extend.ExtendedMenuXmlParser;
import com.ontimize.jee.common.util.extend.ExtendedXmlParser;
import com.ontimize.xml.XMLInterpreter;

public class XMLMenuBuilder extends XMLInterpreter implements MenuBuilder {

	static final Logger logger = LoggerFactory.getLogger(XMLMenuBuilder.class);

	public static boolean INCLUDE_DEFAULT_LABELS = true;

	protected String defaultPackage = "com.ontimize.gui.";

	protected Map<Object, Object>			equivalenceLabelList	= new HashMap<>();

	protected JMenuBar menuBar = null;

	protected static ExtendedMenuXmlParser menuParser = new ExtendedMenuXmlParser();

	protected String baseClasspath = null;

	/**
	 * @param uriLabelsFile URI to the labels file. Example 'http://.../xml/labels.xml'.<br>
	 * @throws Exception
	 */
	public XMLMenuBuilder(String uriLabelsFile) throws Exception {
		if (XMLMenuBuilder.INCLUDE_DEFAULT_LABELS) {
			this.equivalenceLabelList = this.getDefaultLabelList();
		}

		try {
			this.processLabelFile(uriLabelsFile, this.equivalenceLabelList, new ArrayList());
		} catch (Exception e) {
			XMLMenuBuilder.logger.error("Process Label file", e);
		}
	}

	/**
	 * @param uriLabelsFile URI to the labels file. Example 'http://.../xml/labels.xml'.<br>
	 * @param guiClassesPackage Package where all classes specified in the xml file are stored
	 * @throws Exception
	 */
	public XMLMenuBuilder(String uriLabelsFile, String guiClassesPackage) throws Exception {
		this(uriLabelsFile);
		if (this.defaultPackage != null) {
			this.defaultPackage = guiClassesPackage;
		}
	}

	public XMLMenuBuilder(Map<Object, Object> labelEquivalences) throws Exception {
		this.equivalenceLabelList = (Map<Object, Object>) ((HashMap<Object, Object>) labelEquivalences).clone();
	}

	public XMLMenuBuilder(Map<Object, Object> labelEquivalences, String guiClassesPackage) throws Exception {
		this(labelEquivalences);
		if (this.defaultPackage != null) {
			this.defaultPackage = guiClassesPackage;
		}
	}

	protected Document performExtendedMenu(Document doc, String fileURI) {
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
			} catch (IOException e) {
				// TODO Auto-generated catch block
				XMLMenuBuilder.logger.error(null, e);
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
				doc = XMLMenuBuilder.menuParser.parseExtendedXml(doc, extendedDocument);
				XMLMenuBuilder.logger.debug("{} Menu extend, Load order -> {}", entry.getKey(), entry.getValue());
			}
		} catch (Exception ex) {
			XMLMenuBuilder.logger.error("Extending menu", ex);
			// If an error happens executing the parser, the original dom is
			// reloaded
			return this.getDocumentModel(fileURI);
		}
		return doc;
	}

	@Override
	public void appendMenu(JMenuBar menuBar, String xmlDefinition) {
		try {
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(xmlDefinition.getBytes());
			Document current = this.getDocumentModel(byteArrayInputStream);
			CustomNode rootNode = new CustomNode(current.getDocumentElement());
			for (int i = 0; i < rootNode.getChildrenNumber(); i++) {
				this.processChildren(rootNode.child(i), menuBar, true, false);
			}
		} catch (Exception ex) {
			XMLMenuBuilder.logger.error("Append menu", ex);
		}
	}

	@Override
	public JMenuBar buildMenu(String fileURI) {
		// Creates a menu. This menu is specified in the xml file
		long initTime = System.currentTimeMillis();
		Document current = this.getDocumentModel(fileURI);
		current = this.performExtendedMenu(current, fileURI);
		CustomNode rootNode = new CustomNode(current.getDocumentElement());
		MenuElement menu = null;
		try {
			if (rootNode.isTag()) {
				String tag = rootNode.getNodeInfo();
				String className = (String) this.equivalenceLabelList.get(tag);
				if (className == null) {
					XMLMenuBuilder.logger.debug("Label not found in equivalence list: {}", tag);
					// Trying with the tag
					className = tag;
				}
				// Convert the tag to the correct format (package + class name)
				className = this.defaultPackage + className;
				// Get the attribute list
				NamedNodeMap attributeList = rootNode.attributeList();
				Hashtable attributeTable = new Hashtable();
				for (int i = 0; i < attributeList.getLength(); i++) {
					Node node = attributeList.item(i);
					attributeTable.put(node.getNodeName(), node.getNodeValue());
				}
				try {
					Class classObject = Class.forName(className);
					try {
						Constructor[] constructors = classObject.getConstructors();
						Object[] parameters = { attributeTable };
						menu = (MenuElement) constructors[0].newInstance(parameters);
					} catch (Exception e2) {
						XMLMenuBuilder.logger.error("Error creatin object.", e2);
					}
				} catch (Exception e) {
					XMLMenuBuilder.logger.error("Error loading class {}", className, e);
				}
			}
			this.menuBar = (JMenuBar) menu;
			for (int i = 0; i < rootNode.getChildrenNumber(); i++) {
				this.processChildren(rootNode.child(i), menu);
			}
			long endTime = System.currentTimeMillis();
			double totalTime = (endTime - initTime) / 1000.0;
			XMLMenuBuilder.logger.trace("Total time building Menu: {} seconds", new Double(totalTime).toString());
		} catch (Exception e) {
			XMLMenuBuilder.logger.error("Error building Menu", e);
		}
		return (JMenuBar) menu;
	}

	public JMenuBar buildMenu(StringBuffer fileContent) throws Exception {
		// fileContent must be a xml file
		MenuElement menu = null;
		try {
			long initTime = System.currentTimeMillis();

			CustomNode rootNode = new CustomNode(this.getDocumentModel(fileContent).getDocumentElement());

			if (rootNode.isTag()) {
				String tag = rootNode.getNodeInfo();
				String className = (String) this.equivalenceLabelList.get(tag);
				if (className == null) {
					XMLMenuBuilder.logger.debug("Label not found in equivalence list: {}" + tag);
					// Trying with the tag
					className = tag;
				}
				// Convert the tag to the correct format (package + class name)
				className = this.defaultPackage + className;
				// Get the attribute list
				NamedNodeMap attributeList = rootNode.attributeList();
				Hashtable attributeTable = new Hashtable();
				for (int i = 0; i < attributeList.getLength(); i++) {
					Node node = attributeList.item(i);
					attributeTable.put(node.getNodeName(), node.getNodeValue());
				}
				try {
					Class classObject = Class.forName(className);
					try {
						Constructor[] constructors = classObject.getConstructors();
						Object[] parameters = { attributeTable };
						menu = (MenuElement) constructors[0].newInstance(parameters);
					} catch (Exception e2) {
						XMLMenuBuilder.logger.error("Error creating object.", e2);
					}
				} catch (Exception e) {
					XMLMenuBuilder.logger.error("Error loading class: {}", className, e);
				}
			}
			this.menuBar = (JMenuBar) menu;
			for (int i = 0; i < rootNode.getChildrenNumber(); i++) {
				this.processChildren(rootNode.child(i), menu);
			}
			long endTime = System.currentTimeMillis();
			double totalTime = (endTime - initTime) / 1000.0;
			XMLMenuBuilder.logger.trace("Total time building Menu: {} seconds.", new Double(totalTime).toString());
		} catch (Exception e) {
			XMLMenuBuilder.logger.error("Error building Menu {}", e);
			throw e;
		}
		return (JMenuBar) menu;
	}

	protected void processChildren(CustomNode node, Object parent) {
		this.processChildren(node, parent, false, false);
	}

	protected void processChildren(CustomNode node, Object parent, boolean dynamic, boolean module) {
		Object menuElement = this.translateTag(node, parent, this.defaultPackage, this.equivalenceLabelList, dynamic,
				module);
		if (menuElement instanceof RadioItemGroup) {
			if (this.menuBar instanceof ApplicationMenuBar) {
				((ApplicationMenuBar) this.menuBar).add((RadioItemGroup) menuElement);
			}
			for (int i = 0; i < node.getChildrenNumber(); i++) {
				Object childElement = this.translateTag(node.child(i), parent, this.defaultPackage,
						this.equivalenceLabelList, dynamic, module);
				// If the element is a RadioButton element it is added
				if (childElement != null) {
					if (childElement instanceof RadioMenuItem) {
						((RadioItemGroup) menuElement).add((AbstractButton) childElement);
					} else if (childElement instanceof CheckMenuItem) {
						((RadioItemGroup) menuElement).add((AbstractButton) childElement);
					}
				}
			}
		} else {
			for (int i = 0; i < node.getChildrenNumber(); i++) {
				CustomNode auxNode = node.child(i);
				this.processChildren(auxNode, menuElement, dynamic, module);
			}
		}
	}

	public void processModules(CustomNode node, Object parent) {
		for (int i = 0; i < node.getChildrenNumber(); i++) {
			CustomNode childNode = node.child(i);
			this.processChildren(childNode, parent, false, true);
		}
	}

	protected Object translateTag(CustomNode childNode, Object parent, String packageName, Map<Object, Object> equivalenceList) {
		return this.translateTag(childNode, parent, packageName, equivalenceList, false, false);
	}

	protected Object findAttr(Object parent, String attr) {
		if (attr == null) {
			return null;
		}

		if (parent instanceof JComponent) {
			JComponent cParent = (JComponent) parent;
			Component[] components = cParent.getComponents();
			for (Component current : components) {
				if (current instanceof IdentifiedElement) {
					IdentifiedElement identify = (IdentifiedElement) current;
					if (attr.equals(identify.getAttribute())) {
						return current;
					}
				}
			}
		}
		return null;
	}

	protected Object translateTag(CustomNode childNode, Object parent, String packageName, Map<Object, Object> equivalenceList,
			boolean dynamic, boolean module) {
		Object childMenuElement = null;
		if (childNode.isTag()) {
			String tag = childNode.getNodeInfo();
			if (module) {
				Map<String, String> params = childNode.hashtableAttribute();
				String attr = params.get("attr");
				Object current = this.findAttr(parent, attr);
				if (current != null) {
					return current;
				}
			}

			String className = (String) this.equivalenceLabelList.get(tag);
			if (className == null) {
				XMLMenuBuilder.logger.debug("Label not found in equivalence list:{}", tag);
				// Trying with the tag
				className = tag;
			}
			// Convert the tag to the correct format (package + class name)
			className = packageName + className;
			// Get the attribute list
			NamedNodeMap attributeList = childNode.attributeList();
			Map<Object, Object> attributeTable = new HashMap<>();
			for (int i = 0; i < attributeList.getLength(); i++) {
				Node node = attributeList.item(i);
				attributeTable.put(node.getNodeName(), node.getNodeValue());
			}
			attributeTable.put("dynamic", Boolean.toString(dynamic));

			try {
				Class<?> classObject = Class.forName(className);
				try {
					Constructor[] constructors = classObject.getConstructors();
					Object[] parameters = { attributeTable };
					Object element = constructors[0].newInstance(parameters);
					if (element instanceof MenuElement) {
						childMenuElement = element;
					} else {
						if (element instanceof RadioItemGroup) {
							childMenuElement = element;
						} else {
							childMenuElement = element;
						}
					}
					if (parent instanceof JMenuBar) {
						JMenuBar pa = (JMenuBar) parent;
						if (childMenuElement instanceof JMenuItem) {
							pa.add((JMenuItem) childMenuElement);
						} else {
							// If element is a ButtonGroup, it must not be
							// added.
							// The children of RadioItemGroup must be added to
							// the
							// ButtonGroup parent
							if (childMenuElement instanceof RadioItemGroup) {
							} else {
								pa.add((Component) childMenuElement);
							}
						}
					} else {
						if (parent instanceof JMenu) {
							JMenu pa = (JMenu) parent;
							if (childMenuElement instanceof JMenuItem) {
								pa.add((JMenuItem) childMenuElement);
							} else {
								if (childMenuElement instanceof RadioItemGroup) {

								} else {
									pa.add((Component) childMenuElement);
								}
							}
						}
					}
				} catch (Exception exc2) {
					XMLMenuBuilder.logger.error("Error creating object", exc2);
				}
			} catch (Exception exc) {
				XMLMenuBuilder.logger.error("Error loading class {}", className, exc);
			}
		}
		return childMenuElement;
	}

	public String getBaseClasspath() {
		return this.baseClasspath;
	}

	public void setBaseClasspath(String baseClasspath) {
		this.baseClasspath = baseClasspath;
	}

}
