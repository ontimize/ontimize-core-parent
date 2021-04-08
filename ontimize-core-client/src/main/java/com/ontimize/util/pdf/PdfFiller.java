package com.ontimize.util.pdf;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.table.TableColumnModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.BarcodePDF417;
import com.lowagie.text.pdf.PRAcroForm;
import com.lowagie.text.pdf.PRIndirectReference;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfLister;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfNumber;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.text.pdf.PdfWriter;
import com.ontimize.gui.table.Table;
import com.ontimize.util.remote.BytesBlock;

/**
 * Class that contains static methods to create PDF files filling fields for other PDF used like
 * template. Steps to create a template are:<br>
 * <ul>
 * <li>Creates a document with a text editor (Word or similar) with a fixed text.
 * <li>Creates a pdf from this document.
 * <li>Opens PDF with default PDF reader and inserts fields.
 * </ul>
 * Text to insert will have characteristics of field where is inserted. Images to insert will be
 * adjusted automatically keeping original aspect relation.
 *
 * @author Imatia Innovation
 */
public abstract class PdfFiller {

    private static final Logger logger = LoggerFactory.getLogger(PdfFiller.class);

    public static boolean DEBUG = true;

    public static class FieldProp {

        /**
         * Page number when field is inserted
         */
        public int page;

        /**
         * Field name
         */
        protected String name = null;

        /**
         * X1 coordinate
         */
        public float x1;

        /**
         * Y1 coordinate
         */
        public float y1;

        /**
         * X2 coordinate
         */
        public float x2;

        /**
         * Y2 coordinate
         */
        public float y2;

        /**
         * Class constructor.
         * @param name field name
         * @param pag page number
         * @param x_1 x1 coordinate
         * @param y_1 y1 coordinate
         * @param x_2 x2 coordinate
         * @param y_2 y2 coordinate
         */
        public FieldProp(String name, int pag, float x_1, float y_1, float x_2, float y_2) {
            this(name);
            this.page = pag;
            this.x1 = x_1;
            this.y1 = y_1;
            this.x2 = x_2;
            this.y2 = y_2;
        }

        public FieldProp(String name) {
            this.page = 0;
            this.name = name;
            this.x1 = 0;
            this.y1 = 0;
            this.x2 = 0;
            this.y2 = 0;
        }

        /**
         * Difference between y2 and y1.
         * @return height of field
         */
        public float height() {
            return this.y2 - this.y1;
        }

        /**
         * Difference between x2 and x1.
         * @return width of field
         */
        public float width() {
            return this.x2 - this.x1;
        }

        @Override
        public String toString() {
            return this.name;
        }

    }

