//package com.example.inventory_factory_management.config;
//
//import io.github.cdimascio.dotenv.Dotenv;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.env.ConfigurableEnvironment;
//import org.springframework.core.env.MapPropertySource;
//import org.springframework.context.annotation.Bean;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Configuration
//public class EnvConfig {
//
//    @Bean
//    public Dotenv dotenv() {
//        return Dotenv.configure()
//                .directory("C:/SpringBoot_Projects/inventory_factory_management")
//                .ignoreIfMalformed()
//                .ignoreIfMissing()
//                .load();
//    }
//
//    @Bean
//    public MapPropertySource dotenvPropertySource(Dotenv dotenv) {
//        Map<String, Object> properties = new HashMap<>();
//        dotenv.entries().forEach(entry -> {
//            properties.put(entry.getKey(), entry.getValue());
//        });
//        return new MapPropertySource("dotenvProperties", properties);
//    }
//}