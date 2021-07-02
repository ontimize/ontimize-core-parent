package com.ontimize.gui.table.blocked;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import com.ontimize.gui.table.OTableModel;
import com.ontimize.gui.table.Sortable;
import com.ontimize.gui.table.TableSorter;

public class BlockedTableModel extends AbstractTableModel implements OTableModel, TableModelListener, Sortable {

    protected TableSorter tableSorter;

    int blockedColumnIndex = 0;

    public BlockedTableModel(TableSorter tableSorter) {
        this.tableSorter = tableSorter;
        this.tableSorter.addTableModelListener(this);
    }

    public TableSorter getTableSorter() {
        return tableSorter;
    }

    @Override
    public int getRowCount() {
        return this.tableSorter.getRowCount();
    }

    public void setBlockedColumnIndex(int blockedColumnIndex) {
        this.blockedColumnIndex = blockedColumnIndex;
    }

    @Override
    public int getColumnCount() {
        return blockedColumnIndex + 1;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return this.tableSorter.getColumnName(columnIndex);
    }


    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return this.tableSorter.getColumnClass(columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return this.tableSorter.isCellEditable(rowIndex, columnIndex);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return this.tableSorter.getValueAt(rowIndex, columnIndex);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        this.tableSorter.setValueAt(aValue, rowIndex, columnIndex);
    }

    @Override
    public boolean isSum() {
        return this.tableSorter.isSum();
    }

    @Override
    public Number getColumnOperation(int columnIndex) {
        return this.tableSorter.getColumnOperation(columnIndex);
    }

    @Override
    public TableCellRenderer getSumCellRenderer(boolean currency) {
        return this.tableSorter.getSumCellRenderer(currency);
    }

    @Override
    public int convertRowIndexToFilteredModel(int viewRowIndex) {
        return this.tableSorter.convertRowIndexToFilteredModel(viewRowIndex);
    }

    @Override
    public Number getSelectedColumnOperation(Object columnName, int[] rowIndex) {
        return this.tableSorter.getSelectedColumnOperation(columnName, rowIndex);
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        this.fireTableChanged(e);
    }

    @Override
    public int[] getSortingColumns() {
        return this.tableSorter.getSortingColumns();
    }

}