    /**
     * This function returns a <code>Vector</code> with properties of fields.
     * @param pdfInputStream InputStream that contains PDF
     * @return the <code>Vector</code> with properties
     * @throws Exception when an <code>Exception</code> occurs
     */
    public static Vector getFieldProps(InputStream pdfInputStream) throws Exception {
        Vector res = new Vector();

        try {
            byte[] buffer = new byte[pdfInputStream.available()];
            pdfInputStream.read(buffer);
            PdfFiller.logger.debug("getFieldProps before instanciating pdfReader");
            PdfReader pdfReader = new PdfReader(buffer);
            PdfFiller.logger.debug("getFieldProps after instanciating pdfReader");
            PRAcroForm pdfForm = pdfReader.getAcroForm();
            if (pdfForm == null) {
                PdfFiller.logger.debug("Document has not pages");
                return res;
            }

            PdfLister list = new PdfLister(System.out);
            HashMap retToField = new HashMap();
            ArrayList fields = pdfForm.getFields();
            for (int k = 0; k < fields.size(); ++k) {
                PRAcroForm.FieldInformation field = (PRAcroForm.FieldInformation) fields.get(k);
                retToField.put(new Integer(field.getRef().getNumber()), field);
            }
            for (int page = 1; page <= pdfReader.getNumberOfPages(); ++page) {
                PdfDictionary dPage = pdfReader.getPageN(page);
                PdfArray annots = (PdfArray) PdfReader.getPdfObject(dPage.get(PdfName.ANNOTS));
                if (annots == null) {
                    continue;
                }
                ArrayList ali = annots.getArrayList();
                for (int annot = 0; annot < ali.size(); ++annot) {
                    PdfObject refObj = (PdfObject) ali.get(annot);
                    PRIndirectReference ref = null;
                    PdfDictionary an = (PdfDictionary) PdfReader.getPdfObject(refObj);
                    PdfName name = (PdfName) an.get(PdfName.SUBTYPE);
                    if ((name == null) || !name.equals(PdfName.WIDGET)) {
                        continue;
                    }
                    PdfArray rect = (PdfArray) PdfReader.getPdfObject(an.get(PdfName.RECT));
                    String fName = "";
                    PRAcroForm.FieldInformation field = null;
                    while (an != null) {
                        PdfString tName = (PdfString) an.get(PdfName.T);
                        if (tName != null) {
                            fName = tName.toString() + "." + fName;
                        }
                        if ((refObj.type() == PdfObject.INDIRECT) && (field == null)) {
                            ref = (PRIndirectReference) refObj;
                            field = (PRAcroForm.FieldInformation) retToField.get(new Integer(ref.getNumber()));
                        }
                        refObj = an.get(PdfName.PARENT);
                        an = (PdfDictionary) PdfReader.getPdfObject(refObj);
                    }
                    if (fName.endsWith(".")) {
                        fName = fName.substring(0, fName.length() - 1);
                    }

                    FieldProp fieldProp = new FieldProp(fName);
                    fieldProp.page = page;
                    ArrayList arr = rect.getArrayList();
                    PdfNumber num = (PdfNumber) PdfReader.getPdfObject((PdfObject) arr.get(0));
                    fieldProp.x1 = num.floatValue();
                    num = (PdfNumber) PdfReader.getPdfObject((PdfObject) arr.get(1));
                    fieldProp.y1 = num.floatValue();
                    num = (PdfNumber) PdfReader.getPdfObject((PdfObject) arr.get(2));
                    fieldProp.x2 = num.floatValue();
                    num = (PdfNumber) PdfReader.getPdfObject((PdfObject) arr.get(3));
                    fieldProp.y2 = num.floatValue();
                    res.add(fieldProp);
                }
            }

            return res;
        } catch (Exception ex) {
            PdfFiller.logger.error(null, ex);
            throw ex;
        }
    }

    /**
     * Gets properties of fields for a input file parameter.
     * @param pdfFile the name of file
     * @return vector with properties
     * @throws Exception when an <code>Exception</code> occurs
     */
    public static Vector getFieldProps(String pdfFile) throws Exception {
        try {
            FileInputStream pdfFileStream = new FileInputStream(pdfFile);
            Vector res = PdfFiller.getFieldProps(pdfFileStream);
            pdfFileStream.close();
            return res;

        } catch (Exception ex) {
            PdfFiller.logger.error(null, ex);
            throw ex;
        }
    }

    /**
     * Fills PDF fields that parameters included in <code>params</code>.
     * @param pdfInputStream input document stream
     * @param pdfOutputStream output document stream
     * @param params fields and values to fill
     * @param flatFields condition to delete fields
     * @throws Exception when an <code>Exception</code> occurs
     */
    public static void fillFields(InputStream pdfInputStream, OutputStream pdfOutputStream, Hashtable params,
            boolean flatFields) throws Exception {
        try {
            ByteArrayOutputStream baOut = new ByteArrayOutputStream();
            BufferedInputStream bInput = new BufferedInputStream(pdfInputStream);
            int a = 0;
            while ((a = bInput.read()) != -1) {
                baOut.write(a);
            }
            byte[] buffer = baOut.toByteArray();

            PdfReader reader = new PdfReader(buffer);
            PdfStamper stamp = new PdfStamper(reader, pdfOutputStream);
            AcroFields form = stamp.getAcroFields();
            Enumeration e_key = params.keys();
            while (e_key.hasMoreElements()) {
                String field = e_key.nextElement().toString();
                Object value = params.get(field);
                if (value != null) {
                    form.setField(field, value.toString());
                    if (PdfFiller.DEBUG) {
                        PdfFiller.logger.debug("Filled field " + field + " with value: " + value);
                    }
                } else {
                    if (PdfFiller.DEBUG) {
                        PdfFiller.logger
                            .debug("It has not been filled field: " + field + " because value has not received");
                    }
                }
            }

            if (PdfFiller.DEBUG) {
                PdfFiller.logger.debug("-----------------pdf fields(start)------------------------------------");
                HashMap map = form.getFields();
                Set keySet = map.keySet();
                Iterator it = keySet.iterator();
                while (it.hasNext()) {
                    Object oKey = it.next();
                    Object oValue = map.get(oKey);
                    PdfFiller.logger.debug(" " + oKey + " ---- > " + oValue);
                }
                PdfFiller.logger.debug("-----------------pdf fields(end)------------------------------------");

                PdfFiller.logger.debug(" Received values: " + params);
            }

            stamp.setFormFlattening(flatFields);
            stamp.close();
            PdfFiller.logger.debug("Filled PDF");
        } catch (IOException ex) {
            PdfFiller.logger.error(null, ex);
            throw ex;
        } catch (Exception e) {
            PdfFiller.logger.error(null, e);
        }
    }

