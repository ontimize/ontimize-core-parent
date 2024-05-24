package com.ontimize.util.templates;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.util.FileUtils;
import com.ontimize.windows.office.ScriptUtilities;

public class WordTemplateGenerator extends AbstractTemplateGenerator {

    private static final Logger logger = LoggerFactory.getLogger(WordTemplateGenerator.class);

    /**
     * Variable used to highlight the bookmarks when a new template is created. If
     * highlightBookmarksInTemplate is true when filling a template then undo the highlight.
     */
    public static boolean highlightBookmarksInTemplate = false;

    /**
     * Variable to use when you want to keep the boorkmarks in the document after write the data in the
     * template. By default, bookmarks are deleted (deleteBookmarks = true)
     */
    public static boolean deleteBookmarks = true;

    protected boolean showTemplate = true;

    @Override
    public File fillDocument(InputStream input, String nameFile, Map<Object, Object> fieldValues, Map<Object, Object> valuesTable,
    		Map<Object, Object> valuesImages, Map<Object, Object> valuesPivotTable)
            throws Exception {

        fieldValues = this.translateDotFields(fieldValues);
        valuesImages = this.translateDotFields(valuesImages);
        File directory = FileUtils.createTempDirectory();
        File template = new File(directory.getAbsolutePath(), FileUtils.getFileName(nameFile));
        FileUtils.copyFile(input, template);

        String fieldDataPath = null;
        if (fieldValues != null) {
            String stringDataField = AbstractTemplateGenerator.transformFieldData(fieldValues, this.dateFormat,
                    this.numberFormat);
            File data = AbstractTemplateGenerator.createFileFieldData(directory, stringDataField);
            if (data != null) {
                fieldDataPath = data.getPath();
            }
        }

        String setupTablePath = null;
        if (valuesTable != null) {
            File data = AbstractTemplateGenerator.createTableDataFile(directory, valuesTable);
            if (data != null) {
                setupTablePath = data.getPath();
            }
        }

        String setupImagesPath = null;
        if (valuesImages != null) {
            File data = AbstractTemplateGenerator.createImageDataFile(directory, valuesImages);
            if (data != null) {
                setupImagesPath = data.getPath();
            }
        }

        String setupPivotTablePath = null;
        if (valuesPivotTable != null) {
            File data = AbstractTemplateGenerator.createTableDataFile(directory, valuesPivotTable,
                    "pivotTableIndex.txt");
            if (data != null) {
                setupPivotTablePath = data.getPath();
            }
        }

        StringBuilder buffer = new StringBuilder();
        if (!this.showTemplate) {
            buffer.append(WordTemplateGenerator.HIDE);
        }

        WordTemplateGenerator.fillTemplateWord(this.scriptFillDocPath, template.getPath(), fieldDataPath,
                setupTablePath, setupImagesPath, setupPivotTablePath, buffer.toString());
        WordTemplateGenerator.removeTemplateResources(template, directory);
        return template;
    }

    /**
     * Remove all the resources in the specified directory excepts the template File
     * @param template Template File that must not be removed
     * @param directory Directory
     */
    private static void removeTemplateResources(File template, File directory) {
        try {
            if (directory.exists()) {
                File[] listfile = directory.listFiles();
                if ((listfile != null) && (listfile.length > 0)) {
                    for (int i = 0, j = listfile.length; i < j; i++) {
                        if (!listfile[i].equals(template)) {
                            listfile[i].delete();
                        }
                    }
                }
            }
        } catch (Exception e) {
            WordTemplateGenerator.logger.error("Error to delete template resources", e);
        }
    }

    @Override
    public List queryTemplateFields(File template) throws Exception {
        return this.queryTemplateFields(template.getCanonicalPath());
    }

