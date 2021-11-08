package com.ontimize.gui.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import java.util.TooManyListenersException;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.TransferHandler.TransferSupport;
import javax.swing.border.LineBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlPanel extends JPanel implements DropTargetListener {

    private static final Logger logger = LoggerFactory.getLogger(ControlPanel.class);

    protected static final String name = "TableButtonPanel";

    public static final String CHANGE_BUTTON_PROPERTY = "change_button_property";

    protected JComponent locationComponent = new JPanel() {

        {
            this.setBorder(new LineBorder(Color.GRAY, 2, false));
        }

        @Override
        public java.awt.Dimension getPreferredSize() {
            return new Dimension(5, 22);
        };
    };

    public ControlPanel() {
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.setTransferHandler(new ButtonTransferHandler());

        DropTarget dt = this.getDropTarget();
        try {
            dt.addDropTargetListener(this);
        } catch (TooManyListenersException e) {
            ControlPanel.logger.error(null, e);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width += this.locationComponent.getPreferredSize().width;
        return d;
    }

    @Override
    public String getName() {
        return ControlPanel.name;
    };

    public void setDropLocation(TransferSupport support) {
        this.remove(this.locationComponent);
        int locationIndex = this.getLocationIndex(support);
        if (locationIndex >= 0) {
            this.add(this.locationComponent, this.getLocationIndex(support));
        }
        this.doLayout();
    }

    protected int getLocationIndex(TransferSupport support) {
        Point dropPoint = support.getDropLocation().getDropPoint();
        Transferable t = this.getTransferable(support);

        if (t == null) {
            return -1;
        }

        int member = this.getComponentCount();
        Component beforeComponent, afterComponent;
        if (member == 0) {
            return 0;
        }

        Rectangle bound = this.getComponent(0).getBounds();
        if (dropPoint.x < (bound.x + (bound.width / 2))) {
            if (t.equals(this.getComponent(0))) {
                return -1;
            }
            return 0;
        }

        bound = this.getComponent(member - 1).getBounds();
        if (dropPoint.x > (bound.x + (bound.width / 2))) {
            if (t.equals(this.getComponent(member - 1))) {
                return -1;
            }
            return member;
        }

        for (int i = 1; i < member; i++) {
            beforeComponent = this.getComponent(i - 1);
            afterComponent = this.getComponent(i);
            if (t.equals(beforeComponent) || t.equals(afterComponent)) {
                continue;
            }
            Rectangle b = beforeComponent.getBounds();
            Rectangle a = afterComponent.getBounds();
            int x1 = b.x + (b.width / 2);
            int x2 = a.x + (a.width / 2);
            Rectangle r = new Rectangle(x1, 0, x2 - x1, this.getHeight());
            if (r.contains(dropPoint)) {
                return i;
            }
        }
        return -1;
    }

    protected Transferable getTransferable(TransferSupport support) {
        Transferable transferable = support.getTransferable();
        try {
            return (Transferable) transferable.getTransferData(transferable.getTransferDataFlavors()[0]);
        } catch (Exception e) {
            ControlPanel.logger.error(null, e);
        }
        return null;
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        this.remove(this.locationComponent);
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        this.remove(this.locationComponent);
        this.firePropertyChange(ControlPanel.CHANGE_BUTTON_PROPERTY, false, true);
    }

    public void removeLocationComponent() {
        this.remove(this.locationComponent);
        this.doLayout();
    }

    public void setButtonPosition(String preferences) {
        if (preferences == null) {
            return;
        }
        Component[] components = this.getComponents();
        LinkedHashMap<String, Component> componentKeys = new LinkedHashMap<String, Component>();
        for (Component c : components) {
            if (c instanceof TableComponent) {
                Object key = ((TableComponent) c).getKey();
                if (key != null) {
                    this.remove(c);
                    componentKeys.put(key.toString(), c);
                } else {
                    ControlPanel.logger.debug("WARNING: TableButton without KEY:{}", c);
                }
            }
        }

        StringTokenizer tokens = new StringTokenizer(preferences, ";");
        while (tokens.hasMoreElements()) {
            String key = tokens.nextToken();
            if (componentKeys.containsKey(key)) {
                Component c = componentKeys.remove(key);
                this.add(c);
                if (c instanceof GroupTableButton) {
                    this.addGroupTableButton(key, (GroupTableButton) c, tokens, componentKeys);
                }
            }
        }

        Collection<Component> rest = componentKeys.values();
        for (Component current : rest) {
            this.add(current);
        }
        componentKeys.clear();
    }

    public String getButtonPosition() {
        StringBuilder buffer = new StringBuilder();
        Component[] components = this.getComponents();
        for (Component c : components) {
            if (c instanceof GroupTableButton) {
                if (buffer.length() != 0) {
                    buffer.append(";");
                }
                buffer.append(this.processGroupTableButton((GroupTableButton) c));
            } else if (c instanceof TableComponent) {
                Object key = ((TableComponent) c).getKey();
                if (buffer.length() != 0) {
                    buffer.append(";");
                }
                buffer.append(key);
            }
        }
        return buffer.length() == 0 ? null : buffer.toString();
    }

    protected void addGroupTableButton(String currentKey, GroupTableButton group, StringTokenizer tokens,
            HashMap<String, Component> componentKeys) {
        while (tokens.hasMoreElements()) {
            String token = tokens.nextToken();
            if (currentKey.equalsIgnoreCase(token)) {
                return;
            }
            if (componentKeys.containsKey(token)) {
                group.add(componentKeys.remove(token));
            }
        }
    }

    protected String processGroupTableButton(GroupTableButton group) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(group.getKey());

        Component[] components = group.getInnerComponents();
        for (Component c : components) {
            if (c instanceof TableComponent) {
                Object key = ((TableComponent) c).getKey();
                buffer.append(";");
                buffer.append(key);
            }
        }

        buffer.append(";");
        buffer.append(group.getKey());
        return buffer.toString();
    }

}
