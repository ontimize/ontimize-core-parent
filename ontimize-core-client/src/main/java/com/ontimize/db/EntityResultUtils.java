package com.ontimize.db;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.images.ImageManager;
import com.ontimize.gui.table.TableSorter;
import com.ontimize.jee.common.db.NullValue;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.util.remote.BytesBlock;
import com.ontimize.util.swing.image.BooleanImage;

public abstract class EntityResultUtils extends EntityResultTools {

	private static final Logger logger = LoggerFactory.getLogger(EntityResultUtils.class);

	public static class EntityResultTableModel extends AbstractTableModel {

		private static final Logger	logger				= LoggerFactory.getLogger(EntityResultUtils.EntityResultTableModel.class);

		private static final Image	check				= ImageManager.getIcon(ImageManager.CHECK_SELECTED).getImage();

		private static final Image	uncheck				= ImageManager.getIcon(ImageManager.CHECK_UNSELECTED).getImage();

		protected List<Object>		columns				= new ArrayList<>();

		protected List<Object>		dataVectors			= new ArrayList<>();

		protected int				rowsNumber			= 0;

		protected EntityResult		res					= null;

		protected boolean			returnEmptyStrings	= false;

		public EntityResultTableModel(EntityResult res) {
			this(res, false, true, true);
		}

		public EntityResultTableModel(EntityResult res, boolean returnEmptyStrings, boolean convertBytesBlockToIm, boolean convertBooleanToIm) {
			super();
			this.res = res;
			this.rowsNumber = res.calculateRecordNumber();
			Enumeration<?> enumKeys = res.keys();
			while (enumKeys.hasMoreElements()) {
				Object oKey = enumKeys.nextElement();
				this.columns.add(oKey);
				this.dataVectors.add(res.get(oKey));
				if (convertBytesBlockToIm || convertBooleanToIm) {
					List<Object> vector = (List<Object>) res.get(oKey);
					for (int i = 0; i < vector.size(); i++) {
						Object v = vector.get(i);
						if ((v instanceof BytesBlock) && convertBytesBlockToIm) {
							try {
								Image im = new ImageIcon(((BytesBlock) v).getBytes()).getImage();
								vector.set(i, im);
							} catch (Exception ex) {
								EntityResultUtils.logger.trace(null, ex);
							}
						} else if ((v instanceof Boolean) && convertBooleanToIm) {
							try {
								boolean bValue = ((Boolean) v).booleanValue();
								Image image = bValue ? EntityResultTableModel.check : EntityResultTableModel.uncheck;
								BufferedImage bImage = com.ontimize.util.twain.TwainUtilities.toBufferedImage(image);
								BooleanImage booleanImage = new BooleanImage(bValue, bImage);
								vector.set(i,booleanImage);
							} catch (Exception ex) {
								EntityResultUtils.logger.trace(null, ex);
							}
						}
					}
				}
			}
			this.returnEmptyStrings = returnEmptyStrings;
		}

		@Override
		public Class<?> getColumnClass(int column) {
			if (column >= this.dataVectors.size()) {
				return super.getColumnClass(column);
			}
			List<?> list = (List<?>) this.dataVectors.get(column);
			for (Object element : list) {
				if (element != null) {
					return element.getClass();
				}
			}
			return super.getColumnClass(column);
		}

		@Override
		public String getColumnName(int c) {
			if (c < this.columns.size()) {
				return this.columns.get(c).toString();
			} else {
				return super.getColumnName(c);
			}
		}

		@Override
		public int getRowCount() {
			return this.rowsNumber;
		}

		@Override
		public int getColumnCount() {
			return this.columns.size();
		}

		@Override
		public Object getValueAt(int f, int c) {
			try {
				if (c >= this.dataVectors.size()) {
					return null;
				}
				List<?> v = (List<?>) this.dataVectors.get(c);
				if (f >= v.size()) {
					return this.returnEmptyStrings ? "" : null;
				}
				return v.get(f);
			} catch (Exception exc) {
				EntityResultUtils.logger.trace(null, exc);
				return this.returnEmptyStrings ? "" : null;
			}
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if (columnIndex >= this.dataVectors.size()) {
				return;
			}
			List<Object> v = (List<Object>) this.dataVectors.get(columnIndex);
			if (rowIndex >= v.size()) {
				return;
			}
			v.set(rowIndex, value);
			this.dataVectors.set(columnIndex, v);
		}

		public EntityResult getEntityResult() {
			return this.res;
		}

	}

