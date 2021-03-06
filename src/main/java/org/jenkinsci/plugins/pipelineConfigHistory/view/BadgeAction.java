/*
 * The MIT License
 *
 * Copyright (c) 2019, Jochen A. Fuerbacher
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

import org.jenkinsci.plugins.pipelineConfigHistory.Messages;
import org.jenkinsci.plugins.pipelineConfigHistory.PipelineConfigHistoryConsts;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import hudson.model.BuildBadgeAction;
import hudson.model.Run;
import jenkins.model.Jenkins;
import jenkins.model.RunAction2;

public class BadgeAction implements BuildBadgeAction, RunAction2 {

  private String url;
  
  private transient WorkflowRun run;

  public BadgeAction(String url) {
    this.url = url;
  }

  @Override
  public String getIconFileName() {
    return PipelineConfigHistoryConsts.BADGE_ACTION_ICON_PATH;
  }

  @Override
  public String getDisplayName() {
    return PipelineConfigHistoryConsts.DISPLAY_NAME;
  }

  @Override
  public String getUrlName() {
    return "/" + run.getParent().getUrl() + this.url;
  }

  public String getTooltip() {
    return Messages.BadgeAction_ToolTip();
  }

  @Override
  public void onAttached(Run<?, ?> r) {
	  run = (WorkflowRun)r;
  }

  @Override
  public void onLoad(Run<?, ?> r) {
	  run = (WorkflowRun)r;
  }
}
