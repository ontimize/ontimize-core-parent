package com.ontimize.builder.xml;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.ontimize.builder.TreeBuilder;
import com.ontimize.jee.common.builder.CustomNode;
import com.ontimize.xml.XMLInterpreter;

/**
 * Implementation of a {@link TreeBuilder} from a XML file
 */
public class XMLTreeBuilder extends XMLInterpreter implements com.ontimize.builder.TreeBuilder {

	static final Logger logger = LoggerFactory.getLogger(XMLTreeBuilder.class);

	public static boolean INCLUDE_DEFAULT_LABELS = true;

	protected String defaultPackage = "com.ontimize.gui.";

	protected Map<Object, Object>	equivalenceLabelList	= new HashMap<>();

	/**
	 * @param uriLabelsFile URI to the labels file. Example 'http://.../xml/labels.xml'.<br>
	 * @throws Exception
	 */
	public XMLTreeBuilder(String uriLabelsFile) throws Exception {
		if (XMLTreeBuilder.INCLUDE_DEFAULT_LABELS) {
			this.equivalenceLabelList = this.getDefaultLabelList();
		}

		try {
			this.processLabelFile(uriLabelsFile, this.equivalenceLabelList, new ArrayList<>());
		} catch (Exception e) {
			XMLTreeBuilder.logger.error("Processing label file", e);
		}
	}

	/**
	 * @param uriLabelsFile URI to the labels file. Example 'http://.../xml/labels.xml'.<br>
	 * @param guiClassesPackage Default package where the gui classes are stored
	 * @throws Exception
	 */
	public XMLTreeBuilder(String uriLabelsFile, String guiClassesPackage) throws Exception {
		this(uriLabelsFile);
		if (this.defaultPackage != null) {
			this.defaultPackage = guiClassesPackage;
		}
	}

	public XMLTreeBuilder(Map<Object, Object> labelEquivalences) throws Exception {
		this.equivalenceLabelList = (Map<Object, Object>) new HashMap<>(labelEquivalences).clone();
	}

	public XMLTreeBuilder(Map<Object, Object> labelEquivalences, String guiClassesPackage) throws Exception {
		this(labelEquivalences);
		if (this.defaultPackage != null) {
			this.defaultPackage = guiClassesPackage;
		}
	}

	protected TreeModel buildTree(CustomNode aux) {
		String tag = aux.getNodeInfo();
		String className = (String) this.equivalenceLabelList.get(tag);
		if (className == null) {
			XMLTreeBuilder.logger.debug("Label not found in equivalence list: {}", tag);
			// Trying with the tag
			className = tag;
		}
		// Convert the tag to the correct format (package + class name)
		className = this.defaultPackage + className;
		// Get the attribute list
		NamedNodeMap attributeList = aux.attributeList();
		Map<Object, Object> attributeTable = new HashMap<>();
		DefaultMutableTreeNode guiRootNode = null;
		for (int i = 0; i < attributeList.getLength(); i++) {
			Node auxNode = attributeList.item(i);
			attributeTable.put(auxNode.getNodeName(), auxNode.getNodeValue());
		}
		try {
			Class<?> classObject = Class.forName(className);
			try {
				Constructor<?>[] constructors = classObject.getConstructors();
				Object[] parameters = { attributeTable };
				guiRootNode = (DefaultMutableTreeNode) constructors[0].newInstance(parameters);
				this.processChildren(aux, guiRootNode);
			} catch (Exception e2) {
				XMLTreeBuilder.logger.error("Error creating object. ", e2);
			}
		} catch (Exception exc) {
			XMLTreeBuilder.logger.error("Error loading class", exc);
		}
		return new DefaultTreeModel(guiRootNode);
	}

	@Override
	public TreeModel buildTree(String fileURI) {
		// This function allows to create a object tree for tree building
		CustomNode aux = new CustomNode(this.getDocumentModel(fileURI).getDocumentElement());
		return this.buildTree(aux);
	}

	@Override
	public TreeModel buildTree(StringBuffer content) {
		CustomNode aux = null;
		try {
			aux = new CustomNode(this.getDocumentModel(content).getDocumentElement());
		} catch (Exception exc) {
			XMLTreeBuilder.logger.error(null, exc);
		}
		return this.buildTree(aux);
	}

	protected void processChildren(CustomNode node, DefaultMutableTreeNode guiNode) {
		for (int i = 0; i < node.getChildrenNumber(); i++) {
			if (node.child(i).isTag()) {
				String className = (String) this.equivalenceLabelList.get(node.child(i).getNodeInfo());

				if (className == null) {
					String tag = node.child(i).getNodeInfo();
					XMLTreeBuilder.logger.debug("Label not found in equivalence list: {}", tag);
					// Trying with the tag
					className = tag;
				}

				// Convert the tag to the correct format (package + class name)
				className = this.defaultPackage + className;
				// Get the attribute list
				DefaultMutableTreeNode guiChildNode = null;
				NamedNodeMap attributeList = node.child(i).attributeList();
				Map<Object, Object> attributeTable = new HashMap<>();
				for (int j = 0; j < attributeList.getLength(); j++) {
					Node auxNode = attributeList.item(j);
					attributeTable.put(auxNode.getNodeName(), auxNode.getNodeValue());
				}
				try {
					Class<?> classObject = Class.forName(className);
					try {
						Constructor<?>[] constructors = classObject.getConstructors();
						Object[] parameters = { attributeTable };
						guiChildNode = (DefaultMutableTreeNode) constructors[0].newInstance(parameters);
						guiNode.add(guiChildNode);
					} catch (Exception e2) {
						XMLTreeBuilder.logger.error("Error creating object", e2);
					}
				} catch (Exception exc) {
					XMLTreeBuilder.logger.error("Error loading class ", exc);
				}
				this.processChildren(node.child(i), guiChildNode);
			}
		}
	}

}
