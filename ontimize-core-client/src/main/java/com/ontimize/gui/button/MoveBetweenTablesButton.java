package com.ontimize.gui.button;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.Form;
import com.ontimize.gui.RowTransferEvent;
import com.ontimize.gui.RowTransferListener;
import com.ontimize.gui.table.Table;

/**
 * This class implements a button to move records between two tables. <p>
 *
 * @author Imatia Innovation
 */
public class MoveBetweenTablesButton extends Button {

	private static final Logger	logger			= LoggerFactory.getLogger(MoveBetweenTablesButton.class);

	private String				sourceTableName	= null;

	private String				targetTableName	= null;

	private Table				sourceTable		= null;

	private Table				targetTable		= null;

	private final List<Object>	listeners		= new ArrayList<>();

	/**
	 * The condition to keep the order in moved rows. By default, false.
	 */
	protected boolean			keeporder		= false;

	/**
	 * The class constructor. Calls to <code>super()</code> with <code>Map</code> parameters. <p>
	 * 
	 * @param params
	 *            the <code>Map</code> with parameters
	 *
	 *            <p>
	 *
	 *
	 *            <Table BORDER=1 CELLPADDING=3 CELLSPACING=1 RULES=ROWS FRAME=BOX> <tr> <td><b>attribute</td> <td><b>values</td> <td><b>default</td> <td><b>required</td>
	 *            <td><b>meaning</td> </tr>
	 *
	 *            <tr> <td>sourcetable</td> <td></td> <td></td> <td>yes</td> <td>The source table.</td> </tr>
	 *
	 *            <tr> <td>destinationtable</td> <td></td> <td></td> <td>yes</td> <td>The destination table.</td> </tr>
	 *
	 *
	 *            <tr> <td>keeporder</td> <td><i>yes/no</td> <td>no</td> <td>no</td> <td>Moves rows in the same order that its appear in the source table.</td> </tr>
	 *
	 *            </TABLE>
	 *
	 */
	public MoveBetweenTablesButton(Map<Object, Object> params) throws Exception {
		super(params);
		if (params.containsKey("sourcetable")) {
			this.sourceTableName = (String) params.get("sourcetable");
		} else {
			throw new Exception("Parameter 'sourcetable' required");
		}
		if (params.containsKey("destinationtable")) {
			this.targetTableName = (String) params.get("destinationtable");
		} else {
			throw new Exception("Paramter 'destinationtable' required");
		}
		if (params.containsKey("keeporder")) {
			this.keeporder = ApplicationManager.parseStringValue(params.get("keeporder").toString());
		}
	}

	@Override
	public void setParentForm(Form f) {
		super.setParentForm(f);
		if ((this.sourceTableName != null) && (this.targetTableName != null)) {
			this.sourceTable = (Table) f.getDataFieldReference(this.sourceTableName);
			this.targetTable = (Table) f.getDataFieldReference(this.targetTableName);
			if ((this.sourceTable != null) && (this.targetTable != null)) {
				// Lister for source table selection model that enable/disable
				// the
				// button
				ListSelectionModel selectionModel = this.sourceTable.getJTable().getSelectionModel();
				if (selectionModel != null) {
					selectionModel.addListSelectionListener(new ListSelectionListener() {

						@Override
						public void valueChanged(ListSelectionEvent ev) {
							if (MoveBetweenTablesButton.this.sourceTable.getSelectedRowsNumber() > 0) {
								MoveBetweenTablesButton.this.setEnabled(true);
							} else {
								MoveBetweenTablesButton.this.setEnabled(false);
							}
						}
					});
				}
				// Button listener
				this.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent ev) {
						int[] selectedRows = MoveBetweenTablesButton.this.sourceTable.getSelectedRows();
						List<Object> vRowsAdd = new ArrayList<>();
						List<Object> vRowAddKeys = new ArrayList<>();
						if (MoveBetweenTablesButton.this.keeporder) {
							for (int i = selectedRows.length - 1; i >= 0; i--) {
								Map<Object, Object> hRowData = MoveBetweenTablesButton.this.sourceTable.getRowData(selectedRows[i]);
								vRowsAdd.add(hRowData);
								Map<Object, Object> hRowKeys = MoveBetweenTablesButton.this.sourceTable.getRowKeys(selectedRows[i]);
								vRowAddKeys.add(hRowKeys);
							}
						} else {
							for (int i = 0; i < selectedRows.length; i++) {
								Map<Object, Object> hRowData = MoveBetweenTablesButton.this.sourceTable.getRowData(selectedRows[i]);
								vRowsAdd.add(hRowData);
								Map<Object, Object> hRowKeys = MoveBetweenTablesButton.this.sourceTable.getRowKeys(selectedRows[i]);
								vRowAddKeys.add(hRowKeys);
							}
						}
						MoveBetweenTablesButton.this.targetTable.addRows(vRowsAdd);
						MoveBetweenTablesButton.this.sourceTable.deleteRows(selectedRows);
						MoveBetweenTablesButton.this.fireRowTransferEvent(vRowAddKeys);
					}
				});
			} else {
				MoveBetweenTablesButton.logger.debug("MoveBetweenTablesButton: some of specified tables not found");
			}
		} else {
			MoveBetweenTablesButton.logger.debug("MoveBetweenTablesButton: actions can not be registered because source table or destination table not found");
		}
	}

	public void addRowTransferListener(RowTransferListener listener) {
		this.listeners.add(listener);
	}

	public void removeRowTransferListener(RowTransferListener listener) {
		if (this.listeners.contains(listener)) {
			this.listeners.remove(listener);
		}
	}

	protected void fireRowTransferEvent(List<?> transferRowKeys) {
		RowTransferEvent ev = new RowTransferEvent(this, transferRowKeys);
		for (int i = 0; i < this.listeners.size(); i++) {
			((RowTransferListener) this.listeners.get(i)).rowsTransferred(ev);
		}
	}

}
