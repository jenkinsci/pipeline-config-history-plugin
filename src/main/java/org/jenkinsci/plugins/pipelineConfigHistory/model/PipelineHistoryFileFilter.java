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

import org.jenkinsci.plugins.pipelineConfigHistory.PipelineConfigHistoryConsts;

import java.io.File;
import java.io.FileFilter;

/**
 * A filter to return only those directories of a file listing that represent
 * pipeline configuration history directories.
 *
 * @author Robin Schulz
 */
public class PipelineHistoryFileFilter implements FileFilter {

  /**
   * Singleton.
   */
  private PipelineHistoryFileFilter() {
  }

  private static PipelineHistoryFileFilter instance;

  /**
   * This is a singleton. Get the only instance.
   * @return the one and only PipelineHistoryFileFilter which asserts if a file is a directory
   * containing a build xml file.
   */
  public static PipelineHistoryFileFilter getInstance() {
    if (instance == null) {
      instance = new PipelineHistoryFileFilter();
    }
    return instance;
  }

  @Override
  public boolean accept(File file) {
    return file.exists() && new File(file, PipelineConfigHistoryConsts.BUILD_XML_FILENAME).exists();
  }
}
