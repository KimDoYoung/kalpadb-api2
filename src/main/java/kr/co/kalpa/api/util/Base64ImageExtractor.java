package kr.co.kalpa.api.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Base64ImageExtractor {

    private static final Pattern BASE64_IMAGE_PATTERN = Pattern.compile(
        "<img[^>]*src=[\"']data:image/([^;]+);base64,([^\"']+)[\"'][^>]*>",
        Pattern.CASE_INSENSITIVE
    );

    public List<Base64ImageInfo> extractBase64Images(String htmlContent) {
        List<Base64ImageInfo> images = new ArrayList<>();

        if (htmlContent == null || htmlContent.isEmpty()) {
            return images;
        }

        Matcher matcher = BASE64_IMAGE_PATTERN.matcher(htmlContent);

        while (matcher.find()) {
            String fullTag = matcher.group(0);
            String format = matcher.group(1);
            String base64Data = matcher.group(2);

            images.add(new Base64ImageInfo(fullTag, format, base64Data));
        }

        return images;
    }

    @Getter
    @AllArgsConstructor
    public static class Base64ImageInfo {
        private String originalTag;
        private String format;
        private String base64Data;
    }
}
