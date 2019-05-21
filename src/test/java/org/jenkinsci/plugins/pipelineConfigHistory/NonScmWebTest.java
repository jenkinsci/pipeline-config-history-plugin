package org.jenkinsci.plugins.pipelineConfigHistory;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientUtil;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.host.event.Event;
import hudson.model.Job;
import hudson.model.queue.QueueTaskFuture;
import jenkins.model.ParameterizedJobMixIn;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
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
import java.util.ArrayList;
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
public class NonScmWebTest {

  @Rule
  public JenkinsRule jenkinsRule = new JenkinsRule();

  public static final String PIPELINE_NAME = "pipeline";
  public static final String SCRIPT = "node {\n" +
      "//nothing\n" +
      "}";
  public static final String SCRIPT_2 =
      "node {\n" +
          "//nothing2\n" +
          "}";

  public WorkflowJob workflowJob;
  private HtmlPage currentPage;
  private String currentPageAsXml;
  private String currentPageAsText;
  private final String configOverviewString = "configOverview";
  private final String configSingleFileString = "configSingleFile";
  private final String showSingleDiff = "showSingleDiff";


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

      assertTrue(currentPageAsXml.contains("No pipeline configuration history available."));
      assertFalse(currentPageAsXml.contains("Show Configuration"));

      createNewBuild(workflowJob, SCRIPT);
      createNewBuild(workflowJob, SCRIPT_2);
      assertTrue(PluginUtils.getHistoryDao().isHistoryPresent(workflowJob));

      currentPage = webClient.getPage(indexUrl());
      refresh();

