# dariy

@src/main/java/kr/co/kalpa/dbapi2/controller/DiaryController.java  에 수정사항이 있음.

1. @docs/kalpadb_schema.sql 의 dairy 테이블은 비교적 간단하게 3개의 필드로 이루어져 있음.
2. 그러나. 실제로 CRUD는 첨부파일을 갖고 동작해야함.
3. 일지에서 crate시  첨부파일이 같이 올라올 수 있음.
4. 마찬가지로 crud 다른 api에서도 그러함.
5. 올라온 file은 저장되어야함file.upload.attach-files-dir 에 yyyy/mm 을 작성하여 저장
6. 첨부파일은
7. 첨부파일은 match_file_var에  `dairy`,
