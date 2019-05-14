package org.jenkinsci.plugins.pipelineConfigHistory;
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

import hudson.XmlFile;
import hudson.model.Run;
import hudson.scm.ChangeLogParser;
import hudson.scm.ChangeLogSet;
import hudson.scm.RepositoryBrowser;
import hudson.scm.SCM;
import org.apache.commons.io.FileUtils;
import org.jenkinsci.plugins.pipelineConfigHistory.model.PipelineItemHistoryDao;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import static org.junit.Assert.*;

public class PluginUtilsTest {

	final String filePath = "test/123/4";
	final String someFileName = "329nvb3892fdn223d9283fk3d8f2";
	final String someFileContent =
			"line1 10fn329m ,.39 " +
			"\n lin223f9 " +
			"\n l3 209n dm" +
			"\n ";
	/**
	 * tag which contains the original number.
	 */
	final String actionsTag = "<actions>\n" +
			"    <hudson.model.CauseAction>\n" +
			"      <causeBag class=\"linked-hash-map\">\n" +
			"        <entry>\n" +
			"          <hudson.model.Cause_-UserIdCause/>\n" +
			"          <int>1</int>\n" +
			"        </entry>\n" +
			"        <entry>\n" +
			"          <org.jenkinsci.plugins.workflow.cps.replay.ReplayCause plugin=\"workflow-cps@2.57.3\">\n" +
			"            <originalNumber>12</originalNumber>\n" +
			"          </org.jenkinsci.plugins.workflow.cps.replay.ReplayCause>\n" +
			"          <int>1</int>\n" +
			"        </entry>\n" +
			"      </causeBag>\n" +
			"    </hudson.model.CauseAction>\n" +
			"  </actions>\n";
	final String xmlFileWithScript1Content =
			"<?xml version='1.1' encoding='UTF-8'?>\n" +
					"<flow-build plugin=\"workflow-job@2.32-SNAPSHOT\">\n" +
					actionsTag +
					"  <execution class=\"org.jenkinsci.plugins.workflow.cps.CpsFlowExecution\">\n" +
					"    <result>SUCCESS</result>\n" +
					"    <script>#!groovy\n" +
					"\n" +
					"//ssh://abc.de.git\n" +
					"@Library(&apos;access&apos;) _\n" +
					"\n" +
					"//Run pipeline\n" +
					"//accessPipeline()\n" +
					"node {\n" +
					"    stage(&apos;s1123&apos;) {\n" +
					"        echo &apos;Helloooooo Worldd&apos;\n" +
					"    }\n" +
					"    stage(&apos;s2&apos;) {\n" +
					"        echo &apos;Hello orlda&apos;\n" +
					"    }\n" +
					"    stage(&apos;s3&apos;) {\n" +
					"        echo &apos;Hellod &apos;\n" +
					"    }\n" +
					"}\n" +
					"//pipelinePostSteps([test:&quot;testValue&quot;, otherTest:&quot;otherTestValue&quot;])\n" +
					"</script>\n" +
					"  </execution>\n" +
					"</flow-build>\n";

	final String xmlFileWithScript2Content =
			"<?xml version='1.1' encoding='UTF-8'?>\n" +
					"<flow-build plugin=\"workflow-job@2.32-SNAPSHOT\">\n" +
					"  <execution class=\"org.jenkinsci.plugins.workflow.cps.CpsFlowExecution\">\n" +
					"    <result>SUCCESS</result>\n" +
					"    <script>#!groovy\n" +
					"\n" +
					"//ssh://abc.de.git\n" +
					"@Library(&apos;access&apos;) _\n" +
					"\n" +
					"//Run pipeline\n" +
					"//accessPipeline()\n" +
					"node {\n" +
					"    stage(&apos;s1123&apos;) {\n" +
					"        echo &apos;Helloooooo Worldd&apos;\n" +
					"    }\n" +
					"    stage(&apos;s3&apos;) {\n" +
					"        echo &apos;Hellod &apos;\n" +
					"    }\n" +
					"}\n" +
					"//pipelinePostSteps([test:&quot;testValue&quot;, otherTest:&quot;otherTestValue&quot;])\n" +
					"</script>\n" +
					"  </execution>\n" +
					"</flow-build>\n";

	@Rule
	public JenkinsRule jenkinsRule = new JenkinsRule();

	@Test
	public void getHistoryDao() {
		PipelineItemHistoryDao historyDao =	PluginUtils.getHistoryDao();
		assertNotEquals(null, historyDao);
	}

	@Test
	public void getLibDir() {
		assertEquals(filePath + "/libs", PluginUtils.getLibDir(new File(filePath)).getPath());
	}

	@Test
	public void getBuildDotXml() {
		assertEquals(filePath + "/build.xml", PluginUtils.getBuildXml(new File(filePath)).getFile().getPath());
	}

