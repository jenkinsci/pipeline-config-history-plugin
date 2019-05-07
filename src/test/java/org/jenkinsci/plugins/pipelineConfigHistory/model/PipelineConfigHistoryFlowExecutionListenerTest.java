package org.jenkinsci.plugins.pipelineConfigHistory.model;
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

import hudson.model.Job;
import hudson.model.queue.QueueTaskFuture;
import jenkins.model.ParameterizedJobMixIn;
import org.jenkinsci.plugins.pipelineConfigHistory.PluginUtils;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PipelineConfigHistoryFlowExecutionListenerTest {

	@Rule
	public JenkinsRule jenkinsRule = new JenkinsRule();

	public static final String PIPELINE_NAME = "pipeline";
	public static final String SCRIPT = "none {\n" +
			"//nothing\n" +
			"}";

	public WorkflowJob pipelineProject;

	@Before
	public void setUpJenkins() throws Exception {
		jenkinsRule.createProject(WorkflowJob.class, PIPELINE_NAME);

		pipelineProject = (WorkflowJob) jenkinsRule.jenkins.getItem(PIPELINE_NAME);
		pipelineProject.setDefinition(new CpsFlowDefinition(SCRIPT, false));

		jenkinsRule.recipeLoadCurrentPlugin();

	}


	@Test
	public void onCompleted() throws Exception {
		createNewBuild(pipelineProject, SCRIPT);

		assertTrue(PluginUtils.getHistoryDao().isHistoryPresent(pipelineProject));

		//test minimal correctness
		Map<String, PipelineHistoryDescription> revisions = PluginUtils.getHistoryDao().getRevisions(pipelineProject);
		assertEquals(1, revisions.size());
		Optional<PipelineHistoryDescription> pipelineHistoryDescriptionOptional = revisions.values().stream().findAny();
		assertTrue(pipelineHistoryDescriptionOptional.isPresent());
		assertEquals(pipelineProject, pipelineHistoryDescriptionOptional.get().getWorkflowJob());
		assertEquals(pipelineProject.getFullName(), pipelineHistoryDescriptionOptional.get().getFullName());
		assertEquals(1, pipelineHistoryDescriptionOptional.get().getBuildNumber());
	}

	private void createNewBuild(WorkflowJob workflowJob, String script) throws Exception {
		workflowJob.setDefinition(new CpsFlowDefinition(script, false));

		WorkflowRun oldRun = workflowJob.getLastBuild();

		QueueTaskFuture f = new ParameterizedJobMixIn() {
			@Override protected Job asJob() {
				return workflowJob;
			}
		}.scheduleBuild2(0);

		//WAIT
		System.out.println("Build completed: " + f.get());
	}
}