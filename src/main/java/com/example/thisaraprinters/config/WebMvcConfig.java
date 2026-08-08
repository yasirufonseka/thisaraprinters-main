package com.example.thisaraprinters.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded artwork files from the external uploads/ directory
        // Files are saved to uploads/artwork/<filename>
        // Accessible via URL  /artwork-uploads/<filename>
        String uploadPath = Paths.get("uploads/artwork/").toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/artwork-uploads/**")
                .addResourceLocations(uploadPath);
    }

   
}
