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

import difflib.DiffUtils;
import difflib.Patch;
import difflib.StringUtills;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DiffLineGenerator {

  private String file1Content;

  private String file2Content;

  public DiffLineGenerator(String file1Content, String file2Content) {
    this.file1Content = file1Content;
    this.file2Content = file2Content;
  }

  /**
   * Calculate the line-wise diff between the two files and return a @link{SideBySideView} representation.
   * @return the lines representing the diff
   */
  public List<SideBySideView.Line> getLines() {
    List<String> stringDiffLines =
        Arrays.asList(getDiffAsString(
            preProcessFileString(this.file1Content),
            preProcessFileString(this.file2Content))
            .split("\\n"));

    List<SideBySideView.Line> lines = new GetDiffLines(stringDiffLines).get();

    return (lines.stream().anyMatch(line -> !line.isEmpty())) ? lines : Collections.emptyList();
  }

  private final String getDiffAsString(String file1Str, String file2Str) {

    List<String> file1Lines = Arrays.asList(file1Str.split("\n"));
    List<String> file2Lines = Arrays.asList(file2Str.split("\n"));

    Patch patch = DiffUtils.diff(file1Lines, file2Lines);

    final List<String> unifiedDiff = DiffUtils.generateUnifiedDiff("", "",
        file1Lines, patch, 3);

    return StringUtills.join(unifiedDiff, "\n") + "\n";
  }

  /**
   * Replaces empty lines with " ".
   * This is needed because DiffUtils can't tell empty lines from non-existing ones.
   * @param fileString the file as a string
   * @return the preprocessed file string
   */
  private String preProcessFileString(String fileString) {
    return fileString.replaceAll("\\n\\n", "\n \n");
  }

}
