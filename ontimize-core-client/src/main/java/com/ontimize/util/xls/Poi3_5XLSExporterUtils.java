package com.ontimize.util.xls;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.field.document.CurrencyDocument;
import com.ontimize.gui.table.CellRenderer;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.windows.office.WindowsUtils;

/**
 * Factory to export to xls and xlsx using poi library (version 3.5, 3.6, 3.7, 3.8, 3.9). <br>
 * <br>
 * <b>For exporting to xls</b> only requires:
 * <ul>
 * <li>poi3.xfinal.jar</li>
 * <li>poi-ooxml-3.x-FINAL</li>
 * <li>commons-logging 1.1</li>
 * <li>log4j 1.2.13</li>
 * </ul>
 * (x>=5, with the same x version for both (poi and ooxml) libraries) <br>
 * <br>
 * <b>For exporting to xls and xlsx</b> requires:
 * <ul>
 * <li>poi3.xfinal.jar</li>
 * <li>poi-ooxml-3.x-FINAL</li>
 * <li>commons-logging 1.1</li>
 * <li>log4j 1.2.13</li>
 * <li><i>xmlbeans-2.3.0</i></li>
 * <li><i>dom4j-1.6.1</i></li>
 * <li><i>ooxml-schemas-1.0</i></li>
 * </ul>
 * (x>=5, with the same x version for both (poi and ooxml) libraries)<br>
 * <br>
 *
 * @author Imatia Innovation SL
 * @since 5.2062EN
 */
public class Poi3_5XLSExporterUtils extends AbstractXLSExporter implements XLSExporter {

    private static final Logger logger = LoggerFactory.getLogger(Poi3_5XLSExporterUtils.class);

    public DecimalFormatSymbols dfs = new DecimalFormatSymbols();

    public String numericPattern = "#,##0";

    public String decimalPattern = "#,#0.#";

    protected Charset utf8charset = Charset.forName("UTF-8");

    public DecimalFormat numericFormat = new DecimalFormat();

    public DecimalFormat decimalFormat = new DecimalFormat();

    public String dateFormat = "dd/MM/yyyy";

    public SimpleDateFormat sdf = new SimpleDateFormat(this.dateFormat);

    public String dateHourFormat = "dd/MM/yyyy HH:mm";

    public SimpleDateFormat sdfHour = new SimpleDateFormat(this.dateHourFormat);

    public CellStyle cs_style = null;

    public CellStyle cs_date_hour = null;

    public CellStyle cs_date = null;

    public CellStyle cs_currency = null;

    public CellStyle cs_percent = null;

    public CellStyle cs_real = null;

    protected CreationHelper helper;

    protected Drawing drawing;

    public final int MAX_ROWS_XLSX = 1000000;

    public final int MAX_ROWS_XLS = 65000;

    /**
     * Constructor of this class
     */
    public Poi3_5XLSExporterUtils() {
        // Class constructor empty.
    }

