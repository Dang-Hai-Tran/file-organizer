package com.app.command;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

/** Unit tests for {@link FileOrganizerCommand}. */
public class FileOrganizerCommandTest {

    private CommandLine commandLine;
    private FileOrganizerCommand command;
    private StringWriter outputWriter;
    private StringWriter errorWriter;

    @BeforeEach
    void setUp() {
        command = new FileOrganizerCommand();
        commandLine = new CommandLine(command);

        // Capture output and error streams
        outputWriter = new StringWriter();
        errorWriter = new StringWriter();
        commandLine.setOut(new PrintWriter(outputWriter));
        commandLine.setErr(new PrintWriter(errorWriter));
    }

    @Test
    void testCommandWithRequiredOptions(@TempDir Path tempDir) {
        // Create a source directory with files
        File sourceDir = tempDir.resolve("source").toFile();
        assertTrue(sourceDir.mkdir());

        // Create a destination directory
        File destDir = tempDir.resolve("dest").toFile();
        assertTrue(destDir.mkdir());

        // Execute command with required options

        int exitCode = commandLine.execute("--source", sourceDir.getAbsolutePath(), "--dest",
                destDir.getAbsolutePath());

        // Verify command executed successfully
        assertEquals(0, exitCode);
        String output = outputWriter.toString();
        assertTrue(output.contains("Organizing files from: " + sourceDir.getAbsolutePath()));
        assertTrue(output.contains("Moving to: " + destDir.getAbsolutePath()));
        assertTrue(output.contains("Successfully organized"));
    }

    @Test
    void testCommandWithShortOptions(@TempDir Path tempDir) {
        // Create a source directory with files
        File sourceDir = tempDir.resolve("source").toFile();
        assertTrue(sourceDir.mkdir());

        // Create a destination directory
        File destDir = tempDir.resolve("dest").toFile();
        assertTrue(destDir.mkdir());

        // Execute command with short options
        int exitCode = commandLine.execute("-s", sourceDir.getAbsolutePath(), "-d", destDir.getAbsolutePath());

        // Verify command executed successfully
        assertEquals(0, exitCode);
        String output = outputWriter.toString();
        assertTrue(output.contains("Organizing files from: " + sourceDir.getAbsolutePath()));
        assertTrue(output.contains("Moving to: " + destDir.getAbsolutePath()));
        assertTrue(output.contains("Successfully organized"));
    }

    @Test
    void testNonExistentSourceDirectory(@TempDir Path tempDir) {
        // Create a non-existent source directory path
        File sourceDir = tempDir.resolve("non-existent").toFile();

        // Create a destination directory
        File destDir = tempDir.resolve("dest").toFile();
        assertTrue(destDir.mkdir());

        // Execute command
        int exitCode = commandLine.execute("--source", sourceDir.getAbsolutePath(), "--dest",
                destDir.getAbsolutePath());

        // Verify command failed with the appropriate error
        assertEquals(1, exitCode);
        String error = errorWriter.toString();
        assertTrue(error.contains("Error: Source directory does not exist"));
    }

    @Test
    void testFileAsSourceDirectory(@TempDir Path tempDir) throws Exception {
        // Create a file instead of a directory for the source
        File sourceFile = tempDir.resolve("sourcefile.txt").toFile();
        assertTrue(sourceFile.createNewFile());

        // Create a destination directory
        File destDir = tempDir.resolve("dest").toFile();
        assertTrue(destDir.mkdir());

        // Execute command
        int exitCode = commandLine.execute("--source", sourceFile.getAbsolutePath(), "--dest",
                destDir.getAbsolutePath());

        // Verify command failed with the appropriate error
        assertEquals(1, exitCode);
        String error = errorWriter.toString();
        assertTrue(error.contains("Error: Source directory does not exist or is not a directory"));
    }

    @Test
    void testNonExistentDestDirectory(@TempDir Path tempDir) {
        // Create source directory
        File sourceDir = tempDir.resolve("source").toFile();
        assertTrue(sourceDir.mkdir());

        // Create non-existent destination directory path
        File destDir = tempDir.resolve("non-existent-dest").toFile();

        // Execute command
        int exitCode = commandLine.execute("--source", sourceDir.getAbsolutePath(), "--dest",
                destDir.getAbsolutePath());

        // Verify command executed successfully and created the destination directory
        assertEquals(0, exitCode);
        String output = outputWriter.toString();
        assertTrue(output.contains("Destination directory does not exist. Creating it now"));
        assertTrue(destDir.exists());
    }

    @Test
    void testFileAsDestDirectory(@TempDir Path tempDir) throws Exception {
        // Create source directory
        File sourceDir = tempDir.resolve("source").toFile();
        assertTrue(sourceDir.mkdir());

        // Create a file instead of a directory for destination
        File destFile = tempDir.resolve("destfile.txt").toFile();
        assertTrue(destFile.createNewFile());

        // Execute command
        int exitCode = commandLine.execute("--source", sourceDir.getAbsolutePath(), "--dest",
                destFile.getAbsolutePath());

        // Verify command failed with appropriate error
        assertEquals(1, exitCode);
        String error = errorWriter.toString();
        assertTrue(error.contains("Error: Destination path exists but is not a directory"));
    }

    @Test
    void testMissingRequiredOptions() {
        // Execute command without required options
        int exitCode = commandLine.execute();

        // Verify command failed
        assertNotEquals(0, exitCode);
        String error = errorWriter.toString();
        assertTrue(error.contains("Missing required options"));
    }

    @Test
    void testHelpOption() {
        // Execute command with help option
        int exitCode = commandLine.execute("--help");

        // Verify help is displayed
        assertEquals(0, exitCode);
        String output = outputWriter.toString();
        assertTrue(output.contains("Usage: file-organizer"));
        assertTrue(output.contains("Organizes files in a directory"));
    }
}
