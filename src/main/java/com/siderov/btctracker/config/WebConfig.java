package com.siderov.btctracker.config;

import com.siderov.btctracker.interceptor.SeedInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final SeedInterceptor seedInterceptor;

    @Autowired
    public WebConfig(SeedInterceptor seedInterceptor) {
        this.seedInterceptor = seedInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(seedInterceptor)
                .excludePathPatterns(
                        "/",
                        "/setup",
                        "/restore",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/qr-code",
                        "/send",
                        "/api/**" ,
                        "/icons/**"       // ← allow all AJAX endpoints
                );
    }
}
