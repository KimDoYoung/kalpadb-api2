package kr.co.kalpa.api.repository;

import kr.co.kalpa.api.entity.CalendarPublic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalendarPublicRepository extends JpaRepository<CalendarPublic, Integer> {
    List<CalendarPublic> findByYmdBetween(String startYmd, String endYmd);
}
