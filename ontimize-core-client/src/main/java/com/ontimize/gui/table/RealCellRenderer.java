package com.ontimize.gui.table;

import java.awt.Component;
import java.text.Format;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.field.DateDataField;
import com.ontimize.gui.i18n.Internationalization;
import com.ontimize.jee.common.db.NullValue;

/**
 * Renderer used to show Double and float types in a table.
 *
 * @author Imatia Innovation
 */
public class RealCellRenderer extends CellRenderer implements Internationalization {

    private static final Logger logger = LoggerFactory.getLogger(RealCellRenderer.class);

    protected ResourceBundle bundle = null;

    public RealCellRenderer() {
        this.setHorizontalAlignment(SwingConstants.RIGHT);
        this.createFormatter(Locale.getDefault());
    }

    public void setMaximumFractionDigits(int d) {
        ((NumberFormat) this.format).setMaximumFractionDigits(d);
    }

    public void setMinimumFractionDigits(int d) {
        ((NumberFormat) this.format).setMinimumFractionDigits(d);
    }

    public void setMaximumIntegerDigits(int d) {
        ((NumberFormat) this.format).setMaximumIntegerDigits(d);
    }

    public void setMinimumIntegerDigits(int d) {
        ((NumberFormat) this.format).setMinimumIntegerDigits(d);
    }

    public int getMaximumFractionDigits() {
        return ((NumberFormat) this.format).getMaximumFractionDigits();
    }

    public int getMaximumIntegerDigits() {
        return ((NumberFormat) this.format).getMaximumIntegerDigits();
    }

    public int getMinimumFractionDigits() {
        return ((NumberFormat) this.format).getMinimumFractionDigits();
    }

    public int getMinimumIntegerDigits() {
        return ((NumberFormat) this.format).getMinimumIntegerDigits();
    }

    protected void createFormatter(Locale l) {
        if ((this.format != null) && (this.format instanceof NumberFormat)) {
            int minimumDecimalDigits = ((NumberFormat) this.format).getMinimumFractionDigits();
            int maximumDecimalDigits = ((NumberFormat) this.format).getMaximumFractionDigits();

            int minIntegerDigits = ((NumberFormat) this.format).getMinimumIntegerDigits();
            int maxIntegerDigits = ((NumberFormat) this.format).getMaximumIntegerDigits();
            NumberFormat formatter = NumberFormat.getInstance(l);
            formatter.setMaximumFractionDigits(maximumDecimalDigits);
            formatter.setMinimumFractionDigits(minimumDecimalDigits);
            formatter.setMaximumIntegerDigits(maxIntegerDigits);
            formatter.setMinimumIntegerDigits(minIntegerDigits);
            this.setFormater(formatter);
        } else {
            NumberFormat formatter = NumberFormat.getInstance(l);
            formatter.setMaximumFractionDigits(3);
            this.setFormater(formatter);
        }

    }

    protected int[] checkHasSumRow(int[] index, int sumRow) {
        int[] newIndex = null;
        int ind = -1;
        for (int i = 0; i < index.length; i++) {
            if (index[i] == sumRow) {
                ind = i;
                break;
            }
        }
        if (ind == -1) {
            return index;
        }
        newIndex = new int[index.length - 1];
        if (ind == 0) {
            System.arraycopy(index, 1, newIndex, 0, newIndex.length);
        } else if (ind == index.length) {
            System.arraycopy(index, 0, newIndex, 0, newIndex.length);
        } else {
            System.arraycopy(index, 0, newIndex, 0, ind);
            System.arraycopy(index, ind + 1, newIndex, ind, index.length - ind - 1);
        }
        return newIndex;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean hasFocus,
            int row, int column) {

        Component c = super.getTableCellRendererComponent(table, value, selected, hasFocus, row, column);
        if (table == null) {
            return c;
        }
        try {
            if ((value != null) && (!(value instanceof NullValue)) && (value instanceof Number)) {
                StringBuilder sText = new StringBuilder();
                TableModel model = table.getModel();

                if ((model != null) && (model instanceof SumRowTableModel)) {
                    if ((row == 0) && (table instanceof SumRowTable)) {
                        JTable dataTable = ((SumRowTable) table).dataTable;
                        int[] index = dataTable.getSelectedRows();

                        SumRowTableModel sumRowModel = (SumRowTableModel) model;

                        // index = checkHasSumRow(index, row);
                        if (index.length > 0) {
                            Object nameColumn = table.getColumnName(column);
                            for (int j = 0; j < index.length; j++) {
                                index[j] = sumRowModel.convertRowIndexToFilteredModel(index[j]);
                            }
                            Number n = sumRowModel.getSelectedColumnOperation(nameColumn, index);
                            if (n != null) {
                                sText.append("( ");
                                sText.append(this.format.format(n));
                                sText.append(" ) ");
                            }
                        }
                    }
                } else
                // Deprecated by SumRowTableSorter
                if ((model != null) && (model instanceof TableSorter)) {
                    if (((TableSorter) model).isSumRow(row)) {
                        TableSorter sorter = (TableSorter) model;
                        int[] index = table.getSelectedRows();

                        index = this.checkHasSumRow(index, row);
                        if (index.length > 0) {
                            Object nameColumn = table.getColumnName(column);
                            for (int j = 0; j < index.length; j++) {
                                index[j] = sorter.convertRowIndexToFilteredModel(index[j]);
                            }
                            Number n = sorter.getSelectedColumnOperation(nameColumn, index);
                            if (n != null) {
                                sText.append("( ");
                                sText.append(this.format.format(n));
                                sText.append(" ) ");
                            }
                        }
                    }
                }
                sText.append(this.format.format(value));
                this.setText(sText.toString());
            } else {
                if (value instanceof String) {
                    this.setText((String) value);
                } else {
                    this.setText("");
                }
            }
        } catch (Exception e) {
            RealCellRenderer.logger.error(null, e);
            if (value != null) {
                this.setText(value.toString());
            }
            if (ApplicationManager.DEBUG_DETAILS) {
                RealCellRenderer.logger.error(null, e);
            }
        }

        this.setTipWhenNeeded(table, value, column);
        return c;
    }

    @Override
    public void setResourceBundle(ResourceBundle res) {
        this.bundle = res;
    }

    @Override
    public void setComponentLocale(Locale loc) {
        Locale l = DateDataField.getSameCountryLocale(loc);
        this.createFormatter(l);
    }

    @Override
    public List<String> getTextsToTranslate() {
        return new ArrayList<>(0);
    }

    public Format getFormat() {
        return this.format;
    }

}
