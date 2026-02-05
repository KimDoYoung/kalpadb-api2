package kr.co.kalpa.api.dto.response;

import kr.co.kalpa.api.entity.ApFile;
import kr.co.kalpa.api.entity.ApNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApNodeResponse {

    private String id;
    private String nodeType;
    private String parentId;
    private String name;
    private Integer depth;
    private LocalDateTime createDt;
    private LocalDateTime modifyDt;
    private Integer childCount;
    private Long totalSize;

    // 파일 전용 필드
    private Long fileSize;
    private String contentType;
    private String originalName;

    // 트리용 필드
    private List<ApNodeResponse> children;

    public static ApNodeResponse from(ApNode node) {
        ApNodeResponseBuilder builder = ApNodeResponse.builder()
                .id(node.getId())
                .nodeType(node.getNodeType())
                .parentId(node.getParentId())
                .name(node.getName())
                .depth(node.getDepth())
                .createDt(node.getCreateDt())
                .modifyDt(node.getModifyDt())
                .childCount(node.getChildCount())
                .totalSize(node.getTotalSize());

        ApFile file = node.getApFile();
        if (file != null) {
            builder.fileSize(file.getFileSize())
                   .contentType(file.getContentType())
                   .originalName(file.getOriginalName());
        }

        return builder.build();
    }
}
