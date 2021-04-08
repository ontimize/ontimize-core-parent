package com.ontimize.gui.field;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.Freeable;

/**
 * Graphic panel with two levels:
 * <ul>
 * <li>First level: Groups components:</li>
 * <li>Second level: Groups CheckToggleImage components.
 * </ul>
 * Component shows the number of images in function of a <code>Hashtable</code> value. This one has
 * the identifiers for the first levels and values will be another <code>Hashtable</code> with
 * identifiers for second level in keys and <code>Short</code> in values.
 * <p>
 *
 * @author Imatia Innovation
 */

public class TwoLevelImageComponent extends JPanel implements DataComponent, Freeable {

    private static final Logger logger = LoggerFactory.getLogger(TwoLevelImageComponent.class);

    protected JScrollPane scroll = new JScrollPane();

    protected String entity = null;

    protected Hashtable value = null;

    protected Hashtable componentBounds = null;

    protected boolean required = false;

    protected Object attribute = null;

    protected boolean modificable = false;

    protected String labelText = "";

    private CheckToggleImage twoLevelComponent = null;

    private int paintedMaxHigh = 0;

    private int imagenWidth = 50;

    private final int hComponentSeparation = 15;

    private final int vComponentSeparation = 15;

    private final int levelOneUpperMargin = 25;

    private final int levelOneLowerMargin = 10;

