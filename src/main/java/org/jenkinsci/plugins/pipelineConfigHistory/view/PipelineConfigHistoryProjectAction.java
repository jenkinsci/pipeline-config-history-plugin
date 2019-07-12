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
package org.jenkinsci.plugins.pipelineConfigHistory.view;

import difflib.DiffRow;
import hudson.XmlFile;
import hudson.model.AbstractItem;
import hudson.model.Action;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.jenkinsci.plugins.pipelineConfigHistory.DirectoryUtils;
import org.jenkinsci.plugins.pipelineConfigHistory.PipelineConfigHistoryConsts;
import org.jenkinsci.plugins.pipelineConfigHistory.PluginUtils;
import org.jenkinsci.plugins.pipelineConfigHistory.model.PipelineHistoryDescription;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import javax.xml.parsers.ParserConfigurationException;

public class PipelineConfigHistoryProjectAction implements Action {


  private static final Logger LOG = Logger
      .getLogger(PipelineConfigHistoryProjectAction.class.getName());

  private final WorkflowJob project;

  public PipelineConfigHistoryProjectAction(WorkflowJob project) {
    super();
    this.project = project;
  }

  /**
   *  Get a list of all PipelineHistoryDescriptions.
   * @return a list representing all revisions of this pipeline.
   */
  public List<PipelineHistoryDescription> getPipelineHistoryDescriptions() {

    try {
      return new ArrayList<>(PluginUtils.getHistoryDao().getRevisions(project).values());
    } catch (IOException e) {
      LOG.log(Level.SEVERE, "PipelineHistoryDescriptions could not be acquired: {0}",
          e.getMessage());
      return Collections.emptyList();
    }
  }

  /**
   * Get the PipelineHistoryDescription describing the config this timestamp represents.
   *
   * @param timestamp the timestamp to identify the config
   * @return the config description object.
   * @throws IOException if the wanted pipeline history is not found.
   */
  public PipelineHistoryDescription getPipelineHistoryDescription(String timestamp)
      throws IOException {

    Optional<PipelineHistoryDescription> pipelineHistoryDescriptionOptional =
        getPipelineHistoryDescriptions().stream().filter(pipelineHistoryDescription ->
            pipelineHistoryDescription.getTimestamp().equals(timestamp)
        ).findAny();
    if (pipelineHistoryDescriptionOptional.isPresent()) {
      return pipelineHistoryDescriptionOptional.get();
    } else {
      throw new IOException("Pipeline history for " + timestamp + " not found");
    }
  }

  public String getFileNameDisplayable(String timestamp, File file) {

    return getRevision(timestamp).toPath().relativize(file.toPath()).toString();
  }

  public String getJenkinsfilePath() {
    return PluginUtils.getJenkinsfilePath(this.project);
  }

  /**
   * Return a file from a given config by its hashcode.
   *
   * @param timestamp the config identifier
   * @param hashCode  the file identifier
   * @return the wanted file.
   */
  public File getFileByHashCode(String timestamp, String hashCode) {
    int hashCodeInt = Integer.parseInt(hashCode);
    Optional<File> fileOptional = null;
    try {
      fileOptional = Arrays.stream(DirectoryUtils.getAllFilesExceptHistoryXmlFromDirectory(getRevision(timestamp)))
          .filter(file -> file.hashCode() == hashCodeInt)
          .findAny();
    } catch (IOException e) {
      LOG.log(Level.SEVERE,
          "IOException occurred trying to obtain files from directory: {0}", e.getMessage());
      return null;
    }
    if (fileOptional.isPresent()) {
      return fileOptional.get();
    } else {
      LOG.log(Level.WARNING, "File {0} not found in revision ({1})s directory.",
          new Object[] {hashCodeInt, timestamp});
      return null;
    }
  }

