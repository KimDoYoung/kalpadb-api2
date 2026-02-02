package kr.co.kalpa.api.entity;

/**
 * File type enum for file_match table
 */
public enum FileType {
    ATTACHMENT,      // 일반 첨부파일
    THUMBNAIL,       // 썸네일
    PROFILE_IMAGE,   // 프로필 이미지
    EDITOR_IMAGE     // 에디터 이미지 (Quill 등)
}
