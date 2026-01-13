package kr.co.kalpa.api.service;

import kr.co.kalpa.api.entity.FileMatch;
import kr.co.kalpa.api.repository.FileMatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileMatchService {

    private final FileMatchRepository fileMatchRepository;

    /**
     * Create file match
     */
    @Transactional
    public FileMatch createMatch(String tableName, Long targetId, Long fileId) {
        FileMatch fileMatch = FileMatch.builder()
                .tableName(tableName)
                .targetId(targetId)
                .fileId(fileId)
                .build();

        FileMatch saved = fileMatchRepository.save(fileMatch);
        log.info("File match created - table: {}, targetId: {}, fileId: {}", tableName, targetId, fileId);

        return saved;
    }

    /**
     * Get all file IDs for a target
     */
    @Transactional(readOnly = true)
    public List<Long> getFileIdsByTarget(String tableName, Long targetId) {
        List<FileMatch> matches = fileMatchRepository.findByTableNameAndTargetId(tableName, targetId);
        return matches.stream().map(FileMatch::getFileId).toList();
    }

    /**
     * Get all matches for a target
     */
    @Transactional(readOnly = true)
    public List<FileMatch> getMatchesByTarget(String tableName, Long targetId) {
        return fileMatchRepository.findByTableNameAndTargetId(tableName, targetId);
    }

    /**
     * Delete file match
     */
    @Transactional
    public void deleteMatch(String tableName, Long targetId, Long fileId) {
        FileMatch match = fileMatchRepository.findByTableNameAndTargetIdAndFileId(tableName, targetId, fileId);
        if (match != null) {
            fileMatchRepository.delete(match);
            log.info("File match deleted - table: {}, targetId: {}, fileId: {}", tableName, targetId, fileId);
        }
    }

    /**
     * Delete all matches for a target
     */
    @Transactional
    public void deleteMatchesByTarget(String tableName, Long targetId) {
        fileMatchRepository.deleteByTableNameAndTargetId(tableName, targetId);
        log.info("File matches deleted for target - table: {}, targetId: {}", tableName, targetId);
    }
}
