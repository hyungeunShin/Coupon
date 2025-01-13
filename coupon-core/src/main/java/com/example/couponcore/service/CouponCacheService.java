package com.example.couponcore.service;

import com.example.couponcore.dto.CouponRedisEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponCacheService {
    private final CouponIssueService couponIssueService;

    @Cacheable(cacheNames = "coupon")
    public CouponRedisEntity getCouponCache(Long couponId) {
        //매번 redis에서 캐시 데이터를 받아오는 구조
        return new CouponRedisEntity(couponIssueService.findCoupon(couponId));
    }

    @Cacheable(cacheNames = "coupon", cacheManager = "localCacheManager")
    public CouponRedisEntity getCouponLocalCache(Long couponId) {
        //local에 캐시가 있으면 받아오고 없으면 redis에서 받아온다
        return proxy().getCouponCache(couponId);
    }

    @CachePut(cacheNames = "coupon")
    public CouponRedisEntity putCouponCache(Long couponId) {
        return getCouponCache(couponId);
    }

    @CachePut(cacheNames = "coupon")
    public CouponRedisEntity putCouponLocalCache(Long couponId) {
        return getCouponLocalCache(couponId);
    }

    private CouponCacheService proxy() {
        //@EnableAspectJAutoProxy(exposeProxy = true) 추가
        return (CouponCacheService) AopContext.currentProxy();
    }
}
