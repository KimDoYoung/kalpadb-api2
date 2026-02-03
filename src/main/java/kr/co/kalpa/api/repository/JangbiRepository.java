package kr.co.kalpa.api.repository;

import kr.co.kalpa.api.entity.Jangbi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JangbiRepository extends JpaRepository<Jangbi, Long> {

    /**
     * Find jangbi within date range with optional keyword search
     * Uses JPQL for database-agnostic queries
     */
    @Query("SELECT j FROM Jangbi j " +
           "WHERE (:fromYmd IS NULL OR j.ymd >= :fromYmd) " +
           "AND (:toYmd IS NULL OR j.ymd <= :toYmd) " +
           "AND (:keyword IS NULL OR j.item LIKE %:keyword% OR j.location LIKE %:keyword% OR j.spec LIKE %:keyword%)")
    Page<Jangbi> findByFilters(
            @Param("fromYmd") String fromYmd,
            @Param("toYmd") String toYmd,
            @Param("keyword") String keyword,
            Pageable pageable);
}