  /**
   * Get all files the given directory contains, except the history xml in the root dir.
   *
   * @param directory the directory.
   * @return all files this directory contains, except the history xml in the root dir.
   */
  @SuppressWarnings("unused")
  public File[] getAllFilesExceptHistoryXmlFromDirectory(File directory) {
    try {
      return DirectoryUtils.getAllFilesExceptHistoryXmlFromDirectory(directory);
    } catch (IOException e) {
      LOG.log(
          Level.SEVERE,
          "IOException occured while trying to walk through directory tree {0}: {1}",
          new Object[]{directory, e});
      return new File[]{};
    }
  }

  /**
   * Return a file's content from a given config by its hashcode.
   *
   * @param timestamp         the config identifier
   * @param hashCode          the file identifier
   * @param syntaxHighlighted whether syntax highlighting html tags should be included or not
   * @return the wanted file.
   */
  public String getFileContentByHashCode(String timestamp, String hashCode,
                                         boolean syntaxHighlighted) {
    List<String> lines =
        null;
    File file = getFileByHashCode(timestamp, hashCode);
    try {
      lines = new ArrayList<>(FileUtils.readLines(file));
    } catch (IOException e) {
      LOG.log(Level.SEVERE, "IOException occured while trying to read file {0}:{1}",
          new Object[] {file, e.getMessage()});
      return null;
    }
    if (syntaxHighlighted) {
      return escapeShebangLinesAndXml(lines);
    } else {
      return String.join("\n", lines);
    }
  }

  /**
   * Return a file's content from a given config by its hashcode.
   *
   * @param timestamp the config identifier
   * @param hashCode  the file identifier
   * @return the wanted file.
   */
  public String getFileContentByHashCode(String timestamp, String hashCode) {
    return getFileContentByHashCode(timestamp, hashCode, false);
  }

  /**
   * Return the content of the script-Tag in the given xml file.
   *
   * @param file              the xml file.
   * @param syntaxHighlighted whether syntax highlighting html tags should be included or not
   * @return the wanted script
   */
  public String getScriptFromXmlFile(File file, boolean syntaxHighlighted) {
    List<String> lines = null;
    try {
      lines = new ArrayList<>(FileUtils.readLines(file));
    } catch (IOException e) {
      LOG.log(Level.SEVERE, "File {0} could not be read correctly: {1}",
          new Object[] {file, e.getMessage()});
      return null;
    }
    String value = String.join("\n", lines);

    final String scriptBeginTag = "<script>";
    final String scriptEndTag = "</script>";
    final int scriptBegin = value.indexOf(scriptBeginTag) + scriptBeginTag.length();
    final int scriptEnd = value.indexOf(scriptEndTag);

    // check for multiple occurences of the tags
    if (value.split(scriptBeginTag).length > 2 || value.split(scriptEndTag).length > 2) {
      LOG.log(Level.WARNING, "XML file contains more than one script tag!");
      return "displaying script impossible.";
    } else if (value.split(scriptBeginTag).length < 2 || value.split(scriptEndTag).length < 2) {
      LOG.log(Level.WARNING, "XML file contains no script tag.");
      return "";
    }

    String buildScript = value.substring(scriptBegin, scriptEnd);

    if (syntaxHighlighted) {
      return escapeShebangLinesAndXml(Arrays.asList(buildScript.split("\n")));
    } else {

      //this is needed because the build Script is stored in escaped form.
      return StringEscapeUtils.unescapeXml(buildScript);
    }
  }

  /**
   * Return the content of the script-Tag in the given xml file.
   *
   * @param timestamp         the config identifier
   * @param hashCode          the file identifier
   * @param syntaxHighlighted whether syntax highlighting html tags should be included or not
   * @return the wanted script
   */
  public String getScriptFromXmlFile(String timestamp, String hashCode,
                                     boolean syntaxHighlighted) {
    return getScriptFromXmlFile(getFileByHashCode(timestamp, hashCode), syntaxHighlighted);
  }

