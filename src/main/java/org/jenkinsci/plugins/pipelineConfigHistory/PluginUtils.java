/*
 * The MIT License
 *
 * Copyright (c) 2019, Robin Schulz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkinsci.plugins.pipelineConfigHistory;

import hudson.PluginWrapper;
import hudson.XmlFile;
import hudson.model.Item;
import jenkins.model.Jenkins;
import org.apache.commons.io.FileUtils;
import org.jenkinsci.plugins.pipelineConfigHistory.model.FilePipelineItemHistoryDao;
import org.jenkinsci.plugins.pipelineConfigHistory.model.PipelineItemHistoryDao;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public final class PluginUtils {

  //util class.
  private PluginUtils() {
  }

  /**
   * Get the history dao which is used to manage the config history storage.
   *
   * @return the currently used history dao.
   */
  public static PipelineItemHistoryDao getHistoryDao() {
    return new FilePipelineItemHistoryDao(
        new File(Jenkins.get().getRootDir(), PipelineConfigHistoryConsts.DEFAULT_HISTORY_DIR)
    );
  }

  /**
   * Get the library directory for a given root directory (normally a build dir).
   *
   * @param buildRootDir the build root directory.
   * @return the library subdirectory.
   */
  public static File getLibDir(File buildRootDir) {
    return new File(buildRootDir, "libs");
  }

  /**
   * Get the build xml file contained in the buildRootDir.
   *
   * @param buildRootDir the directory containing the build xml file
   * @return the build xml file
   */
  public static XmlFile getBuildXml(File buildRootDir) {
    return new XmlFile(new File(buildRootDir, "build.xml"));
  }

  /**
   * Get the original number for builds which are replays from other builds.
   *
   * @param buildXmlFile the build xml file
   * @return the original number.
   * @throws ParserConfigurationException if something goes wrong creating a document parser.
   * @throws IOException                  if something IO related goes wrong int the parsing
   *                                      process.
   * @throws SAXException                 if something goes wrong parsing the xml file.
   */
  public static int getOriginalNumberFromBuildXml(File buildXmlFile)
      throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setIgnoringElementContentWhitespace(true);
    Document document = factory.newDocumentBuilder().parse(buildXmlFile);

    NodeList originalNumberElements = document.getElementsByTagName("originalNumber");

    if (originalNumberElements.getLength() != 1) {
      throw new IOException("Original build number not found: build.xml corrupt.");
    }

    try {
      return Integer.parseInt(originalNumberElements.item(0).getTextContent());
    } catch (NumberFormatException e) {
      throw new IOException("Original build number not found: build.xml corrupt.");
    }
  }

  /**
   * Get a file's content as String.
   *
   * @param file the file to read
   * @return the file's content as \n-separated String.
   * @throws IOException if the file could not be read.
   */
  public static String fileToString(File file) throws IOException {
    if (file == null) {
      return "";
    } else {
      return FileUtils.readFileToString(file);
    }

  }

  /**
   * get the workflowJob associated with the given fullName.
   *
   * @param fullName the job's fullName
   * @return the associated workflowJob.
   */
  public static WorkflowJob getWorkflowJob(String fullName) {
    Item pipelineItem = Jenkins.get().getItemByFullName(fullName);
    if (!(pipelineItem instanceof WorkflowJob)) {
      throw new IllegalArgumentException(fullName + " is not an instance of WorkflowJob");
    }
    return (WorkflowJob) pipelineItem;
  }

  /**
   * Get the path to a workflowJob's current jenkinsfile.
   *
   * @param workflowJob the workflowJob.
   * @return the path
   */
  public static String getJenkinsfilePath(WorkflowJob workflowJob) {
    return getJenkinsfilePathFromFlowDefinition(workflowJob.getDefinition());
  }

  /**
   * Get the path to a flowDefinition's current jenkinsfile.
   *
   * @param flowDefinition the FlowDefinition.
   * @return the path
   */
  public static String getJenkinsfilePathFromFlowDefinition(FlowDefinition flowDefinition) {

    if (flowDefinition instanceof CpsScmFlowDefinition) {
      return getJenkinsfilePathFromFlowDefinition((CpsScmFlowDefinition) flowDefinition);
    } else if (flowDefinition instanceof CpsFlowDefinition) {
      return "from Pipeline-Job Configuration";
    } else {
      return flowDefinition.getDescriptor().getDisplayName();
    }
  }

  private static String getJenkinsfilePathFromFlowDefinition(CpsScmFlowDefinition flowDefinition) {
    return flowDefinition.getScm().getKey();
  }

  /**
   * Get the current Jenkins root directory.
   *
   * @return the current Jenkins root directory
   */
  public static File getJenkinsRootDir() {
    return new File(Jenkins.get().root.getPath());
  }

  /**
   * Test if the required plugins are installed.
   *
   * @return whether the required plugins this plugin needs to work correctly are installed or not.
   */
  public static boolean requiredPluginsInstalled() {
    return getMissingRequiredPlugins().equals("");
  }

  /**
   * Get the missing required plugins as String.
   *
   * @return the missing required plugins.
   */
  public static String getMissingRequiredPlugins() {
    List<PluginWrapper> plugins = Jenkins.get().getPluginManager().getPlugins();

    HashSet<String> shortNames =
        new HashSet<>(Arrays.asList(PipelineConfigHistoryConsts.REQUIRED_PLUGINS_SHORT_NAMES));

    for (PluginWrapper pluginWrapper : plugins) {
      String name = pluginWrapper.getShortName();
      shortNames.remove(name);
    }
    return shortNames.isEmpty() ? "" : shortNames.toString();
  }

  /**
   * Test if the scripts contained in the two files are equal.
   *
   * @param xmlFile1 file1
   * @param xmlFile2 file2
   * @return whether the scripts contained in the two files are equal.
   */
  public static boolean scriptInXmlFileIsEqual(XmlFile xmlFile1, XmlFile xmlFile2) throws ParserConfigurationException, IOException, SAXException {

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setIgnoringElementContentWhitespace(true);
    Document document1 = factory.newDocumentBuilder().parse(xmlFile1.getFile());
    Document document2 = factory.newDocumentBuilder().parse(xmlFile2.getFile());

    NodeList scriptElements1 = document1.getElementsByTagName("script");
    NodeList scriptElements2 = document2.getElementsByTagName("script");
    if (scriptElements1.getLength() != 1) {
      throw new IOException("Original build number not found: first build.xml corrupt.");
    }
    if (scriptElements2.getLength() != 1) {
      throw new IOException("Original build number not found: second build.xml corrupt.");
    }

    return scriptElements1.item(0).getTextContent()
        .equals(scriptElements2.item(0).getTextContent());
  }

  /**
   * Returns a {@link Date}.
   *
   * @param timeStamp date as string.
   * @return The parsed date as a java.util.Date.
   */
  public static Date parsedDate(final String timeStamp) {
    try {
      return new SimpleDateFormat(PipelineConfigHistoryConsts.ID_FORMATTER)
          .parse(timeStamp);
    } catch (ParseException ex) {
      throw new IllegalArgumentException(
          "Could not parse Date" + timeStamp, ex);
    }
  }

  /**
   * Compare two xml files, ignore whitespace.
   *
   * @param xmlFile1 the first file
   * @param xmlFile2 the second file
   * @return a Diff object mapping the xml structure of the diffs found.
   */
  public static Diff computeXmlDiff(XmlFile xmlFile1, XmlFile xmlFile2) {
    return DiffBuilder
        .compare(Input.fromFile(xmlFile1.getFile())).withTest(Input.fromFile(xmlFile2.getFile()))
        .ignoreWhitespace()
        .build();
  }
}
