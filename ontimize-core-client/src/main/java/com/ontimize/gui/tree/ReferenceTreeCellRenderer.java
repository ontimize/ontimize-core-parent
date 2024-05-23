package com.ontimize.gui.tree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.Form;
import com.ontimize.gui.field.ReferenceComboDataField;
import com.ontimize.jee.common.locator.EntityReferenceLocator;

public class ReferenceTreeCellRenderer extends DefaultTreeCellRenderer {

    private static final Logger logger = LoggerFactory.getLogger(ReferenceTreeCellRenderer.class);

    protected ReferenceComboDataField comboReferenceDataField = null;

    public void setReferencesLocator(EntityReferenceLocator buscador) {
        this.comboReferenceDataField.setReferenceLocator(buscador);
        this.comboReferenceDataField.initCache();
    }

    public void setParentForm(Form f) {
        this.comboReferenceDataField.setParentForm(f);
    }

    public ReferenceTreeCellRenderer(Map<Object, Object> parameters) {
        parameters.remove("cachetime");
        this.comboReferenceDataField = new ReferenceComboDataField(parameters);
        this.comboReferenceDataField.setUseCacheManager(false);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return d;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {

        Component component = null;
        try {
            TreePath path = tree.getPathForRow(row);
            if (path != null) {
                Object node = path.getLastPathComponent();
                if (node != null) {
                    if (node instanceof OTreeNode) {
                        OTreeNode n = (OTreeNode) node;
                        if (!n.isOrganizational()) {
                            String referenceDescription = null;
                            Object attr = this.comboReferenceDataField.getAttribute();
                            Object attributeValue = n.getValueForAttribute(attr);
                            if (attributeValue != null) {
                                referenceDescription = this.comboReferenceDataField.getRenderer()
                                    .getCodeDescription(attributeValue, this.comboReferenceDataField.getDataCache());
                            }
                            StringBuilder sText = new StringBuilder();
                            for (int i = 0; i < n.getShownAttributeList().size(); i++) {
                                if (n.getShownAttributeList().get(i).equals(attr)) {
                                    if (referenceDescription != null) {
                                        sText.append(referenceDescription + n.getSeparator());
                                    }
                                } else {
                                    sText.append(n.getValueForAttribute(n.getShownAttributeList().get(i))
                                            + n.getSeparator());
                                }
                            }
                            String t = sText.toString();
                            component = super.getTreeCellRendererComponent(tree, t, selected, expanded, leaf, row,
                                    hasFocus);
                        } else {
                            component = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row,
                                    hasFocus);
                        }

                        ImageIcon icon = n.getIcon();
                        if (icon != null) {
                            if (component instanceof JLabel) {
                                ((JLabel) component).setIcon(icon);
                            }
                        }
                        if (n.isOrganizational() && tree.isExpanded(path)) {
                            if (component instanceof JLabel) {
                                int childCount = n.getChildCount();
                                if (childCount > 1) {
                                    StringBuilder b = new StringBuilder(((JLabel) component).getText());
                                    b.append(" ");
                                    b.append("(");
                                    b.append(childCount);
                                    b.append(")");
                                    ((JLabel) component).setText(b.toString());
                                }
                            }
                        }
                        if (n.isOrganizational()) {
                            if (!selected) {
                                component.setForeground(Color.blue);
                            }
                        }
                        if (n.getRemark()) {
                            if (!selected) {
                                component.setForeground(Color.green.darker());
                            }
                            if (component instanceof JLabel) {
                                final Icon i = ((JLabel) component).getIcon();
                                if (i != null) {
                                    Icon compoIcon = new Icon() {

                                        @Override
                                        public int getIconHeight() {
                                            return i.getIconHeight();
                                        }

                                        @Override
                                        public int getIconWidth() {
                                            return i.getIconWidth();
                                        }

                                        @Override
                                        public void paintIcon(Component c, Graphics g, int x, int y) {
                                            i.paintIcon(c, g, x, y);
                                            // Paint a red start in the top left
                                            // corner
                                            g.translate(x, y);
                                            // 4 lines to the star
                                            Color color = g.getColor();
                                            g.setColor(Color.red);
                                            g.drawLine(12, 0, 16, 4);
                                            g.drawLine(14, 0, 14, 4);
                                            g.drawLine(16, 0, 12, 4);
                                            g.drawLine(12, 2, 16, 2);
                                            g.setColor(color);
                                        }
                                    };
                                    ((JLabel) component).setIcon(compoIcon);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            ReferenceTreeCellRenderer.logger.error(null, e);
        }
        if (component == null) {
            component = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        }
        return component;
    }

}
