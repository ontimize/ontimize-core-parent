package com.ontimize.report.store;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.jee.common.report.ReportResource;
import com.ontimize.jee.common.report.store.ReportProperties;
import com.ontimize.jee.common.report.store.ReportStore;
import com.ontimize.jee.common.report.store.ReportStoreDefinition;

public class LocalRemoteReportStore implements ReportStore {

    private static final Logger logger = LoggerFactory.getLogger(LocalRemoteReportStore.class);

    private static final String DEFAULT_DIRECTORY = System.getProperty("user.home");

    private static final String SUBDIR_NAME = ".reports" + File.separator + ".temp";

    private static final String EXTENSION = ".xml";

    private static final String PREFIX = "rep";

    // private static final String DIR_PREFIX = "rep";
    private static final String PROP_EXTENSION = ".properties";

    private static final String EXPRESSION_EXTENSION = ".qry";

    private final String storePath = LocalRemoteReportStore.DEFAULT_DIRECTORY + File.separator
            + LocalRemoteReportStore.SUBDIR_NAME;

    protected ReportStore rs = null;

    protected ResourceBundle bundle = null;

    protected String description = "";

    public LocalRemoteReportStore(ReportStore rs) {
        this(rs, null);
    }

    public LocalRemoteReportStore(ReportStore rs, ResourceBundle bundle) {
        this.rs = rs;
        this.bundle = bundle;
        try {
            this.description = this.rs
                .getDescription(ApplicationManager.getApplication().getReferenceLocator().getSessionId());
        } catch (Exception exc) {
            LocalRemoteReportStore.logger.error(exc.getMessage(), exc);
        }
    }

    /**
     * add
     * @param reportId String
     * @param rDef ReportStoreDefinition
     */
    @Override
    public void add(String reportId, ReportStoreDefinition rDef, int sessionId) throws Exception {
        this.rs.add(reportId, rDef, sessionId);
    }

