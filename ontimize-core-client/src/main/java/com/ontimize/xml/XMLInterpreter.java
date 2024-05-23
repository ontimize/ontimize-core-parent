package com.ontimize.xml;

import java.awt.Container;
import java.awt.LayoutManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ontimize.gui.field.FormComponent;
import com.ontimize.jee.common.builder.CustomNode;
import com.ontimize.jee.common.xml.DocumentTreeModel;
import com.ontimize.xml.DefaultXMLParametersManager.ParameterValue;

/**
 * Abstract class which method are used by <code>TreeBuilder</code> and <code>FormBuilder</code> to handle the xml descriptions
 */
public abstract class XMLInterpreter {

	private static final Logger		logger		= LoggerFactory.getLogger(XMLInterpreter.class);

	public static boolean			SILENT		= false;

	public static final String		LABELS_FILE	= "com/ontimize/gui/labels.xml";

	protected static final String	IMPORT_TAG	= "import";

	public Document getDocumentModel(InputStream input) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// First of all the labels file
		long tiempoInicial = System.currentTimeMillis();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(input);
			long finalTime = System.currentTimeMillis();
			double passTime = (finalTime - tiempoInicial) / 1000.0;
			XMLInterpreter.logger.trace("Time parsing xml {} seconds.", new Double(passTime).toString());
			return document;
		} catch (Exception e) {
			XMLInterpreter.logger.error("{}", e.getMessage(), e);
			return null;
		}
	}

	public Document getDocumentModel(String fileURI) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// First of all the labels file
		long initialTime = System.currentTimeMillis();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(fileURI);
			long finalTime = System.currentTimeMillis();
			double passTime = (finalTime - initialTime) / 1000.0;
			XMLInterpreter.logger.trace("Time parsing xml {} seconds.", new Double(passTime).toString());
			return document;
		} catch (Exception e) {
			XMLInterpreter.logger.error("{}", e.getMessage(), e);
			return null;
		}
	}

	public Document getExtendedDocument(String fileURI) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(fileURI);
			return document;
		} catch (Exception e) {
			XMLInterpreter.logger.error("{}", e.getMessage(), e);
			return null;
		}
	}

	public Document getExtendedDocument(InputStream input) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(input);
			return document;
		} catch (Exception e) {
			XMLInterpreter.logger.error("{}", e.getMessage(), e);
			return null;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					XMLInterpreter.logger.error(null, e);
				}
			}
		}
	}

	public Document getDocumentModel(StringBuffer fileContent) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// First of all the labels file
		long initialTime = System.currentTimeMillis();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new StringBufferInputStream(fileContent.toString()));
			long finalTime = System.currentTimeMillis();
			double passTime = (finalTime - initialTime) / 1000.0;
			XMLInterpreter.logger.trace("Time parsing xml {} seconds.", new Double(passTime).toString());
			return document;
		} catch (Exception exc) {
			XMLInterpreter.logger.error("{}", exc.getMessage(), exc);
			throw exc;
		}
	}

	public DocumentTreeModel getDocumentTreeModel(Document document) {
		return new DocumentTreeModel(document.getDocumentElement());
	}

	public Node getRoot(Document document) {
		return document.getDocumentElement();
	}

	protected void processLabelFile(String fileURI, Map<Object, Object> equivalences, List<Object> processedFiled) throws Exception {
		if (processedFiled.contains(fileURI)) {
			return;
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(fileURI);
			NodeList importList = document.getElementsByTagName(XMLInterpreter.IMPORT_TAG);
			for (int i = 0; i < importList.getLength(); i++) {
				Node currentImport = importList.item(i);
				if (currentImport.hasAttributes()) {
					NamedNodeMap map = currentImport.getAttributes();
					Node srcNode = map.getNamedItem("src");
					if (srcNode != null) {
						String filePath = srcNode.getNodeValue().trim();
						URL url = this.getClass().getClassLoader().getResource(filePath);
						if (url != null) {
							this.processLabelFile(url.toString(), equivalences, processedFiled);
						}
					}
				}
			}

			DocumentTreeModel model = this.getDocumentTreeModel(document);
			CustomNode rootNode = (CustomNode) model.getRoot();
			for (int i = 0; i < rootNode.getChildrenNumber(); i++) {
				if (rootNode.child(i).isTag()) {
					String sLabel = rootNode.child(i).getNodeInfo();
					if (XMLInterpreter.IMPORT_TAG.equals(sLabel)) {
						continue;
					}
					for (int j = 0; j < rootNode.child(i).getChildrenNumber(); j++) {
						if (rootNode.child(i).child(j).isTag()) {
							String equivalence = rootNode.child(i).child(j).getNodeInfo();
							equivalences.put(sLabel, equivalence);
							break;
						}
					}
				}
			}
			processedFiled.add(fileURI);
		} catch (Exception exc) {
			throw exc;
		}

	}

	public Map<Object, Object> getDefaultLabelList() throws Exception {
		Map<Object, Object> equivalenceList = new HashMap<>();
		URL labelsURL = this.getClass().getClassLoader().getResource(XMLInterpreter.LABELS_FILE);
		if (labelsURL == null) {
			return equivalenceList;
		}
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(labelsURL.openStream());
			DocumentTreeModel model = this.getDocumentTreeModel(document);
			CustomNode rootNode = (CustomNode) model.getRoot();
			for (int i = 0; i < rootNode.getChildrenNumber(); i++) {
				if (rootNode.child(i).isTag()) {
					String sLabel = rootNode.child(i).getNodeInfo();
					for (int j = 0; j < rootNode.child(i).getChildrenNumber(); j++) {
						if (rootNode.child(i).child(j).isTag()) {
							String equivalence = rootNode.child(i).child(j).getNodeInfo();
							equivalenceList.put(sLabel, equivalence);
							break;
						}
					}
				}
			}
			return equivalenceList;
		} catch (Exception exc) {
			throw exc;
		}
	}

	protected Container interpreterTag(CustomNode childNode, Container containerParent, String packageName, Map<Object, Object> labelsEquivalenceList) {
		Container childContainer = null;
		if (childNode.isTag()) {
			long t = System.currentTimeMillis();
			String tag = childNode.getNodeInfo();
			LayoutManager parentLayout = containerParent.getLayout();
			String className = (String) labelsEquivalenceList.get(tag);
			if (className == null) {
				XMLInterpreter.logger.debug("Tag not found: {}", tag);
				// Try with the tag
				className = tag;
			}
			// Converts the tag to the appropriate format using the package name
			className = packageName + className;
			// Gets the attribute list
			NamedNodeMap attributeList = childNode.attributeList();
			Map<Object, Object> attributeTable = new HashMap<>();

			try {
				// Default parameters
				DefaultXMLParametersManager.ParameterValue[] params = DefaultXMLParametersManager.getStartsWith(className);
				if (params != null) {
					for (ParameterValue param : params) {
						attributeTable.put(param.getParameter(), param.getValue());
					}
				}
				for (int i = 0; i < attributeList.getLength(); i++) {
					Node node = attributeList.item(i);
					attributeTable.put(node.getNodeName(), node.getNodeValue());
				}
				Class<?> classObject = Class.forName(className);
				try {
					childContainer = (Container) this.createComponent(tag, classObject, attributeTable);
					childContainer.setVisible(true);
					containerParent.add(childContainer, ((FormComponent) childContainer).getConstraints(parentLayout));
					XMLInterpreter.logger.trace("Time to create the object {} : {}", className, System.currentTimeMillis() - t);
				} catch (Exception e2) {
					XMLInterpreter.logger.error("Error creating object {}", className, e2);
				}
			} catch (Exception e) {
				XMLInterpreter.logger.error("Error loading class: {}", className, e);
				childContainer = null;
			}
		}
		return childContainer;
	}

	protected Object createComponent(String tagName, Class<?> componentClass, Map<Object, Object> attributes) throws Exception {
		Object[] parameters = { attributes };
		Class[] p = { Hashtable.class };
		Constructor constructorHash = componentClass.getConstructor(p);
		return constructorHash.newInstance(parameters);
	}

}
