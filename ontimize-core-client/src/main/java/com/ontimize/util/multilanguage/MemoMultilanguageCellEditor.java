package com.ontimize.util.multilanguage;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.field.MemoDataField;
import com.ontimize.gui.table.CellEditor;

public class MemoMultilanguageCellEditor extends CellEditor {

	private static final Logger logger = LoggerFactory.getLogger(MemoMultilanguageCellEditor.class);

	public MemoMultilanguageCellEditor(Map<Object, Object> parameters) {
		super(parameters.get(CellEditor.COLUMN_PARAMETER), new MemoDataField(parameters) {
			@Override
			public void init(Map<Object, Object> params) {
				super.init(params);
			}
		});
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		if (value != null) {
			MemoMultilanguageCellEditor.logger.debug("getTableCellEditorComponent: {}", value.toString());
		} else {
			MemoMultilanguageCellEditor.logger.debug("getTableCellEditorComponent");
		}
		if (table != null) {
			this.currentEditor = this.field;
			this.field.deleteData();
			this.field.setValue(value);
			this.editor = new JScrollPane(this.field.getDataField());
			if (ApplicationManager.useOntimizePlaf) {
				this.editor.setName("\"Table.editor\"");
			} else {
				this.editor.setBorder(this.getDefaultFocusBorder());
				this.editor.setFont(this.getEditorFont(table));
				this.editor.setForeground(CellEditor.fontColor);
				this.editor.setBackground(CellEditor.backgroundColor);
			}
			return this.editor;
		} else {
			this.currentEditor = null;
			return null;
		}
	}

	@Override
	protected void installKeyListener() {
		JComponent dataCompnent = this.field.getDataField();
		dataCompnent.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent event) {
				if (event.isConsumed()) {
					return;
				}
			}
		});
	}

}
