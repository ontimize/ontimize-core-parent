package com.ontimize.util.swing.text;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentTextPane extends JTextPane {

    private static final Logger logger = LoggerFactory.getLogger(ComponentTextPane.class);

    public ComponentTextPane() {
        super();
        this.setEditorKit(new CustomStyledEditorKit());
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        try {
            super.processMouseMotionEvent(e);
        } catch (Exception exc) {
            ComponentTextPane.logger.trace(null, exc);
        }
    }

    /**
     * Create a CustomLabel component with the specified text
     * @param originalText Original text
     * @param displayText Text to show in the component
     */
    public void insertTextComponent(String originalText, String displayText) {
        CustomLabel label = new CustomLabel(originalText, displayText);
        label.setFont(this.getFont());
        label.setOpaque(true);
        label.setStartOffset(this.getSelectionStart());
        label.setEndOffset(this.getSelectionStart() + 1);
        this.insertComponent(label);
    }

    protected List<Object> currentLabelComponents = new ArrayList<>();

    public List<Object> getLabelComponents() {
        return this.currentLabelComponents;
    }

    @Override
    public void remove(Component comp) {
        if (comp instanceof Container) {
            Component[] components = ((Container) comp).getComponents();
            if (components != null) {
                for (int i = 0; i < components.length; i++) {
                    if (components[i] instanceof CustomLabel) {
                        this.currentLabelComponents.remove(components[i]);
                    }
                }
            }
        } else if (comp instanceof CustomLabel) {
            this.currentLabelComponents.remove(comp);
        }
        super.remove(comp);
    }

    @Override
    public void copy() {
        super.copy();
    }

    @Override
    public void paste() {
        super.paste();
    }

    @Override
    public void insertComponent(Component c) {
        if (c instanceof CustomLabel) {
            this.currentLabelComponents.add(c);
        }
        super.insertComponent(c);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (int i = 0; i < this.currentLabelComponents.size(); i++) {
            ((Component) this.currentLabelComponents.get(i)).setEnabled(enabled);
        }

    }

    /**
     * Include the text of the {@link CustomLabel} components if they exist.
     */
    public String getExpression() {
        // Update the positions of the label components
        View rootView = this.getUI().getRootView(this);
        List<Object> lcurrentLabelComponents = new ArrayList<>();
        this.configureLabelPositions(lcurrentLabelComponents, rootView);

        // Create the text using the original text and the label texts
        String text = super.getText();
        if (this.currentLabelComponents.size() > 0) {
            Collections.sort(this.currentLabelComponents, new ComparatorLabelsPosition());
            StringBuilder sb = new StringBuilder(text);
            for (int i = this.currentLabelComponents.size() - 1; i >= 0; i--) {
                CustomLabel cLabel = (CustomLabel) this.currentLabelComponents.get(i);
                if ((sb.length() >= cLabel.getEndOffset()) && (cLabel.getStartOffset() >= 0)) {
                    try {
                        sb.replace(cLabel.getStartOffset(), cLabel.getStartOffset() + 1, cLabel.getOriginalText());
                    } catch (Exception e) {
                        ComponentTextPane.logger.error(null, e);
                    }
                }
            }
            text = sb.toString();
        }
        return text;
    }

    protected void configureLabelPositions(List<Object> components, View view) {
        if (view != null) {
            if (view instanceof ComponentLabelView) {
                Component labelComponent = ((ComponentLabelView) view).getComponent();
                if (labelComponent instanceof CustomLabel) {
                    int start = view.getElement().getStartOffset();
                    int end = view.getElement().getEndOffset();

                    if ((end - start) > 1) {
                        char startChar = this.getText().charAt(start);
                        if (startChar != ' ') {
                            start = start + 1;
                            this.getText().charAt(start);
                            this.getText().charAt(end);
                            ComponentTextPane.logger.debug(null);
                        }
                    }

                    ((CustomLabel) labelComponent).setStartOffset(start);
                    ((CustomLabel) labelComponent).setEndOffset(end);

                    components.add(labelComponent);
                }
            }

            for (int i = 0; i < view.getViewCount(); i++) {
                View viewChild = view.getView(i);
                this.configureLabelPositions(components, viewChild);
            }
        }
    }

    @Override
    public String getText() {
        return super.getText();
    };

    @Override
    public void setText(String t) {
        super.setText(t);
        this.clear();
    }

    protected void clear() {
        Component[] components = this.getComponents();
        for (int i = 0; i < components.length; i++) {
            this.remove(components[i]);
        }
    }

    /**
     * This class allow to configure the ViewFactory to use. By default the ViewFactory used is
     * {@link CustomViewFactory}
     */
    protected class CustomStyledEditorKit extends StyledEditorKit {

        protected ViewFactory viewFactory;

        protected void setViewFactory(ViewFactory factory) {
            this.viewFactory = factory;
        }

        @Override
        public ViewFactory getViewFactory() {
            if (this.viewFactory == null) {
                this.viewFactory = new CustomViewFactory();
            }
            return this.viewFactory;
        }

    }

    protected class CustomViewFactory implements ViewFactory {

        /**
         * Introduces the {@link ComponentLabelView} class when the name of the element if
         * {@link StyleConstants#ComponentElementName}
         */
        @Override
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                if (kind.equals(AbstractDocument.ContentElementName)) {
                    return new LabelView(elem);
                } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                    return new ParagraphView(elem);
                } else if (kind.equals(AbstractDocument.SectionElementName)) {
                    return new BoxView(elem, View.Y_AXIS);
                } else if (kind.equals(StyleConstants.ComponentElementName)) {
                    ComponentLabelView clv = new ComponentLabelView(elem);
                    return clv;
                } else if (kind.equals(StyleConstants.IconElementName)) {
                    return new IconView(elem);
                }
            }

            // default to text display
            return new LabelView(elem);
        }

    }

    protected class ComponentLabelView extends ComponentView {

        public ComponentLabelView(Element elem) {
            super(elem);
        }

        @Override
        protected Component createComponent() {
            AttributeSet attr = this.getElement().getAttributes();
            Component comp = StyleConstants.getComponent(attr);
            Rectangle bounds = comp.getBounds();
            // bounds.y = -3;
            comp.setBounds(bounds);
            return comp;
        }

        @Override
        public void paint(Graphics g, Shape a) {
            if (a instanceof Rectangle) {
                // To center the component in the line
                ((Rectangle) a).setBounds(a.getBounds().x, a.getBounds().y - 4, a.getBounds().width,
                        a.getBounds().height);
            }

            // Check if the element is in the selected area of the JTextPane to
            // configure the background color
            int startOffset = this.getStartOffset();
            int endOffset = this.getEndOffset();
            int selectionStart = ComponentTextPane.this.getSelectionStart();
            int selectionEnd = ComponentTextPane.this.getSelectionEnd();

            if ((selectionStart <= startOffset) && (selectionEnd >= endOffset)) {
                this.getComponent().setBackground(ComponentTextPane.this.getSelectionColor());
                this.getComponent().setForeground(ComponentTextPane.this.getSelectedTextColor());
            } else {
                this.getComponent().setBackground(ComponentTextPane.this.getBackground());
                this.getComponent().setForeground(ComponentTextPane.this.getForeground());
            }

            // Configure the position in the CustomLabel component
            if (this.getComponent() instanceof CustomLabel) {
                ((CustomLabel) this.getComponent()).setEndOffset(endOffset);
                ((CustomLabel) this.getComponent()).setStartOffset(startOffset);
            }
            super.paint(g, a);
        }

    }

    /**
     * Compare two objects. If the objects are instance of CustomLabel then compare the start offset of
     * this components, in other case return -1
     */
    protected static class ComparatorLabelsPosition implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            if ((o1 instanceof CustomLabel) && (o2 instanceof CustomLabel)) {
                return ((CustomLabel) o1).getStartOffset() - ((CustomLabel) o2).getStartOffset();
            }
            return -1;
        }

    }

    /**
     * Class used to insert the column names as components in the JTextPane. This class shows the
     * translated text but stores the original text too. This class knows the position in the JTextPanel
     * where it is
     */
    public static class CustomLabel extends JLabel {

        /**
         * Position in the JTextPane where this component starts
         */
        protected int startOffset = -1;

        /**
         * Position in the JTextPane where this components ends (usually is start offset + 1)
         */
        protected int endOffset = -1;

        /**
         * Original text
         */
        protected String originalText;

        /**
         * Create a new JLabel that shows the display text but stores the original one too
         * @param originaltext
         * @param displayText
         */
        public CustomLabel(String originaltext, String displayText) {
            super(displayText);
            this.originalText = originaltext;
        }

        public int getStartOffset() {
            return this.startOffset;
        }

        public void setStartOffset(int startOffset) {
            this.startOffset = startOffset;
        }

        public int getEndOffset() {
            return this.endOffset;
        }

        public void setEndOffset(int endOffset) {
            this.endOffset = endOffset;
        }

        public String getOriginalText() {
            return this.originalText;
        }

        @Override
        public Dimension getPreferredSize() {
            Font font = this.getFont();
            if (font != null) {
                FontMetrics fMetrics = this.getFontMetrics(font);
                int height = fMetrics.getHeight();

                Dimension dim = super.getPreferredSize();
                return new Dimension(dim.width, height);
            } else {
                return super.getPreferredSize();
            }
        }

    }

}
