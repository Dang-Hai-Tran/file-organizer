# File Organizer CLI Implementation Plan

## Overview
This document outlines the implementation plan for the File Organizer CLI project (BJ Sprint 1), which includes tasks from BJ-1 and BJ-2 Jira tickets.

## Sprint Information
- **Sprint Name**: BJ Sprint 1
- **Start Date**: April 24, 2025
- **End Date**: May 7, 2025

## Jira Tickets

### BJ-1: File Organizer CLI
**User Story**: As a power user, when I want to tidy up my project directories, I want a command-line tool that moves files into folders by extension so that I can do this quickly without manual sorting.

**Acceptance Criteria**:
1. Run command: `file-organizer --source <dir> --dest <dir>` should execute without errors.
2. Extension-based folders: For each file in the source directory, a folder matching its extension should be created in the destination directory (e.g., .doc → /doc).
3. File move: Files must be physically moved, not copied, while preserving modification timestamps.
4. Edge cases:
   - When no files match an extension, no empty folder should be created.
   - Conflicting filenames should be auto-renamed with a numeric suffix.
5. Help output: `file-organizer --help` should print usage instructions and return 0 as the exit code.

### BJ-2: Scaffold Gradle project with CLI entry point
This task involves setting up the Gradle project structure for the CLI application.

## Implementation Plan

### 1. Project Setup (BJ-3)
- [x] Initialize Gradle project
- [x] Update build.gradle with required dependencies:
  - [x] Add command-line parsing library (picocli)
  - [x] Configure build.gradle for CLI application distribution
- [x] Configure Gradle application plugin for CLI application

### 2. Core Implementation (BJ-4, 5, 6)
- [x] Design the command-line interface:
  - [x] Create FileOrganizerCommand class with picocli annotations
  - [x] Define command-line arguments (--source, --dest, --help)
  - [x] Implement help command functionality

- [x] Implement file organization logic:
  - [x] Create FileOrganizer service class
  - [x] Implement directory traversal for source directory
  - [x] Create extension detection methods
  - [x] Implement folder creation logic
  - [x] Write file moving logic with timestamp preservation
  
- [ ] Implement edge case handling:
  - [ ] Skip creating folders for non-existent extensions
  - [ ] Add file renaming logic for name conflicts
  - [ ] Add validation for source and destination directories

### 3. Testing
- [ ] Update AppTest.java to remove template test
- [ ] Write unit tests for core functionality:
  - [ ] Test extension detection
  - [ ] Test folder creation logic
  - [ ] Test file moving operations
  
- [ ] Write integration tests:
  - [ ] End-to-end tests for complete file organization process
  - [ ] Edge case tests for conflicting filenames

### 4. Documentation
- [ ] Add JavaDoc comments to all public classes and methods
- [ ] Create README.md file with:
  - [ ] Project description
  - [ ] Installation instructions
  - [ ] Usage examples
  - [ ] Command-line options documentation
- [ ] Add command-line help documentation

### 5. Quality Assurance
- [ ] Run static code analysis
- [ ] Ensure all tests pass
- [ ] Verify all acceptance criteria:
  - [ ] Command executes without errors
  - [ ] Folders are created based on extension
  - [ ] Files are moved correctly with preserved timestamps
  - [ ] Edge cases are handled properly
  - [ ] Help output works as expected
