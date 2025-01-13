package com.example.couponcore.service;

import com.example.couponcore.dto.CouponRedisEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponCacheService {
    private final CouponIssueService couponIssueService;

    @Cacheable(cacheNames = "coupon")
    public CouponRedisEntity getCouponCache(Long couponId) {
        log.info("getCouponCache");
        //매번 redis에서 캐시 데이터를 받아오는 구조
        return new CouponRedisEntity(couponIssueService.findCoupon(couponId));
    }

    @Cacheable(cacheNames = "coupon", cacheManager = "localCacheManager")
    public CouponRedisEntity getCouponLocalCache(Long couponId) {
        log.info("getCouponLocalCache");
        //local에 캐시가 있으면 받아오고 없으면 redis에서 받아온다
        return proxy().getCouponCache(couponId);
    }

    /*
     @Cacheable
        캐시가 이미 있으면 메서드 실행을 건너뛰어야 하므로 proxy()를 통해 호출해야 한다.
        그렇지 않으면 캐시가 이미 존재하는 경우에도 메서드가 다시 실행된다.

     @CachePut
        메서드 호출 후 결과를 캐시에 저장하므로, 캐시가 이미 존재하든 말든 메서드가 항상 실행된다.
        따라서 putCouponCache와 putCouponLocalCache에서 프록시를 통해 호출하지 않아도 캐시 갱신이 이루어진다.
    */
    @CachePut(cacheNames = "coupon")
    public CouponRedisEntity putCouponCache(Long couponId) {
        log.info("putCouponCache");
        return getCouponCache(couponId);
    }

    @CachePut(cacheNames = "coupon")
    public CouponRedisEntity putCouponLocalCache(Long couponId) {
        log.info("putCouponLocalCache");
        return getCouponLocalCache(couponId);
    }

    private CouponCacheService proxy() {
        //@EnableAspectJAutoProxy(exposeProxy = true) 추가
        return (CouponCacheService) AopContext.currentProxy();
    }
}