	public static TableModel createTableModel(EntityResult res) {
		return new EntityResultTableModel(res);
	}

	public static TableModel createTableModel(EntityResult res, List<?> cols) {
		return EntityResultUtils.createTableModel(res, cols, false);
	}

	public static TableModel createTableModel(EntityResult res, List<?> cols, boolean returnEmptyStrings) {
		return EntityResultUtils.createTableModel(res, cols, returnEmptyStrings, true);
	}

	public static TableModel createTableModel(EntityResult res, List<?> cols, boolean returnEmptyStrings, boolean convertBytesBlockToIm, boolean convertBooleanToIm) {
		EntityResult resN = new EntityResultMapImpl();
		for (Object col : cols) {
			if (res.containsKey(col)) {
				resN.put(col, res.get(col));
			}
		}
		resN.setColumnOrder(cols);
		return new EntityResultTableModel(resN, returnEmptyStrings, convertBytesBlockToIm, convertBooleanToIm);
	}

	/**
	 * @param res
	 *            Values of this object must be Vector elements
	 * @return
	 */
	public static TableModel createTableModel(Map<?, ?> res) {
		return EntityResultUtils.createTableModel(res, false);
	}

	public static TableModel createTableModel(Map<?, ?> res, boolean returnEmptyStrings) {
		return EntityResultUtils.createTableModel(res, returnEmptyStrings, true, true);
	}

	public static TableModel createTableModel(Map<?, ?> res, boolean returnEmptyStrings, boolean convertBB2Im, boolean convertBooleanToIm) {
		EntityResult result = new EntityResultMapImpl();
		for (Entry<?, ?> entry : res.entrySet()) {
			result.put(entry.getKey(), entry.getValue());
		}
		return new EntityResultTableModel(result, returnEmptyStrings, convertBB2Im, convertBooleanToIm);
	}

	public static TableModel createTableModel(Map<?, ?> res, List<?> cols) {
		return EntityResultUtils.createTableModel(res, cols, false);
	}

	public static TableModel createTableModel(Map<?, ?> res, List<?> cols, boolean returnEmptyStrings) {
		return EntityResultUtils.createTableModel(res, cols, returnEmptyStrings, true);
	}

	public static TableModel createTableModel(Map<?, ?> res, List<?> cols, boolean returnEmptyStrings, boolean convertBB2Im) {
		EntityResult resN = new EntityResultMapImpl();
		for (Object col : cols) {
			if (res.containsKey(col)) {
				resN.put(col, res.get(col));
			}
		}
		resN.setColumnOrder(cols);
		return new EntityResultTableModel(resN, returnEmptyStrings, convertBB2Im, true);
	}

	/**
	 * @param entityResult
	 * @param recordValue
	 * @param index
	 * @use EntityResultTools.updateRecordValues(entityResult, recordValue, index);
	 */
	@Deprecated
	public static void updateRecordValues(EntityResult entityResult, Map<?, ?> recordValue, int index) {
		EntityResultTools.updateRecordValues(entityResult, recordValue, index);
	}

	/**
	 * @param entityResult
	 * @param kv
	 * @return
	 * @use EntityResultTools.getValuesKeysIndex(entityResult, kv);
	 */
	@Deprecated
	public static int getValuesKeysIndex(Map<?, ?> entityResult, Map<?, ?> kv) {
		return EntityResultTools.getValuesKeysIndex(entityResult, kv);
	}

	/**
	 * Joins the data in two EntityResult objects. These objects must have the same structure, is they have not it the method uses the structure of res1
	 *
	 * @param r1
	 * @param r2
	 * @return
	 */
	public static EntityResult merge(EntityResult r1, EntityResult r2) {
		if (r1.isEmpty()) {
			return r2.clone();
		}
		if (r2.isEmpty()) {
			return r1.clone();
		}
		// None of them are empty
		EntityResult res1 = r1.clone();
		Enumeration<?> enumKeys = res1.keys();
		int necordNumber2 = r2.calculateRecordNumber();
		while (enumKeys.hasMoreElements()) {
			Object oKey = enumKeys.nextElement();
			List<Object> vValues1 = (List<Object>) res1.get(oKey);
			List<?> vValues2 = (List<?>) r2.get(oKey);
			if (vValues2 == null) {
				for (int i = 0; i < necordNumber2; i++) {
					vValues1.add(vValues1.size(), null);
				}
			} else {
				for (int i = 0; (i < vValues2.size()) && (i < necordNumber2); i++) {
					vValues1.add(vValues1.size(), vValues2.get(i));
				}
				for (int i = vValues1.size(); i < necordNumber2; i++) {
					vValues1.add(vValues1.size(), null);
				}
			}
		}
		return res1;
	}

