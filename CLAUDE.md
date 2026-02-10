# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a RESTful API for the MySQL `kalpadb` database, built with Spring Boot. The API is consumed by a client application called PCMS2.
** 모바일은 생각하지 말 것!** 완전히 desktop only!

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

- mysql을 사용
- /api 로 Restful api를 제공한다.
- frontend도 함께 구현한다.
  - alpine.js
  - 타임리프
  - tailwindcss(npm사용)
  - UI는 **desktop only** (모바일 반응형 불필요, 모바일은 Flutter로 별도 개발 예정)
  - sm:, md:, lg:, xl: 등 반응형 breakpoint 클래스 사용 금지
  - 모바일 전용 메뉴, hamburger 메뉴 등 불필요

## table desc

- docs/tables.sql 을 참고한다.
- **주의: kalpadb_schema.sql이 아니다!**
