package com.ontimize.gui.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.text.JTextComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.Freeable;
import com.ontimize.gui.FreeableUtils;
import com.ontimize.gui.field.DataComponent;
import com.ontimize.gui.field.DataField;
import com.ontimize.gui.field.FormComponent;

public abstract class CellEditor extends JComponent implements TableCellEditor, FormComponent, Freeable {

	private static final Logger logger = LoggerFactory.getLogger(CellEditor.class);

	public static boolean selectAllFocusGained = true;

	public static boolean DEBUG_RENDERER = true;

	public static String COLUMN_PARAMETER = "column";

	public static final String EDIT_CELL_COMMAND = "EditCell";

	protected DataField field = null;

	protected Object value = null;

	protected int clickNumber = 1;

	protected DataComponent currentEditor = null;

	protected List<Object>		listeners				= new ArrayList<>();

	protected JComponent editor = null;

	protected String column = null;

	public static Color focusBorderColor = Color.red;

	public static Border focusBorder;

	public static Font font = null;

	public static Color fontColor = Color.black;

	public static Color backgroundColor = Color.white;

	public CellEditor(Object col, DataField c) {
		super();
		if (col == null) {
			throw new IllegalArgumentException(
					this.getClass().toString() + " : Parameter " + CellEditor.COLUMN_PARAMETER + " is MANDATORY");
		}
		if (col != null) {
			this.column = col.toString();
		}
		this.field = c;
		JComponent dataComponent = this.field.getDataField();
		dataComponent.addMouseListener(new MouseAdapter() {

			boolean selectAll = false;

			@Override
			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				if (e.getSource() instanceof JComponent) {
					this.selectAll = !((JComponent) e.getSource()).hasFocus();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				try {
					super.mouseReleased(e);
					if (CellEditor.selectAllFocusGained && this.selectAll
							&& (e.getSource() instanceof JTextComponent)) {
						((javax.swing.text.JTextComponent) e.getSource()).selectAll();
					}
				} finally {
					this.selectAll = false;
				}
			}
		});

		this.installKeyListener();
	}

	protected void installKeyListener() {
		JComponent dataCompnent = this.field.getDataField();
		dataCompnent.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.isConsumed()) {
					return;
				}

				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					CellEditor.this.stopCellEditing();
					return;
				}
			}
		});
	}

	@Override
	public Object getCellEditorValue() {
		CellEditor.logger.debug("Request Editor Value");
		if (this.value != null) {
			CellEditor.logger.debug("Return: {}", this.value.toString());
		}
		return this.value;
	}

	@Override
	public boolean isCellEditable(EventObject anEvent) {
		CellEditor.logger.debug("Asking whether the CellEditor is editable");
		if (anEvent == null) {
			return true;
		} else if (anEvent instanceof MouseEvent) {
			Object oSource = anEvent.getSource();
			if (oSource instanceof JTable) {
				((MouseEvent) anEvent).consume();
				if (((MouseEvent) anEvent).getClickCount() == this.clickNumber) {
					JTable t = (JTable) oSource;
					int eventRow = t.rowAtPoint(((MouseEvent) anEvent).getPoint());
					CellEditor.logger.debug("CellEditor.isCellEditable() row: {}", eventRow);
					if (eventRow == t.getSelectedRow()) {
						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else if ((anEvent instanceof ActionEvent) && (((ActionEvent) anEvent).getActionCommand() != null)) {
			if (((ActionEvent) anEvent).getActionCommand().equalsIgnoreCase(CellEditor.EDIT_CELL_COMMAND)) {
				return true;
			} else {
				return false;
			}
		} else if (anEvent instanceof KeyEvent) {
			return true;
		} else {
			return true;
		}
	}

	@Override
	public boolean shouldSelectCell(EventObject e) {
		return true;
	}

	@Override
	public boolean stopCellEditing() {
		if (this.currentEditor.isEmpty()) {
			this.value = null;
			this.fireEditingStopped();
			return true;
		} else {
			this.value = this.field.getValue();
			this.fireEditingStopped();
			return true;
		}
	}

	@Override
	public void cancelCellEditing() {
		this.value = null;
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
		CellEditor.logger.debug("fireEditingStopped");
		for (Object element : this.listeners) {
			((CellEditorListener) element).editingStopped(new ChangeEvent(this));
		}
	}

	protected void fireEditingCanceled() {
		CellEditor.logger.debug("fireEditingCanceled");
		for (Object element : this.listeners) {
			((CellEditorListener) element).editingCanceled(new ChangeEvent(this));
		}
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		if (value != null) {
			CellEditor.logger.debug("getTableCellEditorComponent: {}", value.toString());
		} else {
			CellEditor.logger.debug("getTableCellEditorComponent");
		}
		if (table != null) {
			this.currentEditor = this.field;
			this.field.deleteData();
			this.field.setValue(value);
			this.editor = this.field.getDataField();
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

	public Font getEditorFont(JTable table) {
		if (CellEditor.font == null) {
			CellEditor.font = CellRenderer.font != null ? CellRenderer.font : table.getFont();
		}
		return CellEditor.font;
	}

	public Border getDefaultFocusBorder() {
		if (CellEditor.focusBorder == null) {
			CellEditor.focusBorder = BorderFactory.createLineBorder(CellEditor.focusBorderColor, 2);
		}
		return CellEditor.focusBorder;
	}

	@Override
	public void addKeyListener(KeyListener k) {
		if (this.field != null) {
			this.field.getDataField().addKeyListener(k);
		} else {
			this.addKeyListener(k);
		}
	}

	@Override
	public void addFocusListener(FocusListener f) {
		if (this.field != null) {
			this.field.getDataField().addFocusListener(f);
		} else {
			super.addFocusListener(f);
		}
	}

	@Override
	public void setComponentLocale(Locale l) {
		if (this.field != null) {
			this.field.setComponentLocale(l);
		}
	}

	@Override
	public void setResourceBundle(ResourceBundle res) {
		if (this.field != null) {
			this.field.setResourceBundle(res);
		}
	}

	@Override
	public List<String> getTextsToTranslate() {
		if (this.field != null) {
			return this.field.getTextsToTranslate();
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public void setVisible(boolean visible) {
		if (this.field != null) {
			this.field.setVisible(visible);
		} else {
			super.setVisible(visible);
		}
	}

	@Override
	public Object getConstraints(LayoutManager l) {
		return this.column;
	}

	@Override
	public void init(Map<Object, Object> parameters) {
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (this.field != null) {
			this.field.setEnabled(enabled);
		} else {
			super.setEnabled(enabled);
		}
	}

	@Override
	public void requestFocus() {
		this.field.requestFocus();
	}

	public int getSQLDataType() {
		return this.field == null ? java.sql.Types.VARCHAR : this.field.getSQLDataType();
	}

	@Override
	public void free() {
		FreeableUtils.freeComponent(this.field);
		this.value = null;
	}

}
