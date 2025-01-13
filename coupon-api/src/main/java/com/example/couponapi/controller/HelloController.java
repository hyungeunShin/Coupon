package com.example.couponapi.controller;

import com.example.couponcore.dto.CouponRedisEntity;
import com.example.couponcore.service.CouponCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HelloController {
    private final CouponCacheService service;

    private final CacheManager cacheManager;

    @Autowired
    public HelloController(CouponCacheService service, @Qualifier("localCacheManager") CacheManager cacheManager) {
        this.service = service;
        this.cacheManager = cacheManager;
    }

    @GetMapping("/hello")
    public String hello() {
        return "hello!";
    }

    @GetMapping("/get")
    public void a() {
        CouponRedisEntity couponLocalCache = service.getCouponLocalCache(1L);
        log.info("{}", couponLocalCache);
    }

    @GetMapping("/update")
    public void b() {
        CouponRedisEntity couponCache = service.putCouponCache(1L);
        log.info("{}", couponCache);
        Cache cache = cacheManager.getCache("coupon");
        log.info("{}", cache);
        CouponRedisEntity couponLocalCache = service.putCouponLocalCache(1L);
        log.info("{}", couponLocalCache);
        Cache cache2 = cacheManager.getCache("coupon");
        log.info("{}", cache2);
    }
}
