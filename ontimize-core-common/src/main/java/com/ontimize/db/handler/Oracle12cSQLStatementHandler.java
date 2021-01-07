package com.ontimize.db.handler;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.db.EntityResultMapImpl;
import com.ontimize.db.SQLStatementBuilder;
import com.ontimize.db.SQLStatementBuilder.SQLStatement;

public class Oracle12cSQLStatementHandler extends DefaultSQLStatementHandler {

    static final Logger logger = LoggerFactory.getLogger(SQLStatementBuilder.class);

    public static final String START_FETCH = " ROWS FETCH NEXT ";

    public static final String END_FETCH = " ROWS ONLY ";

    public static final String OFFSET = " OFFSET ";

    public Oracle12cSQLStatementHandler() {
        super();
        this.setUseAsInSubqueries(false);
    }

    @Override
    public SQLStatement createSelectQuery(String table, Vector requestedColumns, Hashtable conditions, Vector wildcards,
            Vector columnSorting, int recordCount, boolean descending,
            boolean forceDistinct) {
        return super.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount, 0,
                descending, forceDistinct);
    }

    @Override
    public SQLStatement createSelectQuery(String table, Vector requestedColumns, Hashtable conditions, Vector wildcards,
            Vector columnSorting, int recordCount, int offset,
            boolean descending, boolean forceDistinct) {
        StringBuilder sql = new StringBuilder();
        Vector vValues = new Vector();
        if ((columnSorting != null) && !requestedColumns.isEmpty()) {
            for (int i = 0; i < columnSorting.size(); i++) {
                if (!requestedColumns.contains(columnSorting.get(i).toString())) {
                    requestedColumns.add(columnSorting.get(i).toString());
                }
            }
        }
        sql.append(this.createSelectQuery(table, requestedColumns, forceDistinct));

        String cond = this.createQueryConditions(conditions, wildcards, vValues);
        if (cond != null) {
            sql.append(cond);
        }
        if ((columnSorting != null) && (!columnSorting.isEmpty())) {
            String sort = this.createSortStatement(columnSorting, descending);
            sql.append(sort);
        }

        if (offset >= 0) {
            sql.append(Oracle12cSQLStatementHandler.OFFSET);
            sql.append(offset);
        }

        if (recordCount >= 0) {
            sql.append(Oracle12cSQLStatementHandler.START_FETCH);
            sql.append(recordCount);
            sql.append(Oracle12cSQLStatementHandler.END_FETCH);
        }

        Oracle12cSQLStatementHandler.logger.debug(sql.toString());
        return new SQLStatement(sql.toString(), vValues);
    }

    @Override
    public boolean isPageable() {
        return true;
    }

    // getObject(String columnName) no support, call by column index.
    @Override
    public void generatedKeysToEntityResult(ResultSet resultSet, EntityResultMapImpl entityResult, List generatedKeys)
            throws Exception {
        try {
            ResultSetMetaData rsMetaData = resultSet.getMetaData();
            // Optimization: Array access, instead of request the name in each
            // loop
            String[] sColumnNames = this.getColumnNames(rsMetaData);

            // Optimization: use column types.
            int[] columnTypes = new int[sColumnNames.length];
            for (int i = 1; i <= columnTypes.length; i++) {
                columnTypes[i - 1] = rsMetaData.getColumnType(i);
            }

            Hashtable hColumnTypesAux = new Hashtable();
            if (hColumnTypesAux != null) {
                for (int i = 0; i < columnTypes.length; i++) {
                    hColumnTypesAux.put(sColumnNames[i], new Integer(columnTypes[i]));
                }
            }
            entityResult.setColumnSQLTypes(hColumnTypesAux);

            while (resultSet.next()) {
                for (int i = 0; i < sColumnNames.length; i++) {
                    String columnName = sColumnNames[i];
                    Object oValue = resultSet.getObject(i + 1);
                    entityResult.put(columnName, oValue);
                }
            }
            this.changeGenerateKeyNames(entityResult, generatedKeys);
        } catch (Exception e) {
            Oracle12cSQLStatementHandler.logger.error(null, e);
            throw e;
        }
    }

    @Override
    public String addOuterMultilanguageColumnsPageable(String sqlQuery, String table, Hashtable hLocaleTablesAV) {
        Enumeration av = hLocaleTablesAV.keys();
        StringBuilder buffer = new StringBuilder();
        String atPos = "(";
        buffer.append(sqlQuery);
        while (av.hasMoreElements()) {
            Object avActualKey = av.nextElement();
            Object avActualValue = hLocaleTablesAV.get(avActualKey);
            int index = buffer.toString().toLowerCase().indexOf(" from", buffer.toString().indexOf(atPos));
            buffer.insert(index, ", " + table + "." + avActualValue);
        }
        return buffer.toString();
    }

    @Override
    public SQLStatement createLeftJoinSelectQueryPageable(String mainTable, String subquery, String secondaryTable,
            Vector mainKeys, Vector secondaryKeys,
            Vector mainTableRequestedColumns, Vector secondaryTableRequestedColumns, Hashtable mainTableConditions,
            Hashtable secondaryTableConditions, Vector wildcards,
            Vector columnSorting, boolean forceDistinct, boolean descending, int recordNumber, int startIndex) {
        // TODO Auto-generated method stub
        SQLStatement stSQL = super.createLeftJoinSelectQuery(mainTable, subquery, secondaryTable, mainKeys,
                secondaryKeys, mainTableRequestedColumns,
                secondaryTableRequestedColumns, mainTableConditions, secondaryTableConditions, wildcards, columnSorting,
                forceDistinct, descending);

        StringBuilder stSQLString = new StringBuilder(stSQL.getSQLStatement());
        Vector vValues = stSQL.getValues();

        if (startIndex >= 0) {
            stSQLString.append(Oracle12cSQLStatementHandler.OFFSET);
            stSQLString.append(startIndex);
        }

        if (recordNumber >= 0) {
            stSQLString.append(Oracle12cSQLStatementHandler.START_FETCH);
            stSQLString.append(recordNumber);
            stSQLString.append(Oracle12cSQLStatementHandler.END_FETCH);
        }

        Oracle12cSQLStatementHandler.logger.debug(stSQLString.toString());
        return new SQLStatement(stSQLString.toString(), vValues);
    }

    @Override
    public String convertPaginationStatement(String sqlTemplate, int startIndex, int recordNumber) {
        StringBuilder sql = new StringBuilder(sqlTemplate);

        if (startIndex >= 0) {
            sql.append(Oracle12cSQLStatementHandler.OFFSET);
            sql.append(startIndex);
        }

        if (recordNumber >= 0) {
            sql.append(Oracle12cSQLStatementHandler.START_FETCH);
            sql.append(recordNumber);
            sql.append(Oracle12cSQLStatementHandler.END_FETCH);
        }

        return sql.toString();
    }

}
