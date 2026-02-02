package kr.co.kalpa.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.editor-images-dir}")
    private String editorImagesDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /files/images/** to editor-images-dir
        String absolutePath = Paths.get(editorImagesDir).toAbsolutePath().toString();
        log.info("Configuring image resource handler - URL: /files/images/**, Path: {}", absolutePath);

        registry.addResourceHandler("/files/images/**")
                .addResourceLocations("file:" + absolutePath + "/")
                .setCachePeriod(3600);  // Cache for 1 hour
    }
}
