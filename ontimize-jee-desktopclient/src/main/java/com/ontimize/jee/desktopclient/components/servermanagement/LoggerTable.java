package com.ontimize.jee.desktopclient.components.servermanagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.table.ExtendedTableModel;
import com.ontimize.gui.table.Table;
import com.ontimize.jee.common.services.servermanagement.IServerManagementService;
import com.ontimize.jee.common.services.servermanagement.IServerManagementService.OntimizeJEELogger;
import com.ontimize.jee.common.util.logging.Level;
import com.ontimize.jee.desktopclient.spring.BeansFactory;

public class LoggerTable extends Table {

    private static final Logger logger = LoggerFactory.getLogger(LoggerTable.class);

    public LoggerTable(Map<Object, Object> params) throws Exception {
        super(params);
    }

    @Override
    protected ExtendedTableModel createExtendedTableModel() {
        return new LoggerExtendedTableModel(new HashMap<>(0), new ArrayList<>(this.attributes), this.calculedColumns, true);
    }

    public class LoggerExtendedTableModel extends ExtendedTableModel {

        public LoggerExtendedTableModel(Map<Object, Object> map, List<Object> columns,
                Map<Object, Object> map2, boolean editable) {
            super(map, columns, map2, editable, new ArrayList<>());
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            OntimizeJEELogger oJEELogger = (OntimizeJEELogger) super.getValueAt(rowIndex,
                    this.getColumnIndex("OntimizeLogger"));
            Level level = oJEELogger.getLoggerLevel();
            Object columnName = this.columnTexts.get(columnIndex);
            if ("Logger".equals(columnName)) {
                return oJEELogger.getLoggerName();
            } else if ("Level".equals(columnName)) {
                return level;
            } else if ("Trace".equals(columnName)) {
                return Level.TRACE.equals(level);
            } else if ("Debug".equals(columnName)) {
                return Level.DEBUG.equals(level);
            } else if ("Info".equals(columnName)) {
                return Level.INFO.equals(level);
            } else if ("Warn".equals(columnName)) {
                return Level.WARN.equals(level);
            } else if ("Error".equals(columnName)) {
                return Level.ERROR.equals(level);
            } else if ("Off".equals(columnName)) {
                return Level.OFF.equals(level);
            } else if ("Inherit".equals(columnName)) {
                return null;
            }
            return null;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if ("Level".equals(this.columnTexts.get(columnIndex))) {
                return Level.class;
            } else if (!"Logger".equals(this.columnTexts.get(columnIndex))) {
                return Boolean.class;
            }
            return super.getColumnClass(columnIndex);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return (columnIndex != this.getColumnIndex("Logger"));
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            try {
                OntimizeJEELogger oJEELogger = (OntimizeJEELogger) super.getValueAt(rowIndex,
                        this.getColumnIndex("OntimizeLogger"));
                Object columnName = this.columnTexts.get(columnIndex);
                if ("Level".equals(columnName)) {
                    this.setLevel(oJEELogger, (Level) value);
                    this.fireTableDataChanged();
                    return;
                } else if ("Trace".equals(columnName)) {
                    this.setLevel(oJEELogger, Level.TRACE);
                } else if ("Debug".equals(columnName)) {
                    this.setLevel(oJEELogger, Level.DEBUG);
                } else if ("Info".equals(columnName)) {
                    this.setLevel(oJEELogger, Level.INFO);
                } else if ("Warn".equals(columnName)) {
                    this.setLevel(oJEELogger, Level.WARN);
                } else if ("Error".equals(columnName)) {
                    this.setLevel(oJEELogger, Level.ERROR);
                } else if ("Off".equals(columnName)) {
                    this.setLevel(oJEELogger, Level.OFF);
                } else if ("Inherit".equals(columnName)) {
                    this.setLevel(oJEELogger, null);
                }
                this.getValueAt(rowIndex, 1);
            } catch (Exception e) {
                LoggerTable.logger.error("setValueAt", e);
            }
            this.fireTableDataChanged();
        }

        public Level getLevel(OntimizeJEELogger logger) {
            IServerManagementService manager = BeansFactory.getBean(IServerManagementService.class);
            if (manager != null) {
                try {
                    return manager.getLevel(logger);
                } catch (Exception e) {
                    LoggerTable.logger.error("remote getLevel exception", e);
                }
            }
            return null;
        }

        public void setLevel(OntimizeJEELogger logger, Level level) throws Exception {
            IServerManagementService manager = BeansFactory.getBean(IServerManagementService.class);
            if (manager != null) {
                logger.setLoggerLevel(level);
                manager.setLevel(logger);
            }
        }

    }

}
