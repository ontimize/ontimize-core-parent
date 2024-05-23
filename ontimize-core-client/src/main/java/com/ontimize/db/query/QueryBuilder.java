package com.ontimize.db.query;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.db.query.store.FileQueryStore;
import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.MessageDialog;
import com.ontimize.gui.container.EJDialog;
import com.ontimize.gui.images.ImageManager;
import com.ontimize.jee.common.db.AdvancedQueryEntity;
import com.ontimize.jee.common.db.ContainsExtendedSQLConditionValuesProcessor;
import com.ontimize.jee.common.db.ContainsOperator;
import com.ontimize.jee.common.db.Entity;
import com.ontimize.jee.common.db.SQLStatementBuilder.BasicExpression;
import com.ontimize.jee.common.db.SQLStatementBuilder.BasicField;
import com.ontimize.jee.common.db.SQLStatementBuilder.BasicOperator;
import com.ontimize.jee.common.db.SQLStatementBuilder.Expression;
import com.ontimize.jee.common.db.SQLStatementBuilder.ExtendedSQLConditionValuesProcessor;
import com.ontimize.jee.common.db.SQLStatementBuilder.Field;
import com.ontimize.jee.common.db.SQLStatementBuilder.Operator;
import com.ontimize.jee.common.db.query.ParameterField;
import com.ontimize.jee.common.db.query.QueryExpression;
import com.ontimize.jee.common.db.query.store.QueryStore;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.locator.EntityReferenceLocator;

public class QueryBuilder extends JPanel {

	private static final Logger	logger		= LoggerFactory.getLogger(QueryBuilder.class);

	public static boolean		DEBUG		= false;

	/**
	 * Variable to indicate when must be shown the save/load expressions buttons without the Report
	 */
	private boolean				basicSave	= false;

	public boolean getBasicSave() {
		return this.basicSave;
	}

	public boolean setBasicSave(boolean basicSave) {
		return this.basicSave = basicSave;
	}

	protected static class ExpressionRenderer extends DefaultTableCellRenderer {

		public ExpressionRenderer() {
			super();
		}

		@Override
		public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {

			if (v instanceof Expression) {
				v = ExtendedSQLConditionValuesProcessor.createQueryConditionsExpress((Expression) v);
			}
			Component comp = super.getTableCellRendererComponent(t, v, s, false, r, c);

			if (c == 1) {
				if (s) {
					comp.setForeground(new Color(255, 255, 255));
				} else {
					comp.setForeground(new Color(0, 0, 0));
				}
			}
			return comp;
		}

	}

	protected static class ColumnsComparator implements Comparator {

		private ResourceBundle bundle = null;

		public ColumnsComparator(ResourceBundle bundle) {
			this.bundle = bundle;
		}

		@Override
		public int compare(Object o1, Object o2) {
			if (((String) o1).equals(ContainsExtendedSQLConditionValuesProcessor.ANY_COLUMN)) {
				return -1;
			}
			if (((String) o2).equals(ContainsExtendedSQLConditionValuesProcessor.ANY_COLUMN)) {
				return 1;
			}

			String s1 = ApplicationManager.getTranslation(o1.toString(), this.bundle);
			String s2 = ApplicationManager.getTranslation(o2.toString(), this.bundle);
			return s1.compareToIgnoreCase(s2);
		}

	}

	public static int getVarchar() {
		return ConditionsTableModel.VARCHAR;
	}

	public static int getDate() {
		return ConditionsTableModel.DATE;
	}

	public static int getNumber() {
		return ConditionsTableModel.NUMBER;
	}

	public static int getAny() {
		return ConditionsTableModel.ANY;
	}

	public static boolean needsExpressionParameters(Expression e) {
		if (e == null) {
			return false;
		}
		if (e.getLeftOperand() instanceof Expression) {
			boolean aux = QueryBuilder.needsExpressionParameters((Expression) e.getLeftOperand());
			if (aux) {
				return true;
			}
			if (e.getRightOperand() instanceof Expression) {
				aux = QueryBuilder.needsExpressionParameters((Expression) e.getRightOperand());
			}
			return aux;
		} else {
			return (e.getRightOperand() != null) && (e.getRightOperand() instanceof ParameterField) && (((ParameterField) e.getRightOperand()).getValue() == null);
		}
	}

	public static boolean hasExpressionParameters(Expression e) {
		if (e == null) {
			return false;
		}
		if (e.getLeftOperand() instanceof Expression) {
			boolean aux = QueryBuilder.needsExpressionParameters((Expression) e.getLeftOperand());
			if (aux) {
				return true;
			}
			if (e.getRightOperand() instanceof Expression) {
				aux = QueryBuilder.hasExpressionParameters((Expression) e.getRightOperand());
			}
			return aux;
		} else {
			return (e.getRightOperand() != null) && (e.getRightOperand() instanceof ParameterField);
		}
	}

	protected static class ConditionsTableModel extends AbstractTableModel {

		public static final int		NUMBER			= 0;

		public static final int		DATE			= 1;

		public static final int		VARCHAR			= 2;

		public static final int		ANY				= 3;

		public static final int		BOOLEAN			= 4;

		protected Expression[]		expressions		= new Expression[0];

		protected String[]			columns			= null;

		protected List<Object>		expressionList	= new ArrayList<>();

		protected List<Object>		auxcols			= new ArrayList<>();

		protected List<Object>		auxtypes		= new ArrayList<>();

		protected ResourceBundle	bundle			= null;

		protected List<Object> getColumnTypes() {
			return this.auxtypes;
		}

		public ConditionsTableModel(ResourceBundle bundle) {
			this.bundle = bundle;
			this.columns = new String[] { ApplicationManager.getTranslation("QueryBuilderField", bundle), ApplicationManager.getTranslation("QueryBuilderOperator",
					bundle), ApplicationManager.getTranslation("QueryBuilderValue", bundle), ApplicationManager.getTranslation("QueryBuilderExpression", bundle) };
		}

		protected void setExpressions(List<Object> list) {
			Expression[] e = new Expression[list.size()];

			for (int i = 0, j = list.size(); i < j; i++) {
				e[i] = (Expression) list.get(i);
			}
			this.expressions = e;
		}

		protected void setColumnTypes(Object[] auxcols, int[] auxtypes) {
			this.auxcols = java.util.Arrays.asList(auxcols);
			for (int auxtype : auxtypes) {
				this.auxtypes.add(new Integer(auxtype));
			}
		}

		public Operator[] getTypeOperators(int type) {
			switch (type) {
				case NUMBER:
					return new Operator[] { BasicOperator.EQUAL_OP, BasicOperator.NOT_EQUAL_OP, BasicOperator.LESS_OP, BasicOperator.LESS_EQUAL_OP, BasicOperator.MORE_OP, BasicOperator.MORE_EQUAL_OP, BasicOperator.NULL_OP, BasicOperator.NOT_NULL_OP, BasicOperator.LIKE_OP, BasicOperator.NOT_LIKE_OP };

				case DATE:
					return new Operator[] { BasicOperator.EQUAL_OP, BasicOperator.NOT_EQUAL_OP, BasicOperator.LESS_OP, BasicOperator.LESS_EQUAL_OP, BasicOperator.MORE_OP, BasicOperator.MORE_EQUAL_OP, BasicOperator.NULL_OP, BasicOperator.NOT_NULL_OP, BasicOperator.LIKE_OP, BasicOperator.NOT_LIKE_OP };

				case VARCHAR:
					return new Operator[] { BasicOperator.EQUAL_OP, BasicOperator.NOT_EQUAL_OP, BasicOperator.LIKE_OP, BasicOperator.NOT_LIKE_OP, BasicOperator.NULL_OP, BasicOperator.NOT_NULL_OP };

				case ANY:
					return new Operator[] { ContainsOperator.CONTAINS_OP, ContainsOperator.NOT_CONTAINS_OP };

				case BOOLEAN:
					return new Operator[] { BasicOperator.EQUAL_OP, BasicOperator.NOT_EQUAL_OP, BasicOperator.NULL_OP, BasicOperator.NOT_NULL_OP };

				default:
					return new Operator[] { BasicOperator.NULL_OP, BasicOperator.NOT_NULL_OP, BasicOperator.LIKE_OP, BasicOperator.NOT_LIKE_OP };
			}
		}

		protected void addExpression() {
			this.addExpression(new BasicExpression(null, null, null));
		}

		protected void addExpression(Expression e) {
			Expression[] ne = new Expression[this.expressions.length + 1];
			BasicExpression be = (BasicExpression) e;
			System.arraycopy(this.expressions, 0, ne, 0, this.expressions.length);
			ne[this.expressions.length] = be;
			this.expressions = ne;
			this.expressionList.add(e);

			this.fireTableChanged(new TableModelEvent(this));

		}

		private void initListExpression(Expression expression) {
			if (expression.getRightOperand() instanceof Expression) {
				this.initListExpression((Expression) expression.getLeftOperand());
				this.initListExpression((Expression) expression.getRightOperand());
			} else {
				this.expressionList.add(expression);
			}
		}

		public void addInitExpression(Expression expression) {
			// Insert all in the list
			this.initListExpression(expression);
			Expression[] ne = new Expression[this.expressions.length + 1];
			BasicExpression be = (BasicExpression) expression;
			System.arraycopy(this.expressions, 0, ne, 0, this.expressions.length);
			ne[this.expressions.length] = be;
			this.expressions = ne;
			this.fireTableChanged(new TableModelEvent(this));
		}

		public List<Object> getExpressionsList() {
			return this.expressionList;
		}

		public void clearExpressionValues() {
			if (this.expressionList == null) {
				return;
			}

			for (Object element : this.expressionList) {
				if (((Expression) element).getRightOperand() instanceof ParameterField) {
					((ParameterField) ((Expression) element).getRightOperand()).setValue(null);
				}
			}
			this.fireTableChanged(new TableModelEvent(this));
		}

		@Override
		public int getRowCount() {
			return this.expressions != null ? this.expressions.length : 0;
		}

		@Override
		public int getColumnCount() {
			return this.columns.length;
		}

		@Override
		public String getColumnName(int c) {
			return this.columns[c];
		}

		@Override
		public Object getValueAt(int r, int c) {
			if (r >= this.expressions.length) {
				return null;
			}
			if (c == 0) {
				return this.expressions[r].getLeftOperand();
			} else if (c == 1) {
				return this.expressions[r].getOperator();
			} else if (c == 2) {
				return this.expressions[r].getRightOperand();
			} else if (c == 3) {
				return ContainsExtendedSQLConditionValuesProcessor.createQueryConditionsExpress(this.expressions[r]);
			} else {
				return null;
			}
		}

		public void setValueAtCustom(Object v, int r, int c) throws Exception {

			if (!(r < this.expressions.length)) {
				return;
			}
			if (c == 0) {

				if ((v instanceof Field) || (v instanceof Expression) || (v == null)) {
					super.setValueAt(v, r, c);
					this.expressions[r].setLeftOperand(v);
				} else if (v instanceof String) {
					BasicField bf = new BasicField((String) v);
					super.setValueAt(bf, r, c);
					super.setValueAt(null, r, 1);
					super.setValueAt(null, r, 2);

					this.expressions[r].setLeftOperand(bf);
					this.expressions[r].setOperator(null);
					this.expressions[r].setRightOperand(null);
				} else {
					throw new IllegalArgumentException("must be field or expression");
				}
			} else if (c == 1) {
				if ((v == null) || (v instanceof Operator)) {
					super.setValueAt(v, r, c);
					this.expressions[r].setOperator((Operator) v);
					super.setValueAt(null, r, 2);
					this.expressions[r].setRightOperand(null);
				} else {
					throw new IllegalArgumentException("must be operator");
				}
			} else if (c == 2) {
				if (v instanceof String) {

					String s = ((BasicField) this.expressions[r].getLeftOperand()).toString().trim();
					int i = this.auxcols.indexOf(s);
					if (ConditionsTableModel.NUMBER == ((Integer) this.auxtypes.get(i)).intValue()) {
						String sValue = (String) v;

						if (v.equals("")) {
							return;
						}

						if (this.expressions[r].getOperator().toString().equals(BasicOperator.LIKE) || this.expressions[r].getOperator().toString()
								.equals(BasicOperator.NOT_LIKE)) {
							sValue = sValue.replace('?', '0');
							sValue = sValue.replace('*', '0');
						}
						try {
							double current = Double.parseDouble(sValue);
							QueryBuilder.logger.trace("Value is right : {}", current);
						} catch (Exception e) {
							throw new Exception("M_QueryBuilderErrorInsercionEsperaInt", e);
						}
					} else if (ConditionsTableModel.BOOLEAN == ((Integer) this.auxtypes.get(i)).intValue()) {
						String sValue = (String) v;
						if (!sValue.equals("0") && !sValue.equals("1")) {
							throw new Exception("M_QueryBuilderErrorInsercionEsperaBoo");
						}
					}
				}
				super.setValueAt(v, r, c);
				if (v instanceof String) {
					this.expressions[r].setRightOperand(v);
				} else {
					this.expressions[r].setRightOperand(v);
				}
			}
			this.fireTableChanged(new TableModelEvent(this));
		}

		@Override
		public boolean isCellEditable(int r, int c) {
			if (c == 0) {
				Object v = this.getValueAt(r, 0);
				if (v instanceof Expression) {
					return false;
				}
				return true;
			} else if (c == 1) {
				Object v = this.getValueAt(r, 0);
				if (v == null) {
					return false;
				} else {
					return true;
				}
			} else if (c == 2) {
				Object v = this.getValueAt(r, 0);
				Object v2 = this.getValueAt(r, 1);
				if (v instanceof Expression) {
					return false;
				}

				if ((v == null) || (v2 == null) || (v2 == BasicOperator.NULL_OP) || (v2 == BasicOperator.NOT_NULL_OP)) {
					this.setValueAt(null, r, 2);
					return false;
				} else {
					return true;
				}
			} else {
				return false;
			}
		}

		public Expression getExpression(int r) {
			if (r < this.expressions.length) {
				return this.expressions[r];
			}
			return null;
		}

