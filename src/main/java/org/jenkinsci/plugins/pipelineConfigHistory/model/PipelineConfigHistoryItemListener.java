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
import hudson.model.listeners.ItemListener;
import org.jenkinsci.plugins.pipelineConfigHistory.PluginUtils;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import java.util.logging.Logger;

import static java.util.logging.Level.FINEST;

/**
 * Saves the pipeline config if the pipeline is created, removed, moved or renamed.
 */
@Extension
public class PipelineConfigHistoryItemListener extends ItemListener {

  private static final Logger LOG =
      Logger.getLogger(PipelineConfigHistoryItemListener.class.getName());


  @Override
  public void onCreated(Item item) {
    //do nothing! the pipeline has not been fetched, yet!
  }

  @Override
  public void onLocationChanged(Item item, String oldFullName, String newFullName) {
    if (isWorkflowJob(item)) {
      final String onLocationChangedDescription = "old full name: " + oldFullName
          + ", new full name: " + newFullName;
      LOG.log(FINEST, "In onLocationChanged for {0}{1}",
          new Object[] {item, onLocationChangedDescription});
      PluginUtils
          .getHistoryDao()
          .changeHistoryLocation((WorkflowJob) item, oldFullName, newFullName);
      LOG.log(FINEST, "Completed onLocationChanged for {0}", item);
    }
  }

  @Override
  public void onDeleted(Item item) {
    if (isWorkflowJob(item)) {
      LOG.log(FINEST, "In onDeleted for {0}", item);
      //explicitly do nothing. The project might be restored via jobConfigHistory.
      LOG.log(FINEST, "onDeleted for {0} done.", item);
    }
  }

  private boolean isWorkflowJob(Item item) {
    return item instanceof WorkflowJob;
  }
}
