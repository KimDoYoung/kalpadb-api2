package kr.co.kalpa.api.controller;

import jakarta.validation.constraints.Min;
import kr.co.kalpa.api.dto.ApiResponse;
import kr.co.kalpa.api.dto.request.DiaryCreateRequest;
import kr.co.kalpa.api.dto.request.DiaryUpdateRequest;
import kr.co.kalpa.api.dto.response.DiaryPageResponse;
import kr.co.kalpa.api.dto.response.DiaryResponse;
import kr.co.kalpa.api.dto.response.FileResponse;
import kr.co.kalpa.api.entity.FileMatch;
import kr.co.kalpa.api.entity.FileType;
import kr.co.kalpa.api.service.DiaryService;
import kr.co.kalpa.api.service.FileMatchService;
import kr.co.kalpa.api.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
@Slf4j
public class DiaryApiController {

    private final DiaryService diaryService;
    private final FileMatchService fileMatchService;
    private final FileService fileService;

    /**
     * Create a new diary (FormData 지원)
     * POST /api/diary
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<DiaryResponse>> createDiary(
            @RequestParam String ymd,
            @RequestParam(required = false) String summary,
            @RequestParam String content,
            @RequestParam(required = false) List<MultipartFile> files) {

        log.info("Create diary request for ymd: {}", ymd);

        DiaryCreateRequest request = new DiaryCreateRequest();
        request = new DiaryCreateRequest(ymd, content, summary, files);

        DiaryResponse response = diaryService.createDiary(request);

        return ResponseEntity.ok(
                ApiResponse.success("일기가 생성되었습니다", response));
    }

    /**
     * Get diary by YMD
     * GET /api/diary/{ymd}
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
     * GET /api/diary?page=0&size=10&sort=ymd,desc&fromYmd=20260101&toYmd=20260131&keyword=검색어&summaryOnly=true
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
     * GET /api/diary/recent?limit=10
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
     * Update diary (FormData 지원)
     * PUT /api/diary/{ymd}
     */
    @PutMapping(value = "/{ymd}", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<DiaryResponse>> updateDiary(
            @PathVariable String ymd,
            @RequestParam String content,
            @RequestParam(required = false) String summary,
            @RequestParam(required = false) List<MultipartFile> newFiles,
            @RequestParam(required = false) String deletedFileIds) {

        log.info("Update diary request for ymd: {}", ymd);

        // deletedFileIds JSON 파싱
        List<Long> deletedIds = new ArrayList<>();
        if (deletedFileIds != null && !deletedFileIds.isBlank()) {
            try {
                // JSON 배열 파싱 (간단 구현)
                String trimmed = deletedFileIds.replaceAll("[\\[\\]\\s]", "");
                if (!trimmed.isEmpty()) {
                    String[] ids = trimmed.split(",");
                    for (String id : ids) {
                        deletedIds.add(Long.parseLong(id));
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse deletedFileIds: {}", deletedFileIds);
            }
        }

        DiaryUpdateRequest request = new DiaryUpdateRequest(content, summary, deletedIds, newFiles);

        DiaryResponse response = diaryService.updateDiary(ymd, request);

        return ResponseEntity.ok(
                ApiResponse.success("일기가 수정되었습니다", response));
    }

    /**
     * Delete diary (파일은 보존)
     * DELETE /api/diary/{ymd}
     */
    @DeleteMapping("/{ymd}")
    public ResponseEntity<ApiResponse<Void>> deleteDiary(
            @PathVariable String ymd) {

        log.info("Delete diary request for ymd: {}", ymd);

        diaryService.deleteDiary(ymd);

        return ResponseEntity.ok(
                ApiResponse.success("일기가 삭제되었습니다", null));
    }

    /**
     * Get files attached to a diary
     * GET /api/diary/{ymd}/files
     */
    @GetMapping("/{ymd}/files")
    public ResponseEntity<ApiResponse<List<FileResponse>>> getDiaryFiles(
            @PathVariable String ymd) {

        log.info("Get diary files request for ymd: {}", ymd);

        // Get diary by ymd to get its ID
        DiaryResponse diary = diaryService.getDiary(ymd);

        // Get file matches for this diary (only ATTACHMENT type, exclude EDITOR_IMAGE)
        List<FileMatch> matches = fileMatchService.getMatchesByTargetAndType("diary", diary.getId(), FileType.ATTACHMENT);

        // Convert file IDs to FileResponse objects
        List<FileResponse> files = new ArrayList<>();
        for (FileMatch match : matches) {
            try {
                var file = fileService.getFile(match.getFileId());
                files.add(FileResponse.from(file));
            } catch (Exception e) {
                log.error("Error loading file: {}", match.getFileId(), e);
            }
        }

        return ResponseEntity.ok(
                ApiResponse.success("파일 목록 조회 성공", files));
    }
}