    /**
     * Fills PDF fields that parameters included in <code>params</code>.
     * @param pdfFile name of input pdf
     * @param pdfOutputStream output document stream
     * @param params fields and values to fill
     * @param flatFields condition to delete fields
     * @throws Exception when an <code>Exception</code> occurs
     */
    public static void fillFields(String pdfFile, OutputStream pdfOutputStream, Hashtable params, boolean flatFields)
            throws Exception {
        FileInputStream pdfFileStream = null;
        try {
            pdfFileStream = new FileInputStream(pdfFile);
            PdfFiller.fillFields(pdfFileStream, pdfOutputStream, params, flatFields);
        } catch (Exception ex) {
            PdfFiller.logger.error(null, ex);
            throw ex;
        } finally {
            try {
                if (pdfFileStream != null) {
                    pdfFileStream.close();
                }
            } catch (Exception ex) {
                PdfFiller.logger.error(null, ex);
            }
        }
    }

    /**
     * Fills PDF fields that parameters included in <code>params</code>.
     * @param pdfFile name of input pdf
     * @param pdfFileOut name of output pdf
     * @param params fields and values to fill
     * @param flatFields condition to delete fields
     * @throws Exception when an <code>Exception</code> occurs
     */
    public static void fillFields(String pdfFile, String pdfFileOut, Hashtable params, boolean flatFields)
            throws Exception {
        FileOutputStream pdfOutputStream = null;
        try {
            pdfOutputStream = new FileOutputStream(pdfFileOut, false);
            PdfFiller.fillFields(pdfFile, pdfOutputStream, params, flatFields);
        } catch (Exception ex) {
            PdfFiller.logger.error(null, ex);
            throw ex;
        } finally {
            try {
                if (pdfOutputStream != null) {
                    pdfOutputStream.flush();
                    pdfOutputStream.close();
                }
            } catch (Exception ex) {
                PdfFiller.logger.error(null, ex);
            }
        }
    }

