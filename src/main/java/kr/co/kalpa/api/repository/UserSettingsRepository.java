package kr.co.kalpa.api.repository;

import kr.co.kalpa.api.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {
    Optional<UserSettings> findByUserIdAndSettingKey(String userId, String settingKey);

    List<UserSettings> findByUserId(String userId);
}
