package com.astra.config;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Configuration;

import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class MediaResourceConfig
        implements WebMvcConfigurer {

    @Value("${astra.media.upload-dir:uploads}")
    private String uploadDirectory;

    @Override
    public void addResourceHandlers(
            ResourceHandlerRegistry registry
    ) {

        String location =
                "file:"
                        + uploadDirectory
                        + "/";

        registry.addResourceHandler(
                        "/uploads/**"
                )
                .addResourceLocations(
                        location
                );
    }
}