    /**
     * Inserts images from <code>params</code> like byte[] or {@link BytesBlock} in
     * <code>pdfInputStream</code>. Output is set in <code>pdfOutputStream</code>. Images are inserted
     * in (x,y) coordinates correspondent of original field documents that have the same name that
     * <code>params</code> keys.
     * @param pdfInputStream Input PDF used like template
     * @param pdfOutputStream Output PDF file
     * @param params Images to insert
     * @param flatFields This param indicates whether original PDF fields are kept
     * @throws Exception When <code>Exception</code> occurs
     */
    public static void fillImageFields(InputStream pdfInputStream, OutputStream pdfOutputStream, Hashtable params,
            boolean flatFields) throws Exception {
        try {
            ByteArrayOutputStream baOut = new ByteArrayOutputStream();
            BufferedInputStream bInput = new BufferedInputStream(pdfInputStream);
            int a = 0;
            while ((a = bInput.read()) != -1) {
                baOut.write(a);
            }
            byte[] buffer = baOut.toByteArray();
            baOut.close();

            PdfReader reader = new PdfReader(buffer);
            reader.getPageSize(1);

            PdfStamper writer = new PdfStamper(reader, pdfOutputStream);

            PdfContentByte cb = null;

            // Create other InputStream.
            ByteArrayInputStream pdfBytesArrayInputStream = new ByteArrayInputStream(buffer);
            Vector fieldProps = PdfFiller.getFieldProps(pdfBytesArrayInputStream);
            if (PdfFiller.DEBUG) {
                PdfFiller.logger.debug("Fields " + fieldProps);
            }
            for (int pageNumber = 1; pageNumber <= reader.getNumberOfPages(); pageNumber++) {
                // Original images will be overwritten
                cb = writer.getOverContent(pageNumber);

                // We insert images in (x1,y1) keeping the "aspect ratio".

                Enumeration enumKeys = Collections.enumeration(fieldProps);
                while (enumKeys.hasMoreElements()) {
                    Object oKey = enumKeys.nextElement();
                    FieldProp field = (FieldProp) oKey;
                    if (field.page == pageNumber) {
                        // Insert the image in field position if any key in the
                        // hashtable of parameters
                        // matches
                        if (params.containsKey(field.name)) {
                            Object value = params.get(field.name);

                            Image img = null;
                            if (value instanceof InputStream) {
                            }
                            if (value instanceof byte[]) {
                                img = Image.getInstance((byte[]) value);
                            }
                            if (value instanceof java.awt.Image) {
                                img = Image.getInstance((java.awt.Image) value, null);
                            }
                            if (img != null) {
                                float realimagewidth = img.width();
                                float realimageheight = img.height();
                                if ((img.width() > field.width()) || (img.height() > field.height())) {
                                    float scaleX = field.width() / img.width();
                                    float scaleY = field.height() / img.height();
                                    float scale = Math.min(scaleX, scaleY);
                                    float newwidth = img.width() * scale;
                                    float newheight = img.height() * scale;
                                    img.scaleToFit(newwidth, newheight);
                                    realimagewidth = newwidth;
                                    realimageheight = newheight;
                                    PdfFiller.logger.debug("Original size for image: " + img.width() + ", "
                                            + img.height() + "Scaled to: " + newwidth + " , " + newheight);
                                } else {
                                    PdfFiller.logger.debug("It is not necessary resize the image: " + img.width() + ", "
                                            + img.height() + " --> Field size: " + field
                                                .width()
                                            + " , " + field.height());
                                }
                                img.setAbsolutePosition(field.x1 + ((field.width() - realimagewidth) / 2),
                                        field.y1 + ((field.height() - realimageheight) / 2));
                                cb.addImage(img);
                            }
                        }
                    }
                }
            }
            PRAcroForm form = reader.getAcroForm();
            writer.setFormFlattening(flatFields);
            writer.close();
        } catch (Exception ex) {
            PdfFiller.logger.error(null, ex);
            throw ex;
        }
    }

    /**
     * Inserts images from <code>params</code> like byte[] or {@link BytesBlock} in
     * <code>FileInputStream</code> created with <code>pdfFile</code>. Output is set in
     * <code>pdfOutputStream</code>. Images are inserted in (x,y) coordinates correspondent of original
     * field documents that have the same name that <code>params</code> keys.
     * @param pdfFile name of input PDF used like template
     * @param pdfOutputStream Output PDF
     * @param params Images to insert
     * @param flatFields This param indicates whether original PDF fields are kept
     * @throws Exception When <code>Exception</code> occurs
     */
    public static void fillImageFields(String pdfFile, OutputStream pdfOutputStream, Hashtable params,
            boolean flatFields) throws Exception {
        FileInputStream pdfFileStream = null;
        try {
            pdfFileStream = new FileInputStream(pdfFile);
            PdfFiller.fillImageFields(pdfFileStream, pdfOutputStream, params, flatFields);
        } catch (Exception ex) {
            PdfFiller.logger.error(null, ex);
            throw ex;
        } finally {
            try {
                if (pdfFileStream != null) {
                    pdfFileStream.close();
                }
            } catch (Exception ex) {
                PdfFiller.logger.error(null, ex);
            }
        }
    }

