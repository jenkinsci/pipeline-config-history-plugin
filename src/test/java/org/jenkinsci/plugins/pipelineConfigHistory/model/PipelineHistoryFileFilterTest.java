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

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class PipelineHistoryFileFilterTest {

	static final PipelineHistoryFileFilter filter = PipelineHistoryFileFilter.getInstance();

	@Rule
	public JenkinsRule jenkinsRule = new JenkinsRule();

	@Test
	public void accept() throws IOException {
		String buildFileStr = "934ghjklghjk/build.xml";
		File buildFile = createFile(buildFileStr, "");

		assertTrue(filter.accept(buildFile.getParentFile()));
		assertFalse(filter.accept(buildFile));
	}

	@Test
	public void acceptInvalid() throws IOException {
		String otherXmlFileStr = "934ghjklghjk/build_.xml";
		File otherXmlFile = createFile(otherXmlFileStr, "");

		assertFalse(filter.accept(otherXmlFile.getParentFile()));
		assertFalse(filter.accept(otherXmlFile));
	}

	private File createFile(String fileName, String content) throws IOException {
		File file = new File(jenkinsRule.jenkins.getRootDir(), fileName);
		FileUtils.write(file, content);
		return file;
	}
}