package org.jenkinsci.plugins.pipelineConfigHistory.view;

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 *
 * @author Mirko Friedenhagen
 */
public class GetDiffLinesTest {

	private String resourceString = "diff --git a/src/main/java/hudson/plugins/jobConfigHistory/JobConfigHistoryBaseAction.java b/src/main/java/hudson/plugins/jobConfigHistory/JobConfigHistoryBaseAction.java\n" +
			"index c383ef4..5398de1 100644\n" +
			"--- a/src/main/java/hudson/plugins/jobConfigHistory/JobConfigHistoryBaseAction.java\n" +
			"+++ b/src/main/java/hudson/plugins/jobConfigHistory/JobConfigHistoryBaseAction.java\n" +
			"@@ -30,6 +30,7 @@ import difflib.Patch;\n" +
			" import bmsi.util.Diff;\n" +
			" import bmsi.util.DiffPrint;\n" +
			" import bmsi.util.Diff.change;\n" +
			"+import org.kohsuke.stapler.StaplerRequest;\n" +
			" \n" +
			" /**\n" +
			"  * Implements some basic methods needed by the\n" +
			"@@ -117,7 +118,7 @@ public abstract class JobConfigHistoryBaseAction implements Action {\n" +
			"      * @return value of the request parameter or null if it does not exist.\n" +
			"      */\n" +
			"     protected String getRequestParameter(final String parameterName) {\n" +
			"-        return Stapler.getCurrentRequest().getParameter(parameterName);\n" +
			"+        return getCurrentRequest().getParameter(parameterName);\n" +
			"     }\n" +
			" \n" +
			"     /**\n" +
			"@@ -277,6 +278,10 @@ public abstract class JobConfigHistoryBaseAction implements Action {\n" +
			"         return output.toString();\n" +
			"     }\n" +
			" \n" +
			"+    StaplerRequest getCurrentRequest() {\n" +
			"+        return Stapler.getCurrentRequest();\n" +
			"+    }\n" +
			"+\n" +
			" \n" +
			"     /**\n" +
			"      * Holds information for the SideBySideView.";

	/**
	 * Test of get method, of class GetDiffLines.
	 */
	@Test
	public void testGet() throws IOException {
		GetDiffLines sut = createGetDiffLines();
		List<SideBySideView.Line> result = sut.get();
		assertEquals(24, result.size());
		SideBySideView.Line firstLine = result.get(0);
		assertEquals("import bmsi.util.Diff;", firstLine.getLeft().getText());
		assertEquals("import bmsi.util.Diff;", firstLine.getRight().getText());
		SideBySideView.Line fourthLine = result.get(3);
		final SideBySideView.Line.Item left = fourthLine.getLeft();
		final SideBySideView.Line.Item right = fourthLine.getRight();
		assertEquals("3", right.getLineNumber());
		assertNull(left.getText());
		assertEquals("import org.kohsuke.stapler.StaplerRequest;",
				right.getText());
		assertEquals("diff_original", left.getCssClass());
		assertEquals("diff_revised", right.getCssClass());
	}

	GetDiffLines createGetDiffLines() throws IOException {
		final List<String> lines = Arrays.asList(resourceString.split("\n"));
		GetDiffLines sut = new GetDiffLines(lines);
		return sut;
	}

}