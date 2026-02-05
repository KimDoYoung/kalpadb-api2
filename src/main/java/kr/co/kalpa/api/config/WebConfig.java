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

    @Value("${apnode.file.base-dir}")
    private String apNodeBaseDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /files/images/** to editor-images-dir
        String absolutePath = Paths.get(editorImagesDir).toAbsolutePath().toString();
        log.info("Configuring image resource handler - URL: /files/images/**, Path: {}", absolutePath);

        registry.addResourceHandler("/files/images/**")
                .addResourceLocations("file:" + absolutePath + "/")
                .setCachePeriod(3600);  // Cache for 1 hour

        // Map /apnodes/** to apnode-file-base-dir
        String apNodeAbsolutePath = Paths.get(apNodeBaseDir).toAbsolutePath().toString();
        log.info("Configuring apnode resource handler - URL: /apnodes/**, Path: {}", apNodeAbsolutePath);

        registry.addResourceHandler("/apnodes/**")
                 .addResourceLocations("file:" + apNodeAbsolutePath + "/")
                 .setCachePeriod(3600);
    }
}
