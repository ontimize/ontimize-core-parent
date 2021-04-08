package com.ontimize.builder.xml;

import java.awt.Container;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.ontimize.builder.FormBuilder;
import com.ontimize.gui.Form;
import com.ontimize.module.ModuleManager;
import com.ontimize.util.ParseUtils;
import com.ontimize.util.extend.ExtendedFormXmlParser;
import com.ontimize.xml.XMLInterpreter;

/**
 * Implementation of {@link FormBuilder} from a XML File
 */
public class XMLFormBuilder extends XMLInterpreter implements FormBuilder {

    static final Logger logger = LoggerFactory.getLogger(XMLFormBuilder.class);

    public static boolean INCLUDE_DEFAULT_LABELS = true;

    protected String baseClasspath = null;

    protected String defaultPackage = "com.ontimize.gui.";

    protected Hashtable equivalenceLabelList = new Hashtable();

    protected String labelFileURI = null;

    protected ModuleManager moduleManager;

    protected static ExtendedFormXmlParser formParser = new ExtendedFormXmlParser();

    /**
     * @param labelFileURI URI to the labels file. Example 'http://.../xml/labels.xml'<br>
     *        This class uses reflection to load the objects (Class.forName())
     * @throws Exception
     */
    public XMLFormBuilder(String labelFileURI) throws Exception {
        if (XMLFormBuilder.INCLUDE_DEFAULT_LABELS) {
            this.equivalenceLabelList = this.getDefaultLabelList();
        }
        // labelFileURI is the labels equivalence file
        this.labelFileURI = labelFileURI;
        this.processLabelFile(this.labelFileURI, this.equivalenceLabelList, new ArrayList());
    }

    /**
     * @param labelFileURI
     * @param guiClassesPackage
     * @throws Exception
     */
    public XMLFormBuilder(String labelFileURI, String guiClassesPackage) throws Exception {
        this(labelFileURI);
        if (this.defaultPackage != null) {
            this.defaultPackage = guiClassesPackage;
        }
    }

    public XMLFormBuilder(Hashtable equivalenceLabelList) throws Exception {
        this.equivalenceLabelList = (Hashtable) equivalenceLabelList.clone();
    }

    public XMLFormBuilder(Hashtable equivalenceLabelList, String gUIClassPackage) throws Exception {
        this(equivalenceLabelList);
        if (this.defaultPackage != null) {
            this.defaultPackage = gUIClassPackage;
        }
    }

    protected Document performExtendedForm(Document doc, String fileURI) {
        try {

            doc = XMLFormBuilder.formParser.getExtendedDocumentForm(doc, fileURI, this.getBaseClasspath());

        } catch (Exception ex) {
            XMLFormBuilder.logger.error("Extending form", ex);
            // If an error happens executing the parser, original dom is reload.
            return this.getDocumentModel(fileURI);
        }
        return doc;
    }

    @Override
    public Form buildForm(Container parentContainer, String fileURI) {
        // Move throw the tree and crate the UI
        // From root node and while children number is greater than zero
        XMLFormBuilder.logger.debug("Creating form: {}", fileURI);
        try {
            long initTime = System.currentTimeMillis();
            parentContainer.setVisible(false);
            Document current = this.getDocumentModel(fileURI);
            current = this.performExtendedForm(current, fileURI);
            CustomNode auxiliar = new CustomNode(current.getDocumentElement());
            this.processChildren(auxiliar, parentContainer);
            parentContainer.setVisible(true);
            parentContainer.setSize(parentContainer.getSize());
            parentContainer.repaint();
            parentContainer.validate();
            long endTime = System.currentTimeMillis();
            double tiempoTranscurrido = (endTime - initTime) / 1000.0;
            XMLFormBuilder.logger.trace("Total time building UI: {}  seconds.",
                    new Double(tiempoTranscurrido).toString());
            return (Form) parentContainer.getComponent(0);
        } catch (Exception e) {
            XMLFormBuilder.logger.error("Building form: {}", fileURI, e);
            return null;
        }
    }

    @Override
    public Form buildForm(Container parentContainer, InputStream input) {
        try {
            long initTime = System.currentTimeMillis();
            parentContainer.setVisible(false);
            CustomNode auxiliar = new CustomNode(this.getDocumentModel(input).getDocumentElement());
            this.processChildren(auxiliar, parentContainer);
            parentContainer.setVisible(true);
            parentContainer.setSize(parentContainer.getSize());
            parentContainer.repaint();
            parentContainer.validate();
            long endTime = System.currentTimeMillis();
            double totalTime = (endTime - initTime) / 1000.0;
            XMLFormBuilder.logger.trace("Total time building UI from InputStream: {} seconds.",
                    new Double(totalTime).toString());
            return (Form) parentContainer.getComponent(0);
        } catch (Exception e) {
            XMLFormBuilder.logger.error("Building form from InputStream ", e);
            return null;
        }
    }

    public Form buildForm(Container parentContainer, StringBuffer fileContent) throws Exception {
        try {
            long initTime = System.currentTimeMillis();
            parentContainer.setVisible(false);
            CustomNode auxiliar = new CustomNode(this.getDocumentModel(fileContent).getDocumentElement());
            this.processChildren(auxiliar, parentContainer);
            parentContainer.setVisible(true);
            parentContainer.setSize(parentContainer.getSize());
            parentContainer.repaint();
            parentContainer.validate();
            long endTime = System.currentTimeMillis();
            double totalTime = (endTime - initTime) / 1000.0;
            XMLFormBuilder.logger.trace("Total time building UI from StringBuilder: {} seconds.",
                    new Double(totalTime).toString());
            return (Form) parentContainer.getComponent(0);
        } catch (Exception e) {
            XMLFormBuilder.logger.error("Building form from StringBuilder ", e);
            throw e;
        }
    }

    protected void processChildren(CustomNode node, Container parentContainer) {
        Container container = this.interpreterTag(node, parentContainer, this.defaultPackage,
                this.equivalenceLabelList);
        for (int i = 0; i < node.getChildrenNumber(); i++) {
            CustomNode cnAuxNode = node.child(i);
            if (cnAuxNode.isTag()) {
                if (this.moduleManager != null) {
                    Hashtable<String, String> attributes = cnAuxNode.hashtableAttribute();
                    if (attributes.containsKey(ModuleManager.MODULE_ATTR)) {
                        String modules = ParseUtils.getString(attributes.get(ModuleManager.MODULE_ATTR), "");
                        String attr = ParseUtils.getString(attributes.get("attr"), cnAuxNode.getNodeInfo());
                        StringTokenizer tokens = new StringTokenizer(modules, ";");
                        boolean hasModules = true;
                        while (tokens.hasMoreTokens()) {
                            String moduleName = tokens.nextToken();
                            if (!this.moduleManager.hasModule(moduleName)) {
                                hasModules = false;
                                XMLFormBuilder.logger.info("{} attr has not been created by {} module dependencies",
                                        attr, moduleName);
                                break;
                            }
                        }

                        if (!hasModules) {
                            continue;
                        }
                    }
                }
                this.processChildren(cnAuxNode, container);
            }
        }
    }

    public String getLabelFileURI() {
        return this.labelFileURI;
    }

    public String getBaseClasspath() {
        return this.baseClasspath;
    }

    public void setBaseClasspath(String baseClasspath) {
        this.baseClasspath = baseClasspath;
    }

    public Hashtable getCurrentLabelList() {
        return this.equivalenceLabelList;
    }

    public ModuleManager getModuleManager() {
        return this.moduleManager;
    }

    public void setModuleManager(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

}
