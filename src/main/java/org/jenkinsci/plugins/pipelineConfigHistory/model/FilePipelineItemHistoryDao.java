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
package org.jenkinsci.plugins.pipelineConfigHistory.model;

import hudson.FilePath;
import hudson.XmlFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.jenkinsci.plugins.pipelineConfigHistory.DirectoryUtils;
import org.jenkinsci.plugins.pipelineConfigHistory.PipelineConfigHistoryConsts;
import org.jenkinsci.plugins.pipelineConfigHistory.PluginUtils;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import org.xml.sax.SAXException;

/**
 * A PipelineItemHistoryDao-Implementation for storing the history directly in the file system
 * (where Jenkins is stored).
 */
public class FilePipelineItemHistoryDao implements PipelineItemHistoryDao {

  /**
   * This classes loger.
   */
  private static final Logger LOG =
      Logger.getLogger(PipelineConfigHistoryItemListener.class.getName());

  private final File historyRootDir;
  private static final long CLASH_SLEEP_TIME = 500;

  public FilePipelineItemHistoryDao(final File historyRootDir) {
    this.historyRootDir = historyRootDir;
  }

  @Override
  public void createHistory(WorkflowJob workflowJob, int buildNumber) throws IOException {
    boolean isHistoryPresent = isHistoryPresent(workflowJob);
    if (!isHistoryPresent) {
      writeUpdateToDisk(workflowJob, buildNumber);
    }
    LOG.log(Level.FINEST,
        !isHistoryPresent ? "Pipeline history was updated" : "Pipeline history was not updated.");
  }

  public File getRootDirectory() {
    return historyRootDir;
  }

  @Override
  public boolean updateHistory(WorkflowJob workflowJob, int buildNumber) throws IOException {
    //only create new entry if something has changed.
    boolean hasSomethingChanged = false;
    try {
      hasSomethingChanged = hasSomethingChanged(workflowJob, buildNumber);
    } catch (FileNotFoundException e) {
      LOG.log(Level.SEVERE, "history could not be updated: {0}", e.getMessage());
      return false;
    }
    if (hasSomethingChanged) {
      writeUpdateToDisk(workflowJob, buildNumber);
    }
    LOG.log(Level.FINEST,
        hasSomethingChanged ? "Pipeline history was updated" : "Pipeline history was not updated.");
    
    return hasSomethingChanged;
  }

  @Override
  public void deleteHistory(WorkflowJob workflowJob) throws IOException {
    FileUtils.deleteDirectory(getHistoryRootDirectory(workflowJob));
  }

  @Override
  public void changeHistoryLocation(WorkflowJob workflowJob, String oldFullName,
                                    String newFullName) {
    final String onLocationChangedDescription = "old full name: " + oldFullName
        + ", new full name: " + newFullName;

    final File oldDir = getHistoryRootDirectory(oldFullName);
    final File newDir = getHistoryRootDirectory(newFullName);

    if (oldDir.exists()) {
      FilePath oldDirFilePath = new FilePath(oldDir);
      FilePath newDirFilePath = new FilePath(newDir);
      try {
        oldDirFilePath.copyRecursiveTo(newDirFilePath);
        updateHistoryXmls(workflowJob);

        oldDirFilePath.deleteRecursive();

        LOG.log(Level.FINEST,
            "completed move of old history files on location change {0}",
            onLocationChangedDescription);
      } catch (IOException e) {
        final String ioExceptionStr = "unable to move old history on location change."
            + onLocationChangedDescription;
        LOG.log(Level.WARNING, ioExceptionStr, e);
        return;
      } catch (InterruptedException e) {
        final String irExceptionStr = "interrupted while moving old history on location change."
            + onLocationChangedDescription;
        LOG.log(Level.WARNING, irExceptionStr, e);
        Thread.currentThread().interrupt();
      }
      LOG.log(Level.FINEST, "Pipeline history moved.");
    } else {
      LOG.log(Level.FINEST,
          "Pipeline history not moved, directory does not exist: {0}", oldDir);
    }
  }

  private void updateHistoryXmls(WorkflowJob workflowJob) throws IOException {
    final IOException[] exception = new IOException[1];

    Arrays.stream(getHistoryRootDirectory(workflowJob).listFiles())
        .filter(File::isDirectory)
        .filter(file -> new File(file, PipelineConfigHistoryConsts.HISTORY_XML_FILENAME).exists())
        .forEach(file -> {
          XmlFile historyXml = new XmlFile(
              new File(file, PipelineConfigHistoryConsts.HISTORY_XML_FILENAME)
          );
          PipelineHistoryDescription oldDescr =
              getPipelineHistoryDescriptionFromHistoryXml(historyXml);
          try {
            savePipelineHistoryDescriptionToXmlFile(
                new PipelineHistoryDescription(oldDescr.getTimestamp(),
                    workflowJob.getFullName(), oldDescr.getBuildNumber()
                ),
                historyXml
            );
          } catch (IOException e) {
            exception[0] = e;
          }
        });
    if (exception[0] != null) {
      throw exception[0];
    }
  }