    @Override
    public List queryTemplateFields(String template) throws Exception {

        File script = ScriptUtilities.createTemporalFileForScript(this.scriptQueryDocFields);
        Vector parameters = new Vector(2);

        if (AbstractTemplateGenerator.DEBUG) {
            parameters.add("DEBUG;FIELDS;END");
        } else {
            parameters.add("FIELDS;END");
        }

        parameters.add(template);

        String directorioUsuario = System.getProperty("java.io.tmpdir");

        String outputFileName = directorioUsuario + "Fields~" + System.currentTimeMillis() + ".txt";
        parameters.add(outputFileName);
        ScriptUtilities.executeScript("\"" + script.getPath() + "\"", parameters, ScriptUtilities.WSCRIPT);

        if (script.exists()) {
            script.delete();
        }

        File result = new File(outputFileName);
        if (result.exists()) {
            StringBuilder contents = new StringBuilder();
            // use buffering, reading one line at a time
            // FileReader always assumes default encoding is OK!
            BufferedReader input = new BufferedReader(new FileReader(result));
            try {
                String line = null; // not declared within while loop
                /*
                 * readLine is a bit quirky : it returns the content of a line MINUS the newline. it returns null
                 * only for the END of the stream. it returns an empty String if two newlines appear in a row.
                 */
                while ((line = input.readLine()) != null) {
                    contents.append(line);
                    // contents.append(System.getProperty("line.separator"));
                }
            } finally {
                input.close();
            }
            result.delete();
            Vector fieldNames = ApplicationManager.getTokensAt(contents.toString(), "#");
            return fieldNames;
        }
        return null;
    }

    /*
     * Script input parameters are: 'setup': String with the type of the operations to execute(Options:
     * NOTHING,FIELDS,TABLES,IMAGES) separated by ;
     */
    private static final String NOTHING = "NOTHING";

    private static final String FIELDS = "FIELDS";

    private static final String FIELDSWITHOUTLABEL = "FIELDSWITHOUTLABEL";

    private static final String TABLES = "TABLES";

    private static final String PIVOTTABLES = "PIVOTTABLES";

    private static final String IMAGES = "IMAGES";

    private static final String DEBUG = "DEBUG";

    private static final String HIDE = "HIDE";

    private static final String HIGHLIGHT = "HIGHLIGHT";

    private static final String FIELDSUNDOHIGHLIGHT = "FIELDSUNDOHIGHLIGHT";

    private static final String KEEPBOOKMARKS = "KEEPBOOKMARKS";

    private String scriptFillDocPath = "com/ontimize/util/templates/script/fillDoc.vbs";

    private final String scriptQueryDocFields = "com/ontimize/util/templates/script/queryDocFields.vbs";

    private static String scriptCreateDocTemplate = "com/ontimize/util/templates/script/createDoc.vbs";

    /**
     * Sets the file path for the fill document script.<br>
     * Default value is "com/ontimize/util/templates/script/fillDoc.vbs".
     * @param path
     */
    public void setScriptFillDocPath(String path) {
        this.scriptFillDocPath = path;
    }

    private static void fillTemplateWord(String scriptPath, String template, String pathFieldData, String pathTableData,
            String pathImageD, String pathPivotTableData,
            String options) throws Exception {

        File script = ScriptUtilities.createTemporalFileForScript(scriptPath);
        Vector parameters = new Vector(2);

        StringBuilder bufferSetup = new StringBuilder();
        if (AbstractTemplateGenerator.DEBUG) {
            bufferSetup.append(WordTemplateGenerator.DEBUG).append(";");
        }

        if (!WordTemplateGenerator.deleteBookmarks) {
            bufferSetup.append(WordTemplateGenerator.KEEPBOOKMARKS).append(";");
        }

        if ((pathFieldData == null) && (pathTableData == null) && (pathImageD == null)
                && (pathPivotTableData == null)) {
            bufferSetup.append(WordTemplateGenerator.NOTHING);
        } else {
            if (pathFieldData != null) {
                if (WordTemplateGenerator.highlightBookmarksInTemplate) {
                    bufferSetup.append(WordTemplateGenerator.FIELDSUNDOHIGHLIGHT).append(";");
                } else {
                    bufferSetup.append(WordTemplateGenerator.FIELDS).append(";");
                }
            } else {
                pathFieldData = "EMPTY";
            }
            if (pathTableData != null) {
                bufferSetup.append(WordTemplateGenerator.TABLES).append(";");
            } else {
                pathTableData = "EMPTY";
            }
            if (pathImageD != null) {
                bufferSetup.append(WordTemplateGenerator.IMAGES).append(";");
            } else {
                pathImageD = "EMPTY";
            }

            if (pathPivotTableData != null) {
                bufferSetup.append(WordTemplateGenerator.PIVOTTABLES).append(";");
            } else {
                pathPivotTableData = "EMPTY";
            }

        }

        if ((options != null) && (options.length() > 0)) {
            bufferSetup.append(options).append(";");
        }

        bufferSetup.append("END");
        parameters.add(bufferSetup.toString());
        parameters.add(template);
        parameters.add(pathFieldData);
        parameters.add(pathTableData);
        parameters.add(pathImageD);
        parameters.add(pathPivotTableData);

        ScriptUtilities.executeScript("\"" + script.getPath() + "\"", parameters, ScriptUtilities.WSCRIPT);
        if (script.exists()) {
            script.delete();
        }
    }

