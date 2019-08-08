/*
 * The MIT License
 *
 * Copyright 2019 Robin Schulz
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

public class SingleLineView {

  public static class Line {
    public enum Kind {
      EQUAL, INSERT, DELETE, SKIPPING
    }

    //for access via jelly
    @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public final Kind EQUAL = Kind.EQUAL;
    @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public final Kind INSERT = Kind.INSERT;
    @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public final Kind DELETE = Kind.DELETE;
    @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public final Kind SKIPPING = Kind.SKIPPING;

    private Kind kind;

    private String content;

    private String lineNumLeft;

    private String lineNumRight;

    private String cssClass;

    public Line(Kind kind, String content, String lineNumLeft, String lineNumRight) {
      this.kind = kind;
      this.content = content;
      this.lineNumLeft = lineNumLeft;
      this.lineNumRight = lineNumRight;

      switch (kind) {
        case DELETE:
          this.cssClass = "diff_original";
          break;
        case INSERT:
          this.cssClass = "diff_revised";
          break;
        case EQUAL:
          this.cssClass = "diff_equal";
          break;
        case SKIPPING:
          this.cssClass = "skipping";
          break;
        default:
          this.cssClass = "";
          break;
      }
    }

    public Kind getKind() {
      return kind;
    }

    public String getContent() {
      return content;
    }

    public String getLineNumLeft() {
      return lineNumLeft;
    }

    public String getLineNumRight() {
      return lineNumRight;
    }

    public String getCssClass() {
      return cssClass;
    }

  }
}
