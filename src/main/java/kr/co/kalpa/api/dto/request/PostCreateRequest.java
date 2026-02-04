package kr.co.kalpa.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCreateRequest {

    @NotNull(message = "게시판 ID는 필수입니다")
    private Long boardId;

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 500, message = "제목은 500자 이하여야 합니다")
    private String title;

    private String author;

    @NotBlank(message = "내용은 필수입니다")
    private String content;

    @NotBlank(message = "기준일은 필수입니다")
    @Size(min = 8, max = 8, message = "기준일은 YYYYMMDD 형식이어야 합니다")
    private String baseYmd;

    private List<MultipartFile> files;
}
