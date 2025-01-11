package com.example.couponcore.service;

import com.example.couponcore.dto.CouponRedisEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponCacheService {
    private final CouponIssueService couponIssueService;

    @Cacheable(cacheNames = "coupon")
    public CouponRedisEntity getCouponCache(Long couponId) {
        return new CouponRedisEntity(couponIssueService.findCoupon(couponId));
    }
}
