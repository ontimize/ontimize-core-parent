package com.ontimize.util.rtf;

import java.awt.Color;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;
import javax.swing.tree.TreeNode;

import com.ontimize.util.rtf.style.RTFDocument;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codecimpl.PNGCodec;

public class RTFGenerator {

    protected Document document;

    protected Vector fontList;

    protected Vector colorList;

    public RTFGenerator(Document doc) {
        this.document = doc;
    }

    public void write(String fileName) throws IOException {
        FileWriter out = new FileWriter(fileName);
        this.write(out, 0, this.document.getLength());
        out.close();
    }

    public void write(Writer out, int pos, int len) throws IOException {
        Element root = this.getDocumentTree();
        JTree tree = new JTree((TreeNode) root);

        this.fontList = new Vector();
        this.fontList.add("Arial");
        this.fontList.add("Wingdings");
        this.fontList = this.getFontList(root, this.fontList);
        this.colorList = this.getDefaultColorList();
        this.colorList = this.getColorList(root, this.colorList);

        out.write("{\\rtf1\\ansi\\ansicpg1252");
        out.write(this.createFontTable(this.fontList));
        out.write(this.createColorTable(this.colorList));

        Element el = root.getElement(root.getElementIndex(pos));
        while (el.getName().equals("table")) {
            Element table = el;
            Element row = table.getElement(table.getElementIndex(pos));
            Element cell = row.getElement(row.getElementIndex(pos));
            if (cell.getEndOffset() < (pos + len)) {
                break;
            }
            root = cell;
            el = root.getElement(root.getElementIndex(pos));
        }

        this.writeContent(root, out, 0, pos, len);
        out.write("}");
        out.flush();
    }

    protected void writeContent(Element root, Writer out, int level, int pos, int len) throws IOException {
        int elCount = root.getElementCount();
        int startIndex = root.getElementIndex(pos);
        int endIndex = root.getElementIndex(pos + len);
        for (int i = startIndex; i <= endIndex; i++) {
            Element child = root.getElement(i);

            if (child.getStartOffset() > (pos + len)) {
                return;
            }
            if (child.getName().equals("paragraph")) {
                if ((level > 0) && (i == (elCount - 1))) {
                    this.writeParagraph(child, out, level, true, pos, len);
                } else {
                    this.writeParagraph(child, out, level, false, pos, len);
                }
            } else if (child.getName().equals("table")) {
                this.writeTable(child, out, level);
                if ((i == endIndex) && (level > 0)) {
                    out.write("\\pard\\intbl");
                    if (level > 1) {
                        out.write("\\itap" + Integer.toString(level));
                    }
                }
            }
        }
    }

    protected void writeParagraph(Element paragraph, Writer out, int level, boolean lastInTable, int pos, int len)
            throws IOException {
        out.write("\\pard ");
        int ind = 0;
        int elCount = paragraph.getElementCount();
        out.write(this.getParagraphDescription(paragraph.getAttributes()));
        if (level > 0) {
            out.write("\\intbl");
        }
        out.write("\\itap" + Integer.toString(level));
        int startIndex = paragraph.getElementIndex(pos);
        int endIndex = paragraph.getElementIndex(pos + len);
        for (int i = startIndex; i <= endIndex; i++) {
            Element leaf = paragraph.getElement(i);
            if (leaf.getName().equals("content")) {
                this.writeLeaf(leaf, out, pos, len);
            } else if (leaf.getName().equals("icon")) {
                this.writeIcon(leaf, out);
            }
        }
        if (!lastInTable) {
            out.write("\\par");
        }
    }

