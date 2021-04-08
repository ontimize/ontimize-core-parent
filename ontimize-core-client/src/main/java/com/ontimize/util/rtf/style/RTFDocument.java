package com.ontimize.util.rtf.style;

import java.awt.Insets;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.GapContent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.undo.UndoableEdit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.util.rtf.BorderAttributes;

public class RTFDocument extends DefaultStyledDocument {

    private static final Logger logger = LoggerFactory.getLogger(RTFDocument.class);

    public int DOCUMENT_WIDTH = -1;

    public boolean isSplitted = false;

    private Insets margins = new Insets(0, 0, 0, 0);

    public RTFDocument(AbstractDocument.Content c, StyleContext styles) {
        super(c, styles);
    }

    public RTFDocument(StyleContext styles) {
        this(new GapContent(4096), styles);
    }

    public RTFDocument() {
        this(new GapContent(4096), new StyleContext());
    }

    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        super.insertString(offs, str, a);
    }

    @Override
    protected Element createLeafElement(Element parent, AttributeSet a, int p0, int p1) {
        return new LeafElement(parent, a, p0, p1);
    }

    @Override
    protected Element createBranchElement(Element parent, AttributeSet a) {
        return new BranchElement(parent, a);
    }

    public Element insertTable(int offset, int rowCount, int colCount, AttributeSet attr, int[] colWidths,
            int[] rowHeights) {
        Element table = null;
        try {
            Element root = this.getDefaultRootElement();

            Element elem = root;
            while (!elem.isLeaf()) {
                root = elem;
                elem = elem.getElement(elem.getElementIndex(offset));
            }
            Element paragraph = root;
            root = root.getParentElement();
            int insertIndex = root.getElementIndex(offset);

            if ((offset > paragraph.getStartOffset()) && (offset < paragraph.getEndOffset())) {
                this.insertString(offset, "\n", new SimpleAttributeSet());
                insertIndex++;
            }

            int insertOffset = root.getElement(insertIndex).getStartOffset();
            AbstractDocument.Content c = this.getContent();
            String ins = "";

            for (int i = 0; i < (rowCount * colCount); i++) {
                ins = ins + '\n';
            }
            this.writeLock();
            UndoableEdit u = c.insertString(insertOffset, ins);
            AbstractDocument.DefaultDocumentEvent dde = new AbstractDocument.DefaultDocumentEvent(insertOffset,
                    rowCount * colCount, DocumentEvent.EventType.INSERT);
            dde.addEdit(u);
            this.insertUpdate(dde, new SimpleAttributeSet());
            dde.end();

            this.fireInsertUpdate(dde);

            AbstractDocument.DefaultDocumentEvent e = new AbstractDocument.DefaultDocumentEvent(insertOffset,
                    rowCount * colCount, DocumentEvent.EventType.INSERT);
            e.addEdit(u);
            int[] rowOffsets = new int[rowCount];
            int[] rowLenghts = new int[rowCount];
            for (int i = 0; i < rowCount; i++) {
                rowOffsets[i] = insertOffset + (i * colCount);
                rowLenghts[i] = colCount;
            }

            table = new TableElement(rowOffsets, rowLenghts, root, attr, rowCount, colCount, colWidths, rowHeights);
            Element[] el = new Element[1];
            el[0] = table;
            Element[] repl = new Element[rowCount * colCount];
            for (int i = 0; i < (rowCount * colCount); i++) {
                repl[i] = root.getElement(insertIndex + i);
            }

            ((AbstractDocument.BranchElement) root).replace(insertIndex, rowCount * colCount, el);
            AbstractDocument.ElementEdit uu = new AbstractDocument.ElementEdit(root, insertIndex, repl, el);
            e.addEdit(uu);
            this.fireInsertUpdate(e);
            e.end();
        } catch (Exception error) {
            RTFDocument.logger.error("Can't insert table!", error);
        } finally {
            this.writeUnlock();
        }
        return table;
    }

    public void insertPicture(ImageIcon icon, int pos) {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setIcon(attrs, icon);
        try {
            this.insertString(pos, " ", attrs);
        } catch (BadLocationException e) {
            RTFDocument.logger.error(null, e);
            JOptionPane.showMessageDialog(null, "Can't insert image!");
        }
    }

    public void deleteTable(int offset) {
        Element elem = this.getDefaultRootElement();

        Element table = null;

        while (!elem.isLeaf()) {
            if (elem.getName().equals("table")) {
                table = elem;
            }
            elem = elem.getElement(elem.getElementIndex(offset));
        }
        if (table != null) {
            AbstractDocument.BranchElement root = (AbstractDocument.BranchElement) table.getParentElement();

            if (root.getChildCount() == 1) {
                return;
            }
            int start = table.getStartOffset();
            int end = table.getEndOffset();
            try {
                AbstractDocument.DefaultDocumentEvent e = new AbstractDocument.DefaultDocumentEvent(start, end - start,
                        DocumentEvent.EventType.REMOVE);
                int index = root.getElementIndex(offset);
                AbstractDocument.ElementEdit ee = new AbstractDocument.ElementEdit(root, index, new Element[] { table },
                        new Element[0]);
                this.getContent().remove(start, end - start);
                root.replace(index, 1, new Element[0]);
                e.addEdit(ee);
                e.end();
                this.fireRemoveUpdate(e);
            } catch (Exception ex) {
                RTFDocument.logger.error(null, ex);
                JOptionPane.showMessageDialog(null, "Can't delete table! " + ex.getMessage());
            }
        }
    }

    public void deleteRow(int offset) {
        Element elem = this.getDefaultRootElement();

        Element row = null;

        while (!elem.isLeaf()) {
            if (elem.getName().equals("row")) {
                row = elem;
            }
            elem = elem.getElement(elem.getElementIndex(offset));
        }
        if (row != null) {
            AbstractDocument.BranchElement table = (AbstractDocument.BranchElement) row.getParentElement();

            if (table.getChildCount() == 1) {
                try {
                    this.remove(table.getStartOffset(), table.getEndOffset() - table.getStartOffset());
                    return;
                } catch (Exception ex) {
                    RTFDocument.logger.error(null, ex);
                }
            }
            int start = row.getStartOffset();
            int end = row.getEndOffset();
            try {
                AbstractDocument.DefaultDocumentEvent e = new AbstractDocument.DefaultDocumentEvent(start, end - start,
                        DocumentEvent.EventType.REMOVE);
                int rowNum = table.getElementIndex(offset);
                AbstractDocument.ElementEdit ee = new AbstractDocument.ElementEdit(table, rowNum, new Element[] { row },
                        new Element[0]);
                this.getContent().remove(start, end - start);
                table.replace(rowNum, 1, new Element[0]);
                e.addEdit(ee);
                e.end();
                this.fireRemoveUpdate(e);
            } catch (Exception ex) {
                RTFDocument.logger.error(null, ex);
                JOptionPane.showMessageDialog(null, "Can't delete row! " + ex.getMessage());
            }
        }
    }

    public void deleteColumn(int offset) {
        Element elem = this.getDefaultRootElement();

        Element cell = null;
        Element table = null;

        while (!elem.isLeaf()) {
            if (elem.getName().equals("table")) {
                table = elem;
            }
            if (elem.getName().equals("cell")) {
                cell = elem;
            }
            elem = elem.getElement(elem.getElementIndex(offset));
        }
        if (cell != null) {
            Element row = cell.getParentElement();

            if (row.getElementCount() == 1) {
                try {
                    this.remove(table.getStartOffset(), table.getEndOffset() - table.getStartOffset());
                    return;
                } catch (Exception ex) {
                    RTFDocument.logger.error(null, ex);
                }
            }
            int colNum = row.getElementIndex(offset);

            for (int i = 0; i < table.getElementCount(); i++) {
                AbstractDocument.BranchElement editableRow = (AbstractDocument.BranchElement) table.getElement(i);
                Element editableCell = editableRow.getElement(colNum);
                AbstractDocument.DefaultDocumentEvent e = new AbstractDocument.DefaultDocumentEvent(
                        editableCell.getStartOffset(),
                        editableCell.getEndOffset() - editableCell.getStartOffset(), DocumentEvent.EventType.REMOVE);
                AbstractDocument.ElementEdit ee = new AbstractDocument.ElementEdit(editableRow, colNum,
                        new Element[] { editableCell }, new Element[0]);
                try {
                    this.getContent()
                        .remove(editableCell.getStartOffset(),
                                editableCell.getEndOffset() - editableCell.getStartOffset());
                } catch (Exception ex) {
                    RTFDocument.logger.error(null, ex);
                }
                e.addEdit(ee);
                e.end();

                editableRow.replace(colNum, 1, new Element[0]);

                this.fireRemoveUpdate(e);
            }
        }
    }

    public void insertRow(int offset, boolean insertAbove) {
        Element elem = this.getDefaultRootElement();

        Element row = null;

        while (!elem.isLeaf()) {
            if (elem.getName().equals("row")) {
                row = elem;
            }
            elem = elem.getElement(elem.getElementIndex(offset));
        }
        if (row != null) {
            AbstractDocument.BranchElement table = (AbstractDocument.BranchElement) row.getParentElement();
            int insertOffset = row.getStartOffset();
            int insertIndex = table.getElementIndex(insertOffset);
            if (!insertAbove) {
                insertIndex++;
                insertOffset = row.getEndOffset();
                if (insertIndex < table.getElementCount()) {
                    row = table.getElement(insertIndex);
                }
            }
            int cellCount = row.getElementCount();

            AbstractDocument.Content c = this.getContent();
            String ins = "";
            for (int i = 0; i < cellCount; i++) {
                ins = ins + '\n';
            }
            this.writeLock();
            try {
                UndoableEdit u = c.insertString(insertOffset, ins);
                AbstractDocument.DefaultDocumentEvent dde = new AbstractDocument.DefaultDocumentEvent(insertOffset,
                        cellCount, DocumentEvent.EventType.INSERT);
                dde.addEdit(u);
                MutableAttributeSet attr = new SimpleAttributeSet();
                this.insertUpdate(dde, attr);
                dde.end();
                this.fireInsertUpdate(dde);
            } catch (Exception ex) {
                RTFDocument.logger.error("Insert row error! " + ex.getMessage(), ex);
            }

            AbstractDocument.DefaultDocumentEvent e = new AbstractDocument.DefaultDocumentEvent(insertOffset, cellCount,
                    DocumentEvent.EventType.INSERT);
            int[] widths = new int[cellCount];
            int[] offsets = new int[cellCount];
            int[] lengths = new int[cellCount];
            for (int i = 0; i < cellCount; i++) {
                widths[i] = ((RowElement) row).getCellWidth(i);
                offsets[i] = insertOffset + i;
                lengths[i] = 1;
            }
            MutableAttributeSet attr = new SimpleAttributeSet();
            BorderAttributes rowBorders = (BorderAttributes) row.getAttributes().getAttribute("BorderAttributes");
            BorderAttributes ba = new BorderAttributes();
            ba.setBorders(rowBorders.getBorders());
            ba.lineColor = rowBorders.lineColor;
            attr.addAttribute("BorderAttributes", ba);

            Element[] rows = new Element[1];
            rows[0] = new RowElement(table, attr, cellCount, offsets, lengths, widths, 1);

            Element[] removed = new Element[cellCount];
            if (insertIndex < table.getElementCount()) {
                CellElement cell = (CellElement) row.getElement(0);
                for (int k = 0; k < cellCount; k++) {
                    removed[k] = cell.getElement(k);
                }
                cell.replace(0, cellCount, new Element[0]);
                e.addEdit(new AbstractDocument.ElementEdit(cell, 0, removed, new Element[0]));
            } else {
                AbstractDocument.BranchElement tableParent = (AbstractDocument.BranchElement) table.getParentElement();
                int replIndex = tableParent.getElementIndex(table.getEndOffset());
                for (int k = 0; k < cellCount; k++) {
                    removed[k] = tableParent.getElement(replIndex + k);
                }
                tableParent.replace(replIndex, cellCount, new Element[0]);
                e.addEdit(new AbstractDocument.ElementEdit(tableParent, replIndex, removed, new Element[0]));
            }
            table.replace(insertIndex, 0, rows);
            e.addEdit(new AbstractDocument.ElementEdit(table, insertIndex, new Element[0], rows));
            e.end();
            this.fireInsertUpdate(e);
            this.writeUnlock();
        }
    }

    public void insertColumn(int offset, int colWidth, boolean insertBefore) {
        Element elem = this.getDefaultRootElement();

        Element row = null;
        Element table = null;
        while (!elem.isLeaf()) {
            if (elem.getName().equals("table")) {
                table = elem;
            }
            if (elem.getName().equals("row")) {
                row = elem;
            }
            elem = elem.getElement(elem.getElementIndex(offset));
        }
        if (row != null) {
            int colNum = row.getElementIndex(offset);
            if (!insertBefore) {
                colNum++;
            }

            Element[] addedCells = new Element[table.getElementCount()];
            for (int i = 0; i < table.getElementCount(); i++) {
                RowElement editableRow = (RowElement) table.getElement(i);
                int insertOffset;
                if (colNum < editableRow.getElementCount()) {
                    insertOffset = editableRow.getElement(colNum).getStartOffset();
                } else {
                    insertOffset = editableRow.getEndOffset();
                }
                AbstractDocument.Content c = this.getContent();
                this.writeLock();
                try {
                    UndoableEdit u = c.insertString(insertOffset, "\n");
                    AbstractDocument.DefaultDocumentEvent dde = new AbstractDocument.DefaultDocumentEvent(insertOffset,
                            1, DocumentEvent.EventType.INSERT);
                    dde.addEdit(u);
                    MutableAttributeSet attr = new SimpleAttributeSet();
                    super.insertUpdate(dde, attr);
                    dde.end();
                    this.fireInsertUpdate(dde);
                } catch (Exception ex) {
                    RTFDocument.logger.error("Insert column error! " + ex.getMessage(), ex);
                }

                AbstractDocument.DefaultDocumentEvent e = new AbstractDocument.DefaultDocumentEvent(insertOffset, 1,
                        DocumentEvent.EventType.INSERT);
                CellElement cell;
                if (colNum < editableRow.getElementCount()) {
                    cell = (CellElement) editableRow.getElement(colNum);
                } else {
                    cell = (CellElement) editableRow.getElement(editableRow.getElementCount() - 1);
                }
                int removeIndex;
                AbstractDocument.BranchElement remove;
                AbstractDocument.BranchElement paragraph;
                if (colNum < editableRow.getElementCount()) {
                    remove = (AbstractDocument.BranchElement) editableRow.getElement(colNum);
                    paragraph = (AbstractDocument.BranchElement) remove.getElement(0);
                    removeIndex = 0;
                } else {
                    AbstractDocument.BranchElement parent = (AbstractDocument.BranchElement) editableRow
                        .getParentElement();
                    int rowIndex = parent.getElementIndex(editableRow.getStartOffset());
                    rowIndex++;
                    if (rowIndex < parent.getElementCount()) {
                        remove = (AbstractDocument.BranchElement) parent.getElement(rowIndex).getElement(0);
                        paragraph = (AbstractDocument.BranchElement) remove.getElement(0);
                        removeIndex = 0;
                    } else {
                        remove = (AbstractDocument.BranchElement) parent.getParentElement();
                        removeIndex = remove.getElementIndex(parent.getStartOffset());
                        removeIndex++;
                        paragraph = (AbstractDocument.BranchElement) remove.getElement(removeIndex);
                    }
                }
                remove.replace(removeIndex, 1, new Element[0]);
                Element[] removed = new Element[1];
                removed[0] = paragraph;
                e.addEdit(new AbstractDocument.ElementEdit(remove, removeIndex, removed, new Element[0]));

                MutableAttributeSet attr = new SimpleAttributeSet();
                BorderAttributes cellBorders = (BorderAttributes) cell.getAttributes().getAttribute("BorderAttributes");
                BorderAttributes ba = new BorderAttributes();
                ba.setBorders(cellBorders.getBorders());
                ba.lineColor = cellBorders.lineColor;
                attr.addAttribute("BorderAttributes", ba);

                Element[] rows = new Element[1];
                rows[0] = new CellElement(editableRow, attr, insertOffset, 1, colWidth, 1);
                addedCells[i] = rows[0];
                editableRow.replace(colNum, 0, rows);
                e.addEdit(new AbstractDocument.ElementEdit(editableRow, colNum, new Element[0], rows));
                e.end();
                this.fireInsertUpdate(e);
                this.writeUnlock();
            }
        }
    }

    public void setDocumentMargins(Insets margins) {
        this.margins = margins;
        this.refresh();
    }

    public Insets getDocumentMargins() {
        return this.margins;
    }

    @Override
    public void remove(int offset, int length) throws BadLocationException {
        Element startCell = this.getCell(offset);
        Element endCell = this.getCell(offset + length);
        String text = this.getText(offset, length);
        if (startCell != endCell) {
            Element startCellTable = null;
            Element endCellTable = null;
            if (startCell != null) {
                if (startCell.getEndOffset() == (offset + length)) {
                    String s = this.getText(startCell.getStartOffset(), offset - startCell.getStartOffset());
                    if (s.length() == 0) {
                        return;
                    }
                    if ((s.charAt(s.length() - 1) == '\n') && text.equals("\n")) {
                        this.deleteLastParagraph((CellElement) startCell);
                    }
                }
                startCellTable = startCell.getParentElement().getParentElement();
                if ((startCellTable.getStartOffset() < offset) || (startCellTable.getEndOffset() > (offset + length))) {
                    return;
                }
            }
            if (endCell != null) {
                endCellTable = endCell.getParentElement().getParentElement();
                if ((endCellTable.getStartOffset() < offset) || (endCellTable.getEndOffset() > (offset + length))) {
                    return;
                }

            }

        }

        Vector tableList = this.getInnerTableList(offset, offset + length);
        if (tableList.size() == 0) {
            super.remove(offset, length);
        } else {
            int currentLength = length;
            boolean flag = true;
            for (int i = 0; i < tableList.size(); i++) {
                Element table = (Element) tableList.get(i);
                if ((offset > table.getStartOffset()) && ((offset + length) < table.getEndOffset())) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                super.remove(offset, length);
            }
        }
    }

    public Element getCell(int offset) {
        Element cell = null;
        Element elem = this.getDefaultRootElement();

        while (!elem.isLeaf()) {
            if (elem.getName().equals("cell")) {
                cell = elem;
            }
            elem = elem.getElement(elem.getElementIndex(offset));
        }
        return cell;
    }

    public Element getRow(int offset) {
        Element row = null;
        Element elem = this.getDefaultRootElement();

        while (!elem.isLeaf()) {
            if (elem.getName().equals("row")) {
                row = elem;
            }
            elem = elem.getElement(elem.getElementIndex(offset));
        }
        return row;
    }

    public Element getParagraph(int offset) {
        Element paragraph = null;
        Element elem = this.getDefaultRootElement();

        while (!elem.isLeaf()) {
            if (elem.getName().equals("paragraph")) {
                paragraph = elem;
            }
            elem = elem.getElement(elem.getElementIndex(offset));
        }
        return paragraph;
    }

    public Vector getInnerTableList(int startOffset, int endOffset) {
        Vector result = new Vector();
        Element root = this.getDefaultRootElement();
        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (!elem.getName().equals("table") || (startOffset > elem.getEndOffset())
                    || (endOffset < elem.getEndOffset())) {
                continue;
            }
            result.add(elem);
        }

        return result;
    }

    public void deleteLastParagraph(CellElement cell) {
        int cnt = cell.getElementCount();
        if (cnt <= 1) {
            return;
        }
        Element par = cell.getElement(cnt - 1);
        int start = par.getStartOffset();
        int end = par.getEndOffset();
        AbstractDocument.DefaultDocumentEvent de = new AbstractDocument.DefaultDocumentEvent(start, end - start,
                DocumentEvent.EventType.REMOVE);
        AbstractDocument.ElementEdit ee = new AbstractDocument.ElementEdit(cell, cnt - 1, new Element[] { par },
                new Element[0]);
        cell.replace(cnt - 1, 1, new Element[0]);
        try {
            this.getContent().remove(start, end - start);
        } catch (Exception ex) {
            RTFDocument.logger.error(null, ex);
        }
        de.addEdit(ee);
        de.end();
        this.fireRemoveUpdate(de);
    }

    public Element getTable(int offset) {
        Element table = null;
        Element elem = this.getDefaultRootElement();
        while (!elem.isLeaf()) {
            if (elem.getName().equals("table")) {
                table = elem;
            }
            elem = elem.getElement(elem.getElementIndex(offset));
        }
        return table;
    }

    @Override
    public void setParagraphAttributes(int offset, int length, AttributeSet attrs, boolean replace) {
        try {
            this.writeLock();
            AbstractDocument.DefaultDocumentEvent changes = new AbstractDocument.DefaultDocumentEvent(offset, length,
                    DocumentEvent.EventType.CHANGE);

            AttributeSet sCopy = attrs.copyAttributes();

            int pos = offset;
            Element paragraph = this.getParagraph(pos);
            MutableAttributeSet attr = (MutableAttributeSet) paragraph.getAttributes();
            changes.addEdit(new DefaultStyledDocument.AttributeUndoableEdit(paragraph, sCopy, replace));
            if (replace) {
                attr.removeAttributes(attr);
            }
            attr.addAttributes(attrs);
            while (pos < (offset + length)) {
                attr = (MutableAttributeSet) paragraph.getAttributes();
                changes.addEdit(new DefaultStyledDocument.AttributeUndoableEdit(paragraph, sCopy, replace));
                if (replace) {
                    attr.removeAttributes(attr);
                }
                attr.addAttributes(attrs);
                if (pos == this.getLength()) {
                    break;
                }
                pos = paragraph.getEndOffset();
                paragraph = this.getParagraph(pos);
            }

            changes.end();
            this.fireChangedUpdate(changes);
            this.fireUndoableEditUpdate(new UndoableEditEvent(this, changes));
        } finally {
            this.writeUnlock();
        }
    }

    public void refresh() {
        AbstractDocument.DefaultDocumentEvent e = new AbstractDocument.DefaultDocumentEvent(0, this.getLength() - 1,
                DocumentEvent.EventType.CHANGE);
        e.end();
        this.fireChangedUpdate(e);
    }

    public class CellElement extends AbstractDocument.BranchElement {

        private int width = 1;

        private int height = 1;

        public static final int MARGIN_MIN = 2;

        private Insets m_margins = new Insets(2, 2, 2, 2);

        public CellElement(Element parent, AttributeSet attr, int startOffset, int length, int width, int height) {
            super(parent, attr);
            this.width = width;
            this.height = height;
            AbstractDocument.BranchElement paragraph = new AbstractDocument.BranchElement(this, null);

            AbstractDocument.LeafElement brk = new AbstractDocument.LeafElement(paragraph, null, startOffset,
                    startOffset + length);
            Element[] buff = new Element[1];
            buff[0] = brk;
            paragraph.replace(0, 0, buff);

            buff[0] = paragraph;
            this.replace(0, 0, buff);
        }

        public CellElement(Element parent, AttributeSet attr, int[] paragraphOffsets, int[] paragraphLenghts,
                int width) {
            super(parent, attr);
            this.width = width;
        }

        @Override
        public String getName() {
            return "cell";
        }

        public int getWidth() {
            return this.width;
        }

        public void setWidth(int w) {
            this.width = w;
        }

        public int getHeight() {
            return this.height;
        }

        public void setHeight(int h) {
            this.height = h;
        }

        public Insets getMargins() {
            return this.m_margins;
        }

        public void setMargins(Insets margins) {
            this.m_margins = margins;
        }

        public void setMargins(int top, int left, int bottom, int right) {
            this.m_margins.top = top;
            this.m_margins.left = left;
            this.m_margins.bottom = bottom;
            this.m_margins.right = right;
        }

        public void setBorders(BorderAttributes ba) {
            BorderAttributes cellBorders = (BorderAttributes) this.getAttribute("BorderAttributes");
            cellBorders.lineColor = ba.lineColor;
            cellBorders.borderTop = ba.borderTop;
            cellBorders.borderBottom = ba.borderBottom;
            cellBorders.borderLeft = ba.borderLeft;
            cellBorders.borderRight = ba.borderRight;
            AbstractDocument.DefaultDocumentEvent dde = new AbstractDocument.DefaultDocumentEvent(
                    Math.max(this.getStartOffset() - 1, 0), this.getEndOffset(),
                    DocumentEvent.EventType.CHANGE);

            dde.end();
            RTFDocument.this.fireChangedUpdate(dde);
        }

    }

    public class RowElement extends AbstractDocument.BranchElement {

        public RowElement(Element parent, AttributeSet attr, int cellCount, int[] cellOffsets, int[] cellLengths,
                int[] widths, int height) {
            super(parent, attr);

            BorderAttributes ba = (BorderAttributes) attr.getAttribute("BorderAttributes");
            Element[] cells = new Element[cellCount];
            for (int i = 0; i < cellCount; i++) {
                MutableAttributeSet cellAttr = new SimpleAttributeSet(attr);
                BorderAttributes cellBorders = new BorderAttributes();
                cellBorders.lineColor = ba.lineColor;
                cellBorders.borderTop = ba.borderTop;
                cellBorders.borderBottom = ba.borderBottom;
                if (i == 0) {
                    cellBorders.borderLeft = ba.borderLeft;
                } else {
                    cellBorders.borderLeft = ba.borderVertical;
                }

                if (i == (cellCount - 1)) {
                    cellBorders.borderRight = ba.borderRight;
                }
                cellAttr.addAttribute("BorderAttributes", cellBorders);
                cells[i] = new RTFDocument.CellElement(this, cellAttr, cellOffsets[i], cellLengths[i], widths[i],
                        height);
            }
            this.replace(0, 0, cells);
        }

        @Override
        public String getName() {
            return "row";
        }

        @Override
        public boolean isLeaf() {
            return false;
        }

        public int getWidth() {
            int width = 0;
            for (int i = 0; i < this.getElementCount(); i++) {
                RTFDocument.CellElement cell = (RTFDocument.CellElement) this.getElement(i);
                width += cell.getWidth();
            }
            return width;
        }

        public int getHeight() {
            int height = 0;
            for (int i = 0; i < this.getElementCount(); i++) {
                RTFDocument.CellElement cell = (RTFDocument.CellElement) this.getElement(i);
                height = Math.max(cell.getHeight(), height);
            }
            return height;
        }

        public int getCellWidth(int index) {
            RTFDocument.CellElement cell = (RTFDocument.CellElement) this.getElement(index);
            return cell.getWidth();
        }

        public void setBorders(BorderAttributes ba) {
            BorderAttributes currentBorders = (BorderAttributes) this.getAttribute("BorderAttributes");
            currentBorders.setBorders(ba.getBorders());
            currentBorders.lineColor = ba.lineColor;

            for (int i = 0; i < this.getElementCount(); i++) {
                RTFDocument.CellElement cell = (RTFDocument.CellElement) this.getElement(i);
                BorderAttributes cellBorders = new BorderAttributes();
                cellBorders.lineColor = ba.lineColor;
                cellBorders.borderTop = ba.borderTop;
                cellBorders.borderBottom = ba.borderBottom;
                if (i == 0) {
                    cellBorders.borderLeft = ba.borderLeft;
                } else {
                    cellBorders.borderLeft = ba.borderVertical;
                }

                if (i == (this.getElementCount() - 1)) {
                    cellBorders.borderRight = ba.borderRight;
                }
                cell.setBorders(cellBorders);
            }
        }

        public void setMargins(Insets margins) {
            RTFDocument.this.writeLock();
            int cnt = this.getElementCount();
            for (int i = 0; i < cnt; i++) {
                RTFDocument.CellElement cell = (RTFDocument.CellElement) this.getElement(i);
                cell.setMargins(margins);
            }
            RTFDocument.this.writeUnlock();
        }

        public void setHeight(int height) {
            RTFDocument.this.writeLock();
            int cnt = this.getElementCount();
            for (int i = 0; i < cnt; i++) {
                RTFDocument.CellElement cell = (RTFDocument.CellElement) this.getElement(i);
                // ExtendedStyledDocument.CellElement.access$902(cell, height);
            }
            RTFDocument.this.writeUnlock();
        }

    }

    public class TableElement extends AbstractDocument.BranchElement {

        public TableElement(int[] rowOffsets, int[] rowLengths, Element parent, AttributeSet attr, int rowCount,
                int colCount, int[] widths, int[] heights) {
            super(parent, attr);
            BorderAttributes ba = (BorderAttributes) attr.getAttribute("BorderAttributes");
            if (ba == null) {
                ba = new BorderAttributes();
                ba.setBorders(63);
            }
            int tablelength = 0;
            Element[] rows = new Element[rowCount];
            for (int i = 0; i < rowCount; i++) {
                MutableAttributeSet rowAttr = new SimpleAttributeSet(attr);
                BorderAttributes rowBorders = new BorderAttributes();
                rowBorders.lineColor = ba.lineColor;
                rowBorders.borderLeft = ba.borderLeft;
                rowBorders.borderRight = ba.borderRight;
                rowBorders.borderVertical = ba.borderVertical;
                if (i == 0) {
                    rowBorders.borderTop = ba.borderTop;
                } else {
                    rowBorders.borderTop = ba.borderHorizontal;
                }

                if (i == (rowCount - 1)) {
                    rowBorders.borderBottom = ba.borderBottom;
                }

                rowAttr.addAttribute("BorderAttributes", rowBorders);
                int[] cellOffsets = new int[colCount];
                int[] cellLengths = new int[colCount];
                for (int j = 0; j < colCount; j++) {
                    cellOffsets[j] = rowOffsets[i] + j;
                    cellLengths[j] = 1;
                }
                rows[i] = new RTFDocument.RowElement(this, rowAttr, colCount, cellOffsets, cellLengths, widths,
                        heights[i]);
            }
            this.replace(0, 0, rows);
        }

        @Override
        public String getName() {
            return "table";
        }

        public int getWidth() {
            RTFDocument.RowElement row = (RTFDocument.RowElement) this.getElement(1);
            return row.getWidth();
        }

        public int getHeight() {
            int cnt = this.getElementCount();
            int height = 1;
            for (int i = 0; i < cnt; i++) {
                RTFDocument.RowElement row = (RTFDocument.RowElement) this.getElement(i);
                height += row.getHeight();
            }
            return height;
        }

        @Override
        public boolean isLeaf() {
            return false;
        }

        public void setBorders(BorderAttributes ba) {
            RTFDocument.this.writeLock();
            this.addAttribute("BorderAttributes", ba);
            for (int i = 0; i < this.getElementCount(); i++) {
                RTFDocument.RowElement row = (RTFDocument.RowElement) this.getElement(i);
                BorderAttributes rowBorders = (BorderAttributes) row.getAttribute("BorderAttributes");
                rowBorders.lineColor = ba.lineColor;
                rowBorders.borderLeft = ba.borderLeft;
                rowBorders.borderRight = ba.borderRight;
                rowBorders.borderVertical = ba.borderVertical;
                if (i == 0) {
                    rowBorders.borderTop = ba.borderTop;
                } else {
                    rowBorders.borderTop = ba.borderHorizontal;
                }

                if (i == (this.getElementCount() - 1)) {
                    rowBorders.borderBottom = ba.borderBottom;
                }
                row.setBorders(rowBorders);
            }
            RTFDocument.this.writeUnlock();
        }

        public void setMargins(Insets margins) {
            RTFDocument.this.writeLock();
            int cnt = this.getElementCount();
            for (int i = 0; i < cnt; i++) {
                RTFDocument.RowElement row = (RTFDocument.RowElement) this.getElement(i);
                int cnt2 = row.getElementCount();
                for (int j = 0; j < cnt2; j++) {
                    RTFDocument.CellElement cell = (RTFDocument.CellElement) row.getElement(j);
                    cell.setMargins(margins);
                }
            }
            RTFDocument.this.writeUnlock();
        }

        public void setAlignment(int align) {
            RTFDocument.this.writeLock();
            StyleConstants.setAlignment((MutableAttributeSet) this.getAttributes(), align);
            RTFDocument.this.writeUnlock();
        }

    }

}
