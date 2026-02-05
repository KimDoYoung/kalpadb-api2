package kr.co.kalpa.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ap_file")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApFile {

    @Id
    @Column(name = "node_id", length = 40)
    private String nodeId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "node_id")
    private ApNode node;

    @Column(name = "saved_path", nullable = false, length = 1000)
    private String savedPath; // base-dir 제외한 상대경로 (yyyy/MM/dd/파일명)

    @Column(name = "original_name", nullable = false, length = 500)
    private String originalName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "sha256_hash", length = 64)
    private String sha256Hash;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "thumbnail_path", length = 500)
    private String thumbnailPath;

    @Column(name = "upload_dt", nullable = false, updatable = false)
    private LocalDateTime uploadDt;

    @PrePersist
    protected void onCreate() {
        if (uploadDt == null) uploadDt = LocalDateTime.now();
    }
}
