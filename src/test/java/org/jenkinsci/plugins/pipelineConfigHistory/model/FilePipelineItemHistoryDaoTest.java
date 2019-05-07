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

import org.jenkinsci.plugins.pipelineConfigHistory.PluginUtils;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Map;
import java.util.Optional;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;

public class FilePipelineItemHistoryDaoTest {

	@Rule
	public JenkinsRule jenkinsRule = new JenkinsRule();

	public static final String PIPELINE_NAME = "pipeline";
	public static final String PIPELINE_NEW_NAME = "pipeline_new";
	public static final String SCRIPT = "none {\n" +
			"//nothing\n" +
			"}";
	public static final String SCRIPT_2 =
			"none {\n" +
			"//nothing2\n" +
			"}";
	public static final String SCRIPT_3 =
			"none {\n" +
			"//nothing3\n" +
			"}";

	public WorkflowJob pipelineProject;

	@Before
	public void setUpJenkins() throws Exception {
		jenkinsRule.createProject(WorkflowJob.class, PIPELINE_NAME);
		pipelineProject = (WorkflowJob) jenkinsRule.jenkins.getItem(PIPELINE_NAME);

		pipelineProject.setDefinition(new CpsFlowDefinition(SCRIPT, false));

	}


	private void createNewBuild(WorkflowJob workflowJob, String script) throws Exception {
		pipelineProject.setDefinition(new CpsFlowDefinition(script, false));

		WorkflowRun oldRun = workflowJob.getLastBuild();

		jenkinsRule.buildAndAssertSuccess(workflowJob);

		await().until(() -> oldRun != workflowJob.getLastBuild());

		WorkflowRun lastRun = workflowJob.getLastBuild();
		System.out.println("Build completed:" + lastRun + ": "
				+ lastRun.getBuildStatusSummary().message);
	}

