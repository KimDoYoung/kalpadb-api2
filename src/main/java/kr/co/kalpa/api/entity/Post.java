package kr.co.kalpa.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "board_id", nullable = false)
    private Long boardId;

    @Column(name = "title", length = 500, nullable = false)
    private String title;

    @Column(name = "author", length = 100, nullable = false)
    private String author;

    @Column(name = "content_type", length = 30, nullable = false)
    private String contentType; // html, markdown

    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Column(name = "base_ymd", length = 8, nullable = false)
    private String baseYmd; // YYYYMMDD

    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (viewCount == null) {
            viewCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void update(String title, String author, String contentType, String content, String baseYmd) {
        this.title = title;
        this.author = author;
        this.contentType = contentType;
        this.content = content;
        this.baseYmd = baseYmd;
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementViewCount() {
        this.viewCount++;
    }
}
