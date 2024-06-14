package com.ontimize.gui;

import java.awt.BorderLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.button.Button;
import com.ontimize.gui.manager.IFormManager;
import com.ontimize.gui.manager.ITabbedFormManager;
import com.ontimize.gui.table.Table;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.util.FormatPattern;

public class TabbedDetailForm extends BaseDetailForm {

    private static final String DOTS = "...";

    private static final Logger logger = LoggerFactory.getLogger(TabbedDetailForm.class);

    public TabbedDetailForm(Form form, Map<Object, Object> tableKeys, List<String> keyFields, Table sourceTable, Map<Object, Object> parentkeys,
    		Map<String, String> codValues) {
        this.form = form;
        this.setLayout(new BorderLayout());
        this.add(form, BorderLayout.CENTER);
        this.initCodValues(codValues);
        this.tableKeys = new EntityResultMapImpl(new HashMap<>(this.valuesToForm(tableKeys)));
        this.table = sourceTable;
        this.parentkeys = this.valuesToForm(parentkeys);

        this.form.setDetailForm(this);
        this.form.disableDataFields();
        this.fieldsKey = this.listToForm(keyFields);

        if (form.getInteractionManager() instanceof BasicInteractionManager) {
            ((BasicInteractionManager) form.getInteractionManager()).setDetailForm(true);
        }

        Iterator<?> c = this.parentkeys.keySet().iterator();
        while (c.hasNext()) {
            form.setModifiable(c.next().toString(), false);
        }
        this.vectorIndex = 0;
    }

    public String getTitle() {
        int currentMode = this.form.getInteractionManager().getCurrentMode();
        String title = null;

        if (InteractionManager.INSERT == currentMode) {
            title = ApplicationManager.getTranslation(this.table.getInsertTitleKey(), this.form.getResourceBundle());
        }

        if (title == null) {
            if (this.table.getDetailFormatPattern() != null) {
                FormatPattern formatPattern = this.table.getDetailFormatPattern();
                title = formatPattern.parse(0, this.tableKeys);
            } else {
                StringBuilder buffer = new StringBuilder();
                List<String> keys = this.form.getKeys();
                for (Object current : keys) {
                    Object value = this.form.getDataFieldValue(current.toString());
                    if (buffer.length() > 0) {
                        buffer.append(" ");
                    }
                    buffer.append(value.toString());
                }
                title = buffer.toString();
            }
        }

        int detailTitleMaxSize = this.table.getDetailTitleMaxSize();

        if ((detailTitleMaxSize > 0) && (title != null) && (title.length() > detailTitleMaxSize)) {

            title = title.substring(0, detailTitleMaxSize) + TabbedDetailForm.DOTS;
        }

        return title;
    }

    public void updateTitle() {
        IFormManager formManager = this.form.getFormManager();
        if (formManager instanceof ITabbedFormManager) {
            ITabbedFormManager tabbedFormManager = (ITabbedFormManager) formManager;
            int index = tabbedFormManager.indexOfComponent(this);
            if (index > 0) {
                String descriptionText = this.getTitle();
                tabbedFormManager.setTitleAt(index, descriptionText);
            }
        }
    }

    @Override
    public void showDetailForm() {
        IFormManager formManager = this.form.getFormManager();

        if (formManager instanceof ITabbedFormManager) {
            ITabbedFormManager tabbedFormManager = (ITabbedFormManager) formManager;
            int index = tabbedFormManager.indexOfKeys(this.tableKeys.getRecordValues(0));

            if (index > 0) {
                // If tab already exits.
                tabbedFormManager.showTab(index);
                return;
            }
            String descriptionText = this.getTitle();
            formManager.addFormToContainer(this, descriptionText);
        } else {
            TabbedDetailForm.logger.warn("IFormManager of this TabbedDetailForm isn't a ITabbedFormManager");
        }
    }

