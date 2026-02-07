package kr.co.kalpa.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "calendar_public", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "ymd", "data_type" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarPublic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "data_type", nullable = false, length = 10)
    private String dataType; // HOLIDAY, ANNIVERSARY, SOLAR_TERM

    @Column(nullable = false, length = 8)
    private String ymd;

    @Column(nullable = false, length = 200)
    private String content;

    @CreationTimestamp
    @Column(name = "created_dt", nullable = false, updatable = false)
    private LocalDateTime createdDt;
}
