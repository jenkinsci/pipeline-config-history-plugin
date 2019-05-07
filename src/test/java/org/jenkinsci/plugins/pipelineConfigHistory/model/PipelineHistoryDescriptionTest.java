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

import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;

import static org.junit.Assert.*;

public class PipelineHistoryDescriptionTest {

	final static String timestamp = "2019-04-11_12-46-05";
	final static String fullName = "full_name";

	@Rule
	public JenkinsRule jenkinsRule = new JenkinsRule();

	@Test
	public void getTimestamp() {
		assertEquals(timestamp, new PipelineHistoryDescription(timestamp, fullName, 1).getTimestamp());
	}

	@Test
	public void getFullName() {
		assertEquals(fullName, new PipelineHistoryDescription(timestamp, fullName, 1).getFullName());
	}

	@Test
	public void getWorkflowJob() throws IOException {
		WorkflowJob workflowJob = new WorkflowJob(jenkinsRule.jenkins, fullName);
		jenkinsRule.jenkins.add(workflowJob, fullName);

		assertEquals(workflowJob, new PipelineHistoryDescription(timestamp, fullName, 1).getWorkflowJob());
	}
}