    @Override
    public void setShowTemplate(boolean show) {
        this.showTemplate = show;
    }

    /**
     * The {@link WordTemplateGenerator} adds the possibility to put the fields in the template in a
     * specified order.<br>
     * The parameter <code>fieldValues</code> can be a {@link EntityResult} object, and use the
     * {@link EntityResult#setColumnOrder(List)} method to specified the order of the fields in the
     * template.<br>
     * <br>
     * It is possible to create groups with some fields and put a title for the group in the
     * template.<br>
     * If some value in the <code>fieldValues</code> parameter is a Map (or EntityResult) then the
     * fields in this hashtable will be in a group in the template and the title will be the key of this
     * hashtable in the parameter <code>fieldValues</code>
     */
    @Override
    public File createTemplate(Map<Object, Object> fieldValues, Map<Object, Object> valuesTable, Map<Object, Object> valuesImages) {
        try {
            fieldValues = this.translateDotFields(fieldValues);
            valuesImages = this.translateDotFields(valuesImages);

            if (valuesImages != null) {
                if (fieldValues instanceof EntityResult) {
                    List orderColumns = ((EntityResult) fieldValues).getOrderColumns();
                    if ((orderColumns != null) && (orderColumns.size() > 0)) {
                        Enumeration imageKeys = valuesImages.keys();
                        while (imageKeys.hasMoreElements()) {
                            Object key = imageKeys.nextElement();
                            orderColumns.add(key);
                            fieldValues.put(key, valuesImages.get(key));
                        }
                        ((EntityResult) fieldValues).setColumnOrder(orderColumns);
                    }
                } else {
                    fieldValues.putAll(valuesImages);
                }
            }
            File directory = FileUtils.createTempDirectory();
            File template = new File(directory.getAbsolutePath(),
                    FileUtils.getFileName("template_" + System.currentTimeMillis() + ".doc"));
            if (template.createNewFile()) {

                String fieldDataPath = null;
                if (fieldValues != null) {
                    String stringDataField = WordTemplateGenerator.createFieldDataString(fieldValues, "$#",
                            this.dateFormat);
                    File data = AbstractTemplateGenerator.createFileFieldData(directory, stringDataField);
                    if (data != null) {
                        fieldDataPath = data.getPath();
                    }
                }

                String setupTablePath = null;
                if (valuesTable != null) {
                    File data = AbstractTemplateGenerator.createTableDataDefinition(directory, valuesTable,
                            this.dateFormat);
                    if (data != null) {
                        setupTablePath = data.getPath();
                    }
                }

                String setupImagesPath = null;

                StringBuilder options = new StringBuilder();
                if (!this.showTemplate) {
                    options.append(WordTemplateGenerator.HIDE);
                    options.append(";");
                }

                if (WordTemplateGenerator.highlightBookmarksInTemplate) {
                    options.append(WordTemplateGenerator.HIGHLIGHT);
                    options.append(";");
                }

                WordTemplateGenerator.generateWordTemplate(template.getPath(), fieldDataPath, setupTablePath,
                        setupImagesPath, options.toString());
                return template;
            }

        } catch (Exception e) {
            WordTemplateGenerator.logger.error(null, e);
        }
        return null;
    }

