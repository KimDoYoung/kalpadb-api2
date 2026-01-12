---
name: make-crud
description: 언급한 테이블에 대해서 crud api를 만든다.
context : fork
allowed-tools :
    - WebSearch
    - Write
    - Edit
    - Bash
---

# 작업 순서

1. @docs/tables.sql에서 언급한 테이블의 schema를 확인한다.
2. api url 은 /api/{table명} 로 시작한다.
3. create, view, update, delete는  method로 구분한다.
4. controller, service, dto 등을 {table명}을 바탕으로 작성한다.
5. tools/ 폴더 하위에 test-{table}.sh을 작성한다.