      assertTrue(currentPageAsText.contains("Show Configuration"));
      assertFalse(currentPageAsText.contains("No pipeline configuration history available."));
      assertTrue(currentPageAsText.contains("Build #1"));
      assertTrue(currentPageAsText.contains("Build #2"));

    }
  }

  /*
  Test configOverview with one single configuration.
   */
  @Test
  public void test_1_0_configOverviewTest() throws Exception {
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
      assertTrue(currentPageAsText.contains("Jenkinsfile"));
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
  public void test_1_1_configOverviewDownloadTest() throws Exception {
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
      System.out.println("DOWNLOADTEXTCONTENT:\n" + currentPageAsText);
      System.out.println("DOWNLOADXMLCONTENT:\n" + currentPageAsXml);

      assertEquals(
          SCRIPT,
          removeEnclosingTagFromXml(currentPage.getBody().asXml(), "body")
      );
    }
  }

  @Test
  public void test_1_2_configOverviewShowFileTest() throws Exception {
    workflowJob = createWorkflowJob(PIPELINE_NAME, SCRIPT);

    try (final WebClient webClient = jenkinsRule.createWebClient()) {
      WebClientUtil.ExceptionListener exceptionListener = WebClientUtil.addExceptionListener(webClient);
      // Make sure all background JavaScript has completed so as expected exceptions have been thrown.
      WebClientUtil.waitForJSExec(webClient);

      //don't throw exceptions. Somehow this framework won't find no stylesheets or .js files...
      webClient.getOptions().setThrowExceptionOnScriptError(false);
      webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);


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


      //test view fancy and raw
      LinkedList<HtmlAnchor> configSingleFileAnchors = currentPage.getAnchors().stream()
          .filter(htmlAnchor ->
              !htmlAnchor.hasAttribute("download")
              && htmlAnchor.getHrefAttribute().startsWith(configSingleFileString))
          .collect(Collectors.toCollection(LinkedList::new));
      assertEquals(2, configSingleFileAnchors.size());

      //test raw
      HtmlAnchor rawAnchor = configSingleFileAnchors.stream().filter(htmlAnchor -> htmlAnchor.getTextContent().trim().equals("(RAW)")).findAny().get();
      currentPage = rawAnchor.click(new Event(), true);
      WebClientUtil.waitForJSExec(webClient);
      refresh();

      assertEquals(
          SCRIPT,
          removeEnclosingTagFromXml(currentPage.getBody().asXml(), "body")
      );

      //test view fancy
      HtmlAnchor viewFancyAnchor = configSingleFileAnchors.stream().filter(htmlAnchor -> htmlAnchor.getTextContent().trim().equals("View Fancy")).findAny().get();
      currentPage = viewFancyAnchor.click(new Event(), true);
      WebClientUtil.waitForJSExec(webClient);
      refresh();

      assertTrue(currentPageAsText.contains("Jenkinsfile (Root Script)"));
      assertTrue(currentPageAsText.contains("(Src: from Pipeline-Job Configuration)"));
      assertTrue(currentPageAsText.contains(SCRIPT));
    }
  }

  @Test
  public void test_2_showAllDiffsTest() throws Exception {
    workflowJob = createWorkflowJob(PIPELINE_NAME, SCRIPT);

    try (final WebClient webClient = jenkinsRule.createWebClient()) {
      WebClientUtil.ExceptionListener exceptionListener = WebClientUtil.addExceptionListener(webClient);
      // Make sure all background JavaScript has completed so as expected exceptions have been thrown.
      WebClientUtil.waitForJSExec(webClient);

      //don't throw exceptions. Somehow this framework won't find no stylesheets or .js files...
      webClient.getOptions().setThrowExceptionOnScriptError(false);
      webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);


      //go to the index page of pipelineconfighistory
      createNewBuild(workflowJob, SCRIPT);
      createNewBuild(workflowJob, SCRIPT_2);
      currentPage = webClient.getPage(indexUrl());
      refresh();

      //this button is not an anchor but an <input>, so it must be found via its name.
      //the following test code assumes that the two selection "radio"s are set on different configs by default.
      currentPage = currentPage.getElementByName("showDiffsInOneSite").click(new Event(), true);
      // go to showAllDiffs
      refresh();

      DomElement leftTable = currentPage.getElementByName("left-table");
      DomElement rightTable = currentPage.getElementByName("right-table");

      double levenshteinPercentage = 1 -
          ( (double) new LevenshteinDistance().apply(leftTable.asText(), getIndexedScript(SCRIPT)) / leftTable.asText().length());
      //this is an os fix. It also hides the uglyness of getIndexedScript(..)
      assertTrue(levenshteinPercentage >= 0.9);

      if (SystemUtils.IS_OS_UNIX) {
        //this will not work on windows.
        assertEquals(leftTable.asText(), getIndexedScript(SCRIPT));
        assertEquals(rightTable.asText(), getIndexedScript(SCRIPT_2));
      }
    }
  }

  @Test
  public void test_3_0_showDiffFilesTest() throws Exception {
    workflowJob = createWorkflowJob(PIPELINE_NAME, SCRIPT);

    try (final WebClient webClient = jenkinsRule.createWebClient()) {
      WebClientUtil.ExceptionListener exceptionListener = WebClientUtil.addExceptionListener(webClient);
      // Make sure all background JavaScript has completed so as expected exceptions have been thrown.
      WebClientUtil.waitForJSExec(webClient);

      //don't throw exceptions. Somehow this framework won't find no stylesheets or .js files...
      webClient.getOptions().setThrowExceptionOnScriptError(false);
      webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);


      //go to the index page of pipelineconfighistory
      createNewBuild(workflowJob, SCRIPT);
      createNewBuild(workflowJob, SCRIPT_2);
      currentPage = webClient.getPage(indexUrl());
      refresh();

      //this button is not an anchor but an <input>, so it must be found via its name.
      //the following test code assumes that the two selection "radio"s are set on different configs by default.
      currentPage = currentPage.getElementByName("showAllDiffs").click(new Event(), true);
      // go to showAllDiffs
      refresh();

//      Thread.sleep(5000000);
      assertTrue(currentPageAsText.contains(PIPELINE_NAME + " Differences"));
      assertTrue(currentPageAsText.contains("Show Diff"));
      assertTrue(currentPageAsText.contains("Jenkinsfile"));

    }
  }

  @Test
  public void test_3_1_showSingleDiffTest() throws Exception {
    workflowJob = createWorkflowJob(PIPELINE_NAME, SCRIPT);

    try (final WebClient webClient = jenkinsRule.createWebClient()) {
      WebClientUtil.ExceptionListener exceptionListener = WebClientUtil.addExceptionListener(webClient);
      // Make sure all background JavaScript has completed so as expected exceptions have been thrown.
      WebClientUtil.waitForJSExec(webClient);

      //don't throw exceptions. Somehow this framework won't find no stylesheets or .js files...
      webClient.getOptions().setThrowExceptionOnScriptError(false);
      webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);


      //go to the index page of pipelineconfighistory
      createNewBuild(workflowJob, SCRIPT);
      createNewBuild(workflowJob, SCRIPT_2);
      currentPage = webClient.getPage(indexUrl());
      refresh();

      //this button is not an anchor but an <input>, so it must be found via its name.
      //the following test code assumes that the two selection "radio"s are set on different configs by default.
      // go to showAllDiffs
      currentPage = currentPage.getElementByName("showAllDiffs").click(new Event(), true);
      refresh();

      //go to showSingleDiff
      LinkedList<HtmlAnchor> showDiffAnchors = currentPage.getAnchors()
          .stream()
          .filter(htmlAnchor -> htmlAnchor.getHrefAttribute().startsWith(showSingleDiff))
          .collect(Collectors.toCollection(LinkedList::new));
      assertEquals(1, showDiffAnchors.size());
      currentPage = showDiffAnchors.getFirst().click(new Event(), true);
      refresh();

      System.out.println(currentPageAsText);
      DomElement tbody = currentPage.getElementById("tbody_versionDiffsShown");
      Iterable<DomElement> tableRowsIterable = tbody.getChildElements();
      ArrayList<DomElement> tableRowsList = new ArrayList<>(1);
      tableRowsIterable.forEach(tableRow -> tableRowsList.add(tableRowsList.size(), tableRow));

      String[] scriptAsArray = SCRIPT.split("\n");
      String[] script2AsArray = SCRIPT_2.split("\n");


      for (int i=0; i < tableRowsList.size(); ++i) {
        DomElement tr = tableRowsList.get(i);
        System.out.println("ROW: ");


        DomElement[] tdArray = new DomElement[2];
        int j = 0;
        for (DomElement trChild : tr.getChildElements()) {
          if (trChild.getTagName().equals("td")) {
            tdArray[j++] = trChild;
          }
        }
        assertEquals(scriptAsArray[i], tdArray[0].asText());
        assertEquals(script2AsArray[i], tdArray[1].asText());
      }
    }

  }

  private String getIndexedScript(String script) {

    String s1 = "<1\t[\n" +
        "\n" +
        "[2019-05-16T14:54:25.941Z] node {\n" +
        "\n" +
        "[2019-05-16T14:54:25.941Z] 2\t\n" +
        "\n" +
        "[2019-05-16T14:54:25.941Z] //nothing\n" +
        "\n" +
        "[2019-05-16T14:54:25.941Z] 3\t\n" +
        "]\n" +
        "\n" +
        "[2019-05-16T14:54:25.941Z] }>";
    String s2 = "<1\t[\n" +
        "\n" +
        "[2019-05-16T14:54:25.941Z] node {\n" +
        "\n" +
        "[2019-05-16T14:54:25.941Z] 2\t\n" +
        "\n" +
        "[2019-05-16T14:54:25.941Z] //nothing\n" +
        "\n" +
        "[2019-05-16T14:54:25.941Z] 3\t]\n" +
        "\n" +
        "[2019-05-16T14:54:25.941Z] }>\n";

    StringBuilder resultBuilder = new StringBuilder();
    String[] lines = script.split("\\n");
    for (int i = 0; i < lines.length; ++i) {
      resultBuilder.append(i+1).append("\t\n").append(lines[i]);
      if ( i < lines.length-1) {
        resultBuilder.append("\n");
      }
    }
    return resultBuilder.toString();
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
    return jenkinsRule.getURL() + workflowJob.getUrl() + "pipeline-config-history/";
  }

  private String configOverviewUrl(String timestamp) throws  IOException {
    return
        indexUrl()
        + workflowJob.getFullName()
        + "/" + timestamp;
  }

  private void refresh() throws IOException {
    //currentPage.refresh(); won't work unfortunately

    currentPageAsXml = currentPage.asXml();
    currentPageAsText = currentPage.asText();
  }

}