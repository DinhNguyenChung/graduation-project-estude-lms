package org.example.estudebackendspring.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Spring Cache Configuration
 * Sử dụng in-memory caching để tối ưu performance
 * 
 * Có thể nâng cấp lên Redis hoặc Caffeine trong tương lai
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
            // Cache cho question bank summary (TTL ngắn vì hay thay đổi)
            new ConcurrentMapCache("questionBankSummary"),
            
            // Cache cho question bank full details
            new ConcurrentMapCache("questionBankFull"),
            
            // Cache cho question by topic
            new ConcurrentMapCache("questionBankByTopic"),
            
            // Cache cho question by subject
            new ConcurrentMapCache("questionBankBySubject"),
            
            // Cache cho question by subject and grade
            new ConcurrentMapCache("questionBankBySubjectGrade"),
            
            // Cache cho question by grade
            new ConcurrentMapCache("questionBankByGrade"),
            
            // Cache cho single question detail (TTL dài hơn)
            new ConcurrentMapCache("questionDetail"),
            
            // ===== Analytics Caches =====
            
            // Cache cho question bank statistics overview
            new ConcurrentMapCache("questionBankStats"),
            
            // Cache cho question usage ranking
            new ConcurrentMapCache("questionUsageRanking"),
            
            // Cache cho questions needing improvement
            new ConcurrentMapCache("questionsNeedingImprovement")
        ));
        return cacheManager;
    }
}