	@Test
	public void createHistory() throws Exception {
		createNewBuild(pipelineProject, SCRIPT);
		PluginUtils.getHistoryDao().createHistory(pipelineProject, 1);
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

	@Test
	public void updateHistory() throws Exception {
		createNewBuild(pipelineProject, SCRIPT);
		PluginUtils.getHistoryDao().createHistory(pipelineProject, 1);
		assertEquals(1, PluginUtils.getHistoryDao().getRevisions(pipelineProject).size());

		createNewBuild(pipelineProject, SCRIPT_2);
		PluginUtils.getHistoryDao().updateHistory(pipelineProject, 2);
		assertEquals(2, PluginUtils.getHistoryDao().getRevisions(pipelineProject).size());

		//same script, no update!
		createNewBuild(pipelineProject, SCRIPT_2);
		PluginUtils.getHistoryDao().updateHistory(pipelineProject, 3);
		assertEquals(2, PluginUtils.getHistoryDao().getRevisions(pipelineProject).size());

		//now a new script
		createNewBuild(pipelineProject, SCRIPT_3);
		PluginUtils.getHistoryDao().updateHistory(pipelineProject, 4);
		assertEquals(3, PluginUtils.getHistoryDao().getRevisions(pipelineProject).size());

		//now the first script again
		createNewBuild(pipelineProject, SCRIPT);
		PluginUtils.getHistoryDao().updateHistory(pipelineProject, 5);
		assertEquals(4, PluginUtils.getHistoryDao().getRevisions(pipelineProject).size());
	}

	@Test
	public void deleteHistory() throws Exception {
		PipelineItemHistoryDao historyDao = PluginUtils.getHistoryDao();

		createNewBuild(pipelineProject, SCRIPT);
		historyDao.createHistory(pipelineProject, 1);

		createNewBuild(pipelineProject, SCRIPT_2);
		historyDao.updateHistory(pipelineProject, 2);
		assertEquals(2, historyDao.getRevisions(pipelineProject).size());


		int oldSize = historyDao.getRevisions(pipelineProject).size();
		boolean oldExisting = false;
		if (historyDao instanceof FilePipelineItemHistoryDao) {
			oldExisting = ((FilePipelineItemHistoryDao) historyDao)
					.getHistoryRootDirectory(pipelineProject).exists();
		}

		PluginUtils.getHistoryDao().deleteHistory(pipelineProject);

		int newSize = historyDao.getRevisions(pipelineProject).size();
		boolean newExisting = false;
		if (historyDao instanceof FilePipelineItemHistoryDao) {
			newExisting = ((FilePipelineItemHistoryDao) historyDao)
					.getHistoryRootDirectory(pipelineProject).exists();
		}


		//check deletion within the plugin.
		assertEquals(0, newSize);
		assertNotEquals(oldSize, newSize);

		if (historyDao instanceof FilePipelineItemHistoryDao) {
			//check file system deletion
			assertEquals(true, oldExisting);
			assertNotEquals(oldExisting, newExisting);
		}
	}

	@Test
	public void changeHistoryLocation() throws Exception {
		createNewBuild(pipelineProject, SCRIPT);
		PluginUtils.getHistoryDao().createHistory(pipelineProject, 1);
		System.out.println(PluginUtils.getHistoryDao().getRevisions(pipelineProject).size());
		int formerSize = PluginUtils.getHistoryDao().getRevisions(pipelineProject).size();
		pipelineProject.renameTo(PIPELINE_NEW_NAME);

		assertEquals(formerSize, PluginUtils.getHistoryDao().getRevisions(pipelineProject).size());
	}

	@Test
	public void rename() throws Exception {
		createNewBuild(pipelineProject, SCRIPT);
		PluginUtils.getHistoryDao().createHistory(pipelineProject, 1);
		System.out.println(PluginUtils.getHistoryDao().getRevisions(pipelineProject).size());
		int formerSize = PluginUtils.getHistoryDao().getRevisions(pipelineProject).size();
		pipelineProject.renameTo(PIPELINE_NEW_NAME);

		assertEquals(formerSize, PluginUtils.getHistoryDao().getRevisions(pipelineProject).size());
	}

	@Test
	public void getMostRecentRevision() throws Exception {
		String oldTimestamp = "";
		String newTimestamp = "";

		createNewBuild(pipelineProject, SCRIPT);
		PluginUtils.getHistoryDao().createHistory(pipelineProject, 1);
		newTimestamp = PluginUtils.getHistoryDao().getMostRecentRevision(pipelineProject).getName();
		assertNotEquals(oldTimestamp, newTimestamp);

		createNewBuild(pipelineProject, SCRIPT_2);
		PluginUtils.getHistoryDao().updateHistory(pipelineProject, 2);
		oldTimestamp = newTimestamp;
		newTimestamp = PluginUtils.getHistoryDao().getMostRecentRevision(pipelineProject).getName();
		assertNotEquals(oldTimestamp, newTimestamp);

		//same script, no update!
		createNewBuild(pipelineProject, SCRIPT_2);
		PluginUtils.getHistoryDao().updateHistory(pipelineProject, 3);
		oldTimestamp = newTimestamp;
		newTimestamp = PluginUtils.getHistoryDao().getMostRecentRevision(pipelineProject).getName();
		assertEquals(oldTimestamp, newTimestamp);

		//now a new script
		createNewBuild(pipelineProject, SCRIPT_3);
		PluginUtils.getHistoryDao().updateHistory(pipelineProject, 4);
		oldTimestamp = newTimestamp;
		newTimestamp = PluginUtils.getHistoryDao().getMostRecentRevision(pipelineProject).getName();
		assertNotEquals(oldTimestamp, newTimestamp);
	}

	@Test
	public void getRevision() throws Exception {
		String timestamp = "";

		createNewBuild(pipelineProject, SCRIPT);
		PluginUtils.getHistoryDao().createHistory(pipelineProject, 1);
		timestamp = PluginUtils.getHistoryDao().getMostRecentRevision(pipelineProject).getName();
		assertEquals(timestamp, PluginUtils.getHistoryDao().getRevision(pipelineProject, timestamp).getName());

		createNewBuild(pipelineProject, SCRIPT_2);
		PluginUtils.getHistoryDao().updateHistory(pipelineProject, 2);
		timestamp = PluginUtils.getHistoryDao().getMostRecentRevision(pipelineProject).getName();
		assertEquals(timestamp, PluginUtils.getHistoryDao().getRevision(pipelineProject, timestamp).getName());
	}

	@Test
	public void getRevision1() throws Exception {
		String timestamp = "";

		createNewBuild(pipelineProject, SCRIPT);
		PluginUtils.getHistoryDao().createHistory(pipelineProject, 1);
		timestamp = PluginUtils.getHistoryDao().getMostRecentRevision(pipelineProject).getName();
		assertEquals(
				timestamp,
				PluginUtils.getHistoryDao()
						.getRevision(new PipelineHistoryDescription(timestamp, pipelineProject.getFullName(), 1))
						.getName()
		);

		createNewBuild(pipelineProject, SCRIPT_2);
		PluginUtils.getHistoryDao().updateHistory(pipelineProject, 2);
		timestamp = PluginUtils.getHistoryDao().getMostRecentRevision(pipelineProject).getName();
		assertEquals(
				timestamp,
				PluginUtils.getHistoryDao()
						.getRevision(new PipelineHistoryDescription(timestamp, pipelineProject.getFullName(), 1))
						.getName()
		);
	}
}