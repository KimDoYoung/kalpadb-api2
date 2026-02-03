package kr.co.kalpa.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "jangbi")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Jangbi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "ymd", length = 8, nullable = false)
    private String ymd;

    @Column(name = "item", length = 100, nullable = false)
    private String item;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "cost")
    private Integer cost;

    @Column(name = "spec", columnDefinition = "TEXT")
    private String spec;

    @Column(name = "lvl", length = 1, nullable = false)
    private String lvl;

    @Column(name = "modify_dt", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime modifyDt;

    @PrePersist
    protected void onCreate() {
        if (modifyDt == null) {
            modifyDt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        modifyDt = LocalDateTime.now();
    }

    /**
     * Update method for PUT requests
     */
    public void update(String item, String location, Integer cost, String spec, String lvl) {
        this.item = item;
        this.location = location;
        this.cost = cost;
        this.spec = spec;
        this.lvl = lvl;
        this.modifyDt = LocalDateTime.now();
    }

    /**
     * Update spec only (for editor image processing)
     */
    public void updateSpec(String spec) {
        this.spec = spec;
        this.modifyDt = LocalDateTime.now();
    }
}
