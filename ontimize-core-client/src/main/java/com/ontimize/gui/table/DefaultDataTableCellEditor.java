package com.ontimize.gui.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.MessageDialog;
import com.ontimize.gui.OpenDialog;
import com.ontimize.gui.field.DataComponent;
import com.ontimize.gui.field.TextDataField;
import com.ontimize.jee.common.db.Entity;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.locator.EntityReferenceLocator;

/**
 * Basic table cell editor class. It is possible to set a TableEditionValidator to validate the input values. Default behavior only allows not null input values.<br> The
 * TableEditionValidator will be call in the stopCellEditing method. If validEdition method return false then the changes are not applied .
 *
 * @deprecated
 */

@Deprecated
public class DefaultDataTableCellEditor implements TableCellEditor, OpenDialog {

	private static final Logger			logger			= LoggerFactory.getLogger(DefaultDataTableCellEditor.class);

	public static boolean				DEBUG_RENDERER	= true;

	protected TextDataField				textField		= new TextDataField(new HashMap<>());

	protected DataComponent				currentEditor	= null;

	protected int						clicksNumber	= 1;

	protected List<Object>				listeners		= new ArrayList<>();

	protected Object					value			= null;

	protected EntityReferenceLocator	locator			= null;

	protected int						editedColumn	= -1;

	protected int						editedRow		= -1;

	protected JTable					editedTable		= null;

	protected Table						table			= null;

	protected Frame						parentFrame		= null;

	protected ResourceBundle			resources		= null;

	protected TableEditionValidator		editValidator	= null;

	protected JComponent				editor			= null;