    /**
     * Returns the localized decimal pattern according to the supplied decimal format
     * @param decimalSymbols
     * @return An {@code String} that returns the localized decimal pattern according to the supplied
     *         decimal format
     */
    public String getDecimalPattern(DecimalFormatSymbols decimalSymbols) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("#").append(decimalSymbols.getGroupingSeparator());
        buffer.append("##0").append(decimalSymbols.getDecimalSeparator()).append("#");
        return buffer.toString();
    }

    /**
     * Returns the localized numeric pattern according to the supplied numeric format
     * @param decimalSymbols
     * @return An {@code String} that returns the localized numeric pattern according to the supplied
     *         numeric format
     */
    public String getNumericPattern(DecimalFormatSymbols decimalSymbols) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("#").append(decimalSymbols.getGroupingSeparator());
        buffer.append("##0");
        return buffer.toString();
    }

    /**
     * This method creates an XLS from the input data supplied in the {@link EntityResult rs} and open
     * it if the param {@code openFile} is true.
     * @param rs {@link to EntityResult} with the data to export from the table.
     * @param output {@link File} indicating the path where the file will be saved.
     * @param sheetName {@link String} The page name of the Excel file.
     * @param hColumnRenderers {@link CellRenderer} Rendering of table columns.
     * @param columnSort {@link List} List of column order.
     * @param writeHeader {@link Boolean} Boolean indicating whether the column headers are to be
     *        written.
     * @param xlsx {@link Boolean} Boolean indicating whether the file has a *. xslx extension
     * @param openFile {@link Boolean} Boolean indicating whether the generated file should be opened at
     *        the end of the export
     * @throws Exception
     *
     * @see {@link #createXLS(EntityResult, OutputStream, String, Hashtable, List, boolean, boolean)}
     *
     */
    @Override
    public void createXLS(EntityResult rs, File output, String sheetName, Map<?,?> hColumnRenderers, List<?> columnSort,
            boolean writeHeader, boolean xlsx, boolean openFile)
            throws Exception {
        OutputStream os = new FileOutputStream(output);
        try {
            this.createXLS(rs, os, sheetName, hColumnRenderers, columnSort, writeHeader, xlsx);
            if (openFile) {
                WindowsUtils.openFile(output);
            }
        } catch (Exception e) {
            Poi3_5XLSExporterUtils.logger.error(null, e);
        } finally {
            os.close();
        }
    }

    /**
     * Create an *xlsx or *xls file containing multiple sheets. Export one {@link EntityResult} per
     * sheet in order. If the parameter {@code xlsx} is {@code true} and
     * {@link XLSExporterFactory#isAvailableXLSX()} is {@code true}, use an {@link SXSSFWorkbook} as
     * {@link Workbook}, otherwise, uses an {@link HSSFWorkbook}
     * @param entityResultsList {@link List} of {@link EntityResult} to export (1 per sheet)
     * @param os {@link OutputStream} The output stream to save the document
     * @param sheetNames {@link List} List of sheet names
     * @param hColumnRenderers {@link Hashtable} Hashtable with the column renderers
     * @param columnSort {@link List} List of ordered columns
     * @param writeHeader {@link Boolean} Indicates whether the column header is written or not.
     * @param xlsx {@link Boolean} Boolean indicating whether the extension *.xlsx or not.
     * @throws IOException
     */
    public void createXLS(List<?> entityResultsList, OutputStream os, List<?> sheetNames, Map<?,?> hColumnRenderers,
            List<?> columnSort, boolean writeHeader, boolean xlsx)
            throws IOException {
        Workbook wb = null;
        if (xlsx && XLSExporterFactory.isAvailableXLSX()) {
            wb = new org.apache.poi.xssf.streaming.SXSSFWorkbook();
        } else {
            wb = new org.apache.poi.hssf.usermodel.HSSFWorkbook();
        }
        sheetNames = this.checkSheetNames(entityResultsList.size(), sheetNames);
        for (int i = 0; i < entityResultsList.size(); i++) {
            this.writeSheet(wb, (EntityResult) entityResultsList.get(i), (List) columnSort.get(i), null, null,
                    (String) sheetNames.get(i), hColumnRenderers, writeHeader);
        }
        wb.write(os);
    }

    /**
     * Calculates row height according to presence of images.
     * @param sqlColumnTypes Sql types
     * @return row height
     * @since 5.3.8
     *
     */
    protected float calculateRowHeight(Map<?, ?> sqlColumnTypes) {
        if ((sqlColumnTypes != null) && !sqlColumnTypes.isEmpty()) {
            Collection<?> collectionTypes = sqlColumnTypes.values();
            for (Object data : collectionTypes) {
                if (Types.BINARY == ((Integer) data).intValue()) {
                    return 50;
                }
            }
        }
        return 12.75f;
    }

    /**
     * Creates a *.xls file containing multiple sheets. Export one {@link EntityResult} per sheet in
     * order
     * @param entityResultsList {@link List} of {@link EntityResult} to export (1 per sheet)
     * @param os {@link OutputStream} The output stream to save the document
     * @param sheetNames {@link List} List of sheet names
     * @param hColumnRenderers {@link Hashtable} Hashtable with the column renderers
     * @param columnSort {@link List} List of ordered columns
     * @param writeHeader {@link Boolean} Indicates whether the column header is written or not.
     * @throws IOException
     */
    public void createXLS(List<?> entityResultsList, OutputStream os, List<?> sheetNames, Map<?,?> hColumnRenderers,
            List<?> columnSort, boolean writeHeader) throws IOException {
        org.apache.poi.hssf.usermodel.HSSFWorkbook wb = new org.apache.poi.hssf.usermodel.HSSFWorkbook();
        sheetNames = this.checkSheetNames(entityResultsList.size(), sheetNames);
        for (int i = 0; i < entityResultsList.size(); i++) {
            this.writeSheet(wb, (EntityResult) entityResultsList.get(i), columnSort, (String) sheetNames.get(i),
                    hColumnRenderers, writeHeader);
        }
        wb.write(os);
    }

    /**
     * This method creates an XLS from the input data supplied in the {@link EntityResult rs}. If the
     * parameter {@code xlsx} is {@code true} and {@link XLSExporterFactory#isAvailableXLSX()} is
     * {@code true}, use an {@link SXSSFWorkbook} as {@link Workbook}, otherwise, uses an
     * {@link HSSFWorkbook}
     * @param rs {@link to EntityResult} The data to export
     * @param os {@link OutputStream} The output stream to save the document
     * @param sheetName {@link String} The page name of the Excel file.
     * @param hColumnRenderers {@link CellRenderer} Renderers of table columns.
     * @param columnSort {@link List} List of column order.
     * @param writeHeader {@link Boolean} Boolean indicating whether the column headers are to be
     *        written.
     * @param xlsx {@link Boolean} Boolean indicating whether the file has a *. xslx extension
     * @throws IOException
     *
     * @see {@link #writeSheet(Workbook, EntityResult, List, List, List, String, Hashtable, boolean)
     *      This method} for *.xlsx and
     *      {@link #writeSheet(Workbook, EntityResult, List, String, Hashtable, boolean) this other
     *      method} for *.xls
     */
    public void createXLS(EntityResult rs, OutputStream os, String sheetName, Map<?,?> hColumnRenderers,
            List<?> columnSort, boolean writeHeader, boolean xlsx) throws IOException {
        Workbook wb = null;
        if (xlsx && XLSExporterFactory.isAvailableXLSX()) {
            wb = new org.apache.poi.xssf.streaming.SXSSFWorkbook();
            sheetName = sheetName == null ? "Sheet" : sheetName;
            this.writeSheet(wb, rs, columnSort, null, null, sheetName, hColumnRenderers, writeHeader);
            wb.write(os);
        } else {
            wb = new org.apache.poi.hssf.usermodel.HSSFWorkbook();
            sheetName = sheetName == null ? "Sheet" : sheetName;
            this.writeSheet(wb, rs, columnSort, sheetName, hColumnRenderers, writeHeader);
            wb.write(os);
        }
    }

    /**
     * Return a {@link List} with the names of the sheets. If the {@link List} is not null and its size
     * matches the {@code size} parameter, it returns the same list. Otherwise, it creates a
     * {@link Vector} with the name of the sheets according to the "Sheet X" pattern, X being the number
     * of the sheet and returns it.
     * @param size {@link Integer} The size of the list with de sheet names
     * @param sheetNames {@link List} List of {@link String} with the names of the sheets
     * @return A {@link List} with the names of the sheet
     */
    protected List<Object> checkSheetNames(int size, List<?> sheetNames) {
        if ((sheetNames != null) && (sheetNames.size() == size)) {
            return new ArrayList<>(sheetNames);
        }
        List<Object> res = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            res.add("Sheet" + i);
        }
        return res;
    }

    /**
     * Write a spreadsheet with the data provided {@link EntityResult rs} in the supplied workbook
     * {@link HSSFWorkbook wb}
     * @param wb {@link Workbook} Workbook where the spreadsheet will be written.
     * @param rs {@link EntityResult} the data to export.
     * @param order {@link List} List of column order.
     * @param sheetName {@link String} The page name of the Excel file.
     * @param hColumnRenderers {@link Hashtable} Hashtable with the column renderers.
     * @param writeHeader {@link Boolean} Boolean to indicate if the column headers are written.
     */
    protected void writeSheet(Workbook wb, EntityResult rs, List<?> order, String sheetName, Map<?,?> hColumnRenderers,
            boolean writeHeader) {

        int count = 0;

        this.dfs = new DecimalFormatSymbols(ApplicationManager.getLocale());
        this.decimalFormat.setDecimalFormatSymbols(this.dfs);
        this.decimalFormat.applyLocalizedPattern(this.getDecimalPattern(this.dfs));
        this.numericFormat.setDecimalFormatSymbols(this.dfs);
        this.numericFormat.applyLocalizedPattern(this.getNumericPattern(this.dfs));

        if (order == null) {
            order = rs.getOrderColumns();
        }
        if (order == null) {
            order = new ArrayList<>(rs.keySet());
        }

        this.helper = wb.getCreationHelper();

        int rowNumbers = rs.calculateRecordNumber();
        int numberLoops = (int) Math.floor(rowNumbers / this.MAX_ROWS_XLS) + 1;

        for (int i = 0; i < numberLoops; i++) {

            Sheet sheet = wb.createSheet(sheetName + (i + 1));

            if (sheet instanceof SXSSFSheet) {
                SXSSFSheet sxSheet = (SXSSFSheet) sheet;
                try {
                    Method m = SXSSFSheet.class.getMethod("trackAllColumnsForAutoSizing", new Class[] {});
                    if (m != null) {
                        m.invoke(sxSheet, new Object[] {});
                    }
                } catch (Exception e) {
                    Poi3_5XLSExporterUtils.logger
                        .debug("Apache POI libraries below 3.15 version. Method \"{}\" not found.", e.getMessage(), e);
                }
            }

            this.drawing = sheet.createDrawingPatriarch();

            this.setColumnStyle(wb, sheet, order, null, hColumnRenderers, rs.getColumnSQLTypes());

            if (writeHeader) {
                this.writeLine(wb, sheet, rs.getOrderColumns(), order, hColumnRenderers, null, rs.getColumnSQLTypes());
            }

            if (rs.calculateRecordNumber() > this.MAX_ROWS_XLS) {
                count = this.MAX_ROWS_XLS;
            } else {
                count = rs.calculateRecordNumber();
            }

            for (int j = 0; j < count; j++) {
                Map<?,?> h = rs.getRecordValues(j);
                List<Object> values = new ArrayList<>(order.size());
                for (Iterator it = order.iterator(); it.hasNext();) {
                    Object key = it.next();
                    values.add(h.get(key));
                }
                this.writeLineWithoutStyle(wb, sheet, values, order, hColumnRenderers, null, rs.getColumnSQLTypes());
            }

            for (int k = 0; k < order.size(); k++) {
                sheet.autoSizeColumn(k);
            }

            Enumeration<?> enu = rs.keys();
            while (enu.hasMoreElements()) {
                Object key = enu.nextElement();
                List<?> values = (List<?>) rs.get(key);
                try {
                    values.subList(0, this.MAX_ROWS_XLS).clear();
                    rs.put(key, values);
                } catch (Exception e) {
                    Poi3_5XLSExporterUtils.logger.trace(null, e);
                    continue;
                }
            }
        }
    }

    /**
     * Sets the column styles in the workbook and sheet the supplied.
     * @param wb {@link Workbook} Workbook containing the sheet to which the styles will be applied.
     * @param sheet {@link Sheet} Sheet to which the styles will be applied.
     * @param orderColumns {@link List} List of ordered columns.
     * @param columnStyles {@link List} List of columns styles.
     * @param hColumnRenderers {@link Map} Map with columns renderers.
     * @param hColumnTypes {@link Map} Map with the list of column types.
     */
    protected void setColumnStyle(Workbook wb, Sheet sheet, List<?> orderColumns, List<?> columnStyles,
            Map<?,?> hColumnRenderers, Map<?,?> hColumnTypes) {
        Row row = sheet.createRow(sheet.getLastRowNum());
        int column = 0;

        for (int i = 0; i < orderColumns.size(); i++) {
            if (columnStyles != null) {
                try {
                    Object style = columnStyles.get(i);
                    if (style instanceof CellStyle) {
                        sheet.setDefaultColumnStyle(i, (CellStyle) style);
                        continue;
                    }
                } catch (Exception x) {
                    Poi3_5XLSExporterUtils.logger.error(null, x);
                }
            }

            Cell cell = null;
            try {
                cell = row.createCell((short) column++);
            } catch (Exception e1) {
                Poi3_5XLSExporterUtils.logger.error(null, e1);
            }

            switch (this.getCellType(orderColumns.get(i).toString(), true, hColumnRenderers, hColumnTypes)) {

                case DATE_CELL:
                case DATE_HOUR_CELL:

                    this.cs_date_hour = wb.createCellStyle();
                    this.cs_date_hour.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy h:mm"));

                    this.cs_date = wb.createCellStyle();
                    this.cs_date.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));

                    sheet.setDefaultColumnStyle(i, this.cs_date);

                    break;

                case CURRENCY_CELL:

                    this.cs_currency = wb.createCellStyle();

                    this.cs_currency.setDataFormat(wb.createDataFormat()
                        .getFormat(AbstractXLSExporter.currencyPattern + " " + CurrencyDocument.defaultCurrencySymbol));

                    sheet.setDefaultColumnStyle(i, this.cs_currency);

                    break;

                case PERCENT_CELL:
                    this.cs_percent = wb.createCellStyle();
                    this.cs_percent
                        .setDataFormat(wb.createDataFormat().getFormat(AbstractXLSExporter.currencyPattern + "\" %\""));
                    sheet.setDefaultColumnStyle(i, this.cs_percent);
                    break;

                case REAL_CELL:
                    this.cs_real = wb.createCellStyle();
                    this.cs_real.setDataFormat(wb.createDataFormat().getFormat(AbstractXLSExporter.currencyPattern));
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * Write a spreadsheet with the data provided {@link EntityResult rs} in the supplied workbook
     * {@link SXSSFWorkbook wb}
     * @param wb {@link Workbook} Workbook that will contain the page to write.
     * @param rs {@link EntityResult} The data to write in the sheet
     * @param order {@link List} List of ordered columns.
     * @param columnStyles {@link List} List of column styles.
     * @param columnHeaderStyles {@link List} List of column header styles.
     * @param sheetName {@link String} Name of spreadsheet
     * @param hColumnRenderers {@link Hashtable} Hashtable with the renderers to apply in the columns.
     * @param writeHeader {@link Boolean} Boolean to indicate whether the column headers are written.
     */
    protected void writeSheet(Workbook wb, EntityResult rs, List<?> order, List<?> columnStyles, List<?> columnHeaderStyles,
            String sheetName, Map<?,?> hColumnRenderers,
            boolean writeHeader) {

        int count = 0;

        this.dfs = new DecimalFormatSymbols(ApplicationManager.getLocale());
        this.decimalFormat.setDecimalFormatSymbols(this.dfs);
        this.decimalFormat.applyLocalizedPattern(this.getDecimalPattern(this.dfs));
        this.numericFormat.setDecimalFormatSymbols(this.dfs);
        this.numericFormat.applyLocalizedPattern(this.getNumericPattern(this.dfs));

        if (order == null) {
            order = rs.getOrderColumns();
        }
        if (order == null) {
            order = new ArrayList<>(rs.keySet());
        }

        int rowNumbers = rs.calculateRecordNumber();
        int numberLoops = (int) Math.floor(rowNumbers / this.MAX_ROWS_XLSX) + 1;

        this.drawing = null;
        this.helper = wb.getCreationHelper();
        for (int i = 0; i < numberLoops; i++) {

            Sheet sheet = wb.createSheet("Sheet " + (i + 1));

            if (sheet instanceof SXSSFSheet) {
                SXSSFSheet sxSheet = (SXSSFSheet) sheet;
                try {
                    Method m = SXSSFSheet.class.getMethod("trackAllColumnsForAutoSizing", new Class[] {});
                    if (m != null) {
                        m.invoke(sxSheet, new Object[] {});
                    }
                } catch (Exception e) {
                    Poi3_5XLSExporterUtils.logger
                        .debug("Apache POI libraries below 3.15 version. Method \"{}\" not found.", e.getMessage(), e);
                }
            }

            if (this.drawing == null) {
                this.drawing = sheet.createDrawingPatriarch();
            }

            this.setColumnStyle(wb, sheet, order, columnStyles, hColumnRenderers, rs.getColumnSQLTypes());

            if (writeHeader) {
                this.writeLine(wb, sheet, order, order, hColumnRenderers, columnHeaderStyles, rs.getColumnSQLTypes());
            }

            if (rs.calculateRecordNumber() > this.MAX_ROWS_XLSX) {
                count = this.MAX_ROWS_XLSX;
            } else {
                count = rs.calculateRecordNumber();
            }

            for (int j = 0; j < count; j++) {
                Map<?,?> h = rs.getRecordValues(j);
                List<Object> values = new ArrayList<>(order.size());
                for (Iterator it = order.iterator(); it.hasNext();) {
                    Object key = it.next();
                    values.add(h.get(key));
                }
                this.writeLineWithoutStyle(wb, sheet, values, order, hColumnRenderers, columnStyles,
                        rs.getColumnSQLTypes());
            }

            for (int k = 0; k < order.size(); k++) {
                sheet.autoSizeColumn(k);
            }

            Enumeration<?> enu = rs.keys();
            while (enu.hasMoreElements()) {
                Object key = enu.nextElement();
                List<?> values = (List<?>) rs.get(key);
                try {
                    values.subList(0, this.MAX_ROWS_XLSX).clear();
                    rs.put(key, values);
                } catch (Exception e) {
                    Poi3_5XLSExporterUtils.logger.trace(null, e);
                    continue;
                }
            }

        }
    }

    /**
     * Add a new line after the last line written in the spreadsheet without any style.
     * @param wb {@link Workbook} Workbook to which the spreadsheet belongs
     * @param sheet {@link Sheet} Sheet to which the new line will be added.
     * @param values {@link List} List of values to be added to the new line
     * @param orderColumns {@link List} List of column order
     * @param hColumnRenderers {@link Map} Map with the renderers of the columns
     * @param columnStyles {@link List} List of column styles (not used in this method)
     * @param hColumnTypes {@link Map} Map containing the column data type.
     */
    protected void writeLineWithoutStyle(Workbook wb, Sheet sheet, List<?> values, List<?> orderColumns,
            Map<?,?> hColumnRenderers, List<?> columnStyles, Map<?,?> hColumnTypes) {
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        int column = 0;
        for (int i = 0; i < values.size(); i++) {
            Object ob = values.get(i);
            Cell cell = null;
            try {
                cell = row.createCell((short) column++);
            } catch (Exception e1) {
                Poi3_5XLSExporterUtils.logger.error(null, e1);
            }

            if (cell != null) {
                switch (this.getCellType(orderColumns.get(i).toString(), ob, hColumnRenderers, hColumnTypes)) {

                    case DECIMAL_CELL:
                        this.writeDecimalLineWithoutStyle(ob, cell);
                        break;
                    case NUMERIC_CELL:
                        this.writeNumericLineWithoutStyle(ob, cell);
                        break;

                    case DATE_CELL:
                    case DATE_HOUR_CELL:
                        this.writeDateLineWithoutStyle(ob, cell);

                        break;

                    case CURRENCY_CELL:
                        this.writeCurrencyLineWithoutStyle(ob, cell);
                        break;
                    // since 5.3.8
                    case IMAGE_CELL:
                        this.writeImageLineWithoutStyle(wb, sheet, row, column, ob);
                        break;

                    case PERCENT_CELL:

                        this.writePercentLineWithoutStyle(ob, cell);
                        break;

                    case REAL_CELL:
                        this.writeRealLineWithoutStyle(ob, cell);
                        break;

                    case TEXT_CELL:
                        cell.setCellType(Cell.CELL_TYPE_STRING);
                        cell.setCellValue(String.valueOf(ob));
                        break;
                    default:
                        try {
                            ParsePosition parsePos = new ParsePosition(0);
                            Number number = this.decimalFormat.parse(String.valueOf(ob).trim(), parsePos);
                            if (parsePos.getIndex() < String.valueOf(ob).trim().length()) {
                                throw new Exception("Parse not complete");
                            }
                            cell.setCellValue(number.doubleValue());
                        } catch (Exception e) {
                            Poi3_5XLSExporterUtils.logger.trace(null, e);
                            cell.setCellType(Cell.CELL_TYPE_STRING);
                            cell.setCellValue(String.valueOf(ob));
                        }
                        break;
                }
            }
        }
    }

    protected void writeRealLineWithoutStyle(Object ob, Cell cell) {
        cell.setCellStyle(this.cs_real);
        if (ob != null) {
            if (ob instanceof Number) {
                if ((ob instanceof Double) || (ob instanceof Float) || (ob instanceof Long)) {
                    cell.setCellValue(((Double) ob).doubleValue());
                }
                cell.setCellType(Cell.CELL_TYPE_NUMERIC);
            } else {
                try {
                    Number number = this.decimalFormat.parse(ob.toString());
                    if ((number instanceof Double) || (number instanceof Float) || (number instanceof Long)) {
                        cell.setCellValue(number.doubleValue());
                    }
                    cell.setCellType(Cell.CELL_TYPE_NUMERIC);

                } catch (Exception e) {
                    Poi3_5XLSExporterUtils.logger.trace(null, e);
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    cell.setCellValue(ob.toString());
                }
            }
        }
    }

    protected void writePercentLineWithoutStyle(Object ob, Cell cell) {
        ParsePosition ppt = new ParsePosition(0);
        cell.setCellType(Cell.CELL_TYPE_NUMERIC);

        if (ob != null) {

            String cellValue = ((String) ob).replaceAll(this.getPercentSymbol((String) ob), "").trim();
            Number number = this.decimalFormat.parse(String.valueOf(cellValue).trim(), ppt);
            if (number != null) {
                cell.setCellValue(number.doubleValue());
            }

            cell.setCellStyle(this.cs_percent);
        }
    }

    protected void writeImageLineWithoutStyle(Workbook wb, Sheet sheet, Row row, int column, Object ob) {
        byte[] bytesImage = new byte[0];
        try {
            ByteBuffer bb = ByteBuffer.wrap(ob.toString().getBytes(Charset.forName("ISO-8859-1")));
            bytesImage = bb.array();
        } catch (Exception e1) {
            Poi3_5XLSExporterUtils.logger.error(null, e1);
        }
        if (bytesImage.length > 0) {
            int pictureIdx = wb.addPicture(bytesImage, Workbook.PICTURE_TYPE_JPEG);

            // add a picture shape
            ClientAnchor anchor = this.helper.createClientAnchor();

            // set top-left corner of the picture,
            // subsequent call of Picture#resize() will operate relative
            // to it
            anchor.setCol1(column - 1);
            anchor.setRow1(sheet.getLastRowNum());

            anchor.setCol2(column);
            anchor.setRow2(sheet.getLastRowNum() + 1);

            Picture pict = this.drawing.createPicture(anchor, pictureIdx);
            row.setHeight((short) 1000);
        }
    }

    protected void writeCurrencyLineWithoutStyle(Object ob, Cell cell) {
        ParsePosition pp = new ParsePosition(0);
        cell.setCellType(Cell.CELL_TYPE_NUMERIC);

        if (ob != null) {

            String cellValue = ((String) ob).replaceAll(this.getCurrencySymbol((String) ob), "").trim();
            Number number = this.decimalFormat.parse(String.valueOf(cellValue).trim(), pp);
            // cellValue = cellValue.replaceAll(",", "");
            cell.setCellStyle(this.cs_currency);
            if (number != null) {
                cell.setCellValue(number.doubleValue());
            }
        }
    }

    protected void writeDateLineWithoutStyle(Object ob, Cell cell) {
        if (ob != null) {
            if (ob.toString().contains(":")) {
                if (ob instanceof Date) {
                    cell.setCellValue((Date) ob);
                    cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                    cell.setCellStyle(this.cs_date_hour);
                } else {
                    try {
                        cell.setCellValue(this.sdfHour.parse(ob.toString()));
                        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                        cell.setCellStyle(this.cs_date_hour);
                    } catch (ParseException e) {
                        Poi3_5XLSExporterUtils.logger.trace(null, e);
                        cell.setCellType(Cell.CELL_TYPE_STRING);
                        cell.setCellValue(String.valueOf(ob));
                    }
                }
            } else {
                if (ob instanceof Date) {
                    cell.setCellValue((Date) ob);
                    cell.setCellStyle(this.cs_date);
                } else {
                    try {
                        cell.setCellValue(this.sdf.parse(ob.toString()));
                        cell.setCellStyle(this.cs_date);
                    } catch (ParseException e) {
                        Poi3_5XLSExporterUtils.logger.trace(null, e);
                        cell.setCellType(Cell.CELL_TYPE_STRING);
                        cell.setCellValue(String.valueOf(ob));
                    }
                }

            }
        }
    }

    protected void writeNumericLineWithoutStyle(Object ob, Cell cell) {
        if (ob != null) {
            if (ob instanceof Number) {
                cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                cell.setCellValue(((Number) ob).intValue());
            } else {
                try {
                    int value = this.numericFormat.parse(ob.toString()).intValue();
                    cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                    cell.setCellValue(value);
                } catch (Exception e) {
                    Poi3_5XLSExporterUtils.logger.trace(null, e);
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    cell.setCellValue(ob.toString());
                }
            }

        }
    }

    protected void writeDecimalLineWithoutStyle(Object ob, Cell cell) {
        if (ob != null) {
            if (ob instanceof Number) {
                if (ob instanceof Long) {
                    cell.setCellValue(((Long) ob).longValue());
                }
                if (ob instanceof Float) {
                    cell.setCellValue(((Float) ob).floatValue());
                }
                if (ob instanceof Double) {
                    cell.setCellValue(((Double) ob).doubleValue());
                }
                cell.setCellType(Cell.CELL_TYPE_NUMERIC);
            } else {
                try {
                    Number number = this.decimalFormat.parse(ob.toString());
                    if (number instanceof Long) {
                        cell.setCellValue(number.longValue());
                    }
                    if (number instanceof Float) {
                        cell.setCellValue(number.floatValue());
                    }
                    if (number instanceof Double) {
                        cell.setCellValue(number.doubleValue());
                    }
                    cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                } catch (Exception e) {
                    Poi3_5XLSExporterUtils.logger.trace(null, e);
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    cell.setCellValue(ob.toString());
                }
            }

        }
    }

    /**
     * @deprecated
     *
     *             Write a spreadsheet with the data provided {@link EntityResult rs} in the supplied
     *             {@link Workbook}
     * @param wb {@link Workbook} Workbook that will contain the page to write.
     * @param rs {@link EntityResult} The data to write in the sheet
     * @param order {@link List} List of ordered columns.
     * @param columnStyles {@link List} List of column styles.
     * @param columnHeaderStyles {@link List} List of column header styles.
     * @param sheetName {@link String} Name of spreadsheet
     * @param hColumnRenderers {@link Map} Map with the renderers to apply in the columns.
     * @param writeHeader {@link Boolean} Boolean to indicate whether the column headers are written.
     *
     */
    @Deprecated
    protected void writeSheet_(Workbook wb, EntityResult rs, List<?> order, List<?> columnStyles, List<?> columnHeaderStyles,
            String sheetName, Map<?,?> hColumnRenderers,
            boolean writeHeader) {
        this.dfs = new DecimalFormatSymbols(ApplicationManager.getLocale());
        this.decimalFormat.setDecimalFormatSymbols(this.dfs);
        this.decimalFormat.applyLocalizedPattern(this.getDecimalPattern(this.dfs));
        this.numericFormat.setDecimalFormatSymbols(this.dfs);
        this.numericFormat.applyLocalizedPattern(this.getNumericPattern(this.dfs));
        if (order == null) {
            order = rs.getOrderColumns();
        }
        if (order == null) {
            order = new ArrayList<>(rs.keySet());
        }

        Sheet sheet = wb.createSheet(sheetName);

        if (writeHeader) {
            this.writeLine(wb, sheet, order, order, hColumnRenderers, columnHeaderStyles, rs.getColumnSQLTypes());
        }
        for (int count = rs.calculateRecordNumber(), i = 0; i < count; i++) {
            Map<?,?> h = rs.getRecordValues(i);
            List<Object> values = new ArrayList<>(order.size());
            for (Iterator it = order.iterator(); it.hasNext();) {
                Object key = it.next();
                values.add(h.get(key));
            }
            this.writeLine(wb, sheet, values, order, hColumnRenderers, columnStyles, rs.getColumnSQLTypes());
        }

        for (int i = 0; i < order.size(); i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Write a new line (row) after the last line written on the worksheet and workbook selected
     * @param wb {@link Workbook} Workbook selected
     * @param sheet {@link Sheet} Sheet selected
     * @param values {@link List} List of values to write in the sheet row
     * @param orderColumns {@link List} List of ordered columns
     * @param hColumnRenderers {@link Map} Map with column renderers
     * @param columnStyles {@link List} List of column styles
     * @param hColumnTypes {@link Map} Map of column types to export
     */
    protected void writeLine(Workbook wb, Sheet sheet, List<?> values, List<?> orderColumns, Map<?, ?> hColumnRenderers,
            List<?> columnStyles, Map<?, ?> hColumnTypes) {
        Row row = sheet.createRow(sheet.getLastRowNum());
        int column = 0;
        for (int i = 0; i < values.size(); i++) {
            Object ob = values.get(i);
            Cell cell = null;
            try {
                cell = row.createCell((short) column++);
                CellStyle cs_style = columnStyles != null ? (CellStyle) columnStyles.get(i) : wb.createCellStyle();
                cell.setCellStyle(cs_style);
            } catch (Exception e1) {
                Poi3_5XLSExporterUtils.logger.error(null, e1);
            }

            if (cell != null) {
                switch (this.getCellType(orderColumns.get(i).toString(), ob, hColumnRenderers, hColumnTypes)) {
                    case DECIMAL_CELL:
                        this.writeDecimalLineWithoutStyle(ob, cell);
                        break;
                    case NUMERIC_CELL:
                        this.writeNumericLineWithoutStyle(ob, cell);
                        break;

                    case DATE_CELL:
                    case DATE_HOUR_CELL:
                        this.writeDateLine(ob, cell);

                        break;

                    case CURRENCY_CELL:
                        this.writeCurrencyLine(wb, ob, cell);
                        break;
                    // since 5.3.8
                    case IMAGE_CELL:
                        this.writeImageLine(wb, sheet, column, ob);
                        break;
                    case TEXT_CELL:
                        cell.setCellType(Cell.CELL_TYPE_STRING);
                        cell.setCellValue(String.valueOf(ob));
                        break;
                    default:
                        try {
                            ParsePosition parsePos = new ParsePosition(0);
                            Number number = this.decimalFormat.parse(String.valueOf(ob).trim(), parsePos);
                            if (parsePos.getIndex() < String.valueOf(ob).trim().length()) {
                                throw new Exception("Parse not complete");
                            }
                            cell.setCellValue(number.doubleValue());
                        } catch (Exception e) {
                            Poi3_5XLSExporterUtils.logger.trace(null, e);
                            cell.setCellType(Cell.CELL_TYPE_STRING);
                            cell.setCellValue(String.valueOf(ob));
                        }
                        break;
                }
            }
        }
    }

    protected void writeImageLine(Workbook wb, Sheet sheet, int column, Object ob) {
        byte[] bytesImage = new byte[0];
        try {
            ByteBuffer bb = ByteBuffer.wrap(ob.toString().getBytes(Charset.forName("ISO-8859-1")));
            bytesImage = bb.array();
        } catch (Exception e1) {
            Poi3_5XLSExporterUtils.logger.error(null, e1);
        }
        if (bytesImage.length > 0) {
            int pictureIdx = wb.addPicture(bytesImage, Workbook.PICTURE_TYPE_JPEG);

            // add a picture shape
            ClientAnchor anchor = this.helper.createClientAnchor();
            // set top-left corner of the picture,
            // subsequent call of Picture#resize() will operate relative
            // to it
            anchor.setCol1(column - 1);
            anchor.setRow1(sheet.getLastRowNum());
            Picture pict = this.drawing.createPicture(anchor, pictureIdx);

            // auto-size picture relative to its top-left corner
            pict.resize(0.3);
        }
    }

    protected void writeCurrencyLine(Workbook wb, Object ob, Cell cell) {
        ParsePosition pp = new ParsePosition(0);
        cell.setCellType(Cell.CELL_TYPE_NUMERIC);

        if (ob != null) {

            String cellValue = ((String) ob).replaceAll(this.getCurrencySymbol((String) ob), "").trim();
            Number number = this.decimalFormat.parse(String.valueOf(cellValue).trim(), pp);
            // cellValue = cellValue.replaceAll(",", "");
            if (number != null) {
                CellStyle cs_currency = cell.getCellStyle();
                cs_currency.setDataFormat(wb.createDataFormat()
                    .getFormat(AbstractXLSExporter.currencyPattern + " " + this.getCurrencySymbol((String) ob)));
                cell.setCellStyle(cs_currency);
                cell.setCellValue(number.doubleValue());
            }
        }
    }

    protected void writeDateLine(Object ob, Cell cell) {
        if (ob != null) {
            if (ob.toString().contains(":")) {
                if (ob instanceof Date) {
                    cell.setCellValue((Date) ob);
                    cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                    CellStyle cs_date_hour = cell.getCellStyle();
                    cs_date_hour.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy h:mm"));
                    cell.setCellStyle(cs_date_hour);
                } else {
                    try {
                        cell.setCellValue(this.sdfHour.parse(ob.toString()));
                        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                        CellStyle cs_date_hour = cell.getCellStyle();
                        cs_date_hour.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy h:mm"));
                        cell.setCellStyle(cs_date_hour);
                    } catch (ParseException e) {
                        Poi3_5XLSExporterUtils.logger.trace(null, e);
                        cell.setCellType(Cell.CELL_TYPE_STRING);
                        cell.setCellValue(String.valueOf(ob));
                    }
                }
            } else {
                if (ob instanceof Date) {
                    cell.setCellValue((Date) ob);
                    CellStyle cs_date = cell.getCellStyle();
                    cs_date.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));
                    cell.setCellStyle(cs_date);
                } else {
                    try {
                        cell.setCellValue(this.sdf.parse(ob.toString()));
                        CellStyle cs_date = cell.getCellStyle();
                        cs_date.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));
                        cell.setCellStyle(cs_date);
                    } catch (ParseException e) {
                        Poi3_5XLSExporterUtils.logger.trace(null, e);
                        cell.setCellType(Cell.CELL_TYPE_STRING);
                        cell.setCellValue(String.valueOf(ob));
                    }
                }
            }
        }
    }

    /**
     * Adds a line after the last line added to an {@link XSSFSheet} instance sheet.
     * @param wb {@link Object} Workbook to which the sheet belongs
     * @param sheet {@link XSSFSheet} Hoja a la que se añadirá la línea.
     * @param values {@link List} List of values to be added to the line.
     * @param orderColumns {@link List} List of column order.
     * @param hColumnRenderers {@link Map} Map containing the renderers of the columns.
     * @param columnStyles {@link List} List of column styles
     * @param hColumnTypes {@link Map} Map containing the data type of the columns to be
     *        exported.
     */
    protected void writeLine(Object wb, org.apache.poi.xssf.usermodel.XSSFSheet sheet, List<?> values, List<?> orderColumns,
    		Map<?, ?> hColumnRenderers, List<?> columnStyles,
    		Map<?, ?> hColumnTypes) {
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        int column = 0;
        for (int i = 0; i < values.size(); i++) {
            Object ob = values.get(i);
            Method method = null;
            Cell cell = null;
            try {
                method = Row.class.getMethod("createCell", new Class[] { int.class });
                cell = (Cell) method.invoke(row, new Object[] { new Short((short) column++) });
                if (columnStyles != null) {
                    cell.setCellStyle((org.apache.poi.xssf.usermodel.XSSFCellStyle) columnStyles.get(i));
                }
            } catch (Exception e1) {
                Poi3_5XLSExporterUtils.logger.error(null, e1);
            }

            if (cell != null) {
                switch (this.getCellType(orderColumns.get(i).toString(), ob, hColumnRenderers, hColumnTypes)) {
                    case NUMERIC_CELL:
                        try {
                            cell.setCellValue(((Number) ob).doubleValue());
                        } catch (Exception e) {
                            Poi3_5XLSExporterUtils.logger.trace(null, e);
                            if (ob != null) {
                                if (!(ob instanceof Number)) {
                                    cell.setCellValue(new Double(ob.toString()));
                                }
                            }
                        }
                        break;
                    case DATE_CELL:
                        cell.setCellValue((Date) ob);
                        CellStyle style = cell.getCellStyle();
                        DataFormat dataFormat = ((Workbook) wb).createDataFormat();
                        style.setDataFormat(dataFormat.getFormat(this.dateFormat));
                        cell.setCellStyle(style);
                        break;
                    case CURRENCY_CELL:
                        CellStyle cs = cell.getCellStyle();
                        String cellValue = ((String) ob).replaceAll(this.getCurrencySymbol((String) ob), "").trim();
                        ParsePosition pp = new ParsePosition(0);
                        Number number = this.decimalFormat.parse(String.valueOf(cellValue).trim(), pp);
                        // cellValue = cellValue.replaceAll(",", "");
                        cell.setCellStyle(cs);
                        if (number != null) {
                            cs.setDataFormat(((Workbook) wb).createDataFormat()
                                .getFormat(AbstractXLSExporter.currencyPattern + this.getCurrencySymbol((String) ob)));
                            cell.setCellValue(number.doubleValue());
                        }
                        break;
                    default:
                        try {
                            pp = new ParsePosition(0);
                            number = this.decimalFormat.parse(String.valueOf(ob).trim(), pp);
                            if (pp.getIndex() < String.valueOf(ob).trim().length()) {
                                throw new Exception("Parse not complete");
                            }
                            cell.setCellValue(number.doubleValue());
                        } catch (Exception e) {
                            Poi3_5XLSExporterUtils.logger.trace(null, e);
                            cell.setCellValue(String.valueOf(ob));
                            if (columnStyles != null) {
                                cell.setCellStyle((CellStyle) columnStyles.get(i));
                            }
                        }
                        break;
                }
            }
        }
    }

    /**
     * Write a new line (row) after the last line written on the worksheet and workbook selected
     * @param wb {@link Workbook} Workbook selected
     * @param sheet {@link Sheet} Sheet selected
     * @param values {@link List} List of values to write in the sheet row
     * @param order {@link List} List of ordered columns
     * @param hColumnRenderers {@link Map} Map with column renderers
     * @param hColumnTypes {@link Map} Map of column types to export
     *
     * @see #writeLine(Workbook, Sheet, List, List, Hashtable, List, Hashtable)
     */
    protected void writeLine(Workbook wb, Sheet sheet, List<?> values, List<?> order, Map<?,?> hColumnRenderers,
            Map<?,?> hColumnTypes) {
        this.writeLine(wb, sheet, values, order, hColumnRenderers, null, hColumnTypes);
    }

    /**
     * Creates a *.xls or *.xslx file with the {@link EntityResult} content in the {@link OutputStream}
     * indicated depending on whether the {@code xslx} parameter is {@code true} or not.
     * @param rs {@link EntityResult} EntityResult with the data to export
     * @param os {@link OutputStream} The output stream to save the document
     * @param sheetName {@link String} The name of the sheet
     * @param hColumnRenderers {@link Map} Map with the column renderers
     * @param columnSort {@link List} List with the ordered columns
     * @param columnStyles {@link List} List with the styles of the column
     * @param columnHeaderStyles {@link List} List with the styles of the column headers
     * @param wb {@link Workbook} Workbook to which the spreadsheet belongs
     * @param xlsx {@link Boolean} Boolean indicating whether the export is in *.xsl or *.xslx
     * @param writeHeader {@link Boolean} Boolean indicating whether to write column headers
     * @throws Exception
     *
     */
    public void createXLS(EntityResult rs, OutputStream os, String sheetName, Map<?,?> hColumnRenderers,
            List<?> columnSort, List<?> columnStyles, List<?> columnHeaderStyles, Workbook wb,
            boolean xlsx, boolean writeHeader) throws Exception {
        if (wb == null) {
            if (xlsx) {
                wb = new org.apache.poi.xssf.streaming.SXSSFWorkbook();
            } else {
                wb = new org.apache.poi.hssf.usermodel.HSSFWorkbook();
            }

        }
        sheetName = sheetName == null ? "Sheet" : sheetName;
        this.writeSheet(wb, rs, columnSort, columnStyles, columnHeaderStyles, sheetName, hColumnRenderers, writeHeader);
        wb.write(os);
    }

    @Override
    public void createXLS(EntityResult rs, File output, String sheetName, Map<?,?> hColumnRenderers, List<?> columnSort,
            List<?> columnStyles, List<?> columnHeaderStyles, Workbook wb,
            boolean writeHeader, boolean xlsx, boolean openFile) throws Exception {
        OutputStream os = new FileOutputStream(output);
        try {
            this.createXLS(rs, os, sheetName, hColumnRenderers, columnSort, columnStyles, columnHeaderStyles, wb, xlsx,
                    writeHeader);
            if (openFile) {
                WindowsUtils.openFile(output);
            }
        } catch (Exception e) {
            Poi3_5XLSExporterUtils.logger.error(null, e);
        } finally {
            os.close();
        }
    }

    /**
     * Try to load a return the {@link XSSFWorkbook} class. If it is not possible, return null
     * @return An {@link XSSFWorkbook} class or null if not exists.
     */
    public static Object createXSSFWorkbook() {
        Class<?> classObject = null;
        try {
            classObject = Poi3_5XLSExporterUtils.class.getClassLoader()
                .loadClass("org.apache.poi.xssf.usermodel.XSSFWorkbook");
        } catch (Exception e) {
            Poi3_5XLSExporterUtils.logger.error(null, e);
        }
        return classObject;
    }

    /**
     * @see #createXLS(EntityResult, File, String, Map, List, boolean, boolean, boolean)
     */
    @Override
    public void createXLS(EntityResult rs, File output, String sheetName, Map<?,?> hColumnRenderers, List<?> columnSort,
            boolean writeHeader, boolean openFile) throws Exception {
        this.createXLS(rs, output, sheetName, hColumnRenderers, columnSort, writeHeader, false, openFile);
    }

    /**
     * @see #createXLS(EntityResult, File, String, Map, List, List, List, Workbook, boolean,
     *      boolean, boolean)
     */
    @Override
    public void createXLS(EntityResult rs, File output, String sheetName, Map<?,?> hColumnRenderers, List<?> columnSort,
            List<?> columnStyles, List<?> columnHeaderStyles, Workbook wb,
            boolean writeHeader, boolean openFile) throws Exception {
        this.createXLS(rs, output, sheetName, hColumnRenderers, columnSort, columnStyles, columnHeaderStyles, wb, false,
                writeHeader, openFile);
    }

    /**
     * @see #createXLS(EntityResult, File, String, Hashtable, List, boolean, boolean)
     */
    @Override
    public void createXLS(EntityResult rs, File output, String sheetName, List<?> columnSort, boolean writeHeader,
            boolean openFile) throws Exception {
        this.createXLS(rs, output, sheetName, new HashMap<>(), columnSort, writeHeader, openFile);
    }

    /**
     * @see #createXLS(EntityResult, File, String, Hashtable, List, boolean, boolean)
     */
    @Override
    public void createXLS(EntityResult rs, File output, String sheetName, List<?> columnSort, boolean writeHeader,
            boolean xlsx, boolean openFile) throws Exception {
        this.createXLS(rs, output, sheetName, new HashMap<>(), columnSort, writeHeader, openFile);
    }

    /**
     * @see #createXLS(EntityResult, File, String, Hashtable, List, List, List, Workbook, boolean,
     *      boolean, boolean)
     */
    @Override
    public void createXLS(EntityResult rs, File output, String sheetName, List<?> columnSort, List<?> columnStyles,
            List<?> columnHeaderStyles, Workbook wb, boolean writeHeader,
            boolean xlsx, boolean openFile) throws Exception {
        this.createXLS(rs, output, sheetName, new HashMap<>(), columnSort, columnStyles, columnHeaderStyles, wb, xlsx,
                writeHeader, openFile);
    }

    /**
     * @see #createXLS(EntityResult, File, String, Hashtable, List, List, List, Workbook, boolean,
     *      boolean)
     */
    @Override
    public void createXLS(EntityResult rs, File output, String sheetName, List<?> columnSort, List<?> columnStyles,
            List<?> columnHeaderStyles, Workbook wb, boolean writeHeader,
            boolean openFile) throws Exception {
        this.createXLS(rs, output, sheetName, new HashMap<>(), columnSort, columnStyles, columnHeaderStyles, wb,
                writeHeader, openFile);
    }

}
