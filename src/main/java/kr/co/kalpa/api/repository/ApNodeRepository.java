package kr.co.kalpa.api.repository;

import kr.co.kalpa.api.entity.ApNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApNodeRepository extends JpaRepository<ApNode, String> {

    /**
     * 루트 노드 조회 (parent_id IS NULL)
     */
    Optional<ApNode> findByParentIdIsNullAndIsDeletedFalse();

    /**
     * 특정 폴더의 하위 노드 목록 (삭제되지 않은 것만, 디렉토리 우선 정렬)
     */
    @Query("SELECT n FROM ApNode n WHERE n.parentId = :parentId AND n.isDeleted = false " +
           "ORDER BY n.nodeType ASC, n.name ASC")
    List<ApNode> findChildrenByParentId(@Param("parentId") String parentId);

    /**
     * 디렉토리만 조회 (트리 구성용)
     */
    @Query("SELECT n FROM ApNode n WHERE n.nodeType = 'D' AND n.isDeleted = false " +
           "ORDER BY n.depth ASC, n.name ASC")
    List<ApNode> findAllDirectories();

    /**
     * 특정 노드의 하위 디렉토리만 조회
     */
    @Query("SELECT n FROM ApNode n WHERE n.parentId = :parentId AND n.nodeType = 'D' " +
           "AND n.isDeleted = false ORDER BY n.name ASC")
    List<ApNode> findChildDirectories(@Param("parentId") String parentId);

    /**
     * 이름으로 검색 (삭제되지 않은 것만)
     */
    @Query("SELECT n FROM ApNode n LEFT JOIN FETCH n.apFile WHERE n.name LIKE %:keyword% " +
           "AND n.isDeleted = false ORDER BY n.nodeType ASC, n.name ASC")
    List<ApNode> searchByName(@Param("keyword") String keyword);

    /**
     * 같은 부모 내 동일 이름 존재 여부 확인
     */
    boolean existsByParentIdAndNameAndIsDeletedFalse(String parentId, String name);

    /**
     * 하위 노드 수 카운트
     */
    @Query("SELECT COUNT(n) FROM ApNode n WHERE n.parentId = :parentId AND n.isDeleted = false")
    int countChildrenByParentId(@Param("parentId") String parentId);

    /**
     * 재귀적 하위 노드 조회 (논리 삭제용) - native query
     */
    @Query(value = """
            WITH RECURSIVE descendants AS (
                SELECT id FROM ap_node WHERE id = :nodeId
                UNION ALL
                SELECT n.id FROM ap_node n
                INNER JOIN descendants d ON n.parent_id = d.id
            )
            SELECT n.* FROM ap_node n
            INNER JOIN descendants d ON n.id = d.id
            """, nativeQuery = true)
    List<ApNode> findAllDescendants(@Param("nodeId") String nodeId);

    /**
     * 특정 폴더의 하위 노드 (파일 정보 포함)
     */
    @Query("SELECT n FROM ApNode n LEFT JOIN FETCH n.apFile WHERE n.parentId = :parentId " +
           "AND n.isDeleted = false ORDER BY n.nodeType ASC, n.name ASC")
    List<ApNode> findChildrenWithFile(@Param("parentId") String parentId);
}
