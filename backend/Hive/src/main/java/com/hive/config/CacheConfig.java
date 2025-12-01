package com.hive.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                "usersById",
                "usersByUsername",
                "threadById",
                "threadList",
                "threadPage",
                "eventById",
                "getUserEvents",
                "getParticipants"
        );
    }
}
