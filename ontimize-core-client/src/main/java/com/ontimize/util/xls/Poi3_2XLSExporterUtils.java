package com.ontimize.util.xls;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.windows.office.WindowsUtils;

/**
 * @author Imatia Innovation SL
 * @since 5.2057EN-1.3
 * @deprecated Since 5.2080EN. Use Poi3_5XLSExporterUtils. See {@link XLSExporterFactory}
 */
@Deprecated
public class Poi3_2XLSExporterUtils extends AbstractXLSExporter implements XLSExporter {

    private static final Logger logger = LoggerFactory.getLogger(Poi3_2XLSExporterUtils.class);

    public static DecimalFormatSymbols dfs = new DecimalFormatSymbols();

    public static String numericPattern = "#,##0";

    public static String decimalPattern = "#,#0.#";

    public static DecimalFormat numericFormat = new DecimalFormat();

    public static DecimalFormat decimalFormat = new DecimalFormat();

    public static String dateFormat = "dd/MM/yyyy";

    public static SimpleDateFormat sdf = new SimpleDateFormat(Poi3_2XLSExporterUtils.dateFormat);

    public static String dateHourFormat = "dd/MM/yyyy HH:mm";

    public static SimpleDateFormat sdfHour = new SimpleDateFormat(Poi3_2XLSExporterUtils.dateHourFormat);

    protected static Workbook wb;

    public Poi3_2XLSExporterUtils() {
    }

    @Override
    public void createXLS(EntityResult rs, File output, String sheetName, Hashtable hColumnRenderers, List columnSort,
            boolean writeHeader, boolean openFile) throws Exception {
        OutputStream os = new FileOutputStream(output);
        try {
            this.createXLS(rs, os, sheetName, hColumnRenderers, columnSort, writeHeader);
            if (openFile) {
                WindowsUtils.openFile(output);
            }
        } catch (Exception e) {
            Poi3_2XLSExporterUtils.logger.error(null, e);
        } finally {
            os.close();
        }
    }

