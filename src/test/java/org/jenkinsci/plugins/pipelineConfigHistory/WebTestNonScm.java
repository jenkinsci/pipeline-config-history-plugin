package org.jenkinsci.plugins.pipelineConfigHistory;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientUtil;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.host.event.Event;
import hudson.model.Job;
import hudson.model.queue.QueueTaskFuture;
import jenkins.model.ParameterizedJobMixIn;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.jvnet.hudson.test.JenkinsRule;


import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Simple test of a non scm hosted pipeline (stored somewhere in jenkins' home dir).
 * The tests build on each other, so if index fails on some critical condition (e.g. nothing is displayed),
 * everything else will probably fail, too.
 *
 * To assure that the tests are executed in the desired order, they are executed alphabetically.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//TODO handle all these exceptions spamming the console...
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
  private final String configOverviewString = "configOverview";
  private final String configSingleFileString = "configSingleFile";


  @Test
  public void test_0_indexTest() throws Exception {
    workflowJob = createWorkflowJob(PIPELINE_NAME, SCRIPT);


    try (final WebClient webClient = jenkinsRule.createWebClient()) {
      WebClientUtil.ExceptionListener exceptionListener = WebClientUtil.addExceptionListener(webClient);
      // Make sure all background JavaScript has completed so as expected exceptions have been thrown.
      WebClientUtil.waitForJSExec(webClient);


      System.out.println("#################URL: " + indexUrl());
      currentPage = webClient.getPage(indexUrl());
      Assert.assertEquals(PipelineConfigHistoryConsts.DISPLAY_NAME + " [Jenkins]", currentPage.getTitleText());
      refresh();

      assertTrue(currentIndexPageAsXml.contains("No pipeline configuration history available."));
      assertFalse(currentIndexPageAsXml.contains("Show Configuration"));

      createNewBuild(workflowJob, SCRIPT);
      createNewBuild(workflowJob, SCRIPT_2);
      assertTrue(PluginUtils.getHistoryDao().isHistoryPresent(workflowJob));

      currentPage = webClient.getPage(indexUrl());
      refresh();

      assertTrue(currentIndexPageAsText.contains("Show Configuration"));
      assertFalse(currentIndexPageAsText.contains("No pipeline configuration history available."));
      assertTrue(currentIndexPageAsText.contains("Build #1"));
      assertTrue(currentIndexPageAsText.contains("Build #2"));

    }
  }

  /*
  Test configOverview with one single configuration.
   */
  @Test
  public void test_1_configOverviewTest() throws Exception {
    workflowJob = createWorkflowJob(PIPELINE_NAME, SCRIPT);

    try (final WebClient webClient = jenkinsRule.createWebClient()) {
      WebClientUtil.ExceptionListener exceptionListener = WebClientUtil.addExceptionListener(webClient);
      // Make sure all background JavaScript has completed so as expected exceptions have been thrown.
      WebClientUtil.waitForJSExec(webClient);

      //go to the index page of pipelineconfighistory
      createNewBuild(workflowJob, SCRIPT);
      currentPage = webClient.getPage(indexUrl());
      refresh();

      //Get the one and only configOverview anchor
      LinkedList<HtmlAnchor> showConfigAnchors = currentPage.getAnchors()
          .stream()
          .filter(htmlAnchor -> htmlAnchor.getHrefAttribute().startsWith(configOverviewString))
          .collect(Collectors.toCollection(LinkedList::new));
      assertEquals(1, showConfigAnchors.size());

      //click the link
      currentPage = showConfigAnchors.getFirst().click(new Event(), true);
      WebClientUtil.waitForJSExec(webClient);
      refresh();


      //now we're on configOverview. Test that the one build exists.
//      currentPage.getAnchors().stream().forEach(htmlAnchor -> System.out.println("Anchor: " + htmlAnchor.toString()));
      assertTrue(currentIndexPageAsText.contains("Jenkinsfile"));
      List<HtmlAnchor> configOverviewAnchors = currentPage.getAnchors();

      //download button
      LinkedList<HtmlAnchor> configOverviewDownloadAnchors =
          configOverviewAnchors.stream()
              .filter(htmlAnchor -> htmlAnchor.hasAttribute("download"))
              .collect(Collectors.toCollection(LinkedList::new));
      assertEquals(1, configOverviewDownloadAnchors.size());
      assertTrue(configOverviewDownloadAnchors.getFirst().getHrefAttribute().startsWith(configSingleFileString));

      //"show file" buttons ("View Fancy" and "RAW"), exclude the download button
      LinkedList<HtmlAnchor> configOverviewconfigSingleFileAnchors =
          configOverviewAnchors.stream()
          .filter(htmlAnchor ->
              htmlAnchor.getHrefAttribute().startsWith(configSingleFileString)
              && !htmlAnchor.hasAttribute("download"))
          .collect(Collectors.toCollection(LinkedList::new));
      assertEquals(2, configOverviewconfigSingleFileAnchors.size());
    }
  }

  @Test
  public void test_2_configOverviewDownloadTest() throws Exception {
    workflowJob = createWorkflowJob(PIPELINE_NAME, SCRIPT);

    try (final WebClient webClient = jenkinsRule.createWebClient()) {
      WebClientUtil.ExceptionListener exceptionListener = WebClientUtil.addExceptionListener(webClient);
      // Make sure all background JavaScript has completed so as expected exceptions have been thrown.
      WebClientUtil.waitForJSExec(webClient);

      //go to the index page of pipelineconfighistory
      createNewBuild(workflowJob, SCRIPT);
      currentPage = webClient.getPage(indexUrl());
      refresh();

      // go to configOverview
      currentPage = currentPage.getAnchors()
          .stream()
          .filter(htmlAnchor -> htmlAnchor.getHrefAttribute().startsWith(configOverviewString))
          .collect(Collectors.toCollection(LinkedList::new)).getFirst()
          .click(new Event(), true);
      refresh();
      //click the download button
      currentPage = currentPage.getAnchors().stream()
          .filter(htmlAnchor -> htmlAnchor.hasAttribute("download"))
          .collect(Collectors.toCollection(LinkedList::new)).getFirst()
          .click(new Event(), true);
      refresh();
      System.out.println("DOWNLOADTEXTCONTENT:\n" + currentIndexPageAsText);
      System.out.println("DOWNLOADXMLCONTENT:\n" + currentIndexPageAsXml);

      assertEquals(
          SCRIPT,
          removeEnclosingTagFromXml(currentPage.getBody().asXml(), "body")
      );
    }
  }



  private String removeEnclosingTagFromXml(String xmlString, String tag) {
    String newLineRegex = "(\r\n|\r|\n)+";
    //TODO this is not really good but works with the current script example...
    return Arrays.stream(xmlString
        .replaceAll("<" + tag + ">" + newLineRegex, "")
        .replaceAll("</" + tag + ">" + newLineRegex, "")
        .split("\n"))
        .map(line -> line.trim())
        .filter(line -> !line.isEmpty())
        .reduce("", (line1, line2) -> (line1.isEmpty()) ? line2 : (line1 + "\n" + line2));
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

  private String indexUrl() throws IOException {
    return jenkinsRule.getURL() + workflowJob.getUrl() + "pipelineConfigHistory/";
  }

  private String configOverviewUrl(String timestamp) throws  IOException {
    return
        indexUrl()
        + workflowJob.getFullName()
        + "/" + timestamp;
  }

  private void refresh() throws IOException {
    //currentPage.refresh(); won't work unfortunately

    currentIndexPageAsXml = currentPage.asXml();
    currentIndexPageAsText = currentPage.asText();
  }

}