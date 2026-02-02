package kr.co.kalpa.api.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Base64ImageExtractorTest {

    private final Base64ImageExtractor extractor = new Base64ImageExtractor();

    @Test
    @DisplayName("Base64 이미지 추출 - 단일 이미지")
    void testExtractSingleBase64Image() {
        // Given
        String content = "<p>테스트 이미지</p><img src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUA\" />";

        // When
        List<Base64ImageExtractor.Base64ImageInfo> images = extractor.extractBase64Images(content);

        // Then
        assertEquals(1, images.size());
        assertEquals("png", images.get(0).getFormat());
        assertEquals("iVBORw0KGgoAAAANSUhEUgAAAAUA", images.get(0).getBase64Data());
        assertTrue(images.get(0).getOriginalTag().contains("data:image/png;base64"));
    }

    @Test
    @DisplayName("Base64 이미지 추출 - 다중 이미지")
    void testExtractMultipleBase64Images() {
        // Given
        String content = "<img src=\"data:image/png;base64,abc123\" />" +
                "<p>텍스트</p>" +
                "<img src=\"data:image/jpeg;base64,def456\" />";

        // When
        List<Base64ImageExtractor.Base64ImageInfo> images = extractor.extractBase64Images(content);

        // Then
        assertEquals(2, images.size());
        assertEquals("png", images.get(0).getFormat());
        assertEquals("jpeg", images.get(1).getFormat());
        assertEquals("abc123", images.get(0).getBase64Data());
        assertEquals("def456", images.get(1).getBase64Data());
    }

    @Test
    @DisplayName("Base64 이미지 추출 - Base64 없음")
    void testExtractNoBase64Images() {
        // Given
        String content = "<p>일반 텍스트 <img src=\"https://example.com/image.png\" /></p>";

        // When
        List<Base64ImageExtractor.Base64ImageInfo> images = extractor.extractBase64Images(content);

        // Then
        assertEquals(0, images.size());
    }

    @Test
    @DisplayName("Base64 이미지 추출 - null 입력")
    void testExtractNullContent() {
        // When
        List<Base64ImageExtractor.Base64ImageInfo> images = extractor.extractBase64Images(null);

        // Then
        assertEquals(0, images.size());
    }

    @Test
    @DisplayName("Base64 이미지 추출 - 여러 태그 속성")
    void testExtractWithMultipleAttributes() {
        // Given
        String content = "<img class=\"editor-image\" width=\"300\" src=\"data:image/gif;base64,xyz789\" alt=\"test\" />";

        // When
        List<Base64ImageExtractor.Base64ImageInfo> images = extractor.extractBase64Images(content);

        // Then
        assertEquals(1, images.size());
        assertEquals("gif", images.get(0).getFormat());
        assertEquals("xyz789", images.get(0).getBase64Data());
    }

    @Test
    @DisplayName("Base64 이미지 추출 - 싱글 따옴표")
    void testExtractWithSingleQuotes() {
        // Given
        String content = "<img src='data:image/webp;base64,test123' />";

        // When
        List<Base64ImageExtractor.Base64ImageInfo> images = extractor.extractBase64Images(content);

        // Then
        assertEquals(1, images.size());
        assertEquals("webp", images.get(0).getFormat());
        assertEquals("test123", images.get(0).getBase64Data());
    }
}
