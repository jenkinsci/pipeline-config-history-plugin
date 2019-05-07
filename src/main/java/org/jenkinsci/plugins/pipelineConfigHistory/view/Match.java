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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.File;

public class Match {

  public enum Kind {
    UNEQUAL, EQUAL, SINGLE_1, SINGLE_2
  }

  //for access via jelly
  @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
  public final Kind UNEQUAL = Kind.UNEQUAL;
  @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
  public final Kind EQUAL = Kind.EQUAL;
  @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
  public final Kind SINGLE_1 = Kind.SINGLE_1;
  @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
  public final Kind SINGLE_2 = Kind.SINGLE_2;

  private Kind kind;

  private File file1;

  private File file2;

  /**
   * Get a new file match. At least one of the files must not be null.
   *
   * @param file1 the first file.
   * @param file2 the second file
   * @param kind  this matches Kind
   */
  public Match(File file1, File file2, Kind kind) {
    this.kind = kind;
    this.file1 = file1;
    this.file2 = file2;
    if (file1 != null && file2 != null && !file1.getName().equals(file2.getName())) {
      throw new IllegalArgumentException("File names are not equal: ["
          + file1.getName() + ", "
          + file2.getName()
          + "]"
      );
    }
  }

  public boolean hasFile1() {
    return file1 != null;
  }

  public boolean hasFile2() {
    return file2 != null;
  }

  public File getFile1() {
    return file1;
  }

  public File getFile2() {
    return file2;
  }

  public Kind getKind() {
    return kind;
  }

  public String getFileName() {
    return (kind.equals(Kind.SINGLE_1)) ? getFile1().getName() : getFile2().getName();
  }

  /**
   * Get the full file name (largest common suffix).
   *
   * @return the largest common suffix of both paths.
   */
  public String getFullFileName() {
    if (getFile1() == null && getFile2() == null) {
      return "";
    }
    if (getFile1() == null || getFile2() == null) {
      return getFileName();
    }
    File currentFile1 = getFile1();
    File currentFile2 = getFile2();
    StringBuilder fullFileNameBuilder = new StringBuilder();

    boolean equalFileName = currentFile1.getName().equals(currentFile2.getName());
    while (equalFileName) {
      fullFileNameBuilder.insert(0, currentFile1.getName() + "/");

      currentFile1 = currentFile1.getParentFile();
      currentFile2 = currentFile2.getParentFile();

      equalFileName = (currentFile1 != null && currentFile2 != null)
          && currentFile1.getName().equals(currentFile2.getName());
    }
    String fullFileName = fullFileNameBuilder.toString();
    return fullFileName.substring(0, fullFileName.length() - 1);
  }
}