    /**
     * Inserts images from <code>params</code> like byte[] or {@link BytesBlock} in
     * <code>FileInputStream</code> created with <code>pdfFile</code>. Output is set in
     * <code>FileOutputStream</code> created with <code>pdfFileOut</code>. Images are inserted in (x,y)
     * coordinates correspondent of original field documents that have the same name that
     * <code>params</code> keys.
     * @param pdfFile name of input PDF used like template
     * @param pdfFileOut name of output PDF file
     * @param params Images to insert
     * @param flatFields This param indicates whether original PDF fields are kept
     * @throws Exception When <code>Exception</code> occurs
     */
    public static void fillImageFields(String pdfFile, String pdfFileOut, Hashtable params, boolean flatFields)
            throws Exception {
        FileOutputStream pdfFileOutputStream = null;
        try {
            pdfFileOutputStream = new FileOutputStream(pdfFileOut, false);
            PdfFiller.fillImageFields(pdfFile, pdfFileOutputStream, params, flatFields);
        } catch (Exception ex) {
            PdfFiller.logger.error(null, ex);
            throw ex;
        } finally {
            try {
                pdfFileOutputStream.flush();
                pdfFileOutputStream.close();
            } catch (Exception ex) {
                PdfFiller.logger.error(null, ex);
            }
        }
    }

    /**
     * Fills the fields in PDF file with:
     * <ul>
     * <li>Images contained in imagesFields that are in params.
     * <li>Text in all other fields.
     * </ul>
     * @param pdfInputStream Input PDF used like template
     * @param pdfOutputStream Output PDF file
     * @param params Data to insert
     * @param imagesFields The vector with name of image fields
     * @param flatFields This parameter indicates whether original PDF fields are kept
     * @throws Exception When <code>Exception</code> occurs
     */
    public static void fillTextImageFields(InputStream pdfInputStream, OutputStream pdfOutputStream, Hashtable params,
            Vector imagesFields, boolean flatFields) throws Exception {
        Hashtable htextfields = new Hashtable();
        Hashtable himagefields = new Hashtable();
        Enumeration enumKeys;
        try {
            enumKeys = params.keys();
            while (enumKeys.hasMoreElements()) {
                String sKey = enumKeys.nextElement().toString();
                if (imagesFields.contains(sKey)) {
                    himagefields.put(sKey, params.get(sKey));
                } else {
                    htextfields.put(sKey, params.get(sKey));
                }
            }
            // Auxiliary stream
            ByteArrayOutputStream inter = new ByteArrayOutputStream();
            // Fill text fields
            PdfFiller.fillFields(pdfInputStream, inter, htextfields, false);
            // Fill with images considering flatFields
            PdfFiller.fillImageFields(new ByteArrayInputStream(inter.toByteArray()), pdfOutputStream, himagefields,
                    flatFields);
            inter.close();
        } catch (IOException ex) {
            PdfFiller.logger.error(null, ex);
            throw ex;
        }
    }

    /**
     * Fills the fields in PDF file with:
     * <ul>
     * <li>Images contained in imagesFields that are in params.
     * <li>Text in all other fields.
     * </ul>
     * @param pdfFile PDF file name
     * @param pdfOutputStream Output PDF file
     * @param params Data to insert
     * @param imagesFields The vector with name of image fields
     * @param flatFields This parameter indicates whether original PDF fields are kept
     * @throws Exception When <code>Exception</code> occurs
     */
    public static void fillTextImageFields(String pdfFile, OutputStream pdfOutputStream, Hashtable params,
            Vector imagesFields, boolean flatFields) throws Exception {
        FileInputStream pdfFileStream = null;
        try {
            pdfFileStream = new FileInputStream(pdfFile);
            PdfFiller.fillTextImageFields(pdfFileStream, pdfOutputStream, params, imagesFields, flatFields);
        } catch (Exception ex) {
            PdfFiller.logger.error(null, ex);
            throw ex;
        } finally {
            try {
                pdfFileStream.close();
            } catch (Exception ex) {
                PdfFiller.logger.error(null, ex);
            }
        }
    }