	/**
	 * Combines all data in the Hashtable h and the EntityResult r2. The combination is done in the next way:<BR> - All pairs (key-value) in the Hashtable h are added in each
	 * record of r2.<BR> - NullValue objects in the Hashtable h are null objects in the result.<BR>
	 *
	 * @param h
	 * @param r2
	 * @return
	 */
	public static EntityResult combine(Map<?, ?> h, EntityResult r2) {
		EntityResult res = r2.clone();
		if (h.isEmpty()) {
			return res;
		}
		int r = res.calculateRecordNumber();
		// If r == 0, combine using 1
		if (r == 0) {
			r = 1;
		}

		for (Entry<?, ?> entry : h.entrySet()) {
			Object oKey = entry.getKey();
			Object oValue = entry.getValue();
			if (oValue instanceof NullValue) {
				oValue = null;
			}
			List<Object> v = new ArrayList<>();
			for (int i = 0; i < r; i++) {
				v.add(i, oValue);
			}
			res.put(oKey, v);
		}
		return res;
	}

	/**
	 * Combines the data from two hashtables. The combination is the next:<BR> - All pairs (key-value) in the Hashtable h are added in h2.<BR> - NullValue objects are null objects
	 * in the result.<BR>
	 *
	 * @param h
	 * @param h2
	 * @return
	 */
	public static EntityResult combine(Map<?, ?> h, Map<?, ?> h2) {
		EntityResult rAux = new EntityResultMapImpl();
		rAux.putAll(h2);
		return EntityResultUtils.combine(h, rAux);
	}

	public static class Order implements Serializable {

		protected String	columnName	= null;

		protected boolean	ascendent	= true;

		public Order(String columnName) {
			this.columnName = columnName;
		}

		public Order(String columnName, boolean ascendent) {
			this.columnName = columnName;
			this.ascendent = ascendent;
		}

		public String getColumnName() {
			return this.columnName;
		}

		public boolean isAscendent() {
			return this.ascendent;
		}

		@Override
		public String toString() {
			return this.columnName;
		}

	}

	public static void addColumn(EntityResult entityResult, String columnName) {
		if (!entityResult.containsKey(columnName)) {
			List<Object> columnRecords = new ArrayList<>(entityResult.calculateRecordNumber());
			entityResult.put(columnName, columnRecords);
		} else {
			EntityResultUtils.logger.warn("{} column already exists in this EntityResult", columnName);
		}
	}

	public static void sort(EntityResult entityResult, List<Order> order) {
		EntityResultSorter sorter = new EntityResultSorter(entityResult, order);
		sorter.sort();
	}

	public static class EntityResultSorter {

		private static final Logger	logger	= LoggerFactory.getLogger(EntityResultUtils.EntityResultSorter.class);

		protected List<Order>		order;

		protected EntityResult		entityResult;

		protected int				totalRecord;

		protected int[]				indexes;

		protected int				compares;

		public EntityResultSorter(EntityResult entityResult, List<Order> order) {
			this.entityResult = entityResult;
			this.order = order;
			this.init();
		}

		protected void init() {
			this.totalRecord = this.entityResult.calculateRecordNumber();
			this.indexes = new int[this.totalRecord];
			for (int row = 0; row < this.totalRecord; row++) {
				this.indexes[row] = row;
			}
		}

		public void sort() {
			this.compares = 0;
			long t = System.currentTimeMillis();
			EntityResultSorter.logger.debug("Sorting of an array of {} records from 0 up to {}", this.indexes.length, this.indexes.length - 1);
			this.shuttleSort(this.indexes.clone(), this.indexes, 0, this.totalRecord);
			EntityResultSorter.logger.debug(" Indexes result : {}", this.indexes);
			this.relocatedRecords();
			long t2 = System.currentTimeMillis();
			EntityResultSorter.logger.trace(" Sorting time ShuttleSort : {} millisecs", t2 - t);

		}

		protected void relocatedRecords() {
			Object[] records = new Object[this.totalRecord];
			for (int t = this.totalRecord - 1; t >= 0; t--) {
				records[t] = this.entityResult.getRecordValues(t);
				this.entityResult.deleteRecord(t);
			}
			for (int i = 0; i < this.indexes.length; i++) {
				this.entityResult.addRecord((Map<?, ?>) records[this.indexes[i]], i);
			}

		}

