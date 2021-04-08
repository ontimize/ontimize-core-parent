package com.ontimize.gui.field;

import java.util.Vector;

import javax.swing.DefaultComboBoxModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomComboBoxModel extends DefaultComboBoxModel {

    private static final Logger logger = LoggerFactory.getLogger(CustomComboBoxModel.class);

    public static final String NULL_SELECTION = "NULL_SELECTION";

    private boolean disableEvents = false;

    protected boolean nullSelection = true;

    public boolean isNullSelection() {
        return this.nullSelection;
    }

    public void setNullSelection(boolean nullSelection) {
        this.nullSelection = nullSelection;
        this.checkNullSelection();
    }

    public CustomComboBoxModel() {
        this(true);
    }

    public CustomComboBoxModel(boolean nullSelection) {
        super();
        this.setNullSelection(nullSelection);
        this.checkNullSelection();
        super.setSelectedItem(null);
    }

    public CustomComboBoxModel(Vector v) {
        this(v, true);
    }

    public CustomComboBoxModel(Vector v, boolean nullSelection) {
        super(v);
        this.setNullSelection(true);
        this.checkNullSelection();
        super.setSelectedItem(null);
    }

    @Override
    public void removeAllElements() {
        super.removeAllElements();
        this.checkNullSelection();
    }

    @Override
    public void removeElement(Object ob) {
        super.removeElement(ob);
        this.checkNullSelection();
    }

    @Override
    public void removeElementAt(int index) {
        super.removeElementAt(index);
        this.checkNullSelection();
    }

    public void setDataVector(Vector v) {
        try {
            this.disableEvents = true;
            super.removeAllElements();
            this.checkNullSelection();
            if (!((v == null) || v.isEmpty())) {
                for (int i = 0; i < v.size(); i++) {
                    super.insertElementAt(v.get(i), super.getSize());
                }
            }
        } catch (Exception e) {
            CustomComboBoxModel.logger.error(null, e);
        } finally {
            this.disableEvents = false;
        }
        this.fireContentsChanged(this, 0, (v == null) || v.isEmpty() ? 0 : v.size() - 1);
    }

    @Override
    protected void fireIntervalRemoved(Object source, int index0, int index1) {
        if (!this.disableEvents) {
            super.fireIntervalRemoved(source, index0, index1);
        }
    }

    @Override
    protected void fireIntervalAdded(Object source, int index0, int index1) {
        if (!this.disableEvents) {
            super.fireIntervalAdded(source, index0, index1);
        }
    }

    protected void checkNullSelection() {
        int index = this.getIndexOf(CustomComboBoxModel.NULL_SELECTION);
        if (this.isNullSelection()) {
            if (index < 0) {
                // Null selection does not exist then add it
                this.insertElementAt(CustomComboBoxModel.NULL_SELECTION, 0);
            }
        } else {
            if (index >= 0) {
                this.removeElementAt(index);
            }
        }
    }

}
