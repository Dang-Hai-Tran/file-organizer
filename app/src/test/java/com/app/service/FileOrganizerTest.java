package com.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Unit tests for {@link FileOrganizer}. */
public class FileOrganizerTest {

    private FileOrganizer fileOrganizer;

    @BeforeEach
    void setUp() {
        fileOrganizer = new FileOrganizer();
    }

    @Test
    void testGetFileExtension() {
        // Test normal cases
        assertEquals("txt", fileOrganizer.getFileExtension("document.txt"));
        assertEquals("jpg", fileOrganizer.getFileExtension("photo.jpg"));
        assertEquals("gz", fileOrganizer.getFileExtension("archive.tar.gz"));

        // Test empty extension
        assertEquals("", fileOrganizer.getFileExtension("filename"));

        // Test hidden files
        assertEquals("", fileOrganizer.getFileExtension(".hidden"));

        // Test edge cases
        assertEquals("", fileOrganizer.getFileExtension(""));
        assertEquals("", fileOrganizer.getFileExtension("."));
    }

    @Test
    void testOrganizeFilesEmptyDirectory(@TempDir Path tempDir) throws IOException {
        // Create an empty source directory
        File sourceDir = tempDir.resolve("source").toFile();
        assertTrue(sourceDir.mkdir());

        // Create a destination directory
        File destDir = tempDir.resolve("destination").toFile();
        assertTrue(destDir.mkdir());

        // Organize files
        int count = fileOrganizer.organizeFiles(sourceDir, destDir);

        // Verify no files were moved
        assertEquals(0, count);
        assertEquals(0, Objects.requireNonNull(destDir.list()).length);
    }

    @Test
    void testOrganizeFilesByExtension(@TempDir Path tempDir) throws IOException {
        // Create a source directory
        File sourceDir = tempDir.resolve("source").toFile();
        assertTrue(sourceDir.mkdir());

        // Create a destination directory
        File destDir = tempDir.resolve("destination").toFile();
        assertTrue(destDir.mkdir());

        // Create test files with different extensions
        createTestFile(sourceDir, "document1.txt", "text content");
        createTestFile(sourceDir, "document2.txt", "more text");
        createTestFile(sourceDir, "image.jpg", "image data");
        createTestFile(sourceDir, "archive.zip", "zip data");
        createTestFile(sourceDir, "noextension", "no extension data");

        // Organize files
        int count = fileOrganizer.organizeFiles(sourceDir, destDir);

        // Verify files were moved correctly
        assertEquals(5, count);

        // Check extension folders were created
        assertTrue(new File(destDir, "txt").exists());
        assertTrue(new File(destDir, "jpg").exists());
        assertTrue(new File(destDir, "zip").exists());
        assertTrue(new File(destDir, "no_extension").exists());

        // Check files are in their correct folders
        assertTrue(new File(destDir, "txt/document1.txt").exists());
        assertTrue(new File(destDir, "txt/document2.txt").exists());
        assertTrue(new File(destDir, "jpg/image.jpg").exists());
        assertTrue(new File(destDir, "zip/archive.zip").exists());
        assertTrue(new File(destDir, "no_extension/noextension").exists());

        // The source directory should be empty (except for any hidden files)
        assertEquals(0, countVisibleFiles(sourceDir));
    }

    @Test
    void testOrganizeFilesWithNameConflicts(@TempDir Path tempDir) throws IOException {
        // Create a source directory
        File sourceDir = tempDir.resolve("source").toFile();
        assertTrue(sourceDir.mkdir());

        // Create a destination directory
        File destDir = tempDir.resolve("destination").toFile();
        assertTrue(destDir.mkdir());

        // Create test files with the same name
        createTestFile(sourceDir, "document.txt", "original content");

        // Create a pre-existing file in the destination with the same name
        File txtFolder = new File(destDir, "txt");
        assertTrue(txtFolder.mkdir());
        createTestFile(txtFolder, "document.txt", "pre-existing content");

        // Organize files
        int count = fileOrganizer.organizeFiles(sourceDir, destDir);

        // Verify file was moved correctly
        assertEquals(1, count);

        // Check renamed file exists
        assertTrue(new File(destDir, "txt/document_1.txt").exists());

        // Original file in destination should still exist with its original content
        File originalFile = new File(destDir, "txt/document.txt");
        assertTrue(originalFile.exists());
        assertEquals("pre-existing content", new String(Files.readAllBytes(originalFile.toPath())));

        // Renamed file should have the content from the source file
        File renamedFile = new File(destDir, "txt/document_1.txt");
        assertEquals("original content", new String(Files.readAllBytes(renamedFile.toPath())));
    }

