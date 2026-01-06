package kr.co.kalpa.dbapi2.repository;

import kr.co.kalpa.dbapi2.entity.Diary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, String> {

    /**
     * Check if diary exists by ymd
     */
    boolean existsByYmd(String ymd);

    /**
     * Find diaries within date range with optional keyword search
     * Uses JPQL for database-agnostic queries
     */
    @Query("SELECT d FROM Diary d " +
           "WHERE (:fromYmd IS NULL OR d.ymd >= :fromYmd) " +
           "AND (:toYmd IS NULL OR d.ymd <= :toYmd) " +
           "AND (:keyword IS NULL OR d.content LIKE %:keyword% OR d.summary LIKE %:keyword%)")
    Page<Diary> findByFilters(
            @Param("fromYmd") String fromYmd,
            @Param("toYmd") String toYmd,
            @Param("keyword") String keyword,
            Pageable pageable);
}
