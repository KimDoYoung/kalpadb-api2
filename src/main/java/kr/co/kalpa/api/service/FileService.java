package kr.co.kalpa.api.service;

import kr.co.kalpa.api.dto.response.FileResponse;
import kr.co.kalpa.api.entity.Files;
import kr.co.kalpa.api.repository.FilesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    @Value("${file.upload.attach-files-dir}")
    private String attachFilesDir;

    @Value("${file.upload.editor-images-dir}")
    private String editorImagesDir;

    @Value("${file.image.base-url}")
    private String imageBaseUrl;

    private final FilesRepository filesRepository;

    /**
     * Save file to disk and database
     */
    @Transactional
    public FileResponse saveFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }

        // Generate physical file name with UUID
        String physicalFileName = generatePhysicalFileName();

        // Create folder structure with date
        LocalDateTime now = LocalDateTime.now();
        String dateFolder = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        
        // Resolve absolute path to avoid issues with relative paths in different environments (e.g., Tomcat temp dir)
        Path baseDirPath = Paths.get(attachFilesDir).toAbsolutePath();
        Path folderPath = baseDirPath.resolve(dateFolder);
        String absoluteFolderPath = folderPath.toString();

        // Create directories
        File dir = folderPath.toFile();
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new IOException("디렉토리 생성 실패: " + absoluteFolderPath);
            }
        }

        // Save file to disk
        Path filePath = folderPath.resolve(physicalFileName);
        file.transferTo(filePath.toFile());
        log.info("File saved to: {}", filePath);

        // Create Files entity
        Files fileEntity = Files.builder()
                .savedFolder(dateFolder)
                .orgFileName(file.getOriginalFilename())
                .physicalFileName(physicalFileName)
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .createdAt(now)
                .build();

        Files savedFile = filesRepository.save(fileEntity);
        log.info("File entity saved - fileId: {}, name: {}", savedFile.getFileId(), savedFile.getOrgFileName());

        return FileResponse.from(savedFile);
    }

    /**
     * Get file for download
     */
    @Transactional(readOnly = true)
    public Files getFile(Long fileId) {
        return filesRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다: " + fileId));
    }

    /**
     * Get file path for download
     */
    public Path getFilePath(Long fileId) throws IOException {
        Files file = getFile(fileId);
        Path baseDirPath = Paths.get(attachFilesDir).toAbsolutePath();
        Path filePath = baseDirPath.resolve(file.getSavedFolder()).resolve(file.getPhysicalFileName());

        if (!java.nio.file.Files.exists(filePath)) {
            throw new IOException("파일이 존재하지 않습니다: " + filePath);
        }

        return filePath;
    }

    /**
     * Delete file from disk and database
     */
    @Transactional
    public void deleteFile(Long fileId) {
        Files file = getFile(fileId);

        // Delete from disk
        try {
            Path baseDirPath = Paths.get(attachFilesDir).toAbsolutePath();
            Path filePath = baseDirPath.resolve(file.getSavedFolder()).resolve(file.getPhysicalFileName());
            if (java.nio.file.Files.exists(filePath)) {
                java.nio.file.Files.delete(filePath);
                log.info("File deleted from disk: {}", filePath);
            }
        } catch (IOException e) {
            log.error("Error deleting file from disk: {}", fileId, e);
            // Continue with DB deletion even if disk deletion fails
        }

        // Delete from database
        filesRepository.deleteById(fileId);
        log.info("File deleted from database: {}", fileId);
    }

    /**
     * Save Base64 image to disk and database
     */
    @Transactional
    public FileResponse saveBase64Image(String base64Data, String imageFormat) throws IOException {
        // Validate format
        if (!isValidImageFormat(imageFormat)) {
            throw new IllegalArgumentException("지원하지 않는 이미지 형식: " + imageFormat);
        }

        // Decode Base64
        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(base64Data);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 Base64 데이터", e);
        }

        // Validate size (5MB limit)
        if (imageBytes.length > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("이미지 크기가 너무 큽니다 (최대 5MB)");
        }

        // Generate filename with extension
        String extension = normalizeImageFormat(imageFormat);
        String physicalFileName = generatePhysicalFileName() + "." + extension;

        // Create date-based folder structure
        LocalDateTime now = LocalDateTime.now();
        String dateFolder = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        Path baseDirPath = Paths.get(editorImagesDir).toAbsolutePath();
        Path folderPath = baseDirPath.resolve(dateFolder);

        // Create directories
        File dir = folderPath.toFile();
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new IOException("디렉토리 생성 실패: " + folderPath);
            }
        }

        // Save file
        Path filePath = folderPath.resolve(physicalFileName);
        java.nio.file.Files.write(filePath, imageBytes);
        log.info("Editor image saved: {}", filePath);

        // Create Files entity
        Files fileEntity = Files.builder()
                .savedFolder(dateFolder)
                .orgFileName(physicalFileName)
                .physicalFileName(physicalFileName)
                .fileSize((long) imageBytes.length)
                .mimeType("image/" + extension)
                .createdAt(now)
                .build();

        Files savedFile = filesRepository.save(fileEntity);
        log.info("Editor image entity saved - fileId: {}", savedFile.getFileId());

        return FileResponse.from(savedFile);
    }

    /**
     * Construct image URL from file entity
     */
    public String constructImageUrl(Files file) {
        return imageBaseUrl + file.getSavedFolder() + "/" + file.getPhysicalFileName();
    }

    /**
     * Validate image format
     */
    private boolean isValidImageFormat(String format) {
        return Set.of("png", "jpg", "jpeg", "gif", "webp").contains(format.toLowerCase());
    }

    /**
     * Normalize image format (e.g., jpeg -> jpg)
     */
    private String normalizeImageFormat(String format) {
        return format.equalsIgnoreCase("jpeg") ? "jpg" : format.toLowerCase();
    }

    /**
     * Generate unique physical file name
     */
    private String generatePhysicalFileName() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