    public TwoLevelImageComponent(Hashtable parameters) {
        this.setBackground(Color.white);
        this.init(parameters);

        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                Object at = TwoLevelImageComponent.this.getClickComponentKey(e.getX(), e.getY());
                if (at == null) {
                    TwoLevelImageComponent.logger.debug("No component found");
                } else {
                    TwoLevelImageComponent.logger.debug("Component click is " + at.toString());
                }
            }
        });

    }

    @Override
    public void init(Hashtable parameters) {
        Object entity = parameters.get("entity");
        if (entity == null) {
            TwoLevelImageComponent.logger.debug(this.getClass().toString() + ": Parameter 'entity' is required");
        } else {
            this.entity = entity.toString();
        }
        Object attr = parameters.get("attr");
        if (attr == null) {
            TwoLevelImageComponent.logger.debug(this.getClass().toString() + ": Parameter 'attr' is required");
        } else {
            this.attribute = attr;
        }

        Object required = parameters.get("required");
        if (required != null) {
            if (required.equals("yes")) {
                this.required = true;
            } else {
                this.required = false;
            }
        }
        this.twoLevelComponent = new CheckToggleImage(parameters);
        this.twoLevelComponent.setCallFormAlarm(false);
    }

    @Override
    public Object getConstraints(LayoutManager parentLayout) {
        if (parentLayout instanceof GridBagLayout) {
            return new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH,
                    new Insets(5, 5, 5, 5), 0, 0);
        } else {
            return null;
        }
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Hashtable) {
            this.value = (Hashtable) value;
        } else {
            this.deleteData();
        }
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public String getLabelComponentText() {
        return this.labelText;
    }

    @Override
    public void deleteData() {
        this.value = null;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isModifiable() {
        return this.modificable;
    }

    @Override
    public void setModifiable(boolean modifiable) {
        this.modificable = modifiable;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public int getSQLDataType() {
        return java.sql.Types.SMALLINT;
    }

    @Override
    public Object getAttribute() {
        return this.attribute;
    }

    @Override
    public boolean isRequired() {
        return this.required;
    }

    @Override
    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void setResourceBundle(ResourceBundle resources) {
    }

    @Override
    public Vector getTextsToTranslate() {
        Vector v = new Vector();
        return v;
    }

    @Override
    public void setComponentLocale(Locale l) {
    }

    @Override
    public void paintComponent(Graphics g) {
        int xPoint = 0;
        int yPoint = 0;
        super.paintComponent(g);
        if (this.value == null) {
            Color c = g.getColor();
            g.setColor(Color.red);
            g.drawString("There is no data", 10, 10);
            this.imagenWidth = 100;
            this.paintedMaxHigh = 50;
            g.setColor(c);
        } else {
            this.componentBounds = new Hashtable();
            this.paintedMaxHigh = 0;
            // Paint grouping.
            Enumeration enumKeys = this.value.keys();
            while (enumKeys.hasMoreElements()) {
                Object oKey = enumKeys.nextElement();
                Object oLevel2Values = this.value.get(oKey);
                // Paint the rectangle and the component by value.
                int availableWidth = (int) this.getSize().getWidth();
                if (oLevel2Values instanceof Hashtable) {
                    Hashtable hLevel2Values = (Hashtable) oLevel2Values;
                    Enumeration enumLevel2Keys = hLevel2Values.keys();
                    // translate the origin 5,20
                    g.translate(this.hComponentSeparation, this.levelOneUpperMargin);
                    xPoint += this.hComponentSeparation;
                    yPoint += this.levelOneUpperMargin;
                    int translationX = this.hComponentSeparation;
                    int translationY = this.levelOneUpperMargin;
                    int totalWidth = 0;
                    int totalHeight = 0;

                    Hashtable bounds = new Hashtable();
                    while (enumLevel2Keys.hasMoreElements()) {
                        Object oLevel2Key = enumLevel2Keys.nextElement();
                        Object oLevel2Value = hLevel2Values.get(oLevel2Key);
                        this.twoLevelComponent.setValue(oLevel2Value);
                        // Now paint it in this graphics.
                        Dimension d = this.twoLevelComponent.getSize();
                        // Set the component bounds
                        bounds.put(oLevel2Key, new Rectangle(xPoint, yPoint, d.width, d.height));
                        this.imagenWidth = d.width;
                        g.translate(d.width + this.hComponentSeparation, 0);
                        xPoint += d.width + this.hComponentSeparation;
                        yPoint += 0;
                        translationX += d.width + this.hComponentSeparation;
                        totalWidth += d.width + this.hComponentSeparation;

                        // include the first line height.
                        if (totalHeight == 0) {
                            totalHeight = d.height;
                        }
                        if ((totalWidth + d.width) >= availableWidth) {
                            // Next components line.
                            g.translate(-translationX + this.hComponentSeparation,
                                    d.height + this.vComponentSeparation);
                            xPoint += -translationX + this.hComponentSeparation;
                            yPoint += d.height + this.vComponentSeparation;
                            totalHeight += d.height + this.vComponentSeparation;
                            translationY += d.height + this.vComponentSeparation;
                            translationX = this.hComponentSeparation;
                            totalWidth = this.hComponentSeparation;
                        }
                    }
                    // Now paint the first level rectangle.
                    g.translate(-translationX, -translationY);
                    xPoint += -translationX;
                    yPoint += -translationY;
                    g.drawRoundRect(3, 3, availableWidth - (2 * 3),
                            (totalHeight + this.levelOneUpperMargin + this.vComponentSeparation) - (2 * 3), 5, 5);

                    // Translate the Y to paint the next first levels
                    g.translate(0, totalHeight + this.levelOneUpperMargin + this.vComponentSeparation
                            + this.levelOneLowerMargin);
                    xPoint += 0;
                    yPoint += totalHeight + this.levelOneUpperMargin + this.vComponentSeparation
                            + this.levelOneLowerMargin;
                    this.paintedMaxHigh += totalHeight + this.levelOneUpperMargin + this.vComponentSeparation
                            + this.levelOneLowerMargin;
                    this.componentBounds.put(oKey, bounds);
                }
            }
        }
    }

    private void recalculatePaintedDimension() {
        if (this.value == null) {
            this.imagenWidth = 100;
            this.paintedMaxHigh = 50;
        } else {
            this.paintedMaxHigh = 0;

            Enumeration enumKeys = this.value.keys();
            while (enumKeys.hasMoreElements()) {
                Object oKey = enumKeys.nextElement();
                Object oLevel2Values = this.value.get(oKey);
                int availableWidth = (int) this.getSize().getWidth();
                if (oLevel2Values instanceof Hashtable) {
                    Hashtable hLevel2 = (Hashtable) oLevel2Values;
                    Enumeration enumLevel2Keys = hLevel2.keys();

                    int translationX = this.hComponentSeparation;
                    int translationY = this.levelOneUpperMargin;
                    int totalWidth = 0;
                    int totalHeight = 0;
                    while (enumLevel2Keys.hasMoreElements()) {
                        Object oLevel2Key = enumLevel2Keys.nextElement();
                        Object oLevel2Value = hLevel2.get(oLevel2Key);
                        this.twoLevelComponent.setValue(oLevel2Value);

                        Dimension d = this.twoLevelComponent.getSize();
                        this.imagenWidth = d.width;

                        translationX += d.width + this.hComponentSeparation;
                        totalWidth += d.width + this.hComponentSeparation;

                        // include the first line height.
                        if (totalHeight == 0) {
                            totalHeight = d.height;
                        }
                        if ((totalWidth + d.width) >= availableWidth) {
                            // Next components line
                            totalHeight += d.height + this.vComponentSeparation;
                            translationY += d.height + this.vComponentSeparation;
                            translationX = this.hComponentSeparation;
                            totalWidth = this.hComponentSeparation;
                        }
                    }
                    this.paintedMaxHigh += totalHeight + this.levelOneUpperMargin + this.vComponentSeparation
                            + this.levelOneLowerMargin;
                }
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        this.recalculatePaintedDimension();
        if (com.ontimize.gui.ApplicationManager.DEBUG) {
            TwoLevelImageComponent.logger.debug("Preferred height: " + this.paintedMaxHigh);
        }
        return new Dimension(this.imagenWidth, this.paintedMaxHigh);
    }

    public Object getClickComponentKey(int x, int y) {
        if (this.componentBounds == null) {
            return null;
        }
        Enumeration enumKeys = this.componentBounds.keys();
        while (enumKeys.hasMoreElements()) {
            Object oKey = enumKeys.nextElement();
            Hashtable hLevel2Value = (Hashtable) this.componentBounds.get(oKey);
            Enumeration enumLevel2Keys = hLevel2Value.keys();
            while (enumLevel2Keys.hasMoreElements() == true) {
                Object oLevel2Key = enumLevel2Keys.nextElement();
                Rectangle rectangleBounds = (Rectangle) hLevel2Value.get(oLevel2Key);
                if (rectangleBounds.contains(x, y)) {
                    Hashtable res = new Hashtable();
                    res.put(oKey, oLevel2Key);
                    return res;
                }
            }
        }
        return null;
    }

    @Override
    public void initPermissions() {
    }

    @Override
    public boolean isRestricted() {
        return false;
    }

    @Override
    public void free() {
        // TODO Auto-generated method stub

    }

}
