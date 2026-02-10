package com.ontimize.gui.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.text.Format;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.JTextComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.field.DataComponent;

public abstract class CellRenderer extends DefaultTableCellRenderer {

    private static final Logger logger = LoggerFactory.getLogger(CellRenderer.class);

    /**
     * The name of class. Used by L&F to put UI properties.
     *
     * @since 5.2062EN
     */
    public static final String CELLRENDERER_NAME = "Table.cellRenderer";

    public static interface CellRendererColorManager {

        public Color getForeground(JTable t, int row, int col, boolean sel);

        public Color getBackground(JTable t, int row, int col, boolean sel);

    }

    public static interface CellRendererFontManager {

        public Font getFont(JTable t, int row, int col, boolean sel);

    }

    protected boolean remarkLines = true;

    protected boolean remarkEditable = true;

    public static Color focusBorderColor = Color.yellow;

    public static Border focusBorder;

    public static Border emptyBorder = new EmptyBorder(0, 2, 0, 2);

    protected Format format = null;

    protected JComponent component = null;

    protected Dimension prefSize = new Dimension(10, 10);

    public static Color evenRowBackgroundColor = DataComponent.VERY_LIGHT_SKYBLUE;

    public static Color oddRowBackgroundColor = Color.white;

    public static Color oddEditableBackgroundColor = DataComponent.VERY_LIGHT_YELLOW_2;

    public static Color evenEditableBackgroundColor = CellRenderer
        .getSoftDarker(CellRenderer.oddEditableBackgroundColor);

    public static Color selectedBackgroundColor = Color.blue;

    public static Color selectedEditableBackgroundColor = Color.blue.brighter();

    public static Color requiredInsertColumns = new Color(0xb8bacb);

    public static Color noRequiredInsertColumns = new Color(0xcdced9);

    public static Font font = null;

    public static Color selectedFontColor = Color.white;

    public static Color editableFontColor = Color.black;

    public static Color fontColor = Color.black;

    protected CellRendererColorManager cellRendererColorManager = null;

    protected CellRendererFontManager cellRendererFontManager = null;

    protected boolean configureRenderer = true;

    public CellRenderer() {
        this.component = this;
    }

    public void setFormater(Format f) {
        this.format = f;
    }

    public void setLineRemark(boolean lineRemark) {
        this.remarkLines = lineRemark;
    }

    public void setEditableRemark(boolean editableRemark) {
        this.remarkEditable = editableRemark;
    }

    @Override
    public Dimension getPreferredSize() {
        if (cellRendererFontManager!=null){
            try {
                Font font = cellRendererFontManager.getFont(null, -1, -1, false);
                if (font!=null && this.component!=null){
                    this.component.setFont(font);
                }
            }catch (Exception ex){
                logger.warn("");
            }
        }

        if (this.component == this) {
            this.prefSize.width = CellRenderer.calculatePreferredTextWidth(this);
            this.prefSize.height = this.getFontMetrics(this.getFont()).getHeight();
            return this.prefSize;
        } else if (this.component instanceof JTextField) {
            this.prefSize.width = CellRenderer.calculatePreferredTextWidth((JTextField) this.component);
            this.prefSize.height = this.component.getFontMetrics(this.component.getFont()).getHeight();
            return this.prefSize;
        } else {
            return this.component.getPreferredSize();
        }
    }

    protected static int calculatePreferredTextWidth(JTextField tf) {
        if ((tf == null) || (tf.getText() == null)) {
            return 0;
        }
        FontMetrics fontMetrics = tf.getFontMetrics(tf.getFont());
        int iAuxWidth = fontMetrics.stringWidth(tf.getText());
        return iAuxWidth + 4;
    }

    @Override
    public Font getFont() {
        if ((this.component != null) && (this.component != this)) {
            return this.component.getFont();
        } else {
            return super.getFont();
        }
    }

    @Override
    public FontMetrics getFontMetrics(Font f) {
        if ((this.component != null) && (this.component != this)) {
            return this.component.getFontMetrics(f);
        } else {
            return super.getFontMetrics(f);
        }
    }

    protected static int calculatePreferredTextWidth(JLabel l) {
        if (l == null) {
            return 0;
        }
        FontMetrics fontMetrics = l.getFontMetrics(l.getFont());
        if (l.getText() == null) {
            return 0;
        }
        int auxWidth = fontMetrics.stringWidth(l.getText());
        return auxWidth + 4;

    }

    @Override
    public void updateUI() {
        super.updateUI();
    }

    @Override
    public String getName() {
        return CellRenderer.CELLRENDERER_NAME;
    }

