package org.example.estudebackendspring.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Jackson configuration to handle Java Time and Hibernate lazy loading
 */
@Configuration(proxyBeanMethods = false)
public class JacksonConfig {

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void setUp() {
        // Register Java Time module for LocalDateTime, LocalDate, etc.
        objectMapper.registerModule(new JavaTimeModule());
        
        // Register Hibernate5 module to handle lazy-loaded entities
        Hibernate5JakartaModule hibernate5Module = new Hibernate5JakartaModule();
        
        // Configure to not force lazy loading - prevents "no session" errors
        hibernate5Module.disable(Hibernate5JakartaModule.Feature.FORCE_LAZY_LOADING);
        
        // Serialize lazy-loaded proxies as null/empty instead of throwing exception
        hibernate5Module.enable(Hibernate5JakartaModule.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS);
        
        objectMapper.registerModule(hibernate5Module);
    }
}

