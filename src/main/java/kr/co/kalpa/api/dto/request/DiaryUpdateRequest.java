package kr.co.kalpa.api.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DiaryUpdateRequest {

    private String content;

    @Size(max = 300, message = "요약은 최대 300자까지 입력 가능합니다")
    private String summary;

    // 삭제할 파일 ID 목록
    private List<Long> deletedFileIds;

    // 새로 추가할 파일 목록
    private List<MultipartFile> newFiles;
}
