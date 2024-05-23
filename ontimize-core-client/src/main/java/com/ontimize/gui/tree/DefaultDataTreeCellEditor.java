package com.ontimize.gui.tree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.border.LineBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.MessageDialog;
import com.ontimize.gui.OpenDialog;
import com.ontimize.gui.field.DataComponent;
import com.ontimize.gui.field.DateDataField;
import com.ontimize.gui.field.IntegerDataField;
import com.ontimize.gui.field.RealDataField;
import com.ontimize.gui.field.TextDataField;
import com.ontimize.jee.common.db.Entity;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.locator.EntityReferenceLocator;

public class DefaultDataTreeCellEditor implements TreeCellEditor, OpenDialog {

	private static final Logger			logger			= LoggerFactory.getLogger(DefaultDataTreeCellEditor.class);

	public static boolean				DEBUG_RENDERER	= false;

	protected DateDataField				dateField		= new DateDataField(new HashMap<>());

	protected IntegerDataField			integerField	= new IntegerDataField(new HashMap<>());

	protected RealDataField				doubleField		= new RealDataField(new HashMap<>());

	protected TextDataField				textField		= new TextDataField(new HashMap<>());

	protected DataComponent				currentEditor	= null;

	protected int						clickCount		= 1;

	protected List<Object>				listeners		= new ArrayList<>();

	protected Object					value			= null;

	protected OTreeNode					editingNode		= null;

	protected JTree						tree			= null;

	protected TreePath					editingNodePath	= null;

	protected EntityReferenceLocator	locator			= null;

	protected Frame						parentFrame		= null;

	protected ResourceBundle			resources		= null;

