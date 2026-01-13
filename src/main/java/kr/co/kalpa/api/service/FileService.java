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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    @Value("${file.upload.base-dir:./uploads}")
    private String uploadBaseDir;

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
        String savedFolder = uploadBaseDir + File.separator + dateFolder;

        // Create directories
        File dir = new File(savedFolder);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new IOException("디렉토리 생성 실패: " + savedFolder);
            }
        }

        // Save file to disk
        Path filePath = Paths.get(savedFolder, physicalFileName);
        file.transferTo(filePath.toFile());
        log.info("File saved to: {}", filePath);

        // Create Files entity
        Files fileEntity = Files.builder()
                .savedFolder(savedFolder)
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
        Path filePath = Paths.get(file.getSavedFolder(), file.getPhysicalFileName());

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
            Path filePath = Paths.get(file.getSavedFolder(), file.getPhysicalFileName());
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
     * Generate unique physical file name
     */
    private String generatePhysicalFileName() {
        return UUID.randomUUID().toString();
    }
}
