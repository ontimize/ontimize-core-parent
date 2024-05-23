package com.ontimize.util.swing.table;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.text.DateFormatSymbols;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.BorderManager;
import com.ontimize.gui.MessageDialog;
import com.ontimize.gui.OperationThread;
import com.ontimize.gui.container.EJDialog;
import com.ontimize.gui.field.document.CurrencyDocument;
import com.ontimize.gui.i18n.Internationalization;
import com.ontimize.gui.images.ImageManager;
import com.ontimize.gui.table.ExtendedTableModel;
import com.ontimize.gui.table.HeadCellRenderer;
import com.ontimize.gui.table.SortTableCellRenderer.CustomHeaderUI;
import com.ontimize.gui.table.Table;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.tools.Pair;
import com.ontimize.util.FileUtils;
import com.ontimize.util.ParseUtils;
import com.ontimize.util.swing.AJDialog;
import com.ontimize.util.xls.XLSExporter;
import com.ontimize.util.xls.XLSExporterFactory;

public class PivotTableUtils extends JPanel {

	private static final Logger						logger								= LoggerFactory.getLogger(PivotTableUtils.class);

	public static final int							SUM									= 0;

	public static final int							AVG									= 1;

	public static final int							MAX									= 2;

	public static final int							MIN									= 3;

	public static final int							COUNT								= 4;

	public static final int							DMY									= 0;

	public static final int							MY									= 1;

	public static final int							QY									= 2;

	public static final int							M									= 3;

	public static final int							Q									= 4;

	public static final int							Y									= 5;

	public static final int							W									= 6;

	public static final String						PIVOTTABLE_FORMAT_STANDARD_STRING	= "pivottable.format.standard";

	public static final String						PIVOTTABLE_FORMAT_PERCENTAGE_STRING	= "pivottable.format.percentage";

	public static final String						PIVOTTABLE_FORMAT_CURRENCY_STRING	= "pivottable.format.currency";

	public static final String						PIVOTTABLE_FORMAT_REAL_STRING		= "pivottable.format.real";

	public static final String						PIVOTTABLE_FORMAT_OPTIONS			= "pivottable.format.options";

	public static final String						PIVOTTABLE_ROWFIELD					= "pivottable.rowfield";

	public static final String						PIVOTTABLE_COLUMNFIELD				= "pivottable.columnfield";

	public static final String						PIVOTTABLE_DATAFIELD				= "pivottable.datafield";

	public static final String						PIVOTTABLE_OPERATION				= "pivottable.operation";

	public static final String						PIVOTTABLE_DATEGROUPOPTIONS			= "pivottable.dategroupoptions";

	public static final String						PIVOTTABLE_TOOLTIP_KEY				= "pivottable.tooltip.key";

	protected static Map<String, RenderFormatKey>	rendererMapKeyFormatRender			= new HashMap<String, RenderFormatKey>();

	static {
		NumberFormat nFReal = NumberFormat.getInstance(ApplicationManager.getLocale());
		nFReal.setMaximumFractionDigits(3);
		NumberFormat nFCurrency = NumberFormat.getInstance(ApplicationManager.getLocale());
		nFCurrency.setMaximumFractionDigits(2);
		NumberFormat nFPercent = NumberFormat.getPercentInstance(ApplicationManager.getLocale());
		nFPercent.setMaximumFractionDigits(0);
		PivotTableUtils.addRenderAndFormat(PivotTableUtils.PIVOTTABLE_FORMAT_PERCENTAGE_STRING, nFPercent, NumberRenderer.class);
		PivotTableUtils.addRenderAndFormat(PivotTableUtils.PIVOTTABLE_FORMAT_CURRENCY_STRING, nFCurrency, NumberRenderer.class);
		PivotTableUtils.addRenderAndFormat(PivotTableUtils.PIVOTTABLE_FORMAT_REAL_STRING, nFReal, NumberRenderer.class);
	}

	public static void addRenderAndFormat(String key, NumberFormat format, Class renderer) {
		if ((key != null) && (format != null)) {
			PivotTableUtils.rendererMapKeyFormatRender.put(key, new RenderFormatKey(key, format, renderer));
		}
	}

	public static NumberRenderer getRenderInstance(String key, Object[] constructorValues) {

		if (!PivotTableUtils.rendererMapKeyFormatRender.containsKey(key)) {
			NumberRenderer nR = new NumberRenderer();
			return nR;
		} else {
			RenderFormatKey actualKey = PivotTableUtils.rendererMapKeyFormatRender.get(key);
			Class c = actualKey.getRenderer();
			try {

				Class[] classArray = new Class[constructorValues.length];
				for (int i = 0; i < constructorValues.length; i++) {
					classArray[i] = constructorValues[i].getClass();
				}

				Constructor myc = c.getConstructor(classArray);

				NumberRenderer nR = (NumberRenderer) myc.newInstance(constructorValues);
				nR.setNumberFormat(actualKey.getFormat());
				nR.setDataFormat(actualKey.getKey());

				return nR;

			} catch (Exception e) {
				PivotTableUtils.logger.error("Error retrieving a custom render class. Retrieving a NumberRenderer() instance", e);
				return new NumberRenderer();
			}
		}
	}

	protected static class GroupValue implements Comparable {

		private String[]		columns		= null;

		private Object[]		values		= null;

		private Comparator[]	comparators	= null;

		public GroupValue(String[] columns, Object[] values, Comparator[] comparators) {
			this.columns = columns;
			this.values = values;
			this.comparators = comparators;

			if (this.columns.length != this.values.length) {
				throw new IllegalArgumentException("different lenght");
			}
		}

		protected boolean sameColumns(Object o) {
			GroupValue other = (GroupValue) o;
			if (other.columns.length == this.columns.length) {
				for (int i = 0; i < this.columns.length; i++) {
					String c1 = this.columns[i];
					String c2 = other.columns[i];
					if (((c1 != null) && (c2 == null)) || ((c1 == null) && (c2 != null))) {
						return false;
					}
					if (!c1.equals(c2)) {
						return false;
					}
				}
				return true;
			}
			return false;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (o instanceof GroupValue) {
				GroupValue other = (GroupValue) o;
				if (other.columns.length == this.columns.length) {
					if (this.values.length == other.values.length) {
						for (int i = 0; i < this.columns.length; i++) {
							String c1 = this.columns[i];
							String c2 = other.columns[i];
							Object o1 = this.values[i];
							Object o2 = other.values[i];
							if (((o1 != null) && (o2 == null)) || ((o1 == null) && (o2 != null))) {
								return false;
							}
							if (((c1 != null) && (c2 == null)) || ((c1 == null) && (c2 != null))) {
								return false;
							}
							if (!c1.equals(c2)) {
								return false;
							}
							if ((o1 == null) && (o2 == null)) {
								return true;
							}
							if (!o1.equals(o2)) {
								return false;
							}
						}
						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			}
			return false;
		}

		@Override
		public int hashCode() {
			return super.hashCode();
		}

		public int index(Object o, Object[] array) {
			for (int i = 0; i < array.length; i++) {
				if ((o == null) && (array[i] == null)) {
					return i;
				}
				if ((o != null) && (array[i] != null) && o.equals(array[i])) {
					return i;
				}
			}
			return -1;
		}

		@Override
		public int compareTo(Object o) {
			if (o == this) {
				return 0;
			}
			if (this.equals(o)) {
				return 0;
			}
			if (o instanceof GroupValue) {
				GroupValue other = (GroupValue) o;
				if (other.columns.length != this.columns.length) {
					return 0;
				}
				if (!this.sameColumns(o)) {
					return 0;
				}
				int acum = 0;
				for (int i = 0; i < this.columns.length; i++) {
					// String c1 = columns[i];
					// String c2 = other.columns[i];
					Object o1 = this.values[i];
					Object o2 = other.values[i];
					if (this.comparators[i] != null) {
						int aux = this.comparators[i].compare(o1, o2);
						if (aux != 0) {
							return aux;
						}
					} else {
						if ((o1 == null) && (o2 == null)) {
							continue;
						}
						if ((o1 != null) && (o2 == null)) {
							return 1;
						}
						if ((o1 == null) && (o2 != null)) {
							return -1;
						}
						if ((o1 instanceof Comparable) && (o2 instanceof Comparable)) {
							int aux = ((Comparable) o1).compareTo(o2);
							if (aux != 0) {
								return aux;
							}
						}
					}
				}
				return acum;
			}
			return 0;
		}

	}

	public static class DateGroupTableModel extends AbstractTableModel {

		private TableModel		innerModel	= null;

		private int[]			columns		= new int[0];

		private int				group		= -1;

		private final Calendar	calendar	= Calendar.getInstance();

		public DateGroupTableModel(TableModel m, int[] columns, int group) {
			this.innerModel = m;
			this.columns = columns;
			this.group = group;
		}

		public TableModel getInnerModel() {
			return this.innerModel;
		}

		@Override
		public int getColumnCount() {
			return this.innerModel.getColumnCount();
		}

		@Override
		public String getColumnName(int i) {
			return this.innerModel.getColumnName(i);
		}

		public int getColumnIndex(String name) {
			if (name != null) {
				for (int i = 0; i < this.innerModel.getColumnCount(); i++) {
					if (name.equals(this.innerModel.getColumnName(i))) {
						return i;
					}
				}
			}
			return -1;
		}

		@Override
		public int getRowCount() {
			return this.innerModel.getRowCount();
		}

		public boolean containsColumn(int col) {
			for (int i = 0; i < this.columns.length; i++) {
				if (col == this.columns[i]) {
					return true;
				}
			}
			return false;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (this.containsColumn(columnIndex)) {
				Object v = this.innerModel.getValueAt(rowIndex, columnIndex);
				if (v instanceof Date) {
					switch (this.group) {
						case DMY:
							this.calendar.setTime((Date) v);
							return new DayMonthYear(this.calendar.get(Calendar.DAY_OF_MONTH), this.calendar.get(Calendar.MONTH), this.calendar.get(Calendar.YEAR),
									this.calendar.getTimeInMillis());
						case MY:
							this.calendar.setTime((Date) v);
							return new MonthYear(this.calendar.get(Calendar.MONTH), this.calendar.get(Calendar.YEAR), this.calendar.getTimeInMillis());
						case QY:
							this.calendar.setTime((Date) v);
							return new QuarterYear(this.calendar.get(Calendar.MONTH), this.calendar.get(Calendar.YEAR), this.calendar.getTimeInMillis());
						case M:
							this.calendar.setTime((Date) v);
							return new Month(this.calendar.get(Calendar.MONTH), this.calendar.getTimeInMillis());
						case Q:
							this.calendar.setTime((Date) v);
							return new Quarter(this.calendar.get(Calendar.MONTH), this.calendar.getTimeInMillis());
						case Y:
							this.calendar.setTime((Date) v);
							return new Year(this.calendar.get(Calendar.YEAR), this.calendar.getTimeInMillis());
						case W:
							this.calendar.setTime((Date) v);
							return new WeekYear(this.calendar.get(Calendar.DAY_OF_MONTH), this.calendar.get(Calendar.MONTH), this.calendar.get(Calendar.YEAR),
									this.calendar.getTimeInMillis());
						default:
							return v;
					}
				} else {
					return v;
				}
			}
			return this.innerModel.getValueAt(rowIndex, columnIndex);
		}

	}

	protected static class DayMonthYear implements Comparable {

		protected String	rep		= null;

		protected int		d		= 0;

		protected int		m		= 0;

		protected int		y		= 0;

		protected int		t		= 0;

		protected int		w		= 0;

		protected long		time	= 0;

		public DayMonthYear(int d, int m, int y, long time) {
			this.d = d;
			this.m = m + 1;
			this.y = y;
			this.time = time;
		}

		public long getTime() {
			return this.time;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			} else if (o instanceof DayMonthYear) {
				DayMonthYear dmy = (DayMonthYear) o;
				return (this.d == dmy.d) && (this.m == dmy.m) && (this.y == dmy.y) && (this.t == dmy.t) && (this.w == dmy.w);
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return super.hashCode();
		}

		@Override
		public String toString() {
			if (this.rep == null) {
				this.rep = (this.d > 9 ? "" + this.d : "0" + this.d) + "/" + (this.m > 9 ? "" + this.m : "0" + this.m) + "/" + this.y;
			}
			return this.rep;
		}

		@Override
		public int compareTo(Object o) {
			if (o == null) {
				throw new NullPointerException();
			}
			if (this.equals(o)) {
				return 0;
			}
			if (o instanceof DayMonthYear) {
				DayMonthYear dmY = (DayMonthYear) o;
				if (this.y > dmY.y) {
					return 1;
				} else if (this.y < dmY.y) {
					return -1;
				}

				if (this.m > dmY.m) {
					return 1;
				} else if (this.m < dmY.m) {
					return -1;
				}

				if (this.d > dmY.d) {
					return 1;
				} else if (this.d < dmY.d) {
					return -1;
				}

				if (this.t > dmY.t) {
					return 1;
				} else if (this.t < dmY.t) {
					return -1;
				}

				if (this.w > dmY.w) {
					return 1;
				} else if (this.w < dmY.w) {
					return -1;
				}

				return 0;
			} else {
				throw new ClassCastException(this.getClass().getName() + " Object type is different that DayMonthYear");
			}
		}

	}

	protected static class WeekYear extends DayMonthYear {

		public WeekYear(int d, int m, int y, long time) {
			super(-1, -1, y, time);
			Calendar cal = Calendar.getInstance();
			cal.set(y, m, d, 0, 0, 0);
			this.w = cal.get(Calendar.WEEK_OF_YEAR);
			if ((m == 0) && (this.w > 10)) {
				this.y = y - 1;
			}
		}

		@Override
		public String toString() {
			if (this.rep == null) {
				this.rep = (this.w > 9 ? "" + this.w : "0" + this.w) + " - " + this.y;
			}
			return this.rep;
		}

	}

	protected static class MonthYear extends DayMonthYear {

		public MonthYear(int m, int y, long time) {
			super(-1, m, y, time);
		}

		@Override
		public String toString() {
			if (this.rep == null) {
				this.rep = (this.m > 9 ? "" + this.m : "0" + this.m) + "/" + this.y;
			}
			return this.rep;
		}

	}

	protected static class Year extends MonthYear {

		public Year(int y, long time) {
			super(-1, y, time);
		}

		@Override
		public String toString() {
			if (this.rep == null) {
				this.rep = "" + this.y;
			}
			return this.rep;
		}

	}

	protected static class Month extends MonthYear {

		static DateFormatSymbols df = new DateFormatSymbols();

		public Month(int m, long time) {
			super(m, -1, time);
		}

		@Override
		public String toString() {
			if (this.rep == null) {
				if (this.m > 0) {
					this.rep = Month.df.getMonths()[this.m - 1];
				}
			}
			return this.rep;
		}

	}

	protected static class QuarterYear extends MonthYear {

		public QuarterYear(int m, int y, long time) {
			super(-1, y, time);
			switch (m) {
				case 0:
				case 1:
				case 2:
					this.t = 1;
					break;
				case 3:
				case 4:
				case 5:
					this.t = 2;
					break;
				case 6:
				case 7:
				case 8:
					this.t = 3;
					break;
				case 9:
				case 10:
				case 11:
					this.t = 4;
					break;
			}
		}

		@Override
		public String toString() {
			if (this.rep == null) {
				switch (this.t) {
					case 1:
						this.rep = "T1/" + this.y;
						break;
					case 2:
						this.rep = "T2/" + this.y;
						break;
					case 3:
						this.rep = "T3/" + this.y;
						break;
					case 4:
						this.rep = "T4/" + this.y;
						break;
				}
			}
			return this.rep;
		}

	}

	protected static class Quarter extends QuarterYear {

		public Quarter(int m, long time) {
			super(m, -1, time);
			switch (m) {
				case 0:
				case 1:
				case 2:
					this.t = 1;
					break;
				case 3:
				case 4:
				case 5:
					this.t = 2;
					break;
				case 6:
				case 7:
				case 8:
					this.t = 3;
					break;
				case 9:
				case 10:
				case 11:
					this.t = 4;
					break;
			}
		}

		@Override
		public String toString() {
			if (this.rep == null) {
				switch (this.t) {
					case 1:
						this.rep = "T1";
						break;
					case 2:
						this.rep = "T2";
						break;
					case 3:
						this.rep = "T3";
						break;
					case 4:
						this.rep = "T4";
						break;
				}
			}
			return this.rep;
		}

	}

	public static class Value {

		double	max		= -Double.MAX_VALUE;

		double	min		= Double.MAX_VALUE;

		double	sum		= 0.0;

		int		count	= 0;

		boolean	isNull	= true;

		Number	maxN	= null;

		Number	minN	= null;

		Number	countN	= null;

		Number	sumN	= null;

		Number	avgN	= null;

		public Value() {}

		public void add(double d) {
			this.isNull = false;
			this.sum = this.sum + d;
			this.count++;
			if (d > this.max) {
				this.max = d;
			}
			if (d < this.min) {
				this.min = d;
			}
			this.maxN = null;
			this.minN = null;
			this.sumN = null;
			this.countN = null;
			this.avgN = null;
		}

		public void count(boolean numeric) {
			this.count++;
			// Por seguridad...
			if (!numeric) {
				this.maxN = null;
				this.minN = null;
				this.sumN = null;
				this.countN = null;
				this.avgN = null;
			}
		}

		public void count() {
			this.count(false);
		}

		public int getCount() {
			return this.count;
		}

		public double getAvg() {
			return this.sum / this.count;
		}

		public double getSum() {
			return this.sum;
		}

		public double getMax() {
			return this.max;
		}

		public double getMin() {
			return this.min;
		}

		public Number getCountN() {
			if (this.countN == null) {
				this.countN = new Integer(this.count);
			}
			return this.countN;
		}

		public Number getSumN() {
			if (this.sumN == null) {
				this.sumN = new Double(this.sum);
			}
			return this.sumN;
		}

		public Number getAvgN() {
			if (this.avgN == null) {
				this.avgN = new Double(this.sum / this.count);
			}
			return this.avgN;
		}

		public Number getMaxN() {
			if (this.maxN == null) {
				this.maxN = new Double(this.max);
			}
			return this.maxN;
		}

		public Number getMinN() {
			if (this.minN == null) {
				this.minN = new Double(this.min);
			}
			return this.minN;
		}

	}

	public static class TranslateListRenderer extends DefaultListCellRenderer {

		ResourceBundle bundle = null;

		public TranslateListRenderer(ResourceBundle bundle) {
			this.setOpaque(true);
			this.bundle = bundle;
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if ((comp instanceof JLabel) && (value != null)) {
				this.setText(PivotTableUtils.translate(value.toString(), this.bundle, null));
				((JLabel) comp).setToolTipText(ApplicationManager.getTranslation(PivotTableUtils.PIVOTTABLE_TOOLTIP_KEY, this.bundle));
			}
			return comp;
		}

		@Override
		public Dimension getPreferredSize() {
			Dimension d = super.getPreferredSize();
			if ("".equals(this.getText())) {
				d.height = Math.max(d.height, this.getFontMetrics(this.getFont()).getHeight());
			}
			return d;
		}

	}

	public static class LeftLineBorder extends AbstractBorder {

		protected Insets insets = new Insets(0, 1, 0, 0);

		@Override
		public Insets getBorderInsets(Component c) {
			return this.insets;
		}

		@Override
		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			g.setColor(Color.black);
			g.drawLine(x, y, x, height - 1);
		}

	}

