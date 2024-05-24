package com.ontimize.gui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.button.Button;
import com.ontimize.gui.field.FormComponent;
import com.ontimize.gui.images.ImageManager;

/**
 * @version 1.0
 * @deprecated
 */
@Deprecated
public class ButtonBar extends JToolBar implements FormComponent, Freeable {

    private static final Logger logger = LoggerFactory.getLogger(ButtonBar.class);

    protected int alignment = GridBagConstraints.NORTHWEST;

    protected ImageIcon bgImage = null;

    protected boolean rollover = true;

    protected Button hideButton = null;

    private boolean hide = false;

    protected ImageIcon leftImage = null;

    protected ImageIcon rightImage = null;

    private final EmptyBorder margin = new EmptyBorder(4, 5, 4, 5);

    public ButtonBar(Map<Object, Object> parameters) {
        super();
        this.init(parameters);
        this.setFloatable(false);
        if (this.bgImage != null) {
            Border b = this.getBorder();
            if (b == null) {
                b = new EtchedBorder(EtchedBorder.LOWERED);
            }
            this.setBorder(new CompoundBorder(b, this.margin));
        }
        this.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        if (this.alignment == GridBagConstraints.NORTHWEST) {
            this.hideButton = new Button(new HashMap<>());
        } else if (this.alignment == GridBagConstraints.NORTHEAST) {
            this.hideButton = new Button(new HashMap<>());
        }
        if (this.hideButton != null) {
            ImageIcon iconL = ImageManager.getIcon(ImageManager.LEFT_ARROW);
            if (iconL != null) {
                this.leftImage = iconL;
            } else {
                ButtonBar.logger.debug(ImageManager.LEFT_ARROW + " hasn't been found");
            }

            ImageIcon iconR = ImageManager.getIcon(ImageManager.RIGHT_ARROW);
            if (iconR != null) {
                this.rightImage = iconR;
            } else {
                ButtonBar.logger.debug(ImageManager.RIGHT_ARROW + " hasn't been found");
            }
            this.hideButton.setFocusPainted(false);
            this.hideButton.setMargin(new Insets(0, 0, 0, 0));
            if (this.alignment == GridBagConstraints.NORTHEAST) {
                this.hideButton.setIcon(this.rightImage);
            } else if (this.alignment == GridBagConstraints.NORTHWEST) {
                this.hideButton.setIcon(this.leftImage);
            }
            this.add(this.hideButton, null);
            this.hideButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    ButtonBar.this.hide = !ButtonBar.this.hide;
                    if (ButtonBar.this.hide) {
                        Border b = ButtonBar.this.getBorder();
                        if (b instanceof CompoundBorder) {
                            ButtonBar.this.setBorder(((CompoundBorder) b).getOutsideBorder());
                        }
                        // If it is hide and on the right then icon to the left
                        if (ButtonBar.this.alignment == GridBagConstraints.NORTHEAST) {
                            ButtonBar.this.hideButton.setIcon(ButtonBar.this.leftImage);
                        } else if (ButtonBar.this.alignment == GridBagConstraints.NORTHWEST) {
                            ButtonBar.this.hideButton.setIcon(ButtonBar.this.rightImage);
                        }
                    } else {
                        Border b = ButtonBar.this.getBorder();
                        if (!(b instanceof CompoundBorder)) {
                            if (ButtonBar.this.bgImage != null) {
                                ButtonBar.this.setBorder(new CompoundBorder(b, ButtonBar.this.margin));
                            }
                        }
                        if (ButtonBar.this.alignment == GridBagConstraints.NORTHEAST) {
                            ButtonBar.this.hideButton.setIcon(ButtonBar.this.rightImage);
                        } else if (ButtonBar.this.alignment == GridBagConstraints.NORTHWEST) {
                            ButtonBar.this.hideButton.setIcon(ButtonBar.this.leftImage);
                        }
                    }
                    for (int i = 0; i < ButtonBar.this.getComponentCount(); i++) {
                        Component c = ButtonBar.this.getComponent(i);
                        if (!c.equals(ButtonBar.this.hideButton)) {
                            if (ButtonBar.this.hide) {
                                c.setVisible(false);
                            } else {
                                c.setVisible(true);
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public void init(Map<Object, Object> parameters) {
        Object rollover = parameters.get("rollover");
        if (rollover == null) {
            this.rollover = true;
        } else {
            if (rollover.toString().equalsIgnoreCase("no")) {
                this.rollover = false;
            } else {
                this.rollover = true;
            }
        }
        Object bg = parameters.get("bg");
        if (bg != null) {

            this.bgImage = ImageManager.getIcon(bg.toString());

            if (this.bgImage == null) {
                ButtonBar.logger.debug(this.getClass().toString() + ". Image hasn't been found: " + bg.toString());
                ButtonBar.logger.warn("{}. Image hasn't been found: {}", this.getClass().toString(), bg.toString());
            }
        }
        Object orientation = parameters.get("orientation");
        if (orientation != null) {
            if (orientation.toString().equalsIgnoreCase("v")) {
                this.setOrientation(SwingConstants.VERTICAL);
            } else {
                this.setOrientation(SwingConstants.HORIZONTAL);
            }
        } else {
            this.setOrientation(SwingConstants.HORIZONTAL);
        }

        // Parameter: Alignment
        Object oAlign = parameters.get("align");
        if (oAlign == null) {
        } else {
            if (oAlign.equals("right")) {
                this.alignment = GridBagConstraints.NORTHEAST;
            } else {
                if (oAlign.equals("left")) {
                    this.alignment = GridBagConstraints.NORTHWEST;
                } else {
                    this.alignment = GridBagConstraints.NORTH;
                }
            }
        }
    }

    @Override
    public Object getConstraints(LayoutManager parentLayout) {
        if (parentLayout instanceof GridBagLayout) {
            return new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 0, 0, this.alignment,
                    GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);
        } else {
            return null;
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
    }

    @Override
    public void setComponentLocale(Locale l) {
    }

    @Override
    public void setResourceBundle(ResourceBundle resources) {
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Image
        if (this.bgImage != null) {
            g.drawImage(this.bgImage.getImage(), 0, 0, this.getWidth(), this.getHeight(), this);
        }
    }

    @Override
    public void add(Component c, Object con) {
        if ((this.getComponentCount() > 0) && (this.bgImage != null)) {
            this.addSeparator();
        }
        super.add(c, con);
        if (c instanceof Button) {
            ((Button) c).setRollover(true);
            ((Button) c).setMargin(new Insets(0, 0, 0, 0));
        }
    }

    @Override
    public List<String> getTextsToTranslate() {
    	List<String> v = new ArrayList<>();
        return v;
    }

    @Override
    public void free() {
        // TODO Auto-generated method stub

    }

}