  @Override
  public File getMostRecentRevision(WorkflowJob workflowJob) {
    List<File> currentEntriesSorted = getCurrentEntriesSorted(workflowJob);
    return currentEntriesSorted.get(currentEntriesSorted.size() - 1);
  }

  @Override
  public File getRevision(WorkflowJob workflowJob, String identifier) throws FileNotFoundException {
    List<File> currentEntriesSorted = getCurrentEntriesSorted(workflowJob);
    Optional<File> optionalFile = currentEntriesSorted.stream()
        .filter(entry -> entry.getName().equals(identifier))
        .findAny();
    if (!optionalFile.isPresent()) {
      throw new FileNotFoundException("File with identifier " + identifier + " not found.");
    }
    return optionalFile.get();
  }

  @Override
  public File getRevision(PipelineHistoryDescription pipelineHistoryDescription)
      throws FileNotFoundException {
    return getRevision(
        pipelineHistoryDescription.getWorkflowJob(),
        pipelineHistoryDescription.getTimestamp()
    );
  }

  @Override
  public boolean isBuiltfromReplay(PipelineHistoryDescription pipelineHistoryDescription)
      throws IOException {
    File buildXmlFile =
        PluginUtils.getBuildXml(getRevision(pipelineHistoryDescription)).getFile();
    return FileUtils.readFileToString(buildXmlFile).contains("ReplayCause");
  }

  @Override
  public int getOriginalBuildNumberFromReplay(PipelineHistoryDescription pipelineHistoryDescription)
      throws IOException, SAXException, ParserConfigurationException {
    File buildXmlFile =
        PluginUtils.getBuildXml(getRevision(pipelineHistoryDescription)).getFile();
    return PluginUtils.getOriginalNumberFromBuildXml(buildXmlFile);
  }

  private PipelineHistoryDescription getPipelineHistoryDescriptionFromHistoryXml(
      XmlFile historyXml) {
    return (PipelineHistoryDescription) historyXml.getXStream().fromXML(historyXml.getFile());
  }

  private XmlFile getHistoryXmlFile(File configEntryDir) {
    return new XmlFile(new File(configEntryDir, PipelineConfigHistoryConsts.HISTORY_XML_FILENAME));
  }

  private void savePipelineHistoryDescriptionToXmlFile(
      PipelineHistoryDescription pipelineHistoryDescription, XmlFile xmlFile) throws IOException {
    xmlFile.write(pipelineHistoryDescription);
  }

  @Override
  public SortedMap<String, PipelineHistoryDescription> getRevisions(WorkflowJob workflowJob)
      throws IOException {
    //get all dirs containing a build.xml.
    final TreeMap<String, PipelineHistoryDescription> map = new TreeMap<>();
    final File[] files =
        getHistoryRootDirectory(workflowJob).listFiles(PipelineHistoryFileFilter.getInstance());
    if (files == null) {
      return map;
    } else {
      for (File historyDir : files) {
        map.put(historyDir.getName(),
            getPipelineHistoryDescriptionFromHistoryXml(getHistoryXmlFile(historyDir)));
      }
    }

    return map;
  }

  @Override
  public boolean isHistoryPresent(WorkflowJob workflowJob) {
    File historyJobDir = getHistoryRootDirectory(workflowJob);
    File[] historyJobDirChildren = historyJobDir.listFiles();

    return historyJobDir.exists()
        && historyJobDirChildren != null
        && historyJobDirChildren.length != 0;
  }

  private void writeUpdateToDisk(WorkflowJob workflowJob, int buildNumber) throws IOException {
    //get source and create target direction
    WorkflowRun workflowRun = workflowJob.getBuildByNumber(buildNumber);
    if (workflowRun == null) {
      LOG.log(Level.SEVERE,
          "build not found: {0}, pipeline config update is not written to disk.", buildNumber);
      return;
    }
    File buildRootDir = workflowJob.getBuildByNumber(buildNumber).getRootDir();
    File timestampedRootDir = createNextTimestampedHistoryDir(workflowJob);

    //save build.xml
    XmlFile buildDotXml = PluginUtils.getBuildXml(buildRootDir);
    if (buildDotXml.exists()) {
      copySingleFileToDestination(buildDotXml.getFile(), timestampedRootDir);
    }

    //save libs folder (pipeline script libraries formerly pulled from scm)
    File buildLibDir = PluginUtils.getLibDir(buildRootDir);
    if (buildLibDir.exists()) {
      copyRecursively(buildLibDir, new File(timestampedRootDir, "libs"));
    }

    //save history xml
    savePipelineHistoryDescriptionToXmlFile(
        new PipelineHistoryDescription(
            timestampedRootDir.getName(),
            workflowJob.getFullName(),
            buildNumber
        ),
        getHistoryXmlFile(timestampedRootDir)
    );
  }

