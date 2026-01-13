---
name: make-table-crud
description: 언급한 table과 관련된 controller과 service, dto등을 만들고 api와 ui를 만든다.
tools: Read, Grep, Glob, Bash
---

## 전제

- 사용자는 분명하게 테이블명을 언급해야한다. 만약 사용자로부터 테이블명을 언급받지 못한 경우 사용자에게 묻는다.

## API 만들기

1. Package kr.co.kalpa.api 하위를 사용한다.
2. Controller, Service, Dto등을 테이블명에 기초하여 작성한다.  ex: diary -> DiaryApiController, DiaryService
3. Controller명인 경우 Ui와 구분하기 위해서 DiaryApiController라고 명명한다.
4. @docs/tables.sql에서 언급한 테이블의 schema를 확인한다.
5. api는 '/api/{table}' 로 시작한다.
6. CRUD는 post,get등 method로 구분한다.
7. 4개의 api를 구현한다.
   1. create : post , '/api/diary'
   2. update : put , '/api/diary'
   3. view   : get , '/api/diary/{id}'
   4. delete : delete, '/api/diary/{id}'

## UI만들기

1. Package kr.co.kalpa.ui 하위를 사용한다.
2. html을 랜더링하기 위한 Controller 를 작성한다. ex: DiaryController
3. templates/{table명} 폴더 생성 그 하위에. list.html, create.html, edit.html, view.html을 만든다.
