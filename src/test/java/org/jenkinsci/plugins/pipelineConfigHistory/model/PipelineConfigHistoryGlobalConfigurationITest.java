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
package org.jenkinsci.plugins.pipelineConfigHistory.model;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.util.FormValidation;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * This partially is an integration test in so far as the configuration is done via the web interface.
 * Nevertheless, assertions are done via classes, not the web interface.
 * Also, the global config's actual effects on the running system are not tested.
 */
public class PipelineConfigHistoryGlobalConfigurationITest {

  @Rule
  public JenkinsRule jenkinsRule = new JenkinsRule();

  @Test
  public void configure() {
  }

  @Test
  public void getMaxHistoryEntries() throws IOException, SAXException {
    assertEquals(null, PipelineConfigHistoryGlobalConfiguration.get().getMaxHistoryEntries());

    setUpGlobalConfig("20", null);
    assertEquals("20", PipelineConfigHistoryGlobalConfiguration.get().getMaxHistoryEntries());
  }

  @Test
  public void getMaxDaysToKeepEntries() throws IOException, SAXException {
    assertEquals(null, PipelineConfigHistoryGlobalConfiguration.get().getMaxHistoryEntries());

    setUpGlobalConfig(null, "20");
    assertEquals("20", PipelineConfigHistoryGlobalConfiguration.get().getMaxDaysToKeepEntries());
  }

  @Test
  public void getMaxHistoryEntriesOptional() throws IOException, SAXException {
    Optional<Integer> maxHistoryEntriesOptional1 = PipelineConfigHistoryGlobalConfiguration.get().getMaxHistoryEntriesOptional();
    assertEquals(false, maxHistoryEntriesOptional1.isPresent());

    setUpGlobalConfig("20", null);

    Optional<Integer> maxHistoryEntriesOptional2 = PipelineConfigHistoryGlobalConfiguration.get().getMaxHistoryEntriesOptional();
    assertEquals(true, maxHistoryEntriesOptional2.isPresent());
    assertEquals(Integer.valueOf(20), maxHistoryEntriesOptional2.get());

    setUpGlobalConfig("", "123");

    Optional<Integer> maxHistoryEntriesOptional3 = PipelineConfigHistoryGlobalConfiguration.get().getMaxHistoryEntriesOptional();
    assertEquals(false, maxHistoryEntriesOptional3.isPresent());
  }

  @Test
  public void getMaxDaysToKeepEntriesOptional() throws IOException, SAXException {
    Optional<Integer> maxDaysToKeepEntriesOptional1 = PipelineConfigHistoryGlobalConfiguration.get().getMaxDaysToKeepEntriesOptional();
    assertEquals(false, maxDaysToKeepEntriesOptional1.isPresent());

    setUpGlobalConfig(null, "20");
    Optional<Integer> maxDaysToKeepEntriesOptional2 = PipelineConfigHistoryGlobalConfiguration.get().getMaxDaysToKeepEntriesOptional();
    assertEquals(true, maxDaysToKeepEntriesOptional2.isPresent());
    assertEquals(Integer.valueOf(20), maxDaysToKeepEntriesOptional2.get());

    setUpGlobalConfig("123", "");
    Optional<Integer> maxDaysToKeepEntriesOptional3 = PipelineConfigHistoryGlobalConfiguration.get().getMaxDaysToKeepEntriesOptional();
    assertEquals(false, maxDaysToKeepEntriesOptional3.isPresent());
  }

  @Test
  public void doCheckMaxHistoryEntries() {
    PipelineConfigHistoryGlobalConfiguration sut = PipelineConfigHistoryGlobalConfiguration.get();

    assertEquals(FormValidation.Kind.OK, sut.doCheckMaxHistoryEntries("").kind);
    assertEquals(FormValidation.Kind.OK, sut.doCheckMaxHistoryEntries(null).kind);

    assertEquals(FormValidation.Kind.OK, sut.doCheckMaxHistoryEntries("0").kind);
    assertEquals(FormValidation.Kind.OK, sut.doCheckMaxHistoryEntries("1").kind);
    assertEquals(FormValidation.Kind.OK, sut.doCheckMaxHistoryEntries("55").kind);


    assertEquals(FormValidation.Kind.ERROR, sut.doCheckMaxHistoryEntries("-1").kind);
    assertEquals(FormValidation.Kind.ERROR, sut.doCheckMaxHistoryEntries("notANumber").kind);

  }

  @Test
  public void doCheckMaxDaysToKeepEntries() {
    PipelineConfigHistoryGlobalConfiguration sut = PipelineConfigHistoryGlobalConfiguration.get();

    assertEquals(FormValidation.Kind.OK, sut.doCheckMaxDaysToKeepEntries("").kind);
    assertEquals(FormValidation.Kind.OK, sut.doCheckMaxDaysToKeepEntries(null).kind);

    assertEquals(FormValidation.Kind.OK, sut.doCheckMaxDaysToKeepEntries("0").kind);
    assertEquals(FormValidation.Kind.OK, sut.doCheckMaxDaysToKeepEntries("1").kind);
    assertEquals(FormValidation.Kind.OK, sut.doCheckMaxDaysToKeepEntries("55").kind);


    assertEquals(FormValidation.Kind.ERROR, sut.doCheckMaxDaysToKeepEntries("-1").kind);
    assertEquals(FormValidation.Kind.ERROR, sut.doCheckMaxDaysToKeepEntries("notANumber").kind);
  }

  public void setUpGlobalConfig(String maxHistoryEntries, String maxDaysToKeepEntries) throws IOException, SAXException {
    HtmlPage configPage = jenkinsRule.createWebClient().goTo("configure");

    HtmlForm configForm = configPage.getFormByName("config");

    configForm.getInputByName("maxHistoryEntries").setValueAttribute(maxHistoryEntries != null ? maxHistoryEntries : "");
    configForm.getInputByName("maxDaysToKeepEntries").setValueAttribute(maxDaysToKeepEntries != null ? maxDaysToKeepEntries : "");

    DomElement buttonDomElement = configPage.getElementByName("Submit").getFirstElementChild().getFirstElementChild();
    if (!(buttonDomElement instanceof HtmlButton)) {
      fail("submit button not found in config page.");
    }
    HtmlButton configFormSubmit = (HtmlButton) buttonDomElement;
    configFormSubmit.click();
  }
}