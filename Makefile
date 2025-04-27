# Makefile for File Organizer Java Project

all:
	@echo "Available targets:"
	@echo "  build   - Build the project"
	@echo "  run     - Run the project"
	@echo "  test    - Run tests"
	@echo "  clean   - Clean the project"
	@echo "  dist    - Create distribution packages"
	@echo "  format   - Format the code with Spotless"
	@echo "  format-check - Check code formatting with Spotless"
	@echo "  check    - Run all checks"
	@echo "  fatjar   - Create a fat JAR file"

build:
	./gradlew build

run:
	./gradlew run

test:
	./gradlew allTests

clean:
	./gradlew clean

dist:
	./gradlew distTar distZip

format:
	./gradlew spotlessApply

format-check:
	./gradlew spotlessCheck

check:
	./gradlew check

fatjar:
	./gradlew fatJar

.PHONY: build run test clean dist format check fatjar