		/**
		 * Fast algorithm to sort an array.
		 *
		 * @param from
		 *            the original array
		 * @param to
		 *            the sorted array
		 * @param low
		 *            the starting index (typically 0)
		 * @param high
		 *            the ending index (typically from.length)
		 */
		public void shuttleSort(int[] from, int[] to, int low, int high) {
			if ((high - low) < 2) {
				return;
			}
			int middle = (low + high) / 2;
			this.shuttleSort(to, from, low, middle);
			this.shuttleSort(to, from, middle, high);

			int p = low;
			int q = middle;

			/*
			 * This is an optional short-cut; at each recursive call, check to see if the elements in this subset are already ordered. If so, no further comparisons are needed; the
			 * sub-array can just be copied. The array must be copied rather than assigned otherwise sister calls in the recursion might get out of sinc. When the number of
			 * elements is three they are partitioned so that the first set, [low, mid), has one element and and the second, [mid, high), has two. We skip the optimisation when the
			 * number of elements is three or less as the first compare in the normal merge will produce the same sequence of steps. This optimisation seems to be worthwhile for
			 * partially ordered lists but some analysis is needed to find out how the performance drops to Nlog(N) as the initial order diminishes - it may drop very quickly.
			 */

			if (((high - low) >= 4) && (this.compare(from[middle - 1], from[middle]) <= 0)) {
				for (int i = low; i < high; i++) {
					to[i] = from[i];
				}
				return;
			}

			// A normal merge.

			for (int i = low; i < high; i++) {
				if ((q >= high) || ((p < middle) && (this.compare(from[p], from[q]) <= 0))) {
					to[i] = from[p++];
				} else {
					to[i] = from[q++];
				}
			}
		}

		/**
		 * Compares two rows column by column, following the sorting of the columns.
		 *
		 * @see #compareRowsByColumn(int, int, int)
		 * @param rowIndex1
		 * @param rowIndex2
		 * @return 0 if the rows are equal<br> 1 if the first row has a null or a bigger value than the second<br> -1 if the first row has a null or a lower value than the
		 *         second<br>
		 */
		protected int compare(int rowIndex1, int rowIndex2) {
			this.compares++;
			for (Order order : this.order) {
				String columnName = order.getColumnName();
				boolean ascending = order.isAscendent();
				int result = this.compareRowsByColumn(rowIndex1, rowIndex2, columnName);
				if (result != 0) {
					return ascending ? result : -result;
				}
			}
			return 0;
		}

		// TODO Revisar si es necesario utilizar para comparar 2 textos ->public
		// static Collator comparator = Collator.getInstance();
		/**
		 * Compares to row values of the same column.
		 *
		 * @param rowIndex1
		 * @param rowIndex2
		 * @param columnName
		 * @return 0 if both values are null or equal<br> -1 if the first value is null or less than the second<br> 1 if the second value is null or less than the first<br>
		 */
		public int compareRowsByColumn(int rowIndex1, int rowIndex2, String columnName) {
			if (this.entityResult.containsKey(columnName)) {
				List<?> columnData = (List<?>) this.entityResult.get(columnName);
				Object o1 = columnData.get(rowIndex1);
				Object o2 = columnData.get(rowIndex2);

				// If both values are null, return 0.
				if (((o1 == null) || (o1 instanceof NullValue)) && ((o2 == null) || (o2 instanceof NullValue))) {
					return 0;
				} else if ((o1 == null) || (o1 instanceof NullValue)) { // Define
					// null
					// less
					// than
					// everything.
					return -1;
				} else if ((o2 == null) || (o2 instanceof NullValue)) {
					return 1;
				}

				if ((o1 instanceof String) && (o2 instanceof String)) {
					int result = TableSorter.comparator.compare(o1, o2);
					if (result < 0) {
						return -1;
					} else if (result > 0) {
						return 1;
					} else {
						return 0;
					}

				} else if ((o1 instanceof Comparable) && (o2 instanceof Comparable)) {
					if (o1.getClass() == o2.getClass()) {
						Comparable n1 = (Comparable) o1;
						Comparable n2 = (Comparable) o2;
						return n1.compareTo(n2);
					} else {
						EntityResultUtils.logger.debug("WARNING: Two comparable, but with different classes: {} and {}", o1.getClass(), o2.getClass());
					}
				}

			}
			return 0;
		}

	}

}
