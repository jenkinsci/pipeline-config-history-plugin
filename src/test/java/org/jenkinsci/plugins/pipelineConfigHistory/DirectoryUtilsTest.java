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

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class DirectoryUtilsTest {

	//this is constructed such that:
	/*
		dir1 and dir 2 are unequal (structural)
		dir1 and dir 3 are structurally equal.
		dir1 and dir 3 are contently unequal.
		dir1 and dir 4 are partly equal --> unequal.
	 */
	static final String dir1Root = "subFolder/dir1Root/";
	static final String dir2Root = "subFolder/dir2Root/";
	static final String dir3Root = "subFolder/dir3Root/";
	static final String dir4Root = "subFolder/dir4Root/";

	static final String dir1RootFile1Name = dir1Root + "file1";
	static final String dir2RootFile1Name = dir2Root + "filee1";
	static final String dir3RootFile1Name = dir3Root + "file1";
	static final String dir4RootFile1Name = dir4Root + "file1";

	static final String dir1RootFile2Name = dir1Root + "file2";
	static final String dir2RootFile2Name = dir2Root + "filee2";
	static final String dir3RootFile2Name = dir3Root + "file2";
	static final String dir4RootFile2Name = dir4Root + "file2_";

	static final String dir1RootFile2Content = "3f92hf23\n";
	static final String dir3RootFile2Content = "8fb";

	@Rule
	public JenkinsRule jenkinsRule = new JenkinsRule();

	@Before
	public void beforeTest() throws IOException {

		createFile(dir1RootFile1Name, "");
		createFile(dir2RootFile1Name, "");
		createFile(dir3RootFile1Name, "");
		createFile(dir4RootFile1Name, "");

		createFile(dir1RootFile2Name, dir1RootFile2Content);
		createFile(dir2RootFile2Name, "");
		createFile(dir3RootFile2Name, dir3RootFile2Content);
		createFile(dir4RootFile2Name, "");
	}

	@Test
	public void isEqual_1_1() throws IOException {
		assertTrue(DirectoryUtils.isEqual(createPath(dir1Root), createPath(dir1Root), false));
		assertTrue(DirectoryUtils.isEqual(createPath(dir1Root), createPath(dir1Root), true));
	}

	@Test
	public void isEqual_1_2() throws IOException {
		assertFalse(DirectoryUtils.isEqual(createPath(dir1Root), createPath(dir2Root), false));
		assertFalse(DirectoryUtils.isEqual(createPath(dir1Root), createPath(dir2Root), true));
	}

	@Test
	public void isEqual_1_3() throws IOException {
		assertTrue(DirectoryUtils.isEqual(createPath(dir1Root), createPath(dir3Root), false));
		assertFalse(DirectoryUtils.isEqual(createPath(dir1Root), createPath(dir3Root), true));
	}

	@Test
	public void isEqual_1_4() throws IOException {
		assertFalse(DirectoryUtils.isEqual(createPath(dir1Root), createPath(dir4Root), false));
		assertFalse(DirectoryUtils.isEqual(createPath(dir1Root), createPath(dir4Root), true));
	}

	private File createFile(String fileName, String content) throws IOException {
		File file = new File(jenkinsRule.jenkins.getRootDir(), fileName);
		FileUtils.write(file, content);
		return file;
	}

	private Path createPath(String relativeName) {
		return Paths.get(jenkinsRule.jenkins.getRootDir().getPath() + "/" + relativeName);
	}
}