    @Test
    void testOrganizeFilesPreservesAttributes(@TempDir Path tempDir) throws IOException, InterruptedException {
        // Create a source directory
        File sourceDir = tempDir.resolve("source").toFile();
        assertTrue(sourceDir.mkdir());

        // Create a destination directory
        File destDir = tempDir.resolve("destination").toFile();
        assertTrue(destDir.mkdir());

        // Create a test file
        File sourceFile = createTestFile(sourceDir, "document.txt", "content");

        // Get original attributes
        BasicFileAttributes originalAttrs = Files.readAttributes(sourceFile.toPath(), BasicFileAttributes.class);

        // Wait a moment to ensure timestamps would be different if not preserved
        Thread.sleep(100);

        // Organize files
        int count = fileOrganizer.organizeFiles(sourceDir, destDir);

        // Verify file was moved
        assertEquals(1, count);

        // Check attributes were preserved
        File destFile = new File(destDir, "txt/document.txt");
        BasicFileAttributes destAttrs = Files.readAttributes(destFile.toPath(), BasicFileAttributes.class);

        // Verify creation time was preserved
        assertEquals(originalAttrs.creationTime().toMillis(), destAttrs.creationTime().toMillis());

        // Verify last modified time was preserved
        assertEquals(originalAttrs.lastModifiedTime().toMillis(), destAttrs.lastModifiedTime().toMillis());
    }

    @Test
    void testOrganizeFilesSkipsDirectories(@TempDir Path tempDir) throws IOException {
        // Create source directory
        File sourceDir = tempDir.resolve("source").toFile();
        assertTrue(sourceDir.mkdir());

        // Create a subdirectory
        File subDir = new File(sourceDir, "subdir");
        assertTrue(subDir.mkdir());

        // Create a file in the source directory
        createTestFile(sourceDir, "document.txt", "content");

        // Create destination directory
        File destDir = tempDir.resolve("destination").toFile();
        assertTrue(destDir.mkdir());

        // Organize files
        int count = fileOrganizer.organizeFiles(sourceDir, destDir);

        // Verify only the file was moved, not the directory
        assertEquals(1, count);
        assertTrue(new File(destDir, "txt/document.txt").exists());

        // Subdirectory should still be in source
        assertTrue(subDir.exists());
        assertEquals(sourceDir.getAbsolutePath(), subDir.getParentFile().getAbsolutePath());
    }

    @Test
    void testOrganizeFilesSkipsHiddenFiles(@TempDir Path tempDir) throws IOException {
        // Create source directory
        File sourceDir = tempDir.resolve("source").toFile();
        assertTrue(sourceDir.mkdir());

        // Create a hidden file
        createTestFile(sourceDir, ".hidden", "hidden content");

        // Create a normal file
        createTestFile(sourceDir, "visible.txt", "visible content");

        // Create a destination directory
        File destDir = tempDir.resolve("destination").toFile();
        assertTrue(destDir.mkdir());

        // Organize files
        int count = fileOrganizer.organizeFiles(sourceDir, destDir);

        // Verify only the visible file was moved
        assertEquals(1, count);
        assertTrue(new File(destDir, "txt/visible.txt").exists());

        // Hidden file should still be in source
        assertTrue(new File(sourceDir, ".hidden").exists());
    }

    /** Helper method to create a test file with content */
    private File createTestFile(File directory, String filename, String content) throws IOException {
        File file = new File(directory, filename);
        Files.write(file.toPath(), content.getBytes());
        return file;
    }

    /** Helper method to count visible files in a directory */
    private int countVisibleFiles(File directory) {
        File[] files = directory.listFiles(file -> !file.isHidden());
        return files != null ? files.length : 0;
    }
}
