package kr.co.kalpa.api.service;

import kr.co.kalpa.api.dto.request.DiaryCreateRequest;
import kr.co.kalpa.api.dto.request.DiaryUpdateRequest;
import kr.co.kalpa.api.dto.response.DiaryPageResponse;
import kr.co.kalpa.api.dto.response.DiaryResponse;
import kr.co.kalpa.api.dto.response.FileResponse;
import kr.co.kalpa.api.entity.Diary;
import kr.co.kalpa.api.entity.FileType;
import kr.co.kalpa.api.exception.DiaryAlreadyExistsException;
import kr.co.kalpa.api.exception.DiaryNotFoundException;
import kr.co.kalpa.api.repository.DiaryRepository;
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
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final FileService fileService;
    private final FileMatchService fileMatchService;

    @Transactional
    public DiaryResponse createDiary(DiaryCreateRequest request) {
        if (diaryRepository.findByYmd(request.getYmd()) != null) {
            throw new DiaryAlreadyExistsException(request.getYmd());
        }

        Diary diary = Diary.builder()
                .ymd(request.getYmd())
                .content(request.getContent())
                .summary(request.getSummary())
                .build();

        Diary savedDiary = diaryRepository.save(diary);

        // Files handling
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

        diary.update(request.getContent(), request.getSummary());

        if (request.getDeletedFileIds() != null && !request.getDeletedFileIds().isEmpty()) {
            for (Long fileId : request.getDeletedFileIds()) {
                 fileMatchService.deleteMatch("diary", diary.getId(), fileId);
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
        
        fileMatchService.deleteMatchesByTarget("diary", diary.getId());
        diaryRepository.delete(diary);
    }
}
