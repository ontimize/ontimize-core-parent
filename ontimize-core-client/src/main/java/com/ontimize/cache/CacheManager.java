package com.ontimize.cache;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.NoSuchObjectException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.container.EJDialog;
import com.ontimize.jee.common.db.Entity;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.locator.EntityReferenceLocator;
import com.ontimize.jee.common.locator.UtilReferenceLocator;
import com.ontimize.jee.common.tools.Pair;

public class CacheManager {

	private static final Logger						logger					= LoggerFactory.getLogger(CacheManager.class);

	public static boolean							defaultParentKeyCache	= true;

	protected static CacheManager					cacheManager			= null;

	protected List<Object>							cachedComponents		= new ArrayList<>();

	protected Map<DataCacheId, EntityResult>	data					= new HashMap<>();

	protected EntityReferenceLocator				locator					= null;

	/**
	 * It identifies the characteristics of one data set (corresponding to one entity), several attributes and without conditions
	 */
	public static class DataCacheId implements Serializable {

		protected String				entity		= null;

		protected List<?>	attributes	= null;

		protected Map<?, ?>	keysValues	= null;

		protected long					time		= 0;

		public String getEntity() {
			return this.entity;
		}

		public List<?> getAttributes() {
			return this.attributes;
		}

		public Map<?, ?> getKeysValues() {
			return this.keysValues;
		}

		public void setTime(long time) {
			this.time = time;
		}

		public DataCacheId(String entity, List<?> attributes, Map<?, ?> keysValues) {
			this.entity = entity;
			this.attributes = attributes;
			this.keysValues = keysValues;
			this.time = System.currentTimeMillis();
		}

		public long getTime() {
			return this.time;
		}

		@Override
		public int hashCode() {
			if (this.entity == null) {
				return 0;
			}
			if (this.keysValues == null) {
				return this.entity.hashCode();
			}
			return this.entity.hashCode() + this.keysValues.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o instanceof DataCacheId) {
				if (this.entity.equals(((DataCacheId) o).entity)) {
					if (this.keysValues == null) {
						return true;
					}
					return this.keysValues.equals(((DataCacheId) o).keysValues);
				}
			}
			return super.equals(o);
		}

