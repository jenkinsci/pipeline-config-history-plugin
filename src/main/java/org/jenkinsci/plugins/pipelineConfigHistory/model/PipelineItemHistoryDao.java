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

import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.SortedMap;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Config file I/O.
 */
public interface PipelineItemHistoryDao {

  /**
   * Creates the history entry of this WorkflowJob.
   *
   * @param workflowJob the Pipeline Job.
   * @param buildNumber the build number to be associated to the history entry.
   * @throws IOException if creating a history entry fails on IO level.
   */
  void createHistory(WorkflowJob workflowJob, int buildNumber) throws IOException;

  /**
   * Updates the history entry of this WorkflowJob.
   *
   * @param workflowJob the Pipeline Job.
   * @param buildNumber the build number to be associated to the history entry.
   * @return if the history got updated,
   * @throws IOException if updating the history fails on IO level.
   */
  boolean updateHistory(WorkflowJob workflowJob, int buildNumber) throws IOException;

  /**
   * Deletes the history of this WorkflowJob, if present.
   *
   * @param workflowJob the Pipeline Job.
   * @throws IOException if deleting the history fails on IO level.
   */
  void deleteHistory(WorkflowJob workflowJob) throws IOException;

  /**
   * Change the history of this WorkflowJob, if present.
   *
   * @param workflowJob the Pipeline Job.
   * @param oldFullName the old full name (its folder-including tree path)
   * @param newFullName the new full name
   */
  void changeHistoryLocation(WorkflowJob workflowJob, String oldFullName, String newFullName);

  /**
   * Returns an old revision.
   *
   * @param workflowJob the workflowJob
   * @param identifier the identifier
   * @throws FileNotFoundException if the revision id'd by the identifier can't be found.
   * @return the wanted revision identified by the workflowjob and a revision identifier.
   */ //TODO returning Files should not be part of the Dao!
  File getRevision(WorkflowJob workflowJob, String identifier) throws FileNotFoundException;

  File getRevision(PipelineHistoryDescription pipelineHistoryDescription)
      throws FileNotFoundException;

  File getMostRecentRevision(WorkflowJob workflowJob);

  boolean isBuiltfromReplay(PipelineHistoryDescription pipelineHistoryDescription)
      throws IOException;

  int getOriginalBuildNumberFromReplay(PipelineHistoryDescription pipelineHistoryDescription)
      throws IOException, SAXException, ParserConfigurationException;

  /**
   * For a given Pipeline Job, return all revisions.
   *
   * @param workflowJob the pipeline job
   * @throws IOException if retrieving the history revisions fails on IO level.
   * @return all revisions the pipeline job has.
   */
  SortedMap<String, PipelineHistoryDescription> getRevisions(final WorkflowJob workflowJob)
      throws IOException;

  /**
   * Determines whether there are history entries present or not.
   *
   * @param workflowJob the Pipeline Job.
   * @return whether there are history entries present or not
   */
  boolean isHistoryPresent(WorkflowJob workflowJob);
}
