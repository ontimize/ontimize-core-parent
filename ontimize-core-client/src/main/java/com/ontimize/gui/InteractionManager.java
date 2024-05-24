package com.ontimize.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.tree.TreePath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.annotation.FormComponent;
import com.ontimize.builder.InteractionManagerActionBuilder;
import com.ontimize.builder.xml.XMLInteractionManagerActionBuilder;
import com.ontimize.gui.button.Button;
import com.ontimize.gui.button.HelpButton;
import com.ontimize.gui.button.RefreshTableButton;
import com.ontimize.gui.field.DataComponent;
import com.ontimize.gui.field.DateDataField;
import com.ontimize.gui.field.IdentifiedElement;
import com.ontimize.gui.manager.IFormManager;
import com.ontimize.gui.manager.ITreeFormManager;
import com.ontimize.gui.table.Table;
import com.ontimize.gui.tree.OTreeNode;
import com.ontimize.gui.tree.Tree;
import com.ontimize.jee.common.gui.SearchValue;
import com.ontimize.jee.common.util.remote.BytesBlock;
import com.ontimize.printing.HTMLProcessor;
import com.ontimize.util.templates.TemplateUtils;

/**
 *
 * InteractionManager is the class that manages the {@link Form} basic events. The
 * {@link FormManager} sets an InteractionManager to every Form. When a specific InteractionManager
 * is needed, this class must be extended, and the relation between the Form to manage and the
 * implemented Interaction Manager must be done in the way specified by the {@link FormManager}
 * <p>
 * <b>Important</b>
 * <p>
 * In order to make this Manager to work right, the buttons (see {@link Button}) must have the
 * following standard keys:
 * <ul>
 * <li>query
 * <li>insert
 * <li>update
 * <li>delete
 * </ul>
 *
 *
 */
public class InteractionManager implements Freeable, ValueChangeListener, DataNavigationListener {

    private static final Logger logger = LoggerFactory.getLogger(InteractionManager.class);

    /**
     * Activates the DEBUG mode, which prints some useful information about the execution of this class.
     */
    // public static boolean DEBUG = false;

    /**
     * Activates a new state diagram, in which in {@link #QUERYINSERT} state you can either insert or
     * query directly.
     */
    public static boolean NEWMODE = false;

    public static String selectionPrintingKey = "interactionmanager.please_select_data_to_print";

    public static boolean CHECK_MODIFIED_DATA_CHANGED_DEFAULT_VALUE = true;

    public static final int QUERYINSERT = 0;

    public static final int QUERY = 1;

    public static final int INSERT = 2;

    public static final int UPDATE = 3;

    public static final String HELP_KEY = "help";

    public static final String QUERY_KEY = "query";

    public static final String QUERY_INSERT_KEY = "queryinsert";

    public static final String INSERT_KEY = "insert";

    public static final String UPDATE_KEY = "update";

    public static final String DELETE_KEY = "delete";

    public static final String ADVANCED_QUERY_KEY = "advancedquery";

    protected String f7Button = InteractionManager.QUERY_KEY;

    protected String f9Button = InteractionManager.INSERT_KEY;

    protected String f11Button = InteractionManager.UPDATE_KEY;

    protected String f12Button = InteractionManager.DELETE_KEY;

    protected List<InteractionManagerModeListener> interactionManagerModeListenerList = null;

    protected boolean dataChangedEventProcessing = true;

    protected static String warningBirthdayMessage = "M_BIRTHDAY_MUST_BE_IN_THE_PAST_CONTINUE_INSERT";

    protected boolean setDefaultFocusEnabled = true;

    /**
     * Enables or disables checking the modified data when performing a navigation operation in the
     * application, for example, changing the record when a field was modified.
     */
    protected boolean checkModifiedDataChangeEvent = InteractionManager.CHECK_MODIFIED_DATA_CHANGED_DEFAULT_VALUE;

    /**
     * Class that creates a thread which prints the form contents using a {@link HTMLProcessor}
     */
    protected class PrintingThread extends Thread {

        HTMLProcessor processor = null;

        public PrintingThread(HTMLProcessor p) {
            this.processor = p;
        }

