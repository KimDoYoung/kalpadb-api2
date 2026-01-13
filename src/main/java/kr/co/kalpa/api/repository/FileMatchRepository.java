package kr.co.kalpa.api.repository;

import kr.co.kalpa.api.entity.FileMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileMatchRepository extends JpaRepository<FileMatch, Long> {

    /**
     * Find all file matches by table name and target ID
     */
    List<FileMatch> findByTableNameAndTargetId(String tableName, Long targetId);

    /**
     * Delete all file matches by table name and target ID
     */
    void deleteByTableNameAndTargetId(String tableName, Long targetId);

    /**
     * Find by table name, target ID and file ID
     */
    FileMatch findByTableNameAndTargetIdAndFileId(String tableName, Long targetId, Long fileId);
}
