package com.app;

import static org.junit.jupiter.api.Assertions.*;

import com.app.command.FileOrganizerCommand;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

/**
 * Integration tests for the File Organizer CLI application. These tests verify the end-to-end
 * functionality of the application.
 */
public class FileOrganizerIntegrationTest {

  @TempDir Path sourceDirPath;

  @TempDir Path destDirPath;

  private File sourceDir;
  private File destDir;

  @BeforeEach
  void setUp() {
    sourceDir = sourceDirPath.toFile();
    destDir = destDirPath.toFile();
  }

  @AfterEach
  void tearDown() {
    // Clean up any test files that might be left
    Arrays.stream(sourceDir.listFiles()).forEach(File::delete);

    if (destDir.exists()) {
      Arrays.stream(destDir.listFiles())
          .forEach(
              file -> {
                if (file.isDirectory()) {
                  Arrays.stream(file.listFiles()).forEach(File::delete);
                }
                file.delete();
              });
    }
  }

  /** Test the complete file organization process end-to-end. */
  @Test
  void testCompleteFileOrganization() throws IOException {
    // Create test files in the source directory with different extensions
    createTestFile(sourceDir, "document1.txt", "Test content 1");
    createTestFile(sourceDir, "document2.txt", "Test content 2");
    createTestFile(sourceDir, "image.jpg", "Fake image content");
    createTestFile(sourceDir, "script.sh", "#!/bin/bash\necho 'Hello'");
    createTestFile(sourceDir, "noextension", "File with no extension");

    // Set specific modification times for testing preservation
    FileTime modTime = FileTime.from(Instant.parse("2023-01-01T12:00:00Z"));
    Files.setLastModifiedTime(sourceDirPath.resolve("document1.txt"), modTime);

    // Create the command line arguments
    String[] args = {
      "--source", sourceDir.getAbsolutePath(),
      "--dest", destDir.getAbsolutePath()
    };

    // Execute the command
    int exitCode = new CommandLine(new FileOrganizerCommand()).execute(args);

    // Verify command executed successfully
    assertEquals(0, exitCode, "Command should execute without errors");

    // Verify folders were created based on file extensions
    assertTrue(new File(destDir, "txt").exists(), "txt folder should be created");
    assertTrue(new File(destDir, "jpg").exists(), "jpg folder should be created");
    assertTrue(new File(destDir, "sh").exists(), "sh folder should be created");
    assertTrue(new File(destDir, "no_extension").exists(), "no_extension folder should be created");

    // Verify files were moved correctly
    assertTrue(
        new File(destDir, "txt/document1.txt").exists(),
        "document1.txt should be moved to txt folder");
    assertTrue(
        new File(destDir, "txt/document2.txt").exists(),
        "document2.txt should be moved to txt folder");
    assertTrue(
        new File(destDir, "jpg/image.jpg").exists(), "image.jpg should be moved to jpg folder");
    assertTrue(
        new File(destDir, "sh/script.sh").exists(), "script.sh should be moved to sh folder");
    assertTrue(
        new File(destDir, "no_extension/noextension").exists(),
        "noextension should be moved to no_extension folder");

    // Verify source files no longer exist
    assertFalse(
        new File(sourceDir, "document1.txt").exists(),
        "document1.txt should no longer exist in source");
    assertFalse(
        new File(sourceDir, "document2.txt").exists(),
        "document2.txt should no longer exist in source");

    // Verify timestamp preservation
    FileTime movedFileTime = Files.getLastModifiedTime(destDirPath.resolve("txt/document1.txt"));
    assertEquals(modTime, movedFileTime, "File modification time should be preserved");
  }

  /** Test handling of name conflicts during file organization. */
  @Test
  void testFileNameConflictHandling() throws IOException {
    // Create files with the same name in the source directory
    createTestFile(sourceDir, "duplicate.txt", "Original content");

    // Create a conflict by pre-creating the destination structure with the same filename
    File txtFolder = new File(destDir, "txt");
    txtFolder.mkdir();
    createTestFile(txtFolder, "duplicate.txt", "Existing content");

    // Execute the command
    String[] args = {
      "--source", sourceDir.getAbsolutePath(),
      "--dest", destDir.getAbsolutePath()
    };
    int exitCode = new CommandLine(new FileOrganizerCommand()).execute(args);

    // Verify command executed successfully
    assertEquals(0, exitCode, "Command should execute without errors");

    // Verify original file still exists in destination
    File originalFile = new File(destDir, "txt/duplicate.txt");
    assertTrue(originalFile.exists(), "Original file should still exist");
    assertEquals(
        "Existing content",
        Files.readString(originalFile.toPath()),
        "Original file content should be preserved");

    // Verify renamed file was created with suffix
    File renamedFile = new File(destDir, "txt/duplicate_1.txt");
    assertTrue(renamedFile.exists(), "Renamed file should exist");
    assertEquals(
        "Original content",
        Files.readString(renamedFile.toPath()),
        "Renamed file should have the moved content");
  }

  /** Test handling of empty source directory. */
  @Test
  void testEmptySourceDirectory() {
    // Execute the command with empty source directory
    String[] args = {
      "--source", sourceDir.getAbsolutePath(),
      "--dest", destDir.getAbsolutePath()
    };
    int exitCode = new CommandLine(new FileOrganizerCommand()).execute(args);

    // Verify command executed successfully
    assertEquals(0, exitCode, "Command should execute without errors even with empty source");

    // Verify no folders were created in destination
    assertEquals(0, destDir.list().length, "No folders should be created when source is empty");
  }

  /** Test the help command functionality. */
  @Test
  void testHelpCommand() {
    // Execute the help command
    String[] args = {"--help"};
    int exitCode = new CommandLine(new FileOrganizerCommand()).execute(args);

    // Verify command executed successfully with exit code 0
    assertEquals(0, exitCode, "Help command should return exit code 0");
  }

  /** Helper method to create a test file with content. */
  private void createTestFile(File directory, String filename, String content) throws IOException {
    File file = new File(directory, filename);
    Files.write(file.toPath(), content.getBytes());
  }
}
