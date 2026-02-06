package kr.co.kalpa.api.controller;

import kr.co.kalpa.api.dto.request.PasswordChangeRequest;
import kr.co.kalpa.api.dto.request.ThemeChangeRequest;
import kr.co.kalpa.api.dto.response.SettingsResponse;
import kr.co.kalpa.api.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsApiController {

    private final SettingsService settingsService;

    @PostMapping("/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PasswordChangeRequest request) {
        settingsService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/theme")
    public ResponseEntity<Void> updateTheme(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ThemeChangeRequest request) {
        settingsService.updateTheme(userDetails.getUsername(), request.getTheme());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<SettingsResponse> getSettings(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(settingsService.getSettings(userDetails.getUsername()));
    }
}
