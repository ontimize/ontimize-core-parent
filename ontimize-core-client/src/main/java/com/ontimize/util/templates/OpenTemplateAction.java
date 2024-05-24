package com.ontimize.util.templates;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.Form;

/*
 * Class to implements the action to open a template with the form data. 'fieldData' : List with all
 * the fields to get the data separated by ;. 'imageData' : Attributes of the images to use in the
 * templates separated by ;. 'tableData' : Table data used to create the template separated by ;.
 * 'templatePath' : Template path 'generator' : Manager to use in the template creation process,
 * default value is 'word'.
 */

public class OpenTemplateAction extends AbstractAction {

    private static final Logger logger = LoggerFactory.getLogger(OpenTemplateAction.class);

    public static final String FIELD_DATA = "fieldData";

    public static final String IMAGE_DATA = "imageData";

    public static final String TABLE_DATA = "tableData";

    public static final String TEMPLATE_PATH = "templatePath";

    public static final String GENERATOR = "generator";

    protected Form currentForm = null;

    protected List<Object> fieldDataList;

    protected List<Object> imageDataList;

    protected List<Object> tableDataList;

    protected String templateURL;

    public OpenTemplateAction(Form f, Map<Object, Object> parameters) {
        this.currentForm = f;
        this.init(parameters);
    }

    public OpenTemplateAction(Form f, Map<Object, Object> parameters, String name) {
        super(name);
        this.currentForm = f;
        this.init(parameters);
    }

    public OpenTemplateAction(Form f, Map<Object, Object> parameters, String name, Icon icon) {
        super(name, icon);
        this.currentForm = f;
        this.init(parameters);
    }

    protected void init(Map<Object, Object> parameters) {
        if (parameters.containsKey(OpenTemplateAction.TEMPLATE_PATH)) {
            this.templateURL = (String) parameters.get(OpenTemplateAction.TEMPLATE_PATH);
        }

        if (parameters.containsKey(OpenTemplateAction.FIELD_DATA)) {
            this.fieldDataList = new ArrayList<>();
            StringTokenizer temp = new StringTokenizer((String) parameters.get(OpenTemplateAction.FIELD_DATA), ";");
            while (temp.hasMoreTokens()) {
                this.fieldDataList.add(temp.nextToken());
            }
        }

        if (parameters.containsKey(OpenTemplateAction.IMAGE_DATA)) {
            this.imageDataList = new ArrayList<>();
            StringTokenizer temp = new StringTokenizer((String) parameters.get(OpenTemplateAction.IMAGE_DATA), ";");
            while (temp.hasMoreTokens()) {
                this.imageDataList.add(temp.nextToken());
            }
        }

        if (parameters.containsKey(OpenTemplateAction.TABLE_DATA)) {
            this.tableDataList = new ArrayList<>();
            StringTokenizer temp = new StringTokenizer((String) parameters.get(OpenTemplateAction.TABLE_DATA), ";");
            while (temp.hasMoreTokens()) {
                this.tableDataList.add(temp.nextToken());
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        try {
            TemplateGenerator generator = this.getTemplateGenerator();
            generator.fillDocument(this.templateURL, this.createFieldData(), this.createTableData(),
                    this.createImageData());
        } catch (Exception ex) {
            OpenTemplateAction.logger.error(null, ex);
        }
    }

    protected TemplateGenerator getTemplateGenerator() {
        return TemplateGeneratorFactory.templateGeneratorInstance(TemplateGeneratorFactory.WORD);
    }

    protected Map<Object, Object> createFieldData() {
    	Map<Object, Object> data = new HashMap<>();
        if ((this.fieldDataList == null) || (this.fieldDataList.size() == 0)) {
            if (AbstractTemplateGenerator.DEBUG) {
                OpenTemplateAction.logger.debug("Warning " + this.getClass() + ":" + OpenTemplateAction.FIELD_DATA
                        + " doesn't set parameter value. All form fields be used");
            }
            data = this.currentForm.getDataFieldText();
        } else {
            data = this.currentForm.getDataFieldText(this.fieldDataList);
        }

        if (data.isEmpty()) {
            return data;
        }
        for(Entry<Object, Object> entry:data.entrySet()) {
        	List<Object> list = new ArrayList<>();
        	list.add(entry.getValue());
        	data.put(entry.getKey(), list);
        }
        return data;
    }

    protected Map<Object, Object> createTableData() {
        return new HashMap<>();
    }

    protected Map<Object, Object> createImageData() {
        return new HashMap<>();
    }

}
