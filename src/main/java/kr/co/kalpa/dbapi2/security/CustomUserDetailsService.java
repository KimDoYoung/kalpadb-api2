package kr.co.kalpa.dbapi2.security;

import kr.co.kalpa.dbapi2.entity.ApUser;
import kr.co.kalpa.dbapi2.repository.ApUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final ApUserRepository apUserRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        log.debug("Loading user by userId: {}", userId);

        ApUser apUser = apUserRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "사용자를 찾을 수 없습니다: " + userId));

        return new CustomUserDetails(apUser);
    }
}
