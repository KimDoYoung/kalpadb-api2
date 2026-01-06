package kr.co.kalpa.dbapi2.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import kr.co.kalpa.dbapi2.dto.ApiResponse;
import kr.co.kalpa.dbapi2.dto.request.DiaryCreateRequest;
import kr.co.kalpa.dbapi2.dto.request.DiaryUpdateRequest;
import kr.co.kalpa.dbapi2.dto.response.DiaryPageResponse;
import kr.co.kalpa.dbapi2.dto.response.DiaryResponse;
import kr.co.kalpa.dbapi2.service.DiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dairy")
@RequiredArgsConstructor
@Slf4j
public class DiaryController {

    private final DiaryService diaryService;

    /**
     * Create a new diary
     * POST /api/dairy
     */
    @PostMapping
    public ResponseEntity<ApiResponse<DiaryResponse>> createDiary(
            @Valid @RequestBody DiaryCreateRequest request) {

        log.info("Create diary request for ymd: {}", request.getYmd());

        DiaryResponse response = diaryService.createDiary(request);

        return ResponseEntity.ok(
                ApiResponse.success("일기가 생성되었습니다", response));
    }

    /**
     * Get diary by YMD
     * GET /api/dairy/{ymd}
     */
    @GetMapping("/{ymd}")
    public ResponseEntity<ApiResponse<DiaryResponse>> getDiary(
            @PathVariable String ymd) {

        log.info("Get diary request for ymd: {}", ymd);

        DiaryResponse response = diaryService.getDiary(ymd);

        return ResponseEntity.ok(
                ApiResponse.success("일기 조회 성공", response));
    }

    /**
     * Get all diaries with pagination and filters
     * GET /api/dairy?page=0&size=10&sort=ymd,desc&fromYmd=20260101&toYmd=20260131&keyword=검색어&summaryOnly=true
     */
    @GetMapping
    public ResponseEntity<ApiResponse<DiaryPageResponse>> getDiaries(
            @RequestParam(required = false) String fromYmd,
            @RequestParam(required = false) String toYmd,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "false") Boolean summaryOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ymd,desc") String[] sort) {

        log.info("Get diaries request - page: {}, size: {}, fromYmd: {}, toYmd: {}, keyword: {}, summaryOnly: {}",
                 page, size, fromYmd, toYmd, keyword, summaryOnly);

        // Parse sort parameter
        Sort.Direction direction = sort.length > 1 && sort[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = sort.length > 0 ? sort[0] : "ymd";

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        DiaryPageResponse response = diaryService.getDiaries(
                fromYmd, toYmd, keyword, summaryOnly, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("일기 목록 조회 성공", response));
    }

    /**
     * Get recent N diaries
     * GET /api/dairy/recent?limit=10
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<DiaryResponse>>> getRecentDiaries(
            @RequestParam(defaultValue = "10") @Min(1) int limit) {

        log.info("Get recent {} diaries request", limit);

        List<DiaryResponse> response = diaryService.getRecentDiaries(limit);

        return ResponseEntity.ok(
                ApiResponse.success("최근 일기 조회 성공", response));
    }

    /**
     * Update diary
     * PUT /api/dairy/{ymd}
     */
    @PutMapping("/{ymd}")
    public ResponseEntity<ApiResponse<DiaryResponse>> updateDiary(
            @PathVariable String ymd,
            @Valid @RequestBody DiaryUpdateRequest request) {

        log.info("Update diary request for ymd: {}", ymd);

        DiaryResponse response = diaryService.updateDiary(ymd, request);

        return ResponseEntity.ok(
                ApiResponse.success("일기가 수정되었습니다", response));
    }
}
