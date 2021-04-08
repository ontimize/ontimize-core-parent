package com.ontimize.gui.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.sql.Types;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.cache.CacheManager;
import com.ontimize.cache.CachedComponent;
import com.ontimize.db.NullValue;
import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.Form;
import com.ontimize.gui.ReferenceComponent;
import com.ontimize.gui.field.AccessForm;
import com.ontimize.gui.field.ComboDataField;
import com.ontimize.gui.field.DataField;
import com.ontimize.gui.field.ReferenceComboDataField;
import com.ontimize.gui.images.ImageManager;
import com.ontimize.locator.EntityReferenceLocator;
import com.ontimize.util.ParseUtils;

public class ComboReferenceCellEditor extends CellEditor
        implements ReferenceComponent, AccessForm, CachedComponent, ISetReferenceValues {

    private static final Logger logger = LoggerFactory.getLogger(ComboReferenceCellEditor.class);

    private EditorComp editorAux = null;

    protected Hashtable colsSetTypes;

    public ComboReferenceCellEditor(Hashtable parameters) {
        super(parameters.get("column"), new ExtCampoComboRef(parameters));

        ((ReferenceComboDataField) this.field).remove(((ReferenceComboDataField) this.field).getDataField());
        if (((ReferenceComboDataField) this.field).getDetailButtonListener() != null) {
            this.editorAux = new EditorComp((ReferenceComboDataField) this.field);
        }
        ((ExtCampoComboRef) this.field).setComboReferenceCellEditor(this);

        Object setTypes = parameters.get("onsetsqltypes");
        if (setTypes != null) {
            this.colsSetTypes = ApplicationManager.getTokensAt(setTypes.toString(), ";", ":");
        }
    }

    @Override
    public String getEntity() {
        return ((ExtCampoComboRef) this.field).getEntity();
    }

    @Override
    public Vector getAttributes() {
        return ((ExtCampoComboRef) this.field).getAttributes();
    }

    @Override
    public void setCacheManager(CacheManager m) {
        ((ExtCampoComboRef) this.field).setCacheManager(m);
    }

    protected Object getParentKeyValue(String p) {
        return null;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

        if (this.editorAux == null) {
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }

        if (value != null) {
            if (ApplicationManager.DEBUG) {
                System.out.println("getTableCellEditorComponent: " + value.toString());
            }
        } else {
            if (ApplicationManager.DEBUG) {
                System.out.println("getTableCellEditorComponent: NULL");
            }
        }
        if (table != null) {
            this.currentEditor = this.field;
            this.field.deleteData();
            this.field.setValue(value);
            this.editor = this.editorAux;
            this.editor.setBorder(this.getDefaultFocusBorder());
            this.editor.setFont(this.getEditorFont(table));
            this.editor.setForeground(CellEditor.fontColor);
            this.editor.setBackground(CellEditor.backgroundColor);
            return this.editor;
        } else {
            this.currentEditor = null;
            return null;
        }
    }

    @Override
    public void setReferenceLocator(EntityReferenceLocator locator) {
        if (this.field != null) {
            ((ReferenceComponent) this.field).setReferenceLocator(locator);
        }
    }

    @Override
    public void setParentForm(Form f) {
        if (this.field != null) {
            ((AccessForm) this.field).setParentForm(f);
        }
    }

    @Override
    public List getSetColumns() {
        if ((this.field != null) && (this.field instanceof ReferenceComboDataField)) {
            return ((ReferenceComboDataField) this.field).getOnSetValueSetAttributes();
        }
        return null;
    }

    @Override
    public Hashtable getSetData(boolean useNullValues) {
        if ((this.field != null) && (this.field instanceof ReferenceComboDataField)) {
            List columnsToSet = ((ReferenceComboDataField) this.field).getOnSetValueSetAttributes();
            if ((columnsToSet != null) && (columnsToSet.size() > 0)) {

                Object currentCode = this.field.getValue();

                Hashtable hCurrentComboCodeValues = new Hashtable();
                if (currentCode != null) {
                    hCurrentComboCodeValues = ((ReferenceComboDataField) this.field).getValuesToCode(currentCode);
                }

                Hashtable onSetValueSetEquivalences = ((ReferenceComboDataField) this.field)
                    .getOnSetValueSetEquivalences();

                Hashtable result = new Hashtable();
                for (int i = 0; i < columnsToSet.size(); i++) {
                    String colName = (String) columnsToSet.get(i);
                    String originalName = colName;
                    if ((onSetValueSetEquivalences != null) && onSetValueSetEquivalences.containsKey(colName)) {
                        originalName = (String) onSetValueSetEquivalences.get(colName);
                    }
                    if (!hCurrentComboCodeValues.containsKey(originalName)) {
                        if ((this.colsSetTypes != null) && this.colsSetTypes.containsKey(colName)) {
                            String colType = (String) this.colsSetTypes.get(colName);
                            result.put(columnsToSet.get(i), new NullValue(ParseUtils.getSQLType(colType)));
                        } else {
                            result.put(columnsToSet.get(i), new NullValue(Types.VARCHAR));
                        }
                    } else {
                        result.put(colName, hCurrentComboCodeValues.get(originalName));
                    }
                }
                return result;
            }
        }
        return null;
    }

    private class EditorComp extends JPanel {

        private JComponent dataComponent = null;

        private JButton detailButton = null;

        public EditorComp(ReferenceComboDataField dataField) {
            this.setLayout(new BorderLayout(0, 0));
            this.dataComponent = dataField.getDataField();
            this.setOpaque(false);

            if (dataField.getDetailButtonListener() != null) {
                this.detailButton = new DataField.FieldButton(ImageManager.getIcon(ImageManager.MAGNIFYING_GLASS)) {
                    @Override
                    public boolean isFocusTraversable() {
                        return false;
                    }
                };
                this.add(this.detailButton, BorderLayout.EAST);
                this.detailButton.setRequestFocusEnabled(false);
                this.detailButton.addActionListener(dataField.getDetailButtonListener());
            }

            this.add(dataField.getDataField());
            dataField.getDataField().setRequestFocusEnabled(false);
            dataField.getDataField().setBorder(null);
        }

    };

    protected static class ExtCampoComboRef extends ReferenceComboDataField {

        protected ComboReferenceCellEditor comboReferenceCellEditor = null;

        ExtCampoComboRef(Hashtable p) {
            super(p);

            ((CustomComboBox) this.dataField).setKeySelectionManager(new ComboDataField.ExtKeySelectionManager() {

                @Override
                public int getComboIndex(String str, ComboBoxModel m) {
                    long t = System.currentTimeMillis();

                    int selectedIndex = ((CustomComboBox) ExtCampoComboRef.this.dataField).getSelectedIndex();
                    if ((selectedIndex < 0) || (str.length() == 1)) {
                        selectedIndex = 0;
                    }
                    int nCoincidences = 0;
                    int maxIndex = -1;
                    for (int i = selectedIndex; i < m.getSize(); i++) {
                        int nEastCoincidences = 0;
                        String sText = ExtCampoComboRef.this.getCodeDescription(m.getElementAt(i));
                        sText = sText.replace('�', 'a');
                        sText = sText.replace('�', 'e');
                        sText = sText.replace('�', 'i');
                        sText = sText.replace('�', 'o');
                        sText = sText.replace('�', 'u');
                        sText = sText.replace('�', 'a');
                        sText = sText.replace('�', 'e');
                        sText = sText.replace('�', 'i');
                        sText = sText.replace('�', 'o');
                        sText = sText.replace('�', 'u');
                        for (int j = 0; (j < sText.length()) && (j < str.length()); j++) {
                            if (Character.toLowerCase(sText.charAt(j)) != Character.toLowerCase(str.charAt(j))) {
                                break;
                            } else {
                                nEastCoincidences++;
                            }
                        }
                        if (nEastCoincidences > nCoincidences) {
                            nCoincidences = nEastCoincidences;
                            maxIndex = i;
                        }
                    }

                    if (ApplicationManager.DEBUG) {
                        System.out.println("getIndexCombo() time: " + (System.currentTimeMillis() - t));
                    }

                    return maxIndex;
                }

                @Override
                public int selectionForKey(char keyChar, ComboBoxModel m) {
                    return -1;
                }
            });
        }

        public void setComboReferenceCellEditor(ComboReferenceCellEditor e) {
            this.comboReferenceCellEditor = e;
        }

        @Override
        protected Object getParentKeyValue(String p) {
            Object parentkey = null;
            if (this.comboReferenceCellEditor != null) {
                parentkey = this.comboReferenceCellEditor.getParentKeyValue(p);
            }
            if (parentkey != null) {
                return parentkey;
            } else {
                return super.getParentKeyValue(p);
            }
        }

    }

}
