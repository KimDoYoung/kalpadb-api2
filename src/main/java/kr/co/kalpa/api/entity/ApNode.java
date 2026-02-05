package kr.co.kalpa.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ap_node")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApNode {

    @Id
    @Column(name = "id", length = 40)
    private String id;

    @Column(name = "node_type", nullable = false, length = 1)
    private String nodeType; // 'F': 파일, 'D': 디렉토리

    @Column(name = "parent_id", length = 40)
    private String parentId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "depth", nullable = false)
    private Integer depth;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "delete_dt")
    private LocalDateTime deleteDt;

    @Column(name = "create_dt", nullable = false, updatable = false)
    private LocalDateTime createDt;

    @Column(name = "modify_dt")
    private LocalDateTime modifyDt;

    @Column(name = "child_count")
    private Integer childCount;

    @Column(name = "total_size")
    private Long totalSize;

    @OneToOne(mappedBy = "node", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ApFile apFile;

    @PrePersist
    protected void onCreate() {
        if (createDt == null) createDt = LocalDateTime.now();
        if (isDeleted == null) isDeleted = false;
        if (childCount == null) childCount = 0;
        if (totalSize == null) totalSize = 0L;
    }

    public void rename(String newName) {
        this.name = newName;
        this.modifyDt = LocalDateTime.now();
    }

    public void moveTo(String newParentId, int newDepth) {
        this.parentId = newParentId;
        this.depth = newDepth;
        this.modifyDt = LocalDateTime.now();
    }

    public void softDelete() {
        this.isDeleted = true;
        this.deleteDt = LocalDateTime.now();
    }

    public void updateChildCount(int count) {
        this.childCount = count;
    }

    public void updateTotalSize(long size) {
        this.totalSize = size;
    }
}
