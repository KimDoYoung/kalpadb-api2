package kr.co.kalpa.dbapi2.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DiaryCreateRequest {

    @NotBlank(message = "날짜(ymd)는 필수입니다")
    @Pattern(regexp = "^\\d{8}$", message = "날짜는 YYYYMMDD 형식의 8자리 숫자여야 합니다")
    private String ymd;

    private String content;

    @Size(max = 300, message = "요약은 최대 300자까지 입력 가능합니다")
    private String summary;
}