		public void split(int r) {
			if ((r < 0) || (r >= this.getRowCount())) {
				return;
			}
			// Combine the expressions
			if (!this.isExpressionOK(r)) {
				return;
			}
			// Split only if it is a composed expressions
			if (!(this.expressions[r].getLeftOperand() instanceof Expression)) {
				return;
			}
			if (!(this.expressions[r].getRightOperand() instanceof Expression)) {
				return;
			}
			// Insert left expression in r
			// and right in r+1
			Expression[] newE = new Expression[this.expressions.length + 1];
			for (int i = 0; i < r; i++) {
				newE[i] = this.expressions[i];
			}
			newE[r] = (Expression) this.expressions[r].getRightOperand();
			newE[r + 1] = (Expression) this.expressions[r].getLeftOperand();
			for (int i = r + 2; i < newE.length; i++) {
				newE[i] = this.expressions[i - 1];
			}
			this.expressions = newE;
			this.fireTableDataChanged();
		}

		public void operationBetweenRows(int[] rows, Operator op) {
			// Go through the objects
			// Get the lower row
			// Order
			Arrays.sort(rows);
			List<Object> list = new ArrayList<>();

			for (int row : rows) {
				if (!this.isExpressionOKWithoutParameters(row)) {
					return;
				}
			}

			for (int i = rows.length - 1; i >= 0; i--) {
				int r = rows[i];
				if ((r < 0) || (r >= this.getRowCount())) {
					continue;
				}
				// Combine expressions
				list.add(this.expressions[r]);
				this.expressions[r] = null;
				for (int j = r + 1; j < this.expressions.length; j++) {
					this.expressions[j - 1] = this.expressions[j];
				}
				Expression[] newE = new Expression[this.expressions.length - 1];
				System.arraycopy(this.expressions, 0, newE, 0, newE.length);
				this.expressions = newE;
			}
			if (list.size() < 2) {
				return;
			}

			BasicExpression be = new BasicExpression(list.get(0), op, list.get(1));
			for (int i = 2; i < list.size(); i++) {
				be = new BasicExpression(be, op, list.get(i));
			}
			// Insert the expression in rows[0]
			Expression[] newE = new Expression[this.expressions.length + 1];
			for (int i = 0; i < rows[0]; i++) {
				newE[i] = this.expressions[i];
			}
			newE[rows[0]] = be;
			for (int i = rows[0] + 1; i < newE.length; i++) {
				newE[i] = this.expressions[i - 1];
			}
			this.expressions = newE;
			this.fireTableDataChanged();
		}

		public void orBetweenRows(int[] rows) {
			this.operationBetweenRows(rows, BasicOperator.OR_OP);
		}

		public void doAndBetweenRows(int[] rows) {
			this.operationBetweenRows(rows, BasicOperator.AND_OP);
		}

		public void doAndNotBetweenRows(int[] rows) {
			this.operationBetweenRows(rows, BasicOperator.AND_NOT_OP);
		}

		public void orNotBetweenRows(int[] rows) {
			this.operationBetweenRows(rows, BasicOperator.OR_NOT_OP);
		}

		private void removeListExpression(Expression e) {

			if (e.getLeftOperand() instanceof Expression) {
				this.removeListExpression((Expression) e.getLeftOperand());
				this.removeListExpression((Expression) e.getRightOperand());
			} else {
				int a = this.expressionList.size();
				int i = 0;
				while ((i < a) && !((Expression) this.expressionList.get(i)).equals(e)) {
					i++;
				}
				if (i < a) {
					this.expressionList.remove(i);
				}
			}
		}

		public void removeRows(int[] rows) {
			Arrays.sort(rows);

			for (int row : rows) {
				this.removeListExpression(this.expressions[row]);
			}

			Expression[] newE = new Expression[this.expressions.length - rows.length];
			int a = 0;
			int b = 0;
			for (int i = 0; i < this.expressions.length; i++) {
				if ((a >= rows.length) || (i != rows[a])) {
					newE[b] = this.expressions[i];
					b++;
				} else {
					a++;
				}
			}

			this.expressions = newE;
			this.fireTableDataChanged();
		}

		public void removeAllExpressions() {
			this.expressions = new Expression[0];
			this.expressionList = new ArrayList<>();
			this.fireTableDataChanged();
		}

		protected boolean isExpressionOKWithoutParameters(int r) {
			return this.isExpressionOKWithoutParameters(this.expressions[r]);
		}

		protected boolean isExpressionOKWithoutParameters(Expression e) {
			return this.analyzeExpression(e, false);
		}

		protected boolean isExpressionOK(int r) {
			return this.isExpressionOK(this.expressions[r]);
		}

		protected boolean isExpressionOK(Expression e) {
			return this.analyzeExpression(e, true);
		}

		private boolean analyzeExpression(Expression e, boolean parameters) {

			if (e == null) {
				return false;
			}

			if (e.getLeftOperand() instanceof Expression) {
				boolean aux = this.analyzeExpression((Expression) e.getLeftOperand(), parameters);
				if (!aux) {
					return false;
				}
				if (e.getRightOperand() instanceof Expression) {
					aux = this.analyzeExpression((Expression) e.getRightOperand(), parameters);
				}
				return aux;
			} else {
				return (e.getLeftOperand() != null) && (e.getOperator() != null) && ((!parameters && (e.getRightOperand() != null)) || (parameters && (e
						.getRightOperand() != null) && !(e.getRightOperand() instanceof ParameterField)) || (parameters && (e
								.getRightOperand() != null) && (e.getRightOperand() instanceof ParameterField) && (((ParameterField) e.getRightOperand()).getValue() != null)) || e
										.getOperator().toString()
										.equals(BasicOperator.NULL_OP.toString()) || e.getOperator().toString().equals(BasicOperator.NOT_NULL_OP.toString()));
			}
		}

		protected boolean needsExpressionParameters(int r) {
			if (r >= this.expressions.length) {
				return false;
			}
			return QueryBuilder.needsExpressionParameters(this.expressions[r]);
		}

	}

	static class CustomCellEditor extends DefaultCellEditor {

		CustomCellEditor(JComboBox combo) {
			super(combo);
			this.setClickCountToStart(2);
		}

		CustomCellEditor(JTextField tf) {
			super(tf);
			this.setClickCountToStart(2);
		}

		@Override
		public boolean shouldSelectCell(EventObject anEvent) {
			return true;
		}

		@Override
		public boolean isCellEditable(EventObject e) {
			if (e instanceof MouseEvent) {
				if (((MouseEvent) e).isControlDown()) {
					return false;
				} else if (((MouseEvent) e).isShiftDown()) {
					return false;
				}
				if (((MouseEvent) e).getClickCount() < this.getClickCountToStart()) {
					return false;
				}
			}

			return true;
		}

	}

	protected static class CustomDefaultListCellRenderer extends DefaultListCellRenderer {

		ResourceBundle bundle = null;

		public CustomDefaultListCellRenderer(ResourceBundle bundle) {
			this.bundle = bundle;
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (c instanceof JLabel) {
				((JLabel) c).setText(ApplicationManager.getTranslation(((JLabel) c).getText(), this.bundle));
			}

			return c;
		}

	}

	static class ComboEditor extends CustomCellEditor /*
														 * implements TableCellEditor
														 */ {

		boolean			editable	= false;

		ResourceBundle	bundle;

		public ComboEditor(ResourceBundle bundle) {
			super(new JComboBox());
			this.bundle = bundle;
		}

		public ComboEditor(boolean editable, ResourceBundle bundle) {
			super(new JComboBox());
			this.editable = editable;
			this.bundle = bundle;
		}

		Object[] items = null;

		@Override
		public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
			Component comp = super.getTableCellEditorComponent(t, v, s, r, c);
			if (comp instanceof JComboBox) {
				if (((DefaultComboBoxModel) ((JComboBox) comp).getModel()).getIndexOf(v) < 0) {
					((JComboBox) comp).setSelectedItem(v);
				}
				((JComboBox) comp).setSelectedItem(v);

				if (this.editable) {
					((JComboBox) comp).setEditable(true);
				}

			}
			return comp;
		}

