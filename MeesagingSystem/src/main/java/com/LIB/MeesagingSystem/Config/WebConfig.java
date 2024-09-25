package com.LIB.MeesagingSystem.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//@Configuration
//public class WebConfig implements WebMvcConfigurer {
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedOrigins("http://localhost:3000","http://10.1.22.92:3000") // Replace with your actual frontend URL
//                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
//                .allowedHeaders("*")
//                .allowCredentials(true); // Optional: if you need credentials (e.g., cookies or HTTP authentication)
//    }

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://10.1.22.92:3000","http://10.1.22.92:3001", "http://localhost:3000", "http://10.1.22.176:3000") // Specify the exact origin
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // Explicitly list allowed methods
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}





