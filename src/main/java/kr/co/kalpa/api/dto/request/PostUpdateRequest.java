package kr.co.kalpa.api.dto.request;

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
public class PostUpdateRequest {

    @Size(max = 500, message = "제목은 500자 이하여야 합니다")
    private String title;

    private String author;

    private String content;

    @Size(min = 8, max = 8, message = "기준일은 YYYYMMDD 형식이어야 합니다")
    private String baseYmd;

    private List<MultipartFile> newFiles;

    private String deletedFileIds; // JSON 배열 형식: [1,2,3]
}