  /**
   * Get your timestamp well formatted.
   *
   * @param timestamp the config identifier
   * @return the timestamp in a humanly readable format.
   */
  public String getTimestampWellFormatted(String timestamp) {
    DateFormat timestampDateFormat = new SimpleDateFormat(PipelineConfigHistoryConsts.ID_FORMATTER);
    Date date = null;
    try {
      date = timestampDateFormat.parse(timestamp);
    } catch (ParseException e) {
      LOG.log(Level.FINEST,
          "Could not format the timestamp well. Will return the given timestamp.");
      return timestamp;
    }

    DateFormat dateFormat = DateFormat.getDateTimeInstance();
    return dateFormat.format(date);
  }

  private String escapeXml(String xmlString) {
    return xmlString
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;");
  }

  /**
   * Get the wanted pipeline revision of the current project.
   *
   * @param timestamp the config identifier
   * @return the wanted revision
   */
  public File getRevision(String timestamp) {
    try {
      return PluginUtils.getHistoryDao().getRevision(getPipelineHistoryDescription(timestamp));
    } catch (IOException e) {
      LOG.log(Level.SEVERE, "Revision {0} could not be read: {1}",
          new Object[] {timestamp, e.getMessage()});
      return null;
    }
  }

  /**
   * Determine whether the given revision's associated build is a replay from anoter build.
   *
   * @param timestamp the config revision identifier.
   * @return true or false
   */
  public boolean isBuiltfromReplay(String timestamp) {
    try {
      return PluginUtils
          .getHistoryDao()
          .isBuiltfromReplay(getPipelineHistoryDescription(timestamp));
    } catch (IOException e) {
      LOG.log(
          Level.SEVERE,
          "Error trying to obtain replay status: build.xml could not be read: {0}",
          e.getMessage()
      );
      return false;
    }
  }

  /**
   * Get the original build number this revision's associated build is a replay from.
   * (Only if isBuiltfromReplay(timestamp) is true.)
   *
   * @param timestamp the config revision identifier.
   * @return true or false
   */
  public int getOriginalNumberFromReplayBuild(String timestamp) {
    try {
      return PluginUtils
          .getHistoryDao()
          .getOriginalBuildNumberFromReplay(getPipelineHistoryDescription(timestamp));
    } catch (IOException | SAXException | ParserConfigurationException e) {
      LOG.log(Level.SEVERE,
          "Original build number could not be acquired: {0}", e.getMessage());
      return -1;
    }
  }

  /**
   * Get the given revision's associated build's buildNumber.
   *
   * @param timestamp the config revision identifier
   * @return the buildNumber
   */
  public int getBuildNumber(String timestamp) {
    try {
      return getPipelineHistoryDescription(timestamp).getBuildNumber();
    } catch (IOException e) {
      LOG.log(Level.SEVERE, "Build number could not be acquired: {0}", e.getMessage());
      return -1;
    }
  }

  /**
   * Get the current pipeline project.
   *
   * @return the pipeline project
   */
  public final AbstractItem getProject() {
    return project;
  }

  /**
   * Determine whether the given revisions are unequal.
   *
   * @param timestamp1 the first config revision identifier
   * @param timestamp2 the second config revision identifier
   * @return whether the given revisions are unequal.
   */
  public boolean isAnyMatchUnequal(String timestamp1, String timestamp2) {
    return getMatchingFiles(timestamp1, timestamp2)
        .stream()
        .anyMatch(match -> !getLines(match).isEmpty());
  }

