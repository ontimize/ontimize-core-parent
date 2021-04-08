package com.ontimize.gui;

import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.Box.Filler;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.field.FormComponent;
import com.ontimize.gui.field.IdentifiedElement;
import com.ontimize.security.ClientSecurityManager;
import com.ontimize.security.MenuPermission;

public class ApToolBarFiller extends Filler implements FormComponent, IdentifiedElement, SecureElement, Freeable,
        Transferable, DragGestureListener, DragSourceListener {

    private static final Logger logger = LoggerFactory.getLogger(ApToolBarFiller.class);

    protected String attribute;

    private MenuPermission visiblePermission = null;

    protected DragSource source;

    public ApToolBarFiller(Hashtable parameter) {
        super(new Dimension(0, 0), new Dimension(0, 0), new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        this.init(parameter);
        this.source = new DragSource();
        this.source.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    ((JComponent) e.getSource()).getParent().dispatchEvent(e);
                }
            }
        });
    }

    @Override
    public Object getConstraints(LayoutManager parentLayout) {
        return null;
    }

    /**
     * Initialize the component with the following parameters:<br>
     * - 'attr': Attribute used to identify the component. Can be null<br>
     * @param parameters Parameter to initialize the component
     */
    @Override
    public void init(Hashtable parameters) {

        Object attr = parameters.get("attr");
        if (attr != null) {
            this.attribute = attr.toString();
        } else {
            this.attribute = "aptoolbarfiller";
        }

    }

    @Override
    public Vector getTextsToTranslate() {
        Vector v = new Vector(0);
        return v;
    }

    @Override
    public void setComponentLocale(Locale l) {

    }

    @Override
    public void setResourceBundle(ResourceBundle resoruces) {

    }

    @Override
    public Object getAttribute() {
        return this.attribute;
    }

    @Override
    public void initPermissions() {
        if (ApplicationManager.getClientSecurityManager() != null) {
            ClientSecurityManager.registerSecuredElement(this);
        }
        if (ApplicationManager.CHECK_VISIBLE_PERMISSION_ON_TOOLBAR_COMPONENTS) {
            ClientSecurityManager manager = ApplicationManager.getClientSecurityManager();
            if (this.visiblePermission == null) {
                this.visiblePermission = new MenuPermission("visible", this.attribute, true);
            }
            try {
                manager.checkPermission(this.visiblePermission);
            } catch (Exception e) {
                super.setVisible(false);
                if (ApplicationManager.DEBUG_SECURITY) {
                    ApToolBarFiller.logger.debug(this.getClass().toString() + ": " + e.getMessage(), e);
                }
            }
        }
    }

    protected boolean restricted = false;

    @Override
    public boolean isRestricted() {
        return this.restricted;
    }

    @Override
    public void free() {
        // TODO Auto-generated method stub

    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        try {
            return new DataFlavor[] { new DataFlavor(
                    DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + ApToolBarFiller.class.getName() + "\"") };
        } catch (ClassNotFoundException e) {
            logger.error(null, e);
        }
        return null;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return true;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        return this;
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
        this.source.startDrag(dge, DragSource.DefaultMoveDrop, this, this);
    }

    @Override
    public void dragEnter(DragSourceDragEvent dsde) {
    }

    @Override
    public void dragOver(DragSourceDragEvent dsde) {
    }

    @Override
    public void dropActionChanged(DragSourceDragEvent dsde) {
    }

    @Override
    public void dragExit(DragSourceEvent dse) {
    }

    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {
    }

}