		@Override
		public String toString() {
			StringBuilder buffer = new StringBuilder();
			buffer.append(this.entity).append(" ");
			buffer.append(this.attributes).append(" ");
			if ((this.keysValues == null) || this.keysValues.isEmpty()) {
				return buffer.toString();
			}
			buffer.append(" [[ KeysValues: ");
			for (Entry<?, ?> entry : this.keysValues.entrySet()) {
				buffer.append(entry.getKey().toString()).append(" - ");
				buffer.append(entry.getValue().toString());
				buffer.append(" | ");
			}
			buffer.append("]]");
			return buffer.toString();
		}

	};

	private CacheManager(EntityReferenceLocator locator) {
		this.locator = locator;
	}

	public synchronized void addCachedComponent(CachedComponent c) {
		if (!this.cachedComponents.contains(c)) {
			this.cachedComponents.add(c);
			CacheManager.logger.debug("Added CachedComponent {} : {}", c.getEntity(), c.getAttributes());
		}
	}

	public static CacheManager getDefaultCacheManager(EntityReferenceLocator locator) {
		if (CacheManager.cacheManager == null) {
			CacheManager.cacheManager = new CacheManager(locator);
		}
		return CacheManager.cacheManager;
	}

	public synchronized void invalidateCache(String entity, Map<?, ?> keysValues) {
		CacheManager.logger.debug("Invalidate cache for entity: {}", entity);
		DataCacheId id = this.findKey(entity, keysValues);
		if (id != null) {
			this.data.remove(id);
		}
	}

	public Map<DataCacheId, EntityResult> getData() {
		return this.data;
	}

	protected DataCacheId findKey(String entity, Map<?, ?> keysValues) {
		if ((keysValues != null) && keysValues.isEmpty()) {
			keysValues = null;
		}
		for (Entry<?, ?> entry : this.data.entrySet()) {
			Object oKey = entry.getKey();
			if (oKey instanceof DataCacheId) {
				String ent = ((DataCacheId) oKey).entity;
				if (ent.equals(entity)) {
					if ((keysValues == null) && ((((DataCacheId) oKey).keysValues == null) || (((DataCacheId) oKey).keysValues).isEmpty())) {
						return (DataCacheId) oKey;
					} else if (keysValues != null) {
						if (keysValues.equals(((DataCacheId) oKey).keysValues)) {
							return (DataCacheId) oKey;
						}
					}
				}
			}
		}
		return null;
	}

	protected synchronized DataCacheId existsCache(String entity, List<?> attributes, Map<?, ?> keysValues, long cacheTime) {
		CacheManager.logger.debug("Checking if the cache exists: {}, {}", entity, attributes);
		DataCacheId cacheID = this.findKey(entity, keysValues);
		if (cacheID == null) {
			CacheManager.logger.debug("Checking if the cache exists: {}, {} -> no exists", entity, attributes);
			return null;
		} else {
			List<?> attrs = cacheID.attributes;
			boolean containsAll = true;
			for (int i = 0; i < attributes.size(); i++) {
				if (attrs.contains(attributes.get(i)) == false) {
					containsAll = false;
					break;
				}
			}
			if (containsAll == true) {
				CacheManager.logger.debug("Checking if the cache exists: {}, {} -> YES exist", entity, attributes);
				if ((cacheID.getTime() + cacheTime) > System.currentTimeMillis()) {
					return cacheID;
				}
			}
		}

		CacheManager.logger.debug("Checking if the cache exists: {}, {} -> NO exist", entity, attributes);
		return null;
	}

	public synchronized boolean existsCache(String entity, List<?> attributes, Map<?, ?> keysValues) {
		CacheManager.logger.debug("Checking if the cache exists: {}, {}", entity, attributes);
		DataCacheId cacheID = this.findKey(entity, keysValues);
		if (cacheID == null) {
			CacheManager.logger.debug("Checking if the cache exists: {}, {} -> no exists", entity, attributes);
			return false;
		} else {
			List<?> attrs = cacheID.attributes;
			boolean containsAll = true;
			for (int i = 0; i < attributes.size(); i++) {
				if (!attrs.contains(attributes.get(i))) {
					containsAll = false;
					break;
				}
			}
			if (containsAll) {
				CacheManager.logger.debug("Checking if the cache exists: {}, {} -> YES exist", entity, attributes);
				return true;
			}
		}
		CacheManager.logger.debug("Checking if the cache exists: {}, {} -> NO exist", entity, attributes);
		return false;
	}

	public synchronized long getLastCacheTime(String entity, Map<?, ?> keysValues) {
		DataCacheId dId = this.findKey(entity, keysValues);
		if (dId != null) {
			return dId.getTime();
		}
		return 0;
	}

	public synchronized Pair<DataCacheId, EntityResult> retrieveDataCache(String entity, List<?> attributes, Map<?, ?> keysValues, long cacheTime) {
		DataCacheId dataCacheId = this.existsCache(entity, attributes, keysValues, cacheTime);
		if (dataCacheId != null) {
			EntityResult eResult = this.getDataCache(dataCacheId);
			return new Pair<>(dataCacheId, eResult);
		}
		return null;
	}

	protected synchronized EntityResult getDataCache(DataCacheId dataCacheId) {
		if (this.data.containsKey(dataCacheId)) {
			return this.data.get(dataCacheId);
		}
		return null;
	}

	public synchronized EntityResult getDataCache(String entity, List<?> attributes, Map<?, ?> keysValues) {
		// If the data are ready return them, in other case request them.
		// Only one cache for each entity is accepted
		// Info:
		try {
			this.printCacheSize();
		} catch (Exception exc) {
			CacheManager.logger.error(null, exc);
		}

		if (this.existsCache(entity, attributes, keysValues)) {
			return this.data.get(this.findKey(entity, keysValues));
		}

		DataCacheId dId = this.getMaxDataCacheId(entity, keysValues, attributes);

		try {
			Entity ent = this.locator.getEntityReference(entity);
			EntityResult res = null;
			try {
				res = ent.query(keysValues == null ? new HashMap<>() : keysValues, dId.attributes, this.locator.getSessionId());
			} catch (NoSuchObjectException ex) {
				if (this.locator instanceof UtilReferenceLocator) {
					((UtilReferenceLocator) this.locator).removeEntity(entity, this.locator.getSessionId());
					ent = this.locator.getEntityReference(entity);
					res = ent.query(keysValues == null ? new HashMap<>() : keysValues, dId.attributes, this.locator.getSessionId());
				} else {
					throw ex;
				}
			}
			this.data.put(dId, res);
			if (CacheManager.logger.isDebugEnabled()) {
				List<Object> at = new ArrayList<>();
				Enumeration enumKeys = res.keys();
				while (enumKeys.hasMoreElements()) {
					at.add(enumKeys.nextElement());
				}
				CacheManager.logger.debug("EntityCache initialized: {} with attributes: {} The entity has returned: {}", entity, dId.attributes, at);
				for (Object element : dId.attributes) {
					if (!at.contains(element)) {
						CacheManager.logger.warn("CacheManager: The entity was asked for: {} but the answer does not contain those values", element);
					}
				}
			}

			this.checkMaximumSizeDataCacheId();
			return res;
		} catch (Exception exc) {
			CacheManager.logger.error("Looking up the entity {} in cache: {}", entity, exc.getMessage(), exc);
			return null;
		}

	}

	protected EntityResult query(String entity, Map<?, ?> kv, List<?> attributes) {
		Entity ent = null;
		EntityResult res = null;
		try {
			ent = this.locator.getEntityReference(entity);
			res = ent.query(kv, attributes, this.locator.getSessionId());
			return res;
		} catch (NoSuchObjectException ex) {
			if (this.locator instanceof UtilReferenceLocator) {
				try {
					((UtilReferenceLocator) this.locator).removeEntity(entity, this.locator.getSessionId());
					ent = this.locator.getEntityReference(entity);
					res = ent.query(kv == null ? new HashMap<>() : kv, attributes, this.locator.getSessionId());
					return res;
				} catch (Exception exc) {
					CacheManager.logger.error(null, exc);
				}
			} else {
				CacheManager.logger.error(null, ex);
			}
		} catch (Exception exc) {
			CacheManager.logger.error(null, exc);
		}
		return null;
	}

	protected synchronized DataCacheId getMaxDataCacheId(String entity, Map<?, ?> keysValues, List<?> oAttrs) {
		List<Object> attributes = new ArrayList<>();
		for (Object element : this.cachedComponents) {
			CachedComponent c = (CachedComponent) element;
			if (c.getEntity() != null) {
				if (c.getEntity().equals(entity)) {
					List<?> attrs = c.getAttributes();
					if (attrs.isEmpty()) {
						return new DataCacheId(entity, new ArrayList<>(), keysValues);
					}
					for (Object at : attrs) {
						if (!attributes.contains(at)) {
							attributes.add(at);
						}
					}
				}
			}
		}

		if (oAttrs != null) {
			for (Object column : oAttrs) {
				if (!attributes.contains(column)) {
					attributes.add(column);
				}
			}
		}

		return new DataCacheId(entity, attributes, keysValues);
	}

	public static int maximumDataCacheIdSize = 100;

	protected synchronized void checkMaximumSizeDataCacheId() {
		int size = 0;
		DataCacheId olderDataCacheId = null;

		for (Entry<DataCacheId, EntityResult> entry : this.data.entrySet()) {
			Object key = entry.getKey();
			if ((key instanceof DataCacheId) && (((DataCacheId) key).keysValues != null)) {
				size++;
				if (olderDataCacheId == null) {
					olderDataCacheId = (DataCacheId) key;
				} else {
					if (olderDataCacheId.time > ((DataCacheId) key).time) {
						olderDataCacheId = (DataCacheId) key;
					}
				}
			}
		}

		if (size > CacheManager.maximumDataCacheIdSize) {
			if (olderDataCacheId != null) {
				this.data.remove(olderDataCacheId);
			}
		}
	}

	public String getCachedEntities() {
		StringBuilder sb = new StringBuilder();
		for (Entry<DataCacheId, EntityResult> entry : this.data.entrySet()) {
			sb.append(entry.getKey() + " ");
		}
		return sb.toString();
	}

	public int getCacheSize() {

		int size = -1;
		ByteArrayOutputStream bOut = null;
		ObjectOutputStream out = null;
		try {
			bOut = new ByteArrayOutputStream();
			out = new ObjectOutputStream(bOut);
			out.writeObject(this.data);
			out.flush();
			size = bOut.size();
			return size;
		} catch (Exception e) {
			CacheManager.logger.error(null, e);
			return -1;
		} finally {
			try {
				if (bOut != null) {
					bOut.reset();
					bOut.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
				CacheManager.logger.error("{}", e.getMessage(), e);
			}
		}

	}

	public void printCacheSize() {
		if (CacheManager.logger.isDebugEnabled()) {
			CacheManager.logger.debug("Cache size : {} bytes", this.getCacheSize());
		}
	}

	public static class CacheManagerViewer extends EJDialog {

		protected static CacheManagerViewer	viewer						= null;

		private static final String			CACHE_MANAGER_WINDOW		= "cachemanager.viewer";

		private static final String			CACHE_MANAGER_ENTITY		= "cachemanager.entity";

		private static final String			CACHE_MANAGER_ATTRIBUTES	= "cachemanager.attributes";

		private static final String			CACHE_MANAGER_KEYS			= "cachemanager.keys";

		private static final String			CACHE_MANAGER_SIZE			= "cachemanager.size";

		private static final String			CACHE_MANAGER_TIME			= "cachemanager.time";

		private static final String			CACHE_MANAGER_REFRESH		= "cachemanager.refresh";

		protected JLabel					sizeLabel					= new JLabel(CacheManagerViewer.CACHE_MANAGER_SIZE);

		protected JTextField				sizeText					= new JTextField(20);

		protected JTable					cacheTable					= new JTable(new CacheModel());

		protected JButton					refreshButton				= new JButton(CacheManagerViewer.CACHE_MANAGER_REFRESH);

		public CacheManagerViewer(Dialog owner) {
			super(owner, ApplicationManager.getTranslation(CacheManagerViewer.CACHE_MANAGER_WINDOW), false);
			this.jInit();
		}

		public CacheManagerViewer(Frame owner) {
			super(owner, ApplicationManager.getTranslation(CacheManagerViewer.CACHE_MANAGER_WINDOW), false);
			this.jInit();
		}

		protected void jInit() {
			this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
			this.getContentPane().setLayout(new GridBagLayout());
			this.getContentPane().add(this.sizeLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
			this.getContentPane().add(this.sizeText, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
			this.getContentPane().add(new JScrollPane(this.cacheTable),
					new GridBagConstraints(0, 1, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
			this.getContentPane().add(this.refreshButton,
					new GridBagConstraints(0, 2, 2, 2, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
			this.refreshButton.setText(ApplicationManager.getTranslation(CacheManagerViewer.CACHE_MANAGER_REFRESH));
			this.refreshButton.addActionListener(event -> CacheManagerViewer.this.refresh());
			this.sizeText.setEnabled(false);

			TableColumn tC = this.cacheTable.getColumn(CacheManagerViewer.CACHE_MANAGER_ATTRIBUTES);
			if (tC != null) {
				tC.setCellRenderer(this.memoRender);
				tC.addPropertyChangeListener(this.columnWidthListener);
			}

			tC = this.cacheTable.getColumn(CacheManagerViewer.CACHE_MANAGER_KEYS);
			if (tC != null) {
				tC.setCellRenderer(this.memoRender);
				tC.addPropertyChangeListener(this.columnWidthListener);
			}

			tC = this.cacheTable.getColumn(CacheManagerViewer.CACHE_MANAGER_SIZE);
			if (tC != null) {
				tC.setCellRenderer(new SizeRenderer());
			}

			this.pack();
		}

		protected MemoCellRenderer memoRender = new MemoCellRenderer();

		protected void refresh() {
			CacheManager.getDefaultCacheManager(ApplicationManager.getApplication().getReferenceLocator());
			int cacheSize = -1;
			// cacheManager.getCacheSize();

			this.sizeText.setText("" + cacheSize + " bytes");
			((CacheModel) this.cacheTable.getModel()).refresh();
			this.setHeight();
		}

		protected void setResourceBundle() {
			this.sizeLabel.setText(ApplicationManager.getTranslation(CacheManagerViewer.CACHE_MANAGER_SIZE));
			this.refreshButton.setText(ApplicationManager.getTranslation(CacheManagerViewer.CACHE_MANAGER_REFRESH));

			TableColumn tC = this.cacheTable.getColumn(CacheManagerViewer.CACHE_MANAGER_ENTITY);
			tC.setIdentifier(CacheManagerViewer.CACHE_MANAGER_ENTITY);
			tC.setHeaderValue(ApplicationManager.getTranslation(CacheManagerViewer.CACHE_MANAGER_ENTITY));

			tC = this.cacheTable.getColumn(CacheManagerViewer.CACHE_MANAGER_ATTRIBUTES);
			tC.setIdentifier(CacheManagerViewer.CACHE_MANAGER_ATTRIBUTES);
			tC.setHeaderValue(ApplicationManager.getTranslation(CacheManagerViewer.CACHE_MANAGER_ATTRIBUTES));

			tC = this.cacheTable.getColumn(CacheManagerViewer.CACHE_MANAGER_KEYS);
			tC.setIdentifier(CacheManagerViewer.CACHE_MANAGER_KEYS);
			tC.setHeaderValue(ApplicationManager.getTranslation(CacheManagerViewer.CACHE_MANAGER_KEYS));

			tC = this.cacheTable.getColumn(CacheManagerViewer.CACHE_MANAGER_SIZE);
			tC.setIdentifier(CacheManagerViewer.CACHE_MANAGER_SIZE);
			tC.setHeaderValue(ApplicationManager.getTranslation(CacheManagerViewer.CACHE_MANAGER_SIZE));

			tC = this.cacheTable.getColumn(CacheManagerViewer.CACHE_MANAGER_TIME);
			tC.setIdentifier(CacheManagerViewer.CACHE_MANAGER_TIME);
			tC.setHeaderValue(ApplicationManager.getTranslation(CacheManagerViewer.CACHE_MANAGER_TIME));
		}

		public static void showViewer(Component component) {
			Window w = null;
			if (component == null) {
				w = ApplicationManager.getApplication().getFrame();
			} else {
				w = SwingUtilities.getWindowAncestor(component);
			}
			if (w == null) {
				return;
			}
			if (CacheManagerViewer.viewer != null) {
				CacheManagerViewer.viewer.setResourceBundle();
				CacheManagerViewer.viewer.refresh();
				CacheManagerViewer.viewer.setVisible(true);
				return;
			}
			if (w instanceof Frame) {
				CacheManagerViewer.viewer = new CacheManagerViewer((Frame) w);
			} else if (w instanceof Dialog) {
				CacheManagerViewer.viewer = new CacheManagerViewer((Dialog) w);
			}

			CacheManagerViewer.viewer.setResourceBundle();
			CacheManagerViewer.viewer.refresh();
			CacheManagerViewer.viewer.setVisible(true);
		}

		public static class CacheModel extends AbstractTableModel {

			protected String[]		columnName	= new String[] { CacheManagerViewer.CACHE_MANAGER_ENTITY, CacheManagerViewer.CACHE_MANAGER_ATTRIBUTES, CacheManagerViewer.CACHE_MANAGER_KEYS, CacheManagerViewer.CACHE_MANAGER_SIZE, CacheManagerViewer.CACHE_MANAGER_TIME };

			protected DateFormat	format		= new SimpleDateFormat("HH:mm:ss YYYY-MM-DD");

			@Override
			public int getColumnCount() {
				return 5;
			}

			@Override
			public String getColumnName(int column) {
				return this.columnName[column];
			}

			@Override
			public int getRowCount() {
				CacheManager cacheManager = CacheManager.getDefaultCacheManager(ApplicationManager.getApplication().getReferenceLocator());
				return cacheManager.data.size();
			}

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				CacheManager cacheManager = CacheManager.getDefaultCacheManager(ApplicationManager.getApplication().getReferenceLocator());
				Map<DataCacheId, EntityResult> data = cacheManager.getData();
				Set<DataCacheId> kSet = data.keySet();
				DataCacheId[] keys = kSet.toArray(new DataCacheId[kSet.size()]);

				DataCacheId currentKey = keys[rowIndex];

				if (columnIndex == 0) {
					return currentKey.getEntity();
				}
				if (columnIndex == 1) {
					return currentKey.getAttributes();
				}
				if (columnIndex == 2) {
					return currentKey.getKeysValues();
				}

				if (columnIndex == 3) {
					int size = this.getCacheSize(data.get(currentKey));
					return new Integer(size);
				}
				if (columnIndex == 4) {
					try {
						return this.format.format(new Date(currentKey.getTime()));
					} catch (Exception ex) {
						CacheManager.logger.error(null, ex);
					}

					return new Double(-1);
				}
				return null;
			}

			protected int getCacheSize(Object current) {

				int iSize = -1;
				ByteArrayOutputStream bOut = null;
				ObjectOutputStream out = null;
				try {
					bOut = new ByteArrayOutputStream();
					out = new ObjectOutputStream(bOut);
					out.writeObject(current);
					out.flush();
					iSize = bOut.size();

					return iSize;
				} catch (Exception e) {
					CacheManager.logger.error(null, e);
					return -1;
				} finally {
					try {
						if (bOut != null) {
							bOut.reset();
							bOut.close();
						}
						if (out != null) {
							out.close();
						}
					} catch (Exception e) {
						CacheManager.logger.trace(null, e);
					}
				}
			}

			public void refresh() {
				this.fireTableDataChanged();
			}

		}

		public static class SizeRenderer extends DefaultTableCellRenderer {

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, selected, hasFocus, row, column);
				if ((c instanceof JLabel) && (value instanceof Integer)) {
					Integer iValue = (Integer) value;
					StringBuilder builder = new StringBuilder();

					String unit = "bytes";
					if (iValue > 1024) {
						iValue = iValue / 1024;
						unit = "kB";
					}

					if (iValue > 1024) {
						iValue = iValue / 1024;
						unit = "MB";
					}

					builder.append(iValue);
					builder.append(" ");
					builder.append(unit);
					((JLabel) c).setText(builder.toString());
				}

				return c;
			}

		}

		public class MemoCellRenderer extends DefaultTableCellRenderer {

			protected JTextArea ta = null;

			public MemoCellRenderer() {
				this.ta = new JTextArea();
				this.ta.setLineWrap(true);
				this.ta.setWrapStyleWord(true);
				this.ta.setOpaque(true);
			}

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {
				if (value != null) {
					this.ta.setText(value.toString());
					if (value instanceof DataCacheId) {
						if (((DataCacheId) value).keysValues != null) {
							this.ta.setBackground(Color.LIGHT_GRAY);
						} else {
							this.ta.setBackground(Color.white);
						}
					}
				} else {
					this.ta.setText("");
				}
				return this.ta;
			}

		}

		protected PropertyChangeListener columnWidthListener = e -> {
			if ("width".equals(e.getPropertyName())) {
				CacheManagerViewer.this.setHeight();
			}
		};

		protected void setHeight() {
			for (int i = 0; i < this.cacheTable.getRowCount(); i++) {
				int height = 0;
				for (int j = 0; j < 3; j++) {
					int width = this.cacheTable.getColumnModel().getColumn(j).getWidth();
					Component c = this.memoRender.getTableCellRendererComponent(this.cacheTable, this.cacheTable.getValueAt(i, this.cacheTable.convertColumnIndexToView(j)),
							false,
							false, i, this.cacheTable.convertColumnIndexToView(j));
					c.setSize(width, 50);
					int h = c.getPreferredSize().height;
					height = Math.max(height, h);
				}
				this.cacheTable.setRowHeight(i, height);
			}
		}

	}

}
