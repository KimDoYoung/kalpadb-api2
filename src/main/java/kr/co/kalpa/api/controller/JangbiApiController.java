package kr.co.kalpa.api.controller;

import jakarta.validation.constraints.Min;
import kr.co.kalpa.api.dto.ApiResponse;
import kr.co.kalpa.api.dto.request.JangbiCreateRequest;
import kr.co.kalpa.api.dto.request.JangbiUpdateRequest;
import kr.co.kalpa.api.dto.response.JangbiResponse;
import kr.co.kalpa.api.dto.response.FileResponse;
import kr.co.kalpa.api.entity.FileMatch;
import kr.co.kalpa.api.entity.FileType;
import kr.co.kalpa.api.service.JangbiService;
import kr.co.kalpa.api.service.FileMatchService;
import kr.co.kalpa.api.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/jangbi")
@RequiredArgsConstructor
@Slf4j
public class JangbiApiController {

    private final JangbiService jangbiService;
    private final FileMatchService fileMatchService;
    private final FileService fileService;

    /**
     * Create a new jangbi (FormData 지원)
     * POST /api/jangbi
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<JangbiResponse>> createJangbi(
            @RequestParam String ymd,
            @RequestParam String item,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer cost,
            @RequestParam(required = false) String spec,
            @RequestParam(required = false, defaultValue = "2") String lvl,
            @RequestParam(required = false) List<MultipartFile> files) {

        log.info("Create jangbi request for item: {}", item);

        JangbiCreateRequest request = new JangbiCreateRequest(ymd, item, location, cost, spec, lvl, files);

        JangbiResponse response = jangbiService.createJangbi(request);

        return ResponseEntity.ok(
                ApiResponse.success("장비가 생성되었습니다", response));
    }

    /**
     * Get jangbi by ID
     * GET /api/jangbi/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JangbiResponse>> getJangbi(
            @PathVariable Long id) {

        log.info("Get jangbi request for id: {}", id);

        JangbiResponse response = jangbiService.getJangbi(id);

        return ResponseEntity.ok(
                ApiResponse.success("장비 조회 성공", response));
    }

    /**
     * Get all jangbis with pagination and filters
     * GET /api/jangbi?page=0&size=10&sort=modifyDt,desc&fromYmd=20260101&toYmd=20260131&keyword=검색어&summaryOnly=true
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<JangbiResponse>>> getJangbis(
            @RequestParam(required = false) String fromYmd,
            @RequestParam(required = false) String toYmd,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "false") Boolean summaryOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "modifyDt,desc") String[] sort) {

        log.info("Get jangbis request - page: {}, size: {}, fromYmd: {}, toYmd: {}, keyword: {}, summaryOnly: {}",
                 page, size, fromYmd, toYmd, keyword, summaryOnly);

        // Parse sort parameter
        Sort.Direction direction = sort.length > 1 && sort[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = sort.length > 0 ? sort[0] : "modifyDt";

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<JangbiResponse> response = jangbiService.getJangbis(
                fromYmd, toYmd, keyword, summaryOnly, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("장비 목록 조회 성공", response));
    }

    /**
     * Get recent N jangbis
     * GET /api/jangbi/recent?limit=10
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<JangbiResponse>>> getRecentJangbis(
            @RequestParam(defaultValue = "10") @Min(1) int limit) {

        log.info("Get recent {} jangbis request", limit);

        List<JangbiResponse> response = jangbiService.getRecentJangbis(limit);

        return ResponseEntity.ok(
                ApiResponse.success("최근 장비 조회 성공", response));
    }

    /**
     * Update jangbi (FormData 지원)
     * PUT /api/jangbi/{id}
     */
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<JangbiResponse>> updateJangbi(
            @PathVariable Long id,
            @RequestParam(required = false) String item,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer cost,
            @RequestParam(required = false) String spec,
            @RequestParam(required = false) String lvl,
            @RequestParam(required = false) List<MultipartFile> newFiles,
            @RequestParam(required = false) String deletedFileIds) {

        log.info("Update jangbi request for id: {}", id);

        // deletedFileIds JSON 파싱
        List<Long> deletedIds = new ArrayList<>();
        if (deletedFileIds != null && !deletedFileIds.isBlank()) {
            try {
                // JSON 배열 파싱 (간단 구현)
                String trimmed = deletedFileIds.replaceAll("[\\[\\]\\s]", "");
                if (!trimmed.isEmpty()) {
                    String[] ids = trimmed.split(",");
                    for (String fileId : ids) {
                        deletedIds.add(Long.parseLong(fileId));
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse deletedFileIds: {}", deletedFileIds);
            }
        }

        JangbiUpdateRequest request = new JangbiUpdateRequest(item, location, cost, spec, lvl, deletedIds, newFiles);

        JangbiResponse response = jangbiService.updateJangbi(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("장비가 수정되었습니다", response));
    }

    /**
     * Delete jangbi (파일은 보존)
     * DELETE /api/jangbi/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteJangbi(
            @PathVariable Long id) {

        log.info("Delete jangbi request for id: {}", id);

        jangbiService.deleteJangbi(id);

        return ResponseEntity.ok(
                ApiResponse.success("장비가 삭제되었습니다", null));
    }

    /**
     * Get files attached to a jangbi
     * GET /api/jangbi/{id}/files
     */
    @GetMapping("/{id}/files")
    public ResponseEntity<ApiResponse<List<FileResponse>>> getJangbiFiles(
            @PathVariable Long id) {

        log.info("Get jangbi files request for id: {}", id);

        // Get file matches for this jangbi (only ATTACHMENT type, exclude EDITOR_IMAGE)
        List<FileMatch> matches = fileMatchService.getMatchesByTargetAndType("jangbi", id, FileType.ATTACHMENT);

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
