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
package org.jenkinsci.plugins.pipelineConfigHistory;

/**
 * Holder for constants.
 *
 * @author Robin Schulz
 */
public final class PipelineConfigHistoryConsts {

  private PipelineConfigHistoryConsts() {
  }

  /**
   * Path to the pipeline-config-history base.
   */
  public static final String PLUGIN_BASE_PATH = "pipeline-config-history";

  /**
   * This plugin's name.
   */
  public static final String DISPLAY_NAME = "Pipeline Configuration History";

  /**
   * Path to the config history icon.
   */
  public static final String ICON_PATH = "/plugin/pipeline-config-history/icons/confighistory.svg";
  
  /**
   * Path to the build padge icon.
   */
  public static final String BADGE_ACTION_ICON_PATH = "/plugin/pipeline-config-history/icons/buildbadge.svg";

  /**
   * Path to the history base. Usage: JENKINS_HOME/DEFAULT_HISTORY_DIR
   */
  public static final String DEFAULT_HISTORY_DIR = "pipeline-config-history";

  /**
   * single configuration history xml's filename.
   * The xml contains information like the corresponding build number and the job's full name.
   */
  public static final String HISTORY_XML_FILENAME = "history.xml";

  /**
   * build xml's filename.
   */
  public static final String BUILD_XML_FILENAME = "build.xml";

  /**
   * Format for timestamped dirs.
   */
  public static final String ID_FORMATTER = "yyyy-MM-dd_HH-mm-ss";

  /**
   * any plugins needed for this plugin to work should be listed here by their short names.
   */
  static final String[] REQUIRED_PLUGINS_SHORT_NAMES = {"workflow-aggregator"};

  public static final String LINK_SYMBOL = "&#128279;";

}
