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

import hudson.Extension;
import hudson.model.Action;
import jenkins.model.TransientActionFactory;
import org.jenkinsci.plugins.pipelineConfigHistory.PluginUtils;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

@Extension
public class PipelineConfigHistoryActionFactory extends TransientActionFactory<WorkflowJob> {

  private static final Logger LOG = Logger
      .getLogger(PipelineConfigHistoryActionFactory.class.getName());

  @Override
  public Class<WorkflowJob> type() {
    return WorkflowJob.class;
  }

  @Nonnull
  @Override
  public Collection<? extends Action> createFor(@Nonnull WorkflowJob abstractItem) {

    if (!PluginUtils.requiredPluginsInstalled()) {
      LOG.log(Level.WARNING, "Required plugins missing: "
          + PluginUtils.getMissingRequiredPlugins());
      return Collections.emptyList();
    }

    final PipelineConfigHistoryProjectAction newAction = new PipelineConfigHistoryProjectAction(
        abstractItem);

    return Collections.singleton(newAction);
  }
}
