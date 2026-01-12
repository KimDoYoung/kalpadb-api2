---
name: make-test-sh
description: 언급한 table과 관련된 controller를 찾아서 tools/ 폴더 하위에 curl을 사용한 bash shell을 만든다.
tools: Read, Grep, Glob, Bash
---

1. 언급한 table을 바탕으로한 controller 즉 {table명}Controller를 찾는다.
2. 찾은 controller에 기술한 api 리스트를 구한다.
3. 각 api에 대한 test용 bash shell 프로그램을 tools/하위에 test-{table}.sh로 작성한다.
4. 각 test는 curl을 이용한다.