    /**
     * Change dot character "." on keys with "ç" character
     * @param values
     * @return
     */
    private Map<Object, Object> translateDotFields(Map<Object, Object> values) {
        try {
        	Map<Object, Object> translations = new HashMap<>();
            if (values == null) {
                return translations;
            }
            Iterator<?> valuesit = values.keySet().iterator();
            while (valuesit.hasNext()) {
                Object o = valuesit.next();
                if ((o != null) && (o instanceof String)) {
                    String str = o.toString();
                    if (str.indexOf(".") > -1) {
                        String stralt = str.replaceAll("\\.", "ç");
                        translations.put(str, stralt);
                    }
                }
            }
            Iterator transit = translations.keySet().iterator();
            while (transit.hasNext()) {
                String val = transit.next().toString();
                values.put(translations.get(val), values.remove(val));
            }
            if (values instanceof EntityResult) {
                ((EntityResult) values).setColumnOrder(new ArrayList<>(translations.values()));
            }
        } catch (Exception exc) {
            WordTemplateGenerator.logger.error(null, exc);
        }
        return values;
    }

    private static void generateWordTemplate(String template, String pathFieldData, String pathTableData,
            String pathImageD, String options) throws Exception {

        File script = ScriptUtilities.createTemporalFileForScript(WordTemplateGenerator.scriptCreateDocTemplate);
        List<Object> parameters = new ArrayList<>(2);

        StringBuilder bufferSetup = new StringBuilder();
        if (AbstractTemplateGenerator.DEBUG) {
            bufferSetup.append(WordTemplateGenerator.DEBUG).append(";");
        }

        if ((pathFieldData == null) && (pathTableData == null) && (pathImageD == null)) {
            bufferSetup.append(WordTemplateGenerator.NOTHING);
        } else {
            if (pathFieldData != null) {
                if (AbstractTemplateGenerator.createLabelsInTemplate) {
                    bufferSetup.append(WordTemplateGenerator.FIELDS).append(";");
                } else {
                    bufferSetup.append(WordTemplateGenerator.FIELDSWITHOUTLABEL).append(";");
                }
            } else {
                pathFieldData = "EMPTY";
            }
            if (pathTableData != null) {
                bufferSetup.append(WordTemplateGenerator.TABLES).append(";");
            } else {
                pathTableData = "EMPTY";
            }
            if (pathImageD != null) {
                bufferSetup.append(WordTemplateGenerator.IMAGES).append(";");
            } else {
                pathImageD = "EMPTY";
            }
        }

        if ((options != null) && (options.length() > 0)) {
            bufferSetup.append(options);
        }

        bufferSetup.append("END");
        parameters.add(bufferSetup.toString());
        parameters.add(template);
        parameters.add(pathFieldData);
        parameters.add(pathTableData);
        parameters.add(pathImageD);
        ScriptUtilities.executeScript("\"" + script.getPath() + "\"", parameters, ScriptUtilities.WSCRIPT);
        if (script.exists()) {
            script.delete();
        }
    }

