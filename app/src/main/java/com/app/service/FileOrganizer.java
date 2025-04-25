package com.app.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Map;

/** Service class to handle the core functionality of organizing files by extension. */
public class FileOrganizer {

  /**
   * Organizes files by moving them from the source directory to extension-based folders in the
   * destination directory.
   *
   * @param sourceDir Source directory containing files to organize
   * @param destDir Destination directory where organized folders will be created
   * @return Number of files successfully moved
   * @throws IOException If an I/O error occurs
   */
  public int organizeFiles(File sourceDir, File destDir) throws IOException {
    // Count successfully moved files
    int movedFilesCount = 0;

    // Track created folders to avoid duplicating folder creation checks
    Map<String, File> extensionFolders = new HashMap<>();

    // Get all files in the source directory (non-recursive)
    File[] files = sourceDir.listFiles();
    if (files == null || files.length == 0) {
      return 0;
    }

    for (File file : files) {
      // Skip directories and hidden files
      if (file.isDirectory() || file.getName().startsWith(".")) {
        continue;
      }

      // Extract the file extension
      String extension = getFileExtension(file.getName());
      String folderName = extension.isEmpty() ? "no_extension" : extension;

      // Get or create the destination folder for this extension
      File extensionFolder = extensionFolders.get(folderName);
      if (extensionFolder == null) {
        extensionFolder = new File(destDir, folderName);
        extensionFolders.put(folderName, extensionFolder);
      }

      // Create the folder if it doesn't exist
      if (!extensionFolder.exists()) {
        if (!extensionFolder.mkdir()) {
          System.err.println("Failed to create folder: " + extensionFolder.getPath());
          continue;
        }
      }

      // Prepare destination file
      File destFile = new File(extensionFolder, file.getName());

      // Handle name conflicts
      if (destFile.exists()) {
        destFile = generateUniqueFileName(extensionFolder, file.getName());
      }

      // Move the file and preserve timestamps
      if (moveFileWithAttributes(file, destFile)) {
        movedFilesCount++;
      }
    }

    return movedFilesCount;
  }

  /**
   * Extracts the extension from a filename.
   *
   * @param fileName Name of the file
   * @return The extension without the dot, or an empty string if no extension
   */
  public String getFileExtension(String fileName) {
    int lastDotIndex = fileName.lastIndexOf('.');
    if (lastDotIndex <= 0 || fileName.startsWith(".")) {
      return ""; // No extension or hidden file
    }
    return fileName.substring(lastDotIndex + 1).toLowerCase(); // Normalize to lowercase
  }

  /**
   * Generates a unique filename when a name conflict occurs.
   *
   * @param folder The folder where the file will be placed
   * @param fileName Original file name
   * @return A File object with a unique name
   */
  private File generateUniqueFileName(File folder, String fileName) {
    String baseName;
    String extension;

    // Use the last dot to split the basename and extension, ensuring this behavior is intentional
    int lastDotIndex = fileName.lastIndexOf('.');
    if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
      baseName = fileName.substring(0, lastDotIndex);
      extension = fileName.substring(lastDotIndex);
    } else {
      baseName = fileName;
      extension = "";
    }

    int counter = 1;
    File newFile;

    do {
      newFile = new File(folder, baseName + "_" + counter + extension);
      counter++;
    } while (newFile.exists());

    return newFile;
  }

  /**
   * Moves a file to the destination and preserves its timestamps.
   *
   * @param source Source file
   * @param dest Destination file
   * @return True if the move was successful
   */
  private boolean moveFileWithAttributes(File source, File dest) {
    try {
      Path sourcePath = source.toPath();
      Path destPath = dest.toPath();

      // Get file attributes before moving
      BasicFileAttributes attrs = Files.readAttributes(sourcePath, BasicFileAttributes.class);
      FileTime creationTime = attrs.creationTime();
      FileTime lastModifiedTime = attrs.lastModifiedTime();
      FileTime lastAccessTime = attrs.lastAccessTime();

      // Move the file
      Files.move(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);

      // Preserve the timestamps
      Files.setAttribute(destPath, "creationTime", creationTime);
      Files.setAttribute(destPath, "lastModifiedTime", lastModifiedTime);
      Files.setAttribute(destPath, "lastAccessTime", lastAccessTime);

      System.out.println("Moved: " + source.getName() + " -> " + dest.getPath());
      return true;
    } catch (IOException e) {
      System.err.println("Failed to move file " + source.getName() + ": " + e.getMessage());
      return false;
    }
  }
}