    public void createXLS(List entityResultsList, OutputStream os, List sheetNames, Hashtable hColumnRenderers,
            List columnSort, boolean writeHeader) throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        sheetNames = this.checkSheetNames(entityResultsList.size(), sheetNames);
        for (int i = 0; i < entityResultsList.size(); i++) {
            this.writeSheet(wb, (EntityResult) entityResultsList.get(i), columnSort, (String) sheetNames.get(i),
                    hColumnRenderers, writeHeader);
        }
        wb.write(os);
    }

    public void createXLS(EntityResult rs, OutputStream os, String sheetName, Hashtable hColumnRenderers,
            List columnSort, boolean writeHeader) throws IOException {
        Poi3_2XLSExporterUtils.wb = new HSSFWorkbook();
        sheetName = sheetName == null ? "Sheet1" : sheetName;
        this.writeSheet(Poi3_2XLSExporterUtils.wb, rs, columnSort, sheetName, hColumnRenderers, writeHeader);
        Poi3_2XLSExporterUtils.wb.write(os);
    }

    protected List checkSheetNames(int size, List sheetNames) {
        if ((sheetNames != null) && (sheetNames.size() == size)) {
            return sheetNames;
        }
        List res = new Vector(size);
        for (int i = 0; i < size; i++) {
            res.add("Sheet" + i);
        }
        return res;
    }

    protected void writeSheet(Workbook wb, EntityResult rs, List order, String sheetName, Hashtable hColumnRenderers,
            boolean writeHeader) {
        Poi3_2XLSExporterUtils.dfs = new DecimalFormatSymbols(ApplicationManager.getLocale());
        Poi3_2XLSExporterUtils.decimalFormat.setDecimalFormatSymbols(Poi3_2XLSExporterUtils.dfs);
        Poi3_2XLSExporterUtils.decimalFormat.applyPattern(Poi3_2XLSExporterUtils.decimalPattern);
        Poi3_2XLSExporterUtils.numericFormat.setDecimalFormatSymbols(Poi3_2XLSExporterUtils.dfs);
        Poi3_2XLSExporterUtils.numericFormat.applyPattern(Poi3_2XLSExporterUtils.numericPattern);
        if (order == null) {
            order = rs.getOrderColumns();
        }
        if (order == null) {
            order = new Vector(rs.keySet());
        }

        HSSFSheet sheet = ((HSSFWorkbook) wb).createSheet(sheetName);
        if (writeHeader) {
            this.writeLine(sheet, rs.getOrderColumns(), order, hColumnRenderers, rs.getColumnSQLTypes());
        }
        for (int count = rs.calculateRecordNumber(), i = 0; i < count; i++) {
            Hashtable h = rs.getRecordValues(i);
            Vector values = new Vector(order.size());
            for (Iterator it = rs.getOrderColumns().iterator(); it.hasNext();) {
                Object key = it.next();
                values.add(h.get(key));
            }
            this.writeLine(sheet, values, order, hColumnRenderers, rs.getColumnSQLTypes());
        }
    }

    protected void writeSheet(Workbook wb, EntityResult rs, List order, List columnStyles, List columnHeaderStyles,
            String sheetName, Hashtable hColumnRenderers,
            boolean writeHeader) {
        Poi3_2XLSExporterUtils.dfs = new DecimalFormatSymbols(ApplicationManager.getLocale());
        Poi3_2XLSExporterUtils.decimalFormat.setDecimalFormatSymbols(Poi3_2XLSExporterUtils.dfs);
        Poi3_2XLSExporterUtils.decimalFormat.applyLocalizedPattern(Poi3_2XLSExporterUtils.decimalPattern);
        Poi3_2XLSExporterUtils.numericFormat.setDecimalFormatSymbols(Poi3_2XLSExporterUtils.dfs);
        Poi3_2XLSExporterUtils.numericFormat.applyLocalizedPattern(Poi3_2XLSExporterUtils.numericPattern);
        if (order == null) {
            order = rs.getOrderColumns();
        }
        if (order == null) {
            order = new Vector(rs.keySet());
        }

        HSSFSheet sheet = ((HSSFWorkbook) wb).createSheet(sheetName);
        if (writeHeader) {
            this.writeLine(sheet, order, order, hColumnRenderers, columnHeaderStyles, rs.getColumnSQLTypes());
        }
        for (int count = rs.calculateRecordNumber(), i = 0; i < count; i++) {
            Hashtable h = rs.getRecordValues(i);
            Vector values = new Vector(order.size());
            for (Iterator it = order.iterator(); it.hasNext();) {
                Object key = it.next();
                values.add(h.get(key));
            }
            this.writeLine(sheet, values, order, hColumnRenderers, columnStyles, rs.getColumnSQLTypes());
        }
    }

    protected void writeLine(HSSFSheet sheet, List values, List orderColumns, Hashtable hColumnRenderers,
            List columnStyles, Hashtable hColumnTypes) {
        HSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
        int column = 0;
        for (int i = 0; i < values.size(); i++) {
            Object ob = values.get(i);
            HSSFCell cell = null;
            try {
                cell = row.createCell((short) column++);
                CellStyle cs_style = columnStyles != null ? (CellStyle) columnStyles.get(i)
                        : Poi3_2XLSExporterUtils.wb.createCellStyle();
                cell.setCellStyle(cs_style);
            } catch (Exception e1) {
                Poi3_2XLSExporterUtils.logger.error(null, e1);
            }
            switch (this.getCellType(orderColumns.get(i).toString(), ob, hColumnRenderers, hColumnTypes)) {
                case DECIMAL_CELL:
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
                                Number number = Poi3_2XLSExporterUtils.decimalFormat.parse(ob.toString());
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
                                Poi3_2XLSExporterUtils.logger.trace(null, e);
                                cell.setCellType(Cell.CELL_TYPE_STRING);
                                cell.setCellValue(ob.toString());
                            }
                        }

                    }
                    break;
                case NUMERIC_CELL:
                    if (ob != null) {
                        if (ob instanceof Number) {
                            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                            cell.setCellValue(((Number) ob).intValue());
                        } else {
                            try {
                                int value = Poi3_2XLSExporterUtils.numericFormat.parse(ob.toString()).intValue();
                                cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                                cell.setCellValue(value);
                            } catch (Exception e) {
                                Poi3_2XLSExporterUtils.logger.trace(null, e);
                                cell.setCellType(Cell.CELL_TYPE_STRING);
                                cell.setCellValue(ob.toString());
                            }
                        }

                    }
                    break;
                case DATE_CELL:
                    if (ob != null) {
                        if (ob instanceof Date) {
                            cell.setCellValue((Date) ob);
                            HSSFCellStyle cs_date = cell.getCellStyle();
                            cs_date.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));
                            cell.setCellStyle(cs_date);
                        } else {
                            try {
                                cell.setCellValue(Poi3_2XLSExporterUtils.sdf.parse(ob.toString()));
                                HSSFCellStyle cs_date = cell.getCellStyle();
                                cs_date.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));
                                cell.setCellStyle(cs_date);
                            } catch (ParseException e) {
                                Poi3_2XLSExporterUtils.logger.trace(null, e);
                                cell.setCellType(Cell.CELL_TYPE_STRING);
                                cell.setCellValue(String.valueOf(ob));
                            }
                        }
                    }

                    break;

                case DATE_HOUR_CELL:
                    if (ob != null) {

                        if (ob instanceof Date) {
                            cell.setCellValue((Date) ob);
                            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                            HSSFCellStyle cs_date_hour = cell.getCellStyle();
                            cs_date_hour.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy h:mm"));
                            cell.setCellStyle(cs_date_hour);
                        } else {
                            try {
                                cell.setCellValue(Poi3_2XLSExporterUtils.sdfHour.parse(ob.toString()));
                                cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                                HSSFCellStyle cs_date_hour = cell.getCellStyle();
                                cs_date_hour.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy h:mm"));
                                cell.setCellStyle(cs_date_hour);
                            } catch (ParseException e) {
                                Poi3_2XLSExporterUtils.logger.trace(null, e);
                                cell.setCellType(Cell.CELL_TYPE_STRING);
                                cell.setCellValue(String.valueOf(ob));
                            }
                        }
                    }

                    break;
                case CURRENCY_CELL:
                    ParsePosition pp = new ParsePosition(0);
                    cell.setCellType(Cell.CELL_TYPE_NUMERIC);

                    if (ob != null) {

                        String cellValue = ((String) ob).replaceAll(this.getCurrencySymbol((String) ob), "").trim();
                        Number number = Poi3_2XLSExporterUtils.decimalFormat.parse(String.valueOf(cellValue).trim(),
                                pp);
                        // cellValue = cellValue.replaceAll(",", "");
                        if (number != null) {
                            HSSFCellStyle cs_currency = cell.getCellStyle();
                            cs_currency.setDataFormat(
                                    Poi3_2XLSExporterUtils.wb.createDataFormat()
                                        .getFormat(AbstractXLSExporter.currencyPattern
                                                + this.getCurrencySymbol((String) ob)));
                            cell.setCellStyle(cs_currency);
                            cell.setCellValue(number.doubleValue());
                        }
                    }
                    break;
                case TEXT_CELL:
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    cell.setCellValue(String.valueOf(ob));
                    break;
                default:
                    try {
                        ParsePosition parsePos = new ParsePosition(0);
                        Number number = Poi3_2XLSExporterUtils.decimalFormat.parse(String.valueOf(ob).trim(), parsePos);
                        if (parsePos.getIndex() < String.valueOf(ob).trim().length()) {
                            throw new Exception("Parse not complete");
                        }
                        cell.setCellValue(number.doubleValue());
                    } catch (Exception e) {
                        Poi3_2XLSExporterUtils.logger.trace(null, e);
                        cell.setCellType(Cell.CELL_TYPE_STRING);
                        cell.setCellValue(String.valueOf(ob));
                    }
                    break;
            }
        }
    }

    protected void writeLine(HSSFSheet sheet, List values, List order, Hashtable hColumnRenderers,
            Hashtable hColumnTypes) {
        this.writeLine(sheet, values, order, hColumnRenderers, null, hColumnTypes);
    }

    public void createXLS(EntityResult rs, OutputStream os, String sheetName, Hashtable hColumnRenderers,
            List columnSort, List columnStyles, List columnHeaderStyles, Workbook wb,
            boolean writeHeader) throws Exception {
        if (wb == null) {
            wb = new HSSFWorkbook();
        }
        sheetName = sheetName == null ? "Sheet1" : sheetName;
        this.writeSheet(wb, rs, columnSort, columnStyles, columnHeaderStyles, sheetName, hColumnRenderers, writeHeader);
        wb.write(os);
    }

    @Override
    public void createXLS(EntityResult rs, File output, String sheetName, Hashtable hColumnRenderers, List columnSort,
            List columnStyles, List columnHeaderStyles, Workbook wb,
            boolean writeHeader, boolean openFile) throws Exception {
        OutputStream os = new FileOutputStream(output);
        try {
            this.createXLS(rs, os, sheetName, hColumnRenderers, columnSort, columnStyles, columnHeaderStyles, wb,
                    writeHeader);
            if (openFile) {
                WindowsUtils.openFile(output);
            }
        } catch (Exception e) {
            Poi3_2XLSExporterUtils.logger.error(null, e);
        } finally {
            os.close();
        }
    }

    @Override
    public void createXLS(EntityResult rs, File output, String sheetName, Hashtable hColumnRenderers, List columnSort,
            boolean writeHeader, boolean xlsx, boolean openFile)
            throws Exception {
        this.createXLS(rs, output, sheetName, hColumnRenderers, columnSort, writeHeader, openFile);
    }

    @Override
    public void createXLS(EntityResult rs, File output, String sheetName, Hashtable hColumnRenderers, List columnSort,
            List columnStyles, List columnHeaderStyles, Workbook wb,
            boolean writeHeader, boolean xlsx, boolean openFile) throws Exception {
        this.createXLS(rs, output, sheetName, hColumnRenderers, columnSort, columnStyles, columnHeaderStyles, wb,
                writeHeader, openFile);
    }

    @Override
    public void createXLS(EntityResult rs, File output, String sheetName, List columnSort, boolean writeHeader,
            boolean openFile) throws Exception {
        this.createXLS(rs, output, sheetName, new Hashtable(), columnSort, writeHeader, openFile);
    }

    @Override
    public void createXLS(EntityResult rs, File output, String sheetName, List columnSort, boolean writeHeader,
            boolean xlsx, boolean openFile) throws Exception {
        this.createXLS(rs, output, sheetName, new Hashtable(), columnSort, writeHeader, openFile);
    }

    @Override
    public void createXLS(EntityResult rs, File output, String sheetName, List columnSort, List columnStyles,
            List columnHeaderStyles, Workbook wb, boolean writeHeader,
            boolean xlsx, boolean openFile) throws Exception {
        this.createXLS(rs, output, sheetName, new Hashtable(), columnSort, columnStyles, columnHeaderStyles, wb, xlsx,
                writeHeader, openFile);
    }

    @Override
    public void createXLS(EntityResult rs, File output, String sheetName, List columnSort, List columnStyles,
            List columnHeaderStyles, Workbook wb, boolean writeHeader,
            boolean openFile) throws Exception {
        this.createXLS(rs, output, sheetName, new Hashtable(), columnSort, columnStyles, columnHeaderStyles, wb,
                writeHeader, openFile);
    }

}
