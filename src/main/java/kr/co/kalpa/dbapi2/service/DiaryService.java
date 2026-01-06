package kr.co.kalpa.dbapi2.service;

import kr.co.kalpa.dbapi2.dto.request.DiaryCreateRequest;
import kr.co.kalpa.dbapi2.dto.request.DiaryUpdateRequest;
import kr.co.kalpa.dbapi2.dto.response.DiaryPageResponse;
import kr.co.kalpa.dbapi2.dto.response.DiaryResponse;
import kr.co.kalpa.dbapi2.entity.Diary;
import kr.co.kalpa.dbapi2.exception.DiaryAlreadyExistsException;
import kr.co.kalpa.dbapi2.exception.DiaryNotFoundException;
import kr.co.kalpa.dbapi2.repository.DiaryRepository;
import kr.co.kalpa.dbapi2.util.YmdValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryService {

    private final DiaryRepository diaryRepository;

    /**
     * Create a new diary
     */
    @Transactional
    public DiaryResponse createDiary(DiaryCreateRequest request) {
        log.debug("Creating diary for ymd: {}", request.getYmd());

        // Validate YMD format
        YmdValidator.validate(request.getYmd());

        // Check if diary already exists
        if (diaryRepository.existsByYmd(request.getYmd())) {
            throw new DiaryAlreadyExistsException(request.getYmd());
        }

        // Create and save diary
        Diary diary = Diary.builder()
                .ymd(request.getYmd())
                .content(request.getContent())
                .summary(request.getSummary())
                .build();

        Diary savedDiary = diaryRepository.save(diary);
        log.info("Diary created successfully for ymd: {}", savedDiary.getYmd());

        return DiaryResponse.from(savedDiary);
    }

    /**
     * Get diary by YMD
     */
    @Transactional(readOnly = true)
    public DiaryResponse getDiary(String ymd) {
        log.debug("Getting diary for ymd: {}", ymd);

        // Validate YMD format
        YmdValidator.validate(ymd);

        Diary diary = diaryRepository.findById(ymd)
                .orElseThrow(() -> new DiaryNotFoundException(ymd));

        return DiaryResponse.from(diary);
    }

    /**
     * Get all diaries with pagination and filters
     */
    @Transactional(readOnly = true)
    public DiaryPageResponse getDiaries(
            String fromYmd,
            String toYmd,
            String keyword,
            Boolean summaryOnly,
            Pageable pageable) {

        log.debug("Getting diaries with filters - fromYmd: {}, toYmd: {}, keyword: {}, summaryOnly: {}",
                  fromYmd, toYmd, keyword, summaryOnly);

        // Validate date formats if provided
        if (fromYmd != null && !fromYmd.isBlank()) {
            YmdValidator.validate(fromYmd);
        }
        if (toYmd != null && !toYmd.isBlank()) {
            YmdValidator.validate(toYmd);
        }

        // Clean up empty strings to null for query
        String cleanFromYmd = (fromYmd != null && fromYmd.isBlank()) ? null : fromYmd;
        String cleanToYmd = (toYmd != null && toYmd.isBlank()) ? null : toYmd;
        String cleanKeyword = (keyword != null && keyword.isBlank()) ? null : keyword;

        // Query diaries
        Page<Diary> diaryPage = diaryRepository.findByFilters(
                cleanFromYmd,
                cleanToYmd,
                cleanKeyword,
                pageable);

        // Convert to response DTOs
        Page<DiaryResponse> responsePage;
        if (Boolean.TRUE.equals(summaryOnly)) {
            responsePage = diaryPage.map(DiaryResponse::summaryOnly);
        } else {
            responsePage = diaryPage.map(DiaryResponse::from);
        }

        return DiaryPageResponse.from(responsePage);
    }

    /**
     * Get recent N diaries
     */
    @Transactional(readOnly = true)
    public List<DiaryResponse> getRecentDiaries(int limit) {
        log.debug("Getting recent {} diaries", limit);

        // Use Pageable for limit
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "ymd"));
        Page<Diary> diaryPage = diaryRepository.findAll(pageable);

        return diaryPage.getContent().stream()
                .map(DiaryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Update diary
     */
    @Transactional
    public DiaryResponse updateDiary(String ymd, DiaryUpdateRequest request) {
        log.debug("Updating diary for ymd: {}", ymd);

        // Validate YMD format
        YmdValidator.validate(ymd);

        // Find existing diary
        Diary diary = diaryRepository.findById(ymd)
                .orElseThrow(() -> new DiaryNotFoundException(ymd));

        // Update fields
        diary.update(request.getContent(), request.getSummary());

        // Save is automatic due to @Transactional and JPA dirty checking
        log.info("Diary updated successfully for ymd: {}", ymd);

        return DiaryResponse.from(diary);
    }
}