  private boolean hasSomethingChanged(WorkflowJob workflowJob, int buildNumber)
      throws FileNotFoundException {
    File mostRecentHistoryEntryRootDir = getMostRecentRevision(workflowJob);
    File buildRootDir = workflowJob.getBuildByNumber(buildNumber).getRootDir();

    File mostRecentHistoryEntryLibDir = PluginUtils.getLibDir(mostRecentHistoryEntryRootDir);
    File buildLibDir = PluginUtils.getLibDir(buildRootDir);

    XmlFile buildLibBuildXml = new XmlFile(new File(buildRootDir, "build.xml"));
    XmlFile mostRecentHistoryEntryBuildXml =
        new XmlFile(new File(mostRecentHistoryEntryRootDir, "build.xml"));

    if (!buildLibBuildXml.exists()) {
      throw new FileNotFoundException("file not found: " + buildLibBuildXml.getFile().getPath());
    } else if (!mostRecentHistoryEntryBuildXml.exists()) {
      throw new FileNotFoundException("file not found: "
          + mostRecentHistoryEntryBuildXml.getFile().getPath());
    }

    try {
      return (
        !PluginUtils.scriptInXmlFileIsEqual(buildLibBuildXml, mostRecentHistoryEntryBuildXml)
          || !DirectoryUtils.isEqual(
          mostRecentHistoryEntryLibDir.toPath(), buildLibDir.toPath(), true));
    } catch (IOException | ParserConfigurationException | SAXException e) {
      LOG.log(Level.SEVERE,
          "comparison between current build and most recent history entry failed.");
      return false;
    }
  }

  private List<File> getCurrentEntriesSorted(WorkflowJob workflowJob) {
    final File timestampDirectory = getHistoryRootDirectory(workflowJob);

    File[] filesArr = timestampDirectory.listFiles();
    if (filesArr != null) {
      List<File> files = Arrays.asList(filesArr);
      files.sort((File::compareTo));
      return files;
    } else {
      LOG.log(Level.WARNING, timestampDirectory.getPath() + " has no children.");
      return Collections.emptyList();
    }
  }

  public final File getHistoryRootDirectory(WorkflowJob workflowJob) {
    //don't need nor want to map the filepath like in jobconfighistory,
    // so instead of folder1/jobs/folder2/jobX it's folder1/folder2/jobX.
    return getHistoryRootDirectory(workflowJob.getFullName());
  }

  private final File getHistoryRootDirectory(String jobName) {
    return new File(historyRootDir, jobName);
  }

  private void copyRecursively(File source, File destinationParent) {
    try {
      FileUtils.copyDirectory(source, destinationParent);
    } catch (IOException e) {
      LOG.log(Level.SEVERE,
          "Unable to copy " + source.toPath() + ": " + e.getLocalizedMessage());
    }
  }

  private void copySingleFileToDestination(File sourceFile, File destinationFile) {
    try {
      Files.copy(sourceFile.toPath(), new File(destinationFile, sourceFile.getName()).toPath());
    } catch (IOException e) {
      LOG.log(Level.SEVERE,
          "Unable to copy " + sourceFile.toPath() + ": " + e.getLocalizedMessage());
    }
  }

  private File createNextTimestampedHistoryDir(WorkflowJob workflowJob) throws IOException {

    final File timestampDirectory = getHistoryRootDirectory(workflowJob);
    final AtomicReference<Calendar> timestampHolder = new AtomicReference<>();

    Calendar timestamp;
    File file = null;
    boolean continueLoop = true;
    while (continueLoop) {
      timestamp = new GregorianCalendar();
      file = new File(timestampDirectory, getIdFormatter().format(timestamp.getTime()));
      if (file.isDirectory()) {
        LOG.log(Level.FINE, "clash on {0}, will wait a moment", file);
        try {
          Thread.sleep(CLASH_SLEEP_TIME);
        } catch (InterruptedException x) {
          LOG.log(Level.WARNING, "Interrupted while creating a timestamp.");
          Thread.currentThread().interrupt();
        }
      } else {
        //no clash!
        timestampHolder.set(timestamp);
        continueLoop = false;
      }
    }
    // mkdirs sometimes fails although the directory exists afterwards,
    // so check for existence as well and just be happy if it does.
    if (!(file.mkdirs() || file.exists())) {
      LOG.log(Level.SEVERE, "Could not create rootDir: {0}", file);

      throw new IOException("Could not create rootDir " + file);
    }
    return file;
  }

  private SimpleDateFormat getIdFormatter() {
    return new SimpleDateFormat(PipelineConfigHistoryConsts.ID_FORMATTER);
  }
}
