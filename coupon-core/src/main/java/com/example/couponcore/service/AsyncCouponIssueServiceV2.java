package com.example.couponcore.service;

import com.example.couponcore.dto.CouponRedisEntity;
import com.example.couponcore.repository.redis.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AsyncCouponIssueServiceV2 {
    private final RedisRepository redisRepository;

    private final CouponCacheService couponCacheService;

    @Transactional
    public void issue(Long couponId, Long userId) {
//        CouponRedisEntity coupon = couponCacheService.getCouponCache(couponId);
        CouponRedisEntity coupon = couponCacheService.getCouponLocalCache(couponId);
        coupon.checkIssuableCoupon();

        //redis는 기본적으로 single thread이기 때문에 redis script를 활용하면 동시서을 보장받을 수 있음
        issueRequest(couponId, userId, coupon.totalQuantity());
    }

    private void issueRequest(Long couponId, Long userId, Integer totalIssueQuantity) {
        if(totalIssueQuantity == null) {
            redisRepository.issueRequest(couponId, userId, Integer.MAX_VALUE);
        }
        redisRepository.issueRequest(couponId, userId, totalIssueQuantity);
    }
}