  /**
   * Calculate the revision file equality and return a list encapsulating that .
   *
   * @param timestamp1 the first config revision identifier
   * @param timestamp2 the second config revision identifier
   * @return the given revisions' matching files.
   */
  public List<Match> getMatchingFiles(String timestamp1, String timestamp2) {
    List<Match> matches = new ArrayList<>();

    File revision1Dir = getRevision(timestamp1);
    File revision2Dir = getRevision(timestamp2);

    List<File> allFiles1 = null;
    List<File> allFiles2 = null;
    try {
      allFiles1 = Arrays.asList(
          DirectoryUtils.getAllFilesExceptHistoryXmlFromDirectory(revision1Dir)
      );
      allFiles2 = Arrays.asList(
          DirectoryUtils.getAllFilesExceptHistoryXmlFromDirectory(revision2Dir)
      );
    } catch (IOException e) {
      LOG.log(Level.SEVERE,
          "IOException occurred trying to obtain files from directory: {0}", e.getMessage());
      return Collections.emptyList();
    }

    Set<File> remainingFiles1 = new HashSet<>(allFiles1);
    Set<File> remainingFiles2 = new HashSet<>(allFiles2);

    List<File> finalAllFiles2 = allFiles2;
    allFiles1.forEach(file1 -> {
      File[] matchingFiles = finalAllFiles2.stream()
          .filter(file2 -> relativizedFileEquals(file1, file2, revision1Dir, revision2Dir))
          .toArray(File[]::new);
      if (matchingFiles.length == 0) {
        //no matching file found, do nothing.
      } else if (matchingFiles.length == 1) {
        //found a unique match that is NOT the history xml file
        //check for content equality
        if (fileContentEquals(file1, matchingFiles[0])) {
          matches.add(new Match(file1, matchingFiles[0], Match.Kind.EQUAL));
        } else {
          matches.add(new Match(file1, matchingFiles[0], Match.Kind.UNEQUAL));
        }
        //they can be deleted
        remainingFiles1.remove(file1);
        remainingFiles2.remove(matchingFiles[0]);
      } else {
        LOG.log(Level.WARNING, "Found non-unique files!");
      }
    });

    for (File file : remainingFiles1) {
      matches.add(new Match(file, null, Match.Kind.SINGLE_1));
    }
    for (File file : remainingFiles2) {
      matches.add(new Match(null, file, Match.Kind.SINGLE_2));
    }

    matches.sort(Comparator.comparing(Match::getKind));
    //filter history xml
    return matches
        .stream()
        .filter(match ->
            !match.getFileName().equals(PipelineConfigHistoryConsts.HISTORY_XML_FILENAME)
        )
        .collect(Collectors.toList());
  }

  /**
   * Calculate the line-wise diff of the given files.
   *
   * @param file1 the first file
   * @param file2 the second file
   * @return the difference between the given files.
   */
  public final List<SideBySideView.Line> getLines(File file1, File file2) {
    try {
      return getLines(PluginUtils.fileToString(file1), PluginUtils.fileToString(file2));
    } catch (IOException e) {
      LOG.log(Level.WARNING, "File could not be read: {0}", e.getMessage());
      return Collections.emptyList();
    }
  }

  /**
   * Calculate the line-wise diff of the files contained in the given match.
   *
   * @param match the match.
   * @return the diff.
   */
  public final List<SideBySideView.Line> getLines(Match match) {
    if (match.getFileName().equals(PipelineConfigHistoryConsts.BUILD_XML_FILENAME)) {
      return getLines(
          getScriptFromXmlFile(match.getFile1(), false),
          getScriptFromXmlFile(match.getFile2(), false)
      );
    } else {
      try {
        return getLines(
            PluginUtils.fileToString(match.getFile1()),
            PluginUtils.fileToString(match.getFile2())
        );
      } catch (IOException e) {
        LOG.log(Level.WARNING, "File could not be read: {0}", e.getMessage());
        return Collections.emptyList();
      }
    }
  }

  /**
   * Calculate the line-wise diff of the given files (as \n-containing strings).
   *
   * @param file1Str the first file
   * @param file2Str the second file
   * @return the diff.
   */
  public final List<SideBySideView.Line> getLines(String file1Str, String file2Str) {
    return new DiffLineGenerator(file1Str, file2Str).getLines();
  }