    protected Color getForegroundColor(JTable table, Object value, boolean selected, boolean editable, boolean hasFocus,
            int row, int column) {
        if (selected) {
            return CellRenderer.selectedFontColor;
        }
        if (editable) {
            return CellRenderer.editableFontColor;
        }

        return CellRenderer.fontColor;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean hasFocus,
            int row, int column) {
        if (this.configureRenderer) {
            if (table != null) {
                this.component.setFont(CellRenderer.font == null ? table.getFont() : CellRenderer.font);
            }

            if (CellRenderer.isInsertingRow(row, table)) {
                if (CellRenderer.isRequiredInsertingRow(row, column, table)) {
                    this.component.setBackground(CellRenderer.requiredInsertColumns);
                } else {
                    this.component.setBackground(CellRenderer.noRequiredInsertColumns);
                }
            } else if (selected) {
                this.component
                    .setForeground(this.getForegroundColor(table, value, selected, false, hasFocus, row, column));
                if (this.remarkEditable && table.isCellEditable(row, column)) {
                    this.component.setBackground(CellRenderer.selectedEditableBackgroundColor);
                } else {
                    this.component.setBackground(CellRenderer.selectedBackgroundColor);
                }
            } else {
                if ((table != null) && this.remarkEditable && table.isCellEditable(row, column)) {
                    if (table.isEnabled()) {
                        if ((row % 2) == 0) { // odd row
                            this.component.setBackground(CellRenderer.oddEditableBackgroundColor);
                        } else {
                            this.component.setBackground(CellRenderer.evenEditableBackgroundColor);
                        }
                    } else {
                        if ((row % 2) == 0) { // odd row
                            this.component
                                .setBackground(CellRenderer.getSoftDarker(CellRenderer.oddEditableBackgroundColor));
                        } else {
                            this.component
                                .setBackground(CellRenderer.getSoftDarker(CellRenderer.evenEditableBackgroundColor));
                        }
                    }
                    this.component
                        .setForeground(this.getForegroundColor(table, value, false, true, hasFocus, row, column));
                } else {
                    if ((row % 2) == 0) { // odd row
                        if ((table == null) || table.isEnabled()) {
                            this.component.setBackground(CellRenderer.oddRowBackgroundColor);
                        } else {
                            this.component.setBackground(CellRenderer.getDarker(CellRenderer.oddRowBackgroundColor));
                        }
                    } else {
                        if (this.remarkLines) {
                            if ((table == null) || table.isEnabled()) {
                                this.component.setBackground(CellRenderer.evenRowBackgroundColor);
                            } else {
                                this.component
                                    .setBackground(CellRenderer.getDarker(CellRenderer.evenRowBackgroundColor));
                            }
                        } else {
                            if ((table == null) || table.isEnabled()) {
                                this.component.setBackground(CellRenderer.oddRowBackgroundColor);
                            } else {
                                this.component.setBackground(CellRenderer.getDarker(CellRenderer.oddRowBackgroundColor));
                            }
                        }
                    }
                    this.component
                        .setForeground(this.getForegroundColor(table, value, false, false, hasFocus, row, column));
                }
            }
            if (hasFocus) {
                this.component.setBorder(this.getDefaultFocusBorder());
            } else {
                this.component.setBorder(CellRenderer.emptyBorder);
            }
            if (this.cellRendererColorManager != null) {
                Color bg = this.cellRendererColorManager.getBackground(table, row, column, selected);
                if (bg != null) {
                    this.component.setBackground(bg);
                }
                Color fg = this.cellRendererColorManager.getForeground(table, row, column, selected);
                if (fg != null) {
                    this.component.setForeground(fg);
                }
            }

            if (this.cellRendererFontManager != null) {
                Font f = this.cellRendererFontManager.getFont(table, row, column, selected);
                if (f != null) {
                    this.component.setFont(f);
                }
            }
        }
        return this.component;
    }

    public void setJComponent(JComponent c) {
        this.component = c;
    }

    public void setTipWhenNeeded(JTable table, Object value, int column) {
        // TIP
        try {
            this.setToolTipText(null);
            if (table != null) {
                if (this.component instanceof JLabel) {
                    TableColumn tc = table.getColumn(table.getColumnName(column));
                    if (tc.getWidth() < CellRenderer.calculatePreferredTextWidth((JLabel) this.component)) {
                        this.component.setToolTipText(this.getText());
                    }
                }
            }
        } catch (Exception e) {
            CellRenderer.logger.error(null, e);
        }
    }

    public Border getDefaultFocusBorder() {
        if (CellRenderer.focusBorder == null) {
            CellRenderer.focusBorder = BorderFactory.createLineBorder(CellRenderer.focusBorderColor, 2);
        }
        return CellRenderer.focusBorder;
    }

    public static void setEvenRowBackgroundColor(Color c) {
        CellRenderer.evenRowBackgroundColor = c;
    }

    public static Color getEvenRowBackgroundColor() {
        return CellRenderer.evenRowBackgroundColor;
    }

    public static Color getDarker(Color c) {
        return new Color(Math.max((int) (c.getRed() * 0.9), 0), Math.max((int) (c.getGreen() * 0.9), 0),
                Math.max((int) (c.getBlue() * 0.9), 0));
    }

    public static Color getSoftDarker(Color c) {
        return new Color(Math.max((int) (c.getRed() * 0.96), 0), Math.max((int) (c.getGreen() * 0.96), 0),
                Math.max((int) (c.getBlue() * 0.96), 0));
    }

    public String getShownText() {
        if ((this.component != null) && (this.component instanceof JTextComponent)) {
            return ((JTextComponent) this.component).getText();
        } else if ((this.component != null) && (this.component instanceof JLabel)) {
            return ((JLabel) this.component).getText();
        } else {
            return this.getText();
        }
    }

    public void setCellRendererColorManager(CellRendererColorManager rend) {
        this.cellRendererColorManager = rend;
    }

    public void setCellRendererFontManager(CellRendererFontManager rend) {
        this.cellRendererFontManager = rend;
    }

    public static boolean isInsertingRow(int row, JTable table) {
        if ((table != null) && (table.getModel() != null) && (table.getModel() instanceof TableSorter)
                && ((TableSorter) table.getModel()).isInsertingRow(row)) {
            return true;
        }
        return false;
    }

    public static boolean isRequiredInsertingRow(int row, int column, JTable table) {
        Table oTable = (Table) SwingUtilities.getAncestorOfClass(Table.class, table);
        if ((oTable != null) && oTable.isInsertingEnabled()
                && oTable.getRequieredCols().contains(table.getColumnName(column))) {
            return true;
        }
        return false;
    }





}
