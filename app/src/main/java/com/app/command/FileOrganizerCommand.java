package com.app.command;

import com.app.service.FileOrganizer;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

/**
 * Main command class for the File Organizer CLI. Handles command-line arguments and options using
 * Picocli.
 */
@Command(
    name = "file-organizer",
    mixinStandardHelpOptions = true, // Adds --help and --version options
    version = "1.0",
    description =
        "Organizes files in a directory by moving them into folders based on their extensions.")
public class FileOrganizerCommand implements Callable<Integer> {

  @Spec CommandSpec spec; // injected by picocli

  @Option(
      names = {"--source", "-s"},
      description = "Source directory containing files to organize",
      required = true)
  private File sourceDir;

  @Option(
      names = {"--dest", "-d"},
      description = "Destination directory where organized folders will be created",
      required = true)
  private File destDir;

  @Override
  public Integer call() throws Exception {
    // Get the output and error streams
    PrintWriter out = spec.commandLine().getOut();
    PrintWriter err = spec.commandLine().getErr();

    // Validate input directories
    if (!sourceDir.exists() || !sourceDir.isDirectory()) {
      err.println("Error: Source directory does not exist or is not a directory");
      return 1;
    }

    if (!destDir.exists()) {
      out.println("Destination directory does not exist. Creating it now...");
      if (!destDir.mkdirs()) {
        err.println("Error: Could not create destination directory");
        return 1;
      }
    } else if (!destDir.isDirectory()) {
      err.println("Error: Destination path exists but is not a directory");
      return 1;
    }

    // Implement file organization logic by calling the FileOrganizer service
    out.println("Organizing files from: " + sourceDir.getAbsolutePath());
    out.println("Moving to: " + destDir.getAbsolutePath());

    FileOrganizer organizer = new FileOrganizer();
    try {
      int movedFilesCount = organizer.organizeFiles(sourceDir, destDir);
      out.println("Successfully organized " + movedFilesCount + " files.");

      // If no files were moved, it might be because the directory was empty
      // or contained only directories, which is still a success condition
      return 0;
    } catch (IOException e) {
      err.println("Error organizing files: " + e.getMessage());
      return 1;
    }
  }
}
