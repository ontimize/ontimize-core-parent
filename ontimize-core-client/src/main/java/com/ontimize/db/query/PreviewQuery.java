package com.ontimize.db.query;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Window;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.EntityResultViewer;
import com.ontimize.gui.container.EJDialog;
import com.ontimize.gui.images.ImageManager;
import com.ontimize.jee.common.dto.EntityResult;

class QueryEntityResultViewer extends EntityResultViewer {

	public QueryEntityResultViewer(Map<Object, Object> h) {
		super(h);
		this.table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}

	@Override
	public void setValue(Object o) {
		super.setValue(o);
		TableColumn column = null;
		for (int i = 0, a = this.table.getColumnCount(); i < a; i++) {
			column = this.table.getColumnModel().getColumn(i);
			if (i == 0) {
				column.setPreferredWidth(25);
			}
		}
	}

}

public class PreviewQuery extends EJDialog {

	private static final Logger logger = LoggerFactory.getLogger(PreviewQuery.class);

	protected ResourceBundle bundle = null;

	protected class TableModel extends AbstractTableModel {

		protected ResourceBundle bundle = null;

		protected String[] column = new String[0];

		protected EntityResult entityResult = null;

		public TableModel(EntityResult rs, ResourceBundle bundle) {
			this.bundle = bundle;
			this.entityResult = rs;
			Enumeration enu = rs.keys();
			while (enu.hasMoreElements()) {
				String[] aux = new String[this.column.length + 1];
				System.arraycopy(this.column, 0, aux, 0, this.column.length);
				aux[this.column.length] = (String) enu.nextElement();
				this.column = aux;
			}

		}

		@Override
		public int getColumnCount() {
			return this.column.length;
		}

		@Override
		public int getRowCount() {
			return this.entityResult.calculateRecordNumber();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			String nColumn = this.column[columnIndex];
			Vector vData = (Vector) this.entityResult.get(nColumn);
			return vData.get(rowIndex);
		}

		@Override
		public String getColumnName(int columnIndex) {
			return this.column[columnIndex];
		}

	}

	public PreviewQuery(Frame f, EntityResult entityResult, ResourceBundle bundle) {
		super(f, ApplicationManager.getTranslation("PreviewQuery", bundle), true);
		this.bundle = bundle;
		this.init(entityResult);
	}

	public PreviewQuery(Dialog d, EntityResult entityResult, ResourceBundle bundle) {
		super(d, ApplicationManager.getTranslation("PreviewQuery", bundle), true);
		this.bundle = bundle;
		this.init(entityResult);
	}

	public void init(EntityResult entityResult) {

		QueryEntityResultViewer v = new QueryEntityResultViewer(new Hashtable());

		v.setValue(entityResult);
		v.setResourceBundle(this.bundle);

		JScrollPane scroll = new JScrollPane(v);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.revalidate();

		JLabel label = new JLabel(ApplicationManager.getTranslation("previewquery.Results", this.bundle));
		JButton bOK = new com.ontimize.report.ReportDesignerButton(ImageManager.getIcon(ImageManager.OK));
		bOK.setToolTipText(ApplicationManager.getTranslation("previewquery.Accept", this.bundle));
		bOK.setText(ApplicationManager.getTranslation("previewquery.Accept", this.bundle));

		FlowLayout f = new FlowLayout();
		f.setAlignment(FlowLayout.CENTER);
		JPanel pButton = new JPanel();
		pButton.setLayout(f);
		pButton.add(bOK);
		bOK.addActionListener(e -> {
			Window w = SwingUtilities.getWindowAncestor((Component) e.getSource());
			w.setVisible(false);
		});

		label.setBorder(new EmptyBorder(5, 15, 5, 5));
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(label, BorderLayout.NORTH);
		this.getContentPane().add(scroll, BorderLayout.CENTER);
		this.getContentPane().add(pButton, BorderLayout.SOUTH);
		this.pack();
	}

	public static void show(Component component, EntityResult rs, ResourceBundle bundle) {
		Window window = SwingUtilities.getWindowAncestor(component);
		PreviewQuery preview = null;

		if (window instanceof Frame) {
			preview = new PreviewQuery((Frame) window, rs, bundle);
		} else if (window instanceof Dialog) {
			preview = new PreviewQuery((Dialog) window, rs, bundle);
		}

		if (preview != null) {
			ApplicationManager.center(preview);
			preview.setVisible(true);
		} else {
			PreviewQuery.logger.debug("Preview can not be created....");
		}
	}

}