  public final List<SingleLineView.Line> getSingleLineViewLines(File file1, File file2) {
    try {
      return getSingleLineViewLines(PluginUtils.fileToString(file1), PluginUtils.fileToString(file2));
    } catch (IOException e) {
      LOG.log(Level.WARNING, "File could not be read: {0}", e.getMessage());
      return Collections.emptyList();
    }
  }

  public final List<SingleLineView.Line> getSingleLineViewLines(Match match) {
    return getSingleLineViewLines(getLines(match));
  }

  public final List<SingleLineView.Line> getSingleLineViewLines(List<SideBySideView.Line> unsortedLines) {
    List<SingleLineView.Line> sortedLines = new LinkedList<>();

    List<SingleLineView.Line> currentInsertBuffer = new LinkedList<>();
    List<SingleLineView.Line> currentDeleteBuffer = new LinkedList<>();

    ListIterator<SideBySideView.Line> iterator = unsortedLines.listIterator();
    if (!iterator.hasNext()) {
      return sortedLines;
    }

    SideBySideView.Line currentLine = iterator.next();

    boolean nextIteration = true;
    do {
      boolean changeLine = true;
      if (currentLine.isSkipping()) {
        sortedLines.add(new SingleLineView.Line(
            SingleLineView.Line.Kind.SKIPPING,
            "",
            currentLine.getLeft().getLineNumber(),
            currentLine.getRight().getLineNumber()
        ));
      } else if (currentLine.getTag().equals(DiffRow.Tag.CHANGE)) {
        // start collecting all lines in the two buffers
        while (currentLine.getTag().equals(DiffRow.Tag.CHANGE) && iterator.hasNext()) {
          currentDeleteBuffer.add(new SingleLineView.Line(
              SingleLineView.Line.Kind.DELETE,
              currentLine.getLeft().getText(),
              currentLine.getLeft().getLineNumber(),
              currentLine.getRight().getLineNumber()
          ));
          currentInsertBuffer.add(new SingleLineView.Line(
              SingleLineView.Line.Kind.INSERT,
              currentLine.getRight().getText(),
              currentLine.getLeft().getLineNumber(),
              currentLine.getRight().getLineNumber()
          ));

          // next line check
          if (iterator.hasNext()) {
            currentLine = iterator.next();
          }
          if (!currentLine.getTag().equals(DiffRow.Tag.CHANGE) || !iterator.hasNext()) {
            //add the buffers to the list!
            sortedLines.addAll(currentDeleteBuffer);
            sortedLines.addAll(currentInsertBuffer);

            currentDeleteBuffer = new LinkedList<>();
            currentInsertBuffer = new LinkedList<>();

            //else, the next line is swallowed.
            changeLine = false;
          }
        }
      } else if (currentLine.getTag().equals(DiffRow.Tag.EQUAL)) {
        sortedLines.add(new SingleLineView.Line(
            SingleLineView.Line.Kind.EQUAL,
            currentLine.getLeft().getText(),
            currentLine.getLeft().getLineNumber(),
            currentLine.getRight().getLineNumber()
        ));
      } else if (currentLine.getTag().equals(DiffRow.Tag.DELETE)) {
        sortedLines.add(new SingleLineView.Line(
            SingleLineView.Line.Kind.DELETE,
            currentLine.getLeft().getText(),
            currentLine.getLeft().getLineNumber(),
            currentLine.getRight().getLineNumber()
        ));
      } else if (currentLine.getTag().equals(DiffRow.Tag.INSERT)) {
        sortedLines.add(new SingleLineView.Line(SingleLineView.Line.Kind.INSERT,
            currentLine.getRight().getText(),
            currentLine.getLeft().getLineNumber(),
            currentLine.getRight().getLineNumber()
        ));
      }

      if (!iterator.hasNext()) {
        nextIteration = false;
      }
      if (nextIteration && changeLine) {
        currentLine = iterator.next();
      }
    } while (nextIteration);



    return sortedLines;
  }

