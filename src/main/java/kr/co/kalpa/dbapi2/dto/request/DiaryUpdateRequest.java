package kr.co.kalpa.dbapi2.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DiaryUpdateRequest {

    private String content;

    @Size(max = 300, message = "요약은 최대 300자까지 입력 가능합니다")
    private String summary;
}
