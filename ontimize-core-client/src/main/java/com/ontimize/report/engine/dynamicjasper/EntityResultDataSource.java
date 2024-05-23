package com.ontimize.report.engine.dynamicjasper;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.table.TableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.util.remote.BytesBlock;
import com.ontimize.report.TableSorter;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRRewindableDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperReport;

/**
 * <p>
 * Wrappers a Ontimize {@link EntityResult} into a Jasper data source to allow fill reports without
 * data transformation.
 *
 * @see JRDataSource
 * @see JasperFillManager#fillReport(JasperReport, String, Map, JRDataSource)
 * @see EntityDataSource
 * @see ReportServer#fill(int, Hashtable, Hashtable, EntityResult)
 * @author Imatia Innovation S.L.
 * @since 05/11/2008
 */
public class EntityResultDataSource implements JRDataSource, JRRewindableDataSource {

    private static final Logger logger = LoggerFactory.getLogger(EntityResultDataSource.class);

    protected EntityResult result;

    private int index;

    private final int size;

    public EntityResultDataSource(EntityResult result) {
        this.result = result;

        this.size = result.calculateRecordNumber();
        this.index = -1;
    }

    public EntityResultDataSource(TableModel originalmodel, TableSorter sorter) {
        this.result = this.createEntityResultFromTableSorter(originalmodel, sorter);
        this.size = this.result.calculateRecordNumber();
        this.index = -1;
    }

    public EntityResult createEntityResultFromTableSorter(TableModel model, TableSorter sorter) {
        return sorter.getOrderedEntityResult(model);
    }

    @Override
    public Object getFieldValue(JRField field) throws JRException {
        Object obj = this.result.get(field.getName());
        if ((obj == null) || !(obj instanceof List)) {
            return null;
        }

        List<Object> v = (List<Object>) obj;

        Class<?> fieldClass = field.getValueClass();
        Object value = (this.index >= 0) && (this.index < this.size) ? v.get(this.index) : null;

        if (java.awt.Image.class.equals(fieldClass) && (value instanceof BytesBlock)) {
            Image im = new ImageIcon(((BytesBlock) value).getBytes()).getImage();
            v.set(this.index, im);
            value = im;
        }
        return value;
    }

    @Override
    public boolean next() throws JRException {
        this.index++;
        return this.index < this.size;
    }

    public void reset() {
        this.index = -1;
    }

    @Override
    public void moveFirst() throws JRException {
        this.index = -1;
    }

    public EntityResult getEntityResult() {
        return this.result;
    }

    public JRField[] getFields() {
        return EntityResultDataSource.getFields(this.result);
    }

    public static JRField[] getFields(EntityResult result) {
        Enumeration<?> keys = result.keys();
        List<Object> tmp = new ArrayList<>();

        try {
            while (keys.hasMoreElements()) {
                Object o = keys.nextElement();
                if ((o == null) || !(o instanceof String)) {
                    continue;
                }

                String name = (String) o;
                int type = result.getColumnSQLType(name);
                Class<?> classClass = TypeMappingsUtils.getClass(type);
                String className = TypeMappingsUtils.getClassName(type);

                Map<Object, Object> m = new HashMap<>();
                m.put(CustomField.NAME_KEY, name);
                m.put(CustomField.VALUE_CLASS_NAME_KEY, className);
                m.put(CustomField.VALUE_CLASS_KEY, classClass);

                tmp.add(new CustomField(m));
            }

        } catch (Exception ex) {
            EntityResultDataSource.logger.error(ex.getMessage(), ex);
        }

        // To array
        int s = tmp.size();
        CustomField[] a = new CustomField[s];
        for (int i = 0; i < s; i++) {
            Object o = tmp.get(i);
            if ((o == null) || !(o instanceof CustomField)) {
                continue;
            }
            a[i] = (CustomField) o;
        }
        return a;
    }

}