    @Override
    public void hideDetailForm() {
        IFormManager formManager = this.form.getFormManager();
        if (formManager instanceof ITabbedFormManager) {
            ITabbedFormManager tabbedFormManager = (ITabbedFormManager) formManager;
            int index = tabbedFormManager.indexOfComponent(this);
            if (index > 0) {
                tabbedFormManager.removeTab(index);
                this.free();
            } else {
                // If index<0 then it's in a new frame
                Window parent = SwingUtilities.getWindowAncestor(this);
                List<JFrame> frameList = tabbedFormManager.getFrameList();
                int indexW = frameList == null ? -1 : frameList.indexOf(parent);
                if (indexW >= 0) {
                    JFrame remove = frameList.remove(indexW);
                    parent.setVisible(false);
                    FreeableUtils.freeComponent(remove);
                } else {
                    FreeableUtils.freeComponent(this);
                }
            }
            try {
            } catch (Exception e1) {
                TabbedDetailForm.logger.error(null, e1);
            }
        }
    }

    @Override
    public void setInsertMode() {
        super.setInsertMode();
        if (this.form.clearDataFieldButton != null) {
            this.form.clearDataFieldButton.setVisible(false);
        }

        Button queryButton = this.form.getButton(InteractionManager.QUERY_KEY);
        if (queryButton != null) {
            queryButton.setVisible(false);
        }
    }

    @Override
    public void setUpdateMode() {
        super.setUpdateMode();
        if (this.form.clearDataFieldButton != null) {
            this.form.clearDataFieldButton.setVisible(false);
        }

        Button queryButton = this.form.getButton(InteractionManager.QUERY_KEY);
        if (queryButton != null) {
            queryButton.setVisible(false);
        }

        Button insertButton = this.form.getButton(InteractionManager.INSERT_KEY);
        if (insertButton != null) {
            insertButton.setVisible(false);
        }
    }

    /**
     * This method sets the keys in the table records.<br>
     * This keys are used to query the record values
     * @param tableData
     * @param index
     */
    @Override
    public void setKeys(EntityResult tableData, int index) {
//        this.tableKeys = this.valuesToForm(tableData, index);
        this.tableKeys = new EntityResultMapImpl(new HashMap<>(this.valuesToForm(tableData, index)));
        // Reset the index of the selected element
        this.vectorIndex = 0;

        // If there are more than one record
        int recordNumber = 0;

        if (this.tableKeys.isEmpty() || (index < 0)) {
            this.form.disableButtons();
            this.form.disableDataFields();
        } else {
            Iterator<?> itTableKeys = this.tableKeys.keySet().iterator();
            recordNumber = 1;
            while (itTableKeys.hasNext()) {
                Object currentKey = itTableKeys.next();
                List<?> dataRow = (List<?>) this.tableKeys.get(currentKey);
                List<Object> newRow = new ArrayList<>();
                if (dataRow.size() > 0) {
                    newRow.add(dataRow.get(index));
                    dataRow.clear();
                    this.tableKeys.put(currentKey, newRow);
                } else {
                    recordNumber = 0;
                    break;
                }
            }

        }

        // if (index < recordNumber) {
        // this.vectorIndex = index;
        // }
        //

        if (!this.tableKeys.isEmpty() && (index >= 0)) {
            if (!(this.form instanceof FormExt)) {
                if (this.vectorIndex >= 0) {
                    this.data = this.query(this.vectorIndex);
                    this.form.updateDataFields(this.data);
                    if (recordNumber > 1) {
                        this.form.startButton.setEnabled(true);
                        this.form.previousButton.setEnabled(true);
                        this.form.nextButton.setEnabled(true);
                        this.form.endButton.setEnabled(true);
                        if (this.vectorIndex == 0) {
                            this.form.startButton.setEnabled(false);
                            this.form.previousButton.setEnabled(false);
                        } else if (this.vectorIndex >= (recordNumber - 1)) {
                            this.form.nextButton.setEnabled(false);
                            this.form.endButton.setEnabled(false);
                        }
                    }
                } else {
                    this.form.updateDataFields(new HashMap<>());
                }
            } else {
                ((FormExt) this.form).updateDataFields(this.tableKeys, this.vectorIndex);
            }
        } else {
            this.form.updateDataFields(new HashMap<>());
        }
        if (recordNumber == 0) {
            this.setQueryInsertMode();
        }
    }

    @Override
    public void setResourceBundle(ResourceBundle resourceBundle) {
        super.setResourceBundle(resourceBundle);
    }

}
