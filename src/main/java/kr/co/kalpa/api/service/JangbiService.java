package kr.co.kalpa.api.service;

import kr.co.kalpa.api.dto.request.JangbiCreateRequest;
import kr.co.kalpa.api.dto.request.JangbiUpdateRequest;
import kr.co.kalpa.api.dto.response.JangbiResponse;
import kr.co.kalpa.api.dto.response.FileResponse;
import kr.co.kalpa.api.entity.Jangbi;
import kr.co.kalpa.api.entity.FileMatch;
import kr.co.kalpa.api.entity.FileType;
import kr.co.kalpa.api.entity.Files;
import kr.co.kalpa.api.exception.JangbiNotFoundException;
import kr.co.kalpa.api.repository.JangbiRepository;
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
public class JangbiService {

    private final JangbiRepository jangbiRepository;
    private final FileService fileService;
    private final FileMatchService fileMatchService;
    private final Base64ImageExtractor base64ImageExtractor;

    @Transactional
    public JangbiResponse createJangbi(JangbiCreateRequest request) {
        // Extract Base64 images from spec
        String processedSpec = request.getSpec();
        List<Base64ImageExtractor.Base64ImageInfo> base64Images = base64ImageExtractor.extractBase64Images(processedSpec != null ? processedSpec : "");

        // Create jangbi with empty spec first to get ID
        Jangbi jangbi = Jangbi.builder()
                .ymd(request.getYmd())
                .item(request.getItem())
                .location(request.getLocation())
                .cost(request.getCost())
                .spec("")  // Empty spec for now
                .lvl(request.getLvl())
                .build();

        Jangbi savedJangbi = jangbiRepository.save(jangbi);

        // Process Base64 images and replace with URLs
        if (!base64Images.isEmpty() && processedSpec != null) {
            processedSpec = processEditorImages(processedSpec, savedJangbi.getId(), base64Images);
        }

        // Update jangbi with processed spec (after Base64 images converted to URLs)
        savedJangbi.updateSpec(processedSpec != null ? processedSpec : "");
        jangbiRepository.save(savedJangbi);

        // Files handling (attachments)
        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            for (MultipartFile file : request.getFiles()) {
                if(file.isEmpty()) continue;
                try {
                    FileResponse fileResponse = fileService.saveFile(file);
                    fileMatchService.createMatch("jangbi", savedJangbi.getId(), fileResponse.getFileId(), FileType.ATTACHMENT);
                } catch (IOException e) {
                    log.error("Failed to save file", e);
                    throw new RuntimeException("Failed to save file", e);
                }
            }
        }

