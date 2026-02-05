package kr.co.kalpa.api.repository;

import kr.co.kalpa.api.entity.ApFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApFileRepository extends JpaRepository<ApFile, String> {
}