	public DefaultDataTableCellEditor(EntityReferenceLocator referenceLocator, Table table) {
		super();

		if (referenceLocator == null) {
			throw new IllegalArgumentException("Reference locator can not be null");
		}

		if (table == null) {
			throw new IllegalArgumentException("Table can not be null");
		}

		this.locator = referenceLocator;
		this.table = table;
		KeyAdapter kl = new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					DefaultDataTableCellEditor.this.stopCellEditing();
				}
			}
		};
		this.textField.getDataField().addKeyListener(kl);
		FocusAdapter fl = new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				DefaultDataTableCellEditor.this.cancelCellEditing();
			}
		};
		this.textField.getDataField().addFocusListener(fl);
	}

	@Override
	public Object getCellEditorValue() {
		if (DefaultDataTableCellEditor.DEBUG_RENDERER) {
			DefaultDataTableCellEditor.logger.debug("Requested editor value");
			if (this.value != null) {
				DefaultDataTableCellEditor.logger.debug("Return: " + this.value.toString());
			}
		}
		return this.value;
	}

	@Override
	public boolean isCellEditable(EventObject anEvent) {
		if (DefaultDataTableCellEditor.DEBUG_RENDERER) {
			DefaultDataTableCellEditor.logger.debug(this.getClass().getName() + ".isCellEditable() execution");
		}
		if (anEvent instanceof MouseEvent) {
			if (((MouseEvent) anEvent).isAltDown()) {
				Object origen = anEvent.getSource();
				if (origen instanceof JTable) {
					((MouseEvent) anEvent).consume();
					return ((MouseEvent) anEvent).getClickCount() >= this.clicksNumber;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else if (anEvent instanceof ActionEvent) {
			if (((ActionEvent) anEvent).getActionCommand().equalsIgnoreCase("EditCell")) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public boolean shouldSelectCell(EventObject e) {
		return true;
	}

	@Override
	public boolean stopCellEditing() {
		if (this.currentEditor.isEmpty()) {
			this.fireEditingStopped();
			return false;
		} else {
			if (this.editedTable != null) {
				this.value = this.currentEditor.getValue();
				if (this.fireValidateEdition(new CustomTableEditionEvent(this.table, this.value, this.editedRow, this.editedColumn))) {
					// Execute the update operation
					try {
						Entity ent = this.locator.getEntityReference(this.table.getEntityName());
						Map<Object, Object> kv = new HashMap<>();
						Object oRowKeyName = this.table.getKeyFieldName();
						Object oRowKey = this.table.getRowKey(this.editedRow);
						Object oParentKeyName = this.table.getParentKeyFieldName();
						Object oParentKeyValue = this.table.getParentKeyValue();

						kv.put(oRowKeyName, oRowKey);
						if (oParentKeyName != null) {
							kv.put(oParentKeyName, oParentKeyValue);
						}

						Map<Object, Object> av = new HashMap<>();
						av.put(this.table.getColumnName(this.editedColumn), this.value);
						EntityResult res = ent.update(av, kv, this.locator.getSessionId());
						if (res.getCode() != EntityResult.OPERATION_SUCCESSFUL) {
							this.fireEditingCanceled();
							MessageDialog.showMessage(this.parentFrame, res.getMessage(), ((EntityResultMapImpl) res).getDetails(), JOptionPane.ERROR_MESSAGE, this.resources);
							DefaultDataTableCellEditor.logger.debug("Error updatating data: " + res.getMessage());
							return false;
						} else {
							this.fireEditingStopped();
							// Cell is updated
							return true;
						}
					} catch (Exception e) {
						MessageDialog.showMessage(this.parentFrame, "Editor.ErrorUpdatingData", e.getMessage(), JOptionPane.ERROR_MESSAGE, this.resources);
						DefaultDataTableCellEditor.logger.error(this.getClass().toString() + ": " + "Error updating data: " + e.getMessage(), e);
						return false;
					}
				} else {
					this.fireEditingCanceled();
					this.fireEditingStopped();
					return false;
				}
			} else {
				this.fireEditingStopped();
				return false;
			}
		}
	}

	@Override
	public void cancelCellEditing() {
		this.fireEditingCanceled();
	}

	@Override
	public void addCellEditorListener(CellEditorListener l) {
		this.listeners.add(l);
	}

	@Override
	public void removeCellEditorListener(CellEditorListener l) {
		this.listeners.remove(l);
	}

	protected void fireEditingStopped() {
		if (DefaultDataTableCellEditor.DEBUG_RENDERER) {
			DefaultDataTableCellEditor.logger.debug("Editing stopped event");
		}
		for (Object element : this.listeners) {
			((CellEditorListener) element).editingStopped(new ChangeEvent(this));
		}
	}

	protected void fireEditingCanceled() {
		if (DefaultDataTableCellEditor.DEBUG_RENDERER) {
			DefaultDataTableCellEditor.logger.debug("Editing canceled event");
		}
		for (Object element : this.listeners) {
			((CellEditorListener) element).editingCanceled(new ChangeEvent(this));
		}
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		if (DefaultDataTableCellEditor.DEBUG_RENDERER) {
			if (value != null) {
				DefaultDataTableCellEditor.logger.debug("Requested editor component for cell: " + value.toString());
			} else {
				DefaultDataTableCellEditor.logger.debug("Requested editor component for cell: ");
			}
		}
		this.editedTable = table;
		this.editedRow = row;
		this.editedColumn = column;
		if (table != null) {
			this.textField.deleteData();
			this.textField.setValue(value);
			this.editor = this.textField.getDataField();
			this.editor.setBorder(new LineBorder(Color.red));
			this.currentEditor = this.textField;
			return this.editor;
		} else {
			this.currentEditor = null;
			return null;
		}
	}

	@Override
	public void setParentFrame(Frame parentFrame) {
		this.parentFrame = parentFrame;
	}

	public void setTableEditionValidator(TableEditionValidator l) {
		this.editValidator = l;
	}

	public void deleteTableEditionValidator() {
		this.editValidator = null;
	}

	protected boolean fireValidateEdition(CustomTableEditionEvent ev) {
		if (this.editValidator != null) {
			return this.editValidator.validEdition(ev);
		} else {
			return true;
		}
	}

}