    protected void writeLeaf(Element leaf, Writer out, int pos, int len) throws IOException {
        AttributeSet attr = leaf.getAttributes();
        Document doc = leaf.getDocument();
        String contentText = "";
        try {
            int start = Math.max(leaf.getStartOffset(), pos);
            int end = Math.min(leaf.getEndOffset(), pos + len) - start;
            contentText = this.convertString(doc.getText(start, end));
        } catch (Exception ex) {
            throw new IOException("Error reading leaf content from source document!", ex);
        }
        if (contentText.length() <= 0) {
            return;
        }
        out.write(this.getBeforeFontDescription(attr, false) + " ");
        out.write(contentText);
        String after = this.getAfterFontDescription(leaf.getAttributes());
        if (after.length() > 0) {
            out.write(this.getAfterFontDescription(leaf.getAttributes()));
        }
    }

    protected void writeIcon(Element leaf, Writer out) throws IOException {
        AttributeSet attr = leaf.getAttributes();
        ImageIcon icon = (ImageIcon) StyleConstants.getIcon(attr);
        int w = StyleConstants.getIcon(attr).getIconWidth();
        int h = StyleConstants.getIcon(attr).getIconHeight();
        if (icon != null) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PNGCodec p = new PNGCodec();
            ImageEncoder pe = ImageCodec.createImageEncoder("PNG", os, null);
            BufferedImage bi = new BufferedImage(w, h, 2);
            bi.getGraphics().drawImage(icon.getImage(), 0, 0, null);
            pe.encode(bi);
            byte[] ba = os.toByteArray();

            int len = ba.length;
            StringBuilder sb = new StringBuilder(len * 2);
            for (int i = 0; i < len; i++) {
                String sByte = Integer.toHexString(ba[i] & 0xFF);
                if (sByte.length() < 2) {
                    sb.append('0' + sByte);
                } else {
                    sb.append(sByte);
                }
            }
            String s = sb.toString();
            String size = Integer.toString(s.length());
            out.write("{\\pict\\pngblip ");
            out.write(s);
            out.write("}");
        }
    }

    protected void writeTable(Element table, Writer out, int level) throws IOException {
        int rowCount = table.getElementCount();
        for (int i = 0; i < rowCount; i++) {
            Element row = table.getElement(i);
            this.writeRow(row, out, level);
        }
    }

    protected void writeRow(Element row, Writer out, int level) throws IOException {
        for (int i = 0; i < row.getElementCount(); i++) {
            this.writeCellContent(row.getElement(i), out, level);
        }

        if (level > 0) {
            out.write("{\\*\\nesttableprops");
        }
        out.write("{\\trowd");
        Element table = row.getParentElement();
        AttributeSet tableAttr = table.getAttributes();
        switch (StyleConstants.getAlignment(tableAttr)) {
            case 0:
                out.write("\\trql");
                break;
            case 2:
                out.write("\\trqr");
                break;
            case 1:
                out.write("\\trqc");
        }

        AttributeSet attr = row.getAttributes();
        int indent = new Float(StyleConstants.getLeftIndent(attr)).intValue();
        out.write("\\tleft-" + Integer.toString(indent));

        out.write("\\trftsWidth1");
        out.write("\\trpaddl108");
        out.write("\\trpaddr108");
        out.write("\\trpaddfl3");
        out.write("\\trpaddfr3 ");
        int x = 1;
        for (int i = 0; i < row.getElementCount(); i++) {
            RTFDocument.CellElement cell = (RTFDocument.CellElement) row.getElement(i);
            Double dX = this.convertPixelsToTwips(new Double(x));
            this.writeCell(row.getElement(i), out, dX.intValue(), level);
            x += cell.getWidth();
        }
        if (level == 0) {
            out.write("\\row}");
        } else {
            out.write("\\nestrow}}");
        }
    }

    protected void writeCellContent(Element cell, Writer out, int level) throws IOException {
        this.writeContent(cell, out, level + 1, cell.getStartOffset(), cell.getEndOffset() - cell.getStartOffset());
        if (level == 0) {
            out.write("\\cell");
        } else {
            out.write("\\nestcell{\\nonesttables\\par }");
        }
    }

    protected void writeCell(Element cell, Writer out, int x, int level) throws IOException {
        out.write("\\clvertalt");
        AttributeSet attr = cell.getAttributes();
        RTFDocument.CellElement currentCell = (RTFDocument.CellElement) cell;

        BorderAttributes ba = (BorderAttributes) attr.getAttribute("BorderAttributes");
        if (ba != null) {
            String borderType = "\\brdrs";

            Color bc = ba.lineColor;
            String lineColor = "";
            if (bc != Color.black) {
                lineColor = "\\brdrcf" + (this.colorList.indexOf(bc) + 1);
            }
            if (ba.borderTop != 0) {
                out.write("\\clbrdrt" + borderType + "\\brdrw10" + lineColor);
            }
            if (ba.borderLeft != 0) {
                out.write("\\clbrdrl" + borderType + "\\brdrw10" + lineColor);
            }
            if (ba.borderBottom != 0) {
                out.write("\\clbrdrb" + borderType + "\\brdrw10" + lineColor);
            }
            if (ba.borderRight != 0) {
                out.write("\\clbrdrr" + borderType + "\\brdrw10" + lineColor);
            }
            Insets margins = currentCell.getMargins();
            out.write("\\clpadl" + Integer.toString(margins.left * 15));
            out.write("\\clpadr" + Integer.toString(margins.right * 15));
            out.write("\\clpadt" + Integer.toString(margins.top * 15));
            out.write("\\clpadb" + Integer.toString(margins.bottom * 15));
            out.write("\\clpadfl3");
            out.write("\\clpadfr3");
            out.write("\\clpadft3");
            out.write("\\clpadfb3");
            out.write("\\clftsWidth3");
        }
        Double dWidth = this.convertPixelsToTwips(new Double(currentCell.getWidth()));
        out.write("\\clwWidth" + Integer.toString(dWidth.intValue()));
        out.write("\\cellx" + Integer.toString(x) + " ");
    }

    protected Element getDocumentTree() {
        StyledDocument doc = (StyledDocument) this.document;
        return doc.getDefaultRootElement();
    }

    protected Vector getFontList(Element root, Vector list) {
        AttributeSet attr = root.getAttributes();
        String curFontName = StyleConstants.getFontFamily(attr);
        if (!this.isInList(list, curFontName)) {
            list.add(curFontName);
        }

        int cnt = root.getElementCount();
        for (int i = 0; i < cnt; i++) {
            Element el = root.getElement(i);
            list = this.getFontList(el, list);
        }
        return list;
    }

    protected boolean isInList(Vector list, Object fontName) {
        int len = list.size();
        for (int i = 0; i < len; i++) {
            if (fontName.equals(list.get(i))) {
                return true;
            }
        }
        return false;
    }

    protected String createFontTable(Vector fontList) {
        String result = "";
        int fontN = 0;
        result = result + "{\\fonttbl";
        int len = fontList.size();
        for (int i = 0; i < len; i++) {
            result = result + "{\\f" + new Integer(fontN).toString();
            fontN++;
            result = result + "\\fnil\\fcharset1\\fprq2 ";
            result = result + (String) fontList.get(i) + ";}";
        }
        result = result + "}";
        return result;
    }

    protected Vector getDefaultColorList() {
        Vector result = new Vector();
        int[] values = { 0, 128, 192, 255 };
        for (int r = 0; r < values.length; r++) {
            for (int g = 0; g < values.length; g++) {
                for (int b = 0; b < values.length; b++) {
                    Color c = new Color(values[r], values[g], values[b]);
                    result.add(c);
                }
            }
        }
        return result;
    }

    protected Vector getColorList(Element root, Vector list) {
        AttributeSet attr = root.getAttributes();
        Color bgColor = StyleConstants.getBackground(attr);
        if (!this.isInList(list, bgColor)) {
            list.add(bgColor);
        }
        Color fgColor = StyleConstants.getForeground(attr);
        if (!this.isInList(list, fgColor)) {
            list.add(fgColor);
        }
        BorderAttributes ba = (BorderAttributes) attr.getAttribute("BorderAttributes");
        if ((ba != null) && !this.isInList(list, ba.lineColor)) {
            list.add(ba.lineColor);
        }

        int cnt = root.getElementCount();
        for (int i = 0; i < cnt; i++) {
            Element el = root.getElement(i);
            list = this.getColorList(el, list);
        }
        return list;
    }

    protected String createColorTable(Vector colorList) {
        String result = "";

        result = result + "{\\colortbl;";
        int len = colorList.size();
        for (int i = 0; i < len; i++) {
            Color c = (Color) colorList.get(i);
            int red = c.getRed();
            int green = c.getGreen();
            int blue = c.getBlue();
            result = result + "\\red" + new Integer(red).toString();
            result = result + "\\green" + new Integer(green).toString();
            result = result + "\\blue" + new Integer(blue).toString() + ";";
        }
        result = result + "}";
        return result;
    }

    protected String getBeforeFontDescription(AttributeSet attr, boolean isStyle) {
        String result = "";

        if (StyleConstants.isItalic(attr)) {
            result = result + "\\i";
        }
        if (StyleConstants.isUnderline(attr)) {
            result = result + "\\ul";
        }
        if (StyleConstants.isStrikeThrough(attr)) {
            result = result + "\\strike";
        }
        if (StyleConstants.isSubscript(attr)) {
            result = result + "\\sub";
        }
        result = result + "\\f" + this.fontList.indexOf(StyleConstants.getFontFamily(attr));
        result = result + "\\fs" + new Integer(StyleConstants.getFontSize(attr) * 2).toString();

        boolean openSubgroup = false;
        if (StyleConstants.isBold(attr)) {
            result = result + "{\\b";
            openSubgroup = true;
        }
        if (StyleConstants.isSuperscript(attr)) {
            if (!openSubgroup) {
                result = result + "{";
            }
            result = result + "\\super";
            openSubgroup = true;
        }
        Color fg = (Color) attr.getAttribute(StyleConstants.Foreground);
        if (fg != null) {
            if (!isStyle && !openSubgroup) {
                result = result + "{";
            }
            result = result + "\\cf" + (this.colorList.indexOf(fg) + 1);
        }

        Color bg = (Color) attr.getAttribute(StyleConstants.Background);
        if (bg != null) {
            if (!isStyle) {
                result = result + "{";
            }
            result = result + "\\highlight" + (this.colorList.indexOf(bg) + 1);
        }
        return result;
    }

    protected String getAfterFontDescription(AttributeSet attr) {
        String result = "";

        Color bg = (Color) attr.getAttribute(StyleConstants.Background);
        boolean openSubgroup = false;
        if (bg != null) {
            result = result + "}";
            openSubgroup = true;
        }
        Color fg = (Color) attr.getAttribute(StyleConstants.Foreground);
        if (fg != null) {
            result = result + "}";
            openSubgroup = true;
        }
        if (StyleConstants.isSuperscript(attr) && !openSubgroup) {
            result = result + "} ";
        }

        if (StyleConstants.isBold(attr) && !openSubgroup) {
            result = result + "} ";
        }

        if (StyleConstants.isItalic(attr)) {
            result = result + "\\i0";
        }
        if (StyleConstants.isUnderline(attr)) {
            result = result + "\\ulnone";
        }
        if (StyleConstants.isStrikeThrough(attr)) {
            result = result + "\\strike0";
        }
        if (StyleConstants.isSubscript(attr)) {
            result = result + "\\sub0";
        }
        return result;
    }

    protected String getParagraphDescription(AttributeSet attr) {
        String result = "";
        StyleConstants.getIcon(attr);

        switch (StyleConstants.getAlignment(attr)) {
            case 0:
                result = result + "\\ql ";
                break;
            case 2:
                result = result + "\\qr ";
                break;
            case 1:
                result = result + "\\qc ";
                break;
            case 3:
                result = result + "\\qj ";
        }

        TabSet ts = StyleConstants.getTabSet(attr);
        if (ts != null) {
            for (int i = 0; i < ts.getTabCount(); i++) {
                TabStop stop = ts.getTab(i);
                Double f = new Double(stop.getPosition());
                f = this.convertPixelsToTwips(f);
                result = result + "\\tx" + new Integer(f.intValue()).toString();
            }
        }
        if (Float.compare(StyleConstants.getLeftIndent(attr), 0) != 0) {
            Double f = new Double(StyleConstants.getLeftIndent(attr));
            f = this.convertPixelsToTwips(f);
            result = result + "\\li" + new Integer(f.intValue()).toString();
        } else {
            result = result + "\\li0";
        }
        if (Float.compare(StyleConstants.getRightIndent(attr), 0) != 0) {
            Double f = new Double(StyleConstants.getRightIndent(attr));
            f = this.convertPixelsToTwips(f);
            result = result + "\\ri" + new Integer(f.intValue()).toString();
        } else {
            result = result + "\\ri0";
        }
        if (Float.compare(StyleConstants.getFirstLineIndent(attr), 0) != 0) {
            Double f = new Double(StyleConstants.getFirstLineIndent(attr));
            f = this.convertPixelsToTwips(f);
            result = result + "\\fi" + new Integer(f.intValue()).toString();
        } else {
            result = result + "\\fi0";
        }

        if (Float.compare(StyleConstants.getSpaceAbove(attr), 0) != 0) {
            Double f = new Double(StyleConstants.getSpaceAbove(attr));
            f = this.convertPixelsToTwips(f);
            result = result + "\\sa" + new Integer(f.intValue()).toString();
        } else {
            result = result + "\\sa0";
        }
        if (Float.compare(StyleConstants.getSpaceBelow(attr), 0) != 0) {
            Double f = new Double(StyleConstants.getSpaceBelow(attr));
            f = this.convertPixelsToTwips(f);
            result = result + "\\sb" + new Integer(f.intValue()).toString();
        } else {
            result = result + "\\sb0";
        }
        if (Float.compare(StyleConstants.getLineSpacing(attr), 0.0F) != 0) {
            double spacing = StyleConstants.getLineSpacing(attr);
            if (spacing < 1.0D) {
                spacing = 1.0D;
            }
            spacing *= 240.0D;
            result = result + "\\sl" + new Integer(new Double(spacing).intValue()).toString() + "\\slmult1";
        } else {
            result = result + "\\sl240";
        }
        return result;
    }

    protected Double convertPixelsToTwips(Double value) {
        double result = value.doubleValue();

        result *= 15.0D;
        return new Double(result);
    }

    protected String convertString(String source) {
        String dest = "";
        int i = 0;
        int index = source.indexOf('\\', i);
        while (index >= 0) {
            dest = dest + source.substring(i, index + 1) + '\\';
            i = index + 1;
            index = source.indexOf('\\', i);
        }
        dest = dest + source.substring(i);
        source = dest;
        dest = "";

        i = 0;
        index = source.indexOf('\t', i);
        while (index >= 0) {
            dest = dest + source.substring(i, index) + '\\' + "tab ";
            i = index + 1;
            index = source.indexOf('\t', i);
        }
        dest = dest + source.substring(i);

        i = 0;
        String src = dest;
        dest = "";
        index = src.indexOf('{', i);
        while (index >= 0) {
            dest = dest + src.substring(i, index) + '\\' + '{';
            i = index + 1;
            index = src.indexOf('{', i);
        }
        dest = dest + src.substring(i);
        i = 0;

        src = dest;
        dest = "";
        index = src.indexOf('}', i);
        while (index >= 0) {
            dest = dest + src.substring(i, index) + '\\' + '}';
            i = index + 1;
            index = src.indexOf('}', i);
        }
        dest = dest + src.substring(i);

        i = 0;
        src = dest;
        dest = "";
        index = src.indexOf("\f", i);
        while (index >= 0) {
            dest = dest + src.substring(i, index) + '\\' + "page";
            i = index + 1;
            index = src.indexOf('\f', i);
        }
        dest = dest + src.substring(i);

        return dest;
    }

}
