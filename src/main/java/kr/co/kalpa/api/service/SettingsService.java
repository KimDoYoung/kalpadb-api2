package kr.co.kalpa.api.service;

import kr.co.kalpa.api.dto.request.PasswordChangeRequest;
import kr.co.kalpa.api.dto.response.SettingsResponse;
import kr.co.kalpa.api.entity.UserSettings;
import kr.co.kalpa.api.entity.Users;
import kr.co.kalpa.api.repository.UserSettingsRepository;
import kr.co.kalpa.api.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettingsService {

    private final UsersRepository usersRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void changePassword(String userId, PasswordChangeRequest request) {
        Users user = usersRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getUserPw())) {
            throw new BadCredentialsException("현재 비밀번호가 일치하지 않습니다.");
        }

        user.setUserPw(passwordEncoder.encode(request.getNewPassword()));
        usersRepository.save(user);
        log.info("Password changed for user: {}", userId);
    }

    @Transactional
    public void updateTheme(String userId, String theme) {
        UserSettings settings = userSettingsRepository.findByUserIdAndSettingKey(userId, "ui-theme")
                .orElse(UserSettings.builder()
                        .userId(userId)
                        .settingKey("ui-theme")
                        .build());

        settings.setSettingValue(theme);
        userSettingsRepository.save(settings);
    }

    @Transactional(readOnly = true)
    public SettingsResponse getSettings(String userId) {
        List<UserSettings> settingsList = userSettingsRepository.findByUserId(userId);

        Map<String, String> settingsMap = settingsList.stream()
                .collect(Collectors.toMap(UserSettings::getSettingKey, UserSettings::getSettingValue));

        // Default values if not present
        if (!settingsMap.containsKey("ui-theme")) {
            settingsMap.put("ui-theme", "light"); // Default theme
        }

        return SettingsResponse.builder()
                .userId(userId)
                .settings(settingsMap)
                .build();
    }
}
