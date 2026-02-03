package kr.co.kalpa.api.repository;

import kr.co.kalpa.api.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 게시판별 게시글 조회 (페이지네이션)
     */
    Page<Post> findByBoardId(Long boardId, Pageable pageable);

    /**
     * 게시판별 + 키워드 검색
     */
    @Query("SELECT p FROM Post p WHERE p.boardId = :boardId " +
           "AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%)")
    Page<Post> findByBoardIdAndKeyword(
            @Param("boardId") Long boardId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * 게시판별 + 기준일 범위 검색
     */
    @Query("SELECT p FROM Post p WHERE p.boardId = :boardId " +
           "AND p.baseYmd BETWEEN :startYmd AND :endYmd")
    Page<Post> findByBoardIdAndDateRange(
            @Param("boardId") Long boardId,
            @Param("startYmd") String startYmd,
            @Param("endYmd") String endYmd,
            Pageable pageable
    );

    /**
     * 게시판별 + 키워드 + 기준일 범위
     */
    @Query("SELECT p FROM Post p WHERE p.boardId = :boardId " +
           "AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%) " +
           "AND p.baseYmd BETWEEN :startYmd AND :endYmd")
    Page<Post> findByBoardIdAndKeywordAndDateRange(
            @Param("boardId") Long boardId,
            @Param("keyword") String keyword,
            @Param("startYmd") String startYmd,
            @Param("endYmd") String endYmd,
            Pageable pageable
    );

    /**
     * 전체 게시글 키워드 검색
     */
    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword%")
    Page<Post> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 전체 게시글 기준일 범위 검색
     */
    @Query("SELECT p FROM Post p WHERE p.baseYmd BETWEEN :startYmd AND :endYmd")
    Page<Post> findByDateRange(
            @Param("startYmd") String startYmd,
            @Param("endYmd") String endYmd,
            Pageable pageable
    );
}
