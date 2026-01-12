package kr.co.kalpa.api.repository;

import kr.co.kalpa.api.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Integer> {
    Optional<Users> findByUserId(String userId);

    boolean existsByUserId(String userId);
}
