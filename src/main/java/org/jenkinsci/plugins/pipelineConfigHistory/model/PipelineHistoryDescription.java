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

import org.jenkinsci.plugins.pipelineConfigHistory.PluginUtils;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import java.util.Objects;

public class PipelineHistoryDescription {

  private final String timestamp;

  private final String fullName;

  private final int buildNumber;

  /**
   * Get a PipelineHistoryDescription encapsulating the given information.
   * @param timestamp the config revision identifier
   * @param fullName the pipeline's full Name (jenkins convention)
   * @param buildNumber the build number associated to this config revision
   */
  public PipelineHistoryDescription(String timestamp, String fullName, int buildNumber) {
    this.timestamp = timestamp;
    this.fullName = fullName;
    this.buildNumber = buildNumber;
  }

  public int getBuildNumber() {
    return buildNumber;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public String getFullName() {
    return fullName;
  }

  public WorkflowJob getWorkflowJob() {
    return PluginUtils.getWorkflowJob(getFullName());
  }

  /**
   * Compare this object to another object via its getter-available fields.
   *
   * @param other the object to compare
   * @return true, if equals() is true on all getters.
   */
  @Override
  public boolean equals(Object other) {
    if (!(other instanceof PipelineHistoryDescription)) return false;
    PipelineHistoryDescription compPipelineHistoryDescription = (PipelineHistoryDescription) other;

    return compPipelineHistoryDescription.getWorkflowJob().equals(this.getWorkflowJob())
        && compPipelineHistoryDescription.getTimestamp().equals(this.getTimestamp())
        && compPipelineHistoryDescription.getBuildNumber() == this.getBuildNumber()
        && compPipelineHistoryDescription.getFullName().equals(this.getFullName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(timestamp, fullName, buildNumber, this.getWorkflowJob());
  }
}
