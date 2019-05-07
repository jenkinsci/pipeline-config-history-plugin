package org.jenkinsci.plugins.pipelineConfigHistory.view;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class MatchTest {

	private static final File file1 = new File("diffferent/foo/name1");
	private static final File file1_= new File("different/foo/name1");
	private static final String longestCommonFileSuffix = "foo/name1";
	private static final File file2 = new File("name1");

	@Test
	public void hasFile1() {
		assertTrue(new Match(file1,file2, Match.Kind.EQUAL).hasFile1());
		assertTrue(new Match(file1,file2, Match.Kind.UNEQUAL).hasFile1());
		assertTrue(new Match(file1,file2, Match.Kind.SINGLE_2).hasFile1());
		assertTrue(new Match(file1,file2, Match.Kind.SINGLE_1).hasFile1());

		assertTrue(new Match(file1,null, Match.Kind.EQUAL).hasFile1());
		assertTrue(new Match(file1,null, Match.Kind.UNEQUAL).hasFile1());
		assertTrue(new Match(file1,null, Match.Kind.SINGLE_2).hasFile1());
		assertTrue(new Match(file1,null, Match.Kind.SINGLE_1).hasFile1());

		assertFalse(new Match(null,file2, Match.Kind.EQUAL).hasFile1());
		assertFalse(new Match(null,file2, Match.Kind.UNEQUAL).hasFile1());
		assertFalse(new Match(null,file2, Match.Kind.SINGLE_2).hasFile1());
		assertFalse(new Match(null,file2, Match.Kind.SINGLE_1).hasFile1());

		assertFalse(new Match(null,null, Match.Kind.EQUAL).hasFile1());
		assertFalse(new Match(null,null, Match.Kind.UNEQUAL).hasFile1());
		assertFalse(new Match(null,null, Match.Kind.SINGLE_2).hasFile1());
		assertFalse(new Match(null,null, Match.Kind.SINGLE_1).hasFile1());
	}

	@Test
	public void hasFile2() {
		assertTrue(new Match(file1,file2, Match.Kind.EQUAL).hasFile2());
		assertTrue(new Match(file1,file2, Match.Kind.UNEQUAL).hasFile2());
		assertTrue(new Match(file1,file2, Match.Kind.SINGLE_2).hasFile2());
		assertTrue(new Match(file1,file2, Match.Kind.SINGLE_1).hasFile2());

		assertFalse(new Match(file1,null, Match.Kind.EQUAL).hasFile2());
		assertFalse(new Match(file1,null, Match.Kind.UNEQUAL).hasFile2());
		assertFalse(new Match(file1,null, Match.Kind.SINGLE_2).hasFile2());
		assertFalse(new Match(file1,null, Match.Kind.SINGLE_1).hasFile2());

		assertTrue(new Match(null,file2, Match.Kind.EQUAL).hasFile2());
		assertTrue(new Match(null,file2, Match.Kind.UNEQUAL).hasFile2());
		assertTrue(new Match(null,file2, Match.Kind.SINGLE_2).hasFile2());
		assertTrue(new Match(null,file2, Match.Kind.SINGLE_1).hasFile2());

		assertFalse(new Match(null,null, Match.Kind.EQUAL).hasFile2());
		assertFalse(new Match(null,null, Match.Kind.UNEQUAL).hasFile2());
		assertFalse(new Match(null,null, Match.Kind.SINGLE_2).hasFile2());
		assertFalse(new Match(null,null, Match.Kind.SINGLE_1).hasFile2());
	}

	@Test
	public void getFile1() {
		assertEquals(file1, new Match(file1,file2, Match.Kind.EQUAL).getFile1());
		assertEquals(file1, new Match(file1,null, Match.Kind.EQUAL).getFile1());
		assertEquals(null, new Match(null,file2, Match.Kind.EQUAL).getFile1());
		assertEquals(null, new Match(null,null, Match.Kind.EQUAL).getFile1());
	}

	@Test
	public void getFile2() {
		assertEquals(file2, new Match(file1,file2, Match.Kind.EQUAL).getFile2());
		assertEquals(file2, new Match(null,file2, Match.Kind.EQUAL).getFile2());
		assertEquals(null, new Match(file1,null, Match.Kind.EQUAL).getFile2());
		assertEquals(null, new Match(null,null, Match.Kind.EQUAL).getFile2());
	}

	@Test
	public void getKind() {
		assertEquals(Match.Kind.EQUAL, new Match(file1,file2, Match.Kind.EQUAL).getKind());
		assertEquals(Match.Kind.UNEQUAL, new Match(file1,file2, Match.Kind.UNEQUAL).getKind());
		assertEquals(Match.Kind.SINGLE_1, new Match(file1,file2, Match.Kind.SINGLE_1).getKind());
		assertEquals(Match.Kind.SINGLE_2, new Match(file1,file2, Match.Kind.SINGLE_2).getKind());
	}

	@Test
	public void getFileName() {
		assertEquals(file2.getName(), new Match(file1,file2, Match.Kind.EQUAL).getFileName());
		assertEquals(file2.getName(), new Match(file1,file2, Match.Kind.UNEQUAL).getFileName());
		assertEquals(file1.getName(), new Match(file1,file2, Match.Kind.SINGLE_1).getFileName());
		assertEquals(file2.getName(), new Match(file1,file2, Match.Kind.SINGLE_2).getFileName());
	}

	@Test
	public void getFullFileName() {
		assertEquals(longestCommonFileSuffix, new Match(file1, file1_, Match.Kind.EQUAL).getFullFileName());
		assertEquals(file1.getName(), new Match(null, file1, Match.Kind.EQUAL).getFullFileName());
	}
}