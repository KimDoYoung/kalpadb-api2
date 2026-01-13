package kr.co.kalpa.api.dto.response;

import kr.co.kalpa.api.entity.Files;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileResponse {

    private Long fileId;
    private String orgFileName;
    private Long fileSize;
    private String mimeType;
    private LocalDateTime createdAt;

    public static FileResponse from(Files files) {
        return FileResponse.builder()
                .fileId(files.getFileId())
                .orgFileName(files.getOrgFileName())
                .fileSize(files.getFileSize())
                .mimeType(files.getMimeType())
                .createdAt(files.getCreatedAt())
                .build();
    }
}
