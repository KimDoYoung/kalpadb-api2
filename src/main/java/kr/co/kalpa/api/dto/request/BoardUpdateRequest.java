package kr.co.kalpa.api.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardUpdateRequest {

    @Size(max = 100, message = "게시판 이름은 100자 이하여야 합니다")
    private String boardNameKor;

    private String description;
}
