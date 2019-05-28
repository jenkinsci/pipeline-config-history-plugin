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

import hudson.model.Job;
import hudson.model.queue.QueueTaskFuture;
import jenkins.model.ParameterizedJobMixIn;
import org.apache.commons.io.FileUtils;
import org.jenkinsci.plugins.pipelineConfigHistory.PipelineConfigHistoryConsts;
import org.jenkinsci.plugins.pipelineConfigHistory.model.PipelineHistoryDescription;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PipelineConfigHistoryProjectActionTest {

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
					"//nothing2\n" +
					"\n" +
					"}";
	public static final String SCRIPT_2_SYNTAX_HIGHLIGHTED =
			"<pre><code>none {\n" +
			"//nothing2\n" +
			"}</pre></code>";
	public static final String SCRIPT_SCRIPT2_DIFFLINES =
			"none {, none {\n" +
			"//nothing, //nothing2\n" +
			"}, }";

	public PipelineConfigHistoryProjectAction sut;
	public WorkflowJob workflowJob;

	@Before
	public void setUp() throws IOException {
		workflowJob = createWorkflowJob(PIPELINE_NAME, SCRIPT);
		sut = createSut(workflowJob);
	}

	@Test
	public void getPipelineHistoryDescriptions() throws Exception {
		List<PipelineHistoryDescription> pipelineHistoryDescriptions_0 = sut.getPipelineHistoryDescriptions();
		assertEquals(0, pipelineHistoryDescriptions_0.size());

		createNewBuild(workflowJob, SCRIPT);
		List<PipelineHistoryDescription> pipelineHistoryDescriptions_1 = sut.getPipelineHistoryDescriptions();
		assertEquals(1, pipelineHistoryDescriptions_1.size());
		assertEquals(workflowJob, pipelineHistoryDescriptions_1.get(0).getWorkflowJob());
		assertEquals(1, pipelineHistoryDescriptions_1.get(0).getBuildNumber());
		assertEquals(workflowJob.getFullName(), pipelineHistoryDescriptions_1.get(0).getFullName());

		//nothing changed --> no history
		createNewBuild(workflowJob, SCRIPT);
		List<PipelineHistoryDescription> pipelineHistoryDescriptions_2 = sut.getPipelineHistoryDescriptions();
		assertEquals(1, pipelineHistoryDescriptions_2.size());

		createNewBuild(workflowJob, SCRIPT_2);
		List<PipelineHistoryDescription> pipelineHistoryDescriptions_3 = sut.getPipelineHistoryDescriptions();
		assertEquals(2, pipelineHistoryDescriptions_3.size());

		createNewBuild(workflowJob, SCRIPT_3);
		List<PipelineHistoryDescription> pipelineHistoryDescriptions_4 = sut.getPipelineHistoryDescriptions();
		assertEquals(3, pipelineHistoryDescriptions_4.size());
	}

	@Test
	public void getPipelineHistoryDescription() throws Exception {
		createNewBuild(workflowJob, SCRIPT);
		PipelineHistoryDescription historyDescription = sut.getPipelineHistoryDescription(sut.getPipelineHistoryDescriptions().get(0).getTimestamp());
		assertEquals(sut.getPipelineHistoryDescriptions().get(0), historyDescription);
	}

	@Test
	public void getFileByHashCode() throws Exception {
		createNewBuild(workflowJob, SCRIPT);

		String timestamp = sut.getPipelineHistoryDescriptions().get(0).getTimestamp();
		File buildXml = new File(
				sut.getRevision(sut.getPipelineHistoryDescriptions().get(0).getTimestamp()),
				PipelineConfigHistoryConsts.BUILD_XML_FILENAME
		);

		assertEquals(buildXml, sut.getFileByHashCode(timestamp, Integer.toString(buildXml.hashCode())));
	}

	@Test
	public void getFileContentByHashCode() throws Exception {
		createNewBuild(workflowJob, SCRIPT);

		String timestamp = sut.getPipelineHistoryDescriptions().get(0).getTimestamp();
		File buildXml = new File(
				sut.getRevision(sut.getPipelineHistoryDescriptions().get(0).getTimestamp()),
				PipelineConfigHistoryConsts.BUILD_XML_FILENAME
		);
		String hashCode = Integer.toString(buildXml.hashCode());

		//syntax hl == false
		assertEquals(
				FileUtils.readFileToString(buildXml),
				sut.getFileContentByHashCode(timestamp, hashCode)
		);

		//TODO: syntax hl == true case.
	}

	@Test
	public void getScriptFromXmlFile() throws Exception {
		createNewBuild(workflowJob, SCRIPT_2);
		File revision = sut.getRevision(sut.getPipelineHistoryDescriptions().get(0).getTimestamp());

		assertEquals(
				SCRIPT_2,
				sut.getScriptFromXmlFile(
						new File(
								revision,
								PipelineConfigHistoryConsts.BUILD_XML_FILENAME),
						false
				)
		);
		assertEquals(
				SCRIPT_2_SYNTAX_HIGHLIGHTED,
				sut.getScriptFromXmlFile(
						new File(
								revision,
								PipelineConfigHistoryConsts.BUILD_XML_FILENAME),
						true
				)
		);
	}

	@Test
	public void getScriptFromXmlFile1() throws Exception {
		createNewBuild(workflowJob, SCRIPT_3);
		File buildXml = new File(
				sut.getRevision(sut.getPipelineHistoryDescriptions().get(0).getTimestamp()),
				PipelineConfigHistoryConsts.BUILD_XML_FILENAME
		);

		assertEquals(
				SCRIPT_3,
				sut.getScriptFromXmlFile(
						sut.getPipelineHistoryDescriptions().get(0).getTimestamp(),
						Integer.toString(buildXml.hashCode()),
						false
				)
		);
	}

	@Test
	public void getTimestampWellFormatted() {
		//not really testable..
	}

	@Test
	public void getFileContentByHashCode1() {

	}

	@Test
	public void getAllFilesFromDirectory() throws Exception {
		createNewBuild(workflowJob, SCRIPT_3);
		File revision = sut.getRevision(sut.getPipelineHistoryDescriptions().get(0).getTimestamp());

		File[] files = sut.getAllFilesExceptHistoryXmlFromDirectory(revision);

		assertEquals(1, files.length);
		assertEquals(2, revision.listFiles().length);

		FileUtils.write(new File(revision, "subDir/subSubDir/libFile.groovy"), "#!groovy\nnode(){}");
		FileUtils.write(new File(revision, "subDir/subSubDir2/libFile.groovy"), "");
		File[] files_2 = sut.getAllFilesExceptHistoryXmlFromDirectory(revision);
		assertEquals(3, files_2.length);
	}

	@Test
	public void getProject() {
		assertEquals(workflowJob, sut.getProject());
	}

	@Test
	public void isAnyMatchUnequal() throws Exception {
		createNewBuild(workflowJob, SCRIPT);
		createNewBuild(workflowJob, SCRIPT_2);
		createNewBuild(workflowJob, SCRIPT);

		String timestamp1 = sut.getPipelineHistoryDescriptions().get(0).getTimestamp();
		String timestamp2 = sut.getPipelineHistoryDescriptions().get(1).getTimestamp();
		String timestamp3 = sut.getPipelineHistoryDescriptions().get(2).getTimestamp();

		assertTrue(sut.isAnyMatchUnequal(timestamp1, timestamp2));
		assertTrue(sut.isAnyMatchUnequal(timestamp2, timestamp3));
		assertFalse(sut.isAnyMatchUnequal(timestamp1, timestamp3));
	}

	@Test
	public void getMatchingFiles() throws Exception {
		createNewBuild(workflowJob, SCRIPT);
		createNewBuild(workflowJob, SCRIPT_2);
		createNewBuild(workflowJob, SCRIPT);

		String timestamp1 = sut.getPipelineHistoryDescriptions().get(0).getTimestamp();
		String timestamp2 = sut.getPipelineHistoryDescriptions().get(1).getTimestamp();
		String timestamp3 = sut.getPipelineHistoryDescriptions().get(2).getTimestamp();

		List<Match> matches1_2 = sut.getMatchingFiles(timestamp1, timestamp2);
		List<Match> matches2_3 = sut.getMatchingFiles(timestamp2, timestamp3);

		assertEquals(1, matches1_2.size());
		assertEquals(Match.Kind.UNEQUAL, matches1_2.get(0).getKind());

		assertEquals(1, matches2_3.size());
		assertEquals(Match.Kind.UNEQUAL, matches2_3.get(0).getKind());
	}

	@Test
	public void getLines() throws Exception {
		//this should test all three getLines()-methods.
		createNewBuild(workflowJob, SCRIPT);
		createNewBuild(workflowJob, SCRIPT_2);

		String timestamp1 = sut.getPipelineHistoryDescriptions().get(0).getTimestamp();
		String timestamp2 = sut.getPipelineHistoryDescriptions().get(1).getTimestamp();
		List<Match> matches1_2 = sut.getMatchingFiles(timestamp1, timestamp2);

		assertEquals(
				SCRIPT_SCRIPT2_DIFFLINES,
				sut.getLines(matches1_2.get(0))
						.stream()
						.map(line -> line.getLeft().getText() + ", " + line.getRight().getText())
						.reduce((
								(line, line2) -> line + "\n" + line2)
						)
						.get()
		);
	}

	@Test
	public void doDiffFiles() {
		//TODO implement.
	}

	@Test
	public void getIconFileName() {
		assertEquals(PipelineConfigHistoryConsts.ICON_PATH, sut.getIconFileName());
	}

	@Test
	public void getDisplayName() {
		assertEquals(PipelineConfigHistoryConsts.DISPLAY_NAME, sut.getDisplayName());
	}

	@Test
	public void getUrlName() {
		assertEquals(PipelineConfigHistoryConsts.PLUGIN_BASE_PATH, sut.getUrlName());
	}

	@Test
	public void isBuiltfromReplay() throws Exception {
		createNewBuild(workflowJob, SCRIPT);
		createNewBuild(workflowJob, SCRIPT_2);

		String timestamp1 = sut.getPipelineHistoryDescriptions().get(0).getTimestamp();
		String timestamp2 = sut.getPipelineHistoryDescriptions().get(1).getTimestamp();

		assertFalse(sut.isBuiltfromReplay(timestamp1));
		assertFalse(sut.isBuiltfromReplay(timestamp2));


		String timestamp3 = sut.getPipelineHistoryDescriptions().get(2).getTimestamp();
		assertTrue(sut.isBuiltfromReplay(timestamp3));
	}

	@Test
	public void getOriginalNumberFromReplayBuild() throws Exception {
		createNewBuild(workflowJob, SCRIPT);
		createNewBuild(workflowJob, SCRIPT_2);

		String timestamp3 = sut.getPipelineHistoryDescriptions().get(2).getTimestamp();

		assertEquals(1, sut.getOriginalNumberFromReplayBuild(timestamp3));
	}

	@Test
	public void getBuildNumber() throws Exception {
		createNewBuild(workflowJob, SCRIPT);
		createNewBuild(workflowJob, SCRIPT_2);

		String timestamp1 = sut.getPipelineHistoryDescriptions().get(0).getTimestamp();
		String timestamp2 = sut.getPipelineHistoryDescriptions().get(1).getTimestamp();

		String timestamp3 = sut.getPipelineHistoryDescriptions().get(2).getTimestamp();

		assertEquals(1, sut.getBuildNumber(timestamp1));
		assertEquals(2, sut.getBuildNumber(timestamp2));
		assertEquals(3, sut.getBuildNumber(timestamp3));

	}

	private WorkflowJob createWorkflowJob(String name, String script) throws IOException {
		jenkinsRule.createProject(WorkflowJob.class, name);
		WorkflowJob workflowJob = (WorkflowJob) jenkinsRule.jenkins.getItem(name);
		workflowJob.setDefinition(new CpsFlowDefinition(script, false));
		return workflowJob;
	}

	private PipelineConfigHistoryProjectAction createSut(WorkflowJob workflowJob) {
		return new PipelineConfigHistoryProjectAction(workflowJob);
	}

	private void createNewBuild(WorkflowJob workflowJob, String script) throws Exception {
		workflowJob.setDefinition(new CpsFlowDefinition(script, false));

		QueueTaskFuture f = new ParameterizedJobMixIn() {
			@Override protected Job asJob() {
				return workflowJob;
			}
		}.scheduleBuild2(0);

		//WAIT
		System.out.println("Build completed: " + f.get());
	}
}