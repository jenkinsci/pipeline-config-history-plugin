package org.jenkinsci.plugins.pipelineConfigHistory.view;

import org.jenkinsci.plugins.pipelineConfigHistory.Pair;

import java.util.List;

public class LineMark {

  public static Pair<List<TextChunk>, List<TextChunk>> markLines(String line1, String line2) {
    char[] line1Arr = line1.toCharArray();
    char[] line2Arr = line2.toCharArray();

    int line1Index = 0;
    int line2Index = 0;
    while (line1Index < line1Arr.length && line2Index < line2Arr.length) {

    }


    //TODO implement
    return null;
  }

  private static int findOffsetToNextDiffPos(char[] s1, char[] s2, int s1BeginAt, int s2BeginAt) {
    int s1Index = s1BeginAt;
    int s2Index = s2BeginAt;
    while (s1Index < s1.length && s2Index < s2.length) {
      if (s1[s1Index] != s2[s2Index]) {
        return s1Index - s1BeginAt;
      }

      s1Index++;
      s2Index++;
    }
    //no diff found
    return -1;
  }

  public static class TextChunk {
    private String text;
    private boolean marked;

    public TextChunk(String text, boolean marked) {
      this.text = text;
      this.marked = marked;
    }
  }
}