	public static class RowHeaderCustomLineBorder extends CustomLineBorder {

		protected Insets insets = new Insets(0, 1, 1, 0);

		@Override
		public Insets getBorderInsets(Component c) {
			return this.insets;
		}

		@Override
		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			g.setColor(Color.black);
			g.drawLine(x, height - 1, width - 1, height - 1);
			g.drawLine(x, y, x, height - 1);
		}

	}

	public static class RowHeaderRenderer extends DefaultTableCellRenderer {

		private boolean			remarkLastRow	= true;

		public static boolean	remarkRows		= true;

		private final Border	b				= new CustomLineBorder();

		private final Border	b1				= new RowHeaderCustomLineBorder();

		private final Border	bLeft			= new LeftLineBorder();

		public RowHeaderRenderer(boolean remarkLastRow) {
			this.remarkLastRow = remarkLastRow;
		}

		public RowHeaderRenderer() {}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			((JComponent) c).setOpaque(true);
			if ((row + 1) < table.getRowCount()) {
				Object vr1 = table.getValueAt(row + 1, column);
				if (value == vr1) {
					if (column == 0) {
						((JComponent) c).setBorder(this.bLeft);
					}
				} else {
					if ((value != null) && (vr1 != null) && vr1.equals(value)) {
						if (column == 0) {
							((JComponent) c).setBorder(this.bLeft);
						}
					} else {
						if (column == 0) {
							((JComponent) c).setBorder(this.b1);
						} else {
							((JComponent) c).setBorder(this.b);
						}
					}
				}
			} else {
				if (column == 0) {
					((JComponent) c).setBorder(this.b1);
				} else {
					((JComponent) c).setBorder(this.b);
				}
			}
			if (row > 0) {
				Object vant = table.getValueAt(row - 1, column);
				boolean bEquals = ((vant == null) && (value == null)) || ((vant != null) && (value != null) && vant.equals(value));
				if (bEquals) {
					if (c instanceof JLabel) {
						((JLabel) c).setText("");
					}
				}
			}
			if (c instanceof JLabel) {
				((JLabel) c).setToolTipText(ApplicationManager.getTranslation(PivotTableUtils.PIVOTTABLE_TOOLTIP_KEY));
			}
			if (table != null) {
				c.setForeground(table.getTableHeader().getForeground());
				c.setBackground(table.getTableHeader().getBackground());
				if (RowHeaderRenderer.remarkRows) {
					c.setFont(table.getFont().deriveFont(Font.BOLD));
				} else {
					c.setFont(table.getFont());
				}
			} else {
				c.setFont(Font.getFont("Arial"));
			}
			if ((row == (table.getRowCount() - 1)) && this.remarkLastRow) {
				c.setForeground(Color.red);
				c.setFont(c.getFont().deriveFont(Font.BOLD));
			}
			return c;
		}

	}

	public static class MainHeaderRenderer extends HeaderRenderer {

		public MainHeaderRenderer(ResourceBundle bundle) {
			super(bundle);
		}

		public MainHeaderRenderer(ResourceBundle bundle, Map<Object, Object> params) {
			super(bundle, params);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (!this.translate(value) && (comp instanceof JLabel)) {
				((JLabel) comp).setText(value.toString());
			} else {
				((JLabel) comp).getText();
			}
			return comp;
		}

		public boolean translate(Object value) {
			if ("table.total".equals(value)) {
				return true;
			}
			if ("pivottable.unknown.colitem".equals(value)) {
				return true;
			}
			return false;
		}

	}

	public static class HeaderRenderer extends HeadCellRenderer implements Internationalization {

		/**
		 * Vertical margin to use in the table header
		 */
		public static int		defaultVerticalHeaderMargin	= 4;

		private final Border	border						= new LineBorder(Color.black);

		protected int			minHeaderHeight;

		protected int			verticalHeaderMargin		= HeaderRenderer.defaultVerticalHeaderMargin;

		protected JTable		currentTable;

		protected Font			predFont					= null;

		protected Color			groupBackgroundColor		= new Color(201, 203, 235);

		protected Color			backgroundColor				= null;

		protected Color			foregroundColor;

		protected Color			foregroundFilterColor;

		protected ImageIcon		bgImage;

		protected Image			bgCurrentImage;

		protected Border		defaultBorder;

		protected Border		lastColumnBorder;

		protected Border		firstColumnBorder;

		private ResourceBundle	bundle						= null;

		protected boolean		percentage					= false;

		public boolean isPercentage() {
			return this.percentage;
		}

		public void setPercentage(boolean percentage) {
			this.percentage = percentage;
		}

		public HeaderRenderer() {}

		public HeaderRenderer(ResourceBundle bundle) {
			this.bundle = bundle;
		}

		public HeaderRenderer(ResourceBundle bundle, Map<Object, Object> params) {
			this(bundle);
			this.init(params);
		}

		protected void init(Map<Object, Object> parameters) {
			if (parameters != null) {
				this.minHeaderHeight = ParseUtils.getInteger((String) parameters.get("headerheight"), this.minHeaderHeight);
				this.predFont = ParseUtils.getFont((String) parameters.get("headerfont"), this.predFont);
				this.setFont(this.predFont);
				this.foregroundColor = ParseUtils.getColor((String) parameters.get("headerfg"), this.foregroundColor);
				this.backgroundColor = ParseUtils.getColor((String) parameters.get("headerbg"), this.backgroundColor);
				Color shadowColor = ParseUtils.getColor((String) parameters.get("fontshadowcolor"), null);
				if (shadowColor != null) {
					this.setUI(new CustomHeaderUI(shadowColor));
				}

				this.bgImage = ParseUtils.getImageIcon((String) parameters.get("headerbgimage"), null);
				this.defaultBorder = ParseUtils.getBorder((String) parameters.get("headerborder"), null);
				this.lastColumnBorder = ParseUtils.getBorder((String) parameters.get("headerlastcolumnborder"), this.defaultBorder);
				this.firstColumnBorder = ParseUtils.getBorder((String) parameters.get("headerfirstcolumnborder"), this.defaultBorder);

				this.percentage = ParseUtils.getBoolean((String) parameters.get("percentage"), false);
			}
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			// ((JComponent) c).setOpaque(true);
			// ((JComponent) c).setBorder(border);
			// if (table != null) {
			// c.setForeground(table.getTableHeader().getForeground());
			// c.setBackground(table.getTableHeader().getBackground());
			// c.setFont(table.getFont());
			// } else
			// c.setFont(Font.getFont("Arial"));

			value = this.getRenderedValue(table, value, isSelected, hasFocus, row, column);

			this.currentTable = table;
			if (this.bgImage == null) {
				if (this.backgroundColor != null) {
					c.setBackground(this.backgroundColor);
				} else {
					if (table != null) {
						table.getTableHeader().getBackground();
					}
				}
			} else {
				this.bgCurrentImage = this.bgImage.getImage();
			}
			if (this.foregroundColor != null) {
				c.setForeground(this.foregroundColor);
			} else {
				if (table != null) {
					c.setForeground(table.getTableHeader().getForeground());
				}
			}
			if (this.predFont != null) {
				c.setFont(this.predFont);
			} else {
				if (table != null) {
					c.setFont(table.getTableHeader().getFont());
				}
			}
			if ((value != null) && (value instanceof String) && (c instanceof JLabel)) {
				((JLabel) c).setText(ApplicationManager.getTranslation(value.toString(), this.bundle));
			}

			if ((this.defaultBorder != null) && (c != null) && (c instanceof JComponent)) {
				if (column == this.getLastColumnIndex()) {
					((JComponent) c).setBorder(this.lastColumnBorder);
				} else if (column == this.getFirstColumnIndex()) {
					((JComponent) c).setBorder(this.firstColumnBorder);
				} else {
					((JComponent) c).setBorder(this.defaultBorder);
				}
			}
			return c;
		}

		public Object getRenderedValue(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if ("table.total".equals(value)) {
				if (this.isPercentage()) {
					value = "table.total.percentage";
				}
			}
			return value;
		}

		protected int getLastColumnIndex() {
			int columnCount = this.currentTable.getColumnCount();
			int lastColumnIndex = -1;
			for (int i = 0; i < columnCount; i++) {
				int width = this.currentTable.getColumnModel().getColumn(i).getWidth();
				if (width > 0) {
					lastColumnIndex = i;
				}
			}
			return lastColumnIndex;
		}

		protected int getFirstColumnIndex() {
			int columnCount = this.currentTable.getColumnCount();
			for (int i = 0; i < columnCount; i++) {
				int width = this.currentTable.getColumnModel().getColumn(i).getWidth();
				if (width > 0) {
					return i;
				}
			}
			return 0;
		}

		@Override
		public Dimension getPreferredSize() {
			Dimension d = super.getPreferredSize();
			int height = Math.max(this.minHeaderHeight, d.height + this.verticalHeaderMargin);
			return new Dimension(d.width + 10, height);
		}

		@Override
		public void paintComponent(Graphics g) {
			if (this.bgCurrentImage != null) {
				g.drawImage(this.bgCurrentImage, 0, 0, this.getSize().width, this.getSize().height, this);
			}
			super.paintComponent(g);
		}

		@Override
		public void setResourceBundle(ResourceBundle bundle) {
			this.bundle = bundle;
		}

		@Override
		public void setComponentLocale(Locale l) {

		}

		@Override
		public List<String> getTextsToTranslate() {
			return null;
		}

	}

	private static class CustomLineBorder extends AbstractBorder {

		protected Insets insets = new Insets(0, 0, 1, 0);

		@Override
		public Insets getBorderInsets(Component c) {
			return this.insets;
		}

		@Override
		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			g.setColor(Color.black);
			g.drawLine(x, height - 1, width - 1, height - 1);
		}

	}

	public static class NumberRenderer extends DefaultTableCellRenderer {

		protected ResourceBundle	bundle		= ApplicationManager.getApplicationBundle();

		protected Color				bg			= new Color(230, 230, 245);

		protected NumberFormat		format		= NumberFormat.getInstance(ApplicationManager.getLocale());

		protected Color				customBG	= null;

		protected final Border		b			= new CustomLineBorder();

		protected String			dataFormat	= PivotTableUtils.PIVOTTABLE_FORMAT_STANDARD_STRING;

		public NumberRenderer() {}

		public NumberRenderer(Color bgColor) {
			this.customBG = bgColor;
		}

		public void setCustomBG(Color color) {
			this.customBG = color;
		}

		public void setDataFormat(String dataFormat) {
			this.dataFormat = dataFormat;
		}

		public void setNumberFormat(NumberFormat format) {
			this.format = format;
		}

		public NumberFormat getNumberFormat() {
			return this.format;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			value = this.createPreProcess(table, value, isSelected, hasFocus, row, column);

			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			((JComponent) c).setOpaque(true);
			((JComponent) c).setBorder(this.b);
			((JComponent) c).setToolTipText(ApplicationManager.getTranslation(PivotTableUtils.PIVOTTABLE_TOOLTIP_KEY, this.bundle));
			if (table != null) {
				if (isSelected) {
					c.setForeground(table.getSelectionForeground());
					c.setBackground(table.getSelectionBackground());
				} else {
					c.setForeground(table.getForeground());
					if (this.customBG != null) {
						c.setBackground(this.customBG);
					} else {
						if ((row % 2) != 0) {
							c.setBackground(table.getBackground());
						} else {
							c.setBackground(this.bg);
						}
					}
				}
				c.setFont(table.getFont());
			}

			c = this.createPostProcess(c, table, value, isSelected, hasFocus, row, column);

			return c;
		}

		protected Object createPreProcess(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			this.setHorizontalAlignment(SwingConstants.RIGHT);
			value = this.formatIfNumber(value);
			return value;

		}

		protected Object formatIfNumber(Object value) {
			if (value instanceof Number) {
				value = this.format.format(((Number) value).doubleValue());
			}

			return value;
		}

		protected Component createPostProcess(Component c, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (PivotTableUtils.PIVOTTABLE_FORMAT_CURRENCY_STRING.equalsIgnoreCase(this.dataFormat)) {
				if ((((JLabel) c).getText() != null) && (((JLabel) c).getText().length() > 0)) {
					StringBuffer sb = new StringBuffer(((JLabel) c).getText());
					sb.append(" ");
					sb.append(CurrencyDocument.defaultCurrencySymbol);
					((JLabel) c).setText(sb.toString());
				}
			}

			return c;
		}

		protected void createRealFormater() {
			this.format = NumberFormat.getInstance(ApplicationManager.getLocale());
			this.format.setMaximumFractionDigits(3);
		}

		protected void createStandardFormater() {
			this.format = NumberFormat.getInstance(ApplicationManager.getLocale());
			this.format.setMaximumFractionDigits(2);
		}

		private void createCurrencyFormater() {
			this.format = NumberFormat.getInstance(ApplicationManager.getLocale());
			this.format.setMaximumFractionDigits(2);

		}

		protected void createPercentFormater() {
			this.format = NumberFormat.getPercentInstance(ApplicationManager.getLocale());
			this.format.setMaximumFractionDigits(0);
		}

	}

	public static class TotalNumberRenderer extends NumberRenderer {

		public TotalNumberRenderer() {
			super();
		}

		public TotalNumberRenderer(Color bgColor) {
			super(bgColor);
		}

		protected boolean percentage = false;

		public boolean isPercentage() {
			return this.percentage;
		}

		public void setPercentage(boolean percentage) {
			this.percentage = percentage;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			value = this.getRenderedValue(table, value, isSelected, hasFocus, row, column);
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}

		public Object getRenderedValue(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (this.isPercentage()) {
				if ((table != null) && (value instanceof Number)) {
					double total = 0.0;
					for (int i = 0; i < (table.getRowCount() - 1); i++) {
						Object current = table.getValueAt(i, column);
						if ((current != null) && (current instanceof Number)) {
							total = total + ((Number) current).doubleValue();
						}
					}

					value = new Double((((Number) value).doubleValue() / total) * 100);
				}
			}
			return value;
		}

	}

	public interface ITotalDetailTableModel {

		public TableModel getDetailTableModel(int rowIndex, int columnIndex);

		public Map<Object, Object> getDetailTableInformation(int rowIndex, int columnIndex);

	}

	public static class TotalTableModel extends AbstractTableModel implements ITotalDetailTableModel {

		private TableModel	innerModel	= null;

		private int			operation	= PivotTableUtils.SUM;

		private int			rowColumns	= 1;

		private String		column;

		public TotalTableModel(TableModel m, int operation, int rowColumns) {
			this.innerModel = m;
			this.operation = operation;
			this.rowColumns = rowColumns;
		}

		private DateGroupTableModel dateGroupTableModel;

		public void setDateGroupTableModel(DateGroupTableModel dateGroupTableModel) {
			this.dateGroupTableModel = dateGroupTableModel;
		}

		public void setColumnName(String column) {
			this.column = column;
		}

		public DateGroupTableModel getDateGroupTableModel() {
			return this.dateGroupTableModel;
		}

		@Override
		public int getColumnCount() {
			return this.innerModel.getColumnCount() + 1;
		}

		@Override
		public int getRowCount() {
			return this.innerModel.getRowCount() + 1;
		}

		public void setOperation(int o) {
			this.operation = o;
			this.fireTableDataChanged();
		}

		protected Object getValue(Object v) {
			if (v instanceof Value) {
				switch (this.operation) {
					case SUM:
						return ((Value) v).getSumN();
					case AVG:
						return ((Value) v).getAvgN();
					case MAX:
						return ((Value) v).getMaxN();
					case MIN:
						return ((Value) v).getMinN();
					case COUNT:
						return ((Value) v).getCountN();
					default:
						return ((Value) v).getSumN();
				}
			} else if (v instanceof Number) {
				return v;
			} else {
				return null;
			}
		}

		protected Object getValue(Value[] v) {
			double res = 0.0;
			boolean data = false;
			switch (this.operation) {
				case SUM:
					res = 0.0;
					for (int i = 0; i < v.length; i++) {
						if (v[i] == null) {
							continue;
						}
						data = true;
						res = res + v[i].getSum();
					}
					if (!data) {
						return null;
					}
					return new Double(res);
				case AVG:
					res = 0.0;
					int count = 0;
					for (int i = 0; i < v.length; i++) {
						if (v[i] == null) {
							continue;
						}
						data = true;
						res = res + v[i].getSum();
						count = count + v[i].getCount();
					}
					if (!data) {
						return null;
					}
					return new Double(res / count);
				case MAX:
					res = Double.MIN_VALUE;
					for (int i = 0; i < v.length; i++) {
						if (v[i] == null) {
							continue;
						}
						data = true;
						if (v[i].getMax() > res) {
							res = v[i].getMax();
						}
					}
					if (!data) {
						return null;
					}
					return new Double(res);
				case MIN:
					res = Double.MAX_VALUE;
					for (int i = 0; i < v.length; i++) {
						if (v[i] == null) {
							continue;
						}
						data = true;
						if (v[i].getMin() < res) {
							res = v[i].getMin();
						}
					}
					if (!data) {
						return null;
					}
					return new Double(res);
				case COUNT:
					res = 0;
					for (int i = 0; i < v.length; i++) {
						if (v[i] == null) {
							continue;
						}
						data = true;
						res = res + v[i].getCount();
					}
					if (!data) {
						return null;
					}
					return new Double(res);
				default:
					res = 0.0;
					for (int i = 0; i < v.length; i++) {
						if (v[i] == null) {
							continue;
						}
						data = true;
						res = res + v[i].getSum();
					}
					if (!data) {
						return null;
					}
					return new Double(res);
			}
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == this.innerModel.getColumnCount()) {
				if (rowIndex < this.innerModel.getRowCount()) {
					double t = 0.0;
					// boolean dat = false;
					for (int i = 0; i < this.innerModel.getColumnCount(); i++) {
						Object o = this.innerModel.getValueAt(rowIndex, i);
						o = this.getValue(o);
						if ((o != null) && (o instanceof Number)) {
							// dat = true;
							t = t + ((Number) o).doubleValue();
						}
					}

					Value[] values = new Value[this.innerModel.getColumnCount() - 1];
					for (int i = this.rowColumns; i < this.innerModel.getColumnCount(); i++) {
						Object o = this.innerModel.getValueAt(rowIndex, i);
						values[i - 1] = (Value) o;
					}
					return this.getValue(values);
				} else if (rowIndex == this.innerModel.getRowCount()) {
					double t = 0.0;
					for (int i = 0; i < this.innerModel.getRowCount(); i++) {
						Object o = this.getValueAt(i, columnIndex);
						o = this.getValue(o);
						if ((o != null) && (o instanceof Number)) {
							t = t + ((Number) o).doubleValue();
						}
					}
					return new Double(t);
				} else {
					return null;
				}
			} else {
				if (rowIndex < this.innerModel.getRowCount()) {
					Object v = this.innerModel.getValueAt(rowIndex, columnIndex);
					if (v instanceof Value) {
						return this.getValue(v);
					} else {
						return v;
					}
				} else if (rowIndex == this.innerModel.getRowCount()) {
					if (columnIndex == 0) {
						return "Total";
					}
					if (columnIndex < this.rowColumns) {
						return null;
					}
					Value[] values = new Value[this.innerModel.getRowCount()];
					for (int i = 0; i < this.innerModel.getRowCount(); i++) {
						values[i] = (Value) this.innerModel.getValueAt(i, columnIndex);
					}
					return this.getValue(values);
				} else {
					return null;
				}
			}
		}

		@Override
		public String getColumnName(int i) {
			if (i == this.innerModel.getColumnCount()) {
				return "table.total";
			}
			return this.innerModel.getColumnName(i);
		}

		@Override
		public TableModel getDetailTableModel(int rowIndex, int columnIndex) {
			DefaultTableModel model = new DefaultTableModel();
			if (this.dateGroupTableModel != null) {
				// Recovering the name of the columns of the parameter 'rows'...
				ArrayList<Object> rowNames = new ArrayList<>();
				ArrayList<Object> rowValuesGrouped = new ArrayList<>();
				for (int i = 0; i < this.rowColumns; i++) {
					// with i<= columnIndex we allow to show in detail data when
					// user
					// clicks in one grouped column
					// with columnIndex == -1 we are indicating that user has
					// clicked
					// over total column.
					if ((i <= columnIndex) || (columnIndex == -1)) {
						rowNames.add(this.innerModel.getColumnName(i));
						if (rowIndex != -1) {
							rowValuesGrouped.add(this.getValueAt(rowIndex, i));
						} else {
							rowValuesGrouped.add(null);
						}
					}
				}

				// The index of the column of the parameter 'column' into the
				// original table model.
				int origColumnIndex = this.dateGroupTableModel.getColumnIndex(this.column);

				// Recovering the indexes of the rowNames into DateGroupModel...
				int[] rowIndexes = new int[rowNames.size()];
				for (int i = 0; i < rowNames.size(); i++) {
					int index = this.dateGroupTableModel.getColumnIndex((String) rowNames.get(i));
					rowIndexes[i] = index;
				}

				// dateInnerModel contains the original data (previously
				// DateGroupModel)
				TableModel dateInnerModel = this.dateGroupTableModel.getInnerModel();
				// Initialization of empty array data...
				ArrayList[] data = new ArrayList[this.dateGroupTableModel.getColumnCount()];
				for (int i = 0; i < data.length; i++) {
					data[i] = new ArrayList();
				}
				int emptyIndex = 0;
				ArrayList colItems = PivotPanel.getItems(this.dateGroupTableModel, this.column, (Comparator) null);
				ArrayList allColItems = new ArrayList();
				allColItems.addAll(rowNames);
				allColItems.addAll(colItems);

				for (int i = 0; i < this.dateGroupTableModel.getRowCount(); i++) {
					boolean candidate = false;
					for (int j = 0; j < rowIndexes.length; j++) {
						// Recovering the values from original model of the
						// grouped
						// columns into the selected row that invokes the
						// listener
						Object value = this.dateGroupTableModel.getValueAt(i, rowIndexes[j]);
						if ((value != null) && value.equals(rowValuesGrouped.get(j))) {
							candidate = true;
						} else if ((value == null) && (rowValuesGrouped.get(j) == null)) {
							candidate = true;
						} else {
							candidate = false;
							break;
						}
					}

					if (candidate || (rowIndex == -1)) {
						// If candidate means that the values of grouped columns
						// ('rows') are equals. Now it is necessary to check
						// whether the value of the column 'column' is equal
						// too.
						Object current = this.dateGroupTableModel.getValueAt(i, origColumnIndex);
						int iCols = allColItems.indexOf(current);
						if ((columnIndex < this.rowColumns) || (columnIndex == iCols) || (columnIndex == -1)) {
							// Recover all row values from inner model of
							// DateGroupModel (the original).
							for (int k = 0; k < this.dateGroupTableModel.getColumnCount(); k++) {
								Object currentValue = dateInnerModel.getValueAt(i, k);
								data[k].add(emptyIndex, currentValue);
							}
							emptyIndex++;
						}

					}
				}

				// Adding data to the model...
				for (int i = 0; i < data.length; i++) {
					model.addColumn(this.dateGroupTableModel.getColumnName(i), data[i].toArray());
				}

				return model;
			}
			return model;
		}

		@Override
		public Map<Object, Object> getDetailTableInformation(int rowIndex, int columnIndex) {
			Map<Object, Object> information = new HashMap<>();

			ArrayList rows = new ArrayList(this.rowColumns);
			for (int i = 0; i < this.rowColumns; i++) {
				ArrayList data = new ArrayList(2);
				data.add(0, this.innerModel.getColumnName(i));
				if (rowIndex != -1) {
					data.add(1, this.getValueAt(rowIndex, i));
				} else {
					data.add(1, ApplicationManager.getTranslation(Table.TOTAL));
				}
				rows.add(data);

			}
			information.put("rows", rows);

			ArrayList column = new ArrayList(1);
			ArrayList datac = new ArrayList(2);
			datac.add(0, this.column);
			if (columnIndex != -1) {
				datac.add(1, ApplicationManager.getTranslation(this.innerModel.getColumnName(columnIndex)));
			} else {
				datac.add(1, ApplicationManager.getTranslation(Table.TOTAL));
			}

			column.add(0, datac);
			information.put("column", column);

			return information;
		}

	}

	public static class PivotDialog extends AJDialog {

		public static String	PIVOT_DIALOG_TITLE	= "pivottable.dialog.title";

		protected PivotPanel	centerPanel;

		public PivotDialog(Frame owner, TableModel model, ResourceBundle res) {
			this(owner, model, res, null, null);
		}

		public PivotDialog(Frame owner, TableModel model, ResourceBundle res, Map<Object, Object> parameters, String originalColPosAndWith) {
			super(owner, PivotTableUtils.translate(PivotDialog.PIVOT_DIALOG_TITLE, res, null), true);
			this.init(model, res, parameters, originalColPosAndWith);
		}

		public PivotDialog(Dialog owner, TableModel model, ResourceBundle res) {
			this(owner, model, res, null, null);
		}

		public PivotDialog(Dialog owner, TableModel model, ResourceBundle res, Map<Object, Object> parameters, String originalColPosAndWith) {
			super(owner, PivotTableUtils.translate(PivotDialog.PIVOT_DIALOG_TITLE, res, null), true);
			this.init(model, res, parameters, originalColPosAndWith);
		}

		protected void init(TableModel model, ResourceBundle res) {
			this.init(model, res, null, null);
		}

		protected void init(TableModel model, ResourceBundle res, Map<Object, Object> parameters, String originalColPosAndWith) {
			this.centerPanel = new PivotPanel(model, res, parameters, originalColPosAndWith);
			this.getContentPane().setLayout(new BorderLayout());
			this.getContentPane().add(this.centerPanel);
			this.pack();
		}

		public void setModel(TableModel model) {
			this.centerPanel.setModel(model);
		}

		// Since 5.2066EN-0.1 combos in this component are populated according
		// to
		// visible columns (dynamically)
		public void setModel(TableModel model, boolean configureCombos) {
			this.centerPanel.setModel(model, configureCombos);
		}

		public void setOriginalColPosAndWith(String originalColPosAndWith) {
			this.centerPanel.setOriginalColPosAndWith(originalColPosAndWith);
		}

		public void setRenderersForColumns(Map<?, ?> columnRendererMap) {
			this.centerPanel.setRenderersForColumns(columnRendererMap);
		}

		public void addButton(AbstractButton button) {
			this.centerPanel.addButton(button);
		}

		public void setSelectedColumn(Map<Object, Object> h) {
			this.centerPanel.setSelectedColumn(h);
		}

		public Map<Object, Object> getSelectedColumn() {
			return this.centerPanel.getSelectedColumn();
		}

		public List<Pair> getFixedColumnWidth() {
			return this.centerPanel.getFixedColumnWidth();
		}

	}

	public static class PivotPanel extends JPanel {

		private static final Logger									logger					= LoggerFactory.getLogger(PivotTableUtils.PivotPanel.class);

		private boolean												updateEnabled			= true;

		protected int												NUMBER_CROW_FIELD		= 5;

		protected TableModel										model					= null;

		protected ResourceBundle									resources				= null;

		// protected JTable table = new JTable();

		// protected JScrollPane scrollTable;

		protected FixedColumnTable									fixedColumnTable;

		protected HeaderRenderer									headerRenderer			= null;

		protected JPanel											topPanel				= new JPanel(new BorderLayout());

		protected JPanel											buttonPanel				= new JPanel(new FlowLayout(FlowLayout.LEFT));

		protected JPanel											titlePanel				= new JPanel(new GridBagLayout());

		protected JTextField										titleTextField			= new JTextField();

		protected static final String								PIVOT_TABLE_TITLE		= "pivottable.title";

		protected JLabel											titleLabel				= new JLabel(PivotPanel.PIVOT_TABLE_TITLE);

		protected JTextField										subtitleTextField		= new JTextField();

		protected static final String								PIVOT_TABLE_SUBTITLE	= "pivottable.subtitle";

		protected JLabel											subtitleLabel			= new JLabel(PivotPanel.PIVOT_TABLE_SUBTITLE);

		protected JPanel											columnPanel				= new JPanel(new FlowLayout(FlowLayout.LEFT));

		protected JPanel											leftPanel				= new JPanel(new GridBagLayout());

		protected JButton											printButton				= new JButton();

		protected JButton											ppButton				= new JButton();

		protected JButton											pageButton				= new JButton();

		protected JButton											exportExcelButton		= new JButton();

		protected JComboBox[]										arrayCrowField			= new JComboBox[this.NUMBER_CROW_FIELD];

		protected ArrayList<Pair<String, Integer>>					rowFieldWidth			= new ArrayList<Pair<String, Integer>>();

		/**
		 * @deprecated All combos are inside of arrayCrowField variable
		 */
		@Deprecated
		protected JComboBox											crowField				= new JComboBox();

		/**
		 * @deprecated All combos are inside of arrayCrowField variable
		 */
		@Deprecated
		protected JComboBox											crowField2				= new JComboBox();

		/**
		 * @deprecated All combos are inside of arrayCrowField variable
		 */
		@Deprecated
		protected JComboBox											crowField3				= new JComboBox();

		protected JComboBox											ccolumnField			= new JComboBox();

		protected JComboBox											cdataField				= new JComboBox();

		protected JComboBox											cdategroup				= new JComboBox();

		protected JComboBox											coperation				= new JComboBox();

		protected JComboBox											cformat					= new JComboBox();

		protected com.ontimize.util.swing.table.PrintablePivotTable	pTable					= null;

		protected Map<Object, Object>								selectedColumn			= null;

		protected Map<Object, Object>								parameters;

		protected String											originalColPosAndWith	= null;

		protected Map<?, ?>											rendererForColumns		= null;

		public String getOriginalColPosAndWith() {
			return this.originalColPosAndWith;
		}

		public void setOriginalColPosAndWith(String originalColPosAndWith) {
			this.originalColPosAndWith = originalColPosAndWith;
		}

		public Map<?, ?> getRenderersForColumnsMap() {
			return this.rendererForColumns;
		}

		public void setRenderersForColumns(Map<?, ?> rederersMap) {
			this.rendererForColumns = rederersMap;
		}

		static class AuxPanel extends JPanel {

			public AuxPanel(String title, ResourceBundle res) {
				this.setLayout(new BorderLayout());
				this.setBorder(new TitledBorder(PivotTableUtils.translate(title, res, null)));
			}

			@Override
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				if (d.width < 120) {
					d.width = 120;
				}
				return d;
			}

		}

		public void setModel(TableModel model, boolean configureCombos) {
			this.model = model;
			if (configureCombos) {
				this.configure(this.arrayCrowField);
				this.configure(this.ccolumnField);
				this.configureDataField();
			}
			this.updateTable();
		}

		public void setModel(TableModel model) {
			this.setModel(model, false);
		}

		public TableModel getModel() {
			return this.model;
		}

		public PivotPanel(TableModel model, ResourceBundle res) {
			this(model, res, null, null);
		}

		public PivotPanel(TableModel model, ResourceBundle res, Map<Object, Object> parameters, String originalColPosAndWith) {
			this.model = model;
			this.resources = res;
			this.parameters = parameters;
			this.setOriginalColPosAndWith(originalColPosAndWith);
			this.init();
		}

		public JPanel getTopPanel() {
			return this.topPanel;
		}

		public JPanel getButtonPanel() {
			return this.buttonPanel;
		}

		public JPanel getTitlePanel() {
			return this.titlePanel;
		}

		protected void init() {
			this.configurePrintButton();
			this.configurePreviewButton();
			this.configurePageButton();
			this.configureExportExcelButton();

			JPanel pRow = new AuxPanel(PivotTableUtils.PIVOTTABLE_ROWFIELD, this.resources);
			JPanel pColumn = new AuxPanel(PivotTableUtils.PIVOTTABLE_COLUMNFIELD, this.resources);
			JPanel pData = new AuxPanel(PivotTableUtils.PIVOTTABLE_DATAFIELD, this.resources);
			JPanel pOperation = new AuxPanel(PivotTableUtils.PIVOTTABLE_OPERATION, this.resources);
			JPanel pDate = new AuxPanel(PivotTableUtils.PIVOTTABLE_DATEGROUPOPTIONS, this.resources);
			JPanel pFormat = new AuxPanel(PivotTableUtils.PIVOTTABLE_FORMAT_OPTIONS, this.resources);

			this.buttonPanel.add(this.printButton);
			this.buttonPanel.add(this.pageButton);
			this.buttonPanel.add(this.ppButton);
			this.buttonPanel.add(this.exportExcelButton);

			JPanel top = new JPanel(new GridBagLayout());
			top.add(this.buttonPanel, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			top.add(this.titlePanel, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			this.topPanel.add(top, BorderLayout.NORTH);
			this.topPanel.add(pRow, BorderLayout.CENTER);

			// Title panel
			this.titlePanel.add(this.titleLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 2, 5), 0, 0));
			this.titleLabel.setText(ApplicationManager.getTranslation(PivotPanel.PIVOT_TABLE_TITLE, this.resources));
			this.titlePanel.add(this.titleTextField,
					new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 5, 2, 5), 0, 0));
			this.titlePanel.add(this.subtitleLabel, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 2, 5), 0, 0));
			this.subtitleLabel.setText(ApplicationManager.getTranslation(PivotPanel.PIVOT_TABLE_SUBTITLE, this.resources));
			this.titlePanel.add(this.subtitleTextField,
					new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 5, 2, 5), 0, 0));

			this.columnPanel.add(pColumn);
			this.columnPanel.add(pDate);
			this.columnPanel.add(pData);
			this.columnPanel.add(pOperation);
			this.columnPanel.add(pFormat);
			this.topPanel.add(this.columnPanel, BorderLayout.SOUTH);

			pRow.setLayout(new FlowLayout(FlowLayout.LEFT));

			for (int i = 0; i < this.arrayCrowField.length; i++) {
				this.arrayCrowField[i] = new JComboBox() {
					@Override
					public Dimension getPreferredSize() {
						Dimension d = super.getPreferredSize();
						if (d.width < 100) {
							d.width = 100;
						}
						return d;
					}
				};
				pRow.add(this.arrayCrowField[i]);
			}

			pColumn.add(this.ccolumnField);
			pDate.add(this.cdategroup);
			pData.add(this.cdataField);
			pOperation.add(this.coperation);
			pFormat.add(this.cformat);

			this.configure(this.arrayCrowField);

			this.configure(this.ccolumnField);

			this.configureDateGroup();

			this.configureOperations();

			this.configureFormat();

			this.configureDataField();
			this.setLayout(new BorderLayout());
			this.add(this.topPanel, BorderLayout.NORTH);
			this.add(this.leftPanel, BorderLayout.WEST);
			this.configureTable();
		}

		public EntityResult getValueToExport() {
			EntityResult result = new EntityResultMapImpl();

			List<Object> columnNames = new ArrayList<>();
			List<Object> visibleColumnNames = new ArrayList<>();
			for (int i = 0; i < this.fixedColumnTable.getColumnCount(); i++) {
				TableColumn tc = this.fixedColumnTable.getColumn(i);
				result.put(tc.getHeaderValue(), new ArrayList<>());
				visibleColumnNames.add(tc.getHeaderValue());
				columnNames.add(tc.getHeaderValue());
			}

			for (int j = 0; j < this.fixedColumnTable.getRowCount(); j++) {
				for (int i = 0; i < this.fixedColumnTable.getColumnCount(); i++) {

					JTable table = this.fixedColumnTable.getTableFromColumn(i);
					int index = this.fixedColumnTable.indexAtTable(table, i);

					Object value = table.getValueAt(j, index);
					TableCellRenderer r = table.getCellRenderer(j, index);
					Component c = r.getTableCellRendererComponent(table, value, false, false, j, index);
					String text = null;
					if (c instanceof JLabel) {
						text = ((JLabel) c).getText();
					} else if (c instanceof JTextComponent) {
						text = ((JTextComponent) c).getText();
					} else if (c instanceof JCheckBox) {
						if (((JCheckBox) c).isSelected()) {
							text = ApplicationManager.getTranslation("Yes");
						} else {
							text = ApplicationManager.getTranslation("No");
						}
					} else {
						text = "";
						if (value != null) {
							if (value instanceof Boolean) {
								if (((Boolean) value).booleanValue()) {
									text = ApplicationManager.getTranslation("Yes");
								} else {
									text = ApplicationManager.getTranslation("No");
								}
							} else {
								text = value.toString();
							}
						}
					}

					List<Object> v = (List<Object>) result.get(columnNames.get(i));
					v.add(text);
					result.put(columnNames.get(i), v);
				}
			}
			result.setColumnOrder(visibleColumnNames);
			return result;
		}

		public String getDataString() {
			return this.fixedColumnTable.getDataString();
		}

		protected void configurePrintButton() {
			this.printButton.setIcon(ImageManager.getIcon(ImageManager.PRINT));
			this.printButton.setMargin(new Insets(2, 2, 2, 2));
			this.printButton.setToolTipText(PivotTableUtils.translate("pivottable.printbutton.tip", this.resources, null));
			this.printButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						if (PivotPanel.this.pTable == null) {
							JTable fixedColumnTable = new JTable();
							fixedColumnTable.setAutoCreateColumnsFromModel(false);
							JTable contentTable = new JTable();
							contentTable.setAutoCreateColumnsFromModel(true);

							fixedColumnTable.setRowMargin(0);
							contentTable.setRowMargin(0);

							fixedColumnTable.setDefaultRenderer(Object.class, new NumberRenderer());
							fixedColumnTable.getTableHeader().setDefaultRenderer(new HeaderRenderer(PivotPanel.this.resources));

							contentTable.setDefaultRenderer(Object.class, PivotTableUtils.getRenderInstance(PivotPanel.this.cformat.getSelectedItem().toString(), new Object[] {}));
							contentTable.getTableHeader().setDefaultRenderer(new MainHeaderRenderer(PivotPanel.this.resources));

							PivotPanel.this.pTable = new com.ontimize.util.swing.table.PrintablePivotTable(fixedColumnTable, contentTable, true);
						}

						PivotPanel.this.pTable.setData(PivotPanel.this.fixedColumnTable.getModel(), PivotPanel.this.fixedColumnTable.getFixedTable().getColumnCount());
						PivotPanel.this.pTable.setPageTitle(PivotPanel.this.titleTextField.getText());
						PivotPanel.this.pTable.setPageSubtitle(PivotPanel.this.subtitleTextField.getText());
						for (int i = 0; i < PivotPanel.this.pTable.getFixedColumnTable().getColumnModel().getColumnCount(); i++) {
							PivotPanel.this.pTable.getFixedColumnTable().getColumnModel().getColumn(i)
									.setCellRenderer(PivotPanel.this.fixedColumnTable.getColumn(i).getCellRenderer());
						}

						PivotPanel.this.pTable.getContentTable().setDefaultRenderer(Object.class,
								PivotTableUtils.getRenderInstance(PivotPanel.this.cformat.getSelectedItem().toString(), new Object[] {}));

						PivotPanel.this.pTable.fitPage();
						Thread thread = new Thread() {

							@Override
							public void run() {
								try {
									PivotPanel.this.pTable.print();
								} catch (Exception ex) {
									PivotTableUtils.logger.error(null, ex);
								}
							}
						};
						thread.start();
					} catch (Exception ex) {
						PivotTableUtils.logger.error(null, ex);
						com.ontimize.gui.MessageDialog.showErrorMessage(SwingUtilities.getWindowAncestor(PivotPanel.this), ex.getMessage());
					}
				}
			});
		}

		protected void configurePreviewButton() {
			this.ppButton.setIcon(ImageManager.getIcon(ImageManager.PREVIEW));
			this.ppButton.setMargin(new Insets(2, 2, 2, 2));
			this.ppButton.setToolTipText(PivotTableUtils.translate("pivottable.preview.tip", this.resources, null));
			this.ppButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						if (PivotPanel.this.pTable == null) {
							JTable fixedColumnTable = new JTable();
							fixedColumnTable.setAutoCreateColumnsFromModel(false);
							JTable contentTable = new JTable();
							contentTable.setAutoCreateColumnsFromModel(true);

							fixedColumnTable.setRowMargin(0);
							contentTable.setRowMargin(0);

							fixedColumnTable.setDefaultRenderer(Object.class, new NumberRenderer());
							fixedColumnTable.getTableHeader().setDefaultRenderer(new HeaderRenderer(PivotPanel.this.resources));

							contentTable.setDefaultRenderer(Object.class, PivotTableUtils.getRenderInstance(PivotPanel.this.cformat.getSelectedItem().toString(), new Object[] {}));
							contentTable.getTableHeader().setDefaultRenderer(new MainHeaderRenderer(PivotPanel.this.resources));

							PivotPanel.this.pTable = new com.ontimize.util.swing.table.PrintablePivotTable(fixedColumnTable, contentTable, true);
						}
						PivotPanel.this.pTable.setScale(1.0);
						PivotPanel.this.pTable.setData(PivotPanel.this.fixedColumnTable.getModel(), PivotPanel.this.fixedColumnTable.getFixedTable().getColumnCount());
						PivotPanel.this.pTable.setColumnWidth(PivotPanel.this.fixedColumnTable.getFixedTable().getColumnModel(),
								PivotPanel.this.fixedColumnTable.getMainTable().getColumnModel());
						PivotPanel.this.pTable.setPageTitle(PivotPanel.this.titleTextField.getText());
						PivotPanel.this.pTable.setPageSubtitle(PivotPanel.this.subtitleTextField.getText());

						for (int i = 0; i < PivotPanel.this.pTable.getFixedColumnTable().getColumnModel().getColumnCount(); i++) {
							PivotPanel.this.pTable.getFixedColumnTable().getColumnModel().getColumn(i)
									.setCellRenderer(PivotPanel.this.fixedColumnTable.getColumn(i).getCellRenderer());
						}

						PivotPanel.this.pTable.getContentTable().setDefaultRenderer(Object.class,
								PivotTableUtils.getRenderInstance(PivotPanel.this.cformat.getSelectedItem().toString(), new Object[] {}));

						PivotPanel.this.pTable.fitPage();
						Pageable pageable = new Pageable() {

							@Override
							public Printable getPrintable(int index) {
								return PivotPanel.this.pTable;
							}

							@Override
							public PageFormat getPageFormat(int i) {
								return PivotPanel.this.pTable.getPageFormat();
							}

							@Override
							public int getNumberOfPages() {
								return PivotPanel.this.pTable.getNumberOfPages();
							}
						};

						com.ontimize.util.swing.printpreview.PrintPreviewDialog.showPrintPreviewDialog(PivotPanel.this.fixedColumnTable, pageable,
								PivotPanel.this.pTable.getPageFormat(), "Pivot table report");
					} catch (Exception ex) {
						PivotTableUtils.logger.error(null, ex);
						com.ontimize.gui.MessageDialog.showErrorMessage(SwingUtilities.getWindowAncestor(PivotPanel.this), ex.getMessage());
					}
				}
			});
		}

		protected void configurePageButton() {
			this.pageButton.setIcon(ImageManager.getIcon(ImageManager.PAGE));
			this.pageButton.setMargin(new Insets(2, 2, 2, 2));
			this.pageButton.setToolTipText(PivotTableUtils.translate("pivottable.pagesetup.tip", this.resources, null));

			this.pageButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						if (PivotPanel.this.pTable == null) {
							JTable fixedColumnTable = new JTable();
							fixedColumnTable.setAutoCreateColumnsFromModel(false);
							JTable contentTable = new JTable();
							contentTable.setAutoCreateColumnsFromModel(true);

							fixedColumnTable.setRowMargin(0);
							contentTable.setRowMargin(0);

							fixedColumnTable.setDefaultRenderer(Object.class, new NumberRenderer());
							fixedColumnTable.getTableHeader().setDefaultRenderer(new HeaderRenderer(PivotPanel.this.resources));

							contentTable.setDefaultRenderer(Object.class, PivotTableUtils.getRenderInstance(PivotPanel.this.cformat.getSelectedItem().toString(), new Object[] {}));
							contentTable.getTableHeader().setDefaultRenderer(new MainHeaderRenderer(PivotPanel.this.resources));

							PivotPanel.this.pTable = new com.ontimize.util.swing.table.PrintablePivotTable(fixedColumnTable, contentTable, true);
						}

						PivotPanel.this.pTable.setData(PivotPanel.this.fixedColumnTable.getModel(), PivotPanel.this.fixedColumnTable.getFixedTable().getColumnCount());
						PivotPanel.this.pTable.setColumnWidth(PivotPanel.this.fixedColumnTable.getFixedTable().getColumnModel(),
								PivotPanel.this.fixedColumnTable.getMainTable().getColumnModel());
						PivotPanel.this.pTable.getContentTable().setDefaultRenderer(Object.class,
								PivotTableUtils.getRenderInstance(PivotPanel.this.cformat.getSelectedItem().toString(), new Object[] {}));
						PivotPanel.this.pTable.setPageTitle(PivotPanel.this.titleTextField.getText());
						PivotPanel.this.pTable.setPageSubtitle(PivotPanel.this.subtitleTextField.getText());
						for (int i = 0; i < PivotPanel.this.pTable.getFixedColumnTable().getColumnModel().getColumnCount(); i++) {
							PivotPanel.this.pTable.getFixedColumnTable().getColumnModel().getColumn(i)
									.setCellRenderer(PivotPanel.this.fixedColumnTable.getColumn(i).getCellRenderer());
						}
						PivotPanel.this.pTable.pageSetup();
					} catch (Exception ex) {
						PivotTableUtils.logger.error(null, ex);
						com.ontimize.gui.MessageDialog.showErrorMessage(SwingUtilities.getWindowAncestor(PivotPanel.this), ex.getMessage());
					}
				}
			});
		}

		protected void configureExportExcelButton() {
			this.exportExcelButton.setIcon(ImageManager.getIcon(ImageManager.EXCEL));
			this.exportExcelButton.setMargin(new Insets(2, 2, 2, 2));
			this.exportExcelButton.setToolTipText(PivotTableUtils.translate(Table.TIP_EXCEL_EXPORT, this.resources, null));

			this.exportExcelButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent actionEvent) {

					JFileChooser selFile = new JFileChooser();

					String[] exts = { "xls" };
					String[] extsXLSX = { "xlsx" };
					javax.swing.filechooser.FileFilter ffText = FileUtils.getExtensionFileFilter("text_files", new String[] { "txt" });
					javax.swing.filechooser.FileFilter ffExcel = FileUtils.getExtensionFileFilter("Excel 97-2003 (*.xls)", exts);
					javax.swing.filechooser.FileFilter ffXLSXExcel = FileUtils.getExtensionFileFilter("Excel 2007-2010 (*.xlsx)", extsXLSX);

					selFile.addChoosableFileFilter(ffText);
					selFile.addChoosableFileFilter(ffExcel);
					selFile.setFileFilter(ffExcel);
					if (XLSExporterFactory.isAvailableXLSX()) {

						selFile.addChoosableFileFilter(ffXLSXExcel);
						selFile.setFileFilter(ffXLSXExcel);
					}

					// In java7, all-files filter always appears at first,
					// we must force to set xls extension
					// since 5.2078EN-0.4

					int iChoice = selFile.showSaveDialog(PivotPanel.this.exportExcelButton);
					if (iChoice == JFileChooser.APPROVE_OPTION) {
						try {
							boolean bXLSX = false;
							File selectedFile = selFile.getSelectedFile();
							String selectedFileString = selectedFile.getPath();
							javax.swing.filechooser.FileFilter ff = selFile.getFileFilter();
							if ((ff == ffExcel) || selectedFileString.endsWith(".xlsx") || (ff == ffXLSXExcel)) {
								if (selectedFileString.endsWith(".xlsx")) {
									if (XLSExporterFactory.isAvailableXLSX()) {
										bXLSX = true;
									} else {
										MessageDialog.showMessage(ApplicationManager.getApplication().getFrame(), "table.xlsx_extension_not_supported",
												XLSExporterFactory.getErrorMessage(), JOptionPane.WARNING_MESSAGE, PivotPanel.this.resources);
										selectedFileString = selectedFileString.substring(0, selectedFileString.length() - 5);
									}
								}

								if ((!selectedFileString.endsWith(".xls") && !selectedFileString.endsWith(".xlsx")) && (ff == ffXLSXExcel)) {
									bXLSX = true;
									selectedFile = new File(selectedFileString + ".xlsx");
								} else if (!selectedFileString.endsWith(".xls") && !selectedFileString.endsWith(".xlsx")) {
									selectedFile = new File(selectedFileString + ".xls");
								}
								// If selection is xls then save a temporal
								// file and
								// convert
								final File finalFile = selectedFile;
								final boolean xlsx = bXLSX;
								try {
									PivotPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
									Window w = SwingUtilities.getWindowAncestor(PivotPanel.this);
									OperationThread op = new OperationThread() {
										@Override
										public void run() {
											this.hasStarted = true;
											this.status = "";
											try {
												if (PivotPanel.this.resources != null) {
													this.status = PivotPanel.this.resources.getString("table.generating_xls_file");
												} else {
													this.status = "Generating XLS file";
												}
											} catch (Exception e) {
												PivotPanel.logger.error("Generting XLS file", e);
												this.status = "Generating XLS file";
											}
											try {
												// Create the excel file
												EntityResult res = PivotPanel.this.getValueToExport();
												XLSExporter exporter = XLSExporterFactory.instanceXLSExporter(Table.XLS_EXPORT_CLASS);
												List orderColumns = res.getOrderColumns();
												// Hashtable renderers =
												// Table.this.getAllColumnRenderer();
												// for (Object current :
												// orderColumns) {
												// if (current instanceof
												// Table.KeyObject) {
												// KeyObject currentKO =
												// (KeyObject) current;
												// if
												// (renderers.containsKey(currentKO.getKey()))
												// {
												// renderers.put(currentKO.toString(),
												// renderers.get(currentKO.getKey()));
												// }
												// }
												// }
												exporter.createXLS(res, finalFile, null, new HashMap<>(), res.getOrderColumns(), true, xlsx, true);
											} catch (Exception e) {
												PivotPanel.logger.error(null, e);
												MessageDialog.showErrorMessage(SwingUtilities.getWindowAncestor(PivotPanel.this), "table.error_generating_xls_file");
											} finally {
												this.hasFinished = true;
											}
										}
									};

									if (w instanceof Dialog) {
										ApplicationManager.proccessOperation((Dialog) w, op, 500);
									} else {
										ApplicationManager.proccessOperation((Frame) w, op, 500);
									}

								} catch (Exception e) {
									PivotTableUtils.logger.error(null, e);
									MessageDialog.showErrorMessage(SwingUtilities.getWindowAncestor(PivotPanel.this), "table.error_generating_xls_file");
								} finally {
									PivotPanel.this.setCursor(Cursor.getDefaultCursor());
								}
							} else {
								// If selection is text file then save it
								if (!selectedFileString.substring(selectedFileString.length() - 4, selectedFileString.length()).equalsIgnoreCase(".txt")) {
									selectedFile = new File(selectedFileString + ".txt");
								}
								FileWriter fw = new FileWriter(selectedFile);
								String cadenaExcel = PivotPanel.this.getDataString();
								fw.write(cadenaExcel, 0, cadenaExcel.length());
								fw.flush();
								fw.close();
							}
						} catch (Exception e) {
							PivotTableUtils.logger.trace(null, e);
							// If this is a SecurityException
							if (e instanceof SecurityException) {
								Window w = SwingUtilities.getWindowAncestor(PivotPanel.this);
								if (w instanceof Dialog) {
									MessageDialog.showMessage((Dialog) w, "table.operation_cannot_be_performed", "table.security_error_message", JOptionPane.WARNING_MESSAGE,
											PivotPanel.this.resources);
								} else {
									MessageDialog.showMessage((Frame) w, "table.operation_cannot_be_performed", "table.security_error_message", JOptionPane.WARNING_MESSAGE,
											PivotPanel.this.resources);
								}
							}
						}
					}
				}
			});
		}

		protected void configureTable() {
			this.fixedColumnTable = new FixedColumnTable(this.parameters, this.resources);
			this.add(this.fixedColumnTable);
			this.installDetailMouseListener();
		}

		// protected void configureTable(){
		// scrollTable = new JScrollPane(table);
		// scrollTable.setBorder(ParseUtils.getBorder(this.parameters!=null ?
		// (String) parameters.get("border") : null,
		// BorderManager.getBorder(BorderManager.DEFAULT_TABLE_BORDER_KEY)));
		// add(scrollTable);
		// table.setDefaultRenderer(Object.class, new NumberRenderer());
		// this.table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		// this.table.getTableHeader().setReorderingAllowed(false);
		// this.table.setRowMargin(0);
		// this.headerRenderer = new HeaderRenderer(resources, this.parameters);
		// table.getTableHeader().setDefaultRenderer(this.headerRenderer);
		// table.addKeyListener(new KeyAdapter() {
		//
		// public void keyPressed(KeyEvent e) {
		// if (e.getModifiers() == KeyEvent.CTRL_MASK && e.getKeyCode() ==
		// KeyEvent.VK_C) {
		// Cursor c = getCursor();
		// try {
		// ((JTable)
		// e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		// final StringSelection sselection = new
		// StringSelection(getDataString());
		// Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sselection,
		// sselection);
		// } catch (Exception ex) {
		// // If this is a SecurityException
		// if (ex instanceof SecurityException) {
		// java.security.AccessController.doPrivileged(new
		// java.security.PrivilegedAction() {
		// public Object run() {
		// JTextField campoTexto = new JTextField();
		// campoTexto.setText(getDataString());
		// campoTexto.selectAll();
		// campoTexto.copy();
		// if (ApplicationManager.DEBUG) {
		// System.out.println("Copied to clipboard");
		// }
		// return null;
		// }
		// });
		// }
		// } finally {
		// ((JTable) e.getSource()).setCursor(c);
		// }
		// }
		// }
		//
		// });
		//
		// table.addMouseListener(new MouseAdapter() {
		// protected JPopupMenu menu;
		//
		// public void mouseClicked(MouseEvent e) {
		// if (SwingUtilities.isRightMouseButton(e)){
		// if (menu==null){
		// menu = new JPopupMenu();
		// JMenuItem item = new JMenuItem("fixed");
		// item.addActionListener(new ActionListener() {
		// public void actionPerformed(ActionEvent e) {
		// fixedColumn(2);
		// }
		// });
		// menu.add(item);
		// }
		// menu.show((Component)e.getSource(), e.getX(), e.getY());
		//
		// }
		// }
		//
		// });
		// installDetailMouseListener();
		// }

		protected EJDialog dDetailPivot = null;

		protected void installDetailMouseListener() {
			final JTable fixedTable = this.fixedColumnTable.getFixedTable();
			fixedTable.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					super.mouseClicked(e);
					if (e.getClickCount() == 2) {
						Object obj = e.getSource();
						if (obj instanceof JTable) {
							int col = ((JTable) obj).getSelectedColumn();
							int row = ((JTable) obj).getSelectedRow();

							TableModel model = ((JTable) obj).getModel();
							if (model instanceof ITotalDetailTableModel) {
								if (row == (fixedTable.getRowCount() - 1)) {
									row = -1;
								}
								if (col == (fixedTable.getColumnCount() - 1)) {
									col = -1;
								}
								Cursor cursor = PivotPanel.this.fixedColumnTable.getCursor();
								try {
									PivotPanel.this.fixedColumnTable.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
									TableModel mod = ((ITotalDetailTableModel) model).getDetailTableModel(row, col);
									Map<Object, Object> information = ((ITotalDetailTableModel) model).getDetailTableInformation(row, col);
									PivotPanel.this.showDetailPivotTableWindow(mod, information);
								} finally {
									PivotPanel.this.fixedColumnTable.setCursor(cursor);
								}
							}
						}
					}
				}
			});

			final JTable mainTable = this.fixedColumnTable.getMainTable();
			mainTable.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					super.mouseClicked(e);
					if (e.getClickCount() == 2) {
						Object obj = e.getSource();
						if (obj instanceof JTable) {
							int col = ((JTable) obj).getSelectedColumn();
							int row = ((JTable) obj).getSelectedRow();

							TableModel model = ((JTable) obj).getModel();
							if (model instanceof ITotalDetailTableModel) {
								if (row == (mainTable.getRowCount() - 1)) {
									row = -1;
								}
								if (col == (mainTable.getColumnCount() - 1)) {
									col = -1;
								}
								Cursor cursor = PivotPanel.this.fixedColumnTable.getCursor();
								try {
									col = col + fixedTable.getColumnCount();
									PivotPanel.this.fixedColumnTable.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
									TableModel mod = ((ITotalDetailTableModel) model).getDetailTableModel(row, col);
									Map<Object, Object> information = ((ITotalDetailTableModel) model).getDetailTableInformation(row, col);
									PivotPanel.this.showDetailPivotTableWindow(mod, information);
								} finally {
									PivotPanel.this.fixedColumnTable.setCursor(cursor);
								}
							}
						}
					}
				}
			});
		}

		protected void showDetailPivotTableWindow(TableModel model, Map<Object, Object> information) {
			if (this.dDetailPivot == null) {
				Window w = SwingUtilities.getWindowAncestor(this.fixedColumnTable);
				this.dDetailPivot = PivotDetailTableUtils.createPivotDetailDialog(w, model, this.getDetailWindowParameters(), this.resources);
				// setPivotTablePreferences(dDetailPivot);
			}
			((PivotDetailTableUtils.PivotDetailDialog) this.dDetailPivot).setModel(model, information);
			((PivotDetailTableUtils.PivotDetailDialog) this.dDetailPivot).setWidthAndPositionColumns(this.getOriginalColPosAndWith());
			((PivotDetailTableUtils.PivotDetailDialog) this.dDetailPivot).setRenderers(this.getRenderersForColumnsMap());
			((PivotDetailTableUtils.PivotDetailDialog) this.dDetailPivot).setResourceBundle(this.resources);

			this.dDetailPivot.setVisible(true);
		}

		protected Map<Object, Object> getDetailWindowParameters() {
			Map<Object, Object> param = new HashMap<>();
			param.putAll(this.parameters);
			param.put("entity", "entity");
			param.put("dynamic", "yes");
			param.put("translateheader", "yes");
			param.put(Table.D_PIVOT_TABLE_PREFERENCES, "yes");
			return param;
		}

		protected void configureDataField() {
			ArrayList cols = new ArrayList();
			for (int i = 0; i < this.model.getColumnCount(); i++) {
				cols.add(this.model.getColumnName(i));
			}

			Collections.sort(cols, new BundleComparator(PivotPanel.this.resources));

			cols.add(0, null);
			DefaultComboBoxModel listmodel = new DefaultComboBoxModel(cols.toArray());

			this.cdataField.setModel(listmodel);
			this.cdataField.setRenderer(new TranslateListRenderer(this.resources));

			this.cdataField.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					PivotPanel.this.updateTable();
					Object sel = PivotPanel.this.cdataField.getSelectedItem();
					if (sel != null) {
						int i = PivotPanel.columnIndex(PivotPanel.this.model, (String) sel);
						if (i >= 0) {
							Class cl = PivotPanel.this.model.getColumnClass(i);
							if (cl.getSuperclass() == Number.class) {
								if (!PivotPanel.this.coperation.isEnabled()) {
									PivotPanel.this.coperation.setSelectedIndex(0);
									PivotPanel.this.coperation.setEnabled(true);
								}
							} else {
								PivotPanel.this.coperation.setSelectedIndex(4);
								PivotPanel.this.coperation.setEnabled(false);
							}
						}
					}
				}
			});
		}

		protected void configureOperations() {
			DefaultComboBoxModel listmodel = new DefaultComboBoxModel();
			listmodel.addElement("pivottable.sum");
			listmodel.addElement("pivottable.avg");
			listmodel.addElement("pivottable.max");
			listmodel.addElement("pivottable.min");
			listmodel.addElement("pivottable.count");
			this.coperation.setModel(listmodel);
			this.coperation.setRenderer(new TranslateListRenderer(this.resources));
			this.coperation.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					PivotPanel.this.updateTableOperation();
				}
			});
		}

		protected void configureFormat() {
			ArrayList listRender = new ArrayList<String>();
			// listRender.add(PivotTableUtils.PIVOTTABLE_FORMAT_PERCENTAGE_STRING);
			// listRender.add(PivotTableUtils.PIVOTTABLE_FORMAT_CURRENCY_STRING);
			// listRender.add(PivotTableUtils.PIVOTTABLE_FORMAT_REAL_STRING);
			this.addCustomKeys(listRender, PivotTableUtils.rendererMapKeyFormatRender);
			Collections.sort(listRender, new BundleComparator(this.resources));
			NumberFormat nF = NumberFormat.getInstance(ApplicationManager.getLocale());
			nF.setMaximumFractionDigits(2);
			PivotTableUtils.addRenderAndFormat(PivotTableUtils.PIVOTTABLE_FORMAT_STANDARD_STRING, nF, NumberRenderer.class);
			listRender.add(0, PivotTableUtils.PIVOTTABLE_FORMAT_STANDARD_STRING);
			DefaultComboBoxModel listmodel = new DefaultComboBoxModel(listRender.toArray());
			this.cformat.setModel(listmodel);
			this.cformat.setRenderer(new TranslateListRenderer(this.resources));
			this.cformat.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					PivotPanel.this.updateTable();

				}
			});
		}

		protected void addCustomKeys(List listRender, Map<String, RenderFormatKey> rendererMapKeyAndFormatter) {
			for (Map.Entry<String, RenderFormatKey> entry : rendererMapKeyAndFormatter.entrySet()) {
				listRender.add(entry.getKey());
			}
		}

		protected void configureDateGroup() {
			DefaultComboBoxModel listmodel = new DefaultComboBoxModel();
			listmodel.addElement("pivottable.daymonthyear");
			listmodel.addElement("pivottable.monthyear");
			listmodel.addElement("pivottable.quarteryear");
			listmodel.addElement("pivottable.month");
			listmodel.addElement("pivottable.quarter");
			listmodel.addElement("pivottable.year");
			listmodel.addElement("pivottable.weekyear");
			this.cdategroup.setModel(listmodel);
			this.cdategroup.setRenderer(new TranslateListRenderer(this.resources));
			this.cdategroup.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					PivotPanel.this.updateTable();
				}
			});
		}

		public void addButton(AbstractButton button) {
			this.buttonPanel.add(button);
		}

		public Map<Object, Object> getSelectedColumn() {
			if (this.selectedColumn == null) {
				this.selectedColumn = new HashMap<>();
			} else {
				this.selectedColumn.clear();
			}

			ArrayList rowFields = this.getSelectedRowFields();
			this.selectedColumn.put(PivotTableUtils.PIVOTTABLE_ROWFIELD, rowFields);

			ArrayList columnFields = this.getSelectedColumnFields();
			this.selectedColumn.put(PivotTableUtils.PIVOTTABLE_COLUMNFIELD, columnFields);

			ArrayList dataFields = this.getSelectedDataFields();
			this.selectedColumn.put(PivotTableUtils.PIVOTTABLE_DATAFIELD, dataFields);

			Object oc = this.coperation.getItemAt(this.coperation.getSelectedIndex());
			if (oc != null) {
				this.selectedColumn.put(PivotTableUtils.PIVOTTABLE_OPERATION, oc);
			}

			Object od = this.cdategroup.getItemAt(this.cdategroup.getSelectedIndex());
			if (od != null) {
				this.selectedColumn.put(PivotTableUtils.PIVOTTABLE_DATEGROUPOPTIONS, od);
			}

			Object of = this.cformat.getItemAt(this.cformat.getSelectedIndex());
			if (of != null) {
				this.selectedColumn.put(PivotTableUtils.PIVOTTABLE_FORMAT_OPTIONS, of);
			}

			return this.selectedColumn;
		}

		public void setSelectedColumn(Map<Object, Object> h) {
			this.updateEnabled = false;
			try {
				if (this.selectedColumn == null) {
					this.selectedColumn = new HashMap<>();
				} else {
					this.selectedColumn.clear();
				}

				for (int i = 0; i < this.arrayCrowField.length; i++) {
					this.arrayCrowField[i].setSelectedItem(null);
				}

				if (h.containsKey(PivotTableUtils.PIVOTTABLE_ROWFIELD)) {
					Object v = h.get(PivotTableUtils.PIVOTTABLE_ROWFIELD);
					if (v instanceof String) {
						this.rowFieldWidth.clear();
						this.arrayCrowField[0].setSelectedItem(v);
					} else if (v instanceof Pair<?, ?>) {
						this.rowFieldWidth.clear();
						this.rowFieldWidth.add((Pair<String, Integer>) v);
						this.arrayCrowField[0].setSelectedItem(((Pair<String, Integer>) v).getFirst());
					} else if (v instanceof ArrayList) {
						this.rowFieldWidth.clear();
						ArrayList list = (ArrayList) v;
						for (int i = 0; i < list.size(); i++) {
							Object item = list.get(i);
							if (item instanceof Pair<?, ?>) {
								this.rowFieldWidth.add((Pair<String, Integer>) item);
								item = ((Pair<String, Integer>) item).getFirst();
							}
							this.arrayCrowField[i].setSelectedItem(item);
						}
					}
				}

				if (h.containsKey(PivotTableUtils.PIVOTTABLE_COLUMNFIELD)) {
					Object v = h.get(PivotTableUtils.PIVOTTABLE_COLUMNFIELD);
					if (v instanceof String) {
						this.ccolumnField.setSelectedItem(v);
					} else if (v instanceof ArrayList) {
						ArrayList list = (ArrayList) v;
						for (int i = 0; i < list.size(); i++) {
							this.ccolumnField.setSelectedItem(list.get(i));
						}
					}
				}

				this.cdataField.setSelectedItem(null);
				if (h.containsKey(PivotTableUtils.PIVOTTABLE_DATAFIELD)) {
					Object v = h.get(PivotTableUtils.PIVOTTABLE_DATAFIELD);
					if (v instanceof String) {
						this.cdataField.setSelectedItem(v);
					} else if (v instanceof ArrayList) {
						ArrayList list = (ArrayList) v;
						this.cdataField.setSelectedItem(list.get(0));
					}
				}

				if (h.containsKey(PivotTableUtils.PIVOTTABLE_OPERATION)) {
					Object v = h.get(PivotTableUtils.PIVOTTABLE_OPERATION);
					this.coperation.setSelectedItem(v);
				}

				this.cdategroup.setSelectedItem(null);
				if (h.containsKey(PivotTableUtils.PIVOTTABLE_DATEGROUPOPTIONS)) {
					Object v = h.get(PivotTableUtils.PIVOTTABLE_DATEGROUPOPTIONS);
					this.cdategroup.setSelectedItem(v);
				}

				this.cformat.setSelectedItem(PivotTableUtils.PIVOTTABLE_FORMAT_STANDARD_STRING);
				if (h.containsKey(PivotTableUtils.PIVOTTABLE_FORMAT_OPTIONS)) {
					Object v = h.get(PivotTableUtils.PIVOTTABLE_FORMAT_OPTIONS);
					this.cformat.setSelectedItem(v);
				}
			} catch (Exception e) {
				PivotTableUtils.logger.error(null, e);
			}
			this.updateEnabled = true;
			this.updateTable();
		}

		protected void configure(JComboBox[] r) {
			for (int i = 0; i < r.length; i++) {
				this.configure(r[i]);
			}
		}

		protected void configure(JComboBox c) {
			ArrayList cols = new ArrayList();
			for (int i = 0; i < this.model.getColumnCount(); i++) {
				cols.add(this.model.getColumnName(i));
			}
			Collections.sort(cols, new BundleComparator(PivotPanel.this.resources));
			cols.add(0, null);
			c.setRenderer(new TranslateListRenderer(this.resources));
			DefaultComboBoxModel listmodel = new DefaultComboBoxModel(cols.toArray());
			c.setModel(listmodel);

			c.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if ((e.getSource() == PivotPanel.this.arrayCrowField[0]) || (e.getSource() == PivotPanel.this.arrayCrowField[1]) || (e
							.getSource() == PivotPanel.this.ccolumnField) || (e.getSource() == PivotPanel.this.arrayCrowField[2]) || (e
									.getSource() == PivotPanel.this.arrayCrowField[3]) || (e.getSource() == PivotPanel.this.arrayCrowField[4])) {
						Object col1 = PivotPanel.this.arrayCrowField[0].getSelectedItem();
						Object col3 = PivotPanel.this.arrayCrowField[1].getSelectedItem();
						Object col4 = PivotPanel.this.arrayCrowField[2].getSelectedItem();
						Object col5 = PivotPanel.this.arrayCrowField[3].getSelectedItem();
						Object col6 = PivotPanel.this.arrayCrowField[4].getSelectedItem();
						Object col2 = PivotPanel.this.ccolumnField.getSelectedItem();
						boolean date = false;
						if (col1 != null) {
							if ((PivotPanel.this.model.getColumnClass(PivotPanel.columnIndex(PivotPanel.this.model, (String) col1)) == Date.class) || (PivotPanel.this.model
									.getColumnClass(PivotPanel.columnIndex(PivotPanel.this.model, (String) col1)).getSuperclass() == Date.class)) {
								date = true;
							}
						}
						if (col2 != null) {
							if ((PivotPanel.this.model.getColumnClass(PivotPanel.columnIndex(PivotPanel.this.model, (String) col2)) == Date.class) || (PivotPanel.this.model
									.getColumnClass(PivotPanel.columnIndex(PivotPanel.this.model, (String) col2)).getSuperclass() == Date.class)) {
								date = true;
							}
						}
						if (col3 != null) {
							if ((PivotPanel.this.model.getColumnClass(PivotPanel.columnIndex(PivotPanel.this.model, (String) col3)) == Date.class) || (PivotPanel.this.model
									.getColumnClass(PivotPanel.columnIndex(PivotPanel.this.model, (String) col3)).getSuperclass() == Date.class)) {
								date = true;
							}
						}
						if (col4 != null) {
							if ((PivotPanel.this.model.getColumnClass(PivotPanel.columnIndex(PivotPanel.this.model, (String) col4)) == Date.class) || (PivotPanel.this.model
									.getColumnClass(PivotPanel.columnIndex(PivotPanel.this.model, (String) col4)).getSuperclass() == Date.class)) {
								date = true;
							}
						}
						if (col5 != null) {
							if ((PivotPanel.this.model.getColumnClass(PivotPanel.columnIndex(PivotPanel.this.model, (String) col5)) == Date.class) || (PivotPanel.this.model
									.getColumnClass(PivotPanel.columnIndex(PivotPanel.this.model, (String) col5)).getSuperclass() == Date.class)) {
								date = true;
							}
						}
						if (col6 != null) {
							if ((PivotPanel.this.model.getColumnClass(PivotPanel.columnIndex(PivotPanel.this.model, (String) col6)) == Date.class) || (PivotPanel.this.model
									.getColumnClass(PivotPanel.columnIndex(PivotPanel.this.model, (String) col6)).getSuperclass() == Date.class)) {
								date = true;
							}
						}
						if (date) {
							if (!PivotPanel.this.cdategroup.isEnabled()) {
								PivotPanel.this.cdategroup.setEnabled(true);
								PivotPanel.this.cdategroup.setSelectedIndex(0);
							}
						} else {
							PivotPanel.this.cdategroup.setEnabled(false);
						}
					}
					PivotPanel.this.updateTable();
				}
			});
		}

		protected void updateTableOperation() {
			if (this.updateEnabled) {
				if (this.fixedColumnTable.getModel() instanceof TotalTableModel) {
					((TotalTableModel) this.fixedColumnTable.getModel()).setOperation(this.coperation.getSelectedIndex());
				}
			}
		}

		protected void updateTable() {
			try {
				if (this.updateEnabled) {
					ArrayList rowFields = this.getSelectedRowFields();
					ArrayList columnFields = this.getSelectedColumnFields();
					ArrayList dataFields = this.getSelectedDataFields();

					if (rowFields.isEmpty() || columnFields.isEmpty() || dataFields.isEmpty()) {
						this.clearTable();
					} else {
						String dataColumn = (String) dataFields.get(0);
						String[] rows = null;
						if (!rowFields.isEmpty()) {
							rows = (String[]) rowFields.toArray(new String[] {});
						}
						String column = null;
						if (!columnFields.isEmpty()) {
							column = (String) columnFields.get(0);
						}

						TableModel pModel = PivotPanel.create(this.model, rows, column, dataColumn, this.coperation.getSelectedIndex(), this.cdategroup.getSelectedIndex());
						this.fixedColumnTable.setModel(pModel, rows.length, this.cformat.getSelectedItem());
						if (!this.rowFieldWidth.isEmpty()) {
							this.fixedColumnTable.setFixedColumnWidth(this.rowFieldWidth);
						}

					}
				}
			} catch (Exception e) {
				PivotTableUtils.logger.trace(null, e);
				JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}

		public static TableModel create(TableModel orig, String[] rows, String column, String dataColumn, int operation, int dategroup) {
			return PivotPanel.create(orig, rows, column, dataColumn, operation, dategroup, null);
		}

		public static TableModel create(TableModel orig, String[] rows, String column, String dataColumn, int operation, int dategroup, Map<Object, Object> comparators) {
			if (dataColumn == null) {
				throw new IllegalArgumentException("need datacolumn");
			}
			long t = System.currentTimeMillis();
			boolean dataNumeric = false;
			int index = PivotPanel.columnIndex(orig, dataColumn);
			Class cl = orig.getColumnClass(index);
			if (cl.getSuperclass() == Number.class) {
				dataNumeric = true;
			}

			int[] rowIndexes = new int[rows.length + 1];
			for (int i = 0; i < rows.length; i++) {
				rowIndexes[i] = PivotPanel.columnIndex(orig, rows[i]);
			}
			rowIndexes[rows.length] = PivotPanel.columnIndex(orig, column);
			orig = new DateGroupTableModel(orig, rowIndexes, dategroup);

			// TableModel
			DefaultTableModel model = new DefaultTableModel() {

				@Override
				public void addColumn(Object columnName, Object[] columnData) {
					if (columnName == null) {
						super.addColumn("pivottable.unknown.colitem", columnData);
					} else {
						super.addColumn(columnName, columnData);
					}
				}
			};
			if (rows != null) {
				// For this row, all the column values
				if (column != null) {
					Comparator columnComparator = null;
					if ((comparators != null) && comparators.containsKey(column)) {
						columnComparator = (Comparator) comparators.get(column);
					}

					Comparator[] rowComparator = new Comparator[rows.length];
					for (int i = 0; i < rows.length; i++) {
						if ((comparators != null) && comparators.containsKey(rows[i])) {
							rowComparator[i] = (Comparator) comparators.get(rows[i]);
						}
					}

					ArrayList colItems = PivotPanel.getItems(orig, column, columnComparator);
					ArrayList rowItems = PivotPanel.getItems(orig, rows, rowComparator);
					int[] rindex = PivotPanel.columnIndex(orig, rows);
					int cindex = PivotPanel.columnIndex(orig, column);
					// TableModel contains : columns = colItems, rows = rowItems
					// The first column contains: column name: row, values
					// rowItems
					// el rest, names = colItems
					if (colItems.size() > 1024) {
						throw new IllegalArgumentException("Too many columns. 1024 allowed");
					}

					for (int i = 0; i < rows.length; i++) {
						ArrayList data = new ArrayList();
						for (int j = 0; j < rowItems.size(); j++) {
							GroupValue gv = (GroupValue) rowItems.get(j);
							data.add(gv.values[i]);
						}
						model.addColumn(rows[i], data.toArray());
					}

					ArrayList[] data = new ArrayList[colItems.size()];
					for (int i = 0; i < data.length; i++) {
						data[i] = new ArrayList();
						for (int j = 0; j < rowItems.size(); j++) {
							data[i].add(j, null);
						}
					}
					// Get all the table rows
					for (int i = 0; i < orig.getRowCount(); i++) {
						// Get the values of the specified columns
						Object[] vrows = new Object[rindex.length];
						for (int j = 0; j < rindex.length; j++) {
							vrows[j] = orig.getValueAt(i, rindex[j]);
						}
						GroupValue gv = new GroupValue(rows, vrows, rowComparator);
						Object vc = orig.getValueAt(i, cindex);
						Object val = orig.getValueAt(i, index);
						int iCols = colItems.indexOf(vc);
						int iRows = rowItems.indexOf(gv);
						if ((iCols >= 0) && (iRows >= 0)) {
							Object ant = data[iCols].get(iRows);
							if (ant == null) {
								ant = new Value();
							}
							if (dataNumeric) {
								// 5.2068EN-0.6
								// When val is null, it is counted but ant
								// object is not
								// reset
								if (val instanceof Number) {
									((Value) ant).add(((Number) val).doubleValue());
								} else {
									((Value) ant).count(true);
								}

							} else {
								((Value) ant).count();
							}
							data[iCols].set(iRows, ant);
						}
					}
					for (int i = 0; i < data.length; i++) {
						model.addColumn(colItems.get(i), data[i].toArray());
					}
				} else {
				}
			}
			if (ApplicationManager.DEBUG_TIMES) {
				PivotTableUtils.logger.debug("Model time: " + (System.currentTimeMillis() - t));
			}
			TotalTableModel totalTableModel = new TotalTableModel(model, operation, rows.length);
			totalTableModel.setDateGroupTableModel((DateGroupTableModel) orig);
			totalTableModel.setColumnName(column);

			return totalTableModel;
		}

		public TableCellRenderer getFormatRender(int selectedRenderer) {
			return null;
		}

		protected static class NullComparator implements Comparator {

			@Override
			public int compare(Object o1, Object o2) {
				if ((o1 == null) && (o2 == null)) {
					return 0;
				}
				if ((o1 == null) && (o2 != null)) {
					return -1;
				}
				if ((o2 == null) && (o1 != null)) {
					return 1;
				}
				if ((o1 instanceof Comparable) && (o2 instanceof Comparable)) {
					return ((Comparable) o1).compareTo(o2);
				}
				return 0;
			}

		}

		protected static NullComparator comparator = new NullComparator();

		protected static ArrayList getItems(TableModel model, String column, Comparator currentComparator) {
			int iColumn = PivotPanel.columnIndex(model, column);
			ArrayList columnItems = new ArrayList();
			for (int i = 0; i < model.getRowCount(); i++) {
				Object v = model.getValueAt(i, iColumn);
				if (!columnItems.contains(v)) {
					columnItems.add(v);
				}
			}

			if (currentComparator == null) {
				Collections.sort(columnItems, PivotPanel.comparator);
			} else {
				Collections.sort(columnItems, currentComparator);
			}

			return columnItems;
		}

		protected static ArrayList getItems(TableModel model, String[] columns, Comparator[] comparators) {
			int[] columnIndexes = new int[columns.length];

			for (int i = 0; i < columns.length; i++) {
				columnIndexes[i] = PivotPanel.columnIndex(model, columns[i]);
			}
			ArrayList columnItems = new ArrayList();
			for (int i = 0; i < model.getRowCount(); i++) {
				Object[] values = new Object[columns.length];
				for (int j = 0; j < columnIndexes.length; j++) {
					values[j] = model.getValueAt(i, columnIndexes[j]);
				}
				GroupValue gv = new GroupValue(columns, values, comparators);
				if (!columnItems.contains(gv)) {
					columnItems.add(gv);
				}
			}
			Collections.sort(columnItems, PivotPanel.comparator);
			return columnItems;
		}

		protected static int columnIndex(TableModel model, String col) {
			if (col == null) {
				return -1;
			}
			for (int i = 0; i < model.getColumnCount(); i++) {
				if (model.getColumnName(i).equals(col)) {
					return i;
				}
			}
			return -1;
		}

		protected static int[] columnIndex(TableModel model, String[] col) {
			if (col == null) {
				return null;
			}
			int[] cols = new int[col.length];
			for (int i = 0; i < cols.length; i++) {
				cols[i] = -1;
			}
			for (int j = 0; j < col.length; j++) {
				for (int i = 0; i < model.getColumnCount(); i++) {
					if (model.getColumnName(i).equals(col[j])) {
						cols[j] = i;
					}
				}
			}
			return cols;
		}

		protected void clearTable() {
			this.fixedColumnTable.setModel(new DefaultTableModel(), 0, this.cformat.getSelectedItem());
		}

		public ArrayList getSelectedRowFields() {
			ArrayList slist = new ArrayList();
			for (int i = 0; i < this.arrayCrowField.length; i++) {
				if (this.arrayCrowField[i].getSelectedItem() != null) {
					slist.add(this.arrayCrowField[i].getSelectedItem());
				}
			}
			return slist;
		}

		public ArrayList getSelectedColumnFields() {
			ArrayList slist = new ArrayList();
			if (this.ccolumnField.getSelectedItem() != null) {
				slist.add(this.ccolumnField.getSelectedItem());
			}
			return slist;
		}

		public ArrayList getSelectedDataFields() {
			ArrayList slist = new ArrayList();
			if (this.cdataField.getSelectedItem() != null) {
				slist.add(this.cdataField.getSelectedItem());
			}
			return slist;
		}

		public String getSelectedOperation() {
			Object current = this.coperation.getSelectedItem();
			if (current == null) {
				return null;
			}
			return current.toString();
		}

		public String getSelectedDateGroup() {
			Object current = this.cdategroup.getSelectedItem();
			if (current == null) {
				return null;
			}
			return current.toString();
		}

		public List<Pair> getFixedColumnWidth() {
			return this.fixedColumnTable.getFixedColumnWidth();
		}

	}

	public static JDialog createPivotDialog(Window w, TableModel model, ResourceBundle res) {
		return PivotTableUtils.createPivotDialog(w, model, res, null, null);
	}

	public static JDialog createPivotDialog(Window w, TableModel model, ResourceBundle res, Map<Object, Object> parameters, String originalColPosAndWith) {
		PivotDialog pd = null;
		if (w instanceof Dialog) {
			pd = new PivotDialog((Dialog) w, model, res, parameters, originalColPosAndWith);
		} else {
			pd = new PivotDialog((Frame) w, model, res, parameters, originalColPosAndWith);
		}
		pd.pack();
		return pd;
	}

	public static String translate(String text, ResourceBundle res, Object[] args) {
		if (res == null) {
			return new String(text);
		} else {
			try {
				String trad = res.getString(text);
				if (trad != null) {
					if (trad.startsWith("<HTML>") || trad.startsWith("<html>") || trad.startsWith("<Html>")) {
						int index = trad.indexOf("<DEFAULTBASE>");
						if (index >= 0) {
							URL url = PivotTableUtils.class.getClassLoader().getResource("./");
							if (url != null) {
								trad = trad.substring(0, index) + "<BASE href=\"" + url.toString() + "\">" + trad.substring(index + 13);
								PivotTableUtils.logger.debug("Establecida BASE : " + url.toString());
							}
						}
					}
				}
				// Arguments
				if (args != null) {
					String transArgs = MessageFormat.format(trad, args);
					return transArgs;
				} else {
					return trad;
				}
			} catch (Exception e) {
				PivotTableUtils.logger.error(null, e);
				return new String(text);
			}
		}
	}

	public static class FixedColumnTable extends JSplitPane implements ChangeListener, PropertyChangeListener {

		private static final Logger	logger	= LoggerFactory.getLogger(PivotTableUtils.FixedColumnTable.class);

		private final JTable		main;

		private final JTable		fixed;

		protected JScrollPane		mainScrollPane;

		protected JScrollPane		fixedScrollPane;

		protected HeaderRenderer	headerRenderer;

		/*
		 * Specify the number of columns to be fixed and the scroll pane containing the table.
		 */
		public FixedColumnTable(Map<Object, Object> parameters, ResourceBundle resources) {
			this.main = new JTable();
			this.mainScrollPane = new JScrollPane(this.main, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
			this.mainScrollPane.addPropertyChangeListener(this);
			this.setDividerSize(2);
			this.setDividerLocation(0);

			// Use the existing table to create a new table sharing
			// the DataModel and ListSelectionModel
			this.fixed = new JTable();
			this.fixed.setAutoCreateColumnsFromModel(false);
			this.fixed.setModel(this.main.getModel());
			this.fixed.setSelectionModel(this.main.getSelectionModel());
			this.fixed.setFocusable(false);
			// fixed.setShowGrid(false);

			this.fixedScrollPane = new JScrollPane(this.fixed, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
			this.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
			// this.fixedScrollPane.addMouseWheelListener(new
			// MouseWheelListener() {
			// @Override
			// public void mouseWheelMoved(MouseWheelEvent e) {
			// int direction = e.getWheelRotation() < 0 ? -1 : 1;
			// int unit = e.getUnitsToScroll();
			// int position =
			// FixedColumnTable.this.fixedScrollPane.getVerticalScrollBar().getValue();
			// int newposition = position + unit;
			// FixedColumnTable.this.fixedScrollPane.getVerticalScrollBar().setValue(newposition);
			// }
			// });
			// this.fixedScrollPane.setWheelScrollingEnabled(true);

			this.setLeftComponent(this.fixedScrollPane);
			this.setRightComponent(this.mainScrollPane);

			// Synchronized too bars
			JScrollBar fixedBar = this.fixedScrollPane.getVerticalScrollBar();
			JScrollBar mainBar = this.mainScrollPane.getVerticalScrollBar();
			fixedBar.setModel(mainBar.getModel());

			// treeTableScroll.addMouseWheelListener(new MouseWheelListener(){
			// public void mouseWheelMoved(MouseWheelEvent e) {
			// int unit = e.getUnitsToScroll();
			// if (MouseWheelEvent.WHEEL_UNIT_SCROLL==e.getScrollType()){
			// if (unit>0){
			// treeBar.getModel().setValue(treeBar.getModel().getValue()+chart.getScrollableUnitIncrement(null,
			// 0, 0));
			// }else{
			// treeBar.getModel().setValue(treeBar.getModel().getValue()-chart.getScrollableUnitIncrement(null,
			// 0, 0));
			// }
			// }
			// }
			// });

			this.fixedScrollPane.setBorder(
					ParseUtils.getBorder(parameters != null ? (String) parameters.get("border") : null, BorderManager.getBorder(BorderManager.DEFAULT_TABLE_BORDER_KEY)));

			this.fixed.setDefaultRenderer(Object.class, new NumberRenderer());

			this.fixed.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			this.main.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

			this.fixed.getTableHeader().setReorderingAllowed(false);
			this.fixed.setRowMargin(0);
			this.main.getTableHeader().setReorderingAllowed(false);
			this.main.setRowMargin(0);
			this.headerRenderer = new HeaderRenderer(resources, parameters);

			this.main.getTableHeader().setDefaultRenderer(new MainHeaderRenderer(resources, parameters));
			this.fixed.getTableHeader().setDefaultRenderer(this.headerRenderer);
			CopyAction copyAction = new CopyAction();

			KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
			this.main.registerKeyboardAction(copyAction, "Copy", copy, JComponent.WHEN_FOCUSED);

			// InputMap inMap = main.getInputMap(JComponent.WHEN_FOCUSED);
			// ActionMap actMap = main.getActionMap();
			//
			// InputMap inMap2 = fixed.getInputMap(JComponent.WHEN_FOCUSED);
			// ActionMap actMap2 = fixed.getActionMap();

		}

		public TableModel getModel() {
			return this.main.getModel();
		}

		public void setModel(TableModel dataModel, int rows, Object dataformat) {
			this.main.setAutoCreateColumnsFromModel(true);
			this.main.setModel(dataModel);
			this.fixed.setAutoCreateColumnsFromModel(false);
			while (this.fixed.getColumnCount() > 0) {
				TableColumn column = this.fixed.getColumnModel().getColumn(0);
				this.fixed.getColumnModel().removeColumn(column);
			}
			this.fixed.setModel(dataModel);

			for (int i = 0; i < rows; i++) {
				TableColumnModel columnModel = this.main.getColumnModel();
				TableColumn column = columnModel.getColumn(0);
				columnModel.removeColumn(column);
				this.fixed.getColumnModel().addColumn(column);
			}

			NumberRenderer nR = PivotTableUtils.getRenderInstance(dataformat.toString(), new Object[] {});
			this.main.setDefaultRenderer(Object.class, nR);
			// this.main.setDefaultRenderer(Object.class, new
			// RealCellRenderer());

			this.fixed.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

			for (int i = 0; i < this.fixed.getColumnCount(); i++) {
				this.fixed.getColumnModel().getColumn(i).setCellRenderer(new RowHeaderRenderer());
			}

			for (int i = 0; i < this.fixed.getColumnModel().getColumnCount(); i++) {
				this.fixed.getColumnModel().getColumn(i).sizeWidthToFit();
			}

			if (dataModel instanceof TotalTableModel) {
				PivotTableUtils.getRenderInstance(dataformat.toString(), new Object[] { Color.lightGray });
				this.main.getColumnModel().getColumn(this.main.getColumnCount() - 1)
						.setCellRenderer(PivotTableUtils.getRenderInstance(dataformat.toString(), new Object[] { Color.lightGray }));
			}

			for (int i = 0; i < this.main.getColumnModel().getColumnCount(); i++) {
				this.adjustColumn(this.main, i, 10);
			}

			if (rows == 0) {
				this.setDividerLocation(0);
			} else {
				this.setDividerLocation(this.fixed.getPreferredSize().width);
			}

		}

		public List<Pair> getFixedColumnWidth() {
			List<Pair> list = new ArrayList<Pair>();
			for (int i = 0; i < this.fixed.getColumnCount(); i++) {
				TableColumn tC = this.fixed.getColumnModel().getColumn(i);
				int width = tC.getWidth();
				String name = tC.getIdentifier().toString();
				list.add(new Pair<String, Integer>(name, width));
			}
			return list;
		}

		public void setFixedColumnWidth(List<Pair<String, Integer>> columns) {
			List<Pair<String, Integer>> list = columns;
			int widthScrollPane = 0;

			for (Pair<String, Integer> p : list) {
				widthScrollPane += p.getSecond();
			}
			this.setDividerLocation(widthScrollPane);

			for (int i = 0; i < list.size(); i++) {
				Pair<String, Integer> p = list.get(i);
				TableColumn tC = this.fixed.getColumnModel().getColumn(i);
				widthScrollPane += p.getSecond();
				tC.setWidth(p.getSecond());
				tC.setPreferredWidth(p.getSecond());
			}
			this.fixed.doLayout();
		}

		public void adjustColumn(JTable table, int column, int margin) {
			DefaultTableColumnModel colModel = (DefaultTableColumnModel) table.getColumnModel();
			TableColumn tColumns = colModel.getColumn(column);

			TableCellRenderer renderer = tColumns.getHeaderRenderer();
			if (renderer == null) {
				renderer = table.getTableHeader().getDefaultRenderer();
			}
			JLabel label = (JLabel) renderer.getTableCellRendererComponent(table, tColumns.getHeaderValue(), false, false, 0, 0);
			int colWidth = label.getPreferredSize().width;

			for (int i = 0; i < table.getRowCount(); i++) {
				renderer = table.getCellRenderer(i, column);
				label = (JLabel) renderer.getTableCellRendererComponent(table, table.getValueAt(i, column), false, false, i, column);
				int currentWidth = label.getPreferredSize().width;
				colWidth = Math.max(colWidth, currentWidth);
			}

			colWidth += margin;

			tColumns.setPreferredWidth(colWidth);
		}

		/*
		 * Return the table being used in the row header
		 */
		public JTable getFixedTable() {
			return this.fixed;
		}

		public JTable getMainTable() {
			return this.main;
		}

		public TableColumn getColumn(int index) {
			if (this.fixed.getColumnCount() > index) {
				return this.fixed.getColumnModel().getColumn(index);
			} else {
				return this.main.getColumnModel().getColumn(index - this.fixed.getColumnCount());
			}
		}

		public JTable getTableFromColumn(int column) {
			if (this.fixed.getColumnCount() > column) {
				return this.fixed;
			} else {
				return this.main;
			}
		}

		public int indexAtTable(JTable table, int column) {
			if (table.equals(this.fixed)) {
				return column;
			}
			return column - this.fixed.getColumnCount();
		}

		public int getRowCount() {
			if (this.main != null) {
				return this.main.getRowCount();
			}
			return 0;
		}

		public int getColumnCount() {
			int total = 0;
			if (this.fixed != null) {
				total = this.fixed.getColumnCount();
			}
			if (this.main != null) {
				total = total + this.main.getColumnCount();
			}
			return total;
		}

		//
		// Implement the ChangeListener
		//
		@Override
		public void stateChanged(ChangeEvent e) {
			// Sync the scroll pane scrollbar with the row header
			// JViewport viewport = (JViewport) e.getSource();
			// scrollPane.getVerticalScrollBar().setValue(viewport.getViewPosition().y);
		}

		//
		// Implement the PropertyChangeListener
		//
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			// Keep the fixed table in sync with the main table
			if ("selectionModel".equals(e.getPropertyName())) {
				this.fixed.setSelectionModel(this.main.getSelectionModel());
			}
			// if ("model".equals(e.getPropertyName()))
			// {
			// fixed.setModel( main.getModel() );
			// }
		}

		public String getDataString(int row) {
			StringBuilder sbValues = new StringBuilder();
			for (int i = 0; i < this.getColumnCount(); i++) {
				TableColumn tc = this.getColumn(i);
				JTable table = this.getTableFromColumn(i);
				int index = this.indexAtTable(table, i);

				if (tc.getIdentifier() != null) {
					Object oValue = table.getValueAt(row, index);
					TableCellRenderer r = table.getCellRenderer(row, index);
					Component c = r.getTableCellRendererComponent(table, oValue, false, false, row, index);
					if (c instanceof JLabel) {
						String sText = ((JLabel) c).getText();
						sbValues.append("\"");
						sbValues.append(sText);
						sbValues.append("\"");
						sbValues.append("\t");
						continue;
					} else if (c instanceof JTextComponent) {
						String texto = ((JTextComponent) c).getText();
						sbValues.append("\"");
						sbValues.append(texto);
						sbValues.append("\"");
						sbValues.append("\t");
						continue;
					} else {
						String sText = "";
						if (oValue != null) {
							if (oValue instanceof Boolean) {
								if (((Boolean) oValue).booleanValue()) {
									sText = "Si";
								} else {
									sText = "No";
								}
							} else {
								sText = oValue.toString();
							}
						}
						sbValues.append("\"");
						sbValues.append(sText);
						sbValues.append("\"");
						sbValues.append("\t");
						continue;
					}
				}

			}
			return sbValues.toString();
		}

		public String getDataString() {
			StringBuilder cabecera = new StringBuilder();
			for (int i = 0; i < this.getColumnCount(); i++) {
				TableColumn tc = this.getColumn(i);
				cabecera.append("\"");
				cabecera.append(tc.getHeaderValue());
				cabecera.append("\"");
				cabecera.append("\t");
			}

			StringBuilder sbValues = new StringBuilder("");

			for (int j = 0; j < this.getRowCount(); j++) {
				sbValues.append("\n");
				sbValues.append(this.getDataString(j));
			}
			sbValues.append("\n");
			StringBuilder total = new StringBuilder();
			total.append(cabecera.toString());
			total.append(sbValues.toString());
			return total.toString();
		}

		protected class CopyAction extends AbstractAction {

			@Override
			public void actionPerformed(ActionEvent e) {
				Cursor c = FixedColumnTable.this.getCursor();
				try {
					((JComponent) e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					String selection;
					if ((FixedColumnTable.this.main != null) && (FixedColumnTable.this.main.getSelectedRowCount() > 0)) {
						int[] selectedRows = FixedColumnTable.this.main.getSelectedRows();
						StringBuilder sbValues = new StringBuilder();
						for (int i = 0; i < selectedRows.length; i++) {
							sbValues.append(FixedColumnTable.this.getDataString(selectedRows[i]));
							sbValues.append("\n");
						}
						selection = sbValues.toString();
					} else {
						selection = FixedColumnTable.this.getDataString();
					}
					final StringSelection sselection = new StringSelection(selection);
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sselection, sselection);
				} catch (Exception ex) {
					PivotTableUtils.logger.trace(null, ex);
					// If this is a SecurityException
					if (ex instanceof SecurityException) {
						java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {

							@Override
							public Object run() {
								JTextField campoTexto = new JTextField();
								campoTexto.setText(FixedColumnTable.this.getDataString());
								campoTexto.selectAll();
								campoTexto.copy();
								if (ApplicationManager.DEBUG) {
									PivotTableUtils.logger.debug("Copied to clipboard");
								}
								return null;
							}
						});
					}
				} finally {
					((JTable) e.getSource()).setCursor(c);
				}
			}

		}

		/**
		 * Returns the preferred width for the column passed as param.
		 * 
		 * @param modelColumnIndex
		 *            the index of the column
		 * @param maxRowNumber
		 *            the max number of rows to check
		 * @return
		 */
		protected int getPreferredColumnWidth(JTable table, int modelColumnIndex, int maxRowNumber) {
			int width = 0;
			String sName = table.getColumnName(modelColumnIndex);

			TableColumn tableColumn = table.getColumn(sName);
			if (sName.equals(ExtendedTableModel.ROW_NUMBERS_COLUMN)) {
				width = tableColumn.getPreferredWidth();
				return width;
			}

			// If there is no data then initialize the columns with using the
			// header
			try {
				// JRE 1.2 does not contain the function
				// TableCellRenderer.getDefaultRenderer()

				JTableHeader header = table.getTableHeader();
				TableCellRenderer headerRenderer = header.getDefaultRenderer();
				Object oHeaderValue = tableColumn.getHeaderValue();
				Component hederRendererComponent = headerRenderer.getTableCellRendererComponent(table, oHeaderValue, false, false, 0, 1);
				int headerPreferredWidth = hederRendererComponent.getPreferredSize().width;
				if (hederRendererComponent instanceof JLabel) {
					FontMetrics metrics = ((JLabel) hederRendererComponent).getFontMetrics(((JLabel) hederRendererComponent).getFont());
					if (oHeaderValue != null) {
						headerPreferredWidth = metrics.stringWidth(oHeaderValue.toString());
					}
				} else if (hederRendererComponent instanceof JTextComponent) {
					FontMetrics fontMetrics = ((JTextComponent) hederRendererComponent).getFontMetrics(((JTextComponent) hederRendererComponent).getFont());
					if (oHeaderValue != null) {
						headerPreferredWidth = fontMetrics.stringWidth(oHeaderValue.toString() + 6);
					}
				} else {
					headerPreferredWidth = hederRendererComponent.getPreferredSize().width;
				}
				width = headerPreferredWidth + 4;
			} catch (Exception e) {
				FixedColumnTable.logger.error("Exception initiating table column width. JRE 1.3 or above is required:", e);
				width = tableColumn.getPreferredWidth();
			}

			TableCellRenderer renderer = table.getDefaultRenderer(table.getColumnClass(modelColumnIndex));
			TableCellRenderer cellRenderer2 = table.getColumnModel().getColumn(modelColumnIndex).getCellRenderer();
			if (cellRenderer2 != null) {
				renderer = cellRenderer2;
			}

			for (int j = 0; j < Math.min(table.getRowCount(), maxRowNumber); j++) {
				Object oValue = table.getValueAt(j, modelColumnIndex);
				Component rendererComponent = renderer.getTableCellRendererComponent(table, oValue, false, false, 0, 0);
				int preferredWidth = rendererComponent.getPreferredSize().width;
				if (rendererComponent instanceof JComponent) {
					preferredWidth = preferredWidth - ((JComponent) rendererComponent).getInsets().left - ((JComponent) rendererComponent).getInsets().right;
				}
				if (rendererComponent instanceof JTextField) {
					FontMetrics fontMetrics = ((JTextField) rendererComponent).getFontMetrics(((JTextField) rendererComponent).getFont());
					preferredWidth = fontMetrics.stringWidth(((JTextField) rendererComponent).getText()) + 4;
				} else if (rendererComponent instanceof JLabel) {
					FontMetrics fontMetrics = ((JLabel) rendererComponent).getFontMetrics(((JLabel) rendererComponent).getFont());
					try {
						String text = ((JLabel) rendererComponent).getText();
						if (text == null) {
							text = "";
						}
						preferredWidth = fontMetrics.stringWidth(text) + 4;
					} catch (Exception eM) {
						PivotTableUtils.logger.trace(null, eM);
					}
				}
				width = Math.max(preferredWidth + 5, width);

			}
			return width;
		}

	}

	public static class BundleComparator implements Comparator<String> {

		private final ResourceBundle bundle;

		public BundleComparator(ResourceBundle bundle) {
			this.bundle = bundle;
		}

		@Override
		public int compare(String o1, String o2) {
			String o1Translated = ApplicationManager.getTranslation(o1, this.bundle);
			String o2Translated = ApplicationManager.getTranslation(o2, this.bundle);

			return o1Translated.compareTo(o2Translated);
		}

	};

}
