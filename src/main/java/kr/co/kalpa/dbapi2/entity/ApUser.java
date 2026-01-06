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
@Table(name = "ap_user")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApUser {
    @Id
    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "user_pw", nullable = false)
    private String userPw;

    @Column(name = "user_nm", nullable = false, length = 100)
    private String userNm;

    @Column(name = "user_home_folder", length = 500)
    private String userHomeFolder;
}