  public final List<SingleLineView.Line> getSingleLineViewLines(String file1Str, String file2Str) {
    List<SideBySideView.Line> unsortedLines = getLines(file1Str, file2Str);
      return getSingleLineViewLines(unsortedLines);
  }

  /**
   * Show a diff file overview or all diffs combined in one page,
   * depending on the showDiffsInOneSite-parameter.
   *
   * @param req the request containing the parameters
   * @param rsp the response.
   * @throws IOException some exception that might occur.
   */
  public void doDiffFiles(StaplerRequest req, StaplerResponse rsp) throws IOException {
    String timestamp1 = req.getParameter("timestamp1");
    String timestamp2 = req.getParameter("timestamp2");

    String showDiffsInOneSite = req.getParameter("showDiffsInOneSite");

    if (PluginUtils.parsedDate(timestamp1).after(PluginUtils.parsedDate(timestamp2))) {
      timestamp1 = req.getParameter("timestamp2");
      timestamp2 = req.getParameter("timestamp1");
    }
    if (showDiffsInOneSite != null) {
      boolean anyDiffExists =
          getMatchingFiles(timestamp1, timestamp2)
              .stream()
              .anyMatch(match -> !getLines(match).isEmpty());
      rsp.sendRedirect("showAllDiffs?timestamp1=" + timestamp1
          + "&timestamp2=" + timestamp2 + "&anyDiffExists=" + anyDiffExists);
      return;
    }
    rsp.sendRedirect("showDiffFiles?timestamp1=" + timestamp1
        + "&timestamp2=" + timestamp2);
  }

  private String escapeShebangLinesAndXml(List<String> lines) {
    StringBuilder resultBuilder = new StringBuilder();

    final String shebang = "#!";
    final String startTag = "<pre><code>";
    final String endTag = "</pre></code>";
    final String newLine = "<br/>";
    final int firstElement = 0;

    List<String> remainingLines = new ArrayList<>(lines);
    String currentLine = lines.get(firstElement);

    while (currentLine.trim().startsWith(shebang) || currentLine.trim().isEmpty()) {
      if (currentLine.trim().startsWith(shebang)) {
        resultBuilder
            .append(startTag)
            .append(currentLine)
            .append(endTag);
      }
      //avoid duplicates.
      remainingLines.remove(firstElement);

      //prepare for next iteration
      currentLine = remainingLines.get(firstElement);
    }
    if (lines.size() != remainingLines.size()) {
      //sth changed in previous loop
      resultBuilder.append(newLine);
    }

    //append the rest of the file
    return resultBuilder
        .append(startTag)
        .append(escapeXml(String.join("\n", remainingLines)))
        .append(endTag)
        .toString();
  }

  private boolean relativizedFileEquals(File file1, File file2, File root1, File root2) {
    return
        root1.toPath().relativize(file1.toPath()).equals(
            root2.toPath().relativize(file2.toPath())
        );
  }

  private boolean fileContentEquals(File file1, File file2) {
    //also checks for build xml!
    try {
      return (file1.getName().equals(PipelineConfigHistoryConsts.BUILD_XML_FILENAME))
          ? PluginUtils.scriptInXmlFileIsEqual(new XmlFile(file1), new XmlFile(file2))
          : FileUtils.contentEquals(file1, file2);
    } catch (IOException e) {
      LOG.log(Level.SEVERE, "IOException occured during file comparison: " + e.getMessage());
      return false;
    } catch (SAXException | ParserConfigurationException e) {
      LOG.log(Level.WARNING, "Build file script comparison failed: " + e.getMessage());
      return false;
    }
  }

  @CheckForNull
  @Override
  public String getIconFileName() {
    return PipelineConfigHistoryConsts.ICON_PATH;
  }

  @CheckForNull
  @Override
  public String getDisplayName() {
    return PipelineConfigHistoryConsts.DISPLAY_NAME;
  }

  @CheckForNull
  @Override
  public String getUrlName() {
    return PipelineConfigHistoryConsts.PLUGIN_BASE_PATH;
  }
}
