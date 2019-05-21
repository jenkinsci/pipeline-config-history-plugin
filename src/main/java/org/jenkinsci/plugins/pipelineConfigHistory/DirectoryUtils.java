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

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;

public class DirectoryUtils {

  /**
   * util class.
   */
  private DirectoryUtils() {
  }



  /**
   * Get all files the given directory contains, except the history xml in the root dir.
   *
   * @param directory the directory.
   * @throws IOException if file walking fails.
   * @return all files this directory contains, except the history xml in the root dir.
   */
  public static File[] getAllFilesExceptHistoryXmlFromDirectory(File directory) throws IOException {
    if (!directory.isDirectory()) {
      return new File[0];
    }
    try (Stream<Path> fileWalker = Files.walk(directory.toPath())) {
      return fileWalker
          .filter(Files::isRegularFile)
          .map(Path::toFile)
          .filter(file ->
              !(file.getName().equals(PipelineConfigHistoryConsts.HISTORY_XML_FILENAME)
                  && file.getParentFile().equals(directory))
          )
          .toArray(File[]::new);
    }
  }

  /**
   * checks if the directory file lists are equal.
   *
   * <p>If checkFileContent is true, then also if the file content is equal
   *
   * @param directory        the directory
   * @param compareDirectory the directory to compare with
   * @param checkFileContent also compare file content
   * @return true if directory and compareDirectory are equal
   * @throws IOException if anything goes wrong comparing the directories.
   */
  public static boolean isEqual(Path directory, Path compareDirectory, boolean checkFileContent)
      throws IOException {

    boolean check = isEverythingInCompareDirectory(directory, compareDirectory, checkFileContent);

    // we only need to check file content in on direction.
    boolean checkOppositeFileContent = false;

    boolean checkOpposite = check
        && isEverythingInCompareDirectory(compareDirectory, directory, checkOppositeFileContent);
    return check && checkOpposite;

  }

  /**
   * Checks if the directory file lists and file content is equal.
   *
   * @param directory        the directory
   * @param compareDirectory the directory to compare with
   * @param checkFileContent also compare file content
   * @return true if directory and compareDirectory are equal
   * @throws IOException if anything goes wrong while comparing.
   */
  private static boolean isEverythingInCompareDirectory(Path directory, Path compareDirectory,
                                                        boolean checkFileContent)
      throws IOException {
    if (directory == null || compareDirectory == null) {
      return checkForNulls(directory, compareDirectory);
    }

    File directoryFile = directory.toFile();
    File compareFile = compareDirectory.toFile();

    // check, if there is the same number of files/subdirectories
    File[] directoryFiles = directoryFile.listFiles();
    File[] compareFiles = compareFile.listFiles();

    if (directoryFiles == null || compareFiles == null) {
      return checkForNulls(directoryFiles, compareFiles);
    }
    return (directoryFiles.length == compareFiles.length)
        && compareDirectoryContents(directory, compareDirectory, checkFileContent);
  }

  private static boolean checkForNulls(Object directory, Object compareDirectory) {
    return (directory == null && compareDirectory == null);
  }

  private static boolean compareDirectoryContents(Path directory, Path compareDirectory,
                                                  boolean checkFileContent) throws IOException {
    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
      for (Path directoryFilePath : directoryStream) {
        // search for directoryFile in the compareDirectory
        Path compareFilePath = compareDirectory.resolve(directoryFilePath.getFileName());
        if (compareFilePath == null) {
          return false;
        }

        File directoryFile = directoryFilePath.toFile();
        boolean compareFile = !compareFilePath.toFile().exists();
        boolean fileContentUnequalIfAskedFor =
            directoryFile.isFile()
                && checkFileContent
                && !FileUtils.contentEquals(compareFilePath.toFile(), directoryFile);
        if (compareFile || fileContentUnequalIfAskedFor) {
          return false;
        } else if (directoryFile.isDirectory()) {
          boolean result = isEverythingInCompareDirectory(directoryFilePath, compareFilePath,
              checkFileContent);

          // cancel if not equal, otherwise continue processing
          if (!result) {
            return false;
          }
        }

      }
    }

    return true;
  }
}