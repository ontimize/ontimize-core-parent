package com.ontimize.gui.table;

import java.util.Map;

import com.ontimize.gui.field.MaskDataField;

public class MaskCellEditor extends CellEditor {

	public MaskCellEditor(Map<Object, Object> parameters) {
		super(parameters.get(CellEditor.COLUMN_PARAMETER), new MaskDataField(parameters));
	}

}
