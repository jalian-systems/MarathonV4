/*******************************************************************************
 * Copyright 2016 Jalian Systems Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package net.sourceforge.marathon;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;

import net.sourceforge.marathon.runtime.api.AbstractFileConsole;
import net.sourceforge.marathon.runtime.api.ClassPathHelper;
import net.sourceforge.marathon.runtime.api.Constants;

/**
 * Processes arguments and provides getters to get at them. Marathon specific.
 */
public class ArgumentProcessor {
    private final static Logger logger = Logger.getLogger(ArgumentProcessor.class.getName());

    private List<String> tests = new ArrayList<String>();
    private String projectDirName;
    private boolean batchMode = false;
    private boolean showSplash = true;
    private boolean acceptchecklists = false;
    private String reportDir = null;
    private boolean capture = false;

    /**
     * @return the HTML filename given on command line with <code>-html</code>
     *         option.
     */
    public String getHtmlFileName() {
        if (reportDir == null)
            return null;
        return new File(reportDir, "results.html").getAbsolutePath();
    }

    /**
     * @return the name of Marathon Project File given on the command line.
     */
    public String getProjectDirectory() {
        return projectDirName;
    }

    /**
     * @return the tests given on the command line following the MPF name.
     */
    public List<String> getTests() {
        return tests;
    }

    /**
     * @return the text filename given on command line with <code>-text</code>
     *         option.
     */
    public String getTextFileName() {
        if (reportDir == null)
            return null;
        return new File(reportDir, "results.txt").getAbsolutePath();
    }

    /**
     * @return the XML filename given on command line with <code>-xml</code>
     *         option.
     */
    public String getXmlFileName() {
        if (reportDir == null)
            return null;
        return new File(reportDir, "results.xml").getAbsolutePath();
    }

    /**
     * @return the TestLink XML filename given on command line with
     *         <code>-tlxml</code> option.
     */
    public String getTestLinkXmlFileName() {
        if (reportDir == null)
            return null;
        return new File(reportDir, "testlink-results.xml").getAbsolutePath();
    }

    /**
     * @return Whether <code>-batch</code> option is given on the command line.
     */
    public boolean isBatchMode() {
        return batchMode;
    }

    /**
     * @return false if <code>-nosplash</code> is given on the command line.
     */
    public boolean showSplash() {
        return showSplash;
    }

    /**
     * Process the given arguments.
     * 
     * @param args
     *            , the arguments given on the command line.
     */
    public void process(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-help") || args[i].equals("-?") || args[i].equals("-h"))
                help("");
            if (args[i].equals("-b") || args[i].equals("-batch")) {
                batchMode = true;
            } else if (args[i].equals("-i") || args[i].equals("-ignore")) {
                // Just ignore the argument (used in batch file)
            } else if (args[i].equals("-nosplash")) {
                showSplash = false;
            } else if (args[i].equals("-acceptchecklists")) {
                acceptchecklists = true;
            } else if (args[i].equals("-noconsolelog")) {
                AbstractFileConsole.setConsoleLogNeeded(false);
            } else if (args[i].equals("-capture")) {
                capture = true;
            } else if (args[i].equals("-reportdir")) {
                i++;
                checkArgs(args, i);
                reportDir = args[i];
                File rdir = new File(reportDir);
                boolean b = true;
                if (!rdir.exists())
                    b = rdir.mkdirs();
                else if (!rdir.isDirectory()) {
                    logger.severe("Given report directory is not a directory " + reportDir);
                    System.exit(1);
                }
                if (!b) {
                    logger.severe("Could not create given report directory " + reportDir);
                    System.exit(1);
                }
            } else if (args[i].startsWith("-")) {
                help("Invalid argument " + args[i]);
            } else {
                if (projectDirName == null)
                    projectDirName = args[i];
                else
                    tests.add(args[i]);
            }
        }
        if (tests.size() == 0)
            tests.add("AllTests");
        if (batchMode && reportDir == null) {
            reportDir = "marathon-reports";
            File rdir = new File(reportDir);
            boolean b = true;
            if (!rdir.exists())
                b = rdir.mkdirs();
            else if (!rdir.isDirectory()) {
                logger.severe("Given report directory is not a directory " + reportDir);
                System.exit(1);
            }
            if (!b) {
                logger.severe("Could not create given report directory " + reportDir);
                System.exit(1);
            }
        }
        if (reportDir != null) {
            System.setProperty(Constants.PROP_REPORT_DIR, new File(reportDir).getAbsolutePath());
            if (capture || acceptchecklists)
                System.setProperty(Constants.PROP_IMAGE_CAPTURE_DIR, new File(reportDir).getAbsolutePath());
        }
        String home = System.getProperty(Constants.PROP_HOME);
        if (home == null) {
            String classPath = ClassPathHelper.getClassPath(Main.class);
            home = new File(classPath).getParent();
        }
        File f = new File(home);
        if (!f.exists())
            help("Given home folder(" + f.getAbsolutePath()
                    + ") doesn't exist. Set MARATHON_HOME environment variable and try again");
        if (!f.isDirectory()) {
            help("Given home folder(" + f.getAbsolutePath()
                    + ") is not a folder. Set MARATHON_HOME environment variable and try again");
        }
        System.setProperty(Constants.PROP_HOME, f.getAbsolutePath());
    }

    /**
     * Check whether the mandatory argument is provided with an option.
     * 
     * @param args
     * @param i
     */
    private void checkArgs(String[] args, int i) {
        if (i == args.length)
            help("Invalid arguments");
    }

    /**
     * Provide a help message.
     * 
     * @param errorMessage
     *            , if called because of an error on the command line.
     */
    public void help(String errorMessage) {
        if (!isBatchMode()) {
            JEditorPane pane = new JEditorPane("text/html", "");
            StringBuffer message = new StringBuffer();
            if (!errorMessage.equals("")) {
                message.append("Error: " + errorMessage + "<br><br>");
            }
            message.append("Usage:<br>");
            message.append(
                    "java net.sourceforge.marathon.Main -batch [-reportdir &lt;report-directory&gt; [-acceptchecklists ] [-capture]] &lt;Project Directory&gt; [ (&lt;TestCase&gt;|+&lt;TestSuite&gt;) ...]<br>");
            message.append("or<br>");
            message.append("java net.sourceforge.marathon.Main [-ignore] [-nosplash] [&lt;Project Directory&gt;]<br>");
            pane.setText(message.toString());
            pane.setEditable(false);
            if (errorMessage.equals(""))
                JOptionPane.showMessageDialog(null, pane, "Usage", JOptionPane.INFORMATION_MESSAGE);
            else
                JOptionPane.showMessageDialog(null, pane, "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            StringBuffer message = new StringBuffer();
            if (!errorMessage.equals("")) {
                message.append("Error: " + errorMessage + "\n\n");
            }
            message.append("Usage:\n");
            message.append(
                    "java net.sourceforge.marathon.Main -batch [-reportdir <report-directory> [-acceptchecklists ] [-capture]] <Project Directory> [ (<TestCase>|+<TestSuite>) ...]<br>");
            message.append("or\n");
            message.append("java net.sourceforge.marathon.Main [-ignore] [-nosplash] [<Project Directory>]\n");
            logger.severe(message.toString());
        }
        System.exit(0);
    }

    public boolean getAcceptChecklists() {
        return acceptchecklists;
    }

    public String getReportDir() {
        return reportDir;
    }
}
