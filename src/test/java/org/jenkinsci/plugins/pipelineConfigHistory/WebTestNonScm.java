package org.jenkinsci.plugins.pipelineConfigHistory;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientUtil;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.model.Job;
import hudson.model.queue.QueueTaskFuture;
import jenkins.model.ParameterizedJobMixIn;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;


import java.io.IOException;

import static org.junit.Assert.*;

public class WebTestNonScm {

  @Rule
  public JenkinsRule jenkinsRule = new JenkinsRule();

  public static final String PIPELINE_NAME = "pipeline";
  public static final String PIPELINE_NEW_NAME = "pipeline_new";
  public static final String SCRIPT = "node {\n" +
      "//nothing\n" +
      "}";
  public static final String SCRIPT_2 =
      "node {\n" +
          "//nothing2\n" +
          "}";
  public static final String SCRIPT_3 =
      "node {\n" +
          "//nothing2\n" +
          "\n" +
          "}";

  public WorkflowJob workflowJob;
  private HtmlPage currentPage;
  private String currentIndexPageAsXml;
  private String currentIndexPageAsText;


  /*
  Simple
   */
  @Test
  public void indexTest() throws Exception {
    workflowJob = createWorkflowJob(PIPELINE_NAME, SCRIPT);


    try (final WebClient webClient = jenkinsRule.createWebClient()) {
      WebClientUtil.ExceptionListener exceptionListener = WebClientUtil.addExceptionListener(webClient);
      // Make sure all background JavaScript has completed so as expected exceptions have been thrown.
      WebClientUtil.waitForJSExec(webClient);


      System.out.println("#################URL: " + jenkinsRule.getURL() + workflowJob.getUrl() + "pipelineConfigHistory/");
      currentPage = webClient.getPage(jenkinsRule.getURL() + workflowJob.getUrl() + "pipelineConfigHistory/");
      Assert.assertEquals(PipelineConfigHistoryConsts.DISPLAY_NAME + " [Jenkins]", currentPage.getTitleText());
      currentIndexPageAsXml = currentPage.asXml();
      currentIndexPageAsText = currentPage.asText();

      assertTrue(currentIndexPageAsXml.contains("No pipeline configuration history available."));
      assertFalse(currentIndexPageAsXml.contains("Show Configuration"));

      createNewBuild(workflowJob, SCRIPT);
      createNewBuild(workflowJob, SCRIPT_2);
      assertTrue(PluginUtils.getHistoryDao().isHistoryPresent(workflowJob));

      currentPage = webClient.getPage(jenkinsRule.getURL() + workflowJob.getUrl() + "pipelineConfigHistory/");
      refresh();

      assertTrue(currentIndexPageAsText.contains("Show Configuration"));
      assertFalse(currentIndexPageAsText.contains("No pipeline configuration history available."));
      assertTrue(currentIndexPageAsText.contains("Build #1"));
      assertTrue(currentIndexPageAsText.contains("Build #2"));

//
//      final String pageAsXml = indexPage.asXml();
//      Assert.assertTrue(pageAsXml.contains("<body class=\"composite\">"));
//
//      final String pageAsText = indexPage.asText();
//      Assert.assertTrue(pageAsText.contains("Support for the HTTP and HTTPS protocols"));
    }
//    jenkinsRule.submit(HtmlForm.)
  }

  private WorkflowJob createWorkflowJob(String name, String script) throws IOException {
    jenkinsRule.createProject(WorkflowJob.class, name);
    WorkflowJob workflowJob = (WorkflowJob) jenkinsRule.jenkins.getItem(name);
    workflowJob.setDefinition(new CpsFlowDefinition(script, false));
    return workflowJob;
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

  private void refresh() throws IOException {
    //currentPage.refresh(); won't work unfortunately

    currentIndexPageAsXml = currentPage.asXml();
    currentIndexPageAsText = currentPage.asText();
  }

}