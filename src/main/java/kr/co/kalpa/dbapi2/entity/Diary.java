package kr.co.kalpa.dbapi2.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dairy")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Diary {

    @Id
    @Column(name = "ymd", length = 8)
    private String ymd;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "summary", length = 300)
    private String summary;

    /**
     * Update method for PUT requests
     */
    public void update(String content, String summary) {
        this.content = content;
        this.summary = summary;
    }
}