        return JangbiResponse.from(savedJangbi);
    }

    @Transactional(readOnly = true)
    public JangbiResponse getJangbi(Long id) {
        Jangbi jangbi = jangbiRepository.findById(id)
                .orElseThrow(() -> new JangbiNotFoundException(id));
        return JangbiResponse.from(jangbi);
    }

    @Transactional(readOnly = true)
    public Page<JangbiResponse> getJangbis(String fromYmd, String toYmd, String keyword, Boolean summaryOnly, Pageable pageable) {
        Page<Jangbi> jangbiPage;

        if (fromYmd != null || toYmd != null || keyword != null) {
            jangbiPage = jangbiRepository.findByFilters(fromYmd, toYmd, keyword, pageable);
        } else {
            jangbiPage = jangbiRepository.findAll(pageable);
        }

        return jangbiPage.map(j -> Boolean.TRUE.equals(summaryOnly) ? JangbiResponse.summaryOnly(j) : JangbiResponse.from(j));
    }

    @Transactional(readOnly = true)
    public List<JangbiResponse> getRecentJangbis(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "modifyDt"));
        Page<Jangbi> jangbiPage = jangbiRepository.findAll(pageable);
        return jangbiPage.getContent().stream()
                .map(JangbiResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public JangbiResponse updateJangbi(Long id, JangbiUpdateRequest request) {
        Jangbi jangbi = jangbiRepository.findById(id)
                .orElseThrow(() -> new JangbiNotFoundException(id));

        // Process editor images and replace Base64 with URLs
        String processedSpec = request.getSpec();
        List<Base64ImageExtractor.Base64ImageInfo> base64Images =
            base64ImageExtractor.extractBase64Images(processedSpec != null ? processedSpec : "");

        // Save new editor images and update file_match
        if (!base64Images.isEmpty() && processedSpec != null) {
            processedSpec = processEditorImages(processedSpec, jangbi.getId(), base64Images);
        }

        // Clean up orphaned editor images
        if (processedSpec != null) {
            cleanupOrphanedEditorImages(jangbi.getId(), processedSpec);
        }

        // Update jangbi with processed spec
        jangbi.update(
            request.getItem() != null ? request.getItem() : jangbi.getItem(),
            request.getLocation() != null ? request.getLocation() : jangbi.getLocation(),
            request.getCost() != null ? request.getCost() : jangbi.getCost(),
            processedSpec != null ? processedSpec : jangbi.getSpec(),
            request.getLvl() != null ? request.getLvl() : jangbi.getLvl()
        );

        // Delete removed attachment files (physical files and database records)
        if (request.getDeletedFileIds() != null && !request.getDeletedFileIds().isEmpty()) {
            for (Long fileId : request.getDeletedFileIds()) {
                fileMatchService.deleteMatch("jangbi", jangbi.getId(), fileId);
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
                     fileMatchService.createMatch("jangbi", jangbi.getId(), fileResponse.getFileId(), FileType.ATTACHMENT);
                 } catch (IOException e) {
                     log.error("Failed to save file", e);
                     throw new RuntimeException("Failed to save file", e);
                 }
             }
        }

        return JangbiResponse.from(jangbi);
    }

    @Transactional
    public void deleteJangbi(Long id) {
        Jangbi jangbi = jangbiRepository.findById(id)
                .orElseThrow(() -> new JangbiNotFoundException(id));

        // Delete attachment files (physical files and database records)
        List<FileMatch> attachmentFiles = fileMatchService.getMatchesByTargetAndType("jangbi", jangbi.getId(), FileType.ATTACHMENT);
        for (FileMatch match : attachmentFiles) {
            try {
                // Delete file_match first before physical file
                fileMatchService.deleteMatch("jangbi", jangbi.getId(), match.getFileId());
                // Delete physical file and Files record
                fileService.deleteFile(match.getFileId());
                log.info("Attachment file deleted during jangbi deletion - fileId: {}", match.getFileId());
            } catch (Exception e) {
                log.error("Error deleting attachment file - fileId: {}", match.getFileId(), e);
                // Continue with deletion even if file deletion fails
            }
        }

        // Delete all remaining file matches (EDITOR_IMAGE, which will remain as orphaned files)
        fileMatchService.deleteMatchesByTarget("jangbi", jangbi.getId());

        // Delete jangbi
        jangbiRepository.delete(jangbi);
    }

    /**
     * Process Base64 images in spec: save to file and replace with URLs
     */
    private String processEditorImages(String spec, Long jangbiId, List<Base64ImageExtractor.Base64ImageInfo> base64Images) {
        if (base64Images.isEmpty()) {
            return spec;
        }

        log.info("Processing {} editor images for jangbi {}", base64Images.size(), jangbiId);

        String processedSpec = spec;

        for (Base64ImageExtractor.Base64ImageInfo imageInfo : base64Images) {
            try {
                // Save Base64 image to file
                FileResponse fileResponse = fileService.saveBase64Image(
                    imageInfo.getBase64Data(),
                    imageInfo.getFormat()
                );

                // Create file_match entry
                fileMatchService.createMatch(
                    "jangbi",
                    jangbiId,
                    fileResponse.getFileId(),
                    FileType.EDITOR_IMAGE
                );

                // Get file entity and construct URL
                Files fileEntity = fileService.getFile(fileResponse.getFileId());
                String imageUrl = fileService.constructImageUrl(fileEntity);

                // Replace Base64 tag with URL tag
                String newImgTag = "<img src=\"" + imageUrl + "\">";
                processedSpec = processedSpec.replace(
                    imageInfo.getOriginalTag(),
                    newImgTag
                );

                log.info("Replaced Base64 image with URL: {}", imageUrl);

            } catch (Exception e) {
                log.error("Failed to process editor image for jangbi {}", jangbiId, e);
                throw new RuntimeException("에디터 이미지 처리 실패", e);
            }
        }

        return processedSpec;
    }

    /**
     * Clean up orphaned editor images that are no longer referenced in spec
     */
    private void cleanupOrphanedEditorImages(Long jangbiId, String newSpec) {
        try {
            // Get all EDITOR_IMAGE file matches for this jangbi
            List<FileMatch> editorImages = fileMatchService.getMatchesByTargetAndType("jangbi", jangbiId, FileType.EDITOR_IMAGE);

            if (editorImages.isEmpty()) {
                return;
            }

            // Extract all image URLs currently in spec
            Set<String> usedUrls = extractImageUrls(newSpec);

            // Delete file_match entries for images not in new spec
            for (FileMatch match : editorImages) {
                Files file = fileService.getFile(match.getFileId());
                String fileUrl = fileService.constructImageUrl(file);

                if (!usedUrls.contains(fileUrl)) {
                    log.info("Cleaning up orphaned editor image: {}", fileUrl);
                    fileMatchService.deleteMatch("jangbi", jangbiId, match.getFileId());
                }
            }
        } catch (Exception e) {
            log.error("Error cleaning up orphaned editor images for jangbi {}", jangbiId, e);
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
