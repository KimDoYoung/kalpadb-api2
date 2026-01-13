package kr.co.kalpa.api.controller;

import kr.co.kalpa.api.dto.ApiResponse;
import kr.co.kalpa.api.dto.response.FileResponse;
import kr.co.kalpa.api.entity.Files;
import kr.co.kalpa.api.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
@Slf4j
public class FileApiController {

    private final FileService fileService;

    /**
     * Download file by file ID
     * GET /api/file/download/{fileId}
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        log.info("Download file request - fileId: {}", fileId);

        try {
            // Get file info from database
            Files file = fileService.getFile(fileId);
            Path filePath = fileService.getFilePath(fileId);

            // Create resource
            Resource resource = new FileSystemResource(filePath.toFile());

            // Set response headers
            String fileName = URLEncoder.encode(file.getOrgFileName(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                    .header(HttpHeaders.CONTENT_TYPE, file.getMimeType() != null ? file.getMimeType() : MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.getFileSize()))
                    .body(resource);
        } catch (IOException e) {
            log.error("Error downloading file: {}", fileId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Unexpected error downloading file: {}", fileId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get file info by file ID
     * GET /api/file/{fileId}
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<ApiResponse<FileResponse>> getFileInfo(@PathVariable Long fileId) {
        log.info("Get file info request - fileId: {}", fileId);

        try {
            Files file = fileService.getFile(fileId);
            return ResponseEntity.ok(ApiResponse.success("파일 정보 조회 성공", FileResponse.from(file)));
        } catch (Exception e) {
            log.error("Error getting file info: {}", fileId, e);
            return ResponseEntity.notFound().build();
        }
    }
}
