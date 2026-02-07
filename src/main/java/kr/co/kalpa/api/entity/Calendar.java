package kr.co.kalpa.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "calendar", indexes = {
        @Index(name = "idx_gubun_ymd", columnList = "gubun, ymd"),
        @Index(name = "idx_sorl", columnList = "sorl")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Calendar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 1)
    private String gubun; // H:Holiday, E:Event, Y:Yearly, M:Monthly, S:SolarTerm

    @Column(nullable = false, length = 1)
    @Builder.Default
    private String sorl = "S"; // S:Solar, L:Lunar

    @Column(nullable = false, length = 8)
    private String ymd;

    @Column(nullable = false, length = 200)
    private String content;

    @CreationTimestamp
    @Column(name = "created_dt", nullable = false, updatable = false)
    private LocalDateTime createdDt;
}
