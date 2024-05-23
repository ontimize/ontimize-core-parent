package com.ontimize.gui.container;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JWindow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.field.FormComponent;
import com.ontimize.gui.field.JImage;
import com.ontimize.gui.field.ToggleImage;

/**
 * Implements a container with a background image. It is possible to add new components in a
 * specified pixel position
 */

public class JImageContainer extends JImage {

    private static final Logger logger = LoggerFactory.getLogger(JImageContainer.class);

    protected class PointLayout implements LayoutManager2 {

        List<Object> components = new ArrayList<>();

        List<Object> constraints = new ArrayList<>();

        Insets insets = new Insets(0, 0, 0, 0);

        public PointLayout() {
            super();
        }

        @Override
        public Dimension preferredLayoutSize(Container cont) {
            return cont.getPreferredSize();
        }

        @Override
        public void addLayoutComponent(String id, Component component) {
            this.addLayoutComponent(component, new Point(0, 0));
        }

        @Override
        public void removeLayoutComponent(Component component) {
            this.constraints.remove(this.components.indexOf(component));
            this.components.remove(component);
        }

        @Override
        public Dimension minimumLayoutSize(Container cont) {
            return new Dimension(1, 1);
        }

        @Override
        public void invalidateLayout(Container cont) {

            this.insets = cont.getInsets();
            // Get the components constraints again
            this.constraints = new ArrayList<>(this.components.size());
            for (int i = 0; i < this.components.size(); i++) {
                Object comp = this.components.get(i);
                if (comp instanceof FormComponent) {
                    this.constraints.add(i, ((FormComponent) comp).getConstraints(this));
                }
            }
        }

        @Override
        public float getLayoutAlignmentX(Container cont) {
            return 0;
        }

        @Override
        public float getLayoutAlignmentY(Container cont) {
            return 0;
        }

        @Override
        public Dimension maximumLayoutSize(Container container) {
            // Calculate the maximum size. It is the container preferred size
            return container.getPreferredSize();
        }

        @Override
        public void addLayoutComponent(Component component, Object constraints) {
            if (component == null) {
                return;
            }
            if (constraints instanceof Point) {
                this.components.add(this.components.size(), component);
                this.constraints.add(this.constraints.size(), constraints);
                component.setBounds(((Point) constraints).x, ((Point) constraints).y,
                        component.getPreferredSize().width, component.getPreferredSize().height);
                if (JImage.DEBUG) {
                    final Component c = component;
                    c.addMouseMotionListener(new MouseMotionAdapter() {

                        JWindow tip = null;

                        JLabel pos = new JLabel();

                        ;

                        @Override
                        public void mouseDragged(MouseEvent event) {
                            // The source of this event is the Thread.
                            // We want to set the component position in order to
                            // the parent position
                            Rectangle r = c.getBounds();
                            c.setBounds(event.getX() + (int) r.getX(), event.getY() + (int) r.getY(),
                                    c.getPreferredSize().width, c.getPreferredSize().height);
                            Component p = c.getParent();
                            if (p instanceof JComponent) {
                                ((JComponent) p).paintImmediately(0, 0, c.getWidth(), c.getHeight());
                            } else {
                                p.repaint();
                            }
                            if (c instanceof ToggleImage) {
                                if (this.tip == null) {
                                    this.tip = new JWindow();
                                    this.tip.getContentPane().add(this.pos);
                                    this.tip.pack();
                                }
                                ((JComponent) c).setToolTipText(Integer.toString((int) c.getBounds().getX()) + ","
                                        + Integer.toString((int) c.getBounds().getY()));
                                this.pos.setText(Integer
                                    .toString((int) (c.getBounds().x + ((ToggleImage) c).getImageOffset().getX())) + ","
                                        + Integer
                                            .toString((int) (c.getBounds().getY()
                                                    + ((ToggleImage) c).getImageOffset().getY())));
                                this.tip.pack();
                                this.tip.setVisible(true);
                                this.pos.paintImmediately(0, 0, this.tip.getWidth(), this.tip.getHeight());
                            }
                        }
                    });
                }

            } else {
                JImageContainer.logger.debug("Constrainst in this layout must be Point");
            }
        }

        @Override
        public void layoutContainer(Container containter) {

            for (int i = 0; i < this.components.size(); i++) {
                Object constraints = this.constraints.get(i);
                Component c = (Component) this.components.get(i);
                c.setBounds(((Point) constraints).x, ((Point) constraints).y, c.getPreferredSize().width,
                        c.getPreferredSize().height);
            }
            containter.repaint();
        }

    }

    public JImageContainer(Map<Object, Object> parameters) {
        super(parameters);
        this.setLayout(new PointLayout());
        this.addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                if (JImage.DEBUG) {
                    JImageContainer.this.setToolTipText(Integer.toString(e.getX()) + "," + Integer.toString(e.getY()));
                }
            }
        });
    }

}
