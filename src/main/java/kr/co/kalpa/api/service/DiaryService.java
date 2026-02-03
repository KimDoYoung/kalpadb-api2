package kr.co.kalpa.api.service;

import kr.co.kalpa.api.dto.request.DiaryCreateRequest;
import kr.co.kalpa.api.dto.request.DiaryUpdateRequest;
import kr.co.kalpa.api.dto.response.DiaryPageResponse;
import kr.co.kalpa.api.dto.response.DiaryResponse;
import kr.co.kalpa.api.dto.response.FileResponse;
import kr.co.kalpa.api.entity.Diary;
import kr.co.kalpa.api.entity.FileMatch;
import kr.co.kalpa.api.entity.FileType;
import kr.co.kalpa.api.entity.Files;
import kr.co.kalpa.api.exception.DiaryAlreadyExistsException;
import kr.co.kalpa.api.exception.DiaryNotFoundException;
import kr.co.kalpa.api.repository.DiaryRepository;
import kr.co.kalpa.api.util.Base64ImageExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final FileService fileService;
    private final FileMatchService fileMatchService;
    private final Base64ImageExtractor base64ImageExtractor;

    @Transactional
    public DiaryResponse createDiary(DiaryCreateRequest request) {
        if (diaryRepository.findByYmd(request.getYmd()) != null) {
            throw new DiaryAlreadyExistsException(request.getYmd());
        }

        // Extract Base64 images from content
        String processedContent = request.getContent();
        List<Base64ImageExtractor.Base64ImageInfo> base64Images = base64ImageExtractor.extractBase64Images(processedContent);

        // Create diary with empty content first to get ID
        Diary diary = Diary.builder()
                .ymd(request.getYmd())
                .content("")  // Empty content for now
                .summary(request.getSummary())
                .build();

        Diary savedDiary = diaryRepository.save(diary);

        // Process Base64 images and replace with URLs
        if (!base64Images.isEmpty()) {
            processedContent = processEditorImages(processedContent, savedDiary.getId(), base64Images);
        }

        // Update diary with processed content (after Base64 images converted to URLs)
        savedDiary.updateContent(processedContent);
        diaryRepository.save(savedDiary);

        // Files handling (attachments)
        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            for (MultipartFile file : request.getFiles()) {
                if(file.isEmpty()) continue;
                try {
                    FileResponse fileResponse = fileService.saveFile(file);
                    fileMatchService.createMatch("diary", savedDiary.getId(), fileResponse.getFileId(), FileType.ATTACHMENT);
                } catch (IOException e) {
                    log.error("Failed to save file", e);
                    throw new RuntimeException("Failed to save file", e);
                }
            }
        }

        return DiaryResponse.from(savedDiary);
    }

    @Transactional(readOnly = true)
    public DiaryResponse getDiary(String ymd) {
        Diary diary = diaryRepository.findByYmd(ymd);
        if (diary == null) {
            throw new DiaryNotFoundException(ymd);
        }
        return DiaryResponse.from(diary);
    }

    @Transactional(readOnly = true)
    public DiaryPageResponse getDiaries(String fromYmd, String toYmd, String keyword, Boolean summaryOnly, Pageable pageable) {
        Page<Diary> diaryPage = diaryRepository.findAll(pageable);
        
        List<DiaryResponse> content = diaryPage.getContent().stream()
                .map(d -> Boolean.TRUE.equals(summaryOnly) ? DiaryResponse.summaryOnly(d) : DiaryResponse.from(d))
                .collect(Collectors.toList());

        return DiaryPageResponse.from(
                new org.springframework.data.domain.PageImpl<>(content, pageable, diaryPage.getTotalElements())
        );
    }

    @Transactional(readOnly = true)
    public List<DiaryResponse> getRecentDiaries(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "ymd"));
        Page<Diary> diaryPage = diaryRepository.findAll(pageable);
        return diaryPage.getContent().stream()
                .map(DiaryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public DiaryResponse updateDiary(String ymd, DiaryUpdateRequest request) {
        Diary diary = diaryRepository.findByYmd(ymd);
        if (diary == null) {
            throw new DiaryNotFoundException(ymd);
        }

        // Process editor images and replace Base64 with URLs
        String processedContent = request.getContent();
        List<Base64ImageExtractor.Base64ImageInfo> base64Images = base64ImageExtractor.extractBase64Images(processedContent);

        // Save new editor images and update file_match
        if (!base64Images.isEmpty()) {
            processedContent = processEditorImages(processedContent, diary.getId(), base64Images);
        }

        // Clean up orphaned editor images
        cleanupOrphanedEditorImages(diary.getId(), processedContent);

        // Update diary with processed content
        diary.update(processedContent, request.getSummary());

        // Delete removed attachment files (physical files and database records)
        if (request.getDeletedFileIds() != null && !request.getDeletedFileIds().isEmpty()) {
            for (Long fileId : request.getDeletedFileIds()) {
                fileMatchService.deleteMatch("diary", diary.getId(), fileId);
                // Delete physical file and database record
                fileService.deleteFile(fileId);
                log.info("Attachment file deleted - fileId: {}", fileId);
            }
        }

        if (request.getNewFiles() != null && !request.getNewFiles().isEmpty()) {
             for (MultipartFile file : request.getNewFiles()) {
                 if(file.isEmpty()) continue;
                 try {
                     FileResponse fileResponse = fileService.saveFile(file);
                     fileMatchService.createMatch("diary", diary.getId(), fileResponse.getFileId(), FileType.ATTACHMENT);
                 } catch (IOException e) {
                     log.error("Failed to save file", e);
                     throw new RuntimeException("Failed to save file", e);
                 }
             }
        }

        return DiaryResponse.from(diary);
    }

    @Transactional
    public void deleteDiary(String ymd) {
        Diary diary = diaryRepository.findByYmd(ymd);
        if (diary == null) {
            throw new DiaryNotFoundException(ymd);
        }

        // Delete attachment files (physical files and database records)
        List<FileMatch> attachmentFiles = fileMatchService.getMatchesByTargetAndType("diary", diary.getId(), FileType.ATTACHMENT);
        for (FileMatch match : attachmentFiles) {
            try {
                // Delete file_match first before physical file
                fileMatchService.deleteMatch("diary", diary.getId(), match.getFileId());
                // Delete physical file and Files record
                fileService.deleteFile(match.getFileId());
                log.info("Attachment file deleted during diary deletion - fileId: {}", match.getFileId());
            } catch (Exception e) {
                log.error("Error deleting attachment file - fileId: {}", match.getFileId(), e);
                // Continue with deletion even if file deletion fails
            }
        }

        // Delete all remaining file matches (EDITOR_IMAGE, which will remain as orphaned files)
        fileMatchService.deleteMatchesByTarget("diary", diary.getId());

        // Delete diary
        diaryRepository.delete(diary);
    }

    /**
     * Process Base64 images in content: save to file and replace with URLs
     */
    private String processEditorImages(String content, Long diaryId, List<Base64ImageExtractor.Base64ImageInfo> base64Images) {
        if (base64Images.isEmpty()) {
            return content;
        }

        log.info("Processing {} editor images for diary {}", base64Images.size(), diaryId);

        String processedContent = content;

        for (Base64ImageExtractor.Base64ImageInfo imageInfo : base64Images) {
            try {
                // Save Base64 image to file
                FileResponse fileResponse = fileService.saveBase64Image(
                    imageInfo.getBase64Data(),
                    imageInfo.getFormat()
                );

                // Create file_match entry
                fileMatchService.createMatch(
                    "diary",
                    diaryId,
                    fileResponse.getFileId(),
                    FileType.EDITOR_IMAGE
                );

                // Get file entity and construct URL
                Files fileEntity = fileService.getFile(fileResponse.getFileId());
                String imageUrl = fileService.constructImageUrl(fileEntity);

                // Replace Base64 tag with URL tag
                String newImgTag = "<img src=\"" + imageUrl + "\">";
                processedContent = processedContent.replace(
                    imageInfo.getOriginalTag(),
                    newImgTag
                );

                log.info("Replaced Base64 image with URL: {}", imageUrl);

            } catch (Exception e) {
                log.error("Failed to process editor image for diary {}", diaryId, e);
                throw new RuntimeException("에디터 이미지 처리 실패", e);
            }
        }

        return processedContent;
    }

    /**
     * Clean up orphaned editor images that are no longer referenced in content
     */
    private void cleanupOrphanedEditorImages(Long diaryId, String newContent) {
        try {
            // Get all EDITOR_IMAGE file matches for this diary
            List<FileMatch> editorImages = fileMatchService.getMatchesByTargetAndType("diary", diaryId, FileType.EDITOR_IMAGE);

            if (editorImages.isEmpty()) {
                return;
            }

            // Extract all image URLs currently in content
            Set<String> usedUrls = extractImageUrls(newContent);

            // Delete file_match entries for images not in new content
            for (FileMatch match : editorImages) {
                Files file = fileService.getFile(match.getFileId());
                String fileUrl = fileService.constructImageUrl(file);

                if (!usedUrls.contains(fileUrl)) {
                    log.info("Cleaning up orphaned editor image: {}", fileUrl);
                    fileMatchService.deleteMatch("diary", diaryId, match.getFileId());
                }
            }
        } catch (Exception e) {
            log.error("Error cleaning up orphaned editor images for diary {}", diaryId, e);
            // Don't throw exception here, as cleanup should not block the update
        }
    }

    /**
     * Extract all image URLs from HTML content
     */
    private Set<String> extractImageUrls(String htmlContent) {
        Set<String> urls = new HashSet<>();

        if (htmlContent == null || htmlContent.isEmpty()) {
            return urls;
        }

        // Pattern to find all image URLs in <img src="...">
        Pattern pattern = Pattern.compile("<img[^>]*src=[\"']([^\"']+)[\"'][^>]*>");
        Matcher matcher = pattern.matcher(htmlContent);

        while (matcher.find()) {
            String url = matcher.group(1);
            if (!url.startsWith("data:")) {  // Skip Base64 data URIs
                urls.add(url);
            }
        }

        return urls;
    }
}
