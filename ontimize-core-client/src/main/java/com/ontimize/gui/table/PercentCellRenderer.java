package com.ontimize.gui.table;

import java.awt.Component;
import java.text.Format;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.SwingConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.i18n.Internationalization;
import com.ontimize.jee.common.db.NullValue;

/**
 * @version 1.0 23-06-2004
 */
public class PercentCellRenderer extends CellRenderer implements Internationalization {

    private static final Logger logger = LoggerFactory.getLogger(PercentCellRenderer.class);

    private double max = 1.0;

    public PercentCellRenderer() {
        this.setHorizontalAlignment(SwingConstants.RIGHT);
        this.createFormatter(Locale.getDefault());
        ((NumberFormat) this.format).setMaximumFractionDigits(0);
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

    public PercentCellRenderer(double max) {
        this.max = max;
        this.setHorizontalAlignment(SwingConstants.RIGHT);
        this.createFormatter(Locale.getDefault());
        ((NumberFormat) this.format).setMaximumFractionDigits(0);
    }

    protected void createFormatter(Locale l) {
        if ((this.format != null) && (this.format instanceof NumberFormat)) {
            int maximumDecimalDigits = ((NumberFormat) this.format).getMaximumFractionDigits();
            int minimumDecimalDigits = ((NumberFormat) this.format).getMinimumFractionDigits();
            NumberFormat formatter = NumberFormat.getPercentInstance(l);
            formatter.setMaximumFractionDigits(maximumDecimalDigits);
            formatter.setMinimumFractionDigits(minimumDecimalDigits);
            this.setFormater(formatter);
        } else {
            NumberFormat formatter = NumberFormat.getPercentInstance(l);
            formatter.setMaximumFractionDigits(0);
            this.setFormater(formatter);
        }

    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean hasFocus,
            int row, int column) {

        Component c = super.getTableCellRendererComponent(table, value, selected, hasFocus, row, column);
        try {
            if ((value != null) && (!(value instanceof NullValue)) && (value instanceof Number)) {
                this.setText(this.format.format(new Double(((Number) value).doubleValue() / this.max)));
            } else {
                if (value instanceof String) {
                    this.setText((String) value);
                } else {
                    this.setText("");
                }
            }
        } catch (Exception e) {
            if (value != null) {
                this.setText(value.toString());
            }
            if (ApplicationManager.DEBUG_DETAILS) {
                PercentCellRenderer.logger.error(null, e);
            }
        }

        this.setTipWhenNeeded(table, value, column);
        return c;
    }

    @Override
    public void setResourceBundle(ResourceBundle res) {

    }

    @Override
    public void setComponentLocale(Locale l) {
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
