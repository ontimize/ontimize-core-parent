package com.ontimize.gui.table;

import java.util.EventObject;
import java.util.Map;

public class InsertTableInsertRowEvent extends EventObject {

	Map<Object, Object> rowData = null;

    public InsertTableInsertRowEvent(InsertTableInsertRowChange source, Map<Object, Object> rowData) {
        super(source);
        this.rowData = rowData;
    }

    public Map<Object, Object> getRowData() {
        return this.rowData;
    }

}
