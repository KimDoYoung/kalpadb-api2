package kr.co.kalpa.dbapi2.repository;

import kr.co.kalpa.dbapi2.entity.ApUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApUserRepository extends JpaRepository<ApUser, String> {
    Optional<ApUser> findByUserId(String userId);

    boolean existsByUserId(String userId);
}
