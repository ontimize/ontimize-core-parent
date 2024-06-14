package com.ontimize.gui.table;

import java.awt.Component;
import java.util.Map;

import javax.swing.JTable;

import com.ontimize.gui.field.IntegerDataField;
import com.ontimize.gui.field.TextDataField;

public class IntegerCellEditor extends CellEditor {

    public IntegerCellEditor(Map<Object, Object> parameters) {
        super(parameters.get(CellEditor.COLUMN_PARAMETER), IntegerCellEditor.initializeDataField(parameters));
    }

    protected static IntegerDataField initializeDataField(Map<Object, Object> parameters) {
        IntegerDataField tdf = new IntegerDataField(parameters);
        if (tdf.getDataField() instanceof TextDataField.EJTextField) {
            ((TextDataField.EJTextField) tdf.getDataField()).setCaretPositionOnFocusLost(false);
        }
        return tdf;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }

}
