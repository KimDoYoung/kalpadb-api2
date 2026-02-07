package kr.co.kalpa.api.repository;

import kr.co.kalpa.api.entity.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalendarRepository extends JpaRepository<Calendar, Integer> {

    // Solar Fixed Events in Range
    @Query("SELECT c FROM Calendar c WHERE c.sorl = 'S' AND c.gubun IN ('H', 'E') AND c.ymd BETWEEN :startYmd AND :endYmd")
    List<Calendar> findSolarFixedEventsInRange(@Param("startYmd") String startYmd, @Param("endYmd") String endYmd);

    // All Repeating or Lunar Events (to be filtered in memory)
    @Query("SELECT c FROM Calendar c WHERE c.sorl = 'L' OR c.gubun IN ('Y', 'M')")
    List<Calendar> findRepeatingOrLunarEvents();
}
