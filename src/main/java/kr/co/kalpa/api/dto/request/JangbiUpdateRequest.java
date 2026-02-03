package kr.co.kalpa.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JangbiUpdateRequest {

    private String item;

    private String location;

    private Integer cost;

    private String spec;

    private String lvl;

    // 삭제할 파일 ID 목록
    private List<Long> deletedFileIds;

    // 새로 추가할 파일 목록
    private List<MultipartFile> newFiles;
}
