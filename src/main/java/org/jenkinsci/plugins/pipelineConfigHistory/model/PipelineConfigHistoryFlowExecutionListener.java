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

import hudson.Extension;
import hudson.model.Item;
import jenkins.model.Jenkins;
import org.apache.commons.lang.SystemUtils;
import org.jenkinsci.plugins.pipelineConfigHistory.PluginUtils;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.flow.FlowExecutionListener;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

/**
 * This only listens to builds (FlowExecution).
 * Creation, deletion etc. of the respective job are handled elsewhere.
 *
 * @author Robin Schulz
 */
@Extension
public class PipelineConfigHistoryFlowExecutionListener extends FlowExecutionListener {

  private static final Logger LOG = Logger
      .getLogger(PipelineConfigHistoryFlowExecutionListener.class.getName());

  @Override
  public void onCompleted(@Nonnull FlowExecution execution) {
    LOG.log(Level.FINEST, "Pipeline config history triggered.");

    //get the pipeline job's name.
    Optional<String> pipelineNameOptional = getPipelineFullName(execution);
    if (!pipelineNameOptional.isPresent()) {
      LOG.log(Level.WARNING, "build url could not be parsed!");
      return;
    }

    //get the corresponding Jenkins job and cast it to a pipeline job.

    Item pipelineItem = Jenkins.get().getItemByFullName(pipelineNameOptional.get());
    if (pipelineItem == null) {
      LOG.log(Level.WARNING, "pipeline item could not be found.");
      return;
    } else if (!(pipelineItem instanceof WorkflowJob)) {
      LOG.log(Level.SEVERE, "given job is a job but not a pipeline: {0}!",
          pipelineItem.getClass());
      return;
    }
    WorkflowJob workflowJob = (WorkflowJob) pipelineItem;


    //do the I/O
    PipelineItemHistoryDao historyDao = PluginUtils.getHistoryDao();

    if (!historyDao.isHistoryPresent(workflowJob)) {
      try {
        historyDao.createHistory(workflowJob, getBuildNumber(execution, workflowJob));
      } catch (IOException e) {
        LOG.log(Level.WARNING, "pipeline config could not be created: {0}", e.getMessage());
      }
    } else {
      try {
        historyDao.updateHistory(workflowJob, getBuildNumber(execution, workflowJob));
      } catch (IOException e) {
        LOG.log(Level.WARNING, "pipeline config could not be updated: {0}", e.getMessage());
      }
    }
  }

  private Optional<String> getPipelineFullName(@Nonnull FlowExecution flowExecution) {
    final String jobPlusSeparator = "job" + File.separator;
    String jobPlusSeparatorForRegex = "job" + File.separator;
    if (SystemUtils.IS_OS_WINDOWS) {
      //File.separator is not enough, it needs to be escaped...
      jobPlusSeparatorForRegex = "job" + "\\\\";
    }

    File flowExecutionOwner;
    try {
      flowExecutionOwner = new File(flowExecution.getOwner().getUrl());
      //it always starts with "job/"
      if (!flowExecutionOwner.toString().startsWith(jobPlusSeparator)) {
        return Optional.empty();
      } else {
        return Optional.of(
            flowExecutionOwner.getParentFile()
                .toString()
                .replaceAll(jobPlusSeparatorForRegex, "")
                .replaceAll("%20", " ")
        );
      }
    } catch (IOException e) {
      LOG.log(Level.SEVERE, "Error parsing the Pipeline Execution's URL: "
          + e.getLocalizedMessage());
      return Optional.empty();
    }
  }

  private int getBuildNumber(@Nonnull FlowExecution flowExecution, WorkflowJob workflowJob) {
    // this is done to prevent two racing builds from destroying the history
    // by simply calling workflowJob.getLastBuild().number
    File flowExecutionOwner;
    String errorMessage =
        "build number has to be taken from a the workflowjob's \"last Build\""
            + ".Inconsistencies might occur!";

    try {
      flowExecutionOwner = new File(flowExecution.getOwner().getUrl());
      //it always starts with "job/"
      if (!flowExecutionOwner.toString().startsWith("job/")) {
        LOG.log(Level.WARNING, errorMessage);
        return workflowJob.getLastBuild().number;
      } else {
        return Integer.parseInt(flowExecutionOwner.getName());
      }
    } catch (IOException | NumberFormatException e) {
      LOG.log(Level.WARNING, errorMessage);
      return workflowJob.getLastBuild().number;
    }
  }
}
