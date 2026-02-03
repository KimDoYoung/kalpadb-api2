package kr.co.kalpa.api.dto.response;

import kr.co.kalpa.api.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {

    private Long id;
    private Long boardId;
    private String title;
    private String author;
    private String contentType;
    private String content;
    private Integer viewCount;
    private String baseYmd;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer attachmentCount;

    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .boardId(post.getBoardId())
                .title(post.getTitle())
                .author(post.getAuthor())
                .contentType(post.getContentType())
                .content(post.getContent())
                .viewCount(post.getViewCount())
                .baseYmd(post.getBaseYmd())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    /**
     * 목록 조회용 (content 제외)
     */
    public static PostResponse fromWithoutContent(Post post) {
        PostResponse response = from(post);
        response.setContent(null);
        return response;
    }
}
