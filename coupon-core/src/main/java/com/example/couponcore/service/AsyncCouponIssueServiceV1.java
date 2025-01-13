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

    private final ObjectMapper mapper = new ObjectMapper();

    @Transactional
    public void issue(Long couponId, Long userId) {
        CouponRedisEntity coupon = couponCacheService.getCouponCache(couponId);
        coupon.checkIssuableCoupon();

        /*
        1. totalQuantity > redisRepository.sCard(key); //쿠폰 수량 검증
        2. !redisRepository.sIsMember(key, String.valueOf(userId)); //중복 발급 검증
        3. redisRepository.sAdd //쿠폰 발급 요청 저장
        4. redisRepository.rPush //쿠폰 발급 큐 적재

        distributeLockExecutor.execute의 성능을 redis의 script를 활용해서 향상
        */
        distributeLockExecutor.execute("lock_%s".formatted(couponId), 3000, 3000, () -> {
            couponIssueRedisService.checkCouponIssueQuantity(coupon, userId);
            issueRequest(couponId, userId);
        });
    }

    private void issueRequest(Long couponId, Long userId) {
        try {
            redisRepository.sAdd(CouponRedisUtil.getIssueRequestKey(couponId), String.valueOf(userId));
            CouponIssueRequest issueRequest = new CouponIssueRequest(couponId, userId);
            redisRepository.rPush(CouponRedisUtil.getIssueRequestQueueKey(), mapper.writeValueAsString(issueRequest));
        } catch(JsonProcessingException e) {
            throw new CouponIssueException(ErrorCode.FAIL_COUPON_ISSUE_REQUEST, "JSON 파싱 에러");
        }
    }
}
