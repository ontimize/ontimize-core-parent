package com.ontimize.windows.office;

import java.awt.Desktop;
import java.io.File;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.util.FileUtils;

public abstract class WindowsUtils {

    private static final Logger logger = LoggerFactory.getLogger(WindowsUtils.class);

    public static final boolean checkWindowXP = true;

    private static final String OPEN_COMMAND = "rundll32 url.dll,FileProtocolHandler ";

    public WindowsUtils() {
    }

    public static final void openFile(File f) throws Exception {
        if (f == null) {
            throw new IllegalArgumentException("Archivo no puede ser null");
        }
        Process process = java.lang.Runtime.getRuntime().exec(WindowsUtils.OPEN_COMMAND + f.toString());
    }

    protected static void openOS(File file) throws Exception {
        Desktop desktop = Desktop.getDesktop();
        desktop.open(file);
    }

    protected static void printOS(File file) throws Exception {
        Desktop desktop = Desktop.getDesktop();
        desktop.print(file);
    }

    public static final void openFile_Script(File f) throws Exception {
        if (!WindowsUtils.checkWindowXP || (System.getProperty("os.name").indexOf("Windows XP") == -1)) {
            try {
                // With new versions of JVM it is possible open files directly
                // using the class Desktop
                Class.forName("java.awt.Desktop");
                WindowsUtils.openOS(f);
                return;
            } catch (Exception e) {
                WindowsUtils.logger.trace(null, e);
            }
        }

        if (System.getProperty("os.name").indexOf("Windows") != -1) {
	        // If the JVM does not allow to open files directly then use a vbs
	        // script (Only in Windows OS)
	        URL url = WindowsUtils.class.getResource("scripts/openfile.vbs");
	        if (url == null) {
	            WindowsUtils.logger.debug("WindowsUtils: ");
	        }
	        String script = url.toString();
	        if (script.startsWith("file:")) {
	            script = script.substring(6);
	        } else if ((url != null) && (url.toString().indexOf("jar:") >= 0)) {
	            File fScript = ScriptUtilities
	                .createTemporalFileForScript("com/ontimize/windows/office/scripts/openfile.vbs");
	            script = fScript.getAbsolutePath();
	        } else {
	            throw new Exception("Invalid script URL: " + url);
	        }

	        String fileName = f.getAbsolutePath();
	        if (fileName.indexOf(' ') >= 0) {
	            fileName = "\"" + fileName + "\"";
	        }
	        ExecutionResult res = ScriptUtilities.executeScript(FileUtils.decode(script), fileName,
	                ScriptUtilities.WSCRIPT);
	        if (res.getResult() != 0) {
	            throw new Exception("Error: " + res.getOuput());
	        }
        }
    }

    public static final void printFile_Script(File f) throws Exception {
        if (!WindowsUtils.checkWindowXP || (System.getProperty("os.name").indexOf("Windows XP") == -1)) {
            try {
                Class.forName("java.awt.Desktop");
                WindowsUtils.printOS(f);
                return;
            } catch (Exception e) {
                WindowsUtils.logger.error(null, e);
            }
        }
        URL url = WindowsUtils.class.getResource("scripts/printfile.vbs");
        if (url == null) {
            WindowsUtils.logger.debug("WindowsUtils: ");
        }
        String script = url.toString();
        if (script.startsWith("file:")) {
            script = script.substring(6);
        } else if ((url != null) && (url.toString().indexOf("jar:") >= 0)) {
            File fScript = ScriptUtilities
                .createTemporalFileForScript("com/ontimize/windows/office/scripts/printfile.vbs");
            script = fScript.getAbsolutePath();
        } else {
            throw new Exception("URL for script invalid: " + url);
        }

        String fileName = f.getAbsolutePath();

        if (fileName.indexOf(' ') >= 0) {
            fileName = "\"" + fileName + "\"";
        }

        ExecutionResult res = ScriptUtilities.executeScript(script, fileName, ScriptUtilities.WSCRIPT);
        if (res.getResult() != 0) {
            throw new Exception("Error: " + res.getOuput());
        }
    }

    public static final void openFileTemplate(File f, File fData) throws Exception {
        URL url = WindowsUtils.class.getResource("scripts/openwordtemplate.vbs");
        if (url == null) {
            WindowsUtils.logger.debug("WindowsUtils: ");
        }
        String script = url.toString();
        if (script.startsWith("file:")) {
            script = script.substring(6);
        } else if ((url != null) && (url.toString().indexOf("jar:") >= 0)) {
            File fScript = ScriptUtilities
                .createTemporalFileForScript("com/ontimize/windows/office/scripts/openwordtemplate.vbs");
            script = fScript.getAbsolutePath();
        } else {
            throw new Exception("Invalid script URL: " + url);
        }
        String openFileName = f.getAbsolutePath();
        String dataFileName = fData.getAbsolutePath();

        if (openFileName.indexOf(' ') >= 0) {
            openFileName = "\"" + openFileName + "\"";
        }
        if (dataFileName.indexOf(' ') >= 0) {
            dataFileName = "\"" + dataFileName + "\"";
        }
        java.util.Vector v = new java.util.Vector();
        v.add(openFileName);
        v.add(dataFileName);

        ExecutionResult res = ScriptUtilities.executeScript(script, v, ScriptUtilities.WSCRIPT);
        if (res.getResult() != 0) {
            throw new Exception("Error: " + res.getOuput());
        }
    }

}