    protected static String createFieldDataString(Map<Object, Object> data, String delimiter, DateFormat df) {

        // format : key1 + delimiter + value1 + delimiter + key2 + delimiter +
        // value2 + delimiter + ... + keyN + delimiter + valueN
        if (data == null) {
            throw new IllegalArgumentException("exportFieldData: data is null");
        }

        // If some value in data is a Hashtable then it is a group to create in
        // the template and the key is the title
        boolean fieldsInGroups = AbstractTemplateGenerator.containsHashtableValue(data);

        if (fieldsInGroups) {
            return WordTemplateGenerator.createFieldsInGroups(data, delimiter, "%#", df);
        } else {
            Object[] keys = AbstractTemplateGenerator.getKeysOrder(data);
            StringBuilder res = new StringBuilder();
            for (int i = 0; i < keys.length; i++) {
                Object key = keys[i];
                Object value = data.get(key);
                res.append(key);
                res.append(delimiter);

                if (value instanceof List) {
                    if (((List<?>) value).get(0) != null) {
                        Object currentData = ((Vector) value).get(0);
                        if ((df != null) && (currentData instanceof Date)) {
                            res.append(df.format(currentData));
                        } else {
                            res.append(currentData.toString());
                        }

                    } else {
                        res.append(" ");
                    }
                } else {
                    if ((df != null) && (value instanceof Date)) {
                        res.append(df.format(value));
                    } else {
                        res.append(value.toString());
                    }
                }

                if (i < (keys.length - 1)) {
                    res.append(delimiter);
                }
            }
            String stringResult = res.toString();
            return stringResult;
        }
    }

    /**
     * Create an String with the information of the fields to put in the template.<br>
     * The string has the following format: Group1Title groupDelimiter Group1Fields Group2Title
     * groupDelimiter ... <br>
     * where the group fields have the format: attr1$#label1$#attr2$#label2$#attr3$#label3...<br>
     * Example: Group3%#identification$#DNI/CIF$#country$#Country%#Group7%#place$
     * #Place$#subject$#Subject
     * @param data
     * @param delimiter
     * @param groupDelimiter
     * @param df
     * @return
     */
    protected static String createFieldsInGroups(Map<Object, Object> data, String delimiter, String groupDelimiter,
            DateFormat df) {

        Object[] keys = AbstractTemplateGenerator.getKeysOrder(data);
        StringBuilder res = new StringBuilder();

        // If one group exist then all fields must be in a group

        EntityResult noGroupFields = new EntityResultMapImpl();
        List<Object> noGroupFieldsKeys = new ArrayList<>();

        // Search for fields out of any group and group all of them
        for (int i = 0; i < keys.length; i++) {
            if (!(data.get(keys[i]) instanceof Map)) {
                noGroupFieldsKeys.add(keys[i]);
                noGroupFields.put(keys[i], data.get(keys[i]));
                data.remove(keys[i]);
            }
        }

        if (!noGroupFields.isEmpty()) {
            noGroupFields.setColumnOrder(noGroupFieldsKeys);
            data.put("    ", noGroupFields);
            keys = AbstractTemplateGenerator.getKeysOrder(data);
        }

        for (int i = 0; i < keys.length; i++) {
            Object key = keys[i];
            Object value = data.get(key);
            res.append(key);
            res.append(groupDelimiter);
            WordTemplateGenerator.addFieldGroupToDataString((Hashtable) value, res, df, delimiter);
            if (i < (keys.length - 1)) {
                res.append(groupDelimiter);
            }
        }
        String stringResult = res.toString();
        return stringResult;
    }

    protected static void addFieldGroupToDataString(Map<Object, Object> group, StringBuilder buffer, DateFormat df,
            String delimiter) {
        Object[] keys = AbstractTemplateGenerator.getKeysOrder(group);

        for (int i = 0; i < keys.length; i++) {

            buffer.append(keys[i]);
            buffer.append(delimiter);

            Object value = group.get(keys[i]);
            if (value instanceof List) {
                if (((List<?>) value).get(0) != null) {
                    Object currentData = ((List<?>) value).get(0);
                    if ((df != null) && (currentData instanceof Date)) {
                        buffer.append(df.format(currentData));
                    } else {
                        buffer.append(currentData.toString());
                    }

                } else {
                    buffer.append(" ");
                }
            } else {
                if ((df != null) && (value instanceof Date)) {
                    buffer.append(df.format(value));
                } else {
                    buffer.append(value.toString());
                }
            }
            if (i < (keys.length - 1)) {
                buffer.append(delimiter);
            }
        }
    }

}
