# File Organizer CLI

A command-line tool to organize files into folders based on their extensions.

## Overview

File Organizer CLI helps you tidy up directories by automatically moving files into folders based on their file extensions. For example, all `.txt` files will be moved into a `txt` folder, all `.jpg` files into a `jpg` folder, and so on.

## Features

- Organize files by extension with a simple command
- Preserve file modification timestamps during moves
- Auto-rename files to avoid conflicts
- Files without extensions are organized into a "no_extension" folder
- Clean command-line interface with help documentation

## Installation

1. Ensure you have Java 21 or later installed:
   ```bash
   java -version
   ```

2. Build `file-organizer.jar` yourself:
   ```bash
   git clone https://gitlab.com/datran4/file-organizer
   cd file-organizer
   ./gradlew fatJar
   ```

3. Copy the JAR file to your preferred location:
   ```bash
   cp app/build/libs/app-all.jar ~/file-organizer.jar
   ```

## Usage

### Basic Command

```bash
java -jar file-organizer.jar --source /path/to/source --dest /path/to/destination
```

### Command Options

- `--source` or `-s`: Source directory containing files to organize (required)
- `--dest` or `-d`: Destination directory where organized folders will be created (required)
- `--help` or `-h`: Show help message
- `--version` or `-V`: Show version information

### Examples

Organize files from your Downloads folder into a new "Organized" folder:
```bash
java -jar file-organizer.jar --source ~/Downloads --dest ~/Organized
```

Organize files from the current directory into a subdirectory:
```bash
java -jar file-organizer.jar --source . --dest ./Organized
```

Get help:
```bash
java -jar file-organizer.jar --help
```

## How It Works

1. The tool scans the source directory for files (non-recursive)
2. For each file found:
   - Extracts the file extension (e.g., "txt" from "document.txt")
   - Creates a folder for that extension in the destination if it doesn't exist
   - Moves the file to the corresponding folder, preserving timestamps
   - If a file with the same name already exists, adds a numeric suffix (e.g., "document_1.txt")

## Edge Cases Handled

- Files without extensions are moved to a "no_extension" folder
- Hidden files (those starting with a dot) are skipped
- Folders are not created for extensions that don't exist in the source
- Files are renamed with numeric suffixes when name conflicts occur

## Building from Source

1. Clone the repository:
   ```bash
   git clone https://gitlab.com/datran4/file-organizer.git
   cd file-organizer
   ```

2. Build with Gradle:
   ```bash
   ./gradlew build
   ```

3. Create a distribution package:
   ```bash
   ./gradlew distZip
   ```
   The resulting zip file will be in `app/build/distributions/`.

4. To create a fat JAR with all dependencies:
   ```bash
   ./gradlew fatJar
   ```
   The JAR file will be in `app/build/libs/app-all.jar`.

## Running Tests

Run the unit tests:
```bash
./gradlew test
```

Run the integration tests:
```bash
./gradlew integrationTest
```

Run all tests:
```bash
./gradlew allTests
```

## Contributing

1. Fork the repository
2. Create your feature branch: `git checkout -b feature/my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin feature/my-new-feature`
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Built with [picocli](https://picocli.info/) for command-line parsing
- Developed as part of the BJ-1 ticket in the Backend Java sprint