    /**
     * Fills the fields in PDF file with:
     * <ul>
     * <li>Images contained in imagesFields that are in params.
     * <li>Text in all other fields.
     * </ul>
     * @param pdfFile PDF file name
     * @param pdfFileOut Output PDF file name
     * @param params Data to insert
     * @param imagesFields The vector with name of image fields
     * @param flatFields This parameter indicates whether original PDF fields are kept
     * @throws Exception When <code>Exception</code> occurs
     */
    public static void fillTextImageFields(String pdfFile, String pdfFileOut, Hashtable params, Vector imagesFields,
            boolean flatFields) throws Exception {
        FileOutputStream pdfOutputStream = null;
        try {
            pdfOutputStream = new FileOutputStream(pdfFileOut, false);
            PdfFiller.fillTextImageFields(pdfFile, pdfOutputStream, params, imagesFields, flatFields);
        } catch (Exception ex) {
            PdfFiller.logger.error(null, ex);
            throw ex;
        } finally {
            try {
                pdfOutputStream.flush();
                pdfOutputStream.close();
            } catch (Exception ex) {
                PdfFiller.logger.error(null, ex);
            }
        }
    }

    /**
     * Returns the specific bar code for 417PDF specified in text.
     * @param text the text to set to bar code
     * @return the bar code wit text set
     */
    public static BarcodePDF417 getPDF417(String text) {
        BarcodePDF417 codePDF417 = new BarcodePDF417();
        codePDF417.setText(text);
        codePDF417.createAwtImage(java.awt.Color.black, java.awt.Color.white);
        return codePDF417;
    }

    /**
     * Fills the table with data specified in <code>pdfInputStream</code>.
     * @param pdfInputStream the input data to fill the table
     * @param table table to fill
     * @throws Exception when <code>Exception</code> occurs
     */
    public static void fillTable(OutputStream pdfInputStream, Table table) throws Exception {

        FileOutputStream pdfOutputStream = null;
        Document document = new Document();
        try {
            // Get an instance
            PdfWriter.getInstance(document, pdfInputStream);
            document.open();

            Vector v = table.getVisibleColumns();
            int numCols = v.size();
            PdfPTable datatable = new PdfPTable(numCols);

            int totalWidth = 0;
            int headerwidths[] = new int[numCols];
            String nameColumn[] = new String[numCols];
            int indColumn[] = new int[numCols];

            TableColumnModel mColumn = table.getJTable().getColumnModel();
            for (int i = 0; i < v.size(); i++) {
                nameColumn[i] = (String) v.get(i);
                int indView = table.getColumnIndex(nameColumn[i]);
                int indModel = table.getJTable().convertColumnIndexToModel(indView);
                indColumn[i] = indModel;
                int widthC = mColumn.getColumn(indModel).getWidth();
                headerwidths[i] = widthC * 100;
                totalWidth += widthC;
            }

            for (int i = 0; i < v.size(); i++) {
                headerwidths[i] = headerwidths[i] / totalWidth;
            }

            datatable.setWidths(headerwidths);
            datatable.setWidthPercentage(100); // percentage
            datatable.getDefaultCell().setPadding(3);
            datatable.getDefaultCell().setBorderWidth(2);
            datatable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            for (int j = 0; j < nameColumn.length; j++) {
                datatable.addCell(nameColumn[j]);
            }

            datatable.setHeaderRows(1); // this is the end of the table header

            datatable.getDefaultCell().setBorderWidth(1);
            for (int i = 1; i < table.getJTable().getModel().getRowCount(); i++) {
                if ((i % 2) == 1) {
                    datatable.getDefaultCell().setGrayFill(0.9f);
                }
                for (int x = 0; x < numCols; x++) {
                    datatable.addCell(table.getJTable().getModel().getValueAt(i, indColumn[x]) == null ? ""
                            : table.getJTable().getModel().getValueAt(i, indColumn[x]).toString());
                }
                if ((i % 2) == 1) {
                    datatable.getDefaultCell().setGrayFill(0.0f);
                }
            }
            document.add(datatable);

        } catch (Exception e) {
            PdfFiller.logger.error(null, e);
        }
        document.close();
    }

}
