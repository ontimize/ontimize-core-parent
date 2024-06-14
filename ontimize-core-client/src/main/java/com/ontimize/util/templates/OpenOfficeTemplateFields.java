package com.ontimize.util.templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;

public class OpenOfficeTemplateFields {

	protected List<String>			singleFields	= new ArrayList<>();

	protected EntityResult	tableFields		= new EntityResultMapImpl();

	public OpenOfficeTemplateFields(List<String> fields) {
		if (fields != null) {
			for (int i = 0; i < fields.size(); i++) {
				if (fields.get(i).indexOf(".") < 0) {
					this.singleFields.add(fields.get(i));
				} else {

					String entityName = fields.get(i).substring(0, fields.get(i).indexOf("."));
					String columnName = fields.get(i).substring(fields.get(i).indexOf(".") + 1);
					if (this.tableFields.containsKey(entityName)) {
						List<Object> columns = (List<Object>) this.tableFields.get(entityName);
						columns.add(columnName);
						this.tableFields.put(entityName, columns);
					} else {
						List<Object> columns = new ArrayList<>();
						columns.add(columnName);
						this.tableFields.put(entityName, columns);
					}
				}
			}
		}
	}

	/**
	 * @return the singleFields
	 */
	public List<String> getSingleFields() {
		return this.singleFields;
	}

	public List<Object> getTableNames() {
		List<Object> result = new ArrayList<>();
		Iterator<?> entities = this.tableFields.keySet().iterator();
		while (entities.hasNext()) {
			result.add(entities.next());
		}
		return result;
	}

	public List<Object> getTableFields(String tableName) {
		return (List<Object>) this.tableFields.get(tableName);
	}

	public Map<Object, Object> checkTemplateFieldValues(Map<Object, Object> fieldValues) {
		Map<Object, Object> result = new HashMap<>();
		if (fieldValues != null) {
			result.putAll(fieldValues);
		}
		for (int i = 0; i < this.singleFields.size(); i++) {
			if (!result.containsKey(this.singleFields.get(i))) {
				result.put(this.singleFields.get(i), " - ");
			}
		}

		return result;
	}

	public Map<Object, Object> checkTemplateTableValues(Map<Object, Object> tableValues) {
		Map<Object, Object> result = new HashMap<>();
		if (tableValues != null) {
			result.putAll(tableValues);
		}
		Iterator<?> entities = this.tableFields.keySet().iterator();
		while (entities.hasNext()) {
			String entity = (String) entities.next();
			if (!result.containsKey(entity)) {
				EntityResult resEnt = new EntityResultMapImpl();
				List<?> entityColumns = (List<?>) this.tableFields.get(entity);
				Map<Object, Object> currentReg = new HashMap<>();
				for (int i = 0; i < entityColumns.size(); i++) {
					currentReg.put(entityColumns.get(i), "  ");
				}
				resEnt.addRecord(currentReg);
				result.put(entity, resEnt);
			}
		}
		return result;
	}

}
