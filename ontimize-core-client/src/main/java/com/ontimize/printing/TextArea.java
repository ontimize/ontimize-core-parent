package com.ontimize.printing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Map;

import javax.swing.JTextArea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextArea extends RectangleElement {

    private static final Logger logger = LoggerFactory.getLogger(TextArea.class);

    private final JTextArea textArea = new JTextArea();

    public JTextArea getTextArea() {
        return this.textArea;
    }

    public TextArea(Map<Object, Object> parameters) {
        super(parameters);

        this.textArea.setOpaque(false);
        this.textArea.setBorder(null);
        this.textArea.setLineWrap(true);
        this.textArea.setWrapStyleWord(true);

        if (AbstractPrintingElement.defaultFont != null) {
            this.textArea.setFont(AbstractPrintingElement.defaultFont);
        }

        if (this.color != null) {
            this.textArea.setForeground(this.color);
        }
        try {
            if (this.italics && this.bold) {
                this.textArea.setFont(this.textArea.getFont().deriveFont(Font.ITALIC | Font.BOLD));
            } else if (this.italics) {
                this.textArea.setFont(this.textArea.getFont().deriveFont(Font.ITALIC));
            } else if (this.bold) {
                this.textArea.setFont(this.textArea.getFont().deriveFont(Font.BOLD));
            }
        } catch (Exception exc) {
            TextArea.logger.error(exc.getMessage(), exc);
        }

        Object c = parameters.get("text");
        if (c != null) {
            this.setContent(c);

        }
    }

    @Override
    public void setContent(Object c) {
        super.setContent(c);
        if (c != null) {
            this.textArea.setText(this.contain.toString());
        }
    }

    @Override
    public void paint(Graphics g, double scale) {
        if (this.color == null) {
            return;
        }
        Color c = g.getColor();
        g.setColor(this.color);

        this.textArea.setFont(this.textArea.getFont().deriveFont((float) this.fontSize));
        int iPreviousTextSize = this.textArea.getFontMetrics(this.textArea.getFont())
            .stringWidth(this.textArea.getText());
        int aimSize = (int) (scale * iPreviousTextSize);
        try {
            if (Double.compare(scale, 1) == 0) {
                this.textArea.setFont(this.textArea.getFont().deriveFont((float) this.fontSize));
            } else if (scale < 1.0) {
                this.textArea.setFont(this.textArea.getFont().deriveFont((float) (scale * this.fontSize)));
                int iCurrentTextSize = this.textArea.getFontMetrics(this.textArea.getFont())
                    .stringWidth(this.textArea.getText());
                int iCurrentFontSize = (int) (scale * this.fontSize);
                while ((iCurrentTextSize > aimSize) && (iCurrentFontSize > 5)) {
                    iCurrentFontSize--;
                    this.textArea.setFont(this.textArea.getFont().deriveFont((float) iCurrentFontSize));
                    iCurrentTextSize = this.textArea.getFontMetrics(this.textArea.getFont())
                        .stringWidth(this.textArea.getText());
                }
            } else {
                this.textArea.setFont(this.textArea.getFont().deriveFont((float) (scale * this.fontSize)));
                int iCurrentTextSize = this.textArea.getFontMetrics(this.textArea.getFont())
                    .stringWidth(this.textArea.getText());
                int iCurrentFontSize = (int) (scale * this.fontSize);
                while ((iCurrentTextSize < aimSize) && (iCurrentFontSize < 60)) {
                    iCurrentFontSize++;
                    this.textArea.setFont(this.textArea.getFont().deriveFont((float) iCurrentFontSize));
                    iCurrentTextSize = this.textArea.getFontMetrics(this.textArea.getFont())
                        .stringWidth(this.textArea.getText());
                }
            }
        } catch (Exception exc) {
            TextArea.logger.error(exc.getMessage(), exc);
        }

        if ((this.width != -1) && (this.high != -1)) {
            this.textArea.setSize((int) (AbstractPrintingElement.millimeterToPagePixels(this.width) * scale),
                    (int) (AbstractPrintingElement.millimeterToPagePixels(this.high) * scale));
        } else {
            this.textArea.setSize(this.textArea.getPreferredSize());
        }
        g.translate((int) (AbstractPrintingElement.millimeterToPagePixels(this.xi) * scale),
                (int) (AbstractPrintingElement.millimeterToPagePixels(this.yi) * scale));
        this.textArea.paint(g);
        g.translate((int) (-AbstractPrintingElement.millimeterToPagePixels(this.xi) * scale),
                (int) (-AbstractPrintingElement.millimeterToPagePixels(this.yi) * scale));
        g.setColor(c);
    }

}
