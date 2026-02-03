package kr.co.kalpa.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JangbiCreateRequest {

    @NotBlank(message = "날짜(ymd)는 필수입니다")
    @Pattern(regexp = "^\\d{8}$", message = "날짜는 YYYYMMDD 형식의 8자리 숫자여야 합니다")
    private String ymd;

    @NotBlank(message = "품목(item)은 필수입니다")
    private String item;

    private String location;

    private Integer cost;

    private String spec;

    @NotBlank(message = "레벨(lvl)은 필수입니다")
    private String lvl;

    // 첨부 파일 목록
    private List<MultipartFile> files;
}
