package com.banking.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 프론트엔드(모바일 앱 / Expo Web) 로컬 개발 시 CORS 허용.
 *
 * 네이티브 RN 빌드는 브라우저가 아니므로 CORS 무관.
 * 브라우저 컨텍스트(Expo Web, 디버거, curl --origin) 대응용.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(
                        "http://localhost:*",
                        "http://127.0.0.1:*",
                        "http://192.168.*:*"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
