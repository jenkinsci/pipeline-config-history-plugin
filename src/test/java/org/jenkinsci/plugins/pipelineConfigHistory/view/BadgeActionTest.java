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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.model.CauseAction;
import hudson.model.queue.QueueTaskFuture;

public class BadgeActionTest {

  private static final String PIPELINE_NAME = "pipeline";

  private static final String SCRIPT = "node () { }";
  private static final String SCRIPT_2 = "node () { // \n}";

  private WorkflowJob pipeline;

  @Rule
  public JenkinsRule j = new JenkinsRule();

  @Before
  public void setUp() throws IOException {
    pipeline = createWorkflowJob(PIPELINE_NAME, SCRIPT);
  }

  @Test
  public void testBadgeActionOneBuild() throws InterruptedException, ExecutionException {
    QueueTaskFuture<WorkflowRun> future = pipeline.scheduleBuild2(0, new CauseAction());
    WorkflowRun run = future.get();
    assertNull(run.getAction(BadgeAction.class));
  }

  @Test
  public void testBadgeActionTwoBuildsWithNoChanges() throws InterruptedException, ExecutionException {
    QueueTaskFuture<WorkflowRun> future = pipeline.scheduleBuild2(0, new CauseAction());
    WorkflowRun firstRun = future.get();

    QueueTaskFuture<WorkflowRun> future2 = pipeline.scheduleBuild2(0, new CauseAction());
    WorkflowRun secondRun = future2.get();

    assertNull(firstRun.getAction(BadgeAction.class));
    assertNull(secondRun.getAction(BadgeAction.class));
	
    assertEquals(2, pipeline.getBuildsAsMap().size());
  }

  @Test
  public void testBadgeActionTwoBuildsWithChanges() throws InterruptedException, ExecutionException {
    QueueTaskFuture<WorkflowRun> future = pipeline.scheduleBuild2(0, new CauseAction());
    WorkflowRun firstRun = future.get();

    pipeline.setDefinition(new CpsFlowDefinition(SCRIPT_2, false));

    QueueTaskFuture<WorkflowRun> future2 = pipeline.scheduleBuild2(0, new CauseAction());
    WorkflowRun secondRun = future2.get();

    assertNull(firstRun.getAction(BadgeAction.class));
    assertNotNull(secondRun.getAction(BadgeAction.class));

    assertEquals(2, pipeline.getBuildsAsMap().size());

    String url = secondRun.getAction(BadgeAction.class).getUrlName();
    assertTrue(url.matches("http://localhost:\\d{1,5}/jenkins/job/" 
      + PIPELINE_NAME 
      + "/pipeline-config-history/showAllDiffs\\"
      + "?timestamp1=\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}"
      + "\\&timestamp2=\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}"
      + "\\&anyDiffExists=true"));
  }
	
  private WorkflowJob createWorkflowJob(String name, String script) throws IOException {
    WorkflowJob workflowJob = j.createProject(WorkflowJob.class, name);
    workflowJob.setDefinition(new CpsFlowDefinition(script, false));
    return workflowJob;
  }

}