	public DefaultDataTreeCellEditor(EntityReferenceLocator locator) throws IllegalArgumentException {
		super();
		if (locator == null) {
			throw new IllegalArgumentException("Locator parameter can not be null");
		}
		this.locator = locator;
		KeyAdapter kl = new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					DefaultDataTreeCellEditor.this.stopCellEditing();
				}
			}
		};
		this.integerField.getDataField().addKeyListener(kl);
		this.dateField.getDataField().addKeyListener(kl);
		this.doubleField.getDataField().addKeyListener(kl);
		this.textField.getDataField().addKeyListener(kl);
		FocusAdapter fl = new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				DefaultDataTreeCellEditor.this.cancelCellEditing();
			}
		};
		this.integerField.getDataField().addFocusListener(fl);
		this.dateField.getDataField().addFocusListener(fl);
		this.doubleField.getDataField().addFocusListener(fl);
		this.textField.getDataField().addFocusListener(fl);
	}

	@Override
	public Object getCellEditorValue() {
		if (DefaultDataTreeCellEditor.DEBUG_RENDERER) {
			DefaultDataTreeCellEditor.logger.debug(this.getClass().getName() + " : get cell editor value");
		}
		return this.value;
	}

	@Override
	public boolean isCellEditable(EventObject anEvent) {
		if (DefaultDataTreeCellEditor.DEBUG_RENDERER) {
			DefaultDataTreeCellEditor.logger.debug(this.getClass().getName() + " : is Cell Editable");
		}
		if (anEvent instanceof MouseEvent) {
			if (((MouseEvent) anEvent).isAltDown()) {
				Object oSource = anEvent.getSource();
				if (oSource instanceof JTree) {
					Object oNode = ((JTree) oSource).getClosestPathForLocation(((MouseEvent) anEvent).getX(), ((MouseEvent) anEvent).getY()).getLastPathComponent();
					if (oNode instanceof OTreeNode) {
						// ((MouseEvent)anEvent).consume();
						if (((OTreeNode) oNode).isOrganizational()) {
							return false;
						} else {
							if (((OTreeNode) oNode).isEmptyNode()) {
								return false;
							} else {
								// If it has more than one attribute then it is
								// not editable
								String[] attributes = ((OTreeNode) oNode).getAttributes();
								if ((attributes != null) && (attributes.length == 1)) {
									if (((MouseEvent) anEvent).getClickCount() >= this.clickCount) {
										return true;
									} else {
										return false;
									}
								} else {
									return false;
								}
							}
						}
					} else {
						return false;
					}
				} else {
					return false;
				}
			}
		}
		return false;
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
			if (this.editingNode != null) {
				this.value = this.currentEditor.getValue();
				this.fireEditingStopped();
				// Use entity to update
				try {
					String sNodeAttribute = this.editingNode.getAttributes()[0];
					Entity ent = this.locator.getEntityReference(this.editingNode.getEntityName());
					Map<Object, Object> filter = this.editingNode.getKeysValues();
					Map<Object, Object> hUpateAttributes = new HashMap<>();
					hUpateAttributes.put(sNodeAttribute, this.value);
					EntityResult res = ent.update(hUpateAttributes, filter, this.locator.getSessionId());
					if (res.getCode() == EntityResult.OPERATION_WRONG) {
						DefaultDataTreeCellEditor.logger.debug("Error updating data: " + res.getMessage());
						MessageDialog.showMessage(this.parentFrame, res.getMessage(), null, JOptionPane.ERROR_MESSAGE, this.resources);
						return false;
					} else {
						this.editingNode.setAttribute(sNodeAttribute, this.value);
						// Update form values
						if (this.tree instanceof Tree) {
							if (this.tree.isCollapsed(this.editingNodePath)) {
								((Tree) this.tree).updatePath(this.editingNodePath);
							} else {
								this.tree.collapsePath(this.editingNodePath);
							}
						}
						return true;
					}
				} catch (Exception e) {
					MessageDialog.showMessage(this.parentFrame, "Editor.ErrorUpdatingData", e.getMessage(), JOptionPane.ERROR_MESSAGE, this.resources);
					DefaultDataTreeCellEditor.logger.error(this.getClass().toString() + ": " + "Error updating data: " + e.getMessage(), e);
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
		if (DefaultDataTreeCellEditor.DEBUG_RENDERER) {
			DefaultDataTreeCellEditor.logger.debug("Editing Stopped event");
		}
		for (int i = 0; i < this.listeners.size(); i++) {
			((CellEditorListener) this.listeners.get(i)).editingStopped(new ChangeEvent(this));
		}
	}

	protected void fireEditingCanceled() {
		if (DefaultDataTreeCellEditor.DEBUG_RENDERER) {
			DefaultDataTreeCellEditor.logger.debug("Editinng canceled event");
		}
		for (int i = 0; i < this.listeners.size(); i++) {
			((CellEditorListener) this.listeners.get(i)).editingCanceled(new ChangeEvent(this));
		}
	}

	@Override
	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {
		if (DefaultDataTreeCellEditor.DEBUG_RENDERER) {
			DefaultDataTreeCellEditor.logger.debug("Get Tree Cell Editor Component for node: " + value.toString());
		}
		Object oNode = value;
		if (oNode instanceof OTreeNode) {
			if (!((OTreeNode) oNode).isOrganizational()) {
				// Save the node.
				this.editingNode = (OTreeNode) oNode;
				this.tree = tree;
				this.editingNodePath = tree.getPathForRow(row);
				this.dateField.deleteData();
				this.integerField.deleteData();
				this.doubleField.deleteData();
				this.textField.deleteData();
				String[] attributes = this.editingNode.getAttributes();
				if ((attributes == null) || (attributes.length != 1)) {
					return null;
				}
				String sAttribute = attributes[0];
				Object oEditingValue = this.editingNode.getNodeData().get(sAttribute);
				if (oEditingValue == null) {
					return null;
				}
				this.value = oEditingValue;
				if (oEditingValue instanceof Date) {
					this.dateField.setValue(oEditingValue);
					JComponent component = this.dateField.getDataField();
					component.setBorder(new LineBorder(Color.black));
					this.currentEditor = this.dateField;
					return component;
				} else if ((oEditingValue instanceof Integer) || (oEditingValue instanceof Short)) {
					this.integerField.setValue(oEditingValue);
					JComponent component = this.integerField.getDataField();
					component.setBorder(new LineBorder(Color.black));
					this.currentEditor = this.integerField;
					return component;
				} else if ((oEditingValue instanceof Double) || (oEditingValue instanceof Float)) {
					this.doubleField.setValue(oEditingValue);
					JComponent component = this.doubleField.getDataField();
					component.setBorder(new LineBorder(Color.black));
					this.currentEditor = this.doubleField;
					return component;
				} else {
					this.textField.setValue(oEditingValue);
					JComponent c = this.textField.getDataField();
					c.setBorder(new LineBorder(Color.black));
					this.currentEditor = this.textField;
					return c;
				}
			} else {
				this.currentEditor = null;
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public void setParentFrame(Frame parentFrame) {
		this.parentFrame = parentFrame;
	}

}
