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

    public Kind kind;

    public String content;

    public String lineNumLeft;

    public String lineNumRight;

    public String cssClass;

    public Line(Kind kind, String content, String lineNumLeft, String lineNumRight) {
      this.kind = kind;
      this.content = content;
      this.lineNumLeft = lineNumLeft;
      this.lineNumRight = lineNumRight;

      switch (kind) {
        case DELETE:
          this.cssClass = "diff_delete";
          break;
        case INSERT:
          this.cssClass = "diff_insert";
          break;
        case EQUAL:
          this.cssClass = "diff_equal";
          break;
        case SKIPPING:
          this.cssClass = "diff_skipping";
          break;
        default:
          this.cssClass = "";
          break;
      }
    }

  }
}