		public void setItems(Object[] items) {
			this.items = items;
			((JComboBox) super.getComponent()).setRenderer(new CustomDefaultListCellRenderer(this.bundle));
			((JComboBox) super.getComponent()).setModel(new DefaultComboBoxModel(this.items));
		}

	};

	static class TextEditor extends CustomCellEditor implements TableCellEditor {

		public TextEditor() {
			super(new JTextField());
		}

		@Override
		public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
			Component comp = super.getTableCellEditorComponent(t, v, s, r, c);
			return comp;
		}

	};

	protected static class ETable extends JTable {

		protected ExpressionRenderer expressionRenderer = new ExpressionRenderer();

		public Expression getExpression(int i) {
			return null;
		}

		public boolean expressionOK(Expression e) {
			return false;
		}

		@Override
		public TableCellRenderer getCellRenderer(int r, int c) {
			return this.expressionRenderer;
		}

	}

	protected static class CustomTableCellRenderer extends DefaultTableCellRenderer {

		ResourceBundle bundle;

		public CustomTableCellRenderer(ResourceBundle bundle) {
			this.bundle = bundle;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
			if ((column == 0) || (column == 2)) {

				if (value instanceof ParameterField) {
					if (!isSelected) {
						if (((ParameterField) value).getValue() == null) {
							c.setForeground(new Color(255, 0, 0));
						} else {
							c.setForeground(new Color(0, 0, 255));
						}
					} else {
						c.setForeground(new Color(255, 255, 255));
					}
				} else {
					if (value instanceof Field) {
						if (!isSelected) {
							c.setForeground(new Color(0, 0, 255));
						} else {
							c.setForeground(new Color(255, 255, 255));
						}
					} else {
						if (!isSelected) {
							c.setForeground(new Color(0, 0, 0));
						} else {
							c.setForeground(new Color(255, 255, 255));
						}
					}
				}

				if (c instanceof JLabel) {

					if (column == 0) {
						if (((ConditionsTableModel) table.getModel()).getExpression(row).getLeftOperand() instanceof Expression) {
							((JLabel) c).setText(ContainsSQLConditionValuesProcessorHelper
									.renderQueryConditionsExpressBundle((Expression) ((ConditionsTableModel) table.getModel()).getExpression(row).getLeftOperand(), this.bundle));
						} else {

							if (((ConditionsTableModel) table.getModel()).getExpression(row).getLeftOperand() != null) {
								((JLabel) c).setText(ApplicationManager
										.getTranslation(((Field) ((ConditionsTableModel) table.getModel()).getExpression(row).getLeftOperand()).toString(), this.bundle));
							}
						}
					} else {
						if (((ConditionsTableModel) table.getModel()).getExpression(row).getRightOperand() instanceof Expression) {
							((JLabel) c).setText(ContainsSQLConditionValuesProcessorHelper
									.renderQueryConditionsExpressBundle((Expression) ((ConditionsTableModel) table.getModel()).getExpression(row).getRightOperand(), this.bundle));
						} else {
							if (((ConditionsTableModel) table.getModel()).getExpression(row).getRightOperand() != null) {
								if (((ConditionsTableModel) table.getModel()).getExpression(row).getRightOperand() instanceof Field) {
									((JLabel) c).setText(ApplicationManager
											.getTranslation(((Field) ((ConditionsTableModel) table.getModel()).getExpression(row).getRightOperand()).toString(), this.bundle));
								} else {
									((JLabel) c).setText(ApplicationManager.getTranslation((String) ((ConditionsTableModel) table.getModel()).getExpression(row).getRightOperand(),
											this.bundle));
								}
							}
						}
					}
				}
			}

			return c;

		}

	}

	protected static class CustomTableCellRendererExpression extends DefaultTableCellRenderer {

		ResourceBundle bundle;

		public CustomTableCellRendererExpression(ResourceBundle bundle) {
			this.bundle = bundle;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			if (c instanceof JLabel) {
				if (column == 3) {

					if (((ConditionsTableModel) table.getModel()).getRowCount() != 0) {
						((JLabel) c).setText(ContainsSQLConditionValuesProcessorHelper
								.renderQueryConditionsExpressBundle(((ConditionsTableModel) table.getModel()).getExpression(row), this.bundle));
						Expression e = ((ConditionsTableModel) table.getModel()).getExpression(row);
						if (!isSelected) {
							if (((ConditionsTableModel) table.getModel()).isExpressionOK(e)) {
								Color b = new Color(0, 0, 0);
								c.setForeground(b);
							} else {
								Color b = new Color(255, 0, 0);
								c.setForeground(b);

							}
						}
					}
				}
			}
			return c;
		}

	}

	protected static class pvTableModel extends AbstractTableModel {

		private List<Object>	parameterValueList	= null;

		private ResourceBundle	bundle				= null;

		private int[]			types;

		private String[]		cols;

		public pvTableModel(ResourceBundle bundle, List<Object> l, String[] cols, int[] types) {
			this.bundle = bundle;
			this.parameterValueList = l;
			this.cols = cols;
			this.types = types;
		}

		public void setColsAndTypes(String[] cols, int[] types) {
			this.types = types;
			this.cols = cols;
		}

		@Override
		public int getRowCount() {
			if (this.parameterValueList == null) {
				return 0;
			}
			return this.parameterValueList.size();
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public String getColumnName(int c) {
			if (c == 0) {
				return ApplicationManager.getTranslation("QueryBuilderField", this.bundle);
			}
			if (c == 1) {
				return ApplicationManager.getTranslation("QueryBuilderOperator", this.bundle);
			}
			if (c == 2) {
				return ApplicationManager.getTranslation("QueryBuilderValue", this.bundle);
			}
			return null;
		}

		@Override
		public Object getValueAt(int r, int c) {
			if (this.parameterValueList == null) {
				return null;
			}

			if (r < this.parameterValueList.size()) {
				Expression e = (Expression) this.parameterValueList.get(r);
				if (c == 0) {
					return e.getLeftOperand();
				} else if (c == 1) {
					return e.getOperator();
				} else if (c == 2) {
					if (((ParameterField) ((Expression) this.parameterValueList.get(r)).getRightOperand()).getValue() == null) {
						return null;
					}
					return ((ParameterField) e.getRightOperand()).getValue();
				} else {
					return null;
				}
			}
			return null;
		}

		public void setListParameter(List list) {
			this.parameterValueList = list;
			this.fireTableDataChanged();
		}

		@Override
		public boolean isCellEditable(int r, int c) {
			return c == 2;
		}

		public void setValueAtCustom(Object aValue, int r, int c) throws Exception {
			if (r >= this.parameterValueList.size()) {
				return;
			}
			if (c != 2) {
				return;
			}

			String s = this.getValueAt(r, 0).toString().trim();

			int ind = -1;
			for (int i = 0, a = this.cols.length; i < a; i++) {
				if (this.cols[i].equals(s)) {
					ind = i;
					break;
				}
			}

			if (ind != -1) {
				if (ConditionsTableModel.NUMBER == this.types[ind]) {
					String sValue = (String) aValue;
					if (sValue.equals("")) {
						return;
					}

					Operator op = (Operator) this.getValueAt(r, 1);

					if (op.toString().equals(BasicOperator.LIKE) || op.toString().equals(BasicOperator.NOT_LIKE)) {
						sValue = sValue.replace('?', '0');
						sValue = sValue.replace('*', '0');
					}

					try {
						double current = Double.parseDouble(sValue);
						QueryBuilder.logger.trace("Value is right : " + current);
					} catch (Exception e) {
						throw new Exception("M_QueryBuilderErrorInsercionEsperaInt", e);
					}
					((ParameterField) ((Expression) this.parameterValueList.get(r)).getRightOperand()).setValue(aValue);
				} else {
					((ParameterField) ((Expression) this.parameterValueList.get(r)).getRightOperand()).setValue(aValue);
				}
			} else {
				((ParameterField) ((Expression) this.parameterValueList.get(r)).getRightOperand()).setValue(aValue);
			}
		}

	}

	protected static class pvTable extends JTable {

		private final TextEditor	textEditor			= new TextEditor();

		private ResourceBundle		bundle				= null;

		private String[]			cols				= null;

		private int[]				types				= null;

		private CustomCellEditor	customCellEditor	= null;

		public pvTable(ResourceBundle bundle, List<Object> l, String[] cols, int[] types) {
			this.bundle = bundle;
			this.cols = cols;
			this.types = types;
			((JTable) this).setModel(new pvTableModel(bundle, l, cols, types));
		}

		public void setColsAndTypes(String[] cols, int[] types) {
			TableModel model = this.getModel();
			if (model instanceof pvTableModel) {
				((pvTableModel) model).setColsAndTypes(cols, types);
			}
		}

		@Override
		public TableCellEditor getCellEditor(int row, int c) {
			if ((c == 0) || (c == 1) || (c == 2)) {
				String sName = ((BasicField) ((JTable) this).getModel().getValueAt(row, 0)).toString().trim();
				int ind = -1;
				for (int i = 0, a = this.cols.length; i < a; i++) {
					if (this.cols[i].equals(sName)) {
						ind = i;
						break;
					}
				}
				if (ind != -1) {
					int tipo = this.types[ind];
					if (tipo == ConditionsTableModel.BOOLEAN) {
						List<String> v = new ArrayList<>();
						v.add("0");
						v.add("1");
						JComboBox jc = new JComboBox(v.toArray());
						this.customCellEditor = new CustomCellEditor(jc);
						return this.customCellEditor;
					}
				}
				return this.textEditor;
			} else {
				return null;
			}
		}

		@Override
		public void setValueAt(Object aValue, int row, int column) {
			try {
				((pvTableModel) this.getModel()).setValueAtCustom(aValue, row, this.convertColumnIndexToModel(column));
			} catch (Exception e) {
				QueryBuilder.logger.error(null, e);
				super.setValueAt(null, row, column);
				if (SwingUtilities.getWindowAncestor(this) instanceof Frame) {
					MessageDialog.showMessage((Frame) SwingUtilities.getWindowAncestor(this), e.getMessage(), JOptionPane.OK_OPTION, this.bundle);
				} else {
					MessageDialog.showMessage((Dialog) SwingUtilities.getWindowAncestor(this), e.getMessage(), JOptionPane.OK_OPTION, this.bundle);
				}
			}

		}

		public void setListParameter(List l) {
			TableModel model = this.getModel();
			if (model instanceof pvTableModel) {
				pvTableModel tModel = (pvTableModel) model;
				tModel.setListParameter(l);
			}
		}

	}

	protected static Map<Object, Object> getColumnTypes(String e, ResourceBundle bu) {

		Map<Object, Object> h = new HashMap<>();
		try {
			EntityReferenceLocator b = ApplicationManager.getApplication().getReferenceLocator();
			Object entity = b.getEntityReference(e);

			if ((e != null) && (entity instanceof AdvancedQueryEntity)) {

				AdvancedQueryEntity eAv = (AdvancedQueryEntity) entity;
				Map<?, ?> m = eAv.getColumnListForAvancedQuery(b.getSessionId());
				List<Object> colum = new ArrayList<>();
				List<Object> tips = new ArrayList<>();
				List<Object> columaux = new ArrayList<>();
				Set setKeys = m.keySet();
				Iterator it = setKeys.iterator();

				while (it.hasNext()) {
					Object c = it.next();
					columaux.add(c);
					colum.add(c);
					tips.add(m.get(c));
				}

				Collections.sort(columaux, new ColumnsComparator(bu));

				// Values to change
				String[] c = new String[colum.size()];
				int[] t = new int[tips.size()];

				for (int i = 0, j = tips.size(); i < j; i++) {
					c[i] = (String) columaux.get(i);
					int a = colum.indexOf(columaux.get(i));
					t[i] = QueryBuilder.getTypeCol((String) tips.get(a));
				}

				h.put("cols", c);
				h.put("types", t);
			}

		} catch (Exception ex) {
			QueryBuilder.logger.error(null, ex);
		}

		return h;
	}

	protected static class ParameterValuesTable extends EJDialog implements ActionListener {

		private ResourceBundle	bundle			= null;

		private List<Object>	parameterList	= null;

		private JButton			bAccept			= null;

		private JButton			bCancel			= null;

		private JTable			table			= null;

		private final String[]	cols;

		private final int[]		types;

		private JTable			t;

		boolean					allOk			= true;

		public boolean isEmptyParameterList() {
			return this.parameterList.isEmpty();
		}

		protected boolean getAllOk() {
			return this.allOk;
		}

		public ParameterValuesTable(Frame o, JTable table, ResourceBundle bundle, String[] cols, int[] types) {
			super(o, ApplicationManager.getTranslation("QueryBuilderParameterValuesTable", bundle), true);
			this.bundle = bundle;
			// this.parameterList = parameterList;
			this.table = table;
			this.cols = cols;
			this.types = types;
			this.init();
		}

		public ParameterValuesTable(Frame o, Expression e, ResourceBundle bundle, String[] cols, int[] types) {
			super(o, ApplicationManager.getTranslation("QueryBuilderParameterValuesTable", bundle), true);
			this.bundle = bundle;
			// this.parameterList = parameterList;
			this.table = new EJTable(bundle);
			this.cols = cols;
			this.types = types;
			((EJTable) this.table).addInitExpression(e);
			this.init();
		}

		public ParameterValuesTable(Dialog o, JTable table, ResourceBundle bundle, String[] cols, int[] types) {
			super(o, ApplicationManager.getTranslation("QueryBuilderParameterValuesTable", bundle), true);
			this.bundle = bundle;
			this.table = table;
			// this.parameterList = parameterList;
			this.cols = cols;
			this.types = types;
			this.init();
		}

		public ParameterValuesTable(Dialog o, Expression e, ResourceBundle bundle, String[] cols, int[] types) {
			super(o, ApplicationManager.getTranslation("QueryBuilderParameterValuesTable", bundle), true);
			this.bundle = bundle;
			this.table = new EJTable(bundle);
			((EJTable) this.table).addInitExpression(e);
			// this.parameterList = parameterList;
			this.cols = cols;
			this.types = types;
			this.init();
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			boolean ok = true;
			this.allOk = true;

			if (e.getActionCommand().equals("accept")) {
				for (Object element : this.parameterList) {
					if (((ParameterField) ((Expression) element).getRightOperand()).getValue() == null) {
						ok = false;
						this.allOk = false;
						break;
					}
				}

				if (!ok) {
					if (MessageDialog.showQuestionMessage(SwingUtilities.getWindowAncestor((Component) e.getSource()), "M_QueryBuilderParameterValuesCamposNulos", this.bundle)) {
						this.setVisible(false);
					}
				} else {
					this.setVisible(false);
				}
			} else {
				this.allOk = false;
				this.setVisible(false);
			}
		}

		public void init() {
			List<Object> le = ((ConditionsTableModel) this.table.getModel()).getExpressionsList();
			this.parameterList = new ArrayList<>();

			for (int i = 0, a = le.size(); i < a; i++) {
				if (((Expression) le.get(i)).getRightOperand() instanceof ParameterField) {
					this.parameterList.add(le.get(i));
				}
			}

			this.t = new pvTable(this.bundle, this.parameterList, this.cols, this.types) {

				@Override
				public Dimension getPreferredScrollableViewportSize() {
					Dimension d = super.getPreferredScrollableViewportSize();
					d.height = this.getRowHeight() * 12;
					return d;
				}
			};

			this.buildView();
		}

		private void buildView() {
			this.bAccept = new com.ontimize.report.ReportDesignerButton(ImageManager.getIcon(ImageManager.OK));
			this.bCancel = new com.ontimize.report.ReportDesignerButton(ImageManager.getIcon(ImageManager.CANCEL));
			this.bAccept.setToolTipText(ApplicationManager.getTranslation("QueryBuilderAceptar", this.bundle));
			this.bCancel.setToolTipText(ApplicationManager.getTranslation("QueryBuilderCancelCons", this.bundle));
			this.bAccept.setText(ApplicationManager.getTranslation("QueryBuilderAceptar", this.bundle));
			this.bCancel.setText(ApplicationManager.getTranslation("QueryBuilderCancelCons", this.bundle));

			this.getContentPane().setLayout(new GridBagLayout());
			JPanel jbButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			jbButtonsPanel.add(this.bAccept);
			jbButtonsPanel.add(this.bCancel);
			this.bAccept.setActionCommand("application.accept");
			this.bAccept.addActionListener(this);

			this.bCancel.setActionCommand("application.cancel");
			this.bCancel.addActionListener(this);

			this.getContentPane().add(new JLabel(ApplicationManager.getTranslation("QueryBuilderParameterValuesTable", this.bundle)),
					new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

			this.getContentPane().add(new JScrollPane(this.t),
					new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

			this.getContentPane().add(jbButtonsPanel, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
		}

	}

	protected static class EJTable extends ETable {

		protected ComboEditor		colEditor			= new ComboEditor(null);

		protected ComboEditor		valueEditor			= new ComboEditor(true, null);

		protected ComboEditor		operationsEditor	= new ComboEditor(null);

		protected TextEditor		textEditor			= new TextEditor();

		protected List<Object>	columns				= null;

		protected int[]				types				= new int[0];

		// protected ExpressionRenderer eR = new ExpressionRenderer();

		protected ResourceBundle	bundle				= null;

		public EJTable() {

		}

		public EJTable(ResourceBundle bundle) {
			this.setModel(new ConditionsTableModel(bundle));
			this.colEditor = new ComboEditor(bundle);
			this.valueEditor = new ComboEditor(true, bundle);
			this.operationsEditor = new ComboEditor(bundle);
			this.bundle = bundle;
		}

		public void setColumns(Object[] cols, int[] types) {
			boolean alguno = false;
			for (int type : types) {
				if (QueryBuilder.isTextType(type)) {
					alguno = true;
					break;
				}
			}

			Object[] colsaux = null;
			if (alguno) {
				colsaux = cols;
			} else {
				colsaux = new Object[cols.length - 1];
				for (int i = 0, a = cols.length - 1; i < a; i++) {
					colsaux[i] = cols[i];
				}
			}
			this.colEditor.setItems(colsaux);
			this.columns = java.util.Arrays.asList(cols);
			this.setTypes(types);
			((ConditionsTableModel) this.getModel()).setColumnTypes(cols, types);
		}

		public void addExpression() {
			((ConditionsTableModel) this.getModel()).addExpression();
		}

		public void addInitExpression(Expression e) {
			this.removeAllExpressions();
			((ConditionsTableModel) this.getModel()).addInitExpression(e);
		}

		public void addExpression(Expression e) {
			((ConditionsTableModel) this.getModel()).addExpression(e);
		}

		protected void setTypes(int[] types) {
			this.types = types;
		}

		public void setOperators(Object[] ops) {
			this.operationsEditor.setItems(ops);
		}

		public void clearExpressionValues() {
			((ConditionsTableModel) this.getModel()).clearExpressionValues();
		}

		@Override
		public TableCellEditor getCellEditor(int row, int c) {
			if (c == 0) {
				return this.colEditor;
			} else if (c == 1) {
				Object v = this.getValueAt(row, 0);
				if ((v == null) || (!(v instanceof BasicField))) {
					return null;
				}
				int i = this.columns.indexOf(((BasicField) v).toString());
				if ((i >= 0) && (i < this.types.length)) {
					this.operationsEditor.setItems(((ConditionsTableModel) this.getModel()).getTypeOperators(this.types[i]));
				}
				return this.operationsEditor;
			} else if (c == 2) {
				if (this.getValueAt(row, 1).toString().equals(ContainsOperator.CONTAINS_OP.toString()) || this.getValueAt(row, 1).toString()
						.equals(ContainsOperator.NOT_CONTAINS_OP.toString())) {
					// return textEditor;
					this.valueEditor = new ComboEditor(true, this.bundle);
					Object[] t = new Object[1];
					t[0] = new ParameterField();
					this.valueEditor.setItems(t);
					return this.valueEditor;
				}

				this.valueEditor = new ComboEditor(true, this.bundle);
				String s = ((BasicField) this.getValueAt(row, 0)).toString();
				int t = this.types[this.columns.indexOf(((BasicField) this.getValueAt(row, 0)).toString())];

				List<Object> l = new ArrayList<>();
				if (t == ConditionsTableModel.BOOLEAN) {
					this.valueEditor = new ComboEditor(false, this.bundle);
					l.add("0");
					l.add("1");
				}

				for (int i = 0, a = this.columns.size(); i < a; i++) {
					if (((String) this.columns.get(i)).equals(s)) {
						continue;
					}
					if (!(BasicOperator.LIKE.equals(this.getValueAt(row, 1).toString())) || (!BasicOperator.NOT_LIKE.equals(this.getValueAt(row, 1).toString()))) {
						if (t != this.types[i]) {
							continue;
						}
					}
					l.add(this.columns.get(i));
				}

				Object[] bt = new Object[l.size() + 1];
				for (int i = 0, a = l.size(); i < a; i++) {
					bt[i] = new BasicField((String) l.get(i));
				}
				bt[l.size()] = new ParameterField();
				this.valueEditor.setItems(bt);
				return this.valueEditor;
			} else {
				return null;
			}
		}

		@Override
		public TableCellRenderer getCellRenderer(int r, int c) {
			if (c == 0) {
				return new CustomTableCellRenderer(this.bundle);
			}
			if (c == 2) {
				return new CustomTableCellRenderer(this.bundle);
			}
			if (c == 3) {
				return new CustomTableCellRendererExpression(this.bundle);
			}

			return super.getCellRenderer(r, c);
		}

		public void orBetweenRows(int[] r) {
			((ConditionsTableModel) this.getModel()).orBetweenRows(r);
		}

		public void orNotBetweenRows(int[] r) {
			((ConditionsTableModel) this.getModel()).orNotBetweenRows(r);
		}

		public void doAndBetweenRows(int[] r) {
			((ConditionsTableModel) this.getModel()).doAndBetweenRows(r);
		}

		public void doNotBetweenRows(int[] r) {
			((ConditionsTableModel) this.getModel()).doAndNotBetweenRows(r);
		}

		public void split(int r) {
			((ConditionsTableModel) this.getModel()).split(r);
		}

		public void removeRows(int[] r) {
			((ConditionsTableModel) this.getModel()).removeRows(r);
		}

		public void removeAllExpressions() {
			((ConditionsTableModel) this.getModel()).removeAllExpressions();
		}

		@Override
		public Expression getExpression(int r) {
			return ((ConditionsTableModel) this.getModel()).getExpression(r);
		}

		public List<Object> getExpressionsList() {
			return ((ConditionsTableModel) this.getModel()).getExpressionsList();
		}

		public boolean expressionOK(int r) {
			return ((ConditionsTableModel) this.getModel()).isExpressionOK(r);
		}

		public boolean expressionOKWithoutParameters(int r) {
			return ((ConditionsTableModel) this.getModel()).isExpressionOKWithoutParameters(r);
		}

		public boolean expressionOKWithoutParameters(Expression e) {
			return ((ConditionsTableModel) this.getModel()).isExpressionOKWithoutParameters(e);
		}

		public boolean needsExpressionParameters(int r) {
			return ((ConditionsTableModel) this.getModel()).needsExpressionParameters(r);
		}

		// public boolean expressionNeedParameters(Expression e){
		// return
		// ((ConditionsTableModel)this.getModel()).expressionNeedParameters(e);
		// }

		@Override
		public boolean expressionOK(Expression e) {
			return ((ConditionsTableModel) this.getModel()).isExpressionOK(e);
		}

		@Override
		public void setValueAt(Object aValue, int row, int column) {
			try {
				((ConditionsTableModel) this.getModel()).setValueAtCustom(aValue, row, column);
			} catch (Exception e) {
				QueryBuilder.logger.error(null, e);
				super.setValueAt(null, row, column);
				if (SwingUtilities.getWindowAncestor(this) instanceof Frame) {
					MessageDialog.showMessage((Frame) SwingUtilities.getWindowAncestor(this), e.getMessage(), JOptionPane.OK_OPTION, this.bundle);
				} else {
					MessageDialog.showMessage((Dialog) SwingUtilities.getWindowAncestor(this), e.getMessage(), JOptionPane.OK_OPTION, this.bundle);
				}
			}
		}

	};

	protected JButton					bOKCons				= new com.ontimize.report.ReportDesignerButton(ImageManager.getIcon(ImageManager.OK));

	protected JButton					bCancelCons			= new com.ontimize.report.ReportDesignerButton(ImageManager.getIcon(ImageManager.CANCEL));

	protected JButton					bStore				= new com.ontimize.report.ReportDesignerButton(ImageManager.getIcon(ImageManager.SAVE_FILE));

	protected JButton					bPreview			= new com.ontimize.report.ReportDesignerButton(ImageManager.getIcon(ImageManager.PREVIEW));

	// Ponemos otro icono para distingir
	protected JButton					bSave				= new com.ontimize.report.ReportDesignerButton(ImageManager.getIcon(ImageManager.SAVE_TABLE_FILTER));

	protected JButton					bLoad				= new com.ontimize.report.ReportDesignerButton(ImageManager.getIcon(ImageManager.OPEN));

	protected JButton					bCols				= new com.ontimize.report.ReportDesignerButton(ImageManager.getIcon(ImageManager.CONF_VISIBLE_COLS));

	protected JButton					bClear				= new com.ontimize.report.ReportDesignerButton(ImageManager.getIcon(ImageManager.RECYCLER));

	protected JButton					bNew				= new com.ontimize.report.ReportDesignerButton(ImageManager.getIcon(ImageManager.NEW_GIF));

	protected JButton					bDelete				= new com.ontimize.report.ReportDesignerButton(ImageManager.getIcon(ImageManager.DELETE_GIF));

	protected JButton					bOR					= new com.ontimize.report.ReportDesignerButton(ImageManager.getIcon(ImageManager.OR));

	protected JButton					bAnd				= new com.ontimize.report.ReportDesignerButton(ImageManager.getIcon(ImageManager.AND));

	protected JButton					bModif				= new com.ontimize.report.ReportDesignerButton(ImageManager.getIcon(ImageManager.MODIF));

	protected JButton					bValues				= new com.ontimize.report.ReportDesignerButton(ImageManager.getIcon(ImageManager.OPTIONS));

	protected JButton					bClearValues		= new com.ontimize.report.ReportDesignerButton(ImageManager.getIcon(ImageManager.DOCUMENT_DELETE));

	protected JButton					bSplit				= new com.ontimize.report.ReportDesignerButton(ImageManager.getIcon(ImageManager.LINK_DELETE));

	protected JButton					bAndN				= new com.ontimize.report.ReportDesignerButton(ImageManager.getIcon(ImageManager.AND_NOT));

	protected JButton					bORN				= new com.ontimize.report.ReportDesignerButton(ImageManager.getIcon(ImageManager.OR_NOT));

	protected JButton					bHelp				= new com.ontimize.report.ReportDesignerButton(ImageManager.getIcon(ImageManager.HELP));

	protected JButton					bHelp2				= new com.ontimize.report.ReportDesignerButton(ImageManager.getIcon(ImageManager.GEAR));

	protected JTextArea					text				= new JTextArea();

	protected JPanel					pButtons			= new JPanel(new FlowLayout(FlowLayout.LEFT));

	protected JPanel					pOKCancel			= new JPanel(new FlowLayout(FlowLayout.CENTER));

	protected boolean					cancelPressed		= false;

	// protected JPanel pHelp = new JPanel();
	protected JPanel					panelQuery			= new JPanel();

	protected EJTable					table				= null;

	protected JScrollPane				js					= null;

	protected JComboBox					comboEntities;

	protected String[]					cols;

	protected boolean[]					queryColumns;

	protected int[]						types;

	protected ResourceBundle			bundle				= null;

	protected SimpleExpressionsDialog	expressionDialog	= null;

	protected EntityReferenceLocator	locator				= null;

	protected String					entity				= null;

	protected List<?>					entityList			= null;

	protected boolean					okCancel			= false;

	protected boolean					showColumns			= false;

	// private static final String DEFAULT_DIRECTORY =
	// System.getProperty("user.home");

	// private static final String SUBDIR_NAME = ".reports" + File.separator +
	// "entities";

	// private static final String EXTENSION = ".conf";

	public String getEntity() {
		return this.entity;
	}

	public String[] getCols() {
		return this.cols;
	}

	public boolean[] getQueryColumns() {
		return this.queryColumns;
	}

	public QueryBuilder(List<?> entityList, ResourceBundle bundle, EntityReferenceLocator locator, boolean okCancel, QueryExpression initQuery, boolean mostrarCols,
			String[] tCols) {
		this.initWithQueryExpression(entityList, bundle, locator, okCancel, initQuery, mostrarCols, tCols);
		this.initMainWindow();
		if (this.table.getRowCount() == 0) {
			this.table.addExpression();
		}
	}

	private void initWithQueryExpression(List<?> entityList, ResourceBundle bundle, EntityReferenceLocator locator, boolean okCancel, QueryExpression initQuery,
			boolean mostrarCols, String[] tCols) {
		if (initQuery == null) {
			this.entityList = entityList;
		}
		this.locator = locator;
		this.bundle = bundle;
		this.okCancel = okCancel;
		this.showColumns = mostrarCols;

		if (this.table == null) {
			this.table = new EJTable(bundle) {

				@Override
				public Dimension getPreferredScrollableViewportSize() {
					Dimension d = super.getPreferredScrollableViewportSize();
					d.height = this.getRowHeight() * 10;
					return d;
				}
			};
		}

		if (initQuery != null) {
			if (tCols == null) {
				this.initVariables(initQuery);
			} else {
				this.initVariables(initQuery, tCols);
			}
		} else if ((entityList != null) && !entityList.isEmpty()) {
			this.initVariables((String) entityList.get(0));
		}

		this.installTableListener();
		this.js = new JScrollPane();

		if (this.table.getRowCount() == 0) {
			this.table.addExpression();
		}
	}

	public QueryBuilder(ResourceBundle bundle, String[] lCols, String[] tCols, Expression initExpression) {
		this.entityList = null;
		this.entity = null;
		this.bundle = bundle;
		this.okCancel = true;
		this.showColumns = true;

		this.table = new EJTable(bundle) {

			@Override
			public Dimension getPreferredScrollableViewportSize() {
				Dimension d = super.getPreferredScrollableViewportSize();
				d.height = this.getRowHeight() * 10;
				return d;
			}
		};

		String[] auxCols = new String[lCols.length + 1];
		this.queryColumns = new boolean[lCols.length + 1];

		for (int i = 0, a = lCols.length; i < a; i++) {
			auxCols[i] = new String(lCols[i]);
			this.queryColumns[i] = true;
		}

		auxCols[lCols.length] = ContainsExtendedSQLConditionValuesProcessor.ANY_COLUMN;
		this.queryColumns[lCols.length] = false;
		this.cols = auxCols;
		java.util.List l = new ArrayList();
		for (String tCol : tCols) {
			l.add(tCol);
		}
		this.types = QueryBuilder.convertType(l);
		this.setColumns(this.cols, this.types);

		if (initExpression != null) {
			this.table.addInitExpression(initExpression);
			this.text.setText(ContainsSQLConditionValuesProcessorHelper.renderQueryConditionsExpressBundle(initExpression, bundle));
		}

		this.bPreview.setEnabled(false);
		// this.bStore.setEnabled(false);

		this.initMainWindow();

		if (this.table.getRowCount() == 0) {
			this.table.addExpression();
		}
	}

	private void setColumnTypes(String[] cols, String entityName, boolean option) {

		try {
			Object entity = this.locator.getEntityReference(entityName);

			if (entity instanceof AdvancedQueryEntity) {
				if (option) {
					this.table.removeAllExpressions();
				}

				AdvancedQueryEntity eAv = (AdvancedQueryEntity) entity;
				Map<?,?> m = eAv.getColumnListForAvancedQuery(this.locator.getSessionId());

				List<Object> tips = new ArrayList<>();
				List<Object> colsTips = new ArrayList<>();
				List<Object> ordenTips = new ArrayList<>();

				Set<?> setKeys = m.keySet();
				Iterator<?> it = setKeys.iterator();
				while (it.hasNext()) {
					Object c = it.next();
					colsTips.add(c);
					tips.add(m.get(c));

				}

				for (String col : cols) {
					if (!col.equals(ContainsExtendedSQLConditionValuesProcessor.ANY_COLUMN)) {
						int k = colsTips.indexOf(col);
						// logger.debug(cols[i]+" k "+k);
						if (k >= 0) {
							ordenTips.add(tips.get(k));
						} else {
							ordenTips.add("String");
						}
					}
				}

				// Values to change
				this.types = new int[tips.size() + 1];
				this.types = QueryBuilder.convertType(ordenTips);
				this.setColumns(cols, this.types);
			}
		} catch (Exception ex) {
			QueryBuilder.logger.error(null, ex);
		}
	}

	private void initVariables(QueryExpression initExpression, String[] tCols) {

		List<Object> v = new ArrayList<Object>();
		v.add(initExpression.getEntity());
		this.entityList = v;
		this.entity = initExpression.getEntity();

		List<?> l = initExpression.getCols();
		List<?> l2 = initExpression.getColumnToQuery();

		if (l != null) {
			this.cols = new String[l.size() + 1];
			this.queryColumns = new boolean[l.size() + 1];

			for (int i = 0, a = l.size(); i < a; i++) {
				this.cols[i] = new String((String) l.get(i));
				this.queryColumns[i] = ((Boolean) l2.get(i)).booleanValue();
			}
			this.cols[l.size()] = ContainsExtendedSQLConditionValuesProcessor.ANY_COLUMN;
			this.queryColumns[l.size()] = false;
		}

		this.types = new int[tCols.length + 1];
		for (int i = 0, a = tCols.length; i < a; i++) {
			this.types[i] = QueryBuilder.getTypeCol(tCols[i]);
		}
		this.types[tCols.length] = ConditionsTableModel.ANY;

		if (QueryBuilder.DEBUG) {
			QueryBuilder.logger.debug("Init Expression  y tCols");
			QueryBuilder.logger.debug("Column  Types");
			for (int i = 0, a = this.cols.length; i < a; i++) {
				QueryBuilder.logger.debug(this.cols[i] + " = " + QueryBuilder.getStringType(this.types[i]));
			}
		}

		this.setColumns(this.cols, this.types);
	}

	private void initVariables(QueryExpression initExpression) {
		List<Object> v = new ArrayList<Object>();
		v.add(initExpression.getEntity());
		this.entityList = v;
		this.entity = initExpression.getEntity();

		List<?> l = initExpression.getCols();
		List<?> lb = initExpression.getCols();
		List<?> l2 = initExpression.getColumnToQuery();

		if (l != null) {
			this.cols = new String[l.size() + 1];
			this.queryColumns = new boolean[l.size() + 1];

			Collections.sort(lb, new ColumnsComparator(this.bundle));

			for (int i = 0, a = l.size(); i < a; i++) {
				this.cols[i] = new String((String) lb.get(i));
				int b = l.indexOf(lb.get(i));
				this.queryColumns[i] = ((Boolean) l2.get(b)).booleanValue();
			}
			this.cols[l.size()] = ContainsExtendedSQLConditionValuesProcessor.ANY_COLUMN;
			this.queryColumns[l.size()] = false;
		}

		this.setColumnTypes(this.cols, this.entity, true);

		if (QueryBuilder.DEBUG) {
			QueryBuilder.logger.debug("Init Expression ");
			QueryBuilder.logger.debug("Column Types");
			for (int i = 0, a = this.cols.length; i < a; i++) {
				QueryBuilder.logger.debug(this.cols[i] + " = " + QueryBuilder.getStringType(this.types[i]));
			}
		}

		if (initExpression.getExpression() != null) {
			this.table.addInitExpression(initExpression.getExpression());
			this.text.setText(ContainsSQLConditionValuesProcessorHelper.renderQueryConditionsExpressBundle(initExpression.getExpression(), this.bundle));
		}
	}

	protected int[] getTypes() {
		return this.types;
	}

	private void initVariables(String entityName) {

		try {
			Entity entity = this.locator.getEntityReference(entityName);

			if ((this.entity == null) || ((this.entity != null) && !this.entity.equals(entityName))) {
				if (entity instanceof AdvancedQueryEntity) {
					this.table.removeAllExpressions();

					this.bPreview.setEnabled(true);
					this.entity = entityName;

					AdvancedQueryEntity eAv = (AdvancedQueryEntity) entity;
					Map<?, ?> m = eAv.getColumnListForAvancedQuery(this.locator.getSessionId());
					List<Object> colum = new ArrayList<>();
					List<Object> tips = new ArrayList<>();
					List<Object> auxCOlumn = new ArrayList<>();
					Set<?> setKeys = m.keySet();
					Iterator<?> it = setKeys.iterator();

					while (it.hasNext()) {
						Object c = it.next();
						auxCOlumn.add(c);
						colum.add(c);
						tips.add(m.get(c));
					}

					Collections.sort(auxCOlumn, new ColumnsComparator(this.bundle));

					// Values to change
					this.cols = new String[colum.size() + 1];
					this.types = new int[tips.size() + 1];
					this.queryColumns = new boolean[tips.size() + 1];

					for (int i = 0, j = tips.size(); i < j; i++) {
						this.cols[i] = (String) auxCOlumn.get(i);
						int a = colum.indexOf(auxCOlumn.get(i));
						this.types[i] = QueryBuilder.getTypeCol((String) tips.get(a));
						this.queryColumns[i] = true;
					}

					this.cols[colum.size()] = ContainsExtendedSQLConditionValuesProcessor.ANY_COLUMN;
					this.types[colum.size()] = ConditionsTableModel.ANY;
					this.queryColumns[colum.size()] = false;

					if (QueryBuilder.DEBUG) {
						QueryBuilder.logger.debug("Entity " + entityName);
						QueryBuilder.logger.debug("Columns    Types");
						for (int i = 0, a = this.cols.length; i < a; i++) {
							QueryBuilder.logger.debug(this.cols[i] + " = " + QueryBuilder.getStringType(this.types[i]));
						}
					}

					this.setColumns(this.cols, this.types);
				}
			}
		} catch (Exception e) {
			QueryBuilder.logger.error(null, e);
		}
	}

	private void initMainWindow() {

		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);

		this.text.setLineWrap(true);
		this.text.setWrapStyleWord(true);
		this.text.setEditable(false);
		this.text.setRows(4);
		this.text.setColumns(50);

		this.setLayout(new GridBagLayout());

		this.bOKCons.setToolTipText(ApplicationManager.getTranslation("QueryBuilderOKCons", this.bundle));
		this.bCancelCons.setToolTipText(ApplicationManager.getTranslation("QueryBuilderCancelCons", this.bundle));

		this.bSave.setToolTipText(ApplicationManager.getTranslation("QueryBuilderSalvar", this.bundle));
		this.bCols.setToolTipText(ApplicationManager.getTranslation("QueryBuilderColumns", this.bundle));
		this.bClear.setToolTipText(ApplicationManager.getTranslation("QueryBuilderBorrarTodo", this.bundle));
		this.bLoad.setToolTipText(ApplicationManager.getTranslation("QueryBuilderCargar", this.bundle));
		this.bNew.setToolTipText(ApplicationManager.getTranslation("QueryBuilderNuevaRestric", this.bundle));
		this.bDelete.setToolTipText(ApplicationManager.getTranslation("QueryBuilderBorrarRestric", this.bundle));
		this.bOR.setToolTipText(ApplicationManager.getTranslation("QueryBuilderOR", this.bundle));
		this.bAnd.setToolTipText(ApplicationManager.getTranslation("QueryBuilderAND", this.bundle));
		this.bSplit.setToolTipText(ApplicationManager.getTranslation("QueryBuilderSPLIT", this.bundle));
		this.bModif.setToolTipText(ApplicationManager.getTranslation("QueryBuilderModifRestric", this.bundle));
		this.bValues.setToolTipText(ApplicationManager.getTranslation("QueryBuilderValues", this.bundle));
		this.bClearValues.setToolTipText(ApplicationManager.getTranslation("QueryBuilderClearValues", this.bundle));
		this.bStore.setToolTipText(ApplicationManager.getTranslation("QueryBuilderSalvarStore", this.bundle));
		this.bPreview.setToolTipText(ApplicationManager.getTranslation("QueryBuilderPreview", this.bundle));
		this.bAndN.setToolTipText(ApplicationManager.getTranslation("QueryBuilderANDNOT", this.bundle));
		this.bORN.setToolTipText(ApplicationManager.getTranslation("QueryBuilderORNOT", this.bundle));
		this.bHelp.setToolTipText(ApplicationManager.getTranslation("QueryBuilderHELP", this.bundle));
		this.bHelp2.setToolTipText(ApplicationManager.getTranslation("QueryBuilderConfig", this.bundle));

		toolbar.add(this.bStore);
		toolbar.add(this.bClear);
		toolbar.addSeparator();

		if (this.basicSave) {
			this.pButtons.add(this.bSave);// Si se ponen cambiar estos
			this.pButtons.add(this.bLoad);
		}

		if (!this.okCancel || (this.okCancel && (this.entityList != null) && (this.entityList.size() > 1))) {
			this.comboEntities = new JComboBox(this.entityList.toArray());
			toolbar.add(this.comboEntities);

			this.comboEntities.addActionListener(e -> {
				if (((JComboBox) e.getSource()).getSelectedItem() != null) {
					QueryBuilder.this.initVariables((String) ((JComboBox) e.getSource()).getSelectedItem());
					if (QueryBuilder.this.table.getRowCount() == 0) {
						QueryBuilder.this.table.addExpression();
					}
				}
			});
		}

		toolbar.add(this.bPreview);

		if (!this.okCancel) {
			toolbar.add(this.bCols);
		} else if (this.showColumns) {
			toolbar.add(this.bCols);
		}
		toolbar.add(this.bValues);
		toolbar.add(this.bClearValues);
		toolbar.addSeparator();

		toolbar.add(this.bNew);
		toolbar.add(this.bDelete);
		toolbar.add(this.bOR);
		toolbar.add(this.bAnd);
		toolbar.add(this.bORN);
		toolbar.add(this.bAndN);
		toolbar.add(this.bSplit);
		toolbar.add(this.bModif);

		toolbar.addSeparator();
		toolbar.add(this.bHelp);
		toolbar.add(this.bHelp2);

		if (this.okCancel) {
			this.bOKCons.setText(ApplicationManager.getTranslation("QueryBuilderRealizarConsA", this.bundle));
			this.pOKCancel.add(this.bOKCons);
			this.bCancelCons.setText(ApplicationManager.getTranslation("QueryBuilderCancelConsA", this.bundle));
			this.pOKCancel.add(this.bCancelCons);
		}

		this.bHelp.addActionListener(e -> {
			int t = -1;
			if (QueryBuilder.this.entityList != null) {
				t = QueryBuilder.this.entityList.size();
			}
			QueryBuilderHelp.show((Component) e.getSource(), QueryBuilder.this.bundle, QueryBuilder.this.okCancel, QueryBuilder.this.showColumns, t);
		});

		this.bHelp2.addActionListener(e -> QueryBuilderShowColsAndTypes.show((Component) e.getSource(), QueryBuilder.this.bundle, QueryBuilder.this.cols, QueryBuilder.this.types,
				QueryBuilder.this.entity));

		this.panelQuery.setLayout(new BorderLayout());
		this.panelQuery.add(new JLabel(ApplicationManager.getTranslation("QueryBuilderConsultaTotal", this.bundle)), BorderLayout.NORTH);
		this.panelQuery.add(new JScrollPane(this.text));

		this.add(toolbar, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 2, 2, 2), 0, 0));

		this.add(this.panelQuery, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
		this.js = new JScrollPane(this.table);
		this.add(this.js, new GridBagConstraints(0, 2, 1, 1, 0, 0.01, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

		if (this.okCancel) {
			this.add(this.pOKCancel, new GridBagConstraints(0, 3, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
		}

		this.installTableListener();
		this.installButtonListeners();
	}

	protected static class ExpTable extends EJTable {

		private String[]			cols;

		protected ComboEditor		colEditor			= new ComboEditor(null);

		protected ComboEditor		operationsEditor	= new ComboEditor(null);

		protected TextEditor		textEditor			= new TextEditor();

		protected List<String>		columns				= null;

		protected int[]				types				= new int[0];

		protected ResourceBundle	bundle				= null;

		public ExpTable(List<Object> expresiones, String[] cols, int[] types, ResourceBundle bundle) {
			this.bundle = bundle;
			((JTable) this).setModel(new ConditionsTableModel(bundle));
			this.setExpressions(expresiones);
			this.setCols(cols);
			this.setTypes(types);
			this.colEditor = new ComboEditor(bundle);
			this.operationsEditor = new ComboEditor(bundle);

			boolean alguno = false;
			for (int type : types) {
				if (QueryBuilder.isTextType(type)) {
					alguno = true;
					break;
				}
			}

			Object[] colsaux = null;
			if (alguno) {
				colsaux = cols;
			} else {
				colsaux = new Object[cols.length - 1];

				for (int i = 0, a = cols.length - 1; i < a; i++) {
					colsaux[i] = cols[i];
				}
			}
			this.colEditor.setItems(colsaux);

			// colEditor.setItems(cols);
			this.columns = Arrays.asList(cols);

			((ConditionsTableModel) this.getModel()).setColumnTypes(cols, types);
		}

		public void setExpressions(List<Object> expresiones) {
			((ConditionsTableModel) ((JTable) this).getModel()).setExpressions(expresiones);
		}

		@Override
		public void setTypes(int[] types) {
			this.types = types;
		}

		public void setCols(String[] cols) {
			this.cols = cols;
		}

		@Override
		public TableCellEditor getCellEditor(int row, int c) {
			if (c == 0) {
				return this.colEditor;
			} else if (c == 1) {
				Object v = this.getValueAt(row, 0);
				if ((v == null) || (!(v instanceof BasicField))) {
					return null;
				}
				int i = this.columns.indexOf(((BasicField) v).toString());
				if ((i >= 0) && (i < this.types.length)) {
					this.operationsEditor.setItems(((ConditionsTableModel) this.getModel()).getTypeOperators(this.types[i]));
				}
				return this.operationsEditor;
			} else if (c == 2) {
				if (this.getValueAt(row, 1).toString().equals(ContainsOperator.CONTAINS_OP.toString()) || this.getValueAt(row, 1).toString()
						.equals(ContainsOperator.NOT_CONTAINS_OP.toString())) {
					return this.textEditor;
				}

				this.valueEditor = new ComboEditor(true, this.bundle);
				String s = ((BasicField) this.getValueAt(row, 0)).toString();
				int t = this.types[this.columns.indexOf(((BasicField) this.getValueAt(row, 0)).toString())];

				List<String> l = new ArrayList<>();
				if (t == ConditionsTableModel.BOOLEAN) {
					this.valueEditor = new ComboEditor(false, this.bundle);
					l.add("0");
					l.add("1");
				}

				for (int i = 0, a = this.columns.size(); i < a; i++) {
					if (((String) this.columns.get(i)).equals(s)) {
						continue;
					}
					if (t != this.types[i]) {
						continue;
					}
					l.add(this.columns.get(i));
				}

				BasicField[] bt = new BasicField[l.size()];
				for (int i = 0, a = l.size(); i < a; i++) {
					bt[i] = new BasicField((String) l.get(i));
				}

				this.valueEditor.setItems(bt);
				return this.valueEditor;
			} else {
				return null;
			}
		}

		@Override
		public TableCellRenderer getCellRenderer(int r, int c) {
			if (c == 0) {
				return new CustomTableCellRenderer(this.bundle);
			}
			if (c == 2) {
				return new CustomTableCellRenderer(this.bundle);
			}
			if (c == 3) {
				return new CustomTableCellRendererExpression(this.bundle);
			} else {
				return super.getCellRenderer(r, c);
			}
		}

		@Override
		public void setValueAt(Object aValue, int row, int column) {
			try {
				((ConditionsTableModel) this.getModel()).setValueAtCustom(aValue, row, column);
			} catch (Exception e) {
				QueryBuilder.logger.error(null, e);
				super.setValueAt(null, row, column);
				if (SwingUtilities.getWindowAncestor(this) instanceof Frame) {
					MessageDialog.showMessage((Frame) SwingUtilities.getWindowAncestor(this), "M_QueryBuilderErrorInsercionEsperaInt", JOptionPane.OK_OPTION, this.bundle);
				} else {
					MessageDialog.showMessage((Dialog) SwingUtilities.getWindowAncestor(this), "M_QueryBuilderErrorInsercionEsperaInt", JOptionPane.OK_OPTION, this.bundle);
				}
			}
		}

	}

	protected static class SimpleExpressionsDialog extends EJDialog implements ActionListener {

		JButton			bAccept			= null;

		ExpTable		expressionTable	= null;

		ResourceBundle	bundle			= null;

		List<Object>	expressionList	= null;

		public SimpleExpressionsDialog(Frame o, List<Object> l, String[] cols, int[] types, JTable table, ResourceBundle bundle) {
			super(o, ApplicationManager.getTranslation("QueryBuilderExpresionesSimplesTitle", bundle), true);
			this.init(l, cols, types, table, bundle);
		}

		public SimpleExpressionsDialog(Dialog o, List<Object> l, String[] cols, int[] types, JTable table, ResourceBundle bundle) {
			super(o, ApplicationManager.getTranslation("QueryBuilderExpresionesSimplesTitle", bundle), true);
			this.init(l, cols, types, table, bundle);
		}

		public boolean checkAllExpressions() {

			boolean ok = true;
			for (Object element : this.expressionList) {
				if (this.expressionTable != null) {
					if (!this.expressionTable.expressionOK((Expression) element)) {
						ok = false;
						break;
					}
				}
			}
			return ok;
		}

		public void showQuestionDialog(Object ex) {

			if (MessageDialog.showQuestionMessage(SwingUtilities.getWindowAncestor(this), "M_QueryBuilder_AlgunaResInvalida", this.bundle)) {
				this.setVisible(false);
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (!this.checkAllExpressions()) {
				this.showQuestionDialog(e);
			} else {
				this.setVisible(false);
			}

		}

		public void init(List<Object> l, String[] cols, int[] types, JTable table, ResourceBundle bundle) {
			this.expressionList = l;
			this.bundle = bundle;
			this.bAccept = new com.ontimize.report.ReportDesignerButton(ImageManager.getIcon(ImageManager.OK));
			this.bAccept.setToolTipText(ApplicationManager.getTranslation("QueryBuilderAccept", bundle));
			this.bAccept.setText(ApplicationManager.getTranslation("QueryBuilderAccept", bundle));

			this.expressionTable = new ExpTable(l, cols, types, bundle);

			this.getContentPane().setLayout(new GridBagLayout());

			JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			buttonsPanel.add(this.bAccept);

			this.bAccept.addActionListener(this);

			this.getContentPane().add(new JLabel(ApplicationManager.getTranslation("QueryBuilderExpSimples", bundle)),
					new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

			this.getContentPane().add(new JScrollPane(this.expressionTable),
					new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

			this.getContentPane().add(buttonsPanel, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
		}

	}

	protected static ParameterValuesTable pvt = null;

	protected static boolean showParameterValuesTable(Component c, ResourceBundle b, JTable t, String[] cols, int[] types) {
		ResourceBundle bundle = b;
		JTable table = t;

		Window w = SwingUtilities.getWindowAncestor(c);
		if ((QueryBuilder.pvt == null) || (QueryBuilder.pvt.getOwner() != w)) {
			if (QueryBuilder.pvt != null) {
				QueryBuilder.pvt.dispose();
			}
			if (w instanceof Frame) {
				QueryBuilder.pvt = new ParameterValuesTable((Frame) w, table, bundle, cols, types);
			} else {
				QueryBuilder.pvt = new ParameterValuesTable((Dialog) w, table, bundle, cols, types);
			}
			QueryBuilder.pvt.pack();
			ApplicationManager.center(QueryBuilder.pvt);
		}
		QueryBuilder.pvt.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (!QueryBuilder.pvt.isEmptyParameterList()) {
			QueryBuilder.pvt.setVisible(true);
			((ConditionsTableModel) table.getModel()).fireTableDataChanged();
		}
		return QueryBuilder.pvt.getAllOk();
	}

	protected static boolean showParameterValuesTable(Component c, ResourceBundle b, Expression e, String[] cols, int[] types) {
		ResourceBundle bundle = b;
		JTable table = new EJTable(b);
		((ConditionsTableModel) table.getModel()).addExpression(e);

		Window w = SwingUtilities.getWindowAncestor(c);
		if (c instanceof Frame) {
			w = (Frame) c;
		}
		if ((QueryBuilder.pvt == null) || (QueryBuilder.pvt.getOwner() != w)) {
			if (QueryBuilder.pvt != null) {
				QueryBuilder.pvt.dispose();
			}
			if (w instanceof Frame) {
				QueryBuilder.pvt = new ParameterValuesTable((Frame) w, table, bundle, cols, types);
			} else {
				QueryBuilder.pvt = new ParameterValuesTable((Dialog) w, table, bundle, cols, types);
			}
			QueryBuilder.pvt.pack();
			ApplicationManager.center(QueryBuilder.pvt);
		}

		QueryBuilder.pvt.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (!QueryBuilder.pvt.isEmptyParameterList()) {
			QueryBuilder.pvt.setVisible(true);
			((ConditionsTableModel) table.getModel()).fireTableDataChanged();
		}
		return QueryBuilder.pvt.getAllOk();
	}

	public static boolean showParameterValues(Component c, ResourceBundle b, Expression e, String[] co, int[] ty) {
		ResourceBundle bundle = b;
		JTable table = new EJTable(b);

		Window w = SwingUtilities.getWindowAncestor(c);
		if (c instanceof Frame) {
			w = (Frame) c;
		}
		if ((QueryBuilder.pvt == null) || (QueryBuilder.pvt.getOwner() != w)) {
			if (QueryBuilder.pvt != null) {
				QueryBuilder.pvt.dispose();
			}
			if (w instanceof Frame) {
				QueryBuilder.pvt = new ParameterValuesTable((Frame) w, table, bundle, co, ty);
			} else {
				QueryBuilder.pvt = new ParameterValuesTable((Dialog) w, table, bundle, co, ty);
			}
			QueryBuilder.pvt.pack();
			ApplicationManager.center(QueryBuilder.pvt);
		}

		QueryBuilder.pvt.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (!QueryBuilder.pvt.isEmptyParameterList()) {
			QueryBuilder.pvt.setVisible(true);
			((ConditionsTableModel) table.getModel()).fireTableDataChanged();
		}

		return QueryBuilder.pvt.getAllOk();
	}

	public static boolean showParameterValuesTable(Component c, ResourceBundle b, Expression e, String entity) {
		Map<Object, Object> ht = QueryBuilder.getColumnTypes(entity, b);

		String[] co = (String[]) ht.get("cols");
		int[] ty = (int[]) ht.get("types");
		if ((co == null) || (ty == null)) {
			return true;
		}
		return QueryBuilder.showParameterValuesTable(c, b, e, co, ty);
	}

	public static boolean showParameterValuesTable(Component c, ResourceBundle b, QueryExpression qe) {

		java.util.List l = qe.getCols();

		String[] co = new String[l.size()];
		int[] ty = new int[l.size()];

		for (int i = 0, a = l.size(); i < a; i++) {
			co[i] = new String((String) l.get(i));
			ty[i] = ConditionsTableModel.VARCHAR;
		}
		return QueryBuilder.showParameterValuesTable(c, b, qe.getExpression(), co, ty);
	}

	protected void simpleAll() {
		Window w = SwingUtilities.getWindowAncestor(this);
		if (w instanceof Frame) {
			this.expressionDialog = new SimpleExpressionsDialog((Frame) w, this.table.getExpressionsList(), this.cols, this.types, this.table, this.bundle);
		} else {
			this.expressionDialog = new SimpleExpressionsDialog((Dialog) w, this.table.getExpressionsList(), this.cols, this.types, this.table, this.bundle);
		}

		this.expressionDialog.pack();

		ApplicationManager.center(this.expressionDialog);
		this.expressionDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.expressionDialog.setVisible(true);
		((ConditionsTableModel) this.table.getModel()).fireTableDataChanged();

	}

	protected void installButtonListeners() {

		this.installSaveButtonListener();

		this.installLoadButtonListener();

		this.installClearButtonListener();

		this.installColumnsButtonListener();

		this.installNewButtonListener();

		this.installDeleteButtonListener();

		this.installOrButtonListener();

		this.installOrNotButtonListener();

		this.installAndButtonListener();

		this.installAndNotButtonListener();

		this.installSplitButtonListener();

		this.installModifButtonListener();

		this.installValuesButtonListener();

		this.installClearValuesButtonListener();

		this.installStoreButtonListener();

		this.installPreviewButtonListener();

		this.installOKConsButtonListener();

		this.installCancelConsButtonListener();
	}

	protected void installCancelConsButtonListener() {
		this.bCancelCons.addActionListener(e -> {
			Window w = SwingUtilities.getWindowAncestor((Component) e.getSource());
			QueryBuilder.this.table.removeAllExpressions();
			QueryBuilder.this.cancelPressed = true;
			w.setVisible(false);
		});
	}

	protected void installOKConsButtonListener() {
		this.bOKCons.addActionListener(e -> {
			Window w = SwingUtilities.getWindowAncestor((Component) e.getSource());

			if (QueryBuilder.this.table.getRowCount() > 0) {
				boolean ok = QueryBuilder.this.table.expressionOKWithoutParameters(0);

				if (!ok) {
					MessageDialog.showErrorMessage(SwingUtilities.getWindowAncestor((Component) e.getSource()),
							ApplicationManager.getTranslation("M_QueryBuilder_FiltroIncorrecto", QueryBuilder.this.bundle));
					return;
				} else {
					w.setVisible(false);
				}
			} else {
				w.setVisible(false);
			}
		});
	}

	protected void installPreviewButtonListener() {
		this.bPreview.addActionListener(e -> {
			Expression query = QueryBuilder.this.table.getExpression(0);

			try {
				Entity en = QueryBuilder.this.locator.getEntityReference(QueryBuilder.this.entity);
				List<Object> attributes = new ArrayList<>();
				for (int i = 0; i < QueryBuilder.this.cols.length; i++) {
					if (QueryBuilder.this.queryColumns[i]) {
						attributes.add(QueryBuilder.this.cols[i]);
					}
				}

				Map<Object, Object> keysValues = new HashMap<>();

				if (query != null) {

					boolean needParameters = QueryBuilder.this.table.needsExpressionParameters(0);
					if (needParameters) {
						Component c1 = (Component) e.getSource();
						if (c1 instanceof Frame) {
							MessageDialog.showMessage((Frame) SwingUtilities.getWindowAncestor((Component) e.getSource()), "M_QueryBuilderQueryWithoutParameter",
									JOptionPane.WARNING_MESSAGE, QueryBuilder.this.bundle);
						} else {
							MessageDialog.showMessage((Dialog) SwingUtilities.getWindowAncestor((Component) e.getSource()), "M_QueryBuilderQueryWithoutParameter",
									JOptionPane.WARNING_MESSAGE, QueryBuilder.this.bundle);
						}
					}

					if (needParameters && !QueryBuilder.showParameterValuesTable((Component) e.getSource(), QueryBuilder.this.bundle, QueryBuilder.this.table,
							QueryBuilder.this.cols, QueryBuilder.this.types)) {
						Component c2 = (Component) e.getSource();
						if (SwingUtilities.getWindowAncestor(c2) instanceof Frame) {
							MessageDialog.showMessage((Frame) SwingUtilities.getWindowAncestor(c2), "M_QueryBuilderQueryWithoutParameter", JOptionPane.OK_OPTION,
									QueryBuilder.this.bundle);
						} else {
							MessageDialog.showMessage((Dialog) SwingUtilities.getWindowAncestor(c2), "M_QueryBuilderQueryWithoutParameter", JOptionPane.OK_OPTION,
									QueryBuilder.this.bundle);
						}
						return;
					}

					boolean okversion = QueryBuilder.this.table.expressionOK(0);
					boolean yes = false;

					if (!okversion) {
						if (MessageDialog.showQuestionMessage(SwingUtilities.getWindowAncestor((Component) e.getSource()), "M_QueryBuilder_invalidFilterInRow",
								QueryBuilder.this.bundle)) {
							yes = true;
						}
					}

					if (okversion) {
						query = ContainsExtendedSQLConditionValuesProcessor.queryToStandard(query, QueryBuilder.this.cols, QueryBuilder.this.types);
						keysValues.put(ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY, query);
					}

					if (okversion || (!okversion && yes)) {
						EntityResult rs1 = en.query(keysValues, attributes, QueryBuilder.this.locator.getSessionId());
						PreviewQuery.show((Component) e.getSource(), rs1, QueryBuilder.this.bundle);

					}
				} else {
					if (MessageDialog.showQuestionMessage(SwingUtilities.getWindowAncestor((Component) e.getSource()), "M_QueryBuilder_sinFiltroEnTabla",
							QueryBuilder.this.bundle)) {

						EntityResult rs2 = en.query(keysValues, attributes, QueryBuilder.this.locator.getSessionId());

						if (rs2.isEmpty() || (rs2.calculateRecordNumber() == 0)) {

							MessageDialog.showInputMessage(SwingUtilities.getWindowAncestor((Component) e.getSource()), "M_QueryBuilder_consSinResult", QueryBuilder.this.bundle);
						} else {
							PreviewQuery.show((Component) e.getSource(), rs2, QueryBuilder.this.bundle);
						}
					}
				}
			} catch (Exception ex) {
				QueryBuilder.logger.error(null, ex);
			}

		});
	}

	protected void installStoreButtonListener() {
		this.bStore.addActionListener(e -> {

			String[] auxCols = new String[QueryBuilder.this.cols.length - 1];
			boolean[] auxColsCons = new boolean[QueryBuilder.this.cols.length - 1];

			for (int i1 = 0, a1 = QueryBuilder.this.cols.length - 1; i1 < a1; i1++) {
				auxCols[i1] = new String(QueryBuilder.this.cols[i1]);
				auxColsCons[i1] = QueryBuilder.this.queryColumns[i1];
			}

			Expression ex = QueryBuilder.this.table.getExpression(0);

			Map<Object, Object> h = com.ontimize.db.query.QueryExpressionSelection.showQueryExpressionSelection((Component) e.getSource(),
					new QueryExpression(QueryBuilder.clearExpression(ex), QueryBuilder.this.entity, auxCols, auxColsCons), QueryBuilder.this.entity, QueryBuilder.this.bundle,
					false, QueryBuilder.this.entity == null);

			QueryExpression queryExpress = (QueryExpression) h.get(QueryExpressionSelection.EXPRESSION);

			if ((queryExpress != null) && (queryExpress.getExpression() != null)) {

				QueryBuilder.this.setExpression(queryExpress.getExpression());

				QueryBuilder.this.cols = new String[queryExpress.getCols().size() + 1];
				QueryBuilder.this.queryColumns = new boolean[queryExpress.getCols().size() + 1];
				if (queryExpress.getEntity() != null) {
					QueryBuilder.this.types = new int[queryExpress.getCols().size() + 1];
				}

				for (int i2 = 0, a2 = queryExpress.getCols().size(); i2 < a2; i2++) {
					QueryBuilder.this.cols[i2] = (String) queryExpress.getCols().get(i2);
					QueryBuilder.this.queryColumns[i2] = (boolean) queryExpress.getQueryColumns().get(i2);
					if (queryExpress.getEntity() != null) {
						QueryBuilder.this.types[i2] = ConditionsTableModel.VARCHAR;
					}
				}
				QueryBuilder.this.cols[queryExpress.getCols().size()] = ContainsExtendedSQLConditionValuesProcessor.ANY_COLUMN;
				QueryBuilder.this.queryColumns[queryExpress.getCols().size()] = false;
				if (queryExpress.getEntity() != null) {
					QueryBuilder.this.types[queryExpress.getCols().size()] = ConditionsTableModel.ANY;
				}

				if (queryExpress.getEntity() != null) {
					QueryBuilder.this.setColumnTypes(QueryBuilder.this.cols, queryExpress.getEntity(), false);
				} else {
					QueryBuilder.this.setColumns(QueryBuilder.this.cols, QueryBuilder.this.types);
				}
			}
		});
	}

	protected void installClearValuesButtonListener() {
		this.bClearValues.addActionListener(e -> QueryBuilder.this.table.clearExpressionValues());
	}

	protected void installValuesButtonListener() {
		this.bValues.addActionListener(e -> QueryBuilder.showParameterValuesTable((Component) e.getSource(), QueryBuilder.this.bundle, QueryBuilder.this.table,
				QueryBuilder.this.cols, QueryBuilder.this.types));
	}

	protected void installModifButtonListener() {
		this.bModif.addActionListener(e -> QueryBuilder.this.simpleAll());
	}

	protected void installSplitButtonListener() {
		this.bSplit.addActionListener(e -> {
			// Selected rows
			int[] rows = QueryBuilder.this.table.getSelectedRows();
			if (rows.length != 1) {
				return;
			}
			QueryBuilder.this.table.split(rows[0]);
		});
	}

	protected void installAndNotButtonListener() {
		this.bAndN.addActionListener(e -> {
			// Selected rows
			int[] rows = QueryBuilder.this.table.getSelectedRows();
			if (rows.length < 2) {
				return;
			}
			QueryBuilder.this.table.doNotBetweenRows(rows);
		});
	}

	protected void installAndButtonListener() {
		this.bAnd.addActionListener(e -> {
			// Selected rows
			int[] rows = QueryBuilder.this.table.getSelectedRows();
			if (rows.length < 2) {
				return;
			}
			QueryBuilder.this.table.doAndBetweenRows(rows);
		});
	}

	protected void installOrNotButtonListener() {
		this.bORN.addActionListener(e -> {
			// Selected rows
			int[] rows = QueryBuilder.this.table.getSelectedRows();
			if (rows.length < 2) {
				return;
			}
			QueryBuilder.this.table.orNotBetweenRows(rows);
		});
	}

	protected void installOrButtonListener() {
		this.bOR.addActionListener(e -> {
			// Selected rows
			int[] rows = QueryBuilder.this.table.getSelectedRows();
			if (rows.length < 2) {
				return;
			}
			QueryBuilder.this.table.orBetweenRows(rows);
		});
	}

	protected void installDeleteButtonListener() {
		this.bDelete.addActionListener(e -> {
			int[] rows = QueryBuilder.this.table.getSelectedRows();
			if (rows.length < 1) {
				return;
			}
			QueryBuilder.this.table.removeRows(rows);
		});
	}

	protected void installNewButtonListener() {
		this.bNew.addActionListener(e -> QueryBuilder.this.table.addExpression());
	}

	protected void installColumnsButtonListener() {
		this.bCols.addActionListener(e -> {

			SelectOutputColumns sCols;

			String[] colsAux = null;
			boolean[] queryColsAux = null;

			List<Object> l = new ArrayList<>();
			List<Object> l2 = new ArrayList<>();

			for (int i1 = 0, a1 = QueryBuilder.this.cols.length; i1 < a1; i1++) {
				if (!QueryBuilder.this.cols[i1].equals(ContainsExtendedSQLConditionValuesProcessor.ANY_COLUMN)) {
					l.add(QueryBuilder.this.cols[i1]);
					l2.add(new Boolean(QueryBuilder.this.queryColumns[i1]));
				}
			}

			colsAux = new String[l.size()];
			queryColsAux = new boolean[l.size()];

			for (int i2 = 0, a2 = l.size(); i2 < a2; i2++) {
				colsAux[i2] = new String((String) l.get(i2));
				queryColsAux[i2] = ((Boolean) l2.get(i2)).booleanValue();
			}

			Window w = SwingUtilities.getWindowAncestor((Component) e.getSource());
			if ((w instanceof Frame) || (w instanceof Dialog)) {
				if (w instanceof Frame) {
					sCols = new SelectOutputColumns((Frame) w, colsAux, queryColsAux, QueryBuilder.this.bundle);
				} else {
					sCols = new SelectOutputColumns((Dialog) w, colsAux, queryColsAux, QueryBuilder.this.bundle);
				}
			} else {
				if (e.getSource() instanceof Frame) {
					sCols = new SelectOutputColumns((Frame) e.getSource(), colsAux, queryColsAux, QueryBuilder.this.bundle);
				} else {
					if (e.getSource() instanceof Dialog) {
						sCols = new SelectOutputColumns((Dialog) e.getSource(), colsAux, queryColsAux, QueryBuilder.this.bundle);
					} else {
						sCols = new SelectOutputColumns(colsAux, queryColsAux, QueryBuilder.this.bundle);
					}
				}
			}

			sCols.pack();
			ApplicationManager.center(sCols);
			boolean[] aux = sCols.showSelectOutputColumns();

			QueryBuilder.this.queryColumns = new boolean[aux.length + 1];

			for (int i3 = 0, a3 = aux.length; i3 < a3; i3++) {
				QueryBuilder.this.queryColumns[i3] = aux[i3];
			}

			QueryBuilder.this.queryColumns[aux.length] = false;
		});
	}

	protected void installClearButtonListener() {
		this.bClear.addActionListener(e -> QueryBuilder.this.table.removeAllExpressions());
	}

	protected void installLoadButtonListener() {
		this.bLoad.addActionListener(e -> QueryBuilder.this.loadQuery());
	}

	protected void installSaveButtonListener() {
		this.bSave.addActionListener(e -> {
			int iRows = QueryBuilder.this.table.getRowCount();
			if (iRows < 1) {
				return;
			}
			QueryBuilder.this.saveQuery();
		});
	}

	protected boolean isCancelPressed() {
		return this.cancelPressed;
	}

	public void setColumns(String[] cols, int[] types) {
		this.table.setColumns(cols, types);
	}

	protected Operator[] getColumnTypeOperators(int type) {
		return null;
	}

	protected void installTableListener() {
		this.table.getModel().addTableModelListener(e -> QueryBuilder.this.text.setText(ContainsSQLConditionValuesProcessorHelper
				.renderQueryConditionsExpressBundle(((ConditionsTableModel) QueryBuilder.this.table.getModel()).getExpression(0), QueryBuilder.this.bundle)));
	}

	private File getFile() {

		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setMultiSelectionEnabled(false);

		int option = fc.showSaveDialog(this);
		if (option == JFileChooser.APPROVE_OPTION) {
			return fc.getSelectedFile();
		}
		return null;

	}

	protected void saveQuery() {

		File f = this.getFile();
		if (f != null) {
			try(ObjectOutputStream bu = new ObjectOutputStream(new FileOutputStream(f))) {
				bu.writeObject(this.table.getExpression(0));
			} catch (Exception e) {
				QueryBuilder.logger.error(null, e);
			}
		}
	}

	protected void loadQuery() {
		File f = this.getFile();
		if (f != null) {
			try (ObjectInputStream bu = new ObjectInputStream(new FileInputStream(f))) {
				Object o = bu.readObject();
				this.table.addExpression((Expression) o);
			} catch (Exception exc) {
				QueryBuilder.logger.debug("abriendo el fichero", exc);
			}
		}
	}

	public Expression getExpression() {

		if (this.table.expressionOKWithoutParameters(this.table.getExpression(0))) {
			return this.table.getExpression(0);
		}
		return null;
	}

	public void setExpression(Expression ex) {
		if (ex == null) {
			return;
		}
		this.table.addInitExpression(ex);
	}

	protected void removeExpression() {
		int rows = this.table.getRowCount();
		int[] f = new int[rows];
		for (int i = 0, a = rows; i < a; i++) {
			f[i] = i;
		}
		this.table.removeRows(f);
	}

	public static int[] convertType(List<Object> type) {
		int[] typeColumns = new int[type.size() + 1];
		for (int i = 0; i < type.size(); i++) {
			typeColumns[i] = QueryBuilder.getTypeCol((String) type.get(i));
		}
		typeColumns[type.size()] = ConditionsTableModel.ANY;
		return typeColumns;
	}

	protected static String getStringType(int i) {
		switch (i) {
			case ConditionsTableModel.VARCHAR:
				return "String";
			case ConditionsTableModel.DATE:
				return "Date";
			case ConditionsTableModel.NUMBER:
				return "Integer";
			case ConditionsTableModel.BOOLEAN:
				return "Boolean";
			default:
				return "unknown";
		}
	}

	public static int getTypeCol(String type) {
		if ("String".equalsIgnoreCase(type)) {
			return ConditionsTableModel.VARCHAR;
		}
		if ("Date".equalsIgnoreCase(type)) {
			return ConditionsTableModel.DATE;
		}
		if ("Number".equalsIgnoreCase(type)) {
			return ConditionsTableModel.NUMBER;
		}
		if ("Integer".equalsIgnoreCase(type)) {
			return ConditionsTableModel.NUMBER;
		}
		if ("Double".equalsIgnoreCase(type)) {
			return ConditionsTableModel.NUMBER;
		}
		if ("Boolean".equalsIgnoreCase(type)) {
			return ConditionsTableModel.BOOLEAN;
		}
		return -1;
	}

	public static boolean isTextType(int i) {
		if (i == ConditionsTableModel.VARCHAR) {
			return true;
		}
		return false;
	}

	public static int[] getAllColsType(String[] col) {
		int[] types = new int[col.length];
		for (int i = 0, a = col.length; i < a; i++) {
			types[i] = QueryBuilder.getTypeCol(col[i]);
		}
		return types;
	}

	public static String[] convertColumns(Object[] cols) {
		String[] columns = new String[cols.length];
		for (int i = 0; i < cols.length; i++) {
			columns[i] = (String) cols[i];
		}
		return columns;
	}

	public static QueryExpression showQueryBuilder(java.awt.Component c, String entity, ResourceBundle bundle, EntityReferenceLocator locator) {
		return QueryBuilder.showQueryBuilder(c, entity, bundle, locator, false);
	}

	public static QueryExpression showQueryBuilder(java.awt.Component c, String entity, ResourceBundle bundle, EntityReferenceLocator locator, boolean okCancel) {
		return QueryBuilder.showQueryBuilder(c, entity, bundle, locator, null, okCancel);
	}

	public static QueryExpression showQueryBuilder(java.awt.Component c, String entity, ResourceBundle bundle, EntityReferenceLocator locator, QueryExpression initQuery) {
		return QueryBuilder.showQueryBuilder(c, entity, bundle, locator, initQuery, false);
	}

	public static QueryExpression showQueryBuilder(java.awt.Component c, String entity, ResourceBundle bundle, EntityReferenceLocator locator, QueryExpression initQuery,
			boolean okCancel) {

		List<Object> v = new ArrayList<>();
		v.add(entity);
		return QueryBuilder.showQueryBuilder(c, v, bundle, locator, initQuery, okCancel, false, false);
	}

	public static QueryExpression showQueryBuilder(java.awt.Component c, String entity, ResourceBundle bundle, EntityReferenceLocator locator, QueryExpression initQuery,
			boolean okCancel, boolean openList) {

		List<Object> v = new ArrayList<>();
		v.add(entity);
		return QueryBuilder.showQueryBuilder(c, v, bundle, locator, initQuery, okCancel, openList, false);
	}

	public static QueryExpression showQueryBuilder(java.awt.Component c, String entity, ResourceBundle bundle, EntityReferenceLocator locator, QueryExpression initQuery,
			boolean okCancel, boolean openList, boolean showCols) {

		List<Object> v = new ArrayList<>();
		v.add(entity);
		return QueryBuilder.showQueryBuilder(c, v, bundle, locator, initQuery, okCancel, openList, showCols);
	}

	public static QueryExpression showQueryBuilder(java.awt.Component c, String entity, ResourceBundle bundle, EntityReferenceLocator locator, QueryExpression initQuery,
			boolean okCancel, boolean openList, boolean showCols, String[] tCols) {

		List<Object> v = new ArrayList<>();
		v.add(entity);
		return QueryBuilder.showQueryBuilder(c, v, bundle, locator, initQuery, okCancel, openList, showCols, tCols);
	}

	public static QueryExpression showQueryBuilder(java.awt.Component c, String entity, ResourceBundle bundle, EntityReferenceLocator locator, QueryExpression initQuery,
			boolean okCancel, boolean openList, boolean mostrarCols, String[] tCols, List<Object> lastName) {

		List<Object> v = new ArrayList<>();
		v.add(entity);
		return QueryBuilder.showQueryBuilder(c, v, bundle, locator, initQuery, okCancel, openList, mostrarCols, tCols, lastName);
	}

	public static QueryExpression showQueryBuilder(java.awt.Component c, String entity, ResourceBundle bundle, EntityReferenceLocator locator, Expression initQuery,
			boolean okCancel, boolean openList) {

		List<Object> v = new ArrayList<>();
		v.add(entity);
		QueryExpression q = new QueryExpression(initQuery, (String) null, (List<?>) null, (List<?>) null);
		return QueryBuilder.showQueryBuilder(c, v, bundle, locator, q, okCancel, openList, false);
	}

	public static QueryExpression showQueryBuilder(java.awt.Component c, List<Object> entities, ResourceBundle bundle, EntityReferenceLocator locator) {
		return QueryBuilder.showQueryBuilder(c, entities, bundle, locator, null, false, false, false);
	}

	private static EJDialog buildDialog(Component c, ResourceBundle bundle) {
		EJDialog dialog = null;

		if (!(c instanceof Frame) && !(c instanceof Dialog)) {
			Window w = SwingUtilities.getWindowAncestor(c);
			if (w instanceof Frame) {
				dialog = new EJDialog((Frame) w, ApplicationManager.getTranslation("QueryBuilderTitle", bundle), true);
			} else if (w instanceof Dialog) {
				dialog = new EJDialog((Dialog) w, ApplicationManager.getTranslation("QueryBuilderTitle", bundle), true);
			}
		} else {
			if (c instanceof Frame) {
				dialog = new EJDialog((Frame) c, ApplicationManager.getTranslation("QueryBuilderTitle", bundle), true);
			}
			if (c instanceof Dialog) {
				dialog = new EJDialog((Dialog) c, ApplicationManager.getTranslation("QueryBuilderTitle", bundle), true);
			}
		}
		return dialog;
	}

	public QueryBuilder() {}

	public static QueryExpression showQueryBuilder(Component c, List<Object> v, ResourceBundle bundle, EntityReferenceLocator locator, QueryExpression initQuery, boolean okCancel,
			boolean openCols, boolean showCols) {
		return QueryBuilder.showQueryBuilder(c, v, bundle, locator, initQuery, okCancel, openCols, showCols, null);
	}

	private static Expression clearExpression(Expression ex) {
		if (ex == null) {
			return ex;
		}
		Expression e = new BasicExpression(ex.getLeftOperand(), ex.getOperator(), ex.getRightOperand());

		if (e.getLeftOperand() instanceof Expression) {
			Expression aux = (Expression) e.getLeftOperand();
			Expression aux2 = (Expression) e.getRightOperand();

			e = new BasicExpression(aux, e.getOperator(), aux2);
			return e;
		} else {
			if (e.getRightOperand() instanceof ParameterField) {
				e.setRightOperand(new ParameterField());
			}
		}

		return e;
	}

	public static QueryExpression showQueryBuilder(Component c, List<Object> v, ResourceBundle bundle, EntityReferenceLocator locator, QueryExpression initQuery, boolean okCancel,
			boolean openList, boolean showCols, String[] tCols) {

		return QueryBuilder.showQueryBuilder(c, v, bundle, locator, initQuery, okCancel, openList, showCols, tCols, new ArrayList<>());
	}

	protected static JDialog		queryBuilderDialog	= null;

	protected static QueryBuilder	qb					= null;

	public static QueryExpression showQueryBuilder(Component c, List<Object> v, ResourceBundle bundle, EntityReferenceLocator locator, QueryExpression initQuery, boolean okCancel,
			boolean openList, boolean showCols, String[] tCols, List<Object> lastName) {

		Map<Object, Object> h = null;

		if (openList) {
			// If returns null => cancel query
			// If returns some not null value but
			// queryExpression.getExpression()
			// is null => Define
			// If returns some not null value but
			// queryExpression.getExpression()
			// != null => returns queryExpression

			if ((initQuery != null) && (initQuery.getEntity() != null)) {
				h = QueryExpressionSelection.showQueryExpressionSelection(c, null, initQuery.getEntity(), bundle, true, false);
			} else {
				if ((v != null) && !v.isEmpty()) {
					h = QueryExpressionSelection.showQueryExpressionSelection(c, null, (String) v.get(0), bundle, true, false);
				} else {
					return null;
				}
			}
			if (h == null) {
				return null;
			}
			if (((Boolean) h.get(QueryExpressionSelection.DEFINE)).booleanValue()) {
				if (h.get(QueryExpressionSelection.EXPRESSION) != null) {
					initQuery = (QueryExpression) h.get(QueryExpressionSelection.EXPRESSION);
				}
			} else {
				QueryExpression qexp = (QueryExpression) h.get(QueryExpressionSelection.EXPRESSION);
				if (qexp == null) {
					return null;
				}

				Map<Object, Object> ht = QueryBuilder.getColumnTypes(qexp.getEntity(), bundle);
				String[] co = (String[]) ht.get("cols");
				int[] ty = (int[]) ht.get("types");
				if ((co == null) || (ty == null)) {
					return null;
				}

				boolean op = QueryBuilder.showParameterValuesTable(c, bundle, qexp.getExpression(), co, ty);
				if (op) {
					if ((lastName != null) && (h.get(QueryExpressionSelection.NAME) != null)) {
						lastName.add(h.get(QueryExpressionSelection.NAME));
					}
					return qexp;
				} else {
					return null;
				}
			}
		}

		QueryBuilder.brute = false;
		if ((QueryBuilder.queryBuilderDialog == null) || (QueryBuilder.queryBuilderDialog.getOwner() != SwingUtilities.getWindowAncestor(c))) {
			if (QueryBuilder.queryBuilderDialog != null) {
				QueryBuilder.queryBuilderDialog.dispose();
			}
			QueryBuilder.queryBuilderDialog = QueryBuilder.buildDialog(c, bundle);

			if (QueryBuilder.queryBuilderDialog != null) {
				QueryBuilder.qb = new QueryBuilder(v, bundle, locator, okCancel, initQuery, showCols, tCols);
				QueryBuilder.queryBuilderDialog.addWindowListener(new WindowListener() {

					@Override
					public void windowClosing(WindowEvent evt) {
						QueryBuilder.brute = true;
					}

					@Override
					public void windowActivated(WindowEvent evt) {}

					@Override
					public void windowClosed(WindowEvent evt) {}

					@Override
					public void windowDeactivated(WindowEvent evt) {}

					@Override
					public void windowDeiconified(WindowEvent evt) {}

					@Override
					public void windowIconified(WindowEvent evt) {}

					@Override
					public void windowOpened(WindowEvent evt) {}
				});

				QueryBuilder.queryBuilderDialog.getContentPane().add(new JScrollPane(QueryBuilder.qb));
				QueryBuilder.queryBuilderDialog.pack();
				ApplicationManager.center(QueryBuilder.queryBuilderDialog);
			}
		} else {
			QueryBuilder.qb.initWithQueryExpression(v, bundle, locator, okCancel, initQuery, showCols, tCols);

		}
		QueryBuilder.queryBuilderDialog.setVisible(true);

		if (QueryBuilder.qb.isCancelPressed()) {
			return null;
		}

		String[] auxCols = new String[QueryBuilder.qb.getCols().length - 1];
		boolean[] auxColsCons = new boolean[QueryBuilder.qb.getCols().length - 1];

		for (int i = 0, a = QueryBuilder.qb.getCols().length - 1; i < a; i++) {
			auxCols[i] = new String(QueryBuilder.qb.getCols()[i]);
			auxColsCons[i] = QueryBuilder.qb.getQueryColumns()[i];
		}

		if (QueryBuilder.brute) {
			return null;
		}

		QueryExpression qexp = new QueryExpression(QueryBuilder.qb.getExpression(), QueryBuilder.qb.getEntity(), auxCols, auxColsCons);

		if (QueryBuilder.qb.getExpression() == null) {
			return qexp;
		}

		Expression e = new BasicExpression(qexp.getExpression().getLeftOperand(), qexp.getExpression().getOperator(), qexp.getExpression().getRightOperand());

		if (h != null) {
			if (h.get(QueryExpressionSelection.NAME) != null) {
				QueryStore store = new FileQueryStore();
				Expression ex = QueryBuilder.clearExpression(e);
				store.addQuery((String) h.get(QueryExpressionSelection.NAME), new QueryExpression(ex, qexp.getEntity(), qexp.getCols(), qexp.getColumnToQuery()));
				lastName.add(h.get(QueryExpressionSelection.NAME));
			} else {
				if (MessageDialog.showQuestionMessage(SwingUtilities.getWindowAncestor(c), "M_QueryBuilderSave", bundle)) {
					Map<Object, Object> hi = QueryExpressionSelection.showQueryExpressionSelection(c, qexp, qexp.getEntity(), bundle, false, true);

					if (hi.get(QueryExpressionSelection.NAME) != null) {
						QueryStore store = new FileQueryStore();
						Expression ex = QueryBuilder.clearExpression(e);
						store.addQuery((String) hi.get(QueryExpressionSelection.NAME), new QueryExpression(ex, qexp.getEntity(), qexp.getCols(), qexp.getColumnToQuery()));
						lastName.add(hi.get(QueryExpressionSelection.NAME));
					}
				}
			}
		}

		boolean needParameters = QueryBuilder.needsExpressionParameters(e);
		if (needParameters) {

			Window w = SwingUtilities.getWindowAncestor(c);
			if (w instanceof Frame) {
				MessageDialog.showMessage((Frame) w, "M_QueryBuilderQueryWithoutParameter", JOptionPane.WARNING_MESSAGE, bundle);
			} else {
				MessageDialog.showMessage((Dialog) w, "M_QueryBuilderQueryWithoutParameter", JOptionPane.WARNING_MESSAGE, bundle);
			}
			return new QueryExpression(e, qexp.getEntity(), qexp.getCols(), qexp.getColumnToQuery());
		}

		return qexp;
	}

	static boolean brute = false;

	public static QueryExpression showQueryBuilder(Component c, ResourceBundle bundle, String[] columns, String[] types, Expression initExpression, List<Object> lastName) {

		// Have not entity
		QueryBuilder.brute = false;

		QueryExpression initQuery = null;
		Map<Object, Object> h = QueryExpressionSelection.showQueryExpressionSelection(c, null, null, bundle, true, false);

		if (h == null) {
			return null;
		}

		if (((Boolean) h.get(QueryExpressionSelection.DEFINE)).booleanValue()) {
			if (h.get(QueryExpressionSelection.EXPRESSION) != null) {
				initQuery = (QueryExpression) h.get(QueryExpressionSelection.EXPRESSION);
			}
		} else {
			QueryExpression qexp = (QueryExpression) h.get(QueryExpressionSelection.EXPRESSION);
			if (qexp == null) {
				return null;
			}

			java.util.List<?> l = qexp.getCols();

			String[] co = new String[l.size()];
			int[] ty = new int[l.size()];

			for (int i = 0, a = l.size(); i < a; i++) {
				co[i] = ApplicationManager.getTranslation((String) l.get(i), bundle);
				ty[i] = ConditionsTableModel.VARCHAR;
			}

			if ((co == null) || (ty == null)) {
				return null;
			}
			boolean op = QueryBuilder.showParameterValuesTable(c, bundle, qexp.getExpression(), co, ty);
			if (op) {
				if ((lastName != null) && (h.get(QueryExpressionSelection.NAME) != null)) {
					lastName.add(h.get(QueryExpressionSelection.NAME));
				}
				return qexp;
			} else {
				return null;
			}
		}

		if ((QueryBuilder.queryBuilderDialog == null) || (QueryBuilder.queryBuilderDialog.getOwner() != SwingUtilities.getWindowAncestor(c))) {
			if (QueryBuilder.queryBuilderDialog != null) {
				QueryBuilder.queryBuilderDialog.dispose();
			}
			QueryBuilder.qb = new QueryBuilder(bundle, columns, types, initExpression);

			QueryBuilder.queryBuilderDialog.addWindowListener(new WindowListener() {

				@Override
				public void windowClosing(WindowEvent evt) {
					QueryBuilder.brute = true;
				}

				@Override
				public void windowActivated(WindowEvent evt) {}

				@Override
				public void windowClosed(WindowEvent evt) {}

				@Override
				public void windowDeactivated(WindowEvent evt) {}

				@Override
				public void windowDeiconified(WindowEvent evt) {}

				@Override
				public void windowIconified(WindowEvent evt) {}

				@Override
				public void windowOpened(WindowEvent evt) {}
			});

			QueryBuilder.queryBuilderDialog.getContentPane().add(new JScrollPane(QueryBuilder.qb));
			QueryBuilder.queryBuilderDialog.pack();
			if (QueryBuilder.queryBuilderDialog != null) {
				ApplicationManager.center(QueryBuilder.queryBuilderDialog);
			}
		}

		QueryBuilder.queryBuilderDialog.setVisible(true);

		if (QueryBuilder.qb.isCancelPressed()) {
			return null;
		}

		String[] auxCols = new String[QueryBuilder.qb.getCols().length - 1];
		boolean[] auxColsCons = new boolean[QueryBuilder.qb.getCols().length - 1];

		for (int i = 0, a = QueryBuilder.qb.getCols().length - 1; i < a; i++) {
			auxCols[i] = new String(QueryBuilder.qb.getCols()[i]);
			auxColsCons[i] = QueryBuilder.qb.getQueryColumns()[i];
		}

		if (QueryBuilder.brute) {
			return null;
		}

		QueryExpression qexp = new QueryExpression(QueryBuilder.qb.getExpression(), QueryBuilder.qb.getEntity(), auxCols, auxColsCons);
		if (QueryBuilder.qb.getExpression() == null) {
			return qexp;
		}

		Expression e = new BasicExpression(qexp.getExpression().getLeftOperand(), qexp.getExpression().getOperator(), qexp.getExpression().getRightOperand());

		if (h != null) {
			if (h.get(QueryExpressionSelection.NAME) != null) {
				QueryStore store = new FileQueryStore();
				Expression ex = QueryBuilder.clearExpression(e);
				store.addQuery((String) h.get(QueryExpressionSelection.NAME), new QueryExpression(ex, qexp.getEntity(), qexp.getCols(), qexp.getColumnToQuery()));
				lastName.add(h.get(QueryExpressionSelection.NAME));
			} else {
				if (MessageDialog.showQuestionMessage(SwingUtilities.getWindowAncestor(c), "M_QueryBuilderSave", bundle)) {
					Map<Object, Object> hi = QueryExpressionSelection.showQueryExpressionSelection(c, qexp, qexp.getEntity(), bundle, false, true);

					if (hi.get(QueryExpressionSelection.NAME) != null) {
						QueryStore store = new FileQueryStore();
						Expression ex = QueryBuilder.clearExpression(e);
						store.addQuery((String) hi.get(QueryExpressionSelection.NAME), new QueryExpression(ex, qexp.getEntity(), qexp.getCols(), qexp.getColumnToQuery()));
						lastName.add(hi.get(QueryExpressionSelection.NAME));
					}
				}
			}
		}

		boolean needParameters = QueryBuilder.needsExpressionParameters(e);
		if (needParameters) {

			Window w = SwingUtilities.getWindowAncestor(c);
			if (w instanceof Frame) {
				MessageDialog.showMessage((Frame) w, "M_QueryBuilderQueryWithoutParameter", JOptionPane.WARNING_MESSAGE, bundle);
			} else {
				MessageDialog.showMessage((Dialog) w, "M_QueryBuilderQueryWithoutParameter", JOptionPane.WARNING_MESSAGE, bundle);
			}

			if (!QueryBuilder.showParameterValuesTable(c, bundle, e, columns, QueryBuilder.getAllColsType(types))) {
				MessageDialog.showErrorMessage(SwingUtilities.getWindowAncestor(c), ApplicationManager.getTranslation("M_QueryBuilderQueryWithoutParameterError", bundle));
				return null;
			}
			return new QueryExpression(e, qexp.getEntity(), qexp.getCols(), qexp.getColumnToQuery());
		}
		return qexp;

	}

}
