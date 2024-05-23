package com.ontimize.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.util.ListUtil;

public class EntityResultTools {

	public static int getValuesKeysIndex(Map<?, ?> entityResult, Map<?, ?> kv) {

		// Check fast
		if (kv.isEmpty()) {
			return -1;
		}
		List<Object> vKeys = new ArrayList<>();
		for (Entry<?, ?> entry : kv.entrySet()) {
			vKeys.add(entry.getKey());
		}
		// Now get the first data vector. Look for all indexes with the
		// specified key
		// and for each one check the other keys
		Object vData = entityResult.get(vKeys.get(0));
		if ((vData == null) || (!(vData instanceof Vector))) {
			return -1;
		}
		int currentValueIndex = -1;

		if (vKeys.size() == 1) {
			return ((List<?>) vData).indexOf(kv.get(vKeys.get(0)));
		}

		// while ((currentValueIndex = ((Vector) vData).indexOf(kv.get(vKeys.get(0)), currentValueIndex + 1)) >= 0) {
		while ((currentValueIndex = ListUtil.indexOf((List<?>) vData, kv.get(vKeys.get(0)), currentValueIndex + 1)) >= 0) {
			boolean allValuesCoincidence = true;
			for (int i = 1; i < vKeys.size(); i++) {
				Object requestValue = kv.get(vKeys.get(i));
				Object vDataAux = entityResult.get(vKeys.get(i));
				if ((vDataAux == null) || (!(vDataAux instanceof Vector))) {
					return -1;
				}
				if (!requestValue.equals(((List<?>) vDataAux).get(currentValueIndex))) {
					allValuesCoincidence = false;
					break;
				}
			}

			if (allValuesCoincidence) {
				return currentValueIndex;
			}
		}
		return -1;
	}

	public static void updateRecordValues(EntityResult entityResult, Map<?, ?> recordValue, int index) {
		for (Entry<?, ?> entry : recordValue.entrySet()) {
			Object currentKey = entry.getKey();
			if (entityResult.containsKey(currentKey)) {
				List<Object> columnRecords = (List<Object>) entityResult.get(currentKey);
				columnRecords.set(index, recordValue.get(currentKey));
			} else {
				List<Object> columnRecords = new ArrayList<>(entityResult.calculateRecordNumber());
				columnRecords.set(index, recordValue.get(currentKey));
				entityResult.put(currentKey, columnRecords);
			}
		}
	}

	/**
	 * Creates an empty <code>EntityResult</code> with structure of columns passed.
	 *
	 * @param columns
	 *            columns of <code>EntityResult</code>
	 * @return an <code>EntityResult</code> with result or null when <code>columns</code> parameter is null
	 */
	public static EntityResult createEmptyEntityResult(List<?> columns) {
		if (columns != null) {
			return new EntityResultMapImpl(columns);
		}
		return null;
	}

	public static Map<Object, Object> entityResultToMap(EntityResult entityResult) {
		if (entityResult instanceof Map) {
			return new HashMap<>((Map<?, ?>) entityResult);
		} else {
			// TODO:
			throw new OntimizeJEERuntimeException("NOT IMPLEMENTED YET");
		}
	}

}
