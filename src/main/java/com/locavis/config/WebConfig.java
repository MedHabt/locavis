package com.locavis.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry){
        // on est ouvert sur tous les orgines (dans production on va pas le laissé comme ça)
        corsRegistry.addMapping("/**")
                .allowedOrigins("http://localhost:4200/*")//.allowedOriginPatterns("*")//
                .allowedMethods("*")
                .maxAge(360L)
                .allowedHeaders("*")
                .exposedHeaders("Authorization") // pour bearer token
                .allowCredentials(true);
    }
}
