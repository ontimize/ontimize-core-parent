package com.ontimize.gui.table;

import java.util.Map;

import com.ontimize.gui.field.CurrencyDataField;
import com.ontimize.gui.field.TextDataField;

public class CurrencyCellEditor extends CellEditor {

    public CurrencyCellEditor(Map<Object, Object> parameters) {
        super(parameters.get(CellEditor.COLUMN_PARAMETER), CurrencyCellEditor.initializeDataField(parameters));
    }

    protected static CurrencyDataField initializeDataField(Map<Object, Object> parameters) {
        CurrencyDataField cdf = new CurrencyDataField(parameters);
        if (cdf.getDataField() instanceof TextDataField.EJTextField) {
            ((TextDataField.EJTextField) cdf.getDataField()).setCaretPositionOnFocusLost(false);
        }
        return cdf;
    }

}
