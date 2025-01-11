package com.example.couponcore.service;

import com.example.couponcore.component.DistributeLockExecutor;
import com.example.couponcore.dto.CouponIssueRequest;
import com.example.couponcore.dto.CouponRedisEntity;
import com.example.couponcore.exception.CouponIssueException;
import com.example.couponcore.exception.ErrorCode;
import com.example.couponcore.repository.redis.RedisRepository;
import com.example.couponcore.util.CouponRedisUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AsyncCouponIssueServiceV1 {
    private final RedisRepository redisRepository;

    private final CouponIssueRedisService couponIssueRedisService;

    private final DistributeLockExecutor distributeLockExecutor;

    private final CouponCacheService couponCacheService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void issue(Long couponId, Long userId) {
        CouponRedisEntity coupon = couponCacheService.getCouponCache(couponId);
        coupon.checkIssuableCoupon();

        distributeLockExecutor.execute("lock_%s".formatted(couponId), 3000, 3000, () -> {
            couponIssueRedisService.checkCouponIssueQuantity(coupon, userId);
            issueRequest(couponId, userId);
        });
    }

    private void issueRequest(Long couponId, Long userId) {
        try {
            redisRepository.sAdd(CouponRedisUtil.getIssueRequestKey(couponId), String.valueOf(userId));
            CouponIssueRequest issueRequest = new CouponIssueRequest(couponId, userId);
            redisRepository.rPush(CouponRedisUtil.getIssueRequestQueueKey(), objectMapper.writeValueAsString(issueRequest));
        } catch(JsonProcessingException e) {
            throw new CouponIssueException(ErrorCode.FAIL_COUPON_ISSUE_REQUEST, "JSON 파싱 에러");
        }
    }
}