    public void setResourceBundle(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    /**
     * exists
     * @param reportId String
     * @return boolean
     */
    @Override
    public boolean exists(String reportId, int sessionId) throws Exception {
        return this.rs.exists(reportId, sessionId);
    }

    /**
     * get
     * @param reportId String
     * @return ReportStoreDefinition
     */
    @Override
    public ReportStoreDefinition get(String reportId, int sessionId) throws Exception {
        return this.rs.get(reportId, sessionId);
    }

    /**
     * getDescription
     * @return String
     */
    @Override
    public String getDescription(int sessionId) throws Exception {
        return ApplicationManager.getTranslation(this.rs.getDescription(sessionId), this.bundle);
    }

    /**
     * getReportProperties
     * @param reportId String
     * @return ReportProperties
     */
    @Override
    public ReportProperties getReportProperties(String reportId, int sessionId) throws Exception {
        return this.rs.getReportProperties(reportId, sessionId);
    }

    /**
     * getURL
     * @param reportId String
     * @return URL
     */
    @Override
    public URL getURL(String reportId, int sessionId) throws Exception {
        // guardamos en local
        return this.saveTemp(reportId, this.get(reportId, sessionId));
    }

    private URL saveTemp(String reportId, ReportStoreDefinition rDef) throws IOException {

        String name = this.generateFileName(reportId);
        File fAux = new File(this.storePath + File.separator + this.generateDirectoryName(reportId));
        fAux.deleteOnExit();
        fAux.mkdirs();
        File f = new File(this.storePath + File.separator + this.generateDirectoryName(reportId), name);
        f.deleteOnExit();
        FileOutputStream fOut = new FileOutputStream(f);
        File f2 = new File(this.storePath + File.separator + this.generateDirectoryName(reportId),
                this.generatePropertiesFileName(reportId));
        f2.deleteOnExit();
        FileOutputStream fOutProp = new FileOutputStream(f2);
        FileOutputStream fOutRes = null;
        File fObject = new File(this.storePath + File.separator + this.generateDirectoryName(reportId),
                this.generateExpressionFileName(reportId));
        fObject.deleteOnExit();
        FileOutputStream fOutExpr = null;
        fOutExpr = new FileOutputStream(fObject);

        try {
            fOut.write(rDef.getXMLDefinition().getBytes());
            fOut.close();
            Properties prop = new Properties();
            prop.setProperty("name", reportId);
            if (rDef.getDescription() != null) {
                prop.setProperty("description", rDef.getDescription());
            }
            if (rDef.getEntity() != null) {
                prop.setProperty("entity", rDef.getEntity());
            }
            if (rDef.getSQLQuery() != null) {
                prop.setProperty("query", rDef.getSQLQuery());
            }
            if (rDef.getReportType() != null) {
                prop.setProperty("type", rDef.getReportType());
            }
            prop.store(fOutProp, " Report Properties ");
            fOutProp.close();
            ReportResource[] resources = rDef.getResources();
            if ((resources != null) && (resources.length > 0)) {
                // Creamos el directorio
                for (int i = 0; i < resources.length; i++) {
                    File resName = new File(this.storePath + File.separator + this.generateDirectoryName(reportId),
                            resources[i].getName());
                    resName.deleteOnExit();
                    fOutRes = new FileOutputStream(resName);
                    fOutRes.write(resources[i].getBytes());
                    fOutRes.close();
                }
            }

            Object oExpression = rDef.getQueryExpression();
            if (oExpression != null) {
                ObjectOutputStream output = new ObjectOutputStream(fOutExpr);
                output.writeObject(oExpression);
                output.flush();
                output.close();
            }

            fOut.flush();
            fOut.close();
            return f.toURL();
        } catch (IOException e) {
            LocalRemoteReportStore.logger.error(e.getMessage(), e);
            throw e;
        } finally {
            if (fOut != null) {
                fOut.close();
            }
            if (fOutRes != null) {
                fOutRes.close();
            }
            if (fOutProp != null) {
                fOutProp.close();
            }
            if (fOutExpr != null) {
                fOutExpr.close();
            }
        }
    }

    @Override
    public ReportProperties[] list(String entity, String type, int sessionId) throws Exception {
        return this.rs.list(entity, type, sessionId);
    }

    @Override
    public ReportProperties[] list(String entity, int sessionId) throws Exception {
        return this.rs.list(entity, sessionId);
    }

    /**
     * list
     * @return ReportProperties[]
     */
    @Override
    public ReportProperties[] list(int sessionId) throws Exception {
        return this.rs.list(sessionId);
    }

    /**
     * remove
     * @param reportId String
     */
    @Override
    public void remove(String reportId, int sessionId) throws Exception {
        this.rs.remove(reportId, sessionId);
        this.deleteDirAndFiles(this.storePath + File.separator + this.generateDirectoryName(reportId));
    }

    /**
     * Deletes directory and files associated with report (.xml,.qry and .properties file) in local
     * report store.
     * @param path The complete path to directory
     */
    public void deleteDirAndFiles(String path) {
        File dir = new File(path);
        if ((dir != null) && (dir.listFiles() != null)) {
            File sfile[] = dir.listFiles();
            for (int i = 0; i < sfile.length; i++) {
                sfile[i].delete();
            }
            if (dir.isDirectory()) {
                dir.delete();
            }
        }
    }

    private String generateFileName(String reportId) {
        if (reportId == null) {
            throw new IllegalArgumentException("Id can't be null");
        }
        return LocalRemoteReportStore.PREFIX + reportId.hashCode() + LocalRemoteReportStore.EXTENSION;
    }

    private String generatePropertiesFileName(String reportId) {
        if (reportId == null) {
            throw new IllegalArgumentException("Id can't be null");
        }
        return LocalRemoteReportStore.PREFIX + reportId.hashCode() + LocalRemoteReportStore.PROP_EXTENSION;
    }

    private String generateDirectoryName(String reportId) {
        if (reportId == null) {
            throw new IllegalArgumentException("Id can't be null");
        }
        return LocalRemoteReportStore.PREFIX + reportId.hashCode();
    }

    private String generateExpressionFileName(String reportId) {
        if (reportId == null) {
            throw new IllegalArgumentException("Id can't be null");
        }
        return LocalRemoteReportStore.PREFIX + reportId.hashCode() + LocalRemoteReportStore.EXPRESSION_EXTENSION;
    }

    protected String getPropertiesFileName(String dir) {
        return dir + LocalRemoteReportStore.PROP_EXTENSION;
    }

    @Override
    public String toString() {
        try {
            return ApplicationManager.getTranslation(this.description, this.bundle);
        } catch (Exception ex) {
            LocalRemoteReportStore.logger.trace(null, ex);
            return "";
        }
    }

}
