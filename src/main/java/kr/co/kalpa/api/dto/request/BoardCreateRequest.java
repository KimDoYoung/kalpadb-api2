package kr.co.kalpa.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardCreateRequest {

    @NotBlank(message = "게시판 코드는 필수입니다")
    @Size(max = 50, message = "게시판 코드는 50자 이하여야 합니다")
    private String boardCode;

    @NotBlank(message = "게시판 이름은 필수입니다")
    @Size(max = 100, message = "게시판 이름은 100자 이하여야 합니다")
    private String boardNameKor;

    @NotBlank(message = "콘텐츠 타입은 필수입니다")
    private String contentType; // html, markdown

    private String description;
}
