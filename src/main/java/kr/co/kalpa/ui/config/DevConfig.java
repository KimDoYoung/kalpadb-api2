package kr.co.kalpa.ui.config;

import java.io.File;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;

/**
 * 개발 환경에서 Thymeleaf 템플릿 파일 시스템 로딩 설정
 * src/main/resources/templates/ 에서 직접 파일을 읽어 hot reload 지원
 */
@Configuration
@Profile("dev")
public class DevConfig {

    public DevConfig(SpringTemplateEngine templateEngine) {
        // 프로젝트 루트 찾기 (gradlew 파일이 있는 위치)
        String projectRoot = findProjectRoot();
        String templatesPath = projectRoot + File.separator + "src" + File.separator + "main"
                             + File.separator + "resources" + File.separator + "templates" + File.separator;

        // FileTemplateResolver 생성 및 설정
        FileTemplateResolver fileTemplateResolver = new FileTemplateResolver();
        fileTemplateResolver.setPrefix(templatesPath);
        fileTemplateResolver.setSuffix(".html");
        fileTemplateResolver.setTemplateMode("HTML");
        fileTemplateResolver.setCharacterEncoding("UTF-8");
        fileTemplateResolver.setCacheable(false);  // 캐싱 비활성화
        fileTemplateResolver.setOrder(0);  // 최우선 순위

        // 기존 resolver들에 우선하여 등록
        templateEngine.addTemplateResolver(fileTemplateResolver);
    }

    /**
     * 프로젝트 루트 디렉토리 찾기
     * gradlew 또는 pom.xml 파일이 있는 위치를 찾아 반환
     */
    private String findProjectRoot() {
        String currentPath = System.getProperty("user.dir");
        File current = new File(currentPath);

        while (current != null) {
            if (new File(current, "gradlew").exists() || new File(current, "pom.xml").exists()) {
                return current.getAbsolutePath();
            }
            current = current.getParentFile();
        }

        // gradlew/pom.xml이 없으면 현재 디렉토리 반환
        return currentPath;
    }
}
