# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a RESTful API for the MySQL `kalpadb` database, built with Spring Boot. The API is consumed by a client application called PCMS2.

## Technology Stack

- JDK 21
- Spring Boot 4.0.1
- JWT for authentication
- Tomcat (embedded)
- Gradle for build management
- MySQL database

## Development Commands

### Building

```bash
./gradlew build
```

### Running Tests

```bash
./gradlew test
```

### Running Single Test

```bash
./gradlew test --tests <FullyQualifiedTestClassName>
./gradlew test --tests <FullyQualifiedTestClassName>.<methodName>
```

### Running the Application

```bash
./gradlew bootRun
```

### Cleaning Build

```bash
./gradlew clean
```

## Architecture Notes

- This is a REST API layer over the `kalpadb` MySQL database
- JWT-based authentication is used for securing endpoints
- The client application PCMS2 consumes this API
- Standard Spring Boot project structure expected with controllers, services, repositories, and models
