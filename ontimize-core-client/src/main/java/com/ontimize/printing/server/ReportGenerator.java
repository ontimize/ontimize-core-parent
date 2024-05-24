package com.ontimize.printing.server;

import java.util.List;

public interface ReportGenerator {

    public static final String REPORT_NOT_FOUND = "INFORME_NO_EXISTE";

    public static final String ERROR = "ERROR";

    public List<Object> getReportList();

    public List<Object> getReportDescription();

    public String getDescription();

    public String createReport(String name, Object params, String archiveName);

}