	@Test
	public void fileToString() throws IOException {
		assertEquals("", PluginUtils.fileToString(null));

		//some existing file.
		File someFileOnDisk = new File(jenkinsRule.jenkins.getRootDir(), someFileName);
		FileUtils.write(someFileOnDisk, someFileContent);

		assertEquals(someFileContent, PluginUtils.fileToString(someFileOnDisk));
	}

	@Test
	public void getWorkflowJob() throws IOException {
		String workflowJobName = "12d0n";
		WorkflowJob workflowJob = new WorkflowJob(jenkinsRule.jenkins, workflowJobName);
		jenkinsRule.jenkins.add(workflowJob, workflowJobName);
		assertEquals(workflowJob, PluginUtils.getWorkflowJob(workflowJobName));
	}

	@Test
	public void getJenkinsRootDir() {
		assertEquals(jenkinsRule.jenkins.getInstance().root.getPath(), PluginUtils.getJenkinsRootDir().getPath());
	}

	@Test
	public void scriptInXmlFileIsEqual() throws IOException, ParserConfigurationException, SAXException {
		String fileName1 = someFileName +".xml";
		String fileName2  = someFileName +"_2.xml";
		String fileName2_clone  = someFileName +"_2_clone.xml";

		File file1 = new File(jenkinsRule.jenkins.getRootDir(), fileName1);
		File file2 = new File(jenkinsRule.jenkins.getRootDir(), fileName2);
		File file2Clone = new File(jenkinsRule.jenkins.getRootDir(), fileName2_clone);

		FileUtils.write(file1, xmlFileWithScript1Content);
		FileUtils.write(file2, xmlFileWithScript2Content);
		FileUtils.write(file2Clone, xmlFileWithScript2Content);

		XmlFile xmlFile1 = new XmlFile(file1);
		XmlFile xmlFile2 = new XmlFile(file2);
		XmlFile xmlFile2Clone = new XmlFile((file2Clone));

		assertTrue(PluginUtils.scriptInXmlFileIsEqual(xmlFile1, xmlFile1));
		assertTrue(PluginUtils.scriptInXmlFileIsEqual(xmlFile2, xmlFile2));

		assertFalse(PluginUtils.scriptInXmlFileIsEqual(xmlFile1, xmlFile2));
		assertTrue(PluginUtils.scriptInXmlFileIsEqual(xmlFile2, xmlFile2Clone));
	}

	@Test
	public void computeXmlDiff() throws IOException {
		String fileName1 = someFileName +".xml";
		String fileName2  = someFileName +"_2.xml";
		String fileName2_clone  = someFileName +"_2_clone.xml";

		File file1 = new File(jenkinsRule.jenkins.getRootDir(), fileName1);
		File file2 = new File(jenkinsRule.jenkins.getRootDir(), fileName2);
		File file2Clone = new File(jenkinsRule.jenkins.getRootDir(), fileName2_clone);

		FileUtils.write(file1, xmlFileWithScript1Content);
		FileUtils.write(file2, xmlFileWithScript2Content);
		FileUtils.write(file2Clone, xmlFileWithScript2Content);

		XmlFile xmlFile1 = new XmlFile(file1);
		XmlFile xmlFile2 = new XmlFile(file2);
		XmlFile xmlFile2Clone = new XmlFile((file2Clone));

		assertTrue(PluginUtils.computeXmlDiff(xmlFile1, xmlFile2).hasDifferences());
		assertTrue(PluginUtils.computeXmlDiff(xmlFile1, xmlFile2Clone).hasDifferences());
		assertFalse(PluginUtils.computeXmlDiff(xmlFile2, xmlFile2Clone).hasDifferences());
	}

	@Test
	public void getOriginalNumberFromBuildXml() throws IOException, ParserConfigurationException, SAXException {
		String fileName = someFileName + ".xml";
		File file = new File(jenkinsRule.jenkins.getRootDir(), fileName);
		FileUtils.write(file, xmlFileWithScript1Content);

		assertEquals(12, PluginUtils.getOriginalNumberFromBuildXml(file));
	}

	@Test
	public void getJenkinsfilePath() throws IOException {
		String workflowJobName = "12d0n";
		WorkflowJob workflowJob = new WorkflowJob(jenkinsRule.jenkins, workflowJobName);
		jenkinsRule.jenkins.add(workflowJob, workflowJobName);

		final String scriptPath = "github.com/test1234";
		SCM scm = new SCM() {
          @Override
          public ChangeLogParser createChangeLogParser() {
            return null;
          }
          @Override
          public String getKey() {
            return scriptPath;
          }
        };

		workflowJob.setDefinition(new CpsScmFlowDefinition(scm, scriptPath));

		assertEquals(scriptPath, PluginUtils.getJenkinsfilePath(workflowJob));
	}
}