        @Override
        public void run() {
            // Now preliminary presentation
            final PageFormat pf = PrinterJob.getPrinterJob().pageDialog(PrinterJob.getPrinterJob().defaultPage());
            JDialog d = new JDialog(InteractionManager.this.managedForm.getParentFrame(), true);
            JButton printButton = new JButton(InteractionManager.this.managedForm.printIcon);
            JTextPane tPane = this.processor.getJTextPane();
            JScrollPane scroll = new JScrollPane(tPane);
            d.getContentPane().add(printButton, BorderLayout.NORTH);
            scroll.setPreferredSize(new Dimension((int) pf.getImageableWidth(), (int) pf.getImageableHeight()));
            d.getContentPane().add(scroll);
            d.pack();
            final int height2 = (int) tPane.getPreferredScrollableViewportSize().getHeight();
            printButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent evento) {
                    PrintingThread.this.processor.print(pf, height2);
                }
            });
            d.setResizable(false);
            d.setVisible(true);
            d.toFront();
        }

    }

    protected List<Object> valueChangedListenerAttributes = null;

    protected boolean valueChangeListenerEnabled = true;

    public static final int UPDATE_ALL = 0;

    public static final int UPDATE_CHANGED = 1;

    protected List<Object> modifiedFieldAttributes = new ArrayList<>(3);

    protected int updateMethod = InteractionManager.UPDATE_CHANGED;

    /**
     * The form manager related to the InteractionManager
     */
    public IFormManager formManager = null;

    /**
     * The form managed by this {@link InteractionManager}
     */
    public Form managedForm = null;

    /**
     * The form current mode. Values can be: <br/>
     * <ul>
     * <li>{@link #QUERYINSERT}</li>
     * <li>{@link #QUERY}</li>
     * <li>{@link #INSERT}</li>
     * <li>{@link #UPDATE}</li>
     * </ul>
     */
    public int currentMode = InteractionManager.QUERYINSERT;

    protected com.ontimize.gui.InteractionManagerAction actionHandler = null;

    protected Thread printThread = null;

    protected DeleteFieldsListener deleteFieldsListener = new DeleteFieldsListener();

    /**
     * Class that implements the lister to the button 'delete fields' which set a initial mode to the
     * form (QueryInsert mode {@link InteractionManager#setQueryInsertMode()}) and delete the contents
     * of all the fields in the form. The fields are also disabled and in case the form has an
     * associated tree, the right node (organizational) is selected.
     */
    protected class DeleteFieldsListener implements ActionListener {

        public DeleteFieldsListener() {
            super();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            InteractionManager.logger.debug("DeleteFields Button ActionEvent");
            TreePath associatedPath = InteractionManager.this.managedForm.getAssociatedTreePath();
            if ((associatedPath != null) && (InteractionManager.this.formManager instanceof ITreeFormManager)) {
                ITreeFormManager treeFormManager = (ITreeFormManager) InteractionManager.this.formManager;
                Tree tree = treeFormManager.getTree();
                OTreeNode oAssociatedNode = (OTreeNode) associatedPath.getLastPathComponent();
                if ((oAssociatedNode != null) && (tree != null)) {
                    if (oAssociatedNode.isOrganizational()) {
                        // If it is organizational, we select.
                        InteractionManager.logger
                            .debug("Process Tree Selection Event set to false in DeleteFieldListener");
                        treeFormManager.setProcessingTreeSelectionEvent(false);
                        try {
                            tree.setSelectionPath(associatedPath);
                            tree.scrollPathToVisible(associatedPath);
                        } catch (Exception ex) {
                            InteractionManager.logger.error("Error in inserted node", ex);
                        } finally {
                            InteractionManager.logger
                                .debug("Process Tree Selection Event set to true in DeleteFieldListener");
                            treeFormManager.setProcessingTreeSelectionEvent(true);
                        }
                    } else {
                        InteractionManager.logger
                            .debug("Process Tree Selection Event set to false in DeleteFieldListener");
                        // If it is a data node, we select the parent
                        treeFormManager.setProcessingTreeSelectionEvent(false);
                        try {
                            tree.setSelectionPath(associatedPath.getParentPath());
                            treeFormManager
                                .updateParentkeys((OTreeNode) associatedPath.getParentPath().getLastPathComponent());
                            tree.scrollPathToVisible(associatedPath.getParentPath());
                        } catch (Exception ex) {
                            InteractionManager.logger.error("Error in inserted node", ex);
                        } finally {
                            InteractionManager.logger
                                .debug("Process Tree Selection Event set to true in DeleteFieldListener");
                            treeFormManager.setProcessingTreeSelectionEvent(true);
                        }
                    }
                }
            }
            InteractionManager.this.setQueryInsertMode();
        }

    }

    /**
     * Constructs an InteractionManager
     */
    public InteractionManager() {
        this.setUpdateMethodVersion(InteractionManager.UPDATE_CHANGED);
    }

    /**
     * Sets the update method. This can be one of the following:
     * <ul>
     * <li>UPDATE_ALL: All the fields are sent to the server in the update
     * <li>UPDATE_CHANGED: The default mode. Only modified fields are sent to the server in the update.
     * </ul>
     * @param method - the update method to set
     */
    protected void setUpdateMethodVersion(int method) {
        this.updateMethod = method;
    }

    /**
     * Establishes the initial state of the managed form, and sets the form states in QueryInsert mode
     * that implies that by default the query button and the insert button will be enables. Other
     * buttons of the form will not be enabled by default, so they have to be activated overwriting this
     * method.
     */
    public void setInitialState() {
        if (this.managedForm != null) {
            this.setQueryInsertMode();
            if (this.managedForm.printButton != null) {
                this.managedForm.printButton.setEnabled(false);
            }
        } else {
            InteractionManager.logger.debug("Managed Form in Interaction Manager is NULL");
        }
    }

    /**
     * Registers the InteractionManager as form event listener. This method is called automatically from
     * the {@link IFormManager} when a Form is loaded. If new listeners wants to be added to some
     * objects of the managed form, this must be done by overwriting this method.
     * <p>
     * In this basic implementation of the method, only the form and the form manager are assigned and
     * stored by the InteractionManager in order to have references to these classes later. A basic way
     * of overwriting this must call the <b>super.registerInteractionManager(form, FormsManager)</b>
     * <p>
     * This method links a key stroke to the standard buttons:
     *
     * <ul>
     * <li>F7 query
     * <li>F9 insert
     * <li>F11 update
     * <li>F12 delete
     * </ul>
     * @param form - the form to be associated with this manager
     * @param formManager - the form manager that rules the association
     */

    public void registerInteractionManager(Form form, IFormManager formManager) {
        this.managedForm = form;
        this.formManager = formManager;
        this.injectAnnotatedFields();
        this.f7Button = this.managedForm.getQueryButtonKey();
        this.f9Button = this.managedForm.getInsertButtonKey();
        this.f11Button = this.managedForm.getUpdateButtonKey();
        this.f12Button = this.managedForm.getDeleteButtonKey();

        this.registerFormKeyBindings();

        this.valueChangedListenerAttributes = new ArrayList<>(this.managedForm.getDataFieldAttributeList());
        this.managedForm.clearDataFieldButton.addActionListener(this.deleteFieldsListener);
        // Registers navigation listeners
        this.managedForm.addDataNavigationListener(this);
        // Registers change listeners
        List<?> vComponents = this.managedForm.componentList;
        for (int i = 0; i < vComponents.size(); i++) {
            Object c = vComponents.get(i);
            if ((c != null) && (c instanceof ValueChangeDataComponent)) {
                ((ValueChangeDataComponent) c).addValueChangeListener(this);
                ((ValueChangeDataComponent) c).addValueChangeListener(formManager.getRuleEngine());
            }
            if ((c != null) && (c instanceof InteractionManagerModeListener)) {
                this.addInteractionManagerModeListener((InteractionManagerModeListener) c);
            }
            // since 5.2075EN
            this.registerRuleEngineFor(c);
        }

        if (this.actionHandler != null) {
            com.ontimize.gui.InteractionManagerAction.Listener l = this.actionHandler.getListener();
            if (l != null) {
                l.setListener(this.managedForm);
            }
        }
    }

    protected void injectAnnotatedFields() {
        Class<?> cl = this.getClass();
        do {
            Field[] declaredFields = cl.getDeclaredFields();
            for (Field f : declaredFields) {
                if (f.isAnnotationPresent(FormComponent.class)) {
                    FormComponent annotation = f.getAnnotation(FormComponent.class);
                    String attr = annotation.attr();
                    Object ob = this.managedForm.getElementReference(attr);
                    if (ob == null) {
                        ob = this.managedForm.getButton(attr);
                    }
                    if (ob != null) {
                        f.setAccessible(true);
                        try {
                            f.set(this, ob);
                        } catch (Exception e) {
                            InteractionManager.logger.error("Error injecting {}", attr, e);
                        }
                    } else {
                        InteractionManager.logger.warn("Annotation FormComponent: '{}' element not found", attr);
                    }
                }
            }
        } while ((cl = cl.getSuperclass()) != null);
    }

    protected void registerRuleEngineFor(Object component) {
        if ((component != null) && (component instanceof ValueChangeDataComponent)) {
            ((ValueChangeDataComponent) component).addValueChangeListener(this.formManager.getRuleEngine());
        }
        if ((component != null) && (component instanceof Table)) {
            ((Table) component).addListSelectionListener(this.formManager.getRuleEngine());
        }
        if ((component != null) && (component instanceof AbstractButton)) {
            ((AbstractButton) component).addActionListener(this.formManager.getRuleEngine());
        }
    }

    /**
     * Sets the mode of the form in QueryInsert. This implies to delete the field values and to disable
     * all the form buttons but the insert and query ones.
     */
    public void setQueryInsertMode() {
        this.currentMode = InteractionManager.QUERYINSERT;
        this.managedForm.updateDataFieldsEDTh(new HashMap<>());

        // Disable fields
        this.managedForm.clearDataFieldButton.setEnabled(true);
        if (this.managedForm.attachmentButton != null) {
            this.managedForm.attachmentButton.setEnabled(false);
        }
        if (this.managedForm.printTemplateButton != null) {
            this.managedForm.printTemplateButton.setEnabled(false);
        }
        this.managedForm.disableButtons();
        this.managedForm.disableDataFields();
        this.managedForm.enableButton(InteractionManager.QUERY_KEY);
        this.managedForm.enableButton(InteractionManager.ADVANCED_QUERY_KEY);
        Button bAdvancedButton = this.managedForm.getButton(InteractionManager.ADVANCED_QUERY_KEY);
        if (bAdvancedButton != null) {
            bAdvancedButton.setAltMode(false);
        }
        this.managedForm.enableButton(InteractionManager.INSERT_KEY);
        Button b = this.managedForm.getButton(InteractionManager.QUERY_KEY);
        if (b != null) {
            b.setAltMode(false);
        }
        b = this.managedForm.getButton(InteractionManager.INSERT_KEY);
        if (b != null) {
            b.setAltMode(false);
        }
        this.managedForm.enableButton(InteractionManager.HELP_KEY);
        this.managedForm.disableHyperlinkComponents();
        Button[] buttons = this.managedForm.getButtons();
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] instanceof RefreshTableButton) {
                buttons[i].setEnabled(true);
            } else if (buttons[i] instanceof HelpButton) {
                buttons[i].setEnabled(true);
            }
        }
        if (this.managedForm.printButton != null) {
            this.managedForm.printButton.setEnabled(false);
        }
        this.modifiedFieldAttributes.clear();
        this.fireInteractionManagerModeChanged(new InteractionManagerModeEvent(this, InteractionManager.QUERYINSERT));

        if (this.actionHandler != null) {
            this.actionHandler.setMode(this.managedForm, InteractionManager.QUERY_INSERT_KEY);
        }
    }

    /**
     * Sets the mode of the form in Insert. This implies to disable all the form buttons but the insert
     * one, and to disable the tables too.
     */
    public void setInsertMode() {
        this.currentMode = InteractionManager.INSERT;
        this.managedForm.clearDataFieldButton.setEnabled(true);
        if (this.managedForm.attachmentButton != null) {
            this.managedForm.attachmentButton.setEnabled(false);
        }
        if (this.managedForm.printTemplateButton != null) {
            this.managedForm.printTemplateButton.setEnabled(false);
        }

        this.managedForm.disableButtons();
        this.managedForm.setDefaultValues();
        this.managedForm.enableDataFields(false);
        this.managedForm.disableTables();
        this.managedForm.disableSubForms();
        this.managedForm.enableButton(InteractionManager.INSERT_KEY);
        Button b = this.managedForm.getButton(InteractionManager.INSERT_KEY);
        if (b != null) {
            b.setAltMode(true);
        }
        this.managedForm.enableButton(InteractionManager.HELP_KEY);
        this.managedForm.disableHyperlinkComponents();

        if (InteractionManager.NEWMODE) {
            this.managedForm.enableButton(InteractionManager.QUERY_KEY);
        }

        if (this.managedForm.printButton != null) {
            this.managedForm.printButton.setEnabled(false);
        }
        // Put focus in first form component
        this.modifiedFieldAttributes.clear();
        if (this.setDefaultFocusEnabled) {
            this.managedForm.requestFocusForFirstComponent();
        }
        this.fireInteractionManagerModeChanged(new InteractionManagerModeEvent(this, InteractionManager.INSERT));

        if (this.actionHandler != null) {
            this.actionHandler.setMode(this.managedForm, InteractionManager.INSERT_KEY);
        }
    }

    /**
     * Sets the mode of the form in Update. The fields are enabled and allows modifications. The update
     * button is enabled.
     */
    public void setUpdateMode() {
        this.currentMode = InteractionManager.UPDATE;
        this.managedForm.clearDataFieldButton.setEnabled(true);
        if (this.managedForm.attachmentButton != null) {
            this.managedForm.attachmentButton.setEnabled(true);
        }
        if (this.managedForm.printTemplateButton != null) {
            this.managedForm.printTemplateButton.setEnabled(true);
        }

        this.managedForm.disableButtons();
        this.managedForm.enableDataFields(false);
        Button advanceButton = this.managedForm.getButton(InteractionManager.ADVANCED_QUERY_KEY);
        if (advanceButton != null) {
            advanceButton.setAltMode(false);
        }

        Button b = this.managedForm.getButton(InteractionManager.QUERY_KEY);
        if (b != null) {
            b.setAltMode(false);
            if (InteractionManager.NEWMODE) {
                b.setEnabled(true);
            }
        }

        b = this.managedForm.getButton(InteractionManager.INSERT_KEY);
        if (b != null) {
            b.setAltMode(false);
            if (InteractionManager.NEWMODE) {
                b.setEnabled(true);
            }
        }

        this.managedForm.enableButton(InteractionManager.DELETE_KEY);
        this.managedForm.enableHyperlinkComponents();
        Button[] buttons = this.managedForm.getButtons();
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] instanceof RefreshTableButton) {
                buttons[i].setEnabled(true);
            } else if (buttons[i] instanceof HelpButton) {
                buttons[i].setEnabled(true);
            }
        }
        if (this.managedForm.printButton != null) {
            this.managedForm.printButton.setEnabled(true);
        }
        // We do not put focus to the first form data component
        this.modifiedFieldAttributes.clear();
        if (this.setDefaultFocusEnabled) {
            this.managedForm.requestFocusForFirstComponent();
        }
        this.fireInteractionManagerModeChanged(new InteractionManagerModeEvent(this, InteractionManager.UPDATE));

        if (this.actionHandler != null) {
            this.actionHandler.setMode(this.managedForm, InteractionManager.UPDATE_KEY);
        }
    }

    /**
     * Sets the mode of the form in Query. This implies to disable all the form buttons but the insert
     * one, and to disable the tables too.
     */
    public void setQueryMode() {
        this.currentMode = InteractionManager.QUERY;
        this.managedForm.clearDataFieldButton.setEnabled(true);
        if (this.managedForm.attachmentButton != null) {
            this.managedForm.attachmentButton.setEnabled(false);
        }
        if (this.managedForm.printTemplateButton != null) {
            this.managedForm.printTemplateButton.setEnabled(false);
        }

        this.managedForm.disableButtons();
        this.managedForm.enableDataFields(false);
        this.managedForm.disableTables();
        this.managedForm.enableButton(InteractionManager.ADVANCED_QUERY_KEY);
        Button ba = this.managedForm.getButton(InteractionManager.ADVANCED_QUERY_KEY);
        if (ba != null) {
            ba.setAltMode(false);
        }

        this.managedForm.enableButton(InteractionManager.QUERY_KEY);
        Button b = this.managedForm.getButton(InteractionManager.QUERY_KEY);
        if (b != null) {
            b.setAltMode(true);
        }
        this.managedForm.enableButton(InteractionManager.HELP_KEY);
        this.managedForm.disableHyperlinkComponents();

        if (InteractionManager.NEWMODE) {
            this.managedForm.enableButton(InteractionManager.INSERT_KEY);
        }

        if (this.managedForm.printButton != null) {
            this.managedForm.printButton.setEnabled(false);
        }
        // Put focus in first form component
        this.modifiedFieldAttributes.clear();
        if (this.setDefaultFocusEnabled) {
            this.managedForm.requestFocusForFirstComponent();
        }
        this.fireInteractionManagerModeChanged(new InteractionManagerModeEvent(this, InteractionManager.QUERY));

        if (this.actionHandler != null) {
            this.actionHandler.setMode(this.managedForm, InteractionManager.QUERY_KEY);
        }
    }

    /**
     * Method that responds to the print button keystroke.
     * @throws Exception
     */
    public void print() throws Exception {

        if ((this.printThread != null) && this.printThread.isAlive()) {
            this.managedForm.message("interactionmanager.wait_until_current_printing_task_finishes",
                    Form.WARNING_MESSAGE);
            return;
        } else {
            // We get form template and print data in template
            if ((this.managedForm.printingTemplateName == null)
                    || (this.managedForm.printingTemplateName.length() == 0)) {
                // Prints form
                this.managedForm.printingFormWithDefaultTemplate();
                return;
            } else {
                // Loads the template
                URL templateURL = this.getClass().getResource("templates/" + this.managedForm.printingTemplateName);
                if (templateURL == null) {
                    this.managedForm.message("interactionmanager.no_printing_templates_be_found",
                            Form.INFORMATION_MESSAGE);
                    return;
                } else {
                    // Uses html processor
                    final HTMLProcessor processor = new HTMLProcessor(templateURL);

                    // For each data component, we replace it
                    Iterator<?> enumKeys = this.managedForm.dataComponentList.keySet().iterator();
                    while (enumKeys.hasNext()) {
                        Object oKey = enumKeys.next();
                        if (!(oKey instanceof Map)) {
                            // If key is a hashtable then it is a table and the
                            // tables have a button to print the data
                            Object oValue = this.managedForm.getDataFieldValue(oKey.toString());
                            if (oValue instanceof BytesBlock) {
                                processor.replaceTextInHTML(this.managedForm.getEntityName() + "." + oKey.toString(),
                                        ((BytesBlock) oValue).getBytes(), 100, 100);
                            } else {
                                String etiquetaComponente = this.managedForm.getDataFieldReference(oKey.toString())
                                    .getLabelComponentText();
                                String textoComponente = this.managedForm.getDataFieldText(oKey.toString());
                                // Changes some haracters
                                StringBuilder sbHTMLKey = new StringBuilder(oKey.toString());
                                for (int i = 0; i < sbHTMLKey.length(); i++) {
                                    char c = sbHTMLKey.charAt(i);
                                    if (c == 'á') {
                                        sbHTMLKey.replace(i, i + 1, "&#225;");
                                        continue;
                                    }
                                    if (c == 'é') {
                                        sbHTMLKey.replace(i, i + 1, "&#233;");
                                    }
                                    if (c == 'í') {
                                        sbHTMLKey.replace(i, i + 1, "&#237;");
                                    }
                                    if (c == 'ó') {
                                        sbHTMLKey.replace(i, i + 1, "&#243;");
                                    }
                                    if (c == 'ú') {
                                        sbHTMLKey.replace(i, i + 1, "&#250;");
                                    }
                                }
                                processor.replaceTextInHTML(
                                        this.managedForm.getEntityName() + "." + sbHTMLKey.toString(),
                                        "<B>" + etiquetaComponente + "</B>" + "&nbsp;&nbsp;&nbsp;" + textoComponente);
                            }
                        }
                    }

                    InteractionManager.logger.debug(processor.getHTML());
                    this.managedForm.message("interactionmanager.document_be_printing_is_being_generated",
                            Form.INFORMATION_MESSAGE);
                    this.printThread = new PrintingThread(processor);
                }
            }
            this.printThread.start();
        }
    }

    /**
     * Clones this InteractionManager. It is used in forms that are opened from tables. This basic
     * implementation creates the InteractionManager with Class.forName() and a no parameter
     * constructor.
     * @return
     */
    public InteractionManager cloneInteractionManager() {
        try {
            return this.getClass().newInstance();
        } catch (Exception e) {
            InteractionManager.logger.trace(null, e);
            return new InteractionManager();
        }
    }

    /**
     * Removes all the references that this InteractionManager has in order to free the memory.
     */
    @Override
    public void free() {
        try {
            this.interactionManagerModeListenerList.clear();
            this.managedForm.clearDataFieldButton.removeActionListener(this.deleteFieldsListener);
            this.formManager = null;
            this.managedForm = null;
            InteractionManager.logger.debug("Free references in InteractionManager.");
        } catch (Exception e) {
            InteractionManager.logger.error("Error freeing InteractionManager", e);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        InteractionManager.logger.debug("Invoke finalized method.");
        this.managedForm = null;
        this.formManager = null;
        super.finalize();
    }

    /**
     * Checks whether the verification of modified data is enabled or not.
     * @return
     */
    public boolean getCheckModifiedDataChangeEvent() {
        return this.checkModifiedDataChangeEvent;
    }

    /**
     * Enables or disables the verification of modified data.
     * @param check
     */
    public void setCheckModifiedDataChangeEvent(boolean check) {
        this.checkModifiedDataChangeEvent = check;
    }

    /**
     * Returns a Vector with the atributes of the fields that have been modified.
     * @return - the vector with the modified field attributes.
     */
    public List<Object> getModifiedFieldAttributes() {
        return this.modifiedFieldAttributes;
    }

    /**
     * Invoked when a value in some of the form elements has changed. If the source of the event
     * implements the interface IdentifyElement the element's attribute is added to the modified field
     * attributes (see getModifiedFieldAttributes())
     */
    @Override
    public void valueChanged(ValueEvent e) {
        if ((this.currentMode == InteractionManager.UPDATE) && this.isValueChangeListenerEnabled()) {
            Object oSource = e.getSource();
            if ((oSource != null) && (oSource instanceof IdentifiedElement)) {
                if (this.valueChangedListenerAttributes.contains(((IdentifiedElement) oSource).getAttribute())) {
                    if ((e.getOldValue() == e.getNewValue()) || ((e.getOldValue() != null) && (e.getNewValue() != null)
                            && e.getOldValue().equals(e.getNewValue()))) {
                        return;
                    } else {
                        InteractionManager.logger.debug("{}  Value Event: {}  new value: {}, old value: {}",
                                e.getType() == 1 ? "Programmatic" : "User",
                                ((IdentifiedElement) oSource).getAttribute(), e.getNewValue(), e.getOldValue());
                    }

                    if (!this.modifiedFieldAttributes.contains(((IdentifiedElement) oSource).getAttribute())) {
                        this.modifiedFieldAttributes.add(((IdentifiedElement) oSource).getAttribute());
                    }

                    InteractionManager.logger.debug("ValueChanged Event in a component with attribute: {}",
                            ((IdentifiedElement) oSource).getAttribute(), new Throwable());

                    Button updateButton = this.managedForm.getButton(InteractionManager.UPDATE_KEY);
                    if ((updateButton != null) && !updateButton.isEnabled()) {
                        updateButton.setEnabled(true);
                        if (InteractionManager.NEWMODE) {
                            this.managedForm.disableButton(InteractionManager.INSERT_KEY);
                            this.managedForm.disableButton(InteractionManager.QUERY_KEY);
                        }
                    }
                }
            }
        } else if ((this.currentMode == InteractionManager.INSERT) && this.isValueChangeListenerEnabled()) {
            Object oSource = e.getSource();
            if ((oSource != null) && (oSource instanceof IdentifiedElement)) {
                if (this.valueChangedListenerAttributes.contains(((IdentifiedElement) oSource).getAttribute())) {
                    if ((e.getOldValue() == e.getNewValue()) || ((e.getOldValue() != null) && (e.getNewValue() != null)
                            && e.getOldValue().equals(e.getNewValue()))) {
                        return;
                    } else {
                        InteractionManager.logger.debug("{}  Value Event: {}  new value: {}, old value: {}",
                                e.getType() == 1 ? "Programmatic" : "User",
                                ((IdentifiedElement) oSource).getAttribute(), e.getNewValue(), e.getOldValue());
                    }

                    if (!this.modifiedFieldAttributes.contains(((IdentifiedElement) oSource).getAttribute())) {
                        this.modifiedFieldAttributes.add(((IdentifiedElement) oSource).getAttribute());
                    }

                    InteractionManager.logger.debug("ValueChanged Event in a component with attribute: {}",
                            ((IdentifiedElement) oSource).getAttribute(), new Throwable());

                }
            }
        }
    }

    /**
     * Returns the actual mode of the form
     * @return - the actual mode
     */
    public int getCurrentMode() {
        return this.currentMode;
    }

    /**
     * Method invoked when the register shown in the form is going to change.
     */
    @Override
    public boolean dataWillChange(DataNavigationEvent e) {
        InteractionManager.logger.debug("The record shown in the form is going to change: {}", e);
        return true;
    }

    /**
     * Method invoked when the register shown in the form changes. This methods updates the tree
     * selection if the tree exists using the right methods from the Node.
     * <p>
     * If there's no match between the register in the form and the organizational node's child (this
     * could happen because the tree is not refreshed yet) the organizational node is selected. In this
     * case the update button is disabled and the list of modified fields is removed.
     *
     */
    @Override
    public void dataChanged(DataNavigationEvent e) {

        if (this.dataChangedEventProcessing && (this.managedForm != null)) {
            this.modifiedFieldAttributes.clear();

            this.managedForm.disableButton(InteractionManager.UPDATE_KEY);
            if (InteractionManager.NEWMODE && (this.currentMode == InteractionManager.UPDATE)) {
                this.managedForm.enableButton(InteractionManager.INSERT_KEY);
                this.managedForm.enableButton(InteractionManager.QUERY_KEY);
            }

            InteractionManager.logger.debug("InteractionManager: Data Changed Event : {}", e);
            TreePath associatedPath = this.managedForm.getAssociatedTreePath();
            if ((associatedPath != null) && (this.formManager instanceof ITreeFormManager)) {
                // Indicates that associated path is a tree
                ITreeFormManager treeFormManager = (ITreeFormManager) this.formManager;
                Tree tree = treeFormManager.getTree();
                OTreeNode associatedNode = (OTreeNode) associatedPath.getLastPathComponent();
                if ((associatedNode != null) && (tree != null)) {
                    OTreeNode oNode = associatedNode;
                    TreePath path = associatedPath;
                    if (!oNode.isOrganizational()) {
                        if (oNode.isRoot()) {
                            return;
                        }
                        oNode = (OTreeNode) associatedNode.getParent();
                        path = associatedPath.getParentPath();
                    }
                    List<?> vKeys = oNode.getKeys();
                    Map<?,?> hFormData = e.getData();

                    // Now, node is associated organizational node to the form.
                    // It
                    // searches in children.
                    for (int i = 0; i < oNode.getChildCount(); i++) {
                        boolean bKeysMatch = true;
                        OTreeNode n = (OTreeNode) oNode.getChildAt(i);
                        Map<?,?> hNodeData = n.getKeysValues();
                        for (int j = 0; j < vKeys.size(); j++) {
                            Object oFieldValue = hFormData.get(vKeys.get(j));
                            if (oFieldValue != null) {
                                if (oFieldValue instanceof SearchValue) {
                                    oFieldValue = ((SearchValue) oFieldValue).getValue();
                                }
                                if (!oFieldValue.equals(hNodeData.get(vKeys.get(j)))) {
                                    bKeysMatch = false;
                                    break;
                                }
                            } else {
                                bKeysMatch = false;
                                break;
                            }
                        }
                        if (bKeysMatch) {
                            InteractionManager.logger.debug("Process Tree Selection Event set to false in DataChanged");
                            treeFormManager.setProcessingTreeSelectionEvent(false);
                            try {
                                TreePath pSel = path.pathByAddingChild(n);
                                tree.setSelectionPath(pSel);
                                tree.scrollPathToVisible(pSel);
                            } catch (Exception ex) {
                                InteractionManager.logger.error("Data navigation error", ex);
                            } finally {
                                InteractionManager.logger
                                    .debug("Process Tree Selection Event set to true in DataChanged");
                                treeFormManager.setProcessingTreeSelectionEvent(true);
                            }
                            return;
                        }
                    }
                    // Selects the organizational node
                    try {
                        InteractionManager.logger.debug("Process Tree Selection Event set to false in DataChanged");
                        treeFormManager.setProcessingTreeSelectionEvent(false);
                        tree.setSelectionPath(path);
                        tree.scrollPathToVisible(path);
                    } catch (Exception ex) {
                        InteractionManager.logger.error("Error data navigation", ex);
                    } finally {
                        InteractionManager.logger.debug("Process Tree Selection Event set to true in DataChanged");
                        treeFormManager.setProcessingTreeSelectionEvent(true);
                    }
                }
            }
        }
    }

    /**
     * Enables or disables the execution of the method {@link #dataChanged(DataNavigationEvent)}.
     * @param enable
     */
    public void setDataChangedEventProcessing(boolean enable) {
        this.dataChangedEventProcessing = enable;
    }

    /**
     * Checks whether the execution of the method {@link #dataChanged(DataNavigationEvent)} is enabled
     * or not.
     * @return
     */
    public boolean getDataChangedEventProcessing() {
        return this.dataChangedEventProcessing;
    }

    /**
     * @deprecated
     * @param message
     */
    @Deprecated
    public void setWarningBirthdayMessage(String message) {
        InteractionManager.warningBirthdayMessage = message;
    }

    /**
     *
     * Checks that the field in the managed form identified by the attr Strings is a Date and, if so,
     * checks that the Date is before than the actual Date. In that case returns true. In other case
     * displays a question advicing the user that the Date is after the actual day, and asking whether
     * the execution must continue with this value.
     * @param attr = the field which has to be compared to
     * @return true if the execution can continue, false if it can't
     */
    public boolean checkBirthday(String attr) {
        DataComponent c = this.managedForm.getDataFieldReference(attr);
        if ((c == null) || (!(c instanceof DateDataField))) {
            return true;
        } else {
            if (c.isEmpty()) {
                return true;
            }
            Date d = (Date) ((DateDataField) c).getDateValue();
            if (d.getTime() < System.currentTimeMillis()) {
                return true;
            } else {
                return this.managedForm.question(InteractionManager.warningBirthdayMessage);
            }
        }
    }

    /**
     * Register elements as valuechanged listeners, so in the case that they are a valuechangeevent
     * source the InteractionManager will process that events.
     * @param attr the attribute of the element that wants to be registered. This object should be a
     *        String in the text fields or integer fields, but it can be a different object in other
     *        fields like reference fields
     * @param listen if the value is true, the element will be added, is false, the element will be
     *        removed
     */

    public void setValueChangedEventListener(Object attr, boolean listen) {
        if (listen) {
            if (!this.valueChangedListenerAttributes.contains(attr)) {
                this.valueChangedListenerAttributes.add(attr);
            }
        } else {
            this.valueChangedListenerAttributes.remove(attr);
        }
    }

    public void setValueChangeEventListenerEnabled(boolean enabled) {
        this.valueChangeListenerEnabled = enabled;
    }

    public boolean isValueChangeListenerEnabled() {
        return this.valueChangeListenerEnabled;
    }

    /**
     * This class implements the normal behavior buttons in the {@link InteractionManager} must have.
     * This behavior implies to request the focus prior performing the actions of the button event.
     */
    protected class InteractionManagerAction extends AbstractAction {

        protected Button button = null;

        public InteractionManagerAction(Button b) {
            this.button = b;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if ((this.button != null) && this.button.isVisible() && this.button.isEnabled()) {
                this.button.setRequestFocusEnabled(true);
                this.button.requestFocus();
                this.button.doClick();
            }
        }

    }

    /**
     * Called when creating the InteractionManager, this method registers all the keystrokes for the
     * form. The default keystrokes provide fast access to the main form buttons:
     *
     * <ul>
     * <li>query - F7</li>
     * <li>insert - F9</li>
     * <li>update - F11</li>
     * <li>delete - F12</li>
     * </ul>
     */
    protected void registerFormKeyBindings() {
        InputMap inMap = this.managedForm.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actMap = this.managedForm.getActionMap();
        Button bQuery = this.managedForm.getButton(this.f7Button);
        if (bQuery != null) {
            KeyStroke ksQuery = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0, true);
            inMap.put(ksQuery, InteractionManager.QUERY_KEY);
            bQuery.setKeyStrokeText("F7");
            actMap.put(InteractionManager.QUERY_KEY,
                    new InteractionManagerAction(this.managedForm.getButton(this.f7Button)));
        }

        Button bI = this.managedForm.getButton(this.f9Button);
        if (bI != null) {
            KeyStroke ksInsert = KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0, true);
            inMap.put(ksInsert, InteractionManager.INSERT_KEY);
            bI.setKeyStrokeText("F9");
            actMap.put(InteractionManager.INSERT_KEY,
                    new InteractionManagerAction(this.managedForm.getButton(this.f9Button)));
        }

        Button bUpdate = this.managedForm.getButton(this.f11Button);
        if (bUpdate != null) {
            KeyStroke ksUpdate = KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0, true);
            inMap.put(ksUpdate, InteractionManager.UPDATE_KEY);
            bUpdate.setKeyStrokeText("F11");
            actMap.put(InteractionManager.UPDATE_KEY,
                    new InteractionManagerAction(this.managedForm.getButton(this.f11Button)));
        }

        Button bDelete = this.managedForm.getButton(this.f12Button);
        if (bDelete != null) {
            KeyStroke ksDelete = KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0, true);
            inMap.put(ksDelete, InteractionManager.DELETE_KEY);
            bDelete.setKeyStrokeText("F12");
            actMap.put(InteractionManager.DELETE_KEY,
                    new InteractionManagerAction(this.managedForm.getButton(this.f12Button)));
        }
    }

    /**
     * Enables or not the focus strategy in which the focus is set to the first form component.
     * @param enable
     */
    public void setDefaultFocusEnabled(boolean enable) {
        this.setDefaultFocusEnabled = enable;
    }

    /**
     * Returns the current focus configuration. If true, the focus will be set to the first form
     * component.
     * @return - true if the default active focus is set false in other case
     */
    public boolean getDefaultActiveFocus() {
        return this.setDefaultFocusEnabled;
    }

    // Template methods to manage data used to fill/create template

    /**
     * Gets tables included in form that are inserted when template is created.
     * @param form Form that is source to create template
     * @return <code>Map</code> with tables included in template
     */
    public Map<Object, Object> getTemplateTables(Form form) {
        return TemplateUtils.getTemplateTables(form);
    }

    /**
     * Gets fields included in form that are inserted when template is created.
     * @param form Form that is source to create template
     * @return <code>Map</code> with fields included in template
     */
    public Map<Object, Object> getTemplateFields(Form form) {
        return TemplateUtils.getTemplateFields(form);
    }

    /**
     * Gets images included in form that are inserted when template is created.
     * @param form Form that is source to create template
     * @return <code>Map</code> with tables included in template
     */
    public Map<Object, Object> getTemplateImages(Form form) {
        return TemplateUtils.getTemplateImages(form);
    }

    /**
     * Gets field values included in form that are inserted when template is filled.
     * @param form Form that is source to fill template
     * @return <code>Map</code> with field values included in template
     */
    public Map<Object, Object> getFieldValues(Form form) {
        return TemplateUtils.getFieldsValues(form);
    }

    /**
     * Gets table values included in form that are inserted when template is filled.
     * @param form Form that is source to fill template
     * @return <code>Map</code> with table values included in template
     */
    public Map<Object, Object> getTableValues(Form form) {
        return TemplateUtils.getTablesValues(form);
    }

    /**
     * Gets image values included in form that are inserted when template is filled.
     * @param form Form that is source to fill template
     * @return <code>Map</code> with image values included in template
     */
    public Map<Object, Object> getImageValues(Form form, boolean insertEmptyImages) {
        return TemplateUtils.getImagesValues(form, insertEmptyImages);
    }

    /**
     * Register a listener that will be notified of the changes of mode in the Interaction Manager.
     * @param listener the listener to add
     */
    public void addInteractionManagerModeListener(InteractionManagerModeListener listener) {
        if (this.interactionManagerModeListenerList == null) {
            this.interactionManagerModeListenerList = new ArrayList<InteractionManagerModeListener>();
        }
        if (!this.interactionManagerModeListenerList.contains(listener)) {
            this.interactionManagerModeListenerList.add(listener);
        }
    }

    /**
     * Removes a listener from the ones sets to listen to the InteractionManager mode changes.
     * @param listener the listener to be removed
     */
    public void removeInteractionManagerModeListener(InteractionManagerModeListener listener) {
        if (this.interactionManagerModeListenerList == null) {
            return;
        }
        if (this.interactionManagerModeListenerList.contains(listener)) {
            this.interactionManagerModeListenerList.remove(listener);
        }
    }

    /**
     * Notifies all the registered listeners that the form mode has changed.
     * @param event
     */
    protected void fireInteractionManagerModeChanged(InteractionManagerModeEvent event) {
        if (this.interactionManagerModeListenerList != null) {
            for (int i = this.interactionManagerModeListenerList.size() - 1; i >= 0; i--) {
                this.interactionManagerModeListenerList.get(i).interactionManagerModeChanged(event);
            }
        }
    }

    public com.ontimize.gui.InteractionManagerAction getActionHandler() {
        return this.actionHandler;
    }

    public void loadActionHandler(String resource) {
        InteractionManagerActionBuilder imaBuilder = new XMLInteractionManagerActionBuilder();
        com.ontimize.gui.InteractionManagerAction action = imaBuilder.buildAction(resource);
        this.actionHandler = action;
    }

}
